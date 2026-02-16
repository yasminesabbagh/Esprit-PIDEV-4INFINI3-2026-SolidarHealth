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
import pi.db.piversionbd.entities.groups.Group;
import pi.db.piversionbd.entities.groups.Membership;
import pi.db.piversionbd.service.groups.IGroupService;
import pi.db.piversionbd.service.groups.IMembershipService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "Module 1 – Groups & Memberships", description = "Groups CRUD; membership operations (add/end member in group)")
public class GroupController {

    private final IGroupService groupService;
    private final IMembershipService membershipService;

    @GetMapping
    @Operation(summary = "List all groups", description = "Optional filter by type (query param with dropdown).")
    @ApiResponse(responseCode = "200", description = "OK")
    public ResponseEntity<List<GroupsModuleDto.GroupDto>> getAll(
            @Parameter(description = "Filter by group type. Placeholder: FAMILY. Available values: FAMILY, STUDENTS, WORKERS, MIXED", schema = @Schema(allowableValues = { "FAMILY", "STUDENTS", "WORKERS", "MIXED" }, example = "FAMILY"))
            @RequestParam(required = false) String type) {
        List<Group> groups = type != null && !type.isBlank() ? groupService.getAllGroups(type) : groupService.getAllGroups();
        List<GroupsModuleDto.GroupDto> list = groups.stream()
                .map(GroupsModuleDto.GroupDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/suggestions")
    @Operation(
            summary = "Get group suggestions for a member",
            description = "Returns groups that match the member's criteria: same region, group type compatible with profession, and group not full. Rule-based for now; can be replaced with ML/AI matching later. Age and packageType are accepted for future use.")
    @ApiResponse(responseCode = "200", description = "OK – list of matching groups")
    public ResponseEntity<List<GroupsModuleDto.GroupDto>> getSuggestions(
            @Parameter(description = "Member age (for future age-range matching)") @RequestParam(required = false) Integer age,
            @Parameter(description = "Profession (e.g. student, worker) – maps to group type: students→STUDENTS, workers→WORKERS, family→FAMILY") @RequestParam(required = false) String profession,
            @Parameter(description = "Region – only groups in this region") @RequestParam(required = false) String region,
            @Parameter(description = "Package type (for future use)", schema = @Schema(allowableValues = { "BASIC", "CONFORT", "PREMIUM" })) @RequestParam(required = false) String packageType) {
        List<Group> groups = groupService.getGroupSuggestions(age, profession, region, packageType);
        List<GroupsModuleDto.GroupDto> list = groups.stream()
                .map(GroupsModuleDto.GroupDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{groupId}")
    @Operation(summary = "Get group by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Group not found", content = @Content(mediaType = "text/plain", schema = @Schema(example = "Group not found with id 99")))
    })
    public ResponseEntity<GroupsModuleDto.GroupDto> getById(
            @Parameter(description = "Group ID. Placeholder: 1", required = true, schema = @Schema(example = "1")) @PathVariable Long groupId) {
        Group group = groupService.getGroupById(groupId);
        return ResponseEntity.ok(GroupsModuleDto.GroupDto.fromEntity(group));
    }

    @PostMapping
    @Operation(summary = "Create a new group", description = "Fill each parameter; each has its own placeholder.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Group created"),
            @ApiResponse(responseCode = "400", description = "name required")
    })
    public ResponseEntity<GroupsModuleDto.GroupDto> create(
            @Parameter(description = "Group name. Placeholder: Family Solidarity 42", required = true, schema = @Schema(example = "Family Solidarity 42")) @RequestParam String name,
            @Parameter(description = "Group type. Placeholder: FAMILY. Available values: FAMILY, STUDENTS, WORKERS, MIXED", schema = @Schema(allowableValues = { "FAMILY", "STUDENTS", "WORKERS", "MIXED" }, example = "FAMILY")) @RequestParam(required = false) String type,
            @Parameter(description = "Region / city. Placeholder: Tunis", schema = @Schema(example = "Tunis")) @RequestParam(required = false) String region,
            @Parameter(description = "Minimum members. Placeholder: 5", schema = @Schema(example = "5")) @RequestParam(required = false) Integer minMembers,
            @Parameter(description = "Maximum members. Placeholder: 15", schema = @Schema(example = "15")) @RequestParam(required = false) Integer maxMembers,
            @Parameter(description = "Current member count. Placeholder: 0", schema = @Schema(example = "0")) @RequestParam(required = false) Integer currentMemberCount) {
        GroupsModuleDto.GroupDto dto = new GroupsModuleDto.GroupDto(null, name, type, region, minMembers, maxMembers, currentMemberCount);
        Group group = toEntity(dto);
        Group created = groupService.createGroup(group);
        return ResponseEntity.status(HttpStatus.CREATED).body(GroupsModuleDto.GroupDto.fromEntity(created));
    }

    @PutMapping("/{groupId}")
    @Operation(summary = "Update group by ID", description = "Send the fields to update; omitted params keep existing values. Use placeholders as a guide.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK – updated group"),
            @ApiResponse(responseCode = "400", description = "No fields to update (send at least one param)"),
            @ApiResponse(responseCode = "404", description = "Group not found", content = @Content(mediaType = "text/plain", schema = @Schema(example = "Group not found with id 99")))
    })
    public ResponseEntity<GroupsModuleDto.GroupDto> update(
            @Parameter(description = "Group ID. Placeholder: 1", required = true, schema = @Schema(example = "1")) @PathVariable Long groupId,
            @Parameter(description = "Group name. Placeholder: Family Solidarity 42", schema = @Schema(example = "Family Solidarity 42")) @RequestParam(required = false) String name,
            @Parameter(description = "Group type. Placeholder: FAMILY. Available values: FAMILY, STUDENTS, WORKERS, MIXED", schema = @Schema(allowableValues = { "FAMILY", "STUDENTS", "WORKERS", "MIXED" }, example = "FAMILY")) @RequestParam(required = false) String type,
            @Parameter(description = "Region / city. Placeholder: Tunis", schema = @Schema(example = "Tunis")) @RequestParam(required = false) String region,
            @Parameter(description = "Minimum members. Placeholder: 5", schema = @Schema(example = "5")) @RequestParam(required = false) Integer minMembers,
            @Parameter(description = "Maximum members. Placeholder: 15", schema = @Schema(example = "15")) @RequestParam(required = false) Integer maxMembers,
            @Parameter(description = "Current member count. Placeholder: 0", schema = @Schema(example = "0")) @RequestParam(required = false) Integer currentMemberCount) {
        Group existing = groupService.getGroupById(groupId);
        boolean noParamsSent = name == null && type == null && region == null && minMembers == null && maxMembers == null && currentMemberCount == null;
        if (noParamsSent) {
            return ResponseEntity.badRequest().build();
        }
        String newName = name != null ? name : existing.getName();
        String newType = type != null ? type : existing.getType();
        String newRegion = region != null ? region : existing.getRegion();
        Integer newMin = minMembers != null ? minMembers : existing.getMinMembers();
        Integer newMax = maxMembers != null ? maxMembers : existing.getMaxMembers();
        Integer newCount = currentMemberCount != null ? currentMemberCount : existing.getCurrentMemberCount();
        GroupsModuleDto.GroupDto dto = new GroupsModuleDto.GroupDto(groupId, newName, newType, newRegion, newMin, newMax, newCount);
        Group group = groupService.updateGroup(groupId, toEntity(dto));
        return ResponseEntity.ok(GroupsModuleDto.GroupDto.fromEntity(group));
    }

    @DeleteMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete group by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "No content"),
            @ApiResponse(responseCode = "404", description = "Group not found", content = @Content(mediaType = "text/plain", schema = @Schema(example = "Group not found with id 99")))
    })
    public void delete(
            @Parameter(description = "Group ID. Placeholder: 1", required = true, schema = @Schema(example = "1")) @PathVariable Long groupId) {
        groupService.deleteGroup(groupId);
    }

    // ----- Membership operations (groups & payments after member is approved) -----

    @PostMapping("/{groupId}/members")
    @Operation(summary = "Create membership – add member to group", description = "Creates a membership linking the member and group. Use groupId (path), memberId and packageType (query). monthly_amount is taken from the member's personalized_monthly_price.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Membership created"),
            @ApiResponse(responseCode = "400", description = "memberId required, or member already in group"),
            @ApiResponse(responseCode = "404", description = "Group or member not found")
    })
    public ResponseEntity<GroupsModuleDto.MembershipDto> createMembership(
            @Parameter(description = "Group ID. Placeholder: 1", required = true, schema = @Schema(example = "1")) @PathVariable Long groupId,
            @Parameter(description = "Member ID (current member). Placeholder: 3", required = true, schema = @Schema(example = "3")) @RequestParam Long memberId,
            @Parameter(description = "Package type. Placeholder: BASIC. Available values: BASIC, CONFORT, PREMIUM", schema = @Schema(allowableValues = { "BASIC", "CONFORT", "PREMIUM" }, example = "BASIC")) @RequestParam(required = false) String packageType) {
        Membership membership = membershipService.addMemberToGroup(groupId, memberId, packageType);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GroupsModuleDto.MembershipDto.fromEntity(membership));
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "End membership – remove member from group", description = "Ends the membership (soft delete). Admin or self.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Membership ended"),
            @ApiResponse(responseCode = "404", description = "Group, member, or active membership not found")
    })
    public void endMembership(
            @Parameter(description = "Group ID. Placeholder: 1", required = true, schema = @Schema(example = "1")) @PathVariable Long groupId,
            @Parameter(description = "Member ID. Placeholder: 3", required = true, schema = @Schema(example = "3")) @PathVariable Long memberId) {
        membershipService.removeMemberFromGroup(groupId, memberId);
    }

    private static Group toEntity(GroupsModuleDto.GroupDto dto) {
        Group g = new Group();
        g.setId(dto.getGroupId());
        g.setName(dto.getName());
        g.setType(dto.getType());
        g.setRegion(dto.getRegion());
        g.setMinMembers(dto.getMinMembers());
        g.setMaxMembers(dto.getMaxMembers());
        g.setCurrentMemberCount(dto.getCurrentMemberCount());
        return g;
    }
}
