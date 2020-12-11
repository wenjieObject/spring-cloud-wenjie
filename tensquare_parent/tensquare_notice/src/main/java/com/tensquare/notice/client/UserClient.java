package com.tensquare.notice.client;

import com.tensquare.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(value="tensquare-user")
public interface UserClient {
    /**
     *  根据ID查询用户
     * @param id
     * @return
     */
    @RequestMapping(value="/user/{id}",method = RequestMethod.GET)
    public Result findById(@PathVariable("id") String id);
}
