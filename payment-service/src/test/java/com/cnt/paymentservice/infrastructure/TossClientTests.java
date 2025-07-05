package com.cnt.paymentservice.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

import com.cnt.paymentservice.dto.toss.TossConfirmReq;
import com.cnt.paymentservice.dto.toss.TossPaymentRes;
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
class TossClientTests {

    @Mock
    RestTemplate restTemplate;
    @InjectMocks
    TossClient tossClient;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(tossClient, "tossSecretKey", "test_sk_dummy");
    }

    @Test
    void confirm_ok() {
        TossConfirmReq req = new TossConfirmReq(1L, "payKey", "order-1", 10000, "couponCode");
        TossPaymentRes mockRes = new TossPaymentRes("payKey", "order-1", 10000, "DONE");

        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(TossPaymentRes.class)))
            .thenReturn(mockRes);

        TossPaymentRes res = tossClient.confirm(req);

        assertThat(res.status()).isEqualTo("DONE");
        then(restTemplate).should()
            .postForObject(eq(TossClient.URL), any(HttpEntity.class), eq(TossPaymentRes.class));
    }

    @Test
    void confirm_unauthorized() {
        TossConfirmReq req = new TossConfirmReq(1L, "payKey", "order-1", 10000, "couponCode");
        HttpClientErrorException ex401 = HttpClientErrorException.create(
            HttpStatus.UNAUTHORIZED, "", HttpHeaders.EMPTY, null, null);

        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(TossPaymentRes.class)))
            .thenThrow(ex401);

        assertThatThrownBy(() -> tossClient.confirm(req))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("토스 인증 실패");
    }

    @Test
    void confirm_not_found() {
        TossConfirmReq req = new TossConfirmReq(1L, "payKey", "order-1", 10000, "couponCode");
        HttpClientErrorException ex404 = HttpClientErrorException.create(
            HttpStatus.NOT_FOUND, "", HttpHeaders.EMPTY, null, null);

        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(TossPaymentRes.class)))
            .thenThrow(ex404);

        assertThatThrownBy(() -> tossClient.confirm(req))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("존재하지 않습니다");
    }
}