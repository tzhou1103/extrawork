/**
 * T4PGetSuppliersResp.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.nio.tcserver;

public class T4PGetSuppliersResp  implements java.io.Serializable {
    private java.lang.String infoMsg;

    private com.nio.tcserver.T4PSupplierInfo[] suppliers;

    public T4PGetSuppliersResp() {
    }

    public T4PGetSuppliersResp(
           java.lang.String infoMsg,
           com.nio.tcserver.T4PSupplierInfo[] suppliers) {
           this.infoMsg = infoMsg;
           this.suppliers = suppliers;
    }


    /**
     * Gets the infoMsg value for this T4PGetSuppliersResp.
     * 
     * @return infoMsg
     */
    public java.lang.String getInfoMsg() {
        return infoMsg;
    }


    /**
     * Sets the infoMsg value for this T4PGetSuppliersResp.
     * 
     * @param infoMsg
     */
    public void setInfoMsg(java.lang.String infoMsg) {
        this.infoMsg = infoMsg;
    }


    /**
     * Gets the suppliers value for this T4PGetSuppliersResp.
     * 
     * @return suppliers
     */
    public com.nio.tcserver.T4PSupplierInfo[] getSuppliers() {
        return suppliers;
    }


    /**
     * Sets the suppliers value for this T4PGetSuppliersResp.
     * 
     * @param suppliers
     */
    public void setSuppliers(com.nio.tcserver.T4PSupplierInfo[] suppliers) {
        this.suppliers = suppliers;
    }

    public com.nio.tcserver.T4PSupplierInfo getSuppliers(int i) {
        return this.suppliers[i];
    }

    public void setSuppliers(int i, com.nio.tcserver.T4PSupplierInfo _value) {
        this.suppliers[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof T4PGetSuppliersResp)) return false;
        T4PGetSuppliersResp other = (T4PGetSuppliersResp) obj;
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
            ((this.suppliers==null && other.getSuppliers()==null) || 
             (this.suppliers!=null &&
              java.util.Arrays.equals(this.suppliers, other.getSuppliers())));
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
        if (getSuppliers() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getSuppliers());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getSuppliers(), i);
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
        new org.apache.axis.description.TypeDesc(T4PGetSuppliersResp.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tcserver.nio.com", "T4PGetSuppliersResp"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("infoMsg");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "infoMsg"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("suppliers");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "suppliers"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tcserver.nio.com", "T4PSupplierInfo"));
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
