package com.ebdesk.report.pdf.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("config")

public class ExternalConfig {
	private String path_save;
	private String path_template;
	private String path_chart_image;
	private String path_workspace_logo;
	private String news_logo;
	private String printed_image;
	private String api_data;

	public String getPath_save() {
		return path_save;
	}

	public void setPath_save(String path_save) {
		this.path_save = path_save;
	}

	public String getPath_template() {
		return path_template;
	}

	public void setPath_template(String path_template) {
		this.path_template = path_template;
	}

	public String getPath_chart_image() {
		return path_chart_image;
	}

	public void setPath_chart_image(String path_chart_image) {
		this.path_chart_image = path_chart_image;
	}

	public String getPath_workspace_logo() {
		return path_workspace_logo;
	}

	public void setPath_workspace_logo(String path_workspace_logo) {
		this.path_workspace_logo = path_workspace_logo;
	}

	public String getNews_logo() {
		return news_logo;
	}

	public void setNews_logo(String news_logo) {
		this.news_logo = news_logo;
	}

	public String getPrinted_image() {
		return printed_image;
	}

	public void setPrinted_image(String printed_image) {
		this.printed_image = printed_image;
	}

	public String getApi_data() {
		return api_data;
	}

	public void setApi_data(String api_data) {
		this.api_data = api_data;
	}

}
