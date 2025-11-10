package com.demo.flowable.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.flowable.dto.RoleDTO;
import com.demo.flowable.data.entity.Role;
import com.demo.flowable.data.entity.RolePermission;
import com.demo.flowable.data.mapper.PermissionMapper;
import com.demo.flowable.data.mapper.RoleMapper;
import com.demo.flowable.data.mapper.RolePermissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;

    /**
     * 创建角色
     *
     * @param roleDTO 角色DTO
     * @return 角色ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createRole(RoleDTO roleDTO) {
        log.info("创建角色: {}", roleDTO.getRoleCode());

        // 检查角色编码是否已存在
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getRoleCode, roleDTO.getRoleCode());
        Role existRole = roleMapper.selectOne(wrapper);
        if (existRole != null) {
            throw new RuntimeException("角色编码已存在: " + roleDTO.getRoleCode());
        }

        // 转换DTO为实体
        Role role = new Role();
        BeanUtils.copyProperties(roleDTO, role);

        // 保存角色
        roleMapper.insert(role);
        log.info("角色创建成功, ID: {}", role.getId());

        // 分配权限
        if (roleDTO.getPermissionIds() != null && !roleDTO.getPermissionIds().isEmpty()) {
            assignPermissions(role.getId(), roleDTO.getPermissionIds());
        }

        return role.getId();
    }

    /**
     * 更新角色
     *
     * @param id      角色ID
     * @param roleDTO 角色DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Long id, RoleDTO roleDTO) {
        log.info("更新角色: ID={}", id);

        // 检查角色是否存在
        Role existRole = roleMapper.selectById(id);
        if (existRole == null) {
            throw new RuntimeException("角色不存在: " + id);
        }

        // 如果修改了角色编码，检查角色编码是否已被占用
        if (StringUtils.hasText(roleDTO.getRoleCode()) && !roleDTO.getRoleCode().equals(existRole.getRoleCode())) {
            LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Role::getRoleCode, roleDTO.getRoleCode());
            Role duplicateRole = roleMapper.selectOne(wrapper);
            if (duplicateRole != null) {
                throw new RuntimeException("角色编码已存在: " + roleDTO.getRoleCode());
            }
        }

        // 转换DTO为实体
        Role role = new Role();
        BeanUtils.copyProperties(roleDTO, role);
        role.setId(id);

        // 更新角色
        roleMapper.updateById(role);
        log.info("角色更新成功, ID: {}", id);

        // 更新权限关联
        if (roleDTO.getPermissionIds() != null) {
            // 删除旧的权限关联
            LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RolePermission::getRoleId, id);
            rolePermissionMapper.delete(wrapper);

            // 添加新的权限关联
            if (!roleDTO.getPermissionIds().isEmpty()) {
                assignPermissions(id, roleDTO.getPermissionIds());
            }
        }
    }

    /**
     * 删除角色（逻辑删除）
     *
     * @param id 角色ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        log.info("删除角色: ID={}", id);

        // 检查角色是否存在
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在: " + id);
        }

        // 删除角色（逻辑删除）
        roleMapper.deleteById(id);

        // 删除角色权限关联
        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRoleId, id);
        rolePermissionMapper.delete(wrapper);

        log.info("角色删除成功, ID: {}", id);
    }

    /**
     * 根据ID获取角色
     *
     * @param id 角色ID
     * @return 角色DTO
     */
    public RoleDTO getRoleById(Long id) {
        log.info("查询角色: ID={}", id);

        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在: " + id);
        }

        // 转换为DTO
        RoleDTO roleDTO = convertToDTO(role);

        // 查询角色权限
        roleDTO.setPermissionIds(roleMapper.selectPermissionIdsByRoleId(id));
        roleDTO.setPermissions(permissionMapper.selectPermissionsByRoleId(id).stream()
                .map(permission -> {
                    var permissionDTO = new com.demo.flowable.dto.PermissionDTO();
                    BeanUtils.copyProperties(permission, permissionDTO);
                    return permissionDTO;
                })
                .collect(Collectors.toList()));

        return roleDTO;
    }

    /**
     * 获取角色列表（分页）
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param keyword  关键词（角色编码、角色名称）
     * @return 角色列表
     */
    public Page<RoleDTO> getRoleList(int pageNum, int pageSize, String keyword) {
        log.info("查询角色列表: pageNum={}, pageSize={}, keyword={}", pageNum, pageSize, keyword);

        // 构建查询条件
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Role::getRoleCode, keyword)
                    .or()
                    .like(Role::getRoleName, keyword));
        }
        wrapper.orderByDesc(Role::getCreatedTime);

        // 分页查询
        Page<Role> page = new Page<>(pageNum, pageSize);
        Page<Role> rolePage = roleMapper.selectPage(page, wrapper);

        // 转换为DTO
        Page<RoleDTO> dtoPage = new Page<>(pageNum, pageSize);
        dtoPage.setTotal(rolePage.getTotal());
        dtoPage.setRecords(rolePage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));

        return dtoPage;
    }

    /**
     * 为角色分配权限
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        log.info("为角色分配权限: roleId={}, permissionIds={}", roleId, permissionIds);

        // 检查角色是否存在
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new RuntimeException("角色不存在: " + roleId);
        }

        // 删除旧的权限关联
        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRoleId, roleId);
        rolePermissionMapper.delete(wrapper);

        // 添加新的权限关联
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permissionId);
                rolePermissionMapper.insert(rolePermission);
            }
        }

        log.info("权限分配成功");
    }

    /**
     * 将Role实体转换为RoleDTO
     *
     * @param role 角色实体
     * @return 角色DTO
     */
    private RoleDTO convertToDTO(Role role) {
        RoleDTO roleDTO = new RoleDTO();
        BeanUtils.copyProperties(role, roleDTO);
        return roleDTO;
    }
}
