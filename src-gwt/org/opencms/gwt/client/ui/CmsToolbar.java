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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.util.CmsFadeAnimation;
import org.opencms.gwt.client.util.CmsStyleVariable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
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
            toolbarVisibility.setValue(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.toolbarCss().toolbarShow());
            CmsFadeAnimation.fadeIn(toolbar.getElement(), null, 300);
            // body.getStyle().setMarginTop(m_bodyMarginTop + 36, Unit.PX);
        } else {
            CmsFadeAnimation.fadeOut(toolbar.getElement(), new Command() {

                public void execute() {

                    toolbarVisibility.setValue(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.toolbarCss().toolbarHide());
                }
            },
                300);
            // body.getStyle().setMarginTop(m_bodyMarginTop, Unit.PX);
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
            CmsFadeAnimation.fadeIn(toolbar.getElement(), null, 300);
            // body.getStyle().setMarginTop(m_bodyMarginTop + 36, Unit.PX);
        } else {
            CmsFadeAnimation.fadeOut(toolbar.getElement(), new Command() {

                public void execute() {

                    toolbarVisibility.setValue(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.toolbarCss().toolbarHide());
                }
            },
                300);
            // body.getStyle().setMarginTop(m_bodyMarginTop, Unit.PX);
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
}
