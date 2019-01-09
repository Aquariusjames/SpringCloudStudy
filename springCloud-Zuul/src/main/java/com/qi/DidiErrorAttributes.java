package com.qi;


import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

/**
 * 自定义异常信息
 * 在实现了对自定义过滤器中的异常处理之后， 实际应用到业务系统中时， 往往默认的
 * 错误信息并不符合系统设计的响应格式， 那么我们就需要对返回的异常信息进行定制。 对
 * 于如何定制这个错误信息有很多种方法可以 实现。最直接的是，可以编写 一 个自定义的 post
 * 过滤器来组织错误结果，该方法实现起来简单粗暴，完全可以 参考 SendErrorFilter 的
 * 实现，然后直接组织请求响应结果而不是 forward 到 /error 端点， 只是使用该方法时需要
 * 注意： 为了替代 SendErrorFilter, 还需要禁用 SendErrorFilter 过滤器， 禁用的
 * 配置方法在后文中会详细介绍。
 * 那么 如果不采用重写过滤器的方式，依然想要使用 SendErrorFilter 来处理异常返
 * 回的话，我们要如何定制返回的响应结果呢？这个时候，我们的关注点就不能放在 Zuul 的
 * 过滤器上了， 因为错误信息的生成实际上并不是由 Spring Cloud Zuul 完成的。 我们在介绍
 * SendErrorFilter 的时候提到过， 它会根据请求上下中保存的错误信息来组织
 * 一 个
 * forward 到 /error 端点的请求来获取错误响应， 所以我们的扩展目标转移到了对 /error
 * 端点的实现。
 * /error 端 点 的 实现来源于 Spring Boot 的 org.springframework.boot.
 * autoconfigure.web.BasicErrorController, 它的具体定义如下：
 * @RequestMapping
 * @ResponseBody
 * public ResponseEn巨ty<Map<String, Object>> error(HttpServletRequest request) {
 * Map<String, Object> body = getErrorAttributes(request,
 * isincludeStackTrace(request, MediaType.ALL)) ;
 * HttpStatus status = getStatus(request);
 * return new ResponseEntity<Map<String, Object>>(body, status);
 * 从源码中可以看到，它的实现非常简单，通过调用 getErrorAttributes 方法来根据
 * 请求参数组织错误信息的返回结果，而这里的 getErrorAttributes 方法会将具体组织逻
 * 254
 * 第 7 章 aAPI 网关服务： Spring Cloud Zuul
 * 辑委托给 org.springframework.boot.autoconfigure.web.ErrorAttributes
 * 接口提供的 getErrorAttributes 来实现。在 Spring Boot 的自动化配置机制中，默认会
 * 采用 org.springframework.boot.autoconfigure.web.DefaultErrorAttributes
 * 作为该接口的实现。如下代码所示，在定义 Error 处理的自动化配置中，该接口的默认实现
 * 采用了 @ConditionalOnMissingBean 修饰， 说明 DefaultErrorAttributes 对象
 * 实例仅在没有 ErrorAttributes 接口的实例时才会被创建来使用，所以我们只需要自己
 * 编写 一 个自定义的 ErrorAttributes 接口实现类，并创建它的实例就能替代这个默认的
 * 实现，从而达到自定义错误信息的效果了。
 * @ConditionalOnClass({ Servlet.class, DispatcherServlet.class  })
 * @ConditionalOnWebApplication
 * @AutoConfigureBefore(WebMvcAutoConfiguration.class)
 * @Configuration
 * public class ErrorMvcAutoConfiguration {
 * @Au tow ired
 * private ServerProperties properties;
 * @Bean
 * @ConditionalOnMissingBean(value = ErrorAttributes.class, search =
 * SearchStrategy.CURRENT)
 * public DefaultErrorAttributes errorAttributes () {
 * return new DefaultErrorAttributes();
 * 举个简单的例子， 比如我们不希望将 exception 属性返回给客户端， 那么就可以编
 * 写
 * 一
 * 个自定 义 的 实 现 ， 它可以基于 DefaultErrorAttributes ,  然后重写
 * getErrorAttributes 方法，从原来的结果中将 exception 移除即可，具体实现如下：
 * public class DidiErrorAttributes extends DefaultErrorAttributes {
 * @Override
 * public Map<String, Object> getErrorAttributes (
 * RequestAttributes requestAttributes, boolean includeStackTrace
 * ) {
 * Map<String, Object> result = super.getErrorAttributes(requestAttributes,
 * includeStackTrace);
 * result.remove("exception");
 * return result;
 * 最后， 为了让自定义的错误信息生成逻辑生效， 需要在应用主类中加入如下代码， 为
 * 其创建实例来替代默认的实现：
 * @Bean
 * public DefaultErrorAttributes errorAttributes () {
 * return new DidiErrorAttributes ();
 */
public class DidiErrorAttributes extends DefaultErrorAttributes {

	@Override
	public Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {
		Map<String, Object> result = super.getErrorAttributes(webRequest, includeStackTrace);
		result.remove("exception"); //从原来的结果中将 exception 移除
		return result;
	}
}
