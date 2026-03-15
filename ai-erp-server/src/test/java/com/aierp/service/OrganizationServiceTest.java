package com.aierp.service;

import com.aierp.entity.Organization;
import com.aierp.mapper.OrganizationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.Serializable;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 组织服务测试
 */
@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationMapper organizationMapper;

    @InjectMocks
    private OrganizationService organizationService;

    private Organization testOrg;

    @BeforeEach
    void setUp() {
        testOrg = new Organization();
        testOrg.setId(1L);
        testOrg.setCode("ORG001");
        testOrg.setName("测试部门");
        testOrg.setType("DEPARTMENT");
        testOrg.setStatus("ACTIVE");
    }

    @Test
    void testCreateOrganization_Success() {
        // Given
        Organization newOrg = new Organization();
        newOrg.setName("新部门");

        when(organizationMapper.insert(any(Organization.class))).thenAnswer(invocation -> {
            Organization o = invocation.getArgument(0);
            o.setId(2L);
            return 1;
        });

        // When
        Organization created = organizationService.createOrganization(newOrg);

        // Then
        assertNotNull(created);
        assertEquals("ACTIVE", created.getStatus());
        verify(organizationMapper).insert(any(Organization.class));
    }

    @Test
    void testGetOrganizationById() {
        // Given
        when(organizationMapper.selectById(1L)).thenReturn(testOrg);

        // When
        Organization found = organizationService.getOrganizationById(1L);

        // Then
        assertNotNull(found);
        assertEquals("ORG001", found.getCode());
        assertEquals("测试部门", found.getName());
    }

    @Test
    void testUpdateOrganization() {
        // Given
        testOrg.setName("更新后的部门");
        when(organizationMapper.updateById(any(Organization.class))).thenReturn(1);

        // When
        Organization updated = organizationService.updateOrganization(testOrg);

        // Then
        assertEquals("更新后的部门", updated.getName());
        verify(organizationMapper).updateById(any(Organization.class));
    }

    @Test
    void testDeleteOrganization_Success() {
        // Given
        when(organizationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(organizationMapper.deleteById(any(Serializable.class))).thenReturn(1);

        // When
        organizationService.deleteOrganization(1L);

        // Then
        verify(organizationMapper).deleteById(any(Serializable.class));
    }

    @Test
    void testDeleteOrganization_HasChildren() {
        // Given
        when(organizationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);

        // When & Then
        assertThrows(RuntimeException.class, () -> organizationService.deleteOrganization(1L));
        verify(organizationMapper, never()).deleteById(any(Serializable.class));
    }

    @Test
    void testGetOrganizationTree() {
        // Given
        Organization childOrg = new Organization();
        childOrg.setId(2L);
        childOrg.setParentId(1L);
        childOrg.setName("子部门");

        when(organizationMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(testOrg, childOrg));

        // When
        List<OrganizationService.OrganizationTreeVO> tree = organizationService.getOrganizationTree();

        // Then
        assertNotNull(tree);
        assertEquals(1, tree.size());
        assertEquals("测试部门", tree.get(0).getName());
        assertEquals(1, tree.get(0).getChildren().size());
    }
}
