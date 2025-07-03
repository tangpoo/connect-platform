package com.cnt.paymentservice.repository;

import com.cnt.paymentservice.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

}
