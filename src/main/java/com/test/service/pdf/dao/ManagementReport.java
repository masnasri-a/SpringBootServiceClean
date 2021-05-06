package com.test.service.pdf.dao;

import com.test.service.pdf.config.ElasticConfig;
import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ManagementReport {
    @Autowired
    private ElasticConfig elasticConfig;

    public JSONArray getReport(){
        JSONArray result = new JSONArray();
        result.add("test1");
        result.add("test2");
        result.add("test3");
        result.add("test4");
        return result;
    }


}
