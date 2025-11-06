package com.demo.flowable.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.flowable.common.Result;
import com.demo.flowable.dto.PermissionDTO;
import com.demo.flowable.service.PermissionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限控制器
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 创建权限
     *
     * @param permissionDTO 权限DTO
     * @return 权限ID
     */
    @PostMapping
    public Result<Long> createPermission(@Valid @RequestBody PermissionDTO permissionDTO) {
        log.info("创建权限: {}", permissionDTO.getPermissionCode());
        try {
            Long permissionId = permissionService.createPermission(permissionDTO);
            return Result.success("权限创建成功", permissionId);
        } catch (Exception e) {
            log.error("创建权限失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新权限
     *
     * @param id            权限ID
     * @param permissionDTO 权限DTO
     * @return 响应结果
     */
    @PutMapping("/{id}")
    public Result<Void> updatePermission(
            @NotNull(message = "权限ID不能为空") @PathVariable Long id,
            @Valid @RequestBody PermissionDTO permissionDTO) {
        log.info("更新权限: ID={}", id);
        try {
            permissionService.updatePermission(id, permissionDTO);
            return Result.success("权限更新成功");
        } catch (Exception e) {
            log.error("更新权限失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除权限
     *
     * @param id 权限ID
     * @return 响应结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deletePermission(@NotNull(message = "权限ID不能为空") @PathVariable Long id) {
        log.info("删除权限: ID={}", id);
        try {
            permissionService.deletePermission(id);
            return Result.success("权限删除成功");
        } catch (Exception e) {
            log.error("删除权限失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取权限
     *
     * @param id 权限ID
     * @return 权限DTO
     */
    @GetMapping("/{id}")
    public Result<PermissionDTO> getPermissionById(@NotNull(message = "权限ID不能为空") @PathVariable Long id) {
        log.info("查询权限: ID={}", id);
        try {
            PermissionDTO permissionDTO = permissionService.getPermissionById(id);
            return Result.success(permissionDTO);
        } catch (Exception e) {
            log.error("查询权限失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取权限列表（分页）
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param keyword  关键词
     * @return 权限列表
     */
    @GetMapping("/list")
    public Result<Page<PermissionDTO>> getPermissionList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        log.info("查询权限列表: pageNum={}, pageSize={}, keyword={}", pageNum, pageSize, keyword);
        try {
            Page<PermissionDTO> permissionPage = permissionService.getPermissionList(pageNum, pageSize, keyword);
            return Result.success(permissionPage);
        } catch (Exception e) {
            log.error("查询权限列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取菜单权限树
     *
     * @return 权限树
     */
    @GetMapping("/tree")
    public Result<List<PermissionDTO>> getMenuTree() {
        log.info("获取菜单权限树");
        try {
            List<PermissionDTO> permissionTree = permissionService.getMenuTree();
            return Result.success(permissionTree);
        } catch (Exception e) {
            log.error("获取菜单权限树失败", e);
            return Result.error(e.getMessage());
        }
    }
}
