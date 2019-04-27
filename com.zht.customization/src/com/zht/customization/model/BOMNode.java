package com.zht.customization.model;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.zht.customization.utils.SessionUtil;
import com.zht.report.utils.ReportUtil;

public class BOMNode {
	public TCComponentBOMLine bomline = null;
	public TCComponentBOMLine parentLine = null;
	public TCComponentItem item = null;
	public TCComponentItemRevision revision = null;

	public BOMNode parentNode = null;
	public List<BOMNode> children = new ArrayList<BOMNode>();
	public Map<String, Integer> quantityMap = new HashMap<String, Integer>();

	private static String[] bomProps = { "bl_sequence_no", "Z9_Structure_Feature", "bl_level_starting_0",
			"bl_quantity" };
	private static String[] itemRevProps = { "item_id", "item_revision_id", "object_name", "object_desc",
			"owning_group", "owning_user", "z9_IR_Techcode", "z9_IR_Material", "z9_IR_Weight", "last_release_status" };

	public String partID;
	public String level;
	public String partName;
	public String sequenceNo;
	public String techCode;
	public String material;
	public String weight;
	public String drawing;
	public String model;
	public String exchange;
	public String structureFeature;
	public String parent;
	public String engineer;
	public String depart;
	public String desc;
	public String substitutes;
	public String status = "";
	public String itemType = "";
	public String quantity = "1";

	public BOMNode() {

	}

	public BOMNode(TCComponentBOMLine bomline) {
		this.bomline = bomline;
		try {
			this.parentLine = bomline.parent();
			this.revision = bomline.getItemRevision();
			this.item = bomline.getItem();
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	public void initProps() {
		try {
			this.itemType = this.item.getProperty("object_type");
			TCProperty[] bomPropValues = this.bomline.getTCProperties(bomProps);
			this.sequenceNo = NullToString(bomPropValues[0]);
			if (!this.sequenceNo.equals("")) {
				int parseInt = Integer.parseInt(this.sequenceNo);
				DecimalFormat decimalFormat = new DecimalFormat("0000");
				this.sequenceNo = decimalFormat.format(parseInt);
			}

			this.structureFeature = NullToString(bomPropValues[1]);
			this.quantity = NullToString(bomPropValues[3]);
			this.quantity = quantity.equals("") ? "1" : quantity;
			// if (this.structureFeature.equals("F"))
			// this.structureFeature = "数量为1";
			// else if (this.structureFeature.equals("-"))
			// this.structureFeature = "数量为-1";
			if (bomPropValues[2] == null)
				this.level = null;
			else
				this.level = bomPropValues[2].getDisplayableValue();

			TCProperty[] itemRevPropValues = this.revision.getTCProperties(itemRevProps);
			this.status = itemRevPropValues[itemRevProps.length - 1].getDisplayableValue();
			this.status = this.status == null ? "" : this.status;
			this.partID = NullToString(itemRevPropValues[0]);
			this.partName = NullToString(itemRevPropValues[2]);
			String revNo = itemRevPropValues[1].getDisplayableValue();
			// this.model = this.partID + "_" + revNo;

			TCComponent[] specification_components = this.revision.getRelatedComponents("IMAN_specification");
			TCComponentDataset catDataset = null;

			for (TCComponent component : specification_components) {
				String type = component.getType();
				if (type.equalsIgnoreCase("CATPart") || type.equalsIgnoreCase("CATProduct")) {
					catDataset = (TCComponentDataset) component;
					break;
				}
			}

			if (catDataset != null) {
				TCComponent namedRefComponent = catDataset.getNamedRefComponent("catproduct");
				if (namedRefComponent == null)
					namedRefComponent = catDataset.getNamedRefComponent("catpart");
				if (namedRefComponent != null)
					this.model = namedRefComponent.getProperty("object_string");
			}

			this.desc = itemRevPropValues[3].getDisplayableValue();
			this.depart = itemRevPropValues[4].getReferenceValue().getProperty("description");
			this.engineer = itemRevPropValues[5].getReferenceValue().getProperty("user_name");
			this.techCode = NullToString(itemRevPropValues[6]);
			if (!this.revision.getType().equals("Z9_VEHICLERevision"))
				this.material = NullToString(itemRevPropValues[7]);
			this.weight = NullToString(itemRevPropValues[8]);
			
			// 修改2D数据 获取，modified by zhoutong, 2018-11-13
			this.drawing = ReportUtil.getDrawingNo(this.revision);
			
			/*TCComponentItemType z9PartType = (TCComponentItemType) SessionUtil.GetSession()
					.getTypeComponent("Z9_Drawing");
			TCComponentItem[] dwgItems = z9PartType.findItems(this.partID + "_*-DWG");
			if (dwgItems.length > 0) {
				TCComponentItem dwgItem = null;
				TCComponentItemRevision latestReleasedRevision = null;
				int maxRevId = 0;
				for (TCComponentItem tcComponentItem : dwgItems) {
					String itemID = tcComponentItem.getProperty("item_id");
					// System.out.println(itemID);
					itemID = itemID.replace(this.partID + "_", "");
					// System.out.println(itemID);
					String revId = itemID.replace("-DWG", "");
					if (revId.contains("dwg"))
						revId = itemID.replace("-dwg", "");
					// System.out.println(revId);
					int parseInt = Integer.parseInt(revId);
					if (parseInt > maxRevId) {
						maxRevId = parseInt;
						// System.out.println(maxRevId);
						dwgItem = tcComponentItem;
					}
				}
				if (dwgItem != null) {
					TCComponentItemRevision[] releasedItemRevisions = dwgItem.getReleasedItemRevisions();
					int maxRev = 0;
					for (TCComponentItemRevision localRevision : releasedItemRevisions) {
						String revID = localRevision.getProperty("item_revision_id");
						int parseInt = Integer.parseInt(revID);
						// System.out.println(status + "--" + revID);
						if (parseInt > maxRev) {
							maxRev = parseInt;
							latestReleasedRevision = localRevision;
						}
					}
				}*/
//				if (latestReleasedRevision != null) {
//					StringBuilder drawingSb = new StringBuilder();
//					String status = latestReleasedRevision.getProperty("last_release_status");
//					if (!status.equals("D")) 
//					{
						/*
						TCComponent[] relatedComponents = latestReleasedRevision.getRelatedComponents();
						for (TCComponent tcComponent : relatedComponents) {
							if (tcComponent instanceof TCComponentDataset) {
								TCComponentDataset dataset = (TCComponentDataset) tcComponent;
								TCComponent dwgRefComponent = dataset.getNamedRefComponent("DWG");
								TCComponent catdrawingRefComponent = dataset.getNamedRefComponent("catdrawing");
								if (dwgRefComponent != null)
									drawingSb.append(dwgRefComponent.getProperty("object_string") + ";");
								if (catdrawingRefComponent != null)
									drawingSb.append(catdrawingRefComponent.getProperty("object_string") + ";");
								
								// TCComponent[] namedReferences =
								// dataset.getNamedReferences();
								// for (TCComponent tcComponent2 :
								// namedReferences) {
								// drawingSb.append(tcComponent2.getProperty("object_string")
								// + ";");
								// // System.out.println(drawingSb.toString());
								// }
							}
						}
						*/
//					}
//					this.drawing = drawingSb.toString();
					// 修改二维数据集汇总，modifid by zhotuong, 20180926
//					this.drawing = getDrawingNo(latestReleasedRevision);
//				}

				// TCProperty revs = dwgItems[0].getTCProperty("revision_list");
				// TCComponent[] revList = revs.getReferenceValueArray();
				// for (TCComponent rev : revList) {
				// String dwgRevNo = rev.getProperty("item_revision_id");
				// if (dwgRevNo.equals(revNo))
				// this.drawing = this.partID + "_" + dwgRevNo + "-DWG/" +
				// dwgRevNo;
				// }
//			}
			/*TCComponentBOMLine[] substitutes = bomline.listSubstitutes();
			StringBuilder sb = new StringBuilder();
			for (TCComponentBOMLine sub : substitutes) {
				String[] properties = sub.getItemRevision()
						.getProperties(new String[] { "item_id", "item_revision_id" });
				sb.append(properties[0] + "_" + properties[1] + ";");
			}
			this.substitutes = sb.toString();*/
			
			// 修改替换号获取，modified by zhoutong, 2018-11-20
			this.substitutes = ReportUtil.getReplacePartId(this.bomline);
			this.parent = this.parentLine == null ? "" : this.parentLine.getItem().getProperty("item_id");
		} catch (TCException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取二维数据集汇总
	 * 
	 * @param itemRevision
	 * @return
	 * @throws TCException
	 * @author zhoutong
	 */
	/*private String getDrawingNo(TCComponentItemRevision itemRevision) throws TCException
	{
		StringBuilder stringBuilder = new StringBuilder();
		TCComponent[] components = itemRevision.getRelatedComponents();
		for (int i = 0; i < components.length; i++) 
		{
			if (components[i] instanceof TCComponentDataset)
			{
				TCComponentDataset dataset = (TCComponentDataset) components[i];
				
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
					for (TCComponentTcFile tcFile : tcFiles) 
					{
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
	}*/
	

	public Integer getQuantity(String sosName) {
		return quantityMap.get(sosName);
	}

	public void setQuantity(String sosName, int quantity) {
		quantityMap.put(sosName, quantity);
	}

	private String NullToString(TCProperty prop) {
		return prop == null ? "" : prop.getDisplayableValue();
	}

	public void addChild(BOMNode node) {
		this.children.add(node);
	}

	@Override
	public String toString() {
		return "BOMNode [partID=" + partID + ", itemType=" + itemType + ", level=" + level + ", partName=" + partName
				+ ", sequenceNo=" + sequenceNo + ", techCode=" + techCode + ", material=" + material + ", weight="
				+ weight + ", drawing=" + drawing + ", model=" + model + ", exchange=" + exchange
				+ ", structureFeature=" + structureFeature + ", parent=" + parent + ", engineer=" + engineer
				+ ", depart=" + depart + ", desc=" + desc + ", substitutes=" + substitutes + ", status=" + status + "]";
	}
}
