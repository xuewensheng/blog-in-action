package com.spring.boot.blog.initializrstart.service;

import com.spring.boot.blog.initializrstart.model.Comment;

public interface CommentService {

    /**
     * 根据id获取 Comment
     * @param id
     * @return
     */
    Comment getCommentById(Long id);
    /**
     * 删除评论
     * @param id
     * @return
     */
    void removeComment(Long id);
}
