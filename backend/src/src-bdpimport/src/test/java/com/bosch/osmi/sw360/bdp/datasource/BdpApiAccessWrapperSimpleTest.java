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

import com.bosch.osmi.bdp.access.api.model.Component;
import com.bosch.osmi.bdp.access.api.model.ProjectInfo;
import com.siemens.sw360.datahandler.thrift.bdpimport.RemoteCredentials;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.Map.Entry;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BdpApiAccessWrapperSimpleTest {

    BdpApiAccessWrapper bdpApiAccessWrapper;

    @Before
    public void setUp() throws Exception {
        this.bdpApiAccessWrapper = new BdpApiAccessWrapperSimple(new RemoteCredentials()
                .setUsername("")
                .setPassword("")
                .setServerUrl("mock"));
    }

    @Test
    public void testGetEmailAddress() {
        assertThat(this.bdpApiAccessWrapper.getEmailAddress(), is("john@example.com"));
    }

    @Test
    public void testGetUserProjectInfos() {
        Collection<ProjectInfo> userProjectInfos = this.bdpApiAccessWrapper.getUserProjectInfos();
        assertThat(userProjectInfos.size(), is(2));
        Assertions.assertProjectInfo(firstOf(userProjectInfos));
    }

    @Test
    public void testGetAllProject() {
        Collection<ProjectInfo> allProjectInfos = this.bdpApiAccessWrapper.getUserProjectInfos();
        TreeSet<ProjectInfo> sortedProjects = new TreeSet<>(Comparator.comparing(ProjectInfo::getProjectId).reversed());
        sortedProjects.addAll(allProjectInfos);
        assertThat(sortedProjects.size(), is(2));

        Assertions.assertProject(firstOf(sortedProjects));
    }

    @Test
    public void testGetProjectMapComponents() {
        Map<ProjectInfo, Collection<Component>> projectInfoMapComponents = this.bdpApiAccessWrapper.getProjectInfoMapComponents();

        TreeMap<ProjectInfo, Collection<Component>> sortedProjectMapComponents = new TreeMap<>(Comparator.comparing(ProjectInfo::getProjectId).reversed());
        sortedProjectMapComponents.putAll(projectInfoMapComponents);
        assertThat(sortedProjectMapComponents.size(), is(2));

        Entry<ProjectInfo, Collection<Component>> entry = firstOf(sortedProjectMapComponents);
        Assertions.assertProject(entry.getKey());

        Collection<Component> value = entry.getValue();
        assertThat(value.size(), is(7));
        TreeSet<Component> sortedComponent = new TreeSet<>(Comparator.comparing(Component::getName).reversed());
        sortedComponent.addAll(value);

        Assertions.assertComponent(firstOf(sortedComponent));
    }

    private static <T> T firstOf(Collection<T> value) {
        return Assertions.firstOf(value);
    }

    private static <K, V> Entry<K, V> firstOf(Map<K, V> map) {
        return map.entrySet().iterator().next();
    }

}
