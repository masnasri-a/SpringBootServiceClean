package com.ebdesk.report.pdf.controller;

import com.ebdesk.report.pdf.model.MessageModel;
import com.ebdesk.report.pdf.service.ServiceReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@RestController
public class SeriviceManagementController {
    @Autowired
    ServiceReport service;

    @DeleteMapping("/delete-report")
    public MessageModel serviceReportController(@RequestParam(value = "id") String id
    ) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException, IOException {
        MessageModel msg = new MessageModel();
        msg = service.deleteById(id);
        return msg;
    }

    @GetMapping("/get-report")
    public MessageModel serviceGetReportController(@RequestParam(value = "w_id") String w_id,
                                                   @RequestParam(value = "topic") String topic,
                                                   @RequestParam(value = "platform") String platform){
        MessageModel msg = new MessageModel();
        msg = service.getReport(w_id, topic.toLowerCase(), platform.toLowerCase());
        return msg;
    }

    @PostMapping("/generate-report")
    public MessageModel serviceReportController(@RequestParam(value = "criteria") String criteria,
                                                @RequestParam(value = "w_id") String w_id,
                                                @RequestParam(value = "category", defaultValue = "pdf") String category,
                                                @RequestParam(value = "platform") String platform,
                                                @RequestParam(value = "start") String start, @RequestParam(value = "end") String end,
                                                @RequestParam(value = "interval", defaultValue = "hour") String interval,
                                                @RequestParam(value = "w_app_logo", defaultValue = "default+logo.png") String w_app_logo) throws IOException {

        MessageModel msg = new MessageModel();

        msg = service.generateReport(criteria, w_id, category, platform, start, end, interval, w_app_logo);

        return msg;

    }
}
