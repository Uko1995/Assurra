package com.uko.eaas.identity.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Performance Configuration and Monitoring.
 * 
 * Provides performance monitoring and optimization features.
 */
@Slf4j
@Configuration
public class PerformanceConfig implements WebMvcConfigurer {

    /**
     * Request timing interceptor for monitoring slow endpoints.
     * Only active in dev and staging profiles.
     */
    @Bean
    @Profile({"dev", "staging"})
    public RequestTimingInterceptor requestTimingInterceptor() {
        return new RequestTimingInterceptor();
    }

    @Override
    @Profile({"dev", "staging"})
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestTimingInterceptor())
                .addPathPatterns("/api/v1/**");
    }

    /**
     * Simple interceptor to log slow requests.
     */
    public static class RequestTimingInterceptor implements org.springframework.web.servlet.HandlerInterceptor {
        
        private static final long SLOW_REQUEST_THRESHOLD_MS = 1000; // Log requests slower than 1 second
        
        @Override
        public boolean preHandle(jakarta.servlet.http.HttpServletRequest request,
                                jakarta.servlet.http.HttpServletResponse response,
                                Object handler) throws Exception {
            request.setAttribute("startTime", System.currentTimeMillis());
            return true;
        }

        @Override
        public void afterCompletion(jakarta.servlet.http.HttpServletRequest request,
                                   jakarta.servlet.http.HttpServletResponse response,
                                   Object handler, Exception ex) throws Exception {
            Long startTime = (Long) request.getAttribute("startTime");
            if (startTime != null) {
                long duration = System.currentTimeMillis() - startTime;
                if (duration > SLOW_REQUEST_THRESHOLD_MS) {
                    log.warn("SLOW REQUEST: {} {} took {}ms (Status: {})", 
                            request.getMethod(), 
                            request.getRequestURI(),
                            duration,
                            response.getStatus());
                } else if (request.getRequestURI().contains("/login")) {
                    log.info("Login request took {}ms (Status: {})", duration, response.getStatus());
                }
            }
        }
    }
}
