package com.ebdesk.report.pdf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import springfox.documentation.builders.PathSelectors;
import com.google.common.base.Predicates;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
	@Bean
	public Docket cyberPatrolApi() {
		return new Docket(DocumentationType.SWAGGER_2).apiInfo(getApiInfo()).select()
				.paths(Predicates.not(PathSelectors.regex("/error"))).build();
	}

	@SuppressWarnings("deprecation")
	private ApiInfo getApiInfo() {
		// We do not use all info
		ApiInfo info = new ApiInfo("IMA Report news pdf", "REST Reporting ima news pdf", "1.0", "", "", "", "");
		return info;
	}

}