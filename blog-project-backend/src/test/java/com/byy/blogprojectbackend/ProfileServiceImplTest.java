package com.byy.blogprojectbackend;

import com.byy.blogprojectbackend.common.exception.ResourceNotFoundException;
import com.byy.blogprojectbackend.common.exception.VersionConflictException;
import com.byy.blogprojectbackend.guestbook.mapper.GuestbookMapper;
import com.byy.blogprojectbackend.post.mapper.PostMapper;
import com.byy.blogprojectbackend.profile.dto.UpdateProfileDTO;
import com.byy.blogprojectbackend.profile.dto.UpdateProfileStatusDTO;
import com.byy.blogprojectbackend.profile.entity.SpaceProfile;
import com.byy.blogprojectbackend.profile.mapper.SpaceProfileMapper;
import com.byy.blogprojectbackend.profile.service.impl.ProfileServiceImpl;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProfileServiceImplTest {

    private final SpaceProfileMapper profileMapper = mock(SpaceProfileMapper.class);
    private final ProfileServiceImpl profileService = new ProfileServiceImpl(
            profileMapper,
            mock(PostMapper.class),
            mock(GuestbookMapper.class)
    );

    @Test
    void updateProfile_whenProfileDoesNotExist_throwsResourceNotFound() {
        when(profileMapper.selectOwnerProfile()).thenReturn(null);

        assertThrows(
                ResourceNotFoundException.class,
                () -> profileService.updateProfile(validRequest())
        );
    }

    @Test
    void updateProfile_whenVersionIsStale_throwsVersionConflict() {
        SpaceProfile profile = new SpaceProfile();
        profile.setId(1L);
        when(profileMapper.selectOwnerProfile()).thenReturn(profile);
        when(profileMapper.updateOwnerProfile(any(), anyInt())).thenReturn(0);

        assertThrows(
                VersionConflictException.class,
                () -> profileService.updateProfile(validRequest())
        );
    }

    @Test
    void updateProfileStatus_rejectsFieldsLongerThanDatabaseColumns() {
        Validator validator = Validation
                .buildDefaultValidatorFactory()
                .getValidator();

        UpdateProfileDTO request = new UpdateProfileDTO(
                "玢",
                null,
                null,
                null,
                new UpdateProfileStatusDTO(
                        "a".repeat(65),
                        "b".repeat(256),
                        "c".repeat(33)
                ),
                0
        );

        assertEquals(3, validator.validate(request).size());
    }

    private UpdateProfileDTO validRequest() {
        return new UpdateProfileDTO(
                "玢",
                null,
                "Java Backend Developer",
                "记录代码，也记录生活。",
                new UpdateProfileStatusDTO(
                        "今日状态",
                        "正在写 BinSpace",
                        "☕"
                ),
                0
        );
    }
}
