/**
 * T4PGetBuyersResp.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.nio.tcserver;

public class T4PGetBuyersResp  implements java.io.Serializable {
    private java.lang.String infoMsg;

    private com.nio.tcserver.T4PBuyerInfo[] buyers;

    public T4PGetBuyersResp() {
    }

    public T4PGetBuyersResp(
           java.lang.String infoMsg,
           com.nio.tcserver.T4PBuyerInfo[] buyers) {
           this.infoMsg = infoMsg;
           this.buyers = buyers;
    }


    /**
     * Gets the infoMsg value for this T4PGetBuyersResp.
     * 
     * @return infoMsg
     */
    public java.lang.String getInfoMsg() {
        return infoMsg;
    }


    /**
     * Sets the infoMsg value for this T4PGetBuyersResp.
     * 
     * @param infoMsg
     */
    public void setInfoMsg(java.lang.String infoMsg) {
        this.infoMsg = infoMsg;
    }


    /**
     * Gets the buyers value for this T4PGetBuyersResp.
     * 
     * @return buyers
     */
    public com.nio.tcserver.T4PBuyerInfo[] getBuyers() {
        return buyers;
    }


    /**
     * Sets the buyers value for this T4PGetBuyersResp.
     * 
     * @param buyers
     */
    public void setBuyers(com.nio.tcserver.T4PBuyerInfo[] buyers) {
        this.buyers = buyers;
    }

    public com.nio.tcserver.T4PBuyerInfo getBuyers(int i) {
        return this.buyers[i];
    }

    public void setBuyers(int i, com.nio.tcserver.T4PBuyerInfo _value) {
        this.buyers[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof T4PGetBuyersResp)) return false;
        T4PGetBuyersResp other = (T4PGetBuyersResp) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.infoMsg==null && other.getInfoMsg()==null) || 
             (this.infoMsg!=null &&
              this.infoMsg.equals(other.getInfoMsg()))) &&
            ((this.buyers==null && other.getBuyers()==null) || 
             (this.buyers!=null &&
              java.util.Arrays.equals(this.buyers, other.getBuyers())));
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
        if (getInfoMsg() != null) {
            _hashCode += getInfoMsg().hashCode();
        }
        if (getBuyers() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getBuyers());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getBuyers(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(T4PGetBuyersResp.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tcserver.nio.com", "T4PGetBuyersResp"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("infoMsg");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "infoMsg"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("buyers");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "buyers"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tcserver.nio.com", "T4PBuyerInfo"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
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
