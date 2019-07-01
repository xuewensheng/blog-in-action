package com.spring.boot.blog.initializrstart.service;

import com.spring.boot.blog.initializrstart.model.Comment;
import com.spring.boot.blog.initializrstart.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;


    @Override
    @Transactional(rollbackFor =  Exception.class)
    public void removeComment(Long id) {
        commentRepository.deleteById(id);
    }
    @Override
    public Comment getCommentById(Long id) {
        return commentRepository.getOne(id);
    }

}
