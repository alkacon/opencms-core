/*
 * File   : $Source: /alkacon/cvs/opencms/src-setup/org/opencms/setup/xml/v8/Attic/CmsXmlAddExplorerTypes.java,v $
 * Date   : $Date: 2010/02/24 12:44:21 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.setup.xml.v8;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsWorkplaceConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.CmsResourceTypeXmlSitemap;
import org.opencms.setup.xml.A_CmsXmlWorkplace;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Add new explorer types.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 */
public class CmsXmlAddExplorerTypes extends A_CmsXmlWorkplace {

    /** List of xpaths to update. */
    private List<String> m_xpaths;

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Add new explorer types";
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        boolean modified = false;
        Node node = document.selectSingleNode(xpath);
        if (node == null) {
            if (xpath.equals(getXPathsToUpdate().get(0))) {
                // <explorertype name="ade" key="fileicon.ade" icon="ade_menu.gif" reference="xmlcontent">
                createExplorerType(document, xpath, "ade", "fileicon.ade", "ade_menu.gif", "xmlcontent");
                // <newresource uri="newresource.jsp?page=ade" order="13" autosetnavigation="false" autosettitle="false" info="desc.ade"/>
                createEtNewResource(document, xpath, "newresource.jsp?page=ade", 13, false, false, "desc.ade", null);
            } else if (xpath.equals(getXPathsToUpdate().get(1))) {
                // <explorertype name="containerpage" key="fileicon.containerpage" icon="containerpage.gif" reference="xmlcontent">
                createExplorerType(
                    document,
                    xpath,
                    "containerpage",
                    "fileicon.containerpage",
                    "containerpage.gif",
                    "xmlcontent");
                // <newresource page="ade" uri="newresource_xmlcontent.jsp?newresourcetype=containerpage" order="10" autosetnavigation="false" autosettitle="false" info="desc.containerpage"/>
                createEtNewResource(
                    document,
                    xpath,
                    "newresource_xmlcontent.jsp?newresourcetype=containerpage",
                    10,
                    false,
                    false,
                    "desc.containerpage",
                    "ade");
                // <accessentry principal="ROLE.WORKPLACE_USER" permissions="+r+v+w+c"/>
                setAccessEntry(
                    document,
                    xpath + "/" + CmsWorkplaceConfiguration.N_ACCESSCONTROL,
                    "ROLE.WORKPLACE_USER",
                    "+r+v+w+c");
            } else if (xpath.equals(getXPathsToUpdate().get(2))) {
                // <explorertype name="containerpage_config" key="fileicon.containerpage_config" icon="containerpage_config.png" reference="xmlcontent">
                createExplorerType(
                    document,
                    xpath,
                    "containerpage_config",
                    "fileicon.containerpage_config",
                    "containerpage_config.png",
                    "xmlcontent");
                // <newresource page="ade" uri="newresource_xmlcontent.jsp?newresourcetype=containerpage_config" order="20" autosetnavigation="false" autosettitle="false" info="desc.containerpage_config"/>
                createEtNewResource(
                    document,
                    xpath,
                    "newresource_xmlcontent.jsp?newresourcetype=containerpage_config",
                    20,
                    false,
                    false,
                    "desc.containerpage_config",
                    "ade");
                // <accessentry principal="ROLE.WORKPLACE_USER" permissions="+r+v+w+c"/>
                setAccessEntry(
                    document,
                    xpath + "/" + CmsWorkplaceConfiguration.N_ACCESSCONTROL,
                    "ROLE.WORKPLACE_USER",
                    "+r+v+w+c");
            } else if (xpath.equals(getXPathsToUpdate().get(3))) {
                // <explorertype name="subcontainer" key="fileicon.subcontainer" icon="containerpage.gif" reference="xmlcontent">
                createExplorerType(
                    document,
                    xpath,
                    "subcontainer",
                    "fileicon.subcontainer",
                    "containerpage.gif",
                    "xmlcontent");
                // <newresource page="ade" uri="newresource_xmlcontent.jsp?newresourcetype=subcontainer" order="10" autosetnavigation="false" autosettitle="false" info="desc.subcontainer"/>
                createEtNewResource(
                    document,
                    xpath,
                    "newresource_xmlcontent.jsp?newresourcetype=subcontainer",
                    10,
                    false,
                    false,
                    "desc.subcontainer",
                    "ade");
                // <accessentry principal="ROLE.WORKPLACE_USER" permissions="+r+v+w+c"/>
                setAccessEntry(
                    document,
                    xpath + "/" + CmsWorkplaceConfiguration.N_ACCESSCONTROL,
                    "ROLE.WORKPLACE_USER",
                    "+r+v+w+c");
            } else if (xpath.equals(getXPathsToUpdate().get(4))) {
                // <explorertype name="sitemap" key="fileicon.sitemap" icon="sitemap.gif" reference="xmlcontent">
                createExplorerType(document, xpath, "sitemap", "fileicon.sitemap", "sitemap.gif", "xmlcontent");
                // <newresource page="ade" uri="newresource_xmlcontent.jsp?newresourcetype=sitemap" order="30" autosetnavigation="false" autosettitle="false" info="desc.sitemap"/>
                createEtNewResource(
                    document,
                    xpath,
                    "newresource_xmlcontent.jsp?newresourcetype=sitemap",
                    30,
                    false,
                    false,
                    "desc.sitemap",
                    "ade");
                // <accessentry principal="ROLE.WORKPLACE_USER" permissions="+r+v+w+c"/>
                setAccessEntry(
                    document,
                    xpath + "/" + CmsWorkplaceConfiguration.N_ACCESSCONTROL,
                    "ROLE.WORKPLACE_USER",
                    "+r+v+w+c");
            } else if (xpath.equals(getXPathsToUpdate().get(5))) {
                // <explorertype name="cntpagegallery" key="fileicon.cntpagegallery" icon="htmlgallery.gif" reference="downloadgallery">
                createExplorerType(
                    document,
                    xpath,
                    "cntpagegallery",
                    "fileicon.cntpagegallery",
                    "htmlgallery.gif",
                    "downloadgallery");
                // <newresource page="extendedfolder" uri="newresource.jsp?newresourcetype=cntpagegallery" order="40" autosetnavigation="false" autosettitle="false" info="desc.cntpagegallery"/>
                createEtNewResource(
                    document,
                    xpath,
                    "newresource.jsp?newresourcetype=cntpagegallery",
                    40,
                    false,
                    false,
                    "desc.cntpagegallery",
                    "extendedfolder");
            } else if (xpath.equals(getXPathsToUpdate().get(6))) {
                // <explorertype name="containerpage_template" key="fileicon.containerpage_template" icon="containerpage.gif" reference="jsp">
                createExplorerType(
                    document,
                    xpath,
                    "containerpage_template",
                    "fileicon.containerpage_template",
                    "containerpage.gif",
                    "jsp");
                // <newresource page="ade" uri="newresource.jsp?newresourcetype=containerpage_template" order="21" autosetnavigation="false" autosettitle="false" info="desc.containerpage_template" />
                createEtNewResource(
                    document,
                    xpath,
                    "newresource.jsp?newresourcetype=containerpage_template",
                    21,
                    false,
                    false,
                    "desc.containerpage_template",
                    "ade");
            }
            modified = true;
        }
        return modified;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getCommonPath()
     */
    @Override
    protected String getCommonPath() {

        // /opencms/workplace/explorertypes
        return new StringBuffer("/").append(CmsConfigurationManager.N_ROOT).append("/").append(
            CmsWorkplaceConfiguration.N_WORKPLACE).append("/").append(CmsWorkplaceConfiguration.N_EXPLORERTYPES).toString();
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        if (m_xpaths == null) {
            // /opencms/workplace/explorertypes/explorertype[@name='${etype}']
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
            xp.append("='${etype}']");

            m_xpaths = new ArrayList<String>();
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), "${etype}", "ade"));
            m_xpaths.add(CmsStringUtil.substitute(
                xp.toString(),
                "${etype}",
                CmsResourceTypeXmlContainerPage.getStaticTypeName()));
            m_xpaths.add(CmsStringUtil.substitute(
                xp.toString(),
                "${etype}",
                CmsResourceTypeXmlContainerPage.CONFIGURATION_TYPE_NAME));
            m_xpaths.add(CmsStringUtil.substitute(
                xp.toString(),
                "${etype}",
                CmsResourceTypeXmlContainerPage.SUB_CONTAINER_TYPE_NAME));
            m_xpaths.add(CmsStringUtil.substitute(
                xp.toString(),
                "${etype}",
                CmsResourceTypeXmlSitemap.getStaticTypeName()));
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), "${etype}", "cntpagegallery"));
            m_xpaths.add(CmsStringUtil.substitute(
                xp.toString(),
                "${etype}",
                CmsResourceTypeJsp.getContainerPageTemplateTypeName()));
        }
        return m_xpaths;
    }
}
