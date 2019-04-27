/**
 * T4PAttrProperty.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.nio.tcserver;

public class T4PAttrProperty  implements java.io.Serializable {
    private java.lang.String attr_name;

    private java.lang.String attr_display_name;

    private java.lang.String[] attr_values;

    public T4PAttrProperty() {
    }

    public T4PAttrProperty(
           java.lang.String attr_name,
           java.lang.String attr_display_name,
           java.lang.String[] attr_values) {
           this.attr_name = attr_name;
           this.attr_display_name = attr_display_name;
           this.attr_values = attr_values;
    }


    /**
     * Gets the attr_name value for this T4PAttrProperty.
     * 
     * @return attr_name
     */
    public java.lang.String getAttr_name() {
        return attr_name;
    }


    /**
     * Sets the attr_name value for this T4PAttrProperty.
     * 
     * @param attr_name
     */
    public void setAttr_name(java.lang.String attr_name) {
        this.attr_name = attr_name;
    }


    /**
     * Gets the attr_display_name value for this T4PAttrProperty.
     * 
     * @return attr_display_name
     */
    public java.lang.String getAttr_display_name() {
        return attr_display_name;
    }


    /**
     * Sets the attr_display_name value for this T4PAttrProperty.
     * 
     * @param attr_display_name
     */
    public void setAttr_display_name(java.lang.String attr_display_name) {
        this.attr_display_name = attr_display_name;
    }


    /**
     * Gets the attr_values value for this T4PAttrProperty.
     * 
     * @return attr_values
     */
    public java.lang.String[] getAttr_values() {
        return attr_values;
    }


    /**
     * Sets the attr_values value for this T4PAttrProperty.
     * 
     * @param attr_values
     */
    public void setAttr_values(java.lang.String[] attr_values) {
        this.attr_values = attr_values;
    }

    public java.lang.String getAttr_values(int i) {
        return this.attr_values[i];
    }

    public void setAttr_values(int i, java.lang.String _value) {
        this.attr_values[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof T4PAttrProperty)) return false;
        T4PAttrProperty other = (T4PAttrProperty) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.attr_name==null && other.getAttr_name()==null) || 
             (this.attr_name!=null &&
              this.attr_name.equals(other.getAttr_name()))) &&
            ((this.attr_display_name==null && other.getAttr_display_name()==null) || 
             (this.attr_display_name!=null &&
              this.attr_display_name.equals(other.getAttr_display_name()))) &&
            ((this.attr_values==null && other.getAttr_values()==null) || 
             (this.attr_values!=null &&
              java.util.Arrays.equals(this.attr_values, other.getAttr_values())));
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
        if (getAttr_name() != null) {
            _hashCode += getAttr_name().hashCode();
        }
        if (getAttr_display_name() != null) {
            _hashCode += getAttr_display_name().hashCode();
        }
        if (getAttr_values() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAttr_values());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAttr_values(), i);
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
        new org.apache.axis.description.TypeDesc(T4PAttrProperty.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tcserver.nio.com", "T4PAttrProperty"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("attr_name");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "attr_name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("attr_display_name");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "attr_display_name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("attr_values");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "attr_values"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
