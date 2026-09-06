package com.byy.blogprojectbackend.tag.service.impl;

import com.byy.blogprojectbackend.common.exception.ResourceNotFoundException;
import com.byy.blogprojectbackend.common.id.IdGenerator;
import com.byy.blogprojectbackend.common.util.SlugUtils;
import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.post.vo.TagVO;
import com.byy.blogprojectbackend.tag.dto.CreateTagDTO;
import com.byy.blogprojectbackend.tag.dto.UpdateTagDTO;
import com.byy.blogprojectbackend.tag.entity.Tag;
import com.byy.blogprojectbackend.tag.mapper.TagMapper;
import com.byy.blogprojectbackend.tag.service.TagService;
import com.byy.blogprojectbackend.tag.vo.TagAdminVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZoneOffset;
import java.util.List;

/** 标签查询与逻辑停用；post_tag 历史关联不会被删除。 */
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {
    private final TagMapper tagMapper;
    private final IdGenerator idGenerator;

    public PageVO<TagVO> listPublic(boolean used, int page, int pageSize) {
        long total=tagMapper.countPublic(used);
        var items=tagMapper.selectPublic(used, offset(page,pageSize),pageSize).stream().map(this::toPublic).toList();
        return page(items,page,pageSize,total);
    }
    public PageVO<TagAdminVO> listOwner(String status,int page,int pageSize) {
        String normalized=normalizeStatus(status); long total=tagMapper.countOwner(normalized);
        var items=tagMapper.selectOwner(normalized,offset(page,pageSize),pageSize).stream().map(this::toAdmin).toList();
        return page(items,page,pageSize,total);
    }
    @Transactional public TagAdminVO create(CreateTagDTO dto) {
        Long id=idGenerator.nextId(); Tag tag=new Tag(); tag.setId(id); tag.setName(dto.name().trim());
        tag.setSlug(SlugUtils.normalize(dto.slug(),"tag-"+id)); tagMapper.insert(tag); return toAdmin(require(id));
    }
    @Transactional public TagAdminVO update(Long id,UpdateTagDTO dto) {
        requireActive(id); Tag tag=new Tag(); tag.setId(id); tag.setName(dto.name().trim());
        tag.setSlug(SlugUtils.normalize(dto.slug(),"tag-"+id)); tagMapper.updateActive(tag); return toAdmin(require(id));
    }
    @Transactional public void delete(Long id) { requireActive(id); tagMapper.softDelete(id); }
    private Tag require(Long id){Tag t=tagMapper.selectAnyById(id);if(t==null)throw new ResourceNotFoundException("标签不存在");return t;}
    private void requireActive(Long id){if(Boolean.TRUE.equals(require(id).getDeleted()))throw new ResourceNotFoundException("标签不存在");}
    private TagVO toPublic(Tag t){return new TagVO(String.valueOf(t.getId()),t.getName(),t.getSlug(),true);}
    private TagAdminVO toAdmin(Tag t){return new TagAdminVO(String.valueOf(t.getId()),t.getName(),t.getSlug(),!Boolean.TRUE.equals(t.getDeleted()),t.getUpdatedAt().toInstant(ZoneOffset.UTC));}
    private String normalizeStatus(String status){String v=status==null?"ALL":status.toUpperCase();if(!List.of("ALL","ACTIVE","DISABLED").contains(v))throw new IllegalArgumentException("status 参数不正确");return v;}
    private long offset(int p,int s){return(long)(p-1)*s;}
    private <T> PageVO<T> page(List<T> items,int p,int s,long total){long pages=(total+s-1)/s;return new PageVO<>(items,p,s,total,pages,p<pages);}
}
