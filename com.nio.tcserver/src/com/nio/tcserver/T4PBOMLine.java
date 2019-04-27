/**
 * T4PBOMLine.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.nio.tcserver;

public class T4PBOMLine  implements java.io.Serializable {
    private com.nio.tcserver.T4PAttrProperty[] attrs;

    private java.lang.String partNo;

    public T4PBOMLine() {
    }

    public T4PBOMLine(
           com.nio.tcserver.T4PAttrProperty[] attrs,
           java.lang.String partNo) {
           this.attrs = attrs;
           this.partNo = partNo;
    }


    /**
     * Gets the attrs value for this T4PBOMLine.
     * 
     * @return attrs
     */
    public com.nio.tcserver.T4PAttrProperty[] getAttrs() {
        return attrs;
    }


    /**
     * Sets the attrs value for this T4PBOMLine.
     * 
     * @param attrs
     */
    public void setAttrs(com.nio.tcserver.T4PAttrProperty[] attrs) {
        this.attrs = attrs;
    }

    public com.nio.tcserver.T4PAttrProperty getAttrs(int i) {
        return this.attrs[i];
    }

    public void setAttrs(int i, com.nio.tcserver.T4PAttrProperty _value) {
        this.attrs[i] = _value;
    }


    /**
     * Gets the partNo value for this T4PBOMLine.
     * 
     * @return partNo
     */
    public java.lang.String getPartNo() {
        return partNo;
    }


    /**
     * Sets the partNo value for this T4PBOMLine.
     * 
     * @param partNo
     */
    public void setPartNo(java.lang.String partNo) {
        this.partNo = partNo;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof T4PBOMLine)) return false;
        T4PBOMLine other = (T4PBOMLine) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.attrs==null && other.getAttrs()==null) || 
             (this.attrs!=null &&
              java.util.Arrays.equals(this.attrs, other.getAttrs()))) &&
            ((this.partNo==null && other.getPartNo()==null) || 
             (this.partNo!=null &&
              this.partNo.equals(other.getPartNo())));
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
        if (getAttrs() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAttrs());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAttrs(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getPartNo() != null) {
            _hashCode += getPartNo().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(T4PBOMLine.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tcserver.nio.com", "T4PBOMLine"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("attrs");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "attrs"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tcserver.nio.com", "T4PAttrProperty"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("partNo");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "partNo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
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
