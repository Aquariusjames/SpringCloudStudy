package com.qi;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

public class PreRequestLogZuulFilter extends ZuulFilter {
	private Logger logger = LoggerFactory.getLogger(PreRequestLogZuulFilter.class);
	@Override
	public String filterType() {
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
	public Object run() throws ZuulException {
		RequestContext requestContext = RequestContext.getCurrentContext();
		HttpServletRequest request = requestContext.getRequest();
		logger.info(String.format("send s% request to s%",request.getMethod(),request.getRequestURL().toString()));
		return null;
	}
}
