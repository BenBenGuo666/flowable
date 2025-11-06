package com.demo.flowable.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.flowable.common.Result;
import com.demo.flowable.dto.RoleDTO;
import com.demo.flowable.service.RoleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色控制器
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * 创建角色
     *
     * @param roleDTO 角色DTO
     * @return 角色ID
     */
    @PostMapping
    public Result<Long> createRole(@Valid @RequestBody RoleDTO roleDTO) {
        log.info("创建角色: {}", roleDTO.getRoleCode());
        try {
            Long roleId = roleService.createRole(roleDTO);
            return Result.success("角色创建成功", roleId);
        } catch (Exception e) {
            log.error("创建角色失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新角色
     *
     * @param id      角色ID
     * @param roleDTO 角色DTO
     * @return 响应结果
     */
    @PutMapping("/{id}")
    public Result<Void> updateRole(
            @NotNull(message = "角色ID不能为空") @PathVariable Long id,
            @Valid @RequestBody RoleDTO roleDTO) {
        log.info("更新角色: ID={}", id);
        try {
            roleService.updateRole(id, roleDTO);
            return Result.success("角色更新成功");
        } catch (Exception e) {
            log.error("更新角色失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 响应结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@NotNull(message = "角色ID不能为空") @PathVariable Long id) {
        log.info("删除角色: ID={}", id);
        try {
            roleService.deleteRole(id);
            return Result.success("角色删除成功");
        } catch (Exception e) {
            log.error("删除角色失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取角色
     *
     * @param id 角色ID
     * @return 角色DTO
     */
    @GetMapping("/{id}")
    public Result<RoleDTO> getRoleById(@NotNull(message = "角色ID不能为空") @PathVariable Long id) {
        log.info("查询角色: ID={}", id);
        try {
            RoleDTO roleDTO = roleService.getRoleById(id);
            return Result.success(roleDTO);
        } catch (Exception e) {
            log.error("查询角色失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取角色列表（分页）
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param keyword  关键词
     * @return 角色列表
     */
    @GetMapping("/list")
    public Result<Page<RoleDTO>> getRoleList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        log.info("查询角色列表: pageNum={}, pageSize={}, keyword={}", pageNum, pageSize, keyword);
        try {
            Page<RoleDTO> rolePage = roleService.getRoleList(pageNum, pageSize, keyword);
            return Result.success(rolePage);
        } catch (Exception e) {
            log.error("查询角色列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 为角色分配权限
     *
     * @param id            角色ID
     * @param permissionIds 权限ID列表
     * @return 响应结果
     */
    @PostMapping("/{id}/permissions")
    public Result<Void> assignPermissions(
            @NotNull(message = "角色ID不能为空") @PathVariable Long id,
            @RequestBody List<Long> permissionIds) {
        log.info("为角色分配权限: roleId={}, permissionIds={}", id, permissionIds);
        try {
            roleService.assignPermissions(id, permissionIds);
            return Result.success("权限分配成功");
        } catch (Exception e) {
            log.error("权限分配失败", e);
            return Result.error(e.getMessage());
        }
    }
}
