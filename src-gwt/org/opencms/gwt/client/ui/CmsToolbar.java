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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.ui.CmsQuickLauncher.I_QuickLaunchHandler;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsStyleVariable;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a tool-bar to be shown at the top of a page.<p>
 *
 * @since 8.0.0
 */
public class CmsToolbar extends Composite {

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    protected interface I_CmsToolbarUiBinder extends UiBinder<Widget, CmsToolbar> {
        // GWT interface, nothing to do here
    }

    /** The ui-binder instance for this class. */
    private static I_CmsToolbarUiBinder uiBinder = GWT.create(I_CmsToolbarUiBinder.class);

    /** Holds left-side buttons associated with the tool-bar. */
    @UiField
    protected FlowPanel m_buttonPanelLeft;

    /** Holds right-side buttons associated with the tool-bar. */
    @UiField
    protected FlowPanel m_buttonPanelRight;

    /**
     * Center of the toolbar, normally for displaying the logo, but the content can be changed.
     */
    @UiField
    protected FlowPanel m_toolbarCenter;

    /** The quick launcher (initially invisible). */
    @UiField
    protected CmsQuickLauncher m_quickLauncher;

    /** The user info button HTML. */
    @UiField
    protected CmsUserInfo m_userInfo;

    /** The title label. */
    private Label m_titleLabel;

    /**
     * Constructor.<p>
     */
    public CmsToolbar() {

        initWidget(uiBinder.createAndBindUi(this));

    }

    /**
     * Helper method for setting toolbar visibility.<p>
     *
     * @param toolbar the toolbar
     * @param show true if the toolbar should be shown
     * @param toolbarVisibility the style variable controlling the toolbar visibility
     */
    public static void showToolbar(
        final CmsToolbar toolbar,
        final boolean show,
        final CmsStyleVariable toolbarVisibility) {

        if (show) {
            toolbarVisibility.setValue(null);
        } else {
            toolbarVisibility.setValue(I_CmsLayoutBundle.INSTANCE.toolbarCss().toolbarHide());
        }
    }

    /**
     * Helper method for setting toolbar visibility.<p>
     *
     * @param toolbar the toolbar
     * @param show true if the toolbar should be shown
     * @param toolbarVisibility the style variable controlling the toolbar visibility
     * @param showClass the class which should be used for showing the toolbar
     */
    public static void showToolbar(
        final CmsToolbar toolbar,
        final boolean show,
        final CmsStyleVariable toolbarVisibility,
        String showClass) {

        if (show) {
            toolbarVisibility.setValue(showClass);
        } else {
            toolbarVisibility.setValue(I_CmsLayoutBundle.INSTANCE.toolbarCss().toolbarHide());
        }
    }

    /**
     * Adds a widget to the left button panel.<p>
     *
     * @param widget the widget to add
     */
    public void addLeft(Widget widget) {

        m_buttonPanelLeft.add(widget);
    }

    /**
     * Adds a widget to the left button panel.<p>
     *
     * @param widget the widget to add
     */
    public void addRight(Widget widget) {

        m_buttonPanelRight.add(widget);

    }

    /**
     * Returns all {@link com.google.gwt.user.client.ui.Widget} added to the tool-bar in order of addition first left than right.<p>
     *
     * @return all added Widgets
     */
    public List<Widget> getAll() {

        List<Widget> all = new ArrayList<Widget>();
        Iterator<Widget> it = m_buttonPanelLeft.iterator();
        while (it.hasNext()) {
            all.add(it.next());
        }
        it = m_buttonPanelRight.iterator();
        while (it.hasNext()) {
            all.add(it.next());
        }
        return all;
    }

    /**
     * Returns the quick launcher.<p>
     *
     * @return the quick launch menu button
     */
    public CmsQuickLauncher getQuickLauncher() {

        return m_quickLauncher;
    }

    /**
     * Gets the center area of the toolbar, which normally contains the logo.
     *
     * @return the center toolbar area
     */
    public FlowPanel getToolbarCenter() {
        return m_toolbarCenter;
    }

    /**
     * Returns the user info button.<p>
     *
     * @return the user info button
     */
    public CmsUserInfo getUserInfo() {

        return m_userInfo;
    }

    /**
     * Inserts a widget into the left button panel.<p>
     *
     * @param widget the widget to add
     * @param index the before index
     */
    public void insertLeft(Widget widget, int index) {

        m_buttonPanelLeft.insert(widget, index);
    }

    /**
     * Inserts a widget into the left button panel.<p>
     *
     * @param widget the widget to add
     * @param index the before index
     */
    public void insertRight(Widget widget, int index) {

        m_buttonPanelRight.insert(widget, index);
    }

    /**
     * Sets the toolbar title label.<p>
     *
     * @param title the title
     */
    public void setAppTitle(String title) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
            if (m_titleLabel != null) {
                m_titleLabel.removeFromParent();
                m_titleLabel = null;
            }
        } else {

            if (m_titleLabel == null) {
                m_titleLabel = new Label();
                m_titleLabel.setStyleName(I_CmsLayoutBundle.INSTANCE.toolbarCss().title());
                m_buttonPanelLeft.insert(m_titleLabel, 0);
            }
            m_titleLabel.setText(title);
        }
    }

    /**
     * Sets the handler for the quick launch menu and turns that menu visible.<p>
     *
     * @param quicklaunchHandler the quick launch handler
     */
    public void setQuickLaunchHandler(I_QuickLaunchHandler quicklaunchHandler) {

        m_quickLauncher.setQuicklaunchHandler(quicklaunchHandler);
    }
}
