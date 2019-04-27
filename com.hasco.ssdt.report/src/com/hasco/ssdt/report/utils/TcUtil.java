package com.hasco.ssdt.report.utils;

import java.io.File;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.*;
import com.teamcenter.rac.ui.common.RACUIUtil;

public class TcUtil
{	
	/**
	 * @return 获取当前会话
	 */
	public static TCSession getTcSession()
	{
		return (TCSession) AIFUtility.getCurrentApplication().getSession();
	}
	
	/**
	 * 检查当前用户对对象是否具有写权限
	 * 
	 * @param component
	 * @return
	 */
	public static boolean checkUserWriteAccessPrivilige(TCComponent component)
	{
		try {
			TCSession session = getTcSession();
			TCAccessControlService accessControlService = session.getTCAccessControlService();
			return accessControlService.checkUsersPrivilege(session.getUser(), component, "WRITE");
		} catch (TCException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 获取报表模板数据集
	 * @param templateDatasetName
	 * @return
	 * @throws TCException
	 */
	public static TCComponentDataset getTemplateDataset(String dbaUser, String templateDatasetName) throws TCException
	{
		TCComponentDataset templateDataset = null;
		
		TCComponentUserType userType = (TCComponentUserType) RACUIUtil.getTCSession().getTypeComponent("User");
		TCComponentUser user = userType.find(dbaUser);
		if (user != null) 
		{
			TCComponentFolder homeFolder = user.getHomeFolder();
			TCComponent[] relatedComponents = homeFolder.getRelatedComponents("contents");
			if (relatedComponents != null && relatedComponents.length > 0) 
			{
				for (TCComponent relatedComponent : relatedComponents) 
				{
					String objectName = relatedComponent.getStringProperty("object_name");
					String objectType = relatedComponent.getType();
					if (relatedComponent instanceof TCComponentFolder 
							&& objectName.equals("TCM_ReportTemplate") && objectType.equals("H5_ReportTemplate")) {
						TCComponentFolder templateFolder = (TCComponentFolder) relatedComponent;
						TCComponent dsComponent = getRelatedComponent(templateFolder, "contents", "MSExcelX", templateDatasetName);
						if (dsComponent != null) {
							templateDataset = (TCComponentDataset) dsComponent;
						}
					}
				}
			}
		}

		return templateDataset;
	}
	
	/**
	 * 获取对象指定关系下指定类型和指定名称的对象
	 * 
	 * @param parentComp
	 * @param relation
	 * @param compType
	 * @param compName
	 * @return
	 * @throws TCException
	 */
	public static TCComponent getRelatedComponent(TCComponent parentComp, String relation, String compType, String compName) throws TCException
	{
		if (parentComp == null)
			return null;
		
		TCComponent[] components = parentComp.getRelatedComponents(relation);
		for (int i = 0; i < components.length; i++)
		{
			String objectType = components[i].getType();
			String objectName = components[i].getStringProperty("object_name");
			if (objectType.equals(compType) && objectName.equals(compName)) {
				return components[i];
			}
		}
		
		return null;
	}
	
	/**
	 * 下载模板数据集文件到本地
	 * 
	 * @param dataset
	 * @param dirctory 
	 * @return
	 * @throws TCException
	 */
	public static File getTemplateFile(TCComponentDataset dataset, String dirctory) throws TCException
	{
		String workingDirPath = dirctory;
		if (workingDirPath ==null || workingDirPath.isEmpty()) {
			workingDirPath = FileUtility.getTempDir() + File.separator + System.currentTimeMillis();
		}
		
		File workingDir = new File(workingDirPath);
		if (!workingDir.exists()) {
			workingDir.mkdirs();
		}
		File[] files = dataset.getFiles("excel", workingDirPath);
		if (files != null && files.length > 0) {
			return files[0];
		}
		return null;
	}
	


	/**
	 * 创建数据集
	 * 
	 * @param name
	 * @param description
	 * @param type
	 * @return
	 * @throws TCException
	 */
	public static final TCComponentDataset createDataset(String name, String description, String type) throws TCException
	{
		TCComponentDataset dataset = null;
		TCComponentDatasetType datasetType = (TCComponentDatasetType) getTcSession().getTypeComponent(type);
		if (datasetType == null)
			throw new TCException("无法获取名为" + type + "的数据集类型！");
		dataset = datasetType.create(name, description, type);
		return dataset;
	}
	
	/**
	 * 对数据集导入文件
	 * 
	 * @param dataset
	 * @param file
	 * @param fileType
	 * @param refType
	 * @throws TCException
	 */
	public static final void importFileToDataset(TCComponentDataset dataset, File file, String fileType, String refType) throws TCException 
	{
		String[] as1 = { file.getPath() };
		String[] as2 = { fileType };
		String[] as3 = { "Plain" };
		String[] as4 = { refType };
		dataset.setFiles(as1, as2, as3, as4);
	}
	
	/**
	 * 移除数据集指定的命名引用
	 * 
	 * @param dataset
	 * @param namedReference
	 * @throws TCException
	 */
	public static final void removeFilesFromDataset(TCComponentDataset dataset, String namedReference) throws TCException 
	{
		if (dataset == null)
			return;
		
		NamedReferenceContext[] contexts = dataset.getDatasetDefinitionComponent().getNamedReferenceContexts();
		for (int i = 0; i < contexts.length; i++)
		{
			NamedReferenceContext context = contexts[i];
			String reference = context.getNamedReference();
			if (reference.equals(namedReference))
				dataset.removeNamedReference(reference);
		}
	}
	
	/**
	 * 移除数据集所有的命名引用
	 * 
	 * @param dataset
	 * @throws TCException
	 */
	public static final void removeAllFilesFromDataset(TCComponentDataset dataset) throws TCException 
	{
		if (dataset == null)
			return;
		
		NamedReferenceContext[] contexts = dataset.getDatasetDefinitionComponent().getNamedReferenceContexts();
		for (int i = 0; i < contexts.length; i++)
		{
			NamedReferenceContext context = contexts[i];
			String reference = context.getNamedReference();
			dataset.removeNamedReference(reference);
		}
	}

}
