/**
 * TC4PortalSoapBindingSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.nio.tcserver;

public class TC4PortalSoapBindingSkeleton implements com.nio.tcserver.TC4Portal, org.apache.axis.wsdl.Skeleton {
    private com.nio.tcserver.TC4Portal impl;
    private static java.util.Map _myOperations = new java.util.Hashtable();
    private static java.util.Collection _myOperationsList = new java.util.ArrayList();

    /**
    * Returns List of OperationDesc objects with this name
    */
    public static java.util.List getOperationDescByName(java.lang.String methodName) {
        return (java.util.List)_myOperations.get(methodName);
    }

    /**
    * Returns Collection of OperationDescs
    */
    public static java.util.Collection getOperationDescs() {
        return _myOperationsList;
    }

    static {
        org.apache.axis.description.OperationDesc _oper;
        org.apache.axis.description.FaultDesc _fault;
        org.apache.axis.description.ParameterDesc [] _params;
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tcserver.nio.com", "object_type"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tcserver.nio.com", "owning_user"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tcserver.nio.com", "owning_group"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tcserver.nio.com", "crNum"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getCreateInfo", _params, new javax.xml.namespace.QName("http://tcserver.nio.com", "getCreateInfoReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://tcserver.nio.com", "T4PCreatePartResp"));
        _oper.setElementQName(new javax.xml.namespace.QName("http://tcserver.nio.com", "getCreateInfo"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getCreateInfo") == null) {
            _myOperations.put("getCreateInfo", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getCreateInfo")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tcserver.nio.com", "item_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tcserver.nio.com", "item_revision_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tcserver.nio.com", "attr_names"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String[].class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getPartAttrs", _params, new javax.xml.namespace.QName("http://tcserver.nio.com", "getPartAttrsReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://tcserver.nio.com", "T4PPartAttrsOutput"));
        _oper.setElementQName(new javax.xml.namespace.QName("http://tcserver.nio.com", "getPartAttrs"));
        _oper.setSoapAction("http://tcserver.nio.com/getPartAttrs");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getPartAttrs") == null) {
            _myOperations.put("getPartAttrs", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getPartAttrs")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tcserver.nio.com", "item_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tcserver.nio.com", "item_revision_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getPartAttrsAll", _params, new javax.xml.namespace.QName("http://tcserver.nio.com", "getPartAttrsAllReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://tcserver.nio.com", "T4PPartAttrsOutput"));
        _oper.setElementQName(new javax.xml.namespace.QName("http://tcserver.nio.com", "getPartAttrsAll"));
        _oper.setSoapAction("http://tcserver.nio.com/getPartAttrsAll");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getPartAttrsAll") == null) {
            _myOperations.put("getPartAttrsAll", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getPartAttrsAll")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tcserver.nio.com", "item_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tcserver.nio.com", "item_revision_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tcserver.nio.com", "attrs"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://tcserver.nio.com", "T4PAttrProperty"), com.nio.tcserver.T4PAttrProperty[].class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("setPartAttrs", _params, new javax.xml.namespace.QName("http://tcserver.nio.com", "setPartAttrsReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://tcserver.nio.com", "T4PSetPartAttrsResp"));
        _oper.setElementQName(new javax.xml.namespace.QName("http://tcserver.nio.com", "setPartAttrs"));
        _oper.setSoapAction("http://tcserver.nio.com/setPartAttrs");
        _myOperationsList.add(_oper);
        if (_myOperations.get("setPartAttrs") == null) {
            _myOperations.put("setPartAttrs", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("setPartAttrs")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
        };
        _oper = new org.apache.axis.description.OperationDesc("getBuyers", _params, new javax.xml.namespace.QName("http://tcserver.nio.com", "getBuyersReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://tcserver.nio.com", "T4PGetBuyersResp"));
        _oper.setElementQName(new javax.xml.namespace.QName("http://tcserver.nio.com", "getBuyers"));
        _oper.setSoapAction("http://tcserver.nio.com/getBuyers");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getBuyers") == null) {
            _myOperations.put("getBuyers", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getBuyers")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
        };
        _oper = new org.apache.axis.description.OperationDesc("getSuppliers", _params, new javax.xml.namespace.QName("http://tcserver.nio.com", "getSuppliersReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://tcserver.nio.com", "T4PGetSuppliersResp"));
        _oper.setElementQName(new javax.xml.namespace.QName("http://tcserver.nio.com", "getSuppliers"));
        _oper.setSoapAction("http://tcserver.nio.com/getSuppliers");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getSuppliers") == null) {
            _myOperations.put("getSuppliers", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getSuppliers")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
        };
        _oper = new org.apache.axis.description.OperationDesc("getMaterials", _params, new javax.xml.namespace.QName("http://tcserver.nio.com", "getMaterialsReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://tcserver.nio.com", "T4PGetMaterialsResp"));
        _oper.setElementQName(new javax.xml.namespace.QName("http://tcserver.nio.com", "getMaterials"));
        _oper.setSoapAction("http://tcserver.nio.com/getMaterials");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getMaterials") == null) {
            _myOperations.put("getMaterials", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getMaterials")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tcserver.nio.com", "item_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tcserver.nio.com", "item_revision_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("setPartObsolete", _params, new javax.xml.namespace.QName("http://tcserver.nio.com", "setPartObsoleteReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://tcserver.nio.com", "T4PSetPartObsoleteResp"));
        _oper.setElementQName(new javax.xml.namespace.QName("http://tcserver.nio.com", "setPartObsolete"));
        _oper.setSoapAction("http://tcserver.nio.com/setPartObsolete");
        _myOperationsList.add(_oper);
        if (_myOperations.get("setPartObsolete") == null) {
            _myOperations.put("setPartObsolete", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("setPartObsolete")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tcserver.nio.com", "item_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tcserver.nio.com", "item_revision_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getPartPdf", _params, new javax.xml.namespace.QName("http://tcserver.nio.com", "getPartPdfReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://tcserver.nio.com", "T4PGetPartPdfResp"));
        _oper.setElementQName(new javax.xml.namespace.QName("http://tcserver.nio.com", "getPartPdf"));
        _oper.setSoapAction("http://tcserver.nio.com/getPartPdf");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getPartPdf") == null) {
            _myOperations.put("getPartPdf", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getPartPdf")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tcserver.nio.com", "functionName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tcserver.nio.com", "parts"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String[].class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("loopbackFunction", _params, new javax.xml.namespace.QName("http://tcserver.nio.com", "out"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        _oper.setElementQName(new javax.xml.namespace.QName("http://tcserver.nio.com", "loopbackFunction"));
        _oper.setSoapAction("http://tcserver.nio.com/loopbackFunction");
        _myOperationsList.add(_oper);
        if (_myOperations.get("loopbackFunction") == null) {
            _myOperations.put("loopbackFunction", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("loopbackFunction")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tcserver.nio.com", "item_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tcserver.nio.com", "old_rev_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tcserver.nio.com", "new_rev_id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("revisePart", _params, new javax.xml.namespace.QName("http://tcserver.nio.com", "revisePartReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://tcserver.nio.com", "T4PRevisePartResp"));
        _oper.setElementQName(new javax.xml.namespace.QName("http://tcserver.nio.com", "revisePart"));
        _oper.setSoapAction("http://tcserver.nio.com/revisePart");
        _myOperationsList.add(_oper);
        if (_myOperations.get("revisePart") == null) {
            _myOperations.put("revisePart", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("revisePart")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tcserver.nio.com", "updateBOMInput"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://tcserver.nio.com", "T4PUpdateBOMInfo"), com.nio.tcserver.T4PUpdateBOMInfo[].class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("updateBOM", _params, new javax.xml.namespace.QName("http://tcserver.nio.com", "out"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://tcserver.nio.com", "T4PUpdateBOMResp"));
        _oper.setElementQName(new javax.xml.namespace.QName("http://tcserver.nio.com", "updateBOM"));
        _oper.setSoapAction("http://tcserver.nio.com/updateBOM");
        _myOperationsList.add(_oper);
        if (_myOperations.get("updateBOM") == null) {
            _myOperations.put("updateBOM", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("updateBOM")).add(_oper);
    }

    public TC4PortalSoapBindingSkeleton() {
        this.impl = new com.nio.tcserver.TC4PortalSoapBindingImpl();
    }

    public TC4PortalSoapBindingSkeleton(com.nio.tcserver.TC4Portal impl) {
        this.impl = impl;
    }
    public com.nio.tcserver.T4PCreatePartResp getCreateInfo(java.lang.String object_type, java.lang.String owning_user, java.lang.String owning_group, java.lang.String crNum) throws java.rmi.RemoteException
    {
        com.nio.tcserver.T4PCreatePartResp ret = impl.getCreateInfo(object_type, owning_user, owning_group, crNum);
        return ret;
    }

    public com.nio.tcserver.T4PPartAttrsOutput getPartAttrs(java.lang.String item_id, java.lang.String item_revision_id, java.lang.String[] attr_names) throws java.rmi.RemoteException
    {
        com.nio.tcserver.T4PPartAttrsOutput ret = impl.getPartAttrs(item_id, item_revision_id, attr_names);
        return ret;
    }

    public com.nio.tcserver.T4PPartAttrsOutput getPartAttrsAll(java.lang.String item_id, java.lang.String item_revision_id) throws java.rmi.RemoteException
    {
        com.nio.tcserver.T4PPartAttrsOutput ret = impl.getPartAttrsAll(item_id, item_revision_id);
        return ret;
    }

    public com.nio.tcserver.T4PSetPartAttrsResp setPartAttrs(java.lang.String item_id, java.lang.String item_revision_id, com.nio.tcserver.T4PAttrProperty[] attrs) throws java.rmi.RemoteException
    {
        com.nio.tcserver.T4PSetPartAttrsResp ret = impl.setPartAttrs(item_id, item_revision_id, attrs);
        return ret;
    }

    public com.nio.tcserver.T4PGetBuyersResp getBuyers() throws java.rmi.RemoteException
    {
        com.nio.tcserver.T4PGetBuyersResp ret = impl.getBuyers();
        return ret;
    }

    public com.nio.tcserver.T4PGetSuppliersResp getSuppliers() throws java.rmi.RemoteException
    {
        com.nio.tcserver.T4PGetSuppliersResp ret = impl.getSuppliers();
        return ret;
    }

    public com.nio.tcserver.T4PGetMaterialsResp getMaterials() throws java.rmi.RemoteException
    {
        com.nio.tcserver.T4PGetMaterialsResp ret = impl.getMaterials();
        return ret;
    }

    public com.nio.tcserver.T4PSetPartObsoleteResp setPartObsolete(java.lang.String item_id, java.lang.String item_revision_id) throws java.rmi.RemoteException
    {
        com.nio.tcserver.T4PSetPartObsoleteResp ret = impl.setPartObsolete(item_id, item_revision_id);
        return ret;
    }

    public com.nio.tcserver.T4PGetPartPdfResp getPartPdf(java.lang.String item_id, java.lang.String item_revision_id) throws java.rmi.RemoteException
    {
        com.nio.tcserver.T4PGetPartPdfResp ret = impl.getPartPdf(item_id, item_revision_id);
        return ret;
    }

    public java.lang.String loopbackFunction(java.lang.String functionName, java.lang.String[] parts) throws java.rmi.RemoteException
    {
        java.lang.String ret = impl.loopbackFunction(functionName, parts);
        return ret;
    }

    public com.nio.tcserver.T4PRevisePartResp revisePart(java.lang.String item_id, java.lang.String old_rev_id, java.lang.String new_rev_id) throws java.rmi.RemoteException
    {
        com.nio.tcserver.T4PRevisePartResp ret = impl.revisePart(item_id, old_rev_id, new_rev_id);
        return ret;
    }

    public com.nio.tcserver.T4PUpdateBOMResp updateBOM(com.nio.tcserver.T4PUpdateBOMInfo[] updateBOMInput) throws java.rmi.RemoteException
    {
        com.nio.tcserver.T4PUpdateBOMResp ret = impl.updateBOM(updateBOMInput);
        return ret;
    }

}
