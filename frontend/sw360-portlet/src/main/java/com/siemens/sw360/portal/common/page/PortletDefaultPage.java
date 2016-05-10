/*
 * Copyright Siemens AG, 2015. Part of the SW360 Portal Project.
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
package com.siemens.sw360.portal.common.page;

import com.siemens.sw360.portal.common.PortalConstants;

/**
 * @author daniele.fognini@tngtech.com
 */
public enum PortletDefaultPage implements PortletPage {
    DETAIL(PortalConstants.PAGENAME_DETAIL),
    RELEASE_DETAIL(PortalConstants.PAGENAME_RELEASE_DETAIL),
    EDIT(PortalConstants.PAGENAME_EDIT);

    private final String pagename;

    PortletDefaultPage(String pagename) {
        this.pagename = pagename;
    }

    @Override
    public String pagename() {
        return pagename;
    }
}
