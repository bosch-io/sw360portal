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
package com.bosch.osmi.sw360.bdp.datasource;

import com.bosch.osmi.bdp.access.api.BdpApiAccess;
import com.bosch.osmi.bdp.access.api.model.*;
import com.bosch.osmi.bdp.access.impl.BdpApiAccessImpl;
import com.bosch.osmi.bdp.access.mock.BdpApiAccessMockImpl;
import com.siemens.sw360.datahandler.thrift.bdpimport.RemoteCredentials;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Wrapper over BdpApiAccess.
 */
public class BdpApiAccessWrapperSimple implements BdpApiAccessWrapper {

    private static final Logger log = Logger.getLogger(BdpApiAccessWrapperSimple.class);

    private BdpApiAccess bdpApiAccess;
    private User user;

    public BdpApiAccessWrapperSimple(RemoteCredentials remoteCredentials) {
        String serverUrl = remoteCredentials.getServerUrl();

        log.info("server: " + remoteCredentials.getServerUrl());
        if ("mock".equals(serverUrl)) {
            log.error("Using the mock with canned data.");
            bdpApiAccess = new BdpApiAccessMockImpl();
        } else {
            bdpApiAccess = new BdpApiAccessImpl(serverUrl, remoteCredentials.getUsername(), remoteCredentials.getPassword());
        }
        user = bdpApiAccess.retrieveUser();
    }

    @Override
    public boolean validateCredentials() {
        return bdpApiAccess.validateCredentials();
    }

    @Override
    public String getEmailAddress() {
        return user.getEmailAddress();
    }

    @Override
    public Collection<ProjectInfo> getUserProjectInfos() {
        return user.getProjectInfos();
    }

    @Override
    public Map<ProjectInfo, Collection<Component>> getProjectInfoMapComponents() {
        return getUserProjectInfos().stream().collect(Collectors.toMap(p -> p, p -> p.getProject().getComponents()));
    }

    @Override
    public Map<Component, License> getComponentMapLicense(Collection<Component> allComponent) {
        return allComponent.stream().collect(Collectors.toMap(c -> c, Component::getLicense));
    }

    @Override
    public ProjectInfo getProjectInfo(String bdpId) {
        Map<String, ProjectInfo> namedProjectInfo = getUserProjectInfos().stream()
                .collect(Collectors.toMap(p -> p.getProjectId(), p -> p));
        return namedProjectInfo.get(bdpId);
    }
}
