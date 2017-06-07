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
import org.opencms.setup.xml.CmsSetupXmlHelper;
import org.opencms.setup.xml.CmsXmlUpdateAction;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.menu.CmsMirAlwaysInvisible;
import org.opencms.workplace.explorer.menu.CmsMirContainerpageInvisible;
import org.opencms.workplace.explorer.menu.CmsMirEditProviderActive;
import org.opencms.workplace.explorer.menu.CmsMirNonContainerpageInvisible;
import org.opencms.workplace.explorer.menu.CmsMirRequireEditorRole;
import org.opencms.workplace.explorer.menu.CmsMirRequireWorkplaceUserRole;
import org.opencms.workplace.explorer.menu.CmsMirSitemapActive;
import org.opencms.workplace.explorer.menu.CmsMirSitemapInvisible;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

/**
 * XML updater class for adding context menu rules specific to ADE.<p>
 *
 * @since 8.0.0
 */
public class CmsXmlUpdateMenuRules extends A_CmsXmlWorkplace {

    /**
     * Helper class for updating a single context menu entry.<p>
     */
    private class UpdateInsertContainerpageRule extends CmsXmlUpdateAction {

        /** The class after which the special menu item rules should be inserted. */
        private String m_insertAfter;

        /**
         * Creates a new instance.<p>
         *
         * @param insertAfter the class of the menu item rule after which the new menu item rules should be inserted
         */
        public UpdateInsertContainerpageRule(String insertAfter) {

            m_insertAfter = insertAfter;
        }

        /**
         * @see org.opencms.setup.xml.CmsXmlUpdateAction#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
         */
        @Override
        public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

            Element elem = (Element)doc.selectSingleNode(xpath);
            if ((elem != null)
                && (elem.selectSingleNode(
                    CmsWorkplaceConfiguration.N_MENUITEMRULE
                        + "[@"
                        + I_CmsXmlConfiguration.A_CLASS
                        + "='"
                        + CmsMirSitemapInvisible.class.getName()
                        + "']") == null)) {
                updateMenuRule(elem, m_insertAfter);
                return true;
            }
            return false;
        }
    }

    /**
     * The map of update actions to be executed.<p>
     */
    private Map<String, CmsXmlUpdateAction> m_updateActions;

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Update the menu rules for various explorer types";
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

            String[][] names = {
                {"standard", "org.opencms.workplace.explorer.menu.CmsMirPrOtherInvisible"},
                {"copy", "org.opencms.workplace.explorer.menu.CmsMirPrOtherInvisible"},
                {"copytoproject", "org.opencms.workplace.explorer.menu.CmsMirPrOnlineInvisible"},
                {"undelete", "org.opencms.workplace.explorer.menu.CmsMirPrOtherInvisible"},
                {"undochanges", "org.opencms.workplace.explorer.menu.CmsMirPrOtherInvisible"},
                {"permissions", null},
                {"directpublish", "org.opencms.workplace.explorer.menu.CmsMirPrOtherInvisible"},
                {"lock", "org.opencms.workplace.explorer.menu.CmsMirPrOtherInvisible"},
                {"unlock", "org.opencms.workplace.explorer.menu.CmsMirPrOtherInvisible"},
                {"changelock", "org.opencms.workplace.explorer.menu.CmsMirPrOtherInvisible"},
                {"showlocks", "org.opencms.workplace.explorer.menu.CmsMirPrOtherInvisible"},
                {"editcontrolcode", null},
                {"showsiblings", null},
                {"nondeleted", null},
                {"multistandard", null},
                {"multipubstandard", null},
                {"publishscheduled", "org.opencms.workplace.explorer.menu.CmsMirPrOtherInvisible"}};

            for (String[] nameAndClass : names) {
                m_updateActions.put(
                    xpathForMenuRule(nameAndClass[0]),
                    new UpdateInsertContainerpageRule(nameAndClass[1]));
            }
            m_updateActions.put(xpathForMenuRule("containerpage"), new CmsXmlUpdateAction() {

                @Override
                public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

                    Element elem = (Element)doc.selectSingleNode(xpath);
                    if (elem == null) {
                        CmsSetupXmlHelper.setValue(
                            doc,
                            xpathForMenuItemRule(
                                "containerpage",
                                "org.opencms.workplace.explorer.menu.CmsMirContainerPageActive"),
                            "");
                        CmsSetupXmlHelper.setValue(doc, xpathForMenuItemRule(
                            "containerpage",
                            "org.opencms.workplace.explorer.menu.CmsMirAlwaysInvisible"), "");
                    }
                    return false;
                }

            });

            m_updateActions.put(xpathForMenuRule("adecheckfile"), new CmsXmlUpdateAction() {

                @Override
                public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

                    Element elem = (Element)doc.selectSingleNode(xpath);
                    if (elem == null) {
                        CmsSetupXmlHelper.setValue(
                            doc,
                            xpathForMenuItemRule(
                                "adecheckfile",
                                "org.opencms.workplace.explorer.menu.CmsMirContainerPageActiveAndFileAvailable"),
                            "");
                        CmsSetupXmlHelper.setValue(doc, xpathForMenuItemRule(
                            "adecheckfile",
                            "org.opencms.workplace.explorer.menu.CmsMirAlwaysInvisible"), "");
                        return true;
                    }
                    return false;
                }

            });

            m_updateActions.put(xpathForMenuRule("ade-undochanges"), new CmsXmlUpdateAction() {

                @Override
                public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

                    Element elem = (Element)doc.selectSingleNode(xpath);
                    if (elem == null) {
                        String[] classes = {
                            "org.opencms.workplace.explorer.menu.CmsMirNonContainerpageInvisible",
                            "org.opencms.workplace.explorer.menu.CmsMirPrOnlineInvisible",
                            "org.opencms.workplace.explorer.menu.CmsMirPrOtherInvisible",
                            "org.opencms.workplace.explorer.menu.CmsMirOtherSiteInactive",
                            "org.opencms.workplace.explorer.menu.CmsMirPrSameUnlockedInactiveNotDeletedNoAl",
                            "org.opencms.workplace.explorer.menu.CmsMirPrSameLockedActiveChangedAl",
                            "org.opencms.workplace.explorer.menu.CmsMirPrSameOtherlockInvisible"};

                        for (String className : classes) {
                            CmsSetupXmlHelper.setValue(doc, xpathForMenuItemRule("ade-undochanges", className), "");
                        }
                        return true;
                    }
                    return false;

                }
            });

            m_updateActions.put(xpathForMenuRule("containerpage-deleted"), new CmsXmlUpdateAction() {

                @Override
                public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

                    Element elem = (Element)doc.selectSingleNode(xpath);
                    if (elem == null) {
                        String[] classes = {
                            "org.opencms.workplace.explorer.menu.CmsMirInvisibleIfNotDeleted",
                            "org.opencms.workplace.explorer.menu.CmsMirNonContainerpageInvisible",
                            "org.opencms.workplace.explorer.menu.CmsMirContainerPageActive"};
                        for (String className : classes) {
                            CmsSetupXmlHelper.setValue(
                                doc,
                                xpathForMenuItemRule("containerpage-deleted", className),
                                "");
                        }
                        return true;
                    }
                    return false;

                }
            });

            m_updateActions.put(xpathForMenuRule("containerpage-no-different-site"), new CmsXmlUpdateAction() {

                @Override
                public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

                    Element elem = (Element)doc.selectSingleNode(xpath);
                    if (elem == null) {
                        String[] classes = {
                            "org.opencms.workplace.explorer.menu.CmsMirNonContainerpageInvisible",
                            "org.opencms.workplace.explorer.menu.CmsMirOtherSiteInactive",
                            "org.opencms.workplace.explorer.menu.CmsMirContainerPageActive"};
                        for (String className : classes) {
                            CmsSetupXmlHelper.setValue(
                                doc,
                                xpathForMenuItemRule("containerpage-no-different-site", className),
                                "");
                        }
                        return true;
                    }
                    return false;

                }
            });

            m_updateActions.put(xpathForMenuRule("editprovider"), new CmsXmlUpdateAction() {

                @Override
                public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

                    Element elem = (Element)doc.selectSingleNode(xpath);
                    String[] classNames = new String[] {
                        CmsMirEditProviderActive.class.getName(),
                        CmsMirAlwaysInvisible.class.getName()};
                    if (elem == null) {
                        for (String classname : classNames) {
                            CmsSetupXmlHelper.setValue(doc, xpathForMenuItemRule("editprovider", classname), "");
                        }
                        return true;
                    }
                    return false;

                }
            });

            m_updateActions.put(xpathForMenuRule("sitemap"), new CmsXmlUpdateAction() {

                @Override
                public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

                    Element elem = (Element)doc.selectSingleNode(xpath);
                    String[] classNames = new String[] {
                        CmsMirSitemapActive.class.getName(),
                        CmsMirAlwaysInvisible.class.getName()};

                    if (elem == null) {
                        for (String classname : classNames) {
                            CmsSetupXmlHelper.setValue(doc, xpathForMenuItemRule("sitemap", classname), "");
                        }
                        return true;
                    }
                    return false;
                }

            });

            m_updateActions.put(xpathForMenuRule("sitemap-wpuser"), new CmsXmlUpdateAction() {

                @Override
                public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

                    Element elem = (Element)doc.selectSingleNode(xpath);
                    String[] classNames = new String[] {
                        CmsMirRequireWorkplaceUserRole.class.getName(),
                        CmsMirSitemapActive.class.getName(),
                        CmsMirAlwaysInvisible.class.getName()};

                    if (elem == null) {
                        for (String classname : classNames) {
                            CmsSetupXmlHelper.setValue(doc, xpathForMenuItemRule("sitemap-wpuser", classname), "");
                        }
                        return true;
                    }
                    return false;
                }

            });

            m_updateActions.put(
                xpathForMenuRule("containerpage-no-different-site-and-has-editor-role"),
                new CmsXmlUpdateAction() {

                    @Override
                    public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

                        Element elem = (Element)doc.selectSingleNode(xpath);
                        String[] classNames = new String[] {
                            CmsMirRequireEditorRole.class.getName(),
                            CmsMirNonContainerpageInvisible.class.getName(),
                            org.opencms.workplace.explorer.menu.CmsMirOtherSiteInactive.class.getName(),
                            org.opencms.workplace.explorer.menu.CmsMirPrSameUnlockedInactiveNoAl.class.getName(),
                            org.opencms.workplace.explorer.menu.CmsMirPrSameLockedActiveNotDeletedAlPermW.class.getName(),
                            org.opencms.workplace.explorer.menu.CmsMirPrSameOtherlockInvisible.class.getName(),
                            org.opencms.workplace.explorer.menu.CmsMirContainerPageActive.class.getName()};

                        if (elem == null) {
                            for (String classname : classNames) {
                                CmsSetupXmlHelper.setValue(
                                    doc,
                                    xpathForMenuItemRule(
                                        "containerpage-no-different-site-and-has-editor-role",
                                        classname),
                                    "");
                            }
                            return true;
                        }
                        return false;
                    }

                });

            m_updateActions.put(xpathForMenuRule("containerpage-wpuser"), new CmsXmlUpdateAction() {

                @Override
                public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

                    Element elem = (Element)doc.selectSingleNode(xpath);
                    String[] classNames = new String[] {
                        CmsMirRequireWorkplaceUserRole.class.getName(),
                        CmsMirNonContainerpageInvisible.class.getName(),
                        org.opencms.workplace.explorer.menu.CmsMirContainerPageActive.class.getName()};

                    if (elem != null) {
                        // remove the already existing rule insert an updated list of item-rules
                        elem.getParent().remove(elem);
                    }
                    for (String classname : classNames) {
                        CmsSetupXmlHelper.setValue(doc, xpathForMenuItemRule("containerpage-wpuser", classname), "");
                    }
                    return true;
                }

            });

            m_updateActions.put(xpathForMenuRule("containerpage-basic"), new CmsXmlUpdateAction() {

                @Override
                public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

                    Element elem = (Element)doc.selectSingleNode(xpath);
                    String[] classNames = new String[] {
                        org.opencms.workplace.explorer.menu.CmsMirNonContainerpageInvisible.class.getName(),
                        org.opencms.workplace.explorer.menu.CmsMirContainerPageActive.class.getName()};

                    if (elem == null) {
                        for (String classname : classNames) {
                            CmsSetupXmlHelper.setValue(doc, xpathForMenuItemRule("containerpage-basic", classname), "");
                        }
                        return true;
                    }
                    return false;
                }

            });

        }
        return new ArrayList<String>(m_updateActions.keySet());

    }

    /**
     * Updates a single context menu entry's rules.<p>
     *
     * @param elem the parent element
     *
     * @param className the class name
     */
    protected void updateMenuRule(Element elem, String className) {

        if (elem == null) {
            return;
        }
        int insertIndex = 0;
        if (className != null) {
            Element insertAfter = (Element)elem.selectSingleNode("menuitemrule[@class='" + className + "']");
            if (insertAfter != null) {
                insertIndex = elem.indexOf(insertAfter) + 1;
            }
        }
        elem.elements().add(insertIndex, createMenuItemRule(CmsMirSitemapInvisible.class.getName()));
        elem.elements().add(insertIndex, createMenuItemRule(CmsMirContainerpageInvisible.class.getName()));
    }

    /**
     * Returns the xpath for a specific menu item rule.<p>
     *
     * @param name the name of the menu rule
     *
     * @param className the class of the menu item rule
     *
     * @return the xpath for that menu item rule
     */
    protected String xpathForMenuItemRule(String name, String className) {

        return xpathForMenuRule(name)
            + "/"
            + CmsWorkplaceConfiguration.N_MENUITEMRULE
            + "[@"
            + I_CmsXmlConfiguration.A_CLASS
            + "='"
            + className
            + "']";
    }

    /**
     * Returns the xpath for a specific menu rule.<p>
     *
     * @param name the menu rule
     *
     * @return the xpath for that menu rule
     */
    protected String xpathForMenuRule(String name) {

        return xpathForExplorerTypes()
            + "/"
            + CmsWorkplaceConfiguration.N_MENURULES
            + "/"
            + CmsWorkplaceConfiguration.N_MENURULE
            + "[@"
            + I_CmsXmlConfiguration.A_NAME
            + "='"
            + name
            + "']";
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
     * Creates a menu item rule element.<p>
     *
     * @param cls the class for the menu item rule
     *
     * @return the menu item rule element
     */
    private Element createMenuItemRule(String cls) {

        Element mirElem = DocumentFactory.getInstance().createElement(CmsWorkplaceConfiguration.N_MENUITEMRULE);
        mirElem.addAttribute(I_CmsXmlConfiguration.A_CLASS, cls);
        return mirElem;
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
