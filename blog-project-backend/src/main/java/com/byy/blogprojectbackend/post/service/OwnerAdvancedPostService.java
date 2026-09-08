package com.byy.blogprojectbackend.post.service;

import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.post.dto.*;
import com.byy.blogprojectbackend.post.vo.*;
import org.springframework.web.multipart.MultipartFile;

public interface OwnerAdvancedPostService {
    PostSummaryVO schedule(Long postId, SchedulePostDTO dto, Long ownerId);
    PostSummaryVO cancelSchedule(Long postId, int version, Long ownerId);
    AutosaveVO autosave(Long postId, AutosavePostDTO dto, Long ownerId);
    PageVO<PostVersionVO> versions(Long postId, int page, int pageSize);
    PostVersionDetailVO version(Long postId, Long versionId);
    PostSummaryVO restoreVersion(Long postId, Long versionId, RestoreVersionDTO dto, Long ownerId);
    PageVO<PostSummaryVO> trash(String type, int page, int pageSize);
    PostSummaryVO restoreTrash(Long postId, int version, Long ownerId);
    BatchPostResultVO batchPublish(BatchPostDTO dto, Long ownerId);
    BatchPostResultVO batchDelete(BatchPostDTO dto, Long ownerId);
    TechDetailVO updateSlug(Long id, UpdateSlugDTO dto, Long ownerId);
    MarkdownImportVO importMarkdown(MultipartFile file);
}
