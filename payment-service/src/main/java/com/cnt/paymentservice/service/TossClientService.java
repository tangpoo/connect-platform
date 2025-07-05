package com.cnt.paymentservice.service;

import com.cnt.paymentservice.dto.toss.TossConfirmReq;
import com.cnt.paymentservice.dto.toss.TossPaymentRes;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class TossClientService {

    public static final String URL = "https://api.tosspayments.com/v1/payments/confirm";
    private final RestTemplate restTemplate;
    @Value("${payments.toss.test_secret_api_key}")
    private String tossSecretKey;

    public TossPaymentRes confirm(TossConfirmReq req) {
        HttpEntity<TossConfirmReq> requestHttpEntity = new HttpEntity<>(req, buildHeaders());

        try {
            return restTemplate.postForObject(URL, requestHttpEntity, TossPaymentRes.class);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new IllegalStateException("토스 인증 실패: 시크릿 키를 확인하세요.");
            } else if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new IllegalStateException("해당 결제 정보가 존재하지 않습니다.");
            }
            throw new IllegalStateException("Toss 클라이언트 오류: " + ex.getStatusText());
        }
    }

    private HttpHeaders buildHeaders() {
        String auth = Base64.getEncoder()
            .encodeToString((tossSecretKey + ":").getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Basic " + auth);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}