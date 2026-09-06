package com.byy.blogprojectbackend.profile.controller;

import com.byy.blogprojectbackend.common.result.Result;
import com.byy.blogprojectbackend.profile.dto.UpdateProfileDTO;
import com.byy.blogprojectbackend.profile.service.ProfileService;
import com.byy.blogprojectbackend.site.vo.ProfileVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/owner/profile")
@RequiredArgsConstructor
public class OwnerProfileController {

    private final ProfileService profileService;

    @PutMapping
    public Result<ProfileVO> updateProfile(
            @Valid @RequestBody UpdateProfileDTO updateProfileDTO
    ) {
        return Result.success(
                profileService.updateProfile(updateProfileDTO)
        );
    }
}