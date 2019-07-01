package com.spring.boot.blog.initializrstart.service;

import com.spring.boot.blog.initializrstart.model.Authority;
import com.spring.boot.blog.initializrstart.repository.AuthorityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthorityServiceImpl implements AuthorityService {

    @Autowired
    private AuthorityRepository authorityRepository;

    @Override
    public Authority getAuthorityById(Long id) {
        return authorityRepository.getOne(id);
    }

}
