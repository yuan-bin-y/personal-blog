package com.byy.blogprojectbackend.media.service;

import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.media.enums.MediaType;
import com.byy.blogprojectbackend.media.enums.MediaUsageType;
import com.byy.blogprojectbackend.media.vo.MediaAssetVO;
import org.springframework.web.multipart.MultipartFile;

public interface MediaService {
    MediaAssetVO upload(MultipartFile file, MediaUsageType usageType, Long ownerId);
    PageVO<MediaAssetVO> list(MediaType mediaType, MediaUsageType usageType, int page, int pageSize);
    void delete(Long mediaId, Long ownerId);
}
