/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;

/**
 * Event handler to toggle the {@link org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.I_CmsStateCss#cmsHovering()} class on mouse out/over.<p>
 *
 * @since 8.0.0
 */
public class CmsClassHoverHandler extends A_CmsHoverHandler {

    /** The owner element. */
    protected Element m_owner;

    /**
     * Constructor.<p>
     *
     * @param owner the owner element
     */
    public CmsClassHoverHandler(Element owner) {

        m_owner = owner;
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsHoverHandler#onHoverIn(com.google.gwt.event.dom.client.MouseOverEvent)
     */
    @Override
    protected void onHoverIn(MouseOverEvent event) {

        m_owner.addClassName(I_CmsLayoutBundle.INSTANCE.stateCss().cmsHovering());
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsHoverHandler#onHoverOut(com.google.gwt.event.dom.client.MouseOutEvent)
     */
    @Override
    protected void onHoverOut(MouseOutEvent event) {

        m_owner.removeClassName(I_CmsLayoutBundle.INSTANCE.stateCss().cmsHovering());
    }
}
