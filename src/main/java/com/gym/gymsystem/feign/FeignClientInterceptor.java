package com.gym.gymsystem.feign;

import com.gym.gymsystem.util.JwtTokenProvider;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    public FeignClientInterceptor(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String token = jwtTokenProvider.getToken();
        if (token != null) {
            requestTemplate.header("Authorization", "Bearer " + token);
        }
    }
}