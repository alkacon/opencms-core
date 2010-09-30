/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/CmsToolbarRemoveButton.java,v $
 * Date   : $Date: 2010/09/30 13:32:25 $
 * Version: $Revision: 1.8 $
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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.client.CmsContainerpageHandler;
import org.opencms.gwt.client.ui.I_CmsButton;

import com.google.gwt.event.dom.client.ClickEvent;

/**
 * The remove button holding all remove related methods.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.8 $
 * 
 * @since 8.0.0
 */
public class CmsToolbarRemoveButton extends A_CmsToolbarOptionButton {

    /**
     * Constructor.<p>
     * 
     * @param handler the container-page handler
     */
    public CmsToolbarRemoveButton(CmsContainerpageHandler handler) {

        super(I_CmsButton.ButtonData.REMOVE, handler);
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.A_CmsToolbarOptionButton#hasPermissions(org.opencms.ade.containerpage.client.ui.CmsContainerPageElement)
     */
    @Override
    public boolean hasPermissions(CmsContainerPageElement element) {

        // TODO: get permissions from element
        return true;
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.A_CmsToolbarOptionButton#onElementClick(com.google.gwt.event.dom.client.ClickEvent, org.opencms.ade.containerpage.client.ui.CmsContainerPageElement)
     */
    @Override
    public void onElementClick(ClickEvent event, CmsContainerPageElement element) {

        getHandler().removeElement(element);
    }
}