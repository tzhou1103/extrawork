package com.nio.tcserver;

public class TC4PortalProxy implements com.nio.tcserver.TC4Portal {
  private String _endpoint = null;
  private com.nio.tcserver.TC4Portal tC4Portal = null;
  
  public TC4PortalProxy() {
    _initTC4PortalProxy();
  }
  
  public TC4PortalProxy(String endpoint) {
    _endpoint = endpoint;
    _initTC4PortalProxy();
  }
  
  private void _initTC4PortalProxy() {
    try {
      tC4Portal = (new com.nio.tcserver.TC4PortalServiceLocator()).getTC4Portal();
      if (tC4Portal != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)tC4Portal)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)tC4Portal)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (tC4Portal != null)
      ((javax.xml.rpc.Stub)tC4Portal)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.nio.tcserver.TC4Portal getTC4Portal() {
    if (tC4Portal == null)
      _initTC4PortalProxy();
    return tC4Portal;
  }
  
  public com.nio.tcserver.T4PCreatePartResp getCreateInfo(java.lang.String object_type, java.lang.String owning_user, java.lang.String owning_group, java.lang.String crNum) throws java.rmi.RemoteException{
    if (tC4Portal == null)
      _initTC4PortalProxy();
    return tC4Portal.getCreateInfo(object_type, owning_user, owning_group, crNum);
  }
  
  public com.nio.tcserver.T4PPartAttrsOutput getPartAttrs(java.lang.String item_id, java.lang.String item_revision_id, java.lang.String[] attr_names) throws java.rmi.RemoteException{
    if (tC4Portal == null)
      _initTC4PortalProxy();
    return tC4Portal.getPartAttrs(item_id, item_revision_id, attr_names);
  }
  
  public com.nio.tcserver.T4PPartAttrsOutput getPartAttrsAll(java.lang.String item_id, java.lang.String item_revision_id) throws java.rmi.RemoteException{
    if (tC4Portal == null)
      _initTC4PortalProxy();
    return tC4Portal.getPartAttrsAll(item_id, item_revision_id);
  }
  
  public com.nio.tcserver.T4PSetPartAttrsResp setPartAttrs(java.lang.String item_id, java.lang.String item_revision_id, com.nio.tcserver.T4PAttrProperty[] attrs) throws java.rmi.RemoteException{
    if (tC4Portal == null)
      _initTC4PortalProxy();
    return tC4Portal.setPartAttrs(item_id, item_revision_id, attrs);
  }
  
  public com.nio.tcserver.T4PGetBuyersResp getBuyers() throws java.rmi.RemoteException{
    if (tC4Portal == null)
      _initTC4PortalProxy();
    return tC4Portal.getBuyers();
  }
  
  public com.nio.tcserver.T4PGetSuppliersResp getSuppliers() throws java.rmi.RemoteException{
    if (tC4Portal == null)
      _initTC4PortalProxy();
    return tC4Portal.getSuppliers();
  }
  
  public com.nio.tcserver.T4PGetMaterialsResp getMaterials() throws java.rmi.RemoteException{
    if (tC4Portal == null)
      _initTC4PortalProxy();
    return tC4Portal.getMaterials();
  }
  
  public com.nio.tcserver.T4PSetPartObsoleteResp setPartObsolete(java.lang.String item_id, java.lang.String item_revision_id) throws java.rmi.RemoteException{
    if (tC4Portal == null)
      _initTC4PortalProxy();
    return tC4Portal.setPartObsolete(item_id, item_revision_id);
  }
  
  public com.nio.tcserver.T4PGetPartPdfResp getPartPdf(java.lang.String item_id, java.lang.String item_revision_id) throws java.rmi.RemoteException{
    if (tC4Portal == null)
      _initTC4PortalProxy();
    return tC4Portal.getPartPdf(item_id, item_revision_id);
  }
  
  public java.lang.String loopbackFunction(java.lang.String functionName, java.lang.String[] parts) throws java.rmi.RemoteException{
    if (tC4Portal == null)
      _initTC4PortalProxy();
    return tC4Portal.loopbackFunction(functionName, parts);
  }
  
  public com.nio.tcserver.T4PRevisePartResp revisePart(java.lang.String item_id, java.lang.String old_rev_id, java.lang.String new_rev_id) throws java.rmi.RemoteException{
    if (tC4Portal == null)
      _initTC4PortalProxy();
    return tC4Portal.revisePart(item_id, old_rev_id, new_rev_id);
  }
  
  public com.nio.tcserver.T4PUpdateBOMResp updateBOM(com.nio.tcserver.T4PUpdateBOMInfo[] updateBOMInput) throws java.rmi.RemoteException{
    if (tC4Portal == null)
      _initTC4PortalProxy();
    return tC4Portal.updateBOM(updateBOMInput);
  }
  
  
}