package com.zht.report.jobs;

import java.io.File;
import java.text.MessageFormat;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.zht.report.datamodels.ProductCategoryInfo;
import com.zht.report.dialogs.ZHTConstants;
import com.zht.report.utils.JacobUtil;
import com.zht.report.utils.ReportUtil;
import com.zht.report.utils.TcUtil;

public class GenerateProductCategoryJob extends Job
{
	private boolean completed = false;
	
	private TCComponentBOMLine targetBOMLine;
	private String filePath;
	
	public GenerateProductCategoryJob(String name, TCComponentBOMLine paramBOMLine, String paramFilePath) {
		super(name);
		this.targetBOMLine = paramBOMLine;
		this.filePath = paramFilePath;
	}

	@Override
	protected IStatus run(IProgressMonitor progressMonitor) 
	{
		progressMonitor.beginTask(ZHTConstants.JOB_BEGINTASK_MSG, -1); 
		
		try {
			Vector<ProductCategoryInfo> infoVector = new Vector<ProductCategoryInfo>();
			
			TCComponentBOMWindow bomWindow = this.targetBOMLine.window();
			bomWindow.clearCache();
//			bomWindow.unlock();
//			bomWindow.refresh();
			
			AIFComponentContext[] contexts = this.targetBOMLine.getChildren();
			for (AIFComponentContext context : contexts) 
			{
				TCComponentBOMLine childBOMLine = (TCComponentBOMLine) context.getComponent();
				ProductCategoryInfo categoryInfo = getProductCategoryInfo(childBOMLine);
				infoVector.add(categoryInfo);
			}
			
			generateCategory(infoVector);
			this.completed = true;
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		} finally {
			progressMonitor.done();
		}
		return Status.OK_STATUS;
	}
	
	private ProductCategoryInfo getProductCategoryInfo(TCComponentBOMLine paramBOMLine) throws Exception
	{
		TCComponentItemRevision itemRevision = paramBOMLine.getItemRevision();
		String drawingNo = ReportUtil.getDrawingNo(itemRevision);
		
		String itemRevType = itemRevision.getType();
		
		TCComponentUser user = (TCComponentUser) itemRevision.getTCProperty("owning_user").getReferenceValue();
		TCComponentGroup group = (TCComponentGroup) itemRevision.getTCProperty("owning_group").getReferenceValue();
		
		ProductCategoryInfo categoryInfo = new ProductCategoryInfo();
		categoryInfo.setAssemblyId(this.targetBOMLine.getItem().getProperty("item_id"));
		categoryInfo.setFindNo(getFindNumber(paramBOMLine));
		categoryInfo.setQuantity(TcUtil.getTotalQuantity(paramBOMLine) + "");
		categoryInfo.setStructureFeature(paramBOMLine.getProperty("Z9_Structure_Feature"));
		categoryInfo.setReplacePartId(ReportUtil.getReplacePartId(paramBOMLine));
		
		// 2018-11-06优化属性取值，减少访问数据库次数
		String[] propNames = { "item_id", "z9_IR_Techcode", "object_name", "z9_IR_Unit", "z9_IR_Material",
				"z9_IR_Weight", "z9_IR_Materialstatus", "object_desc", "z9_IR_Group" };
		String[] propValues = itemRevision.getProperties(propNames);
		if (propValues != null && propValues.length == 9) {
			categoryInfo.setPartId(propValues[0]);
			categoryInfo.setTechCode(propValues[1]);
			categoryInfo.setPartName(propValues[2]);
			categoryInfo.setUnit(propValues[3]);
			categoryInfo.setMaterial(propValues[4]);
			categoryInfo.setWeight(propValues[5]);
			categoryInfo.setMaterialstatus(propValues[6]);
			categoryInfo.setRemark(propValues[7]);
			categoryInfo.setGroupNo(propValues[8]);
		}
		
//		categoryInfo.setPartId(itemRevision.getProperty("item_id"));
//		categoryInfo.setTechCode(itemRevision.getProperty("z9_IR_Techcode"));
//		categoryInfo.setPartName(itemRevision.getProperty("object_name"));
//		categoryInfo.setUnit(itemRevision.getProperty("z9_IR_Unit"));
//		categoryInfo.setMaterial(itemRevision.getProperty("z9_IR_Material"));
//		categoryInfo.setWeight(itemRevision.getProperty("z9_IR_Weight"));
//		categoryInfo.setMaterialstatus(itemRevision.getProperty("z9_IR_Materialstatus"));			
//		categoryInfo.setRemark(itemRevision.getProperty("object_desc"));
		categoryInfo.setDrawingNo(drawingNo);
		categoryInfo.setModelName(ReportUtil.getModelName(itemRevision));
		categoryInfo.setCarStructure(ReportUtil.getCarStructure(paramBOMLine, itemRevType));
		categoryInfo.setCarType(ReportUtil.getCarType(group));
//		categoryInfo.setGroupNo(itemRevision.getProperty("z9_IR_Group"));
		categoryInfo.setDesignResponsible(user.getTCProperty("person").getReferenceValue().getProperty("PA6"));
		categoryInfo.setUserName(user.getProperty("person"));
		categoryInfo.setGroupDesc(group.getProperty("description"));
		return categoryInfo;
	}
	
	private String getFindNumber(TCComponentBOMLine paramBOMLine) throws TCException
	{
		String findNumber = paramBOMLine.getProperty("bl_sequence_no");
		int length = findNumber.length();
		if (length < 4) 
		{
			for (int i = 0; i < 4 - length; i++) {
				findNumber = "0" + findNumber;
			}
		}
		return findNumber;
	}
	
	
	private void generateCategory(Vector<ProductCategoryInfo> infoVector) throws Exception
	{
		TCComponentDataset templateDataset = TcUtil.findTemplateDataset("MSExcelX", ZHTConstants.PRODUCTCATEGORY_DSNAME);
		if (templateDataset == null) {
			throw new Exception(MessageFormat.format(ZHTConstants.DATASETNOTFOUND_MSG, ZHTConstants.PRODUCTCATEGORY_DSNAME));
		}
		
		TCComponentTcFile[] tcFiles = templateDataset.getTcFiles();
		if (tcFiles == null || tcFiles.length < 1) {
			throw new Exception(MessageFormat.format(ZHTConstants.NOFMSFILE_MSG, ZHTConstants.PRODUCTCATEGORY_DSNAME));
		}
		File templateFile = tcFiles[0].getFile(System.getenv("Temp"));
		
		ActiveXComponent excelApp = JacobUtil.getExcelApp();
		Dispatch workBook = null;
		
		try {
			workBook = JacobUtil.getWorkBook(excelApp, templateFile);
			Dispatch sheets = JacobUtil.getSheets(workBook);
			Dispatch firstSheet = JacobUtil.getSheet(sheets, Integer.valueOf(1));
			for (int i = 0; i < infoVector.size(); i++) 
			{
				ProductCategoryInfo categoryInfo = infoVector.get(i);
				JacobUtil.writeCellData(firstSheet, "A" + (i+2), categoryInfo.assemblyId);
				JacobUtil.writeCellData(firstSheet, "B" + (i+2), categoryInfo.findNo);
				JacobUtil.writeCellData(firstSheet, "C" + (i+2), categoryInfo.quantity);
				JacobUtil.writeCellData(firstSheet, "D" + (i+2), categoryInfo.structureFeature);
				JacobUtil.writeCellData(firstSheet, "E" + (i+2), categoryInfo.replacePartId);
				JacobUtil.writeCellData(firstSheet, "F" + (i+2), categoryInfo.partId);
				JacobUtil.writeCellData(firstSheet, "G" + (i+2), categoryInfo.techCode);
				JacobUtil.writeCellData(firstSheet, "H" + (i+2), categoryInfo.partName);
				JacobUtil.writeCellData(firstSheet, "I" + (i+2), categoryInfo.unit);
				JacobUtil.writeCellData(firstSheet, "J" + (i+2), categoryInfo.material);
				JacobUtil.writeCellData(firstSheet, "K" + (i+2), categoryInfo.weight);
				JacobUtil.writeCellData(firstSheet, "L" + (i+2), categoryInfo.materialstatus);
				JacobUtil.writeCellData(firstSheet, "M" + (i+2), categoryInfo.remark);
				JacobUtil.writeCellData(firstSheet, "N" + (i+2), categoryInfo.drawingNo);
				JacobUtil.writeCellData(firstSheet, "O" + (i+2), categoryInfo.modelName);
				JacobUtil.writeCellData(firstSheet, "P" + (i+2), categoryInfo.carStructure);
				JacobUtil.writeCellData(firstSheet, "Q" + (i+2), categoryInfo.carType);
				JacobUtil.writeCellData(firstSheet, "R" + (i+2), categoryInfo.groupNo);
				JacobUtil.writeCellData(firstSheet, "S" + (i+2), categoryInfo.designResponsible);
				JacobUtil.writeCellData(firstSheet, "T" + (i+2), categoryInfo.userName);
				JacobUtil.writeCellData(firstSheet, "U" + (i+2), categoryInfo.groupDesc);
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		} finally {
			JacobUtil.closeExcelApp(excelApp, workBook);
			
			File file = new File(this.filePath);
			if (file.exists()) {
				file.delete();
			}
			templateFile.renameTo(file);
		}
	}
	
	public boolean isCompleted() {
		return completed;
	}
	
}