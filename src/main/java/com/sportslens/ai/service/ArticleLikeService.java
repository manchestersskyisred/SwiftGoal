package com.sportslens.ai.service;

import com.sportslens.ai.domain.ArticleLike;
import com.sportslens.ai.domain.NewsArticle;
import com.sportslens.ai.domain.User;
import com.sportslens.ai.repository.ArticleLikeRepository;
import com.sportslens.ai.repository.NewsArticleRepository;
import com.sportslens.ai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ArticleLikeService {
    
    @Autowired
    private ArticleLikeRepository articleLikeRepository;
    
    @Autowired
    private NewsArticleRepository newsArticleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 点赞文章
     */
    @Transactional
    public Map<String, Object> likeArticle(Long articleId, String username) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 查找用户和文章
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("用户不存在"));
            
            NewsArticle article = newsArticleRepository.findById(articleId)
                    .orElseThrow(() -> new IllegalStateException("文章不存在"));
            
            // 检查是否已经点赞
            if (articleLikeRepository.existsByUserAndNewsArticle(user, article)) {
                result.put("success", false);
                result.put("message", "您已经点赞过此文章");
                result.put("isLiked", true);
                result.put("likeCount", article.getLikeCount());
                return result;
            }
            
            // 创建点赞记录
            ArticleLike articleLike = new ArticleLike(user, article);
            articleLikeRepository.save(articleLike);
            
            // 更新文章点赞数量
            article.setLikeCount(article.getLikeCount() + 1);
            newsArticleRepository.save(article);
            
            result.put("success", true);
            result.put("message", "点赞成功");
            result.put("isLiked", true);
            result.put("likeCount", article.getLikeCount());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "点赞失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 取消点赞
     */
    @Transactional
    public Map<String, Object> unlikeArticle(Long articleId, String username) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 查找用户和文章
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("用户不存在"));
            
            NewsArticle article = newsArticleRepository.findById(articleId)
                    .orElseThrow(() -> new IllegalStateException("文章不存在"));
            
            // 查找点赞记录
            Optional<ArticleLike> articleLike = articleLikeRepository.findByUserAndNewsArticle(user, article);
            
            if (articleLike.isEmpty()) {
                result.put("success", false);
                result.put("message", "您还未点赞此文章");
                result.put("isLiked", false);
                result.put("likeCount", article.getLikeCount());
                return result;
            }
            
            // 删除点赞记录
            articleLikeRepository.delete(articleLike.get());
            
            // 更新文章点赞数量
            article.setLikeCount(Math.max(0, article.getLikeCount() - 1));
            newsArticleRepository.save(article);
            
            result.put("success", true);
            result.put("message", "取消点赞成功");
            result.put("isLiked", false);
            result.put("likeCount", article.getLikeCount());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "取消点赞失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 切换点赞状态（点赞/取消点赞）
     */
    @Transactional
    public Map<String, Object> toggleLike(Long articleId, String username) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("用户不存在"));
            
            NewsArticle article = newsArticleRepository.findById(articleId)
                    .orElseThrow(() -> new IllegalStateException("文章不存在"));
            
            // 检查是否已经点赞
            if (articleLikeRepository.existsByUserAndNewsArticle(user, article)) {
                return unlikeArticle(articleId, username);
            } else {
                return likeArticle(articleId, username);
            }
            
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "操作失败: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * 检查用户是否点赞了某篇文章
     */
    public boolean isLikedByUser(Long articleId, String username) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("用户不存在"));
            
            NewsArticle article = newsArticleRepository.findById(articleId)
                    .orElseThrow(() -> new IllegalStateException("文章不存在"));
            
            return articleLikeRepository.existsByUserAndNewsArticle(user, article);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取文章的点赞状态和数量
     */
    public Map<String, Object> getArticleLikeStatus(Long articleId, String username) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            NewsArticle article = newsArticleRepository.findById(articleId)
                    .orElseThrow(() -> new IllegalStateException("文章不存在"));
            
            result.put("likeCount", article.getLikeCount());
            
            if (username != null) {
                result.put("isLiked", isLikedByUser(articleId, username));
            } else {
                result.put("isLiked", false);
            }
            
            result.put("success", true);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取点赞状态失败: " + e.getMessage());
            result.put("likeCount", 0);
            result.put("isLiked", false);
        }
        
        return result;
    }
}