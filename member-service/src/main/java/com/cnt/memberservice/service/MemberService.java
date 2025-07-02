package com.cnt.memberservice.service;

import com.cnt.memberservice.domain.Member;
import com.cnt.memberservice.dto.MemberDto;
import com.cnt.memberservice.repository.MemberQueryRepository;
import com.cnt.memberservice.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberQueryRepository memberQueryRepository;

    public Page<MemberDto> getMember(String sortBy, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return memberQueryRepository.findMember(sortBy, pageable);
    }

    public void increaseViewCount(Long id) {
        Member member = memberRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Member not found."));
        member.increaseViewCount();
        memberRepository.save(member);
    }
}
