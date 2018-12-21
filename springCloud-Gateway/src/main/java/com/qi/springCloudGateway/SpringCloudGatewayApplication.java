package com.qi.springCloudGateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringCloudGatewayApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpringCloudGatewayApplication.class,args);
	}
	@Bean
	public RouteLocator customerRouteLocator(RouteLocatorBuilder builder){
		return builder.routes()
				.route("path_route", r -> r.path("about").uri("http://localhost:8081"))
				.build();
	}
	/**
	 * 上面配置了一个 id 为 path_route 的路由，当访问地址http://localhost:8080/about时会自动转发到地址：
	 * http://localhost:8081/about和配置文件配置的转发效果一样，只是这里转发的是以项目地址/about格式的请求地址。
	 */
}
