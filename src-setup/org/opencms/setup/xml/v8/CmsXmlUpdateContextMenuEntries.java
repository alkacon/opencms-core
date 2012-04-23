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
import org.opencms.setup.xml.CmsSetupXmlHelper;
import org.opencms.setup.xml.CmsXmlUpdateAction;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;

/**
 * XML updater class for adding context menu rules specific to ADE.<p>
 * 
 * @since 8.0.0
 */
public class CmsXmlUpdateContextMenuEntries extends A_CmsXmlWorkplace {

    /**
     * The map of update actions to be executed.<p>
     */
    private Map<String, CmsXmlUpdateAction> m_updateActions;

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Update some context menu entries";
    }

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#validate(org.opencms.setup.CmsSetupBean)
     */
    @Override
    public boolean validate(CmsSetupBean setupBean) throws Exception {

        return CmsStringUtil.isNotEmptyOrWhitespaceOnly(getCodeToChange(setupBean));
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        CmsXmlUpdateAction action = m_updateActions.get(xpath);
        if (action != null) {
            return m_updateActions.get(xpath).executeUpdate(document, xpath, forReal);
        }
        return false;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getCommonPath()
     */
    @Override
    protected String getCommonPath() {

        return xpathForExplorerTypes();
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        if (m_updateActions == null) {
            m_updateActions = new HashMap<String, CmsXmlUpdateAction>();
            final String pointerAdvanced = xpathForType("pointer")
                + "/"
                + CmsWorkplaceConfiguration.N_EDITOPTIONS
                + "/"
                + CmsWorkplaceConfiguration.N_CONTEXTMENU
                + "/"
                + CmsWorkplaceConfiguration.N_ENTRY
                + xpathAttr(I_CmsXmlConfiguration.A_KEY, "GUI_EXPLORER_CONTEXT_ADVANCED_0");

            CmsXmlUpdateAction updatePointerTouch = new CmsXmlUpdateAction() {

                @SuppressWarnings("unchecked")
                @Override
                public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

                    String touchPath = xpath
                        + "/"
                        + CmsWorkplaceConfiguration.N_ENTRY
                        + xpathAttr(I_CmsXmlConfiguration.A_KEY, "GUI_EXPLORER_CONTEXT_TOUCH_0");
                    if (doc.selectSingleNode(touchPath) != null) {
                        return false;
                    }
                    org.dom4j.Element parent = (org.dom4j.Element)(doc.selectSingleNode(xpath));
                    try {
                        parent.elements().add(
                            0,
                            createElementFromXml("<entry key=\"GUI_EXPLORER_CONTEXT_TOUCH_0\" uri=\"commons/touch.jsp\" rule=\"standard\"/>"));
                        parent.elements().add(1, createElementFromXml("<separator/>"));
                    } catch (DocumentException de) {
                        System.out.println("Failed to update context menu entry for type pointer!");
                        return false;
                    }
                    return true;
                }
            };
            m_updateActions.put(pointerAdvanced, updatePointerTouch);

            final String xmlcontentContext = xpathForType("xmlcontent")
                + "/"
                + CmsWorkplaceConfiguration.N_EDITOPTIONS
                + "/"
                + CmsWorkplaceConfiguration.N_CONTEXTMENU;
            m_updateActions.put(xmlcontentContext, new CmsXmlUpdateAction() {

                @Override
                public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

                    // ------------------------- AVAILABILITY ----------------------------------------
                    String availabilityEntry = xpath
                        + "/"
                        + CmsWorkplaceConfiguration.N_ENTRY
                        + xpathAttr(I_CmsXmlConfiguration.A_KEY, "GUI_EXPLORER_CONTEXT_AVAILABILITY_0");
                    CmsSetupXmlHelper.setValue(
                        doc,
                        availabilityEntry + "/@" + I_CmsXmlConfiguration.A_NAME,
                        "org.opencms.gwt.client.ui.contextmenu.CmsAvailabilityDialog");
                    CmsSetupXmlHelper.setValue(
                        doc,
                        availabilityEntry + "/@" + CmsWorkplaceConfiguration.A_RULE,
                        "containerpage");

                    //--------------------------- WORKPLACE ----------------------------------------------
                    String workplaceEntry = xpath
                        + "/"
                        + CmsWorkplaceConfiguration.N_ENTRY
                        + xpathAttr(I_CmsXmlConfiguration.A_KEY, "GUI_EXPLORER_CONTEXT_SHOW_WORKPLACE_0");
                    CmsSetupXmlHelper.setValue(
                        doc,
                        workplaceEntry + "/@" + I_CmsXmlConfiguration.A_NAME,
                        "org.opencms.gwt.client.ui.contextmenu.CmsShowWorkplace"

                    );
                    CmsSetupXmlHelper.setValue(
                        doc,
                        workplaceEntry + "/@" + CmsWorkplaceConfiguration.A_RULE,
                        "containerpage");

                    //----------------------------- LOGOUT -----------------------------------------------
                    String logoutEntry = xpath
                        + "/"
                        + CmsWorkplaceConfiguration.N_ENTRY
                        + xpathAttr(I_CmsXmlConfiguration.A_KEY, "GUI_EXPLORER_CONTEXT_LOGOUT_0");
                    CmsSetupXmlHelper.setValue(
                        doc,
                        logoutEntry + "/@" + I_CmsXmlConfiguration.A_NAME,
                        "org.opencms.gwt.client.ui.contextmenu.CmsLogout"

                    );
                    CmsSetupXmlHelper.setValue(
                        doc,
                        logoutEntry + "/@" + CmsWorkplaceConfiguration.A_RULE,
                        "containerpage");

                    // ------------------------------- PROPERTIES ---------------------------------------
                    String propertyEntry = xpath
                        + "/"
                        + CmsWorkplaceConfiguration.N_ENTRY
                        + xpathAttr(I_CmsXmlConfiguration.A_KEY, "GUI_EXPLORER_CONTEXT_ADVANCED_PROPERTIES_0");
                    CmsSetupXmlHelper.setValue(
                        doc,
                        propertyEntry + "/@" + I_CmsXmlConfiguration.A_NAME,
                        "org.opencms.gwt.client.ui.contextmenu.CmsEditProperties");
                    CmsSetupXmlHelper.setValue(
                        doc,
                        propertyEntry + "/@" + CmsWorkplaceConfiguration.A_RULE,
                        "containerpage");

                    return true;

                }
            });

        }
        return new ArrayList<String>(m_updateActions.keySet());

    }

    /**
     * Helper method for generating an xpath fragment "[@attr='value']".<p>
     * 
     * @param attr the attribute name 
     * @param value the attribute value 
     * @return the xpath fragment 
     */
    protected String xpathAttr(String attr, String value) {

        return "[@" + attr + "='" + value + "']";
    }

    /**
     * Returns the xpath for a specific explorer type.<p>
     * 
     * @param explorerType the explorer type 
     * 
     * @return the xpath for that explorer type 
     */
    protected String xpathForType(String explorerType) {

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

    /**
     * Returns the xpath for the explorertypes node.<p>
     * 
     * @return the xpath for the explorertypes node 
     */
    private String xpathForExplorerTypes() {

        return "/"
            + CmsConfigurationManager.N_ROOT
            + "/"
            + CmsWorkplaceConfiguration.N_WORKPLACE
            + "/"
            + CmsWorkplaceConfiguration.N_EXPLORERTYPES;
    }

}
