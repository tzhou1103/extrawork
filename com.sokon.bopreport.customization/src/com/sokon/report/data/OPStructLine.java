package com.sokon.report.data;

import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrBOPLine;

public class OPStructLine {
	public String RefNo = "";
	public String Variant = "";
	public TCComponentMfgBvrBOPLine OPStructBOPLine = null;

	public OPStructLine(String RefNo, String Variant, TCComponentMfgBvrBOPLine OPStructBOPLine) {
		this.RefNo = RefNo;
		this.Variant = Variant;
		this.OPStructBOPLine = OPStructBOPLine;
	}

}
