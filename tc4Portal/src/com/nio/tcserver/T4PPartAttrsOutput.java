/**
 * T4PPartAttrsOutput.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.nio.tcserver;

public class T4PPartAttrsOutput  implements java.io.Serializable {
    private java.lang.String item_id;

    private java.lang.String item_revision_id;

    private com.nio.tcserver.T4PAttrProperty[] attr_properties;

    public T4PPartAttrsOutput() {
    }

    public T4PPartAttrsOutput(
           java.lang.String item_id,
           java.lang.String item_revision_id,
           com.nio.tcserver.T4PAttrProperty[] attr_properties) {
           this.item_id = item_id;
           this.item_revision_id = item_revision_id;
           this.attr_properties = attr_properties;
    }


    /**
     * Gets the item_id value for this T4PPartAttrsOutput.
     * 
     * @return item_id
     */
    public java.lang.String getItem_id() {
        return item_id;
    }


    /**
     * Sets the item_id value for this T4PPartAttrsOutput.
     * 
     * @param item_id
     */
    public void setItem_id(java.lang.String item_id) {
        this.item_id = item_id;
    }


    /**
     * Gets the item_revision_id value for this T4PPartAttrsOutput.
     * 
     * @return item_revision_id
     */
    public java.lang.String getItem_revision_id() {
        return item_revision_id;
    }


    /**
     * Sets the item_revision_id value for this T4PPartAttrsOutput.
     * 
     * @param item_revision_id
     */
    public void setItem_revision_id(java.lang.String item_revision_id) {
        this.item_revision_id = item_revision_id;
    }


    /**
     * Gets the attr_properties value for this T4PPartAttrsOutput.
     * 
     * @return attr_properties
     */
    public com.nio.tcserver.T4PAttrProperty[] getAttr_properties() {
        return attr_properties;
    }


    /**
     * Sets the attr_properties value for this T4PPartAttrsOutput.
     * 
     * @param attr_properties
     */
    public void setAttr_properties(com.nio.tcserver.T4PAttrProperty[] attr_properties) {
        this.attr_properties = attr_properties;
    }

    public com.nio.tcserver.T4PAttrProperty getAttr_properties(int i) {
        return this.attr_properties[i];
    }

    public void setAttr_properties(int i, com.nio.tcserver.T4PAttrProperty _value) {
        this.attr_properties[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof T4PPartAttrsOutput)) return false;
        T4PPartAttrsOutput other = (T4PPartAttrsOutput) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.item_id==null && other.getItem_id()==null) || 
             (this.item_id!=null &&
              this.item_id.equals(other.getItem_id()))) &&
            ((this.item_revision_id==null && other.getItem_revision_id()==null) || 
             (this.item_revision_id!=null &&
              this.item_revision_id.equals(other.getItem_revision_id()))) &&
            ((this.attr_properties==null && other.getAttr_properties()==null) || 
             (this.attr_properties!=null &&
              java.util.Arrays.equals(this.attr_properties, other.getAttr_properties())));
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
        if (getItem_id() != null) {
            _hashCode += getItem_id().hashCode();
        }
        if (getItem_revision_id() != null) {
            _hashCode += getItem_revision_id().hashCode();
        }
        if (getAttr_properties() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAttr_properties());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAttr_properties(), i);
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
        new org.apache.axis.description.TypeDesc(T4PPartAttrsOutput.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tcserver.nio.com", "T4PPartAttrsOutput"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("item_id");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "item_id"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("item_revision_id");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "item_revision_id"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("attr_properties");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "attr_properties"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tcserver.nio.com", "T4PAttrProperty"));
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
