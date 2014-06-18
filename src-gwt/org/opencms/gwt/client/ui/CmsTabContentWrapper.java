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

package org.opencms.gwt.client.ui;

import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Wrapper for a tab content widget which is used to bypass the problem that measuring the direct tab content's size doesn't work.<p>
 *
 */
public class CmsTabContentWrapper extends SimplePanel {

    /**
     * Creates a new wrapper widget.<p>
     * 
     * @param widget the widget to wrap
     */
    public CmsTabContentWrapper(Widget widget) {

        super(widget);
        addStyleName("cmsTabContentWrapper");
    }

    /**
     * @see com.google.gwt.user.client.ui.SimplePanel#getWidget()
     */
    @Override
    public Widget getWidget() {

        return super.getWidget();
    }
}
