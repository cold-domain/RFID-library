package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.dto.CategoryDTO;
import com.library.entity.BookCategory;
import com.library.vo.CategoryVO;

import java.util.List;

/**
 * 图书分类服务接口
 */
public interface BookCategoryService extends IService<BookCategory> {

    /**
     * 获取分类树形结构
     */
    List<CategoryVO> getCategoryTree();

    /**
     * 获取所有启用的分类列表（扁平）
     */
    List<CategoryVO> getEnabledCategories();

    /**
     * 根据分类编码查询
     */
    BookCategory getByCategoryCode(String categoryCode);

    /**
     * 新增分类
     */
    void addCategory(CategoryDTO dto, Long creatorId);

    /**
     * 修改分类
     */
    void updateCategory(Long id, CategoryDTO dto);
}
