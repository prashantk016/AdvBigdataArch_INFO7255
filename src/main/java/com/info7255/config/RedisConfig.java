package com.info7255.config;

import javax.servlet.Filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import com.info7255.beans.ElasticSearchConnect;
import com.info7255.beans.JedisBean;
import com.info7255.beans.MyJsonValidator;

@Configuration
public class RedisConfig {

	@Bean("validator")
	public MyJsonValidator myJsonValidator() {
		return new MyJsonValidator(jedisBean()) ;
	}
	
	@Bean("jedisBean")
	public JedisBean jedisBean() {
		return new JedisBean() ;
	}
	
	@Bean("elasticSearchConnect")
	public ElasticSearchConnect elasticSearchConnect() {
		return new ElasticSearchConnect() ;
	}
	
	/*
	 * @Bean public FilterRegistrationBean<ShallowEtagHeaderFilter> filterReg() {
	 * final FilterRegistrationBean<ShallowEtagHeaderFilter> reg = new
	 * FilterRegistrationBean<>(); reg.setFilter((ShallowEtagHeaderFilter)
	 * etagFilter()); reg.addUrlPatterns("/plan/*"); reg.setName("etagFilter");
	 * reg.setOrder(1); System.out.println("inside filter registration bean");
	 * return reg; }
	 * 
	 * @Bean(name="etagFilter") public Filter etagFilter() {
	 * System.out.println("inside e tag filter"); return new
	 * ShallowEtagHeaderFilter(); }
	 */
	
}
