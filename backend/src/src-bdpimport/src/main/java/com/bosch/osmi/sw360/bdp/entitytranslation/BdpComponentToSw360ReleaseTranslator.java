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
package com.bosch.osmi.sw360.bdp.entitytranslation;

import com.bosch.osmi.bdp.access.api.model.Component;
import com.siemens.sw360.datahandler.thrift.components.Release;

import java.util.*;

public class BdpComponentToSw360ReleaseTranslator implements EntityTranslator<Component, Release> {

    @Override
    public Release apply(com.bosch.osmi.bdp.access.api.model.Component componentBdp) {
        Release releaseSW360 = new Release();

        releaseSW360.setName(componentBdp.getName());
        releaseSW360.setBdpId(componentBdp.getComponentKey());


        if (!Objects.isNull(componentBdp.getComponentVersion()) && !componentBdp.getComponentVersion().equals("")) {
            releaseSW360.setVersion(componentBdp.getComponentVersion());
        } else {
            // this appears for example, if componentBdp.getUsageLevel() == "ORIGINAL_CODE"
            releaseSW360.setVersion("UNKNOWN");
        }

        releaseSW360.setReleaseDate(componentBdp.getReleaseDate());

        releaseSW360.setModerators(new HashSet<>());
// Problem: Can not set mail address when no corresponding user is registered
// releaseSW360.getModerators().add(componentBdp.getApprovedBy());

// Not yet used:
// componentBdp.getComponentComment();
// componentBdp.getUsageLevel();

        return releaseSW360;
    }

}
