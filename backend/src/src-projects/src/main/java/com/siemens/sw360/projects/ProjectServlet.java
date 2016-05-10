/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 * With modifications by Bosch Software Innovations GmbH, 2016.
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
package com.siemens.sw360.projects;

import com.siemens.sw360.datahandler.thrift.projects.ProjectService;
import org.apache.log4j.Logger;
import org.apache.thrift.protocol.TCompactProtocol;
import com.siemens.sw360.projects.Sw360ThriftServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.io.IOException;

import static org.apache.log4j.Logger.getLogger;

/**
 * Thrift Servlet instantiation
 *
 * @author cedric.bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 * @author Andreas.Reichel@tngtech.com
 */
public class ProjectServlet extends Sw360ThriftServlet {

    public ProjectServlet() throws MalformedURLException, IOException {
        // Create a service processor using the provided handler
        super(new ProjectService.Processor<>(new ProjectHandler()), new TCompactProtocol.Factory());
    }

}
