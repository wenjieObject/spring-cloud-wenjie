package com.tensquare.article.client;

import com.tensquare.article.pojo.Notice;
import com.tensquare.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(value = "tensquare-notice")
public interface NoticeClient {

    /**
     * 添加消息
     * @param notice
     * @return
     */
    @RequestMapping(value = "notice",method = RequestMethod.POST)
    public Result add(@RequestBody Notice notice) ;
}
