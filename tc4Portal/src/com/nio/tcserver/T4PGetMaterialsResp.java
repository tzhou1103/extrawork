/**
 * T4PGetMaterialsResp.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.nio.tcserver;

public class T4PGetMaterialsResp  implements java.io.Serializable {
    private java.lang.String infoMsg;

    private com.nio.tcserver.T4PMaterialInfo[] materials;

    public T4PGetMaterialsResp() {
    }

    public T4PGetMaterialsResp(
           java.lang.String infoMsg,
           com.nio.tcserver.T4PMaterialInfo[] materials) {
           this.infoMsg = infoMsg;
           this.materials = materials;
    }


    /**
     * Gets the infoMsg value for this T4PGetMaterialsResp.
     * 
     * @return infoMsg
     */
    public java.lang.String getInfoMsg() {
        return infoMsg;
    }


    /**
     * Sets the infoMsg value for this T4PGetMaterialsResp.
     * 
     * @param infoMsg
     */
    public void setInfoMsg(java.lang.String infoMsg) {
        this.infoMsg = infoMsg;
    }


    /**
     * Gets the materials value for this T4PGetMaterialsResp.
     * 
     * @return materials
     */
    public com.nio.tcserver.T4PMaterialInfo[] getMaterials() {
        return materials;
    }


    /**
     * Sets the materials value for this T4PGetMaterialsResp.
     * 
     * @param materials
     */
    public void setMaterials(com.nio.tcserver.T4PMaterialInfo[] materials) {
        this.materials = materials;
    }

    public com.nio.tcserver.T4PMaterialInfo getMaterials(int i) {
        return this.materials[i];
    }

    public void setMaterials(int i, com.nio.tcserver.T4PMaterialInfo _value) {
        this.materials[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof T4PGetMaterialsResp)) return false;
        T4PGetMaterialsResp other = (T4PGetMaterialsResp) obj;
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
            ((this.materials==null && other.getMaterials()==null) || 
             (this.materials!=null &&
              java.util.Arrays.equals(this.materials, other.getMaterials())));
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
        if (getMaterials() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getMaterials());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getMaterials(), i);
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
        new org.apache.axis.description.TypeDesc(T4PGetMaterialsResp.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tcserver.nio.com", "T4PGetMaterialsResp"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("infoMsg");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "infoMsg"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("materials");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "materials"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tcserver.nio.com", "T4PMaterialInfo"));
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
