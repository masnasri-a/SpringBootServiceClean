package com.ebdesk.report.pdf.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ebdesk.report.pdf.model.MessageModel;
import com.ebdesk.report.pdf.service.ServiceReport;

@RestController
public class SeviceReportController {

	@Autowired
	ServiceReport service;

	@PostMapping("/generate")
	public MessageModel generateReportController(@RequestParam(value = "start") String start,
			@RequestParam(value = "end") String end, @RequestParam(value = "criteria") String criteria,
			@RequestParam(value = "interval", defaultValue = "hour") String interval,
			@RequestParam(value = "w_app_logo", defaultValue = "default+logo.png") String w_app_logo,
			@RequestParam(value = "elastic", defaultValue = "online", required = false) String elastic,
			@RequestParam(value = "w_id", defaultValue = "1", required = false) String w_id,
			@RequestParam(value = "id", defaultValue = "", required = false) String id)
			throws FileNotFoundException, InvalidFormatException, IOException, ParseException {

		MessageModel msg = new MessageModel();

		try {

			Map<String, Object> ret = service.serviceReport(start, end, criteria, interval, "", "", elastic, "", w_id, w_app_logo, id);

			msg.setStatus(true);
			msg.setMessage("success");

			ret.remove("path");
			msg.setData(ret);

		} catch (Exception e) {
			e.printStackTrace();
			msg.setStatus(false);
			msg.setMessage("failed");

		}

		return msg;
	}

	@GetMapping("/download")
	public void downloadReport(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "start") String start, @RequestParam(value = "end") String end,
			@RequestParam(value = "criteria") String criteria,
			@RequestParam(value = "interval", defaultValue = "hour") String interval,
			@RequestParam(value = "w_app_logo", defaultValue = "default+logo.png") String w_app_logo,
			@RequestParam(value = "elastic", defaultValue = "online", required = false) String elastic,
			@RequestParam(value = "w_id", defaultValue = "1", required = false) String w_id)
			throws FileNotFoundException, InvalidFormatException, IOException, ParseException {

		Map<String, Object> ret = service.serviceReport(start, end, criteria, interval, "", "", elastic, "", w_id, w_app_logo, "");

		InputStream myStream = new FileInputStream(new File(ret.get("path").toString()));

		response.addHeader("Content-disposition", "attachment;filename=" + ret.get("file_name").toString() + "");
		response.setContentType("application/octet-stream");

		IOUtils.copy(myStream, response.getOutputStream());
		response.flushBuffer();

	}

}
