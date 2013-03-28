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

package org.opencms.gwt.client.ui.resourceinfo;

import org.opencms.ade.containerpage.client.Messages;
import org.opencms.gwt.client.CmsGwtConstants;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsScrollPanel;
import org.opencms.gwt.client.ui.CmsSimpleListItem;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsResourceStatusBean;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

/** 
 * Widget which shows which contents refer to a resource.<p> 
 */
public class CmsResourceUsageView extends Composite {

    /** The panel containing the resource boxes. */
    CmsList<CmsListItem> m_panel = new CmsList<CmsListItem>();

    /** True if the resource boxes have already been created. */
    private boolean m_filled;

    /** Timer for notifying the scroll panel of size changes. */
    private Timer m_resizeTimer = new Timer() {

        @Override
        public void run() {

            CmsDomUtil.resizeAncestor(m_panel);
        }
    };

    /** The resource status from which we get the related resources to display. */
    private CmsResourceStatusBean m_statusBean;

    /** 
     * Creates a new widget instance.<p>
     * 
     * @param status the resource status from which we get the related resources to display.  
     */
    public CmsResourceUsageView(CmsResourceStatusBean status) {

        CmsScrollPanel scroller = GWT.create(CmsScrollPanel.class);
        m_panel.setWidth("487px");
        scroller.setWidth("507px");
        scroller.add(m_panel);
        m_statusBean = status;
        initWidget(scroller);
    }

    /**
     * Creates and renders the resource boxes for the related resources.<p>
     */
    public void fill() {

        m_panel.clear();
        if (m_statusBean.getRelationSources().isEmpty()) {
            CmsSimpleListItem item = new CmsSimpleListItem();
            item.add(new Label(Messages.get().key(Messages.GUI_USAGE_EMPTY_0)));
            m_panel.add(item);
        } else {
            for (CmsListInfoBean sourceBean : m_statusBean.getRelationSources()) {
                CmsListItemWidget itemWidget = new CmsListItemWidget(sourceBean);
                CmsListItem item = new CmsListItem(itemWidget);
                if (CmsGwtConstants.TYPE_CONTAINERPAGE.equals(sourceBean.getResourceType())) {
                    if (sourceBean.getLink() != null) {
                        final String link = sourceBean.getLink();
                        itemWidget.setIconCursor(Cursor.POINTER);
                        itemWidget.addIconClickHandler(new ClickHandler() {

                            public void onClick(ClickEvent e) {

                                Window.open(link, "_blank", "");
                            }
                        });
                    }
                }
                m_panel.add(item);
            }
        }

    }

    /**
     * This method should be called when this view is shown.<p>
     */
    public void onSelect() {

        if (!m_filled) {
            // only produce the widgets when they actually need to be shown 
            fill();
            m_filled = true;
        }
        m_resizeTimer.schedule(1);
    }
}
