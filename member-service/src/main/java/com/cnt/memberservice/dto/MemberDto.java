package com.cnt.memberservice.dto;

import com.cnt.memberservice.domain.Member;
import java.time.LocalDateTime;

public record MemberDto(
    Long id,
    String name,
    int viewCount,
    LocalDateTime createdAt
) {

    public static MemberDto from(Member member) {
        return new MemberDto(
            member.getId(),
            member.getName(),
            member.getViewCount(),
            member.getCreatedAt()
        );
    }
}
