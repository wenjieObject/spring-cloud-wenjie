package com.tensquare.notice.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.tensquare.notice.pojo.Notice;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeDao extends BaseMapper<Notice> {
}
