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
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.setup.xml.A_CmsXmlWorkplace;
import org.opencms.setup.xml.CmsSetupXmlHelper;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlUtils;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Adds the new rename and comment context menues for image galleries.<p>
 *
 * @since 6.1.8
 */
public class CmsXmlAddImgGalleryContextMenues extends A_CmsXmlWorkplace {

    /** List of xpaths to update. */
    private List<String> m_xpaths;

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Add new Rename and Comment context menues for Image Galleries";
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        Node node = document.selectSingleNode(xpath);
        if (xpath.indexOf(CmsWorkplaceConfiguration.N_ENTRY) > 0) {
            if (node == null) {
                String xp = CmsXmlUtils.removeLastComplexXpathElement(xpath);
                setMenuEntry(
                    document,
                    xp,
                    "explorer.context.renameimages",
                    "commons/renameimages.jsp",
                    "d d iiii aaai dddd",
                    "85");
                setMenuEntry(
                    document,
                    xp,
                    "explorer.context.commentimages",
                    "commons/commentimages.jsp",
                    "d d iiii aaai dddd",
                    "90");
                return true;
            }
        } else if (xpath.indexOf(CmsWorkplaceConfiguration.A_REFERENCE + "=") > 0) {
            if (node != null) {
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath + "/@" + CmsWorkplaceConfiguration.A_REFERENCE,
                    "downloadgallery");
                return true;
            }
        } else if (xpath.indexOf(CmsWorkplaceConfiguration.A_REFERENCE) > 0) {
            if (node != null) {
                CmsSetupXmlHelper.setValue(document, xpath, null);
                return true;
            }
        } else {
            if (node == null) {
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath
                        + "/"
                        + CmsWorkplaceConfiguration.N_DEFAULTPROPERTIES
                        + "/@"
                        + I_CmsXmlConfiguration.A_ENABLED,
                    Boolean.TRUE.toString());
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath
                        + "/"
                        + CmsWorkplaceConfiguration.N_DEFAULTPROPERTIES
                        + "/@"
                        + CmsWorkplaceConfiguration.A_SHOWNAVIGATION,
                    Boolean.TRUE.toString());
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath
                        + "/"
                        + CmsWorkplaceConfiguration.N_DEFAULTPROPERTIES
                        + "[1]/"
                        + I_CmsXmlConfiguration.N_PROPERTY
                        + "/@"
                        + I_CmsXmlConfiguration.A_NAME,
                    CmsPropertyDefinition.PROPERTY_TITLE);

                xpath += "/" + CmsWorkplaceConfiguration.N_CONTEXTMENU;
                setMenuEntry(document, xpath, "explorer.context.lock", "commons/lock.jsp", "d d aaaa dddd dddd", "10");
                setMenuEntry(
                    document,
                    xpath,
                    "explorer.context.overridelock",
                    "commons/lockchange.jsp",
                    "d d dddd dddd aaaa",
                    "20");
                setMenuEntry(
                    document,
                    xpath,
                    "explorer.context.unlock",
                    "commons/unlock.jsp",
                    "d d dddd aaaa dddd",
                    "30");
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath
                        + "/"
                        + CmsWorkplaceConfiguration.N_SEPARATOR
                        + "[@"
                        + I_CmsXmlConfiguration.A_ORDER
                        + "='40']"
                        + "/@"
                        + I_CmsXmlConfiguration.A_ORDER,
                    "40");
                setMenuEntry(
                    document,
                    xpath,
                    "explorer.context.copytoproject",
                    "commons/copytoproject.jsp",
                    "d a dddd dddd dddd",
                    "50");
                setMenuEntry(
                    document,
                    xpath,
                    "explorer.context.publish",
                    "commons/publishresource.jsp",
                    "d d aaaa aaaa dddd",
                    "60");
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath
                        + "/"
                        + CmsWorkplaceConfiguration.N_SEPARATOR
                        + "[@"
                        + I_CmsXmlConfiguration.A_ORDER
                        + "='70']"
                        + "/@"
                        + I_CmsXmlConfiguration.A_ORDER,
                    "70");
                setMenuEntry(
                    document,
                    xpath,
                    "explorer.context.opengallery",
                    "commons/opengallery.jsp",
                    "d d iiii aaai dddd",
                    "80");
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath
                        + "/"
                        + CmsWorkplaceConfiguration.N_SEPARATOR
                        + "[@"
                        + I_CmsXmlConfiguration.A_ORDER
                        + "='100']"
                        + "/@"
                        + I_CmsXmlConfiguration.A_ORDER,
                    "100");
                setMenuEntry(document, xpath, "explorer.context.copy", "commons/copy.jsp", "d d iiii aaai dddd", "150");
                setMenuEntry(document, xpath, "explorer.context.move", "commons/move.jsp", "d d iiii aaai dddd", "170");
                setMenuEntry(
                    document,
                    xpath,
                    "explorer.context.delete",
                    "commons/delete.jsp",
                    "d d iiii aaai dddd",
                    "180");
                setMenuEntry(
                    document,
                    xpath,
                    "explorer.context.touch",
                    "commons/touch.jsp",
                    "d d iiii aaai dddd",
                    "190");
                setMenuEntry(
                    document,
                    xpath,
                    "explorer.context.availability",
                    "commons/availability.jsp",
                    "d d iiii aaai dddd",
                    "200");
                setMenuEntry(
                    document,
                    xpath,
                    "explorer.context.undochanges",
                    "commons/undochanges.jsp",
                    "d d iiid aaid dddd",
                    "210");
                setMenuEntry(
                    document,
                    xpath,
                    "explorer.context.undelete",
                    "commons/undelete.jsp",
                    "d d ddda ddda dddd",
                    "220");
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath
                        + "/"
                        + CmsWorkplaceConfiguration.N_SEPARATOR
                        + "[@"
                        + I_CmsXmlConfiguration.A_ORDER
                        + "='240']"
                        + "/@"
                        + I_CmsXmlConfiguration.A_ORDER,
                    "240");
                setMenuEntry(
                    document,
                    xpath,
                    "explorer.context.access",
                    "commons/chacc.jsp",
                    "a a iiii aaai dddd",
                    "300");
                setMenuEntry(
                    document,
                    xpath,
                    "explorer.context.secure",
                    "commons/secure.jsp",
                    "d d iiii aaai dddd",
                    "302");
                setMenuEntry(
                    document,
                    xpath,
                    "explorer.context.type",
                    "commons/chtype.jsp",
                    "d d iiii aaai dddd",
                    "305");
                setMenuEntry(
                    document,
                    xpath,
                    "explorer.context.chnav",
                    "commons/chnav.jsp",
                    "d d iiii aaai dddd",
                    "310");
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath
                        + "/"
                        + CmsWorkplaceConfiguration.N_SEPARATOR
                        + "[@"
                        + I_CmsXmlConfiguration.A_ORDER
                        + "='340']"
                        + "/@"
                        + I_CmsXmlConfiguration.A_ORDER,
                    "340");
                setMenuEntry(document, xpath, "explorer.context.property", "commons/property.jsp", "", "360");
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
            // /opencms/workplace/explorertypes/explorertype[@name='imagegallery']/editoptions/contextmenu/entry[@uri='commons/${res}images.jsp']
            StringBuffer xp = new StringBuffer(256);
            xp.append("/").append(CmsConfigurationManager.N_ROOT);
            xp.append("/").append(CmsWorkplaceConfiguration.N_WORKPLACE);
            xp.append("/").append(CmsWorkplaceConfiguration.N_EXPLORERTYPES);
            xp.append("/").append(CmsWorkplaceConfiguration.N_EXPLORERTYPE);
            xp.append("[@").append(I_CmsXmlConfiguration.N_NAME);
            xp.append("='").append("imagegallery");
            xp.append("']/").append(CmsWorkplaceConfiguration.N_EDITOPTIONS);
            xp.append("/").append(CmsWorkplaceConfiguration.N_CONTEXTMENU);
            xp.append("/").append(CmsWorkplaceConfiguration.N_ENTRY);
            xp.append("[@").append(I_CmsXmlConfiguration.A_URI);
            xp.append("='commons/${res}images.jsp']");
            m_xpaths = new ArrayList<String>();
            // ${res}: rename, comment
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), "${res}", "rename"));
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), "${res}", "comment"));
            // /opencms/workplace/explorertypes/explorertype[@name='${etype}' and @reference='imagegallery']
            xp = new StringBuffer(256);
            xp.append("/").append(CmsConfigurationManager.N_ROOT);
            xp.append("/").append(CmsWorkplaceConfiguration.N_WORKPLACE);
            xp.append("/").append(CmsWorkplaceConfiguration.N_EXPLORERTYPES);
            xp.append("/").append(CmsWorkplaceConfiguration.N_EXPLORERTYPE);
            xp.append("[@").append(I_CmsXmlConfiguration.N_NAME);
            xp.append("='").append("${etype}");
            xp.append("' and @").append(CmsWorkplaceConfiguration.A_REFERENCE);
            xp.append("='").append("imagegallery");
            xp.append("']");
            // ???: linkgallery, htmlgallery, tablegallery
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), "${etype}", "linkgallery"));

            xp = new StringBuffer(256);
            xp.append("/").append(CmsConfigurationManager.N_ROOT);
            xp.append("/").append(CmsWorkplaceConfiguration.N_WORKPLACE);
            xp.append("/").append(CmsWorkplaceConfiguration.N_EXPLORERTYPES);
            xp.append("/").append(CmsWorkplaceConfiguration.N_EXPLORERTYPE);
            xp.append("[@").append(I_CmsXmlConfiguration.N_NAME);
            xp.append("='").append("downloadgallery");
            xp.append("']/");
            // /opencms/workplace/explorertypes/explorertype[@name='downloadgallery']/editoptions
            m_xpaths.add(xp.toString() + CmsWorkplaceConfiguration.N_EDITOPTIONS);
            // /opencms/workplace/explorertypes/explorertype[@name='downloadgallery']/@reference
            xp = new StringBuffer(256);
            xp.append("/");
            xp.append(CmsConfigurationManager.N_ROOT);
            xp.append("/");
            xp.append(CmsWorkplaceConfiguration.N_WORKPLACE);
            xp.append("/");
            xp.append(CmsWorkplaceConfiguration.N_EXPLORERTYPES);
            xp.append("/");
            xp.append(CmsWorkplaceConfiguration.N_EXPLORERTYPE);
            xp.append("[@");
            xp.append(I_CmsXmlConfiguration.N_NAME);
            xp.append("='");
            xp.append("downloadgallery");
            xp.append("']/@");
            xp.append(CmsWorkplaceConfiguration.A_REFERENCE);
            m_xpaths.add(xp.toString());
        }
        return m_xpaths;
    }
}