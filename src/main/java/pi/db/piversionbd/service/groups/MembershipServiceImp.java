package pi.db.piversionbd.service.groups;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pi.db.piversionbd.entities.groups.Group;
import pi.db.piversionbd.entities.groups.Member;
import pi.db.piversionbd.entities.groups.Membership;
import pi.db.piversionbd.exception.ResourceNotFoundException;
import pi.db.piversionbd.repository.groups.GroupRepository;
import pi.db.piversionbd.repository.groups.MemberRepository;
import pi.db.piversionbd.repository.groups.MembershipRepository;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MembershipServiceImp implements IMembershipService {

    private static final String DEFAULT_PACKAGE = "BASIC";

    private static final int CONSULTATIONS_BASIC = 4;
    private static final float ANNUAL_LIMIT_BASIC = 500f;
    private static final int CONSULTATIONS_CONFORT = 6;
    private static final float ANNUAL_LIMIT_CONFORT = 750f;
    private static final int CONSULTATIONS_PREMIUM = 10;
    private static final float ANNUAL_LIMIT_PREMIUM = 1000f;

    private final MembershipRepository membershipRepository;
    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;

    @Override
    public Membership addMemberToGroup(Long groupId, Long memberId, String packageType) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id " + groupId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id " + memberId));

        if (membershipRepository.findByMember_IdAndGroup_IdAndEndedAtIsNull(memberId, groupId).isPresent()) {
            throw new IllegalArgumentException("Member " + memberId + " is already in group " + groupId);
        }

        String pkg = packageType != null && !packageType.isBlank() ? packageType.trim().toUpperCase() : DEFAULT_PACKAGE;
        Float monthlyAmount = getMonthlyAmountForPackage(member, pkg);
        int consultationsLimit = getConsultationsLimitForPackage(pkg);
        float annualLimit = getAnnualLimitForPackage(pkg);

        Membership m = new Membership();
        m.setMember(member);
        m.setGroup(group);
        m.setPackageType(pkg);
        m.setMonthlyAmount(monthlyAmount);
        m.setConsultationsLimit(consultationsLimit);
        m.setAnnualLimit(annualLimit);
        m.setStatus(Membership.STATUS_PENDING);
        m.setEndedAt(null);
        Membership saved = membershipRepository.save(m);

        member.setCurrentGroup(group);
        memberRepository.save(member);

        int count = group.getCurrentMemberCount() != null ? group.getCurrentMemberCount() : 0;
        group.setCurrentMemberCount(count + 1);
        groupRepository.save(group);

        return saved;
    }

    @Override
    public void removeMemberFromGroup(Long groupId, Long memberId) {
        Membership membership = membershipRepository
                .findByMember_IdAndGroup_IdAndEndedAtIsNull(memberId, groupId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active membership found for member " + memberId + " in group " + groupId));

        membership.setStatus(Membership.STATUS_CANCELLED);
        membership.setEndedAt(Instant.now());
        membershipRepository.save(membership);

        Member member = membership.getMember();
        if (member.getCurrentGroup() != null && member.getCurrentGroup().getId().equals(groupId)) {
            member.setCurrentGroup(null);
            memberRepository.save(member);
        }

        Group group = membership.getGroup();
        int count = group.getCurrentMemberCount() != null ? group.getCurrentMemberCount() : 0;
        if (count > 0) {
            group.setCurrentMemberCount(count - 1);
            groupRepository.save(group);
        }
    }

    @Override
    public List<Group> getGroupsForMember(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member not found with id " + memberId);
        }
        return membershipRepository.findByMember_IdAndStatusAndEndedAtIsNull(memberId, Membership.STATUS_ACTIVE).stream()
                .map(Membership::getGroup)
                .collect(Collectors.toList());
    }

    /** Monthly amount from member's price for the given package (price_basic/price_confort/price_premium), fallback to personalized_monthly_price. */
    private static float getMonthlyAmountForPackage(Member member, String pkg) {
        Float price = null;
        if ("BASIC".equalsIgnoreCase(pkg) && member.getPriceBasic() != null) price = member.getPriceBasic();
        else if ("CONFORT".equalsIgnoreCase(pkg) && member.getPriceConfort() != null) price = member.getPriceConfort();
        else if ("PREMIUM".equalsIgnoreCase(pkg) && member.getPricePremium() != null) price = member.getPricePremium();
        if (price == null) price = member.getPersonalizedMonthlyPrice();
        return price != null ? price : 0f;
    }

    private static int getConsultationsLimitForPackage(String pkg) {
        if ("CONFORT".equalsIgnoreCase(pkg)) return CONSULTATIONS_CONFORT;
        if ("PREMIUM".equalsIgnoreCase(pkg)) return CONSULTATIONS_PREMIUM;
        return CONSULTATIONS_BASIC;
    }

    private static float getAnnualLimitForPackage(String pkg) {
        if ("CONFORT".equalsIgnoreCase(pkg)) return ANNUAL_LIMIT_CONFORT;
        if ("PREMIUM".equalsIgnoreCase(pkg)) return ANNUAL_LIMIT_PREMIUM;
        return ANNUAL_LIMIT_BASIC;
    }

    @Override
    public List<Membership> getMembershipsForMember(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member not found with id " + memberId);
        }
        return membershipRepository.findByMember_IdOrderByIdDesc(memberId);
    }

    @Override
    public Membership updateMembershipStatus(Long membershipId, String status) {
        Membership m = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found with id " + membershipId));
        if (status == null || !List.of(Membership.STATUS_PENDING, Membership.STATUS_ACTIVE, Membership.STATUS_SUSPENDED, Membership.STATUS_CANCELLED).contains(status)) {
            throw new IllegalArgumentException("Invalid membership status: " + status);
        }
        m.setStatus(status);
        return membershipRepository.save(m);
    }
}
