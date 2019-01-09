package com.qi;

import com.netflix.zuul.FilterProcessor;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

/**
 * 判断pre,route,post哪个过滤器抛出的异常
 * 直接扩展processZuulFilter(ZuulFilter filter),
 * 当过滤器执行抛出异常的时候， 我们捕获它， 并向请求上下中记录 一 些信息 并在启动主类中添加使其生效 FilterProcessor.setProcessor(new DidiFilterProcessor());
 */
public class DidiFilterProcessor extends FilterProcessor {
	@Override
	public Object processZuulFilter(ZuulFilter filter) throws ZuulException {
		try {
			return super.processZuulFilter(filter);
		} catch (ZuulException e) {
			RequestContext requestContext = RequestContext.getCurrentContext();
			requestContext.set("failed.filter", filter);//在上下文中添加过滤器的信息
			throw e;
		}
	}
}
