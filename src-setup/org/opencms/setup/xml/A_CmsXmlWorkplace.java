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

package org.opencms.setup.xml;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsWorkplaceConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;

import org.dom4j.Document;

/**
 * Skeleton for handling opencms-workplace.xml.<p>
 *
 * @since 6.1.8
 */
public abstract class A_CmsXmlWorkplace extends A_CmsSetupXmlUpdate {

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getXmlFilename()
     */
    public String getXmlFilename() {

        return CmsWorkplaceConfiguration.DEFAULT_XML_FILE_NAME;
    }

    /**
     * Creates a new 'explorertype/newresource' node.<p>
     *
     * @param document the xml document to modify
     * @param xpath the xpath to the existing explorer type (ie <code>/opencms/workplace/explorertypes/explorertype[@name='${etype}']</code>)
     * @param uri the uri attribute value
     * @param order the order attribute value
     * @param autoSetNav the autosetnavigation attribute value
     * @param autoSetTitle the autosettitle attribute value
     * @param info the info attribute value
     * @param page the optional page attribute value
     */
    protected void createEtNewResource(
        Document document,
        String xpath,
        String uri,
        int order,
        boolean autoSetNav,
        boolean autoSetTitle,
        String info,
        String page) {

        String xp = xpath + "/" + CmsWorkplaceConfiguration.N_NEWRESOURCE;
        if (page != null) {
            CmsSetupXmlHelper.setValue(document, xp + "/@" + CmsWorkplaceConfiguration.A_PAGE, page);
        }
        CmsSetupXmlHelper.setValue(document, xp + "/@" + I_CmsXmlConfiguration.A_URI, uri);
        CmsSetupXmlHelper.setValue(document, xp + "/@" + I_CmsXmlConfiguration.A_ORDER, String.valueOf(order));
        CmsSetupXmlHelper.setValue(
            document,
            xp + "/@" + CmsWorkplaceConfiguration.A_AUTOSETNAVIGATION,
            String.valueOf(autoSetNav));
        CmsSetupXmlHelper.setValue(
            document,
            xp + "/@" + CmsWorkplaceConfiguration.A_AUTOSETTITLE,
            String.valueOf(autoSetTitle));
        CmsSetupXmlHelper.setValue(document, xp + "/@" + CmsWorkplaceConfiguration.A_INFO, info);
    }

    /**
     * Creates a new 'explorertype' node.<p>
     *
     * @param document the xml documento to modify
     * @param xpath the xpath to the non-existing explorer type (ie <code>/opencms/workplace/explorertypes/explorertype[@name='${etype}']</code>)
     * @param name the name attribute value
     * @param key the key attribute value
     * @param icon the icon attribute value
     * @param reference the reference attribute value
     */
    protected void createExplorerType(
        Document document,
        String xpath,
        String name,
        String key,
        String icon,
        String reference) {

        StringBuffer insertPoint = new StringBuffer(256);
        insertPoint.append("/");
        insertPoint.append(CmsConfigurationManager.N_ROOT);
        insertPoint.append("/");
        insertPoint.append(CmsWorkplaceConfiguration.N_WORKPLACE);
        insertPoint.append("/");
        insertPoint.append(CmsWorkplaceConfiguration.N_EXPLORERTYPES);
        insertPoint.append("/");
        insertPoint.append(CmsWorkplaceConfiguration.N_EXPLORERTYPE);
        insertPoint.append("[last()]");
        CmsSetupXmlHelper.setValue(document, insertPoint.toString(), null, xpath.substring(xpath.lastIndexOf('/') + 1));
        CmsSetupXmlHelper.setValue(document, xpath + "/@" + I_CmsXmlConfiguration.A_KEY, key);
        CmsSetupXmlHelper.setValue(document, xpath + "/@" + I_CmsXmlConfiguration.A_ICON, icon);
        CmsSetupXmlHelper.setValue(document, xpath + "/@" + CmsWorkplaceConfiguration.A_REFERENCE, reference);
    }

    /**
     * Creates a new access control entry node.<p>
     *
     * @param document the xml document to change
     * @param xpath the base xpath (ie <code>/opencms/workplace/explorertypes/explorertype[@name='${etype}']/accesscontrol</code>)
     * @param principal the principal
     * @param permissions the permissions string
     *
     * @return if a modification has been needed
     */
    protected boolean setAccessEntry(Document document, String xpath, String principal, String permissions) {

        boolean ret = false;
        String xp = xpath
            + "/"
            + CmsWorkplaceConfiguration.N_ACCESSENTRY
            + "[@"
            + CmsWorkplaceConfiguration.A_PRINCIPAL
            + "='"
            + principal
            + "']";
        if (CmsSetupXmlHelper.getValue(document, xp + "/@" + CmsWorkplaceConfiguration.A_PRINCIPAL) == null) {
            ret = true;
        }
        CmsSetupXmlHelper.setValue(document, xp + "/@" + CmsWorkplaceConfiguration.A_PRINCIPAL, principal);
        if (!permissions.equals(
            CmsSetupXmlHelper.getValue(document, xp + "/@" + CmsWorkplaceConfiguration.A_PERMISSIONS))) {
            CmsSetupXmlHelper.setValue(document, xp + "/@" + CmsWorkplaceConfiguration.A_PERMISSIONS, permissions);
            ret = true;
        }
        return ret;
    }

    /**
     * Creates a new context menu entry node.<p>
     *
     * @param document the xml document to change
     * @param xpath the base xpath
     * @param key the localization key name
     * @param uri the entry uri
     * @param rules the permissions rules
     * @param order the relative order
     */
    protected void setMenuEntry(Document document, String xpath, String key, String uri, String rules, String order) {

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