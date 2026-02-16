package pi.db.piversionbd.service.groups;

import pi.db.piversionbd.entities.groups.Membership;
import pi.db.piversionbd.entities.groups.Payment;

import java.util.List;

/**
 * On successful payment: create payment record, update group pool, set membership to active.
 * Monthly payments: verify member in group, 70/20/10 split, update pool; payment history.
 */
public interface IPaymentService {

    /**
     * Record a successful payment for a membership. Creates the payment record,
     * updates the group's pool (balance and total contributions), and sets the
     * membership status to active.
     *
     * @param membershipId the membership this payment is for (must be pending)
     * @param amount       total amount paid
     * @param poolAllocation amount added to group pool (can be null, treated as 0)
     * @param platformFee  platform fee (can be null, treated as 0)
     * @param nationalFund national fund contribution (can be null, treated as 0)
     * @return the updated membership (status set to active)
     */
    Membership recordSuccessfulPayment(Long membershipId, Float amount, Float poolAllocation, Float platformFee, Float nationalFund);

    /**
     * Process a monthly premium payment: verify member is in group, calculate 70/20/10 splits,
     * create payment record, update group pool. Amount should match membership's monthly amount.
     *
     * @return the created payment (confirmation)
     */
    Payment processMonthlyPayment(Long memberId, Long groupId, Float amount);

    /**
     * Get payment history for a member, optionally filtered by group.
     *
     * @param groupId optional; if null, returns all payments for the member
     */
    List<Payment> getPaymentHistory(Long memberId, Long groupId);
}
