<%--
  ~ Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
  ~ With modifications by Bosch Software Innovations GmbH, 2016.
  ~
  ~ This program is free software; you can redistribute it and/or modify it under
  ~ the terms of the GNU General Public License Version 2.0 as published by the
  ~ Free Software Foundation with classpath exception.
  ~
  ~ This program is distributed in the hope that it will be useful, but WITHOUT
  ~ ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  ~ FOR A PARTICULAR PURPOSE. See the GNU General Public License version 2.0 for
  ~ more details.
  ~
  ~ You should have received a copy of the GNU General Public License along with
  ~ this program (please see the COPYING file); if not, write to the Free
  ~ Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
  ~ 02110-1301, USA.
  --%>
<%@ page import="com.siemens.sw360.portal.common.PortalConstants" %>
<%@include file="/html/init.jsp" %>

<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<jsp:useBean id="selectedTab" class="java.lang.String" scope="request"/>
<jsp:useBean id="licenseDetail" class="com.siemens.sw360.datahandler.thrift.licenses.License" scope="request"/>
<jsp:useBean id="moderationLicenseDetail" class="com.siemens.sw360.datahandler.thrift.licenses.License"
             scope="request"/>
<jsp:useBean id="added_todos_from_moderation_request"
             type="java.util.List<com.siemens.sw360.datahandler.thrift.licenses.Todo>" scope="request"/>
<jsp:useBean id="db_todos_from_moderation_request"
             type="java.util.List<com.siemens.sw360.datahandler.thrift.licenses.Todo>" scope="request"/>
<jsp:useBean id="isUserAtLeastClearingAdmin" class="java.lang.String" scope="request"/>
<jsp:useBean id="obligationList" type="java.util.List<com.siemens.sw360.datahandler.thrift.licenses.Obligation>"
             scope="request"/>

<portlet:actionURL var="editLicenseTodosURL" name="updateWhiteList">
    <portlet:param name="<%=PortalConstants.LICENSE_ID%>" value="${licenseDetail.id}"/>
</portlet:actionURL>

<portlet:actionURL var="addLicenseTodoURL" name="addTodo">
    <portlet:param name="<%=PortalConstants.LICENSE_ID%>" value="${licenseDetail.id}"/>
</portlet:actionURL>

<portlet:actionURL var="changeLicenseTextURL" name="changeText">
    <portlet:param name="<%=PortalConstants.LICENSE_ID%>" value="${licenseDetail.id}"/>
</portlet:actionURL>

<portlet:actionURL var="editExternalLinkURL" name="editExternalLink">
    <portlet:param name="<%=PortalConstants.LICENSE_ID%>" value="${licenseDetail.id}" />
</portlet:actionURL>

<div id="header"></div>
<p class="pageHeader"><span
        class="pageHeaderBigSpan">License: ${licenseDetail.fullname} (${licenseDetail.shortname})</span>
    <core_rt:if test="${isUserAtLeastClearingAdmin == 'Yes'}">
         <span class="pull-right">
             <input type="button" onclick="editLicense()" id="edit" value="Edit License Details and Text"
                    class="addButton">
         </span>
    </core_rt:if>
</p>
<core_rt:set var="editMode" value="true" scope="request"/>
<%@include file="includes/detailOverview.jspf" %>

<script>
    var Y = YUI().use(
            'aui-tabview',
            function (Y) {
                new Y.TabView(
                        {
                            srcNode: '#myTab',
                            stacked: true,
                            type: 'tab'
                        }
                ).render();
            }
    );

    function showWhiteListOptions() {
        Y.all('table.todosFromModerationRequest').show();
        Y.all('table.db_table').hide();
    }
    function editLicense() {
        window.location = '<portlet:renderURL >'
                             +'<portlet:param name="<%=PortalConstants.LICENSE_ID%>" value="${licenseDetail.id}"/>'
                             +'<portlet:param name="<%=PortalConstants.PAGENAME%>" value="<%=PortalConstants.PAGENAME_EDIT%>"/>'
                         +'</portlet:renderURL>'
    }
</script>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
