/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

public class AppConfiguration implements I_CmsWorkplaceAppConfiguration {

    public String getAppCategory() {

        // TODO Auto-generated method stub
        return null;

    }

    public I_CmsWorkplaceApp getAppInstance() {

        // TODO Auto-generated method stub
        return null;
    }

    public String getButtonStyle() {

        // TODO Auto-generated method stub
        return I_CmsAppButtonProvider.BUTTON_STYLE_TRANSPARENT;
    }

    public String getHelpText(Locale locale) {

        return null;
    }

    public Resource getIcon() {

        return FontAwesome.AMBULANCE;
    }

    public String getId() {

        return "/";
    }

    public String getName(Locale locale) {

        // TODO Auto-generated method stub
        return "testApp1";
    }

    public int getOrder() {

        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getPriority()
     */
    public int getPriority() {

        return I_CmsWorkplaceAppConfiguration.DEFAULT_PRIORIY;
    }

    public CmsAppVisibilityStatus getVisibility(CmsObject cms) {

        // TODO Auto-generated method stub
        return new CmsAppVisibilityStatus(false, false, "visible");
    }
}
