package com.demo.flowable.auth;

import com.demo.flowable.data.reactor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author: e-Benben.Guo
 * @date: 2025/11
 * @desc:
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReactiveUserService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        userRepository.findByUsername(username);
        return null;
    }
}
