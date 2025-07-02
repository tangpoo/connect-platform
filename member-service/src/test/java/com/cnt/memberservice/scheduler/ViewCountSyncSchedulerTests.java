package com.cnt.memberservice.scheduler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cnt.memberservice.domain.Member;
import com.cnt.memberservice.repository.MemberRepository;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class ViewCountSyncSchedulerTests {

    @Mock
    private RedisTemplate<String, Long> redisTemplate;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private ViewCountSyncScheduler scheduler;

    @Test
    void sync_view_counts_to_database_should_sync_and_clear() {
        // given
        Set<String> keys = Set.of("profile:viewcount:1", "profile:viewcount:2");
        when(redisTemplate.keys("profile:viewcount:*")).thenReturn(keys);

        ValueOperations<String, Long> ops = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(ops);
        when(ops.get("profile:viewcount:1")).thenReturn(5L);
        when(ops.get("profile:viewcount:2")).thenReturn(3L);

        Member m1 = mock(Member.class);
        Member m2 = mock(Member.class);

        when(m1.getId()).thenReturn(1L);
        when(m2.getId()).thenReturn(2L);
        when(memberRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(m1, m2));

        // when
        scheduler.syncViewCountsToDatabase();

        // then
        verify(m1).increaseViewCount(5L);
        verify(m2).increaseViewCount(3L);
        verify(memberRepository).saveAll(List.of(m1, m2));
        verify(redisTemplate).delete(List.of("profile:viewcount:1", "profile:viewcount:2"));
    }
}
