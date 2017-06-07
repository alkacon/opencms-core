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
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Fixes some context menu item nodes.<p>
 *
 * @since 7.5.1
 */
public class CmsXmlFixContextMenuItems extends A_CmsXmlWorkplace {

    /** List of xpaths to update. */
    private List<String> m_xpaths;

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Fixes some context menu item nodes";
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        boolean modified = false;
        Node node = document.selectSingleNode(xpath);
        if (node != null) {
            String xp = xpath + "/@" + CmsWorkplaceConfiguration.A_RULE;
            if (!document.selectSingleNode(xp).getText().equals("nondeleted")) {
                CmsSetupXmlHelper.setValue(document, xp, "nondeleted");
                modified = true;
            }
        }
        return modified;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getCommonPath()
     */
    @Override
    protected String getCommonPath() {

        // /opencms/workplace/explorertypes
        return new StringBuffer("/").append(CmsConfigurationManager.N_ROOT).append("/").append(
            CmsWorkplaceConfiguration.N_WORKPLACE).append("/").append(
                CmsWorkplaceConfiguration.N_EXPLORERTYPES).toString();
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        if (m_xpaths == null) {
            // /opencms/workplace/explorertypes/explorertype[@name='${etype}']/editoptions/contextmenu/entry[@uri='commons/opengallery.jsp']
            StringBuffer xp = new StringBuffer(256);
            xp.append("/");
            xp.append(CmsConfigurationManager.N_ROOT);
            xp.append("/");
            xp.append(CmsWorkplaceConfiguration.N_WORKPLACE);
            xp.append("/");
            xp.append(CmsWorkplaceConfiguration.N_EXPLORERTYPES);
            xp.append("/");
            xp.append(CmsWorkplaceConfiguration.N_EXPLORERTYPE);
            xp.append("[@");
            xp.append(I_CmsXmlConfiguration.N_NAME);
            xp.append("='${etype}']/");
            xp.append(CmsWorkplaceConfiguration.N_EDITOPTIONS);
            xp.append("/");
            xp.append(CmsWorkplaceConfiguration.N_CONTEXTMENU);
            xp.append("/");
            xp.append(CmsWorkplaceConfiguration.N_ENTRY);
            xp.append("[@");
            xp.append(I_CmsXmlConfiguration.A_URI);
            xp.append("='commons/opengallery.jsp']");

            m_xpaths = new ArrayList<String>();
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), "${etype}", "downloadgallery"));
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), "${etype}", "imagegallery"));
        }
        return m_xpaths;
    }
}
