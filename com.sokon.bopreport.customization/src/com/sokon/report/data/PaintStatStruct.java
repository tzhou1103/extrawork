package com.sokon.report.data;

import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrBOPLine;
import com.teamcenter.rac.kernel.TCComponentItem;

public class PaintStatStruct {
	public TCComponentMfgBvrBOPLine PaintBOP = null;
	public TCComponentMfgBvrBOPLine PaintProc = null;
	public TCComponentMfgBvrBOPLine PaintStat = null;
	public TCComponentItem ProcessDoc = null;

	public PaintStatStruct(TCComponentMfgBvrBOPLine PaintBOP, TCComponentMfgBvrBOPLine PaintProc, TCComponentMfgBvrBOPLine PaintStat, TCComponentItem ProcessDoc) {
		this.PaintBOP = PaintBOP;
		this.PaintProc = PaintProc;
		this.PaintStat = PaintStat;
		this.ProcessDoc = ProcessDoc;
	}
}
