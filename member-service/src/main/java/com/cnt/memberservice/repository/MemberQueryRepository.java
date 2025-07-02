package com.cnt.memberservice.repository;


import com.cnt.memberservice.domain.QMember;
import com.cnt.memberservice.dto.MemberDto;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<MemberDto> findMember(String sortBy, Pageable pageable) {
        QMember member = QMember.member;

        OrderSpecifier<?> order = switch (sortBy) {
            case "name" -> member.name.asc();
            case "viewCount" -> member.viewCount.desc();
            default -> member.createdAt.desc();
        };

        List<MemberDto> content = queryFactory
            .select(Projections.constructor(MemberDto.class,
                member.id,
                member.name,
                member.viewCount,
                member.createdAt))
            .from(member)
            .orderBy(order)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long count = queryFactory
            .select(member.count())
            .from(member)
            .fetchOne();

        return new PageImpl<>(content, pageable, count);
    }
}
