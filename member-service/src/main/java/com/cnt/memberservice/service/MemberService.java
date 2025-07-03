package com.cnt.memberservice.service;

import com.cnt.memberservice.dto.MemberDto;
import com.cnt.memberservice.repository.MemberQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private static final String VIEW_COUNT_PREFIX = "profile:viewcount:";
    private final MemberQueryRepository memberQueryRepository;
    private final RedisTemplate<String, Long> redisTemplate;

    public Page<MemberDto> getMember(String sortBy, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return memberQueryRepository.findMember(sortBy, pageable);
    }

    public void increaseViewCount(Long id) {
        String key = VIEW_COUNT_PREFIX + id;
        redisTemplate.opsForValue().increment(key);
    }
}
