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

import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * The toolbar button to refresh the root entry.<p>
 * 
 * @since 8.0.0
 */
public class CmsToolbarRefreshButton extends CmsPushButton {

    /**
     * Constructor.<p>
     * 
     * @param toolbar the toolbar instance
     * @param controller the sitemap controller 
     */
    public CmsToolbarRefreshButton(final CmsSitemapToolbar toolbar, final CmsSitemapController controller) {

        setImageClass(I_CmsButton.ButtonData.REFRESH.getIconClass());
        setTitle(I_CmsButton.ButtonData.REFRESH.getTitle());
        setButtonStyle(ButtonStyle.IMAGE, null);
        addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                toolbar.onButtonActivation(CmsToolbarRefreshButton.this);
                controller.updateEntry(controller.getData().getRoot().getId());
            }
        });
    }
}
