/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsHoverHandler.java,v $
 * Date   : $Date: 2010/04/07 14:46:20 $
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

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;

/**
 * Event handler to toggle the {@link org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.I_CmsStateCss#cmsHovering()} class on mouse out/over.<p>
 * 
 * @author Tobias Herrmann
 * @author Michael Moossen
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsHoverHandler implements MouseOutHandler, MouseOverHandler {

    /** The owner widget. */
    protected Widget m_owner;

    /** Timer to achieve the hover intent effect. */
    protected Timer m_timer;

    /**
     * Constructor.<p>
     * 
     * @param owner the owner widget
     */
    CmsHoverHandler(Widget owner) {

        m_owner = owner;
    }

    /**
     * @see com.google.gwt.event.dom.client.MouseOutHandler#onMouseOut(com.google.gwt.event.dom.client.MouseOutEvent)
     */
    public void onMouseOut(MouseOutEvent event) {

        if (m_timer != null) {
            m_timer.cancel();
            m_timer = null;
        } else {
            m_owner.removeStyleName(I_CmsLayoutBundle.INSTANCE.stateCss().cmsHovering());
        }
    }

    /**
     * @see com.google.gwt.event.dom.client.MouseOverHandler#onMouseOver(com.google.gwt.event.dom.client.MouseOverEvent)
     */
    public void onMouseOver(MouseOverEvent event) {

        if (m_timer != null) {
            return;
        }
        m_timer = new Timer() {

            /**
             * @see com.google.gwt.user.client.Timer#run()
             */
            @Override
            public void run() {

                m_owner.addStyleName(I_CmsLayoutBundle.INSTANCE.stateCss().cmsHovering());
                m_timer = null;
            }
        };
        m_timer.schedule(200);
    }
}
