/**
 * T4PCreatePartResp.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.nio.tcserver;

public class T4PCreatePartResp  implements java.io.Serializable {
    private java.lang.String item_id;

    private java.lang.String item_revision_id;

    private com.nio.tcserver.T4PAttrDesc[] attrDescList;

    public T4PCreatePartResp() {
    }

    public T4PCreatePartResp(
           java.lang.String item_id,
           java.lang.String item_revision_id,
           com.nio.tcserver.T4PAttrDesc[] attrDescList) {
           this.item_id = item_id;
           this.item_revision_id = item_revision_id;
           this.attrDescList = attrDescList;
    }


    /**
     * Gets the item_id value for this T4PCreatePartResp.
     * 
     * @return item_id
     */
    public java.lang.String getItem_id() {
        return item_id;
    }


    /**
     * Sets the item_id value for this T4PCreatePartResp.
     * 
     * @param item_id
     */
    public void setItem_id(java.lang.String item_id) {
        this.item_id = item_id;
    }


    /**
     * Gets the item_revision_id value for this T4PCreatePartResp.
     * 
     * @return item_revision_id
     */
    public java.lang.String getItem_revision_id() {
        return item_revision_id;
    }


    /**
     * Sets the item_revision_id value for this T4PCreatePartResp.
     * 
     * @param item_revision_id
     */
    public void setItem_revision_id(java.lang.String item_revision_id) {
        this.item_revision_id = item_revision_id;
    }


    /**
     * Gets the attrDescList value for this T4PCreatePartResp.
     * 
     * @return attrDescList
     */
    public com.nio.tcserver.T4PAttrDesc[] getAttrDescList() {
        return attrDescList;
    }


    /**
     * Sets the attrDescList value for this T4PCreatePartResp.
     * 
     * @param attrDescList
     */
    public void setAttrDescList(com.nio.tcserver.T4PAttrDesc[] attrDescList) {
        this.attrDescList = attrDescList;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof T4PCreatePartResp)) return false;
        T4PCreatePartResp other = (T4PCreatePartResp) obj;
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
            ((this.attrDescList==null && other.getAttrDescList()==null) || 
             (this.attrDescList!=null &&
              java.util.Arrays.equals(this.attrDescList, other.getAttrDescList())));
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
        if (getAttrDescList() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAttrDescList());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAttrDescList(), i);
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
        new org.apache.axis.description.TypeDesc(T4PCreatePartResp.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tcserver.nio.com", "T4PCreatePartResp"));
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
        elemField.setFieldName("attrDescList");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "attrDescList"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tcserver.nio.com", "T4PAttrDesc"));
        elemField.setNillable(true);
        elemField.setItemQName(new javax.xml.namespace.QName("http://tcserver.nio.com", "item"));
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
