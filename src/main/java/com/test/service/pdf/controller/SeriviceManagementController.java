package com.test.service.pdf.controller;

import com.test.service.pdf.model.BodyModel;
import com.test.service.pdf.model.MessageModel;
import com.test.service.pdf.service.ServiceReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class SeriviceManagementController {

    @Autowired
    ServiceReport service;

    @GetMapping("/get-report")
    public MessageModel serviceGetReportController(){
        MessageModel msg = new MessageModel();
        msg = service.getReport();
        return msg;
    }

}
