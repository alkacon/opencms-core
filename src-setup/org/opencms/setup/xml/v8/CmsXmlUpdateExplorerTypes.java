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
 * For further information about Alkacon Software, please see the
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
import org.opencms.setup.CmsSetupBean;
import org.opencms.setup.xml.A_CmsXmlWorkplace;
import org.opencms.setup.xml.CmsSetValueAction;
import org.opencms.setup.xml.CmsXmlUpdateAction;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

/**
 * XML updater for adding icon rules to opencms-workplace.xml.<p>
 *
 * @since 8.0.0
 */
public class CmsXmlUpdateExplorerTypes extends A_CmsXmlWorkplace {

    /** The map of update actions. */
    private Map<String, CmsXmlUpdateAction> m_actions;

    /** The icon data to update.*/
    private String[][] m_iconValues = {
        {"extendedfolder", "icon", "menu.png"},
        {"structurecontent", "icon", "menu.png"},
        {"otheroptions", "icon", "menu.png"},
        {"folder", "bigicon", "folder_big.png"},
        {"plain", "bigicon", "plain_big.png"},
        {"jsp", "bigicon", "jsp_big.png"},
        {"binary", "bigicon", "binary_big.png"},
        {"pointer", "bigicon", "pointer_big.png"},
        {"imagegallery", "bigicon", "imagegallery_big.png"},
        {"downloadgallery", "bigicon", "downloadgallery_big.png"},
        {"linkgallery", "bigicon", "linkgallery_big.png"},
        {"xmlcontent", "bigicon", "xmlcontent_big.png"}};

    /** The order data to update. */
    private String[][] m_orders = {{"link", "90"}, {"extendedfolder", "5"}, {"structurecontent", "10"}};

    /** The page data to update. */
    private String[][] m_pages = {{"link", "otheroptions"}};

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Adds new icons to explorer types and changes their order in the resource creation menu.";
    }

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#validate(org.opencms.setup.CmsSetupBean)
     */
    @Override
    public boolean validate(CmsSetupBean setupBean) throws Exception {

        return CmsStringUtil.isNotEmptyOrWhitespaceOnly(getCodeToChange(setupBean));
    }

    /**
     * Creates the 'otheroptions' page.<p>
     *
     * @param doc the XML document
     * @return true if the value was updated
     */
    protected boolean createOtherOptionsPage(Document doc) {

        String xpath = xpathForType("otheroptions");
        if (doc.selectSingleNode(xpath) != null) {
            return false;
        }
        createExplorerType(
            doc,
            "/opencms/workplace/explorertypes/explorertype[@name='otheroptions']",
            "otheroptions",
            "fileicon.otheroptions",
            "menu.png",
            "folder");
        createEtNewResource(
            doc,
            xpath,
            "newresource.jsp?page=otheroptions",
            19,
            false,
            false,
            "desc.otheroptions",
            null);
        return true;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        CmsXmlUpdateAction action = m_actions.get(xpath);
        if (action != null) {
            return action.executeUpdate(document, xpath, forReal);
        }
        return false;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getCommonPath()
     */
    @Override
    protected String getCommonPath() {

        return "/"
            + CmsConfigurationManager.N_ROOT
            + "/"
            + CmsWorkplaceConfiguration.N_WORKPLACE
            + "/"
            + CmsWorkplaceConfiguration.N_EXPLORERTYPES;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        if (m_actions == null) {
            m_actions = new HashMap<String, CmsXmlUpdateAction>();

            // icon updates
            for (int i = 0; i < m_iconValues.length; i++) {
                String xpath = xpathForType(m_iconValues[i][0]) + "/@" + m_iconValues[i][1];
                CmsXmlUpdateAction action = new CmsSetValueAction(m_iconValues[i][2]);
                m_actions.put(xpath, action);
            }
            m_actions.put(xpathForType("otheroptions"), new CmsXmlUpdateAction() {

                /**
                 * @see org.opencms.setup.xml.CmsXmlUpdateAction#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
                 */
                @Override
                public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

                    return createOtherOptionsPage(doc);
                }
            });

            m_actions.put(xpathForType("classicgallery"), new CmsXmlUpdateAction() {

                @SuppressWarnings("unchecked")
                @Override
                public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

                    Element typeElement = (Element)(doc.selectSingleNode(xpath));
                    if (typeElement != null) {
                        return false;
                    }
                    Element parent = (Element)(doc.selectSingleNode("/opencms/workplace/explorertypes"));

                    String xml = "      <explorertype name=\"classicgallery\" key=\"fileicon.downloadgallery\" icon=\"downloadgallery.gif\" bigicon=\"downloadgallery_big.png\">\n"
                        + "        <newresource page=\"none\"  uri=\"newresource.jsp?newresourcetype=downloadgallery\"  order=\"20\" />\n"
                        + "        <editoptions>\n"
                        + "          <defaultproperties enabled=\"true\" shownavigation=\"true\">\n"
                        + "            <defaultproperty name=\"Title\"/>\n"
                        + "          </defaultproperties>\n"
                        + "          <contextmenu>\n"
                        + "            <entry key=\"GUI_EXPLORER_CONTEXT_LOCK_0\" uri=\"commons/lock.jsp\" rule=\"lock\"/>\n"
                        + "            <entry key=\"GUI_EXPLORER_CONTEXT_OVERRIDELOCK_0\" uri=\"commons/lockchange.jsp\" rule=\"changelock\"/>\n"
                        + "            <entry key=\"GUI_EXPLORER_CONTEXT_UNLOCK_0\" uri=\"commons/unlock.jsp\" rule=\"unlock\"/>\n"
                        + "            <separator/>\n"
                        + "            <entry key=\"GUI_EXPLORER_CONTEXT_COPYTOPROJECT_0\" uri=\"commons/copytoproject.jsp\" rule=\"copytoproject\"/>\n"
                        + "            <entry key=\"GUI_EXPLORER_CONTEXT_PUBLISH_0\" uri=\"commons/publishresource.jsp\" rule=\"directpublish\"/>\n"
                        + "            <separator/>\n"
                        + "            <entry key=\"GUI_EXPLORER_CONTEXT_OPENGALLERY_0\" uri=\"commons/opengallery.jsp\" rule=\"nondeleted\"/>\n"
                        + "            <entry key=\"GUI_EXPLORER_CONTEXT_MULTIFILE_PROPERTY_0\" uri=\"commons/property_multifile.jsp\" rule=\"nondeleted\"/>     \n"
                        + "            <separator/>\n"
                        + "            <entry key=\"GUI_EXPLORER_CONTEXT_COPY_0\" uri=\"commons/copy.jsp\" rule=\"standard\"/>\n"
                        + "            <entry key=\"GUI_EXPLORER_CONTEXT_MOVE_0\" uri=\"commons/move.jsp\" rule=\"standard\"/>\n"
                        + "            <entry key=\"GUI_EXPLORER_CONTEXT_DELETE_0\" uri=\"commons/delete.jsp\" rule=\"standard\"/>\n"
                        + "            <entry key=\"GUI_EXPLORER_CONTEXT_UNDOCHANGES_0\" uri=\"commons/undochanges.jsp\" rule=\"undochanges\"/>\n"
                        + "            <entry key=\"GUI_EXPLORER_CONTEXT_UNDELETE_0\" uri=\"commons/undelete.jsp\" rule=\"undelete\"/>\n"
                        + "            <separator/>\n"
                        + "            <entry key=\"GUI_EXPLORER_CONTEXT_RELATIONS_0\" rule=\"substandard\">\n"
                        + "                <entry key=\"GUI_EXPLORER_CONTEXT_LINKRELATIONTO_0\" uri=\"views/admin/admin-main.jsp?root=explorer&amp;path=%2Flinkrelationtarget\" rule=\"standard\"/>\n"
                        + "                <entry key=\"GUI_EXPLORER_CONTEXT_LINKRELATIONFROM_0\" uri=\"views/admin/admin-main.jsp?root=explorer&amp;path=%2Flinkrelationsource\" rule=\"standard\"/>\n"
                        + "                <separator/>\n"
                        + "                <entry key=\"GUI_EXPLORER_CONTEXT_CATEGORIES_0\" uri=\"views/admin/admin-main.jsp?root=explorer&amp;path=%2Fcategories\" rule=\"standard\"/>\n"
                        + "            </entry>\n"
                        + "            <separator/>\n"
                        + "            <entry key=\"GUI_EXPLORER_CONTEXT_ACCESS_0\" uri=\"commons/chacc.jsp\" rule=\"permissions\"/>\n"
                        + "            <entry key=\"GUI_EXPLORER_CONTEXT_CHNAV_0\" uri=\"commons/chnav.jsp\" rule=\"standard\"/>\n"
                        + "            <separator/>\n"
                        + "            <entry key=\"GUI_EXPLORER_CONTEXT_ADVANCED_0\" rule=\"substandard\">\n"
                        + "                <entry key=\"GUI_EXPLORER_CONTEXT_TOUCH_0\" uri=\"commons/touch.jsp\" rule=\"standard\"/>\n"
                        + "                <entry key=\"GUI_EXPLORER_CONTEXT_AVAILABILITY_0\" uri=\"commons/availability.jsp\" rule=\"standard\"/>\n"
                        + "                <separator/>\n"
                        + "                <entry key=\"GUI_EXPLORER_CONTEXT_SECURE_0\" uri=\"commons/secure.jsp\" rule=\"standard\"/>\n"
                        + "                <entry key=\"GUI_EXPLORER_CONTEXT_TYPE_0\" uri=\"commons/chtype.jsp\" rule=\"standard\"/>\n"
                        + "                <separator/>\n"
                        + "                <entry key=\"GUI_EXPLORER_CONTEXT_SHOW_DELETED_0\" uri=\"commons/show_deleted.jsp\" rule=\"standard\"/>\n"
                        + "            </entry>\n"
                        + "            <separator/>\n"
                        + "            <entry key=\"GUI_EXPLORER_CONTEXT_HISTORY_0\" uri=\"views/admin/admin-main.jsp?root=explorer&amp;path=%2Fhistory\" rule=\"nondeleted\"/>\n"
                        + "            <entry key=\"GUI_EXPLORER_CONTEXT_PROPERTY_0\" uri=\"commons/property.jsp\" rule=\"nondeleted\"/>\n"
                        + "          </contextmenu>\n"
                        + "        </editoptions>\n"
                        + "      </explorertype>";
                    try {
                        Element elementToInsert = createElementFromXml(xml);
                        parent.elements().add(0, elementToInsert);
                        if (forReal) {
                            for (String referencingType : new String[] {"linkgallery"}) {
                                Element referencingTypeElement = (Element)(doc.selectSingleNode(
                                    xpathForType(referencingType)));
                                if (referencingTypeElement != null) {
                                    referencingTypeElement.attribute("reference").setValue("classicgallery");
                                }
                            }
                        }
                        return true;
                    } catch (DocumentException e) {
                        System.err.println("Couldn't create classicgallery explorer type!");
                        return false;
                    }
                }
            });

            // order updates
            for (int i = 0; i < m_orders.length; i++) {
                String type = m_orders[i][0];
                String order = m_orders[i][1];
                String orderPath = xpathForType(type)
                    + "/"
                    + CmsWorkplaceConfiguration.N_NEWRESOURCE
                    + "/@"
                    + I_CmsXmlConfiguration.A_ORDER;
                m_actions.put(orderPath, new CmsSetValueAction(order));
            }

            // page updates
            for (int i = 0; i < m_pages.length; i++) {
                String type = m_pages[i][0];
                String page = m_pages[i][1];
                String pagePath = xpathForType(type)
                    + "/"
                    + CmsWorkplaceConfiguration.N_NEWRESOURCE
                    + "/@"
                    + CmsWorkplaceConfiguration.A_PAGE;
                m_actions.put(pagePath, new CmsSetValueAction(page));
            }

        }
        return new ArrayList<String>(m_actions.keySet());
    }

    /**
     * Returns the xpath for a given explorer type.<p>
     *
     * @param explorerType the explorer type
     *
     * @return the xpath for that explorer type
     */
    String xpathForType(String explorerType) {

        return "/"
            + CmsConfigurationManager.N_ROOT
            + "/"
            + CmsWorkplaceConfiguration.N_WORKPLACE
            + "/"
            + CmsWorkplaceConfiguration.N_EXPLORERTYPES
            + "/"
            + CmsWorkplaceConfiguration.N_EXPLORERTYPE
            + "[@name='"
            + explorerType
            + "']";
    }

}
