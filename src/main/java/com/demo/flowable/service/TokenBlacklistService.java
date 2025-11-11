package com.demo.flowable.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token 黑名单服务
 * 用于管理已废除的 Token，防止被废除的 Token 继续使用
 * 使用内存存储（ConcurrentHashMap），适用于单机或小规模集群
 *
 * 如需支持大规模集群，可替换为 Redis 实现
 *
 * @author e-Benben.Guo
 * @date 2025/11
 */
@Slf4j
@Service
public class TokenBlacklistService {

    /**
     * 黑名单存储
     * Key: Token ID (jti)
     * Value: 过期时间戳（毫秒）
     */
    private final ConcurrentHashMap<String, Long> blacklist = new ConcurrentHashMap<>();

    /**
     * 将 Token 添加到黑名单
     *
     * @param tokenId Token ID (jti)
     * @param expirationTime Token 过期时间（毫秒时间戳）
     * @return Mono<Void>
     */
    public Mono<Void> addToBlacklist(String tokenId, long expirationTime) {
        return Mono.fromRunnable(() -> {
            blacklist.put(tokenId, expirationTime);
            log.debug("Token 已添加到黑名单: {}, 过期时间: {}", tokenId, Instant.ofEpochMilli(expirationTime));
        });
    }

    /**
     * 检查 Token 是否在黑名单中
     *
     * @param tokenId Token ID (jti)
     * @return Mono<Boolean> 是否在黑名单中
     */
    public Mono<Boolean> isBlacklisted(String tokenId) {
        return Mono.fromSupplier(() -> {
            if (tokenId == null) {
                return false;
            }
            return blacklist.containsKey(tokenId);
        });
    }

    /**
     * 从黑名单中移除 Token
     *
     * @param tokenId Token ID (jti)
     * @return Mono<Void>
     */
    public Mono<Void> removeFromBlacklist(String tokenId) {
        return Mono.fromRunnable(() -> {
            blacklist.remove(tokenId);
            log.debug("Token 已从黑名单移除: {}", tokenId);
        });
    }

    /**
     * 清理已过期的黑名单记录
     * 每小时执行一次
     */
    @Scheduled(fixedRate = 3600000) // 1 小时
    public void cleanupExpiredTokens() {
        long now = System.currentTimeMillis();
        int removedCount = 0;

        // 遍历黑名单，移除已过期的 Token
        for (var entry : blacklist.entrySet()) {
            if (entry.getValue() < now) {
                blacklist.remove(entry.getKey());
                removedCount++;
            }
        }

        if (removedCount > 0) {
            log.info("清理黑名单: 移除 {} 个已过期的 Token", removedCount);
        }
    }

    /**
     * 获取黑名单大小
     *
     * @return 黑名单中的 Token 数量
     */
    public int getBlacklistSize() {
        return blacklist.size();
    }

    /**
     * 清空黑名单（仅用于测试）
     */
    public void clearBlacklist() {
        blacklist.clear();
        log.warn("黑名单已清空");
    }
}
