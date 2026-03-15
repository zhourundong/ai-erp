package com.aierp.ai.tools;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 工具方法中文名称注解
 * 用于在前端展示工具调用时显示中文名称
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ToolName {
    /**
     * 工具方法的中文名称
     */
    String value();
}
