package com.spring.boot.blog.initializrstart.service;

import com.spring.boot.blog.initializrstart.model.Vote;
import com.spring.boot.blog.initializrstart.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class VoteServiceImpl implements  VoteService{

    @Autowired
    private VoteRepository voteRepository;



    @Override
    @Transactional
    public void removeVote(Long id) {
        voteRepository.deleteById(id);
    }

    @Override
    public Vote getVoteById(Long id) {
        return voteRepository.getOne(id);
    }
}
