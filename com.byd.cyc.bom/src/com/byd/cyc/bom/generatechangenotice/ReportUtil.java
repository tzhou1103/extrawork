package com.byd.cyc.bom.generatechangenotice;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTInline;

import com.byd.cyc.bom.utils.CustomXWPFDocument;
import com.byd.cyc.bom.utils.FileUtil;

/**
 * @author zhoutong
 *
 */
public class ReportUtil 
{	
	/**
	 * ����ҳü
	 * 
	 * @param document
	 * @param dcnItemID
	 */
	public static void updateHeader(CustomXWPFDocument document, String dcnItemID) 
	{		
		List<XWPFHeader> headerList = document.getHeaderList();
		if (headerList != null && headerList.size() > 0) 
		{
			XWPFHeader header = headerList.get(0);
			List<XWPFTable> tables = header.getTables();
			if (tables != null && tables.size() > 0) 
			{
				XWPFTable table = tables.get(0);
				XWPFTableCell cell = table.getRow(0).getCell(3); // DCN�����д��ҳü���ĵ�4��
				if (cell != null) 
				{
					List<XWPFParagraph> paragraphs = cell.getParagraphs();
					if (paragraphs != null && paragraphs.size() > 0) 
					{
						XWPFParagraph paragraph = paragraphs.get(0);
						List<XWPFRun> runs = paragraph.getRuns();
						if (runs != null && runs.size() > 0) {
							for (int i = 0; i < runs.size() - 1; i++) { // ����һ��
								paragraph.removeRun(i);
							}
						}
						XWPFRun createRun = paragraph.createRun();
						createRun.setText(dcnItemID); // �������ı���ɾ��ǰ�汣�����ı�
						paragraph.removeRun(0);
					}
				}
			}
		}
	}
	
	/**
	 * ��ָ����Ԫ�����ͼƬ
	 * 
	 * @param cell
	 * @param picturePath
	 * @param document
	 * @param imageType
	 * @param width
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public static void insertPicture(XWPFTableCell cell, String picturePath, CustomXWPFDocument document, String imageType, int width) throws InvalidFormatException, IOException
	{
		XWPFParagraph paragraph = cell.getParagraphs().get(0);
		XWPFRun run = paragraph.createRun();
		run.addBreak();
		
		File pictureFile = new File(picturePath);
		FileInputStream inputStream = new FileInputStream(pictureFile);
		CTInline ctInline = run.getCTR().addNewDrawing().addNewInline();// ���ö�����
		String id = document.addPictureData(inputStream, getPictureType(imageType));// ���ͼƬ����
		int id2 = document.getAllPackagePictures().size() + 1;
		
		int[] imagePixel = FileUtil.getImagePixel(pictureFile);
		int imageWidth = imagePixel[0];
		int imageHeight = imagePixel[1];
		if (imageWidth > width) { // ����ͼƬ���أ����ݹ̶���ȵȱȼ���߶�
			imageWidth = width;
			float widthRatio = (float) width / imagePixel[0];
			imageHeight = Math.round(widthRatio * imageHeight);
		}
		
		document.createPic(id, id2, imageWidth, imageHeight, ctInline);// ���ͼƬ
		run.addBreak(BreakType.PAGE);
		inputStream.close();		
	}
	
	/**
	 * �����ļ���׺��ȡword��ͼƬ����
	 * <p> ֧��png��jpg��jpeg��bmp��gif��tiff�ȸ�ʽ��Ĭ��ʹ��png
	 * 
	 * @param imageType
	 * @return
	 */
	public static int getPictureType(String imageType)
	{
		switch (imageType) 
		{
		case "png":
			return XWPFDocument.PICTURE_TYPE_PNG;
		case "jpg":
			return XWPFDocument.PICTURE_TYPE_JPEG;
		case "jpeg":
			return XWPFDocument.PICTURE_TYPE_JPEG;
		case "bmp":
			return XWPFDocument.PICTURE_TYPE_BMP;
		case "gif":
			return XWPFDocument.PICTURE_TYPE_GIF;
		case "tiff":
			return XWPFDocument.PICTURE_TYPE_TIFF;
		default:
			return XWPFDocument.PICTURE_TYPE_PNG;
		}
	}
	
}
