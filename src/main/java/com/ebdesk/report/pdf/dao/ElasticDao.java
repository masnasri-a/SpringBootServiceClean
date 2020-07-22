package com.ebdesk.report.pdf.dao;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

@Repository
public class ElasticDao {

	public static void main(String[] args) throws UnsupportedEncodingException {
		ElasticDao es = new ElasticDao();
		String start = "202001170000";
		String end = "202001172359";
		String keyword = "\"dana desa\"";
		String media_tags = "";
		String source = "";
		String elastic = "online";
		System.out.println(es.resume(start, end, keyword, source, media_tags, elastic));

	}

	public List<Object> resume(String start, String end, String keyword, String source, String media_tags,
			String elastic) {
		List<Object> datum = new ArrayList<Object>();
		try {

			Client client = Client.create();

			WebResource webResource = client.resource("http://192.168.170.87:9099/getResume?end=" + end + "&keyword="
					+ URLEncoder.encode(keyword, "UTF-8") + "&limit=10&media_tags=" + media_tags + "&source=" + source
					+ "&start=" + start + "&elastic=" + elastic);

			ClientResponse response = webResource.type("application/json").get(ClientResponse.class);

			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
			}

			String output = response.getEntity(String.class);
			JSONArray array = new JSONArray(output);
			for (int i = 0; i < array.length(); i++) {
				JSONObject data = array.getJSONObject(i);
				datum.add(data);
			}

		} catch (Exception e) {

			e.printStackTrace();

		}

		return datum;
	}

	public List<Object> dailyStatistic(String start, String end, String keyword, String interval, String source,
			String media_tags, String elastic) {
		List<Object> datum = new ArrayList<Object>();

		try {

			Client client = Client.create();

			WebResource webResource = client
					.resource("http://192.168.170.87:9099/getDailyStatistic?end=" + end + "&interval=" + interval
							+ "&keyword=" + URLEncoder.encode(keyword, "UTF-8") + "&limit=10&media_tags=" + media_tags
							+ "&source=" + source + "&start=" + start + "&elastic=" + elastic);

			ClientResponse response = webResource.type("application/json").get(ClientResponse.class);

			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
			}

			String output = response.getEntity(String.class);

			JSONArray array = new JSONArray(output);
			for (int i = 0; i < array.length(); i++) {
				JSONObject data = array.getJSONObject(i);
				datum.add(data);
			}

		} catch (Exception e) {

			e.printStackTrace();

		}

		return datum;
	}

	public List<Object> mediaShare(String start, String end, String keyword, String source, String media_tags,
			String limit, String elastic) {
		List<Object> datum = new ArrayList<Object>();

		try {

			Client client = Client.create();

			WebResource webResource = client.resource("http://192.168.170.87:9099/getMediaShare?end=" + end
					+ "&keyword=" + URLEncoder.encode(keyword, "UTF-8") + "&limit=10&media_tags=" + media_tags
					+ "&source=" + source + "&start=" + start + "&elastic=" + elastic);

			ClientResponse response = webResource.type("application/json").get(ClientResponse.class);

			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
			}

			String output = response.getEntity(String.class);

			JSONArray array = new JSONArray(output);
			for (int i = 0; i < array.length(); i++) {
				JSONObject data = array.getJSONObject(i);
				datum.add(data);
			}

		} catch (Exception e) {

			e.printStackTrace();

		}

		return datum;
	}

	public List<Object> influencers(String start, String end, String keyword, String source, String media_tags,
			String elastic, String limit) {
		List<Object> datum = new ArrayList<Object>();

		try {

			Client client = Client.create();

			WebResource webResource = client.resource("http://192.168.170.87:9099/getInfluencer?end=" + end
					+ "&keyword=" + URLEncoder.encode(keyword, "UTF-8") + "&limit=" + limit + "&media_tags="
					+ media_tags + "&source=" + source + "&start=" + start + "&elastic=" + elastic);

			ClientResponse response = webResource.type("application/json").get(ClientResponse.class);

			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
			}

			String output = response.getEntity(String.class);

			JSONArray array = new JSONArray(output);
			for (int i = 0; i < array.length(); i++) {
				JSONObject data = array.getJSONObject(i);
				datum.add(data);
			}

		} catch (Exception e) {

			e.printStackTrace();

		}

		return datum;
	}

	public List<Object> dailyStatisticPie(String start, String end, String keyword, String source, String media_tags,
			String elastic) {
		List<Object> datum = new ArrayList<Object>();

		try {

			Client client = Client.create();

			WebResource webResource = client.resource("http://192.168.170.87:9099/getPie?end=" + end + "&keyword="
					+ URLEncoder.encode(keyword, "UTF-8") + "&limit=10&media_tags=" + media_tags + "&source=" + source
					+ "&start=" + start + "&elastic=" + elastic);

			ClientResponse response = webResource.type("application/json").get(ClientResponse.class);

			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
			}

			String output = response.getEntity(String.class);
			JSONArray array = new JSONArray(output);
			for (int i = 0; i < array.length(); i++) {
				JSONObject data = array.getJSONObject(i);
				datum.add(data);
			}

		} catch (Exception e) {

			e.printStackTrace();

		}

		return datum;
	}

	public List<Object> data(String start, String end, String keyword, String source, String media_tags,
			String elastic) {
		List<Object> datum = new ArrayList<Object>();

		try {

			Client client = Client.create();

			WebResource webResource = client.resource("http://192.168.170.87:9099/getData?end=" + end + "&keyword="
					+ URLEncoder.encode(keyword, "UTF-8") + "&limit=100&media_tags=" + media_tags + "&source=" + source
					+ "&start=" + start + "&elastic=" + elastic);

			ClientResponse response = webResource.type("application/json").get(ClientResponse.class);

			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
			}

			String output = response.getEntity(String.class);
			JSONArray array = new JSONArray(output);
			for (int i = 0; i < array.length(); i++) {
				JSONObject data = array.getJSONObject(i);
				datum.add(data);
			}

		} catch (Exception e) {

			e.printStackTrace();

		}

		return datum;
	}

	public List<Object> content(String start, String end, String keyword, String source, String media_tags,
			String elastic) {
		List<Object> datum = new ArrayList<Object>();

		try {

			Client client = Client.create();

			WebResource webResource = client.resource("http://192.168.170.87:9099/getContent?end=" + end + "&keyword="
					+ URLEncoder.encode(keyword, "UTF-8") + "&limit=100&media_tags=" + media_tags + "&source=" + source
					+ "&start=" + start + "&elastic=" + elastic);

			ClientResponse response = webResource.type("application/json").get(ClientResponse.class);

			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
			}

			String output = response.getEntity(String.class);

			JSONArray array = new JSONArray(output);
			for (int i = 0; i < array.length(); i++) {
				JSONObject data = array.getJSONObject(i);
				datum.add(data);
			}

		} catch (Exception e) {

			e.printStackTrace();

		}

		return datum;
	}

}
