package com.demo.flowable.data.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.flowable.data.entity.User;
import com.demo.flowable.data.mapper.UserMapper;
import com.demo.flowable.data.service.UserDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author: e-Benben.Guo
 * @date: 2025/11
 * @desc:
 */
@Slf4j
@Service
public class UserDataServiceImpl extends ServiceImpl<UserMapper, User> implements UserDataService {
}
