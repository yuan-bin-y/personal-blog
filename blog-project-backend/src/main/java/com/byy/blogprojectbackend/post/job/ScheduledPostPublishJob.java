package com.byy.blogprojectbackend.post.job;

import com.byy.blogprojectbackend.post.entity.Post;
import com.byy.blogprojectbackend.post.mapper.PostAdvancedMapper;
import com.byy.blogprojectbackend.post.service.impl.OwnerAdvancedPostItemService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Publishes due SCHEDULED posts in small independent transactions. */
@Component
@RequiredArgsConstructor
public class ScheduledPostPublishJob {
    private static final Logger log = LoggerFactory.getLogger(ScheduledPostPublishJob.class);
    private final PostAdvancedMapper advancedMapper;
    private final OwnerAdvancedPostItemService itemService;

    @Scheduled(fixedDelayString = "${app.post.schedule-check-ms:30000}")
    public void publishDuePosts() {
        for (Post post : advancedMapper.selectDueScheduled(50)) {
            try {
                itemService.publishScheduledOne(post.getId());
            } catch (RuntimeException exception) {
                log.error("Scheduled post publication failed, postId={}", post.getId(), exception);
            }
        }
    }
}
