package com.sokon.report.data;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrBOPLine;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCException;

public class sokonCommon {

	public static String GetProperty(TCComponent Comp, String Chinese, String English, int Lang) {
		try {
			String Value = "";
			if (Comp != null) {
				if (English.length() == 0) {
					Value = Comp.getProperty(Chinese);
				} else if (Lang == 1) {
					Value = Comp.getProperty(English);
				} else if (Lang == 2) {
					Value = Comp.getProperty(Chinese);
				}
				if (Lang == 4) {
					Value = Comp.getProperty(Chinese) + Comp.getProperty(English);
				}
			}
			return Value;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static TCComponentItem GetProcessDoc(TCComponentMfgBvrBOPLine PaintStat, String DocumentType, String ENDocumentType) {
		try {
			TCComponent Second[] = PaintStat.getItemRevision().getRelatedComponents("IMAN_reference");
			for (int i = 0; i < Second.length; i++) {
				String Type = Second[i].getTCProperty("object_type").getStringValue();
				if (Type.equals("S4_IT_ProcessDoc")) {
					TCComponentItem ProcessDoc = (TCComponentItem) Second[i];
					// ProcessDoc.getLatestItemRevision().getTCProperty("s4_AT_DocumentType").getPropertyValue();
					String TempDocumentType = ProcessDoc.getLatestItemRevision().getProperty("s4_AT_DocumentType");
					if (TempDocumentType.equals(DocumentType) || TempDocumentType.equals(ENDocumentType)) {
						return ProcessDoc;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static TCComponentMfgBvrBOPLine GetSource(TCComponentMfgBvrBOPLine BOPLine, String Type) {
		try {
			AIFComponentContext[] Context = BOPLine.getChildren();
			for (int i = 0; i < Context.length; i++) {
				TCComponentMfgBvrBOPLine SubBOPLine = (TCComponentMfgBvrBOPLine) Context[i].getComponent();
				String TempType = SubBOPLine.getItem().getType();
				if (TempType.equals(Type)) {
					return SubBOPLine;
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static TCComponentMfgBvrBOPLine GetSourceLevel2(TCComponentMfgBvrBOPLine BOPLine, String Type) {
		try {
			AIFComponentContext[] Context = BOPLine.getChildren();
			for (int i = 0; i < Context.length; i++) {
				TCComponentMfgBvrBOPLine SubBOPLine = (TCComponentMfgBvrBOPLine) Context[i].getComponent();
				String TempType = SubBOPLine.getItem().getType();
				if (TempType.equals(Type)) {
					return SubBOPLine;
				}

				AIFComponentContext[] Context2 = SubBOPLine.getChildren();
				for (int m = 0; m < Context2.length; m++) {
					TCComponentMfgBvrBOPLine SubBOPLine2 = (TCComponentMfgBvrBOPLine) Context2[m].getComponent();
					String TempType2 = SubBOPLine2.getItem().getType();
					if (TempType2.equals(Type)) {
						return SubBOPLine2;
					}
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static TCComponentMfgBvrBOPLine GetSource(TCComponentMfgBvrBOPLine BOPLine, String OccType, String Type) {
		try {
			AIFComponentContext[] Context = BOPLine.getChildren();
			for (int i = 0; i < Context.length; i++) {
				if (Context[i].getComponent() instanceof TCComponentMfgBvrBOPLine) {
					TCComponentMfgBvrBOPLine SubBOPLine = (TCComponentMfgBvrBOPLine) Context[i].getComponent();
					String bl_occ_type = SubBOPLine.getProperty("bl_occ_type");
					if (bl_occ_type.equals(OccType)) {
						String TempType = SubBOPLine.getItem().getType();
						if (TempType.equals(Type)) {
							return SubBOPLine;
						}
					}
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		return null;
	}
}
