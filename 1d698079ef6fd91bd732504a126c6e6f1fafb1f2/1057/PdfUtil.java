/*
 * Copyright (C) 2018 dy_only, Inc. All Rights Reserved.
 */
package com.fintech.scf.utils;

import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
/**
 * @author HealerJean
 * @version 1.0v
 * @Description
 * @ClassName pdf工具
 * @date
 */
public class PdfUtil {

	static final Charset UTF8 = Charset.forName("UTF-8");
	public static String cssFile = "itext/itextpdf.css";
	static final String  FONT = "itext/simsun.ttc" ;


	public static void parseHtml2Pdf(String htmlContext, File pdfFile) {
		try {
			parseHtml2Pdf(htmlContext,(new FileOutputStream(pdfFile)));
		} catch (Exception arg2) {
			throw new RuntimeException("pdf转换失败", arg2);
		}
	}

	public static void parseHtml2Pdf(String htmlContext, OutputStream outputStream) {
		ByteArrayInputStream input = null;
		try {
			input = new ByteArrayInputStream(htmlContext.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		parse2Pdf( input,  outputStream);
	}

	public static void parse2Pdf(String htmlFlePath, String pdfFilePath) {
		try {
			parse2Pdf( (new FileInputStream(htmlFlePath)),  (new FileOutputStream(pdfFilePath)));
		} catch (FileNotFoundException arg2) {
			throw new RuntimeException("pdf转换失败", arg2);
		}
	}




	public static void parse2Pdf(InputStream input, OutputStream outputStream) {
		try {
			Document document = new Document(PageSize.A4);
			PdfWriter writer = PdfWriter.getInstance(document, outputStream);
			document.open();
			InputStream cssIn = PdfUtil.MyFontsProvider.class.getClassLoader().getResourceAsStream(cssFile);
			XMLWorkerHelper.getInstance().parseXHtml(writer, document, input, cssIn, UTF8, new PdfUtil.MyFontsProvider(FONT));
			document.close();
		} catch (Exception arg4) {
			throw new RuntimeException("pdf转换失败", arg4);
		}
	}


	public static class MyFontsProvider extends XMLWorkerFontProvider {

		public MyFontsProvider(String ttc)  {
			super( null, null);
			String ttcp = PdfUtil.MyFontsProvider.class.getClassLoader().getResource(ttc).getPath();
			if (StringUtils.indexOf(ttcp, "jar!/") > 0) {
				ttcp = "jar:" + ttcp;
			}
			this.register(ttcp);
		}

		@Override
		public Font getFont(String fontname, String encoding, float size, int style) {
			String fntname = fontname;
			if (fontname == null) {
				fntname = "SimSun";
			}
			return super.getFont(fntname, encoding, size, style);
		}
	}

	public static void main(String[] args) {
		parse2Pdf("D:/pdf/html.html","D:/pdf/html.pdf");
	}


}
