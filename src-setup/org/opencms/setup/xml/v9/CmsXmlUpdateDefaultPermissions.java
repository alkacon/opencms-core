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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.setup.xml.v9;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsWorkplaceConfiguration;
import org.opencms.setup.xml.A_CmsXmlWorkplace;

import java.util.Arrays;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Updates default permissions for explorer access.<p>
 */
public class CmsXmlUpdateDefaultPermissions extends A_CmsXmlWorkplace {

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Update default permissions for explorer access for new roles.";
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        boolean changed = false;
        Node node = document.selectSingleNode(xpath);
        if (node != null) {
            changed = setAccessEntry(document, xpath, "ROLE.ELEMENT_AUTHOR", "+r+v+w+c");
        }
        return changed;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getCommonPath()
     */
    @Override
    protected String getCommonPath() {

        return getXPathsToUpdate().get(0);
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        return Arrays.asList(
            "/"
                + CmsConfigurationManager.N_ROOT
                + "/"
                + CmsWorkplaceConfiguration.N_WORKPLACE
                + "/"
                + CmsWorkplaceConfiguration.N_EXPLORERTYPES
                + "/"
                + CmsWorkplaceConfiguration.N_DEFAULTACCESSCONTROL
                + "/"
                + CmsWorkplaceConfiguration.N_ACCESSCONTROL);

    }
}