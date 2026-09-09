package com.byy.blogprojectbackend.category.service.impl;

import com.byy.blogprojectbackend.category.dto.CreateCategoryDTO;
import com.byy.blogprojectbackend.category.dto.UpdateCategoryDTO;
import com.byy.blogprojectbackend.category.entity.Category;
import com.byy.blogprojectbackend.category.mapper.CategoryMapper;
import com.byy.blogprojectbackend.category.service.CategoryService;
import com.byy.blogprojectbackend.category.vo.CategoryAdminVO;
import com.byy.blogprojectbackend.common.exception.ResourceNotFoundException;
import com.byy.blogprojectbackend.common.exception.VersionConflictException;
import com.byy.blogprojectbackend.common.enums.FilterStatus;
import com.byy.blogprojectbackend.common.id.IdGenerator;
import com.byy.blogprojectbackend.common.util.SlugUtils;
import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.post.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/** 分类查询、唯一性写入和乐观锁停用。 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryMapper categoryMapper;
    private final IdGenerator idGenerator;

    @Override
    public PageVO<CategoryVO> listPublic(boolean used, int page, int pageSize) {
        long total = categoryMapper.countPublic(used);
        List<CategoryVO> items = categoryMapper.selectPublic(used, offset(page, pageSize), pageSize)
                .stream().map(this::toPublic).toList();
        return page(items, page, pageSize, total);
    }

    @Override
    public PageVO<CategoryAdminVO> listOwner(String status, int page, int pageSize) {
        String normalized = normalizeStatus(status);
        long total = categoryMapper.countOwner(normalized);
        List<CategoryAdminVO> items = categoryMapper.selectOwner(normalized, offset(page, pageSize), pageSize)
                .stream().map(this::toAdmin).toList();
        return page(items, page, pageSize, total);
    }

    @Override
    @Transactional
    public CategoryAdminVO create(CreateCategoryDTO dto) {
        Long id = idGenerator.nextId();
        Category category = new Category();
        category.setId(id);
        category.setName(dto.name().trim());
        category.setSlug(SlugUtils.normalize(dto.slug(), "category-" + id));
        category.setDescription(trimToNull(dto.description()));
        category.setSortOrder(dto.sortOrder());
        categoryMapper.insert(category);
        return toAdmin(require(id));
    }

    @Override
    @Transactional
    public CategoryAdminVO update(Long id, UpdateCategoryDTO dto) {
        requireActive(id);
        Category category = new Category();
        category.setId(id);
        category.setName(dto.name().trim());
        category.setSlug(SlugUtils.normalize(dto.slug(), "category-" + id));
        category.setDescription(trimToNull(dto.description()));
        category.setSortOrder(dto.sortOrder());
        if (categoryMapper.updateWithVersion(category, dto.version()) != 1) {
            throw new VersionConflictException("分类已被其他请求修改，请刷新后重试");
        }
        return toAdmin(require(id));
    }

    @Override
    @Transactional
    public void delete(Long id, int version) {
        requireActive(id);
        if (categoryMapper.softDelete(id, version) != 1) {
            throw new VersionConflictException("分类已被其他请求修改，请刷新后重试");
        }
    }

    private Category require(Long id) {
        Category value = categoryMapper.selectAnyById(id);
        if (value == null) throw new ResourceNotFoundException("分类不存在");
        return value;
    }

    private void requireActive(Long id) {
        if (Boolean.TRUE.equals(require(id).getDeleted())) throw new ResourceNotFoundException("分类不存在");
    }

    private CategoryVO toPublic(Category c) {
        return new CategoryVO(String.valueOf(c.getId()), c.getName(), c.getSlug(), c.getDescription(), true);
    }

    private CategoryAdminVO toAdmin(Category c) {
        return new CategoryAdminVO(String.valueOf(c.getId()), c.getName(), c.getSlug(), c.getDescription(),
                !Boolean.TRUE.equals(c.getDeleted()), c.getSortOrder(), c.getVersion(),
                c.getUpdatedAt().toInstant(ZoneOffset.UTC));
    }

    private String normalizeStatus(String status) {
        return FilterStatus.parse(status).code();
    }

    private String trimToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private long offset(int page, int size) { return (long) (page - 1) * size; }
    private <T> PageVO<T> page(List<T> items, int page, int size, long total) {
        long pages = (total + size - 1) / size;
        return new PageVO<>(items, page, size, total, pages, page < pages);
    }
}
