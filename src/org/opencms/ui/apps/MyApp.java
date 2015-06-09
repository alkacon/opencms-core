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
import com.vaadin.ui.Label;

public class MyApp implements I_CmsWorkplaceAppConfiguration {

    public static class MyAppComponent extends Label implements I_CmsWorkplaceApp {

        public MyAppComponent() {

            super("MyApp");
        }

        public void onStateChange(String state) {

            // TODO Auto-generated method stub

        }
    }

    public I_CmsWorkplaceApp getAppInstance() {

        return new MyAppComponent();
    }

    public String getAppPath() {

        return "myapp";
    }

    public String getHelpText(Locale locale) {

        return "The quick brown fox jumps over the lazy dog.";
    }

    public Resource getIcon() {

        return FontAwesome.USER;
    }

    public String getName(Locale locale) {

        return "myapp";
    }

    public CmsAppVisibilityStatus getVisibility(CmsObject cms) {

        return new CmsAppVisibilityStatus(true, true, null);
    }

}
