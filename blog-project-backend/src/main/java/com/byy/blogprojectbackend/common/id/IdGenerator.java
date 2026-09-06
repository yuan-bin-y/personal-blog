package com.byy.blogprojectbackend.common.id;

import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import org.springframework.stereotype.Component;

/**
 * 统一生成数据库 BIGINT 主键。
 *
 * <p>数据库表没有 AUTO_INCREMENT，因此所有新增业务对象都从这里取 ID。</p>
 */
@Component
public class IdGenerator {

    public Long nextId() {
        return DefaultIdentifierGenerator.getInstance().nextId(null);
    }
}
