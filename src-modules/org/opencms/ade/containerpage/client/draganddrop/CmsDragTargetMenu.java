/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/draganddrop/Attic/CmsDragTargetMenu.java,v $
 * Date   : $Date: 2010/04/14 14:33:47 $
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

package org.opencms.ade.containerpage.client.draganddrop;

import org.opencms.gwt.client.draganddrop.I_CmsDragHandler;
import org.opencms.gwt.client.util.CmsPositionBean;

import com.google.gwt.user.client.ui.AbsolutePanel;

/**
 * A widget serving as a drag and drop drop-zone within the tool-bar menu.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsDragTargetMenu extends AbsolutePanel implements I_CmsDragTargetContainer {

    /**
     * Constructor.<p>
     */
    public CmsDragTargetMenu() {

        super();

        // overriding overflow hidden set by AbsolutePanel
        getElement().getStyle().clearOverflow();
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragTarget#getPositionInfo()
     */
    public CmsPositionBean getPositionInfo() {

        return CmsPositionBean.generatePositionInfo(this);
    }

    /**
     * @see org.opencms.ade.containerpage.client.draganddrop.I_CmsDragTargetContainer#highlightContainer()
     */
    public void highlightContainer() {

        // the menu drop-zone will not be highlighted, so there is nothing to do here

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragTarget#onDragEnter(org.opencms.gwt.client.draganddrop.I_CmsDragHandler)
     */
    public void onDragEnter(I_CmsDragHandler<?, ?> handler) {

        // nothing to do here

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragTarget#onDragInside(org.opencms.gwt.client.draganddrop.I_CmsDragHandler)
     */
    public void onDragInside(I_CmsDragHandler<?, ?> handler) {

        // nothing to do here

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragTarget#onDragLeave(org.opencms.gwt.client.draganddrop.I_CmsDragHandler)
     */
    public void onDragLeave(I_CmsDragHandler<?, ?> handler) {

        // nothing to do here

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragTarget#onDrop(org.opencms.gwt.client.draganddrop.I_CmsDragHandler)
     */
    public void onDrop(I_CmsDragHandler<?, ?> handler) {

        // nothing to do here

    }

    /**
     * @see org.opencms.ade.containerpage.client.draganddrop.I_CmsDragTargetContainer#refreshHighlighting()
     */
    public void refreshHighlighting() {

        // the menu drop-zone will not be highlighted, so there is nothing to do here

    }

    /**
     * @see org.opencms.ade.containerpage.client.draganddrop.I_CmsDragTargetContainer#removeHighlighting()
     */
    public void removeHighlighting() {

        // the menu drop-zone will not be highlighted, so there is nothing to do here

    }

}
