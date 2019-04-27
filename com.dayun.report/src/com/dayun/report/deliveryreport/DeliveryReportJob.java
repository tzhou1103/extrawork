package com.dayun.report.deliveryreport;

import com.dayun.report.utils.Constants;
import com.dayun.report.utils.ExcelUtil;
import com.dayun.report.utils.TcUtil;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentScheduleTask;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class DeliveryReportJob extends Job 
{
	private TCComponentScheduleTask scheduleTask;
	private Vector<TCComponentItemRevision> itemRevVector = new Vector<TCComponentItemRevision>();
	private Map<TCComponentItemRevision, TCComponentScheduleTask> itemRevToTaskMap = new HashMap<TCComponentItemRevision, TCComponentScheduleTask>();
	private Map<TCComponentScheduleTask, TCComponentScheduleTask> taskToPlanMap = new HashMap<TCComponentScheduleTask, TCComponentScheduleTask>();
	private LinkedHashMap<String, Vector<Delivery>> groupToDeliveryMap = new LinkedHashMap<String, Vector<Delivery>>();
	private Vector<String> groupNameVector = new Vector<String>();
	private boolean completed = false;

	public DeliveryReportJob(String name,
			TCComponentScheduleTask paramScheduleTask) {
		super(name);
		this.scheduleTask = paramScheduleTask;
	}

	protected IStatus run(IProgressMonitor progressMonitor) {
		String tempDirectory = null;
		try {
			progressMonitor.beginTask("正在导出项目交付物情况报表，请耐心等待...", -1);

			tempDirectory = TcUtil.getTempPath() + System.currentTimeMillis();
			File workingDir = new File(tempDirectory);
			if (!workingDir.exists()) {
				workingDir.mkdirs();
			}
			String preferenceName = "dy_deliverable_report_template";
			File templateFile = TcUtil.getTemplateFile(preferenceName,
					tempDirectory);

			traverseScheduleTask(this.scheduleTask);
			if (this.itemRevVector.size() > 0) {
				File reportFile = outputReportFile(templateFile);

				String scheduleName = this.scheduleTask
						.getProperty("object_name");
				String datasetName = scheduleName + "项目交付物情况报表"
						+ TcUtil.getTimeStamp("yyyyMMddHHmmss");
				TCComponentDataset dataset = TcUtil.createDataset(datasetName,
						"", "MSExcelX");
				TcUtil.importFileToDataset(dataset, reportFile, "MSExcelX",
						"excel");
				TCComponentFolder newStuffFolder = dataset.getSession()
						.getUser().getNewStuffFolder();
				newStuffFolder.add("contents", dataset);
			} else {
				MessageBox.post("没有可导出的项目交付物信息！", "提示", 2);
				IStatus localIStatus = Status.CANCEL_STATUS;
				return localIStatus;
			}
			this.completed = true;
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		} finally {
			TcUtil.deleteFolder(tempDirectory);
			progressMonitor.done();
		}
		return Status.OK_STATUS;
	}

	private void traverseScheduleTask(TCComponentScheduleTask scheduleTask)
			throws TCException {
		AIFComponentContext[] contexts = scheduleTask.getChildren();
		if ((contexts != null) && (contexts.length > 0)) {
			for (AIFComponentContext context : contexts) {
				if ((context.getComponent() instanceof TCComponentScheduleTask)) {
					TCComponentScheduleTask childScheduleTask = (TCComponentScheduleTask) context
							.getComponent();
					String taskType = childScheduleTask.getType();
					if (taskType.equals("E9_TY_WorkTask")) {
						Vector<TCComponentItemRevision> itemRevVector = getTaskDeliveryList(childScheduleTask);
						if (itemRevVector.size() > 0) {
							for (TCComponentItemRevision itemRev : itemRevVector) {
								this.itemRevToTaskMap.put(itemRev,
										childScheduleTask);
							}
							String parentTaskType = scheduleTask.getType();
							if (parentTaskType.equals("E9_TY_MasterPlan")) {
								this.taskToPlanMap.put(childScheduleTask,
										scheduleTask);
							}
							this.itemRevVector.addAll(itemRevVector);
						}
					}
					traverseScheduleTask(childScheduleTask);
				}
			}
		}
	}

	private Vector<TCComponentItemRevision> getTaskDeliveryList(
			TCComponentScheduleTask workTask) throws TCException {
		Vector<TCComponentItemRevision> vector = new Vector<TCComponentItemRevision>();
		TCComponent[] referenceComponents = workTask.getTCProperty(
				"sch_task_deliverable_list").getReferenceValueArray();
		if ((referenceComponents != null) && (referenceComponents.length > 0)) {
			for (TCComponent tcComponent : referenceComponents) {
				TCComponent deliverableInstance = tcComponent
						.getReferenceProperty("fnd0DeliverableInstance");
				if (deliverableInstance.isTypeOf(Constants.DELIVERY_TYPES)) {
					TCComponentItem item = (TCComponentItem) deliverableInstance;
					vector.add(item.getLatestItemRevision());
				} else if (deliverableInstance
						.isTypeOf(Constants.DELIVERY_REV_TYPES)) {
					TCComponentItemRevision itemRev = (TCComponentItemRevision) deliverableInstance;
					vector.add(itemRev);
				}
			}
		}
		return vector;
	}

	private Vector<Delivery> getDeliveryVector() {
		Vector<Delivery> vector = new Vector<Delivery>();
		for (TCComponentItemRevision itemRev : this.itemRevVector) {
			TCComponentScheduleTask workTask = (TCComponentScheduleTask) this.itemRevToTaskMap
					.get(itemRev);
			TCComponentScheduleTask masterPlan = (TCComponentScheduleTask) this.taskToPlanMap
					.get(workTask);
			Delivery delivery = new Delivery(itemRev, workTask, masterPlan);
			vector.add(delivery);

			String groupName = delivery.responsibleDepartment;
			Vector<Delivery> deliveryVector = this.groupToDeliveryMap.get(groupName);
			if (deliveryVector == null) {
				deliveryVector = new Vector<Delivery>();
			}
			deliveryVector.add(delivery);
			this.groupToDeliveryMap.put(groupName, deliveryVector);
		}
		return vector;
	}

	private Vector<DeliveryStatistics> getDeliveryStatisticsVector() {
		Vector<DeliveryStatistics> deliveryStatisticVector = new Vector<DeliveryStatistics>();

		int taskTotalCount = 0;
		int onScheduleCount = 0;
		int delayedCount = 0;
		int deferredCount = 0;
		int unexpiredCount = 0;

		Iterator<Map.Entry<String, Vector<Delivery>>> iterator = this.groupToDeliveryMap
				.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Vector<Delivery>> entry = (Map.Entry<String, Vector<Delivery>>) iterator.next();
			String groupName = (String) entry.getKey();
			Vector<Delivery> deliveryVector = entry.getValue();
			DeliveryStatistics deliveryStatistics = new DeliveryStatistics(
					groupName, deliveryVector);
			deliveryStatisticVector.add(deliveryStatistics);

			taskTotalCount += deliveryStatistics.taskTotalCount;
			onScheduleCount += deliveryStatistics.onScheduleCount;
			delayedCount += deliveryStatistics.delayedCount;
			deferredCount += deliveryStatistics.deferredCount;
			unexpiredCount += deliveryStatistics.unexpiredCount;

			this.groupNameVector.add(groupName);
		}
		DeliveryStatistics projcetTaskStatistics = new DeliveryStatistics("总计",
				taskTotalCount, onScheduleCount, delayedCount, deferredCount,
				unexpiredCount);
		deliveryStatisticVector.add(projcetTaskStatistics);

		return deliveryStatisticVector;
	}

	private File outputReportFile(File templateFile) throws Exception {
		Vector<Delivery> deliveryVector = getDeliveryVector();
		Vector<DeliveryStatistics> deliveryStatisticsVector = getDeliveryStatisticsVector();

		ActiveXComponent excelApp = ExcelUtil.openExcelApp();
		Dispatch workBook = null;
		try {
			workBook = ExcelUtil.getWorkBook(excelApp, templateFile);
			Dispatch sheets = ExcelUtil.getSheets(workBook);

			Dispatch deliverySheet = ExcelUtil.getSheet(sheets,
					Integer.valueOf(1));
			int deliveryNum = deliveryVector.size();
			if (deliveryNum > 7) {
				int minus = deliveryNum - 7;
				ExcelUtil.copyRow(deliverySheet, 4, minus);
			}
			for (int i = 0; i < deliveryNum; i++) {
				Delivery delivery = (Delivery) deliveryVector.get(i);

				ExcelUtil.writeCellData(deliverySheet, "A" + (4 + i),
						Integer.valueOf(i + 1));
				ExcelUtil.writeCellData(deliverySheet, "B" + (4 + i),
						delivery.deliveryName);
				ExcelUtil.writeCellData(deliverySheet, "C" + (4 + i),
						delivery.status);
				ExcelUtil.writeCellData(deliverySheet, "D" + (4 + i),
						delivery.workSource);
				ExcelUtil.writeCellData(deliverySheet, "E" + (4 + i),
						delivery.responsibleDepartment);
				ExcelUtil.writeCellData(deliverySheet, "F" + (4 + i),
						delivery.responsiblePerson);
				ExcelUtil.writeCellData(deliverySheet, "G" + (4 + i),
						delivery.startDate);
				ExcelUtil.writeCellData(deliverySheet, "H" + (4 + i),
						delivery.actualStartDate);
				ExcelUtil.writeCellData(deliverySheet, "I" + (4 + i),
						delivery.finishDate);
				ExcelUtil.writeCellData(deliverySheet, "J" + (4 + i),
						delivery.actualFinishDate);
				ExcelUtil.writeCellData(deliverySheet, "K" + (4 + i),
						delivery.progressDescription);
			}
			ExcelUtil.setAllBorders(deliverySheet, "A4:K" + (deliveryNum + 3));

			Dispatch deliveryStatisticsSheet = ExcelUtil.getSheet(sheets,
					Integer.valueOf(2));
			int deliveryStatisticsNum = deliveryStatisticsVector.size();
			if (deliveryStatisticsNum > 21) {
				int minus = deliveryStatisticsNum - 21;
				ExcelUtil.copyRow(deliveryStatisticsSheet, 4, minus);
			}
			for (int i = 0; i < deliveryStatisticsNum; i++) {
				DeliveryStatistics deliveryStatistics = (DeliveryStatistics) deliveryStatisticsVector
						.get(i);

				ExcelUtil.writeCellData(deliveryStatisticsSheet, "B" + (4 + i),
						Integer.valueOf(i + 1));
				ExcelUtil.writeCellData(deliveryStatisticsSheet, "C" + (4 + i),
						deliveryStatistics.modelName);
				ExcelUtil.writeCellData(deliveryStatisticsSheet, "D" + (4 + i),
						Integer.valueOf(deliveryStatistics.taskTotalCount));
				ExcelUtil.writeCellData(deliveryStatisticsSheet, "E" + (4 + i),
						Integer.valueOf(deliveryStatistics.onScheduleCount));
				ExcelUtil.writeCellData(deliveryStatisticsSheet, "F" + (4 + i),
						Integer.valueOf(deliveryStatistics.delayedCount));
				ExcelUtil.writeCellData(deliveryStatisticsSheet, "G" + (4 + i),
						Integer.valueOf(deliveryStatistics.deferredCount));
				ExcelUtil.writeCellData(deliveryStatisticsSheet, "H" + (4 + i),
						Integer.valueOf(deliveryStatistics.unexpiredCount));
				ExcelUtil.writeCellData(deliveryStatisticsSheet, "I" + (4 + i),
						deliveryStatistics.onSchedulePercent);
				ExcelUtil.writeCellData(deliveryStatisticsSheet, "J" + (4 + i),
						deliveryStatistics.delayedPercent);
				ExcelUtil.writeCellData(deliveryStatisticsSheet, "K" + (4 + i),
						deliveryStatistics.totalPercent);
			}
			ExcelUtil.setAllBorders(deliveryStatisticsSheet, "B4:K"
					+ (deliveryStatisticsNum + 3));
			if (deliveryStatisticsNum > 1) {
				int lastRowNum = 4 + deliveryStatisticsNum - 2;
				ExcelUtil.insertHistogram(deliveryStatisticsSheet, workBook,
						"C3:H" + lastRowNum, "H" + lastRowNum, "", true,
						this.groupNameVector, 1);
			}
		} finally {
			ExcelUtil.closeExcelApp(excelApp, workBook);
		}
		File reportFile = TcUtil.renameFile(templateFile, "项目交付物情况报表");
		return reportFile;
	}

	public boolean isCompleted() {
		return this.completed;
	}
}
