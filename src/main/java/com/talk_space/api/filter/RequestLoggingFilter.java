
package com.talk_space.api.filter;

import com.talk_space.util.LoggingUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;

@Component
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String correlationId = LoggingUtil.generateCorrelationId();
        LoggingUtil.setCorrelationId(correlationId);

        long startTime = System.currentTimeMillis();

        try {
            // Log request
            logRequest(httpRequest);

            chain.doFilter(request, response);

        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logResponse(httpRequest, httpResponse, duration);
            LoggingUtil.clearCorrelationId();
        }
    }

    private void logRequest(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullUrl = queryString == null ? uri : uri + "?" + queryString;

        LoggingUtil.logInfo(this.getClass(), "Incoming request: {} {}", method, fullUrl);

        // Log headers (optional - be careful with sensitive headers)
        Collections.list(request.getHeaderNames()).forEach(headerName ->
                LoggingUtil.logDebug(this.getClass(), "Header: {}: {}", headerName, request.getHeader(headerName))
        );
    }

    private void logResponse(HttpServletRequest request, HttpServletResponse response, long duration) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();

        LoggingUtil.logApiCall(method, uri, String.valueOf(status), duration);
    }
}