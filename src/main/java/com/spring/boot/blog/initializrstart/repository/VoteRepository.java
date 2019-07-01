package com.spring.boot.blog.initializrstart.repository;

import com.spring.boot.blog.initializrstart.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository  extends JpaRepository<Vote,Long> {
}
