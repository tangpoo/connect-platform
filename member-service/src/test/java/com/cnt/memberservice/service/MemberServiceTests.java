package com.cnt.memberservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cnt.memberservice.dto.MemberDto;
import com.cnt.memberservice.repository.MemberQueryRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class MemberServiceTests {

    @Mock
    private MemberQueryRepository memberQueryRepository;

    @Mock
    private RedisTemplate<String, Long> redisTemplate;

    @InjectMocks
    private MemberService memberService;

    @Test
    void get_member_should_call_query_repository() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<MemberDto> expected = new PageImpl<>(List.of());
        when(memberQueryRepository.findMember("name", pageable)).thenReturn(expected);

        // when
        Page<MemberDto> result = memberService.getMember("name", 0, 10);

        // then
        assertEquals(expected, result);
        verify(memberQueryRepository).findMember("name", pageable);
    }

    @Test
    void increase_view_count_should_increment_redis_key() {
        // given
        Long id = 1L;
        ValueOperations<String, Long> ops = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(ops);

        // when
        memberService.increaseViewCount(id);

        // then
        verify(ops).increment("profile:viewcount:1");
    }
}
