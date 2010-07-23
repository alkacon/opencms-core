/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/hoverbar/Attic/CmsHoverbarDeleteButton.java,v $
 * Date   : $Date: 2010/07/23 11:38:25 $
 * Version: $Revision: 1.2 $
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

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.ui.CmsLinkWarningDialog;
import org.opencms.ade.sitemap.client.ui.css.I_CmsImageBundle;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsSitemapBrokenLinkBean;
import org.opencms.gwt.client.ui.CmsConfirmDialog;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Sitemap hoverbar delete button.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0.0
 */
public class CmsHoverbarDeleteButton extends CmsPushButton {

    /**
     * Constructor.<p>
     * 
     * @param hoverbar the hoverbar 
     */
    public CmsHoverbarDeleteButton(final CmsSitemapHoverbar hoverbar) {

        setImageClass(I_CmsImageBundle.INSTANCE.buttonCss().hoverbarDelete());
        setTitle(Messages.get().key(Messages.GUI_HOVERBAR_DELETE_0));
        setShowBorder(false);
        addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                hoverbar.deattach();
                CmsDomUtil.ensureMouseOut(getElement());
                final String sitePath = hoverbar.getSitePath();
                final CmsSitemapController controller = hoverbar.getController();
                CmsPair<List<CmsClientSitemapEntry>, List<CmsClientSitemapEntry>> openAndClosed = CmsSitemapView.getInstance().getOpenAndClosedDescendants(
                    sitePath);
                List<CmsClientSitemapEntry> openEntries = openAndClosed.getFirst();
                List<CmsClientSitemapEntry> closedEntries = openAndClosed.getSecond();
                List<CmsUUID> open = getIds(openEntries);
                List<CmsUUID> closed = getIds(closedEntries);
                controller.getBrokenLinks(open, closed, new AsyncCallback<List<CmsSitemapBrokenLinkBean>>() {

                    /**
                     * @see com.google.gwt.user.client.rpc.AsyncCallback#onFailure(java.lang.Throwable)
                     */
                    public void onFailure(Throwable caught) {

                        // do nothing; will never be called
                    }

                    /**
                     * @see com.google.gwt.user.client.rpc.AsyncCallback#onSuccess(java.lang.Object)
                     */
                    public void onSuccess(List<CmsSitemapBrokenLinkBean> result) {

                        if (result.size() > 0) {
                            I_CmsConfirmDialogHandler handler = new I_CmsConfirmDialogHandler() {

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

                                    controller.delete(sitePath);
                                }
                            };
                            CmsLinkWarningDialog dialog = new CmsLinkWarningDialog(handler, result);
                            dialog.center();

                        } else {

                            if (controller.getEntry(sitePath).getSubEntries().isEmpty()) {
                                controller.delete(sitePath);
                                return;
                            }
                            // show the dialog only if the entry has children 
                            CmsConfirmDialog dialog = new CmsConfirmDialog(Messages.get().key(
                                Messages.GUI_DIALOG_DELETE_TITLE_0), Messages.get().key(
                                Messages.GUI_DIALOG_DELETE_TEXT_0));
                            dialog.setHandler(new I_CmsConfirmDialogHandler() {

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

                                    controller.delete(sitePath);
                                }
                            });
                            dialog.center();
                        }
                    }

                });
            }
        });
        hoverbar.addAttachHandler(new I_CmsHoverbarAttachHandler() {

            /**
             * @see org.opencms.ade.sitemap.client.hoverbar.I_CmsHoverbarAttachHandler#onAttach(org.opencms.ade.sitemap.client.hoverbar.CmsHoverbarAttachEvent)
             */
            public void onAttach(CmsHoverbarAttachEvent event) {

                final String sitePath = hoverbar.getSitePath();
                final CmsSitemapController controller = hoverbar.getController();
                if (controller.isRoot(sitePath)) {
                    disable(Messages.get().key(Messages.GUI_DISABLED_ROOT_ITEM_0));
                } else {
                    enable();
                }
            }
        });
    }

    /**
     * Extracts the ids from a list of client sitemap entries.<p>
     * 
     * @param entries the sitemap entries
     * 
     * @return the ids of the sitemap entries 
     */
    protected List<CmsUUID> getIds(List<CmsClientSitemapEntry> entries) {

        List<CmsUUID> result = new ArrayList<CmsUUID>();
        for (CmsClientSitemapEntry entry : entries) {
            result.add(entry.getId());
        }
        return result;
    }

}
