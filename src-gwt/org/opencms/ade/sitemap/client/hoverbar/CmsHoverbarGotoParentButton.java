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

package org.opencms.ade.sitemap.client.hoverbar;

import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * Sitemap hoverbar "go to parent sitemap" button.<p>
 * 
 * @since 8.0.0
 */
public class CmsHoverbarGotoParentButton extends CmsPushButton {

    /**
     * Constructor.<p>
     * 
     * @param hoverbar the hoverbar 
     */
    public CmsHoverbarGotoParentButton(final CmsSitemapHoverbar hoverbar) {

        setImageClass(I_CmsImageBundle.INSTANCE.buttonCss().hoverbarParent());
        setTitle(Messages.get().key(Messages.GUI_HOVERBAR_PARENT_0));
        setButtonStyle(ButtonStyle.TRANSPARENT, null);
        setVisible(false);
        addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                hoverbar.hide();
                String sitemapLocation = CmsCoreProvider.get().getUri()
                    + "?path="
                    + hoverbar.getController().getData().getParentSitemap();
                hoverbar.getController().leaveEditor(sitemapLocation);
            }
        });
        hoverbar.addShowHandler(new I_CmsHoverbarShowHandler() {

            /**
             * @see org.opencms.ade.sitemap.client.hoverbar.I_CmsHoverbarShowHandler#onShow(org.opencms.ade.sitemap.client.hoverbar.CmsHoverbarShowEvent)
             */
            public void onShow(CmsHoverbarShowEvent event) {

                CmsSitemapController controller = hoverbar.getController();
                boolean show = controller.isRoot(hoverbar.getEntry().getSitePath())
                    && (controller.getData().getParentSitemap() != null);
                setVisible(show);
            }
        });
    }
}
