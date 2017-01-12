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

package org.opencms.setup.xml.v10;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsWorkplaceConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.security.CmsRole;
import org.opencms.setup.xml.A_CmsSetupXmlUpdate;
import org.opencms.setup.xml.CmsSetupXmlHelper;

import java.util.Collections;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Changes the access control entry for explorer type JSP , from 10.0.x to 10.5.0.<p>
 *
 * @since 8.0.0
 */
public class CmsXmlChangeExplorerTypeAccess extends A_CmsSetupXmlUpdate {

    /** List of xpaths to update. */
    private List<String> m_xpaths;

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Change explorer type access control entries for type JSP";
    }

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getXmlFilename()
     */
    public String getXmlFilename() {

        return CmsWorkplaceConfiguration.DEFAULT_XML_FILE_NAME;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        Node node = document.selectSingleNode(xpath);
        if ((node != null) && getXPathsToUpdate().contains(xpath)) {
            CmsSetupXmlHelper.setAttribute(
                document,
                xpath,
                CmsWorkplaceConfiguration.A_PRINCIPAL,
                "ROLE." + CmsRole.VFS_MANAGER.getRoleName());
            return true;
        }
        return false;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getCommonPath()
     */
    @Override
    protected String getCommonPath() {

        // /opencms/workplace/explorertypes/explorertype
        StringBuffer xp = new StringBuffer(256);
        xp.append("/").append(CmsConfigurationManager.N_ROOT);
        xp.append("/").append(CmsWorkplaceConfiguration.N_WORKPLACE);
        xp.append("/").append(CmsWorkplaceConfiguration.N_EXPLORERTYPES);
        xp.append("/").append(CmsWorkplaceConfiguration.N_EXPLORERTYPE);
        return xp.toString();
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        if (m_xpaths == null) {
            StringBuffer xp = new StringBuffer(256);
            xp.append(getCommonPath());
            xp.append("[@").append(I_CmsXmlConfiguration.A_NAME);
            xp.append("='jsp']");
            xp.append("/").append(CmsWorkplaceConfiguration.N_ACCESSCONTROL);
            xp.append("/").append(CmsWorkplaceConfiguration.N_ACCESSENTRY);
            xp.append("[@").append(CmsWorkplaceConfiguration.A_PRINCIPAL);
            xp.append("='ROLE.").append(CmsRole.DEVELOPER.getRoleName()).append("']");
            m_xpaths = Collections.singletonList(xp.toString());
        }
        return m_xpaths;
    }

}