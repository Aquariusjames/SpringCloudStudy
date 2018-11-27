package com.qi;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.springframework.cloud.netflix.zuul.filters.route.FallbackProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class UserFallbackProvider implements FallbackProvider {
	/**
	 * 为哪个微服务提供回退功能
	 * @return
	 */
	@Override
	public String getRoute() {
		return "springCloud-Provider";
	}

	@Override
	public ClientHttpResponse fallbackResponse(String route, Throwable cause) {
		return new ClientHttpResponse() {
			@Override
			public HttpStatus getStatusCode() throws IOException {
				return HttpStatus.OK; //请求成功，返回OK
			}

			@Override
			public int getRawStatusCode() throws IOException {
				return HttpStatus.OK.value();
			}

			/**
			 * 返回状态文本实际就是OK
			 * @return
			 * @throws IOException
			 */
			@Override
			public String getStatusText() throws IOException {
				return HttpStatus.OK.getReasonPhrase();
			}

			@Override
			public void close() {

			}

			@Override
			public InputStream getBody() throws IOException {
				JSONObject json = new JSONObject();
				json.put("state", "501");
				json.put("msg", "后台接口错误");
				return new ByteArrayInputStream(json.toString().getBytes());
			}

			@Override
			public HttpHeaders getHeaders() {
				HttpHeaders httpHeaders = new HttpHeaders();
				httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
				return httpHeaders;
			}
		};
	}
}
