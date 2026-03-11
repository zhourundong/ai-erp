package com.aierp.ai.mapper;

import com.aierp.ai.entity.AiPrediction;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI预测记录 Mapper
 */
@Mapper
public interface AiPredictionMapper extends BaseMapper<AiPrediction> {
}
