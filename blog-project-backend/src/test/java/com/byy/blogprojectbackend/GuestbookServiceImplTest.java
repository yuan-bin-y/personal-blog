package com.byy.blogprojectbackend;

import com.byy.blogprojectbackend.common.exception.ForbiddenOperationException;
import com.byy.blogprojectbackend.common.exception.ResourceNotFoundException;
import com.byy.blogprojectbackend.common.id.IdGenerator;
import com.byy.blogprojectbackend.guestbook.dto.CreateGuestbookDTO;
import com.byy.blogprojectbackend.guestbook.dto.UpdateGuestbookDTO;
import com.byy.blogprojectbackend.guestbook.entity.GuestbookEntry;
import com.byy.blogprojectbackend.guestbook.mapper.GuestbookMapper;
import com.byy.blogprojectbackend.guestbook.mapper.projection.GuestbookRow;
import com.byy.blogprojectbackend.guestbook.service.impl.GuestbookServiceImpl;
import com.byy.blogprojectbackend.guestbook.vo.GuestbookVO;
import com.byy.blogprojectbackend.profile.entity.SpaceProfile;
import com.byy.blogprojectbackend.profile.mapper.SpaceProfileMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GuestbookServiceImplTest {

    private final GuestbookMapper guestbookMapper = mock(GuestbookMapper.class);
    private final SpaceProfileMapper profileMapper = mock(SpaceProfileMapper.class);
    private final IdGenerator idGenerator = mock(IdGenerator.class);
    private final GuestbookServiceImpl guestbookService = new GuestbookServiceImpl(
            guestbookMapper,
            profileMapper,
            idGenerator
    );

    @Test
    void create_buildsAuthorFromProfileAndReturnsOwnedEntry() {
        long userId = 202L;
        long entryId = 303L;
        SpaceProfile profile = profile(userId);
        GuestbookRow row = row(entryId, userId, "来都来了，留下一个脚印。");

        when(profileMapper.selectByUserId(userId)).thenReturn(profile);
        when(idGenerator.nextId()).thenReturn(entryId);
        when(guestbookMapper.insertTopLevel(any(GuestbookEntry.class))).thenReturn(1);
        when(guestbookMapper.selectTopLevelView(entryId)).thenReturn(row);

        GuestbookVO result = guestbookService.create(
                new CreateGuestbookDTO("  来都来了，留下一个脚印。  "),
                userId
        );

        ArgumentCaptor<GuestbookEntry> captor = ArgumentCaptor.forClass(GuestbookEntry.class);
        verify(guestbookMapper).insertTopLevel(captor.capture());
        assertEquals(userId, captor.getValue().getAuthorUserId());
        assertEquals("来都来了，留下一个脚印。", captor.getValue().getContent());
        assertTrue(result.ownedByMe());
    }

    @Test
    void updateOwnEntry_whenCurrentUserIsAuthor_updatesContent() {
        long entryId = 303L;
        long userId = 202L;
        GuestbookEntry existing = entry(entryId, userId);
        GuestbookRow updated = row(entryId, userId, "修改后的留言");

        when(guestbookMapper.selectAny(entryId)).thenReturn(existing);
        when(guestbookMapper.updateOwnEntry(entryId, userId, "修改后的留言")).thenReturn(1);
        when(guestbookMapper.selectTopLevelView(entryId)).thenReturn(updated);

        GuestbookVO result = guestbookService.updateOwnEntry(
                entryId,
                new UpdateGuestbookDTO("  修改后的留言  "),
                userId
        );

        assertEquals("修改后的留言", result.content());
        assertTrue(result.ownedByMe());
    }

    @Test
    void updateOwnEntry_whenCurrentUserIsNotAuthor_throwsForbidden() {
        long entryId = 303L;
        when(guestbookMapper.selectAny(entryId)).thenReturn(entry(entryId, 202L));

        assertThrows(
                ForbiddenOperationException.class,
                () -> guestbookService.updateOwnEntry(
                        entryId,
                        new UpdateGuestbookDTO("不能修改别人的留言"),
                        999L
                )
        );

        verify(guestbookMapper, never()).updateOwnEntry(any(), any(), any());
    }

    @Test
    void deleteOwnEntry_whenCurrentUserIsAuthor_softDeletesEntryAndReply() {
        long entryId = 303L;
        long userId = 202L;
        when(guestbookMapper.selectAny(entryId)).thenReturn(entry(entryId, userId));
        when(guestbookMapper.softDeleteOwnEntry(entryId, userId)).thenReturn(1);

        guestbookService.deleteOwnEntry(entryId, userId);

        verify(guestbookMapper).softDeleteReply(entryId, userId);
    }

    @Test
    void deleteOwnEntry_whenConditionalUpdateMisses_throwsResourceNotFound() {
        long entryId = 303L;
        long userId = 202L;
        when(guestbookMapper.selectAny(entryId)).thenReturn(entry(entryId, userId));
        when(guestbookMapper.softDeleteOwnEntry(entryId, userId)).thenReturn(0);

        assertThrows(
                ResourceNotFoundException.class,
                () -> guestbookService.deleteOwnEntry(entryId, userId)
        );

        verify(guestbookMapper, never()).softDeleteReply(any(), any());
    }

    private SpaceProfile profile(long userId) {
        SpaceProfile profile = new SpaceProfile();
        profile.setUserId(userId);
        profile.setDisplayName("暖光访客");
        profile.setAvatarUrl("/media/visitor.png");
        return profile;
    }

    private GuestbookEntry entry(long entryId, long userId) {
        GuestbookEntry entry = new GuestbookEntry();
        entry.setId(entryId);
        entry.setAuthorUserId(userId);
        entry.setParentId(null);
        entry.setDeleted(false);
        return entry;
    }

    private GuestbookRow row(long entryId, long userId, String content) {
        GuestbookRow row = new GuestbookRow();
        row.setId(entryId);
        row.setAuthorUserId(userId);
        row.setAuthorName("暖光访客");
        row.setAuthorAvatar("/media/visitor.png");
        row.setContent(content);
        row.setCreatedAt(LocalDateTime.of(2026, 9, 7, 12, 0));
        return row;
    }
}
