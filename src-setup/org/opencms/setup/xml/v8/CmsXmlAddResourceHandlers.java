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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.setup.xml.v8;

import org.opencms.ade.detailpage.CmsDetailPageResourceHandler;
import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.main.CmsAliasResourceHandler;
import org.opencms.pdftools.CmsPdfResourceHandler;
import org.opencms.setup.xml.A_CmsSetupXmlUpdate;
import org.opencms.setup.xml.CmsSetupXmlHelper;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Adds the new init resource handler classes, from 7.5.x to 8.0.0.<p>
 * 
 * @since 8.0.0
 */
public class CmsXmlAddResourceHandlers extends A_CmsSetupXmlUpdate {

    /** List of xpaths to update. */
    private List<String> m_xpaths;

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Add new init resource handler classes";
    }

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getXmlFilename()
     */
    public String getXmlFilename() {

        return CmsSystemConfiguration.DEFAULT_XML_FILE_NAME;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        Node node = document.selectSingleNode(xpath);
        if (node == null) {
            if (getXPathsToUpdate().contains(xpath)) {
                CmsSetupXmlHelper.setValue(document, xpath, "");
            } else {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getCommonPath()
     */
    @Override
    protected String getCommonPath() {

        // /opencms/system/resourceinit
        StringBuffer xp = new StringBuffer(256);
        xp.append("/").append(CmsConfigurationManager.N_ROOT);
        xp.append("/").append(CmsSystemConfiguration.N_SYSTEM);
        xp.append("/").append(CmsSystemConfiguration.N_RESOURCEINIT);
        return xp.toString();
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        if (m_xpaths == null) {
            String[] handlers = new String[] {
                CmsDetailPageResourceHandler.class.getName(),
                CmsAliasResourceHandler.class.getName(),
                CmsPdfResourceHandler.class.getName()};
            m_xpaths = new ArrayList<String>();
            for (String handler : handlers) {
                // "/opencms/system/resourceinit/resourceinithandler[@class='...']";
                StringBuffer xp = new StringBuffer(256);
                xp.append("/").append(CmsConfigurationManager.N_ROOT);
                xp.append("/").append(CmsSystemConfiguration.N_SYSTEM);
                xp.append("/").append(CmsSystemConfiguration.N_RESOURCEINIT);
                xp.append("/").append(CmsSystemConfiguration.N_RESOURCEINITHANDLER);
                xp.append("[@").append(I_CmsXmlConfiguration.A_CLASS);
                xp.append("='");
                m_xpaths.add(xp.toString() + handler + "']");
            }
        }
        return m_xpaths;
    }

}