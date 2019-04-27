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

public class EditProblemDialog
  extends CustDialog
{
  private TCComponentItemRevision issueRepRev;
  private Text progressDescriptionText;
  private AttachmentListViewer proSoluImageListViewer;
  private Button addProSoluImageButton;
  private Button removeProSoluImageButton;
  private Vector<File> addProSoluImageVector = new Vector<File>();
  private Vector<TCComponentDataset> removeProSoluImageVector = new Vector<TCComponentDataset>();
  private boolean hasError = true;
  private Vector<DatasetBean> vector = new Vector<DatasetBean>();
  
  public EditProblemDialog(Shell parentShell, TCComponentItemRevision paramItemRev, Vector<DatasetBean> paramVector)
  {
    super(parentShell, CUST_DIALOG_STYLE);
    this.issueRepRev = paramItemRev;
    this.vector = paramVector;
  }
  
  public Control createContents(Composite paramComposite)
  {
    Composite localComposite = (Composite)super.createContents(paramComposite);
    Shell localShell = paramComposite.getShell();
    localShell.setText("编辑问题对象");
    localShell.setMinimumSize(540, 460);
    localShell.pack(true);
    super.centerShell(localShell);
    return localComposite;
  }
  
  protected Control createDialogArea(Composite paramComposite)
  {
    Composite mainComposite = (Composite)super.createDialogArea(paramComposite);
    


    GridForms gridForms = new GridForms(mainComposite, "pref, fill:pref:grow, pref", 
      "pref, fill:pref:grow, pref, pref, fill:pref:grow");
    gridForms.setBorderWidth(10);
    gridForms.setVerticalSpacing(20);
    
    Label progressDescriptionLabel = new Label(mainComposite, 0);
    progressDescriptionLabel.setText("进展描述：");
    this.progressDescriptionText = new Text(mainComposite, 2880);
    








































    Label proSoluImageLabel = new Label(mainComposite, 0);
    
    proSoluImageLabel.setText("问题解决方案附件：");
    this.proSoluImageListViewer = new AttachmentListViewer(mainComposite);
    this.proSoluImageListViewer.addSelectionChangedListener(new ISelectionChangedListener()
    {
      public void selectionChanged(SelectionChangedEvent event)
      {
        if (EditProblemDialog.this.proSoluImageListViewer.getListSelection() != null) {
          EditProblemDialog.this.removeProSoluImageButton.setEnabled(true);
        } else {
          EditProblemDialog.this.removeProSoluImageButton.setEnabled(false);
        }
      }
    });
    this.addProSoluImageButton = new Button(mainComposite, 8);
    
    this.addProSoluImageButton.setToolTipText("添加问题解决方案附件");
    
    this.addProSoluImageButton.setImage(Activator.getImageDescriptor("icons/add_16.png").createImage());
    this.addProSoluImageButton.addSelectionListener(new SelectionAdapter()
    {
      public void widgetSelected(SelectionEvent e)
      {
        EditProblemDialog.this.addImagePressed("E9_REL_ProSolutionPicture");
      }
    });
    this.removeProSoluImageButton = new Button(mainComposite, 8);
    
    this.removeProSoluImageButton.setToolTipText("移除问题解决方案附件");
    
    this.removeProSoluImageButton.setImage(Activator.getImageDescriptor("icons/remove_16.png").createImage());
    this.removeProSoluImageButton.setEnabled(false);
    this.removeProSoluImageButton.addSelectionListener(new SelectionAdapter()
    {
      public void widgetSelected(SelectionEvent e)
      {
        Object selection = EditProblemDialog.this.proSoluImageListViewer.getListSelection();
        EditProblemDialog.this.removeImagePressed("E9_REL_ProSolutionPicture", selection);
      }
    });
    gridForms.setComponentAt(progressDescriptionLabel, 1, 1, 1, 1);
    gridForms.setComponentAt(this.progressDescriptionText, 2, 1, 2, 2);
    









    gridForms.setComponentAt(proSoluImageLabel, 1, 3, 1, 1);
    gridForms.setComponentAt(this.proSoluImageListViewer.getList(), 2, 3, 1, 3);
    gridForms.setComponentAt(this.addProSoluImageButton, 3, 3, 1, 1);
    gridForms.setComponentAt(this.removeProSoluImageButton, 3, 4, 1, 1);
    gridForms.pack();
    
    loadData();
    
    return mainComposite;
  }
  
  private void loadData()
  {
    getShell().getDisplay().asyncExec(new Runnable()
    {
      public void run()
      {
        try
        {
          String progressDescription = EditProblemDialog.this.issueRepRev.getProperty("e9_ProgressDescription");
          if (!progressDescription.isEmpty()) {
            EditProblemDialog.this.progressDescriptionText.setText(progressDescription);
          }
          TCComponent[] proSoluComponents = EditProblemDialog.this.issueRepRev.getRelatedComponents("E9_REL_ProSolutionPicture");
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
              EditProblemDialog.this.proSoluImageListViewer.setInput(proSoluDatasetVector);
              EditProblemDialog.this.proSoluImageListViewer.refresh();
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
  
  @SuppressWarnings("unchecked")
private void addImagePressed(String relation)
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
        if (!this.vector.contains(new DatasetBean(suffix, "", "")))
        {
          MessageDialog.openError(getShell(), "错误", "不支持的文件类型，请联系管理员！"); return;
        }
        String str1;
        switch ((str1 = relation).hashCode())
        {
        case 189922441: 
          if (str1.equals("E9_REL_ProSolutionPicture"))
          {
            Vector<Object> proSoluImageInput = (Vector<Object>)this.proSoluImageListViewer.getInput();
            if (proSoluImageInput == null) {
              proSoluImageInput = new Vector<Object>();
            }
            if (!proSoluImageInput.contains(file))
            {
              proSoluImageInput.add(file);
              this.proSoluImageListViewer.setInput(proSoluImageInput);
              this.proSoluImageListViewer.refresh();
              this.addProSoluImageVector.add(file);
            }
          }
          break;
        }
      }
    }
  }
  
  @SuppressWarnings("unchecked")
private void removeImagePressed(String relation, Object element)
  {
    String str;
    switch ((str = relation).hashCode())
    {
    case 189922441: 
      if (str.equals("E9_REL_ProSolutionPicture"))
      {
        Vector<Object> proSoluImageInput = (Vector<Object>)this.proSoluImageListViewer.getInput();
        proSoluImageInput.remove(element);
        this.proSoluImageListViewer.setInput(proSoluImageInput);
        this.proSoluImageListViewer.refresh();
        if (proSoluImageInput.size() == 0) {
          this.removeProSoluImageButton.setEnabled(false);
        }
        if ((element instanceof TCComponentDataset)) {
          this.removeProSoluImageVector.add((TCComponentDataset)element);
        }
      }
      break;
    }
  }
  
  protected void createButtonsForButtonBar(Composite parent)
  {
    createButton(parent, 0, IDialogConstants.OK_LABEL, true);
  }
  
  protected void okPressed()
  {
    final String progressDescription = this.progressDescriptionText.getText();
    
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
            
            EditProblemDialog.this.issueRepRev.setProperty("e9_ProgressDescription", progressDescription);
            if (EditProblemDialog.this.addProSoluImageVector.size() > 0) {
              for (File file : EditProblemDialog.this.addProSoluImageVector)
              {
                String suffix = TcUtil.getSuffix(file.getName());
                DatasetBean datasetBean = new DatasetBean(suffix, "", "");
                int index = EditProblemDialog.this.vector.indexOf(datasetBean);
                datasetBean = (DatasetBean)EditProblemDialog.this.vector.get(index);
                
                TCComponentDataset dataset = TcUtil.createDataset(file.getName(), "", datasetBean.getDatasetType());
                TcUtil.importFileToDataset(dataset, file, datasetBean.getDatasetType(), datasetBean.getNamedReferenceType());
                EditProblemDialog.this.issueRepRev.add("E9_REL_ProSolutionPicture", dataset);
              }
            }
            if (EditProblemDialog.this.removeProSoluImageVector.size() > 0) {
              for (TCComponentDataset dataset : EditProblemDialog.this.removeProSoluImageVector) {
                EditProblemDialog.this.issueRepRev.remove("E9_REL_ProSolutionPicture", dataset);
              }
            }
            EditProblemDialog.this.issueRepRev.refresh();
            EditProblemDialog.this.hasError = false;
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
    EditProblemDialog dialog = new EditProblemDialog(new Shell(), null, null);
    dialog.open();
  }
}
