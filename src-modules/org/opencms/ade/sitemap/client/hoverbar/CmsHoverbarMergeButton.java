/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/hoverbar/Attic/CmsHoverbarMergeButton.java,v $
 * Date   : $Date: 2010/11/18 15:32:41 $
 * Version: $Revision: 1.3 $
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
 * A hoverbar button for merging a sub-sitemap back into the parent sitemap.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public class CmsHoverbarMergeButton extends CmsPushButton {

    /**
     * Constructor.<p>
     * 
     * @param hoverbar the hoverbar 
     */
    public CmsHoverbarMergeButton(final CmsSitemapHoverbar hoverbar) {

        setImageClass(I_CmsImageBundle.INSTANCE.buttonCss().hoverbarMergeSitemap());
        setTitle(Messages.get().key(Messages.GUI_HOVERBAR_MERGE_SUB_0));
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
                String confirmTitle = Messages.get().key(Messages.GUI_MERGE_SITEMAP_CONFIRM_TITLE_0);
                String confirmMessage = Messages.get().key(Messages.GUI_MERGE_SITEMAP_CONFIRM_TEXT_0);
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

                        controller.saveAndMergeSubSitemap(sitePath);
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
                String sitemapProp = controller.getEntry(sitePath).getOwnProperty(
                    CmsSitemapManager.Property.sitemap.name());
                setVisible(!CmsStringUtil.isEmptyOrWhitespaceOnly(sitemapProp) && !controller.isRoot(sitePath));
            }
        });
    }

}
