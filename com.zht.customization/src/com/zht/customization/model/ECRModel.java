package com.zht.customization.model;

import java.util.Iterator;
import java.util.Set;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.zht.customization.impl.Model;
import com.zht.customization.utils.SessionUtil;

public class ECRModel extends Model {

	private TCComponentBOMLine bomLine = null;
	private TCComponentBOMLine parentLine = null;
	private TCComponentItemRevision itemRev = null;
	private TCComponentItem item = null;
	private String parent = "";
	private String location = "";
	private String quantity = "";
	private String structureFeature = "";
	private String substitutes = "";
	private String child = "";
	private String techCode = "";
	private String childName = "";
	private String unit = "";
	private String material = "";
	private String weight = "";
	private String desc = "";
	private String drawing = "";
	private String model = "";
	private String structure = "";
	private String classification = "";
	private String carStatus = "";
	private String inventory = "";
	private String materialstatus = "";
	private String groupNo = "";
	private String resp = "";
	private String engineer = "";
	private String depart = "";
	private String add = "";

	private boolean isValid = false;

	private static String[] bomProps = { "bl_sequence_no", "Z9_Structure_Feature", "bl_quantity" };
	private static String[] itemRevProps = { "item_id", "item_revision_id", "object_name", "object_desc",
			"owning_group", "owning_user", "z9_IR_Techcode", "z9_IR_Material", "z9_IR_Weight", "z9_IR_Unit",
			"release_status_list", "owning_user", "z9_IR_Group", "owning_group", "z9_IR_Car_status", "z9_IR_Inventory",
			"z9_IR_Materialstatus" };

	private ECRModel(TCComponentBOMLine bomLine) {
		this.bomLine = bomLine;
		try {
			this.parentLine = bomLine.parent();
			this.itemRev = bomLine.getItemRevision();
			this.item = bomLine.getItem();
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	private ECRModel(TCComponentItemRevision itemRev) {
		this.itemRev = itemRev;
		try {
			this.item = itemRev.getItem();
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	public static ECRModel GetInstance(TCComponentBOMLine bomLine) {
		return bomLine == null ? null : new ECRModel(bomLine);
	}

	public static ECRModel GetInstance(TCComponentItemRevision itemRev) {
		return itemRev == null ? null : new ECRModel(itemRev);
	}

	public boolean getCheck() {
		return isValid;
	}

	public void excuteData() throws TCException {
		getBOMProps();

		TCProperty[] itemRevPropValues = this.itemRev.getTCProperties(itemRevProps);
		/***
		 * >0
		 */
		this.isValid = itemRevPropValues[10].getModelObjectArrayValue().length > 0;

		this.child = itemRevPropValues[0].getDisplayableValue();
		this.childName = itemRevPropValues[2].getDisplayableValue();
		this.techCode = itemRevPropValues[6].getDisplayableValue();
		this.unit = itemRevPropValues[9].getDisplayableValue();
		this.weight = itemRevPropValues[8].getDisplayableValue();
		this.material = itemRevPropValues[7].getDisplayableValue();
		this.desc = itemRevPropValues[3].getDisplayableValue();

		TCComponentItemType z9PartType = (TCComponentItemType) SessionUtil.GetSession().getTypeComponent("Z9_Drawing");
		TCComponentItem[] dwgItems = z9PartType.findItems(this.child + "_*-DWG");
		if (dwgItems.length > 0) {
			TCComponentItem dwgItem = null;
			TCComponentItemRevision latestReleasedRevision = null;
			int maxRevId = 0;
			for (TCComponentItem tcComponentItem : dwgItems) {
				String itemID = tcComponentItem.getProperty("item_id");
				// System.out.println(itemID);
				itemID = itemID.replace(this.child + "_", "");
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
							TCComponent[] namedReferences = dataset.getNamedReferences();
							for (TCComponent tcComponent2 : namedReferences) {
								drawingSb.append(tcComponent2.getProperty("object_string") + ";");
								// System.out.println(drawingSb.toString());
							}
						}
					}
				}
				this.drawing = drawingSb.toString();
			}
		}

		TCComponent[] specification_components = this.itemRev.getRelatedComponents("IMAN_specification");
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

		String type = this.item.getType();
		if (type.equalsIgnoreCase("Z9_VEHICLE")) {
			this.structure = "V";
		} else if (type.equalsIgnoreCase("Z9_StdPart")) {
			this.structure = "S";
		} else if (type.equalsIgnoreCase("Z9_System") || type.equalsIgnoreCase("Z9_Part")) {
			if (this.itemRev.getTCProperty("structure_revisions").getModelObjectArrayValue().length == 0)
				this.structure = "P";
		}
		if (!type.equalsIgnoreCase("Z9_StdPart") && !type.equalsIgnoreCase("Z9_VEHICLE")) {
			if (this.itemRev.getTCProperty("structure_revisions").getModelObjectArrayValue().length > 0)
				this.structure = "A";
		}

		this.groupNo = itemRevPropValues[12].getDisplayableValue();
		this.depart = itemRevPropValues[4].getReferenceValue().getProperty("description");
		this.engineer = itemRevPropValues[5].getReferenceValue().getProperty("user_name");
		this.add = "";
		this.resp = itemRevPropValues[11].getReferenceValue().getTCProperty("person").getReferenceValue()
				.getProperty("PA6");
		TCComponentGroup group = (TCComponentGroup) itemRevPropValues[13].getReferenceValue();
		String groupName = group.getGroupName();
		SessionUtil.getQCList();
		Set<String> keySet = SessionUtil.pattern.keySet();
		Iterator<String> iterator = keySet.iterator();
		while (iterator.hasNext()) {
			String next = iterator.next();
			if (groupName.contains(next)) {
				classification = SessionUtil.pattern.get(next);
				break;
			}
		}
		carStatus = itemRevPropValues[14].getDisplayableValue();
		inventory = itemRevPropValues[15].getDisplayableValue();
		materialstatus = itemRevPropValues[16].getDisplayableValue();
		// classification="P";
	}

	private void getBOMProps() throws TCException {
		if (this.parentLine == null)
			return;
		this.parent = this.parentLine == null ? "" : this.parentLine.getItem().getProperty("item_id");
		TCProperty[] bomPropValues = this.bomLine.getTCProperties(bomProps);
		this.location = bomPropValues[0].getDisplayableValue();
		this.structureFeature = bomPropValues[1].getDisplayableValue();
		this.quantity = bomPropValues[2].getDisplayableValue().equals("") ? "1"
				: bomPropValues[2].getDisplayableValue();
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 == null)
			return false;
		ECRModel model = (ECRModel) arg0;
		return this.parent.equals(model.parent) && this.child.equals(model.child)
				&& this.childName.equals(model.childName) && this.location.equals(model.location);
		// && this.classification.equals(model.classification)
		// && this.depart.equals(model.depart)
		// && this.desc.equals(model.desc)
		// && this.drawing.equals(model.drawing)
		// && this.engineer.equals(model.engineer)
		// && this.groupNo.equals(model.groupNo)
		// && this.material.equals(model.material)
		// && this.model.equals(model.model)
		// && this.quantity.equals(model.quantity)
		// && this.resp.equals(model.resp)
		// && this.structure.equals(model.structure)
		// && this.structureFeature.equals(model.structureFeature)
		// && this.substitutes.equals(model.substitutes)
		// && this.techCode.equals(model.techCode)
		// && this.unit.equals(model.unit)
		// && this.weight.equals(model.weight);
	}

	public boolean isModified(ECRModel model) {
		return !this.classification.equals(model.classification) || !this.depart.equals(model.depart)
				|| !this.desc.equals(model.desc) || !this.drawing.equals(model.drawing)
				|| !this.engineer.equals(model.engineer) || !this.groupNo.equals(model.groupNo)
				|| !this.material.equals(model.material) || !this.model.equals(model.model)
				|| !this.quantity.equals(model.quantity) || !this.resp.equals(model.resp)
				|| !this.structure.equals(model.structure) || !this.structureFeature.equals(model.structureFeature)
				|| !this.substitutes.equals(model.substitutes) || !this.techCode.equals(model.techCode)
				|| !this.unit.equals(model.unit) || !this.weight.equals(model.weight);
	}

	public void setAdd(String add) {
		this.add = add;
	}

	public TCComponentBOMLine getBomLine() {
		return bomLine;
	}

	public TCComponentItemRevision getItemRev() {
		return itemRev;
	}

	public TCComponentItem getItem() {
		return item;
	}

	public String getParent() {
		return parent;
	}

	public String getLocation() {
		return location;
	}

	public String getQuantity() {
		return quantity;
	}

	public String getStructureFeature() {
		return structureFeature;
	}

	public String getSubstitutes() {
		return substitutes;
	}

	public String getChild() {
		return child;
	}

	public String getTechCode() {
		return techCode;
	}

	public String getChildName() {
		return childName;
	}

	public String getUnit() {
		return unit;
	}

	public String getMaterial() {
		return material;
	}

	public String getWeight() {
		return weight;
	}

	public String getDesc() {
		return desc;
	}

	public String getDrawing() {
		return drawing;
	}

	public String getModel() {
		return model;
	}

	public String getStructure() {
		return structure;
	}

	public String getClassification() {
		return classification;
	}

	public String getGroupNo() {
		return groupNo;
	}

	public String getResp() {
		return resp;
	}

	public String getEngineer() {
		return engineer;
	}

	public String getDepart() {
		return depart;
	}

	public String getAdd() {
		return add;
	}

	public String getCarStatus() {
		return carStatus;
	}

	public void setCarStatus(String carStatus) {
		this.carStatus = carStatus;
	}

	public String getInventory() {
		return inventory;
	}

	public void setInventory(String inventory) {
		this.inventory = inventory;
	}

	public String getMaterialstatus() {
		return materialstatus;
	}

	public void setMaterialstatus(String materialstatus) {
		this.materialstatus = materialstatus;
	}

	public static void main(String[] args) {
	}

}
