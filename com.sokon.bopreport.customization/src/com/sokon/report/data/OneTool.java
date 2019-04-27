package com.sokon.report.data;

import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrBOPLine;

public class OneTool {
	public String RefNo = "";
	public TCComponentMfgBvrBOPLine OPStructBOPLine = null;

	public OneTool(String RefNo, TCComponentMfgBvrBOPLine OPStructBOPLine) {
		this.RefNo = RefNo;
		this.OPStructBOPLine = OPStructBOPLine;
	}

}
