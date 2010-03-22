/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsSimpleListItem.java,v $
 * Date   : $Date: 2010/03/22 16:16:02 $
 * Version: $Revision: 1.1 $
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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.I_CmsListTreeCss;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Simple list item which uses a CmsFloatDecoratedPanel for layout.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 * 
 */
public class CmsSimpleListItem extends CmsListItem {

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    interface I_CmsSimpleListItemUiBinder extends UiBinder<Panel, CmsSimpleListItem> {
        // GWT interface, nothing to do here
    }

    /** The CSS bundle used for this widget. */
    private static final I_CmsListTreeCss CSS = I_CmsLayoutBundle.INSTANCE.listTreeCss();

    /** The ui-binder instance for this class. */
    private static I_CmsSimpleListItemUiBinder uiBinder = GWT.create(I_CmsSimpleListItemUiBinder.class);

    /** The content area. */
    @UiField
    protected CmsFloatDecoratedPanel m_content;

    /** 
     * Default constructor.<p>
     */
    public CmsSimpleListItem() {

        super();
    }

    /**
     * Creates a new list item containing several widgets.<p>
     * 
     * @param content the widgets to put into the item 
     */
    public CmsSimpleListItem(Widget... content) {

        this();
        if (content.length > 0) {
            // put all but the last widget into the float section
            for (int i = 0; i < content.length - 1; i++) {
                m_content.addToFloat(content[i]);
            }
            m_content.add(content[content.length - 1]);
        }
    }

    static {
        CSS.ensureInjected();
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsListItem#add(com.google.gwt.user.client.ui.Widget)
     */
    @Override
    public void add(Widget w) {

        m_content.add(w);
    }

    /**
     * Adds a widget to the item's floating section.<p>
     * 
     * @param w the widget to add
     */
    public void addLeft(Widget w) {

        m_content.addToFloat(w);

    }

    /**
     * Hides or shows the content.<p>
     * 
     * @param visible if true, shows the content, else hides it
     */
    public void setContentVisible(boolean visible) {

        m_content.setVisible(visible);
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsListItem#updateLayout()
     */
    @Override
    public void updateLayout() {

        m_content.updateLayout();
    }

    /**
     * Initializes this widget.<p>
     * 
     */
    @Override
    protected void init() {

        m_panel = uiBinder.createAndBindUi(this);
        initWidget(m_panel);
        m_content.addStyleName(CSS.listTreeItemContent());
    }

}
