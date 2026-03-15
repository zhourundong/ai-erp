package com.aierp.service;

import com.aierp.entity.Organization;
import com.aierp.mapper.OrganizationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 组织服务
 */
@Service
@RequiredArgsConstructor
public class OrganizationService extends ServiceImpl<OrganizationMapper, Organization> {

    private final OrganizationMapper organizationMapper;

    /**
     * 获取组织树
     */
    public List<OrganizationTreeVO> getOrganizationTree() {
        List<Organization> all = organizationMapper.selectList(
                new LambdaQueryWrapper<Organization>().orderByAsc(Organization::getId)
        );
        return buildTree(all, null);
    }

    private List<OrganizationTreeVO> buildTree(List<Organization> all, Long parentId) {
        List<OrganizationTreeVO> tree = new ArrayList<>();
        for (Organization org : all) {
            if ((parentId == null && org.getParentId() == null) ||
                    (parentId != null && parentId.equals(org.getParentId()))) {
                OrganizationTreeVO node = new OrganizationTreeVO();
                node.setId(org.getId());
                node.setCode(org.getCode());
                node.setName(org.getName());
                node.setType(org.getType());
                node.setParentId(org.getParentId());
                node.setStatus(org.getStatus());
                node.setDescription(org.getDescription());
                node.setChildren(buildTree(all, org.getId()));
                tree.add(node);
            }
        }
        return tree;
    }

    /**
     * 创建组织
     */
    @Transactional
    public Organization createOrganization(Organization org) {
        if (org.getStatus() == null) {
            org.setStatus("ACTIVE");
        }
        organizationMapper.insert(org);
        return org;
    }

    /**
     * 更新组织
     */
    @Transactional
    public Organization updateOrganization(Organization org) {
        organizationMapper.updateById(org);
        return org;
    }

    /**
     * 删除组织
     */
    @Transactional
    public void deleteOrganization(Long id) {
        // 检查是否有子组织
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Organization::getParentId, id);
        long count = organizationMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("存在子组织，无法删除");
        }
        organizationMapper.deleteById(id);
    }

    /**
     * 获取组织详情
     */
    public Organization getOrganizationById(Long id) {
        return organizationMapper.selectById(id);
    }

    /**
     * 组织树VO
     */
    @lombok.Data
    public static class OrganizationTreeVO {
        private Long id;
        private String code;
        private String name;
        private String type;
        private Long parentId;
        private String status;
        private String description;
        private List<OrganizationTreeVO> children;
    }
}
