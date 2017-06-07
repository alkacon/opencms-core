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
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypePointer;
import org.opencms.setup.xml.A_CmsSetupXmlUpdate;
import org.opencms.setup.xml.CmsSetupXmlHelper;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Modifies the history context menu.<p>
 *
 * @since 6.1.8
 */
public class CmsXmlUpdateHistoryContextMenu extends A_CmsSetupXmlUpdate {

    /** List of xpaths to update. */
    private List<String> m_xpaths;

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Modify the History context menu";
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
        if (xpath.indexOf(CmsWorkplaceConfiguration.N_TOOLMANAGER) < 0) {
            if (node != null) {
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath + "/@" + I_CmsXmlConfiguration.A_URI,
                    "views/admin/admin-main.jsp?root=explorer&amp;path=%2Fhistory");

                return true;
            }
        } else {
            if (node == null) {
                String xp = xpath
                    + "/"
                    + CmsWorkplaceConfiguration.N_ROOTS
                    + "/"
                    + CmsWorkplaceConfiguration.N_ROOT
                    + "["
                    + CmsWorkplaceConfiguration.N_KEY
                    + "='admin']";

                CmsSetupXmlHelper.setValue(document, xp + "/" + CmsWorkplaceConfiguration.N_KEY, "admin");
                CmsSetupXmlHelper.setValue(
                    document,
                    xp + "/" + CmsWorkplaceConfiguration.N_URI,
                    "/system/workplace/admin/");
                CmsSetupXmlHelper.setValue(
                    document,
                    xp + "/" + I_CmsXmlConfiguration.N_NAME,
                    "${key.GUI_ADMIN_VIEW_ROOT_NAME_0}");
                CmsSetupXmlHelper.setValue(
                    document,
                    xp + "/" + CmsWorkplaceConfiguration.N_HELPTEXT,
                    "${key.GUI_ADMIN_VIEW_ROOT_HELP_0}");

                xp = CmsStringUtil.substitute(xp, "admin", "explorer");
                CmsSetupXmlHelper.setValue(document, xp + "/" + CmsWorkplaceConfiguration.N_KEY, "explorer");
                CmsSetupXmlHelper.setValue(
                    document,
                    xp + "/" + CmsWorkplaceConfiguration.N_URI,
                    "/system/workplace/explorer/");
                CmsSetupXmlHelper.setValue(
                    document,
                    xp + "/" + I_CmsXmlConfiguration.N_NAME,
                    "${key.GUI_EXPLORER_VIEW_ROOT_NAME_0}");
                CmsSetupXmlHelper.setValue(
                    document,
                    xp + "/" + CmsWorkplaceConfiguration.N_HELPTEXT,
                    "${key.GUI_EXPLORER_VIEW_ROOT_HELP_0}");

                return true;
            }
        }
        return false;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getCommonPath()
     */
    @Override
    protected String getCommonPath() {

        // /opencms/workplace
        return new StringBuffer("/").append(CmsConfigurationManager.N_ROOT).append("/").append(
            CmsWorkplaceConfiguration.N_WORKPLACE).toString();
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        if (m_xpaths == null) {
            // /opencms/workplace/explorertypes/explorertype[@name='${etype}']/editoptions/contextmenu/entry[@uri='commons/history.jsp']
            StringBuffer xp = new StringBuffer(256);
            xp.append("/").append(CmsConfigurationManager.N_ROOT);
            xp.append("/").append(CmsWorkplaceConfiguration.N_WORKPLACE);
            xp.append("/").append(CmsWorkplaceConfiguration.N_EXPLORERTYPES);
            xp.append("/").append(CmsWorkplaceConfiguration.N_EXPLORERTYPE);
            xp.append("[@").append(I_CmsXmlConfiguration.N_NAME);
            xp.append("='${etype}']/");
            xp.append(CmsWorkplaceConfiguration.N_EDITOPTIONS);
            xp.append("/").append(CmsWorkplaceConfiguration.N_CONTEXTMENU);
            xp.append("/").append(CmsWorkplaceConfiguration.N_ENTRY);
            xp.append("[@").append(I_CmsXmlConfiguration.A_URI);
            xp.append("='commons/history.jsp']");
            m_xpaths = new ArrayList<String>();
            // ???: xmlcontent, xmlpage, plain, image, jsp, binary, pointer, XMLTemplate
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), "${etype}", "xmlcontent"));
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), "${etype}", CmsResourceTypePlain.getStaticTypeName()));
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), "${etype}", CmsResourceTypeImage.getStaticTypeName()));
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), "${etype}", CmsResourceTypeJsp.getStaticTypeName()));
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", CmsResourceTypeBinary.getStaticTypeName()));
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", CmsResourceTypePointer.getStaticTypeName()));
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), "${etype}", "XMLTemplate"));
            // /opencms/workplace/tool-manager
            xp = new StringBuffer(256);
            xp.append("/").append(CmsConfigurationManager.N_ROOT);
            xp.append("/").append(CmsWorkplaceConfiguration.N_WORKPLACE);
            xp.append("/").append(CmsWorkplaceConfiguration.N_TOOLMANAGER);
            m_xpaths.add(xp.toString());
        }
        return m_xpaths;
    }

}