package com.hasco.ssdt.report.meopflowchart;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;
import java.util.Map.Entry;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

import com.hasco.ssdt.report.utils.ExcelUtil;
import com.hasco.ssdt.report.utils.FileUtility;
import com.hasco.ssdt.report.utils.TcUtil;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.*;
import com.teamcenter.rac.cme.sequence.pert.PertView;
import com.teamcenter.rac.kernel.*;
import com.teamcenter.rac.util.*;

public class ExportMeopFlowchartJob extends Job 
{
	/** 制造目标版本类型 */
	private static final String[] METARGET_REV_TYPES = { "H5_AssemblyRevision", "H5_UnitPartRevision", "H5_PartRevision" };
	
	/** 工序类型 */
	private static final String[] MEOPREV_TYPES = { "H5_BHSSAsmOpRevision", "H5_BHSSMachOpRevision", "H5_BHSSOpRevision" };
	
	private PertView pertView;
	
	private TCComponentBOMLine processBOMLine;
	private boolean inDataset = false;
	private String dirctoryPath;
	
	private boolean completed = false;
	private TCComponentDataset reportDataset;
	private File meopFlowchartFile;

	public ExportMeopFlowchartJob(String name, TCComponentBOMLine processBOMLine, PertView pertView, boolean inDataset, String dirctoryPath) {
		super(name);
		this.processBOMLine = processBOMLine;
		this.pertView = pertView;
		this.inDataset = inDataset;
		this.dirctoryPath = dirctoryPath;
	}

	@Override
	protected IStatus run(IProgressMonitor progressMonitor) 
	{
		String tempDirectory = null;
		
		try {
			progressMonitor.beginTask("正在导出工序流程图，请耐心等待...", -1);
			
			String adminUser = FileUtility.getValue("adminUser");
			if (adminUser == null || adminUser.isEmpty()) {
				adminUser = "infodba";
			}
			
			TCComponentDataset templateDataset = TcUtil.getTemplateDataset(adminUser, "工序流程图模板");
			if (templateDataset == null) {
				MessageBox.post("未找到数据集【工序流程图模板】，请联系管理员！", "错误", 1);
				return Status.CANCEL_STATUS;
			}
			
			File templateFile = TcUtil.getTemplateFile(templateDataset, this.dirctoryPath);
			if (templateFile == null || !templateFile.exists()) {
				MessageBox.post("文件【工序流程图模板.xlsx】不存在，请联系管理员！", "错误", 1);
				return Status.CANCEL_STATUS;
			}
			
			tempDirectory = templateFile.getParent();
			
			TCComponentItemRevision targetItemRev = null;
			TCComponentItemRevision processItemRev = this.processBOMLine.getItemRevision();
			TCComponent meTarget = this.processBOMLine.getItemRevision().getRelatedComponent("IMAN_METarget");
			if (meTarget != null && meTarget instanceof TCComponentItemRevision 
					&& Utilities.contains(meTarget.getType(), METARGET_REV_TYPES)) {
				targetItemRev = (TCComponentItemRevision) meTarget;
			} else {
				System.out.println(">>> 未找到符合条件的制造目标。");
			}
			
			// 导出PERT到图片
			String imageName = processItemRev.getProperty("item_id") + "_"
					+ processItemRev.getProperty("item_revision_id") + "_"
					+ processItemRev.getProperty("object_name") + "_PERT.jpg";
			final String imagePath = templateFile.getParent() + File.separator + imageName;
			AIFDesktop.getActiveDesktop().getShell().getDisplay().syncExec(new Runnable() 
			{				
				@Override
				public void run() 
				{
					GraphicalViewer localGraphicalViewer = (GraphicalViewer)ExportMeopFlowchartJob.this.pertView.getAdapter(GraphicalViewer.class);
					try {
						exportToImage(localGraphicalViewer, imagePath, 4);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			
			Map<String, TCComponentBOMLine> map = new HashMap<String, TCComponentBOMLine>();
			traverseBOM(this.processBOMLine, map);
			
			Vector<TCComponentBOMLine> vector = new Vector<TCComponentBOMLine>();
			Iterator<Entry<String, TCComponentBOMLine>> iterator = map.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, TCComponentBOMLine> entry = (Map.Entry<String, TCComponentBOMLine>) iterator.next();
				TCComponentBOMLine componentBOMLine = entry.getValue();
				vector.add(componentBOMLine);
			}
			
			Collections.sort(vector, new Comparator<TCComponentBOMLine>() 
			{
				@Override
				public int compare(TCComponentBOMLine o1, TCComponentBOMLine o2)
				{
					try {
						String sequenceNo1 = o1.getStringProperty("bl_sequence_no");
						String sequenceNo2 = o2.getStringProperty("bl_sequence_no");
						return sequenceNo1.hashCode() - sequenceNo2.hashCode();
					} catch (TCException e) {
						e.printStackTrace();
					}
					return 0;
				}
			});
			
			File meopFlowchartFile = generateMeopFlowchart(templateFile, targetItemRev, imagePath, vector);
			if (this.inDataset) {
				this.reportDataset = createOrUpdateDataset(meopFlowchartFile);
			} else {
				this.meopFlowchartFile = meopFlowchartFile;
			}
			
			this.completed = true;
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		} finally {
			if (this.inDataset) {
				FileUtility.deleteFolder(tempDirectory);
			}
			progressMonitor.done();
		}
		
		return Status.OK_STATUS;
	}
	
	
	/**
	 * 导出Pert图
	 * 
	 * @param paramGraphicalViewer
	 * @param paramString 图片路径
	 * @param paramInt 图片类型
	 * @throws Exception
	 */
	private void exportToImage(GraphicalViewer paramGraphicalViewer, String paramString, int paramInt) throws Exception 
	{
		FileOutputStream localFileOutputStream = null;
		try {
			IFigure localIFigure = ((AbstractGraphicalEditPart) paramGraphicalViewer.getRootEditPart()).getFigure();
			File localFile = new File(paramString);
			localFileOutputStream = new FileOutputStream(localFile);
			if ((localIFigure instanceof Viewport)) {
				((Viewport) localIFigure).setViewLocation(0, 0);
			}
			Dimension localDimension = localIFigure.getPreferredSize();
			Image localImage = new Image(Display.getDefault(), localDimension.width, localDimension.height);
			GC localGC = new GC(localImage);
			SWTGraphics localSWTGraphics = new SWTGraphics(localGC);
			localIFigure.paint(localSWTGraphics);
			ImageLoader localImageLoader = new ImageLoader();
			localImageLoader.data = new ImageData[] { localImage.getImageData() };
			localImageLoader.save(localFileOutputStream, paramInt);
			localFileOutputStream.close();
		} finally {
			if (localFileOutputStream != null) {
				localFileOutputStream.close();
			}
		}
	}
	
	/**
	 * 遍历工艺BOP，获取特定类型的工序
	 * 
	 * @param parentBOMLine
	 * @param map
	 * @throws TCException
	 */
	private void traverseBOM(TCComponentBOMLine parentBOMLine, Map<String, TCComponentBOMLine> map) throws TCException
	{
		if (parentBOMLine.hasChildren()) 
		{
			AIFComponentContext[] contexts = parentBOMLine.getChildren();
			if (contexts != null && contexts.length > 0) 
			{
				for (AIFComponentContext context : contexts) 
				{
					TCComponentBOMLine childBOMLine = (TCComponentBOMLine) context.getComponent();
					String itemID = childBOMLine.getStringProperty("bl_item_item_id");
					String itemRevType = childBOMLine.getItemRevision().getType();
					if (Utilities.contains(itemRevType, MEOPREV_TYPES) && !map.containsKey(itemID)) {
						map.put(itemID, childBOMLine);
					}
					
					traverseBOM(childBOMLine, map);
				}
			}
		}
	}
	
	private File generateMeopFlowchart(File templateFile, TCComponentItemRevision targetItemRev, String imagePath, Vector<TCComponentBOMLine> vector) throws Exception
	{
		ActiveXComponent excelApp = ExcelUtil.openExcelApp();
		Dispatch workBook = null;
		
		try {
			workBook = ExcelUtil.getWorkBook(excelApp, templateFile);
			Dispatch sheets = ExcelUtil.getSheets(workBook);
			Dispatch sheet = ExcelUtil.getSheet(sheets, Integer.valueOf(1));
			
			if (targetItemRev != null) {
				ExcelUtil.writeCellData(sheet, "D1", targetItemRev.getProperty("object_name"));
				ExcelUtil.writeCellData(sheet, "C3", targetItemRev.getProperty("item_id"));
			}
			
			if (new File(imagePath).exists()) 
			{
				String cellName = FileUtility.getValue("imageArea");
				if (cellName == null || cellName.isEmpty()) {
					cellName = "G8:I27";
				}
				ExcelUtil.insertPicture(sheet, cellName, imagePath);
			}
			
			int totalNum = vector.size();
			if (totalNum <= 20) {
				for (int i = 0; i < vector.size(); i++) 
				{
					TCComponentBOMLine meopBOMLine = vector.get(i);
					TCComponentItemRevision meopItemRev = meopBOMLine.getItemRevision();
					ExcelUtil.writeCellData(sheet, "A" + (i+8), meopItemRev.getProperty("h5_opid"));
					ExcelUtil.writeCellData(sheet, "B" + (i+8), meopItemRev.getProperty("object_name"));
				}
			} else {
				int pageNum = totalNum % 20 == 0 ? totalNum / 20 : totalNum / 20 + 1; // 总页数
				ExcelUtil.copySheetRows(sheet, 1, 30, pageNum, 31); // 拷贝出多页，每页之间空一行
				
				int index = 8;
				for (int j = 1; j <= totalNum; j++) 
				{
					TCComponentBOMLine meopBOMLine = vector.get(j - 1);
					TCComponentItemRevision meopItemRev = meopBOMLine.getItemRevision();
					ExcelUtil.writeCellData(sheet, "A" + (index + j - 1), meopItemRev.getProperty("h5_opid"));
					ExcelUtil.writeCellData(sheet, "B" + (index + j - 1), meopItemRev.getProperty("object_name"));
					if (j % 20 ==0) {
						index += 11;
					} 
				}
			}
		} finally {
			ExcelUtil.closeExcelApp(excelApp, workBook, true);
		}
		
		File resultFile = FileUtility.renameFile(templateFile.getParent(), templateFile, "工序流程图");
		return resultFile;
	}

	private TCComponentDataset createOrUpdateDataset(File meopFlowchartFile) throws TCException
	{
		TCComponentDataset meopFlowchartDataset = null;
		TCComponentItemRevision processItemRev = this.processBOMLine.getItemRevision();
		TCComponent relatedComponent = TcUtil.getRelatedComponent(processItemRev, "IMAN_specification", "MSExcelX", "工序流程图");
		if (relatedComponent != null) {
			meopFlowchartDataset = (TCComponentDataset) relatedComponent;
			TcUtil.removeFilesFromDataset(meopFlowchartDataset, "excel");
		} else {
			meopFlowchartDataset = TcUtil.createDataset("工序流程图", "", "MSExcelX");
			processItemRev.add("IMAN_specification", meopFlowchartDataset);
		}
		TcUtil.importFileToDataset(meopFlowchartDataset, meopFlowchartFile, "MSExcelX", "excel");
		
		return meopFlowchartDataset;
	}
	
	public boolean isCompleted() {
		return completed;
	}
	
	public TCComponentDataset getReportDataset() {
		return reportDataset;
	}

	public File getMeopFlowchartFile() {
		return meopFlowchartFile;
	}

	
}