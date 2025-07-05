package com.cnt.paymentservice.service;

import com.cnt.paymentservice.dto.kakao.KakaoApproveReq;
import com.cnt.paymentservice.dto.kakao.KakaoApproveRes;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class KakaoPaymentService {

    public static final String URL = "https://kapi.kakaopay.com/v1/payment/approve";

    private final RestTemplate restTemplate;

    @Value("${payments.kakao.admin_key}")
    private String kakaoAdminKey;

    public KakaoApproveRes approve(KakaoApproveReq req) {
        HttpHeaders headers = buildHeaders();

        MultiValueMap<String, String> params = buildParams(req);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        try {
            return restTemplate.postForObject(URL, entity, KakaoApproveRes.class);
        } catch (HttpClientErrorException ex) {
            throw traslateError(ex);
        }
    }

    private static MultiValueMap<String, String> buildParams(
        KakaoApproveReq req) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", req.cid());
        params.add("tid", req.tid());
        params.add("partner_order_id", req.partnerOrderId());
        params.add("partner_user_id", req.partnerUserId());
        params.add("pg_token", req.pgToken());
        if (req.totalAmount() != null) {
            params.add("total_amount", String.valueOf(req.totalAmount()));
        }
        return params;
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "KakaoAK " + kakaoAdminKey);
        return headers;
    }

    private RuntimeException traslateError(HttpClientErrorException ex) {
        if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            return new IllegalStateException("카카오페이 인증 실패: Admin 키를 확인하세요.");
        }
        if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
            return new IllegalArgumentException(
                "카카오페이 요청 파라미터 오류: " + ex.getResponseBodyAsString());
        }
        return new IllegalStateException("KakaoPay 클라이언트 오류: " + ex.getStatusText());
    }
}
