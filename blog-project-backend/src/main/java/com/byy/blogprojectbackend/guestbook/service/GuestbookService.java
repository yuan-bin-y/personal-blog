package com.byy.blogprojectbackend.guestbook.service;

import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.guestbook.vo.GuestbookVO;
import com.byy.blogprojectbackend.interaction.dto.ReplyDTO;
import com.byy.blogprojectbackend.interaction.service.ReplyResult;

public interface GuestbookService {
    PageVO<GuestbookVO> list(int page,int pageSize);
    ReplyResult<GuestbookVO> reply(Long entryId, ReplyDTO dto,Long ownerId);
    void delete(Long entryId,Long ownerId);
}
