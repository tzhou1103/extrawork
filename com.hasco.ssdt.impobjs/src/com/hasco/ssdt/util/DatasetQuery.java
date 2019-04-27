package com.hasco.ssdt.util;

import java.io.File;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCQueryClause;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class DatasetQuery
{
    private TCSession session;

    /**
     * 查询结构类型
     */
    private TCComponentQueryType queryType = null;

    /**
     * 数据集类型
     */
    private TCComponentDatasetType datasetType = null;

    /**
     * 数据集查询
     */
    private TCComponentQuery datasetQuery = null;

    /**
     * 指定的查询条件类别
     */
    private String[] f_dataset_name = new String[1];
    private String[] f_dataset_name_user = new String[2];

    /**
     * 查询条件参数
     */
    private String[] f_dataset_name_value = new String[1];
    private String[] f_dataset_name_user_value = new String[2];

    public DatasetQuery(TCSession session)
    {

	try
	{
	    this.session = session;
	    this.queryType = (TCComponentQueryType) session.getTypeComponent("ImanQuery");

	    datasetQuery = (TCComponentQuery) queryType.find("Dataset...");// 取得Dataset
	    // ...
	    // 查询
	    if (datasetQuery == null || !datasetQuery.isValid())
	    {
		datasetQuery = (TCComponentQuery) queryType.find("数据集...");
	    }

	}
	catch (TCException imane)
	{
	    imane.printStackTrace();
	    imane.dump();
	}
    }

    /**
     * 根据所给的数据集名称查找在系统中是否含有该数据集，存在则返回找到的数据集，不存在返回null
     * 
     * @param datasetname
     *            传入的数据集名称
     * @return 数据集
     */
    public TCComponentDataset findDatasetByName(String datasetname)
    {
    	try
    	{
    		f_dataset_name_value[0] = datasetname;
    		if (datasetQuery == null || !datasetQuery.isValid())
    		{
    			MessageBox.post("查找失败，没有找到数据集查询结构", "提示", MessageBox.INFORMATION);
    			return null;
    		}
    		// System.err.println("Object Name:" + datasetQuery.d);
    		TCQueryClause[] clause = datasetQuery.describe();
    		for (int i = 0; i < clause.length; i++)
    		{
    			// System.err.println("clause:" + clause[i].getUserEntryName());
    			if (clause[i].getAttributeName().equals("object_name"))
    			{
    				f_dataset_name[0] = clause[i].getUserEntryNameDisplay();
    			}
    		}
    		TCComponent[] com_dataset = datasetQuery.execute(f_dataset_name, f_dataset_name_value);// 执行查询
    		if (com_dataset == null || com_dataset.length == 0)
    		{
    			System.err.println("datasetQuery find nothing");
    			return null;
    		}
    		TCComponentDataset dataset0 = (TCComponentDataset) com_dataset[0];
    		TCComponentDataset dataset = dataset0.latest();// 取得最新版本的数据集

    		return dataset;
    	}
    	catch (TCException imane)
    	{
    		imane.printStackTrace();
    		imane.dump();
    		return null;
    	}
    }
    
    public TCComponentDataset findDatasetByNameByUser(String datasetname, String userName)
    {
    	try
    	{
    		f_dataset_name_user_value[0] = datasetname;
    		f_dataset_name_user_value[1] = userName;
    		if (datasetQuery == null || !datasetQuery.isValid())
    		{
    			MessageBox.post("查找失败，没有找到数据集查询结构", "提示", MessageBox.INFORMATION);
    			return null;
    		}
    		TCQueryClause[] clause = datasetQuery.describe();
    		for (int i = 0; i < clause.length; i++)
    		{
    			// System.err.println("clause:" + clause[i].getUserEntryName());
    			if (clause[i].getAttributeName().equals("object_name"))
    			{
    				f_dataset_name_user[0] = clause[i].getUserEntryNameDisplay();
    			}
    			if (clause[i].getAttributeName().equals("owning_user.user_id"))
    			{
    				f_dataset_name_user[1] = clause[i].getUserEntryNameDisplay();
    			}
    		}
    		TCComponent[] com_dataset = datasetQuery.execute(f_dataset_name_user, f_dataset_name_user_value);// 执行查询
    		if (com_dataset == null || com_dataset.length == 0)
    		{
    			System.err.println("datasetQuery find nothing");
    			return null;
    		}
    		TCComponentDataset dataset0 = (TCComponentDataset) com_dataset[0];
    		TCComponentDataset dataset = dataset0.latest();// 取得最新版本的数据集

    		return dataset;
    	}
    	catch (TCException imane)
    	{
    		imane.printStackTrace();
    		imane.dump();
    		return null;
    	}
    }

    /**
     * 将数据集中的文件复制到指定路径下
     * 
     * @param dataset
     *            数据集
     * @param nameRef
     *            命名的引用
     * @param filename
     *            数据集中的文件
     * @param dir
     *            目标路径
     * @return 目标路径下的文件
     */
    public File exportFileToDir(TCComponentDataset dataset, String nameRef, String filename, String dir)
    {
	try
	{
	    String workdir = dir;
	    File tFile = new File(workdir, filename);
	    if (tFile.exists())
		tFile.delete();
	    File expertFile = dataset.getFile(nameRef, filename, workdir); // 得到目标文件
	    return expertFile;
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	    return null;
	}
    }
}