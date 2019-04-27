package com.dayun.report.utils;

import java.util.Collection;
import java.util.List;
import java.util.Vector;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class VectorContentProvider
  implements IStructuredContentProvider
{
  private final Object[] EMPTY = new Object[0];
  
  public Object[] getElements(Object obj)
  {
    if (obj == null) {
      return this.EMPTY;
    }
    if ((obj instanceof Vector)) {
      return ((Vector<?>)obj).toArray();
    }
    if ((obj instanceof List)) {
      return ((List<?>)obj).toArray();
    }
    if ((obj instanceof Collection)) {
      return ((Collection<?>)obj).toArray();
    }
    return this.EMPTY;
  }
  
  public void dispose() {}
  
  public void inputChanged(Viewer arg0, Object arg1, Object arg2) {}
}
