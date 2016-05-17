/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
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

package com.bosch.osmi.bdp.access.impl.model;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.bosch.osmi.bdp.access.api.model.ProjectInfo;
import com.bosch.osmi.bdp.access.api.model.User;
import com.bosch.osmi.bdp.access.impl.BdpApiAccessImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author muj1be
 * @since 11/17/15.
 */
public class UserImpl implements User{

    private static final Logger LOGGER = LogManager.getLogger(UserImpl.class);

    private final String name;
    private final BdpApiAccessImpl access;

    public UserImpl(String name, BdpApiAccessImpl bdpApiAccess){
        this.name = name;
        this.access = bdpApiAccess;
    }

    @Override
    public String getEmailAddress() {
        return name;
    }

    @Override
    public String getFirstName() {
        return getUserByEmail().getFirstName();
    }

    @Override
    public String getLastName() {
        return getUserByEmail().getLastName();
    }

    private com.blackducksoftware.sdk.protex.user.User getUserByEmail(){
        try {
            return access.getUserApi().getUserByEmail(name);
        } catch (SdkFault sdkFault) {
            LOGGER.error("Error occurred while accessing user " + name + "from Bdp server.");
            LOGGER.debug(sdkFault.getMessage());
            throw new IllegalStateException("Cannot access user object from Bdp SDK. Reason " + sdkFault.getMessage());
        }
    }


    @Override
    public Collection<ProjectInfo> getProjectInfos() {
        try {
            final List<com.blackducksoftware.sdk.protex.project.ProjectInfo> projectsByUser = access.getProjectApi().getProjectsByUser(this.getEmailAddress());
            Collection<ProjectInfo> projectInfos = translate(projectsByUser);
            return projectInfos;

        } catch (SdkFault sdkFault) {
            LOGGER.error("Error occurred while retrieving project info for user with " + name + "from Bdp server.");
            LOGGER.debug(sdkFault.getMessage());
            throw new IllegalStateException(sdkFault.getCause());
        }
    }

    private Collection<ProjectInfo> translate(List<com.blackducksoftware.sdk.protex.project.ProjectInfo> projectInfos) {
        Collection<ProjectInfo> result = new ArrayList<ProjectInfo>();
        for(com.blackducksoftware.sdk.protex.project.ProjectInfo source : projectInfos){
            result.add(new ProjectInfoImpl(source, access));
        }
        return result;
    }
}
