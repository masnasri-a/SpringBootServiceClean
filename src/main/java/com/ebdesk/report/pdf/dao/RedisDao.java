package com.ebdesk.report.pdf.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ebdesk.report.pdf.config.RedisConfig;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;

import redis.clients.jedis.Jedis;

@Repository
public class RedisDao {

	@Autowired
	private RedisConfig redisConfig;

	public Map<String, String> keywordRedis(String criteria)
			throws JsonParseException, JsonMappingException, IOException {
		@SuppressWarnings("resource")
		Jedis jedis = new Jedis(redisConfig.getHost());
		jedis.select(redisConfig.getDb());

		String id = jedis.get(criteria).toString();
		JsonNode node = new ObjectMapper().readValue(id, JsonNode.class);
		Map<String, String> query = new HashMap<String, String>();

		if (node.get("c_query_mode").asText().equals("boolean")) {
			if (node.has("c_query_bool_statement")) {
				query.put("query_statement", node.get("c_query_bool_statement").asText());
			} else {
				query.put("query_statement", "");
			}

			query.put("criteria_name", node.get("criteria_name").asText());
			query.put("query_string", node.get("c_query_bool_news").asText());
			query.put("query_mode", "bool");

			List<String> myList = new ArrayList<String>(Arrays.asList(node.get("c_media_tag").asText().split(";")));
			List<String> tags_list = new ArrayList<String>();
			for (String criterias : myList) {
				tags_list.add(new ObjectMapper().writeValueAsString(criterias));
			}

			String media_tag = Joiner.on(",").join(tags_list);
			query.put("tags", media_tag);

		} else {
			query.put("criteria_name", node.get("criteria_name").asText());
			query.put("query_string", node.get("c_query_string").asText().replace("\\", ""));
			query.put("query_mode", "string");

			List<String> myList = new ArrayList<String>(Arrays.asList(node.get("c_media_tag").asText().split(";")));
			List<String> tags_list = new ArrayList<String>();
			for (String criterias : myList) {
				tags_list.add(new ObjectMapper().writeValueAsString(criterias));
			}

			String media_tag = Joiner.on(",").join(tags_list);
			query.put("tags", media_tag);
		}

		return query;
	}

	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
		System.out.println(new RedisDao().keywordRedis("546"));
	}

}
