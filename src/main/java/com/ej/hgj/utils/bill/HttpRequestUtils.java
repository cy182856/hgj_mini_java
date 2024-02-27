package com.ej.hgj.utils.bill;

import com.ej.hgj.utils.SyPostClient;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;

public class HttpRequestUtils {
	static Logger logger = LoggerFactory.getLogger(SyPostClient.class);

	public static String post(String url, String params, String authorization){
		// 获得Http客户端(可以理解为:你得先有一个浏览器;注意:实际上HttpClient与浏览器是不一样的)
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		// 创建Post请求
		HttpPost httpPost = new HttpPost(url);
		CloseableHttpResponse response = null;
		try {
			StringEntity entity = new StringEntity(params,"utf-8");
			entity.setContentType("application/json");
			httpPost.setEntity(entity);
			httpPost.setHeader("Authorization", authorization);
			httpPost.setHeader("Accept", "*/*");
			response = httpClient.execute(httpPost);
			HttpEntity responseEntity = response.getEntity();
			logger.info("响应状态为:" + response.getStatusLine());
			if (responseEntity != null) {
				return EntityUtils.toString(responseEntity);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				// 释放资源
				if (httpClient != null) {
					httpClient.close();
				}
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	public static String get(String url, String authorization) throws URISyntaxException {
		// 获得Http客户端(可以理解为:你得先有一个浏览器;注意:实际上HttpClient与浏览器是不一样的)
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		//请求URL
		URIBuilder uriBuilder = new URIBuilder(url);
		// 创建get请求
		HttpGet httpGet = new HttpGet(uriBuilder.build());
		CloseableHttpResponse response = null;
		try {
			httpGet.setHeader("Authorization", authorization);
			httpGet.setHeader("Accept", "*/*");
			response = httpClient.execute(httpGet);
			HttpEntity responseEntity = response.getEntity();
			logger.info("响应状态为:" + response.getStatusLine());
			if (responseEntity != null) {
				return EntityUtils.toString(responseEntity);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				// 释放资源
				if (httpClient != null) {
					httpClient.close();
				}
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}

}
