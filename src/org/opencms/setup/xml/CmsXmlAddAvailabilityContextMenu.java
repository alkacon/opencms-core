/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/xml/Attic/CmsXmlAddAvailabilityContextMenu.java,v $
 * Date   : $Date: 2006/03/27 14:52:44 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Adds the availability context menu node.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 6.1.8 
 */
public class CmsXmlAddAvailabilityContextMenu extends A_CmsXmlWorkplace {

    /** List of xpaths to update. */
    private List m_xpaths;

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Add context menu for Availability";
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String)
     */
    protected boolean executeUpdate(Document document, String xpath) {

        Node node = document.selectSingleNode(xpath);
        if (node == null) {
            if (xpath.indexOf("availability") > 0) {
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath + "/@" + I_CmsXmlConfiguration.A_URI,
                    "commons/availability.jsp");
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath + "/@" + I_CmsXmlConfiguration.A_KEY,
                    "explorer.context.availability");
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath + "/@" + CmsWorkplaceConfiguration.A_RULES,
                    "d d iiii aaai dddd");
                CmsSetupXmlHelper.setValue(document, xpath + "/@" + I_CmsXmlConfiguration.A_ORDER, "200");
            } else if (xpath.indexOf("undochanges") > 0) {
                CmsSetupXmlHelper.setValue(document, xpath + "/@" + I_CmsXmlConfiguration.A_ORDER, "210");
            } else if (xpath.indexOf("undelete") > 0) {
                CmsSetupXmlHelper.setValue(document, xpath + "/@" + I_CmsXmlConfiguration.A_ORDER, "220");
            }
            return true;
        }
        return false;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getCommonPath()
     */
    protected String getCommonPath() {

        // /opencms/workplace/explorertypes
        return new StringBuffer("/").append(CmsConfigurationManager.N_ROOT).append("/").append(
            CmsWorkplaceConfiguration.N_WORKPLACE).append("/").append(CmsWorkplaceConfiguration.N_EXPLORERTYPES).toString();
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    protected List getXPathsToUpdate() {

        if (m_xpaths == null) {
            // /opencms/workplace/explorertypes/explorertype[@name='${etype}']/editoptions/contextmenu/entry[@uri='commons/${res}.jsp']
            StringBuffer xp = new StringBuffer(256);
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
            xp.append("='${etype}']/");
            xp.append(CmsWorkplaceConfiguration.N_EDITOPTIONS);
            xp.append("/");
            xp.append(CmsWorkplaceConfiguration.N_CONTEXTMENU);
            xp.append("/");
            xp.append(CmsWorkplaceConfiguration.N_ENTRY);
            xp.append("[@");
            xp.append(I_CmsXmlConfiguration.A_URI);
            xp.append("='commons/${res}.jsp']");
            m_xpaths = new ArrayList();
            // ${etype}: folder, imagegallery, xmlcontent, xmlpage, plain, image, jsp, binary, XMLTemplate
            // ${res}: availability, undochanges, undelete 
            Map subs = new HashMap();
            subs.put("${res}", "availability");
            subs.put("${etype}", CmsResourceTypeFolder.getStaticTypeName());
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", "imagegallery");
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", "xmlcontent");
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", CmsResourceTypeXmlPage.getStaticTypeName());
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", CmsResourceTypePlain.getStaticTypeName());
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", CmsResourceTypeImage.getStaticTypeName());
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", CmsResourceTypeJsp.getStaticTypeName());
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", CmsResourceTypeBinary.getStaticTypeName());
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", "XMLTemplate");
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));

            subs.put("${res}", "undochanges");
            subs.put("${etype}", CmsResourceTypeFolder.getStaticTypeName());
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", "imagegallery");
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", "xmlcontent");
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", CmsResourceTypeXmlPage.getStaticTypeName());
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", CmsResourceTypePlain.getStaticTypeName());
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", CmsResourceTypeImage.getStaticTypeName());
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", CmsResourceTypeJsp.getStaticTypeName());
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", CmsResourceTypeBinary.getStaticTypeName());
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", "XMLTemplate");
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));

            subs.put("${res}", "undelete");
            subs.put("${etype}", CmsResourceTypeFolder.getStaticTypeName());
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", "imagegallery");
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", "xmlcontent");
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", CmsResourceTypeXmlPage.getStaticTypeName());
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", CmsResourceTypePlain.getStaticTypeName());
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", CmsResourceTypeImage.getStaticTypeName());
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", CmsResourceTypeJsp.getStaticTypeName());
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", CmsResourceTypeBinary.getStaticTypeName());
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", "XMLTemplate");
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
        }
        return m_xpaths;
    }

}