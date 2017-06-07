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

package org.opencms.setup.xml.v7;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsWorkplaceConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.setup.xml.A_CmsXmlWorkplace;
import org.opencms.setup.xml.CmsSetupXmlHelper;
import org.opencms.workplace.editors.directedit.CmsDirectEditDefaultProvider;
import org.opencms.workplace.editors.directedit.CmsDirectEditJQueryProvider;

import java.util.Collections;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Updates localization keys.<p>
 *
 * @since 6.1.8
 */
public class CmsXmlUpdateDirectEditProvider extends A_CmsXmlWorkplace {

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Update direct edit provider";
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        Node node = document.selectSingleNode(xpath);
        if (node != null) {
            CmsSetupXmlHelper.setValue(
                document,
                getCommonPath() + "/@" + I_CmsXmlConfiguration.A_CLASS,
                CmsDirectEditJQueryProvider.class.getName());
            return true;
        }
        return false;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getCommonPath()
     */
    @Override
    protected String getCommonPath() {

        // /opencms/workplace/directeditprovider
        return new StringBuffer("/").append(CmsConfigurationManager.N_ROOT).append("/").append(
            CmsWorkplaceConfiguration.N_WORKPLACE).append("/").append(
                CmsWorkplaceConfiguration.N_DIRECTEDITPROVIDER).toString();
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        return Collections.singletonList(
            getCommonPath()
                + "[@"
                + I_CmsXmlConfiguration.A_CLASS
                + "='"
                + CmsDirectEditDefaultProvider.class.getName()
                + "']");
    }
}