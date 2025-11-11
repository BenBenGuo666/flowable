package com.demo.flowable.common.filter;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 请求和响应日志拦截器（简洁版）
 *
 * 功能：
 * 1. 记录 HTTP 请求（方法、路径、参数、请求体）
 * 2. 记录 HTTP 响应（状态码、响应体）
 * 3. 记录请求处理耗时
 *
 * 日志格式：紧凑单行或双行显示，便于快速查看
 *
 * @author e-Benben.Guo
 * @date 2025/11
 */
@Slf4j
@Component
public class RequestResponseLoggingFilter implements WebFilter {

    /**
     * 最大日志内容长度（避免日志过大）
     */
    private static final int MAX_LOG_LENGTH = 500;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (!log.isDebugEnabled()) {
            return chain.filter(exchange);
        }

        long startTime = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 包装请求和响应以捕获请求体和响应体
        LoggingRequestDecorator loggingRequest = new LoggingRequestDecorator(request);
        LoggingResponseDecorator loggingResponse = new LoggingResponseDecorator(response, request, startTime);

        return chain.filter(exchange.mutate()
                        .request(loggingRequest)
                        .response(loggingResponse)
                        .build())
                .doOnError(error -> {
                    long duration = System.currentTimeMillis() - startTime;
                    log.error("❌ {} {} - ERROR [{}ms] - {}",
                        request.getMethod(),
                        request.getURI().getPath(),
                        duration,
                        error.getMessage());
                });
    }

    /**
     * 请求装饰器：捕获请求体
     */
    private class LoggingRequestDecorator extends ServerHttpRequestDecorator {

        private final ServerHttpRequest delegate;

        public LoggingRequestDecorator(ServerHttpRequest delegate) {
            super(delegate);
            this.delegate = delegate;
        }

        @Override
        public Flux<DataBuffer> getBody() {
            // 先打印基本请求信息
            logRequestBasic(delegate);

            // 如果没有请求体内容（如 GET 请求），记录为空
            if (!shouldLogBody(delegate)) {
                logRequestBody(null);
                return super.getBody();
            }

            DataBufferFactory bufferFactory = new DefaultDataBufferFactory();

            return DataBufferUtils.join(super.getBody())
                    .flatMapMany(dataBuffer -> {
                        // 读取并记录请求体
                        byte[] content = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(content);
                        DataBufferUtils.release(dataBuffer);

                        String body = new String(content, StandardCharsets.UTF_8);
                        logRequestBody(body);

                        // 返回新的 buffer 供后续使用
                        return Flux.just(bufferFactory.wrap(content));
                    });
        }
    }

    /**
     * 响应装饰器：捕获响应体
     */
    private class LoggingResponseDecorator extends ServerHttpResponseDecorator {

        private final ServerHttpRequest request;
        private final long startTime;

        public LoggingResponseDecorator(ServerHttpResponse delegate, ServerHttpRequest request, long startTime) {
            super(delegate);
            this.request = request;
            this.startTime = startTime;
        }

        @Override
        public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
            // 如果不需要记录响应体内容，记录为空响应
            if (!shouldLogBody(getDelegate())) {
                logResponse(request, getDelegate(), null, startTime);
                return super.writeWith(body);
            }

            DataBufferFactory bufferFactory = new DefaultDataBufferFactory();

            return DataBufferUtils.join(body)
                    .flatMap(dataBuffer -> {
                        // 读取并记录响应体
                        byte[] content = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(content);
                        DataBufferUtils.release(dataBuffer);

                        String responseBody = new String(content, StandardCharsets.UTF_8);
                        logResponse(request, getDelegate(), responseBody, startTime);

                        // 返回新的 buffer 供后续使用
                        return super.writeWith(Flux.just(bufferFactory.wrap(content)));
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        // 处理空响应体的情况
                        logResponse(request, getDelegate(), "", startTime);
                        return Mono.empty();
                    }));
        }
    }

    /**
     * 记录基本请求信息（简洁格式）
     */
    private void logRequestBasic(ServerHttpRequest request) {
        if (!log.isDebugEnabled()) {
            return;
        }

        HttpMethod method = request.getMethod();
        String path = request.getURI().getPath();
        String queryParams = request.getURI().getQuery();
        String ip = request.getRemoteAddress() != null ?
            request.getRemoteAddress().getAddress().getHostAddress() : "unknown";

        // 获取关键请求头
        String contentType = request.getHeaders().getContentType() != null ?
            request.getHeaders().getContentType().toString() : "none";
        String userAgent = request.getHeaders().getFirst("User-Agent");

        // 构建简洁的请求日志
        StringBuilder logMsg = new StringBuilder();
        logMsg.append("📥 ").append(method).append(" ").append(path);

        if (queryParams != null && !queryParams.isEmpty()) {
            logMsg.append("?").append(truncate(queryParams, 100));
        }

        logMsg.append(" | IP: ").append(ip);
        logMsg.append(" | Type: ").append(contentType);

        if (userAgent != null) {
            logMsg.append(" | UA: ").append(truncate(userAgent, 50));
        }

        log.debug(logMsg.toString());
    }

    /**
     * 记录请求体（请求参数）
     */
    private void logRequestBody(String body) {
        if (!log.isDebugEnabled()) {
            return;
        }

        if (body != null && !body.isEmpty()) {
            log.debug("   📝 Request Body: {}", truncate(body, MAX_LOG_LENGTH));
        } else {
            log.debug("   📝 Request Body: (empty)");
        }
    }

    /**
     * 记录响应信息（简洁格式）
     */
    private void logResponse(ServerHttpRequest request, ServerHttpResponse response,
                             String body, long startTime) {
        if (!log.isDebugEnabled()) {
            return;
        }

        long duration = System.currentTimeMillis() - startTime;
        int statusCode = response.getStatusCode() != null ? response.getStatusCode().value() : 0;
        String method = request.getMethod().toString();
        String path = request.getURI().getPath();

        // 获取响应内容类型
        String responseType = response.getHeaders().getContentType() != null ?
            response.getHeaders().getContentType().toString() : "none";

        // 根据状态码选择图标
        String icon = statusCode >= 200 && statusCode < 300 ? "✅" :
                     statusCode >= 400 && statusCode < 500 ? "⚠️" :
                     statusCode >= 500 ? "❌" : "📤";

        // 构建简洁的响应日志
        StringBuilder logMsg = new StringBuilder();
        logMsg.append(icon).append(" ").append(method).append(" ").append(path)
              .append(" | Status: ").append(statusCode)
              .append(" | Time: ").append(duration).append("ms")
              .append(" | Type: ").append(responseType);

        log.debug(logMsg.toString());

        // 打印响应体（响应数据）
        if (body != null && !body.isEmpty()) {
            log.debug("   📝 Response Body: {}", truncate(body, MAX_LOG_LENGTH));
        } else {
            log.debug("   📝 Response Body: (empty)");
        }
    }

    /**
     * 判断是否应该记录请求体
     */
    private boolean shouldLogBody(ServerHttpRequest request) {
        MediaType contentType = request.getHeaders().getContentType();
        if (contentType == null) {
            return false;
        }

        // 记录常见的文本类型内容：JSON、XML、纯文本、表单数据
        return contentType.includes(MediaType.APPLICATION_JSON) ||
               contentType.includes(MediaType.APPLICATION_XML) ||
               contentType.includes(MediaType.TEXT_PLAIN) ||
               contentType.includes(MediaType.TEXT_HTML) ||
               contentType.includes(MediaType.TEXT_XML) ||
               contentType.includes(MediaType.APPLICATION_FORM_URLENCODED) ||
               contentType.toString().contains("json") ||  // 捕获自定义 JSON 类型
               contentType.toString().contains("xml") ||   // 捕获自定义 XML 类型
               contentType.toString().contains("text");    // 捕获所有文本类型
    }

    /**
     * 判断是否应该记录响应体
     */
    private boolean shouldLogBody(ServerHttpResponse response) {
        MediaType contentType = response.getHeaders().getContentType();
        if (contentType == null) {
            // 即使没有 Content-Type，也尝试记录（可能是空响应）
            return true;
        }

        // 记录常见的文本类型内容：JSON、XML、纯文本、HTML
        return contentType.includes(MediaType.APPLICATION_JSON) ||
               contentType.includes(MediaType.APPLICATION_XML) ||
               contentType.includes(MediaType.TEXT_PLAIN) ||
               contentType.includes(MediaType.TEXT_HTML) ||
               contentType.includes(MediaType.TEXT_XML) ||
               contentType.toString().contains("json") ||  // 捕获自定义 JSON 类型
               contentType.toString().contains("xml") ||   // 捕获自定义 XML 类型
               contentType.toString().contains("text");    // 捕获所有文本类型
    }

    /**
     * 截断过长的内容
     */
    private String truncate(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        if (content.length() > maxLength) {
            return content.substring(0, maxLength) + "...";
        }
        return content;
    }
}
