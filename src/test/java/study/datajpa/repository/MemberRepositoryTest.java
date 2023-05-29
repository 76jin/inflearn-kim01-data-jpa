package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;
    @PersistenceContext
    EntityManager em;

    @Test
    void testMember() {
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        Member foundMember = memberRepository.findById(savedMember.getId()).get();

        assertThat(foundMember.getId()).isEqualTo(member.getId());
        assertThat(foundMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(foundMember).isEqualTo(savedMember);
    }

    @Test
    void testBasicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        // 단건 조회 검증
        Member foundMember1 = memberRepository.findById(member1.getId()).get();
        Member foundMember2 = memberRepository.findById(member2.getId()).get();

        assertThat(foundMember1).isEqualTo(member1);
        assertThat(foundMember2).isEqualTo(member2);

        // 리스트 조회 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        // 카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        // 삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    void testFindByUsernameAndAgeGreaterThan() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("AAA", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);

        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    void testFindHelloBy() {
        List<Member> result = memberRepository.findHelloBy(); // check sql
    }

    @Test
    void testFindTopHello3By() {
        List<Member> result = memberRepository.findTop3Hello3By(); // check sql
    }

    @Test
    void testFindByUsername() {
        Member member1 = new Member("AAA", 10);
        memberRepository.save(member1);

        List<Member> result = memberRepository.findByUsername(member1.getUsername());

        assertThat(result.get(0).getUsername()).isEqualTo(member1.getUsername());
        assertThat(result.get(0).getAge()).isEqualTo(member1.getAge());
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0)).isEqualTo(member1);
    }

    @Test
    void testFindByUser() {
        Member member1 = new Member("AAA", 10);
        memberRepository.save(member1);

        List<Member> result = memberRepository.findUser(member1.getUsername(), member1.getAge());

        assertThat(result.get(0).getUsername()).isEqualTo(member1.getUsername());
        assertThat(result.get(0).getAge()).isEqualTo(member1.getAge());
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0)).isEqualTo(member1);
    }

    @Test
    void testFindByUsernameList() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<String> result = memberRepository.findUsernameList();

        for (String username : result) {
            System.out.println("## username:" + username);
        }
    }

    @Test
    void testMemberDto() {
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member member1 = new Member("AAA", 10);
        member1.setTeam(team);
        memberRepository.save(member1);

        List<MemberDto> memberDto = memberRepository.findMemberDto();

        for (MemberDto dto : memberDto) {
            System.out.println("## dto:" + dto);
        }
    }

    @Test
    void testFindByNames() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> result = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));

        for (Member member : result) {
            System.out.println("## member:" + member);
        }
    }

    @Test
    void returnType() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> list = memberRepository.findListByUsername("AAA");
        Member findMember = memberRepository.findMemberByUsername("AAA");
        Optional<Member> optionalList = memberRepository.findOptionalMemberByUsername("AAA");
        Member empty = memberRepository.findMemberByUsername("empty");
        Optional<Member> optionalEmpty = memberRepository.findOptionalMemberByUsername("empty");

        for (Member member : list) {
            System.out.println("## member:" + member);
        }

        System.out.println("## findMember:" + findMember);

        Member findMember2 = optionalList.get();
        System.out.println("## findMember2:" + findMember2);

        System.out.println("## emptyMember:" + empty);
        System.out.println("## optionalEmptyMember:" + optionalEmpty);

    }

    @Test
    void paging() {
        // given
        for (int i = 1; i <= 5; i++) {
            memberRepository.save(new Member("member" + i, 10));
        }
        int age = 10;
        int offset = 0;
        int limit = 3;
        PageRequest pageRequest = PageRequest.of(offset, limit, Sort.Direction.DESC, "username");

        // when
        Page<Member> page = memberRepository.findByAge(age, pageRequest);
        Slice<Member> memberSlice = memberRepository.findMemberSliceByAge(age, pageRequest);
        List<Member> memberList = memberRepository.findMemberListByAge(age, pageRequest);

        Page<Member> memberQuery = memberRepository.findMemberQueryByAge(age, pageRequest);
        List<Member> membersTop = memberRepository.findTop4ByAge(age, Sort.by(Sort.Direction.DESC, "username"));

        Page<MemberDto> toMap = page.map(m -> new MemberDto(m.getId(), m.getUsername(), "teamA"));

        // then
        List<Member> content = page.getContent();
        long totalElements = page.getTotalElements();

        assertThat(content.size()).isEqualTo(3);
        assertThat(totalElements).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();

        for (Member member : content) {
            System.out.println("[Page] member = " + member);
        }
        System.out.println("totalElements = " + totalElements);

        for (Member member : memberSlice) {
            System.out.println("[Slice] member = " + member);
        }
        for (Member member : memberList) {
            System.out.println("[List] member = " + member);
        }
        for (Member member : membersTop) {
            System.out.println("[TOP] member = " + member);
        }
        for (MemberDto dto : toMap) {
            System.out.println("[DTO] dto = " + dto);
        }
    }

    @Test
    void bulkUpdate() {
        // given
        for (int i = 1; i <= 5; i++) {
            memberRepository.save(new Member("member" + i, 10 * i));
        }

        // when
        int resultCount = memberRepository.bulkAgePlus(30);
//        em.flush(); // JPQL은 실행하기 직전에 영속성 컨텍스트를 자동으로 flush 한다. (save 메소드 실행 직전)
//        em.clear();

        List<Member> result = memberRepository.findByUsername("member5");
        Member member5 = result.get(0);
        System.out.println("member5 = " + member5);

        // then
        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    void findMemberLazy() {
        // given
        // member1 -> teamA
        // member2 -> teamB

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        // when N + 1
        // select Member 1
//        List<Member> members = memberRepository.findAll();
//        List<Member> members = memberRepository.findMemberFetchJoin();
//        List<Member> members = memberRepository.findAll();
        List<Member> members = memberRepository.findEntityGraphByUsernameStartingWith("member");

        for (Member member : members) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member.teamClass = " + member.getTeam().getClass());
            System.out.println("member.team.name = " + member.getTeam().getName());
        }

        // thten
    }
}