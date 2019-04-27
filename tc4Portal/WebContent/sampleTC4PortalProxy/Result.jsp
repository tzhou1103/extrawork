<%@page contentType="text/html;charset=UTF-8"%>
<% request.setCharacterEncoding("UTF-8"); %>
<HTML>
<HEAD>
<TITLE>Result</TITLE>
</HEAD>
<BODY>
<H1>Result</H1>

<jsp:useBean id="sampleTC4PortalProxyid" scope="session" class="com.nio.tcserver.TC4PortalProxy" />
<%
if (request.getParameter("endpoint") != null && request.getParameter("endpoint").length() > 0)
sampleTC4PortalProxyid.setEndpoint(request.getParameter("endpoint"));
%>

<%
String method = request.getParameter("method");
int methodID = 0;
if (method == null) methodID = -1;

if(methodID != -1) methodID = Integer.parseInt(method);
boolean gotMethod = false;

try {
switch (methodID){ 
case 2:
        gotMethod = true;
        java.lang.String getEndpoint2mtemp = sampleTC4PortalProxyid.getEndpoint();
if(getEndpoint2mtemp == null){
%>
<%=getEndpoint2mtemp %>
<%
}else{
        String tempResultreturnp3 = org.eclipse.jst.ws.util.JspUtils.markup(String.valueOf(getEndpoint2mtemp));
        %>
        <%= tempResultreturnp3 %>
        <%
}
break;
case 5:
        gotMethod = true;
        String endpoint_0id=  request.getParameter("endpoint8");
            java.lang.String endpoint_0idTemp = null;
        if(!endpoint_0id.equals("")){
         endpoint_0idTemp  = endpoint_0id;
        }
        sampleTC4PortalProxyid.setEndpoint(endpoint_0idTemp);
break;
case 10:
        gotMethod = true;
        com.nio.tcserver.TC4Portal getTC4Portal10mtemp = sampleTC4PortalProxyid.getTC4Portal();
if(getTC4Portal10mtemp == null){
%>
<%=getTC4Portal10mtemp %>
<%
}else{
%>
<TABLE>
<TR>
<TD COLSPAN="3" ALIGN="LEFT">returnp:</TD>
</TABLE>
<%
}
break;
case 25:
        gotMethod = true;
        String object_type_1id=  request.getParameter("object_type34");
            java.lang.String object_type_1idTemp = null;
        if(!object_type_1id.equals("")){
         object_type_1idTemp  = object_type_1id;
        }
        String owning_user_2id=  request.getParameter("owning_user36");
            java.lang.String owning_user_2idTemp = null;
        if(!owning_user_2id.equals("")){
         owning_user_2idTemp  = owning_user_2id;
        }
        String owning_group_3id=  request.getParameter("owning_group38");
            java.lang.String owning_group_3idTemp = null;
        if(!owning_group_3id.equals("")){
         owning_group_3idTemp  = owning_group_3id;
        }
        String crNum_4id=  request.getParameter("crNum40");
            java.lang.String crNum_4idTemp = null;
        if(!crNum_4id.equals("")){
         crNum_4idTemp  = crNum_4id;
        }
        com.nio.tcserver.T4PCreatePartResp getCreateInfo25mtemp = sampleTC4PortalProxyid.getCreateInfo(object_type_1idTemp,owning_user_2idTemp,owning_group_3idTemp,crNum_4idTemp);
if(getCreateInfo25mtemp == null){
%>
<%=getCreateInfo25mtemp %>
<%
}else{
%>
<TABLE>
<TR>
<TD COLSPAN="3" ALIGN="LEFT">returnp:</TD>
<TR>
<TD WIDTH="5%"></TD>
<TD COLSPAN="2" ALIGN="LEFT">item_revision_id:</TD>
<TD>
<%
if(getCreateInfo25mtemp != null){
java.lang.String typeitem_revision_id28 = getCreateInfo25mtemp.getItem_revision_id();
        String tempResultitem_revision_id28 = org.eclipse.jst.ws.util.JspUtils.markup(String.valueOf(typeitem_revision_id28));
        %>
        <%= tempResultitem_revision_id28 %>
        <%
}%>
</TD>
<TR>
<TD WIDTH="5%"></TD>
<TD COLSPAN="2" ALIGN="LEFT">item_id:</TD>
<TD>
<%
if(getCreateInfo25mtemp != null){
java.lang.String typeitem_id30 = getCreateInfo25mtemp.getItem_id();
        String tempResultitem_id30 = org.eclipse.jst.ws.util.JspUtils.markup(String.valueOf(typeitem_id30));
        %>
        <%= tempResultitem_id30 %>
        <%
}%>
</TD>
<TR>
<TD WIDTH="5%"></TD>
<TD COLSPAN="2" ALIGN="LEFT">attrDescList:</TD>
<TD>
<%
if(getCreateInfo25mtemp != null){
com.nio.tcserver.T4PAttrDesc[] typeattrDescList32 = getCreateInfo25mtemp.getAttrDescList();
        String tempattrDescList32 = null;
        if(typeattrDescList32 != null){
        java.util.List listattrDescList32= java.util.Arrays.asList(typeattrDescList32);
        tempattrDescList32 = listattrDescList32.toString();
        }
        %>
        <%=tempattrDescList32%>
        <%
}%>
</TD>
</TABLE>
<%
}
break;
case 42:
        gotMethod = true;
        String item_id_5id=  request.getParameter("item_id49");
            java.lang.String item_id_5idTemp = null;
        if(!item_id_5id.equals("")){
         item_id_5idTemp  = item_id_5id;
        }
        String item_revision_id_6id=  request.getParameter("item_revision_id51");
            java.lang.String item_revision_id_6idTemp = null;
        if(!item_revision_id_6id.equals("")){
         item_revision_id_6idTemp  = item_revision_id_6id;
        }
        com.nio.tcserver.T4PPartAttrsOutput getPartAttrsAll42mtemp = sampleTC4PortalProxyid.getPartAttrsAll(item_id_5idTemp,item_revision_id_6idTemp);
if(getPartAttrsAll42mtemp == null){
%>
<%=getPartAttrsAll42mtemp %>
<%
}else{
%>
<TABLE>
<TR>
<TD COLSPAN="3" ALIGN="LEFT">returnp:</TD>
<TR>
<TD WIDTH="5%"></TD>
<TD COLSPAN="2" ALIGN="LEFT">item_revision_id:</TD>
<TD>
<%
if(getPartAttrsAll42mtemp != null){
java.lang.String typeitem_revision_id45 = getPartAttrsAll42mtemp.getItem_revision_id();
        String tempResultitem_revision_id45 = org.eclipse.jst.ws.util.JspUtils.markup(String.valueOf(typeitem_revision_id45));
        %>
        <%= tempResultitem_revision_id45 %>
        <%
}%>
</TD>
<TR>
<TD WIDTH="5%"></TD>
<TD COLSPAN="2" ALIGN="LEFT">item_id:</TD>
<TD>
<%
if(getPartAttrsAll42mtemp != null){
java.lang.String typeitem_id47 = getPartAttrsAll42mtemp.getItem_id();
        String tempResultitem_id47 = org.eclipse.jst.ws.util.JspUtils.markup(String.valueOf(typeitem_id47));
        %>
        <%= tempResultitem_id47 %>
        <%
}%>
</TD>
</TABLE>
<%
}
break;
case 53:
        gotMethod = true;
        com.nio.tcserver.T4PGetBuyersResp getBuyers53mtemp = sampleTC4PortalProxyid.getBuyers();
if(getBuyers53mtemp == null){
%>
<%=getBuyers53mtemp %>
<%
}else{
%>
<TABLE>
<TR>
<TD COLSPAN="3" ALIGN="LEFT">returnp:</TD>
<TR>
<TD WIDTH="5%"></TD>
<TD COLSPAN="2" ALIGN="LEFT">infoMsg:</TD>
<TD>
<%
if(getBuyers53mtemp != null){
java.lang.String typeinfoMsg56 = getBuyers53mtemp.getInfoMsg();
        String tempResultinfoMsg56 = org.eclipse.jst.ws.util.JspUtils.markup(String.valueOf(typeinfoMsg56));
        %>
        <%= tempResultinfoMsg56 %>
        <%
}%>
</TD>
</TABLE>
<%
}
break;
case 58:
        gotMethod = true;
        com.nio.tcserver.T4PGetSuppliersResp getSuppliers58mtemp = sampleTC4PortalProxyid.getSuppliers();
if(getSuppliers58mtemp == null){
%>
<%=getSuppliers58mtemp %>
<%
}else{
%>
<TABLE>
<TR>
<TD COLSPAN="3" ALIGN="LEFT">returnp:</TD>
<TR>
<TD WIDTH="5%"></TD>
<TD COLSPAN="2" ALIGN="LEFT">infoMsg:</TD>
<TD>
<%
if(getSuppliers58mtemp != null){
java.lang.String typeinfoMsg61 = getSuppliers58mtemp.getInfoMsg();
        String tempResultinfoMsg61 = org.eclipse.jst.ws.util.JspUtils.markup(String.valueOf(typeinfoMsg61));
        %>
        <%= tempResultinfoMsg61 %>
        <%
}%>
</TD>
</TABLE>
<%
}
break;
case 63:
        gotMethod = true;
        com.nio.tcserver.T4PGetMaterialsResp getMaterials63mtemp = sampleTC4PortalProxyid.getMaterials();
if(getMaterials63mtemp == null){
%>
<%=getMaterials63mtemp %>
<%
}else{
%>
<TABLE>
<TR>
<TD COLSPAN="3" ALIGN="LEFT">returnp:</TD>
<TR>
<TD WIDTH="5%"></TD>
<TD COLSPAN="2" ALIGN="LEFT">infoMsg:</TD>
<TD>
<%
if(getMaterials63mtemp != null){
java.lang.String typeinfoMsg66 = getMaterials63mtemp.getInfoMsg();
        String tempResultinfoMsg66 = org.eclipse.jst.ws.util.JspUtils.markup(String.valueOf(typeinfoMsg66));
        %>
        <%= tempResultinfoMsg66 %>
        <%
}%>
</TD>
</TABLE>
<%
}
break;
case 68:
        gotMethod = true;
        String item_id_7id=  request.getParameter("item_id71");
            java.lang.String item_id_7idTemp = null;
        if(!item_id_7id.equals("")){
         item_id_7idTemp  = item_id_7id;
        }
        String item_revision_id_8id=  request.getParameter("item_revision_id73");
            java.lang.String item_revision_id_8idTemp = null;
        if(!item_revision_id_8id.equals("")){
         item_revision_id_8idTemp  = item_revision_id_8id;
        }
        com.nio.tcserver.T4PSetPartObsoleteResp setPartObsolete68mtemp = sampleTC4PortalProxyid.setPartObsolete(item_id_7idTemp,item_revision_id_8idTemp);
if(setPartObsolete68mtemp == null){
%>
<%=setPartObsolete68mtemp %>
<%
}else{
        if(setPartObsolete68mtemp!= null){
        String tempreturnp69 = setPartObsolete68mtemp.toString();
        %>
        <%=tempreturnp69%>
        <%
        }}
break;
case 75:
        gotMethod = true;
        String item_id_9id=  request.getParameter("item_id78");
            java.lang.String item_id_9idTemp = null;
        if(!item_id_9id.equals("")){
         item_id_9idTemp  = item_id_9id;
        }
        String item_revision_id_10id=  request.getParameter("item_revision_id80");
            java.lang.String item_revision_id_10idTemp = null;
        if(!item_revision_id_10id.equals("")){
         item_revision_id_10idTemp  = item_revision_id_10id;
        }
        com.nio.tcserver.T4PGetPartPdfResp getPartPdf75mtemp = sampleTC4PortalProxyid.getPartPdf(item_id_9idTemp,item_revision_id_10idTemp);
if(getPartPdf75mtemp == null){
%>
<%=getPartPdf75mtemp %>
<%
}else{
        if(getPartPdf75mtemp!= null){
        String tempreturnp76 = getPartPdf75mtemp.toString();
        %>
        <%=tempreturnp76%>
        <%
        }}
break;
case 82:
        gotMethod = true;
        String item_id_11id=  request.getParameter("item_id85");
            java.lang.String item_id_11idTemp = null;
        if(!item_id_11id.equals("")){
         item_id_11idTemp  = item_id_11id;
        }
        String old_rev_id_12id=  request.getParameter("old_rev_id87");
            java.lang.String old_rev_id_12idTemp = null;
        if(!old_rev_id_12id.equals("")){
         old_rev_id_12idTemp  = old_rev_id_12id;
        }
        String new_rev_id_13id=  request.getParameter("new_rev_id89");
            java.lang.String new_rev_id_13idTemp = null;
        if(!new_rev_id_13id.equals("")){
         new_rev_id_13idTemp  = new_rev_id_13id;
        }
        com.nio.tcserver.T4PRevisePartResp revisePart82mtemp = sampleTC4PortalProxyid.revisePart(item_id_11idTemp,old_rev_id_12idTemp,new_rev_id_13idTemp);
if(revisePart82mtemp == null){
%>
<%=revisePart82mtemp %>
<%
}else{
        if(revisePart82mtemp!= null){
        String tempreturnp83 = revisePart82mtemp.toString();
        %>
        <%=tempreturnp83%>
        <%
        }}
break;
}
} catch (Exception e) { 
%>
Exception: <%= org.eclipse.jst.ws.util.JspUtils.markup(e.toString()) %>
Message: <%= org.eclipse.jst.ws.util.JspUtils.markup(e.getMessage()) %>
<%
return;
}
if(!gotMethod){
%>
result: N/A
<%
}
%>
</BODY>
</HTML>