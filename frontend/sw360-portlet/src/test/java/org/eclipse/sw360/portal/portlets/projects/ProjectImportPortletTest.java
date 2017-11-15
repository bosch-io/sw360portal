/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 * Part of the SW360 Portal Project.
 *
 * SPDX-License-Identifier: EPL-1.0
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.portal.portlets.projects;

import com.liferay.portal.kernel.json.JSONObject;
import org.eclipse.sw360.datahandler.thrift.bdpimport.RemoteCredentials;
import org.eclipse.sw360.portal.common.PortalConstants;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.portlet.PortletSession;

import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class ProjectImportPortletTest extends TestCase {
    private final String name = "name";
    private final String password = "password";
    private final String newURL = "newURL";

    @Mock
    private PortletSession session;

    @Mock
    private JSONObject responseData;

    @Test
    public void testUpdateInputSourceWithoutUrl() throws Exception {

        ProjectImportPortlet.LoginState loginState = new ProjectImportPortlet.LoginState();
        RemoteCredentials remoteCredentials = new RemoteCredentials();

        new ProjectImportPortlet().setNewImportSource(
                remoteCredentials,
                session,
                responseData,
                loginState);
        verify(responseData).put(PortalConstants.PROJECT_IMPORT_RESPONSE__STATUS, PortalConstants.PROJECT_IMPORT_RESPONSE__DB_URL_NOT_SET);
    }

    @Test
    public void testUpdateInputSourceWithUrlUpdatesResponse() throws Exception {

        ProjectImportPortlet.LoginState loginState = new ProjectImportPortlet.LoginState();
        RemoteCredentials remoteCredentials = new RemoteCredentials();
        remoteCredentials.setUsername(name);
        remoteCredentials.setPassword(password);
        remoteCredentials.setServerUrl(newURL);

        new ProjectImportPortlet().setNewImportSource(
                remoteCredentials,
                session,
                responseData,
                loginState);

        verify(responseData).put(PortalConstants.PROJECT_IMPORT_RESPONSE__STATUS, PortalConstants.PROJECT_IMPORT_RESPONSE__DB_CHANGED);
        verify(responseData).put(PortalConstants.PROJECT_IMPORT_RESPONSE__DB_URL, newURL);
    }

    @Test
    public void testUpdateInputSourceWithUrlUpdatesSession() throws Exception {

        ProjectImportPortlet.LoginState loginState = new ProjectImportPortlet.LoginState();
        RemoteCredentials remoteCredentials = new RemoteCredentials();
        remoteCredentials.setUsername(name);
        remoteCredentials.setPassword(password);
        remoteCredentials.setServerUrl(newURL);

        new ProjectImportPortlet().setNewImportSource(
                remoteCredentials,
                session,
                responseData,
                loginState);

        verify(session).setAttribute(PortalConstants.PROJECT_IMPORT_USERNAME, name);
        verify(session).setAttribute(PortalConstants.PROJECT_IMPORT_PASSWORD, password);
        verify(session).setAttribute(PortalConstants.PROJECT_IMPORT_SERVER_URL, newURL);
    }

}
