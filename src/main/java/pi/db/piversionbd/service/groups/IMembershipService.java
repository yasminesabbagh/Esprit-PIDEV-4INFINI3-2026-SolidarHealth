package pi.db.piversionbd.service.groups;

import pi.db.piversionbd.entities.groups.Group;
import pi.db.piversionbd.entities.groups.Membership;

import java.util.List;

public interface IMembershipService {

    /**
     * Add a member to a group: create an active membership with monthly_amount from the member's
     * personalized_monthly_price (package type can affect price in the future).
     */
    Membership addMemberToGroup(Long groupId, Long memberId, String packageType);

    /**
     * End membership (soft delete). Caller is responsible for admin/self check.
     */
    void removeMemberFromGroup(Long groupId, Long memberId);

    /**
     * Groups the member currently belongs to (active memberships only; usually one).
     */
    List<Group> getGroupsForMember(Long memberId);

    /**
     * All memberships for a member (any status). Use for "groups and payments after approved" view.
     */
    List<Membership> getMembershipsForMember(Long memberId);

    /**
     * Update membership status (e.g. pending → active on first successful payment). Valid: pending, active, suspended, cancelled.
     */
    Membership updateMembershipStatus(Long membershipId, String status);
}
