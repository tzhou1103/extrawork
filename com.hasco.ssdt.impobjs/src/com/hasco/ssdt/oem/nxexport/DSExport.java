package com.hasco.ssdt.oem.nxexport;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;

public class DSExport {

	
	public static void exportDS(TCComponentItemRevision itemRev,String exportPath,ArrayList<String> exportType,ArrayList<String> currentTransferPath,HashMap<String,String>nxPartToPath) throws TCException
	{
		TCComponent[] relateComps = itemRev.getRelatedComponents("IMAN_specification");
		for (int i = 0; i < relateComps.length; i++) {
			
			if(exportType.indexOf(relateComps[i].getType())!=-1)
			{
				TCComponentDataset datasetObject = (TCComponentDataset) relateComps[i];
				export(datasetObject.getType(),datasetObject,exportPath,currentTransferPath,nxPartToPath);
				
			}
			
		}
		
	}
	
	public static void exportDS(TCComponentItemRevision itemRev,String exportPath,ArrayList<String> exportType) throws TCException
	{
		TCComponent[] relateComps = itemRev.getRelatedComponents("IMAN_specification");
		for (int i = 0; i < relateComps.length; i++) {
			
			if(exportType.indexOf(relateComps[i].getType())!=-1)
			{
				TCComponentDataset datasetObject = (TCComponentDataset) relateComps[i];
				export(datasetObject.getType(),datasetObject,exportPath);
				
			}
			
		}
		
	}
	public static void export(String datasetType,TCComponentDataset dataSetObject,String exportPath,ArrayList<String> currentTransferPath,HashMap<String,String>nxPartToPath) throws TCException{
		
		DSType type = DSType.valueOf(datasetType);
		File files[] = null;
		int index = 0;
		
		switch(type){
		
		  case PDF :
			      files = dataSetObject.getFiles("PDF_Reference", exportPath);	
			      for(int j = 0;j< files.length;j++)
			      {
			    	  for(int i=0;i<currentTransferPath.size();i++){
							nxPartToPath.put(files[j].getAbsolutePath()+"@#@"+(index++),currentTransferPath.get(i));
					}
			      }
			      break;	  
		  case Image:
			      files = dataSetObject.getFiles("Image", exportPath);
			      for(int j = 0;j< files.length;j++)
			      {
			    	  for(int i=0;i<currentTransferPath.size();i++){
							nxPartToPath.put(files[j].getAbsolutePath()+"@#@"+(index++),currentTransferPath.get(i));
					}
			      }
			      break;
			  
		  case MSWord:
			     files = dataSetObject.getFiles("word", exportPath);
			     for(int j = 0;j< files.length;j++)
			      {
			    	  for(int i=0;i<currentTransferPath.size();i++){
							nxPartToPath.put(files[j].getAbsolutePath()+"@#@"+(index++),currentTransferPath.get(i));
					}
			      }
			     break;
			     
		  case MSWordX:
			     files = dataSetObject.getFiles("word", exportPath);
			     for(int j = 0;j< files.length;j++)
			      {
			    	  for(int i=0;i<currentTransferPath.size();i++){
							nxPartToPath.put(files[j].getAbsolutePath()+"@#@"+(index++),currentTransferPath.get(i));
					}
			      }
			     break;
			     
		  case MSExcel: 
			     files = dataSetObject.getFiles("excel", exportPath);
			     for(int j = 0;j< files.length;j++)
			      {
			    	  for(int i=0;i<currentTransferPath.size();i++){
							nxPartToPath.put(files[j].getAbsolutePath()+"@#@"+(index++),currentTransferPath.get(i));
					}
			      }
			     break;
			     
		  case MSExcelX: 
			     files = dataSetObject.getFiles("excel", exportPath);
			     for(int j = 0;j< files.length;j++)
			      {
			    	  for(int i=0;i<currentTransferPath.size();i++){
							nxPartToPath.put(files[j].getAbsolutePath()+"@#@"+(index++),currentTransferPath.get(i));
					}
			      }
			     break;
			     
		  case H5Winrar: 
			     files = dataSetObject.getFiles("H5Winrar", exportPath);
			     for(int j = 0;j< files.length;j++)
			      {
			    	  for(int i=0;i<currentTransferPath.size();i++){
							nxPartToPath.put(files[j].getAbsolutePath()+"@#@"+(index++),currentTransferPath.get(i));
					}
			      }
			     break;
			     
		  case Zip: 
			     files = dataSetObject.getFiles("ZIPFILE", exportPath);
			     for(int j = 0;j< files.length;j++)
			      {
			    	  for(int i=0;i<currentTransferPath.size();i++){
							nxPartToPath.put(files[j].getAbsolutePath()+"@#@"+(index++),currentTransferPath.get(i));
					}
			      }
			     break;	
			     
		  case UGPART: 
			     files = dataSetObject.getFiles("UGPART", exportPath);
			     for(int j = 0;j< files.length;j++)
			      {
			    	  for(int i=0;i<currentTransferPath.size();i++){
							nxPartToPath.put(files[j].getAbsolutePath()+"@#@"+(index++),currentTransferPath.get(i));
					}
			      }
			     break;	
		
		}	
	}
	
	
public static void export(String datasetType,TCComponentDataset dataSetObject,String exportPath) throws TCException{
		
		DSType type = DSType.valueOf(datasetType);
		File files[] = null;
	
		switch(type){
		
		  case PDF :
			      files = dataSetObject.getFiles("PDF_Reference", exportPath);	
			      break;	  
		  case Image:
			      files = dataSetObject.getFiles("Image", exportPath);
			      break;
			  
		  case MSWord:
			     files = dataSetObject.getFiles("word", exportPath);
			     break;
			     
		  case MSWordX:
			     files = dataSetObject.getFiles("word", exportPath);
			     break;
			     
		  case MSExcel: 
			     files = dataSetObject.getFiles("excel", exportPath);
			     break;
			     
		  case MSExcelX: 
			     files = dataSetObject.getFiles("excel", exportPath);
			     break;
			     
		  case H5Winrar: 
			     files = dataSetObject.getFiles("H5Winrar", exportPath);
			     break;
			     
		  case Zip: 
			     files = dataSetObject.getFiles("ZIPFILE", exportPath);
			     break;	
			     

		
		}	
	}
	
	
	public enum DSType {
		
		PDF,Image,MSWord,MSWordX,MSExcel,MSExcelX,H5Winrar,Zip,UGPART
		
	}
	
	
	
}
