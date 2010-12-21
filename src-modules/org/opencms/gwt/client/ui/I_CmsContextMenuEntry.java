/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/I_CmsContextMenuEntry.java,v $
 * Date   : $Date: 2010/12/21 10:23:32 $
 * Version: $Revision: 1.2 $
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

import com.google.gwt.user.client.Command;

/**
 * Interface for a context menu entry.<p>
 * 
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.2 $
 * 
 * @since version 8.0.0
 */
public interface I_CmsContextMenuEntry {

    /**
     * Returns the command for the entry.<p>
     * 
     * @return the command
     */
    Command getCommand();

    /**
     * Returns the image class for the icon in front of the label.<p>
     *
     * @return the image class
     */
    String getImageClass();

    /**
     * Returns the image path for the icon in front of the label.<p>
     *
     * @return the image path
     */
    String getImagePath();

    /**
     * Returns the JSP path for the command generation.<p>
     *
     * @return the JSP path
     */
    String getJspPath();

    /**
     * Returns the label (text) for the menu entry.<p>
     *
     * @return the label
     */
    String getLabel();

    /**
     * Returns the name of the entry.<p>
     * 
     * @return the name of the entry
     */
    String getName();

    /**
     * Returns the reason if the entry is de-activated .<p>
     *
     * @return the reason
     */
    String getReason();

    /**
     * Returns a list of {@link I_CmsContextMenuEntry} objects.<p>
     *
     * @return the sub menu entries
     */
    List<I_CmsContextMenuEntry> getSubMenu();

    /**
     * Returns <code>true</code> if this menu entry has a sub menu <code>false</code> otherwise.<p>
     * 
     * @return <code>true</code> if this menu entry has a sub menu <code>false</code> otherwise
     */
    boolean hasSubMenu();

    /**
     * Returns <code>true</code> if this menu entry is active <code>false</code> otherwise.<p>
     *
     * @return <code>true</code> if this menu entry is active <code>false</code> otherwise
     */
    boolean isActive();

    /**
     * Returns <code>true</code> if this menu entry is a separator <code>false</code> otherwise.<p>
     *
     * @return <code>true</code> if this menu entry is a separator <code>false</code> otherwise
     */
    boolean isSeparator();

    /**
     * Returns <code>true</code> if this menu entry is visible <code>false</code> otherwise.<p>
     *
     * @return <code>true</code> if this menu entry is visible <code>false</code> otherwise
     */
    boolean isVisible();

}