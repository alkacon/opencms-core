/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/Attic/TestCmsTabbedPanel.java,v $
 * Date   : $Date: 2010/03/09 10:25:41 $
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

package org.opencms.gwt.client;

import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.ui.CmsTabbedPanel.CmsTabLayout;
import org.opencms.gwt.shared.CmsListInfoBean;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;

public class TestCmsTabbedPanel extends A_CmsEntryPoint {

    @Override
    public void onModuleLoad() {

        // TODO: Auto-generated method stub
        super.onModuleLoad();

        CmsTabbedPanel tabbedPanel = new CmsTabbedPanel(CmsTabLayout.standard);

        // list with list items
        CmsListInfoBean bean = new CmsListInfoBean();
        bean.setTitle("List1");
        bean.setSubTitle("Subtitle1");
        CmsListItem listItem = new CmsListItem(bean);

        CmsListInfoBean bean2 = new CmsListInfoBean();
        bean.setTitle("List2");
        bean.setSubTitle("Subtitle2");
        CmsListItem listItem2 = new CmsListItem(bean);

        CmsListInfoBean bean3 = new CmsListInfoBean();
        bean.setTitle("List3");
        bean.setSubTitle("Subtitle3");
        CmsListItem listItem3 = new CmsListItem(bean);

        CmsList list = new CmsList();
        list.addItem(listItem);
        list.addItem(listItem2);
        list.addItem(listItem3);

        // Flow panel with buttons
        FlowPanel flowPanel = new FlowPanel();
        Button button1 = new Button("Button1");
        Button button2 = new Button("Button2");
        flowPanel.add(button1);

        tabbedPanel.add(list, "List Example");
        tabbedPanel.add(flowPanel, "Buttons Example");

        //FlowPanel html = new FlowPanel();
        HorizontalPanel html = new HorizontalPanel();
        html.add(tabbedPanel);        
        html.setCellHeight(tabbedPanel, "300px");
        html.setWidth("500px");
        RootPanel.get().add(html);
    }
}
