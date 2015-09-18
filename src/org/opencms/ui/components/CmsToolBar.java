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
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsUserIconHelper;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.CmsAppVisibilityStatus;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.CmsDefaultAppButtonProvider;
import org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration;
import org.opencms.ui.contextmenu.CmsContextMenuTreeBuilder;
import org.opencms.ui.contextmenu.I_CmsContextMenuItem;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsTreeNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.ComparisonChain;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontIcon;
import com.vaadin.server.Resource;
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

    /** The serial version id. */
    private static final long serialVersionUID = -4551194983054069395L;

    /** The app indicator. */
    private Label m_appIndicator;

    /** Toolbar items left. */
    private HorizontalLayout m_itemsLeft;

    /** Toolbar items right. */
    private HorizontalLayout m_itemsRight;

    /**
     * Constructor.<p>
     */
    public CmsToolBar() {

        Design.read("CmsToolBar.html", this);
        m_itemsRight.addComponent(createContextMenu());
        m_itemsRight.addComponent(createQuickLaunchDropDown());
        m_itemsRight.addComponent(createUserInfoDropDown());
    }

    /**
     * Creates a properly styled toolbar button.<p>
     *
     * @param icon the button icon
     *
     * @return the button
     */
    public static Button createButton(Resource icon) {

        Button button = new Button(icon);
        button.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        button.addStyleName(OpenCmsTheme.TOOLBAR_BUTTON);
        return button;
    }

    /**
     * Creates a drop down menu.<p>
     *
     * @param icon the button icon
     * @param content the drop down content
     *
     * @return the component
     */
    public static Component createDropDown(ExternalResource icon, Component content) {

        String html = "<div tabindex=\"0\" role=\"button\" class=\"v-button v-widget borderless v-button-borderless "
            + OpenCmsTheme.TOOLBAR_BUTTON
            + " v-button-"
            + OpenCmsTheme.TOOLBAR_BUTTON
            + "\"><span class=\"v-button-wrap\"><img class=\"v-icon\" src=\""
            + icon.getURL()
            + "\" /></span></div>";
        PopupView pv = new PopupView(html, content);
        pv.addStyleName(OpenCmsTheme.NAVIGATOR_DROPDOWN);
        pv.setHideOnMouseOut(false);
        return pv;

    }

    /**
     * Creates a drop down menu.<p>
     *
     * @param icon the button icon
     * @param content the drop down content
     *
     * @return the component
     */
    public static Component createDropDown(FontIcon icon, Component content) {

        String html = "<div tabindex=\"0\" role=\"button\" class=\"v-button v-widget borderless v-button-borderless "
            + OpenCmsTheme.TOOLBAR_BUTTON
            + " v-button-"
            + OpenCmsTheme.TOOLBAR_BUTTON
            + "\"><span class=\"v-button-wrap\">"
            + icon.getHtml()
            + "</span></div>";
        PopupView pv = new PopupView(html, content);
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
     * Creates the context menu button.<p>
     *
     * @return the context menu button
     */
    private Component createContextMenu() {

        MenuBar menuBar = new MenuBar();
        menuBar.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        menuBar.addStyleName(OpenCmsTheme.TOOLBAR_BUTTON);
        MenuItem main = menuBar.addItem("", null);
        main.setIcon(FontOpenCms.CONTEXT_MENU);
        CmsContextMenuTreeBuilder treeBuilder = new CmsContextMenuTreeBuilder(
            A_CmsUI.getCmsObject(),
            Collections.<CmsResource> emptyList());
        CmsTreeNode<I_CmsContextMenuItem> tree = treeBuilder.buildAll(
            CmsAppWorkplaceUi.get().getMenuItemProvider().getMenuItems());
        for (CmsTreeNode<I_CmsContextMenuItem> node : tree.getChildren()) {
            createMenuEntry(main, node, treeBuilder);
        }
        return menuBar;
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

                    node.getData().executeAction(null);
                }
            };
        }
        MenuItem entry = parent.addItem(CmsVaadinUtils.localizeString(node.getData().getTitle()), entryCommand);
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
        return createDropDown(FontOpenCms.APPS, layout);
    }

    /**
     * Creates the user info drop down.<p>
     *
     * @return the drop down component
     */
    private Component createUserInfoDropDown() {

        CmsObject cms = A_CmsUI.getCmsObject();
        Component userDropdown = createDropDown(
            new ExternalResource(
                CmsUserIconHelper.getInstance().getSmallIconPath(cms, cms.getRequestContext().getCurrentUser())),
            new CmsUserInfo());
        userDropdown.addStyleName(OpenCmsTheme.USER_INFO);
        return userDropdown;
    }
}
