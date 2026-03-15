package com.aierp.controller;

import com.aierp.common.Result;
import com.aierp.entity.Customer;
import com.aierp.service.CustomerService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public Result<Page<Customer>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return Result.success(customerService.pageCustomers(pageNum, pageSize, keyword, status));
    }

    @GetMapping("/{id}")
    public Result<Customer> get(@PathVariable Long id) {
        return Result.success(customerService.getById(id));
    }

    @PostMapping
    public Result<Customer> create(@RequestBody Customer customer) {
        return Result.success(customerService.createCustomer(customer));
    }

    @PutMapping("/{id}")
    public Result<Customer> update(@PathVariable Long id, @RequestBody Customer customer) {
        customer.setId(id);
        return Result.success(customerService.updateCustomer(customer));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return Result.success();
    }
}
