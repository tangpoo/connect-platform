package com.cnt.memberservice.controller;

import com.cnt.memberservice.dto.MemberDto;
import com.cnt.memberservice.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public Page<MemberDto> getMembers(
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return memberService.getMember(sortBy, page, size);
    }

    @PostMapping("/{id}/views")
    public ResponseEntity<Void> increaseViewCount(@PathVariable Long id) {
        memberService.increaseViewCount(id);
        return ResponseEntity.ok().build();
    }
}
