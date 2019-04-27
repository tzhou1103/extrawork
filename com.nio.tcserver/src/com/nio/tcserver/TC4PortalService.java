/**
 * TC4PortalService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.nio.tcserver;

public interface TC4PortalService extends javax.xml.rpc.Service {
    public java.lang.String getTC4PortalAddress();

    public com.nio.tcserver.TC4Portal getTC4Portal() throws javax.xml.rpc.ServiceException;

    public com.nio.tcserver.TC4Portal getTC4Portal(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
