package com.cnt.paymentservice.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

import com.cnt.paymentservice.dto.TossConfirmReq;
import com.cnt.paymentservice.dto.TossPaymentRes;
import java.nio.charset.StandardCharsets;
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
class TossClientServiceTests {

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    TossClientService tossClientService;

    @BeforeEach
    void setUp() {
        // @Value 주입 필드를 수동 세팅 (NPE 방지)
        ReflectionTestUtils.setField(
            tossClientService, "tossSecretKey", "test_sk_dummy");
    }

    @Test
    void toss_confirm_payment_should_call_rest_template() {
        // given
        TossConfirmReq req = new TossConfirmReq("payKey", "order-1", 10_000);

        TossPaymentRes tossPaymentRes = new TossPaymentRes("payKey", "order-1", 10_000, "DONE");
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class),
            eq(TossPaymentRes.class)))
            .thenReturn(tossPaymentRes);

        // when
        TossPaymentRes res = tossClientService.tossConfirmPayment(req);

        // then
        assertThat(res.status()).isEqualTo("DONE");
        then(restTemplate).should()
            .postForObject(eq(TossClientService.URL), any(HttpEntity.class),
                eq(TossPaymentRes.class));
    }

    @Test
    void toss_confirm_payment_should_throw_when_unauthorized() {
        // given
        TossConfirmReq req = new TossConfirmReq("payKey", "order-1", 10_000);

        HttpClientErrorException ex401 =
            HttpClientErrorException.create(
                HttpStatus.UNAUTHORIZED, "Unauthorized",
                HttpHeaders.EMPTY, null, StandardCharsets.UTF_8);

        when(restTemplate.postForObject(anyString(), any(HttpEntity.class),
            eq(TossPaymentRes.class)))
            .thenThrow(ex401);

        // then
        assertThatThrownBy(() -> tossClientService.tossConfirmPayment(req))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("토스 인증 실패");
    }

    @Test
    void toss_confirm_payment_should_throw_when_not_found() {
        // given
        TossConfirmReq req = new TossConfirmReq("payKey", "order-1", 10_000);

        HttpClientErrorException ex404 =
            HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "NotFound",
                HttpHeaders.EMPTY, null, StandardCharsets.UTF_8);

        when(restTemplate.postForObject(anyString(), any(HttpEntity.class),
            eq(TossPaymentRes.class)))
            .thenThrow(ex404);

        // then
        assertThatThrownBy(() -> tossClientService.tossConfirmPayment(req))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("해당 결제 정보가 존재하지 않습니다");
    }
}
