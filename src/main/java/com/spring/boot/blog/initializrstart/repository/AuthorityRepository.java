package com.spring.boot.blog.initializrstart.repository;

import com.spring.boot.blog.initializrstart.model.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {

}
