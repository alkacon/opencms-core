/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsSimpleListItem.java,v $
 * Date   : $Date: 2010/05/06 13:09:44 $
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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Simple list item which uses a CmsFloatDecoratedPanel for layout.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 8.0.0
 * 
 */
public class CmsSimpleListItem extends Composite implements I_CmsListItem {

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    protected interface I_CmsSimpleListItemUiBinder extends UiBinder<Panel, CmsSimpleListItem> {
        // GWT interface, nothing to do here
    }

    /** The ui-binder instance for this class. */
    private static I_CmsSimpleListItemUiBinder uiBinder = GWT.create(I_CmsSimpleListItemUiBinder.class);

    /** The content area. */
    @UiField
    protected CmsFloatDecoratedPanel m_content;

    /** The logical id, it is not the HTML id. */
    protected String m_id;

    /** This widgets panel. */
    protected Panel m_panel;

    /** 
     * Default constructor.<p>
     */
    public CmsSimpleListItem() {

        m_panel = uiBinder.createAndBindUi(this);
        initWidget(m_panel);
        m_content.addStyleName(I_CmsLayoutBundle.INSTANCE.listTreeCss().listTreeItemContent());
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

    /**
     * @see org.opencms.gwt.client.ui.I_CmsListItem#add(com.google.gwt.user.client.ui.Widget)
     */
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
     * @see org.opencms.gwt.client.ui.I_CmsListItem#getId()
     */
    public String getId() {

        return m_id;
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
     * @see org.opencms.gwt.client.ui.I_CmsListItem#setId(java.lang.String)
     */
    public void setId(String id) {

        m_id = id;
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsTruncable#truncate(java.lang.String, int)
     */
    public void truncate(String textMetricsKey, int labelWidth) {

        int width = labelWidth - 4; // just to be on the safe side
        for (Widget widget : m_panel) {
            if (widget instanceof CmsListItemWidget) {
                ((CmsListItemWidget)widget).truncate(textMetricsKey, width);
            }
            if (widget instanceof CmsList<?>) {
                ((CmsList<?>)widget).truncate(textMetricsKey, width - 25); // 25px indentation
            }
            if (widget instanceof CmsFloatDecoratedPanel) {
                ((CmsFloatDecoratedPanel)widget).truncate(textMetricsKey, width);
            }
        }

    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsListItem#updateLayout()
     */
    public void updateLayout() {

        m_content.updateLayout();
    }
}
