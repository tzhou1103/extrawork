package com.zht.customization.model;

import java.text.DecimalFormat;
import java.util.Objects;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.zht.customization.impl.Model;
import com.zht.customization.utils.SessionUtil;

public class ModelObject extends Model {
	private TCComponentBOMLine bomLine;
	private TCComponentBOMLine parentLine;
	private TCComponentItem item;
	private TCComponentItemRevision itemRevision;
	// public static String rule;
	// private Map<String, Integer> quantities = new HashMap<String, Integer>();

	private String partID;
	private String level;
	private String partName;
	private String sequenceNo;
	private String techCode;
	private String material;
	private String weight;
	private String drawing;
	private String model;
	private String exchange;
	private String structureFeature;
	private String parent;
	private String engineer;
	private String depart;
	private String desc;
	private String substitutes;
	public String status = "";
	public String itemType = "";

	// private static String[] itemProps = {"object_desc"};
	private static String[] bomProps = { "bl_sequence_no", "Z9_Structure_Feature", "bl_level_starting_0" };
	private static String[] itemRevProps = { "item_id", "item_revision_id", "object_name", "object_desc",
			"owning_group", "owning_user", "z9_IR_Techcode", "z9_IR_Material", "z9_IR_Weight", "last_release_status" };

	// private static String[] userProps = { "user_name" };
	// private static String[] groupProps = { "desription" };

	private ModelObject(TCComponentBOMLine bomLine) {
		this.bomLine = bomLine;
		try {
			this.parentLine = bomLine.parent();
			this.itemRevision = bomLine.getItemRevision();
			this.item = bomLine.getItem();
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	public ModelObject() {
	}

	public static ModelObject GetInstance(TCComponentBOMLine bomLine) {
		return bomLine == null ? null : new ModelObject(bomLine);
	}

	public void excuteData() {
		try {
			this.itemType = this.item.getProperty("object_type");
			TCProperty[] bomPropValues = this.bomLine.getTCProperties(bomProps);
			this.sequenceNo = NullToString(bomPropValues[0]);
			if (!this.sequenceNo.equals("")) {
				int parseInt = Integer.parseInt(this.sequenceNo);
				DecimalFormat decimalFormat = new DecimalFormat("0000");
				this.sequenceNo = decimalFormat.format(parseInt);
			}

			this.structureFeature = NullToString(bomPropValues[1]);
			// if (this.structureFeature.equals("F"))
			// this.structureFeature = "数量为1";
			// else if (this.structureFeature.equals("-"))
			// this.structureFeature = "数量为-1";
			if (bomPropValues[2] == null)
				this.level = null;
			else
				this.level = bomPropValues[2].getDisplayableValue();

			TCProperty[] itemRevPropValues = this.itemRevision.getTCProperties(itemRevProps);
			this.status = itemRevPropValues[itemRevProps.length - 1].getDisplayableValue();
			this.status = this.status == null ? "" : this.status;
			this.partID = NullToString(itemRevPropValues[0]);
			this.partName = NullToString(itemRevPropValues[2]);
			String revNo = itemRevPropValues[1].getDisplayableValue();
			// this.model = this.partID + "_" + revNo;

			TCComponent[] specification_components = this.itemRevision.getRelatedComponents("IMAN_specification");
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
				this.model = namedRefComponent.getProperty("object_string");
			}

			this.desc = itemRevPropValues[3].getDisplayableValue();
			this.depart = itemRevPropValues[4].getReferenceValue().getProperty("description");
			this.engineer = itemRevPropValues[5].getReferenceValue().getProperty("user_name");
			this.techCode = NullToString(itemRevPropValues[6]);
			if (!this.itemRevision.getType().equals("Z9_VEHICLERevision"))
				this.material = NullToString(itemRevPropValues[7]);
			this.weight = NullToString(itemRevPropValues[8]);
			TCComponentItemType z9PartType = (TCComponentItemType) SessionUtil.GetSession()
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
				}
				if (latestReleasedRevision != null) {
					StringBuilder drawingSb = new StringBuilder();
					String status = latestReleasedRevision.getProperty("last_release_status");
					if (!status.equals("D")) {
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
								//// System.out.println(drawingSb.toString());
								// }
							}
						}
					}
					this.drawing = drawingSb.toString();
				}

				// TCProperty revs = dwgItems[0].getTCProperty("revision_list");
				// TCComponent[] revList = revs.getReferenceValueArray();
				// for (TCComponent rev : revList) {
				// String dwgRevNo = rev.getProperty("item_revision_id");
				// if (dwgRevNo.equals(revNo))
				// this.drawing = this.partID + "_" + dwgRevNo + "-DWG/" +
				// dwgRevNo;
				// }
			}
			TCComponentBOMLine[] substitutes = bomLine.listSubstitutes();
			StringBuilder sb = new StringBuilder();
			for (TCComponentBOMLine sub : substitutes) {
				String[] properties = sub.getItemRevision()
						.getProperties(new String[] { "item_id", "item_revision_id" });
				sb.append(properties[0] + "_" + properties[1] + ";");
			}
			this.substitutes = sb.toString();
			this.parent = this.parentLine == null ? "" : this.parentLine.getItem().getProperty("item_id");
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	public String getQuantity() {
		String quantity = "1";
		try {
			quantity = bomLine.getProperty("bl_quantity");
			quantity = quantity.equals("") ? "1" : quantity;
		} catch (TCException e) {
			e.printStackTrace();
		}
		return quantity;
	}

	public static String getQuantity(TCComponentBOMLine bomline) {
		String quantity = "1";
		try {
			quantity = bomline.getProperty("bl_quantity");
			quantity = quantity.equals("") ? "1" : quantity;
		} catch (TCException e) {
			e.printStackTrace();
		}
		return quantity;
	}

	public void outPut2File() {
	}

	@Override
	public String toString() {
		return "ModelObject [partID=" + partID + ", itemType=" + itemType + ", level=" + level + ", partName="
				+ partName + ", sequenceNo=" + sequenceNo + ", techCode=" + techCode + ", material=" + material
				+ ", weight=" + weight + ", drawing=" + drawing + ", model=" + model + ", exchange=" + exchange
				+ ", structureFeature=" + structureFeature + ", parent=" + parent + ", engineer=" + engineer
				+ ", depart=" + depart + ", desc=" + desc + ", substitutes=" + substitutes + ", status=" + status + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ModelObject modelObject = (ModelObject) o;
		return Objects.equals(this.bomLine, modelObject.bomLine)
				&& Objects.equals(this.sequenceNo, modelObject.sequenceNo)
				&& Objects.equals(this.level, modelObject.level)
				&& Objects.equals(this.parentLine, modelObject.parentLine);
	}

	private String NullToString(TCProperty prop) {
		return prop == null ? "" : prop.getDisplayableValue();
	}

	public String getLevel() {
		return level;
	}

	public TCComponentItem getItem() {
		return item;
	}

	public String getPartID() {
		return partID;
	}

	public String getPartName() {
		return partName;
	}

	public String getSequenceNo() {
		return sequenceNo;
	}

	public String getTechCode() {
		return techCode;
	}

	public String getMaterial() {
		return material;
	}

	public String getWeight() {
		return weight;
	}

	public String getDrawing() {
		return drawing;
	}

	public String getModel() {
		return model;
	}

	public String getExchange() {
		return exchange;
	}

	public String getStructureFeature() {
		return structureFeature;
	}

	public String getParent() {
		return parent;
	}

	public String getEngineer() {
		return engineer;
	}

	public String getDepart() {
		return depart;
	}

	public String getDesc() {
		return desc;
	}

	public String getSubstitutes() {
		return substitutes;
	}

	public TCComponentItemRevision getItemRevision() {
		return itemRevision;
	}

	public TCComponentBOMLine getBomLine() {
		return bomLine;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}
}
