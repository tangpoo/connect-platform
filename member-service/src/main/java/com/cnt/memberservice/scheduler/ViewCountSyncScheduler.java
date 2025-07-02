package com.cnt.memberservice.scheduler;

import com.cnt.memberservice.domain.Member;
import com.cnt.memberservice.repository.MemberRepository;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountSyncScheduler {

    private final RedisTemplate<String, Long> redisTemplate;
    private final MemberRepository memberRepository;

    private static final String VIEW_COUNT_PREFIX = "profile:viewcount:";

    @Scheduled(fixedRate = 1000 * 60)
    public void syncViewCountsToDatabase() {
        Map<Long, Long> viewCounts = getViewCountMapFromRedis();
        if (viewCounts.isEmpty()) return;

        List<Member> updatedMembers = applyViewCountToMembers(viewCounts);
        persistAndClear(updatedMembers, viewCounts.keySet());
    }

    private Map<Long, Long> getViewCountMapFromRedis() {
        Set<String> keys = redisTemplate.keys(VIEW_COUNT_PREFIX + "*");
        if (keys == null || keys.isEmpty()) return Collections.emptyMap();

        Map<Long, Long> viewCounts = new HashMap<>();
        for (String key : keys) {
            Long profileId = Long.valueOf(key.replace(VIEW_COUNT_PREFIX, ""));
            Long count = redisTemplate.opsForValue().get(key);
            if (count != null) {
                viewCounts.put(profileId, count);
            }
        }
        return viewCounts;
    }

    private List<Member> applyViewCountToMembers(Map<Long, Long> viewCounts) {
        List<Member> members = memberRepository.findAllById(viewCounts.keySet());
        for (Member member : members) {
            Long increment = viewCounts.get(member.getId());
            if (increment != null) {
                member.increaseViewCount(increment);
            }
        }
        return members;
    }

    private void persistAndClear(List<Member> members, Set<Long> ids) {
        memberRepository.saveAll(members);

        List<String> redisKeys = ids.stream()
            .map(id -> VIEW_COUNT_PREFIX + id)
            .toList();

        redisTemplate.delete(redisKeys);
    }
}
