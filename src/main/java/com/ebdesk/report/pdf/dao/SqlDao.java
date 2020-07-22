package com.ebdesk.report.pdf.dao;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SqlDao {

	@Autowired
	JdbcTemplate jdbc;

	public String pathLogoWorkspace(String w_id) {
		String logo = "ebdesk.png";
		String query = "SELECT w_app_logo as logo FROM `workspace` WHERE w_id ='" + w_id + "'";
		for (Map<String, Object> data : jdbc.queryForList(query)) {
			if (!data.get("logo").toString().equals("")) {
				logo = data.get("logo").toString();
			}
		}
		return logo;
	}

}
