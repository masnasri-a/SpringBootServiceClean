package com.ebdesk.report.pdf.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Map;

import com.ebdesk.report.pdf.model.MessageModel;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

public interface ServiceReport {
	Map<String, Object> serviceReport(String start, String end, String criteria, String interval, String source,
			String media_tags, String elastic, String limit, String w_id, String w_app_logo, String id)
			throws FileNotFoundException, InvalidFormatException, IOException, ParseException;

	MessageModel getReport(String w_id, String topic, String platform);

	MessageModel generateReport(String criteria, String w_id, String category, String platform, String start, String end,
								String interval, String w_app_logo) throws IOException;

	MessageModel deleteById(String id) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException;
}
