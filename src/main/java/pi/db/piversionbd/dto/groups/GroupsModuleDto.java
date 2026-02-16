package pi.db.piversionbd.dto.groups;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pi.db.piversionbd.entities.groups.Group;
import pi.db.piversionbd.entities.groups.Member;
import pi.db.piversionbd.entities.groups.Membership;
import pi.db.piversionbd.entities.groups.Payment;

/**
 * Single file for all Module 1 (Groups) DTOs: groups, members, etc.
 */
public final class GroupsModuleDto {

    private GroupsModuleDto() {}

    // ----- Group -----

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(
            description = "Solidarity group. Fill each field; use placeholders below as guide.",
            example = "{\"groupId\": 1, \"name\": \"Family Solidarity 42\", \"type\": \"FAMILY\", \"region\": \"Tunis\", \"minMembers\": 5, \"maxMembers\": 15, \"currentMemberCount\": 0}"
    )
    public static class GroupDto {

        @Schema(description = "Group ID. Placeholder: 1 (read-only, omit for create)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        private Long groupId;

        @Schema(description = "Group name. Placeholder: Family Solidarity 42", example = "Family Solidarity 42", requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;

        @Schema(description = "Group type. Placeholder: FAMILY. Available values: FAMILY, STUDENTS, WORKERS, MIXED", example = "FAMILY", allowableValues = { "FAMILY", "STUDENTS", "WORKERS", "MIXED" })
        private String type;

        @Schema(description = "Region / city. Placeholder: Tunis", example = "Tunis")
        private String region;

        @Schema(description = "Minimum members. Placeholder: 5", example = "5", minimum = "5", maximum = "30")
        private Integer minMembers;

        @Schema(description = "Maximum members. Placeholder: 15", example = "15", minimum = "5", maximum = "30")
        private Integer maxMembers;

        @Schema(description = "Current member count. Placeholder: 0", example = "0")
        private Integer currentMemberCount;

        public static GroupDto fromEntity(Group g) {
            if (g == null) return null;
            return new GroupDto(
                    g.getId(), // exposed as groupId
                    g.getName(),
                    g.getType(),
                    g.getRegion(),
                    g.getMinMembers(),
                    g.getMaxMembers(),
                    g.getCurrentMemberCount()
            );
        }
    }

    // ----- Member -----

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(
            description = "Member form: after acceptance, app shows this form with pre-registration info. Member fills age, profession, region; chooses package (BASIC/CONFORT/PREMIUM) then pays.",
            example = "{\"memberId\": 1, \"cinNumber\": \"12345678\", \"age\": 28, \"profession\": \"student\", \"region\": \"Tunis\", \"personalizedMonthlyPrice\": 17.5, \"adherenceScore\": 85, \"currentGroupId\": 1}"
    )
    public static class MemberDto {

        @Schema(description = "Member ID (read-only on response; omit for create)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        private Long memberId;

        @Schema(description = "CIN number (from pre-registration, not editable on update)", example = "12345678", requiredMode = Schema.RequiredMode.REQUIRED)
        private String cinNumber;

        @Schema(description = "Age (for group suggestions / future age-range matching)", example = "28")
        private Integer age;

        @Schema(description = "Profession (e.g. student, worker) – for group type matching: students→STUDENTS, workers→WORKERS, family→FAMILY", example = "student")
        private String profession;

        @Schema(description = "Region – for group suggestions (same region)", example = "Tunis")
        private String region;

        @Schema(description = "Personalized monthly premium (DT). Placeholder: 17.5", example = "17.5")
        private Float personalizedMonthlyPrice;

        @Schema(description = "Adherence score (0-100). Placeholder: 85", example = "85", minimum = "0", maximum = "100")
        private Float adherenceScore;

        @Schema(description = "Current group ID (optional)", example = "1")
        private Long currentGroupId;

        public static MemberDto fromEntity(Member m) {
            if (m == null) return null;
            return new MemberDto(
                    m.getId(),
                    m.getCinNumber(),
                    m.getAge(),
                    m.getProfession(),
                    m.getRegion(),
                    m.getPersonalizedMonthlyPrice(),
                    m.getAdherenceScore(),
                    m.getCurrentGroup() != null ? m.getCurrentGroup().getId() : null
            );
        }
    }

    // ----- Membership -----

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Membership: links a member to a group with package and monthly amount (post-approval groups/payments)")
    public static class MembershipDto {

        @Schema(description = "Membership ID. Placeholder: 1", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        private Long membershipId;

        @Schema(description = "Member ID. Placeholder: 1", example = "1")
        private Long memberId;

        @Schema(description = "Group ID. Placeholder: 1", example = "1")
        private Long groupId;

        @Schema(description = "Package type. Placeholder: BASIC. Available values: BASIC, CONFORT, PREMIUM", example = "BASIC", allowableValues = { "BASIC", "CONFORT", "PREMIUM" })
        private String packageType;

        @Schema(description = "Monthly amount (DT). Placeholder: 17.5", example = "17.5")
        private Float monthlyAmount;

        @Schema(description = "Consultations limit. Placeholder: 4", example = "4")
        private Integer consultationsLimit;

        @Schema(description = "Annual limit. Placeholder: 500.0", example = "500.0")
        private Float annualLimit;

        @Schema(description = "Status: pending, active, suspended, cancelled. Pending until first payment.", example = "pending", allowableValues = { "pending", "active", "suspended", "cancelled" })
        private String status;

        @Schema(description = "Active (status=active and not ended). Placeholder: true", example = "true")
        private Boolean active;

        public static MembershipDto fromEntity(Membership m) {
            if (m == null) return null;
            String status = m.getStatus() != null ? m.getStatus() : Membership.STATUS_PENDING;
            boolean active = Membership.STATUS_ACTIVE.equals(status) && m.getEndedAt() == null;
            return new MembershipDto(
                    m.getId(),
                    m.getMember() != null ? m.getMember().getId() : null,
                    m.getGroup() != null ? m.getGroup().getId() : null,
                    m.getPackageType(),
                    m.getMonthlyAmount(),
                    m.getConsultationsLimit(),
                    m.getAnnualLimit(),
                    status,
                    active
            );
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Request to create a membership. Body is package type only; memberId is provided via query param or auth.")
    public static class AddMemberToGroupRequest {

        @Schema(description = "Package type. Placeholder: BASIC. Available values: BASIC, CONFORT, PREMIUM", example = "BASIC", allowableValues = { "BASIC", "CONFORT", "PREMIUM" })
        private String packageType;
    }

    // ----- Payment (successful payment flow) -----

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Record a successful payment: creates payment record, updates group pool, sets membership to active.")
    public static class RecordPaymentRequest {

        @Schema(description = "Total amount paid (DT). Placeholder: 17.5", example = "17.5", requiredMode = Schema.RequiredMode.REQUIRED)
        private Float amount;

        @Schema(description = "Amount allocated to group pool (DT). Placeholder: 15.0", example = "15.0")
        private Float poolAllocation;

        @Schema(description = "Platform fee (DT). Placeholder: 1.5", example = "1.5")
        private Float platformFee;

        @Schema(description = "National fund contribution (DT). Placeholder: 1.0", example = "1.0")
        private Float nationalFund;
    }

    // ----- Monthly payment (POST /api/payments) -----

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Process a monthly premium payment. Amount should match the membership's monthly amount.")
    public static class MonthlyPaymentRequest {

        @Schema(description = "Member ID. Placeholder: 1", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long memberId;

        @Schema(description = "Group ID. Placeholder: 1", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long groupId;

        @Schema(description = "Payment amount (DT). Should match membership monthly amount. Placeholder: 17.5", example = "17.5", requiredMode = Schema.RequiredMode.REQUIRED)
        private Float amount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Payment record / confirmation (id, splits, group pool updated).")
    public static class PaymentDto {

        @Schema(description = "Payment ID.", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        private Long paymentId;

        @Schema(description = "Member ID.", example = "1")
        private Long memberId;

        @Schema(description = "Group ID.", example = "1")
        private Long groupId;

        @Schema(description = "Total amount (DT).", example = "17.5")
        private Float amount;

        @Schema(description = "Pool allocation (70%).", example = "12.25")
        private Float poolAllocation;

        @Schema(description = "Platform fee (20%).", example = "3.5")
        private Float platformFee;

        @Schema(description = "National fund (10%).", example = "1.75")
        private Float nationalFund;

        public static PaymentDto fromEntity(Payment p) {
            if (p == null) return null;
            return new PaymentDto(
                    p.getId(),
                    p.getMember() != null ? p.getMember().getId() : null,
                    p.getGroup() != null ? p.getGroup().getId() : null,
                    p.getAmount(),
                    p.getPoolAllocation(),
                    p.getPlatformFee(),
                    p.getNationalFund()
            );
        }
    }
}
