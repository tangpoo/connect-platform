package com.cnt.memberservice.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cnt.memberservice.config.QuerydslTestConfig;
import com.cnt.memberservice.domain.Member;
import com.cnt.memberservice.dto.MemberDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({QuerydslTestConfig.class, MemberQueryRepository.class})
@ActiveProfiles("test")
class MemberQueryRepositoryTests {

    @Autowired
    private MemberQueryRepository repository;

    @Autowired
    private TestEntityManager em;

    @Test
    void findMember_shouldReturnSortedByName() {
        // given
        Member a = new Member("Alex");
        Member b = new Member("Ben");
        em.persist(a);
        em.persist(b);
        em.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<MemberDto> result = repository.findMember("name", pageable);

        // then
        assertEquals(2, result.getTotalElements());
        assertEquals("Alex", result.getContent().get(0).name());
    }
}
