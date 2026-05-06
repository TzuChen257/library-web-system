package com.library.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer{
	
	@Autowired
	private AuthInterceptor authInterceptor;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")//套用範圍，api以下所有網址
				.allowedOrigins("http://localhost:5500")//允許範圍，跨網域可進入
				.allowedMethods("GET","POST","PUT","DELETE","PATCH","OPTIONS")
				.allowedHeaders("*")//包含Content-Type or Authorization皆可
				.allowCredentials(true);//允許傳送憑證
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(authInterceptor)
				.addPathPatterns("/api/**")
				.excludePathPatterns("/api/auth/login","/api/auth/register");//以下不用進interceptor抓token
	}
	
}