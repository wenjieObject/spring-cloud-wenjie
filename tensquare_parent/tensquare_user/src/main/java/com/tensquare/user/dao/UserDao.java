package com.tensquare.user.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.tensquare.user.pojo.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao extends BaseMapper<User> {
}
