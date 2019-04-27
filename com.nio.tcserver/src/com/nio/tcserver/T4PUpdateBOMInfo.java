/**
 * T4PUpdateBOMInfo.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.nio.tcserver;

public class T4PUpdateBOMInfo  implements java.io.Serializable {
    private java.lang.String parentPartNo;

    private java.lang.String parentRevId;

    private com.nio.tcserver.T4PBOMLine[] childLines;

    public T4PUpdateBOMInfo() {
    }

    public T4PUpdateBOMInfo(
           java.lang.String parentPartNo,
           java.lang.String parentRevId,
           com.nio.tcserver.T4PBOMLine[] childLines) {
           this.parentPartNo = parentPartNo;
           this.parentRevId = parentRevId;
           this.childLines = childLines;
    }


    /**
     * Gets the parentPartNo value for this T4PUpdateBOMInfo.
     * 
     * @return parentPartNo
     */
    public java.lang.String getParentPartNo() {
        return parentPartNo;
    }


    /**
     * Sets the parentPartNo value for this T4PUpdateBOMInfo.
     * 
     * @param parentPartNo
     */
    public void setParentPartNo(java.lang.String parentPartNo) {
        this.parentPartNo = parentPartNo;
    }


    /**
     * Gets the parentRevId value for this T4PUpdateBOMInfo.
     * 
     * @return parentRevId
     */
    public java.lang.String getParentRevId() {
        return parentRevId;
    }


    /**
     * Sets the parentRevId value for this T4PUpdateBOMInfo.
     * 
     * @param parentRevId
     */
    public void setParentRevId(java.lang.String parentRevId) {
        this.parentRevId = parentRevId;
    }


    /**
     * Gets the childLines value for this T4PUpdateBOMInfo.
     * 
     * @return childLines
     */
    public com.nio.tcserver.T4PBOMLine[] getChildLines() {
        return childLines;
    }


    /**
     * Sets the childLines value for this T4PUpdateBOMInfo.
     * 
     * @param childLines
     */
    public void setChildLines(com.nio.tcserver.T4PBOMLine[] childLines) {
        this.childLines = childLines;
    }

    public com.nio.tcserver.T4PBOMLine getChildLines(int i) {
        return this.childLines[i];
    }

    public void setChildLines(int i, com.nio.tcserver.T4PBOMLine _value) {
        this.childLines[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof T4PUpdateBOMInfo)) return false;
        T4PUpdateBOMInfo other = (T4PUpdateBOMInfo) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.parentPartNo==null && other.getParentPartNo()==null) || 
             (this.parentPartNo!=null &&
              this.parentPartNo.equals(other.getParentPartNo()))) &&
            ((this.parentRevId==null && other.getParentRevId()==null) || 
             (this.parentRevId!=null &&
              this.parentRevId.equals(other.getParentRevId()))) &&
            ((this.childLines==null && other.getChildLines()==null) || 
             (this.childLines!=null &&
              java.util.Arrays.equals(this.childLines, other.getChildLines())));
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
        if (getParentPartNo() != null) {
            _hashCode += getParentPartNo().hashCode();
        }
        if (getParentRevId() != null) {
            _hashCode += getParentRevId().hashCode();
        }
        if (getChildLines() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getChildLines());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getChildLines(), i);
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
        new org.apache.axis.description.TypeDesc(T4PUpdateBOMInfo.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tcserver.nio.com", "T4PUpdateBOMInfo"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("parentPartNo");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "parentPartNo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("parentRevId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "parentRevId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("childLines");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "childLines"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tcserver.nio.com", "T4PBOMLine"));
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
