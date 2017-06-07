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

package org.opencms.ui.components.extensions;

import org.opencms.ui.shared.components.CmsScrollPositionCssState;

import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractSingleComponentContainer;

/**
 * Extension to add a CSS class to any component depending on it's scroll position.<p>
 */
public class CmsScrollPositionCss extends AbstractExtension {

    /** The serial version id. */
    private static final long serialVersionUID = 3382283389411937891L;

    /**
     * Constructor.<p>
     *
     * @param componentContainer the component to extend
     * @param scrollBarrier the scroll barrier
     * @param barrierMargin the margin
     * @param styleName the style name to set beyond the scroll barrier
     */
    public CmsScrollPositionCss(
        AbstractComponent componentContainer,
        int scrollBarrier,
        int barrierMargin,
        String styleName) {
        super.extend(componentContainer);
        getState().setScrollBarrier(scrollBarrier);
        getState().setBarrierMargin(barrierMargin);
        getState().setStyleName(styleName);
    }

    /**
     * Adds the scroll position CSS extension to the given component
     *
     * @param componentContainer the component to extend
     * @param scrollBarrier the scroll barrier
     * @param barrierMargin the margin
     * @param styleName the style name to set beyond the scroll barrier
     */
    @SuppressWarnings("unused")
    public static void addTo(
        AbstractSingleComponentContainer componentContainer,
        int scrollBarrier,
        int barrierMargin,
        String styleName) {

        new CmsScrollPositionCss(componentContainer, scrollBarrier, barrierMargin, styleName);
    }

    /**
     * @see com.vaadin.server.AbstractClientConnector#getState()
     */
    @Override
    protected CmsScrollPositionCssState getState() {

        return (CmsScrollPositionCssState)super.getState();
    }

}
