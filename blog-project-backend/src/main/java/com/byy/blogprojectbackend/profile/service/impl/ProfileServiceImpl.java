package com.byy.blogprojectbackend.profile.service.impl;

import com.byy.blogprojectbackend.guestbook.mapper.GuestbookMapper;
import com.byy.blogprojectbackend.common.exception.ResourceNotFoundException;
import com.byy.blogprojectbackend.common.exception.VersionConflictException;
import com.byy.blogprojectbackend.post.mapper.PostMapper;
import com.byy.blogprojectbackend.post.enums.PostType;
import com.byy.blogprojectbackend.profile.dto.UpdateProfileDTO;
import com.byy.blogprojectbackend.profile.entity.SpaceProfile;
import com.byy.blogprojectbackend.profile.mapper.SpaceProfileMapper;
import com.byy.blogprojectbackend.profile.service.ProfileService;
import com.byy.blogprojectbackend.site.vo.ProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final SpaceProfileMapper spaceProfileMapper;
    private final PostMapper postMapper;
    private final GuestbookMapper guestbookMapper;

    @Override
    public ProfileVO updateProfile(UpdateProfileDTO updateProfileDTO) {

        // 1. 查询当前 Owner Profile
        SpaceProfile profile = spaceProfileMapper.selectOwnerProfile();

        if (profile == null) {
            throw new ResourceNotFoundException(
                    "Owner 资料不存在"
            );
        }

        // 2. 将前端提交的新数据写入 Entity
        profile.setDisplayName(updateProfileDTO.name());
        profile.setAvatarUrl(updateProfileDTO.avatar());
        profile.setRoleText(updateProfileDTO.role());
        profile.setBio(updateProfileDTO.bio());

        profile.setStatusLabel(
                updateProfileDTO.status().label()
        );
        profile.setStatusText(
                updateProfileDTO.status().text()
        );
        profile.setStatusEmoji(
                updateProfileDTO.status().emoji()
        );

        // 3. 根据 version 更新
        int affectedRows = spaceProfileMapper.updateOwnerProfile(
                profile,
                updateProfileDTO.version()
        );

        // 4. 更新失败，说明 version 不一致
        if (affectedRows == 0) {
            throw new VersionConflictException(
                    "个人资料已被其他请求修改，请刷新后重试"
            );
        }

        // 5. 查询更新后的最新 Profile
        SpaceProfile updatedProfile =
                spaceProfileMapper.selectOwnerProfile();

        if (updatedProfile == null) {
            throw new ResourceNotFoundException(
                    "Owner 资料不存在"
            );
        }

        // 6. 查询 ProfileVO 需要的统计数据
        int techPostCount =
                postMapper.countPublishedByType(PostType.TECH.code());

        int momentCount =
                postMapper.countPublishedByType(PostType.MOMENT.code());

        int guestbookCount =
                guestbookMapper.countPublicTopLevel();

        // 7. 组装 status
        ProfileVO.ProfileStatusVO status =
                new ProfileVO.ProfileStatusVO(
                        updatedProfile.getStatusLabel(),
                        updatedProfile.getStatusText(),
                        updatedProfile.getStatusEmoji()
                );

        // 8. 组装 stats
        ProfileVO.ProfileStatsVO stats =
                new ProfileVO.ProfileStatsVO(
                        techPostCount,
                        momentCount,
                        guestbookCount
                );

        // 9. 返回完整 ProfileVO
        return new ProfileVO(
                String.valueOf(updatedProfile.getId()),
                String.valueOf(updatedProfile.getUserId()),
                updatedProfile.getDisplayName(),
                updatedProfile.getAvatarUrl(),
                updatedProfile.getRoleText(),
                updatedProfile.getBio(),
                status,
                stats,
                updatedProfile.getVersion(),
                updatedProfile.getUpdatedAt() == null
                        ? null
                        : updatedProfile.getUpdatedAt()
                        .toInstant(ZoneOffset.UTC)
        );
    }
}
