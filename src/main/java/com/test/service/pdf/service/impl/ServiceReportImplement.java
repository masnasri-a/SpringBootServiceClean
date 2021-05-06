package com.test.service.pdf.service.impl;

import com.test.service.pdf.model.MessageModel;
import com.test.service.pdf.dao.ManagementReport;
import com.test.service.pdf.service.ServiceReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ServiceReportImplement implements ServiceReport {

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    ServiceReport service;

    @Autowired
    ManagementReport managementReport;

    @Override
    public MessageModel getReport() {
        MessageModel msg = new MessageModel();
        try {
            msg.setData(managementReport.getReport());
            msg.setStatus(true);
            msg.setMessage("success");
        } catch (Exception e) {
            msg.setStatus(false);
            msg.setMessage("failed");
        }

        return msg;
    }

}
