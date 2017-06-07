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
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypePointer;
import org.opencms.setup.xml.A_CmsXmlWorkplace;
import org.opencms.setup.xml.CmsSetupXmlHelper;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Updates default permissions for explorer access.<p>
 *
 * @since 6.1.8
 */
public class CmsXmlUpdateDefaultPermissions extends A_CmsXmlWorkplace {

    /** List of xpaths to update. */
    private List<String> m_xpaths;

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Update default permissions for explorer access";
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        boolean changed = false;
        if (xpath.endsWith(CmsWorkplaceConfiguration.N_ACCESSCONTROL)) {
            Node node = document.selectSingleNode(xpath);
            if (node != null) {
                String xp = xpath
                    + "/"
                    + CmsWorkplaceConfiguration.N_ACCESSENTRY
                    + "[@"
                    + CmsWorkplaceConfiguration.A_PRINCIPAL
                    + "='???']";
                if ((xpath.indexOf(CmsResourceTypeJsp.getStaticTypeName()) < 0) && (xpath.indexOf("XMLTemplate") < 0)) {
                    if (xpath.indexOf(CmsWorkplaceConfiguration.N_DEFAULTACCESSCONTROL) < 0) {
                        changed = (0 < CmsSetupXmlHelper.setValue(
                            document,
                            CmsStringUtil.substitute(xp, "???", "DEFAULT"),
                            null)) || changed;
                        changed = (0 < CmsSetupXmlHelper.setValue(
                            document,
                            CmsStringUtil.substitute(xp, "???", "GROUP.Guests"),
                            null)) || changed;
                    }
                    changed = (0 < CmsSetupXmlHelper.setValue(
                        document,
                        CmsStringUtil.substitute(xp, "???", "GROUP.Administrators"),
                        null)) || changed;
                }
                changed = (0 < CmsSetupXmlHelper.setValue(
                    document,
                    CmsStringUtil.substitute(xp, "???", "GROUP.Projectmanagers"),
                    null)) || changed;
                changed = (0 < CmsSetupXmlHelper.setValue(
                    document,
                    CmsStringUtil.substitute(xp, "???", "GROUP.Users"),
                    null)) || changed;
                changed = (0 < CmsSetupXmlHelper.setValue(
                    document,
                    CmsStringUtil.substitute(xp, "???", "GROUP.TestGroup"),
                    null)) || changed;
                if (CmsSetupXmlHelper.getValue(
                    document,
                    xpath + "/" + CmsWorkplaceConfiguration.N_ACCESSENTRY) == null) {
                    if ((xpath.indexOf(CmsResourceTypeJsp.getStaticTypeName()) < 0)
                        && (xpath.indexOf("XMLTemplate") < 0)) {
                        changed = (0 < CmsSetupXmlHelper.setValue(document, xpath, null)) || changed;
                    }
                }
            }
            if ((xpath.indexOf(CmsResourceTypeJsp.getStaticTypeName()) > 0) || (xpath.indexOf("XMLTemplate") > 0)) {
                changed = setAccessEntry(document, xpath, "DEFAULT", "+r+v") || changed;
                changed = setAccessEntry(document, xpath, "GROUP.Administrators", "+r+v+w+c") || changed;
                changed = setAccessEntry(document, xpath, "GROUP.Guests", "-r-v-w-c") || changed;
            } else if (xpath.indexOf(CmsWorkplaceConfiguration.N_DEFAULTACCESSCONTROL) > 0) {
                changed = setAccessEntry(document, xpath, "DEFAULT", "+r+v+w+c") || changed;
                changed = setAccessEntry(document, xpath, "GROUP.Guests", "-r-v-w-c") || changed;
                changed = setAccessEntry(document, xpath, "ROLE.ELEMENT_AUTHOR", "+r+v+w+c") || changed;
            }
        }
        return changed;
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
            // /opencms/workplace/explorertypes/explorertype[@name='${etype}']/accesscontrol
            StringBuffer xp = new StringBuffer(256);
            xp.append("/").append(CmsConfigurationManager.N_ROOT);
            xp.append("/").append(CmsWorkplaceConfiguration.N_WORKPLACE);
            xp.append("/").append(CmsWorkplaceConfiguration.N_EXPLORERTYPES);
            xp.append("/").append(CmsWorkplaceConfiguration.N_EXPLORERTYPE);
            xp.append("[@").append(I_CmsXmlConfiguration.N_NAME);
            xp.append("='${etype}']/");
            xp.append(CmsWorkplaceConfiguration.N_ACCESSCONTROL);
            m_xpaths = new ArrayList<String>();
            // ${etype}: folder, imagegallery, downloadgallery, xmlcontent, xmlpage, plain, image, jsp, binary, pointer, XMLTemplate, link, upload
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", CmsResourceTypeFolder.getStaticTypeName()));
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), "${etype}", "imagegallery"));
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), "${etype}", "downloadgallery"));
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), "${etype}", "xmlcontent"));
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), "${etype}", CmsResourceTypePlain.getStaticTypeName()));
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), "${etype}", CmsResourceTypeImage.getStaticTypeName()));
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), "${etype}", CmsResourceTypeJsp.getStaticTypeName()));
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", CmsResourceTypeBinary.getStaticTypeName()));
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), "${etype}", "XMLTemplate"));
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", CmsResourceTypePointer.getStaticTypeName()));
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), "${etype}", "link"));
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), "${etype}", "upload"));

            // /opencms/workplace/explorertypes/defaultaccesscontrol/accesscontrol
            xp = new StringBuffer(256);
            xp.append("/").append(CmsConfigurationManager.N_ROOT);
            xp.append("/").append(CmsWorkplaceConfiguration.N_WORKPLACE);
            xp.append("/").append(CmsWorkplaceConfiguration.N_EXPLORERTYPES);
            xp.append("/").append(CmsWorkplaceConfiguration.N_DEFAULTACCESSCONTROL);
            xp.append("/").append(CmsWorkplaceConfiguration.N_ACCESSCONTROL);
            m_xpaths.add(xp.toString());
        }
        return m_xpaths;
    }

}