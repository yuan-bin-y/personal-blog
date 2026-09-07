package com.byy.blogprojectbackend.guestbook.service;

import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.guestbook.dto.CreateGuestbookDTO;
import com.byy.blogprojectbackend.guestbook.dto.UpdateGuestbookDTO;
import com.byy.blogprojectbackend.guestbook.vo.GuestbookVO;
import com.byy.blogprojectbackend.interaction.dto.ReplyDTO;
import com.byy.blogprojectbackend.interaction.service.ReplyResult;

public interface GuestbookService {
    PageVO<GuestbookVO> list(int page, int pageSize, Long currentUserId);
    GuestbookVO create(CreateGuestbookDTO dto, Long userId);
    GuestbookVO updateOwnEntry(Long entryId, UpdateGuestbookDTO dto, Long userId);
    void deleteOwnEntry(Long entryId, Long userId);
    ReplyResult<GuestbookVO> reply(Long entryId, ReplyDTO dto,Long ownerId);
    void delete(Long entryId,Long ownerId);
}
