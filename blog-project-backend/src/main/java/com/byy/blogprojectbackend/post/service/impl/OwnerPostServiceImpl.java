package com.byy.blogprojectbackend.post.service.impl;

import com.byy.blogprojectbackend.common.exception.ResourceNotFoundException;
import com.byy.blogprojectbackend.common.exception.VersionConflictException;
import com.byy.blogprojectbackend.common.id.IdGenerator;
import com.byy.blogprojectbackend.common.util.SlugUtils;
import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.post.dto.*;
import com.byy.blogprojectbackend.post.entity.Post;
import com.byy.blogprojectbackend.post.entity.PostMedia;
import com.byy.blogprojectbackend.post.mapper.PostMapper;
import com.byy.blogprojectbackend.post.mapper.projection.*;
import com.byy.blogprojectbackend.post.service.OwnerPostService;
import com.byy.blogprojectbackend.post.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

/**
 * Owner 内容写入实现。
 *
 * <p>Post 主表、标签关系和媒体关系在同一事务内变更，任何一步失败都会整体回滚。</p>
 */
@Service
@RequiredArgsConstructor
public class OwnerPostServiceImpl implements OwnerPostService {
    private static final String TECH="TECH", MOMENT="MOMENT", COVER="COVER", CONTENT="CONTENT";
    private final PostMapper postMapper;
    private final IdGenerator idGenerator;

    @Override
    public PageVO<PostSummaryVO> list(String type,String status,int page,int pageSize){
        long total=postMapper.countOwnerPosts(type,status);
        List<PostFeedRow> rows=postMapper.selectOwnerPosts(type,status,(long)(page-1)*pageSize,pageSize);
        Associations a=associations(rows);
        List<PostSummaryVO> items=rows.stream().map(r->summary(r,a.tags(r.getId()),a.media(r.getId()))).toList();
        long pages=(total+pageSize-1)/pageSize;
        return new PageVO<>(items,page,pageSize,total,pages,page<pages);
    }

    @Override @Transactional
    public TechDetailVO createTech(CreateTechPostDTO dto,Long ownerId){
        Long categoryId=parseId(dto.categoryId(),"categoryId");
        List<Long>tagIds=parseUniqueIds(dto.tagIds(),"tagIds"); validateTaxonomy(categoryId,tagIds);
        Long id=idGenerator.nextId(); Post post=base(id,TECH,dto.content(),dto.contentFormat(),dto.status(),ownerId);
        post.setSlug(SlugUtils.normalize(dto.title(),"tech")+"-"+id); post.setTitle(dto.title().trim()); post.setSummary(dto.summary().trim());
        post.setCategoryId(categoryId); post.setReadingTimeMinutes(readingTime(dto.content())); postMapper.insert(post);
        replaceTags(id,tagIds); replaceMedia(id,dto.cover()==null?List.of():List.of(dto.cover()),COVER);
        return getTech(id);
    }

    @Override public TechDetailVO getTech(Long id){
        PostFeedRow row=requireRow(id,TECH); Associations a=associations(List.of(row));
        PostSummaryVO s=summary(row,a.tags(id),a.media(id));
        return techDetail(s,row.getContent(),row.getContentFormat());
    }

    @Override @Transactional
    public TechDetailVO updateTech(Long id,UpdateTechPostDTO dto,Long ownerId){
        PostFeedRow old=requireRow(id,TECH); Long categoryId=parseId(dto.categoryId(),"categoryId");
        List<Long>tagIds=parseUniqueIds(dto.tagIds(),"tagIds"); validateTaxonomy(categoryId,tagIds);
        Post post=base(id,TECH,dto.content(),dto.contentFormat(),dto.status(),ownerId);
        post.setSlug(old.getSlug()); post.setTitle(dto.title().trim()); post.setSummary(dto.summary().trim());
        post.setCategoryId(categoryId); post.setReadingTimeMinutes(readingTime(dto.content()));
        post.setPublishedAt(publicationTime(old,dto.status()));
        update(post,dto.version()); replaceTags(id,tagIds);
        replaceMedia(id,dto.cover()==null?List.of():List.of(dto.cover()),COVER);
        return getTech(id);
    }

    @Override @Transactional public void deleteTech(Long id,int version,Long ownerId){delete(id,TECH,version,ownerId);}

    @Override @Transactional
    public MomentDetailVO createMoment(CreateMomentDTO dto,Long ownerId){
        Long id=idGenerator.nextId(); Post post=base(id,MOMENT,dto.content(),"PLAIN_TEXT",dto.status(),ownerId);
        postMapper.insert(post); replaceMedia(id,dto.images(),CONTENT); return getMoment(id);
    }

    @Override public MomentDetailVO getMoment(Long id){
        PostFeedRow row=requireRow(id,MOMENT); Associations a=associations(List.of(row));
        PostSummaryVO s=summary(row,List.of(),a.media(id)); return momentDetail(s,row.getContentFormat());
    }

    @Override @Transactional
    public MomentDetailVO updateMoment(Long id,UpdateMomentDTO dto,Long ownerId){
        PostFeedRow old=requireRow(id,MOMENT); Post post=base(id,MOMENT,dto.content(),"PLAIN_TEXT",dto.status(),ownerId);
        post.setPublishedAt(publicationTime(old,dto.status())); update(post,dto.version());
        replaceMedia(id,dto.images(),CONTENT); return getMoment(id);
    }

    @Override @Transactional public void deleteMoment(Long id,int version,Long ownerId){delete(id,MOMENT,version,ownerId);}

    private Post base(Long id,String type,String content,String format,String status,Long ownerId){
        Post post=new Post(); post.setId(id); post.setType(type); post.setContent(content.trim()); post.setContentFormat(format);
        post.setStatus(status); post.setPublishedAt("PUBLISHED".equals(status)?LocalDateTime.now(Clock.systemUTC()):null);
        post.setLikeCount(0); post.setCommentCount(0); post.setCreatedBy(ownerId); post.setUpdatedBy(ownerId); return post;
    }

    private LocalDateTime publicationTime(PostFeedRow old,String status){
        if("DRAFT".equals(status))return null;
        return old.getPublishedAt()==null?LocalDateTime.now(Clock.systemUTC()):old.getPublishedAt();
    }

    private void update(Post post,int version){
        if(postMapper.updateOwnerPost(post,version)!=1)throw new VersionConflictException("内容已被其他请求修改，请刷新后重试");
    }

    private void delete(Long id,String type,int version,Long ownerId){
        requireRow(id,type);
        if(postMapper.softDeleteOwnerPost(id,type,version,ownerId)!=1)throw new VersionConflictException("内容已被其他请求修改，请刷新后重试");
        postMapper.softDeletePostMedia(id);
    }

    private void validateTaxonomy(Long categoryId,List<Long>tagIds){
        if(postMapper.countActiveCategory(categoryId)!=1)throw new IllegalArgumentException("categoryId 对应的分类不存在或已停用");
        if(!tagIds.isEmpty()&&postMapper.countActiveTags(tagIds)!=tagIds.size())throw new IllegalArgumentException("tagIds 中包含不存在或已停用的标签");
    }

    private void replaceTags(Long postId,List<Long>tagIds){
        postMapper.deletePostTags(postId); for(Long tagId:tagIds)postMapper.insertPostTag(postId,tagId);
    }

    private void replaceMedia(Long postId,List<MediaInputDTO> media,String usage){
        postMapper.softDeletePostMedia(postId);
        for(int i=0;i<media.size();i++){
            MediaInputDTO input=media.get(i); PostMedia row=new PostMedia(); row.setId(idGenerator.nextId()); row.setPostId(postId);
            row.setUsageType(usage); row.setMediaType(input.mediaType()); row.setUrl(input.src().trim()); row.setPosterUrl(trim(input.poster()));
            row.setAltText(trim(input.alt())); row.setWidth(input.width()); row.setHeight(input.height()); row.setSortOrder(input.sortOrder()==null?i:input.sortOrder());
            postMapper.insertPostMedia(row);
        }
    }

    private PostFeedRow requireRow(Long id,String type){
        PostFeedRow row=postMapper.selectOwnerPostById(id,type); if(row==null)throw new ResourceNotFoundException(type+" 内容不存在"); return row;
    }

    private Associations associations(List<PostFeedRow> rows){
        if(rows.isEmpty())return new Associations(Map.of(),Map.of()); List<Long> ids=rows.stream().map(PostFeedRow::getId).toList();
        Map<Long,List<TagVO>>tags=new HashMap<>(); for(PostTagRow r:postMapper.selectTagsByPostIds(ids))tags.computeIfAbsent(r.getPostId(),k->new ArrayList<>()).add(new TagVO(String.valueOf(r.getTagId()),r.getTagName(),r.getTagSlug(),!Boolean.TRUE.equals(r.getTagDeleted())));
        Map<Long,List<MediaVO>>media=new HashMap<>(); for(PostMediaRow r:postMapper.selectMediaByPostIds(ids))media.computeIfAbsent(r.getPostId(),k->new ArrayList<>()).add(new MediaVO(String.valueOf(r.getId()),r.getUsageType(),r.getMediaType(),r.getUrl(),r.getPosterUrl(),r.getAltText(),r.getWidth(),r.getHeight(),r.getSortOrder()));
        return new Associations(tags,media);
    }

    private PostSummaryVO summary(PostFeedRow r,List<TagVO>tags,List<MediaVO>media){
        boolean tech=TECH.equals(r.getType()); PostAuthorVO author=new PostAuthorVO(String.valueOf(r.getAuthorUserId()),r.getAuthorName(),r.getAuthorAvatar());
        CategoryVO category=tech?new CategoryVO(String.valueOf(r.getCategoryId()),r.getCategoryName(),r.getCategorySlug(),r.getCategoryDescription(),!Boolean.TRUE.equals(r.getCategoryDeleted())):null;
        MediaVO cover=tech?media.stream().filter(m->COVER.equals(m.usageType())).findFirst().orElse(null):null;
        List<MediaVO>images=tech?List.of():media.stream().filter(m->CONTENT.equals(m.usageType())).toList();
        return new PostSummaryVO(String.valueOf(r.getId()),r.getType(),tech?r.getSlug():null,tech?r.getTitle():null,tech?r.getSummary():null,tech?null:r.getContent(),author,category,tech?tags:List.of(),cover,images,toInstant(r.getCreatedAt()),toInstant(r.getUpdatedAt()),toInstant(r.getPublishedAt()),tech?r.getReadingTime():null,r.getStatus(),r.getLikeCount(),r.getCommentCount(),r.getVersion());
    }

    private TechDetailVO techDetail(PostSummaryVO s,String content,String format){return new TechDetailVO(s.id(),s.type(),s.slug(),s.title(),s.summary(),content,s.author(),s.category(),s.tags(),s.cover(),s.images(),s.createdAt(),s.updatedAt(),s.publishedAt(),s.readingTime(),s.status(),s.likeCount(),s.commentCount(),s.version(),format,List.of());}
    private MomentDetailVO momentDetail(PostSummaryVO s,String format){return new MomentDetailVO(s.id(),s.type(),s.slug(),s.title(),s.summary(),s.content(),s.author(),s.category(),s.tags(),s.cover(),s.images(),s.createdAt(),s.updatedAt(),s.publishedAt(),s.readingTime(),s.status(),s.likeCount(),s.commentCount(),s.version(),format);}
    private int readingTime(String content){return Math.max(1,(content.codePointCount(0,content.length())+499)/500);}
    private Long parseId(String value,String field){try{return Long.valueOf(value);}catch(NumberFormatException e){throw new IllegalArgumentException(field+" 格式不正确");}}
    private List<Long> parseUniqueIds(List<String> values,String field){LinkedHashSet<Long> ids=new LinkedHashSet<>();for(String v:values)ids.add(parseId(v,field));if(ids.size()!=values.size())throw new IllegalArgumentException(field+" 不能重复");return List.copyOf(ids);}
    private String trim(String value){return value==null||value.isBlank()?null:value.trim();}
    private Instant toInstant(LocalDateTime value){return value==null?null:value.toInstant(ZoneOffset.UTC);}
    private record Associations(Map<Long,List<TagVO>>tags,Map<Long,List<MediaVO>>media){List<TagVO>tags(Long id){return tags.getOrDefault(id,List.of());}List<MediaVO>media(Long id){return media.getOrDefault(id,List.of());}}
}
