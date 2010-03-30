/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsListItem.java,v $
 * Date   : $Date: 2010/03/30 14:08:37 $
 * Version: $Revision: 1.8 $
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

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a UI list item.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.8 $
 * 
 * @since 8.0.0
 */
public class CmsListItem extends Composite {

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    /* default */interface I_CmsListItemUiBinder extends UiBinder<CmsFlowPanel, CmsListItem> {
        // GWT interface, nothing to do here
    }

    /** The ui-binder instance for this class. */
    private static I_CmsListItemUiBinder uiBinder = GWT.create(I_CmsListItemUiBinder.class);

    /** The underlying panel. */
    protected Panel m_panel;

    /**
     * Constructor.<p>
     */
    public CmsListItem() {

        init();
    }

    /**
     * Constructor.<p>
     * 
     * @param widget the widget to use
     */
    public CmsListItem(CmsListItemWidget widget) {

        this();
        add(widget);
    }

    /**
     * Adds a widget to this list item.<p>
     * 
     * @param w the widget to add
     */
    public void add(Widget w) {

        m_panel.add(w);
    }

    /**
     * Method which should be called to update the layout of the item.<p>
     * 
     * This is only used by the CmsListTreeItem class.
     */
    public void updateLayout() {

    }

    /**
     * Initializes this list item.<p>
     */
    protected void init() {

        m_panel = uiBinder.createAndBindUi(this);
        initWidget(m_panel);
    }
}
