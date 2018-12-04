package com.qi.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.qi.entity.User;
import com.qi.feign.UserFeignClient;
import org.apache.commons.httpclient.util.ExceptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Controller("/springCloud-Sentinel")
public class Controller1 {

	@Autowired
	private RestTemplate restTemplate;

	private UserFeignClient userFeignClient;

	@SentinelResource(fallback = "fallBackMethod", value = "test", blockHandler = "exceptionHandler", blockHandlerClass = {ExceptionUtil.class})
	@GetMapping("/test/{id}")
	@ResponseBody
	public String test(@PathVariable String id){
		throw new RuntimeException("error");
	}

	public String fallBackMethod(){
		return "there is an error when you requested";
	}
	@SentinelResource(fallback = "fallBackMethod")
	@GetMapping("/test2/{id}")
	@ResponseBody
	public User getResource(@PathVariable Long id){
		return userFeignClient.findById(id);
//		return restTemplate.getForObject("http://localhost:8080/"+id, User.class).getName();
	}

	// Block 异常处理函数，参数最后多一个 BlockException，其余与原函数一致.
	public String exceptionHandler(Long id, BlockException e){
		e.printStackTrace();
		return "this user is not exist";
	}

	@GetMapping("/index")
	public String index(){
		return "/static/index";
	}
}
