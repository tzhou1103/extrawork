/**
 * T4PSetPartAttrsResp.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.nio.tcserver;

public class T4PSetPartAttrsResp  implements java.io.Serializable {
    private boolean respInfoStatus;

    private java.lang.String respInfoMsg;

    public T4PSetPartAttrsResp() {
    }

    public T4PSetPartAttrsResp(
           boolean respInfoStatus,
           java.lang.String respInfoMsg) {
           this.respInfoStatus = respInfoStatus;
           this.respInfoMsg = respInfoMsg;
    }


    /**
     * Gets the respInfoStatus value for this T4PSetPartAttrsResp.
     * 
     * @return respInfoStatus
     */
    public boolean isRespInfoStatus() {
        return respInfoStatus;
    }


    /**
     * Sets the respInfoStatus value for this T4PSetPartAttrsResp.
     * 
     * @param respInfoStatus
     */
    public void setRespInfoStatus(boolean respInfoStatus) {
        this.respInfoStatus = respInfoStatus;
    }


    /**
     * Gets the respInfoMsg value for this T4PSetPartAttrsResp.
     * 
     * @return respInfoMsg
     */
    public java.lang.String getRespInfoMsg() {
        return respInfoMsg;
    }


    /**
     * Sets the respInfoMsg value for this T4PSetPartAttrsResp.
     * 
     * @param respInfoMsg
     */
    public void setRespInfoMsg(java.lang.String respInfoMsg) {
        this.respInfoMsg = respInfoMsg;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof T4PSetPartAttrsResp)) return false;
        T4PSetPartAttrsResp other = (T4PSetPartAttrsResp) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.respInfoStatus == other.isRespInfoStatus() &&
            ((this.respInfoMsg==null && other.getRespInfoMsg()==null) || 
             (this.respInfoMsg!=null &&
              this.respInfoMsg.equals(other.getRespInfoMsg())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        _hashCode += (isRespInfoStatus() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getRespInfoMsg() != null) {
            _hashCode += getRespInfoMsg().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(T4PSetPartAttrsResp.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tcserver.nio.com", "T4PSetPartAttrsResp"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("respInfoStatus");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "respInfoStatus"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("respInfoMsg");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "respInfoMsg"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
