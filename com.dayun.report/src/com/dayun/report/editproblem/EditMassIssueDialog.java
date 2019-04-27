package com.dayun.report.editproblem;

import com.dayun.report.Activator;
import com.dayun.report.utils.CustDialog;
import com.dayun.report.utils.TcUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import swthelper.layout.gridforms.GridForms;

public class EditMassIssueDialog
  extends CustDialog
{
  private TCComponentItemRevision massIssueRev;
  private Vector<DatasetBean> datasetBeanVector = new Vector<DatasetBean>();
  private Text issueDescriptionText;
  private Text causeAnalysisText;
  private Text interimMeasuresText;
  private Text ultimateMeasuresText;
  private Text rectificationProgressText;
  private AttachmentListViewer proSoluAttachmentListViewer;
  private Button addProSoluAttachmentButton;
  private Button removeProSoluAttachmentButton;
  private Vector<File> addProSoluAttachmentVector = new Vector<File>();
  private Vector<TCComponentDataset> removeProSoluAttachmentVector = new Vector<TCComponentDataset>();
  private boolean hasError = true;
  
  public EditMassIssueDialog(Shell parentShell, TCComponentItemRevision paramItemRev, Vector<DatasetBean> paramVector)
  {
    super(parentShell, CustDialog.CUST_DIALOG_STYLE);
    this.massIssueRev = paramItemRev;
    this.datasetBeanVector = paramVector;
  }
  
  public Control createContents(Composite paramComposite)
  {
    Composite localComposite = (Composite)super.createContents(paramComposite);
    Shell localShell = paramComposite.getShell();
    localShell.setText("编辑问题对象");
    localShell.setMinimumSize(540, 540);
    localShell.pack(true);
    super.centerShell(localShell);
    return localComposite;
  }
  
  protected Control createDialogArea(Composite paramComposite)
  {
    Composite mainComposite = (Composite)super.createDialogArea(paramComposite);
    
    GridForms gridForms = new GridForms(mainComposite, "pref, fill:pref:grow", 
      "pref, fill:pref:grow, pref, fill:pref:grow, pref, fill:pref:grow, pref, fill:pref:grow, pref, fill:pref:grow,fill:pref:grow");
    




    gridForms.setBorderWidth(10);
    gridForms.setVerticalSpacing(5);
    
    Label issueDescriptionLabel = new Label(mainComposite, 0);
    issueDescriptionLabel.setText("问题描述：");
    this.issueDescriptionText = new Text(mainComposite, 2880);
    
    Label causeAnalysisLabel = new Label(mainComposite, 0);
    causeAnalysisLabel.setText("原因分析：");
    this.causeAnalysisText = new Text(mainComposite, 2880);
    
    Label interimMeasuresLabel = new Label(mainComposite, 0);
    interimMeasuresLabel.setText("临时措施：");
    this.interimMeasuresText = new Text(mainComposite, 2880);
    
    Label ultimateMeasuresLabel = new Label(mainComposite, 0);
    ultimateMeasuresLabel.setText("最终措施：");
    this.ultimateMeasuresText = new Text(mainComposite, 2880);
    
    Label rectificationProgressLabel = new Label(mainComposite, 0);
    rectificationProgressLabel.setText("整改进展：");
    this.rectificationProgressText = new Text(mainComposite, 2880);
    
    Composite attachmentComposite = attachmentComposite(mainComposite);
    
    gridForms.setComponentAt(issueDescriptionLabel, 1, 1, 1, 1);
    gridForms.setComponentAt(this.issueDescriptionText, 2, 1, 1, 2);
    gridForms.setComponentAt(causeAnalysisLabel, 1, 3, 1, 1);
    gridForms.setComponentAt(this.causeAnalysisText, 2, 3, 1, 2);
    gridForms.setComponentAt(interimMeasuresLabel, 1, 5, 1, 1);
    gridForms.setComponentAt(this.interimMeasuresText, 2, 5, 1, 2);
    gridForms.setComponentAt(ultimateMeasuresLabel, 1, 7, 1, 1);
    gridForms.setComponentAt(this.ultimateMeasuresText, 2, 7, 1, 2);
    gridForms.setComponentAt(rectificationProgressLabel, 1, 9, 1, 1);
    gridForms.setComponentAt(this.rectificationProgressText, 2, 9, 1, 2);
    gridForms.setComponentAt(attachmentComposite, 1, 11, 2, 1);
    gridForms.pack();
    
    loadData();
    
    return mainComposite;
  }
  
  private Composite attachmentComposite(Composite parent)
  {
    Composite composite = new Composite(parent, 0);
    GridForms gridForms = new GridForms(composite, "pref, fill:pref:grow, pref", "pref, pref, pref, fill:pref:grow");
    gridForms.setBorderWidth(0);
    
    Label proSoluAttachmentLabel = new Label(composite, 0);
    proSoluAttachmentLabel.setText("问题解决方案附件：");
    this.proSoluAttachmentListViewer = new AttachmentListViewer(composite);
    this.proSoluAttachmentListViewer.addSelectionChangedListener(new ISelectionChangedListener()
    {
      public void selectionChanged(SelectionChangedEvent event)
      {
        if (EditMassIssueDialog.this.proSoluAttachmentListViewer.getListSelection() != null) {
          EditMassIssueDialog.this.removeProSoluAttachmentButton.setEnabled(true);
        } else {
          EditMassIssueDialog.this.removeProSoluAttachmentButton.setEnabled(false);
        }
      }
    });
    this.addProSoluAttachmentButton = new Button(composite, 8);
    this.addProSoluAttachmentButton.setToolTipText("添加问题解决方案附件");
    
    this.addProSoluAttachmentButton.setImage(Activator.getImageDescriptor("icons/add_16.png").createImage());
    this.addProSoluAttachmentButton.addSelectionListener(new SelectionAdapter()
    {
      public void widgetSelected(SelectionEvent e)
      {
        EditMassIssueDialog.this.addAttachmentPressed();
      }
    });
    this.removeProSoluAttachmentButton = new Button(composite, 8);
    this.removeProSoluAttachmentButton.setToolTipText("移除问题解决方案附件");
    
    this.removeProSoluAttachmentButton.setImage(Activator.getImageDescriptor("icons/remove_16.png").createImage());
    this.removeProSoluAttachmentButton.setEnabled(false);
    this.removeProSoluAttachmentButton.addSelectionListener(new SelectionAdapter()
    {
      public void widgetSelected(SelectionEvent e)
      {
        EditMassIssueDialog.this.removeAttachmentPressed();
      }
    });
    gridForms.setComponentAt(proSoluAttachmentLabel, 1, 1, 1, 1);
    gridForms.setComponentAt(this.proSoluAttachmentListViewer.getList(), 1, 2, 2, 3);
    gridForms.setComponentAt(this.addProSoluAttachmentButton, 3, 2, 1, 1);
    gridForms.setComponentAt(this.removeProSoluAttachmentButton, 3, 3, 1, 1);
    
    gridForms.pack();
    
    return composite;
  }
  
  @SuppressWarnings("unchecked")
private void addAttachmentPressed()
  {
    FileDialog fileDialog = new FileDialog(getShell(), 4096);
    fileDialog.setText("选择文件");
    fileDialog.setFilterNames(new String[] { "所有文件" });
    fileDialog.setFilterExtensions(new String[] { "*.*" });
    String filePath = fileDialog.open();
    if (filePath != null)
    {
      File file = new File(filePath);
      if (file.exists())
      {
        String suffix = TcUtil.getSuffix(file.getName());
        if (!this.datasetBeanVector.contains(new DatasetBean(suffix, "", "")))
        {
          MessageDialog.openError(getShell(), "错误", "不支持的文件类型，请联系管理员！");
          return;
        }
        Vector<Object> proSoluImageInput = (Vector<Object>)this.proSoluAttachmentListViewer.getInput();
        if (proSoluImageInput == null) {
          proSoluImageInput = new Vector<Object>();
        }
        if (!proSoluImageInput.contains(file))
        {
          proSoluImageInput.add(file);
          this.proSoluAttachmentListViewer.setInput(proSoluImageInput);
          this.proSoluAttachmentListViewer.refresh();
          this.addProSoluAttachmentVector.add(file);
        }
      }
    }
  }
  
  private void removeAttachmentPressed()
  {
    Object listSelection = this.proSoluAttachmentListViewer.getListSelection();
    if (listSelection != null)
    {
      Vector<?> proSoluImageInput = (Vector<?>)this.proSoluAttachmentListViewer.getInput();
      proSoluImageInput.remove(listSelection);
      this.proSoluAttachmentListViewer.setInput(proSoluImageInput);
      this.proSoluAttachmentListViewer.refresh();
      if (proSoluImageInput.size() == 0) {
        this.removeProSoluAttachmentButton.setEnabled(false);
      }
      if ((listSelection instanceof TCComponentDataset)) {
        this.removeProSoluAttachmentVector.add((TCComponentDataset)listSelection);
      }
    }
  }
  
  private void loadData()
  {
    getShell().getDisplay().asyncExec(new Runnable()
    {
      public void run()
      {
        try
        {
          String[] propNames = { "e9_IssueDescription", "e9_CauseAnalysis", "e9_InterimMeasures", "e9_UltimateMeasures", "e9_RectificationProgress" };
          String[] propValues = EditMassIssueDialog.this.massIssueRev.getProperties(propNames);
          if ((propValues != null) && (propValues.length == 5))
          {
            EditMassIssueDialog.this.issueDescriptionText.setText(propValues[0]);
            EditMassIssueDialog.this.causeAnalysisText.setText(propValues[1]);
            EditMassIssueDialog.this.interimMeasuresText.setText(propValues[2]);
            EditMassIssueDialog.this.ultimateMeasuresText.setText(propValues[3]);
            EditMassIssueDialog.this.rectificationProgressText.setText(propValues[4]);
          }
          TCComponent[] proSoluComponents = EditMassIssueDialog.this.massIssueRev.getRelatedComponents("E9_REL_ProSolutionPicture");
          if ((proSoluComponents != null) && (proSoluComponents.length > 0))
          {
            Vector<TCComponentDataset> proSoluDatasetVector = new Vector<TCComponentDataset>();
            for (TCComponent proSoluComponent : proSoluComponents) {
              if ((proSoluComponent instanceof TCComponentDataset)) {
                proSoluDatasetVector.add((TCComponentDataset)proSoluComponent);
              }
            }
            if (proSoluDatasetVector.size() > 0)
            {
              EditMassIssueDialog.this.proSoluAttachmentListViewer.setInput(proSoluDatasetVector);
              EditMassIssueDialog.this.proSoluAttachmentListViewer.refresh();
            }
          }
        }
        catch (TCException e)
        {
          e.printStackTrace();
          MessageBox.post(e);
        }
      }
    });
  }
  
  protected void createButtonsForButtonBar(Composite parent)
  {
    createButton(parent, 0, IDialogConstants.OK_LABEL, true);
  }
  
  protected void okPressed()
  {
    final String issueDescription = this.issueDescriptionText.getText().trim();
    final String causeAnalysis = this.causeAnalysisText.getText().trim();
    final String interimMeasures = this.interimMeasuresText.getText().trim();
    final String ultimateMeasures = this.ultimateMeasuresText.getText().trim();
    final String rectificationProgress = this.rectificationProgressText.getText().trim();
    
    ProgressMonitorDialog monitorDialog = new ProgressMonitorDialog(getShell());
    try
    {
      monitorDialog.run(true, false, new IRunnableWithProgress()
      {
        public void run(IProgressMonitor progressMonitor)
          throws InvocationTargetException, InterruptedException
        {
          progressMonitor.beginTask("正在编辑问题对象...", -1);
          try
          {
            TcUtil.setByPass("true");
            
            EditMassIssueDialog.this.massIssueRev.setProperty("e9_IssueDescription", issueDescription);
            EditMassIssueDialog.this.massIssueRev.setProperty("e9_CauseAnalysis", causeAnalysis);
            EditMassIssueDialog.this.massIssueRev.setProperty("e9_InterimMeasures", interimMeasures);
            EditMassIssueDialog.this.massIssueRev.setProperty("e9_UltimateMeasures", ultimateMeasures);
            EditMassIssueDialog.this.massIssueRev.setProperty("e9_RectificationProgress", rectificationProgress);
            if (EditMassIssueDialog.this.addProSoluAttachmentVector.size() > 0) {
              for (File file : EditMassIssueDialog.this.addProSoluAttachmentVector)
              {
                String suffix = TcUtil.getSuffix(file.getName());
                DatasetBean datasetBean = new DatasetBean(suffix, "", "");
                int index = EditMassIssueDialog.this.datasetBeanVector.indexOf(datasetBean);
                datasetBean = (DatasetBean)EditMassIssueDialog.this.datasetBeanVector.get(index);
                
                TCComponentDataset dataset = TcUtil.createDataset(file.getName(), "", datasetBean.getDatasetType());
                TcUtil.importFileToDataset(dataset, file, datasetBean.getDatasetType(), datasetBean.getNamedReferenceType());
                EditMassIssueDialog.this.massIssueRev.add("E9_REL_ProSolutionPicture", dataset);
              }
            }
            if (EditMassIssueDialog.this.removeProSoluAttachmentVector.size() > 0) {
              for (TCComponentDataset dataset : EditMassIssueDialog.this.removeProSoluAttachmentVector) {
                EditMassIssueDialog.this.massIssueRev.remove("E9_REL_ProSolutionPicture", dataset);
              }
            }
            EditMassIssueDialog.this.massIssueRev.refresh();
            EditMassIssueDialog.this.hasError = false;
          }
          catch (TCException e)
          {
            e.printStackTrace();
            MessageBox.post(e);
          }
          finally
          {
            try
            {
              TcUtil.setByPass("false");
            }
            catch (TCException e)
            {
              e.printStackTrace();
            }
            progressMonitor.done();
          }
        }
      });
    }
    catch (InvocationTargetException|InterruptedException e)
    {
      e.printStackTrace();
      MessageBox.post(e);
    }
    finally
    {
      if (!this.hasError) {
        MessageBox.post("编辑完成！", "提示", 2);
      }
    }
    super.okPressed();
  }
  
  public static void main(String[] args)
  {
    EditMassIssueDialog dialog = new EditMassIssueDialog(new Shell(), null, null);
    dialog.open();
  }
}
