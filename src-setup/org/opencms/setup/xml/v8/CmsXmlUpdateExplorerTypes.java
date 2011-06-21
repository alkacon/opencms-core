/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

/**
 * XML updater for adding icon rules to opencms-workplace.xml.<p>
 * 
 * @since 8.0.0
 */
public class CmsXmlUpdateExplorerTypes extends A_CmsXmlWorkplace {

    /** The icon data to update.*/
    private String[][] m_iconValues = {
        {"extendedfolder", "icon", "menu.png"},
        {"structurecontent", "icon", "menu.png"},
        {"otheroptions", "icon", "menu.png"},
        {"folder", "bigicon", "folder_big.png"},
        {"xmlpage", "bigicon", "page_big.png"},
        {"plain", "bigicon", "plain_big.png"},
        {"jsp", "bigicon", "jsp_big.png"},
        {"binary", "bigicon", "binary_big.png"},
        {"pointer", "bigicon", "pointer_big.png"},
        {"imagegallery", "bigicon", "imagegallery_big.png"},
        {"downloadgallery", "bigicon", "downloadgallery_big.png"},
        {"linkgallery", "bigicon", "linkgallery_big.png"},
        {"htmlgallery", "bigicon", "htmlgallery_big.png"},
        {"tablegallery", "bigicon", "tablegallery_big.png"},
        {"xmlcontent", "bigicon", "xmlcontent_big.png"}};

    /** The order data to update. */
    private String[][] m_orders = {
        {"xmlpage", "81"},
        {"link", "90"},
        {"extendedfolder", "5"},
        {"structurecontent", "10"}};

    /** The page data to update. */
    private String[][] m_pages = { {"xmlpage", "otheroptions"}, {"link", "otheroptions"}};

    /** The map of update actions. */
    private Map<String, CmsXmlUpdateAction> m_actions;

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
    private String xpathForType(String explorerType) {

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
