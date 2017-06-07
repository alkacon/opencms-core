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
 * Adds the auto set features in new dialog.<p>
 *
 * @since 6.1.8
 */
public class CmsXmlAddAutoSetFeatures extends A_CmsXmlWorkplace {

    /** List of xpaths to update. */
    private List<String> m_xpaths;

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Add auto set features in new dialog";
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        Node node = document.selectSingleNode(xpath);
        if (node == null) {
            CmsSetupXmlHelper.setValue(document, xpath, Boolean.FALSE.toString());
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
            // /opencms/workplace/explorertypes/explorertype[@name='${etype}']/newresource/@${attr}
            StringBuffer xp = new StringBuffer(256);
            xp.append("/").append(CmsConfigurationManager.N_ROOT);
            xp.append("/").append(CmsWorkplaceConfiguration.N_WORKPLACE);
            xp.append("/").append(CmsWorkplaceConfiguration.N_EXPLORERTYPES);
            xp.append("/").append(CmsWorkplaceConfiguration.N_EXPLORERTYPE);
            xp.append("[@").append(I_CmsXmlConfiguration.N_NAME);
            xp.append("='${etype}']/").append(CmsWorkplaceConfiguration.N_NEWRESOURCE);
            xp.append("/@");
            m_xpaths = new ArrayList<String>();
            // ${etype}: folder, imagegallery, downloadgallery, xmlcontent, xmlpage, plain, image, jsp, binary, pointer, XMLTemplate, link, upload, extendedfolder, structurecontent
            // ${attr}: autosetnavigation, autosettitle
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", CmsResourceTypeFolder.getStaticTypeName())
                    + CmsWorkplaceConfiguration.A_AUTOSETNAVIGATION);
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", "imagegallery")
                    + CmsWorkplaceConfiguration.A_AUTOSETNAVIGATION);
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", "downloadgallery")
                    + CmsWorkplaceConfiguration.A_AUTOSETNAVIGATION);
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", "xmlcontent")
                    + CmsWorkplaceConfiguration.A_AUTOSETNAVIGATION);
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", CmsResourceTypePlain.getStaticTypeName())
                    + CmsWorkplaceConfiguration.A_AUTOSETNAVIGATION);
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", CmsResourceTypeImage.getStaticTypeName())
                    + CmsWorkplaceConfiguration.A_AUTOSETNAVIGATION);
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", CmsResourceTypeJsp.getStaticTypeName())
                    + CmsWorkplaceConfiguration.A_AUTOSETNAVIGATION);
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", CmsResourceTypeBinary.getStaticTypeName())
                    + CmsWorkplaceConfiguration.A_AUTOSETNAVIGATION);
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", "XMLTemplate")
                    + CmsWorkplaceConfiguration.A_AUTOSETNAVIGATION);
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", CmsResourceTypePointer.getStaticTypeName())
                    + CmsWorkplaceConfiguration.A_AUTOSETNAVIGATION);
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", "link")
                    + CmsWorkplaceConfiguration.A_AUTOSETNAVIGATION);
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", "upload")
                    + CmsWorkplaceConfiguration.A_AUTOSETNAVIGATION);
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", "extendedfolder")
                    + CmsWorkplaceConfiguration.A_AUTOSETNAVIGATION);
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", "structurecontent")
                    + CmsWorkplaceConfiguration.A_AUTOSETNAVIGATION);

            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", CmsResourceTypeFolder.getStaticTypeName())
                    + CmsWorkplaceConfiguration.A_AUTOSETTITLE);
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", "imagegallery")
                    + CmsWorkplaceConfiguration.A_AUTOSETTITLE);
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", "downloadgallery")
                    + CmsWorkplaceConfiguration.A_AUTOSETTITLE);
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", "xmlcontent")
                    + CmsWorkplaceConfiguration.A_AUTOSETTITLE);
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", CmsResourceTypePlain.getStaticTypeName())
                    + CmsWorkplaceConfiguration.A_AUTOSETTITLE);
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", CmsResourceTypeImage.getStaticTypeName())
                    + CmsWorkplaceConfiguration.A_AUTOSETTITLE);
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", CmsResourceTypeJsp.getStaticTypeName())
                    + CmsWorkplaceConfiguration.A_AUTOSETTITLE);
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", CmsResourceTypeBinary.getStaticTypeName())
                    + CmsWorkplaceConfiguration.A_AUTOSETTITLE);
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", "XMLTemplate")
                    + CmsWorkplaceConfiguration.A_AUTOSETTITLE);
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", CmsResourceTypePointer.getStaticTypeName())
                    + CmsWorkplaceConfiguration.A_AUTOSETTITLE);
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", "link") + CmsWorkplaceConfiguration.A_AUTOSETTITLE);
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", "upload")
                    + CmsWorkplaceConfiguration.A_AUTOSETTITLE);
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", "extendedfolder")
                    + CmsWorkplaceConfiguration.A_AUTOSETTITLE);
            m_xpaths.add(
                CmsStringUtil.substitute(xp.toString(), "${etype}", "structurecontent")
                    + CmsWorkplaceConfiguration.A_AUTOSETTITLE);
        }
        return m_xpaths;
    }

}