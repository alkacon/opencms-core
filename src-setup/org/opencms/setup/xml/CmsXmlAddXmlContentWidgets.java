/*
 * File   : $Source: /alkacon/cvs/opencms/src-setup/org/opencms/setup/xml/CmsXmlAddXmlContentWidgets.java,v $
 * Date   : $Date: 2007/08/22 11:11:45 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2007 Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.configuration.CmsVfsConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsGroupWidget;
import org.opencms.widgets.CmsMultiSelectWidget;
import org.opencms.widgets.CmsUserWidget;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Adds the new xml content widgets.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 6.1.8 
 */
public class CmsXmlAddXmlContentWidgets extends A_CmsSetupXmlUpdate {

    /** List of xpaths to update. */
    private List m_xpaths;

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Add new Xml Content widgets";
    }

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getXmlFilename()
     */
    public String getXmlFilename() {

        return CmsVfsConfiguration.DEFAULT_XML_FILE_NAME;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String)
     */
    protected boolean executeUpdate(Document document, String xpath) {

        Node node = document.selectSingleNode(xpath);
        if (node == null) {
            if (getXPathsToUpdate().contains(xpath)) {
                if (xpath.indexOf("DisplayWidget") > 0) {
                    CmsSetupXmlHelper.setValue(document, xpath + "/@" + I_CmsXmlConfiguration.A_ALIAS, "DisplayWidget");
                    CmsSetupXmlHelper.setValue(
                        document,
                        xpath + "/@" + I_CmsXmlConfiguration.A_CLASS,
                        CmsDisplayWidget.class.getName());
                } else if (xpath.indexOf("MultiSelectWidget") > 0) {
                    CmsSetupXmlHelper.setValue(
                        document,
                        xpath + "/@" + I_CmsXmlConfiguration.A_ALIAS,
                        "MultiSelectWidget");
                    CmsSetupXmlHelper.setValue(
                        document,
                        xpath + "/@" + I_CmsXmlConfiguration.A_CLASS,
                        CmsMultiSelectWidget.class.getName());
                } else if (xpath.indexOf("UserWidget") > 0) {
                    CmsSetupXmlHelper.setValue(document, xpath + "/@" + I_CmsXmlConfiguration.A_ALIAS, "UserWidget");
                    CmsSetupXmlHelper.setValue(
                        document,
                        xpath + "/@" + I_CmsXmlConfiguration.A_CLASS,
                        CmsUserWidget.class.getName());
                } else if (xpath.indexOf("GroupWidget") > 0) {
                    CmsSetupXmlHelper.setValue(document, xpath + "/@" + I_CmsXmlConfiguration.A_ALIAS, "GroupWidget");
                    CmsSetupXmlHelper.setValue(
                        document,
                        xpath + "/@" + I_CmsXmlConfiguration.A_CLASS,
                        CmsGroupWidget.class.getName());
                }
                return true;
            }
        }
        return false;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getCommonPath()
     */
    protected String getCommonPath() {

        // /opencms/vfs/xmlcontent/widgets
        StringBuffer xp = new StringBuffer(256);
        xp.append("/").append(CmsConfigurationManager.N_ROOT);
        xp.append("/").append(CmsVfsConfiguration.N_VFS);
        xp.append("/").append(CmsVfsConfiguration.N_XMLCONTENT);
        xp.append("/").append(CmsVfsConfiguration.N_WIDGETS);
        return xp.toString();
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    protected List getXPathsToUpdate() {

        if (m_xpaths == null) {
            // "/opencms/vfs/xmlcontent/widgets/widget[@alias='widget']";
            StringBuffer xp = new StringBuffer(256);
            xp.append("/").append(CmsConfigurationManager.N_ROOT);
            xp.append("/").append(CmsVfsConfiguration.N_VFS);
            xp.append("/").append(CmsVfsConfiguration.N_XMLCONTENT);
            xp.append("/").append(CmsVfsConfiguration.N_WIDGETS);
            xp.append("/").append(CmsVfsConfiguration.N_WIDGET);
            xp.append("[@").append(I_CmsXmlConfiguration.A_ALIAS);
            xp.append("='");
            m_xpaths = new ArrayList();
            m_xpaths.add(xp.toString() + "DisplayWidget']");
            m_xpaths.add(xp.toString() + "MultiSelectWidget']");
            m_xpaths.add(xp.toString() + "UserWidget']");
            m_xpaths.add(xp.toString() + "GroupWidget']");
        }
        return m_xpaths;
    }

}