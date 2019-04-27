/**
 * TC4Portal.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.nio.tcserver;

public interface TC4Portal extends java.rmi.Remote {
    public com.nio.tcserver.T4PCreatePartResp getCreateInfo(java.lang.String object_type, java.lang.String owning_user, java.lang.String owning_group, java.lang.String crNum) throws java.rmi.RemoteException;
    public com.nio.tcserver.T4PPartAttrsOutput getPartAttrs(java.lang.String item_id, java.lang.String item_revision_id, java.lang.String[] attr_names) throws java.rmi.RemoteException;
    public com.nio.tcserver.T4PPartAttrsOutput getPartAttrsAll(java.lang.String item_id, java.lang.String item_revision_id) throws java.rmi.RemoteException;
    public com.nio.tcserver.T4PSetPartAttrsResp setPartAttrs(java.lang.String item_id, java.lang.String item_revision_id, com.nio.tcserver.T4PAttrProperty[] attrs) throws java.rmi.RemoteException;
    public com.nio.tcserver.T4PGetBuyersResp getBuyers() throws java.rmi.RemoteException;
    public com.nio.tcserver.T4PGetSuppliersResp getSuppliers() throws java.rmi.RemoteException;
    public com.nio.tcserver.T4PGetMaterialsResp getMaterials() throws java.rmi.RemoteException;
    public com.nio.tcserver.T4PSetPartObsoleteResp setPartObsolete(java.lang.String item_id, java.lang.String item_revision_id) throws java.rmi.RemoteException;
    public com.nio.tcserver.T4PGetPartPdfResp getPartPdf(java.lang.String item_id, java.lang.String item_revision_id) throws java.rmi.RemoteException;
    public java.lang.String loopbackFunction(java.lang.String functionName, java.lang.String[] parts) throws java.rmi.RemoteException;
    public com.nio.tcserver.T4PRevisePartResp revisePart(java.lang.String item_id, java.lang.String old_rev_id, java.lang.String new_rev_id) throws java.rmi.RemoteException;
    public com.nio.tcserver.T4PUpdateBOMResp updateBOM(com.nio.tcserver.T4PUpdateBOMInfo[] updateBOMInput) throws java.rmi.RemoteException;
}
