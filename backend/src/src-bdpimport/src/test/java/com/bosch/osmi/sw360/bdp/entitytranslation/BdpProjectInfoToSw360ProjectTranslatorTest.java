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
import com.bosch.osmi.bdp.access.api.model.Project;
import com.bosch.osmi.bdp.access.api.model.ProjectInfo;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class BdpProjectInfoToSw360ProjectTranslatorTest {
	
	private com.siemens.sw360.datahandler.thrift.projects.Project project;
	private ProjectInfo projectInfoBdp;

	@Before
	public void setUp() throws Exception {
		Project projectBdp = new Project() {

			@Override
			public String getName() {
				return "bdpProjectName";
			}

			@Override
			public Collection<Component> getComponents() {
				return null;
			}

			@Override
			public String getCreatedBy() {
				return null;
			}

			@Override
			public String getFilesScanned() {
				return null;
			}

			@Override
			public String getPendingIdentification() {
				return null;
			}
		};
		projectInfoBdp = new ProjectInfo(){

			@Override
			public String getProjectName() { return "bdpProjectName"; }

			@Override
			public String getProjectId() { return "bdpId"; }

			@Override
			public Project getProject() { return projectBdp; }
		};

		project = new BdpProjectInfoToSw360ProjectTranslator().apply(projectInfoBdp);
		
		assertThat(project, is(notNullValue()));
	}

	@Test
	public void testGetProjectName() {
		assertThat(project.getName(), is(projectInfoBdp.getProjectName()));
	}
	
	@Test
	public void testGetProjectId() {
		assertThat(project.getBdpId(), is(projectInfoBdp.getProjectId()));
	}
	
	@Test
	public void testGetProject() {
		assertThat("Has to be attached afterwards", projectInfoBdp.getProject(), is(notNullValue()));
	}

	@Test
	public void testGetDescription() {
		assertThat(project.getDescription(), is(""));
	}

	@Test
	public void testGetComponents() {
		assertThat("Has to be attached afterwards", projectInfoBdp.getProject().getComponents(), is(nullValue()));
	}
}
