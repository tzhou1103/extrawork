package com.zht.customization.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCComponentRevisionRuleType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.pse.AbstractPSEApplication;
import com.zht.customization.exception.StopMsgException;
import com.zht.customization.impl.Model;
import com.zht.customization.manager.ModelManager;
import com.zht.customization.model.BOMNode;
import com.zht.customization.model.ECRModel;
import com.zht.customization.model.ModelObject;

public class BOMUtil {
	private static TCComponentBOMWindow bomWindow;
	private static TCComponent[] sosList;
	public static Vector<String> invalidItem = new Vector<String>();
	public static List<String> typeList = new ArrayList<String>();

	public static List<String> GetTypeList() {
		if (typeList == null || typeList.isEmpty()) {
			String[] preference = SessionUtil.getPreference("Z9_N_Release_type_bom");
			typeList = Arrays.asList(preference);
		}
		return typeList;
	}

	public static boolean askParent(List<TCComponent> itemRevVec, List<TCComponent> targetItemRevVec,
			Vector<TCComponentBOMLine> bomLinesVec) {
		boolean flag = false;
		int index = 0;
		for (TCComponent itemRev : itemRevVec) {
			try {
				TCComponent[] whereUsed = itemRev.whereUsed(TCComponent.WHERE_USED_ALL);
				for (TCComponent tccomp : whereUsed) {
					System.out.println("the tccomponent puid is " + tccomp.getUid());
					if ((flag = targetItemRevVec.contains(tccomp) && !itemRev.equals(tccomp))) {
						bomLinesVec.remove(index);
						return flag;
					}
				}
				if (flag = askParent(Arrays.asList(whereUsed), targetItemRevVec, bomLinesVec))
					break;
			} catch (TCException e) {
				e.printStackTrace();
			}
			index++;
		}
		return flag;
	}

	public static TCComponentBOMWindow GetBOMWindow() {
		return bomWindow == null ? ((AbstractPSEApplication) SessionUtil.GetApplication()).getBOMWindow() : bomWindow;
	}

	public static TCComponent[] GetSOSList() {
		try {
			sosList = GetBOMWindow().getTopBOMLine().getItemRevision().getStoredOptionSets(null);
		} catch (TCException e) {
			e.printStackTrace();
		}
		return sosList;
	}

	public static void loopBOM(TCComponentBOMLine bomLine, BOMNode parentNode) throws StopMsgException {
		BOMNode bomNode = new BOMNode(bomLine);
		bomNode.parentNode = parentNode;
		if (parentNode != null)
			parentNode.children.add(bomNode);
		System.out.println("itemRevision:" + bomNode.revision);
		if (bomNode.revision != null) {
			bomNode.initProps();
			System.out.println(bomNode.toString());
			if (bomNode.status.equals("")) {
				// System.out.println("status:" + modelObject.status);
				if (!GetTypeList().contains(bomNode.itemType)) {
					if (!BOMUtil.invalidItem.contains(bomNode.partID))
						BOMUtil.invalidItem.add(bomNode.partID + "未发布\r\n");
					// System.out.println("invalidItem:" +
					// modelObject.getPartID());
				}
			} else if (bomNode.status.equals("D")) {
				// System.out.println("status:" + modelObject.status);
				if (!GetTypeList().contains(bomNode.itemType)) {
					if (!BOMUtil.invalidItem.contains(bomNode.partID))
						BOMUtil.invalidItem.add(bomNode.partID + "废止\r\n");
					// System.out.println("invalidItem:" +
					// modelObject.getPartID());
				}
			}
			try {
				AIFComponentContext[] children = bomLine.getChildren();
				for (AIFComponentContext child : children) {
					TCComponentBOMLine childComp = (TCComponentBOMLine) child.getComponent();
					loopBOM(childComp, bomNode);
				}
			} catch (TCException e) {
				e.printStackTrace();
			}
		} else {
			AbstractAIFUIApplication application = SessionUtil.GetApplication();
			AbstractPSEApplication pse = (AbstractPSEApplication) application;
			pse.getBOMTreeTable().addSelectedBOMLine(bomLine);
			throw new StopMsgException();
		}
	}

	public static void loopBOM2(TCComponentBOMLine bomLine, BOMNode parentNode) {
		BOMNode bomNode = new BOMNode(bomLine);
		bomNode.parentNode = parentNode;
		if (parentNode != null)
			parentNode.children.add(bomNode);
		System.out.println("itemRevision:" + bomNode.revision);
		if (bomNode.revision != null) {
			bomNode.initProps();
			String quantity = ModelObject.getQuantity(bomLine);
			int parseInt = Integer.parseInt(quantity);
			bomNode.setQuantity(ModelManager.ruleName, parseInt);
			System.out.println(bomNode.toString());
			if (bomNode.status.equals("")) {
				// System.out.println("status:" + modelObject.status);
				if (!GetTypeList().contains(bomNode.itemType)) {
					if (!BOMUtil.invalidItem.contains(bomNode.partID))
						BOMUtil.invalidItem.add(bomNode.partID + "未发布\r\n");
					// System.out.println("invalidItem:" +
					// modelObject.getPartID());
				}
			} else if (bomNode.status.equals("D")) {
				// System.out.println("status:" + modelObject.status);
				if (!GetTypeList().contains(bomNode.itemType)) {
					if (!BOMUtil.invalidItem.contains(bomNode.partID))
						BOMUtil.invalidItem.add(bomNode.partID + "废止\r\n");
					// System.out.println("invalidItem:" +
					// modelObject.getPartID());
				}
			}
			try {
				AIFComponentContext[] children = bomLine.getChildren();
				for (AIFComponentContext child : children) {
					TCComponentBOMLine childComp = (TCComponentBOMLine) child.getComponent();
					loopBOM2(childComp, bomNode);
				}
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
	}

	
	public static String[] bomProps = { "bl_sequence_no", "bl_level_starting_0",
	"bl_quantity" };
	
	public static void LoopBOM2(TCComponentBOMLine bomLine, List<BOMNode> childs) throws StopMsgException {
		BOMNode node = null;
		for (BOMNode bomNode : childs) {
//			System.out.println("compare");
//			if (Objects.equals(bomNode.bomline, bomLine))
//				node = bomNode;
			try {
				TCProperty[] bomPropValues = bomLine.getTCProperties(bomProps);
				String sequenceNo = NullToString(bomPropValues[0]);
				if (!sequenceNo.equals("")) {
					int parseInt = Integer.parseInt(sequenceNo);
					DecimalFormat decimalFormat = new DecimalFormat("0000");
					sequenceNo = decimalFormat.format(parseInt);
				}
				String level = "";
				if (bomPropValues[1] == null)
					level = "";
				else
					level = bomPropValues[1].getDisplayableValue();
				
				if(level.equals(bomNode.level)&&sequenceNo.equals(bomNode.sequenceNo)&&bomLine.getItemRevision().equals(bomNode.revision)){
					node = bomNode;
				}
				
			} catch (TCException e) {
				e.printStackTrace();
			}
			
//			if(bomNode.sequenceNo)
		}
		if (node != null) {
			String quantity = ModelObject.getQuantity(bomLine);
			int parseInt = Integer.parseInt(quantity);
			node.setQuantity(ModelManager.ruleName, parseInt);

			if (node.revision == null) {
				AbstractAIFUIApplication application = SessionUtil.GetApplication();
				AbstractPSEApplication pse = (AbstractPSEApplication) application;
				pse.getBOMTreeTable().addSelectedBOMLine(bomLine);
				throw new StopMsgException();
			}
			System.out.println(node.toString());
			if (node.status.equals("")) {
				// System.out.println("status:" + modelObject.status);
				if (!GetTypeList().contains(node.itemType)) {
					if (!BOMUtil.invalidItem.contains(node.partID))
						BOMUtil.invalidItem.add(node.partID + "未发布\r\n");
					// System.out.println("invalidItem:" +
					// modelObject.getPartID());
				}
			} else if (node.status.equals("D")) {
				// System.out.println("status:" + modelObject.status);d
				if (!GetTypeList().contains(node.itemType)) {
					if (!BOMUtil.invalidItem.contains(node.partID))
						BOMUtil.invalidItem.add(node.partID + "废止\r\n");
					// System.out.println("invalidItem:" +
					// modelObject.getPartID());
				}
			}
			//
			// if (modelObject.getLevel() != null)
			// ModelManager.excuteQuantity(modelObject);
			try {
				AIFComponentContext[] children = bomLine.getChildren();
				for (AIFComponentContext child : children) {
					TCComponentBOMLine childComp = (TCComponentBOMLine) child.getComponent();
					LoopBOM2(childComp, node.children);
				}
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

	}

	public static void LoopBOM(TCComponentBOMLine bomLine) throws StopMsgException {
		ModelObject modelObject = ModelObject.GetInstance(bomLine);
		TCComponentItemRevision itemRevision = modelObject.getItemRevision();
		System.out.println("itemRevision:" + itemRevision);
		if (itemRevision == null) {
			AbstractAIFUIApplication application = SessionUtil.GetApplication();
			AbstractPSEApplication pse = (AbstractPSEApplication) application;
			pse.getBOMTreeTable().addSelectedBOMLine(bomLine);
			throw new StopMsgException();
		}
		modelObject.excuteData();
		System.out.println(modelObject.toString());
		if (modelObject.status.equals("")) {
			// System.out.println("status:" + modelObject.status);
			if (!GetTypeList().contains(modelObject.itemType)) {
				if (!BOMUtil.invalidItem.contains(modelObject.getPartID()))
					BOMUtil.invalidItem.add(modelObject.getPartID() + "未发布\r\n");
				// System.out.println("invalidItem:" + modelObject.getPartID());
			}
		} else if (modelObject.status.equals("D")) {
			// System.out.println("status:" + modelObject.status);
			if (!GetTypeList().contains(modelObject.itemType)) {
				if (!BOMUtil.invalidItem.contains(modelObject.getPartID()))
					BOMUtil.invalidItem.add(modelObject.getPartID() + "废止\r\n");
				// System.out.println("invalidItem:" + modelObject.getPartID());
			}
		}

		//
		// if (modelObject.getLevel() != null)
		// ModelManager.excuteQuantity(modelObject);
		try {
			AIFComponentContext[] children = bomLine.getChildren();
			for (AIFComponentContext child : children) {
				TCComponentBOMLine childComp = (TCComponentBOMLine) child.getComponent();
				LoopBOM(childComp);
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	public static void getECRBOMData(TCComponentBOMLine bomLine, List<Model> modelList) {
		ECRModel instance = ECRModel.GetInstance(bomLine);
		try {
			instance.excuteData();
		} catch (TCException e1) {
			e1.printStackTrace();
		}
		if (SessionUtil.GetCommand().equals("向ERP系统传递新增信息") && !instance.getCheck()) {
			invalidItem.add(instance.getChild());
		}
		try {
			AIFComponentContext[] children = bomLine.getChildren();
			for (AIFComponentContext child : children) {
				TCComponentBOMLine childBOMLine = (TCComponentBOMLine) child.getComponent();
				ECRModel childInstance = ECRModel.GetInstance(childBOMLine);
				childInstance.excuteData();
				if (SessionUtil.GetCommand().equals("向ERP系统传递新增信息"))
					childInstance.setAdd("A");
				if (SessionUtil.GetCommand().equals("向ERP系统传递新增信息") && !childInstance.getCheck()) {
					invalidItem.add(childInstance.getChild());
				} else {
					modelList.add(childInstance);
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	public static TCComponentBOMWindow CreateNewBOMWindow() {
		try {
			TCComponentBOMWindowType bomWindowType = (TCComponentBOMWindowType) SessionUtil.GetSession()
					.getTypeComponent("BOMWindow");
			TCComponentRevisionRuleType ruletype = (TCComponentRevisionRuleType) SessionUtil.GetSession()
					.getTypeComponent("RevisionRule");
			TCComponentRevisionRule defaultRule = ruletype.getDefaultRule();
			return bomWindowType.create(defaultRule);
		} catch (TCException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String[] array = new String[] {};
		List<String> asList = Arrays.asList(array);
		if (!asList.contains("12"))
			System.out.println("123");
	}
	private static String NullToString(TCProperty prop) {
		return prop == null ? "" : prop.getDisplayableValue();
	}
}
