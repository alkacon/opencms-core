/*
 * File   : $Source: /alkacon/cvs/opencms/src-setup/org/opencms/setup/xml/A_CmsXmlWorkplace.java,v $
 * Date   : $Date: 2008/02/27 12:05:37 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2008 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.setup.xml;

import org.opencms.configuration.CmsWorkplaceConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;

import org.dom4j.Document;

/**
 * Skeleton for handling opencms-workplace.xml.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.2 $ 
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
     * Creates a new access control entry node.<p>
     * 
     * @param document the xml document to change
     * @param xpath the base xpath
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
        if (!permissions.equals(CmsSetupXmlHelper.getValue(document, xp
            + "/@"
            + CmsWorkplaceConfiguration.A_PERMISSIONS))) {
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