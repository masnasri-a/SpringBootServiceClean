package com.ebdesk.report.pdf.dao;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class GenerateChart {

	@Autowired
	PdfElasticCriteriaId es;

	public Map<String, String> chartImage(String start, String end, String criteria, String interval, String source,
			String media_tags, String elastic, String limit, String path_image)
			throws JsonMappingException, JsonProcessingException, ParseException {
		Map<String, String> map_chart = new HashMap<String, String>();

		SimpleDateFormat sdf = new SimpleDateFormat("dd");
		SimpleDateFormat sdf_HH = new SimpleDateFormat("HH");
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		ObjectMapper om = new ObjectMapper();

		List<Object> bar_chart = es.dailyStatistic(start, end, criteria, interval, source, media_tags, elastic);
		List<Object> pie_chart = es.dailyStatisticPie(start, end, criteria, source, media_tags, elastic);
		List<Object> media_chart = es.mediaShare(start, end, criteria, source, media_tags, "10", elastic);
		List<Object> influencers_chart = es.influencers(start, end, criteria, source, media_tags, elastic, "5");

		String statistic_bar = path_image + "statistic_bar.jpeg";
		String statistic_pie = path_image + "statistic_sentiment_pie.jpeg";
		String media_share = path_image + "media_share.jpeg";
		String influencer = path_image + "influencers.jpeg";

		// Create Bar chart
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (Object object : bar_chart) {
			@SuppressWarnings("unchecked")
			Map<String, Integer> map = om.readValue(object.toString(), Map.class);
			for (Map.Entry<String, Integer> entry : map.entrySet()) {
				String key = entry.getKey();
				Integer value = entry.getValue();
				if (interval.equals("hour")) {
					dataset.addValue(value, "Interval", sdf_HH.format(format.parse(key)));
				} else {
					dataset.addValue(value, "Interval", sdf.format(format.parse(key)));
				}
			}
		}

		// Create Pie chart
		DefaultPieDataset datasetPie = new DefaultPieDataset();
		for (Object object : pie_chart) {
			@SuppressWarnings("unchecked")
			Map<String, Integer> map = om.readValue(object.toString(), Map.class);

			if (map.containsKey("negative")) {
				datasetPie.setValue("negative", map.get("negative"));

			}

			if (map.containsKey("neutral")) {
				datasetPie.setValue("neutral", map.get("neutral"));

			}

			if (map.containsKey("positive")) {
				datasetPie.setValue("positive", map.get("positive"));

			}

		}

		// Create Bar chart
		DefaultCategoryDataset datasetMediaShare = new DefaultCategoryDataset();
		for (Object object : media_chart) {
			@SuppressWarnings("unchecked")
			Map<String, Integer> map = om.readValue(object.toString(), Map.class);
			for (Map.Entry<String, Integer> entry : map.entrySet()) {
				String key = entry.getKey();
				Integer value = entry.getValue();
				datasetMediaShare.addValue(value, "media", key);
			}
		}

		// Create Bar chart
		DefaultCategoryDataset datasetInfluencer = new DefaultCategoryDataset();
		for (Object object : influencers_chart) {
			@SuppressWarnings("unchecked")
			Map<String, Integer> map = om.readValue(object.toString(), Map.class);
			for (Map.Entry<String, Integer> entry : map.entrySet()) {
				String key = entry.getKey();
				Integer value = entry.getValue();
				datasetInfluencer.addValue(value, "influencer", key);
			}
		}

		// pie chart
		JFreeChart pieChart3d = ChartFactory.createPieChart("Sentiment Analysis", datasetPie);
		pieChart3d.setBackgroundPaint(Color.WHITE);
		PiePlot plot = (PiePlot) pieChart3d.getPlot();
		plot.setSectionPaint("positive", Color.BLUE);
		plot.setSectionPaint("negative", Color.RED);
		plot.setSectionPaint("neutral", Color.WHITE);

		PieSectionLabelGenerator labelGenerator = new StandardPieSectionLabelGenerator("{0} : ({2})",
				new DecimalFormat("0"), new DecimalFormat("0%"));
		((PiePlot) pieChart3d.getPlot()).setLabelGenerator(labelGenerator);

		// bar chart statistic
		JFreeChart chart = ChartFactory.createBarChart("Daily Statistic", "" + interval, "", dataset, PlotOrientation.VERTICAL, false,
				true, false);
		chart.setBackgroundPaint(Color.WHITE);

		// bar chart media share
		JFreeChart chartMedia = ChartFactory.createBarChart("Media Share", "", "", datasetMediaShare,
				PlotOrientation.HORIZONTAL, false, true, false);
		chartMedia.setBackgroundPaint(Color.WHITE);

		// bar chart influencer
		JFreeChart chartInfluencer = ChartFactory.createBarChart("Influencers", "", "", datasetInfluencer,
				PlotOrientation.HORIZONTAL, false, true, false);
		chartInfluencer.setBackgroundPaint(Color.WHITE);

		try {
			ChartUtilities.saveChartAsJPEG(new File(statistic_bar), chart, 700, 300);
			ChartUtilities.saveChartAsJPEG(new File(statistic_pie), pieChart3d, 700, 300);
			ChartUtilities.saveChartAsJPEG(new File(media_share), chartMedia, 700, 300);
			ChartUtilities.saveChartAsJPEG(new File(influencer), chartInfluencer, 700, 300);
		} catch (IOException e) {
			System.err.println("Problem occurred creating chart.");
		}

		map_chart.put("statistic_pie", statistic_pie);
		map_chart.put("statistic_bar", statistic_bar);
		map_chart.put("media_share", media_share);
		map_chart.put("influencer", influencer);

		return map_chart;
	}

}
