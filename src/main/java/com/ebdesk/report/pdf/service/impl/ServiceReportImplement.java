package com.ebdesk.report.pdf.service.impl;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ebdesk.report.pdf.dao.*;
import com.ebdesk.report.pdf.model.MessageModel;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ebdesk.report.pdf.config.ExternalConfig;
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

import javax.imageio.ImageIO;

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

	@Autowired
	private ManagementReport managementReport;

	@Autowired
	ServiceReport service;

	@Override
	public Map<String, Object> serviceReport(String start, String end, String criteria, String interval, String source,
			String media_tags, String elastic, String limit, String w_id, String w_app_logo, String id)
			throws FileNotFoundException, InvalidFormatException, IOException, ParseException {

		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, String> c = redis.keywordRedis(criteria);
		JSONObject status = new JSONObject();

		w_app_logo = cleanText(w_app_logo);

		if (!id.equals("")){
			status.put("status", "1");
			managementReport.updateStatus(id, status);
		}

		String workspace_logo = externalConfig.getPath_workspace_logo() + sql.pathLogoWorkspace(w_id);

		SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");

		ObjectMapper om = new ObjectMapper();

		List<Object> data_resume = es.resume(start, end, criteria, source, media_tags, elastic);
//		System.out.println("data resume : " + data_resume);
		List<Object> data_table = es.data(start, end, criteria, source, media_tags, elastic);
//		System.out.println("data data_table : " + data_table);
		List<Object> data_content = es.content(start, end, criteria, source, media_tags, elastic);
//		System.out.println("data data_content : " + data_content);

		String name_file = "";
		String output = "";

		if (!id.equals("")){
			status.put("status", "20");
			managementReport.updateStatus(id, status);
		}

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
//				document.add(space);

				File f = new File(workspace_logo);

				String default_logo = "default+logo.png";

				if (!w_app_logo.equals("default+logo.png")){
					try {
						URL url = new URL("http://"+externalConfig.getW_app_logo_url()+w_app_logo);

						final HttpURLConnection connection = (HttpURLConnection) url
								.openConnection();

						connection.setRequestProperty(
								"User-Agent",
								"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

						connection.setConnectTimeout(100);

						BufferedImage image = ImageIO.read(connection.getInputStream());

						default_logo = externalConfig.getW_app_logo()+w_app_logo;

						if (image != null){
							if (w_app_logo.contains("jpg")){
								ImageIO.write(image, "jpg", new File(default_logo));
							}else if (w_app_logo.contains("png")){
								ImageIO.write(image, "png", new File(default_logo));
							}else if (w_app_logo.contains(".gif")){
								ImageIO.write(image, "gif", new File(default_logo));
							}
							Image logoCover = Image.getInstance(default_logo);
							logoCover.scaleAbsolute(250, 150);
							logoCover.setAlignment(Element.ALIGN_CENTER);
							document.add(logoCover);
						}
					}catch (Exception ee){
						ee.printStackTrace();
					}
				}else {
					default_logo = externalConfig.getW_app_logo()+w_app_logo;
					Image logo = Image.getInstance(default_logo);
					logo.scaleAbsolute(250, 150);
					logo.setAlignment(Element.ALIGN_CENTER);
					document.add(logo);
				}


//				if (f.exists()) {
//					Image w_app_logo = Image.getInstance(workspace_logo);
//					w_app_logo.scaleAbsolute(180, 70);
//					w_app_logo.setAlignment(Element.ALIGN_CENTER);
//					document.add(w_app_logo);
//				} else {
//					Image w_app_logo = Image.getInstance(externalConfig.getPath_workspace_logo() + "ebdesk.png");
//					w_app_logo.scaleAbsolute(180, 70);
//					w_app_logo.setAlignment(Element.ALIGN_CENTER);
//					document.add(w_app_logo);
//				}

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

				if (!id.equals("")){
					status.put("status", "40");
					managementReport.updateStatus(id, status);
				}
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

				if (!id.equals("")){
					status.put("status", "50");
					managementReport.updateStatus(id, status);
				}

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
				cell7.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell7.setVerticalAlignment(Element.ALIGN_MIDDLE);

				table_contents.addCell(cell1);
				table_contents.addCell(cell2);
				table_contents.addCell(cell3);
				table_contents.addCell(cell4);
				table_contents.addCell(cell5);
				table_contents.addCell(cell6);
				table_contents.addCell(cell7);

				if (!id.equals("")){
					status.put("status", "70");
					managementReport.updateStatus(id, status);
				}

				int no = 1;
				for (Object obj : data_table) {
					@SuppressWarnings("unchecked")
					Map<String, String> data = om.readValue(obj.toString(), Map.class);

					table_contents.addCell(String.valueOf(no));
					table_contents.addCell(data.get("date"));
					if (data.containsKey("link")){
						PdfPCell cellLink = new PdfPCell(new Phrase(data.get("news_title")));
						cellLink.setCellEvent(new LinkInCell(cleanTextLink(data.get("link"))));
						table_contents.addCell(cellLink);
					}else {
						table_contents.addCell(data.get("news_title"));
					}

					table_contents.addCell(data.get("tone"));
					table_contents.addCell(data.get("media"));
					table_contents.addCell(data.get("influencers"));

					PdfPCell cellResume = new PdfPCell(new Paragraph(data.get("resume")));
					cellResume.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
					table_contents.addCell(cellResume);

					no++;
				}

				document.add(table_contents);

				if (!id.equals("")){
					status.put("status", "80");
					managementReport.updateStatus(id, status);
				}

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
					if (node.has("reporter")){
						if (!node.get("reporter").asText().equals("")){
							table_detail_contents.addCell(new Paragraph(node.get("reporter").asText()));
						}else {
							table_detail_contents.addCell(new Paragraph("_noname"));
						}
					}else {
						table_detail_contents.addCell(new Paragraph("_noname"));
					}
					document.add(table_detail_contents);

					String path_logo_news = externalConfig.getNews_logo() + node.get("media").asText().replace(" ", "+")
							+ ".jpg";

					if (!node.get("link_image").equals("") && !node.get("link_image").equals("null") && !node.get("link_image").asText().contains("(") && node.get("link_image") != null){
						if (node.get("link_image").asText().contains(".jpg")
								| node.get("link_image").asText().contains(".png")){
							try {
								URL url = new URL(node.get("link_image").asText().replaceAll("(?<=.jpg).*|(?<=.png).*", ""));

								final HttpURLConnection connection = (HttpURLConnection) url
										.openConnection();

								connection.setRequestProperty(
										"User-Agent",
										"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

								connection.setConnectTimeout(10);

								BufferedImage image = ImageIO.read(connection.getInputStream());

								String path_image = externalConfig.getOnline_image()
										+ node.get("title").asText().replace(" ", "_")
										.replace("?", "")
										.replaceAll("\\W","")+".jpg";

								if (image != null){
									if (w_app_logo.contains("jpg")){
										ImageIO.write(image, "jpg", new File(path_image));
									}else if (w_app_logo.contains("png")){
										ImageIO.write(image, "png", new File(path_image));
									}else if (w_app_logo.contains(".gif")){
										ImageIO.write(image, "gif", new File(path_image));
									}

									Image logo = Image.getInstance(path_image);
									logo.scaleAbsolute(200, 100);
									logo.setAlignment(Element.ALIGN_CENTER);
									document.add(logo);
								}else {
									Image logo = Image.getInstance(default_logo);
									logo.scaleAbsolute(70, 70);
									logo.setAlignment(Element.ALIGN_CENTER);
									document.add(logo);
								}
							}catch (Exception e){
//								e.printStackTrace();
								Image logo = Image.getInstance(default_logo);
								logo.scaleAbsolute(70, 70);
								logo.setAlignment(Element.ALIGN_CENTER);
								document.add(logo);
							}
						}else {
							Image logo = Image.getInstance(default_logo);
							logo.scaleAbsolute(70, 70);
							logo.setAlignment(Element.ALIGN_CENTER);
							document.add(logo);
						}
					}else {
						Image logo = Image.getInstance(default_logo);
						logo.scaleAbsolute(65, 65);
						logo.setAlignment(Element.ALIGN_CENTER);
						document.add(logo);
					}

//						File f_news = new File(path_logo_news);
//
//					if (f_news.exists()) {
//						Image logo = Image.getInstance(path_logo_news);
//						logo.scaleAbsolute(70, 70);
//						logo.setAlignment(Element.ALIGN_CENTER);
//						document.add(logo);
//					} else {
//						Image logo = Image.getInstance(externalConfig.getNews_logo() + "default+logo.png");
//						logo.scaleAbsolute(65, 65);
//						logo.setAlignment(Element.ALIGN_CENTER);
//						document.add(logo);
//					}

					document.add(new Paragraph(" "));

					if (node.has("content")) {
						Paragraph content = new Paragraph(node.get("content").asText());
						content.setAlignment(Element.ALIGN_JUSTIFIED);
						document.add(content);
					}
					
					document.newPage();

				}
				if (!id.equals("")){
					status.put("status", "90");
					managementReport.updateStatus(id, status);
				}

				output = externalConfig.getPath_save() + name_file;

				document.close();
				writer.close();
				if (!id.equals("")){
					status.put("status", "100");
					managementReport.updateStatus(id, status);
				}

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

//				if (f.exists()) {
//					Image w_app_logo = Image.getInstance(workspace_logo);
//					w_app_logo.scaleAbsolute(180, 70);
//					w_app_logo.setAlignment(Element.ALIGN_CENTER);
//					document.add(w_app_logo);
//				} else {
//					Image w_app_logo = Image.getInstance(externalConfig.getPath_workspace_logo() + "ebdesk.png");
//					w_app_logo.scaleAbsolute(180, 70);
//					w_app_logo.setAlignment(Element.ALIGN_CENTER);
//					document.add(w_app_logo);
//				}

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

				if (!id.equals("")){
					status.put("status", "40");
					managementReport.updateStatus(id, status);
				}

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

				if (!id.equals("")){
					status.put("status", "50");
					managementReport.updateStatus(id, status);
				}

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

				PdfPTable table_contents = new PdfPTable(8);
				table_contents.setWidthPercentage(109); // Width 100%
				table_contents.setSpacingBefore(10f); // Space before table
				table_contents.setSpacingAfter(10f); // Space after table

				// Set Column widths
				float[] columnContents = { 0.3f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 1.5f };
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
				PdfPCell cell4 = new PdfPCell(new Paragraph("Page", font_table));
				cell4.setBackgroundColor(BaseColor.LIGHT_GRAY);
				cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell4.setVerticalAlignment(Element.ALIGN_MIDDLE);
				PdfPCell cell5 = new PdfPCell(new Paragraph("Sentiment", font_table));
				cell5.setBackgroundColor(BaseColor.LIGHT_GRAY);
				cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);
				PdfPCell cell6 = new PdfPCell(new Paragraph("Media", font_table));
				cell6.setBackgroundColor(BaseColor.LIGHT_GRAY);
				cell6.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell6.setVerticalAlignment(Element.ALIGN_MIDDLE);
				PdfPCell cell7 = new PdfPCell(new Paragraph("Influencers", font_table));
				cell7.setBackgroundColor(BaseColor.LIGHT_GRAY);
				cell7.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell7.setVerticalAlignment(Element.ALIGN_MIDDLE);
				PdfPCell cell8 = new PdfPCell(new Paragraph("Resume", font_table));
				cell8.setBackgroundColor(BaseColor.LIGHT_GRAY);
				cell8.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell8.setVerticalAlignment(Element.ALIGN_MIDDLE);

				table_contents.addCell(cell1);
				table_contents.addCell(cell2);
				table_contents.addCell(cell3);
				table_contents.addCell(cell4);
				table_contents.addCell(cell5);
				table_contents.addCell(cell6);
				table_contents.addCell(cell7);
				table_contents.addCell(cell8);

				if (!id.equals("")){
					status.put("status", "70");
					managementReport.updateStatus(id, status);
				}

				int no = 1;
				for (Object obj : data_table) {
					@SuppressWarnings("unchecked")
					Map<String, String> data = om.readValue(obj.toString(), Map.class);

					table_contents.addCell(String.valueOf(no));
					table_contents.addCell(data.get("date"));
					if (data.containsKey("link")){
						PdfPCell cellLink = new PdfPCell(new Phrase(data.get("news_title")));
						cellLink.setCellEvent(new LinkInCell(data.get("link")));
						table_contents.addCell(cellLink);
					}else {
						table_contents.addCell(data.get("news_title"));
					}
					table_contents.addCell(data.get("page"));
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

				if (!id.equals("")){
					status.put("status", "80");
					managementReport.updateStatus(id, status);
				}

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

					List<String> list_image = download.downloadImageNew(node.get("link").asText(),
							node.get("media").asText(), externalConfig.getPrinted_image());

					PdfPTable table_detail_contents = new PdfPTable(4);
					table_detail_contents.setWidthPercentage(100); // Width 100%
					table_detail_contents.setSpacingBefore(10f); // Space before table
					table_detail_contents.setSpacingAfter(10f); // Space after table

					// Set Column widths
					float[] columnDetailContents = { 0.3f, 1f, 0.3f, 1f};
					table_detail_contents.setWidths(columnDetailContents);

					table_detail_contents.addCell(new Paragraph("Title"));

					if (node.has("title")) {
						PdfPCell cellTitleValue=new PdfPCell();
						cellTitleValue.setPhrase(new Paragraph(node.get("title").asText()));
						cellTitleValue.setColspan(3);//Merge Cells
						table_detail_contents.addCell(cellTitleValue);
					}

					table_detail_contents.addCell(new Paragraph("Media"));
					if (node.has("media")) {
						table_detail_contents.addCell(new Paragraph(node.get("media").asText()));
					}

					table_detail_contents.addCell(new Paragraph("Reporter"));
					table_detail_contents.addCell(new Paragraph(""));

					table_detail_contents.addCell(new Paragraph("Date"));
					if (node.has("date")) {
						table_detail_contents.addCell(node.get("date").asText());
					}

					table_detail_contents.addCell(new Paragraph("Tone"));
					if (node.has("tone")) {
						table_detail_contents.addCell(new Paragraph(node.get("tone").asText()));
					}

					table_detail_contents.addCell(new Paragraph("Page"));
					if (node.has("page")) {
						table_detail_contents.addCell(new Paragraph(node.get("page").asText()));
					}

					table_detail_contents.addCell(new Paragraph("PR Value"));
					table_detail_contents.addCell(new Paragraph(""));

					table_detail_contents.addCell(new Paragraph("Link"));
					if (node.has("link")) {

						PdfPCell cellLink = new PdfPCell(new Phrase(node.get("link").asText()));
						cellLink.setCellEvent(new LinkInCell(node.get("link").asText().replaceAll("imm.ebdesk.com", "ima.blackeye.id")));
						cellLink.setColspan(3);
						table_detail_contents.addCell(cellLink);
//						table_detail_contents.addCell(new Paragraph(node.get("link").asText()));
					}

					table_detail_contents.addCell(new Paragraph("Resume"));
					if (node.has("resume")) {
						PdfPCell cellResume = new PdfPCell(new Paragraph(node.get("resume").asText()));
						cellResume.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
						cellResume.setColspan(3);
						table_detail_contents.addCell(cellResume);
					}

					document.add(table_detail_contents);

					for (Object image : list_image) {
						Image images = Image.getInstance(image.toString());
						images.scaleToFit(500, 500);
						images.setAlignment(Element.ALIGN_CENTER);
						document.add(images);
					}

					document.newPage();
				}

				if (!id.equals("")){
					status.put("status", "90");
					managementReport.updateStatus(id, status);
				}

				output = externalConfig.getPath_save() + name_file;

				document.close();
				writer.close();

				if (!id.equals("")){
					status.put("status", "100");
					managementReport.updateStatus(id, status);
				}

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

	@Override
	public MessageModel generateReport(String criteria, String w_id, String category, String platform, String start, String end,
									   String interval, String w_app_logo) throws IOException {
		MessageModel msg = new MessageModel();

		Map<String, String> query_string = redis.keywordRedis(criteria);

		try {

			SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
			SimpleDateFormat file = new SimpleDateFormat("dd-MMM-yyyy");

			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			SimpleDateFormat formatter = new SimpleDateFormat("dd MMMMM yyyy");

			Date date_st = sdf.parse(start);
			Date date_ed = sdf.parse(end);

			String start_date = formatter.format(date_st);
			String end_date = formatter.format(date_ed);

			String filename = "";

			switch (platform.toLowerCase()){
				case "online" :
					filename = "report_online_" + query_string.get("criteria_name") + ".pdf";

					break;
				case "printed":
					filename = "report_cetak_" + query_string.get("criteria_name") + ".pdf";
					break;
			}

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("file_name", filename);
			jsonObject.put("status", "0");
			jsonObject.put("w_id", w_id);
			jsonObject.put("topic", query_string.get("criteria_name").toString().toLowerCase());
			jsonObject.put("category", category.toLowerCase());
			jsonObject.put("platform", platform.toLowerCase());
			jsonObject.put("start_date", start);
			jsonObject.put("end_date", end);
			jsonObject.put("created_at", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

			String id = managementReport.insertManagementReport(jsonObject);

			System.out.println("id : " + id);

			Thread newThread = new Thread(() -> {
				try {
					service.serviceReport(start, end, criteria, interval, "", "", platform, "", w_id, w_app_logo, id);
				} catch (Exception e) {
					JSONObject status = new JSONObject();
					status.put("status", "error");
					try {
						managementReport.updateStatus(id, status);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
				}
			});

			newThread.start();

			msg.setStatus(true);
			msg.setMessage("success");
			msg.setData(filename);

		} catch (Exception e) {

			msg.setStatus(false);
			msg.setMessage("data not available");
			e.printStackTrace();
		}

		return msg;
	}

	@Override
	public MessageModel getReport(String w_id, String topic, String platform){
		MessageModel msg = new MessageModel();

		try {
			msg.setData(managementReport.getReport(w_id, topic, platform));
			msg.setStatus(true);
			msg.setMessage("success");
		}catch (Exception e){
			msg.setStatus(false);
			msg.setMessage("failed");
		}

		return msg;
	}

	@SuppressWarnings({ "deprecation", "static-access" })
	@Override
	public MessageModel deleteById(String id) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		MessageModel msg = new MessageModel();
		try {
//		String param = "{\"query\": {\"bool\": {\"must\": [{\"match_phrase\": {\"w_id\": \""+w_id+"\"}}," +
//				"{\"match_phrase\": {\"file_name\": \""+file_name+"\"}},{\"match_phrase\": {\"category\": \""+category+"\"}}" +
//				",{\"match_phrase\": {\"platform\": \""+platform+"\"}}]}}}";
//		Client client1 = Client.create();
//
//		WebResource webResource = client1.resource("http://"+elasticConfig.getHost()+"/management-report/_search");
//		String input = param;
//
//		ClientResponse response = webResource.type("application/json").post(ClientResponse.class, input);
//		if (response.getStatus() != 200) {
//			throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
//		}
//
//		String output = response.getEntity(String.class);
//
//		ObjectNode dataElastic = new ObjectMapper().readValue(output, ObjectNode.class);
//		String hsl = "";
//		for (JsonNode node : dataElastic.get("hits").get("hits")) {
//			hsl = node.get("_id").asText();
//		}
//		System.out.println(hsl);
			msg.setData(managementReport.deleteById(id));
			msg.setStatus(true);
			msg.setMessage("success");
		}catch (Exception e){
			msg.setStatus(false);
			msg.setMessage("failed");
		}
		return msg;
	}

	public String cleanText(String text){
		text = text.replace("%20", " ")
				.replace( "%2B", "+")
				.replace("%22", "\"")
				.replace("%23", "#")
				.replace("%24", "$")
				.replace("%25", "%")
				.replace("%26", "&")
				.replace("%27", "'")
				.replace("%28", "(")
				.replace("%29", ")")
				.replace("%2A", "*")
				.replace("%2B", "+")
				.replace("%2C", ",")
				.replace("%2C", ",")
				.replace("%21", "!");
		return text;
	}

	public String cleanTextLink(String text){
		text = text.replace(" ", "%20")
				.replace( "+", "%2B")
				.replace("\"", "%22")
				.replace("#" , "%23")
				.replace("$","%24")
				.replace("%", "%25")
				.replace("&", "%26")
				.replace("'", "%27")
				.replace("(", "%28")
				.replace(")", "%29")
				.replace("*", "%2A")
				.replace("+", "%2B")
				.replace(",", "%2C")
				.replace(",", "%2C")
				.replace("<", "%3C")
				.replace(">", "%3E")
				.replace(">", "%3E")
				.replace("!", "%21");
		return text;
	}

}
