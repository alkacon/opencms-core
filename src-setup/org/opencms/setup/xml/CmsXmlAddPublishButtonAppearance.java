/*
 * File   : $Source: /alkacon/cvs/opencms/src-setup/org/opencms/setup/xml/CmsXmlAddPublishButtonAppearance.java,v $
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

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsWorkplaceConfiguration;

import java.util.Collections;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Adds the new publish button appearance node.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 6.1.8 
 */
public class CmsXmlAddPublishButtonAppearance extends A_CmsXmlWorkplace {

    /** List of xpaths to update. */
    private List m_xpaths;

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Add new Publish button appearance node";
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String)
     */
    protected boolean executeUpdate(Document document, String xpath) {

        Node node = document.selectSingleNode(xpath);
        if (node == null) {
            CmsSetupXmlHelper.setValue(document, xpath, "always");
            return true;
        }
        return false;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getCommonPath()
     */
    protected String getCommonPath() {

        // /opencms/workplace/default-preferences/workplace-preferences/workplace-generaloptions
        StringBuffer xp = new StringBuffer(256);
        xp.append("/").append(CmsConfigurationManager.N_ROOT);
        xp.append("/").append(CmsWorkplaceConfiguration.N_WORKPLACE);
        xp.append("/").append(CmsWorkplaceConfiguration.N_DEFAULTPREFERENCES);
        xp.append("/").append(CmsWorkplaceConfiguration.N_WORKPLACEPREFERENCES);
        xp.append("/").append(CmsWorkplaceConfiguration.N_WORKPLACEGENERALOPTIONS);
        return xp.toString();
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    protected List getXPathsToUpdate() {

        if (m_xpaths == null) {
            // /opencms/workplace/default-preferences/workplace-preferences/workplace-generaloptions/publishbuttonappearance
            StringBuffer xp = new StringBuffer(256);
            xp.append("/").append(CmsConfigurationManager.N_ROOT);
            xp.append("/").append(CmsWorkplaceConfiguration.N_WORKPLACE);
            xp.append("/").append(CmsWorkplaceConfiguration.N_DEFAULTPREFERENCES);
            xp.append("/").append(CmsWorkplaceConfiguration.N_WORKPLACEPREFERENCES);
            xp.append("/").append(CmsWorkplaceConfiguration.N_WORKPLACEGENERALOPTIONS);
            xp.append("/").append(CmsWorkplaceConfiguration.N_PUBLISHBUTTONAPPEARANCE);
            m_xpaths = Collections.singletonList(xp.toString());
        }
        return m_xpaths;
    }
}