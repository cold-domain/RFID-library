package com.library.controller;

import com.library.common.utils.ContextHolder;
import com.library.common.vo.Result;
import com.library.dto.CategoryDTO;
import com.library.service.BookCategoryService;
import com.library.vo.CategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 图书分类管理控制器（馆员权限）
 */
@RestController
@RequestMapping("/librarian/categories")
public class BookCategoryController {

    @Autowired
    private BookCategoryService bookCategoryService;

    /**
     * 查询分类树
     */
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('category:view')")
    public Result<List<CategoryVO>> tree() {
        List<CategoryVO> tree = bookCategoryService.getCategoryTree();
        return Result.success(tree);
    }

    /**
     * 查询所有启用分类（扁平列表）
     */
    @GetMapping
    @PreAuthorize("hasAuthority('category:view')")
    public Result<List<CategoryVO>> list() {
        List<CategoryVO> categories = bookCategoryService.getEnabledCategories();
        return Result.success(categories);
    }

    /**
     * 新增分类
     */
    @PostMapping
    @PreAuthorize("hasAuthority('category:create')")
    public Result<?> add(@Valid @RequestBody CategoryDTO dto) {
        bookCategoryService.addCategory(dto, ContextHolder.getCurrentUserId());
        return Result.success("新增分类成功");
    }

    /**
     * 修改分类
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('category:update')")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody CategoryDTO dto) {
        bookCategoryService.updateCategory(id, dto);
        return Result.success("修改分类成功");
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('category:delete')")
    public Result<?> delete(@PathVariable Long id) {
        bookCategoryService.removeById(id);
        return Result.success("删除分类成功");
    }
}
