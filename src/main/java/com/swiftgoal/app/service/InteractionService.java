package com.swiftgoal.app.service;

import com.swiftgoal.app.repository.ArticleCommentRepository;
import com.swiftgoal.app.repository.ArticleLikeRepository;
import com.swiftgoal.app.repository.NewsArticleRepository;
import com.swiftgoal.app.repository.UserRepository;
import com.swiftgoal.app.repository.entity.ArticleComment;
import com.swiftgoal.app.repository.entity.ArticleLike;
import com.swiftgoal.app.repository.entity.NewsArticle;
import com.swiftgoal.app.repository.entity.User;
import com.swiftgoal.app.dto.CommentDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InteractionService {

    @Autowired
    private ArticleLikeRepository likeRepository;

    @Autowired
    private ArticleCommentRepository commentRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NewsArticleRepository newsArticleRepository;

    public void likeArticle(String username, Long articleId) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        NewsArticle article = newsArticleRepository.findById(articleId).orElseThrow(() -> new RuntimeException("Article not found"));

        if (likeRepository.findByUserAndNewsArticle(user, article).isEmpty()) {
            ArticleLike like = new ArticleLike();
            like.setUser(user);
            like.setNewsArticle(article);
            likeRepository.save(like);
        }
    }

    public void unlikeArticle(String username, Long articleId) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        NewsArticle article = newsArticleRepository.findById(articleId).orElseThrow(() -> new RuntimeException("Article not found"));
        
        likeRepository.findByUserAndNewsArticle(user, article).ifPresent(likeRepository::delete);
    }

    public ArticleComment addComment(String username, Long articleId, String content, Long parentCommentId) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        NewsArticle article = newsArticleRepository.findById(articleId).orElseThrow(() -> new RuntimeException("Article not found"));
        
        ArticleComment comment = new ArticleComment();
        comment.setUser(user);
        comment.setNewsArticle(article);
        comment.setContent(content);

        if (parentCommentId != null) {
            ArticleComment parent = commentRepository.findById(parentCommentId).orElseThrow(() -> new RuntimeException("Parent comment not found"));
            comment.setParentComment(parent);
        }

        return commentRepository.save(comment);
    }
    
    public List<CommentDto> getComments(Long articleId) {
        NewsArticle article = newsArticleRepository.findById(articleId).orElseThrow(() -> new RuntimeException("Article not found"));
        return commentRepository.findByNewsArticleOrderByCreatedAtDesc(article).stream()
                .filter(c -> c.getParentComment() == null) // Get only top-level comments
                .map(CommentDto::fromEntity)
                .collect(Collectors.toList());
    }

    public long getLikeCount(Long articleId) {
        NewsArticle article = newsArticleRepository.findById(articleId).orElseThrow(() -> new RuntimeException("Article not found"));
        return likeRepository.countByNewsArticle(article);
    }
    
    public boolean hasUserLiked(String username, Long articleId) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return false;
        
        NewsArticle article = newsArticleRepository.findById(articleId).orElseThrow(() -> new RuntimeException("Article not found"));
        return likeRepository.findByUserAndNewsArticle(user, article).isPresent();
    }
} 