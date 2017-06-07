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

package org.opencms.ui.sitemap;

import org.opencms.ui.FontOpenCms;
import org.opencms.ui.components.OpenCmsTheme;

import com.vaadin.ui.Button;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Button used for opening / closing tree levels.<p>
 */
public class CmsSitemapTreeNodeOpener extends Button {

    /** 'Minus' icon for the open state. */
    public static final String MINUS = new String(new int[] {FontOpenCms.TREE_MINUS.getCodepoint()}, 0, 1);

    /** 'Plus' icon for the closed state. */
    public static final String PLUS = new String(new int[] {FontOpenCms.TREE_PLUS.getCodepoint()}, 0, 1);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance.<p<
     */
    public CmsSitemapTreeNodeOpener() {
        super();
        addStyleName("opencms-font-icon");
        addStyleName("o-sitemap-tree-opener");
        addStyleName(ValoTheme.BUTTON_BORDERLESS);
        addStyleName(OpenCmsTheme.BUTTON_UNPADDED);
        setStyleOpen(false);
    }

    /**
     * Sets the style of the button.<p>
     *
     * @param open true if the button should be set to 'open' state, false for the 'closed' state
     */
    public void setStyleOpen(boolean open) {

        setCaption(open ? MINUS : PLUS);
    }
}
