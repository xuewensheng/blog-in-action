package com.spring.boot.blog.initializrstart.service;

import com.spring.boot.blog.initializrstart.model.Vote;

public interface VoteService {

    /**
     * 根据id获取 Vote
     * @param id
     * @return
     */
    Vote getVoteById(Long id);
    /**
     * 删除Vote
     * @param id
     * @return
     */
    void removeVote(Long id);
}
