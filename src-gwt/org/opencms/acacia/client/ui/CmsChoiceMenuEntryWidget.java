/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.acacia.client.ui;

import org.opencms.acacia.client.CmsButtonBarHandler;
import org.opencms.acacia.client.CmsChoiceMenuEntryBean;
import org.opencms.acacia.client.css.I_CmsLayoutBundle;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;

/**
 * A menu entry widget for selecting choices for complex choice values.<p>
 */
public class CmsChoiceMenuEntryWidget extends Composite {

    /** The root attribute choice menu to which this entry belongs. */
    private CmsAttributeChoiceWidget m_attributeChoiceWidget;

    /** The bean to which this entry belongs. */
    private CmsChoiceMenuEntryBean m_entryBean;

    /** The callback to invoke when a widget is selected. */
    private AsyncCallback<CmsChoiceMenuEntryBean> m_selectCallback;

    /** The submenu to which this entry widget belongs. */
    private CmsChoiceSubmenu m_submenu;

    /**
      * Creates a new menu entry instance.<p>
      * 
      * @param label the entry label 
      * @param help the entry help text
      * @param menuEntry the menu entry bean 
      * @param selectHandler the select handler 
      * @param choiceWidget the root choice menu 
      * @param submenu the submenu for which this entry is being created 
      */
    public CmsChoiceMenuEntryWidget(
        String label,
        String help,
        final CmsChoiceMenuEntryBean menuEntry,
        final AsyncCallback<CmsChoiceMenuEntryBean> selectHandler,
        CmsAttributeChoiceWidget choiceWidget,
        CmsChoiceSubmenu submenu) {

        HTML baseWidget = new HTML(label);
        initWidget(baseWidget);
        setStyleName(I_CmsLayoutBundle.INSTANCE.attributeChoice().choice());
        m_entryBean = menuEntry;
        m_selectCallback = selectHandler;
        m_submenu = submenu;
        m_attributeChoiceWidget = choiceWidget;
        setTitle(help);
        if (menuEntry.isLeaf()) {
            baseWidget.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {

                    selectHandler.onSuccess(menuEntry);
                    CmsButtonBarHandler.INSTANCE.closeAll();

                }
            });
        }
        addDomHandler(CmsButtonBarHandler.INSTANCE, MouseOverEvent.getType());
        addDomHandler(CmsButtonBarHandler.INSTANCE, MouseOutEvent.getType());
    }

    /**
     * Gets the root choice menu.<p>
     * 
     * @return the root choice menu 
     */
    public CmsAttributeChoiceWidget getAttributeChoiceWidget() {

        return m_attributeChoiceWidget;
    }

    /** 
     * Gets the menu entry bean.<p>
     * 
     * @return the menu entry bean 
     */
    public CmsChoiceMenuEntryBean getEntryBean() {

        return m_entryBean;
    }

    /**
     * Gets the select handler.<p>
     * 
     * @return the select handler 
     */
    public AsyncCallback<CmsChoiceMenuEntryBean> getSelectHandler() {

        return m_selectCallback;
    }

    /**
     * Gets the submenu to which this entry belongs (or null if it belongs to a root menu).<p>
     * 
     * @return the submenu of this entry 
     */
    public CmsChoiceSubmenu getSubmenu() {

        return m_submenu;
    }
}
