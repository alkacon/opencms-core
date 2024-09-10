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

package org.opencms.ui.apps;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.site.CmsSiteManagerImpl;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.I_CmsDialogContext.ContextType;
import org.opencms.ui.I_CmsUpdateListener;
import org.opencms.ui.actions.CmsCopyDialogAction;
import org.opencms.ui.actions.CmsPropertiesDialogAction;
import org.opencms.ui.actions.I_CmsWorkplaceAction;
import org.opencms.ui.components.A_CmsFocusShortcutListener;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsExtendedSiteSelector;
import org.opencms.ui.components.CmsFileTable;
import org.opencms.ui.components.CmsResourceIcon;
import org.opencms.ui.components.CmsResourceTableProperty;
import org.opencms.ui.components.CmsToolBar;
import org.opencms.ui.components.CmsUploadButton;
import org.opencms.ui.components.CmsUploadButton.I_UploadListener;
import org.opencms.ui.components.I_CmsWindowCloseListener;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.components.extensions.CmsUploadAreaExtension;
import org.opencms.ui.contextmenu.CmsResourceContextMenuBuilder;
import org.opencms.ui.dialogs.CmsCopyMoveDialog;
import org.opencms.ui.dialogs.CmsDeleteDialog;
import org.opencms.ui.dialogs.CmsNewDialog;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.vaadin.event.Action;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.ServerSideCriterion;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.data.util.BeanItemContainer;
import com.vaadin.v7.data.util.HierarchicalContainer;
import com.vaadin.v7.event.FieldEvents.TextChangeEvent;
import com.vaadin.v7.event.FieldEvents.TextChangeListener;
import com.vaadin.v7.event.ItemClickEvent;
import com.vaadin.v7.event.ItemClickEvent.ItemClickListener;
import com.vaadin.v7.shared.ui.combobox.FilteringMode;
import com.vaadin.v7.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.Table.TableDragMode;
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.Tree;
import com.vaadin.v7.ui.Tree.CollapseEvent;
import com.vaadin.v7.ui.Tree.CollapseListener;
import com.vaadin.v7.ui.Tree.ExpandEvent;
import com.vaadin.v7.ui.Tree.ExpandListener;
import com.vaadin.v7.ui.Tree.ItemStyleGenerator;
import com.vaadin.v7.ui.Tree.TreeDragMode;

/**
 * The file explorer app.<p>
 */
@SuppressWarnings("deprecation")
public class CmsFileExplorer
implements I_CmsWorkplaceApp, I_CmsCachableApp, ViewChangeListener, I_CmsWindowCloseListener, I_CmsHasShortcutActions,
I_CmsContextProvider, CmsFileTable.I_FolderSelectHandler {

    /** The drop handler for copy/move operations. */
    public class ExplorerDropHandler implements DropHandler {

        /** The serial version id. */
        private static final long serialVersionUID = 5392136127699472654L;

        /** The copy move action. */
        transient final I_CmsWorkplaceAction m_copyMoveAction = new CmsCopyDialogAction();

        /**
         * @see com.vaadin.event.dd.DropHandler#drop(com.vaadin.event.dd.DragAndDropEvent)
         */
        public void drop(DragAndDropEvent dragEvent) {

            try {
                CmsExplorerDialogContext context = getContext(dragEvent);
                if (m_copyMoveAction.isActive(context)) {
                    CmsCopyMoveDialog dialog = new CmsCopyMoveDialog(
                        context,
                        CmsCopyMoveDialog.DialogMode.copy_and_move);
                    dialog.setTargetFolder(getTargetId(dragEvent));
                    context.start(
                        CmsVaadinUtils.getMessageText(org.opencms.ui.Messages.GUI_DIALOGTITLE_COPYMOVE_0),
                        dialog);
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
                        if (!m_copyMoveAction.isActive(getContext(dragEvent))) {
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
                Object itemOverId = target.getItemIdOver();
                if (itemOverId instanceof CmsUUID) {
                    targetId = (CmsUUID)itemOverId;
                } else if (itemOverId instanceof String) {
                    targetId = m_fileTable.getUUIDFromItemID((String)itemOverId);
                }
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
                result = cms.hasPermissions(
                    target,
                    CmsPermissionSet.ACCESS_WRITE,
                    false,
                    CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
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
                CmsUUID sourceId = m_fileTable.getUUIDFromItemID((String)dragEvent.getTransferable().getData("itemId"));
                CmsResource source = cms.readResource(sourceId, CmsResourceFilter.IGNORE_EXPIRATION);
                resources = Collections.singletonList(source);
            }
            CmsExplorerDialogContext context = new CmsExplorerDialogContext(
                ContextType.fileTable,
                m_fileTable,
                CmsFileExplorer.this,
                resources);
            return context;
        }
    }

    /**
     * File tree expand listener.<p>
     */
    public class TreeExpandListener implements ExpandListener {

        /** The serial version id. */
        private static final long serialVersionUID = 1L;
        /**
         * The path fragment being opened.
         * Will override folder visibility in case the the target path is visible to the user.
         **/
        private String m_openPathFragment;

        /**
         * @see com.vaadin.ui.Tree.ExpandListener#nodeExpand(com.vaadin.ui.Tree.ExpandEvent)
         */
        public void nodeExpand(ExpandEvent event) {

            selectTreeItem((CmsUUID)event.getItemId());
            readTreeLevel((CmsUUID)event.getItemId(), m_openPathFragment);
        }

        /**
         * Sets the open path fragment.<p>
         *
         * @param openPathFragment the open path fragment
         */
        public void setOpenPathFragment(String openPathFragment) {

            m_openPathFragment = openPathFragment;
        }
    }

    /** Bean representing the file explorer navigation substate. */
    static class StateBean {

        /** Current folder. */
        private String m_folder;

        /** Project id. */
        private String m_projectId;

        /**selected resource.*/
        private String m_selectedResource;

        /** The site root. */
        private String m_siteRoot;

        /**
         * Creates a new state bean.<p>
         *
         * @param siteRoot the site root
         * @param folder the folder
         * @param projectId the project id
         */
        public StateBean(String siteRoot, String folder, String projectId) {

            m_siteRoot = siteRoot;
            m_folder = folder;
            m_projectId = projectId;
            if ("".equals(m_siteRoot)) {
                m_siteRoot = "/";
            }
        }

        /**
         * Parses the state bean from a string.<p>
         *
         * @param state the state string
         * @return the state bean
         */
        public static StateBean parse(String state) {

            List<String> fields = CmsStringUtil.splitAsList(state, A_CmsWorkplaceApp.PARAM_SEPARATOR);
            if (fields.size() >= 3) {
                String projectId = fields.get(0);
                String siteRoot = fields.get(1);
                String folder = fields.get(2);
                StateBean ret = new StateBean(siteRoot, folder, projectId);
                if (fields.size() == 4) {
                    ret.setSelectedResource(fields.get(3));
                }
                return ret;
            } else {
                return new StateBean(null, null, null);
            }
        }

        /**
         * Converts state bean to a string.<p>
         *
         * @return the string format of the state
         */
        public String asString() {

            String result = m_projectId
                + A_CmsWorkplaceApp.PARAM_SEPARATOR
                + m_siteRoot
                + A_CmsWorkplaceApp.PARAM_SEPARATOR
                + m_folder
                + A_CmsWorkplaceApp.PARAM_SEPARATOR;
            return result;
        }

        /**
         * Returns the folderId.<p>
         *
         * @return the folderId
         */
        public String getFolder() {

            return m_folder;
        }

        /**
         * Returns the projectId.<p>
         *
         * @return the projectId
         */
        public String getProjectId() {

            return m_projectId;
        }

        /**
         * Returns the resource to select, empty if no one was set.<p>
         *
         * @return UUID as string
         */
        public String getSelectedResource() {

            return m_selectedResource == null ? "" : m_selectedResource;
        }

        /**
         * Returns the siteRoot.<p>
         *
         * @return the siteRoot
         */
        public String getSiteRoot() {

            return m_siteRoot;
        }

        /**
         * Sets a resource to be selected.<p>
         *
         * @param resource to get selected
         */
        public void setSelectedResource(String resource) {

            m_selectedResource = resource;
        }
    }

    /** The file explorer attribute key. */
    public static final String ATTR_KEY = "CmsFileExplorer";

    /** The in line editable resource properties. */
    public static final Collection<CmsResourceTableProperty> INLINE_EDIT_PROPERTIES = Arrays.asList(
        CmsResourceTableProperty.PROPERTY_RESOURCE_NAME,
        CmsResourceTableProperty.PROPERTY_TITLE,
        CmsResourceTableProperty.PROPERTY_NAVIGATION_TEXT,
        CmsResourceTableProperty.PROPERTY_COPYRIGHT,
        CmsResourceTableProperty.PROPERTY_CACHE);

    /** The initial split position between folder tree and file table. */
    public static final int LAYOUT_SPLIT_POSITION = 399;

    /** The opened paths session attribute name. */
    public static final String OPENED_PATHS = "explorer-opened-paths";

    /** Site selector caption property. */
    public static final String SITE_CAPTION = "site_caption";

    /** Site selector site root property. */
    public static final String SITE_ROOT = "site_root";

    /** Threshold for updating the complete folder after file changes. */
    public static final int UPDATE_FOLDER_THRESHOLD = 200;

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

    /** The select all shortcut, (using Apple CMD as modifier). */
    private static final Action ACTION_SELECT_ALL_CMD = new ShortcutAction(
        "CMD+A",
        ShortcutAction.KeyCode.A,
        new int[] {ShortcutAction.ModifierKey.META});

    /** The switch online shortcut. */
    private static final Action ACTION_SWITCH_ONLINE = new ShortcutAction(
        "Ctrl+O",
        ShortcutAction.KeyCode.O,
        new int[] {ShortcutAction.ModifierKey.CTRL});

    /** The switch online shortcut, (using Apple CMD as modifier). */
    private static final Action ACTION_SWITCH_ONLINE_CMD = new ShortcutAction(
        "CMD+O",
        ShortcutAction.KeyCode.O,
        new int[] {ShortcutAction.ModifierKey.META});

    /** The files and folder resource filter. */
    private static final CmsResourceFilter FILES_N_FOLDERS = CmsResourceFilter.ONLY_VISIBLE;

    /** The folders resource filter. */
    private static final CmsResourceFilter FOLDERS = CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireFolder();

    /** The serial version id. */
    private static final long serialVersionUID = 1L;

    /** The UI context. */
    protected I_CmsAppUIContext m_appContext;

    /** Saved explorer state used by dialogs after they have finished. */
    protected String m_savedExplorerState = "";

    /** The table containing the contents of the current folder. */
    CmsFileTable m_fileTable;

    /** The info path. */
    com.vaadin.ui.TextField m_infoPath;

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

    /** The tree expand listener. */
    private TreeExpandListener m_expandListener;

    /** The folder tree. */
    private Tree m_fileTree;

    /** The first visible file table item index. */
    private int m_firstVisibleTableItemIndex;

    /** The last context menu resources. */
    private List<CmsResource> m_lastDialogContextResources;

    /** The quick launch location cache. */
    private CmsQuickLaunchLocationCache m_locationCache;

    /** The new button. */
    private Button m_newButton;

    /** The publish button. */
    private Button m_publishButton;

    /** The search field. */
    private TextField m_searchField;

    /** the currently selected file tree folder, may be null. */
    private CmsUUID m_selectTreeFolder;

    /** The site selector. */
    private ComboBox m_siteSelector;

    /** Button for uploading to folders with special upload actions. */
    private Button m_specialUploadButton;

    /** The folder tree data container. */
    private HierarchicalContainer m_treeContainer;

    /** Upload action for the current folder. */
    private String m_uploadAction;

    /** The upload drop area extension. */
    private CmsUploadAreaExtension m_uploadArea;

    /** The upload button. */
    private CmsUploadButton m_uploadButton;

    /** The current upload folder. */
    private CmsResource m_uploadFolder;

    /**
     * Constructor.<p>
     */
    public CmsFileExplorer() {

        m_shortcutActions = new HashMap<Action, Runnable>();
        m_shortcutActions.put(ACTION_DELETE, new Runnable() {

            public void run() {

                if (!m_fileTable.getSelectedIds().isEmpty()) {
                    I_CmsDialogContext context1 = getDialogContext();
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
                I_CmsDialogContext context = getDialogContext();
                if (propAction.getVisibility(context).isActive()) {
                    propAction.executeAction(context);
                }
            }
        });

        m_shortcutActions.put(ACTION_RENAME, new Runnable() {

            public void run() {

                CmsExplorerDialogContext context = getDialogContext();
                if (context.isPropertyEditable(CmsResourceTableProperty.PROPERTY_RESOURCE_NAME)) {
                    context.editProperty(CmsResourceTableProperty.PROPERTY_RESOURCE_NAME);
                }
            }
        });
        Runnable selectAll = new Runnable() {

            public void run() {

                m_fileTable.selectAll();
            }
        };
        m_shortcutActions.put(ACTION_SELECT_ALL, selectAll);
        m_shortcutActions.put(ACTION_SELECT_ALL_CMD, selectAll);

        Runnable switchOnline = new Runnable() {

            public void run() {

                toggleOnlineOffline();
            }
        };
        m_shortcutActions.put(ACTION_SWITCH_ONLINE, switchOnline);
        m_shortcutActions.put(ACTION_SWITCH_ONLINE_CMD, switchOnline);

        m_fileTable = new CmsFileTable(this);
        m_fileTable.setSizeFull();
        m_fileTable.setMenuBuilder(new CmsResourceContextMenuBuilder());
        m_fileTable.setFolderSelectHandler(this);
        m_uploadArea = new CmsUploadAreaExtension(m_fileTable);
        m_uploadArea.addUploadListener(new I_UploadListener() {

            public void onUploadFinished(List<String> uploadedFiles) {

                updateAll(true);
            }
        });
        m_treeContainer = new HierarchicalContainer();
        addTreeContainerProperties(
            CmsResourceTableProperty.PROPERTY_RESOURCE_NAME,
            CmsResourceTableProperty.PROPERTY_STATE,
            CmsResourceTableProperty.PROPERTY_TREE_CAPTION,
            CmsResourceTableProperty.PROPERTY_INSIDE_PROJECT,
            CmsResourceTableProperty.PROPERTY_RELEASED_NOT_EXPIRED,
            CmsResourceTableProperty.PROPERTY_DISABLED);
        m_fileTree = new Tree();
        m_fileTree.addStyleName(OpenCmsTheme.SIMPLE_DRAG);
        m_fileTree.addStyleName(OpenCmsTheme.FULL_WIDTH_PADDING);
        m_fileTree.setWidth("100%");
        m_fileTree.setContainerDataSource(m_treeContainer);
        //            m_fileTree.setItemIconPropertyId(CmsResourceTableProperty.PROPERTY_TYPE_ICON_RESOURCE);
        m_fileTree.setItemCaptionPropertyId(CmsResourceTableProperty.PROPERTY_TREE_CAPTION);
        //        m_fileTree.setCaptionAsHtml(true);
        m_fileTree.setHtmlContentAllowed(true);
        m_expandListener = new TreeExpandListener();
        m_fileTree.addExpandListener(m_expandListener);
        m_fileTree.addCollapseListener(new CollapseListener() {

            private static final long serialVersionUID = 1L;

            public void nodeCollapse(CollapseEvent event) {

                selectTreeItem((CmsUUID)event.getItemId());
                clearTreeLevel((CmsUUID)event.getItemId());
            }
        });

        m_fileTree.addItemClickListener(new ItemClickListener() {

            private static final long serialVersionUID = 1L;

            public void itemClick(ItemClickEvent event) {

                handleFileTreeClick(event);
            }
        });

        m_fileTree.setItemStyleGenerator(new ItemStyleGenerator() {

            private static final long serialVersionUID = 1L;

            public String getStyle(Tree source, Object itemId) {

                return CmsFileTable.getStateStyle(source.getContainerDataSource().getItem(itemId));
            }
        });
        m_fileTree.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                handleFileTreeValueChange();
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
        m_infoPath = new com.vaadin.ui.TextField();
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

        m_locationCache = CmsQuickLaunchLocationCache.getLocationCache(CmsAppWorkplaceUi.get().getHttpSession());
        String startSite = CmsWorkplace.getStartSiteRoot(
            A_CmsUI.getCmsObject(),
            CmsAppWorkplaceUi.get().getWorkplaceSettings());
        // remove trailing slashes
        while (startSite.endsWith("/")) {
            startSite = startSite.substring(0, startSite.length() - 1);
        }
        if (m_locationCache.getFileExplorerLocation(startSite) == null) {
            // add the configured start folder for the start site
            String startFolder = CmsAppWorkplaceUi.get().getWorkplaceSettings().getUserSettings().getStartFolder();
            m_locationCache.setFileExplorerLocation(startSite, startFolder);
        }
    }

    /**
     * @see com.vaadin.navigator.ViewChangeListener#afterViewChange(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
     */
    public void afterViewChange(ViewChangeEvent event) {

        m_fileTable.setFirstVisibleItemIndex(m_firstVisibleTableItemIndex);
    }

    /**
     * @see com.vaadin.navigator.ViewChangeListener#beforeViewChange(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
     */
    public boolean beforeViewChange(ViewChangeEvent event) {

        m_firstVisibleTableItemIndex = m_fileTable.getFirstVisibleItemIndex();

        OpenCms.getWorkplaceAppManager().storeAppSettings(
            A_CmsUI.getCmsObject(),
            CmsFileExplorerSettings.class,
            m_fileTable.getTableSettings());
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
        String currentSiteRoot = cms.getRequestContext().getSiteRoot();
        if (force || !currentSiteRoot.equals(siteRoot) || (path != null)) {
            CmsAppWorkplaceUi.get().changeSite(siteRoot);
            if (path == null) {
                path = m_locationCache.getFileExplorerLocation(siteRoot);
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(siteRoot)
                    && ((path == null) || !path.startsWith("/system"))) {
                    // switching to the root site and previous root site folder is not below /system
                    // -> stay in the current folder
                    path = m_locationCache.getFileExplorerLocation(currentSiteRoot);
                    if (path != null) {
                        path = CmsStringUtil.joinPaths(currentSiteRoot, path);
                    }
                }
            }
            openPath(path, true);
            Container container = m_siteSelector.getContainerDataSource();
            CmsExtendedSiteSelector.SiteSelectorOption optionToSelect = null;
            for (Object id : container.getItemIds()) {
                CmsExtendedSiteSelector.SiteSelectorOption option = (CmsExtendedSiteSelector.SiteSelectorOption)id;
                if ((option != null) && CmsStringUtil.comparePaths(option.getSite(), siteRoot)) {
                    optionToSelect = option;
                    break;
                }
            }
            m_siteSelector.select(optionToSelect);
        }
    }

    /**
     * Clears the file table selection.
     */
    public void clearSelection() {

        m_fileTable.clearSelection();
    }

    /**
     * Gets all ids of resources in current folder.<p>
     *
     * @return the
     */
    public List<CmsUUID> getAllIds() {

        return m_fileTable.getAllIds();
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
     * @see org.opencms.ui.apps.I_CmsContextProvider#getDialogContext()
     */
    public CmsExplorerDialogContext getDialogContext() {

        List<CmsResource> resources = m_fileTable.getSelectedResources();
        m_lastDialogContextResources = resources;
        CmsExplorerDialogContext context = new CmsExplorerDialogContext(
            ContextType.fileTable,
            m_fileTable,
            this,
            resources);
        context.setEditableProperties(INLINE_EDIT_PROPERTIES);
        return context;
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
        m_appContext.setAttribute(ATTR_KEY, this);
        m_appContext.setMenuDialogContext(
            new CmsExplorerDialogContext(ContextType.appToolbar, m_fileTable, this, null));
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

        sp.setSplitPosition(LAYOUT_SPLIT_POSITION, Unit.PIXELS);

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
        try {
            JavaScript.getCurrent().execute(
                new String(
                    CmsFileUtil.readFully(getClass().getResourceAsStream("update-crumb-wrapper-parent.js")),
                    StandardCharsets.UTF_8));
        } catch (IOException e) {}
        m_fileTable.updateColumnWidths(A_CmsUI.get().getPage().getBrowserWindowWidth() - LAYOUT_SPLIT_POSITION);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsCachableApp#isCachable()
     */
    public boolean isCachable() {

        return true;
    }

    /**
     * @see org.opencms.ui.components.CmsFileTable.I_FolderSelectHandler#onFolderSelect(org.opencms.util.CmsUUID)
     */
    public void onFolderSelect(CmsUUID itemId) {

        expandCurrentFolder();
        if (m_fileTree.getItem(itemId) != null) {
            m_fileTree.select(itemId);
        }
        try {
            readFolder(itemId);
        } catch (CmsException e) {
            CmsErrorDialog.showErrorDialog(
                CmsVaadinUtils.getMessageText(Messages.ERR_EXPLORER_CAN_NOT_READ_RESOURCE_1, itemId),
                e);
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * @see org.opencms.ui.apps.I_CmsCachableApp#onRestoreFromCache()
     */
    public void onRestoreFromCache() {

        if (m_lastDialogContextResources != null) {
            List<CmsUUID> updateIds = Lists.newArrayList();
            for (CmsResource resource : m_lastDialogContextResources) {
                updateIds.add(resource.getStructureId());
            }
            update(updateIds);
        }

    }

    /**
     * Call if site and or project have been changed.<p>
     *
     * @param project the project
     * @param siteRoot the site root
     */
    public void onSiteOrProjectChange(CmsProject project, String siteRoot) {

        if ((siteRoot != null) && !siteRoot.equals(getSiteRootFromState())) {
            changeSite(siteRoot, null, true);
        } else if ((project != null) && !project.getUuid().equals(getProjectIdFromState())) {
            openPath(getPathFromState(), true);
        }
        m_appContext.updateOnChange();
        setToolbarButtonsEnabled(!CmsAppWorkplaceUi.isOnlineProject());
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

            CmsUUID projectId = getProjectIdFromState();
            if ((projectId != null) && !cms.getRequestContext().getCurrentProject().getUuid().equals(projectId)) {
                try {
                    CmsProject project = cms.readProject(projectId);
                    cms.getRequestContext().setCurrentProject(project);
                    CmsAppWorkplaceUi.get().getWorkplaceSettings().setProject(project.getUuid());
                } catch (CmsException e) {
                    LOG.warn("Error reading project from history state", e);
                }
                changeSite(siteRoot, path, true);
            } else if ((siteRoot != null)
                && !CmsStringUtil.comparePaths(siteRoot, cms.getRequestContext().getSiteRoot())) {
                String saveState = m_currentState;
                changeSite(siteRoot, path);
                if (!getSelectionFromState(saveState).isEmpty()) {
                    m_fileTable.setValue(Collections.singleton(getSelectionFromState(saveState)));
                }
            } else {
                String saveState = m_currentState;
                openPath(path, true);
                if (!getSelectionFromState(saveState).isEmpty()) {
                    m_fileTable.setValue(Collections.singleton(getSelectionFromState(saveState)));
                }
            }
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
    }

    /**
     * Fills the file table with the resources from the given path.<p>
     *
     * @param sitePath a folder site path
     */
    public void populateFileTable(String sitePath) {

        CmsObject cms = A_CmsUI.getCmsObject();
        m_searchField.clear();
        m_firstVisibleTableItemIndex = 0;
        try {
            List<CmsResource> folderResources = cms.readResources(sitePath, FILES_N_FOLDERS, false);
            m_fileTable.fillTable(cms, folderResources);
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
            boolean updateFolder = m_fileTable.getItemCount() < UPDATE_FOLDER_THRESHOLD;
            Set<CmsUUID> removeIds = new HashSet<CmsUUID>();
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
                if (remove) {
                    removeIds.add(id);
                }

            }
            for (CmsUUID id : ids) {
                updateTree(id);
            }
            if (updateFolder) {
                updateCurrentFolder(removeIds);
            } else {
                m_fileTable.update(removeIds, true);
                HashSet<CmsUUID> updateIds = new HashSet<CmsUUID>(ids);
                ids.removeAll(removeIds);
                m_fileTable.update(updateIds, false);
            }
            m_fileTable.updateSorting();
            m_fileTable.clearSelection();
        } catch (CmsException e) {
            CmsErrorDialog.showErrorDialog(e);
        }
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_searchField.getValue())) {
            filterTable(m_searchField.getValue());
        }
    }

    /**
     * Updates display for all contents of the current folder.<p>
     *
     * @param clearFilter <code>true</code> to clear the search filter
     */
    public void updateAll(boolean clearFilter) {

        try {
            readFolder(m_currentFolder, clearFilter);
            Set<Object> ids = Sets.newHashSet();
            ids.addAll(m_fileTree.getItemIds());
            for (Object id : ids) {
                updateTree((CmsUUID)id);
            }
        } catch (CmsException e) {
            CmsErrorDialog.showErrorDialog(
                CmsVaadinUtils.getMessageText(Messages.ERR_EXPLORER_CAN_NOT_READ_RESOURCE_1, m_currentFolder),
                e);
            LOG.error(e.getLocalizedMessage(), e);
        }
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
            // ensuring to only add items belonging to the current site
            if (!resource.getRootPath().startsWith(cms.getRequestContext().getSiteRoot())) {
                m_treeContainer.removeItemRecursively(id);
                return;
            }

            CmsResource parent = cms.readParentFolder(id);
            CmsUUID parentId = null;
            if (parent != null) {
                parentId = parent.getStructureId();
            } else {
                LOG.debug("Parent for resource '" + resource.getRootPath() + "' is null");
            }
            Item resourceItem = m_treeContainer.getItem(id);
            if (resourceItem != null) {
                // use the root path as name in case of the root item
                String resName = parentId == null ? resource.getRootPath() : resource.getName();
                resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_RESOURCE_NAME).setValue(resName);
                CmsResourceUtil resUtil = new CmsResourceUtil(cms, resource);
                String iconHTML = CmsResourceIcon.getTreeCaptionHTML(resName, resUtil, null, false);
                resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_TREE_CAPTION).setValue(iconHTML);
                resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_STATE).setValue(resource.getState());
                if (parentId != null) {
                    m_treeContainer.setParent(resource.getStructureId(), parentId);
                }
            } else {
                addTreeItem(cms, resource, parentId, false, m_treeContainer);
            }
            m_fileTree.markAsDirty();
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
     */
    public void updateTree(CmsUUID id) {

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
     * Reads the given folder.<p>
     *
     * @param folderId the folder id
     *
     * @throws CmsException in case reading the folder fails
     */
    protected void readFolder(CmsUUID folderId) throws CmsException {

        readFolder(folderId, true);
    }

    /**
     * Reads the given folder.<p>
     *
     * @param folderId the folder id
     * @param clearFilter <code>true</code> to clear the search filter
     *
     * @throws CmsException in case reading the folder fails
     */
    protected void readFolder(CmsUUID folderId, boolean clearFilter) throws CmsException {

        CmsObject cms = A_CmsUI.getCmsObject();
        if (clearFilter) {
            m_searchField.clear();
        }
        CmsResource folder = cms.readResource(folderId, FOLDERS);
        m_currentFolder = folderId;
        String folderPath = cms.getSitePath(folder);
        if (OpenCms.getSiteManager().isSharedFolder(cms.getRequestContext().getSiteRoot())) {
            folderPath = folderPath.substring(cms.getRequestContext().getSiteRoot().length());
        }
        setPathInfo(folderPath);
        List<CmsResource> childResources = cms.readResources(folder, FILES_N_FOLDERS, false);
        m_fileTable.fillTable(cms, childResources, clearFilter);
        boolean hasFolderChild = false;
        for (CmsResource child : childResources) {
            if (child.isFolder()) {
                hasFolderChild = true;
                break;
            }
        }
        m_treeContainer.setChildrenAllowed(folderId, hasFolderChild);
        String sitePath = folder.getRootPath().equals(cms.getRequestContext().getSiteRoot() + "/") ? "" : folderPath;
        String state = new StateBean(
            cms.getRequestContext().getSiteRoot(),
            sitePath,
            cms.getRequestContext().getCurrentProject().getUuid().toString()).asString();
        if (LOG.isDebugEnabled()) {
            String p = RandomStringUtils.randomAlphanumeric(5) + " readFolder ";
            LOG.debug(p + "siteRoot = " + cms.getRequestContext().getSiteRoot());
            LOG.debug(p + "folder = " + folder.getRootPath());
            LOG.debug(p + "folderPath = " + folderPath);
            LOG.debug(p + "sitePath = " + sitePath);
            LOG.debug(p + "state = " + state);
            LOG.debug(p + "m_currentState = " + m_currentState);
            LOG.debug(p + "m_currentState.asString = " + StateBean.parse(m_currentState).asString());
        }
        if ((m_currentState == null) || !(state).equals(StateBean.parse(m_currentState).asString())) {
            m_currentState = state;
            CmsAppWorkplaceUi.get().changeCurrentAppState(m_currentState);
        }
        m_locationCache.setFileExplorerLocation(cms.getRequestContext().getSiteRoot(), sitePath);
        updateUploadButton(folder);
        if (!m_fileTree.isExpanded(folderId)) {
            expandCurrentFolder();
        }
        if (!m_fileTree.isSelected(folderId)) {
            m_fileTree.select(folderId);
        }
    }

    /**
     * Updates the current folder and removes the given resource items.<p>
     *
     * @param removeIds the resource item ids to remove
     */
    protected void updateCurrentFolder(Collection<CmsUUID> removeIds) {

        m_fileTable.update(removeIds, true);
        CmsObject cms = A_CmsUI.getCmsObject();
        try {
            // current folder may be filtered, so we clear the filters and restore them later
            // to make updates work for filtered out resources
            m_fileTable.saveFilters();
            m_fileTable.clearFilters();
            CmsResource folder = cms.readResource(m_currentFolder, FOLDERS);
            List<CmsResource> childResources = cms.readResources(cms.getSitePath(folder), FILES_N_FOLDERS, false);
            Set<CmsUUID> ids = new HashSet<CmsUUID>();
            for (CmsResource child : childResources) {
                ids.add(child.getStructureId());
            }
            m_fileTable.update(ids, false);
        } catch (CmsException e) {
            CmsErrorDialog.showErrorDialog(e);
            LOG.error(e.getLocalizedMessage(), e);
        } finally {
            m_fileTable.restoreFilters();
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
     * Handles the file tree click events.<p>
     *
     * @param event the event
     */
    void handleFileTreeClick(ItemClickEvent event) {

        Item resourceItem = m_treeContainer.getItem(event.getItemId());
        if ((resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_DISABLED).getValue() == null)
            || !((Boolean)resourceItem.getItemProperty(
                CmsResourceTableProperty.PROPERTY_DISABLED).getValue()).booleanValue()) {
            // don't handle disabled item clicks
            try {
                readFolder((CmsUUID)event.getItemId());
            } catch (CmsException e) {
                CmsErrorDialog.showErrorDialog(
                    CmsVaadinUtils.getMessageText(Messages.ERR_EXPLORER_CAN_NOT_READ_RESOURCE_1, event.getItemId()),
                    e);
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Handles the file tree value changes.<p>
     */
    void handleFileTreeValueChange() {

        Item resourceItem = m_treeContainer.getItem(m_fileTree.getValue());
        if (resourceItem != null) {
            if ((resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_DISABLED).getValue() != null)
                && ((Boolean)resourceItem.getItemProperty(
                    CmsResourceTableProperty.PROPERTY_DISABLED).getValue()).booleanValue()) {
                // in case the folder is disabled due to missing visibility, reset the value to the previous one
                m_fileTree.setValue(m_selectTreeFolder);

            } else {
                m_selectTreeFolder = (CmsUUID)m_fileTree.getValue();
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
            I_CmsDialogContext newDialogContext = getDialogContext();

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

        openPath(path, false);
    }

    /**
     * Opens the given site path.<p>
     *
     * @param path the path
     * @param initTree <code>true</code> in case the tree needs to be initialized
     */
    void openPath(String path, boolean initTree) {

        CmsObject cms = A_CmsUI.getCmsObject();
        if (path == null) {
            String siteRoot = cms.getRequestContext().getSiteRoot();
            path = m_locationCache.getFileExplorerLocation(siteRoot);
            if (path == null) {
                path = "";
            } else if (OpenCms.getSiteManager().startsWithShared(path)) {
                path = path.substring(siteRoot.length());
            }
        }

        CmsSiteManagerImpl siteManager = OpenCms.getSiteManager();
        if (siteManager.isSharedFolder(cms.getRequestContext().getSiteRoot()) && siteManager.startsWithShared(path)) {
            // siteManager.getSharedFolder() has a trailing slash - we want to cut off the shared folder, but keep a leading slash in path
            path = path.substring(siteManager.getSharedFolder().length() - 1);
            if ("".equals(path)) {
                path = "/";
            }
        }

        boolean existsPath = CmsStringUtil.isNotEmptyOrWhitespaceOnly(path)
            && cms.existsResource(path, FILES_N_FOLDERS);

        if (path.startsWith("/")) {
            // remove a leading slash to avoid an empty first path fragment
            path = path.substring(1);
        }

        String[] pathItems = path.split("/");

        if (initTree) {
            try {
                initFolderTree(existsPath);
            } catch (CmsException e) {
                CmsErrorDialog.showErrorDialog(
                    CmsVaadinUtils.getMessageText(Messages.ERR_EXPLORER_CAN_NOT_OPEN_PATH_1, path),
                    e);
                LOG.error(e.getLocalizedMessage(), e);
                return;
            }
        }

        Collection<?> rootItems = m_treeContainer.rootItemIds();
        if (rootItems.size() != 1) {
            throw new RuntimeException("Illeagal state, folder tree has " + rootItems.size() + " children");
        }

        CmsUUID folderId = (CmsUUID)rootItems.iterator().next();
        if (initTree) {
            if (existsPath) {
                m_expandListener.setOpenPathFragment(pathItems[0]);
            }
            m_fileTree.expandItem(folderId);
            m_expandListener.setOpenPathFragment(null);
        }
        Collection<?> children = m_treeContainer.getChildren(folderId);
        for (int i = 0; i < pathItems.length; i++) {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(pathItems[i])) {
                continue;
            }
            CmsUUID level = null;
            if (children != null) {
                for (Object id : children) {
                    if (m_treeContainer.getItem(id).getItemProperty(
                        CmsResourceTableProperty.PROPERTY_RESOURCE_NAME).getValue().equals(pathItems[i])) {
                        level = (CmsUUID)id;
                        m_expandListener.setOpenPathFragment(
                            existsPath && (i < (pathItems.length - 1)) ? pathItems[i + 1] : null);
                        m_fileTree.expandItem(level);
                        m_expandListener.setOpenPathFragment(null);
                        break;
                    }
                }
            }
            if ((level == null) || level.equals(folderId)) {
                break;
            }
            folderId = level;
            children = m_treeContainer.getChildren(folderId);
        }
        try {
            readFolder(folderId);
        } catch (CmsException e) {
            CmsErrorDialog.showErrorDialog(
                CmsVaadinUtils.getMessageText(Messages.ERR_EXPLORER_CAN_NOT_OPEN_PATH_1, path),
                e);
            LOG.error(e.getLocalizedMessage(), e);
            return;
        }
    }

    /**
     * Reads the given tree level.<p>
     *
     * @param parentId the parent id
     * @param openPathFragment the open path fragment
     */
    void readTreeLevel(CmsUUID parentId, String openPathFragment) {

        CmsObject cms = A_CmsUI.getCmsObject();
        boolean openedFragment = false;
        try {
            CmsResource parent = cms.readResource(parentId, CmsResourceFilter.IGNORE_EXPIRATION);
            String folderPath = cms.getSitePath(parent);
            List<CmsResource> folderResources = cms.readResources(folderPath, FOLDERS, false);

            // sets the parent to leaf mode, in case no child folders are present
            m_treeContainer.setChildrenAllowed(parentId, !folderResources.isEmpty());

            for (CmsResource resource : folderResources) {
                addTreeItem(cms, resource, parentId, false, m_treeContainer);
                openedFragment = openedFragment || resource.getName().equals(openPathFragment);
            }
            if (!openedFragment && (openPathFragment != null)) {
                CmsResource resource = cms.readResource(
                    CmsStringUtil.joinPaths(folderPath, openPathFragment),
                    CmsResourceFilter.IGNORE_EXPIRATION);
                addTreeItem(cms, resource, parentId, true, m_treeContainer);
            }

            m_fileTree.markAsDirtyRecursive();
        } catch (CmsException e) {
            CmsErrorDialog.showErrorDialog(e);
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Selects the item with the given id.<p>
     *
     * @param itemId the item id
     */
    void selectTreeItem(CmsUUID itemId) {

        m_fileTree.select(itemId);
    }

    /**
     * Shows and hides the path bread crumb.<p>
     *
     * @param show <code>true</code> to show the bread crumb
     */
    void showCrumbs(boolean show) {

        if (show) {
            m_crumbs.removeStyleName(OpenCmsTheme.HIDDEN);
            CmsAppWorkplaceUi.get().enableGlobalShortcuts();
        } else {
            m_crumbs.addStyleName(OpenCmsTheme.HIDDEN);
            CmsAppWorkplaceUi.get().disableGlobalShortcuts();
        }
    }

    /**
     * Shows the parent folder, if available.<p>
     */
    void showParentFolder() {

        CmsUUID parentId = (CmsUUID)m_treeContainer.getParent(m_currentFolder);
        if (parentId != null) {
            Item resourceItem = m_treeContainer.getItem(parentId);
            if ((resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_DISABLED).getValue() == null)
                || !((Boolean)resourceItem.getItemProperty(
                    CmsResourceTableProperty.PROPERTY_DISABLED).getValue()).booleanValue()) {
                // don't open disabled parent folders
                try {
                    readFolder(parentId);
                    m_fileTree.select(parentId);
                } catch (CmsException e) {
                    CmsErrorDialog.showErrorDialog(
                        CmsVaadinUtils.getMessageText(Messages.ERR_EXPLORER_CAN_NOT_READ_RESOURCE_1, parentId),
                        e);
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    /**
     * Toggles between the online and the offline project.<p>
     */
    void toggleOnlineOffline() {

        CmsObject cms = A_CmsUI.getCmsObject();
        CmsProject targetProject = null;
        if (cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            targetProject = A_CmsUI.get().getLastOfflineProject();
            if (targetProject == null) {
                CmsUserSettings userSettings = new CmsUserSettings(cms);
                try {
                    CmsProject project = cms.readProject(userSettings.getStartProject());
                    if (!project.isOnlineProject()
                        && OpenCms.getOrgUnitManager().getAllAccessibleProjects(
                            cms,
                            project.getOuFqn(),
                            false).contains(project)) {
                        targetProject = project;
                    }
                } catch (CmsException e) {
                    LOG.debug("Error reading user start project.", e);
                }
                if (targetProject == null) {
                    List<CmsProject> availableProjects = CmsVaadinUtils.getAvailableProjects(cms);
                    for (CmsProject project : availableProjects) {
                        if (!project.isOnlineProject()) {
                            targetProject = project;
                            break;
                        }
                    }
                }
            }
        } else {
            try {
                targetProject = cms.readProject(CmsProject.ONLINE_PROJECT_ID);
            } catch (CmsException e) {
                LOG.debug("Error reading online project.", e);
            }
        }
        if (targetProject != null) {
            A_CmsUI.get().changeProject(targetProject);
            onSiteOrProjectChange(targetProject, null);
            Notification.show(
                CmsVaadinUtils.getMessageText(Messages.GUI_SWITCHED_TO_PROJECT_1, targetProject.getName()));
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
     * @param disabled in case the item is disabled, used for non visible parent folders
     * @param container the data container
     */
    private void addTreeItem(
        CmsObject cms,
        CmsResource resource,
        CmsUUID parentId,
        boolean disabled,
        HierarchicalContainer container) {

        CmsResourceUtil resUtil = new CmsResourceUtil(cms, resource);
        Item resourceItem = container.getItem(resource.getStructureId());
        if (resourceItem == null) {
            resourceItem = container.addItem(resource.getStructureId());
        }

        // use the root path as name in case of the root item
        String resName = parentId == null ? resource.getRootPath() : resource.getName();
        resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_RESOURCE_NAME).setValue(resName);
        resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_STATE).setValue(resource.getState());

        resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_INSIDE_PROJECT).setValue(
            Boolean.valueOf(resUtil.isInsideProject()));
        resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_RELEASED_NOT_EXPIRED).setValue(
            Boolean.valueOf(resUtil.isReleasedAndNotExpired()));
        String iconHTML = CmsResourceIcon.getTreeCaptionHTML(resName, resUtil, null, false);

        resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_TREE_CAPTION).setValue(iconHTML);
        if (disabled) {
            resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_DISABLED).setValue(Boolean.TRUE);
        }
        if (parentId != null) {
            container.setChildrenAllowed(parentId, true);
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

        final List<CmsExtendedSiteSelector.SiteSelectorOption> sites = CmsExtendedSiteSelector.getExplorerSiteSelectorOptions(
            cms,
            true);
        final BeanItemContainer<CmsExtendedSiteSelector.SiteSelectorOption> container = new BeanItemContainer<>(
            CmsExtendedSiteSelector.SiteSelectorOption.class);
        for (CmsExtendedSiteSelector.SiteSelectorOption option : sites) {
            container.addItem(option);
        }
        ComboBox combo = new ComboBox(null, container);
        combo.setTextInputAllowed(true);
        combo.setNullSelectionAllowed(false);
        combo.setWidth("200px");
        combo.setInputPrompt(
            Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_CLICK_TO_EDIT_0));
        combo.setItemCaptionPropertyId("label");
        combo.select(new CmsExtendedSiteSelector.SiteSelectorOption(cms.getRequestContext().getSiteRoot(), null, null));
        combo.setFilteringMode(FilteringMode.CONTAINS);
        combo.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            /** Flag used to temporarily disable the standard listener behavior, used when we change the combo box's value programatically. */
            private boolean m_disabled;

            public void valueChange(ValueChangeEvent event) {

                if (m_disabled) {
                    return;
                }
                CmsExtendedSiteSelector.SiteSelectorOption option = (CmsExtendedSiteSelector.SiteSelectorOption)(event.getProperty().getValue());
                if (container.containsId(option)) {
                    changeSite(option.getSite(), option.getPath());
                    m_appContext.updateOnChange();
                    container.removeAllContainerFilters();
                    if (option.getPath() != null) {
                        try {
                            m_disabled = true;
                            combo.select(new CmsExtendedSiteSelector.SiteSelectorOption(option.getSite(), null, null));
                        } finally {
                            m_disabled = false;
                        }
                    }
                }
            }
        });
        if (Page.getCurrent().getBrowserWindowHeight() > 650) {
            combo.setPageLength(20);
        }
        return combo;
    }

    /**
     * Returns the site path from the current state.<p>
     *
     * @return the site path
     */
    private String getPathFromState() {

        return StateBean.parse(m_currentState).getFolder();
    }

    /**
     * Returns the project id from the current state.<p>
     *
     * @return the project id
     */
    private CmsUUID getProjectIdFromState() {

        String projectIdStr = StateBean.parse(m_currentState).getProjectId();
        if (CmsUUID.isValidUUID(projectIdStr)) {
            return new CmsUUID(projectIdStr);

        }
        return null;

    }

    /**
     * Returns selected resource UUID from state (empty if not set).<p>
     *
     * @param state current state
     * @return uuid as string
     */
    private String getSelectionFromState(String state) {

        return StateBean.parse(state).getSelectedResource();
    }

    /**
     * Returns the site root from the current state.<p>
     *
     * @return the site root
     */
    private String getSiteRootFromState() {

        String siteRoot = StateBean.parse(m_currentState).getSiteRoot();
        return siteRoot;
    }

    /**
     * Populates the folder tree.<p>
     *
     * @param showInvisible to show non visible root folder as disabled
     *
     * @throws CmsException in case reading the root folder fails
     */
    private void initFolderTree(boolean showInvisible) throws CmsException {

        CmsObject cms = A_CmsUI.getCmsObject();
        m_treeContainer.removeAllItems();
        try {
            CmsResource siteRoot = cms.readResource("/", FOLDERS);
            addTreeItem(cms, siteRoot, null, false, m_treeContainer);
        } catch (CmsException e) {
            if (showInvisible) {
                CmsResource siteRoot = cms.readResource("/", CmsResourceFilter.IGNORE_EXPIRATION);
                addTreeItem(cms, siteRoot, null, true, m_treeContainer);
            } else {
                throw e;
            }
        }

    }

    /**
     * Initializes the app specific toolbar buttons.<p>
     *
     * @param context the UI context
     */
    private void initToolbarButtons(I_CmsAppUIContext context) {

        m_publishButton = context.addPublishButton(new I_CmsUpdateListener<String>() {

            public void onUpdate(List<String> updatedItems) {

                updateAll(false);
            }

        });

        m_newButton = CmsToolBar.createButton(
            FontOpenCms.WAND,
            CmsVaadinUtils.getMessageText(Messages.GUI_NEW_RESOURCE_TITLE_0));
        if (CmsAppWorkplaceUi.isOnlineProject()) {
            m_newButton.setEnabled(false);
            m_newButton.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_TOOLBAR_NOT_AVAILABLE_ONLINE_0));
        }
        m_newButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                onClickNew();
            }

        });

        context.addToolbarButton(m_newButton);

        m_uploadButton = new CmsUploadButton(FontOpenCms.UPLOAD, "/");
        m_specialUploadButton = new Button(FontOpenCms.UPLOAD);
        m_uploadButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        m_uploadButton.addStyleName(OpenCmsTheme.TOOLBAR_BUTTON);
        if (CmsAppWorkplaceUi.isOnlineProject()) {
            m_uploadButton.setEnabled(false);
            m_specialUploadButton.setEnabled(false);
            m_uploadButton.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_TOOLBAR_NOT_AVAILABLE_ONLINE_0));
            m_specialUploadButton.setDescription(
                CmsVaadinUtils.getMessageText(Messages.GUI_TOOLBAR_NOT_AVAILABLE_ONLINE_0));
        } else {
            m_uploadButton.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_UPLOAD_BUTTON_TITLE_0));
            m_specialUploadButton.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_UPLOAD_BUTTON_TITLE_0));
        }
        m_uploadButton.addUploadListener(new I_UploadListener() {

            public void onUploadFinished(List<String> uploadedFiles) {

                updateAll(true);
            }
        });
        m_specialUploadButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        m_specialUploadButton.addStyleName(OpenCmsTheme.TOOLBAR_BUTTON);
        m_specialUploadButton.addClickListener(event -> {
            try {
                @SuppressWarnings("unchecked")
                Class<I_CmsWorkplaceAction> actionClass = (Class<I_CmsWorkplaceAction>)getClass().getClassLoader().loadClass(
                    m_uploadAction);
                I_CmsWorkplaceAction action = actionClass.newInstance();
                List<CmsResource> resources = new ArrayList<>();
                resources.add(m_uploadFolder);
                CmsExplorerDialogContext uploadContext = new CmsExplorerDialogContext(
                    ContextType.fileTable,
                    m_fileTable,
                    this,
                    resources);
                action.executeAction(uploadContext);
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
                CmsErrorDialog.showErrorDialog(e);
            }

        });

        context.addToolbarButton(m_uploadButton);
        context.addToolbarButton(m_specialUploadButton);
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
        if (state.contains(A_CmsWorkplaceApp.PARAM_SEPARATOR)) {
            result = state;
            while (result.startsWith(CmsAppWorkplaceUi.WORKPLACE_STATE_SEPARATOR)) {
                result = result.substring(1);
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

    /**
     * Sets the toolbar buttons enabled.<p>
     *
     * @param enabled the enabled flag
     */
    private void setToolbarButtonsEnabled(boolean enabled) {

        m_publishButton.setEnabled(enabled);
        m_newButton.setEnabled(enabled);
        m_uploadButton.setEnabled(enabled);
        m_specialUploadButton.setEnabled(enabled);
        if (enabled) {
            m_publishButton.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_PUBLISH_BUTTON_TITLE_0));
            m_newButton.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_NEW_RESOURCE_TITLE_0));
            m_uploadButton.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_UPLOAD_BUTTON_TITLE_0));
            m_specialUploadButton.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_UPLOAD_BUTTON_TITLE_0));
        } else {
            m_publishButton.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_TOOLBAR_NOT_AVAILABLE_ONLINE_0));
            m_newButton.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_TOOLBAR_NOT_AVAILABLE_ONLINE_0));
            m_uploadButton.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_TOOLBAR_NOT_AVAILABLE_ONLINE_0));
            m_specialUploadButton.setDescription(
                CmsVaadinUtils.getMessageText(Messages.GUI_TOOLBAR_NOT_AVAILABLE_ONLINE_0));
        }
    }

    /**
     * Updates the upload button state based on the current folder.
     *
     * @param folder the current folder
     */
    private void updateUploadButton(CmsResource folder) {

        String uploadAction = null;
        m_uploadFolder = folder;
        try {
            uploadAction = OpenCms.getResourceManager().getResourceType(folder).getConfiguration().get(
                "gallery.upload.action");
        } catch (Exception e) {
            // Resource type not found?
            LOG.error(e.getLocalizedMessage(), e);
        }
        m_uploadAction = uploadAction;
        if (uploadAction != null) {
            m_uploadButton.setVisible(false);
            m_specialUploadButton.setVisible(true);
            m_uploadArea.setTargetFolder(null);
        } else {
            boolean enabled = OpenCms.getWorkplaceManager().getUploadRestriction().getUploadRestrictionInfo(
                A_CmsUI.getCmsObject()).isUploadEnabled(folder.getRootPath());
            m_uploadButton.setVisible(enabled);
            m_specialUploadButton.setVisible(false);
            m_uploadButton.setTargetFolder(folder.getRootPath());
            m_uploadArea.setTargetFolder(folder.getRootPath());
        }
    }
}
