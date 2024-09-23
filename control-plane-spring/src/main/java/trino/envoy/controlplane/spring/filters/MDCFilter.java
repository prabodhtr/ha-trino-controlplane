package trino.envoy.controlplane.spring.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import trino.envoy.controlplane.spring.constants.Constants;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static trino.envoy.controlplane.spring.constants.Constants.X_REQUEST_ID;

/**
 * Filter that adds {@link Constants#X_REQUEST_ID} header to request and response for tracing. <br/>
 * The filter also adds request id to MDC and logs each request
 */
@Slf4j
@Component
public class MDCFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = Optional.ofNullable(request.getHeader(X_REQUEST_ID)).orElse(UUID.randomUUID().toString());
        MDC.put(X_REQUEST_ID, requestId);

        log.info("{} {} params: {}", request.getMethod(), request.getRequestURI(), request.getQueryString());

        response.setHeader(X_REQUEST_ID, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(X_REQUEST_ID);
        }
    }
}
