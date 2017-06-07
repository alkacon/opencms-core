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
import org.opencms.gwt.client.I_CmsEditableData;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.I_CmsToolbarHandler;
import org.opencms.gwt.client.ui.contenteditor.CmsContentEditorDialog;
import org.opencms.gwt.client.ui.contenteditor.CmsContentEditorDialog.DialogOptions;
import org.opencms.gwt.client.ui.contenteditor.I_CmsContentEditorHandler;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.gwt.shared.CmsMenuCommandParameters;
import org.opencms.gwt.shared.CmsPrepareEditResponse;
import org.opencms.util.CmsUUID;

import java.util.Map;

import com.google.gwt.dom.client.FormElement;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * A context menu command for editing an XML content. <p>
 */
public final class CmsEditFile implements I_CmsHasContextMenuCommand, I_CmsContextMenuCommand {

    /** The context menu handler for this command instance. */
    protected I_CmsContextMenuHandler m_menuHandler;

    /** A flag which indicates whether the window should be reloaded after editing. */
    protected boolean m_reload;

    /** A flag indicating if the editor should open in the same window, not using any overlays and iFrames. */
    private boolean m_useSelf;

    /**
     * Hidden utility class constructor.<p>
     */
    private CmsEditFile() {

        // nothing to do
    }

    /**
     * Returns the context menu command according to
     * {@link org.opencms.gwt.client.ui.contextmenu.I_CmsHasContextMenuCommand}.<p>
     *
     * @return the context menu command
     */
    public static I_CmsContextMenuCommand getContextMenuCommand() {

        return new CmsEditFile();
    }

    /**
     * Actually starts the XML content editor for a given file name.<p>
     *
     * @param filename the file name
     * @param structureId the file structure id
     */
    public void doEdit(final String filename, final CmsUUID structureId) {

        I_CmsEditableData editData = new I_CmsEditableData() {

            public String getContextId() {

                return "";
            }

            public String getEditId() {

                return null;
            }

            public String getElementLanguage() {

                return null;
            }

            public String getElementName() {

                return null;
            }

            public String getNewLink() {

                return null;
            }

            public String getNewTitle() {

                return null;
            }

            public String getNoEditReason() {

                return null;
            }

            public String getPostCreateHandler() {

                return null;
            }

            public String getSitePath() {

                return filename;
            }

            public CmsUUID getStructureId() {

                return structureId;
            }

            public boolean isUnreleasedOrExpired() {

                return false;
            }

            public void setSitePath(String sitePath) {

                // nothing to do
            }
        };
        if (m_useSelf) {
            FormElement form = CmsContentEditorDialog.generateForm(editData, false, "_self", null);
            RootPanel.getBodyElement().appendChild(form);
            form.submit();
        } else {

            I_CmsContentEditorHandler handler = new I_CmsContentEditorHandler() {

                public void onClose(String sitePath, CmsUUID id, boolean isNew) {

                    if (!m_reload) {
                        return;
                    }

                    // defer the window.location.reload until after the editor window has closed
                    // so we don't get another confirmation dialog
                    Timer timer = new Timer() {

                        @Override
                        public void run() {

                            String url = Window.Location.getHref();
                            m_menuHandler.leavePage(url);
                        }
                    };
                    timer.schedule(10);
                }
            };
            CmsContentEditorDialog.get().openEditDialog(editData, false, null, new DialogOptions(), handler);
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand#execute(org.opencms.util.CmsUUID, org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler, org.opencms.gwt.shared.CmsContextMenuEntryBean)
     */
    public void execute(
        final CmsUUID structureId,
        final I_CmsContextMenuHandler handler,
        CmsContextMenuEntryBean bean) {

        m_menuHandler = handler;
        Map<String, String> params = bean.getParams();
        final String fileName = params.get(CmsMenuCommandParameters.PARAM_FILENAME);
        if (fileName == null) {
            return;
        }
        String reloadStr = params.get(CmsMenuCommandParameters.PARAM_RELOAD);
        m_reload = Boolean.parseBoolean(reloadStr);
        String useSelfStr = params.get(CmsMenuCommandParameters.PARAM_USE_SELF);
        m_useSelf = Boolean.parseBoolean(useSelfStr);

        if (handler instanceof I_CmsToolbarHandler) {
            ((I_CmsToolbarHandler)handler).deactivateCurrentButton();
        }
        CmsRpcAction<CmsPrepareEditResponse> prepareEdit = new CmsRpcAction<CmsPrepareEditResponse>() {

            @Override
            public void execute() {

                start(0, true);
                CmsCoreProvider.getVfsService().prepareEdit(structureId, fileName, this);
            }

            @Override
            public void onResponse(CmsPrepareEditResponse response) {

                stop(false);
                doEdit(response.getSitePath(), response.getStructureId());
            }
        };
        prepareEdit.execute();
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand#getItemWidget(org.opencms.util.CmsUUID, org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler, org.opencms.gwt.shared.CmsContextMenuEntryBean)
     */
    public A_CmsContextMenuItem getItemWidget(
        CmsUUID structureId,
        I_CmsContextMenuHandler handler,
        CmsContextMenuEntryBean bean) {

        return null;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand#hasItemWidget()
     */
    public boolean hasItemWidget() {

        return false;
    }
}
