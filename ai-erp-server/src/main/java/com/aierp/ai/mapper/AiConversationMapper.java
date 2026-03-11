package com.aierp.ai.mapper;

import com.aierp.ai.entity.AiConversation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI对话记录 Mapper
 */
@Mapper
public interface AiConversationMapper extends BaseMapper<AiConversation> {
}
