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

package org.opencms.gwt.client.ui.contextmenu;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsConfirmDialog;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPreviewDialog;
import org.opencms.gwt.client.ui.CmsPreviewDialog.I_PreviewInfoProvider;
import org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler;
import org.opencms.gwt.client.ui.history.CmsHistoryMessages;
import org.opencms.gwt.client.ui.history.CmsResourceHistoryView;
import org.opencms.gwt.client.ui.history.I_CmsHistoryActionHandler;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.gwt.shared.CmsHistoryResourceBean;
import org.opencms.gwt.shared.CmsHistoryResourceCollection;
import org.opencms.gwt.shared.CmsPreviewInfo;
import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Context menu entry class for the history dialog.<p>
 *
 * The history dialog allows the user to preview or restore a previous version of a content.<p>
 */
public class CmsHistory implements I_CmsHasContextMenuCommand, I_CmsContextMenuCommand {

    /**
     * Gets the context menu command.<p>
     *
     * @return the context menu command
     */
    public static I_CmsContextMenuCommand getContextMenuCommand() {

        return new CmsHistory();
    }

    /**
     * Loads the data necessary for the history dialog and then displays that dialog.<p>
     *
     * @param structureId the structure id for which the history should be loaded.<p>
     * @param handler the history action handler to use
     */
    public static void loadDialog(final CmsUUID structureId, final I_CmsHistoryActionHandler handler) {

        CmsRpcAction<CmsHistoryResourceCollection> action = new CmsRpcAction<CmsHistoryResourceCollection>() {

            @Override
            public void execute() {

                start(200, true);
                CmsCoreProvider.getVfsService().getResourceHistory(structureId, this);
            }

            @Override
            protected void onResponse(CmsHistoryResourceCollection result) {

                stop(false);
                final CmsPopup popup = new CmsPopup(CmsHistoryMessages.dialogTitle(), 1150);
                popup.addDialogClose(null);
                CmsResourceHistoryView view = new CmsResourceHistoryView(result, handler);
                handler.setPostRevertAction(new Runnable() {

                    public void run() {

                        popup.hide();
                    }
                });
                popup.setMainContent(view);
                popup.setModal(true);
                popup.setGlassEnabled(true);
                popup.centerHorizontally(80);
            }

        };
        action.execute();

    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand#execute(org.opencms.util.CmsUUID, org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler, org.opencms.gwt.shared.CmsContextMenuEntryBean)
     */
    public void execute(
        final CmsUUID structureId,
        final I_CmsContextMenuHandler handler,
        CmsContextMenuEntryBean bean) {

        I_CmsHistoryActionHandler historyActionHandler = new I_CmsHistoryActionHandler() {

            private Runnable m_postRevertAction;

            public void revert(final CmsHistoryResourceBean historyRes) {

                CmsConfirmDialog confirmation = new CmsConfirmDialog(
                    CmsHistoryMessages.captionConfirm(),
                    CmsHistoryMessages.textConfirm());
                confirmation.setHandler(new I_CmsConfirmDialogHandler() {

                    public void onClose() {

                        // do nothing
                    }

                    public void onOk() {

                        CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

                            @Override
                            public void execute() {

                                start(200, true);
                                CmsCoreProvider.getVfsService().restoreResource(
                                    structureId,
                                    historyRes.getVersion().getVersionNumber().intValue(),
                                    this);
                            }

                            @SuppressWarnings("synthetic-access")
                            @Override
                            protected void onResponse(Void result) {

                                stop(false);
                                if (m_postRevertAction != null) {
                                    m_postRevertAction.run();
                                }
                                handler.refreshResource(structureId);
                            }
                        };
                        action.execute();
                    }
                });
                confirmation.center();
            }

            public void setPostRevertAction(Runnable action) {

                m_postRevertAction = action;
            }

            public void showPreview(final CmsHistoryResourceBean historyRes) {

                CmsRpcAction<CmsPreviewInfo> previewAction = new CmsRpcAction<CmsPreviewInfo>() {

                    @Override
                    public void execute() {

                        start(0, true);
                        CmsCoreProvider.getVfsService().getHistoryPreviewInfo(
                            structureId,
                            CmsCoreProvider.get().getLocale(),
                            historyRes.getVersion(),
                            this);
                    }

                    @Override
                    protected void onResponse(CmsPreviewInfo result) {

                        stop(false);
                        CmsPreviewDialog dialog = CmsPreviewDialog.createPreviewDialog(result);
                        dialog.setPreviewInfoProvider(new I_PreviewInfoProvider() {

                            public void loadPreviewForLocale(
                                String locale,
                                AsyncCallback<CmsPreviewInfo> resultCallback) {

                                CmsCoreProvider.getVfsService().getHistoryPreviewInfo(
                                    structureId,
                                    locale,
                                    historyRes.getVersion(),
                                    resultCallback);
                            }
                        });

                        dialog.center();
                    }
                };
                previewAction.execute();
            }

        };
        loadDialog(structureId, historyActionHandler);
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand#getItemWidget(org.opencms.util.CmsUUID, org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler, org.opencms.gwt.shared.CmsContextMenuEntryBean)
     */
    public A_CmsContextMenuItem getItemWidget(
        CmsUUID structureId,
        I_CmsContextMenuHandler handler,
        CmsContextMenuEntryBean bean) {

        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand#hasItemWidget()
     */
    public boolean hasItemWidget() {

        // TODO Auto-generated method stub
        return false;
    }

}
