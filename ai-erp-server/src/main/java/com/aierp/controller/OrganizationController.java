package com.aierp.controller;

import com.aierp.common.Result;
import com.aierp.entity.Organization;
import com.aierp.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 组织管理控制器
 */
@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    /**
     * 获取组织树
     */
    @GetMapping("/tree")
    public ResponseEntity<Result<List<OrganizationService.OrganizationTreeVO>>> getOrganizationTree() {
        List<OrganizationService.OrganizationTreeVO> tree = organizationService.getOrganizationTree();
        return ResponseEntity.ok(Result.success(tree));
    }

    /**
     * 获取组织详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<Organization>> getOrganization(@PathVariable Long id) {
        Organization org = organizationService.getOrganizationById(id);
        return ResponseEntity.ok(Result.success(org));
    }

    /**
     * 创建组织
     */
    @PostMapping
    public ResponseEntity<Result<Organization>> createOrganization(@RequestBody Organization org) {
        Organization created = organizationService.createOrganization(org);
        return ResponseEntity.ok(Result.success(created));
    }

    /**
     * 更新组织
     */
    @PutMapping("/{id}")
    public ResponseEntity<Result<Organization>> updateOrganization(
            @PathVariable Long id,
            @RequestBody Organization org) {
        org.setId(id);
        Organization updated = organizationService.updateOrganization(org);
        return ResponseEntity.ok(Result.success(updated));
    }

    /**
     * 删除组织
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteOrganization(@PathVariable Long id) {
        organizationService.deleteOrganization(id);
        return ResponseEntity.ok(Result.success());
    }
}
