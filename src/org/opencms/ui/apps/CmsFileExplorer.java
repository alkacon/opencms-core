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

package org.opencms.ui.apps;

import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_INSIDE_PROJECT;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockActionRecord.LockChange;
import org.opencms.lock.CmsLockException;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.I_CmsContextMenuBuilder;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.I_CmsUpdateListener;
import org.opencms.ui.actions.CmsCopyMoveDialogAction;
import org.opencms.ui.actions.CmsPropertiesDialogAction;
import org.opencms.ui.actions.I_CmsWorkplaceAction;
import org.opencms.ui.components.A_CmsFocusShortcutListener;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsFileTable;
import org.opencms.ui.components.CmsResourceTableProperty;
import org.opencms.ui.components.CmsToolBar;
import org.opencms.ui.components.CmsUploadButton;
import org.opencms.ui.components.CmsUploadButton.I_UploadListener;
import org.opencms.ui.components.I_CmsFilePropertyEditHandler;
import org.opencms.ui.components.I_CmsWindowCloseListener;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.components.extensions.CmsGwtDialogExtension;
import org.opencms.ui.components.extensions.CmsUploadAreaExtension;
import org.opencms.ui.contextmenu.CmsContextMenuTreeBuilder;
import org.opencms.ui.contextmenu.I_CmsContextMenuItem;
import org.opencms.ui.contextmenu.I_CmsContextMenuItemProvider;
import org.opencms.ui.dialogs.CmsCopyMoveDialog;
import org.opencms.ui.dialogs.CmsDeleteDialog;
import org.opencms.ui.dialogs.CmsNewDialog;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsTreeNode;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItemClickEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItemClickListener;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.Action;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.ServerSideCriterion;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.AbstractErrorMessage.ContentMode;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.server.UserError;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.CollapseEvent;
import com.vaadin.ui.Tree.CollapseListener;
import com.vaadin.ui.Tree.ExpandEvent;
import com.vaadin.ui.Tree.ExpandListener;
import com.vaadin.ui.Tree.ItemStyleGenerator;
import com.vaadin.ui.Tree.TreeDragMode;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

/**
 * The file explorer app.<p>
 */
public class CmsFileExplorer
implements I_CmsWorkplaceApp, ViewChangeListener, I_CmsWindowCloseListener, I_CmsHasShortcutActions {

    /**
     * Handles inline editing within the file table.<p>
     */
    public class ContextMenuEditHandler implements I_CmsFilePropertyEditHandler {

        /** The serial version id. */
        private static final long serialVersionUID = -9160838301862765592L;

        /** The edited content structure id. */
        private CmsUUID m_editId;

        /** The edited property. */
        private CmsResourceTableProperty m_editProperty;

        /** The lock action record. */
        private CmsLockActionRecord m_lockActionRecord;

        /**
         * Constructor.<p>
         *
         * @param editId the content structure id
         * @param editProperty the property to edit
         */
        public ContextMenuEditHandler(CmsUUID editId, CmsResourceTableProperty editProperty) {
            m_editId = editId;
            m_editProperty = editProperty;
        }

        /**
         * Cancels the edit process. Unlocks the resource if required.<p>
         *
         * @see org.opencms.ui.components.I_CmsFilePropertyEditHandler#cancel()
         */
        public void cancel() {

            if (m_lockActionRecord.getChange() == LockChange.locked) {
                CmsObject cms = A_CmsUI.getCmsObject();
                try {
                    CmsResource res = cms.readResource(m_editId);
                    cms.unlockResource(res);
                } catch (CmsException e) {
                    LOG.warn("Failed to unlock resource " + m_editId.toString(), e);
                }
            }
            CmsAppWorkplaceUi.get().enableGlobalShortcuts();
        }

        /**
         * @see org.opencms.ui.components.I_CmsFilePropertyEditHandler#save(java.lang.String)
         */
        public void save(String value) {

            try {
                CmsObject cms = A_CmsUI.getCmsObject();
                CmsResource res = cms.readResource(m_editId);
                try {
                    if (CmsResourceTableProperty.PROPERTY_NAVIGATION_TEXT.equals(m_editProperty)
                        || CmsResourceTableProperty.PROPERTY_TITLE.equals(m_editProperty)) {

                        CmsProperty prop = new CmsProperty(
                            m_editProperty == CmsResourceTableProperty.PROPERTY_NAVIGATION_TEXT
                            ? CmsPropertyDefinition.PROPERTY_NAVTEXT
                            : CmsPropertyDefinition.PROPERTY_TITLE,
                            value,
                            null);
                        cms.writePropertyObject(cms.getSitePath(res), prop);
                    } else if (CmsResourceTableProperty.PROPERTY_RESOURCE_NAME.equals(m_editProperty)) {
                        String sourcePath = cms.getSitePath(res);
                        cms.renameResource(
                            sourcePath,
                            CmsStringUtil.joinPaths(CmsResource.getParentFolder(sourcePath), value));
                    }
                } finally {
                    if (m_lockActionRecord.getChange() == LockChange.locked) {
                        CmsResource updatedRes = cms.readResource(res.getStructureId(), CmsResourceFilter.ALL);
                        try {
                            cms.unlockResource(updatedRes);
                        } catch (CmsLockException e) {
                            LOG.warn(e.getLocalizedMessage(), e);
                        }
                    }
                    CmsAppWorkplaceUi.get().enableGlobalShortcuts();
                }
            } catch (CmsException e) {
                LOG.error("Exception while saving changed " + m_editProperty + " to resource " + m_editId, e);
                CmsErrorDialog.showErrorDialog(e);
            }

        }

        /**
         * @see org.opencms.ui.components.I_CmsFilePropertyEditHandler#start()
         */
        public void start() {

            CmsObject cms = A_CmsUI.getCmsObject();
            try {
                CmsResource res = cms.readResource(m_editId);
                m_lockActionRecord = CmsLockUtil.ensureLock(cms, res);
                CmsAppWorkplaceUi.get().disableGlobalShortcuts();
                m_fileTable.startEdit(m_editId, m_editProperty, this);
            } catch (CmsException e) {
                CmsErrorDialog.showErrorDialog(e);
                LOG.debug(e.getLocalizedMessage(), e);
            }
        }

        /**
         * @see com.vaadin.event.FieldEvents.TextChangeListener#textChange(com.vaadin.event.FieldEvents.TextChangeEvent)
         */
        public void textChange(TextChangeEvent event) {

            TextField tf = (TextField)event.getSource();
            try {
                validate(event.getText());
                tf.setComponentError(null);
            } catch (InvalidValueException e) {
                tf.setComponentError(new UserError(e.getHtmlMessage(), ContentMode.HTML, null));
            }
        }

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            if ((m_editProperty == CmsResourceTableProperty.PROPERTY_RESOURCE_NAME) && (value instanceof String)) {
                try {
                    String newName = (String)value;
                    CmsResource.checkResourceName(newName);
                    CmsObject cms = A_CmsUI.getCmsObject();
                    CmsResource res = cms.readResource(m_editId);
                    if (!res.getName().equals(newName)) {
                        String sourcePath = cms.getSitePath(res);
                        if (cms.existsResource(
                            CmsStringUtil.joinPaths(CmsResource.getParentFolder(sourcePath), newName))) {
                            throw new InvalidValueException("The selected filename already exists.");
                        }
                    }
                } catch (CmsIllegalArgumentException e) {
                    throw new InvalidValueException(e.getLocalizedMessage(A_CmsUI.get().getLocale()));
                } catch (CmsException e) {
                    LOG.warn("Error while validating new filename", e);
                    throw new InvalidValueException(e.getLocalizedMessage(A_CmsUI.get().getLocale()));
                }
            }
        }
    }

    /** The drop handler for copy/move operations. */
    public class ExplorerDropHandler implements DropHandler {

        /** The serial version id. */
        private static final long serialVersionUID = 5392136127699472654L;

        /** The copy move action. */
        final I_CmsWorkplaceAction COPY_MOVE_ACTION = new CmsCopyMoveDialogAction();

        /**
         * @see com.vaadin.event.dd.DropHandler#drop(com.vaadin.event.dd.DragAndDropEvent)
         */
        public void drop(DragAndDropEvent dragEvent) {

            try {
                CmsExplorerDialogContext context = getContext(dragEvent);
                if (COPY_MOVE_ACTION.isActive(context)) {
                    CmsCopyMoveDialog dialog = new CmsCopyMoveDialog(context);
                    dialog.setTargetFolder(getTargetId(dragEvent));
                    context.start(COPY_MOVE_ACTION.getTitle(), dialog);
                }
            } catch (Exception e) {
                LOG.error("Moving resource failed", e);
            }

        }

        /**
         * @see com.vaadin.event.dd.DropHandler#getAcceptCriterion()
         */
        public AcceptCriterion getAcceptCriterion() {

            return new ServerSideCriterion() {

                private static final long serialVersionUID = 1L;

                public boolean accept(DragAndDropEvent dragEvent) {

                    try {
                        if (!COPY_MOVE_ACTION.isActive(getContext(dragEvent))) {
                            return false;
                        }
                    } catch (CmsException e) {
                        LOG.error("Drag an drop evaluation failed", e);
                        return false;
                    }
                    CmsUUID targetId = getTargetId(dragEvent);
                    return mayDrop(targetId);
                }
            };
        }

        /**
         * Returns the drag target id.<p>
         *
         * @param dragEvent the drag event
         *
         * @return the drag target id
         */
        protected CmsUUID getTargetId(DragAndDropEvent dragEvent) {

            CmsUUID targetId = null;
            if (dragEvent.getTargetDetails() instanceof AbstractSelectTargetDetails) {
                AbstractSelectTargetDetails target = (AbstractSelectTargetDetails)dragEvent.getTargetDetails();
                targetId = (CmsUUID)target.getItemIdOver();
            }
            try {
                CmsObject cms = A_CmsUI.getCmsObject();
                CmsResource target = cms.readResource(targetId);
                if (target.isFile()) {
                    targetId = null;
                }
            } catch (CmsException e) {
                targetId = null;
                LOG.debug("Checking drop target failed, use current folder.", e);
            }

            if (targetId == null) {
                targetId = getCurrentFolder();
            }
            return targetId;
        }

        /**
         * Evaluates if a drop on the given target is allowed.<p>
         *
         * @param targetId the target id
         *
         * @return <code>true</code> if the resources may be dropped to the given target
         */
        protected boolean mayDrop(CmsUUID targetId) {

            boolean result = false;
            try {
                CmsObject cms = A_CmsUI.getCmsObject();
                CmsResource target = cms.readResource(targetId);
                result = cms.hasPermissions(target, CmsPermissionSet.ACCESS_WRITE, false, CmsResourceFilter.ALL);
            } catch (Exception e) {
                LOG.debug("Checking folder write permissions failed", e);
            }
            return result;
        }

        /**
         * Returns the dialog context to use.<p>
         *
         * @param dragEvent the drag event
         *
         * @return the dialog context
         *
         * @throws CmsException if reading the drag resource fails
         */
        CmsExplorerDialogContext getContext(DragAndDropEvent dragEvent) throws CmsException {

            List<CmsResource> resources;
            if ((dragEvent.getTransferable().getSourceComponent() instanceof Table)
                && !m_fileTable.getSelectedResources().isEmpty()) {
                resources = m_fileTable.getSelectedResources();
            } else {
                CmsObject cms = A_CmsUI.getCmsObject();
                CmsUUID sourceId = (CmsUUID)dragEvent.getTransferable().getData("itemId");
                CmsResource source = cms.readResource(sourceId);
                resources = Collections.singletonList(source);
            }
            CmsExplorerDialogContext context = new CmsExplorerDialogContext(
                m_appContext,
                CmsFileExplorer.this,
                resources);
            return context;
        }
    }

    /**
     * Context menu builder for explorer.<p>
     */
    protected class MenuBuilder implements I_CmsContextMenuBuilder {

        /** Tree builder used to build the tree of menu items. */
        private CmsContextMenuTreeBuilder m_treeBuilder;

        /**
         * @see org.opencms.ui.I_CmsContextMenuBuilder#buildContextMenu(java.util.List, org.vaadin.peter.contextmenu.ContextMenu)
         */
        public void buildContextMenu(List<CmsResource> resources, ContextMenu menu) {

            CmsContextMenuTreeBuilder treeBuilder = new CmsContextMenuTreeBuilder(createDialogContext());
            m_treeBuilder = treeBuilder;
            CmsTreeNode<I_CmsContextMenuItem> tree = treeBuilder.buildAll(m_menuItemProvider.getMenuItems());
            for (CmsTreeNode<I_CmsContextMenuItem> node : tree.getChildren()) {
                createItem(menu, node);
            }
        }

        /**
         * Gets the localized title for the context menu item by resolving any message key macros in the raw title using the current locale.<p>
         *
         * @param item the unlocalized title
         * @return the localized title
         */
        String getTitle(I_CmsContextMenuItem item) {

            return CmsVaadinUtils.localizeString(item.getTitle(A_CmsUI.get().getLocale()));
        }

        /**
         * Creates a context menu item.<p>
         *
         * @param parent the parent (either the context menu itself, or a parent item)
         * @param node the node which should be added as a context menu item
         *
         * @return the created item
         */
        private ContextMenuItem createItem(Object parent, CmsTreeNode<I_CmsContextMenuItem> node) {

            final I_CmsContextMenuItem data = node.getData();
            ContextMenuItem guiMenuItem = null;
            if (parent instanceof ContextMenu) {
                guiMenuItem = ((ContextMenu)parent).addItem(getTitle(data));
            } else {
                guiMenuItem = ((ContextMenuItem)parent).addItem(getTitle(data));
            }
            if (m_treeBuilder.getVisibility(data).isInActive()) {
                guiMenuItem.setEnabled(false);
            }
            if (node.getChildren().size() > 0) {
                for (CmsTreeNode<I_CmsContextMenuItem> childNode : node.getChildren()) {
                    createItem(guiMenuItem, childNode);
                }
            } else {
                guiMenuItem.addItemClickListener(new ContextMenuItemClickListener() {

                    public void contextMenuItemClicked(ContextMenuItemClickEvent event) {

                        data.executeAction(createDialogContext());
                    }
                });

            }
            return guiMenuItem;
        }
    }

    /** The opened paths session attribute name. */
    public static final String OPENED_PATHS = "explorer-opened-paths";

    /** Site selector caption property. */
    public static final String SITE_CAPTION = "site_caption";

    /** Site selector site root property. */
    public static final String SITE_ROOT = "site_root";

    /** The state separator string. */
    public static final String STATE_SEPARATOR = "!!";

    /** Logger instance for this class. */
    static final Log LOG = CmsLog.getLog(CmsFileExplorer.class);

    /** The delete shortcut. */
    private static final Action ACTION_DELETE = new ShortcutAction("Del", ShortcutAction.KeyCode.DELETE, null);

    /** The open parent folder shortcut. */
    private static final Action ACTION_FOLDER_UP = new ShortcutAction(
        "Alt+ArrowUp",
        ShortcutAction.KeyCode.ARROW_UP,
        new int[] {ShortcutAction.ModifierKey.ALT});

    /** The edit properties shortcut. */
    private static final Action ACTION_PROPERTIES = new ShortcutAction(
        "Alt+Enter",
        ShortcutAction.KeyCode.ENTER,
        new int[] {ShortcutAction.ModifierKey.ALT});

    /** The rename shortcut. */
    private static final Action ACTION_RENAME = new ShortcutAction("F2", ShortcutAction.KeyCode.F2, null);

    /** The select all shortcut. */
    private static final Action ACTION_SELECT_ALL = new ShortcutAction(
        "Ctrl+A",
        ShortcutAction.KeyCode.A,
        new int[] {ShortcutAction.ModifierKey.CTRL});

    /** The files and folder resource filter. */
    private static final CmsResourceFilter FILES_N_FOLDERS = CmsResourceFilter.ONLY_VISIBLE;

    /** The folders resource filter. */
    private static final CmsResourceFilter FOLDERS = CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireFolder();

    /** The serial version id. */
    private static final long serialVersionUID = 1L;

    /** The UI context. */
    protected I_CmsAppUIContext m_appContext;

    /** The menu item provider. */
    protected I_CmsContextMenuItemProvider m_menuItemProvider = CmsAppWorkplaceUi.get().getMenuItemProvider();

    /** Saved explorer state used by dialogs after they have finished. */
    protected String m_savedExplorerState = "";

    /** The table containing the contents of the current folder. */
    CmsFileTable m_fileTable;

    /** The info path. */
    TextField m_infoPath;

    /** The explorer shortcuts. */
    Map<Action, Runnable> m_shortcutActions;

    /** The bread crumb click listener. */
    private ClickListener m_crumbListener;

    /** The path bread crumb container. */
    private CssLayout m_crumbs;

    /** The currently viewed folder. */
    private CmsUUID m_currentFolder;

    /** The current app state. */
    private String m_currentState;

    /** The folder tree. */
    private Tree m_fileTree;

    /** The opened paths by site. */
    private Map<String, String> m_openedPaths;

    /** The search field. */
    private TextField m_searchField;

    /** The site selector. */
    private ComboBox m_siteSelector;

    /** The folder tree data container. */
    private HierarchicalContainer m_treeContainer;

    /** The upload drop area extension. */
    private CmsUploadAreaExtension m_uploadArea;

    /** The upload button. */
    private CmsUploadButton m_uploadButton;

    /**
     * Constructor.<p>
     */
    @SuppressWarnings("unchecked")
    public CmsFileExplorer() {
        m_shortcutActions = new HashMap<Action, Runnable>();
        m_shortcutActions.put(ACTION_DELETE, new Runnable() {

            public void run() {

                if (!m_fileTable.getSelectedIds().isEmpty()) {
                    I_CmsDialogContext context1 = createDialogContext();
                    context1.start("Delete", new CmsDeleteDialog(context1));
                }
            }
        });

        m_shortcutActions.put(ACTION_FOLDER_UP, new Runnable() {

            public void run() {

                showParentFolder();
            }
        });

        m_shortcutActions.put(ACTION_PROPERTIES, new Runnable() {

            public void run() {

                I_CmsWorkplaceAction propAction = new CmsPropertiesDialogAction();
                I_CmsDialogContext context = createDialogContext();
                if (propAction.getVisibility(A_CmsUI.getCmsObject(), context.getResources()).isActive()) {
                    propAction.executeAction(context);
                }
            }
        });

        m_shortcutActions.put(ACTION_RENAME, new Runnable() {

            public void run() {

                if ((m_fileTable.getSelectedIds().size() == 1)
                    && isPropertyEditable(CmsResourceTableProperty.PROPERTY_RESOURCE_NAME)) {
                    CmsUUID id = m_fileTable.getSelectedIds().iterator().next();
                    editItemProperty(id, CmsResourceTableProperty.PROPERTY_RESOURCE_NAME);
                }
            }
        });

        m_shortcutActions.put(ACTION_SELECT_ALL, new Runnable() {

            public void run() {

                m_fileTable.selectAll();
            }
        });

        m_fileTable = new CmsFileTable();
        m_fileTable.setSizeFull();
        m_fileTable.setMenuBuilder(new MenuBuilder());
        m_fileTable.addItemClickListener(new ItemClickListener() {

            private static final long serialVersionUID = 1L;

            public void itemClick(ItemClickEvent event) {

                handleFileItemClick(event);
            }
        });

        m_uploadArea = new CmsUploadAreaExtension(m_fileTable);
        m_uploadArea.addUploadListener(new I_UploadListener() {

            public void onUploadFinished(List<String> uploadedFiles) {

                updateAll();
            }
        });
        m_treeContainer = new HierarchicalContainer();
        addTreeContainerProperties(
            CmsResourceTableProperty.PROPERTY_RESOURCE_NAME,
            CmsResourceTableProperty.PROPERTY_STATE,
            CmsResourceTableProperty.PROPERTY_TYPE_ICON_RESOURCE,
            CmsResourceTableProperty.PROPERTY_INSIDE_PROJECT);
        m_fileTree = new Tree();
        m_fileTree.addStyleName(OpenCmsTheme.SIMPLE_DRAG);
        m_fileTree.addStyleName(OpenCmsTheme.FULL_WIDTH_PADDING);
        m_fileTree.setWidth("100%");
        m_fileTree.setContainerDataSource(m_treeContainer);
        m_fileTree.setItemIconPropertyId(CmsResourceTableProperty.PROPERTY_TYPE_ICON_RESOURCE);
        m_fileTree.setItemCaptionPropertyId(CmsResourceTableProperty.PROPERTY_RESOURCE_NAME);
        m_fileTree.addExpandListener(new ExpandListener() {

            private static final long serialVersionUID = 1L;

            public void nodeExpand(ExpandEvent event) {

                readTreeLevel((CmsUUID)event.getItemId());
            }
        });
        m_fileTree.addCollapseListener(new CollapseListener() {

            private static final long serialVersionUID = 1L;

            public void nodeCollapse(CollapseEvent event) {

                clearTreeLevel((CmsUUID)event.getItemId());
            }
        });

        m_fileTree.addItemClickListener(new ItemClickListener() {

            private static final long serialVersionUID = 1L;

            public void itemClick(ItemClickEvent event) {

                readFolder((CmsUUID)event.getItemId());
            }
        });

        m_fileTree.setItemStyleGenerator(new ItemStyleGenerator() {

            private static final long serialVersionUID = 1L;

            public String getStyle(Tree source, Object itemId) {

                return CmsFileTable.getStateStyle(source.getContainerDataSource().getItem(itemId));
            }
        });
        m_fileTree.setNullSelectionAllowed(false);

        // init drag and drop
        ExplorerDropHandler handler = new ExplorerDropHandler();
        m_fileTable.setDropHandler(handler);
        m_fileTable.setDragMode(TableDragMode.MULTIROW);
        m_fileTree.setDropHandler(handler);
        m_fileTree.setDragMode(TreeDragMode.NONE);

        m_siteSelector = createSiteSelect(A_CmsUI.getCmsObject());
        m_infoPath = new TextField();
        A_CmsFocusShortcutListener shortcutListener = new A_CmsFocusShortcutListener("Open path", KeyCode.ENTER, null) {

            private static final long serialVersionUID = 1L;

            @Override
            public void blur(BlurEvent event) {

                super.blur(event);
                showCrumbs(true);
            }

            @Override
            public void focus(FocusEvent event) {

                super.focus(event);
                showCrumbs(false);
            }

            @Override
            public void handleAction(Object sender, Object target) {

                openPath(m_infoPath.getValue());
            }
        };
        shortcutListener.installOn(m_infoPath);

        m_crumbs = new CssLayout();
        m_crumbs.setPrimaryStyleName(OpenCmsTheme.CRUMBS);
        m_crumbListener = new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                openPath((String)event.getButton().getData());
            }
        };

        m_searchField = new TextField();
        m_searchField.setIcon(FontOpenCms.FILTER);
        m_searchField.setInputPrompt(
            Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_FILTER_0));
        m_searchField.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        m_searchField.addTextChangeListener(new TextChangeListener() {

            private static final long serialVersionUID = 1L;

            public void textChange(TextChangeEvent event) {

                filterTable(event.getText());

            }
        });

        m_openedPaths = (Map<String, String>)UI.getCurrent().getSession().getAttribute(OPENED_PATHS);
        if (m_openedPaths == null) {
            m_openedPaths = new HashMap<String, String>();
        }
    }

    /**
     * @see com.vaadin.navigator.ViewChangeListener#afterViewChange(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
     */
    public void afterViewChange(ViewChangeEvent event) {

        // nothing to do

    }

    /**
     * @see com.vaadin.navigator.ViewChangeListener#beforeViewChange(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
     */
    public boolean beforeViewChange(ViewChangeEvent event) {

        OpenCms.getWorkplaceAppManager().storeAppSettings(
            A_CmsUI.getCmsObject(),
            CmsFileExplorerSettings.class,
            m_fileTable.getTableSettings());
        UI.getCurrent().getSession().setAttribute(OPENED_PATHS, m_openedPaths);
        return true;
    }

    /**
     * Changes to the given site and path.<p>
     *
     * @param siteRoot the site root
     * @param path the path inside the site
     */
    public void changeSite(String siteRoot, String path) {

        changeSite(siteRoot, path, false);

    }

    /**
     * Switches to the requested site.<p>
     *
     * @param siteRoot the site root
     * @param path the folder path to open
     * @param force force the path change, even if we are currently in the same site
     */
    public void changeSite(String siteRoot, String path, boolean force) {

        CmsObject cms = A_CmsUI.getCmsObject();
        if (!cms.getRequestContext().getSiteRoot().equals(siteRoot) || force) {
            cms.getRequestContext().setSiteRoot(siteRoot);
            populateFolderTree();
            openPath(path);
        }
    }

    /**
     * Returns the current folder id.<p>
     *
     * @return the current folder structure id
     */
    public CmsUUID getCurrentFolder() {

        return m_currentFolder;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsHasShortcutActions#getShortcutActions()
     */
    public Map<Action, Runnable> getShortcutActions() {

        return m_shortcutActions;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceApp#initUI(org.opencms.ui.apps.I_CmsAppUIContext)
     */
    public void initUI(I_CmsAppUIContext context) {

        m_appContext = context;
        HorizontalSplitPanel sp = new HorizontalSplitPanel();
        sp.setSizeFull();
        sp.setFirstComponent(m_fileTree);
        CmsFileExplorerSettings settings;
        try {
            settings = OpenCms.getWorkplaceAppManager().getAppSettings(
                A_CmsUI.getCmsObject(),
                CmsFileExplorerSettings.class);

            m_fileTable.setTableState(settings);
        } catch (Exception e) {
            LOG.error("Error while reading file explorer settings from user.", e);
        }
        sp.setSecondComponent(m_fileTable);
        sp.setSplitPosition(400 - 1, Unit.PIXELS);

        context.setAppContent(sp);
        context.showInfoArea(true);
        HorizontalLayout inf = new HorizontalLayout();
        inf.setSizeFull();
        inf.setSpacing(true);
        inf.setMargin(true);
        m_siteSelector.setWidth("379px");
        inf.addComponent(m_siteSelector);
        CssLayout crumbWrapper = new CssLayout();
        crumbWrapper.setSizeFull();
        crumbWrapper.setPrimaryStyleName(OpenCmsTheme.CRUMB_WRAPPER);
        crumbWrapper.addComponent(m_crumbs);

        m_infoPath.setWidth("100%");
        crumbWrapper.addComponent(m_infoPath);
        inf.addComponent(crumbWrapper);
        inf.setExpandRatio(crumbWrapper, 1);

        m_searchField.setWidth("200px");
        inf.addComponent(m_searchField);
        context.setAppInfo(inf);

        initToolbarButtons(context);
        populateFolderTree();
    }

    /**
     * Triggered when the user clicks the 'publsh' button.<p>
     */
    public void onClickPublish() {

        CmsGwtDialogExtension extension = new CmsGwtDialogExtension(A_CmsUI.get(), new I_CmsUpdateListener<String>() {

            public void onUpdate(List<String> updatedItems) {

                updateAll();
            }

        });
        extension.openPublishDialog(A_CmsUI.getCmsObject().getRequestContext().getCurrentProject());
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceApp#onStateChange(java.lang.String)
     */
    public void onStateChange(String state) {

        state = normalizeState(state);
        if ((m_currentState == null) || !m_currentState.equals(state)) {
            m_currentState = state;
            CmsObject cms = A_CmsUI.getCmsObject();
            String siteRoot = getSiteRootFromState();
            String path = getPathFromState();
            if ((siteRoot != null) && !siteRoot.equals(cms.getRequestContext().getSiteRoot())) {
                changeSite(siteRoot, path);
                m_siteSelector.select(siteRoot);
            }
            openPath(path);
        }
    }

    /**
     * @see org.opencms.ui.components.I_CmsWindowCloseListener#onWindowClose()
     */
    public void onWindowClose() {

        OpenCms.getWorkplaceAppManager().storeAppSettings(
            A_CmsUI.getCmsObject(),
            CmsFileExplorerSettings.class,
            m_fileTable.getTableSettings());
        UI.getCurrent().getSession().setAttribute(OPENED_PATHS, m_openedPaths);
    }

    /**
     * Fills the file table with the resources from the given path.<p>
     *
     * @param sitePath a folder site path
     */
    public void populateFileTable(String sitePath) {

        CmsObject cms = A_CmsUI.getCmsObject();
        m_searchField.clear();
        try {
            List<CmsResource> folderResources = cms.readResources(sitePath, FILES_N_FOLDERS, false);
            m_fileTable.fillTable(cms, folderResources);
        } catch (CmsException e) {
            CmsErrorDialog.showErrorDialog(e);
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Populates the folder tree.<p>
     */
    public void populateFolderTree() {

        CmsObject cms = A_CmsUI.getCmsObject();
        m_treeContainer.removeAllItems();
        try {
            CmsResource siteRoot = cms.readResource("/", FOLDERS);
            addTreeItem(cms, siteRoot, null, m_treeContainer);
            List<CmsResource> folderResources = cms.readResources("/", FOLDERS, false);
            for (CmsResource resource : folderResources) {
                addTreeItem(cms, resource, siteRoot.getStructureId(), m_treeContainer);
            }
            m_fileTree.expandItem(siteRoot.getStructureId());
        } catch (CmsException e) {
            CmsErrorDialog.showErrorDialog(e);
            LOG.error(e.getLocalizedMessage(), e);
        }

    }

    /**
     * Updates the table entries with the given ids.<p>
     *
     * @param ids the ids of the table entries to update
     */
    public void update(Collection<CmsUUID> ids) {

        try {
            CmsResource currentFolderRes = A_CmsUI.getCmsObject().readResource(m_currentFolder, CmsResourceFilter.ALL);
            for (CmsUUID id : ids) {
                boolean remove = false;
                try {
                    CmsResource resource = A_CmsUI.getCmsObject().readResource(id, CmsResourceFilter.ALL);

                    remove = !CmsResource.getParentFolder(resource.getRootPath()).equals(
                        currentFolderRes.getRootPath());

                } catch (CmsVfsResourceNotFoundException e) {
                    remove = true;
                    LOG.debug("Could not read update resource " + id, e);
                }
                m_fileTable.update(id, remove);
                updateTree(id, remove);

            }
            m_fileTable.clearSelection();
        } catch (CmsException e) {
            CmsErrorDialog.showErrorDialog(e);
        }
    }

    /**
     * Updates display for all contents of the current folder.<p>
     */
    public void updateAll() {

        readFolder(m_currentFolder);
    }

    /**
     * Updates the give tree item.<p>
     *
     * @param cms the cms context
     * @param id the item id
     */
    public void updateResourceInTree(CmsObject cms, CmsUUID id) {

        try {
            CmsResource resource = cms.readResource(id, CmsResourceFilter.IGNORE_EXPIRATION);
            if (resource.isFile()) {
                return;
            }

            CmsResource parent = cms.readParentFolder(id);
            CmsUUID parentId = parent.getStructureId();
            Item resourceItem = m_treeContainer.getItem(id);
            if (resourceItem != null) {
                // use the root path as name in case of the root item
                resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_RESOURCE_NAME).setValue(
                    parentId == null ? resource.getRootPath() : resource.getName());
                resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_STATE).setValue(resource.getState());
                if (parentId != null) {
                    m_treeContainer.setParent(resource.getStructureId(), parentId);
                }
            } else {
                addTreeItem(cms, resource, parentId, m_treeContainer);
            }
        } catch (CmsVfsResourceNotFoundException e) {
            m_treeContainer.removeItemRecursively(id);
            LOG.debug(e.getLocalizedMessage(), e);
        } catch (CmsException e) {
            CmsErrorDialog.showErrorDialog(e);
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Updates the tree items with the given ids.<p>
     *
     * @param id the
     * @param remove if true, only remove the tree entry
     */
    public void updateTree(CmsUUID id, boolean remove) {

        CmsObject cms = A_CmsUI.getCmsObject();
        updateResourceInTree(cms, id);

    }

    /**
     * Clears the given tree level.<p>
     *
     * @param parentId the parent id
     */
    protected void clearTreeLevel(CmsUUID parentId) {

        // create a new list to avoid concurrent modifications
        Collection<?> children = m_treeContainer.getChildren(parentId);
        // may be null when monkey clicking
        if (children != null) {
            List<Object> childIds = new ArrayList<Object>(m_treeContainer.getChildren(parentId));
            for (Object childId : childIds) {
                m_treeContainer.removeItemRecursively(childId);
            }
        }
    }

    /**
     * Creates the dialog context for dialogs opened from the context menu.<p>
     *
     * @return the dialog context
     */
    protected I_CmsDialogContext createDialogContext() {

        return new CmsExplorerDialogContext(m_appContext, this, m_fileTable.getSelectedResources());
    }

    /**
     * Edits the given property for the requested item.<p>
     *
     * @param itemId the item id
     * @param propertyId the property id
     */
    protected void editItemProperty(CmsUUID itemId, CmsResourceTableProperty propertyId) {

        new ContextMenuEditHandler(itemId, propertyId).start();
    }

    /**
     * Checks whether the given property is editable.<p>
     *
     * @param propertyId the property id
     *
     * @return <code>true</code> if the given property is editable
     */
    protected boolean isPropertyEditable(CmsResourceTableProperty propertyId) {

        return String.class.equals(propertyId.getColumnType())
            && ((propertyId == CmsResourceTableProperty.PROPERTY_RESOURCE_NAME)
                || (propertyId == CmsResourceTableProperty.PROPERTY_TITLE)
                || (propertyId == CmsResourceTableProperty.PROPERTY_NAVIGATION_TEXT))
            && m_fileTable.isColumnVisible(propertyId);
    }

    /**
     * Reads the given folder.<p>
     *
     * @param folderId the folder id
     */
    protected void readFolder(CmsUUID folderId) {

        CmsObject cms = A_CmsUI.getCmsObject();
        m_searchField.clear();
        try {
            CmsResource folder = cms.readResource(folderId, FOLDERS);
            m_currentFolder = folderId;
            setPathInfo(cms.getSitePath(folder));
            List<CmsResource> childResources = cms.readResources(cms.getSitePath(folder), FILES_N_FOLDERS, false);
            m_fileTable.fillTable(cms, childResources);
            boolean hasFolderChild = false;
            for (CmsResource child : childResources) {
                if (child.isFolder()) {
                    hasFolderChild = true;
                    break;
                }
            }
            m_treeContainer.setChildrenAllowed(folderId, hasFolderChild);
            String sitePath = folder.getRootPath().equals(cms.getRequestContext().getSiteRoot() + "/")
            ? ""
            : cms.getSitePath(folder);

            String state = normalizeState(cms.getRequestContext().getSiteRoot() + STATE_SEPARATOR + sitePath);

            if (!(state).equals(m_currentState)) {
                m_currentState = state;
                CmsAppWorkplaceUi.get().changeCurrentAppState(m_currentState);
            }
            m_openedPaths.put(cms.getRequestContext().getSiteRoot(), sitePath);
            m_uploadButton.setTargetFolder(folder.getRootPath());
            m_uploadArea.setTargetFolder(folder.getRootPath());
            if (!m_fileTree.isExpanded(folderId)) {
                expandCurrentFolder();
            }
            if (!m_fileTree.isSelected(folderId)) {
                m_fileTree.select(folderId);
            }
        } catch (CmsException e) {
            CmsErrorDialog.showErrorDialog(e);
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Reads the given tree level.<p>
     *
     * @param parentId the parent id
     */
    protected void readTreeLevel(CmsUUID parentId) {

        CmsObject cms = A_CmsUI.getCmsObject();
        try {
            CmsResource parent = cms.readResource(parentId, FOLDERS);
            List<CmsResource> folderResources = cms.readResources(cms.getSitePath(parent), FOLDERS, false);

            // sets the parent to leaf mode, in case no child folders are present
            m_treeContainer.setChildrenAllowed(parentId, !folderResources.isEmpty());

            for (CmsResource resource : folderResources) {
                addTreeItem(cms, resource, parentId, m_treeContainer);
            }
            m_fileTree.markAsDirtyRecursive();
        } catch (CmsException e) {
            CmsErrorDialog.showErrorDialog(e);
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Expands the currently viewed folder in the tree.<p>
     */
    void expandCurrentFolder() {

        if (m_currentFolder != null) {
            m_treeContainer.setChildrenAllowed(m_currentFolder, true);
            m_fileTree.expandItem(m_currentFolder);
        }
    }

    /**
     * Filters the file table.<p>
     *
     * @param search the search term
     */
    void filterTable(String search) {

        m_fileTable.filterTable(search);
    }

    /**
     * Handles the file table item click.<p>
     *
     * @param event the click event
     */
    void handleFileItemClick(ItemClickEvent event) {

        if (m_fileTable.isEditing()) {
            m_fileTable.stopEdit();

        } else if (!event.isCtrlKey() && !event.isShiftKey()) {
            // don't interfere with multi-selection using control key
            if (event.getButton().equals(MouseButton.RIGHT)) {
                m_fileTable.handleSelection((CmsUUID)event.getItemId());
                m_fileTable.openContextMenu(event);
            } else {
                if ((event.getPropertyId() == null)
                    || CmsResourceTableProperty.PROPERTY_TYPE_ICON.equals(event.getPropertyId())) {
                    m_fileTable.openContextMenu(event);
                } else if (CmsResourceTableProperty.PROPERTY_RESOURCE_NAME.equals(event.getPropertyId())) {
                    Boolean isFolder = (Boolean)event.getItem().getItemProperty(
                        CmsResourceTableProperty.PROPERTY_IS_FOLDER).getValue();
                    if ((isFolder != null) && isFolder.booleanValue()) {
                        expandCurrentFolder();
                        if (m_fileTree.getItem(event.getItemId()) != null) {
                            m_fileTree.select(event.getItemId());
                        }
                        readFolder((CmsUUID)event.getItemId());

                    } else {
                        CmsUUID id = (CmsUUID)event.getItemId();
                        try {
                            CmsObject cms = A_CmsUI.getCmsObject();
                            CmsResource res = cms.readResource(id, CmsResourceFilter.IGNORE_EXPIRATION);
                            String link = OpenCms.getLinkManager().substituteLink(cms, res);
                            A_CmsUI.get().getPage().open(link, CmsAppWorkplaceUi.EDITOR_WINDOW_NAME);
                        } catch (CmsVfsResourceNotFoundException e) {
                            LOG.info(e.getLocalizedMessage(), e);
                        } catch (CmsException e) {
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Opens the new resource dialog.<p>
     */
    void onClickNew() {

        try {
            CmsObject cms = A_CmsUI.getCmsObject();

            CmsResource folderRes = cms.readResource(m_currentFolder, CmsResourceFilter.IGNORE_EXPIRATION);
            I_CmsDialogContext newDialogContext = createDialogContext();

            CmsNewDialog newDialog = new CmsNewDialog(folderRes, newDialogContext);
            newDialogContext.start(
                CmsVaadinUtils.getMessageText(org.opencms.ui.Messages.GUI_NEWRESOURCEDIALOG_TITLE_0),
                newDialog);

        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Opens the given site path.<p>
     *
     * @param path the path
     */
    void openPath(String path) {

        if (path == null) {
            path = m_openedPaths.get(A_CmsUI.getCmsObject().getRequestContext().getSiteRoot());
            if (path == null) {
                path = "";
            }
        }
        String[] pathItems = path.split("/");
        Collection<?> rootItems = m_treeContainer.rootItemIds();
        if (rootItems.size() != 1) {
            throw new RuntimeException("Illeagal state, folder tree has " + rootItems.size() + " children");
        }
        CmsUUID folderId = (CmsUUID)rootItems.iterator().next();
        Collection<?> children = m_treeContainer.getChildren(folderId);
        for (int i = 0; i < pathItems.length; i++) {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(pathItems[i])) {
                continue;
            }
            CmsUUID level = null;
            for (Object id : children) {
                if (m_treeContainer.getItem(id).getItemProperty(
                    CmsResourceTableProperty.PROPERTY_RESOURCE_NAME).getValue().equals(pathItems[i])) {
                    level = (CmsUUID)id;
                    m_fileTree.expandItem(level);
                    break;
                }
            }
            if ((level == null) || level.equals(folderId)) {
                break;
            }
            folderId = level;
            children = m_treeContainer.getChildren(folderId);
        }
        readFolder(folderId);
    }

    /**
     * Shows and hides the path bread crumb.<p>
     *
     * @param show <code>true</code> to show the bread crumb
     */
    void showCrumbs(boolean show) {

        if (show) {
            m_crumbs.removeStyleName(OpenCmsTheme.HIDDEN);
        } else {
            m_crumbs.addStyleName(OpenCmsTheme.HIDDEN);
        }
    }

    /**
     * Shows the parent folder, if available.<p>
     */
    void showParentFolder() {

        CmsUUID parentId = (CmsUUID)m_treeContainer.getParent(m_currentFolder);
        if (parentId != null) {
            readFolder(parentId);
            m_fileTree.select(parentId);
        }
    }

    /**
     * Adds the given properties to the tree container.<p>
     *
     * @param properties the properties tom add
     */
    private void addTreeContainerProperties(CmsResourceTableProperty... properties) {

        for (CmsResourceTableProperty property : properties) {
            m_treeContainer.addContainerProperty(property, property.getColumnType(), property.getDefaultValue());
        }
    }

    /**
     * Adds an item to the folder tree.<p>
     *
     * @param cms the cms context
     * @param resource the folder resource
     * @param parentId the parent folder id
     * @param container the data container
     */
    private void addTreeItem(CmsObject cms, CmsResource resource, CmsUUID parentId, HierarchicalContainer container) {

        Item resourceItem = container.getItem(resource.getStructureId());
        if (resourceItem == null) {
            resourceItem = container.addItem(resource.getStructureId());
        }
        // use the root path as name in case of the root item
        resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_RESOURCE_NAME).setValue(
            parentId == null ? resource.getRootPath() : resource.getName());
        resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_STATE).setValue(resource.getState());
        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(resource);
        CmsResourceUtil resUtil = new CmsResourceUtil(cms, resource);
        resourceItem.getItemProperty(PROPERTY_INSIDE_PROJECT).setValue(Boolean.valueOf(resUtil.isInsideProject()));
        CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(type.getTypeName());
        resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_TYPE_ICON_RESOURCE).setValue(
            new ExternalResource(CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES + settings.getBigIcon())));
        if (parentId != null) {
            container.setParent(resource.getStructureId(), parentId);
        }
    }

    /**
     * Creates the site selector combo box.<p>
     *
     * @param cms the current cms context
     *
     * @return the combo box
     */
    private ComboBox createSiteSelect(CmsObject cms) {

        final IndexedContainer availableSites = CmsVaadinUtils.getAvailableSitesContainer(cms, SITE_CAPTION);
        ComboBox combo = new ComboBox(null, availableSites);
        combo.setTextInputAllowed(true);
        combo.setNullSelectionAllowed(false);
        combo.setWidth("200px");
        combo.setInputPrompt(
            Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_CLICK_TO_EDIT_0));
        combo.setItemCaptionPropertyId(SITE_CAPTION);
        combo.select(cms.getRequestContext().getSiteRoot());
        combo.setFilteringMode(FilteringMode.CONTAINS);
        combo.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                String value = (String)event.getProperty().getValue();
                if (availableSites.containsId(value)) {
                    changeSite(value, null);
                    availableSites.removeAllContainerFilters();
                }
            }
        });
        return combo;
    }

    /**
     * Returns the site path from the current state.<p>
     *
     * @return the site path
     */
    private String getPathFromState() {

        String path = null;
        if (m_currentState.contains(STATE_SEPARATOR)) {
            path = m_currentState.substring(m_currentState.indexOf(STATE_SEPARATOR) + STATE_SEPARATOR.length());
        }
        return path;
    }

    /**
     * Returns the site root from the current state.<p>
     *
     * @return the site root
     */
    private String getSiteRootFromState() {

        String siteRoot = null;
        if (m_currentState.contains(STATE_SEPARATOR)) {
            siteRoot = m_currentState.substring(0, m_currentState.indexOf(STATE_SEPARATOR));
        }
        return siteRoot;
    }

    /**
     * Initializes the app specific toolbar buttons.<p>
     *
     * @param context the UI context
     */
    private void initToolbarButtons(I_CmsAppUIContext context) {

        Button publishButton = CmsToolBar.createButton(FontOpenCms.PUBLISH);
        publishButton.addClickListener(new ClickListener() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                onClickPublish();
            }
        });
        context.addToolbarButton(publishButton);
        Button newButton = CmsToolBar.createButton(FontOpenCms.WAND);
        newButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                onClickNew();
            }

        });
        context.addToolbarButton(newButton);

        m_uploadButton = new CmsUploadButton(FontOpenCms.UPLOAD, "/");
        m_uploadButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        m_uploadButton.addStyleName(OpenCmsTheme.TOOLBAR_BUTTON);
        m_uploadButton.addUploadListener(new I_UploadListener() {

            public void onUploadFinished(List<String> uploadedFiles) {

                updateAll();
            }
        });
        context.addToolbarButton(m_uploadButton);
    }

    /**
     * Normalizes the state string. Ensuring it starts with the right number of slashes resolving an issue with the vaadin state handling.<p>
     *
     * @param state the state string
     *
     * @return the normalized state string
     */
    private String normalizeState(String state) {

        String result = "";
        if (state.contains(STATE_SEPARATOR)) {
            if (!state.startsWith(STATE_SEPARATOR) && !state.startsWith("/")) {
                // in case the site root part is not empty, it should start with a slash
                result = "/" + state;
            } else {
                result = state;
                // make sure to remove excessive slashes, may get introduced through vaadin state handling
                while (result.startsWith("//")) {
                    result = result.substring(1);
                }
            }
        }
        return result;
    }

    /**
     * Sets the current path info.<p>
     *
     * @param path the path
     */
    private void setPathInfo(String path) {

        m_infoPath.setValue(path);

        // generate the path bread crumb
        m_crumbs.removeAllComponents();
        int i = path.indexOf("/");
        String openPath = "";
        while (i >= 0) {
            String fragment = path.substring(0, i + 1);
            openPath += fragment;
            path = path.substring(i + 1);
            i = path.indexOf("/");
            Button crumb = new Button(fragment, m_crumbListener);
            crumb.setData(openPath);
            crumb.addStyleName(ValoTheme.BUTTON_LINK);
            m_crumbs.addComponent(crumb);
        }
    }
}
