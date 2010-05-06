/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsCategoryTreeItem.java,v $
 * Date   : $Date: 2010/05/06 09:27:20 $
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

package org.opencms.ade.galleries.client.ui;

import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;

import com.google.gwt.user.client.ui.Widget;

/**
 * Provides the specific tree list item for the categories list.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.
 */
public class CmsCategoryTreeItem extends CmsTreeItem {

    /** The reference to the checkbox. */
    private CmsCheckBox m_checkbox;

    /** The title text. */
    private String m_itemTitle;

    /** The subtitle text. */
    private String m_subTitle;

    /**
     * The empty constructor.<p>
     */
    public CmsCategoryTreeItem() {

        // empty
    }

    /**
     * Initialize the required members.<p>
     *
     * @param categoryPath the id
     * @param title the category title to set
     * @param subtitle the the category path to set
     */
    public void init(String categoryPath, String title, String subtitle) {

        setId(categoryPath);
        m_itemTitle = title;
        m_subTitle = subtitle;

    }

    /**
     * The constructor.<p>
     * 
     * @param showOpeners if true, show open/close icons
     * @param content the widgets to put into the tree item 
     */
    public CmsCategoryTreeItem(boolean showOpeners, Widget... content) {

        super(showOpeners, content);
        for (Widget i : content) {
            if (i instanceof CmsCheckBox) {
                m_checkbox = (CmsCheckBox)i;
            }
        }
    }

    /**
     * Returns the reference to the checkbox.<p>
     *
     * @return the checkbox
     */
    public CmsCheckBox getCheckbox() {

        return m_checkbox;
    }

    /**
     * Returns the children of this list item.<p>
     * 
     * @return the children list
     */
    public CmsList<? extends CmsListItem> getChildren() {

        return m_children;
    }

    /**
     * Returns the title of the item.<p>
     *
     * @return the itemTitle
     */
    public String getItemTitle() {

        return m_itemTitle;
    }

    /**
     * Returns the sub title of the item.<p>
     *
     * @return the subTitle
     */
    public String getSubTitle() {

        return m_subTitle;
    }

    /**
     * Sets the title of the item.<p>
     *
     * @param itemTitle the title to set
     */
    public void setItemTitle(String itemTitle) {

        m_itemTitle = itemTitle;
    }

    /**
     * Sets the sub title of the item.<p>
     *
     * @param subTitle the sub title to set
     */
    public void setSubTitle(String subTitle) {

        m_subTitle = subTitle;
    }
}