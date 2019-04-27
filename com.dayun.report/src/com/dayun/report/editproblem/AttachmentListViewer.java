package com.dayun.report.editproblem;

import com.dayun.report.utils.VectorContentProvider;
import com.teamcenter.rac.kernel.TCComponentDataset;
import java.io.File;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public class AttachmentListViewer
  extends ListViewer
{
  public AttachmentListViewer(Composite parent)
  {
    super(parent, 68352);
    
    setContentProvider(new VectorContentProvider());
    setLabelProvider(new ImageListLabelProvider());
  }
  
  class ImageListLabelProvider
    extends LabelProvider
  {
    ImageListLabelProvider() {}
    
    public Image getImage(Object element)
    {
      return super.getImage(element);
    }
    
    public String getText(Object element)
    {
      if ((element instanceof TCComponentDataset)) {
        return ((TCComponentDataset)element).toDisplayString();
      }
      if ((element instanceof File)) {
        return ((File)element).getAbsolutePath();
      }
      return super.getText(element);
    }
  }
  
  public Object getListSelection()
  {
    Object selection = getSelection();
    if ((selection != null) && ((selection instanceof IStructuredSelection)))
    {
      IStructuredSelection structuredSelection = (IStructuredSelection)selection;
      Object firstElement = structuredSelection.getFirstElement();
      if ((firstElement != null) && (
        ((firstElement instanceof TCComponentDataset)) || ((firstElement instanceof File)))) {
        return firstElement;
      }
    }
    return null;
  }
}
