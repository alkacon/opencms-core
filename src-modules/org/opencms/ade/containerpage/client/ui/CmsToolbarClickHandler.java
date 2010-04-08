/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/CmsToolbarClickHandler.java,v $
 * Date   : $Date: 2010/04/08 06:01:24 $
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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.gwt.client.util.CmsDebugLog;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * The click handler for tool-bar button clicks. Calling 
 * {@link org.opencms.ade.containerpage.client.ui.I_CmsContainerpageToolbarButton#setActive(boolean)}.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsToolbarClickHandler implements ClickHandler {

    private I_CmsContainerpageToolbarButton m_currentButton;

    /**
     * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
     */
    public void onClick(ClickEvent event) {

        try {
            I_CmsContainerpageToolbarButton source = (I_CmsContainerpageToolbarButton)event.getSource();
            boolean active = source.isActive();
            if (!active) {
                if (m_currentButton != null) {
                    m_currentButton.setActive(false);
                }
                m_currentButton = source;
            } else {
                m_currentButton = null;
            }
            // setting the button active/inactive depending on its current state
            source.setActive(!active);
        } catch (Exception e) {
            CmsDebugLog.getInstance().printLine(e.getMessage());
        }

    }

}
