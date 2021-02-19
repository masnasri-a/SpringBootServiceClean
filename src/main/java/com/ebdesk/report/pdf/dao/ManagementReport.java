package com.ebdesk.report.pdf.dao;

import com.ebdesk.report.pdf.config.ElasticConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@Repository
public class ManagementReport {
    @Autowired
    private ElasticConfig elasticConfig;

    public String deleteById(String id) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException, IOException {
        String hsl = "";
        System.out.println("masuk");
        try {
            RestClientBuilder builder = RestClient.builder(new HttpHost(elasticConfig.getHost().split(":")[0], Integer.parseInt(elasticConfig.getHost().split(":")[1])));
            RestHighLevelClient client = new RestHighLevelClient(builder);
            DeleteRequest request = new DeleteRequest("management-report", "_doc", id);
            DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);
            hsl= "Id " + response.getId() + " has been deleted";
            client.close();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return hsl;
    }

    public String insertManagementReport(JSONObject result) throws IOException {
        RestClientBuilder builder = RestClient.builder(new HttpHost(elasticConfig.getHost().split(":")[0], Integer.parseInt(elasticConfig.getHost().split(":")[1])))
                .setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
                    @Override
                    public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder builder) {
                        return builder
                                .setConnectTimeout(180000)
                                .setSocketTimeout(300000);
                    }
                });
        RestHighLevelClient client = new RestHighLevelClient(builder);

        IndexRequest request = new IndexRequest("management-report","_doc");
        request.source(result, XContentType.JSON);
        IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);

        client.close();

        return indexResponse.getId().toString();
    }

    public JSONArray getReport(String w_id, String topic, String platform){
        String param = "{\"query\":{\"bool\":{\"must\":[{\"match_phrase\":{\"w_id\":\""
                + w_id + "\"}},{\"match_phrase\":{\"topic\":\"" + topic + "\"}},{\"match_phrase\":{\"platform\":\""
                + platform + "\"}}]}},\"sort\":[{\"created_at\":{\"order\":\"desc\"}}],\"size\":1000}";

        JSONArray result = new JSONArray();

        try {
            Client client = Client.create();

            WebResource webResource = client.resource("http://"+elasticConfig.getHost()+"/management-report/_search");
            String input = param;

            ClientResponse response = webResource.type("application/json").post(ClientResponse.class, input);

            if (response.getStatus() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
            }

            String output = response.getEntity(String.class);

            ObjectNode dataElastic = new ObjectMapper().readValue(output, ObjectNode.class);

            for (JsonNode node : dataElastic.get("hits").get("hits")) {
                JSONObject json = new JSONObject();
                json.put("id", node.get("_id"));
                json.put("file_name", node.get("_source").get("file_name"));
                json.put("category", node.get("_source").get("category"));
                json.put("topic", node.get("_source").get("topic"));
                json.put("status", node.get("_source").get("status"));
                json.put("start_date", node.get("_source").get("start_date"));
                json.put("end_date", node.get("_source").get("end_date"));
                json.put("created_at", node.get("_source").get("created_at"));
                result.add(json);
            }

//            result = dataElastic.get("hits").get("hits");
        }catch (Exception e ){
            e.printStackTrace();
        }

        return result;
    }

    public void updateStatus(String id, JSONObject result) throws IOException {
        RestClientBuilder builder = RestClient.builder(new HttpHost(elasticConfig.getHost().split(":")[0], Integer.parseInt(elasticConfig.getHost().split(":")[1])))
                .setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
                    @Override
                    public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder builder) {
                        return builder
                                .setConnectTimeout(60000)
                                .setSocketTimeout(300000);
                    }
                });

        RestHighLevelClient client = new RestHighLevelClient(builder);

        UpdateRequest request = new UpdateRequest("management-report", "_doc", id);
        request.doc(result, XContentType.JSON).docAsUpsert(true).retryOnConflict(30);
        UpdateResponse updateResponse = client.update(request, RequestOptions.DEFAULT);

        client.close();
    }
}
