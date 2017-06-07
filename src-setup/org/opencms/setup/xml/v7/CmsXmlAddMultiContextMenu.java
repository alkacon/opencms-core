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
import org.opencms.setup.xml.A_CmsSetupXmlUpdate;
import org.opencms.setup.xml.CmsSetupXmlHelper;

import java.util.Collections;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Adds the new content notification node.<p>
 *
 * @since 6.1.8
 */
public class CmsXmlAddMultiContextMenu extends A_CmsSetupXmlUpdate {

    /** List of xpaths to update. */
    private List<String> m_xpaths;

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Add new explorer multi selection feature";
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
        if (node == null) {
            if (xpath.equals(getXPathsToUpdate().get(0))) {
                setEntry(document, xpath, "explorer.context.lock", "commons/lock.jsp", "", "10");
                setEntry(document, xpath, "explorer.context.unlock", "commons/unlock.jsp", "", "20");
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath
                        + "/"
                        + CmsWorkplaceConfiguration.N_SEPARATOR
                        + "[@"
                        + I_CmsXmlConfiguration.A_ORDER
                        + "='30']"
                        + "/@"
                        + I_CmsXmlConfiguration.A_ORDER,
                    "30");
                setEntry(document, xpath, "explorer.context.publish", "commons/publishresource.jsp", "", "40");
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath
                        + "/"
                        + CmsWorkplaceConfiguration.N_SEPARATOR
                        + "[@"
                        + I_CmsXmlConfiguration.A_ORDER
                        + "='50']"
                        + "/@"
                        + I_CmsXmlConfiguration.A_ORDER,
                    "50");
                setEntry(document, xpath, "explorer.context.copy", "commons/copy.jsp", "", "60");
                setEntry(document, xpath, "explorer.context.move.multi", "commons/move.jsp", "", "70");
                setEntry(document, xpath, "explorer.context.delete", "commons/delete.jsp", "", "80");
                setEntry(document, xpath, "explorer.context.touch", "commons/touch.jsp", "", "90");
                setEntry(document, xpath, "explorer.context.availability", "commons/availability.jsp", "", "100");
                setEntry(document, xpath, "explorer.context.undochanges", "commons/undochanges.jsp", "", "110");
                setEntry(document, xpath, "explorer.context.undelete", "commons/undelete.jsp", "", "120");
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

        // /opencms/workplace/explorertypes
        StringBuffer xp = new StringBuffer(256);
        xp.append("/").append(CmsConfigurationManager.N_ROOT);
        xp.append("/").append(CmsWorkplaceConfiguration.N_WORKPLACE);
        xp.append("/").append(CmsWorkplaceConfiguration.N_EXPLORERTYPES);
        return xp.toString();
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        if (m_xpaths == null) {
            // /opencms/workplace/explorertypes/multicontextmenu
            StringBuffer xp = new StringBuffer(256);
            xp.append("/").append(CmsConfigurationManager.N_ROOT);
            xp.append("/").append(CmsWorkplaceConfiguration.N_WORKPLACE);
            xp.append("/").append(CmsWorkplaceConfiguration.N_EXPLORERTYPES);
            xp.append("/").append(CmsWorkplaceConfiguration.N_MULTICONTEXTMENU);
            m_xpaths = Collections.singletonList(xp.toString());
        }
        return m_xpaths;
    }

    /**
     * Creates a new entry node.<p>
     *
     * @param document the xml document to change
     * @param xpath the base xpath
     * @param key the localization key name
     * @param uri the entry uri
     * @param rules the permissions rules
     * @param order the relative order
     */
    private void setEntry(Document document, String xpath, String key, String uri, String rules, String order) {

        String xp = xpath
            + "/"
            + CmsWorkplaceConfiguration.N_ENTRY
            + "[@"
            + I_CmsXmlConfiguration.A_URI
            + "='"
            + uri
            + "']";
        CmsSetupXmlHelper.setValue(document, xp + "/@" + I_CmsXmlConfiguration.A_URI, uri);
        CmsSetupXmlHelper.setValue(document, xp + "/@" + I_CmsXmlConfiguration.A_KEY, key);
        CmsSetupXmlHelper.setValue(document, xp + "/@" + CmsWorkplaceConfiguration.A_RULES, rules);
        CmsSetupXmlHelper.setValue(document, xp + "/@" + I_CmsXmlConfiguration.A_ORDER, order);
    }

}