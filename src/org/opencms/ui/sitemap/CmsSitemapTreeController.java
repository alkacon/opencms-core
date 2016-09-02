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

package org.opencms.ui.sitemap;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsLocaleGroup;
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
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsResourceIcon;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.components.fileselect.CmsSitemapTreeContainer;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.CmsContextMenu.ContextMenuItem;
import org.opencms.ui.contextmenu.CmsContextMenu.ContextMenuItemClickEvent;
import org.opencms.ui.contextmenu.CmsContextMenu.ContextMenuItemClickListener;
import org.opencms.util.CmsUUID;

import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.google.common.base.Joiner;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
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
        @SuppressWarnings("synthetic-access")
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
            if (m_node == m_currentRootNode) {
                m_localeContext.refreshAll();
            } else {
                updateNode(m_node);
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
         * @see org.opencms.ui.I_CmsDialogContext#start(java.lang.String, com.vaadin.ui.Component)
         */
        public void start(String title, Component dialog) {

            start(title, dialog, DialogWidth.narrow);
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#start(java.lang.String, com.vaadin.ui.Component, org.opencms.ui.components.CmsBasicDialog.DialogWidth)
         */
        @SuppressWarnings("synthetic-access")
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
    }

    /** The log isntance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSitemapTreeController.class);

    /** The context menu. */
    CmsContextMenu m_menu = new CmsContextMenu();

    /** Map of already loaded nodes. */
    private IdentityHashMap<CmsSitemapTreeNode, Void> m_alreadyLoaded = new IdentityHashMap<>();

    /** Current root node widget. */
    private CmsSitemapTreeNode m_currentRootNode;

    /** The locale context. */
    private I_CmsLocaleCompareContext m_localeContext;

    /** The tree data provider. */
    private CmsSitemapTreeDataProvider m_treeDataProvider;

    /** The currently opened window. */
    private Window m_window;

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
        m_menu.extend((AbstractComponent)parent);

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
                            System.out.println("linked -> true");
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
        String icon = CmsSitemapTreeContainer.getSitemapResourceIcon(A_CmsUI.getCmsObject(), entry.getResource());
        CmsResourceInfo info = new CmsResourceInfo(
            entry.getClientEntry().getTitle(),
            entry.getClientEntry().getSitePath(),
            icon);
        info = CmsResourceInfo.createSitemapResourceInfo(
            entry.getResource(),
            OpenCms.getSiteManager().getSiteForRootPath(m_localeContext.getRoot().getRootPath()));
        info.getResourceIcon().addStyleName(OpenCmsTheme.POINTER);

        if (entry.getClientEntry().isHiddenNavigationEntry()) {
            info.addStyleName(OpenCmsTheme.RESOURCE_INFO_WEAK);
        }
        final MenuBar menu = new MenuBar();
        if (entry.isMarkedNoTranslation(m_localeContext.getComparisonLocale())) {
            final MenuItem mark = menu.addItem("", null);
            mark.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_LOCALECOMPARE_DONT_TRANSLATE_0));
            mark.setIcon(FontAwesome.BAN);
            mark.setStyleName("o-sitemap-notranslation");
        }
        final MenuItem main = menu.addItem("", null);
        main.setIcon(FontOpenCms.CONTEXT_MENU);
        info.setButton(menu);
        main.setCommand(new Command() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void menuSelected(MenuItem selectedItem) {

                m_menu.removeAllItems();
                if (!entry.isLinked()) {
                    addLinkItem(entry, node);
                }
                if (m_localeContext.getRootLocale().equals(A_CmsUI.getCmsObject().getLocaleGroupService().getMainLocale(
                    m_localeContext.getRoot().getRootPath()))) {
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
                        addMarkItems(entry, node, primaryFinal);
                    } catch (CmsException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                        CmsErrorDialog.showErrorDialog(e);
                    }

                }

                if (entry.isLinked()) {
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
                        CmsLocaleGroupService groupService = A_CmsUI.getCmsObject().getLocaleGroupService();
                        Locale mainLocale = groupService.getMainLocale(m_localeContext.getRoot().getRootPath());
                        int mainLocaleCount = 0;
                        for (Locale testLocale : Arrays.asList(
                            m_localeContext.getRootLocale(),
                            m_localeContext.getComparisonLocale())) {
                            mainLocaleCount += mainLocale.equals(testLocale) ? 1 : 0;
                        }
                        if (mainLocaleCount == 1) {
                            addUnlinkItem(entry, node);
                        }
                        CmsLocaleGroup localeGroup = groupService.readLocaleGroup(primary);
                        if (localeGroup.isRealGroup()) {
                            ContextMenuItem show = m_menu.addItem(
                                CmsVaadinUtils.getMessageText(Messages.GUI_LOCALECOMPARE_SHOW_LOCALE_0));
                            Map<Locale, CmsResource> resourcesByLocale = localeGroup.getResourcesByLocale();
                            String mySiteRoot = A_CmsUI.getCmsObject().getRequestContext().getSiteRoot();
                            int realSubitemCount = 0;
                            for (Map.Entry<Locale, CmsResource> localeGroupEntry : resourcesByLocale.entrySet()) {
                                final Locale locale = localeGroupEntry.getKey();
                                final CmsResource resource = localeGroupEntry.getValue();
                                final boolean sameSite = mySiteRoot.equals(
                                    OpenCms.getSiteManager().getSiteRoot(resource.getRootPath()));

                                String localeName = locale.getDisplayLanguage(A_CmsUI.get().getLocale());
                                ContextMenuItem showLanguage = show.addItem(localeName);
                                if (!sameSite) {
                                    showLanguage.addStyleName("o-show-locale-disabled");
                                }
                                showLanguage.addItemClickListener(new ContextMenuItemClickListener() {

                                    public void contextMenuItemClicked(ContextMenuItemClickEvent event) {

                                        if (sameSite) {
                                            CmsObject cms = A_CmsUI.getCmsObject();
                                            String link = OpenCms.getLinkManager().substituteLink(cms, resource);
                                            A_CmsUI.get().getPage().setLocation(link);
                                        } else {
                                            String message = CmsVaadinUtils.getMessageText(
                                                Messages.GUI_LOCALECOMPARE_SHOW_WRONGSITE_1,
                                                resource.getRootPath());

                                            Notification.show(message, Type.ERROR_MESSAGE);
                                        }
                                    }
                                });
                                realSubitemCount += 1;

                            }
                            if (realSubitemCount == 0) {
                                m_menu.removeItem(show);
                            }
                        }
                    } catch (Exception e) {
                        LOG.error(e.getLocalizedMessage(), e);
                        CmsErrorDialog.showErrorDialog(e);
                    }
                }

                m_menu.open(menu);
            }

        });

        menu.addStyleName("borderless o-toolbar-button o-resourceinfo-toolbar");
        if (entry.isLinked()) {
            CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(m_localeContext.getRoot().getRootPath());
            CmsResourceInfo linkedInfo = CmsResourceInfo.createSitemapResourceInfo(entry.getLinkedResource(), site);
            if (entry.isDirectLink()) {
                linkedInfo.addStyleName(OpenCmsTheme.RESOURCE_INFO_DIRECTLINK);
            }
            HorizontalLayout row = new HorizontalLayout();
            row.addComponent(info);
            row.addComponent(linkedInfo);
            row.setExpandRatio(info, 1.0f);
            row.setExpandRatio(linkedInfo, 1.0f);
            row.setWidth("100%");
            node.setContent(row);
            linkedInfo.setData("linked");
            linkedInfo.getResourceIcon().addStyleName(OpenCmsTheme.POINTER);
        } else {
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
     * Gets the logger for the tree controller.<p>
     *
     * @return the logger
     */
    Log getTreeControllerLog() {

        return LOG;
    }

    /**
     * Adds context menu items.<p<
     *
     * @param entry the sitemap node data
     * @param node the sitemap node widget
     */
    private void addLinkItem(final CmsSitemapTreeNodeData entry, final CmsSitemapTreeNode node) {

        ContextMenuItem linkItem = m_menu.addItem(
            CmsVaadinUtils.getMessageText(Messages.GUI_LOCALECOMPARE_LINK_LOCALE_VARIANT_0));
        linkItem.addItemClickListener(new ContextMenuItemClickListener() {

            @SuppressWarnings("synthetic-access")
            public void contextMenuItemClicked(ContextMenuItemClickEvent event) {

                try {
                    DialogContext dialogContext = new DialogContext(
                        A_CmsUI.getCmsObject().readResource(
                            entry.getClientEntry().getId(),
                            CmsResourceFilter.IGNORE_EXPIRATION),
                        node);
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
        });
    }

    /**
     * Adds context menu items.<p>
     *
     * @param entry the tree node bean
     * @param node the tree node widget
     * @param fileToModify the file which the context menu items should actually operate on
     */
    private void addMarkItems(
        final CmsSitemapTreeNodeData entry,
        final CmsSitemapTreeNode node,
        final CmsResource fileToModify) {

        if (!entry.isMarkedNoTranslation(m_localeContext.getComparisonLocale())) {
            ContextMenuItem markPage = m_menu.addItem(
                CmsVaadinUtils.getMessageText(Messages.GUI_LOCALECOMPARE_ADD_DONT_TRANSLATE_0));
            markPage.addItemClickListener(new ContextMenuItemClickListener() {

                @SuppressWarnings("synthetic-access")
                public void contextMenuItemClicked(ContextMenuItemClickEvent event) {

                    CmsResource fileToModify2 = fileToModify;
                    if (fileToModify2.isFolder()) {
                        try {
                            fileToModify2 = A_CmsUI.getCmsObject().readDefaultFile(
                                fileToModify2,
                                CmsResourceFilter.IGNORE_EXPIRATION);
                        } catch (CmsException e) {
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                    }

                    CmsObject cms = A_CmsUI.getCmsObject();
                    CmsLockActionRecord actionRecord = null;
                    try {
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

            });
        } else {
            ContextMenuItem unmarkPage = m_menu.addItem(
                CmsVaadinUtils.getMessageText(Messages.GUI_LOCALECOMPARE_REMOVE_DONT_TRANSLATE_0));
            unmarkPage.addItemClickListener(new ContextMenuItemClickListener() {

                @SuppressWarnings("synthetic-access")
                public void contextMenuItemClicked(ContextMenuItemClickEvent event) {

                    CmsResource fileToModify2 = fileToModify;
                    if (fileToModify2.isFolder()) {
                        try {
                            fileToModify2 = A_CmsUI.getCmsObject().readDefaultFile(
                                fileToModify2,
                                CmsResourceFilter.IGNORE_EXPIRATION);
                        } catch (CmsException e) {
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                    }

                    CmsObject cms = A_CmsUI.getCmsObject();
                    CmsLockActionRecord actionRecord = null;
                    try {
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
                            cms.writePropertyObjects(fileToModify, Arrays.asList(newProp));
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

            });
        }
    }

    /**
     * Adds a context menu item.<p>
     *
     * @param entry the tree node bean
     * @param node the tree node widget
     */
    private void addUnlinkItem(final CmsSitemapTreeNodeData entry, final CmsSitemapTreeNode node) {

        ContextMenuItem unlinkItem = m_menu.addItem(
            CmsVaadinUtils.getMessageText(Messages.GUI_LOCALECOMPARE_UNLINK_LOCALE_VARIANT_0));
        unlinkItem.addItemClickListener(new ContextMenuItemClickListener() {

            @SuppressWarnings("synthetic-access")
            public void contextMenuItemClicked(ContextMenuItemClickEvent event) {

                try {
                    CmsResource secondary = entry.getLinkedResource();
                    DialogContext dialogContext = new DialogContext(
                        A_CmsUI.getCmsObject().readResource(
                            entry.getClientEntry().getId(),
                            CmsResourceFilter.IGNORE_EXPIRATION),
                        node);
                    CmsUnlinkDialog dialog = new CmsUnlinkDialog(dialogContext, secondary, m_localeContext);
                    dialogContext.start(
                        CmsVaadinUtils.getMessageText(Messages.GUI_LOCALECOMPARE_UNLINK_LOCALE_VARIANT_0),
                        dialog,
                        DialogWidth.wide);
                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                    CmsErrorDialog.showErrorDialog(e);
                }

            }

        });
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
