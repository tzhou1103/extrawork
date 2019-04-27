/**
 * 
 */
package com.zht.customization.utils;

import java.io.File;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCQueryClause;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class DatasetFinder {
	private TCComponentQueryType queryType = null;

	private TCComponentQuery datasetQuery = null;

	private String[] entry_names = new String[2];

	private String[] entry_values = new String[2];

	private DatasetFinder(TCSession session) {
		try {
			this.queryType = ((TCComponentQueryType) session
					.getTypeComponent("ImanQuery"));
			this.datasetQuery = ((TCComponentQuery) this.queryType
					.find("__cust_common"));
			if ((this.datasetQuery == null) || (!this.datasetQuery.isValid())) {
				MessageBox.post("Option failed,not find Query object",
						"Message", MessageBox.INFORMATION);
			}
		} catch (TCException imane) {
			imane.printStackTrace();
			imane.dump();
		}
	}

	public static DatasetFinder GetDataSetFinder(TCSession session) {
		DatasetFinder dsFinder = null;
		if (session != null) {
			dsFinder = new DatasetFinder(session);
		}
		return dsFinder;
	}

	public TCComponentDataset FindDatasetByName(String datasetname,
			String userID) {
		try {
			this.entry_values[0] = datasetname == null ? ""
					: datasetname;
			this.entry_values[1] = userID == null ? "" : userID;
			if ((this.datasetQuery == null) || (!this.datasetQuery.isValid())) {
				MessageBox.post("Option failed，not find Query object",
						"Message", 1);
				return null;
			}
			TCQueryClause[] clause = this.datasetQuery.describe();
			for (int i = 0; i < clause.length; i++) {
				String attributeName = clause[i].getAttributeName();
				if (attributeName.equals("object_name"))
					this.entry_names[0] = clause[i]
							.getUserEntryNameDisplay();
				else if (attributeName.equals("owning_user.user_id"))
					this.entry_names[1] = clause[i]
							.getUserEntryNameDisplay();
			}
			TCComponent[] com_dataset = this.datasetQuery.execute(
					this.entry_names, this.entry_values);
			if ((com_dataset == null) || (com_dataset.length == 0)) {
				System.err.println("datasetQuery find nothing");
				return null;
			}
			TCComponentDataset dataset0 = (TCComponentDataset) com_dataset[0];
			TCComponentDataset dataset = dataset0.latest();
			return dataset;
		} catch (TCException imane) {
			imane.printStackTrace();
			imane.dump();
		}
		return null;
	}

	/**
	 * 导出数据集中的文件到本地
	 * @param dataset 数据集对象
	 * @param nameRef 命名的引用
	 * @param filename 文件名
	 * @param dir 路径
	 * @return
	 */
	public File ExpertFileToDir(TCComponentDataset dataset, String nameRef,
			String filename, String dir) {
		try {
			String workdir = dir;
			File tFile = new File(workdir, filename);
			if (tFile.exists())
				tFile.delete();
			File expertFile = dataset.getFile(nameRef, filename, workdir);
			return expertFile;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
