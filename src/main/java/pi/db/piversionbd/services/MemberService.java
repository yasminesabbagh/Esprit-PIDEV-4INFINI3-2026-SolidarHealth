package pi.db.piversionbd.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pi.db.piversionbd.entities.groups.Member;
import pi.db.piversionbd.repositories.MemberRepository;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class MemberService {
    private static final Logger log = LoggerFactory.getLogger(MemberService.class);
    private static final int MAX_FAILED_ATTEMPTS = 3;

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    // Register member (email + password + cinNumber)
    @Transactional
    public Member register(String email, String rawPassword, String cinNumber) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email requis");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Mot de passe requis");
        }
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }
        Member m = new Member();
        m.setEmail(email);
        m.setPassword(passwordEncoder.encode(rawPassword));
        m.setEnabled(true);
        m.setCreatedAt(LocalDateTime.now());
        // cinNumber est NOT NULL: utiliser celui fourni ou en générer un
        String cin = (cinNumber != null && !cinNumber.isBlank()) ? cinNumber : generateCinNumber();
        m.setCinNumber(cin);
        return memberRepository.save(m);
    }

    // Login member with email + password
    public Member login(String email, String rawPassword) {
        Member m = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        if (Boolean.FALSE.equals(m.getEnabled())) {
            throw new IllegalStateException("Compte désactivé");
        }
        if (m.getLockedAt() != null) {
            throw new IllegalStateException("Compte bloqué après tentatives échouées");
        }
        if (!passwordEncoder.matches(rawPassword, m.getPassword())) {
            int attempts = m.getFailedLoginAttempts() == null ? 0 : m.getFailedLoginAttempts();
            attempts++;
            m.setFailedLoginAttempts(attempts);
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                m.setLockedAt(LocalDateTime.now());
            }
            memberRepository.save(m);
            throw new IllegalArgumentException("Mot de passe invalide");
        }
        m.setFailedLoginAttempts(0);
        m.setLastLogin(LocalDateTime.now());
        return memberRepository.save(m);
    }

    @Transactional
    public void resetPassword(String email) {
        Member m = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email introuvable"));
        String newPassword = generateSecurePassword();
        m.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(m);
        sendResetPasswordEmail(m, newPassword);
    }

    private String generateSecurePassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String generateCinNumber() {
        // Génère un CIN pseudo-unique: "CIN-" + 10 chars aléatoires
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[8];
        random.nextBytes(bytes);
        return "CIN-" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void sendResetPasswordEmail(Member m, String plaintextPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(m.getEmail());
            message.setSubject("Réinitialisation de mot de passe - Compte Membre");
            message.setText("Bonjour,\n\nVotre mot de passe a été réinitialisé. Nouveau mot de passe:\n\n" + plaintextPassword + "\n\nChangez-le dès votre prochaine connexion.\n");
            mailSender.send(message);
            log.info("Email reset password envoyé à {}", m.getEmail());
        } catch (Exception e) {
            log.error("Échec d'envoi de l'email reset password à {}: {}", m.getEmail(), e.getMessage(), e);
        }
    }

    public java.util.Map<String, Long> dashboardStatsForMembers() {
        java.util.Map<String, Long> stats = new java.util.HashMap<>();
        long total = memberRepository.count();
        long blocked = memberRepository.countByEnabledFalse();
        long locked = memberRepository.countByLockedAtNotNull();
        LocalDate today = LocalDate.now();
        long newToday = memberRepository.countByCreatedAtBetween(today.atStartOfDay(), today.atTime(LocalTime.MAX));
        stats.put("totalMembers", total);
        stats.put("blockedMembers", blocked);
        stats.put("lockedMembers", locked);
        stats.put("newMembersToday", newToday);
        return stats;
    }
}

