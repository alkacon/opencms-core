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

package org.opencms.ui.components;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.ui.A_CmsDialogContext;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.CmsDefaultAppButtonProvider;
import org.opencms.ui.apps.CmsFileExplorer;
import org.opencms.ui.apps.I_CmsAppUIContext;
import org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.CmsUploadButton.I_UploadListener;
import org.opencms.ui.contextmenu.CmsContextMenuTreeBuilder;
import org.opencms.ui.contextmenu.I_CmsContextMenuItem;
import org.opencms.ui.favorites.CmsExplorerFavoriteContext;
import org.opencms.ui.favorites.CmsFavoriteDAO;
import org.opencms.ui.favorites.CmsFavoriteDialog;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsTreeNode;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontIcon;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
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
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.declarative.Design;
import com.vaadin.ui.themes.ValoTheme;

/**
 * The workplace toolbar.<p>
 */
public class CmsToolBar extends CssLayout implements BrowserWindowResizeListener {

    /** Toolbar dialog context. */
    protected class ToolbarContext extends A_CmsDialogContext {

        /**
         * Constructor.<p>
         *
         * @param appId the app id
         */
        protected ToolbarContext(String appId) {

            super(appId, ContextType.appToolbar, Collections.<CmsResource> emptyList());
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

        /**
         * @see org.opencms.ui.I_CmsDialogContext#updateUserInfo()
         */
        public void updateUserInfo() {

            refreshUserInfoDropDown();
        }
    }

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsToolBar.class);

    /** The serial version id. */
    private static final long serialVersionUID = -4551194983054069395L;

    /** The app UI context. */
    protected I_CmsAppUIContext m_appContext;

    /** The app indicator. */
    private Label m_appIndicator;

    /** Flag indicating the toolbar buttons are folded into a sub menu. */
    private boolean m_buttonsFolded;

    /** The context menu component. */
    private MenuBar m_contextMenu;

    /** The dialog context. */
    private I_CmsDialogContext m_dialogContext;

    /** The favorite button. */
    private Button m_favButton;

    /** The sub menu displaying the folded buttons. */
    private PopupView m_foldedButtonsMenu;

    /** The browser window width that is required to display all toolbar buttons. */
    private int m_foldingThreshhold;

    /** Toolbar items left. */
    private HorizontalLayout m_itemsLeft;

    /** Toolbar items right. */
    private HorizontalLayout m_itemsRight;

    /** The contains the buttons from the left side, displayed in the folded buttons sub menu. */
    private VerticalLayout m_leftButtons;

    /** The quick launch drop down. */
    private Component m_quickLaunchDropDown;

    /** The contains the buttons from the right side, displayed in the folded buttons sub menu. */
    private VerticalLayout m_rightButtons;

    /** The user drop down. */
    private Component m_userDropDown;

    /**
     * Constructor.<p>
     */
    public CmsToolBar() {

        m_quickLaunchDropDown = createQuickLaunchDropDown();
        m_userDropDown = createUserInfoDropDown();
        m_favButton = CmsToolBar.createButton(
            FontOpenCms.BOOKMARKS,
            CmsVaadinUtils.getMessageText(org.opencms.ui.Messages.GUI_FAVORITES_BUTTON_0),
            true);
        m_leftButtons = new VerticalLayout();
        m_rightButtons = new VerticalLayout();
        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(m_leftButtons);
        layout.addComponent(m_rightButtons);
        m_foldedButtonsMenu = new PopupView(getDropDownButtonHtml(FontOpenCms.CONTEXT_MENU_DOTS), layout);
        m_foldedButtonsMenu.addStyleName(OpenCmsTheme.NAVIGATOR_DROPDOWN);
        m_foldedButtonsMenu.setHideOnMouseOut(false);
        Design.read("CmsToolBar.html", this);

        m_favButton.addClickListener(evt -> {
            CmsFileExplorer explorer = (CmsFileExplorer)m_appContext.getAttribute(CmsFileExplorer.ATTR_KEY);
            openFavoriteDialog(explorer);
        });
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

        return createButton(icon, title, false);
    }

    /**
     * Creates a properly styled toolbar button.<p>
     *
     * @param icon the button icon
     * @param title the button title, will be used for the tooltip
     * @param alwaysShow <code>true</code> to prevent the button to be folded into a sub menu for small screens
     *
     * @return the button
     */
    public static Button createButton(Resource icon, String title, boolean alwaysShow) {

        Button button = new Button(icon);
        button.setDescription(title);
        button.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        button.addStyleName(OpenCmsTheme.TOOLBAR_BUTTON);
        if (alwaysShow) {
            button.addStyleName(OpenCmsTheme.REQUIRED_BUTTON);
        }
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

        return createDropDown(getDropDownButtonHtml(icon), content, title);
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

        return createDropDown(getDropDownButtonHtml(icon), content, title);
    }

    /**
     * Creates a drop down menu.<p>
     *
     * @param buttonHtml the button HTML
     * @param content the drop down content
     * @param title the button title
     *
     * @return the component
     */
    public static Component createDropDown(String buttonHtml, Component content, String title) {

        PopupView pv = new PopupView(buttonHtml, content);
        pv.setDescription(title);
        pv.addStyleName(OpenCmsTheme.NAVIGATOR_DROPDOWN);
        pv.setHideOnMouseOut(false);
        return pv;
    }

    /**
     * Opens the favorite dialog.
     *
     * @param explorer the explorer instance (null if not currently in explorer)
     */
    public static void openFavoriteDialog(CmsFileExplorer explorer) {

        try {
            CmsExplorerFavoriteContext context = new CmsExplorerFavoriteContext(A_CmsUI.getCmsObject(), explorer);
            CmsFavoriteDialog dialog = new CmsFavoriteDialog(context, new CmsFavoriteDAO(A_CmsUI.getCmsObject()));
            Window window = CmsBasicDialog.prepareWindow(DialogWidth.max);
            window.setContent(dialog);
            window.setCaption(CmsVaadinUtils.getMessageText(org.opencms.ui.Messages.GUI_FAVORITES_DIALOG_TITLE_0));
            A_CmsUI.get().addWindow(window);
            window.center();
        } catch (CmsException e) {
            CmsErrorDialog.showErrorDialog(e);
        }
    }

    /**
     * Creates the button HTML for the given icon resource.<p>
     *
     * @param icon the icon
     *
     * @return the HTML
     */
    static String getDropDownButtonHtml(ExternalResource icon) {

        return "<div tabindex=\"0\" role=\"button\" class=\"v-button v-widget borderless v-button-borderless "
            + OpenCmsTheme.TOOLBAR_BUTTON
            + " v-button-"
            + OpenCmsTheme.TOOLBAR_BUTTON
            + "\"><span class=\"v-button-wrap\"><img width=\"32\" height=\"32\" class=\"v-icon\" src=\""
            + icon.getURL()
            + "\" /></span></div>";
    }

    /**
     * Creates the button HTML for the given icon resource.<p>
     *
     * @param icon the icon
     *
     * @return the HTML
     */
    static String getDropDownButtonHtml(FontIcon icon) {

        return "<div tabindex=\"0\" role=\"button\" class=\"v-button v-widget borderless v-button-borderless "
            + OpenCmsTheme.TOOLBAR_BUTTON
            + " v-button-"
            + OpenCmsTheme.TOOLBAR_BUTTON
            + "\"><span class=\"v-button-wrap\">"
            + icon.getHtml()
            + "</span></div>";
    }

    /**
     * Adds a button to left toolbar side.<p>
     *
     * @param button the button
     */
    public void addButtonLeft(Component button) {

        if (m_buttonsFolded && !isAlwaysShow(button)) {
            m_leftButtons.addComponent(button);
        } else {
            m_itemsLeft.addComponent(button);
        }
        updateFoldingThreshhold();
    }

    /**
     * Adds a button to right toolbar side.<p>
     *
     * @param button the button
     */
    public void addButtonRight(Component button) {

        if (m_buttonsFolded && !isAlwaysShow(button)) {
            m_rightButtons.addComponent(button);
        } else {
            int dropDownIndex = m_itemsRight.getComponentIndex(m_userDropDown);
            if (dropDownIndex >= 0) {
                m_itemsRight.addComponent(button, dropDownIndex);
            } else {
                m_itemsRight.addComponent(button);
            }
        }
        updateFoldingThreshhold();
    }

    /**
     * @see com.vaadin.server.Page.BrowserWindowResizeListener#browserWindowResized(com.vaadin.server.Page.BrowserWindowResizeEvent)
     */
    public void browserWindowResized(BrowserWindowResizeEvent event) {

        updateButtonVisibility(event.getWidth());
    }

    /**
     * Clears the left toolbar buttons.<p>
     */
    public void clearButtonsLeft() {

        m_itemsLeft.removeAllComponents();
        m_leftButtons.removeAllComponents();
        // in case the app title is set, make sure to keep the label in the button bar
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_appIndicator.getValue())) {
            m_itemsLeft.addComponent(m_appIndicator);
        }
        updateFoldingThreshhold();
    }

    /**
     * Clears the right toolbar buttons.<p>
     */
    public void clearButtonsRight() {

        m_itemsRight.removeAllComponents();
        m_rightButtons.removeAllComponents();
        updateFoldingThreshhold();
    }

    /**
     * Closes all visible popup views.<p>
     */
    public void closePopupViews() {

        closePopupViews(m_itemsLeft);
        closePopupViews(m_itemsRight);
    }

    /**
     * Enables or removes the default toolbar buttons.<p>
     * These are the context menu and the quick launch drop down.<p>
     * The default is <code>enabled = true</code>.<p>
     *
     * @param enabled <code>true</code> to enable the buttons
     */
    public void enableDefaultButtons(boolean enabled) {

        if (enabled) {
            m_itemsRight.addComponent(m_contextMenu, 0);
            m_itemsRight.addComponent(m_favButton, 1);
            m_itemsRight.addComponent(m_quickLaunchDropDown, 2);
        } else {
            m_itemsRight.removeComponent(m_contextMenu);
            m_itemsRight.removeComponent(m_favButton);
            m_itemsRight.removeComponent(m_quickLaunchDropDown);
        }
        updateFoldingThreshhold();
    }

    /**
     * Refreshes the user drop down.<p>
     */
    public void refreshUserInfoDropDown() {

        Component oldVersion = m_userDropDown;
        m_userDropDown = createUserInfoDropDown();
        m_itemsRight.replaceComponent(oldVersion, m_userDropDown);
    }

    /**
     * Removes the given button from the toolbar.<p>
     *
     * @param button the button to remove
     */
    public void removeButton(Component button) {

        m_itemsLeft.removeComponent(button);
        m_itemsRight.removeComponent(button);
        m_leftButtons.removeComponent(button);
        m_rightButtons.removeComponent(button);
        updateFoldingThreshhold();
    }

    /**
     * Sets the app context.
     *
     * @param context the app context
     */
    public void setAppContext(I_CmsAppUIContext context) {

        m_appContext = context;
    }

    /**
     * Sets the app title.<p>
     *
     * @param appTitle the app title
     */
    public void setAppTitle(String appTitle) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(appTitle)) {
            m_appIndicator.setValue(appTitle);
            updateAppIndicator();
            m_appIndicator.setVisible(true);
        } else {
            m_appIndicator.setVisible(false);
        }
    }

    /**
     * Updates the app indicator site and project info.<p>
     */
    public void updateAppIndicator() {

        if (CmsAppWorkplaceUi.isOnlineProject()) {
            m_appIndicator.addStyleName(OpenCmsTheme.TOOLABER_APP_INDICATOR_ONLINE);

        } else {
            m_appIndicator.removeStyleName(OpenCmsTheme.TOOLABER_APP_INDICATOR_ONLINE);
        }
        CmsObject cms = A_CmsUI.getCmsObject();
        String siteRoot = cms.getRequestContext().getSiteRoot();
        CmsSite site = OpenCms.getSiteManager().getSiteForSiteRoot(siteRoot);
        String siteName = null;
        if (site != null) {
            siteName = site.getTitle();
        } else {
            try {
                CmsResource folder = cms.readResource("/", CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);

                siteName = OpenCms.getSiteManager().getSiteTitle(cms, folder);
            } catch (CmsException e) {
                LOG.warn("Error reading site title.", e);
            }
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(siteName)) {
            siteName = siteRoot;
        } else {
            siteName = CmsWorkplace.substituteSiteTitleStatic(siteName, UI.getCurrent().getLocale());
        }
        m_appIndicator.setDescription(
            CmsVaadinUtils.getMessageText(
                Messages.GUI_TOOLBAR_PROJECT_SITE_INFO_2,
                A_CmsUI.getCmsObject().getRequestContext().getCurrentProject().getName(),
                siteName),
            ContentMode.HTML);
    }

    /**
     * Initializes the toolbar.<p>
     *
     * @param appId the app id
     * @param context the app UI context
     */
    protected void init(String appId, I_CmsAppUIContext context) {

        m_dialogContext = new ToolbarContext(appId);
        m_appContext = context;
        initContextMenu();
        m_itemsRight.addComponent(m_quickLaunchDropDown);
        m_itemsRight.addComponent(m_userDropDown);
        enableDefaultButtons(true);
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
     * Updates the button visibility according o the given widow width.<p>
     *
     * @param width the window width
     */
    protected void updateButtonVisibility(int width) {

        if (!m_buttonsFolded && (m_foldingThreshhold > width)) {
            foldButtons();
        } else if (m_buttonsFolded && (width > m_foldingThreshhold)) {
            unfoldButtons();
        }
    }

    /**
     * Recalculates the space required by the toolbar buttons.<p>
     */
    protected void updateFoldingThreshhold() {

        int left = estimateRequiredWidth(m_itemsLeft) + estimateRequiredWidth(m_leftButtons);
        int right = estimateRequiredWidth(m_itemsRight) + estimateRequiredWidth(m_rightButtons);
        int requiredWidth = left > right ? left : right;
        if (requiredWidth < 350) {
            // folding not required at any width
            m_foldingThreshhold = 0;
        } else if (requiredWidth < 400) {
            m_foldingThreshhold = 984;
        } else if (requiredWidth <= 520) {
            m_foldingThreshhold = 1240;
        } else {
            // always fold
            m_foldingThreshhold = 10000;
        }
        updateButtonVisibility(Page.getCurrent().getBrowserWindowWidth());
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
        boolean success = OpenCms.getWorkplaceAppManager().getUserIconHelper().handleImageUpload(cms, uploadedFiles);
        if (success) {
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

        PopupView pv = new PopupView(new PopupView.Content() {

            private static final long serialVersionUID = 1L;

            public String getMinimizedValueAsHTML() {

                return getDropDownButtonHtml(FontOpenCms.APPS);
            }

            public Component getPopupComponent() {

                CmsObject cms = A_CmsUI.getCmsObject();
                Locale locale = UI.getCurrent().getLocale();
                HorizontalLayout layout = new HorizontalLayout();
                layout.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);
                layout.addStyleName(OpenCmsTheme.QUICK_LAUNCH);
                layout.setSpacing(false);
                layout.setMargin(true);
                for (I_CmsWorkplaceAppConfiguration config : OpenCms.getWorkplaceAppManager().getQuickLaunchConfigurations(
                    cms)) {
                    layout.addComponent(CmsDefaultAppButtonProvider.createAppButton(cms, config, locale));
                }
                return layout;
            }
        });
        pv.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_QUICK_LAUNCH_TITLE_0));
        pv.addStyleName(OpenCmsTheme.NAVIGATOR_DROPDOWN);
        pv.setHideOnMouseOut(false);

        return pv;

    }

    /**
     * Creates the user info drop down.<p>
     *
     * @return the drop down component
     */
    private Component createUserInfoDropDown() {

        PopupView pv = new PopupView(new PopupView.Content() {

            private static final long serialVersionUID = 1L;

            public String getMinimizedValueAsHTML() {

                CmsObject cms = A_CmsUI.getCmsObject();
                return getDropDownButtonHtml(
                    new ExternalResource(
                        OpenCms.getWorkplaceAppManager().getUserIconHelper().getSmallIconPath(
                            cms,
                            cms.getRequestContext().getCurrentUser())));
            }

            public Component getPopupComponent() {

                return new CmsUserInfo(new I_UploadListener() {

                    public void onUploadFinished(List<String> uploadedFiles) {

                        handleUpload(uploadedFiles);
                    }
                }, getDialogContext());
            }
        });
        pv.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_USER_INFO_TITLE_0));
        pv.addStyleName(OpenCmsTheme.NAVIGATOR_DROPDOWN);
        pv.setHideOnMouseOut(false);
        pv.addStyleName(OpenCmsTheme.USER_INFO);
        return pv;
    }

    /**
     * Calculates the width required by the layout components.<p>
     *
     * @param items the layout
     *
     * @return the width
     */
    private int estimateRequiredWidth(AbstractOrderedLayout items) {

        int result = 0;
        if (items != null) {
            for (Component comp : items) {
                if (comp == m_foldedButtonsMenu) {
                    continue;
                } else if ((comp instanceof Button) || (comp instanceof PopupView) || (comp instanceof MenuBar)) {
                    // assume all buttons have a with of 50px
                    result += 50;
                } else if (comp == m_appIndicator) {
                    // assume app indicator requires 150px
                    result += 50;
                } else {
                    float compWidth = comp.getWidth();
                    if ((compWidth > 0) && (comp.getWidthUnits() == Unit.PIXELS)) {
                        // also add 10px margin
                        result += compWidth + 10;
                    } else {
                        result += 200;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Folds the toolbar buttons into a sub menu.<p>
     */
    private void foldButtons() {

        VerticalLayout mainPV = (VerticalLayout)m_foldedButtonsMenu.getContent().getPopupComponent();
        for (int i = m_itemsLeft.getComponentCount() - 1; i > -1; i--) {
            Component comp = m_itemsLeft.getComponent(i);
            if (!isAlwaysShow(comp)) {
                m_itemsLeft.removeComponent(comp);
                m_leftButtons.addComponent(comp, 0);
                m_leftButtons.setComponentAlignment(comp, Alignment.MIDDLE_CENTER);
            }
        }
        if (m_leftButtons.getComponentCount() == 0) {
            mainPV.removeComponent(m_leftButtons);
        } else {
            mainPV.addComponent(m_leftButtons, 0);
        }
        for (int i = m_itemsRight.getComponentCount() - 1; i > -1; i--) {
            Component comp = m_itemsRight.getComponent(i);
            if (!isAlwaysShow(comp)) {
                m_itemsRight.removeComponent(comp);
                m_rightButtons.addComponent(comp, 0);
                m_rightButtons.setComponentAlignment(comp, Alignment.MIDDLE_CENTER);
            }
        }
        if (m_rightButtons.getComponentCount() == 0) {
            mainPV.removeComponent(m_rightButtons);
        } else {
            mainPV.addComponent(m_rightButtons);
        }
        m_itemsRight.addComponent(m_foldedButtonsMenu, 0);
        m_buttonsFolded = true;
        markAsDirtyRecursive();
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
            OpenCms.getWorkplaceAppManager().getMenuItemProvider().getMenuItems());
        for (CmsTreeNode<I_CmsContextMenuItem> node : tree.getChildren()) {
            createMenuEntry(main, node, treeBuilder);
        }
    }

    /**
     * Checks whether the given component may be placed into the buttons sub menu.<p>
     *
     * @param comp the component to check
     *
     * @return <code>true</code> in case the component should always be displayed in the toolbar
     */
    private boolean isAlwaysShow(Component comp) {

        return ((comp == m_appIndicator)
            || (comp == m_contextMenu)
            || (comp == m_userDropDown)
            || (comp == m_quickLaunchDropDown)
            || comp.getStyleName().contains(OpenCmsTheme.REQUIRED_BUTTON));
    }

    /**
     * Places the buttons formerly moved to the sub menu back into the toolbar.<p>
     */
    private void unfoldButtons() {

        m_itemsRight.removeComponent(m_foldedButtonsMenu);
        while (m_leftButtons.getComponentCount() > 0) {
            Component comp = m_leftButtons.getComponent(0);
            if (!isAlwaysShow(comp)) {
                m_leftButtons.removeComponent(comp);
                m_itemsLeft.addComponent(comp);
            }
        }
        int index = 0;
        while (m_rightButtons.getComponentCount() > 0) {
            Component comp = m_rightButtons.getComponent(0);
            m_rightButtons.removeComponent(comp);
            m_itemsRight.addComponent(comp, index);
            index++;
        }
        m_buttonsFolded = false;
        markAsDirtyRecursive();
    }
}
