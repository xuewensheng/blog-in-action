package com.spring.boot.blog.initializrstart.service;

import com.spring.boot.blog.initializrstart.model.Authority;

/**
 * Authority 服务接口.
 *
 * @since 1.0.0 2017年3月18日
 * @author <a href="https://waylau.com">Way Lau</a>
 */
public interface AuthorityService {

    /**
     * 根据id获取 Authority
     * @param
     * @return
     */
    Authority getAuthorityById(Long id);
}
