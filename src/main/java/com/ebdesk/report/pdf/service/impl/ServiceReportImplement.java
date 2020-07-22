package com.ebdesk.report.pdf.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ebdesk.report.pdf.config.ExternalConfig;
import com.ebdesk.report.pdf.dao.DownloadImagePrinted;
import com.ebdesk.report.pdf.dao.GenerateChart;
import com.ebdesk.report.pdf.dao.PdfElasticCriteriaId;
import com.ebdesk.report.pdf.dao.RedisDao;
import com.ebdesk.report.pdf.dao.SqlDao;
import com.ebdesk.report.pdf.service.ServiceReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@Service
public class ServiceReportImplement implements ServiceReport {

	@Autowired
	PdfElasticCriteriaId es;

	@Autowired
	GenerateChart chart;

	@Autowired
	RedisDao redis;

	@Autowired
	DownloadImagePrinted download;

	@Autowired
	SqlDao sql;

	@Autowired
	private ExternalConfig externalConfig;

	@Override
	public Map<String, Object> serviceReport(String start, String end, String criteria, String interval, String source,
			String media_tags, String elastic, String limit, String w_id)
			throws FileNotFoundException, InvalidFormatException, IOException, ParseException {

		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, String> c = redis.keywordRedis(criteria);
		String workspace_logo = externalConfig.getPath_workspace_logo() + sql.pathLogoWorkspace(w_id);

		SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm");

		ObjectMapper om = new ObjectMapper();

		List<Object> data_resume = es.resume(start, end, criteria, source, media_tags, elastic);
		List<Object> data_table = es.data(start, end, criteria, source, media_tags, elastic);
		List<Object> data_content = es.content(start, end, criteria, source, media_tags, elastic);

		String name_file = "";
		String output = "";

		if (elastic.equals("online")) {
			Document document = new Document();
			try {

				name_file = "report_online_" + c.get("criteria_name") + ".pdf";

				PdfWriter writer = PdfWriter.getInstance(document,
						new FileOutputStream(externalConfig.getPath_save() + name_file));
				document.open();

				// Add header image
				Image header_image = Image.getInstance(externalConfig.getPath_chart_image() + "header.jpg");
				// Scale to new height and new width of image
				header_image.scaleAbsolute(280, 70);
				// Add to document
				header_image.setAlignment(Element.ALIGN_CENTER);
				document.add(header_image);

				Paragraph space = new Paragraph();
				for (int i = 0; i < 9; i++) {
					space.add(new Paragraph(" "));
				}

				Font font_cover = new Font(FontFamily.UNDEFINED, 20.0f, Font.BOLD, BaseColor.BLACK);
				Font font_title = new Font(FontFamily.UNDEFINED, 14.0f, Font.BOLD, BaseColor.BLACK);
				Font font_table = new Font(FontFamily.UNDEFINED, 12.0f, Font.BOLD, BaseColor.BLACK);

				Paragraph title = new Paragraph("LAPORAN MEDIA ONLINE", font_cover);
				Paragraph desc = new Paragraph(c.get("criteria_name"), font_cover);
				Paragraph date = new Paragraph(
						sdf.format(format.parseObject(start)) + " - " + sdf.format(format.parseObject(end)),
						font_cover);

				title.setAlignment(Element.ALIGN_CENTER);
				desc.setAlignment(Element.ALIGN_CENTER);
				date.setAlignment(Element.ALIGN_CENTER);

				document.add(space);
				document.add(title);
				document.add(space);

				File f = new File(workspace_logo);

				if (f.exists()) {
					Image w_app_logo = Image.getInstance(workspace_logo);
					w_app_logo.scaleAbsolute(180, 70);
					w_app_logo.setAlignment(Element.ALIGN_CENTER);
					document.add(w_app_logo);
				} else {
					Image w_app_logo = Image.getInstance(externalConfig.getPath_workspace_logo() + "ebdesk.png");
					w_app_logo.scaleAbsolute(180, 70);
					w_app_logo.setAlignment(Element.ALIGN_CENTER);
					document.add(w_app_logo);
				}

				document.add(space);
				document.add(desc);
				document.add(date);

				// page 2
				Paragraph resume = new Paragraph("Resume", font_title);
				resume.setAlignment(Element.ALIGN_CENTER);
				document.newPage();
				document.add(resume);

				PdfPTable table_resume = new PdfPTable(5);
				table_resume.setWidthPercentage(100); // Width 100%
				table_resume.setSpacingBefore(10f); // Space before table
				table_resume.setSpacingAfter(10f); // Space after table

				// Set Column widths
				float[] columnWidths = { 1f, 1f, 1f, 1f, 1f };
				table_resume.setWidths(columnWidths);

				PdfPCell Media = new PdfPCell(new Paragraph("Media", font_table));
				Media.setBackgroundColor(BaseColor.LIGHT_GRAY);
				Media.setVerticalAlignment(Element.ALIGN_MIDDLE);
				PdfPCell News = new PdfPCell(new Paragraph("News", font_table));
				News.setBackgroundColor(BaseColor.LIGHT_GRAY);
				News.setVerticalAlignment(Element.ALIGN_MIDDLE);
				PdfPCell Positive = new PdfPCell(new Paragraph("Positive", font_table));
				Positive.setBackgroundColor(BaseColor.LIGHT_GRAY);
				Positive.setVerticalAlignment(Element.ALIGN_MIDDLE);
				PdfPCell Neutral = new PdfPCell(new Paragraph("Neutral", font_table));
				Neutral.setBackgroundColor(BaseColor.LIGHT_GRAY);
				Neutral.setVerticalAlignment(Element.ALIGN_MIDDLE);
				PdfPCell Negative = new PdfPCell(new Paragraph("Negative", font_table));
				Negative.setBackgroundColor(BaseColor.LIGHT_GRAY);
				Negative.setVerticalAlignment(Element.ALIGN_MIDDLE);

				table_resume.addCell(Media);
				table_resume.addCell(News);
				table_resume.addCell(Positive);
				table_resume.addCell(Neutral);
				table_resume.addCell(Negative);

				String media = "";
				String news = "";
				String positive = "";
				String neutral = "";
				String negative = "";

				for (Object data : data_resume) {
					ObjectNode node = om.readValue(data.toString(), ObjectNode.class);
					if (node.has("media")) {
						media = node.get("media").asText();
					} else if (node.has("news")) {
						news = node.get("news").asText();
					} else if (node.has("positive")) {
						positive = node.get("positive").asText();
					} else if (node.has("neutral")) {
						neutral = node.get("neutral").asText();
					} else if (node.has("negative")) {
						negative = node.get("negative").asText();
					}
				}

				table_resume.addCell(media);
				table_resume.addCell(news);
				table_resume.addCell(positive);
				table_resume.addCell(neutral);
				table_resume.addCell(negative);
				document.add(table_resume);

				Map<String, String> resource_chart = chart.chartImage(start, end, criteria, interval, source,
						media_tags, elastic, limit, externalConfig.getPath_chart_image());

				String statistic_bar = resource_chart.get("statistic_bar");
				String statistic_sentiment_pie = resource_chart.get("statistic_pie");
				String media_share = resource_chart.get("media_share");
				String influencers = resource_chart.get("influencer");

				Image i_statistic_bar = Image.getInstance(statistic_bar);
				i_statistic_bar.scaleAbsolute(261, 160);
				i_statistic_bar.setAlignment(Element.ALIGN_CENTER);
				document.add(i_statistic_bar);

				Image i_pie = Image.getInstance(statistic_sentiment_pie);
				i_pie.scaleAbsolute(261, 160);
				i_pie.setAlignment(Element.ALIGN_CENTER);
				document.add(i_pie);

				Image i_media = Image.getInstance(media_share);
				i_media.scaleAbsolute(261, 160);
				i_media.setAlignment(Element.ALIGN_CENTER);
				document.add(i_media);

				Image i_influencers = Image.getInstance(influencers);
				i_influencers.scaleAbsolute(261, 160);
				i_influencers.setAlignment(Element.ALIGN_CENTER);
				document.add(i_influencers);

				document.newPage();

				// page 3
				Paragraph contents = new Paragraph("Table of Contents : " + sdf.format(format.parseObject(start))
						+ " - " + sdf.format(format.parseObject(end)), font_title);
				document.add(contents);

				PdfPTable table_contents = new PdfPTable(7);
				table_contents.setWidthPercentage(109); // Width 100%
				table_contents.setSpacingBefore(10f); // Space before table
				table_contents.setSpacingAfter(10f); // Space after table

				// Set Column widths
				float[] columnContents = { 0.3f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 1.3f };
				table_contents.setHeaderRows(1);
				table_contents.setWidths(columnContents);

				PdfPCell cell1 = new PdfPCell(new Paragraph("No.", font_table));
				cell1.setBackgroundColor(BaseColor.LIGHT_GRAY);
				cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
				PdfPCell cell2 = new PdfPCell(new Paragraph("Date", font_table));
				cell2.setBackgroundColor(BaseColor.LIGHT_GRAY);
				cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
				PdfPCell cell3 = new PdfPCell(new Paragraph("News Title", font_table));
				cell3.setBackgroundColor(BaseColor.LIGHT_GRAY);
				cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell3.setVerticalAlignment(Element.ALIGN_MIDDLE);
				PdfPCell cell4 = new PdfPCell(new Paragraph("Tone", font_table));
				cell4.setBackgroundColor(BaseColor.LIGHT_GRAY);
				cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell4.setVerticalAlignment(Element.ALIGN_MIDDLE);
				PdfPCell cell5 = new PdfPCell(new Paragraph("Media", font_table));
				cell5.setBackgroundColor(BaseColor.LIGHT_GRAY);
				cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);
				PdfPCell cell6 = new PdfPCell(new Paragraph("Influencers", font_table));
				cell6.setBackgroundColor(BaseColor.LIGHT_GRAY);
				cell6.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell6.setVerticalAlignment(Element.ALIGN_MIDDLE);
				PdfPCell cell7 = new PdfPCell(new Paragraph("Resume", font_table));
				cell7.setBackgroundColor(BaseColor.LIGHT_GRAY);
				cell7.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell7.setVerticalAlignment(Element.ALIGN_MIDDLE);

				table_contents.addCell(cell1);
				table_contents.addCell(cell2);
				table_contents.addCell(cell3);
				table_contents.addCell(cell4);
				table_contents.addCell(cell5);
				table_contents.addCell(cell6);
				table_contents.addCell(cell7);

				int no = 1;
				for (Object obj : data_table) {
					@SuppressWarnings("unchecked")
					Map<String, String> data = om.readValue(obj.toString(), Map.class);

					table_contents.addCell(String.valueOf(no));
					table_contents.addCell(data.get("date"));
					table_contents.addCell(data.get("news_title"));
					table_contents.addCell(data.get("tone"));
					table_contents.addCell(data.get("media"));
					table_contents.addCell(data.get("influencers"));

					PdfPCell cellResume = new PdfPCell(new Paragraph(data.get("resume")));
					cellResume.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
					table_contents.addCell(cellResume);

					no++;
				}

				document.add(table_contents);

				// page 4
				document.newPage();

				for (Object object : data_content) {
					ObjectNode node = om.readValue(object.toString(), ObjectNode.class);

					PdfPTable table_detail_contents = new PdfPTable(2);
					table_detail_contents.setWidthPercentage(100); // Width 100%
					table_detail_contents.setSpacingBefore(10f); // Space before table
					table_detail_contents.setSpacingAfter(10f); // Space after table
					table_detail_contents.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);

					// Set Column widths
					float[] columnDetailContents = { 0.3f, 2f };
					table_detail_contents.setWidths(columnDetailContents);

					table_detail_contents.addCell(new Paragraph("Title"));
					if (node.has("title")) {
						table_detail_contents.addCell(new Paragraph(node.get("title").asText()));
					}

					table_detail_contents.addCell(new Paragraph("Media"));
					if (node.has("media")) {
						table_detail_contents.addCell(new Paragraph(node.get("media").asText()));
					}

					table_detail_contents.addCell(new Paragraph("Date"));
					if (node.has("date")) {
						table_detail_contents.addCell(new Paragraph(node.get("date").asText()));
					}

					table_detail_contents.addCell(new Paragraph("Link"));
					if (node.has("link")) {
						table_detail_contents.addCell(new Paragraph(node.get("link").asText()));
					}

					table_detail_contents.addCell(new Paragraph("Tone"));
					if (node.has("tone")) {
						table_detail_contents.addCell(new Paragraph(node.get("tone").asText()));
					}

					table_detail_contents.addCell(new Paragraph("Resume"));

					if (node.has("resume")) {
						PdfPCell cellResume = new PdfPCell(new Paragraph(node.get("resume").asText()));
						cellResume.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
						table_detail_contents.addCell(cellResume);
					}

					table_detail_contents.addCell(new Paragraph("Reporter"));
					table_detail_contents.addCell(new Paragraph(""));

					table_detail_contents.addCell(new Paragraph("Author"));
					table_detail_contents.addCell(new Paragraph(""));
					document.add(table_detail_contents);

					String path_logo_news = externalConfig.getNews_logo() + node.get("media").asText().replace(" ", "+")
							+ ".jpg";

					File f_news = new File(path_logo_news);

					if (f_news.exists()) {
						Image logo = Image.getInstance(path_logo_news);
						logo.scaleAbsolute(70, 70);
						logo.setAlignment(Element.ALIGN_CENTER);
						document.add(logo);
					} else {
						Image logo = Image.getInstance(externalConfig.getNews_logo() + "default+logo.png");
						logo.scaleAbsolute(65, 65);
						logo.setAlignment(Element.ALIGN_CENTER);
						document.add(logo);
					}

					document.add(new Paragraph(" "));

					if (node.has("content")) {
						Paragraph content = new Paragraph(node.get("content").asText());
						content.setAlignment(Element.ALIGN_JUSTIFIED);
						document.add(content);
					}
					
					document.newPage();

				}

				output = externalConfig.getPath_save() + name_file;

				document.close();
				writer.close();

			} catch (DocumentException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		} else if (elastic.equals("printed")) {
			Document document = new Document();
			try {

				name_file = "report_cetak_" + c.get("criteria_name") + ".pdf";

				PdfWriter writer = PdfWriter.getInstance(document,
						new FileOutputStream(externalConfig.getPath_save() + name_file));
				document.open();

				Image header_image = Image.getInstance(externalConfig.getPath_chart_image() + "header.jpg");
				header_image.scaleAbsolute(280, 70);
				header_image.setAlignment(Element.ALIGN_CENTER);
				document.add(header_image);

				Paragraph space = new Paragraph();
				for (int i = 0; i < 9; i++) {
					space.add(new Paragraph(" "));
				}

				Font font_cover = new Font(FontFamily.UNDEFINED, 20.0f, Font.BOLD, BaseColor.BLACK);
				Font font_title = new Font(FontFamily.UNDEFINED, 14.0f, Font.BOLD, BaseColor.BLACK);
				Font font_table = new Font(FontFamily.UNDEFINED, 12.0f, Font.BOLD, BaseColor.BLACK);

				Paragraph title = new Paragraph("LAPORAN MEDIA CETAK", font_cover);
				Paragraph desc = new Paragraph(c.get("criteria_name"), font_cover);
				Paragraph date = new Paragraph(
						sdf.format(format.parseObject(start)) + " - " + sdf.format(format.parseObject(end)),
						font_cover);

				title.setAlignment(Element.ALIGN_CENTER);
				desc.setAlignment(Element.ALIGN_CENTER);
				date.setAlignment(Element.ALIGN_CENTER);

				document.add(space);
				document.add(title);
				document.add(space);

				File f = new File(workspace_logo);

				if (f.exists()) {
					Image w_app_logo = Image.getInstance(workspace_logo);
					w_app_logo.scaleAbsolute(180, 70);
					w_app_logo.setAlignment(Element.ALIGN_CENTER);
					document.add(w_app_logo);
				} else {
					Image w_app_logo = Image.getInstance(externalConfig.getPath_workspace_logo() + "ebdesk.png");
					w_app_logo.scaleAbsolute(180, 70);
					w_app_logo.setAlignment(Element.ALIGN_CENTER);
					document.add(w_app_logo);
				}

				document.add(space);
				document.add(desc);
				document.add(date);

				// page 2
				Paragraph resume = new Paragraph("Resume", font_title);
				document.newPage();
				document.add(resume);

				PdfPTable table_resume = new PdfPTable(5);
				table_resume.setWidthPercentage(100); // Width 100%
				table_resume.setSpacingBefore(10f); // Space before table
				table_resume.setSpacingAfter(10f); // Space after table

				// Set Column widths
				float[] columnWidths = { 1f, 1f, 1f, 1f, 1f };
				table_resume.setWidths(columnWidths);

				PdfPCell Media = new PdfPCell(new Paragraph("Media", font_table));
				Media.setBackgroundColor(BaseColor.LIGHT_GRAY);
				Media.setVerticalAlignment(Element.ALIGN_MIDDLE);
				PdfPCell News = new PdfPCell(new Paragraph("News", font_table));
				News.setBackgroundColor(BaseColor.LIGHT_GRAY);
				News.setVerticalAlignment(Element.ALIGN_MIDDLE);
				PdfPCell Positive = new PdfPCell(new Paragraph("Positive", font_table));
				Positive.setBackgroundColor(BaseColor.LIGHT_GRAY);
				Positive.setVerticalAlignment(Element.ALIGN_MIDDLE);
				PdfPCell Neutral = new PdfPCell(new Paragraph("Neutral", font_table));
				Neutral.setBackgroundColor(BaseColor.LIGHT_GRAY);
				Neutral.setVerticalAlignment(Element.ALIGN_MIDDLE);
				PdfPCell Negative = new PdfPCell(new Paragraph("Negative", font_table));
				Negative.setBackgroundColor(BaseColor.LIGHT_GRAY);
				Negative.setVerticalAlignment(Element.ALIGN_MIDDLE);

				table_resume.addCell(Media);
				table_resume.addCell(News);
				table_resume.addCell(Positive);
				table_resume.addCell(Neutral);
				table_resume.addCell(Negative);

				String media = "";
				String news = "";
				String positive = "";
				String neutral = "";
				String negative = "";

				for (Object data : data_resume) {
					ObjectNode node = om.readValue(data.toString(), ObjectNode.class);
					if (node.has("media")) {
						media = node.get("media").asText();
					} else if (node.has("news")) {
						news = node.get("news").asText();
					} else if (node.has("positive")) {
						positive = node.get("positive").asText();
					} else if (node.has("neutral")) {
						neutral = node.get("neutral").asText();
					} else if (node.has("negative")) {
						negative = node.get("negative").asText();
					}
				}

				table_resume.addCell(media);
				table_resume.addCell(news);
				table_resume.addCell(positive);
				table_resume.addCell(neutral);
				table_resume.addCell(negative);
				document.add(table_resume);

				// statistic
				Paragraph daily_statistic = new Paragraph("Daily Statistic", font_title);
				document.add(daily_statistic);

				Map<String, String> resource_chart = chart.chartImage(start, end, criteria, interval, source,
						media_tags, elastic, limit, externalConfig.getPath_chart_image());

				String statistic_bar = resource_chart.get("statistic_bar");
				String statistic_sentiment_pie = resource_chart.get("statistic_pie");
				String media_share = resource_chart.get("media_share");
				String influencers = resource_chart.get("influencer");

				Image i_statistic_bar = Image.getInstance(statistic_bar);
				i_statistic_bar.scaleAbsolute(261, 160);
				i_statistic_bar.setAlignment(Element.ALIGN_CENTER);
				document.add(i_statistic_bar);

				Image i_pie = Image.getInstance(statistic_sentiment_pie);
				i_pie.scaleAbsolute(261, 160);
				i_pie.setAlignment(Element.ALIGN_CENTER);
				document.add(i_pie);

				Image i_media = Image.getInstance(media_share);
				i_media.scaleAbsolute(261, 160);
				i_media.setAlignment(Element.ALIGN_CENTER);
				document.add(i_media);

				Image i_influencers = Image.getInstance(influencers);
				i_influencers.scaleAbsolute(261, 160);
				i_influencers.setAlignment(Element.ALIGN_CENTER);
				document.add(i_influencers);
				document.newPage();

				// page 3
				Paragraph contents = new Paragraph("Table of Contents : " + sdf.format(format.parseObject(start))
						+ " - " + sdf.format(format.parseObject(end)), font_title);
				document.add(contents);

				PdfPTable table_contents = new PdfPTable(7);
				table_contents.setWidthPercentage(109); // Width 100%
				table_contents.setSpacingBefore(10f); // Space before table
				table_contents.setSpacingAfter(10f); // Space after table

				// Set Column widths
				float[] columnContents = { 0.3f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 1.5f };
				table_contents.setHeaderRows(1);
				table_contents.setWidths(columnContents);

				PdfPCell cell1 = new PdfPCell(new Paragraph("No.", font_table));
				cell1.setBackgroundColor(BaseColor.LIGHT_GRAY);
				cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
				PdfPCell cell2 = new PdfPCell(new Paragraph("Date", font_table));
				cell2.setBackgroundColor(BaseColor.LIGHT_GRAY);
				cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
				PdfPCell cell3 = new PdfPCell(new Paragraph("News Title", font_table));
				cell3.setBackgroundColor(BaseColor.LIGHT_GRAY);
				cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell3.setVerticalAlignment(Element.ALIGN_MIDDLE);
				PdfPCell cell4 = new PdfPCell(new Paragraph("Sentiment", font_table));
				cell4.setBackgroundColor(BaseColor.LIGHT_GRAY);
				cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell4.setVerticalAlignment(Element.ALIGN_MIDDLE);
				PdfPCell cell5 = new PdfPCell(new Paragraph("Media", font_table));
				cell5.setBackgroundColor(BaseColor.LIGHT_GRAY);
				cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);
				PdfPCell cell6 = new PdfPCell(new Paragraph("Influencers", font_table));
				cell6.setBackgroundColor(BaseColor.LIGHT_GRAY);
				cell6.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell6.setVerticalAlignment(Element.ALIGN_MIDDLE);
				PdfPCell cell7 = new PdfPCell(new Paragraph("Resume", font_table));
				cell7.setBackgroundColor(BaseColor.LIGHT_GRAY);
				cell7.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell7.setVerticalAlignment(Element.ALIGN_MIDDLE);

				table_contents.addCell(cell1);
				table_contents.addCell(cell2);
				table_contents.addCell(cell3);
				table_contents.addCell(cell4);
				table_contents.addCell(cell5);
				table_contents.addCell(cell6);
				table_contents.addCell(cell7);

				int no = 1;
				for (Object obj : data_table) {
					@SuppressWarnings("unchecked")
					Map<String, String> data = om.readValue(obj.toString(), Map.class);

					table_contents.addCell(String.valueOf(no));
					table_contents.addCell(data.get("date"));
					table_contents.addCell(data.get("news_title"));
					table_contents.addCell(data.get("tone"));
					table_contents.addCell(data.get("media"));
					table_contents.addCell(data.get("influencers"));

					PdfPCell cellResume = new PdfPCell(new Paragraph(data.get("resume")));
					cellResume.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
					table_contents.addCell(cellResume);

					no++;
				}

				document.add(table_contents);
				document.newPage();

				// page 4
				for (Object object : data_content) {
					ObjectNode node = om.readValue(object.toString(), ObjectNode.class);
					document.newPage();

					String path_logo_news = externalConfig.getNews_logo() + node.get("media").asText().replace(" ", "+")
							+ ".jpg";

					File fl = new File(path_logo_news);

					if (fl.exists()) {
						Image images = Image.getInstance(path_logo_news);
						images.scaleAbsolute(70, 70);
						images.setAlignment(Element.ALIGN_LEFT);
						document.add(images);
					} else {
						String default_logo = externalConfig.getNews_logo() + "default+logo.png";
						Image images = Image.getInstance(default_logo);
						images.scaleAbsolute(70, 70);
						images.setAlignment(Element.ALIGN_LEFT);
						document.add(images);
					}

					List<String> list_image = download.downloadImage(node.get("link").asText(),
							node.get("media").asText(), externalConfig.getPrinted_image());

					PdfPTable table_detail_contents = new PdfPTable(2);
					table_detail_contents.setWidthPercentage(100); // Width 100%
					table_detail_contents.setSpacingBefore(10f); // Space before table
					table_detail_contents.setSpacingAfter(10f); // Space after table

					// Set Column widths
					float[] columnDetailContents = { 0.3f, 2f };
					table_detail_contents.setWidths(columnDetailContents);

					table_detail_contents.addCell(new Paragraph("Title"));
					if (node.has("title")) {
						table_detail_contents.addCell(new Paragraph(node.get("title").asText()));
					}

					table_detail_contents.addCell(new Paragraph("Media"));
					if (node.has("media")) {
						table_detail_contents.addCell(new Paragraph(node.get("media").asText()));
					}

					table_detail_contents.addCell(new Paragraph("Date"));
					if (node.has("date")) {
						table_detail_contents.addCell(node.get("date").asText());
					}

					table_detail_contents.addCell(new Paragraph("Link"));
					if (node.has("link")) {

						PdfPCell cell = new PdfPCell(new Phrase(node.get("link").asText()));
						cell.setCellEvent(new LinkInCell(node.get("link").asText()));
						table_detail_contents.addCell(cell);
//						table_detail_contents.addCell(new Paragraph(node.get("link").asText()));
					}

					table_detail_contents.addCell(new Paragraph("Sentiment"));
					if (node.has("tone")) {
						table_detail_contents.addCell(new Paragraph(node.get("tone").asText()));
					}

					table_detail_contents.addCell(new Paragraph("Resume"));
					if (node.has("resume")) {
						PdfPCell cellResume = new PdfPCell(new Paragraph(node.get("resume").asText()));
						cellResume.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
						table_detail_contents.addCell(cellResume);
					}

					table_detail_contents.addCell(new Paragraph("Reporter"));
					table_detail_contents.addCell(new Paragraph(""));

					table_detail_contents.addCell(new Paragraph("PR Value"));
					table_detail_contents.addCell(new Paragraph(""));

					table_detail_contents.addCell(new Paragraph("AD Value"));
					table_detail_contents.addCell(new Paragraph(""));
					document.add(table_detail_contents);

					for (Object image : list_image) {
						Image images = Image.getInstance(image.toString());
						images.scaleToFit(500, 500);
						images.setAlignment(Element.ALIGN_CENTER);
						document.add(images);
					}

					document.newPage();
				}

				output = externalConfig.getPath_save() + name_file;

				document.close();
				writer.close();

			} catch (DocumentException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		map.put("path", output);
		map.put("file_name", name_file);

		return map;
	}

	class LinkInCell implements PdfPCellEvent {
		protected String url;

		public LinkInCell(String url) {
			this.url = url;
		}

		public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
			PdfWriter writer = canvases[0].getPdfWriter();
			PdfAction action = new PdfAction(url);
			PdfAnnotation link = PdfAnnotation.createLink(writer, position, PdfAnnotation.HIGHLIGHT_INVERT, action);
			writer.addAnnotation(link);
		}
	}

}