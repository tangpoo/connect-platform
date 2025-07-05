package com.cnt.paymentservice.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

import com.cnt.paymentservice.dto.kakao.Amount;
import com.cnt.paymentservice.dto.kakao.KakaoApproveReq;
import com.cnt.paymentservice.dto.kakao.KakaoApproveRes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class KakaoPayClientTests {

    @Mock
    RestTemplate restTemplate;
    @InjectMocks
    KakaoPayClient kakaoClient;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(kakaoClient, "kakaoAdminKey", "test_admin_dummy");
    }

    @Test
    void approve_ok() {
        KakaoApproveReq req = new KakaoApproveReq(
            1L,
            "TC0ONETIME",
            "T123",
            "order-3",
            "user1",
            "pgToken",
            8000,
            null
        );

        KakaoApproveRes mockRes = new KakaoApproveRes(
            "aid",
            "T123",
            "TC0ONETIME",
            "CARD",
            new Amount(8000, 0, 0, 0, 0),
            "item",
            "user1",
            "order-3",
            "2025-07-05T12:00:00"
        );

        when(restTemplate.postForObject(anyString(), any(HttpEntity.class),
            eq(KakaoApproveRes.class)))
            .thenReturn(mockRes);

        KakaoApproveRes res = kakaoClient.approve(req);

        assertThat(res.tid()).isEqualTo("T123");
        then(restTemplate).should()
            .postForObject(eq(KakaoPayClient.URL), any(HttpEntity.class),
                eq(KakaoApproveRes.class));
    }

    @Test
    void approve_unauthorized() {
        KakaoApproveReq req = new KakaoApproveReq(
            1L,
            "TC0",
            "T1",
            "order",
            "user",
            "pg",
            1000,
            null
        );

        HttpClientErrorException ex401 = HttpClientErrorException.create(
            HttpStatus.UNAUTHORIZED, "", HttpHeaders.EMPTY, null, null);

        when(restTemplate.postForObject(anyString(), any(HttpEntity.class),
            eq(KakaoApproveRes.class)))
            .thenThrow(ex401);

        assertThatThrownBy(() -> kakaoClient.approve(req))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("인증 실패");
    }

    @Test
    void approve_bad_request() {
        KakaoApproveReq req = new KakaoApproveReq(
            1L,
            "TC0",
            "T1",
            "order",
            "user",
            "pg",
            1000,
            null
        );

        HttpClientErrorException ex400 = HttpClientErrorException.create(
            HttpStatus.BAD_REQUEST, "Bad", HttpHeaders.EMPTY, null, null);

        when(restTemplate.postForObject(anyString(), any(HttpEntity.class),
            eq(KakaoApproveRes.class)))
            .thenThrow(ex400);

        assertThatThrownBy(() -> kakaoClient.approve(req))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("요청 파라미터 오류");
    }
}
