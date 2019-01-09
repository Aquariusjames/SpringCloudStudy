package com.qi;

import com.netflix.zuul.FilterProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.discovery.PatternServiceRouteMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;


@EnableZuulProxy //启动Zuul网关 该代理使用Ribbon来定位注册在Eureka Server中的微服务；同时该代理还整合了Hystrix，从而实现了容错
@SpringBootApplication
public class ZuulApplication {
	public static void main(String[] args) {
		FilterProcessor.setProcessor(new DidiFilterProcessor());//添加扩展的核心处理器使其生效
		SpringApplication.run(ZuulApplication.class, args);
	}

	@Bean
	public PatternServiceRouteMapper patternServiceRouteMapper(){
		/**
		 * 调用构造函数PatternServiceRouteMapper(String servicePattern, String routePattern)
		 * servicePattern指定微服务的正则
		 * routePattern指定路由正则
		 * 通过这段代码即可实现将microservice-provider-user-v1这个微服务，映射到/v1/microservice-provider-user/**这个路径
		 */
		return new PatternServiceRouteMapper("(?<name>^.+)-(?<version)v.+$","${version}/${name}");
	}

	@Bean
	public PreRequestLogZuulFilter preRequestLogZuulFilter(){
		return new PreRequestLogZuulFilter();
	}

	@Bean
	public ErrorFilter errorFilter(){
		return new ErrorFilter();
	}

	@Bean
	public DefaultErrorAttributes errorAttributes () {
		return new DidiErrorAttributes();
	}
	@RefreshScope //注解来将Zuul的配置内容动态化
	@ConfigurationProperties("zull")
	public ZuulProperties zuulProperties(){
		return new ZuulProperties();
	}
}
