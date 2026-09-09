package com.byy.blogprojectbackend.tag.service.impl;

import com.byy.blogprojectbackend.common.exception.ResourceNotFoundException;
import com.byy.blogprojectbackend.common.enums.FilterStatus;
import com.byy.blogprojectbackend.common.id.IdGenerator;
import com.byy.blogprojectbackend.common.util.SlugUtils;
import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.post.vo.TagVO;
import com.byy.blogprojectbackend.tag.dto.CreateTagDTO;
import com.byy.blogprojectbackend.tag.dto.UpdateTagDTO;
import com.byy.blogprojectbackend.tag.entity.Tag;
import com.byy.blogprojectbackend.tag.mapper.TagMapper;
import com.byy.blogprojectbackend.tag.service.TagService;
import com.byy.blogprojectbackend.tag.vo.TagAdminVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZoneOffset;
import java.util.List;

/** 标签查询与逻辑停用；post_tag 历史关联不会被删除。 */
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;
    private final IdGenerator idGenerator;

    @Override
    public PageVO<TagVO> listPublic(boolean used, int page, int pageSize) {
        long total = tagMapper.countPublic(used);
        List<TagVO> items = tagMapper.selectPublic(used, offset(page, pageSize), pageSize)
                .stream()
                .map(this::toPublic)
                .toList();
        return page(items, page, pageSize, total);
    }

    @Override
    public PageVO<TagAdminVO> listOwner(String status, int page, int pageSize) {
        String normalized = FilterStatus.parse(status).code();
        long total = tagMapper.countOwner(normalized);
        List<TagAdminVO> items = tagMapper.selectOwner(normalized, offset(page, pageSize), pageSize)
                .stream()
                .map(this::toAdmin)
                .toList();
        return page(items, page, pageSize, total);
    }

    @Override
    @Transactional
    public TagAdminVO create(CreateTagDTO dto) {
        Long id = idGenerator.nextId();
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(dto.name().trim());
        tag.setSlug(SlugUtils.normalize(dto.slug(), "tag-" + id));
        tagMapper.insert(tag);
        return toAdmin(require(id));
    }

    @Override
    @Transactional
    public TagAdminVO update(Long id, UpdateTagDTO dto) {
        requireActive(id);
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(dto.name().trim());
        tag.setSlug(SlugUtils.normalize(dto.slug(), "tag-" + id));
        tagMapper.updateActive(tag);
        return toAdmin(require(id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        requireActive(id);
        tagMapper.softDelete(id);
    }

    private Tag require(Long id) {
        Tag tag = tagMapper.selectAnyById(id);
        if (tag == null) {
            throw new ResourceNotFoundException("标签不存在");
        }
        return tag;
    }

    private void requireActive(Long id) {
        if (Boolean.TRUE.equals(require(id).getDeleted())) {
            throw new ResourceNotFoundException("标签不存在");
        }
    }

    private TagVO toPublic(Tag tag) {
        return new TagVO(String.valueOf(tag.getId()), tag.getName(), tag.getSlug(), true);
    }

    private TagAdminVO toAdmin(Tag tag) {
        return new TagAdminVO(
                String.valueOf(tag.getId()),
                tag.getName(),
                tag.getSlug(),
                !Boolean.TRUE.equals(tag.getDeleted()),
                tag.getUpdatedAt().toInstant(ZoneOffset.UTC)
        );
    }

    private long offset(int page, int pageSize) {
        return (long) (page - 1) * pageSize;
    }

    private <T> PageVO<T> page(List<T> items, int page, int pageSize, long total) {
        long totalPages = (total + pageSize - 1) / pageSize;
        return new PageVO<>(items, page, pageSize, total, totalPages, page < totalPages);
    }
}
