package com.demo.flowable.exception;

/**
 * 速率限制超限异常
 * 当用户请求超过速率限制时抛出此异常
 *
 * @author e-Benben.Guo
 * @date 2025/11
 */
public class RateLimitExceededException extends RuntimeException {

    private final String userId;
    private final int limit;
    private final long resetTimeSeconds;

    public RateLimitExceededException(String userId, int limit, long resetTimeSeconds) {
        super(String.format("用户 %s 请求超过速率限制（%d次/分钟），请在 %d 秒后重试",
                userId, limit, resetTimeSeconds));
        this.userId = userId;
        this.limit = limit;
        this.resetTimeSeconds = resetTimeSeconds;
    }

    public String getUserId() {
        return userId;
    }

    public int getLimit() {
        return limit;
    }

    public long getResetTimeSeconds() {
        return resetTimeSeconds;
    }
}
