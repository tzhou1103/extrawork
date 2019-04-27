package com.zht.customization.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentSavedVariantRule;
import com.zht.customization.impl.Model;
import com.zht.customization.listeners.OKListener;
import com.zht.customization.model.BOMNode;
import com.zht.customization.model.ECRModel;
import com.zht.customization.model.ModelObject;
import com.zht.customization.utils.BOMUtil;

public class ModelManager {
	// static Map<Integer,Integer> mapping = new HashMap<Integer,
	// Integer>();//数量与modelobject对应关系
	public static List<Model> modelList = new ArrayList<Model>();// 存儲modelobject
	public static List<BOMNode> nodeList = new ArrayList<BOMNode>();// 存儲modelobject
	public static List<Map> quantityList = new ArrayList<Map>();// 存储数量
	public static String ruleName;// = "";
	public static BOMNode root = null;

	public static List<Model> modelLList = new Vector<Model>();
	public static List<Model> modelHList = new Vector<Model>();

	public static void CompareBOM() {
		// L->H
		for (Model modelL : modelLList) {
			ECRModel ecrModel = (ECRModel) modelL;
			int index = modelHList.indexOf(modelL);
			if (index == -1 && ecrModel.getAdd().equals("")) {
				ecrModel.setAdd("D");
				modelList.add(ecrModel);
			} else if (index != -1 && ecrModel.getAdd().equals("")) {
				ECRModel model = (ECRModel) modelHList.get(index);
				if (model.isModified(ecrModel)) {
					ecrModel.setAdd("G");
					modelList.add(ecrModel);
				}
			}
		}
		// H->L
		for (Model modelH : modelHList) {
			ECRModel ecrModel = (ECRModel) modelH;
			int index = modelLList.indexOf(modelH);
			if (index == -1 && ecrModel.getAdd().equals("")) {
				ecrModel.setAdd("A");
				modelList.add(ecrModel);
			}
		}
	}

	public static void excuteQuantity(ModelObject modelObject) {
		int indexOf = modelList.indexOf(modelObject);
		System.out.println(modelObject.getPartID() + ":" + indexOf);
		if (indexOf != -1) {
			Map<String, Integer> quantityMap = quantityList.get(indexOf);
			Integer pastQuantity = (Integer) quantityMap.get(ruleName);
			int quantity = 0;
			if (!ModelManager.ruleName.equals(""))
				quantity = Integer.parseInt(modelObject.getQuantity()) + pastQuantity;
			quantityMap.put(ruleName, quantity);
		} else {
			modelList.add(modelObject);
			Map<String, Integer> quantites = new HashMap<String, Integer>();
			for (TCComponentSavedVariantRule sos : OKListener.rules) {
				quantites.put(sos.getName(), 0);
			}
			// quantites.put(ruleName,
			// Integer.parseInt(modelObject.getQuantity()));
			quantityList.add(quantites);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Map<String, Integer> quantites = new HashMap<String, Integer>();
		quantites.put("", 0);
		quantityList.add(quantites);
		System.out.println(quantites.get(""));
		System.out.println("--------------------------");
	}

}
