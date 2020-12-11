package com.tensquare.article.service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.tensquare.article.client.NoticeClient;
import com.tensquare.article.dao.ArticleDao;
import com.tensquare.article.pojo.Article;
import com.tensquare.article.pojo.Notice;
import com.tensquare.entity.Result;
import com.tensquare.entity.StatusCode;
import com.tensquare.util.IdWorker;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ArticleService {

    @Autowired
    private ArticleDao articleDao;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private NoticeClient noticeClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    public List<Article> findAll() {
        return articleDao.selectList(null);
    }
    public Article findById(String id) {
        return articleDao.selectById(id);
    }

    public void add(Article article) {
        //使用分布式id生成器
        String id = idWorker.nextId() + "";
        article.setId(id);

        //初始化数据
        article.setVisits(0);   //浏览量
        article.setThumbup(0);  //点赞数
        article.setComment(0);  //评论数

        //新增
        articleDao.insert(article);

        //TODO 使用jwt获取当前用户的userid，也就是文章作者的id
        String authorId = "3";

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        //获取需要通知的读者
        String authorKey = "article_author_" + authorId;
        Set<String> set = redisTemplate.boundSetOps(authorKey).members();

        for (String uid : set) {
            //消息通知
            Notice notice = new Notice();
            notice.setReceiverId(uid);
            notice.setOperatorId(authorId);
            notice.setAction("publish");
            notice.setTargetType("article");
            notice.setTargetId(id);
            notice.setCreatetime(new Date());
            notice.setType("sys");
            notice.setState("0");

            Result add = noticeClient.add(notice);

            rabbitTemplate.convertAndSend("article_subscribe", authorId, id);

        }

    }

    public void update(Article article) {
        //根据id号更新
        //方法1
        articleDao.updateById(article);
        //方法2
        EntityWrapper wrapper = new EntityWrapper<Article>();
        wrapper.eq("id", article.getId());
        articleDao.update(article, wrapper);
    }

    public void delete(String id) {
        articleDao.deleteById(id);
    }

    public Page search(Map map, int page, int size) {
        EntityWrapper wrapper = new EntityWrapper<Article>();
        Set<String> fieldSet = map.keySet();
        for(String field : fieldSet) {
            //wrapper.eq(field, map.get(field));
            wrapper.eq(null != map.get(field), field, map.get(field));
        }
        Page page1 = new Page(page, size);
        List list = articleDao.selectPage(page1, wrapper);
        page1.setRecords(list);
        return page1;
    }

    public Boolean subscribeMq(String userId,String articleId){
        //根据文章id查询文章作者id
        String authorId = articleDao.selectById(articleId).getUserid();

        //创建Rabbit管理器
        RabbitAdmin rabbitAdmin = new RabbitAdmin(rabbitTemplate.getConnectionFactory());

        //声明exchange
        DirectExchange exchange = new DirectExchange("article_subscribe");
        rabbitAdmin.declareExchange(exchange);

        //创建queue
        Queue queue = new Queue("article_subscribe_" + userId, true);

        //声明exchange和queue的绑定关系，设置路由键为作者id
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(authorId);

        //存放用户订阅作者
        String userKey = "article_subscribe_" + userId;
        //存放作者的订阅者
        String authorKey = "article_author_" + authorId;

        //查询该用户是否已经订阅作者
        //redisTemplate.setKeySerializer(new StringRedisSerializer());
        //redisTemplate.setValueSerializer(new StringRedisSerializer());
        Boolean flag = redisTemplate.boundSetOps(userKey).isMember(authorId);

        if (flag) {
            //如果为flag为true，已经订阅,则取消订阅
            redisTemplate.boundSetOps(userKey).remove(authorId);
            redisTemplate.boundSetOps(authorKey).remove(userId);

            //删除绑定的队列
            rabbitAdmin.removeBinding(binding);
            return false;
        } else {
            // 如果为flag为false，没有订阅，则进行订阅
            redisTemplate.boundSetOps(userKey).add(authorId);
            redisTemplate.boundSetOps(authorKey).add(userId);

            //声明队列和绑定队列
            rabbitAdmin.declareQueue(queue);
            rabbitAdmin.declareBinding(binding);

            return true;
        }


    }

    public Boolean subscribe(String userId, String articleId) {
        //根据文章id查询文章作者id
        String authorId = articleDao.selectById(articleId).getUserid();

        String userKey = "article_subscribe_" + userId;
        String authorKey = "article_author_" + authorId;

        //查询该用户是否已经订阅作者
        //redisTemplate.setKeySerializer(new StringRedisSerializer());
        //redisTemplate.setValueSerializer(new StringRedisSerializer());
        Boolean flag = redisTemplate.boundSetOps(userKey).isMember(authorId);

        if (flag) {
            //如果为flag为true，已经订阅,则取消订阅
            redisTemplate.boundSetOps(userKey).remove(authorId);
            redisTemplate.boundSetOps(authorKey).remove(userId);
            return false;
        } else {
            // 如果为flag为false，没有订阅，则进行订阅
            redisTemplate.boundSetOps(userKey).add(authorId);
            redisTemplate.boundSetOps(authorKey).add(userId);
            return true;
        }
    }

    public String thumbup(String articleId) {

        //模拟用户id
        String userId = "4";
        String key = "thumbup_article_" + userId + "_" + articleId;

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        //查询用户点赞信息，根据用户id和文章id
        Object flag = redisTemplate.opsForValue().get(key);

        //判断查询到的结果是否为空
        if (flag == null) {
            Article article = articleDao.selectById(articleId);
            article.setThumbup(article.getThumbup() + 1);
            articleDao.updateById(article);

            //点赞成功，保存点赞信息
            redisTemplate.opsForValue().set(key, "1");

            return "1";
        }

        return "0";
    }

}
