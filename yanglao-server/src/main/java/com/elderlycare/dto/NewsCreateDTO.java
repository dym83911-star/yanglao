package com.elderlycare.dto;

import lombok.Data;

/**
 * 发布资讯请求体（健康管理员 / 系统管理员）
 */
@Data
public class NewsCreateDTO {
    private String title;
    private String excerpt;
    private String content;
    private String category;
    private String coverUrl;
    private String source;
    private String sourceUrl;
    private Integer isHot;
}
