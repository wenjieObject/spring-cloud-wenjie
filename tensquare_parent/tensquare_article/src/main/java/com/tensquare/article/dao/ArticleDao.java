package com.tensquare.article.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.tensquare.article.pojo.Article;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleDao extends BaseMapper<Article> {

}
