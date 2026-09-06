package com.byy.blogprojectbackend.category.service;

import com.byy.blogprojectbackend.category.dto.CreateCategoryDTO;
import com.byy.blogprojectbackend.category.dto.UpdateCategoryDTO;
import com.byy.blogprojectbackend.category.vo.CategoryAdminVO;
import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.post.vo.CategoryVO;

public interface CategoryService {
    PageVO<CategoryVO> listPublic(boolean used, int page, int pageSize);
    PageVO<CategoryAdminVO> listOwner(String status, int page, int pageSize);
    CategoryAdminVO create(CreateCategoryDTO dto);
    CategoryAdminVO update(Long id, UpdateCategoryDTO dto);
    void delete(Long id, int version);
}
