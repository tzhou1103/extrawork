package com.zht.report.jobs;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Utilities;
import com.zht.report.dialogs.ZHTConstants;
import com.zht.report.log.LogAppend;
import com.zht.report.utils.TcUtil;

public class ERPToPLMUpdatePropertyJob extends Job
{
	private boolean completed = false;
	private StringBuilder stringBuilder = new StringBuilder("");
	
	private String filePath;
	
	private LogAppend logAppend;
	private String logFilePath;
	
	public ERPToPLMUpdatePropertyJob(String name, String paramFilePath) {
		super(name);
		this.filePath = paramFilePath;
	}

	@Override
	protected IStatus run(IProgressMonitor progressMonitor)
	{
		progressMonitor.beginTask(ZHTConstants.UPDATEPROPERTYJOB_BEGINTASK_MSG, -1);
		
		try {
			this.logAppend = new LogAppend(System.getProperty("java.io.tmpdir"), "ERPToPLM_UpdateProperty_");
			
			String[] propertyNames = TcUtil.getPrefStringValues("Z9_itemRevision_Property");
			if (propertyNames == null || propertyNames.length == 0) {
				String message = MessageFormat.format(ZHTConstants.INVALIDPREFCONFIGURATION_MSG, "Z9_itemRevision_Property");
				this.logAppend.messageLog(message);
			} else {
				Vector<String> vector = readFile();
				if (vector.size() > 0)
				{
					this.logAppend.messageLog("属性更新完成！ 以下数据未能更新：\r\n");
					
					for (String lineInfo : vector) 
					{
						if (lineInfo.contains("|")) 
						{
							String[] splitStrs = lineInfo.split("\\|", -1);
							TCComponentItem item = TcUtil.findItem(splitStrs[0]);
							if (item != null)
							{
								progressMonitor.subTask(ZHTConstants.UPDATEPROPERTYJOB_SUBTASK_MSG);
								
								TcUtil.openByPass();
								TCComponentItemRevision latestRevision = item.getLatestItemRevision();
								for (int i = 1; i < splitStrs.length; i++) 
								{
									if (splitStrs[i].contains(","))
									{
										String[] propertyNameAndValue = splitStrs[i].split(",", -1);
										String proprtyName = propertyNameAndValue[0];
										String proprtyValue = propertyNameAndValue[1];
										if (Utilities.contains(proprtyName, propertyNames)) {
											latestRevision.getTCProperty(proprtyName).setStringValue(proprtyValue);
										}
									}
								}
								TcUtil.closeByPass();
							} else {
								this.stringBuilder.append("未找到ID为 " + splitStrs[0] + " 的对象.\n");
								this.logAppend.messageLog("未找到ID为 " + splitStrs[0] + " 的对象.\n");
							}
						}
					}
				} else {
					this.stringBuilder.append("所选文件没有数据.\n");
				}
				
				if (this.stringBuilder.toString().equals("")) {
					this.completed = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		} finally {
			if (this.logAppend != null) {
				this.logAppend.close();
				this.logFilePath = this.logAppend.getLogFilePath();
			}
			progressMonitor.done();
		}
		
		return Status.OK_STATUS;
	}
	
	private Vector<String> readFile() throws IOException
	{
		BufferedReader bufferedReader = null;
		
		Vector<String> vector = new Vector<String>();
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(this.filePath)));
			String line = null;
			while ((line = bufferedReader.readLine()) != null)  {
				if (!line.equals("") && !vector.contains(line)) {
					vector.add(line);
				}
			}
		} finally {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
		}
		
		return vector;
	}
	
	public boolean isCompleted() {
		return completed;
	}

	public String getLogFilePath() {
		return logFilePath;
	}
}