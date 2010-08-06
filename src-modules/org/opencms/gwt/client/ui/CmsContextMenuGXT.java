/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsContextMenuGXT.java,v $
 * Date   : $Date: 2010/08/06 14:08:14 $
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

import java.util.List;

import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.impl.ClippedImagePrototype;

/**
 * A implementation for a context menu.<p>
 * 
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.1 $
 * 
 * @since version 8.0.0
 */
public class CmsContextMenuGXT extends Menu {

    /**
     * Constructor.<p>
     * 
     * @param menuData the data structure for the context menu 
     */
    public CmsContextMenuGXT(List<I_CmsContextMenuEntry> menuData) {

        for (I_CmsContextMenuEntry entry : menuData) {
            CmsContextMenuItemGXT item = null;
            if (entry.isSeparator()) {
                add(new SeparatorMenuItem());
            } else if (entry.hasSubMenu()) {
                item = new CmsContextMenuItemGXT(entry.getLabel());
                item.setSubMenu(new CmsContextMenuGXT(entry.getSubMenu()));
            } else {
                item = new CmsContextMenuItemGXT(entry.getLabel());
                if (entry.getLabel().equals("Edit")) {
                    item.setCmd(new Command() {

                        /**
                         * @see com.google.gwt.user.client.Command#execute()
                         */
                        public void execute() {

                            new CmsAvailabilityWindow().show();
                        }
                    });
                } else {
                    item.setCmd(entry.getCommand());
                }
            }
            if (item != null) {
                item.setIcon(new ClippedImagePrototype(entry.getImagePath(), 0, 0, 16, 16));
                if (!entry.isActive()) {
                    item.setEnabled(false);
                    item.setTitle(entry.getReason());
                }
                add(item);
            }
        }
    }
}
