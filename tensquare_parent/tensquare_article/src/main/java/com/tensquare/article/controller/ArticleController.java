package com.tensquare.article.controller;

import com.baomidou.mybatisplus.plugins.Page;
import com.tensquare.article.pojo.Article;
import com.tensquare.article.service.ArticleService;
import com.tensquare.entity.PageResult;
import com.tensquare.entity.Result;
import com.tensquare.entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/article")
@CrossOrigin
public class ArticleController {

    @Autowired
    private ArticleService articleService;


    @DeleteMapping("/{id}")
    public Result delete(@PathVariable String id) {
        articleService.delete(id);
        return new Result(true, StatusCode.OK, "删除成功");
    }

    @PostMapping
    public Result add(@RequestBody Article article) {
        articleService.add(article);
        return new Result(true, StatusCode.OK, "添加成功");
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable String id, @RequestBody Article article) {
        article.setId(id);
        articleService.update(article);
        return new Result(true, StatusCode.OK, "修改成功");
    }


    @GetMapping
    public Result findAll() {
        List list = articleService.findAll();
        return new Result(true, StatusCode.OK, "查询成功", list);
    }

    @GetMapping("/{id}")
    public Result findById(@PathVariable String id) {
        Article Article = articleService.findById(id);
        return new Result(true, StatusCode.OK, "查询成功", Article);
    }

    @RequestMapping(value="/search/{page}/{size}", method = RequestMethod.POST)
    public Result search(@RequestBody Map map, @PathVariable int page, @PathVariable int size) {
        Page page1 = articleService.search(map, page, size);
        return new Result(true, StatusCode.OK, "查询成功", new PageResult(page1.getTotal(), page1.getRecords()));
    }


    /*
    * 测试使用
    * */
    @RequestMapping(value="/exception", method = RequestMethod.GET)
    public Result exception() throws Exception {
        throw new Exception("测试统一异常处理");
    }

    /**
     * 订阅或者取消订阅文章作者
     *
     * @return
     */
    @RequestMapping(value = "/subscribe", method = RequestMethod.POST)
    private Result subscribe(@RequestBody Map map) {
        //根据文章id，订阅文章作者，返回订阅状态，true表示订阅成功，false表示取消订阅成功
        Boolean flag = articleService.subscribeMq(map.get("userId").toString(), map.get("articleId").toString());
        if (flag) {
            return new Result(true, StatusCode.OK, "订阅成功");
        } else {
            return new Result(true, StatusCode.OK, "订阅取消");
        }
    }

    //文章点赞
    @RequestMapping(value = "thumbup/{articleId}", method = RequestMethod.PUT)
    public Result thumbup(@PathVariable String articleId) {

        String thumbup = articleService.thumbup(articleId);
        if("1".equals(thumbup)){
            return new Result(true, StatusCode.OK, "点赞成功");
        }else{
            return new Result(false, StatusCode.REPERROR, "不能重复点赞");
        }
    }

}
