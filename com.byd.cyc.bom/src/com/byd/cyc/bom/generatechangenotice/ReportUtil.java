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
	 * 更新页眉
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
				XWPFTableCell cell = table.getRow(0).getCell(3); // DCN编号填写到页眉表格的第4列
				if (cell != null) 
				{
					List<XWPFParagraph> paragraphs = cell.getParagraphs();
					if (paragraphs != null && paragraphs.size() > 0) 
					{
						XWPFParagraph paragraph = paragraphs.get(0);
						List<XWPFRun> runs = paragraph.getRuns();
						if (runs != null && runs.size() > 0) {
							for (int i = 0; i < runs.size() - 1; i++) { // 保留一个
								paragraph.removeRun(i);
							}
						}
						XWPFRun createRun = paragraph.createRun();
						createRun.setText(dcnItemID); // 插入新文本，删除前面保留的文本
						paragraph.removeRun(0);
					}
				}
			}
		}
	}
	
	/**
	 * 在指定单元格插入图片
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
		CTInline ctInline = run.getCTR().addNewDrawing().addNewInline();// 设置段落行
		String id = document.addPictureData(inputStream, getPictureType(imageType));// 添加图片数据
		int id2 = document.getAllPackagePictures().size() + 1;
		
		int[] imagePixel = FileUtil.getImagePixel(pictureFile);
		int imageWidth = imagePixel[0];
		int imageHeight = imagePixel[1];
		if (imageWidth > width) { // 调整图片像素，根据固定宽度等比计算高度
			imageWidth = width;
			float widthRatio = (float) width / imagePixel[0];
			imageHeight = Math.round(widthRatio * imageHeight);
		}
		
		document.createPic(id, id2, imageWidth, imageHeight, ctInline);// 添加图片
		run.addBreak(BreakType.PAGE);
		inputStream.close();		
	}
	
	/**
	 * 根据文件后缀获取word中图片类型
	 * <p> 支持png、jpg、jpeg、bmp、gif、tiff等格式，默认使用png
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
