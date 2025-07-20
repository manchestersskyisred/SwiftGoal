package com.swiftgoal.app.controller;

import com.swiftgoal.app.repository.entity.ArticleComment;
import com.swiftgoal.app.service.InteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.swiftgoal.app.dto.CommentDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/articles/{articleId}")
public class InteractionController {

    @Autowired
    private InteractionService interactionService;

    @PostMapping("/like")
    public ResponseEntity<?> toggleLike(@PathVariable Long articleId, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Please log in to like articles.");
        }
        String username = userDetails.getUsername();
        
        boolean hasLiked = interactionService.hasUserLiked(username, articleId);
        if (hasLiked) {
            interactionService.unlikeArticle(username, articleId);
        } else {
            interactionService.likeArticle(username, articleId);
        }
        
        long newLikeCount = interactionService.getLikeCount(articleId);
        Map<String, Object> response = new HashMap<>();
        response.put("likeCount", newLikeCount);
        response.put("liked", !hasLiked);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/comments")
    public ResponseEntity<?> addComment(@PathVariable Long articleId,
                                      @RequestBody Map<String, String> payload,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Please log in to comment.");
        }
        String content = payload.get("content");
        Long parentId = payload.containsKey("parentId") ? Long.valueOf(payload.get("parentId")) : null;
        
        ArticleComment comment = interactionService.addComment(userDetails.getUsername(), articleId, content, parentId);
        return ResponseEntity.ok(CommentDto.fromEntity(comment));
    }

    @GetMapping("/comments")
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable Long articleId) {
        return ResponseEntity.ok(interactionService.getComments(articleId));
    }
    
    @GetMapping("/status")
    public ResponseEntity<?> getArticleStatus(@PathVariable Long articleId, @AuthenticationPrincipal UserDetails userDetails) {
        long likeCount = interactionService.getLikeCount(articleId);
        boolean liked = false;
        if (userDetails != null) {
            liked = interactionService.hasUserLiked(userDetails.getUsername(), articleId);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("likeCount", likeCount);
        response.put("liked", liked);
        return ResponseEntity.ok(response);
    }
} 