package com.qi;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class PreRequestLogZuulFilter extends ZuulFilter {
	private Logger logger = LoggerFactory.getLogger(PreRequestLogZuulFilter.class);
	@Override
	public String filterType() {
		/**
		 * pre:  可以在请求被路由之前调用。
		 * pre过滤器
		 * • ServletDetectionFilter: 它的 执行顺序为-3' 是最先被执行的过滤器。 该过
		 * 滤器总是会被执行， 主要用来检测当前请求是通过Spring的DispatcherServlet
		 * 处理运行的，还是通过Zuu1Servle七来处理运行的。它的检测结果会以布尔类型保
		 * 存在当前请求上下文的isDispatcherServletRequest参数 中，这样在后续的过
		 * 滤器中， 我们就可以通过RequestUtils.isDispatcherServletRequest()
		 * 和RequestUtils.is ZuulServletRequest()方法来判断请求处理的源头，以
		 * 实现后续不同的处理机制。 一般情况下， 发送到API网关的外部请求都会被Spring
		 * 的 DispatcherServlet 处理， 除了通过/zuul尸路径访问的请求会绕过
		 * DispatcherServlet, 被Zuu1Servlet处理， 主要用来应对处理大文件上传的
		 * 情 况。 另外， 对千 Zuu1Servlet 的访问路径 /zuul /*, 我 们可 以 通 过
		 * zuul.servletPath参数来进行修改。
		 * routing: 在路由请求时被调用。
		 * post: 在 routing 和 error 过滤器之后被调用。
		 * post过滤器
		 * • SendErrorFilter: 它的执行顺序为 o, 是post阶段第 一 个执行的过滤器。 该
		 * 过滤器仅在请求上下文中包含 error.status_code参数（由之前执行的过滤器
		 * 设置的错误编码）并且还没有被该过滤器处理过的时候执行。 而该过滤器的具体逻
		 * 辑就是利用请求上下文中的错误信息来组成 一 个forward到API网关/error错误
		 * 端点的请求来产生错误响应。
		 * error:  处理请求时发生错误时被调用。
		 */
		return "pre"; //过滤类型有 pre，route，post，error等几种类型
	}

	/**
	 * 返回一个int值来指定过滤器的执行顺序，不同的过滤器允许返回相同的数字
	 * @return
	 */
	@Override
	public int filterOrder() {
		return 1;
	}

	/**
	 * 返回一个boolean值判断该过滤器是否要执行true表示执行
	 * @return
	 */
	@Override
	public boolean shouldFilter() {
		return true;
	}

	/**
	 * 具体执行逻辑
	 * @return
	 * @throws ZuulException
	 */
	@Override
	public Object run(){
		RequestContext requestContext = RequestContext.getCurrentContext();

		HttpServletRequest request = null;
		try {
			request = requestContext.getRequest();
			logger.info(String.format("send s% request to s%",request.getMethod(),request.getRequestURL().toString()));
		} catch (Exception e) {
			requestContext.set("error.status_code", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			requestContext.set("error.exception", e);
		}
		return null;
	}
}
