package com.byy.blogprojectbackend.post.service;

import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.post.dto.*;
import com.byy.blogprojectbackend.post.vo.*;

public interface OwnerPostService {
    PageVO<PostSummaryVO> list(String type, String status, int page, int pageSize);
    TechDetailVO createTech(CreateTechPostDTO dto, Long ownerId);
    TechDetailVO getTech(Long id);
    TechDetailVO updateTech(Long id, UpdateTechPostDTO dto, Long ownerId);
    void deleteTech(Long id, int version, Long ownerId);
    MomentDetailVO createMoment(CreateMomentDTO dto, Long ownerId);
    MomentDetailVO getMoment(Long id);
    MomentDetailVO updateMoment(Long id, UpdateMomentDTO dto, Long ownerId);
    void deleteMoment(Long id, int version, Long ownerId);
}
