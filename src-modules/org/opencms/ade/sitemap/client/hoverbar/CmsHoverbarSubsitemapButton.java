/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/hoverbar/Attic/CmsHoverbarSubsitemapButton.java,v $
 * Date   : $Date: 2010/11/18 15:32:41 $
 * Version: $Revision: 1.6 $
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

package org.opencms.ade.sitemap.client.hoverbar;

import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.CmsConfirmDialog;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.sitemap.CmsSitemapManager;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * Sitemap hoverbar subsitemap button.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.6 $ 
 * 
 * @since 8.0.0
 */
public class CmsHoverbarSubsitemapButton extends CmsPushButton {

    /**
     * Constructor.<p>
     * 
     * @param hoverbar the hoverbar 
     */
    public CmsHoverbarSubsitemapButton(final CmsSitemapHoverbar hoverbar) {

        setImageClass(I_CmsImageBundle.INSTANCE.buttonCss().hoverbarSubsitemap());
        setTitle(Messages.get().key(Messages.GUI_HOVERBAR_SUBSITEMAP_0));
        setShowBorder(false);
        setVisible(false);
        addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                hoverbar.hide();
                final String sitePath = hoverbar.getSitePath();
                final CmsSitemapController controller = hoverbar.getController();
                String confirmTitle = Messages.get().key(Messages.GUI_SUBSITEMAP_CONFIRM_TITLE_0);
                String confirmMessage = Messages.get().key(Messages.GUI_SUBSITEMAP_CONFIRM_TEXT_0);
                CmsConfirmDialog confirmDialog = new CmsConfirmDialog(confirmTitle, confirmMessage);
                confirmDialog.setHandler(new I_CmsConfirmDialogHandler() {

                    /**
                     * @see org.opencms.gwt.client.ui.I_CmsCloseDialogHandler#onClose()
                     */
                    public void onClose() {

                        // do nothing
                    }

                    /**
                     * @see org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler#onOk()
                     */
                    public void onOk() {

                        controller.createSubSitemap(sitePath);
                    }
                });
                confirmDialog.center();
            }
        });
        hoverbar.addShowHandler(new I_CmsHoverbarShowHandler() {

            /**
             * @see org.opencms.ade.sitemap.client.hoverbar.I_CmsHoverbarShowHandler#onShow(org.opencms.ade.sitemap.client.hoverbar.CmsHoverbarShowEvent)
             */
            public void onShow(CmsHoverbarShowEvent event) {

                final String sitePath = hoverbar.getSitePath();
                final CmsSitemapController controller = hoverbar.getController();
                boolean isLeaf = controller.getEntry(sitePath).getSubEntries().isEmpty();
                String sitemapProp = controller.getEntry(sitePath).getOwnProperty(
                    CmsSitemapManager.Property.sitemap.name());
                setVisible(CmsStringUtil.isEmptyOrWhitespaceOnly(sitemapProp) && !isLeaf);
                if (controller.isRoot(sitePath)) {
                    disable(Messages.get().key(Messages.GUI_DISABLED_ROOT_ITEM_0));
                } else {
                    enable();
                }
            }
        });
    }
}
