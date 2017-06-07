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
 * XML updater class for adding context menu rules specific to ADE.<p>
 *
 * @since 8.0.0
 */
public class CmsXmlUpdateContextMenuEntries extends A_CmsXmlWorkplace {

    /**
     * Update action for adding GWT context menu entries.<p>
     */
    static class GwtContextMenuUpdateAction extends CmsXmlUpdateAction {

        /** The bean containing the information about which context menu entries to add. */
        private GwtEntryList m_entryList;

        /**
         * Creates a new update action instance.<p>
         *
         * @param entryList the list of entries to update
         */
        public GwtContextMenuUpdateAction(GwtEntryList entryList) {

            m_entryList = entryList;
        }

        /**
         * @see org.opencms.setup.xml.CmsXmlUpdateAction#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
         */
        @Override
        public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

            if (!forReal) {
                return true;
            } else {
                String typeName = m_entryList.getTypeName();
                Element typeElement = (Element)(doc.selectSingleNode(xpath));
                if (typeElement == null) {
                    System.err.println("Couldn't find explorer type " + typeName);
                    return false;
                }
                Element menuElement = (Element)(typeElement.selectSingleNode("editoptions/contextmenu"));
                if (menuElement == null) {
                    System.err.println("Couldn't find context menu entry for explorer type " + typeName);
                    return false;
                }
                for (GwtMenuEntry menuEntry : m_entryList.getEntries()) {
                    Element entryElement = (Element)(menuElement.selectSingleNode(
                        "entry[@name='" + menuEntry.getName() + "']"));
                    if (entryElement == null) {
                        menuElement.addElement("entry").addAttribute("key", menuEntry.getKey()).addAttribute(
                            "name",
                            menuEntry.getName()).addAttribute("rule", menuEntry.getRule());
                    } else {
                        entryElement.attribute("rule").setValue(menuEntry.getRule());
                    }
                    String oldName = menuEntry.getName().replace(
                        "org.opencms.gwt.client.ui.contextmenu",
                        "org.opencms.gwt.client.ui");
                    Element oldEntryElement = (Element)(menuElement.selectSingleNode("entry[@name='" + oldName + "']"));
                    if (oldEntryElement != null) {
                        oldEntryElement.detach();
                    }
                }
                return true;
            }

        }

        /**
         * Gets the xpath for the update action.<p>
         *
         * @return the xpath for the update action
         */
        public String getXpath() {

            String keyName = "gwtcontextmenuupdate_" + m_entryList.getTypeName();
            // use a dummy xpath condition to make the xpaths unique
            return "/opencms/workplace/explorertypes/explorertype[@name='"
                + m_entryList.getTypeName()
                + "']['"
                + keyName
                + "'='"
                + keyName
                + "']";
        }

    }

    /**
     * We use this enum to define the GWT context menu entries.<p>
     */
    enum GwtMenuEntry {
        /** Availability. */
        AVAILABILITY("GUI_EXPLORER_CONTEXT_AVAILABILITY_0",
        "org.opencms.gwt.client.ui.contextmenu.CmsAvailabilityDialog", "containerpage-no-different-site"),

        /** Categories. */
        CATEGORIES("GUI_EXPLORER_CONTEXT_CATEGORIES_0", "org.opencms.gwt.client.ui.contextmenu.CmsCategories",
        "containerpage-no-different-site"),

        /** Delete. */
        DELETE("GUI_EXPLORER_CONTEXT_DELETE_0", "org.opencms.gwt.client.ui.contextmenu.CmsDeleteResource",
        "containerpage-no-different-site"),

        /** Resource info. */
        INFO("GUI_EXPLORER_CONTEXT_RESOURCE_INFO_0", "org.opencms.gwt.client.ui.contextmenu.CmsResourceInfo",
        "containerpage"),

        /** Lock report. */
        LOCKREPORT("GUI_EXPLORER_CONTEXT_LOCK_REPORT_0", "org.opencms.gwt.client.ui.contextmenu.CmsLockReport",
        "containerpage"),

        /** Logout. */
        LOGOUT("GUI_EXPLORER_CONTEXT_LOGOUT_0", "org.opencms.gwt.client.ui.contextmenu.CmsLogout", "editprovider"),

        /** Properties. */
        PROPERTIES("GUI_EXPLORER_CONTEXT_ADVANCED_PROPERTIES_0",
        "org.opencms.gwt.client.ui.contextmenu.CmsEditProperties", "containerpage-no-different-site"), /** Rename. */
        RENAME("GUI_EXPLORER_CONTEXT_RENAME_0", "org.opencms.gwt.client.ui.contextmenu.CmsRename",
        "containerpage-no-different-site"),

        /** Replace. */
        REPLACE("GUI_EXPLORER_CONTEXT_REPLACE_0", "org.opencms.gwt.client.ui.contextmenu.CmsReplace",
        "containerpage-no-different-site"),

        /** Show workplace. */
        SHOWWORKPLACE("GUI_EXPLORER_CONTEXT_SHOW_WORKPLACE_0", "org.opencms.gwt.client.ui.contextmenu.CmsShowWorkplace",
        "editprovider"),

        /** Undo changes. */
        UNDOCHANGES("GUI_EXPLORER_CONTEXT_UNDOCHANGES_0", "org.opencms.gwt.client.ui.contextmenu.CmsRestore",
        "ade-undochanges");

        /** The message key. */
        private String m_key;

        /** The class name. */
        private String m_name;

        /** The rule to use. */
        private String m_rule;

        /** Enum constructor.
         *
         * @param key the message key
         * @param name the class name
         * @param rule the menu rule
         */
        private GwtMenuEntry(String key, String name, String rule) {

            m_key = key;
            m_name = name;
            m_rule = rule;
        }

        /**
         * Gets the message key.
         *
         * @return the message key
         */
        public String getKey() {

            return m_key;
        }

        /**
         * Gets the class name.
         *
         * @return the class name
         */
        public String getName() {

            return m_name;
        }

        /**
         * Gets the menu rule.
         *
         * @return the menu rule
         */
        public String getRule() {

            return m_rule;
        }
    }

    /**
     * Data holder class for the menu GWT menu entries defined for a specific explorer type.
     */
    private static class GwtEntryList {

        /** List of menu entries to up. */
        private GwtMenuEntry[] m_entries;

        /** The class name. */
        private String m_typeName;

        /**
         * Creates a new entry list.<p>
         *
         * @param typeName the explorer type name
         * @param entries the entries to add
         */
        public GwtEntryList(String typeName, GwtMenuEntry... entries) {

            m_typeName = typeName;
            m_entries = entries;
        }

        /**
         * Gets the entries.
         *
         * @return the entries
         */
        public GwtMenuEntry[] getEntries() {

            return m_entries;
        }

        /**
         * Gets the explorer type name.
         *
         * @return the explorer type name
         */
        public String getTypeName() {

            return m_typeName;
        }
    }

    /**
     * Data for updating the GWT context menu entries.
     */
    private static GwtEntryList[] entryLists = {
        new GwtEntryList(
            "plain",
            GwtMenuEntry.LOCKREPORT,
            GwtMenuEntry.RENAME,
            GwtMenuEntry.DELETE,
            GwtMenuEntry.REPLACE,
            GwtMenuEntry.AVAILABILITY,
            GwtMenuEntry.PROPERTIES,
            GwtMenuEntry.CATEGORIES),
        new GwtEntryList(
            "binary",
            GwtMenuEntry.LOCKREPORT,
            GwtMenuEntry.RENAME,
            GwtMenuEntry.DELETE,
            GwtMenuEntry.REPLACE,
            GwtMenuEntry.AVAILABILITY,
            GwtMenuEntry.PROPERTIES,
            GwtMenuEntry.INFO,
            GwtMenuEntry.CATEGORIES),
        new GwtEntryList(
            "image",
            GwtMenuEntry.LOCKREPORT,
            GwtMenuEntry.RENAME,
            GwtMenuEntry.DELETE,
            GwtMenuEntry.REPLACE,
            GwtMenuEntry.AVAILABILITY,
            GwtMenuEntry.PROPERTIES,
            GwtMenuEntry.INFO,
            GwtMenuEntry.CATEGORIES),
        new GwtEntryList(
            "xmlcontent",
            GwtMenuEntry.LOCKREPORT,
            GwtMenuEntry.DELETE,
            GwtMenuEntry.UNDOCHANGES,
            GwtMenuEntry.AVAILABILITY,
            GwtMenuEntry.PROPERTIES,
            GwtMenuEntry.INFO,
            GwtMenuEntry.CATEGORIES,
            GwtMenuEntry.SHOWWORKPLACE,
            GwtMenuEntry.LOGOUT)};

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
                            createElementFromXml(
                                "<entry key=\"GUI_EXPLORER_CONTEXT_TOUCH_0\" uri=\"commons/touch.jsp\" rule=\"standard\"/>"));
                        parent.elements().add(1, createElementFromXml("<separator/>"));
                    } catch (DocumentException de) {
                        System.out.println("Failed to update context menu entry for type pointer!");
                        return false;
                    }
                    return true;
                }
            };
            m_updateActions.put(pointerAdvanced, updatePointerTouch);
            for (GwtEntryList entryList : entryLists) {
                GwtContextMenuUpdateAction action = new GwtContextMenuUpdateAction(entryList);
                String xpath = action.getXpath();
                m_updateActions.put(xpath, action);
            }
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
