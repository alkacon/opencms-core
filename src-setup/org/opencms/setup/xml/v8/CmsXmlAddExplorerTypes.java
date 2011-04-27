/*
 * File   : $Source: /alkacon/cvs/opencms/src-setup/org/opencms/setup/xml/v8/Attic/CmsXmlAddExplorerTypes.java,v $
 * Date   : $Date: 2011/04/27 14:44:33 $
 * Version: $Revision: 1.4 $
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
import org.opencms.setup.xml.A_CmsXmlWorkplace;
import org.opencms.util.CmsStringUtil;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
 * Add new explorer types.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.4 $ 
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
     * Adds the edit options to the containerpage explorer type.<p>
     * 
     * @param parent the parent node (i.e. the explorertype node for the container page)
     * 
     * @return true if the update succeeded 
     */
    protected boolean addEditOptions(org.dom4j.Element parent) {

        String editoptions = "        <editoptions>\r\n"
            + "          <defaultproperties enabled=\"true\" shownavigation=\"true\">\r\n"
            + "            <defaultproperty name=\"Title\"/>\r\n"
            + "            <defaultproperty name=\"Keywords\"/>\r\n"
            + "            <defaultproperty name=\"Description\"/>\r\n"
            + "          </defaultproperties>\r\n"
            + "          <contextmenu>\r\n"
            + "            <entry key=\"GUI_EXPLORER_CONTEXT_LOCK_0\" uri=\"commons/lock.jsp\" rule=\"lock\"/>\r\n"
            + "            <entry key=\"GUI_EXPLORER_CONTEXT_OVERRIDELOCK_0\" uri=\"commons/lockchange.jsp\" rule=\"changelock\"/>\r\n"
            + "            <entry key=\"GUI_EXPLORER_CONTEXT_UNLOCK_0\" uri=\"commons/unlock.jsp\" rule=\"unlock\"/>\r\n"
            + "            <separator/>\r\n"
            + "            <entry key=\"GUI_EXPLORER_CONTEXT_COPYTOPROJECT_0\" uri=\"commons/copytoproject.jsp\" rule=\"copytoproject\"/>\r\n"
            + "            <entry key=\"GUI_EXPLORER_CONTEXT_PUBLISH_0\" uri=\"commons/publishresource.jsp\" rule=\"directpublish\"/>\r\n"
            + "            <entry key=\"GUI_EXPLORER_CONTEXT_PUBLISH_SCHEDULED_0\" uri=\"commons/publishscheduledresource.jsp\" rule=\"publishscheduled\"/>\r\n"
            + "            <separator/>\r\n"
            + "            <entry key=\"GUI_EXPLORER_CONTEXT_EDIT_0\" uri=\"editors/editor.jsp\" target=\"_top\" rule=\"standard\"/>\r\n"
            + "            <separator/>\r\n"
            + "            <entry key=\"GUI_EXPLORER_CONTEXT_COPY_0\" uri=\"commons/copy.jsp\" rule=\"copy\"/>\r\n"
            + "            <entry key=\"GUI_EXPLORER_CONTEXT_MOVE_0\" uri=\"commons/move.jsp\" rule=\"standard\"/>\r\n"
            + "            <entry key=\"GUI_EXPLORER_CONTEXT_DELETE_0\" uri=\"commons/delete.jsp\" rule=\"standard\"/>\r\n"
            + "            <entry key=\"GUI_EXPLORER_CONTEXT_UNDOCHANGES_0\" uri=\"commons/undochanges.jsp\" rule=\"undochanges\"/>\r\n"
            + "            <entry key=\"GUI_EXPLORER_CONTEXT_UNDELETE_0\" uri=\"commons/undelete.jsp\" rule=\"undelete\"/>\r\n"
            + "            <separator/>\r\n"
            + "            <entry key=\"GUI_EXPLORER_CONTEXT_RELATIONS_0\" rule=\"substandard\">\r\n"
            + "                <entry key=\"GUI_EXPLORER_CONTEXT_LINKRELATIONTO_0\" uri=\"views/admin/admin-main.jsp?root=explorer&amp;path=%2Flinkrelationtarget\" rule=\"standard\"/>\r\n"
            + "                <entry key=\"GUI_EXPLORER_CONTEXT_LINKRELATIONFROM_0\" uri=\"views/admin/admin-main.jsp?root=explorer&amp;path=%2Flinkrelationsource\" rule=\"standard\"/>\r\n"
            + "                <separator/>\r\n"
            + "                <entry key=\"GUI_EXPLORER_CONTEXT_SHOWSIBLINGS_0\" uri=\"views/admin/admin-main.jsp?root=explorer&amp;path=%2Fsiblings\" rule=\"showsiblings\"/>\r\n"
            + "                <separator/>\r\n"
            + "                <entry key=\"GUI_EXPLORER_CONTEXT_CATEGORIES_0\" uri=\"views/admin/admin-main.jsp?root=explorer&amp;path=%2Fcategories\" rule=\"standard\"/>\r\n"
            + "            </entry>\r\n"
            + "            <separator/>\r\n"
            + "            <entry key=\"GUI_EXPLORER_CONTEXT_ACCESS_0\" uri=\"commons/chacc.jsp\" rule=\"permissions\"/>\r\n"
            + "            <entry key=\"GUI_EXPLORER_CONTEXT_CHNAV_0\" uri=\"commons/chnav.jsp\" rule=\"standard\"/>\r\n"
            + "            <separator/>\r\n"
            + "            <entry key=\"GUI_EXPLORER_CONTEXT_ADVANCED_0\" rule=\"substandard\">\r\n"
            + "                <entry key=\"GUI_EXPLORER_CONTEXT_TOUCH_0\" uri=\"commons/touch.jsp\" rule=\"standard\"/>\r\n"
            + "                <entry key=\"GUI_EXPLORER_CONTEXT_AVAILABILITY_0\" uri=\"commons/availability.jsp\" rule=\"standard\"/>\r\n"
            + "                <separator/>\r\n"
            + "                <entry key=\"GUI_EXPLORER_CONTEXT_SECURE_0\" uri=\"commons/secure.jsp\" rule=\"standard\"/>\r\n"
            + "                <entry key=\"GUI_EXPLORER_CONTEXT_TYPE_0\" uri=\"commons/chtype.jsp\" rule=\"standard\"/>\r\n"
            + "                <separator/>\r\n"
            + "                <entry key=\"GUI_EXPLORER_CONTEXT_EDITCONTROLFILE_0\" uri=\"editors/editor.jsp?editastext=true\" target=\"_top\" rule=\"editcontrolcode\"/>\r\n"
            + "            </entry>\r\n"
            + "            <separator/>\r\n"
            + "            <entry key=\"GUI_EXPLORER_CONTEXT_HISTORY_0\" uri=\"views/admin/admin-main.jsp?root=explorer&amp;path=%2Fhistory\" rule=\"nondeleted\"/>\r\n"
            + "            <entry key=\"GUI_EXPLORER_CONTEXT_PROPERTY_0\" uri=\"commons/property.jsp\" rule=\"nondeleted\"/>\r\n"
            + "            <entry key=\"GUI_EXPLORER_CONTEXT_AVAILABILITY_0\" name=\"org.opencms.gwt.client.ui.CmsAvailabilityDialog\" rule=\"containerpage\"/>\r\n"
            + "          </contextmenu>\r\n"
            + "        </editoptions>\r\n";
        try {
            SAXReader reader = new SAXReader();
            Document newNodeDocument = reader.read(new StringReader(editoptions));
            parent.add(newNodeDocument.getRootElement());
            return true;
        } catch (DocumentException e) {
            System.out.println("failed to update containerpage edit options!");
            e.printStackTrace();
            return false;
        }
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
                org.dom4j.Element parent = (org.dom4j.Element)document.selectSingleNode(xpath);
                addEditOptions(parent);
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
                // <explorertype name="groupcontainer" key="fileicon.groupcontainer" icon="containerpage.gif" reference="xmlcontent">
                createExplorerType(
                    document,
                    xpath,
                    "groupcontainer",
                    "fileicon.groupcontainer",
                    "containerpage.gif",
                    "xmlcontent");
                // <newresource page="ade" uri="newresource_xmlcontent.jsp?newresourcetype=groupcontainer" order="10" autosetnavigation="false" autosettitle="false" info="desc.groupcontainer"/>
                createEtNewResource(
                    document,
                    xpath,
                    "newresource_xmlcontent.jsp?newresourcetype=groupcontainer",
                    10,
                    false,
                    false,
                    "desc.groupcontainer",
                    "ade");
                // <accessentry principal="ROLE.WORKPLACE_USER" permissions="+r+v+w+c"/>
                setAccessEntry(
                    document,
                    xpath + "/" + CmsWorkplaceConfiguration.N_ACCESSCONTROL,
                    "ROLE.WORKPLACE_USER",
                    "+r+v+w+c");
            } else if (xpath.equals(getXPathsToUpdate().get(4))) {
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
            } else if (xpath.equals(getXPathsToUpdate().get(5))) {
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
            } else if (xpath.equals(getXPathsToUpdate().get(6))) {
                //<explorertype name="xmlredirect" key="fileicon.xmlredirect" icon="xmlredirect.png" reference="xmlcontent">
                createExplorerType(
                    document,
                    xpath,
                    "xmlredirect",
                    "fileicon.xmlredirect",
                    "xmlredirect.png",
                    "xmlcontent");
                //<newresource page="ade" uri="newresource_xmlcontent.jsp?newresourcetype=xmlredirect" order="11" autosetnavigation="true" autosettitle="true" info="desc.xmlredirect"/>
                createEtNewResource(
                    document,
                    xpath,
                    "newresource.jsp?newresourcetype=xmlredirect",
                    11,
                    true,
                    true,
                    "desc.xmlredirect",
                    "ade");

                // <accessentry principal="ROLE.WORKPLACE_USER" permissions="+r+v+w+c"/>
                setAccessEntry(
                    document,
                    xpath + "/" + CmsWorkplaceConfiguration.N_ACCESSCONTROL,
                    "ROLE.WORKPLACE_USER",
                    "+r+v+w+c");

            } else if (xpath.equals(getXPathsToUpdate().get(7))) {
                //<explorertype name="sitemap_config" key="fileicon.sitemap_config" icon="containerpage_config.png" reference="xmlcontent">
                createExplorerType(
                    document,
                    xpath,
                    "sitemap_config",
                    "fileicon.sitemap_config",
                    "containerpage_config.png",
                    "xmlcontent");
                // <newresource page="ade" uri="newresource_xmlcontent.jsp?newresourcetype=containerpage_config" order="20" autosetnavigation="false" autosettitle="false" info="desc.containerpage_config"/>
                createEtNewResource(
                    document,
                    xpath,
                    "newresource_xmlcontent.jsp?newresourcetype=sitemap_config",
                    20,
                    false,
                    false,
                    "desc.sitemap_config",
                    "ade");
                // <accessentry principal="ROLE.WORKPLACE_USER" permissions="+r+v+w+c"/>
                setAccessEntry(
                    document,
                    xpath + "/" + CmsWorkplaceConfiguration.N_ACCESSCONTROL,
                    "ROLE.WORKPLACE_USER",
                    "+r+v+w+c");

            } else if (xpath.equals(getXPathsToUpdate().get(8))) {
                createExplorerType(document, xpath, "entrypoint", "fileicon.entrypoint", "gallery.gif", "folder");
                createEtNewResource(
                    document,
                    xpath,
                    "newresource.jsp?newresourcetype=entrypoint",
                    11,
                    false,
                    false,
                    "desc.entrypoint",
                    "extendedfolder");
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
            /*0*/m_xpaths.add(CmsStringUtil.substitute(xp.toString(), "${etype}", "ade"));
            /*1*/m_xpaths.add(CmsStringUtil.substitute(
                xp.toString(),
                "${etype}",
                CmsResourceTypeXmlContainerPage.getStaticTypeName()));
            /*2*/m_xpaths.add(CmsStringUtil.substitute(
                xp.toString(),
                "${etype}",
                CmsResourceTypeXmlContainerPage.CONFIGURATION_TYPE_NAME));
            /*3*/m_xpaths.add(CmsStringUtil.substitute(
                xp.toString(),
                "${etype}",
                CmsResourceTypeXmlContainerPage.GROUP_CONTAINER_TYPE_NAME));
            /*4*/m_xpaths.add(CmsStringUtil.substitute(xp.toString(), "${etype}", "cntpagegallery"));
            /*5*/m_xpaths.add(CmsStringUtil.substitute(
                xp.toString(),
                "${etype}",
                CmsResourceTypeJsp.getContainerPageTemplateTypeName()));

            /*6*/m_xpaths.add(CmsStringUtil.substitute(xp.toString(), "${etype}", "xmlredirect"));
            /*7*/m_xpaths.add(CmsStringUtil.substitute(xp.toString(), "${etype}", "sitemap_config"));
            /*8*/m_xpaths.add(CmsStringUtil.substitute(xp.toString(), "${etype}", "entrypoint"));

        }
        return m_xpaths;
    }
}
