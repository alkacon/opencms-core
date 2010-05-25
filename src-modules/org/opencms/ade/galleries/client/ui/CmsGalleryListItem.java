/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsGalleryListItem.java,v $
 * Date   : $Date: 2010/05/25 12:36:33 $
 * Version: $Revision: 1.5 $
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

import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.input.CmsCheckBox;

import com.google.gwt.user.client.ui.Widget;

/**
 * Provides the specific list item for the galleries list.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.5 $
 * 
 * @since 8.0.
 */
public class CmsGalleryListItem extends CmsListItem {

    /** The title text. */
    private String m_itemTitle;

    /** The subtitle text. */
    private String m_subTitle;

    /**
     * Creates a new gallery list item with a check box and a main widget.<p>
     * 
     * @param checkbox the check box
     * @param mainWidget the main widget
     */
    public CmsGalleryListItem(CmsCheckBox checkbox, Widget mainWidget) {

        initContent(checkbox, mainWidget);
    }

    /**
     * Creates a new gallery list item with a main widget.<p>
     * 
     * @param mainWidget the main widget
     */
    public CmsGalleryListItem(Widget mainWidget) {

        initContent(mainWidget);
    }

    /**
     * Returns the itemTitle.<p>
     *
     * @return the itemTitle
     */
    public String getItemTitle() {

        return m_itemTitle;
    }

    /**
     * Returns the subTitle.<p>
     *
     * @return the subTitle
     */
    public String getSubTitle() {

        return m_subTitle;
    }

    /**
     * Sets the itemTitle.<p>
     *
     * @param itemTitle the itemTitle to set
     */
    public void setItemTitle(String itemTitle) {

        m_itemTitle = itemTitle;
    }

    /**
     * Sets the subTitle.<p>
     *
     * @param subTitle the subTitle to set
     */
    public void setSubTitle(String subTitle) {

        m_subTitle = subTitle;
    }
}