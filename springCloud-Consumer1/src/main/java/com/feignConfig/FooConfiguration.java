package com.feignConfig;

import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 一些接口需要进行基于http basic的认证后才能调用，配置类可以这样写
 */
@Configuration
public class FooConfiguration {
	@Bean
	public BasicAuthRequestInterceptor basicAuthRequestInterceptor(){
		return new BasicAuthRequestInterceptor("user", "password");
	}
}
