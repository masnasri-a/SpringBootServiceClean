package com.ebdesk.report.pdf.dao;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Repository;

import javax.imageio.ImageIO;

@Repository
public class DownloadImagePrinted {

	public List<String> downloadImage(String link, String media, String path_image_printed) throws IOException {

		List<String> listImage = new ArrayList<String>();
		try {
			for (String linkURL:link.replaceAll("jpg;htt", "jpg|htt").split("\\|")) {
				URL url = new URL(linkURL.replace("ima.blackeye.id", "192.168.24.176:9300"));

				final HttpURLConnection connection = (HttpURLConnection) url
						.openConnection();

				connection.setRequestProperty(
						"User-Agent",
						"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

				BufferedImage image = ImageIO.read(connection.getInputStream());
				String fileName = url.getFile();
				String destName = path_image_printed + fileName.replace("/", "");

				if (image != null){
					ImageIO.write(image, "jpg", new File(destName));
					listImage.add(destName);
				}
			}
		}catch (Exception e){

		}

		return listImage;
	}

	public List<String> downloadImageNew(String link, String media, String path_image_printed) throws IOException {
		List<String> destNames = new ArrayList<>();
		for (String linkURL:link.replace(";%20", "|%20").split(";")) {
			URL url = new URL(linkURL.replace("|%20", ";%20").replace("ima.blackeye.id", "192.168.24.176:9300"));

			final HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();

			connection.setRequestProperty(
					"User-Agent",
					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

			BufferedImage image = ImageIO.read(connection.getInputStream());
			String fileName = url.getFile();
			String destName = path_image_printed + fileName.replace("/", "");

			if (image != null){
				ImageIO.write(image, "jpg", new File(destName));
				destNames.add(destName);
			}
		}
		return destNames;
	}
}
