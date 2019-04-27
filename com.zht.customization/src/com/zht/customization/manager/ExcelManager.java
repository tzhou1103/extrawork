package com.zht.customization.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.teamcenter.rac.kernel.TCComponentSavedVariantRule;
import com.zht.customization.impl.Model;
import com.zht.customization.listeners.OKListener;
import com.zht.customization.model.BOMNode;
import com.zht.customization.model.ECRModel;

public class ExcelManager {

	private Workbook workbook;
	private Sheet sheet;
	private CellStyle cellStyle;
	private CellStyle numCellStyle;
	DecimalFormat decimalFormat = new DecimalFormat("0000");
	DataFormat createDataFormat = null;
	int rowIndex = 3;

	public ExcelManager(File templateFile) throws IOException {
		InputStream is = new FileInputStream(templateFile);
		workbook = new XSSFWorkbook(is);
		sheet = workbook.getSheetAt(0);
		createDataFormat = workbook.createDataFormat();
	}

	public Cell getCell(int row, int col) {
		if (sheet == null) {
			System.out.println("sheet is null");
			return null;
		}
		Row row2 = sheet.getRow(row);
		if (row2 == null)
			return null;
		return row2.getCell(col);
	}

	public void saveExcel(String outputPath) {
		FileOutputStream fileOut;
		try {
			System.out.println("saving...");
			if (workbook != null) {
				fileOut = new FileOutputStream(outputPath);
				workbook.write(fileOut);
				fileOut.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void write2Excel() {
		
		Row row2 = sheet.getRow(rowIndex - 1);
		Cell cell = row2.getCell(0);
		cellStyle = cell.getCellStyle();
		Cell cell2 = row2.getCell(24);
		numCellStyle = cell2.getCellStyle();

		for (BOMNode modelObject : ModelManager.root.children) {
			// boolean isNullLine = true;
			// Map<String, Integer> quantityMap =
			// ModelManager.quantityList.get(rowIndex - 3);
			// for (TCComponentSavedVariantRule sos : OKListener.rules) {
			// Integer integer = quantityMap.get(sos.getName());
			// if (integer != 0) {
			// isNullLine = false;
			// break;
			// }
			// }

			// if(isNullLine){
			// rowIndex++;
			// continue;
			// }
			// int colIndex = 0;
			// Row row = sheet.createRow(rowIndex);
			// SetCellValue(row, colIndex, rowIndex - 2 + "", cellStyle);
			// int level = Integer.parseInt(modelObject.level);
			// level = level > 12 ? 12 : level;
			// colIndex += level + 1;
			// SetCellValue(row, colIndex, level + "", cellStyle);
			// colIndex = 14;
			// SetCellValue(row, colIndex, modelObject.partID, cellStyle);
			// colIndex++;
			// SetCellValue(row, colIndex, modelObject.partName, cellStyle);
			// colIndex++;
			// int tempColIndex = colIndex;
			// if (OKListener.rules.length == 0) {
			// String quantity = modelObject.quantity;
			// quantity = quantity.equals("0") ? "" : quantity;
			// SetCellValue(row, colIndex, quantity, cellStyle);
			// }
			// // Map<String, Integer> quantityMap =
			// // ModelManager.quantityList.get(rowIndex - 3);
			// for (TCComponentSavedVariantRule sos : OKListener.rules) {
			// Integer integer = modelObject.quantityMap.get(sos.getName());
			// if (integer == 0)
			// SetCellValue(row, colIndex, "", cellStyle);
			// else
			// SetCellValue(row, colIndex, integer + "", cellStyle);
			// colIndex++;
			// }
			//
			// colIndex = tempColIndex + 5;
			// SetCellValue(row, colIndex, modelObject.sequenceNo, cellStyle);
			// colIndex++;
			// SetCellValue(row, colIndex, modelObject.techCode, cellStyle);
			// colIndex++;
			// SetCellValue(row, colIndex, modelObject.material, cellStyle);
			// colIndex++;
			// String weight = modelObject.weight;
			// Integer weight_num = Integer.getInteger(weight);
			// weight_num = weight_num == null ? 0 : weight_num;
			// SetCellValue(row, colIndex, weight_num, cellStyle);
			// colIndex++;
			// SetCellValue(row, colIndex, modelObject.drawing, cellStyle);
			// colIndex++;
			// SetCellValue(row, colIndex, modelObject.model, cellStyle);
			// colIndex++;
			// SetCellValue(row, colIndex, modelObject.substitutes, cellStyle);
			// colIndex++;
			// SetCellValue(row, colIndex, modelObject.structureFeature,
			// cellStyle);
			// colIndex++;
			// SetCellValue(row, colIndex, modelObject.parent, cellStyle);
			// colIndex++;
			// SetCellValue(row, colIndex, modelObject.engineer, cellStyle);
			// colIndex++;
			// SetCellValue(row, colIndex, modelObject.depart, cellStyle);
			// colIndex++;
			// SetCellValue(row, colIndex, modelObject.desc, cellStyle);
			// rowIndex++;
			loopNode(modelObject);
		}
	}

	public void loopNode(BOMNode node) {
		int colIndex = 0;
		Row row = sheet.createRow(rowIndex);
		SetCellValue(row, colIndex, rowIndex - 2 + "", cellStyle);
		int level = Integer.parseInt(node.level);
		level = level > 12 ? 12 : level;
		colIndex += level + 1;
		SetCellValue(row, colIndex, level + "", cellStyle);
		colIndex = 14;
		SetCellValue(row, colIndex, node.partID, cellStyle);
		colIndex++;
		SetCellValue(row, colIndex, node.partName, cellStyle);
		colIndex++;
		int tempColIndex = colIndex;
		if (OKListener.rules.length == 0) {
			String quantity = node.quantity;
			quantity = quantity.equals("0") ? "" : quantity;
			SetCellValue(row, colIndex, quantity, cellStyle);
		}
		// Map<String, Integer> quantityMap =
		// ModelManager.quantityList.get(rowIndex - 3);
		int validIndex = 0;
		for (TCComponentSavedVariantRule sos : OKListener.rules) {
			Integer integer = node.quantityMap.get(sos.getName());
			if(integer==null){
				validIndex++;
				continue;
			}
			if (integer == 0)
				SetCellValue(row, colIndex+validIndex, "", cellStyle);
			else
				SetCellValue(row, colIndex+validIndex, integer + "", cellStyle);
			colIndex++;
		}
		if(validIndex!=OKListener.rules.length|| OKListener.rules.length==0){
			colIndex = tempColIndex + 5;
			SetCellValue(row, colIndex, node.sequenceNo, cellStyle);
			colIndex++;
			SetCellValue(row, colIndex, node.techCode, cellStyle);
			colIndex++;
			SetCellValue(row, colIndex, node.material, cellStyle);
			colIndex++;
			String weight = node.weight;
			if(weight.equals("")){
				SetCellValue(row, colIndex, 0, numCellStyle);
			}else{
				double weight_num = Double.parseDouble(weight);
//				weight_num = weight_num == null ? 0 : weight_num;
				SetCellValue(row, colIndex, weight_num, numCellStyle);
			}
			colIndex++;
			SetCellValue(row, colIndex, node.drawing, cellStyle);
			colIndex++;
			SetCellValue(row, colIndex, node.model, cellStyle);
			colIndex++;
			SetCellValue(row, colIndex, node.substitutes, cellStyle);
			colIndex++;
			SetCellValue(row, colIndex, node.structureFeature, cellStyle);
			colIndex++;
			SetCellValue(row, colIndex, node.parent, cellStyle);
			colIndex++;
			SetCellValue(row, colIndex, node.engineer, cellStyle);
			colIndex++;
			SetCellValue(row, colIndex, node.depart, cellStyle);
			colIndex++;
			SetCellValue(row, colIndex, node.desc, cellStyle);
			rowIndex++;
			for (BOMNode child : node.children) {
				loopNode(child);
			}
		}
	}

	public void writeSOSNames() {
		Row row = sheet.getRow(2);
		int columnNo = 16;
		Cell cell = row.getCell(16);
		CellStyle cellStyle2 = cell.getCellStyle();
		for (TCComponentSavedVariantRule rule : OKListener.rules) {
			SetCellValue(row, columnNo, rule.getName(), cellStyle2);
			columnNo++;
		}
	}

	public void writeECRData2Excel() {
		int rowIndex = 0;
		Row row2 = sheet.getRow(rowIndex);
		Cell cell = row2.getCell(0);
		cellStyle = cell.getCellStyle();
		for (Model modelObject : ModelManager.modelList) {
			rowIndex++;
			int colIndex = 0;
			ECRModel ecrModel = (ECRModel) modelObject;
			Row row = sheet.createRow(rowIndex);
			SetCellValue(row, colIndex, ecrModel.getParent(), cellStyle);
			colIndex++;
			int parseInt = Integer.parseInt(ecrModel.getLocation());
			SetCellValue(row, colIndex, decimalFormat.format(parseInt), cellStyle);
			colIndex++;
			SetCellValue(row, colIndex, ecrModel.getQuantity(), cellStyle);
			colIndex++;
			SetCellValue(row, colIndex, ecrModel.getStructureFeature(), cellStyle);
			colIndex++;
			SetCellValue(row, colIndex, ecrModel.getSubstitutes(), cellStyle);
			colIndex++;
			colIndex++;
			SetCellValue(row, colIndex, ecrModel.getChild(), cellStyle);
			colIndex++;
			SetCellValue(row, colIndex, ecrModel.getTechCode(), cellStyle);
			colIndex++;
			SetCellValue(row, colIndex, ecrModel.getChildName(), cellStyle);
			colIndex++;
			SetCellValue(row, colIndex, ecrModel.getUnit(), cellStyle);
			colIndex++;
			SetCellValue(row, colIndex, ecrModel.getMaterial(), cellStyle);
			colIndex++;
			SetCellValue(row, colIndex, ecrModel.getWeight(), cellStyle);
			colIndex++;
			colIndex++;
			SetCellValue(row, colIndex, ecrModel.getDesc(), cellStyle);
			colIndex++;
			SetCellValue(row, colIndex, ecrModel.getDrawing(), cellStyle);
			colIndex++;
			SetCellValue(row, colIndex, ecrModel.getModel(), cellStyle);
			colIndex++;
			SetCellValue(row, colIndex, ecrModel.getStructure(), cellStyle);
			colIndex++;
			SetCellValue(row, colIndex, ecrModel.getClassification(), cellStyle);
			colIndex++;
			SetCellValue(row, colIndex, ecrModel.getCarStatus(), cellStyle);
			colIndex++;
			SetCellValue(row, colIndex, ecrModel.getInventory(), cellStyle);
			colIndex++;
			SetCellValue(row, colIndex, ecrModel.getMaterialstatus(), cellStyle);
			colIndex++;
			SetCellValue(row, colIndex, ecrModel.getGroupNo(), cellStyle);
			colIndex++;
			SetCellValue(row, colIndex, ecrModel.getResp(), cellStyle);
			colIndex++;
			SetCellValue(row, colIndex, ecrModel.getEngineer(), cellStyle);
			colIndex++;
			SetCellValue(row, colIndex, ecrModel.getDepart(), cellStyle);
			colIndex++;
			SetCellValue(row, colIndex, ecrModel.getAdd(), cellStyle);
		}
	}

	private void SetCellValue(Row row, int colIndex, String value, CellStyle cellStyle) {
		value = value == null ? "" : value;
		Cell createCell = row.createCell(colIndex);
		cellStyle.setDataFormat(createDataFormat.getFormat("@"));
		createCell.setCellStyle(cellStyle);
		createCell.setCellValue(value);
	}

	private void SetCellValue(Row row, int colIndex, double value, CellStyle cellStyle) {
		Cell createCell = row.createCell(colIndex);
		cellStyle.setDataFormat(createDataFormat.getFormat("0.00"));
		createCell.setCellValue(value);
		createCell.setCellStyle(cellStyle);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ExcelManager excelManager = new ExcelManager(new File("E:\\1.xlsx"));
			Cell cell = excelManager.getCell(0, 0);
			if (cell == null) {
				Row row = excelManager.sheet.getRow(0);
				if (row == null)
					row = excelManager.sheet.createRow(0);
				cell = row.createCell(0);
			}
			CellStyle cellStyle2 = cell.getCellStyle();
			DataFormat createDataFormat = excelManager.workbook.createDataFormat();
			// cellStyle2.setDataFormat(createDataFormat.getFormat("0.00"));
			cell.setCellStyle(cellStyle2);
			cell.setCellType(cell.CELL_TYPE_NUMERIC);
			cell.setCellValue("195");
			excelManager.saveExcel("E:\\2.xlsx");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
