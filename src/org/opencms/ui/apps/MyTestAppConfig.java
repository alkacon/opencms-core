/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ui.apps;

import org.opencms.file.CmsObject;

import java.util.Locale;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;

/**
 * My test app.<p>
 */
public class MyTestAppConfig implements I_CmsWorkplaceAppConfiguration {

    public I_CmsWorkplaceApp getAppInstance() {

        return new MyTestApp();
    }

    public String getAppPath() {

        // TODO Auto-generated method stub
        return "testing/mytestapp";
    }

    public String getHelpText(Locale locale) {

        // TODO Auto-generated method stub
        return "The app help text";
    }

    public Resource getIcon() {

        // TODO Auto-generated method stub
        return FontAwesome.FIGHTER_JET;
    }

    public String getName(Locale locale) {

        // TODO Auto-generated method stub
        return "My test app";
    }

    public CmsAppVisibilityStatus getVisibility(CmsObject cms) {

        // TODO Auto-generated method stub
        return new CmsAppVisibilityStatus(true, true, getHelpText(null));
    }

}
