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
import org.opencms.configuration.CmsVfsConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.setup.xml.A_CmsXmlVfs;
import org.opencms.setup.xml.CmsSetupXmlHelper;
import org.opencms.widgets.CmsHtmlWidget;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Replaces the html area widget entries.<p>
 *
 * @since 6.1.8
 */
public class CmsXmlReplaceHtmlAreaWidgets extends A_CmsXmlVfs {

    /** List of xpaths to update. */
    private List<String> m_xpaths;

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Replace HtmlArea widget entries";
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        // node have to exists to be able to update
        Node node = document.selectSingleNode(xpath);
        if (node != null) {
            if (xpath.equals(getXPathsToUpdate().get(0))) {
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath + "/@" + I_CmsXmlConfiguration.A_CLASS,
                    CmsHtmlWidget.class.getName());
            } else if (xpath.equals(getXPathsToUpdate().get(1))) {
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath + "/@" + CmsVfsConfiguration.A_DEFAULTWIDGET,
                    CmsHtmlWidget.class.getName());
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

        // /opencms/vfs/xmlcontent
        StringBuffer xp = new StringBuffer(256);
        xp.append("/").append(CmsConfigurationManager.N_ROOT);
        xp.append("/").append(CmsVfsConfiguration.N_VFS);
        xp.append("/").append(CmsVfsConfiguration.N_XMLCONTENT);
        return xp.toString();
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        if (m_xpaths == null) {
            // /opencms/vfs/xmlcontent/widgets/widget[@class='org.opencms.widgets.CmsHtmlAreaWidget']
            StringBuffer xp = new StringBuffer(256);
            xp.append("/").append(CmsConfigurationManager.N_ROOT);
            xp.append("/").append(CmsVfsConfiguration.N_VFS);
            xp.append("/").append(CmsVfsConfiguration.N_XMLCONTENT);
            xp.append("/").append(CmsVfsConfiguration.N_WIDGETS);
            xp.append("/").append(CmsVfsConfiguration.N_WIDGET);
            xp.append("[@").append(I_CmsXmlConfiguration.A_CLASS);
            xp.append("='org.opencms.widgets.CmsHtmlAreaWidget']");
            m_xpaths = new ArrayList<String>();
            m_xpaths.add(xp.toString());
            // /opencms/vfs/xmlcontent/schematypes/schematype[@defaultwidget='org.opencms.widgets.CmsHtmlAreaWidget']
            xp = new StringBuffer(256);
            xp.append("/").append(CmsConfigurationManager.N_ROOT);
            xp.append("/").append(CmsVfsConfiguration.N_VFS);
            xp.append("/").append(CmsVfsConfiguration.N_XMLCONTENT);
            xp.append("/").append(CmsVfsConfiguration.N_SCHEMATYPES);
            xp.append("/").append(CmsVfsConfiguration.N_SCHEMATYPE);
            xp.append("[@").append(CmsVfsConfiguration.A_DEFAULTWIDGET);
            xp.append("='org.opencms.widgets.CmsHtmlAreaWidget']");
            m_xpaths.add(xp.toString());
        }
        return m_xpaths;
    }
}