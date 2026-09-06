package com.byy.blogprojectbackend.archive.service;

import com.byy.blogprojectbackend.archive.vo.ArchiveVO;

/**
 * Visitor 公开归档业务。
 */
public interface ArchiveService {

    ArchiveVO getPublicArchive(
            Integer year,
            String type,
            int page,
            int pageSize
    );
}
