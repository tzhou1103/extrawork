package com.zht.report.utils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.zht.report.dialogs.ZHTConstants;

public class ReportUtil 
{
	
	public static String getModelName(TCComponentItemRevision paramItemRevision) throws TCException
	{
		StringBuilder stringBuilder = new StringBuilder();
		TCComponent[] components = paramItemRevision.getRelatedComponents();
		for (int i = 0; i < components.length; i++) 
		{
			if (components[i] instanceof TCComponentDataset)
			{
				TCComponentDataset dataset = (TCComponentDataset) components[i];
				TCComponentTcFile[] tcFiles = dataset.getTcFiles();
				if (tcFiles != null && tcFiles.length > 0) 
				{
					for (TCComponentTcFile tcFile : tcFiles)
					{
						String originalFileName = tcFile.getProperty("original_file_name");
						if (originalFileName.endsWith(".prt") || originalFileName.endsWith(".CATPart") || originalFileName.endsWith(".CATProduct")) {
							stringBuilder.append(originalFileName).append(";");
						}
					}
				}
			}
		}
		
		String modelName = stringBuilder.toString();
		if (modelName.endsWith(";")) {
			modelName = modelName.substring(0, modelName.lastIndexOf(";"));
		}
		
		return modelName;
	}
	
	public static String getCarType(TCComponentGroup group) throws Exception
	{
		String carType= "";
		
		String[] zht_qcfl_list = TcUtil.getPrefStringValues("zht_qcfl_list");
		if (zht_qcfl_list == null || zht_qcfl_list.length < 1) {
			throw new Exception(MessageFormat.format(ZHTConstants.INVALIDPREFCONFIGURATION_MSG, "zht_qcfl_list"));
		} 
		
		for (String zht_qcfl : zht_qcfl_list) 
		{
			if (zht_qcfl.indexOf(",") != -1) 
			{
				String[] splitStrs = zht_qcfl.split(",");
				if (group.getProperty("name").contains(splitStrs[1])) {
					carType = splitStrs[0];
					break;
				}
			}
		}
		return carType;
	}
	
	public static String getCarStructure(TCComponentBOMLine paramBOMLine, String paramType)
	{
		if (paramType.equals("Z9_VEHICLERevision")) {
			return "V";
		} 
		
		if (paramType.equals("Z9_StdPartRevision") && !paramBOMLine.hasChildren()) {
			return "S";
		} 
		
		if (paramBOMLine.hasChildren()) {
			return "A";
		} else {
			return "P";
		}
	}
	
	
	/*public static String getDrawingNo(TCComponentItemRevision paramItemRevision) throws TCException
	{
		StringBuilder stringBuilder = new StringBuilder();
		String itemId = paramItemRevision.getProperty("item_id");
		
		TCComponentItem drawingItem = null;
		TCComponent[] drawingItems = TcUtil.queryComponents(paramItemRevision.getSession(), "Item...", new String[] { "ItemID", "Type" }, new String[] { itemId + "*", "Z9_Drawing" });		
		if (drawingItems != null && drawingItems.length > 0)
		{
			List<TCComponent> list = Arrays.asList(drawingItems);
			if (list.size() > 1) 
			{
				Collections.sort(list, new Comparator<TCComponent>()
				{
					@Override
					public int compare(TCComponent o1, TCComponent o2) 
					{
						try {
							String itemId1 = o1.getProperty("item_id");
							String itemId2 = o2.getProperty("item_id");
							return itemId2.compareTo(itemId1);
						} catch (TCException e) {
							e.printStackTrace();
						}
						return 0;
					}
				});
			}
			
			drawingItem = (TCComponentItem) list.get(0);
		}
		
		if (drawingItem != null) 
		{
			TCComponentItemRevision[] releasedItemRevisions = drawingItem.getReleasedItemRevisions();
			if (releasedItemRevisions != null && releasedItemRevisions.length > 0) 
			{
				TCComponentItemRevision latestReleasedItemRevision = releasedItemRevisions[0];
				if (isAbandon(latestReleasedItemRevision)) {
					return "";
				}
				
				TCComponent[] components = latestReleasedItemRevision.getRelatedComponents();
				for (int i = 0; i < components.length; i++) 
				{
					if (components[i] instanceof TCComponentDataset)
					{
						TCComponentDataset dataset = (TCComponentDataset) components[i];
						TCComponentTcFile[] tcFiles = dataset.getTcFiles();
						if (tcFiles != null && tcFiles.length > 0) 
						{
							for (TCComponentTcFile tcFile : tcFiles) {
								String originalFileName = tcFile.getProperty("original_file_name");
								stringBuilder.append(originalFileName).append(";");
							}
						}
					}
				}
			}
		}
		
		String drawingNo = stringBuilder.toString();
		if (drawingNo.endsWith(";")) {
			drawingNo = drawingNo.substring(0, drawingNo.lastIndexOf(";"));
		}
		
		return drawingNo;
	}*/
	
	public static boolean isAbandon(TCComponentItemRevision paramReleasedItemRevision) throws TCException
	{
		TCComponent[] statuscComponents = paramReleasedItemRevision.getReferenceListProperty("release_status_list");
		if (statuscComponents != null && statuscComponents.length > 0)
		{
			for (TCComponent tcComponent : statuscComponents) {
				String statusName = tcComponent.getProperty("object_name");
				if (statusName.equals("Z9_Discard") || statusName.equals("D")) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static String getReplacePartId(TCComponentBOMLine paramBOMLine) throws TCException
	{
		StringBuilder stringBuilder = new StringBuilder();
		TCComponentBOMLine[] substitutesBOMLineArray = paramBOMLine.listSubstitutes();
		if (substitutesBOMLineArray != null && substitutesBOMLineArray.length > 0)
		{
			for (int i = 0; i < substitutesBOMLineArray.length; i++) 
			{
//				String itemId = substitutesBOMLineArray[i].getItem().getProperty("item_id");
//				if (i < substitutesBOMLineArray.length - 1) {
//					stringBuilder.append(itemId).append(";");
//				} else {
//					stringBuilder.append(itemId);
//				}
				
				// 2018-11-20，修改共图号取值，改为获取替换件的ModelName
				TCComponentItemRevision itemRev = substitutesBOMLineArray[i].getItemRevision();
				if (i < substitutesBOMLineArray.length - 1) {
					stringBuilder.append(getModelName(itemRev)).append(";");
				} else {
					stringBuilder.append(getModelName(itemRev));
				}
			}
		}
		
		return stringBuilder.toString();
	}

	
	// 以下方法于 2018-11-06 新增，by zhoutong
	/**
	 * 汇总二维数据集名称
	 * 
	 * @param paramItemRevision
	 * @return
	 * @throws TCException
	 */
	public static String getDrawingNo(TCComponentItemRevision paramItemRevision) throws TCException 
	{
		String drawingNo = "";
		List<TCComponent> drawingItems = getDrawingItems(paramItemRevision);
		if (drawingItems.size() > 0)
		{
			Iterator<TCComponent> iterator = drawingItems.iterator();
			while (drawingNo.equals("") && iterator.hasNext()) 
			{
				TCComponentItem drawingItem = (TCComponentItem) iterator.next();
				TCComponentItemRevision latestReleasedItemRevision = getLatestReleasedItemRevision(drawingItem);
				drawingNo = getDrawingNo2(latestReleasedItemRevision);
			}
		}
		
		return drawingNo;
	}
	
	
	/**
	 * 获取工程图对象
	 * <p> 按照ID排序，由大到小
	 * 
	 * @param paramItemRevision
	 * @return
	 * @throws TCException
	 */
	public static List<TCComponent> getDrawingItems(TCComponentItemRevision paramItemRevision) throws TCException
	{
		String itemId = paramItemRevision.getProperty("item_id");
		TCComponent[] drawingItems = TcUtil.queryComponents(paramItemRevision.getSession(), "Item...", new String[] { "ItemID", "Type" }, new String[] { itemId + "*", "Z9_Drawing" });		
		if (drawingItems != null && drawingItems.length > 0)
		{
			// 增加Id过滤，2018-11-13
			List<TCComponent> list = new ArrayList<TCComponent>();
			for (TCComponent drawingItem : drawingItems) 
			{
				String drawingItemId = drawingItem.getProperty("item_id");
				if (drawingItemId.length() > 8 
						&& drawingItemId.substring(0, drawingItemId.length() - 8).equals(itemId)) {
					list.add(drawingItem);
				}
			}
			
			if (list.size() > 1) 
			{
				Collections.sort(list, new Comparator<TCComponent>()
				{
					@Override
					public int compare(TCComponent o1, TCComponent o2) 
					{
						try {
							String itemId1 = o1.getProperty("item_id");
							String itemId2 = o2.getProperty("item_id");
							return itemId2.compareTo(itemId1);
						} catch (TCException e) {
							e.printStackTrace();
						}
						return 0;
					}
				});
			}
			
			return list;
		}
		
		return new ArrayList<TCComponent>();
	}
	
	/**
	 * 获取最新发布版本
	 * <p>废弃状态当作未发布处理
	 * 
	 * @param item
	 * @return
	 * @throws TCException
	 */
	public static TCComponentItemRevision getLatestReleasedItemRevision(TCComponentItem item) throws TCException
	{
		if (item == null) {
			return null;
		}
		
		TCComponentItemRevision[] releasedItemRevisions = item.getReleasedItemRevisions();
		if (releasedItemRevisions != null && releasedItemRevisions.length > 0) 
		{
			for (TCComponentItemRevision itemRevision : releasedItemRevisions) {
				if (!isAbandon(itemRevision)) {
					return itemRevision;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * 获取二维数据集名称
	 * 
	 * @param paramItemRevision
	 * @return
	 * @throws TCException
	 */
	public static String getDrawingNo2(TCComponentItemRevision paramItemRevision) throws TCException
	{
		if (paramItemRevision == null) {
			return "";
		}
		
		StringBuilder stringBuilder = new StringBuilder("");
		
		TCComponent[] components = paramItemRevision.getRelatedComponents();
		for (int i = 0; i < components.length; i++) 
		{
			if (components[i] instanceof TCComponentDataset)
			{
				TCComponentDataset dataset = (TCComponentDataset) components[i];
				
				// 增加dwg和catdrawing类型数据集，命名引用过滤，2018-11-13
				TCComponent dwgRefComponent = dataset.getNamedRefComponent("DWG");
				if (dwgRefComponent != null) {
					stringBuilder.append(dwgRefComponent.getProperty("object_string")).append(";");
					continue;
				}
				
				TCComponent catdrawingRefComponent = dataset.getNamedRefComponent("catdrawing");
				if (catdrawingRefComponent != null) {
					stringBuilder.append(catdrawingRefComponent.getProperty("object_string")).append(";");
					continue;
				}
				
				TCComponentTcFile[] tcFiles = dataset.getTcFiles();
				if (tcFiles != null && tcFiles.length > 0) 
				{
					for (TCComponentTcFile tcFile : tcFiles) {
						String originalFileName = tcFile.getProperty("original_file_name");
						stringBuilder.append(originalFileName).append(";");
					}
				}
			}
		}
		
		String drawingNo = stringBuilder.toString();
		if (drawingNo.endsWith(";")) {
			drawingNo = drawingNo.substring(0, drawingNo.lastIndexOf(";"));
		}
		
		return drawingNo;
	}
		
}
