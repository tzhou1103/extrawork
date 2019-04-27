/**
 * T4PMaterialInfo.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.nio.tcserver;

public class T4PMaterialInfo  implements java.io.Serializable {
    private java.lang.String id;

    private java.lang.String item;

    private java.lang.String name;

    private java.lang.String density;

    private java.lang.String lv1;

    private java.lang.String lv2;

    private java.lang.String lv3;

    private java.lang.String lv4;

    private java.lang.String lv5;

    private java.lang.String lv6;

    private java.lang.String grplv1;

    private java.lang.String spec;

    public T4PMaterialInfo() {
    }

    public T4PMaterialInfo(
           java.lang.String id,
           java.lang.String item,
           java.lang.String name,
           java.lang.String density,
           java.lang.String lv1,
           java.lang.String lv2,
           java.lang.String lv3,
           java.lang.String lv4,
           java.lang.String lv5,
           java.lang.String lv6,
           java.lang.String grplv1,
           java.lang.String spec) {
           this.id = id;
           this.item = item;
           this.name = name;
           this.density = density;
           this.lv1 = lv1;
           this.lv2 = lv2;
           this.lv3 = lv3;
           this.lv4 = lv4;
           this.lv5 = lv5;
           this.lv6 = lv6;
           this.grplv1 = grplv1;
           this.spec = spec;
    }


    /**
     * Gets the id value for this T4PMaterialInfo.
     * 
     * @return id
     */
    public java.lang.String getId() {
        return id;
    }


    /**
     * Sets the id value for this T4PMaterialInfo.
     * 
     * @param id
     */
    public void setId(java.lang.String id) {
        this.id = id;
    }


    /**
     * Gets the item value for this T4PMaterialInfo.
     * 
     * @return item
     */
    public java.lang.String getItem() {
        return item;
    }


    /**
     * Sets the item value for this T4PMaterialInfo.
     * 
     * @param item
     */
    public void setItem(java.lang.String item) {
        this.item = item;
    }


    /**
     * Gets the name value for this T4PMaterialInfo.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this T4PMaterialInfo.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the density value for this T4PMaterialInfo.
     * 
     * @return density
     */
    public java.lang.String getDensity() {
        return density;
    }


    /**
     * Sets the density value for this T4PMaterialInfo.
     * 
     * @param density
     */
    public void setDensity(java.lang.String density) {
        this.density = density;
    }


    /**
     * Gets the lv1 value for this T4PMaterialInfo.
     * 
     * @return lv1
     */
    public java.lang.String getLv1() {
        return lv1;
    }


    /**
     * Sets the lv1 value for this T4PMaterialInfo.
     * 
     * @param lv1
     */
    public void setLv1(java.lang.String lv1) {
        this.lv1 = lv1;
    }


    /**
     * Gets the lv2 value for this T4PMaterialInfo.
     * 
     * @return lv2
     */
    public java.lang.String getLv2() {
        return lv2;
    }


    /**
     * Sets the lv2 value for this T4PMaterialInfo.
     * 
     * @param lv2
     */
    public void setLv2(java.lang.String lv2) {
        this.lv2 = lv2;
    }


    /**
     * Gets the lv3 value for this T4PMaterialInfo.
     * 
     * @return lv3
     */
    public java.lang.String getLv3() {
        return lv3;
    }


    /**
     * Sets the lv3 value for this T4PMaterialInfo.
     * 
     * @param lv3
     */
    public void setLv3(java.lang.String lv3) {
        this.lv3 = lv3;
    }


    /**
     * Gets the lv4 value for this T4PMaterialInfo.
     * 
     * @return lv4
     */
    public java.lang.String getLv4() {
        return lv4;
    }


    /**
     * Sets the lv4 value for this T4PMaterialInfo.
     * 
     * @param lv4
     */
    public void setLv4(java.lang.String lv4) {
        this.lv4 = lv4;
    }


    /**
     * Gets the lv5 value for this T4PMaterialInfo.
     * 
     * @return lv5
     */
    public java.lang.String getLv5() {
        return lv5;
    }


    /**
     * Sets the lv5 value for this T4PMaterialInfo.
     * 
     * @param lv5
     */
    public void setLv5(java.lang.String lv5) {
        this.lv5 = lv5;
    }


    /**
     * Gets the lv6 value for this T4PMaterialInfo.
     * 
     * @return lv6
     */
    public java.lang.String getLv6() {
        return lv6;
    }


    /**
     * Sets the lv6 value for this T4PMaterialInfo.
     * 
     * @param lv6
     */
    public void setLv6(java.lang.String lv6) {
        this.lv6 = lv6;
    }


    /**
     * Gets the grplv1 value for this T4PMaterialInfo.
     * 
     * @return grplv1
     */
    public java.lang.String getGrplv1() {
        return grplv1;
    }


    /**
     * Sets the grplv1 value for this T4PMaterialInfo.
     * 
     * @param grplv1
     */
    public void setGrplv1(java.lang.String grplv1) {
        this.grplv1 = grplv1;
    }


    /**
     * Gets the spec value for this T4PMaterialInfo.
     * 
     * @return spec
     */
    public java.lang.String getSpec() {
        return spec;
    }


    /**
     * Sets the spec value for this T4PMaterialInfo.
     * 
     * @param spec
     */
    public void setSpec(java.lang.String spec) {
        this.spec = spec;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof T4PMaterialInfo)) return false;
        T4PMaterialInfo other = (T4PMaterialInfo) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.id==null && other.getId()==null) || 
             (this.id!=null &&
              this.id.equals(other.getId()))) &&
            ((this.item==null && other.getItem()==null) || 
             (this.item!=null &&
              this.item.equals(other.getItem()))) &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.density==null && other.getDensity()==null) || 
             (this.density!=null &&
              this.density.equals(other.getDensity()))) &&
            ((this.lv1==null && other.getLv1()==null) || 
             (this.lv1!=null &&
              this.lv1.equals(other.getLv1()))) &&
            ((this.lv2==null && other.getLv2()==null) || 
             (this.lv2!=null &&
              this.lv2.equals(other.getLv2()))) &&
            ((this.lv3==null && other.getLv3()==null) || 
             (this.lv3!=null &&
              this.lv3.equals(other.getLv3()))) &&
            ((this.lv4==null && other.getLv4()==null) || 
             (this.lv4!=null &&
              this.lv4.equals(other.getLv4()))) &&
            ((this.lv5==null && other.getLv5()==null) || 
             (this.lv5!=null &&
              this.lv5.equals(other.getLv5()))) &&
            ((this.lv6==null && other.getLv6()==null) || 
             (this.lv6!=null &&
              this.lv6.equals(other.getLv6()))) &&
            ((this.grplv1==null && other.getGrplv1()==null) || 
             (this.grplv1!=null &&
              this.grplv1.equals(other.getGrplv1()))) &&
            ((this.spec==null && other.getSpec()==null) || 
             (this.spec!=null &&
              this.spec.equals(other.getSpec())));
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
        if (getId() != null) {
            _hashCode += getId().hashCode();
        }
        if (getItem() != null) {
            _hashCode += getItem().hashCode();
        }
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getDensity() != null) {
            _hashCode += getDensity().hashCode();
        }
        if (getLv1() != null) {
            _hashCode += getLv1().hashCode();
        }
        if (getLv2() != null) {
            _hashCode += getLv2().hashCode();
        }
        if (getLv3() != null) {
            _hashCode += getLv3().hashCode();
        }
        if (getLv4() != null) {
            _hashCode += getLv4().hashCode();
        }
        if (getLv5() != null) {
            _hashCode += getLv5().hashCode();
        }
        if (getLv6() != null) {
            _hashCode += getLv6().hashCode();
        }
        if (getGrplv1() != null) {
            _hashCode += getGrplv1().hashCode();
        }
        if (getSpec() != null) {
            _hashCode += getSpec().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(T4PMaterialInfo.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tcserver.nio.com", "T4PMaterialInfo"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("id");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "id"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("item");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "item"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("density");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "density"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lv1");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "lv1"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lv2");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "lv2"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lv3");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "lv3"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lv4");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "lv4"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lv5");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "lv5"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lv6");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "lv6"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("grplv1");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "grplv1"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("spec");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tcserver.nio.com", "spec"));
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
