package com.ebdesk.report.pdf.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

public interface ServiceReport {
	Map<String, Object> serviceReport(String start, String end, String criteria, String interval, String source,
			String media_tags, String elastic, String limit, String w_id)
			throws FileNotFoundException, InvalidFormatException, IOException, ParseException;
}
