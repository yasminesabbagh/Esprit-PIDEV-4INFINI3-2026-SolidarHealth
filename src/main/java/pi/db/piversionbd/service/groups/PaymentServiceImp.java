package pi.db.piversionbd.service.groups;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pi.db.piversionbd.entities.groups.Group;
import pi.db.piversionbd.entities.groups.GroupPool;
import pi.db.piversionbd.entities.groups.Membership;
import pi.db.piversionbd.entities.groups.Payment;
import pi.db.piversionbd.exception.ResourceNotFoundException;
import pi.db.piversionbd.repository.groups.GroupPoolRepository;
import pi.db.piversionbd.repository.groups.PaymentRepository;
import pi.db.piversionbd.repository.groups.MembershipRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImp implements IPaymentService {

    private final PaymentRepository paymentRepository;
    private final GroupPoolRepository groupPoolRepository;
    private final MembershipRepository membershipRepository;
    private final IMembershipService membershipService;

    @Override
    public Membership recordSuccessfulPayment(Long membershipId, Float amount, Float poolAllocation, Float platformFee, Float nationalFund) {
        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found with id " + membershipId));
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        // Create payment record
        Payment payment = new Payment();
        payment.setMember(membership.getMember());
        payment.setGroup(membership.getGroup());
        payment.setAmount(amount);
        payment.setPoolAllocation(poolAllocation != null ? poolAllocation : 0f);
        payment.setPlatformFee(platformFee != null ? platformFee : 0f);
        payment.setNationalFund(nationalFund != null ? nationalFund : 0f);
        payment = paymentRepository.save(payment);

        // Update group pool: get or create pool for this group
        Group group = membership.getGroup();
        GroupPool pool = groupPoolRepository.findByGroup_Id(group.getId())
                .orElseGet(() -> {
                    GroupPool p = new GroupPool();
                    p.setGroup(group);
                    p.setPoolBalance(0f);
                    p.setTotalContributions(0f);
                    p.setTotalPaidOut(0f);
                    return groupPoolRepository.save(p);
                });
        float addToPool = payment.getPoolAllocation() != null ? payment.getPoolAllocation() : 0f;
        pool.setPoolBalance((pool.getPoolBalance() != null ? pool.getPoolBalance() : 0f) + addToPool);
        pool.setTotalContributions((pool.getTotalContributions() != null ? pool.getTotalContributions() : 0f) + addToPool);
        groupPoolRepository.save(pool);

        // Set membership to active
        Membership updated = membershipService.updateMembershipStatus(membershipId, Membership.STATUS_ACTIVE);

        return updated;
    }

    private static final float POOL_RATIO = 0.7f;
    private static final float PLATFORM_RATIO = 0.2f;
    private static final float NATIONAL_RATIO = 0.1f;
    /** Tolerance for amount vs monthly amount (e.g. rounding). */
    private static final float AMOUNT_TOLERANCE = 0.01f;

    @Override
    public Payment processMonthlyPayment(Long memberId, Long groupId, Float amount) {
        if (memberId == null || groupId == null) {
            throw new IllegalArgumentException("memberId and groupId are required");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        Membership membership = membershipRepository
                .findByMember_IdAndGroup_IdAndEndedAtIsNull(memberId, groupId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active membership found for member " + memberId + " in group " + groupId));
        Float monthlyAmount = membership.getMonthlyAmount();
        if (monthlyAmount != null && Math.abs(amount - monthlyAmount) > AMOUNT_TOLERANCE) {
            throw new IllegalArgumentException(
                    "Amount " + amount + " does not match membership monthly amount " + monthlyAmount);
        }
        float poolAllocation = amount * POOL_RATIO;
        float platformFee = amount * PLATFORM_RATIO;
        float nationalFund = amount * NATIONAL_RATIO;

        Payment payment = new Payment();
        payment.setMember(membership.getMember());
        payment.setGroup(membership.getGroup());
        payment.setAmount(amount);
        payment.setPoolAllocation(poolAllocation);
        payment.setPlatformFee(platformFee);
        payment.setNationalFund(nationalFund);
        payment = paymentRepository.save(payment);

        Group group = membership.getGroup();
        GroupPool pool = groupPoolRepository.findByGroup_Id(group.getId())
                .orElseGet(() -> {
                    GroupPool p = new GroupPool();
                    p.setGroup(group);
                    p.setPoolBalance(0f);
                    p.setTotalContributions(0f);
                    p.setTotalPaidOut(0f);
                    return groupPoolRepository.save(p);
                });
        pool.setPoolBalance((pool.getPoolBalance() != null ? pool.getPoolBalance() : 0f) + poolAllocation);
        pool.setTotalContributions((pool.getTotalContributions() != null ? pool.getTotalContributions() : 0f) + poolAllocation);
        groupPoolRepository.save(pool);

        if (Membership.STATUS_PENDING.equals(membership.getStatus())) {
            membershipService.updateMembershipStatus(membership.getId(), Membership.STATUS_ACTIVE);
        }
        return payment;
    }

    @Override
    public List<Payment> getPaymentHistory(Long memberId, Long groupId) {
        if (memberId == null) {
            throw new IllegalArgumentException("memberId is required");
        }
        if (groupId != null) {
            return paymentRepository.findByMember_IdAndGroup_IdOrderByIdDesc(memberId, groupId);
        }
        return paymentRepository.findByMember_IdOrderByIdDesc(memberId);
    }
}
