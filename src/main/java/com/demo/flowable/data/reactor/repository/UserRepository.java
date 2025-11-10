package com.demo.flowable.data.reactor.repository;

import com.demo.flowable.data.entity.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * @author: e-Benben.Guo
 * @date: 2025/11
 * @desc:
 */
@Repository
public interface UserRepository extends ReactiveCrudRepository<User, Long> {


    @Query("SELECT * FROM sys_user WHERE username = :username")
    Mono<User> findByUsername(String username);

}
