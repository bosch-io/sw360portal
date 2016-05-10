/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 * Part of the SW360 Portal Project.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License Version 2.0 as published by the
 * Free Software Foundation with classpath exception.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License version 2.0 for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (please see the COPYING file); if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */
package com.siemens.sw360.portal.portlets.projects;

/**
 * Created by andreas.reichel@tngtech.com on 19.11.15.
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.util.PortalUtil;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.ThriftClients;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.bdpimport.BdpImportService;
import com.siemens.sw360.datahandler.thrift.bdpimport.RemoteCredentials;
import com.siemens.sw360.datahandler.thrift.bdpimportstatus.BdpImportStatus;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.portal.common.PortalConstants;
import com.siemens.sw360.portal.common.UsedAsLiferayAction;
import com.siemens.sw360.portal.portlets.Sw360Portlet;
import com.siemens.sw360.portal.users.UserCacheHolder;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import javax.portlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Strings.nullToEmpty;

public class ProjectImportPortlet extends Sw360Portlet {
    private static final Logger log = Logger.getLogger(ProjectImportPortlet.class);

    static class LoginState {
        private Boolean loggedIn;
        private String loggedInServerUrl;

        public LoginState() {
            loggedIn = false;
            loggedInServerUrl = "";
        }

        public void login(String serverUrl) {
            loggedIn = true;
            loggedInServerUrl = serverUrl;
        }

        public void logout() {
            loggedIn = false;
            loggedInServerUrl = "";
        }

        public String getServerUrl() {
            return loggedInServerUrl;
        }

        boolean isLoggedIn() {
            return loggedIn;
        }

    }

    private RemoteCredentials getRemoteCredentialsFromSession(PortletSession session) {
        String dbUserName = (String) session.getAttribute(PortalConstants.SESSION_IMPORT_USER);
        String dbUserPass = (String) session.getAttribute(PortalConstants.SESSION_IMPORT_PASS);
        String dbUrl = (String) session.getAttribute(PortalConstants.SESSION_IMPORT_URL);

        return new RemoteCredentials()
                .setPassword(dbUserPass)
                .setUsername(dbUserName)
                .setServerUrl(dbUrl);
    }

    private void putRemoteCredentialsIntoSession(PortletSession session, RemoteCredentials remoteCredentials) {
        session.setAttribute(PortalConstants.SESSION_IMPORT_USER, nullToEmpty(remoteCredentials.getUsername()));
        session.setAttribute(PortalConstants.SESSION_IMPORT_PASS, nullToEmpty(remoteCredentials.getPassword()));
        session.setAttribute(PortalConstants.SESSION_IMPORT_URL, nullToEmpty(remoteCredentials.getServerUrl()));
    }

    @Override
    public void doView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        List<Project> importables = new ArrayList<>();
        Boolean loggedIn = false;
        String loggedInServer = "";

        RemoteCredentials reCred = getRemoteCredentialsFromSession(request.getPortletSession());
        if (!nullToEmpty(reCred.getServerUrl()).isEmpty()) {
            importables = loadImportables(reCred);
            loggedIn = true;
            loggedInServer = reCred.getServerUrl();
        }
        request.setAttribute("importables", importables);
        request.setAttribute("loggedIn", loggedIn);
        request.setAttribute("loggedInServer", loggedInServer);

        super.doView(request, response);
    }

    private List<String> getProjectIdsForImport(PortletRequest request) throws PortletException, IOException {
        String[] checked = request.getParameterValues("checked[]");
        List<String> checkedIds = new ArrayList<>();
        if (checked != null) {
            for (String s : checked) {
                checkedIds.add(s.substring(PortalConstants.CHECKED_PROJECT.length()));
            }
        }
        return checkedIds;
    }

    private boolean isImportSuccessful(BdpImportStatus importStatus) {
        return (importStatus.isSetRequestStatus() && importStatus.getRequestStatus().equals(RequestStatus.SUCCESS) && importStatus.getFailedIds().isEmpty());
    }

    private BdpImportStatus importDatasources(List<String> toImport, User user, RemoteCredentials remoteCredentials)  {
        BdpImportStatus importStatus = new BdpImportStatus();
        try {
            BdpImportService.Iface bdpImportClient = new ThriftClients().makeBdpImportClient();
            importStatus = bdpImportClient.importDatasources(toImport, user, remoteCredentials);
            if (!isImportSuccessful(importStatus)) {
                if(importStatus.getRequestStatus().equals(RequestStatus.FAILURE)){
                    log.error("Importing of data sources failed.");
                } else {
                    log.error("Importing has not succeeded for the following IDs: " + importStatus.getFailedIds().toString());
                }
            }
        } catch (TException e) {
            log.error("ImportDatasources: Exception ".concat(e.getMessage()));
        }
        return importStatus;
    }

    private List<Project> loadImportables(RemoteCredentials remoteCredentials) {
        List<Project> importable;

        BdpImportService.Iface bdpImportClient = new ThriftClients().makeBdpImportClient();
        try {
            importable = bdpImportClient.loadImportables(remoteCredentials);
        } catch (TException e) {
            log.error("Thrift failed, (uncaught TException)", e);
            importable = new ArrayList<>();
        }

        return importable;
    }

    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response) throws IOException, PortletException {
        LoginState loginState = new LoginState();
        String requestedAction = request.getParameter(PortalConstants.IMPORT_USER_ACTION);
        JSONObject responseData = handleRequestedAjaxAction(requestedAction, request, response, loginState);

        PrintWriter writer = response.getWriter();
        writer.write(responseData.toString());
    }

    public JSONObject handleRequestedAjaxAction(String requestedAction, ResourceRequest request,  ResourceResponse response, LoginState loginState) throws IOException, PortletException {
        PortletSession session = request.getPortletSession();
        RemoteCredentials remoteCredentials = getRemoteCredentialsFromSession(session);
        JSONObject responseData = JSONFactoryUtil.createJSONObject();

        switch(requestedAction) {
            case PortalConstants.IMPORT_USER_ACTION__IMPORTBDP:
                User user = UserCacheHolder.getUserFromRequest(request);
                List<String> selectedIds = getProjectIdsForImport(request);
                importBdpProjects(user, selectedIds, responseData, remoteCredentials);
                break;
            case PortalConstants.IMPORT_USER_ACTION__NEWIMPORTSOURCE:
                RemoteCredentials newCredentials = new RemoteCredentials()
                        .setUsername(request.getParameter(PortalConstants.SESSION_IMPORT_USER))
                        .setPassword(request.getParameter(PortalConstants.SESSION_IMPORT_PASS))
                        .setServerUrl(request.getParameter(PortalConstants.SESSION_IMPORT_URL));
                if (!validateCredentials(newCredentials)){
                    responseData.put(PortalConstants.IMPORT_RESPONSE__STATUS,
                            PortalConstants.IMPORT_RESPONSE__UNAUTHORIZED);
                } else {
                    setNewImportSource(newCredentials, session, responseData, loginState);
                }
                break;
            case PortalConstants.IMPORT_USER_ACTION__UPDATEIMPORTABLES:
                updateImportables(responseData, loginState, remoteCredentials);
                break;
            case PortalConstants.IMPORT_USER_ACTION__DISCONNECT:
                putRemoteCredentialsIntoSession(session, new RemoteCredentials());
                loginState.logout();
                break;
            default:
                loginState.logout();
                break;
        }
        return responseData;
    }

    private boolean validateCredentials(RemoteCredentials credentials) {
        try {
            return new ThriftClients().makeBdpImportClient().validateCredentials(credentials);
        } catch (TException e) {
            log.error("Thrift failed, (uncaught TException)", e);
            return false;
        }
    }

    private void importBdpProjects(User user, List<String> selectedIds, JSONObject responseData, RemoteCredentials remoteCredentials) throws PortletException, IOException {
        BdpImportStatus importStatus = importDatasources(selectedIds, user, remoteCredentials);
        JSONArray jsonFailedIds = JSONFactoryUtil.createJSONArray();
        JSONArray jsonSuccessfulIds = JSONFactoryUtil.createJSONArray();
        if(importStatus.isSetRequestStatus() && importStatus.getRequestStatus().equals(RequestStatus.SUCCESS)) {
            importStatus.getFailedIds().forEach(id -> jsonFailedIds.put(id));
            importStatus.getSuccessfulIds().forEach(id -> jsonSuccessfulIds.put(id));

            responseData.put(PortalConstants.IMPORT_RESPONSE__FAILED_IDS, jsonFailedIds);
            responseData.put(PortalConstants.IMPORT_RESPONSE__SUCCESSFUL_IDS, jsonSuccessfulIds);
        }
        if (isImportSuccessful(importStatus)) {
            responseData.put(PortalConstants.IMPORT_RESPONSE__STATUS, PortalConstants.IMPORT_RESPONSE__IMPORT_BDP_SUCCESS);
        } else if (importStatus.isSetRequestStatus() && importStatus.getRequestStatus().equals(RequestStatus.SUCCESS)) {
            responseData.put(PortalConstants.IMPORT_RESPONSE__STATUS, PortalConstants.IMPORT_RESPONSE__IMPORT_BDP_FAILURE);
        } else {
            responseData.put(PortalConstants.IMPORT_RESPONSE__STATUS, PortalConstants.IMPORT_RESPONSE__IMPORT_BDP_GENERAL_FAILURE);
        }
    }

    void setNewImportSource(RemoteCredentials newCredentials, PortletSession session, JSONObject responseData, LoginState loginState) {

        String serverUrl = nullToEmpty(newCredentials.getServerUrl());

        if (serverUrl.isEmpty()) {
            loginState.logout();
            responseData.put(PortalConstants.IMPORT_RESPONSE__STATUS,
                    PortalConstants.IMPORT_RESPONSE__DB_URL_NOTSET);
        } else {
            putRemoteCredentialsIntoSession(session, newCredentials);
            responseData.put(PortalConstants.IMPORT_RESPONSE__STATUS,
                    PortalConstants.IMPORT_RESPONSE__DB_CHANGED);
            responseData.put(PortalConstants.IMPORT_RESPONSE__DBURL, serverUrl);
            loginState.login(serverUrl);
        }
    }

    private void updateImportables(JSONObject responseData, LoginState loginState, RemoteCredentials remoteCredentials) throws JsonProcessingException {
        if (!nullToEmpty(remoteCredentials.getServerUrl()).isEmpty()) {
            List<Project> importables = loadImportables(remoteCredentials);

            JSONArray serializedProjects = JSONFactoryUtil.createJSONArray();
            ObjectMapper mapper = new ObjectMapper();
            for (Project p : importables) {
                serializedProjects.put(mapper.writeValueAsString(p));
            }
            responseData.put(PortalConstants.IMPORT_RESPONSE__NEW_IMPORTABLES, serializedProjects);
        }
        responseData.put(PortalConstants.IMPORT_RESPONSE__DBURL, remoteCredentials.getServerUrl());
        loginState.login(remoteCredentials.getServerUrl());
    }


}
