package com.demo.flowable.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.flowable.common.Result;
import com.demo.flowable.dto.UserDTO;
import com.demo.flowable.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 创建用户
     *
     * @param userDTO 用户DTO
     * @return 用户ID
     */
    @PostMapping
    public Result<Long> createUser(@Valid @RequestBody UserDTO userDTO) {
        log.info("创建用户: {}", userDTO.getUsername());
        try {
            Long userId = userService.createUser(userDTO);
            return Result.success("用户创建成功", userId);
        } catch (Exception e) {
            log.error("创建用户失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新用户
     *
     * @param id      用户ID
     * @param userDTO 用户DTO
     * @return 响应结果
     */
    @PutMapping("/{id}")
    public Result<Void> updateUser(
            @NotNull(message = "用户ID不能为空") @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO) {
        log.info("更新用户: ID={}", id);
        try {
            userService.updateUser(id, userDTO);
            return Result.success("用户更新成功");
        } catch (Exception e) {
            log.error("更新用户失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 响应结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@NotNull(message = "用户ID不能为空") @PathVariable Long id) {
        log.info("删除用户: ID={}", id);
        try {
            userService.deleteUser(id);
            return Result.success("用户删除成功");
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取用户
     *
     * @param id 用户ID
     * @return 用户DTO
     */
    @GetMapping("/{id}")
    public Result<UserDTO> getUserById(@NotNull(message = "用户ID不能为空") @PathVariable Long id) {
        log.info("查询用户: ID={}", id);
        try {
            UserDTO userDTO = userService.getUserById(id);
            return Result.success(userDTO);
        } catch (Exception e) {
            log.error("查询用户失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户列表（分页）
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param keyword  关键词
     * @return 用户列表
     */
    @GetMapping("/list")
    public Result<Page<UserDTO>> getUserList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        log.info("查询用户列表: pageNum={}, pageSize={}, keyword={}", pageNum, pageSize, keyword);
        try {
            Page<UserDTO> userPage = userService.getUserList(pageNum, pageSize, keyword);
            return Result.success(userPage);
        } catch (Exception e) {
            log.error("查询用户列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 为用户分配角色
     *
     * @param id      用户ID
     * @param roleIds 角色ID列表
     * @return 响应结果
     */
    @PostMapping("/{id}/roles")
    public Result<Void> assignRoles(
            @NotNull(message = "用户ID不能为空") @PathVariable Long id,
            @RequestBody List<Long> roleIds) {
        log.info("为用户分配角色: userId={}, roleIds={}", id, roleIds);
        try {
            userService.assignRoles(id, roleIds);
            return Result.success("角色分配成功");
        } catch (Exception e) {
            log.error("角色分配失败", e);
            return Result.error(e.getMessage());
        }
    }
}
