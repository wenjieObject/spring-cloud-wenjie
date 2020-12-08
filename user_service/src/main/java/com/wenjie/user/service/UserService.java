package com.wenjie.user.service;

import com.wenjie.user.mapper.UserMapper;
import com.wenjie.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 根据主键查询用户
     * @param id 用户id
     * @return 用户
     */
    public User queryById(Long id){

        //sleep 2秒钟 是否重试



        return userMapper.selectByPrimaryKey(id);
    }
}
