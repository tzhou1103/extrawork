package com.nio.util;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.teamcenter.clientx.AppXSession;
import com.teamcenter.services.strong.core._2006_03.DataManagement.ItemProperties;
import com.teamcenter.services.strong.core._2006_03.DataManagement.ObjectOwner;
import com.teamcenter.services.strong.core._2006_03.DataManagement.GenerateItemIdsAndInitialRevisionIdsProperties;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.internal.strong.core.ICTService;
import com.teamcenter.services.internal.strong.core._2011_06.ICT.Arg;
import com.teamcenter.services.internal.strong.core._2011_06.ICT.InvokeICTMethodResponse;
import com.teamcenter.services.internal.strong.core._2011_06.ICT.Structure;
import com.teamcenter.services.loose.core._2006_03.FileManagement.DatasetFileInfo;
import com.teamcenter.services.loose.core._2006_03.FileManagement.GetDatasetWriteTicketsInputData;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.core._2006_03.DataManagement.CreateDatasetsResponse;
import com.teamcenter.services.strong.core._2006_03.DataManagement.CreateItemsOutput;
import com.teamcenter.services.strong.core._2006_03.DataManagement.CreateItemsResponse;
import com.teamcenter.services.strong.core._2006_03.DataManagement.CreateRelationsResponse;
import com.teamcenter.services.strong.core._2006_03.DataManagement.DatasetProperties;
import com.teamcenter.services.strong.core._2006_03.DataManagement.GenerateItemIdsAndInitialRevisionIdsResponse;
import com.teamcenter.services.strong.core._2006_03.DataManagement.ItemIdsAndInitialRevisionIds;
import com.teamcenter.services.strong.core._2006_03.DataManagement.Relationship;
import com.teamcenter.services.strong.core._2006_03.DataManagement.RevisionIds;
import com.teamcenter.services.strong.core._2007_01.DataManagement.GetItemFromIdPref;
import com.teamcenter.services.strong.core._2007_01.DataManagement.VecStruct;
import com.teamcenter.services.strong.core._2007_01.DataManagement.WhereReferencedInfo;
import com.teamcenter.services.strong.core._2007_01.DataManagement.WhereReferencedOutput;
import com.teamcenter.services.strong.core._2007_01.DataManagement.WhereReferencedResponse;
import com.teamcenter.services.strong.core._2007_09.DataManagement.RemoveNamedReferenceFromDatasetInfo;
import com.teamcenter.services.strong.core._2008_06.DataManagement.ReviseOutput;
import com.teamcenter.services.strong.core._2009_10.DataManagement.GetItemFromAttributeInfo;
import com.teamcenter.services.strong.core._2009_10.DataManagement.GetItemFromAttributeResponse;
import com.teamcenter.services.strong.core._2013_05.DataManagement.GetChildrenInputData;
import com.teamcenter.services.strong.core._2013_05.DataManagement.GetChildrenOutput;
import com.teamcenter.services.strong.core._2013_05.DataManagement.GetChildrenResponse;
import com.teamcenter.services.strong.core._2014_10.DataManagement.ChildrenInputData;
import com.teamcenter.services.strong.query.SavedQueryService;
import com.teamcenter.services.strong.query._2006_03.SavedQuery.DescribeSavedQueriesResponse;
import com.teamcenter.services.strong.query._2006_03.SavedQuery.GetSavedQueriesResponse;
import com.teamcenter.services.strong.query._2006_03.SavedQuery.SavedQueryFieldListObject;
import com.teamcenter.services.strong.query._2006_03.SavedQuery.SavedQueryFieldObject;
import com.teamcenter.services.strong.query._2007_06.SavedQuery.ExecuteSavedQueriesResponse;
import com.teamcenter.services.strong.query._2007_06.SavedQuery.SavedQueryInput;
import com.teamcenter.services.strong.query._2007_06.SavedQuery.SavedQueryResults;
import com.teamcenter.services.strong.query._2007_09.SavedQuery.SavedQueriesResponse;
import com.teamcenter.services.strong.query._2008_06.SavedQuery.QueryInput;
import com.teamcenter.services.strong.query._2010_04.SavedQuery.FindSavedQueriesCriteriaInput;
import com.teamcenter.services.strong.query._2010_04.SavedQuery.FindSavedQueriesResponse;
import com.teamcenter.soa.client.FileManagementUtility;
import com.teamcenter.soa.client.model.ErrorStack;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.EPMTask;
import com.teamcenter.soa.client.model.strong.Group;
import com.teamcenter.soa.client.model.strong.ImanQuery;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.User;
import com.teamcenter.soa.client.model.strong.WorkspaceObject;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.services.strong.core._2007_09.DataManagement.NamedReferenceInfo;
import com.teamcenter.services.strong.workflow.WorkflowService;
import com.teamcenter.services.strong.workflow._2007_06.Workflow.ReleaseStatusInput;
import com.teamcenter.services.strong.workflow._2007_06.Workflow.ReleaseStatusOption;
import com.teamcenter.services.strong.workflow._2007_06.Workflow.SetReleaseStatusResponse;

public class MySOAUtil 
{
	private static final Logger logger = MyLoggerFactory.getLogger(MySOAUtil.class);
	private static User user = null;
	public static List<String> groupNameList = new ArrayList<String>();
	
	public static User login()
	{
		if (user == null)
		{
			Properties properties = new Properties();
			try 
			{
				properties.load(MyLoggerFactory.class.getClassLoader().getResourceAsStream("tc.properties"));
				if (properties != null)
					PropertyConfigurator.configure(properties);
				
			} catch (IOException e) 
			{
				e.printStackTrace();
			}
			
			String url = properties.getProperty("TcUrl");
			String userName = properties.getProperty("TcUserName");
			String password = properties.getProperty("TcPassword");
			
			String groupNames = properties.getProperty("groupNames");
			if (groupNames != null)
			{
				String[] splitStrs = groupNames.split(",");
				if (splitStrs != null && groupNames.length() > 0) 
				{
					for (String string : splitStrs) {
						if (!groupNameList.contains(string)) {
							groupNameList.add(string);
						}
					}
				}
			}
			
			System.out.println(">>> logining with user:" + userName);
			AppXSession session = new AppXSession(url);
			user = session.login(userName, password);
		} else {
			DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
			dmService.getProperties(new ModelObject[] { user },	new String[] { "user_id" });
			try {
				System.out.println(">>> aleardy logined with user " + user.get_user_id());
			} catch (NotLoadedException e) {
				e.printStackTrace();
			}
			
			if (groupNameList.size() == 0) {
				Properties properties = new Properties();
				try {
					properties.load(MyLoggerFactory.class.getClassLoader().getResourceAsStream("tc.properties"));
					if (properties != null)
						PropertyConfigurator.configure(properties);
				} catch (IOException e) {
					e.printStackTrace();
				}

				String groupNames = properties.getProperty("groupNames");
				if (groupNames != null) 
				{
					String[] splitStrs = groupNames.split(",");
					if (splitStrs != null && groupNames.length() > 0) {
						for (String string : splitStrs) {
							if (!groupNameList.contains(string)) {
								groupNameList.add(string);
							}
						}
					}
				}
			}
		}
		return user;
	}
	
	/**
	 * 查找零组件
	 * 
	 * @param item_id
	 * @return
	 */
	public static final Item findItem(String item_id)
	{
		ModelObject[] modelObjects = MySOAUtil.query("Item ID", new String[] { "Item ID" }, new String[] { item_id });
		if (modelObjects != null && modelObjects.length > 0)
			return (Item) modelObjects[0];
		return null;
	}
	
	/**
	 * 获取零组件最新版本
	 * 
	 * @param item
	 * @return
	 * @throws NotLoadedException
	 */
	public static final ItemRevision getLatestItemRev(Item item) throws NotLoadedException
	{
		DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
		dmService.getProperties(new ModelObject[] { item },	new String[] { "revision_list" });
		ModelObject[] revision_list = item.get_revision_list();
		if (revision_list != null && revision_list.length > 0) 
			return (ItemRevision) revision_list[revision_list.length - 1];
		return null;
	}
	
	/**
	 * 获取零组件最新版本
	 * 
	 * @param item
	 * @return
	 * @throws NotLoadedException
	 */
	public static final ItemRevision getCRAARev(Item crItem) throws NotLoadedException
	{
		ItemRevision crAARev = null;
		DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
		dmService.getProperties(new ModelObject[] { crItem },	new String[] { "revision_list" });
		ModelObject[] revision_list = crItem.get_revision_list();
		if (revision_list != null && revision_list.length > 0) 
		{
			for (ModelObject modelObject : revision_list) 
			{
				ItemRevision itemRevision = (ItemRevision) modelObject;
				dmService.getProperties(new ModelObject[] { itemRevision }, new String[] { "item_revision_id" });
				String item_revision_id = itemRevision.get_item_revision_id();
				if (item_revision_id.equals("AA")) {
					crAARev = itemRevision;
					break;
				}
			}
		}
		return crAARev;
	}

	/**
	 * 调用系统查询
	 * 
	 * @param queryName
	 * @param keys
	 * @param values
	 * @return
	 */
	public static final ModelObject[] query(String queryName, String[] keys, String[] values) 
	{
		ImanQuery query = null;

		SavedQueryService queryService = SavedQueryService.getService(AppXSession.getConnection());
		try 
		{
			GetSavedQueriesResponse savedQueries = queryService.getSavedQueries();

			if (savedQueries.queries.length == 0) 
			{
				System.out.println("There are no saved queries in the system.");
            	logger.error("There are no saved queries in the system.");
				return null;
			}

			for (int i = 0; i < savedQueries.queries.length; i++) 
			{
				if (savedQueries.queries[i].name.equals(queryName)) 
				{
					query = savedQueries.queries[i].query;
					break;
				}
			}
		} catch (ServiceException e) {
			System.out.println("GetSavedQueries service request failed.");
			System.out.println(e.getMessage());
			logger.error("GetSavedQueries service request failed.");
            logger.error(e.getMessage());
			return null;
		}

		if (query == null) {
			System.out.println("There is not an " + queryName + " query.");
			logger.error("There is not an " + queryName + " query.");
			return null;
		}

		try {
			Vector<String> newKeys = new Vector<String>();
			DescribeSavedQueriesResponse desc = queryService.describeSavedQueries(new ImanQuery[] {query});
			SavedQueryFieldListObject fieldLists = desc.fieldLists[0];
			SavedQueryFieldObject[] fields = fieldLists.fields;
			
			for (int i = 0; i < keys.length; i++) 
			{
				String key = keys[i];
				String newKey = key;
				for (int j = 0; j < fields.length; j++) 
				{
					SavedQueryFieldObject field = fields[j];
					if (field.attributeName.equals(key))
					{
						newKey = field.entryName;
						System.out.println("info:" + newKey);
					}
				}
				newKeys.add(newKey);
			}
			
			SavedQueryInput[] savedQueryInput = new SavedQueryInput[1];
			savedQueryInput[0] = new SavedQueryInput();
			savedQueryInput[0].query = query;
			savedQueryInput[0].maxNumToReturn = 999;
			savedQueryInput[0].limitListCount = 0;
			savedQueryInput[0].limitList = new ModelObject[0];
			savedQueryInput[0].entries = newKeys.toArray(new String[newKeys.size()]);
			savedQueryInput[0].values = values;
			savedQueryInput[0].maxNumToInflate = 999;

			ExecuteSavedQueriesResponse savedQueryResult = queryService.executeSavedQueries(savedQueryInput);
			SavedQueryResults found = savedQueryResult.arrayOfResults[0];
			
			return found.objects;
		} catch (Exception e) {
			System.out.println("ExecuteSavedQuery service request failed.");
			System.out.println(e.getMessage());
			logger.error("ExecuteSavedQuery service request failed.");
            logger.error(e.getMessage());
		}
		return null;
	}
	
    /**
     * 修订零组件
     * 
     * @param item
     * @param latestRev
     * @param revIDPrefix
     * @return
     * @throws ServiceException
     * @throws NotLoadedException
     */
    public static ItemRevision reviseItem(Item item, ItemRevision latestRev, String revIDPrefix) throws ServiceException, NotLoadedException
    {
    	DataManagement dataManagement = new DataManagement();
    	Map<BigInteger, RevisionIds> generateRevisionIds = dataManagement.generateRevisionIds(new Item[] { item });
		String[] revIDPrefixArray = { revIDPrefix };
		Map<String, ReviseOutput> reviseItems = dataManagement.reviseItems(generateRevisionIds, new ItemRevision[] { latestRev }, revIDPrefixArray);
		if (reviseItems != null && reviseItems.size() > 0) 
		{
			Iterator<Entry<String, ReviseOutput>> iterator = reviseItems.entrySet().iterator();
			while (iterator.hasNext()) 
			{
				Entry<String, ReviseOutput> entry = iterator.next();
				return entry.getValue().newItemRev;
			}
		}
		return null;
    }
    
	/**
	 * 根据指定关系获取关联对象
	 * 
	 * @param object
	 * @param relation
	 * @return
	 * @throws Exception
	 */
	public static ModelObject[] getRelatedObjects(ModelObject object, String relation) throws Exception
	{
		DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
		dmService.getProperties(new ModelObject[] { object }, new String[] { relation });
		ModelObject[] modelObjects = object.getPropertyObject(relation).getModelObjectArrayValue();
		return modelObjects;
	}
	
	public static boolean isContain(ModelObject[] modelObjects, ModelObject obj2)
	{
		for (ModelObject modelObject : modelObjects) {
			if (modelObject == obj2) {
				System.out.println(">>> contain.");
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 根据关系关联对象
	 * 
	 * @param obj1
	 * @param obj2
	 * @param relationType
	 * @throws Exception
	 */
	public static void createRelation(ModelObject obj1, ModelObject obj2, String relationType) throws Exception 
	{
		DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());

		Relationship relationship = new Relationship();
		relationship.primaryObject = obj1;
		relationship.secondaryObject = obj2;
		relationship.relationType = relationType;

		CreateRelationsResponse response = dmService.createRelations(new Relationship[] { relationship });

		if (response.serviceData.sizeOfPartialErrors() > 0) {
			throw new Exception(buildErrorMessage(response.serviceData));
		}
	}
	
	/**
	 * 给对象增加状态
	 * 
	 * @param wso
	 * @param status
	 * @throws Exception
	 */
	public static void addStatus(WorkspaceObject wso, String status) throws Exception
	{
		WorkflowService wfSrv = WorkflowService.getService(AppXSession.getConnection());
		ReleaseStatusInput[] rsInput = new ReleaseStatusInput[] { new ReleaseStatusInput() };
		rsInput[0].objects = new WorkspaceObject[] { wso };
		rsInput[0].operations = new ReleaseStatusOption[] { new ReleaseStatusOption() };
		rsInput[0].operations[0].operation = "Append";
		rsInput[0].operations[0].newReleaseStatusTypeName = status;
		SetReleaseStatusResponse response = wfSrv.setReleaseStatus(rsInput);
		if (response.serviceData.sizeOfPartialErrors() > 0) {
			throw new Exception(MySOAUtil.buildErrorMessage(response.serviceData));
		}
	}
	
	/**
	 * 获取对象所在流程
	 * 
	 * @param wso
	 * @return
	 * @throws Exception
	 */
	public static EPMTask getEPMTask(WorkspaceObject wso, String epmTaskPrfix) throws Exception
	{
		EPMTask epmTask = null;
		DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
		WhereReferencedResponse response = dmService.whereReferenced(new WorkspaceObject[] { wso }, 1);
		if (response.serviceData.sizeOfPartialErrors() > 0)
			throw new Exception(buildErrorMessage(response.serviceData));
		WhereReferencedOutput[] outputs = response.output;
		for (int j = 0; j < outputs.length; j++) 
		{
			WhereReferencedInfo[] infos = outputs[j].info;
			for (WhereReferencedInfo info : infos) 
			{
				WorkspaceObject reference = info.referencer;
				if (reference.getTypeObject().isInstanceOf("EPMTask")) 
				{
					dmService.getProperties(new ModelObject[] { reference }, new String[] { "object_name" });
//					String object_name = epmTask.get_object_name();
					String object_name = reference.get_object_name();
					if (object_name.startsWith(epmTaskPrfix)) {
						epmTask = (EPMTask) reference;
					}
					break;
				}
			}
		}
		
		return epmTask;
	}
	
	/**
	 * 判断对象是否发布
	 * 
	 * @param latestRev
	 * @return
	 */
	public static boolean isObjectReleased(ModelObject latestRev)
	{
		try
		{
			DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
			dmService.getProperties(new ModelObject[] { latestRev }, new String[] { "release_status_list" });
			ModelObject[] statuses = latestRev.getPropertyObject("release_status_list").getModelObjectArrayValue();
			if (statuses != null && statuses.length > 0) {
				return true;
			}
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 创建数据集
	 * 
	 * @param dsName
	 * @param dsType
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static Dataset createDataset(String dsName, String dsType)
	{
		DatasetProperties props = new DatasetProperties();
		props.clientId = "datasetWriteTixTestClientId";
		props.type = dsType;
		props.name = dsName;
		props.description = "";
		DatasetProperties[] currProps = {props};

		DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
		CreateDatasetsResponse resp =  dmService.createDatasets(currProps);
		
		Dataset dataset = resp.output[0].dataset;
	
		return dataset;
	}
	
	/**
	 * 创建零组件
	 * 
	 * @param item_id
	 * @param rev_id
	 * @param itemName
	 * @param itemType
	 * @param savedFolder
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public static Item createItem(String item_id, String rev_id, String itemName, String itemType, ModelObject savedFolder) throws Exception
	{
		DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
		
		if ("".equals(item_id) || "".equals(rev_id))
		{
			GenerateItemIdsAndInitialRevisionIdsProperties[] properties = new GenerateItemIdsAndInitialRevisionIdsProperties[1];
			properties[0] = new GenerateItemIdsAndInitialRevisionIdsProperties();
			properties[0].count = 1;
			properties[0].item = null;
			properties[0].itemType = itemType;
			
			GenerateItemIdsAndInitialRevisionIdsResponse response = dmService.generateItemIdsAndInitialRevisionIds(properties);
			if (response.serviceData.sizeOfPartialErrors() > 0)
				throw new Exception(buildErrorMessage(response.serviceData));
			
			Map map = response.outputItemIdsAndInitialRevisionIds;
			
			Iterator iterator = map.entrySet().iterator();
			
			if (iterator.hasNext())
			{
				Map.Entry entry = (Map.Entry)iterator.next();
				ItemIdsAndInitialRevisionIds[] arrayOfItemIdsAndInitialRevisionIds = (ItemIdsAndInitialRevisionIds[])entry.getValue();
			    ItemIdsAndInitialRevisionIds itemIdsAndInitialRevisionIds = arrayOfItemIdsAndInitialRevisionIds[0];
			    if ("".equals(item_id))
			    	item_id = itemIdsAndInitialRevisionIds.newItemId;
				if ("".equals(rev_id))
					rev_id = itemIdsAndInitialRevisionIds.newRevId;
			}
		}
		
		ItemProperties[] itemProps = new ItemProperties[1];
		itemProps[0] = new ItemProperties();
		itemProps[0].itemId = item_id;
		itemProps[0].name = itemName;
		itemProps[0].type = itemType;
		itemProps[0].revId = rev_id;
		
		CreateItemsResponse response = dmService.createItems(itemProps, savedFolder, "");
		if (response.serviceData.sizeOfPartialErrors() > 0)
			throw new Exception(buildErrorMessage(response.serviceData));
		CreateItemsOutput[] outputs = response.output;
		if (outputs != null && outputs.length > 0)
			return outputs[0].item;
		return null;
	}
	
	/**
	 * 获取版本对象特定关系下，指定类型和名称的数据集
	 * 
	 * @param rev
	 * @param relation
	 * @param dsName
	 * @param dsType
	 * @return
	 */
	public static Dataset findDataset(ItemRevision rev, String relation, String dsName, String dsType)
	{
		DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
		dmService.getProperties(new ModelObject[] { rev }, new String[] { relation });
		try {
			ModelObject[] datasets = rev.getPropertyObject(relation).getModelObjectArrayValue();
			if (datasets == null || datasets.length == 0)
				return null;
			
			dmService.getProperties(datasets, new String[]{"object_type", "object_name"});
			for (ModelObject dataset : datasets) 
			{
				String objectType = dataset.getPropertyObject("object_type").getStringValue();
				String objectName = dataset.getPropertyObject("object_name").getStringValue();
				if (dsType.equals(objectType) && dsName.equals(objectName))
				{
					return (Dataset) dataset;
				}
			}
		} catch (NotLoadedException e) {
			e.printStackTrace();
			return null;
		}
		return null;		
	}
	
	/**
	 * 上传文件到TC
	 * 
	 * @param file
	 * @param dataset
	 * @param namedRefType
	 * @throws Exception
	 */
	public static void uploadFiles(File file, Dataset dataset, String namedRefType) throws Exception
    {
    	FileManagementUtility fMSFileManagement = null;
		try 
		{
			DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
			fMSFileManagement = new FileManagementUtility(AppXSession.getConnection());
			
			DatasetFileInfo fileInfo = new DatasetFileInfo();
			DatasetFileInfo[] fileInfos = new DatasetFileInfo[1];

			fileInfo.clientId            = "file_1";
			fileInfo.fileName            = file.getAbsolutePath();
			fileInfo.namedReferencedName = namedRefType;
			fileInfo.isText              = true;
			fileInfo.allowReplace        = false;
			fileInfos[0] = fileInfo;

			GetDatasetWriteTicketsInputData inputData = new GetDatasetWriteTicketsInputData();
			inputData.dataset = dataset;
			inputData.createNewVersion = false;
			inputData.datasetFileInfos = fileInfos;
			

			GetDatasetWriteTicketsInputData[] inputs  = new GetDatasetWriteTicketsInputData[1];
			inputs[0] = inputData;

			ServiceData response = fMSFileManagement.putFiles(inputs);

			if (response.sizeOfPartialErrors() > 0)
			{
				String errorMessage = buildErrorMessage(response);
			    ModelObject [] datasets = new ModelObject[1];
			    datasets[0] = inputs[0].dataset;
			    dmService.deleteObjects(datasets);
			    
			    throw new Exception(errorMessage);
			}
		}
		finally
		{
			if (fMSFileManagement != null)
				fMSFileManagement.term();
		}		
    }
	
	/**
	 * 从数据集删除命名引用
	 * 
	 * @param dataset
	 * @param namedRefType
	 */
	public static void removeNamedReferenceFromDataset(Dataset dataset, String namedRefType)
	{
		DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
		RemoveNamedReferenceFromDatasetInfo[] dsInfos = new RemoveNamedReferenceFromDatasetInfo[1];
		dsInfos[0] = new RemoveNamedReferenceFromDatasetInfo();
		dsInfos[0].clientId = "RemoveNamedReference";
		dsInfos[0].dataset = dataset;
		NamedReferenceInfo[] namedReferenceInfos = new NamedReferenceInfo[1];
		namedReferenceInfos[0] = new NamedReferenceInfo();
		namedReferenceInfos[0].deleteTarget = true;
		namedReferenceInfos[0].type = namedRefType;
		dsInfos[0].nrInfo = namedReferenceInfos;
		dmService.removeNamedReferenceFromDataset(dsInfos);
	}

	/**
	 * 获取对象下指定类型的对象
	 * 
	 * @param object
	 * @param type
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public static ItemRevision getRelatedItemRevision(ModelObject object, String type) throws Exception
	{
		DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
		GetChildrenInputData[] inputDatas = new GetChildrenInputData[1];
		inputDatas[0] = new GetChildrenInputData();
		inputDatas[0].obj = object;
		inputDatas[0].clientId = "get related comps";
		GetChildrenResponse childrenResponse = dmService.getChildren(inputDatas);
		if (childrenResponse.serviceData.sizeOfPartialErrors() > 0) {
			 throw new Exception(buildErrorMessage(childrenResponse.serviceData));
		}
		Map objectWithChildren = childrenResponse.objectWithChildren;
		if (objectWithChildren != null && objectWithChildren.size() > 0) 
		{
			Iterator iterator = objectWithChildren.entrySet().iterator();
			while (iterator.hasNext()) 
			{
				Entry entry = (Entry) iterator.next();
				GetChildrenOutput[] outputs = (GetChildrenOutput[]) entry.getValue();
				for (GetChildrenOutput output : outputs) 
				{
					ModelObject[] children = output.children;
					for (ModelObject modelObject : children) 
					{
						if (modelObject.getTypeObject().isInstanceOf("ItemRevision")) 
						{
							ItemRevision itemRevision = (ItemRevision) modelObject;
							dmService.getProperties(new ModelObject[] { itemRevision },	new String[] { "object_type" });
							String object_type = itemRevision.get_object_type();
							if (object_type.equals(type)) {
								return itemRevision;
							}
						}
					}
				}
			}
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static Item getItem(String item_id)
	{
		DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
		GetItemFromAttributeInfo[] itemAttrInfo = new GetItemFromAttributeInfo[1];
		itemAttrInfo[0] = new GetItemFromAttributeInfo();
		itemAttrInfo[0].itemAttributes.put("item_id", item_id);
		itemAttrInfo[0].revIds = new String[1];
		GetItemFromIdPref pref = new GetItemFromIdPref();
		GetItemFromAttributeResponse resp = dmService.getItemFromAttribute(itemAttrInfo, -1, pref);
		return resp.output[0].item;
	}
	
	@SuppressWarnings("unchecked")
	public static ItemRevision getItemRevision(String item_id, String item_revision_id)
	{
		DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
		GetItemFromAttributeInfo[] itemAttrInfo = new GetItemFromAttributeInfo[1];
		itemAttrInfo[0] = new GetItemFromAttributeInfo();
		itemAttrInfo[0].itemAttributes.put("item_id", item_id);
		itemAttrInfo[0].revIds = new String[1];
		itemAttrInfo[0].revIds[0] = item_revision_id;
		int i = (item_revision_id == null || item_revision_id.isEmpty()) ? 1 : 0;
		GetItemFromIdPref pref = new GetItemFromIdPref();
		GetItemFromAttributeResponse resp = dmService.getItemFromAttribute(itemAttrInfo, i, pref);
		ItemRevision itemRevision = resp.output[0].itemRevOutput[0].itemRevision;
		return itemRevision;
	}
	
	/**
	 * 获取零件版本主属性表单
	 * 
	 * @param desItemRevision
	 * @return
	 */
	public ModelObject getMasterForm(ItemRevision itemRevision)
	{
		DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
		dmService.getProperties(new ModelObject[] { itemRevision }, new String[] { "IMAN_master_form_rev" });
		try
		{
			ModelObject[] mos = itemRevision.get_IMAN_master_form_rev();
			if (mos != null && mos.length > 0)
				return mos[0];
		}
		catch (NotLoadedException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static String buildErrorMessage(ServiceData paramServiceData) 
	{
		String str = null;
		if (paramServiceData.sizeOfPartialErrors() > 0) 
		{
			str = "";
			for (int i = 0; i < paramServiceData.sizeOfPartialErrors(); i++) 
			{
				ErrorStack localErrorStack = paramServiceData
						.getPartialError(i);
				String[] arrayOfString = localErrorStack.getMessages();
				for (int j = 0; (arrayOfString != null)
						&& (j < arrayOfString.length); j++) {
					str = str + arrayOfString[j];
					if (j < arrayOfString.length - 1)
						str = str + "\n";
				}
			}
		}
		return str;
	}
	
	
	public static User getUserById(String user_id) throws ServiceException {
		User user = null;

		ICTService ictService = ICTService.getService(AppXSession.getConnection());
		Arg[] args = new Arg[3];
		args[0] = new Arg();
		args[0].val = "User";
		args[1] = new Arg();
		args[1].val = "TYPE::User::User::POM_user";
		args[2] = new Arg();
		args[2].val = user_id;

		InvokeICTMethodResponse userresp = ictService.invokeICTMethod("ICCTUser", "find", args);

		String uid = null;
		if (userresp.output.length > 0 && userresp.output[0].structure.length > 0) {

			Structure re0 = userresp.output[0].structure[0];
			uid = re0.args[1].val;

		} else {

			// id查不到，用email查找
			SavedQueryService queryService = SavedQueryService.getService(AppXSession.getConnection());
			FindSavedQueriesCriteriaInput queryCI = new FindSavedQueriesCriteriaInput();
			queryCI.queryNames = new String[] { "___find_user_by_email" };
			FindSavedQueriesCriteriaInput[] qCIs = new FindSavedQueriesCriteriaInput[1];
			qCIs[0] = queryCI;
			FindSavedQueriesResponse resp = queryService.findSavedQueries(qCIs);
			if (resp.savedQueries.length > 0) {

				ImanQuery qry = resp.savedQueries[0];

				QueryInput savedQueryInput[] = new QueryInput[1];
				savedQueryInput[0] = new QueryInput();
				savedQueryInput[0].query = qry;
				savedQueryInput[0].maxNumToReturn = 1;
				savedQueryInput[0].limitList = new ModelObject[0];
				savedQueryInput[0].entries = new String[] { "_email" };
				savedQueryInput[0].values = new String[] { user_id + "@*" };

				SavedQueriesResponse savedQueryResult = queryService.executeSavedQueries(savedQueryInput);
				if (savedQueryResult.arrayOfResults.length > 0
						&& savedQueryResult.arrayOfResults[0].objectUIDS.length > 0) {
					uid = savedQueryResult.arrayOfResults[0].objectUIDS[0];
				}

			} else {
				logger.error("Teamcenter could not find Query : ___find_user_by_email");
			}

		}

		if (uid != null) {
			DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
			dmService.loadObjects(new String[] { uid });
			ModelObject obj = AppXSession.getConnection().getModelManager().getObject(uid);
			if (obj instanceof User)
				user = (User) obj;
		}

		return user;
	}

	public static Group getGrpById(String grp_id) throws ServiceException {
		ICTService ictService = ICTService.getService(AppXSession.getConnection());
		Arg[] args = new Arg[3];
		args[0] = new Arg();
		args[0].val = "Group";
		args[1] = new Arg();
		args[1].val = "TYPE::Group::Group::POM_group";
		args[2] = new Arg();
		args[2].val = grp_id;

		InvokeICTMethodResponse userresp = ictService.invokeICTMethod("ICCTGroup", "find", args);

		Structure re0 = userresp.output[0].structure[0];
		String uid = re0.args[1].val;

		ModelObject obj = AppXSession.getConnection().getModelManager().getObject(uid);

		return (Group) obj;
	}
	
	/**
	 * 更改所有权
	 * 
	 * @param session
	 * @param user
	 * @param objs
	 * @throws NotLoadedException
	 */
	public static void changeOwnership(User user, Group group, ModelObject[] objs) 
	{
		DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
		ObjectOwner[] object_owner = new ObjectOwner[objs.length];
		for (int i = 0; i < object_owner.length; i++)
		{			
			ObjectOwner ow = new ObjectOwner();
			ow.owner = user;
			ow.group = group;
			ow.object = objs[i];
			object_owner[i] = ow;
		}
		ServiceData serd = dmService.changeOwnership(object_owner);
		if (serd.sizeOfPartialErrors() > 0) {
			System.out.println("error: " + serd.getPartialError(0).getMessages()[0]);
			return;
		}
	}
	
	public static void removeObjectByRelation(ModelObject parentObj, String relation) throws Exception
	{
		DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
		ModelObject[] modelObjects = getRelatedObjects(parentObj, relation);
		if (modelObjects != null && modelObjects.length > 0) 
		{
			ChildrenInputData[] inputDatas = new ChildrenInputData[] { new ChildrenInputData() };
			inputDatas[0].parentObj = parentObj;
			inputDatas[0].childrenObj = modelObjects;
			inputDatas[0].propertyName = relation;
			inputDatas[0].clientId = "Portal";
			dmService.removeChildren(inputDatas);
			dmService.refreshObjects(new ModelObject[] { parentObj });
		}
	}
	
	public static void setProperty(ModelObject modelObject, String propName, String propValue)
	{
		DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
		HashMap<String, VecStruct> valueMap = new HashMap<String, VecStruct>();
		VecStruct struct1 = new VecStruct();
		struct1.stringVec = new String[] { propValue };
		valueMap.put(propName, struct1);
		dmService.setProperties(new ModelObject[] { modelObject }, valueMap);
	}
}
