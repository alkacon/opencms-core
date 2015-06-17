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

package org.opencms.ade.galleries.client;

import org.opencms.ade.galleries.client.ui.CmsResultListItem;
import org.opencms.ade.galleries.shared.CmsResultItemBean;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.ui.I_CmsAutoHider;

import com.google.gwt.user.client.ui.Widget;

/**
 * A handler interface which allows the gallery dialog to interact with the context it is used in, e.g. the container page editor.
 */
public interface I_CmsGalleryHandler {

    /**
     * This method is used to disable drag-and-drop for specific results.<p>
     * 
     * If this returns false, drag and drop should be disabled for the result (however, if true is returned,
     * this does not automatically mean that drag and drop should be enabled.)
     *  
     * @param resultBean the result for which drag-and-drop feasibility should be checked  
     * @return false if DnD should be prohibited for the element, else true 
     */
    boolean filterDnd(CmsResultItemBean resultBean);

    /** 
     * Gets an additional widget to display in the type tab.<p>
     * 
     * @return the additional widget to display 
     */
    Widget getAdditionalTypeTabControl();

    /**
     * Gets the auto-hide parent.<p>
     * 
     * @return the auto-hide parent 
     */
    I_CmsAutoHider getAutoHideParent();

    /**
     * Gets the drag-and-drop handler for the result list.
     * 
     * @return the drag-and-drop handler 
     */
    CmsDNDHandler getDndHandler();

    /** 
     * Processes a result list item.<p>
     *  
     * @param item the item to process 
     */
    void processResultItem(CmsResultListItem item);

}
