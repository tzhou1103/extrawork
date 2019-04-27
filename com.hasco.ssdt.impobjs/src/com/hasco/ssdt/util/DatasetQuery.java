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
     * ��ѯ�ṹ����
     */
    private TCComponentQueryType queryType = null;

    /**
     * ���ݼ�����
     */
    private TCComponentDatasetType datasetType = null;

    /**
     * ���ݼ���ѯ
     */
    private TCComponentQuery datasetQuery = null;

    /**
     * ָ���Ĳ�ѯ�������
     */
    private String[] f_dataset_name = new String[1];
    private String[] f_dataset_name_user = new String[2];

    /**
     * ��ѯ��������
     */
    private String[] f_dataset_name_value = new String[1];
    private String[] f_dataset_name_user_value = new String[2];

    public DatasetQuery(TCSession session)
    {

	try
	{
	    this.session = session;
	    this.queryType = (TCComponentQueryType) session.getTypeComponent("ImanQuery");

	    datasetQuery = (TCComponentQuery) queryType.find("Dataset...");// ȡ��Dataset
	    // ...
	    // ��ѯ
	    if (datasetQuery == null || !datasetQuery.isValid())
	    {
		datasetQuery = (TCComponentQuery) queryType.find("���ݼ�...");
	    }

	}
	catch (TCException imane)
	{
	    imane.printStackTrace();
	    imane.dump();
	}
    }

    /**
     * �������������ݼ����Ʋ�����ϵͳ���Ƿ��и����ݼ��������򷵻��ҵ������ݼ��������ڷ���null
     * 
     * @param datasetname
     *            ��������ݼ�����
     * @return ���ݼ�
     */
    public TCComponentDataset findDatasetByName(String datasetname)
    {
    	try
    	{
    		f_dataset_name_value[0] = datasetname;
    		if (datasetQuery == null || !datasetQuery.isValid())
    		{
    			MessageBox.post("����ʧ�ܣ�û���ҵ����ݼ���ѯ�ṹ", "��ʾ", MessageBox.INFORMATION);
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
    		TCComponent[] com_dataset = datasetQuery.execute(f_dataset_name, f_dataset_name_value);// ִ�в�ѯ
    		if (com_dataset == null || com_dataset.length == 0)
    		{
    			System.err.println("datasetQuery find nothing");
    			return null;
    		}
    		TCComponentDataset dataset0 = (TCComponentDataset) com_dataset[0];
    		TCComponentDataset dataset = dataset0.latest();// ȡ�����°汾�����ݼ�

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
    			MessageBox.post("����ʧ�ܣ�û���ҵ����ݼ���ѯ�ṹ", "��ʾ", MessageBox.INFORMATION);
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
    		TCComponent[] com_dataset = datasetQuery.execute(f_dataset_name_user, f_dataset_name_user_value);// ִ�в�ѯ
    		if (com_dataset == null || com_dataset.length == 0)
    		{
    			System.err.println("datasetQuery find nothing");
    			return null;
    		}
    		TCComponentDataset dataset0 = (TCComponentDataset) com_dataset[0];
    		TCComponentDataset dataset = dataset0.latest();// ȡ�����°汾�����ݼ�

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
     * �����ݼ��е��ļ����Ƶ�ָ��·����
     * 
     * @param dataset
     *            ���ݼ�
     * @param nameRef
     *            ����������
     * @param filename
     *            ���ݼ��е��ļ�
     * @param dir
     *            Ŀ��·��
     * @return Ŀ��·���µ��ļ�
     */
    public File exportFileToDir(TCComponentDataset dataset, String nameRef, String filename, String dir)
    {
	try
	{
	    String workdir = dir;
	    File tFile = new File(workdir, filename);
	    if (tFile.exists())
		tFile.delete();
	    File expertFile = dataset.getFile(nameRef, filename, workdir); // �õ�Ŀ���ļ�
	    return expertFile;
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	    return null;
	}
    }
}