package com.ebdesk.report.pdf.dao;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.ebdesk.report.pdf.config.ExternalConfig;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

@Repository
public class PdfElasticCriteriaId {

	@Autowired
	private ExternalConfig externalConfig;

	public static void main(String[] args) throws UnsupportedEncodingException {
		PdfElasticCriteriaId es = new PdfElasticCriteriaId();

		String start = "202002220000";
		String end = "202002222359";
		String criteria_id = "546";
		String media_tags = "";
		String source = "";
		String elastic = "online";

		System.out.println(new Date());
		System.out.println(es.data(start, end, criteria_id, source, media_tags, elastic));
		System.out.println(new Date());
	}

	public List<Object> resume(String start, String end, String criteria_id, String source, String media_tags,
			String elastic) {
		List<Object> datum = new ArrayList<Object>();
		try {

			Client client = Client.create();

			WebResource webResource = client.resource("http://" + externalConfig.getApi_data() + "/getResume?end=" + end
					+ "&criteria_id=" + criteria_id + "&limit=10&media_tags=" + media_tags + "&source=" + source
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

	public List<Object> dailyStatistic(String start, String end, String criteria_id, String interval, String source,
			String media_tags, String elastic) {
		List<Object> datum = new ArrayList<Object>();

		try {

			Client client = Client.create();

			WebResource webResource = client
					.resource("http://" + externalConfig.getApi_data() + "/getDailyStatistic?end=" + end + "&interval="
							+ interval + "&criteria_id=" + criteria_id + "&limit=10&media_tags=" + media_tags
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

	public List<Object> mediaShare(String start, String end, String criteria_id, String source, String media_tags,
			String limit, String elastic) {
		List<Object> datum = new ArrayList<Object>();

		try {

			Client client = Client.create();

			WebResource webResource = client.resource("http://" + externalConfig.getApi_data() + "/getMediaShare?end="
					+ end + "&criteria_id=" + criteria_id + "&limit=10&media_tags=" + media_tags + "&source=" + source
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

	public List<Object> influencers(String start, String end, String criteria_id, String source, String media_tags,
			String elastic, String limit) {
		List<Object> datum = new ArrayList<Object>();

		try {

			Client client = Client.create();

			WebResource webResource = client.resource("http://" + externalConfig.getApi_data() + "/getInfluencer?end="
					+ end + "&criteria_id=" + criteria_id + "&limit=" + limit + "&media_tags=" + media_tags + "&source="
					+ source + "&start=" + start + "&elastic=" + elastic);

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

	public List<Object> dailyStatisticPie(String start, String end, String criteria_id, String source,
			String media_tags, String elastic) {
		List<Object> datum = new ArrayList<Object>();

		try {

			Client client = Client.create();

			WebResource webResource = client.resource("http://" + externalConfig.getApi_data() + "/getPie?end=" + end
					+ "&criteria_id=" + criteria_id + "&limit=10&media_tags=" + media_tags + "&source=" + source
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

	public List<Object> data(String start, String end, String criteria_id, String source, String media_tags,
			String elastic) {
		List<Object> datum = new ArrayList<Object>();

		try {

			Client client = Client.create();

			WebResource webResource = client.resource("http://" + externalConfig.getApi_data() + "/getData?end=" + end
					+ "&criteria_id=" + criteria_id + "&limit=100&media_tags=" + media_tags + "&source=" + source
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

	public List<Object> content(String start, String end, String criteria_id, String source, String media_tags,
			String elastic) {
		List<Object> datum = new ArrayList<Object>();

		try {

			Client client = Client.create();

			WebResource webResource = client.resource("http://" + externalConfig.getApi_data() + "/getContent?end="
					+ end + "&criteria_id=" + criteria_id + "&limit=100&media_tags=" + media_tags + "&source=" + source
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
