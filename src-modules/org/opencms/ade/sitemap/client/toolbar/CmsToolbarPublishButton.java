/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/toolbar/Attic/CmsToolbarPublishButton.java,v $
 * Date   : $Date: 2010/11/15 16:05:19 $
 * Version: $Revision: 1.4 $
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

package org.opencms.ade.sitemap.client.toolbar;

import org.opencms.ade.publish.client.CmsPublishDialog;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.CmsAcceptDeclineCancelDialog;
import org.opencms.gwt.client.ui.CmsToggleButton;
import org.opencms.gwt.client.ui.I_CmsAcceptDeclineCancelHandler;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.util.CmsDomUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * Sitemap toolbar publish button.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 8.0.0
 */
public class CmsToolbarPublishButton extends CmsToggleButton {

    /**
     * Constructor.<p>
     * 
     * @param toolbar the toolbar instance
     * @param controller the sitemap controller 
     */
    public CmsToolbarPublishButton(final CmsSitemapToolbar toolbar, final CmsSitemapController controller) {

        setImageClass(I_CmsButton.ButtonData.PUBLISH.getIconClass());
        setTitle(I_CmsButton.ButtonData.PUBLISH.getTitle());

        addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                toolbar.onButtonActivation(CmsToolbarPublishButton.this);
                CmsDomUtil.ensureMouseOut(getElement());
                setDown(false);
                setEnabled(false);

                if (controller.hasChanges()) {

                    CmsAcceptDeclineCancelDialog leavingDialog = new CmsAcceptDeclineCancelDialog(Messages.get().key(
                        Messages.GUI_DIALOG_CHANGES_PUBLISH_TITLE_0), Messages.get().key(
                        Messages.GUI_DIALOG_CHANGES_PUBLISH_TEXT_0));
                    leavingDialog.setHandler(new I_CmsAcceptDeclineCancelHandler() {

                        /**
                         * @see org.opencms.gwt.client.ui.I_CmsAcceptDeclineCancelHandler#onAccept()
                         */
                        public void onAccept() {

                            controller.commit(true);
                            openPublish();
                        }

                        /**
                         * @see org.opencms.gwt.client.ui.I_CmsCloseDialogHandler#onClose()
                         */
                        public void onClose() {

                            setEnabled(true);
                        }

                        /**
                         * @see org.opencms.gwt.client.ui.I_CmsAcceptDeclineCancelHandler#onDecline()
                         */
                        public void onDecline() {

                            openPublish();
                        }
                    });
                    leavingDialog.setAcceptText(Messages.get().key(Messages.GUI_YES_0));
                    leavingDialog.setDeclineText(Messages.get().key(Messages.GUI_NO_0));
                    leavingDialog.setCloseText(Messages.get().key(Messages.GUI_CANCEL_0));
                    leavingDialog.center();
                } else {
                    openPublish();
                }
            }
        });
    }

    /**
     * Opens the publish dialog without changes check.<p>
     */
    protected void openPublish() {

        CmsPublishDialog.showPublishDialog(new CloseHandler<PopupPanel>() {

            /**
             * @see com.google.gwt.event.logical.shared.CloseHandler#onClose(CloseEvent)
             */
            public void onClose(CloseEvent<PopupPanel> closeEvent) {

                setEnabled(true);
            }
        });
    }
}
