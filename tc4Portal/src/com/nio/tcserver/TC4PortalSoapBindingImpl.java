/**
 * TC4PortalSoapBindingImpl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.nio.tcserver;

import com.nio.tcserver.common.T4PFunction;
import com.nio.tcserver.common.T4PFunctionBOM;
import com.nio.tcserver.common.T4PFunctionDataset;
import com.nio.tcserver.common.T4PLoopback;
import com.nio.tcserver.common.T4PSync;
import com.nio.tcserver.session.LoggerDefault;

public class TC4PortalSoapBindingImpl implements com.nio.tcserver.TC4Portal
{
    public com.nio.tcserver.T4PCreatePartResp getCreateInfo(java.lang.String object_type, java.lang.String owning_user, java.lang.String owning_group, java.lang.String crNum) throws java.rmi.RemoteException {
    	try {
			return T4PFunction.getCreateInfo(object_type, owning_user, owning_group, crNum);
		} catch (java.rmi.RemoteException e) {
			LoggerDefault.logError(e.getMessage());
			throw e;
		}
    }

	public com.nio.tcserver.T4PPartAttrsOutput getPartAttrs(java.lang.String item_id,
			java.lang.String item_revision_id, java.lang.String[] attr_names) throws java.rmi.RemoteException {
		try {
			return T4PFunction.getPartAttrs(item_id, item_revision_id, attr_names);
		} catch (java.rmi.RemoteException e) {
			LoggerDefault.logError(e.getMessage());
			throw e;
		}
	}

	public com.nio.tcserver.T4PPartAttrsOutput getPartAttrsAll(java.lang.String item_id,
			java.lang.String item_revision_id) throws java.rmi.RemoteException {
		try {
			return T4PFunction.getPartAttrsAll(item_id, item_revision_id);
		} catch (java.rmi.RemoteException e) {
			LoggerDefault.logError(e.getMessage());
			throw e;
		}
	}

	public com.nio.tcserver.T4PSetPartAttrsResp setPartAttrs(java.lang.String item_id,
			java.lang.String item_revision_id, com.nio.tcserver.T4PAttrProperty[] attrs)
			throws java.rmi.RemoteException {
		try {
			return T4PFunction.setPartAttrs(item_id, item_revision_id, attrs);
		} catch (java.rmi.RemoteException e) {
			LoggerDefault.logError(e.getMessage());
			throw e;
		}
	}

	public com.nio.tcserver.T4PGetBuyersResp getBuyers() throws java.rmi.RemoteException {
		try {
			return T4PSync.getBuyers();
		} catch (java.rmi.RemoteException e) {
			LoggerDefault.logError(e.getMessage());
			throw e;
		}
	}

	public com.nio.tcserver.T4PGetSuppliersResp getSuppliers() throws java.rmi.RemoteException {
		try {
			return T4PSync.getSuppliers();
		} catch (java.rmi.RemoteException e) {
			LoggerDefault.logError(e.getMessage());
			throw e;
		}
	}

	public com.nio.tcserver.T4PGetMaterialsResp getMaterials() throws java.rmi.RemoteException {
		try {
			return T4PSync.getMaterials();
		} catch (java.rmi.RemoteException e) {
			LoggerDefault.logError(e.getMessage());
			throw e;
		}
	}

	public com.nio.tcserver.T4PSetPartObsoleteResp setPartObsolete(java.lang.String item_id,
			java.lang.String item_revision_id) throws java.rmi.RemoteException {
		try {
			return T4PFunction.setPartObsolete(item_id, item_revision_id);
		} catch (java.rmi.RemoteException e) {
			LoggerDefault.logError(e.getMessage());
			throw e;
		}
	}

	public com.nio.tcserver.T4PGetPartPdfResp getPartPdf(java.lang.String item_id, java.lang.String item_revision_id)
			throws java.rmi.RemoteException {
		try {
			return T4PFunctionDataset.getPartPdf(item_id, item_revision_id);
		} catch (java.rmi.RemoteException e) {
			LoggerDefault.logError(e.getMessage());
			throw e;
		}
	}

	public java.lang.String loopbackFunction(java.lang.String functionName, java.lang.String[] parts)
			throws java.rmi.RemoteException {
		try {
			return T4PLoopback.callLoopback(functionName, parts);
		} catch (java.rmi.RemoteException e) {
			LoggerDefault.logError(e.getMessage());
			throw e;
		}
	}

	public com.nio.tcserver.T4PRevisePartResp revisePart(java.lang.String item_id, java.lang.String old_rev_id,
			java.lang.String new_rev_id) throws java.rmi.RemoteException {
		try {
			return T4PFunction.revisePart(item_id, old_rev_id, new_rev_id);
		} catch (java.rmi.RemoteException e) {
			LoggerDefault.logError(e.getMessage());
			throw e;
		}
	}

	public com.nio.tcserver.T4PUpdateBOMResp updateBOM(com.nio.tcserver.T4PUpdateBOMInfo[] updateBOMInput)
			throws java.rmi.RemoteException {
		try {
			return T4PFunctionBOM.updateBOM(updateBOMInput);
		} catch (java.rmi.RemoteException e) {
			LoggerDefault.logError(e.getMessage());
			throw e;
		}
	}

}
