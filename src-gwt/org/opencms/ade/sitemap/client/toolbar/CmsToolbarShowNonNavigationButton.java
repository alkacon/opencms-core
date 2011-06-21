/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.sitemap.client.toolbar;

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.CmsSitemapView.EditorMode;
import org.opencms.ade.sitemap.client.Messages;
import org.opencms.gwt.client.ui.CmsToggleButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * The sitemap toolbar change sitemap editor mode button.<p>
 * 
 * @since 8.0.0
 */
public class CmsToolbarShowNonNavigationButton extends CmsToggleButton {

    /**
     * Constructor.<p>
     */
    public CmsToolbarShowNonNavigationButton() {

        setImageClass(I_CmsButton.ButtonData.SITEMAP.getIconClass());
        setTitle(Messages.get().key(Messages.GUI_NON_NAVIGATION_BUTTON_TITLE_0));
        setButtonStyle(ButtonStyle.IMAGE, null);

        addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                showNonNavigationResources(isDown());
            }
        });
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsToggleButton#setDown(boolean)
     */
    @Override
    public void setDown(boolean down) {

        super.setDown(down);
        showNonNavigationResources(down);
    }

    /**
     * Shows all non navigation resources.<p>
     * 
     * @param show <code>true</code> to show the resources
     */
    protected void showNonNavigationResources(boolean show) {

        if (show) {
            CmsSitemapView.getInstance().setEditorMode(EditorMode.vfs);
            setTitle(Messages.get().key(Messages.GUI_ONLY_NAVIGATION_BUTTON_TITLE_0));
        } else {
            CmsSitemapView.getInstance().setEditorMode(EditorMode.navigation);
            setTitle(Messages.get().key(Messages.GUI_NON_NAVIGATION_BUTTON_TITLE_0));
        }
    }

}
