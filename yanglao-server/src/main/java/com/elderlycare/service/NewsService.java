package com.elderlycare.service;

import com.elderlycare.common.PageResult;
import com.elderlycare.common.Result;
import com.elderlycare.dto.NewsQueryDTO;
import com.elderlycare.entity.News;

import java.util.List;

public interface NewsService {
    Result<List<?>> categories();

    Result<PageResult<News>> page(NewsQueryDTO query);

    Result<News> detail(Long id);

    Result<PageResult<News>> search(String keyword, Integer page, Integer pageSize);

    Result<List<News>> hot(Integer limit);

    Result<Void> addFavorite(Long userId, Long newsId);

    Result<Void> removeFavorite(Long userId, Long newsId);

    Result<List<News>> myFavorites(Long userId);

    /** 待审核资讯（系统管理员） */
    Result<List<News>> pending();

    /** 发布资讯：提交后进入待审核状态 */
    Result<Void> create(com.elderlycare.dto.NewsCreateDTO dto, Long authorId);

    /** 审核资讯（系统管理员） */
    Result<Void> review(Long id, boolean approved);
}
