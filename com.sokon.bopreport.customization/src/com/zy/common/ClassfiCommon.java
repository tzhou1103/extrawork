package com.zy.common;

import java.util.Map;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.classification.ClassificationService;
import com.teamcenter.services.rac.classification._2007_01.Classification;
import com.teamcenter.services.rac.classification._2007_01.Classification.GetChildrenResponse;
import com.teamcenter.services.rac.classification._2015_10.Classification.GetClassDefinitionsResponse;

public class ClassfiCommon {
	public static Classification.ClassDef GetClassDef(String ClassID) {
		try {
			TCSession session = (TCSession) AIFUtility.getDefaultSession();
			ClassificationService ClassService = ClassificationService.getService(session);
			String Lib[] = new String[] { ClassID };
			GetClassDefinitionsResponse Respon = ClassService.getClassDefinitions(Lib);
			if (Respon.serviceData.sizeOfPartialErrors() == 0) {
				Map Group = Respon.classDefinitionMap;
				Object Obj[] = Group.values().toArray();
				if (Obj.length > 0) {
					return (Classification.ClassDef) Obj[0];
				}
			}
		} catch (Exception d) {
			d.printStackTrace();
		}
		return null;
	}

	public static Classification.ChildDef[] GetICMNode(String ClassID) {
		try {
			TCSession session = (TCSession) AIFUtility.getDefaultSession();
			ClassificationService ClassService = ClassificationService.getService(session);
			String Lib[] = new String[] { ClassID };
			// Lib[0] = "ICM";// "SAM";
			GetChildrenResponse Respon = ClassService.getChildren(Lib);
			if (Respon.data.sizeOfPartialErrors() == 0) {
				Map Group = Respon.children;
				Object Obj[] = Group.values().toArray();
				if (Obj.length > 0) {
					Classification.ChildDef Childs[] = (Classification.ChildDef[]) Obj[0];
					Classification.ChildDef SubDef[] = new Classification.ChildDef[Childs.length];
					for (int i = 0; i < Childs.length; i++) {
						SubDef[i] = (Classification.ChildDef) Childs[i];
					}
					return SubDef;
				}
			}
		} catch (Exception d) {
			d.printStackTrace();
		}
		return null;
	}

	public static Classification.ChildDef GetParent(Classification.ChildDef CurDef) {
		TCSession session = (TCSession) AIFUtility.getDefaultSession();
		ClassificationService ClassService = ClassificationService.getService(session);
		try {
			com.teamcenter.services.rac.classification._2007_01.Classification.GetParentsResponse Res = ClassService.getParents(new String[] { CurDef.id });
			if (!Res.parents.isEmpty()) {
				Map map1 = Res.parents;
				Object obj1[] = map1.entrySet().toArray();
				Object obj = map1.entrySet().toArray()[0];
				// obj.Map temp = (Map) obj;

				// return (Classification.ChildDef) temp.entrySet().toArray()[0];
			}
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Classification.ChildDef[] GetChild(Classification.ChildDef ParentDef) {
		try {
			TCSession session = (TCSession) AIFUtility.getDefaultSession();
			ClassificationService ClassService = ClassificationService.getService(session);
			String Lib[] = new String[1];
			Lib[0] = ParentDef.id;
			GetChildrenResponse Respon = ClassService.getChildren(Lib);
			if (Respon.data.sizeOfPartialErrors() == 0) {
				Map Group = Respon.children;
				Object Obj[] = Group.values().toArray();
				if (Obj.length > 0) {
					Classification.ChildDef Childs[] = (Classification.ChildDef[]) Obj[0];
					Classification.ChildDef SubDef[] = new Classification.ChildDef[Childs.length];
					for (int i = 0; i < Childs.length; i++) {
						SubDef[i] = (Classification.ChildDef) Childs[i];
					}
					return SubDef;
				}
			}
		} catch (Exception d) {
			d.printStackTrace();
		}
		return null;
	}
}
