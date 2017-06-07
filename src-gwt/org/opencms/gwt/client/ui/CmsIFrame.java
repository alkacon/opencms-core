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

import org.opencms.gwt.client.util.CmsDomUtil;

import com.google.gwt.user.client.ui.Frame;

/**
 * IFrame widget, extends the GWT core {@link com.google.gwt.user.client.ui.Frame} widget.<p>
 *
 * This is necessary, as {@link com.google.gwt.user.client.ui.Frame} won't assign a name attribute on element creation.
 * This is needed, ass setting or changing this attribute later won't have any effect in IE.<p>
 *
 * @since 8.0.0
 */
public class CmsIFrame extends Frame {

    /**
     * Constructor.<p>
     *
     * @param name the iFrame name attribute value
     */
    public CmsIFrame(String name) {

        super(CmsDomUtil.createIFrameElement(name));
    }

    /**
     * Constructor.<p>
     *
     * @param name the iFrame name attribute value
     * @param url the iFrame src attribute value
     */
    public CmsIFrame(String name, String url) {

        this(name);
        setUrl(url);
    }
}
