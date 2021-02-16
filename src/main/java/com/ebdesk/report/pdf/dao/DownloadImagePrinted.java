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
		List<String> link_list = new ArrayList<String>(Arrays.asList(link.split(";")));

		for (String imageUrl : link_list) {
			URL url = new URL(imageUrl.replace("ima.blackeye.id", "imm.ebdesk.com"));
			String fileName = url.getFile();
			String destName = path_image_printed + media + "_" + fileName.replace("/", "");

			InputStream is = url.openStream();
			OutputStream os = new FileOutputStream(destName);

			byte[] b = new byte[2048];
			int length;

			while ((length = is.read(b)) != -1) {
				os.write(b, 0, length);
			}

			is.close();
			os.close();

			listImage.add(destName);
		}

		return listImage;
	}
}
