/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/demo/client/Attic/CmsDemoTabbedPanel.java,v $
 * Date   : $Date: 2010/03/10 08:38:41 $
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

package org.opencms.gwt.demo.client;

import org.opencms.gwt.client.A_CmsEntryPoint;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.ui.CmsTabbedPanel.CmsTabLayout;
import org.opencms.gwt.shared.CmsListInfoBean;

import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * The demo entry point class for the tabbed panel layout.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsDemoTabbedPanel extends A_CmsEntryPoint {

    /**
     * @see org.opencms.gwt.client.A_CmsEntryPoint#onModuleLoad()
     */
    @Override
    public void onModuleLoad() {

        super.onModuleLoad();

        final CmsTabbedPanel tabbedPanel = new CmsTabbedPanel(CmsTabLayout.standard, false);

        // Tab1: list with list items
        CmsListInfoBean bean1 = new CmsListInfoBean();
        bean1.setTitle("List1");
        bean1.setSubTitle("Subtitle1");
        CmsListItem listItem1 = new CmsListItem(bean1);

        CmsListInfoBean bean2 = new CmsListInfoBean();
        bean2.setTitle("List2");
        bean2.setSubTitle("Subtitle2");
        CmsListItem listItem2 = new CmsListItem(bean2);

        CmsListInfoBean bean3 = new CmsListInfoBean();
        bean3.setTitle("List3");
        bean3.setSubTitle("Subtitle3");
        CmsListItem listItem3 = new CmsListItem(bean3);

        CmsList tab1 = new CmsList();
        tab1.addItem(listItem1);
        tab1.addItem(listItem2);
        tab1.addItem(listItem3);

        // Tab2: Flow panel with buttons
        final FlowPanel tab2 = new FlowPanel();
        Button button1 = new Button("Button1");
        final Button button2 = new Button("Button2");
        tab2.add(button1);
        tab2.add(button2);

        // Tab3: Vertical panel with a tabbed panel as bottom
        FlowPanel tab3 = new FlowPanel();
        tab3.setSize("100%", "100%");
        FlowPanel flow1 = new FlowPanel();
        flow1.setSize("100%", "60%");
        tab3.add(flow1);

        FlowPanel flow2 = new FlowPanel();
        flow2.setSize("100%", "38.5%");
        CmsTabbedPanel nestedTabbedPanel = new CmsTabbedPanel(CmsTabLayout.small, true);
        nestedTabbedPanel.add(new HTML("<div>"), "Infos");
        nestedTabbedPanel.add(new HTML("<div>"), "Properties");
        flow2.add(nestedTabbedPanel);
        tab3.add(flow2);

        // add tabs to tabbed panel
        tabbedPanel.add(tab3, "Nested");
        tabbedPanel.add(tab1, "List Example");
        tabbedPanel.add(tab2, "Buttons Example");

        // Tabbed Panel inside a horizontal panel 
        HorizontalPanel html = new HorizontalPanel();
        html.add(tabbedPanel);
        html.setCellHeight(tabbedPanel, "300px");
        html.setWidth("500px");

        tabbedPanel.addBeforeSelectionHandler(new BeforeSelectionHandler<Integer>() {

            public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {

                int index = tabbedPanel.getSelectedIndex();
                Button newBotton = new Button(String.valueOf(index));
                tab2.add(newBotton);
            }
        });

        RootPanel.get().add(html);
    }
}
