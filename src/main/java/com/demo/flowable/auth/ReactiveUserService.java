package com.demo.flowable.auth;

import com.demo.flowable.data.entity.User;
import com.demo.flowable.data.mapper.UserMapper;
import com.demo.flowable.data.reactor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 响应式用户详情服务
 * 实现 ReactiveUserDetailsService 接口，用于 Spring Security 认证
 *
 * @author e-Benben.Guo
 * @since 2025/11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReactiveUserService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * 根据用户名加载用户详情（响应式）
     * Spring Security 认证时会调用此方法
     *
     * @param username 用户名
     * @return Mono<UserDetails> 用户详情
     */
    @Override
    public Mono<UserDetails> findByUsername(String username) {
        log.debug("加载用户详情: {}", username);

        // 使用 R2DBC 响应式查询用户
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("用户不存在: " + username)))
                .flatMap(user -> {
                    // 检查用户状态
                    if (user.getStatus() == null || user.getStatus() != 1) {
                        return Mono.error(new RuntimeException("用户已被禁用"));
                    }

                    // 异步查询用户权限（MyBatis 同步查询需要在独立线程池中执行）
                    return Mono.fromCallable(() -> userMapper.selectPermissionCodesByUserId(user.getId()))
                            .subscribeOn(Schedulers.boundedElastic())
                            .map(permissions -> buildUserDetails(user, permissions))
                            .doOnNext(userDetails -> log.debug("用户详情加载成功: {}, 权限数: {}",
                                    username, userDetails.getAuthorities().size()));
                });
    }

    /**
     * 构建 Spring Security UserDetails 对象
     *
     * @param user 用户实体
     * @param permissions 权限列表
     * @return UserDetails
     */
    private UserDetails buildUserDetails(User user, List<String> permissions) {
        // 将权限字符串转换为 GrantedAuthority
        List<SimpleGrantedAuthority> authorities = permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // 如果权限列表为空，至少添加 ROLE_USER
        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(user.getStatus() != 1)
                .credentialsExpired(false)
                .disabled(user.getStatus() != 1)
                .build();
    }

    /**
     * 通过 R2DBC 查询用户（响应式）
     *
     * @param username 用户名
     * @return Mono<User>
     */
    public Mono<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
