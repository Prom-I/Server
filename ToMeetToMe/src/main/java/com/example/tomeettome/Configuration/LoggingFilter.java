package com.example.tomeettome.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.AbstractRequestLoggingFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

@Component
@Order(1)
public class LoggingFilter extends AbstractRequestLoggingFilter {
    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    private static final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    protected void beforeRequest(HttpServletRequest request, String message) {
    }

    @Override
    protected void afterRequest(HttpServletRequest request, String message) {
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            super.doFilterInternal(requestWrapper, responseWrapper, filterChain);
        } finally {
            String method = requestWrapper.getMethod();
            String uri = requestWrapper.getRequestURI();

            // Log the method and URI
            logger.info("Request: Method: {}, URI: {}", method, uri);

            // Log all headers
            Enumeration<String> headerNames = requestWrapper.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = requestWrapper.getHeader(headerName);
                logger.info("Request: Header: {} = {}", headerName, headerValue);
            }

            logger.info("Request IP : {}", request.getRemoteAddr());
            String requestBody = new String(requestWrapper.getContentAsByteArray(), requestWrapper.getCharacterEncoding());
            logger.info("Request Body: {}", requestBody);

            byte[] responseBytes = responseWrapper.getContentAsByteArray();
            if (responseBytes.length > 0) {
                String responseBody = new String(responseBytes, StandardCharsets.UTF_8);
                try {
                    Object json = mapper.readValue(responseBody, Object.class);
                    String prettyResponse = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
                    logger.info("Response Body: {}", prettyResponse);
                } catch (Exception e) {
                    logger.error("Error occurred while trying to format the JSON response: ", e);
                    logger.info("Response Body (unformatted): {}", responseBody);
                }
            }

            int responseStatus = responseWrapper.getStatus();
            logger.info("Response Status: {}", responseStatus);
            responseWrapper.copyBodyToResponse();
        }
    }
}