package com.qi;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 处理post抛出的异常
 *  怎么判断引起异常的过滤器来自什么阶段呢? shouldFilter方法该如何实现，
 * 对于这个问题， 我们第 一 反应 会寄希望于请求上下文RequestContext 对象， 可是在查
 * 阅文档和源码后发现其中并没有存储异常来源的内容， 所以我们不得不扩展原来的过滤器
 * 处理逻辑。 当有异常抛出的时候， 记录下抛出异常的过滤器， 这样我们就可以在
 * ErrorExtFilter 过滤器的shouldFilter方法中获取并以此判断异常是否来自post
 * 阶段的过滤器了。
 * 为了扩展过滤器的处理逻辑， 为请求上下文增加 一 些自定义属性， 我们需要深入了解
 * Zuul过滤器的核心 处理器： com.netflix.zuul.FilterProcessor 。 该类中定义了下
 * 面列出的过滤器调用和处理相关的核心方法。
 * 252
 * • getinstance(): 该方法用来获取当前处理器的实例。
 * • setProcessor(FilterProcessor processor): 该方法用来设置处理器实
 * 例， 可以使用此方法来设置自定义的处理器。
 * • processZuulF辽ter(ZuulFilter filter): 该方法定义了用来执行 filter
 * 的具体逻辑， 包括对请求上下文的设置， 判断是否应 该 执行， 执行时
 * 一
 * 些异常的处理
 * 等。
 * • getFiltersByType(String filterType) :  该 方 法 用 来 根据 传 入的
 * FilterType 获取API网关中对应类型的过滤器， 并根据这些 过滤器的
 * filterOrder从小到大排序， 组织成 一 个列表返回。
 * • runFilters(StringsType) : 该方法会根据传入的 filterType 来调用
 * getFiltersByType(String filterType)获取排序后的过滤器列表， 然后轮
 * 询这些过滤器， 并调用 processZuulFilter(ZuulFilter filter)来依次执
 * 行它们。
 * • preRoute(): 调用runFilters("pre")来执行所有pre类型的过滤器。
 * • route() : 调用runFilters("route")来执行所有route类型的过滤器。
 * • postRoute(): 调用runFilters("post")来执行所有post类型的过滤器。
 * • error(): 调用runFilters("error")来执行所有error类型的过滤器。
*/
@Component
public class ErrorExtFilter extends ErrorFilter {

	@Override
	public String filterType () {
		return "error";
	}
	@Override
	public int filterOrder() {
		return 30;  //大于 ErrorF 过 ter 的值
	}
	@Override
	public boolean shouldFilter() {
		// TODO判断：仅处理来自post过滤器引起的异常
		RequestContext requestContext = RequestContext.getCurrentContext();
		ZuulFilter failedFilter = (ZuulFilter) requestContext.get("failed.filter");//获取扩展核心过滤器添加的信息判断是否是post过滤器抛出的异常
		if (Objects.nonNull(failedFilter) && failedFilter.filterType().equals("post")){
			return true;
		}
		return false;
	}
}
