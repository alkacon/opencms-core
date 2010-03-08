/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsTreeList.java,v $
 * Date   : $Date: 2010/03/08 16:34:07 $
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

import java.util.Iterator;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Widget;

/**
 * A very basic tree list implementation to hold CmsListItems. Only showing and hiding the level indent is supported.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 * 
 */
public class CmsTreeList extends CmsList {

    /** The default indent. */
    private static final int DEFAULT_LEVEL_INDENT = 20;

    /** The indent of this instance. */
    private int m_levelIndent;

    /**
     * Constructor.
     */
    public CmsTreeList() {

        super();
        m_levelIndent = DEFAULT_LEVEL_INDENT;
    }

    /**
     * Constructor, setting an level indent other than the default 20px.<p>
     * 
     * @param levelIndent the indent to set
     */
    public CmsTreeList(int levelIndent) {

        super();
        m_levelIndent = levelIndent;
    }

    /**
     * Adds an item at the specified tree level.<p>
     * 
     * @param item the item to add
     * @param level the item tree level
     */
    public void addItem(CmsListItem item, int level) {

        super.addItem(item);
        item.getElement().setAttribute("level", String.valueOf(level));
        item.getElement().getStyle().setPaddingLeft(level * m_levelIndent, Unit.PX);
    }

    /**
     * Shows/hides the tree level indent.<p>
     * 
     * @param showLevels if the indent should be shown
     */
    public void toggleTreeLevels(boolean showLevels) {

        if (showLevels) {
            Iterator<Widget> it = m_list.iterator();
            while (it.hasNext()) {
                Widget w = it.next();
                String levelString = w.getElement().getAttribute("level");
                try {
                    int level = Integer.parseInt(levelString);
                    w.getElement().getStyle().setPaddingLeft(level * m_levelIndent, Unit.PX);
                } catch (Exception e) {
                    // may happen if no level was set, nothing to worry about
                }
            }
        } else {
            Iterator<Widget> it = m_list.iterator();
            while (it.hasNext()) {
                Widget w = it.next();
                w.getElement().getStyle().clearPaddingLeft();
            }
        }
    }
}
