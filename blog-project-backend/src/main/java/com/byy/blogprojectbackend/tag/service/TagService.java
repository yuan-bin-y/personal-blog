package com.byy.blogprojectbackend.tag.service;

import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.post.vo.TagVO;
import com.byy.blogprojectbackend.tag.dto.CreateTagDTO;
import com.byy.blogprojectbackend.tag.dto.UpdateTagDTO;
import com.byy.blogprojectbackend.tag.vo.TagAdminVO;

public interface TagService {
    PageVO<TagVO> listPublic(boolean used, int page, int pageSize);
    PageVO<TagAdminVO> listOwner(String status, int page, int pageSize);
    TagAdminVO create(CreateTagDTO dto);
    TagAdminVO update(Long id, UpdateTagDTO dto);
    void delete(Long id);
}
