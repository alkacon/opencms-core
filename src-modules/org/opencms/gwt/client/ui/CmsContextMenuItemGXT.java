/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsContextMenuItemGXT.java,v $
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

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.Command;

/**
 * A cms context menu item to use with gxt.<p>
 * 
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.1 $
 * 
 * @since version 8.0.0
 */
public class CmsContextMenuItemGXT extends MenuItem {

    /** The command to execute. */
    private Command m_cmd;

    /**
     * @param label
     */
    public CmsContextMenuItemGXT(String label) {

        super(label);

    }

    /**
     * @see com.extjs.gxt.ui.client.widget.menu.Item#handleClick(com.extjs.gxt.ui.client.event.ComponentEvent)
     */
    @Override
    protected void handleClick(ComponentEvent be) {

        super.handleClick(be);
        m_cmd.execute();

    }

    /**
     * Returns the cmd.<p>
     *
     * @return the cmd
     */
    public Command getCmd() {

        return m_cmd;
    }

    /**
     * Sets the cmd.<p>
     *
     * @param cmd the cmd to set
     */
    public void setCmd(Command cmd) {

        m_cmd = cmd;
    }

}
