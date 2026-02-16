package pi.db.piversionbd.controller.groups;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pi.db.piversionbd.dto.groups.GroupsModuleDto;
import pi.db.piversionbd.entities.groups.Membership;
import pi.db.piversionbd.entities.groups.Payment;
import pi.db.piversionbd.service.groups.IPaymentService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Module 1 – Payments", description = "Record successful payment (activate membership), process monthly premium (70/20/10), payment history.")
public class PaymentController {

    private final IPaymentService paymentService;

    @PostMapping
    @Operation(
            summary = "Process monthly premium payment",
            description = "Verify member is in group; amount should match membership's monthly amount. Splits: pool 70%, platform 20%, national fund 10%. Creates payment record and updates group pool.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Payment processed, confirmation returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request or amount does not match monthly amount", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "404", description = "Member or membership not found", content = @Content(mediaType = "text/plain", schema = @Schema(example = "No active membership found for member 1 in group 2")))
    })
    public ResponseEntity<GroupsModuleDto.PaymentDto> processMonthlyPayment(
            @RequestBody GroupsModuleDto.MonthlyPaymentRequest body) {
        if (body == null || body.getMemberId() == null || body.getGroupId() == null || body.getAmount() == null) {
            return ResponseEntity.badRequest().build();
        }
        Payment payment = paymentService.processMonthlyPayment(
                body.getMemberId(),
                body.getGroupId(),
                body.getAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body(GroupsModuleDto.PaymentDto.fromEntity(payment));
    }

    @GetMapping("/history")
    @Operation(
            summary = "Get payment history",
            description = "Returns payments for the given member. Optionally filter by groupId (query param).")
    @ApiResponse(responseCode = "200", description = "OK – list of payments (newest first)")
    public ResponseEntity<List<GroupsModuleDto.PaymentDto>> getPaymentHistory(
            @Parameter(description = "Member ID. Placeholder: 1", required = true, schema = @Schema(example = "1")) @RequestParam Long memberId,
            @Parameter(description = "Optional group ID to filter. Placeholder: 1", schema = @Schema(example = "1")) @RequestParam(required = false) Long groupId) {
        List<Payment> payments = paymentService.getPaymentHistory(memberId, groupId);
        List<GroupsModuleDto.PaymentDto> list = payments.stream()
                .map(GroupsModuleDto.PaymentDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PostMapping("/memberships/{membershipId}")
    @Operation(
            summary = "Record successful payment (activate membership)",
            description = "On successful first payment: creates the payment record, updates the group pool, and sets the membership status to active. Call this after the payment gateway confirms payment.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment recorded and membership activated"),
            @ApiResponse(responseCode = "400", description = "Invalid request (e.g. amount missing or not positive)", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "404", description = "Membership not found", content = @Content(mediaType = "text/plain", schema = @Schema(example = "Membership not found with id 99")))
    })
    public ResponseEntity<GroupsModuleDto.MembershipDto> recordPayment(
            @Parameter(description = "Membership ID. Placeholder: 1", required = true, schema = @Schema(example = "1")) @PathVariable Long membershipId,
            @RequestBody GroupsModuleDto.RecordPaymentRequest body) {
        if (body == null || body.getAmount() == null) {
            return ResponseEntity.badRequest().build();
        }
        Membership membership = paymentService.recordSuccessfulPayment(
                membershipId,
                body.getAmount(),
                body.getPoolAllocation(),
                body.getPlatformFee(),
                body.getNationalFund());
        return ResponseEntity.ok(GroupsModuleDto.MembershipDto.fromEntity(membership));
    }
}
