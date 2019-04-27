package com.sokon.bopreport.customization.generalassemblyprocessdoc;

import java.io.File;

import com.teamcenter.rac.kernel.TCComponentDataset;

public class FileDataset 
{
	private TCComponentDataset ds;
	private File imageFile;
	public FileDataset(TCComponentDataset ds, File imageFile) 
	{
		this.ds = ds;
		this.imageFile = imageFile;
	}
	public TCComponentDataset getDs() {
		return ds;
	}
	public void setDs(TCComponentDataset ds) {
		this.ds = ds;
	}
	public File getImageFile() {
		return imageFile;
	}
	public void setImageFile(File imageFile) {
		this.imageFile = imageFile;
	}
	
	

}
