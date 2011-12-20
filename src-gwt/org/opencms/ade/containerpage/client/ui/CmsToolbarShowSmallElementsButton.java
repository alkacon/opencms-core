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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.client.CmsContainerpageHandler;
import org.opencms.gwt.client.ui.A_CmsToolbarButton;
import org.opencms.gwt.client.ui.I_CmsButton;

/**
 * The toolbar button used for enlarging small elements (i.e. elements that don't have a sufficient height for the direct
 * edit buttons).
 */
public class CmsToolbarShowSmallElementsButton extends A_CmsToolbarButton<CmsContainerpageHandler> {

    /**
     * Creates a new toolbar button instance.<p>
     * 
     * @param handler the containerpage handler 
     */
    public CmsToolbarShowSmallElementsButton(CmsContainerpageHandler handler) {

        super(I_CmsButton.ButtonData.SHOWSMALL, handler);
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#onToolbarActivate()
     */
    public void onToolbarActivate() {

        // do nothing, logic will be handled by onToolbarClick()
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsToolbarButton#onToolbarClick()
     */
    @Override
    public void onToolbarClick() {

        getHandler().enlargeSmallElements();
        setDown(false);
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#onToolbarDeactivate()
     */
    public void onToolbarDeactivate() {

        // do nothing, logic will be handled by onToolbarClick()
    }

}
