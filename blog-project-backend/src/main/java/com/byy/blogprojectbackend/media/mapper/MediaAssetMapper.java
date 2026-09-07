package com.byy.blogprojectbackend.media.mapper;

import com.byy.blogprojectbackend.media.entity.MediaAsset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MediaAssetMapper {
    int insert(@Param("asset") MediaAsset asset);
    MediaAsset selectById(@Param("id") Long id);
    MediaAsset selectForUpdate(@Param("id") Long id);
    int markActive(@Param("id") Long id, @Param("url") String url);
    int markUploadFailed(@Param("id") Long id, @Param("error") String error);
    long countActive(@Param("mediaType") String mediaType, @Param("usageType") String usageType);
    List<MediaAsset> selectActivePage(
            @Param("mediaType") String mediaType,
            @Param("usageType") String usageType,
            @Param("offset") long offset,
            @Param("pageSize") int pageSize
    );
    int countReferences(@Param("url") String url);
    int markDeleting(@Param("id") Long id, @Param("ownerId") Long ownerId);
    int markDeleted(@Param("id") Long id, @Param("ownerId") Long ownerId);
    int markDeleteFailed(@Param("id") Long id, @Param("error") String error);
    int markFailedUploadDeleting(@Param("id") Long id, @Param("ownerId") Long ownerId);
    List<MediaAsset> selectRetryableFailures(@Param("limit") int limit);
}
