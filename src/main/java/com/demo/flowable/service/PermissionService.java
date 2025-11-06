package com.demo.flowable.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.flowable.dto.PermissionDTO;
import com.demo.flowable.entity.Permission;
import com.demo.flowable.mapper.PermissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限服务层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionMapper permissionMapper;

    /**
     * 创建权限
     *
     * @param permissionDTO 权限DTO
     * @return 权限ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createPermission(PermissionDTO permissionDTO) {
        log.info("创建权限: {}", permissionDTO.getPermissionCode());

        // 检查权限编码是否已存在
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getPermissionCode, permissionDTO.getPermissionCode());
        Permission existPermission = permissionMapper.selectOne(wrapper);
        if (existPermission != null) {
            throw new RuntimeException("权限编码已存在: " + permissionDTO.getPermissionCode());
        }

        // 如果有父级权限，检查父级权限是否存在
        if (permissionDTO.getParentId() != null && permissionDTO.getParentId() > 0) {
            Permission parentPermission = permissionMapper.selectById(permissionDTO.getParentId());
            if (parentPermission == null) {
                throw new RuntimeException("父级权限不存在: " + permissionDTO.getParentId());
            }
        }

        // 转换DTO为实体
        Permission permission = new Permission();
        BeanUtils.copyProperties(permissionDTO, permission);

        // 默认排序为0
        if (permission.getSortOrder() == null) {
            permission.setSortOrder(0);
        }

        // 保存权限
        permissionMapper.insert(permission);
        log.info("权限创建成功, ID: {}", permission.getId());

        return permission.getId();
    }

    /**
     * 更新权限
     *
     * @param id            权限ID
     * @param permissionDTO 权限DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePermission(Long id, PermissionDTO permissionDTO) {
        log.info("更新权限: ID={}", id);

        // 检查权限是否存在
        Permission existPermission = permissionMapper.selectById(id);
        if (existPermission == null) {
            throw new RuntimeException("权限不存在: " + id);
        }

        // 如果修改了权限编码，检查权限编码是否已被占用
        if (StringUtils.hasText(permissionDTO.getPermissionCode())
                && !permissionDTO.getPermissionCode().equals(existPermission.getPermissionCode())) {
            LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Permission::getPermissionCode, permissionDTO.getPermissionCode());
            Permission duplicatePermission = permissionMapper.selectOne(wrapper);
            if (duplicatePermission != null) {
                throw new RuntimeException("权限编码已存在: " + permissionDTO.getPermissionCode());
            }
        }

        // 如果有父级权限，检查父级权限是否存在
        if (permissionDTO.getParentId() != null && permissionDTO.getParentId() > 0) {
            // 不能将自己设置为父级权限
            if (permissionDTO.getParentId().equals(id)) {
                throw new RuntimeException("不能将自己设置为父级权限");
            }
            Permission parentPermission = permissionMapper.selectById(permissionDTO.getParentId());
            if (parentPermission == null) {
                throw new RuntimeException("父级权限不存在: " + permissionDTO.getParentId());
            }
        }

        // 转换DTO为实体
        Permission permission = new Permission();
        BeanUtils.copyProperties(permissionDTO, permission);
        permission.setId(id);

        // 更新权限
        permissionMapper.updateById(permission);
        log.info("权限更新成功, ID: {}", id);
    }

    /**
     * 删除权限（逻辑删除）
     *
     * @param id 权限ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletePermission(Long id) {
        log.info("删除权限: ID={}", id);

        // 检查权限是否存在
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new RuntimeException("权限不存在: " + id);
        }

        // 检查是否有子权限
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getParentId, id);
        Long childCount = permissionMapper.selectCount(wrapper);
        if (childCount > 0) {
            throw new RuntimeException("该权限下存在子权限，无法删除");
        }

        // 删除权限（逻辑删除）
        permissionMapper.deleteById(id);
        log.info("权限删除成功, ID: {}", id);
    }

    /**
     * 根据ID获取权限
     *
     * @param id 权限ID
     * @return 权限DTO
     */
    public PermissionDTO getPermissionById(Long id) {
        log.info("查询权限: ID={}", id);

        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new RuntimeException("权限不存在: " + id);
        }

        // 转换为DTO
        PermissionDTO permissionDTO = convertToDTO(permission);

        return permissionDTO;
    }

    /**
     * 获取权限列表（分页）
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param keyword  关键词（权限编码、权限名称）
     * @return 权限列表
     */
    public Page<PermissionDTO> getPermissionList(int pageNum, int pageSize, String keyword) {
        log.info("查询权限列表: pageNum={}, pageSize={}, keyword={}", pageNum, pageSize, keyword);

        // 构建查询条件
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Permission::getPermissionCode, keyword)
                    .or()
                    .like(Permission::getPermissionName, keyword));
        }
        wrapper.orderByAsc(Permission::getSortOrder)
                .orderByDesc(Permission::getCreatedTime);

        // 分页查询
        Page<Permission> page = new Page<>(pageNum, pageSize);
        Page<Permission> permissionPage = permissionMapper.selectPage(page, wrapper);

        // 转换为DTO
        Page<PermissionDTO> dtoPage = new Page<>(pageNum, pageSize);
        dtoPage.setTotal(permissionPage.getTotal());
        dtoPage.setRecords(permissionPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));

        return dtoPage;
    }

    /**
     * 获取菜单权限树
     *
     * @return 权限树
     */
    public List<PermissionDTO> getMenuTree() {
        log.info("获取菜单权限树");

        // 查询所有菜单权限
        List<Permission> allPermissions = permissionMapper.selectMenuPermissions();

        // 转换为DTO
        List<PermissionDTO> permissionDTOList = allPermissions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // 构建树形结构
        return buildTree(permissionDTOList, 0L);
    }

    /**
     * 构建权限树形结构
     *
     * @param allPermissions 所有权限列表
     * @param parentId       父级权限ID
     * @return 权限树
     */
    private List<PermissionDTO> buildTree(List<PermissionDTO> allPermissions, Long parentId) {
        List<PermissionDTO> tree = new ArrayList<>();

        for (PermissionDTO permission : allPermissions) {
            // 找到当前父级下的所有子权限
            if (permission.getParentId() != null && permission.getParentId().equals(parentId)) {
                // 递归查找子权限
                permission.setChildren(buildTree(allPermissions, permission.getId()));
                tree.add(permission);
            } else if (parentId == 0L && (permission.getParentId() == null || permission.getParentId() == 0L)) {
                // 顶级权限
                permission.setChildren(buildTree(allPermissions, permission.getId()));
                tree.add(permission);
            }
        }

        return tree;
    }

    /**
     * 将Permission实体转换为PermissionDTO
     *
     * @param permission 权限实体
     * @return 权限DTO
     */
    private PermissionDTO convertToDTO(Permission permission) {
        PermissionDTO permissionDTO = new PermissionDTO();
        BeanUtils.copyProperties(permission, permissionDTO);
        return permissionDTO;
    }
}
