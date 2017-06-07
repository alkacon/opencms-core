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

package org.opencms.ui.client;

import org.opencms.ui.shared.CmsVaadinConstants;

import com.google.gwt.user.client.Element;
import com.vaadin.client.VLoadingIndicator;

/**
 * Replacement for the standard Vaadin loading indicator which adds some custom HTML.<p>
 */
@SuppressWarnings("deprecation")
public class CmsLoadingIndicator extends VLoadingIndicator {

    /**
     * @see com.vaadin.client.VLoadingIndicator#getElement()
     */
    @Override
    public Element getElement() {

        Element element = super.getElement();
        if (element.getChildCount() == 0) {
            element.setInnerHTML(CmsVaadinConstants.LOADING_INDICATOR_HTML);
        }
        return element;
    }

}
