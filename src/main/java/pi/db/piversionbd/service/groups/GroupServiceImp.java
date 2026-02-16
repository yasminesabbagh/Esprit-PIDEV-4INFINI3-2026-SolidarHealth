package pi.db.piversionbd.service.groups;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pi.db.piversionbd.entities.groups.Group;
import pi.db.piversionbd.exception.ResourceNotFoundException;
import pi.db.piversionbd.repository.groups.GroupRepository;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupServiceImp implements IGroupService {

    private final GroupRepository groupRepository;

    @Override
    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    @Override
    public List<Group> getAllGroups(String typeFilter) {
        if (typeFilter == null || typeFilter.isBlank()) {
            return groupRepository.findAll();
        }
        return groupRepository.findByType(typeFilter.trim());
    }

    @Override
    public Group getGroupById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id " + id));
    }

    @Override
    public Group createGroup(Group group) {
        group.setId(null);
        return groupRepository.save(group);
    }

    @Override
    public Group updateGroup(Long id, Group updated) {
        Group existing = getGroupById(id);
        if (updated.getName() != null) existing.setName(updated.getName());
        if (updated.getType() != null) existing.setType(updated.getType());
        if (updated.getRegion() != null) existing.setRegion(updated.getRegion());
        if (updated.getMinMembers() != null) existing.setMinMembers(updated.getMinMembers());
        if (updated.getMaxMembers() != null) existing.setMaxMembers(updated.getMaxMembers());
        if (updated.getCurrentMemberCount() != null) existing.setCurrentMemberCount(updated.getCurrentMemberCount());
        return groupRepository.save(existing);
    }

    @Override
    public void deleteGroup(Long id) {
        groupRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Group> getGroupSuggestions(Integer age, String profession, String region, String packageType) {
        Stream<Group> stream = groupRepository.findAll().stream();
        if (region != null && !region.isBlank()) {
            String r = region.trim();
            stream = stream.filter(g -> r.equalsIgnoreCase(g.getRegion()));
        }
        if (profession != null && !profession.isBlank()) {
            String compatibleType = mapProfessionToGroupType(profession.trim());
            if (compatibleType != null) {
                stream = stream.filter(g -> compatibleType.equalsIgnoreCase(g.getType()));
            }
        }
        stream = stream.filter(g -> {
            Integer max = g.getMaxMembers();
            if (max == null) return true;
            int current = g.getCurrentMemberCount() != null ? g.getCurrentMemberCount() : 0;
            return current < max;
        });
        return stream.toList();
    }

    /** Simple rule: profession → group type. Students→STUDENTS, workers→WORKERS, family→FAMILY, else no filter. */
    private static String mapProfessionToGroupType(String profession) {
        if (profession == null) return null;
        String p = profession.toLowerCase();
        if (p.contains("student") || p.contains("étudiant")) return "STUDENTS";
        if (p.contains("worker") || p.contains("travail") || p.contains("employ")) return "WORKERS";
        if (p.contains("family") || p.contains("famille")) return "FAMILY";
        return "MIXED";
    }
}

