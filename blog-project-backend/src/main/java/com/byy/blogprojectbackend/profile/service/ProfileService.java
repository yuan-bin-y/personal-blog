package com.byy.blogprojectbackend.profile.service;

import com.byy.blogprojectbackend.profile.dto.UpdateProfileDTO;
import com.byy.blogprojectbackend.site.vo.ProfileVO;

/**
 * Owner 个人资料业务接口。
 */
public interface ProfileService {

    /**
     * 更新 Owner 个人资料。
     *
     * @param updateProfileDTO 更新资料请求
     * @return 更新后的 Profile
     */
    ProfileVO updateProfile(UpdateProfileDTO updateProfileDTO);
}