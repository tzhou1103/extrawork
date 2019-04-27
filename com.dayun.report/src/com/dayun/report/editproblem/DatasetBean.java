package com.dayun.report.editproblem;

public class DatasetBean
{
  private String extensionName;
  private String datasetType;
  private String namedReferenceType;
  
  public DatasetBean(String extensionName, String datasetType, String namedReferenceType)
  {
    this.extensionName = extensionName;
    this.datasetType = datasetType;
    this.namedReferenceType = namedReferenceType;
  }
  
  public String getDatasetType()
  {
    return this.datasetType;
  }
  
  public String getNamedReferenceType()
  {
    return this.namedReferenceType;
  }
  
  public String getExtensionName()
  {
    return this.extensionName;
  }
  
  public void setExtensionName(String extensionName)
  {
    this.extensionName = extensionName;
  }
  
  public void setDatasetType(String datasetType)
  {
    this.datasetType = datasetType;
  }
  
  public void setNamedReferenceType(String namedReferenceType)
  {
    this.namedReferenceType = namedReferenceType;
  }
  
  public int hashCode()
  {
    int result = 1;
    result = 31 * result + (
      this.extensionName == null ? 0 : this.extensionName.hashCode());
    return result;
  }
  
  public boolean equals(Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    DatasetBean other = (DatasetBean)obj;
    if (this.extensionName == null)
    {
      if (other.extensionName != null) {
        return false;
      }
    }
    else if (!this.extensionName.equals(other.extensionName)) {
      return false;
    }
    return true;
  }
}
