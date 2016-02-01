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

package org.opencms.ui.components;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsDialogContext;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsUserIconHelper;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.apps.CmsAppVisibilityStatus;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.CmsDefaultAppButtonProvider;
import org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsUploadButton.I_UploadListener;
import org.opencms.ui.contextmenu.CmsContextMenuTreeBuilder;
import org.opencms.ui.contextmenu.I_CmsContextMenuItem;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsTreeNode;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontIcon;
import com.vaadin.server.Resource;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.UI;
import com.vaadin.ui.declarative.Design;
import com.vaadin.ui.themes.ValoTheme;

/**
 * The workplace toolbar.<p>
 */
public class CmsToolBar extends CssLayout {

    /** Toolbar dialog context. */
    protected static class ToolbarContext extends A_CmsDialogContext {

        /**
         * Constructor.<p>
         */
        protected ToolbarContext() {
            super(null, Collections.<CmsResource> emptyList());
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#focus(org.opencms.util.CmsUUID)
         */
        public void focus(CmsUUID structureId) {

            // nothing to do
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#getAllStructureIdsInView()
         */
        public List<CmsUUID> getAllStructureIdsInView() {

            return Lists.newArrayList();
        }
    }

    /** The serial version id. */
    private static final long serialVersionUID = -4551194983054069395L;

    /** The app indicator. */
    private Label m_appIndicator;

    /** The context menu component. */
    private MenuBar m_contextMenu;

    /** The dialog context. */
    private I_CmsDialogContext m_dialogContext;

    /** Toolbar items left. */
    private HorizontalLayout m_itemsLeft;

    /** Toolbar items right. */
    private HorizontalLayout m_itemsRight;

    /** The quick launch drop down. */
    private Component m_quickLaunchDropDown;

    /** The user drop down. */
    private Component m_userDropDown;

    /**
     * Constructor.<p>
     */
    public CmsToolBar() {
        m_quickLaunchDropDown = createQuickLaunchDropDown();
        m_userDropDown = createUserInfoDropDown();
        Design.read("CmsToolBar.html", this);
        m_dialogContext = new ToolbarContext();
        initContextMenu();
        m_itemsRight.addComponent(m_quickLaunchDropDown);
        m_itemsRight.addComponent(m_userDropDown);
    }

    /**
     * Creates a properly styled toolbar button.<p>
     *
     * @param icon the button icon
     * @param title the button title, will be used for the tooltip
     *
     * @return the button
     */
    public static Button createButton(Resource icon, String title) {

        Button button = new Button(icon);
        button.setDescription(title);
        button.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        button.addStyleName(OpenCmsTheme.TOOLBAR_BUTTON);
        return button;
    }

    /**
     * Creates a drop down menu.<p>
     *
     * @param icon the button icon
     * @param content the drop down content
     * @param title the button title
     *
     * @return the component
     */
    public static Component createDropDown(ExternalResource icon, Component content, String title) {

        String html = "<div tabindex=\"0\" role=\"button\" class=\"v-button v-widget borderless v-button-borderless "
            + OpenCmsTheme.TOOLBAR_BUTTON
            + " v-button-"
            + OpenCmsTheme.TOOLBAR_BUTTON
            + "\"><span class=\"v-button-wrap\"><img class=\"v-icon\" src=\""
            + icon.getURL()
            + "\" /></span></div>";
        PopupView pv = new PopupView(html, content);
        pv.setDescription(title);
        pv.addStyleName(OpenCmsTheme.NAVIGATOR_DROPDOWN);
        pv.setHideOnMouseOut(false);
        return pv;

    }

    /**
     * Creates a drop down menu.<p>
     *
     * @param icon the button icon
     * @param content the drop down content
     * @param title the drop down title
     *
     * @return the component
     */
    public static Component createDropDown(FontIcon icon, Component content, String title) {

        String html = "<div tabindex=\"0\" role=\"button\" class=\"v-button v-widget borderless v-button-borderless "
            + OpenCmsTheme.TOOLBAR_BUTTON
            + " v-button-"
            + OpenCmsTheme.TOOLBAR_BUTTON
            + "\"><span class=\"v-button-wrap\">"
            + icon.getHtml()
            + "</span></div>";
        PopupView pv = new PopupView(html, content);
        pv.setDescription(title);
        pv.addStyleName(OpenCmsTheme.NAVIGATOR_DROPDOWN);
        pv.setHideOnMouseOut(false);
        return pv;
    }

    /**
     * Adds a button to left toolbar side.<p>
     *
     * @param button the button
     */
    public void addButtonLeft(Component button) {

        m_itemsLeft.addComponent(button);
    }

    /**
     * Adds a button to right toolbar side.<p>
     *
     * @param button the button
     */
    public void addButtonRight(Component button) {

        m_itemsRight.addComponent(button);
    }

    /**
     * Clears the left toolbar buttons.<p>
     */
    public void clearButtonsLeft() {

        m_itemsLeft.removeAllComponents();
        // in case the app title is set, make sure to keep the label in the button bar
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_appIndicator.getValue())) {
            m_itemsLeft.addComponent(m_appIndicator);
        }
    }

    /**
     * Clears the right toolbar buttons.<p>
     */
    public void clearButtonsRight() {

        m_itemsRight.removeAllComponents();
    }

    /**
     * Closes all visible popup views.<p>
     */
    public void closePopupViews() {

        closePopupViews(m_itemsLeft);
        closePopupViews(m_itemsRight);
    }

    /**
     * Sets the app title.<p>
     *
     * @param appTitle the app title
     */
    public void setAppTitle(String appTitle) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(appTitle)) {
            m_appIndicator.setValue(appTitle);
            m_appIndicator.setVisible(true);
        } else {
            m_appIndicator.setVisible(false);
        }
    }

    /**
     * Sets the dialog context.<p>
     *
     * @param context the dialog context
     */
    protected void setDialogContext(I_CmsDialogContext context) {

        m_dialogContext = context;

        // reinit context menu
        initContextMenu();
    }

    /**
     * Returns the dialog context.<p>
     *
     * @return the dialog context
     */
    I_CmsDialogContext getDialogContext() {

        return m_dialogContext;
    }

    /**
     * Handles the user image file upload.<p>
     *
     * @param uploadedFiles the uploaded file names
     */
    void handleUpload(List<String> uploadedFiles) {

        CmsObject cms = A_CmsUI.getCmsObject();
        if (uploadedFiles.size() == 1) {
            String tempFile = CmsStringUtil.joinPaths(
                CmsUserIconHelper.USER_IMAGE_FOLDER,
                CmsUserIconHelper.TEMP_FOLDER,
                uploadedFiles.get(0));
            OpenCms.getWorkplaceAppManager().getUserIconHelper().handleImageUpload(
                cms,
                cms.getRequestContext().getCurrentUser(),
                tempFile);
            refreshUserInfoDropDown();
        }
    }

    /**
     * Closes the visible popup view children of the given layout.<p>
     *
     * @param layout the layout
     */
    private void closePopupViews(AbstractOrderedLayout layout) {

        for (Component item : layout) {
            if (item instanceof PopupView) {
                ((PopupView)item).setPopupVisible(false);
            }
        }
    }

    /**
     * Creates the context menu entry and it's children.<p>
     *
     * @param parent the entry parent
     * @param node the item tree node
     * @param treeBuilder the tree builder
     */
    private void createMenuEntry(
        MenuItem parent,
        final CmsTreeNode<I_CmsContextMenuItem> node,
        CmsContextMenuTreeBuilder treeBuilder) {

        Command entryCommand = null;
        if (node.getChildren().size() == 0) {
            entryCommand = new Command() {

                private static final long serialVersionUID = 1L;

                public void menuSelected(MenuItem selectedItem) {

                    node.getData().executeAction(getDialogContext());
                }
            };
        }
        MenuItem entry = parent.addItem((node.getData().getTitle(A_CmsUI.get().getLocale())), entryCommand);
        for (CmsTreeNode<I_CmsContextMenuItem> child : node.getChildren()) {
            createMenuEntry(entry, child, treeBuilder);
        }
        if (treeBuilder.getVisibility(node.getData()).isInActive()) {
            entry.setEnabled(false);
        }
    }

    /**
     * Creates the app select drop down.<p>
     *
     * @return the drop down component
     */
    private Component createQuickLaunchDropDown() {

        CmsObject cms = A_CmsUI.getCmsObject();
        Locale locale = UI.getCurrent().getLocale();
        HorizontalLayout layout = new HorizontalLayout();
        layout.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);
        //    layout.setSpacing(true);
        layout.setMargin(true);
        List<I_CmsWorkplaceAppConfiguration> configs = new ArrayList<I_CmsWorkplaceAppConfiguration>(
            OpenCms.getWorkplaceAppManager().getWorkplaceApps());

        Collections.sort(configs, new Comparator<I_CmsWorkplaceAppConfiguration>() {

            public int compare(I_CmsWorkplaceAppConfiguration cat1, I_CmsWorkplaceAppConfiguration cat2) {

                return ComparisonChain.start().compare(cat1.getOrder(), cat2.getOrder()).result();
            }
        });

        for (I_CmsWorkplaceAppConfiguration appConfig : configs) {
            CmsAppVisibilityStatus status = appConfig.getVisibility(cms);
            if (status.isVisible()) {
                layout.addComponent(CmsDefaultAppButtonProvider.createAppIconWidget(appConfig, locale));
                // show only the top 10
                if (layout.getComponentCount() > 9) {
                    break;
                }
            }
        }
        return createDropDown(
            FontOpenCms.APPS,
            layout,
            CmsVaadinUtils.getMessageText(Messages.GUI_QUICK_LAUNCH_TITLE_0));
    }

    /**
     * Creates the user info drop down.<p>
     *
     * @return the drop down component
     */
    private Component createUserInfoDropDown() {

        CmsObject cms = A_CmsUI.getCmsObject();
        Component userDropdown = createDropDown(
            new ExternalResource(OpenCms.getWorkplaceAppManager().getUserIconHelper().getSmallIconPath(
                cms,
                cms.getRequestContext().getCurrentUser())),
            new CmsUserInfo(new I_UploadListener() {

                public void onUploadFinished(List<String> uploadedFiles) {

                    handleUpload(uploadedFiles);
                }
            }),
            CmsVaadinUtils.getMessageText(Messages.GUI_USER_INFO_TITLE_0));
        userDropdown.addStyleName(OpenCmsTheme.USER_INFO);
        return userDropdown;
    }

    /**
     * Initializes the context menu entries.<p>
     */
    private void initContextMenu() {

        m_contextMenu.removeItems();
        MenuItem main = m_contextMenu.addItem("", null);
        main.setIcon(FontOpenCms.CONTEXT_MENU);
        main.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_MENU_TITLE_0));
        CmsContextMenuTreeBuilder treeBuilder = new CmsContextMenuTreeBuilder(getDialogContext());
        CmsTreeNode<I_CmsContextMenuItem> tree = treeBuilder.buildAll(
            CmsAppWorkplaceUi.get().getMenuItemProvider().getMenuItems());
        for (CmsTreeNode<I_CmsContextMenuItem> node : tree.getChildren()) {
            createMenuEntry(main, node, treeBuilder);
        }
    }

    /**
     * Refreshes the user drop down.<p>
     */
    private void refreshUserInfoDropDown() {

        Component oldVersion = m_userDropDown;
        m_userDropDown = createUserInfoDropDown();
        m_itemsRight.replaceComponent(oldVersion, m_userDropDown);
    }
}
