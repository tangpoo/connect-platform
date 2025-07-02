package com.cnt.memberservice.dto;

public record MemberListRequest(
    String sortBy,
    int page,
    int size
) {

}
