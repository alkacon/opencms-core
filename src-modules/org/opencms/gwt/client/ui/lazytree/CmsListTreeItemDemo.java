/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/lazytree/Attic/CmsListTreeItemDemo.java,v $
 * Date   : $Date: 2010/03/18 09:31:16 $
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

package org.opencms.gwt.client.ui.lazytree;

import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.shared.CmsListInfoBean;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;

/**
 * Demo panel for list tree items.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision 1.0$
 * 
 * @since 8.0.0
 * 
 */
public class CmsListTreeItemDemo extends Composite {

    /**
     * Constructs a new instance.<p>
     * 
     */
    public CmsListTreeItemDemo() {

        FlowPanel panel = new FlowPanel();
        init(panel);
        initWidget(panel);
    }

    /**
     * Helper method for making a new list tree item for the purpose of the demo.<p>
     * 
     * @param a the string to display in the list item
     * @param children the children of the list item
     *  
     * @return the new list tree item 
     */
    private CmsListTreeItem t(String a, CmsListTreeItem... children) {

        CmsListInfoBean info = new CmsListInfoBean();
        info.setTitle(a);
        info.setSubTitle(a);
        CmsListItemWidget l = new CmsListItemWidget(info);
        CmsListTreeItem item = new CmsListTreeItem(true, new CmsCheckBox(), l);
        for (CmsListTreeItem child : children) {
            item.addChild(child);
        }
        return item;
    }

    /**
     * Initializes the demo tree list items and puts them into a panel.<p>
     * 
     * @param panel the panel into which the demo list tree items should be put 
     */
    private void init(Panel panel) {

        CmsList list = new CmsList();
        list.addItem(t("a", t("b", t("c", t("1"), t("2"), t("3")), t("d")), t("e", t("f", t("g", t("h", t("i")))))));
        panel.add(list);
    }
}
