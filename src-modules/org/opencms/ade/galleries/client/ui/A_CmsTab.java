/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/A_CmsTab.java,v $
 * Date   : $Date: 2010/05/07 08:16:13 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.galleries.client.ui;

import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle.I_CmsGalleryDialogCss;
import org.opencms.gwt.client.ui.CmsFlowPanel;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.I_CmsListItem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a widget for the content of a tab.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 8.0.
 */
public abstract class A_CmsTab extends Composite {

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    /* default */interface I_CmsTabUiBinder extends UiBinder<Widget, A_CmsTab> {
        // GWT interface, nothing to do here
    }

    /** The css bundle used for this widget. */
    protected static final I_CmsGalleryDialogCss DIALOG_CSS = I_CmsLayoutBundle.INSTANCE.galleryDialogCss();

    /** The ui-binder instance for this class. */
    
    private static I_CmsTabUiBinder uiBinder = GWT.create(I_CmsTabUiBinder.class);

    /** The categories parameter panel. */
    @UiField
    protected Panel m_categories;

    /** The galleries parameter panel. */
    @UiField
    protected Panel m_galleries;

    /** The borded panel to hold the scrollable list. */
    @UiField
    protected CmsFlowPanel m_list;

    /** The option panel. */
    @UiField
    protected CmsFlowPanel m_options;

    /** The option panel. */
    @UiField
    protected Panel m_params;

    /** The scrollable list panel. */
    @UiField
    protected CmsList<? extends I_CmsListItem> m_scrollList;

    /** The option panel. */
    @UiField
    protected HTMLPanel m_tab;

    /** The types parameter panel panel. */
    @UiField
    protected Panel m_types;

    /**
     * The default constructor with drag handler.<p>
     * 
     */
    public A_CmsTab() {

        init();
    }

    // TODO: add the search parameter display button
    //    /** The full text search parameter panel. */
    //    @UiField
    //    protected Panel m_text;

    /**
     * Will be triggered when a tab is selected.<p>
     */
    public abstract void onSelection();

    /**
     * Updates the layout for all list items in this list.<p>
     * 
     * @see org.opencms.gwt.client.ui.CmsList#updateLayout()
     */
    public void updateListLayout() {

        m_scrollList.updateLayout();
    }

    /**
     * Add a list item widget to the list panel.<p>
     * 
     * @param listItem the list item to add
     */
    protected void addWidgetToList(Widget listItem) {

        m_scrollList.add(listItem);
    }

    /**
     * Add a widget to the option panel.<p>
     * 
     * The option panel should contain drop down boxes or other list options.
     * 
     * @param widget the widget to add
     */
    protected void addWidgetToOptions(Widget widget) {

        m_options.add(widget);
    }

    /**
     * Clears the list panel.<p>
     */
    protected void clearList() {

        m_scrollList.clearList();
    }

    /**
     * Clears all search parameters.<p>
     */
    protected void clearParams() {

        m_types.clear();
        m_galleries.clear();
        m_categories.clear();
        //        m_text.clear();
    }

    /**
     * Initializes this list item.<p>
     */
    protected void init() {

        uiBinder.createAndBindUi(this);
        initWidget(uiBinder.createAndBindUi(this));

        CmsGalleryDialog.initCss();
    }
}