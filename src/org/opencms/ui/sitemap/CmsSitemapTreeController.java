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

package org.opencms.ui.sitemap;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.gwt.CmsCoreService;
import org.opencms.i18n.CmsLocaleGroupService;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockActionRecord.LockChange;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.Messages;
import org.opencms.ui.actions.CmsResourceInfoAction;
import org.opencms.ui.apps.CmsSitemapEditorConfiguration;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsResourceIcon;
import org.opencms.ui.components.CmsResourceIcon.IconMode;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Manages the sitemap tree in the 'locale comparison' view in the sitemap editor.<p>
 */
public class CmsSitemapTreeController {

    /**
     * The context used for child dialogs.<p>
     */
    public class DialogContext implements I_CmsDialogContext {

        /** The tree node. */
        private CmsSitemapTreeNode m_node;

        /** The resource. */
        private CmsResource m_resource;

        /**
         * Creates a new instance.<p>
         *
         * @param resource the resource
         * @param node the tree node
         */
        public DialogContext(CmsResource resource, CmsSitemapTreeNode node) {
            m_resource = resource;
            m_node = node;
        }

        /**
         * Closes the dialog window.<p>
         */
        public void closeWindow() {

            if (m_window != null) {
                m_window.close();
                m_window = null;
            }
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#error(java.lang.Throwable)
         */
        public void error(Throwable error) {

            getTreeControllerLog().error(error.getLocalizedMessage(), error);
            CmsErrorDialog.showErrorDialog(error);
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#finish(org.opencms.file.CmsProject, java.lang.String)
         */
        public void finish(CmsProject project, String siteRoot) {

            closeWindow();
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#finish(java.util.Collection)
         */
        @SuppressWarnings("synthetic-access")
        public void finish(Collection<CmsUUID> result) {

            closeWindow();
            if (result.isEmpty()) {
                return;
            }
            if (m_node != null) {
                if (m_node == m_currentRootNode) {
                    m_localeContext.refreshAll();
                } else {
                    updateNode(m_node);
                }
            }
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#focus(org.opencms.util.CmsUUID)
         */
        public void focus(CmsUUID structureId) {
            // not used
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#getAllStructureIdsInView()
         */
        public List<CmsUUID> getAllStructureIdsInView() {

            return null;
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#getAppId()
         */
        public String getAppId() {

            return CmsSitemapEditorConfiguration.APP_ID;
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#getCms()
         */
        public CmsObject getCms() {

            return A_CmsUI.getCmsObject();
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#getContextType()
         */
        public ContextType getContextType() {

            return null;
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#getResources()
         */
        public List<CmsResource> getResources() {

            return Arrays.asList(m_resource);
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#navigateTo(java.lang.String)
         */
        public void navigateTo(String appId) {
            // not used
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#onViewChange()
         */
        public void onViewChange() {
            // do nothing
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#reload()
         */
        public void reload() {

            // do nothing

        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#setWindow(com.vaadin.ui.Window)
         */
        public void setWindow(Window window) {

            m_window = window;
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#start(java.lang.String, com.vaadin.ui.Component)
         */
        public void start(String title, Component dialog) {

            start(title, dialog, DialogWidth.narrow);
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#start(java.lang.String, com.vaadin.ui.Component, org.opencms.ui.components.CmsBasicDialog.DialogWidth)
         */
        public void start(String title, Component dialog, DialogWidth width) {

            if (dialog != null) {
                m_window = CmsBasicDialog.prepareWindow(width);
                m_window.setCaption(title);
                m_window.setContent(dialog);
                UI.getCurrent().addWindow(m_window);
                if (dialog instanceof CmsBasicDialog) {
                    ((CmsBasicDialog)dialog).initActionHandler(m_window);
                }
            }
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#updateUserInfo()
         */
        public void updateUserInfo() {

            // not supported
        }
    }

    /**
     * Copy menu entry.
     */
    class EntryCopy implements I_CmsSimpleContextMenuEntry<MenuContext> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(MenuContext context) {

            openPageCopyDialog(context.getNode(), context.getData());

        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_LOCALECOMPARE_COPY_PAGE_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(MenuContext context) {

            return visibleIfTrue(context.getData().isCopyable());

        }
    }

    /**
     * Menu entry for opening the explorer.<p>
     */
    class EntryExplorer implements I_CmsSimpleContextMenuEntry<MenuContext> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(MenuContext context) {

            String link = CmsCoreService.getVaadinWorkplaceLink(
                A_CmsUI.getCmsObject(),
                context.getData().getResource().getStructureId());
            A_CmsUI.get().getPage().setLocation(link);

        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_LOCALECOMPARE_EXPLORER_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(MenuContext context) {

            return visibleIfTrue(true);
        }

    }

    /**
     * Menu entry for opening the info dialog.<p>
     */
    class EntryInfo implements I_CmsSimpleContextMenuEntry<MenuContext> {

        /**
        
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(MenuContext context) {

            CmsResourceInfoAction infoAction = new CmsResourceInfoAction();
            infoAction.executeAction(new DialogContext(context.getData().getResource(), context.getNode()));
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCE_INFO_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(MenuContext context) {

            return visibleIfTrue(true);
        }

    }

    /**
     * Menu entry for opening the 'LInk locale' dialog.<p>
     */
    class EntryLink implements I_CmsSimpleContextMenuEntry<MenuContext> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        @SuppressWarnings("synthetic-access")
        public void executeAction(MenuContext data) {

            try {
                DialogContext dialogContext = new DialogContext(
                    A_CmsUI.getCmsObject().readResource(
                        data.getData().getClientEntry().getId(),
                        CmsResourceFilter.IGNORE_EXPIRATION),
                    data.getNode());
                CmsLocaleLinkTargetSelectionDialog dialog = new CmsLocaleLinkTargetSelectionDialog(
                    dialogContext,
                    m_localeContext);
                dialogContext.start(
                    CmsVaadinUtils.getMessageText(Messages.GUI_LOCALECOMPARE_LINK_LOCALE_VARIANT_0),
                    dialog,
                    DialogWidth.narrow);
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
                CmsErrorDialog.showErrorDialog(e);
            }
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_LOCALECOMPARE_LINK_LOCALE_VARIANT_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        @SuppressWarnings("synthetic-access")
        public CmsMenuItemVisibilityMode getVisibility(MenuContext context) {

            return activeIfTrue(
                !context.getData().isLinked()
                    && !context.getData().isMarkedNoTranslation(m_localeContext.getComparisonLocale()));
        }
    }

    /**
     * Context menu entry for the 'Do not translate' mark.<p>
     */
    class EntryMark implements I_CmsSimpleContextMenuEntry<MenuContext> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        @SuppressWarnings("synthetic-access")
        public void executeAction(MenuContext context) {

            CmsSitemapTreeNodeData entry = context.getData();
            CmsSitemapTreeNode node = context.getNode();

            CmsObject cms = A_CmsUI.getCmsObject();
            CmsLockActionRecord actionRecord = null;
            CmsResource fileToModify2 = null;
            try {

                CmsResource primary = A_CmsUI.getCmsObject().readResource(
                    entry.getClientEntry().getId(),
                    CmsResourceFilter.IGNORE_EXPIRATION);
                if (primary.isFolder()) {
                    CmsResource defaultFile = A_CmsUI.getCmsObject().readDefaultFile(
                        primary,
                        CmsResourceFilter.IGNORE_EXPIRATION);
                    if (defaultFile != null) {
                        primary = defaultFile;
                    }
                }
                final CmsResource primaryFinal = primary;

                fileToModify2 = primaryFinal;
                if (fileToModify2.isFolder()) {
                    try {
                        fileToModify2 = A_CmsUI.getCmsObject().readDefaultFile(
                            fileToModify2,
                            CmsResourceFilter.IGNORE_EXPIRATION);
                    } catch (CmsException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }

                actionRecord = CmsLockUtil.ensureLock(cms, fileToModify2);
                m_localeContext.getComparisonLocale().toString();
                CmsProperty prop = cms.readPropertyObject(
                    fileToModify2,
                    CmsPropertyDefinition.PROPERTY_LOCALE_NOTRANSLATION,
                    false);
                String propValue = prop.getValue();
                if (propValue == null) {
                    propValue = ""; // make getLocales not return null
                }
                List<Locale> currentLocales = CmsLocaleManager.getLocales(propValue);
                if (!currentLocales.contains(m_localeContext.getComparisonLocale())) {
                    currentLocales.add(m_localeContext.getComparisonLocale());
                    String newPropValue = Joiner.on(",").join(currentLocales);
                    CmsProperty newProp = new CmsProperty(
                        CmsPropertyDefinition.PROPERTY_LOCALE_NOTRANSLATION,
                        newPropValue,
                        null);
                    cms.writePropertyObjects(fileToModify2, Arrays.asList(newProp));
                    DialogContext dialogContext = new DialogContext(
                        A_CmsUI.getCmsObject().readResource(
                            entry.getClientEntry().getId(),
                            CmsResourceFilter.IGNORE_EXPIRATION),
                        node);
                    dialogContext.finish(Arrays.asList(fileToModify2.getStructureId()));

                }

            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
                CmsErrorDialog.showErrorDialog(e);

            } finally {
                if ((actionRecord != null) && (actionRecord.getChange() == LockChange.locked)) {
                    try {
                        cms.unlockResource(fileToModify2);
                    } catch (CmsException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                        CmsErrorDialog.showErrorDialog(e);
                    }
                }
            }

        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_LOCALECOMPARE_ADD_DONT_TRANSLATE_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        @SuppressWarnings("synthetic-access")
        public CmsMenuItemVisibilityMode getVisibility(MenuContext context) {

            CmsSitemapTreeNodeData entry = context.getData();
            boolean result = context.isMainLocale()
                && !entry.isMarkedNoTranslation(m_localeContext.getComparisonLocale())
                && !entry.isLinked();
            return visibleIfTrue(result);

        }

    }

    /**
     * 'Open page' menu entry.<p>
     */
    class EntryOpen implements I_CmsSimpleContextMenuEntry<MenuContext> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        @SuppressWarnings("synthetic-access")
        public void executeAction(MenuContext context) {

            openTargetPage((CmsSitemapTreeNodeData)(context.getNode().getData()), false);

        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_LOCALECOMPARE_OPEN_PAGE_0);

        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(MenuContext data) {

            return visibleIfTrue(true);
        }

    }

    /**
     * 'Properties' menu entry.<p>
     */
    class EntryProperties implements I_CmsSimpleContextMenuEntry<MenuContext> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        @SuppressWarnings("synthetic-access")
        public void executeAction(MenuContext context) {

            ((CmsSitemapUI)A_CmsUI.get()).getSitemapExtension().openPropertyDialog(
                context.getData().getResource().getStructureId(),
                m_root.getStructureId());
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_LOCALECOMPARE_PROPERTIES_0);

        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(MenuContext context) {

            return visibleIfTrue(true);
        }

    }

    /**
     * 'Remove mark' menu entry.<p>
     */
    class EntryRemoveMark implements I_CmsSimpleContextMenuEntry<MenuContext> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        @SuppressWarnings("synthetic-access")
        public void executeAction(MenuContext context) {

            CmsSitemapTreeNodeData entry = context.getData();
            CmsSitemapTreeNode node = context.getNode();

            CmsObject cms = A_CmsUI.getCmsObject();
            CmsLockActionRecord actionRecord = null;
            CmsResource fileToModify2 = null;
            try {
                CmsResource primary = A_CmsUI.getCmsObject().readResource(
                    entry.getClientEntry().getId(),
                    CmsResourceFilter.IGNORE_EXPIRATION);
                if (primary.isFolder()) {
                    CmsResource defaultFile = A_CmsUI.getCmsObject().readDefaultFile(
                        primary,
                        CmsResourceFilter.IGNORE_EXPIRATION);
                    if (defaultFile != null) {
                        primary = defaultFile;
                    }
                }
                final CmsResource primaryFinal = primary;

                fileToModify2 = primaryFinal;
                if (fileToModify2.isFolder()) {
                    try {
                        fileToModify2 = A_CmsUI.getCmsObject().readDefaultFile(
                            fileToModify2,
                            CmsResourceFilter.IGNORE_EXPIRATION);
                    } catch (CmsException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }

                actionRecord = CmsLockUtil.ensureLock(cms, fileToModify2);
                m_localeContext.getComparisonLocale().toString();
                CmsProperty prop = cms.readPropertyObject(
                    fileToModify2,
                    CmsPropertyDefinition.PROPERTY_LOCALE_NOTRANSLATION,
                    false);
                String propValue = prop.getValue();
                if (propValue == null) {
                    propValue = ""; // make getLocales not return null
                }
                List<Locale> currentLocales = CmsLocaleManager.getLocales(propValue);
                if (currentLocales.contains(m_localeContext.getComparisonLocale())) {
                    currentLocales.remove(m_localeContext.getComparisonLocale());
                    String newPropValue = Joiner.on(",").join(currentLocales);
                    CmsProperty newProp = new CmsProperty(
                        CmsPropertyDefinition.PROPERTY_LOCALE_NOTRANSLATION,
                        newPropValue,
                        null);
                    cms.writePropertyObjects(primaryFinal, Arrays.asList(newProp));
                    DialogContext dialogContext = new DialogContext(
                        A_CmsUI.getCmsObject().readResource(
                            entry.getClientEntry().getId(),
                            CmsResourceFilter.IGNORE_EXPIRATION),
                        node);
                    dialogContext.finish(Arrays.asList(fileToModify2.getStructureId()));
                }

            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
                CmsErrorDialog.showErrorDialog(e);

            } finally {
                if ((actionRecord != null) && (actionRecord.getChange() == LockChange.locked)) {
                    try {
                        cms.unlockResource(fileToModify2);
                    } catch (CmsException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                        CmsErrorDialog.showErrorDialog(e);
                    }
                }
            }

        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_LOCALECOMPARE_REMOVE_DONT_TRANSLATE_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        @SuppressWarnings("synthetic-access")
        public CmsMenuItemVisibilityMode getVisibility(MenuContext context) {

            boolean result = context.isMainLocale()
                && context.getData().isMarkedNoTranslation(m_localeContext.getComparisonLocale());
            return visibleIfTrue(result);

        }
    }

    /**
     * 'Unlink' menu entry.<p>
     */
    class EntryUnlink implements I_CmsSimpleContextMenuEntry<MenuContext> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        @SuppressWarnings("synthetic-access")
        public void executeAction(MenuContext context) {

            try {
                CmsResource secondary = context.getData().getLinkedResource();
                DialogContext dialogContext = new DialogContext(
                    A_CmsUI.getCmsObject().readResource(
                        context.getData().getClientEntry().getId(),
                        CmsResourceFilter.IGNORE_EXPIRATION),
                    context.getNode());
                CmsUnlinkDialog dialog = new CmsUnlinkDialog(dialogContext, secondary);
                dialogContext.start(
                    CmsVaadinUtils.getMessageText(Messages.GUI_LOCALECOMPARE_UNLINK_LOCALE_VARIANT_0),
                    dialog,
                    DialogWidth.wide);
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
                CmsErrorDialog.showErrorDialog(e);
            }
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_LOCALECOMPARE_UNLINK_LOCALE_VARIANT_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        @SuppressWarnings("synthetic-access")
        public CmsMenuItemVisibilityMode getVisibility(final MenuContext context) {

            if (!context.getData().isLinked()) {
                return visibleIfTrue(false);
            }
            try {
                CmsResource primary = A_CmsUI.getCmsObject().readResource(
                    context.getData().getClientEntry().getId(),
                    CmsResourceFilter.IGNORE_EXPIRATION);
                if (primary.isFolder()) {
                    CmsResource defaultFile = A_CmsUI.getCmsObject().readDefaultFile(
                        primary,
                        CmsResourceFilter.IGNORE_EXPIRATION);
                    if (defaultFile != null) {
                        primary = defaultFile;
                    }
                }
                CmsLocaleGroupService groupService = A_CmsUI.getCmsObject().getLocaleGroupService();
                Locale mainLocale = groupService.getMainLocale(m_localeContext.getRoot().getRootPath());
                int mainLocaleCount = 0;
                for (Locale testLocale : Arrays.asList(
                    m_localeContext.getRootLocale(),
                    m_localeContext.getComparisonLocale())) {
                    mainLocaleCount += mainLocale.equals(testLocale) ? 1 : 0;
                }
                return visibleIfTrue(mainLocaleCount == 1);
            } catch (Exception e) {
                return visibleIfTrue(false);

            }

        }
    }

    /**
     * Context object for the context menu.<p>
     */
    class MenuContext {

        /** The tree node data. */
        private CmsSitemapTreeNodeData m_data;

        /** The tree node widget. */
        private CmsSitemapTreeNode m_node;

        /**
         * Creates a new instance.<p>
         *
         * @param data the sitemap tree data
         * @param node the tree node widget
         */
        public MenuContext(CmsSitemapTreeNodeData data, CmsSitemapTreeNode node) {
            m_node = node;
            m_data = data;
        }

        /**
         * Gets the tree node data.<p>
         *
         * @return the tree node data
         */
        public CmsSitemapTreeNodeData getData() {

            return m_data;
        }

        /**
         * Gets the tree node widget.<p>
         *
         * @return the tree node widget
         */
        public CmsSitemapTreeNode getNode() {

            return m_node;
        }

        /**
         * Checks if the currently selected locale is the main locale.<p>
         *
         * @return true if we are in the main locale
         */
        @SuppressWarnings("synthetic-access")
        public boolean isMainLocale() {

            return m_localeContext.getRootLocale().equals(
                A_CmsUI.getCmsObject().getLocaleGroupService().getMainLocale(m_localeContext.getRoot().getRootPath()));
        }

    }

    /** Default width for linked items displayed on the right side of tree items. */
    public static final int RHS_WIDTH = 420;

    /** The log isntance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSitemapTreeController.class);

    /** The context menu. */
    CmsContextMenu m_menu = new CmsContextMenu();

    /** The currently opened window. */
    Window m_window;

    /** Map of already loaded nodes. */
    private IdentityHashMap<CmsSitemapTreeNode, Void> m_alreadyLoaded = new IdentityHashMap<>();

    /** Current root node widget. */
    private CmsSitemapTreeNode m_currentRootNode;

    /** The locale context. */
    private I_CmsLocaleCompareContext m_localeContext;

    /** The resource corresponding to the tree's root. */
    private CmsResource m_root;

    /** The tree data provider. */
    private CmsSitemapTreeDataProvider m_treeDataProvider;

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS  context
     * @param root the tree's root resource
     * @param context the locale comparison context
     * @param parent the parent widget in which the tree will be rendered
     */
    public CmsSitemapTreeController(
        CmsObject cms,
        CmsResource root,
        I_CmsLocaleCompareContext context,
        Component parent) {
        m_treeDataProvider = new CmsSitemapTreeDataProvider(cms, root, context);
        m_localeContext = context;
        m_root = root;
        m_menu.extend((AbstractComponent)parent);

    }

    /**
     * Returns VISIBILITY_ACTIVE if the given parameter is true, and VISIBILITY_INACTIVE otherwise.<p>
     *
     * @param condition a boolean value
     * @return the visibility based on the condition value
     */
    public static CmsMenuItemVisibilityMode activeIfTrue(boolean condition) {

        return condition ? CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE : CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE;
    }

    /**
     * Returns VISIBILITY_ACTIVE if the given parameter is true, and VISIBILITY_INVISIBLE otherwise.<p>
     *
     * @param condition a boolean value
     * @return the visibility based on the condition value
     */
    public static CmsMenuItemVisibilityMode visibleIfTrue(boolean condition) {

        return condition ? CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE : CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
    }

    /**
     * Creates a sitemap tree node widget from a tree node bean.<p>
     *
     * @param entry the tree node bean
     * @return the tree node widget
     */
    public CmsSitemapTreeNode createNode(final CmsSitemapTreeNodeData entry) {

        final CmsSitemapTreeNode node = new CmsSitemapTreeNode();

        node.addLayoutClickListener(new LayoutClickListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void layoutClick(LayoutClickEvent event) {

                Component currentComponent = event.getClickedComponent();
                if (currentComponent != null) {
                    boolean linked = false;
                    do {
                        currentComponent = currentComponent.getParent();
                        if ((currentComponent != null)
                            && "linked".equals(((AbstractComponent)currentComponent).getData())) {
                            linked = true;
                        }
                        if (event.getClickedComponent() instanceof CmsResourceIcon) {
                            if (currentComponent == node) {
                                openTargetPage((CmsSitemapTreeNodeData)(node.getData()), linked);
                            } else if (currentComponent instanceof CmsSitemapTreeNode) {
                                break;
                            }
                        }
                    } while (currentComponent != null);
                }

            }

        });
        String icon = CmsResourceIcon.getSitemapResourceIcon(
            A_CmsUI.getCmsObject(),
            entry.getResource(),
            IconMode.localeCompare);
        CmsResourceInfo info = new CmsResourceInfo(
            entry.getClientEntry().getTitle(),
            entry.getClientEntry().getSitePath(),
            icon);
        info = CmsResourceInfo.createSitemapResourceInfo(
            entry.getResource(),
            OpenCms.getSiteManager().getSiteForRootPath(m_localeContext.getRoot().getRootPath()));
        info.getResourceIcon().addStyleName(OpenCmsTheme.POINTER);
        info.getResourceIcon().setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_LOCALECOMPARE_OPEN_PAGE_0));

        if (entry.getClientEntry().isHiddenNavigationEntry()) {
            info.addStyleName(OpenCmsTheme.RESOURCE_INFO_WEAK);
        }
        final MenuBar menu = new MenuBar();
        boolean noTranslation = false;
        noTranslation = entry.isMarkedNoTranslation(m_localeContext.getComparisonLocale());

        final MenuItem main = menu.addItem("", null);
        main.setIcon(FontOpenCms.CONTEXT_MENU);
        CssLayout rightSide = new CssLayout();
        info.setButtonWidget(rightSide);
        rightSide.addComponent(menu);
        main.setCommand(new Command() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            public void menuSelected(MenuItem selectedItem) {

                List<I_CmsSimpleContextMenuEntry<MenuContext>> entries = Arrays.asList(

                    new EntryOpen(),
                    new EntryExplorer(),
                    new EntryProperties(),
                    new EntryLink(),
                    new EntryUnlink(),
                    new EntryMark(),
                    new EntryRemoveMark(),
                    new EntryCopy(),
                    new EntryInfo());

                MenuContext context = new MenuContext(entry, node);
                m_menu.setEntries(entries, context);
                m_menu.open(menu);

            }

        });

        menu.addStyleName("borderless o-toolbar-button o-resourceinfo-toolbar");
        if (entry.isLinked()) {
            CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(m_localeContext.getRoot().getRootPath());
            CmsResourceInfo linkedInfo = CmsResourceInfo.createSitemapResourceInfo(
                readSitemapEntryFolderIfPossible(entry.getLinkedResource()),
                site);
            linkedInfo.addStyleName(OpenCmsTheme.RESOURCE_INFO_DIRECTLINK);
            rightSide.addComponent(linkedInfo, 0);
            linkedInfo.setWidth(RHS_WIDTH + "px");
            node.setContent(info);
            linkedInfo.setData("linked"); // Data used by click handler to distinguish clicked resource icons
            linkedInfo.getResourceIcon().setDescription(
                CmsVaadinUtils.getMessageText(Messages.GUI_LOCALECOMPARE_OPEN_PAGE_0));
            linkedInfo.getResourceIcon().addStyleName(OpenCmsTheme.POINTER);
        } else {
            if (noTranslation) {
                CmsResourceInfo noTranslationInfo = new CmsResourceInfo();
                String topMessage = CmsVaadinUtils.getMessageText(Messages.GUI_LOCALECOMPARE_NO_TRANSLATION_TOP_0);
                String bottomMessage = CmsVaadinUtils.getMessageText(
                    Messages.GUI_LOCALECOMPARE_NO_TRANSLATION_BOTTOM_0);
                noTranslationInfo.getTopLine().setValue(topMessage);
                noTranslationInfo.getBottomLine().setValue(bottomMessage);
                noTranslationInfo.getResourceIcon().setValue(
                    "<span class=\""
                        + OpenCmsTheme.RESOURCE_ICON
                        + " "
                        + OpenCmsTheme.NO_TRANSLATION_ICON
                        + "\">"
                        + FontAwesome.BAN.getHtml()
                        + "</span>");
                noTranslationInfo.addStyleName(OpenCmsTheme.RESOURCE_INFO_DIRECTLINK);
                noTranslationInfo.setWidth(RHS_WIDTH + "px");
                rightSide.addComponent(noTranslationInfo, 0);
            }
            node.setContent(info);
        }

        if (entry.hasNoChildren()) {
            node.setOpen(true);
            node.setOpenerVisible(false);
        }
        node.setData(entry);
        return node;

    }

    /**
     * Creates the root node of the tree.<p>
     *
     * @return the root node of the tree
     */
    public CmsSitemapTreeNode createRootNode() {

        m_currentRootNode = createNode(m_treeDataProvider.getRoot());
        return m_currentRootNode;
    }

    /**
     * Gets the resource corresponding to the tree's root.<p>
     *
     * @return the resource for the root node
     */
    public CmsResource getRoot() {

        return m_root;
    }

    /**
     * Initializes the event handlers for a tree node widget.<p>
     *
     * @param node the node for which to initialize the event handlers
     */
    public void initEventHandlers(final CmsSitemapTreeNode node) {

        node.getOpener().addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                CmsSitemapTreeController.this.onClickOpen(node);
            }
        });
    }

    /**
     * Called when the user clicks on the 'opener' icon of a sitemap tree entry.<p>
     *
     * @param node the sitemap node widget
     */
    public void onClickOpen(CmsSitemapTreeNode node) {

        if (node.isOpen()) {
            node.setOpen(false);
        } else {
            if (!m_alreadyLoaded.containsKey(node)) {
                Object nodeData = node.getData();
                List<CmsSitemapTreeNodeData> children = m_treeDataProvider.getChildren(
                    (CmsSitemapTreeNodeData)nodeData);
                m_alreadyLoaded.put(node, null);
                if (children.isEmpty()) {
                    node.setOpenerVisible(false);
                } else {
                    for (CmsSitemapTreeNodeData child : children) {
                        CmsSitemapTreeNode childNode = createNode(child);
                        childNode.setData(child);
                        initEventHandlers(childNode);
                        node.getChildren().addComponent(childNode);
                    }
                }
            }
            node.setOpen(true);
        }
    }

    /**
     * Opens the page copy dialog for a tree entry.<p>
     *
     * @param node the tree node widget
     * @param entry the tree entry
     */
    public void openPageCopyDialog(CmsSitemapTreeNode node, CmsSitemapTreeNodeData entry) {

        CmsObject cms = A_CmsUI.getCmsObject();
        try {
            CmsResource resource = cms.readResource(
                entry.getClientEntry().getId(),
                CmsResourceFilter.IGNORE_EXPIRATION);
            DialogContext context = new DialogContext(resource, node);
            CmsCopyPageDialog dialog = new CmsCopyPageDialog(context);
            String title = CmsVaadinUtils.getMessageText(Messages.GUI_COPYPAGE_DIALOG_TITLE_0);
            context.start(title, dialog);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            CmsErrorDialog.showErrorDialog(e);
        }
    }

    /**
     * Updates a sitemap node widget after the resource it corresponds to has changed.<p>
     *
     * @param node the sitemap node
     */
    public void updateNode(CmsSitemapTreeNode node) {

        CmsSitemapTreeNodeData data = (CmsSitemapTreeNodeData)node.getData();
        try {
            CmsSitemapTreeNodeData changedData = m_treeDataProvider.getData(
                A_CmsUI.getCmsObject().readResource(
                    data.getClientEntry().getId(),
                    CmsResourceFilter.IGNORE_EXPIRATION));
            CmsSitemapTreeNode changedNode = createNode(changedData);
            initEventHandlers(changedNode);
            ComponentContainer parent = (ComponentContainer)(node.getParent());
            parent.replaceComponent(node, changedNode);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Updates the tree node for the resource with the given structure id, if it exists.<p>
     *
     * @param id the structure id of a resource
     */
    public void updateNodeForId(final CmsUUID id) {

        final List<CmsSitemapTreeNode> nodes = Lists.newArrayList();
        CmsVaadinUtils.visitDescendants(m_currentRootNode, new Predicate<Component>() {

            public boolean apply(Component input) {

                if (input instanceof CmsSitemapTreeNode) {
                    CmsSitemapTreeNode node = (CmsSitemapTreeNode)input;
                    CmsSitemapTreeNodeData data = (CmsSitemapTreeNodeData)node.getData();
                    if (data.getResource().getStructureId().equals(id)) {
                        nodes.add(node);
                        return false;
                    }
                }
                return true;
            }
        });
        if (nodes.size() == 1) {
            updateNode(nodes.get(0));
        }

    }

    /**
     * If the given resource is the default file of a sitmeap entry folder, then returns that
     * folder, else the original file.<p>
     *
     * @param resource a resource
     * @return the resource or its parent folder
     */
    protected CmsResource readSitemapEntryFolderIfPossible(CmsResource resource) {

        CmsObject cms = A_CmsUI.getCmsObject();
        try {
            if (resource.isFolder()) {
                return resource;
            }
            CmsResource parent = cms.readParentFolder(resource.getStructureId());
            CmsResource defaultFile = cms.readDefaultFile(parent, CmsResourceFilter.IGNORE_EXPIRATION);
            if ((defaultFile != null) && defaultFile.equals(resource)) {
                return parent;
            }
            return resource;
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return resource;
        }
    }

    /**
     * Gets the logger for the tree controller.<p>
     *
     * @return the logger
     */
    Log getTreeControllerLog() {

        return LOG;
    }

    /**
     * Opens the page corresponding to a sitemap entry.<p>
     *
     * @param nodeData the node bean
     * @param second true if the user has clicked on the second resource box in a tree node
     */
    private void openTargetPage(CmsSitemapTreeNodeData nodeData, boolean second) {

        CmsUUID id = nodeData.getClientEntry().getId();
        CmsUUID defaultFileId = nodeData.getClientEntry().getDefaultFileId();
        CmsUUID targetId = defaultFileId;
        if (targetId == null) {
            targetId = id;
        }
        try {
            CmsResource resource = A_CmsUI.getCmsObject().readResource(targetId, CmsResourceFilter.IGNORE_EXPIRATION);
            String link = OpenCms.getLinkManager().substituteLink(A_CmsUI.getCmsObject(), resource);
            if (second) {
                resource = A_CmsUI.getCmsObject().readResource(
                    nodeData.getLinkedResource().getStructureId(),
                    CmsResourceFilter.IGNORE_EXPIRATION);
                link = OpenCms.getLinkManager().substituteLink(A_CmsUI.getCmsObject(), resource);
            }

            String mySiteRoot = A_CmsUI.getCmsObject().getRequestContext().getSiteRoot();
            final boolean sameSite = mySiteRoot.equals(OpenCms.getSiteManager().getSiteRoot(resource.getRootPath()));

            if (sameSite) {
                A_CmsUI.get().getPage().setLocation(link);
            } else {
                String message = CmsVaadinUtils.getMessageText(
                    Messages.GUI_LOCALECOMPARE_SHOW_WRONGSITE_1,
                    resource.getRootPath());

                Notification.show(message, Type.ERROR_MESSAGE);
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

    }

}
