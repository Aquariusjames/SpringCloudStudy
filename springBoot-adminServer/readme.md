Spring Boot Admin 2.1.0 全攻略
Spring Boot Admin简介
Spring Boot Admin是一个开源社区项目，用于管理和监控SpringBoot应用程序。 应用程序作为Spring Boot Admin Client向为Spring Boot Admin Server注册（通过HTTP）或使用SpringCloud注册中心（例如Eureka，Consul）发现。 UI是的AngularJs应用程序，展示Spring Boot Admin Client的Actuator端点上的一些监控。常见的功能或者监控如下：

显示健康状况
显示详细信息，例如
JVM和内存指标
micrometer.io指标
数据源指标
缓存指标
显示构建信息编号
关注并下载日志文件
查看jvm系统和环境属性
查看Spring Boot配置属性
支持Spring Cloud的postable / env-和/ refresh-endpoint
轻松的日志级管理
与JMX-beans交互
查看线程转储
查看http跟踪
查看auditevents
查看http-endpoints
查看计划任务
查看和删除活动会话（使用spring-session）
查看Flyway / Liquibase数据库迁移
下载heapdump
状态变更通知（通过电子邮件，Slack，Hipchat，......）
状态更改的事件日志（非持久性）
快速开始
创建Spring Boot Admin Server
本文的所有工程的Spring Boot版本为2.1.0 、Spring Cloud版本为Finchley.SR2。案例采用Maven多module形式，父pom文件引入以下的依赖（完整的依赖见源码）：


    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.0.RELEASE</version>
        <relativePath/>
    </parent>
    
    
     <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>


    <spring-cloud.version>Finchley.SR2</spring-cloud.version>


在工程admin-server引入admin-server的起来依赖和web的起步依赖，代码如下：

<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-starter-server</artifactId>
    <version>2.1.0</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
然后在工程的启动类AdminServerApplication加上@EnableAdminServer注解，开启AdminServer的功能，代码如下：


@SpringBootApplication
@EnableAdminServer
public class AdminServerApplication {

    public static void main(String[] args) {
        SpringApplication.run( AdminServerApplication.class, args );
    }

}

在工程的配置文件application.yml中配置程序名和程序的端口，代码如下：

spring:
  application:
    name: admin-server
server:
  port: 8769
这样Admin Server就创建好了。

创建Spring Boot Admin Client
在admin-client工程的pom文件引入admin-client的起步依赖和web的起步依赖，代码如下：


        <dependency>
            <groupId>de.codecentric</groupId>
            <artifactId>spring-boot-admin-starter-client</artifactId>
            <version>2.1.0</version>
        </dependency>
       
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>


在工程的配置文件application.yml中配置应用名和端口信息，以及向admin-server注册的地址为http://localhost:8769，最后暴露自己的actuator的所有端口信息，具体配置如下：

spring:
  application:
    name: admin-client
  boot:
    admin:
      client:
        url: http://localhost:8769
server:
  port: 8768

management:
  endpoints:
    web:
      exposure:
        include: '*'
  endpoint:
    health:
      show-details: ALWAYS

在工程的启动文件如下：


@SpringBootApplication
public class AdminClientApplication {

    public static void main(String[] args) {
        SpringApplication.run( AdminClientApplication.class, args );
    }
一次启动两个工程，在浏览器上输入localhost:8769 

点击wallboard，可以查看admin-client具体的信息，比如内存状态信息：

Spring boot Admin结合SC注册中心使用
同上一个案例一样，本案例也是使用的是Spring Boot版本为2.1.0 、Spring Cloud版本为Finchley.SR2。案例采用Maven多module形式，父pom文件引入以下的依赖（完整的依赖见源码），此处省略。
搭建注册中心
注册中心使用Eureka、使用Consul也是可以的，在eureka-server工程中的pom文件中引入：
 <dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>

复制代码配置eureka-server的端口信息，以及defaultZone和防止自注册。最后系统暴露eureka-server的actuator的所有端口。
spring:
  application:
    name: eureka-server
server:
  port: 8761
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
    register-with-eureka: false
    fetch-registry: false
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: ALWAYS
复制代码在工程的启动文件EurekaServerApplication加上@EnableEurekaServer注解开启Eureka Server.

@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run( EurekaServerApplication.class, args );
    }
}

复制代码eureka-server搭建完毕。
搭建admin-server
在admin-server工程的pom文件引入admin-server的起步依赖、web的起步依赖、eureka-client的起步依赖，如下：
<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-starter-server</artifactId>
    <version>2.1.0</version>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
        
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
复制代码然后配置admin-server，应用名、端口信息。并向注册中心注册，注册地址为http://localhost:8761，最后将actuator的所有端口暴露出来，配置如下：

spring:
  application:
    name: admin-server
server:
  port: 8769
eureka:
  client:
    registryFetchIntervalSeconds: 5
    service-url:
      defaultZone: ${EUREKA_SERVICE_URL:http://localhost:8761}/eureka/
  instance:
    leaseRenewalIntervalInSeconds: 10
    health-check-url-path: /actuator/health

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: ALWAYS
复制代码在工程的启动类AdminServerApplication加上@EnableAdminServer注解，开启admin server的功能，加上@EnableDiscoveryClient注解开启eurke client的功能。

@SpringBootApplication
@EnableAdminServer
@EnableDiscoveryClient
public class AdminServerApplication {

    public static void main(String[] args) {
        SpringApplication.run( AdminServerApplication.class, args );
    }

}

复制代码搭建admin-client
在admin-client的pom文件引入以下的依赖，由于2.1.0采用webflux，引入webflux的起步依赖，引入eureka-client的起步依赖，并引用actuator的起步依赖如下：

 <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
复制代码在工程的配置文件配置应用名、端口、向注册中心注册的地址，以及暴露actuator的所有端口。

spring:
  application:
    name: admin-client
eureka:
  instance:
    leaseRenewalIntervalInSeconds: 10
    health-check-url-path: /actuator/health

  client:
    registryFetchIntervalSeconds: 5
    service-url:
      defaultZone: ${EUREKA_SERVICE_URL:http://localhost:8761}/eureka/
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: ALWAYS
server:
  port: 8762

复制代码在启动类加上@EnableDiscoveryClie注解，开启DiscoveryClient的功能。
@SpringBootApplication
@EnableDiscoveryClient
public class AdminClientApplication {

    public static void main(String[] args) {
        SpringApplication.run( AdminClientApplication.class, args );
    }
}
复制代码一次启动三个工程，在浏览器上访问localhost:8769，浏览器会显示和上一小节一样的界面

集成spring security
在2.1.0版本中去掉了hystrix dashboard，登录界面默认集成到了spring security模块，只要加上spring security就集成了登录模块。
只需要改变下admin-server工程，需要在admin-server工程的pom文件引入以下的依赖：
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
复制代码在admin-server工的配置文件application.yml中配置spring security的用户名和密码，这时需要在服务注册时带上metadata-map的信息，如下：
spring:
  security:
    user:
      name: "admin"
      password: "admin"
      
eureka:
  instance:
    metadata-map:
      user.name: ${spring.security.user.name}
      user.password: ${spring.security.user.password}

复制代码写一个配置类SecuritySecureConfig继承WebSecurityConfigurerAdapter，配置如下：

@Configuration
public class SecuritySecureConfig extends WebSecurityConfigurerAdapter {

    private final String adminContextPath;

    public SecuritySecureConfig(AdminServerProperties adminServerProperties) {
        this.adminContextPath = adminServerProperties.getContextPath();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter( "redirectTo" );

        http.authorizeRequests()
                .antMatchers( adminContextPath + "/assets/**" ).permitAll()
                .antMatchers( adminContextPath + "/login" ).permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin().loginPage( adminContextPath + "/login" ).successHandler( successHandler ).and()
                .logout().logoutUrl( adminContextPath + "/logout" ).and()
                .httpBasic().and()
                .csrf().disable();
        // @formatter:on
    }
}

复制代码重启启动工程，在浏览器上访问：http://localhost:8769/，会被重定向到登录界面，登录的用户名和密码为配置文件中配置的，分别为admin和admin，界面显示如下：

集成邮箱报警功能
在spring boot admin中，也可以集成邮箱报警功能，比如服务不健康了、下线了，都可以给指定邮箱发送邮件。集成非常简单，只需要改造下admin-server即可：
在admin-server工程Pom文件，加上mail的起步依赖，代码如下：
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

复制代码在配置文件application.yml文件中，需要配置邮件相关的配置，如下：
spring.mail.host: smtp.163.com
spring.mail.username: miles02
spring.mail.password:
spring.boot.admin.notify.mail.to: 124746406@qq.com

复制代码做完以上配置后，当我们已注册的客户端的状态从 UP 变为 OFFLINE 或其他状态，服务端就会自动将电子邮件发送到上面配置的地址。
源码下载
快速开始： github.com/forezp/Spri…
和spring cloud结合：github.com/forezp/Spri…
参考资料
codecentric.github.io/spring-boot…
github.com/codecentric…










