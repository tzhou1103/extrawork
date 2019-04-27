package com.sokon.bopreport.customization.vehicletorquelist;

import com.teamcenter.rac.kernel.TCComponentBOMLine;

public class RowInfo
{
	private TCComponentBOMLine partBOMLine;
	private TCComponentBOMLine opBOMLine;
	private TCComponentBOMLine workAreaBOMLine;
	public RowInfo(TCComponentBOMLine partBOMLine, TCComponentBOMLine opBOMLine, TCComponentBOMLine workAreaBOMLine) 
	{
		this.partBOMLine = partBOMLine;
		this.opBOMLine = opBOMLine;
		this.workAreaBOMLine = workAreaBOMLine;
	}
	public TCComponentBOMLine getPartBOMLine() {
		return partBOMLine;
	}
	public void setPartBOMLine(TCComponentBOMLine partBOMLine) {
		this.partBOMLine = partBOMLine;
	}
	public TCComponentBOMLine getOpBOMLine() {
		return opBOMLine;
	}
	public void setOpBOMLine(TCComponentBOMLine opBOMLine) {
		this.opBOMLine = opBOMLine;
	}
	public TCComponentBOMLine getWorkAreaBOMLine() {
		return workAreaBOMLine;
	}
	public void setWorkAreaBOMLine(TCComponentBOMLine workAreaBOMLine) {
		this.workAreaBOMLine = workAreaBOMLine;
	}
}

