package pi.db.piversionbd.service.groups;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pi.db.piversionbd.entities.groups.Group;
import pi.db.piversionbd.entities.groups.Member;
import pi.db.piversionbd.exception.DuplicateCinException;
import pi.db.piversionbd.exception.ResourceNotFoundException;
import pi.db.piversionbd.repository.groups.GroupRepository;
import pi.db.piversionbd.repository.groups.MemberRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImp implements IMemberService {

    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;

    @Override
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    @Override
    public List<Member> getMembersByGroupId(Long groupId) {
        return memberRepository.findByCurrentGroup_Id(groupId);
    }

    @Override
    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id " + id));
    }

    @Override
    public Member createMember(Member member) {
        member.setId(null);
        if (member.getCinNumber() != null && memberRepository.existsByCinNumber(member.getCinNumber())) {
            throw new DuplicateCinException("CIN number already in use: " + member.getCinNumber());
        }
        return memberRepository.save(member);
    }

    @Override
    public Member updateMember(Long id, Member updated) {
        Member existing = getMemberById(id);
        // CIN is not updatable – it comes from pre-registration, is verified and admin-approved
        if (updated.getAge() != null) existing.setAge(updated.getAge());
        if (updated.getProfession() != null) existing.setProfession(updated.getProfession());
        if (updated.getRegion() != null) existing.setRegion(updated.getRegion());
        if (updated.getPersonalizedMonthlyPrice() != null) existing.setPersonalizedMonthlyPrice(updated.getPersonalizedMonthlyPrice());
        if (updated.getAdherenceScore() != null) existing.setAdherenceScore(updated.getAdherenceScore());
        existing.setCurrentGroup(updated.getCurrentGroup());
        return memberRepository.save(existing);
    }

    @Override
    public void deleteMember(Long id) {
        memberRepository.deleteById(id);
    }

    @Override
    public void resolveCurrentGroup(Member member, Long currentGroupId) {
        if (currentGroupId == null) {
            member.setCurrentGroup(null);
            return;
        }
        Group group = groupRepository.findById(currentGroupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id " + currentGroupId));
        member.setCurrentGroup(group);
    }
}
