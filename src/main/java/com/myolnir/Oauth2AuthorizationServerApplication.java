package com.myolnir;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
//@EnableOAuth2Sso
public class Oauth2AuthorizationServerApplication extends SpringBootServletInitializer  {

	public static void main(String[] args) {
		SpringApplication.run(com.myolnir.Oauth2AuthorizationServerApplication.class, args);
	}

	/**
	 * An opinionated WebApplicationInitializer to run a SpringApplication from a traditional WAR deployment.
	 * Binds Servlet, Filter and ServletContextInitializer beans from the application context to the servlet container.
	 *
	 * @link http://docs.spring.io/spring-boot/docs/current/api/index.html?org/springframework/boot/context/web/SpringBootServletInitializer.html
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(com.myolnir.Oauth2AuthorizationServerApplication.class);
	}
}
