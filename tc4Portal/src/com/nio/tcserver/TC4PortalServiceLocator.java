/**
 * TC4PortalServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.nio.tcserver;

public class TC4PortalServiceLocator extends org.apache.axis.client.Service implements com.nio.tcserver.TC4PortalService {

    public TC4PortalServiceLocator() {
    }


    public TC4PortalServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public TC4PortalServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for TC4Portal
    private java.lang.String TC4Portal_address = "http://localhost:8080/tc4Portal/services/TC4Portal";

    public java.lang.String getTC4PortalAddress() {
        return TC4Portal_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String TC4PortalWSDDServiceName = "TC4Portal";

    public java.lang.String getTC4PortalWSDDServiceName() {
        return TC4PortalWSDDServiceName;
    }

    public void setTC4PortalWSDDServiceName(java.lang.String name) {
        TC4PortalWSDDServiceName = name;
    }

    public com.nio.tcserver.TC4Portal getTC4Portal() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(TC4Portal_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getTC4Portal(endpoint);
    }

    public com.nio.tcserver.TC4Portal getTC4Portal(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.nio.tcserver.TC4PortalSoapBindingStub _stub = new com.nio.tcserver.TC4PortalSoapBindingStub(portAddress, this);
            _stub.setPortName(getTC4PortalWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setTC4PortalEndpointAddress(java.lang.String address) {
        TC4Portal_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.nio.tcserver.TC4Portal.class.isAssignableFrom(serviceEndpointInterface)) {
                com.nio.tcserver.TC4PortalSoapBindingStub _stub = new com.nio.tcserver.TC4PortalSoapBindingStub(new java.net.URL(TC4Portal_address), this);
                _stub.setPortName(getTC4PortalWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("TC4Portal".equals(inputPortName)) {
            return getTC4Portal();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://tcserver.nio.com", "TC4PortalService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://tcserver.nio.com", "TC4Portal"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("TC4Portal".equals(portName)) {
            setTC4PortalEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
