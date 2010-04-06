/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsToolbar.java,v $
 * Date   : $Date: 2010/04/06 08:24:12 $
 * Version: $Revision: 1.3 $
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a tool-bar to be shown at the top of a page.
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 * 
 */
public class CmsToolbar extends Composite {

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    interface I_CmsToolbarUiBinder extends UiBinder<Widget, CmsToolbar> {
        // GWT interface, nothing to do here
    }

    /** The ui-binder instance for this class. */
    private static I_CmsToolbarUiBinder uiBinder = GWT.create(I_CmsToolbarUiBinder.class);

    /** DIV to hold left-side buttons associated with the tool-bar. */
    @UiField
    FlowPanel m_buttonPanelLeft;

    /** DIV to hold right-side buttons associated with the tool-bar. */
    @UiField
    FlowPanel m_buttonPanelRight;

    /** A confirm dialog. */
    CmsConfirmDialog m_confirm;

    /** A pop up dialog. */
    CmsPopupDialog m_popup;

    /**
     * Constructor.<p>
     */
    public CmsToolbar() {

        I_CmsLayoutBundle.INSTANCE.toolbarCss().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));

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
