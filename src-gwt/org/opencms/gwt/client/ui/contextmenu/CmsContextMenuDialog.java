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

package org.opencms.gwt.client.ui.contextmenu;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.CmsFrameDialog;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.util.CmsClientStringUtil;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.gwt.shared.CmsMenuCommandParameters;
import org.opencms.util.CmsUUID;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Window;

/**
 * A context menu entry command to open any dialog within an iFrame.<p>
 * 
 * The dialog will be called with the parameter {@link #PARAM_CONTENT_STRUCTURE_ID}
 * containing the structure id of the currently edited content if available.<p>
 * 
 * To close the dialog call from within the dialog frame context 
 * window.parent[{@link #CLOSING_METHOD_NAME}](boolean reload).<p>
 */
public class CmsContextMenuDialog implements I_CmsHasContextMenuCommand, I_CmsContextMenuCommand {

    /** The name of the dialog close method exported to the window context. */
    public static final String CLOSING_METHOD_NAME = "closeContextMenuDialog";

    /** The parameter name for the content structure id. */
    public static final String PARAM_CONTENT_STRUCTURE_ID = "contentStructureId";

    /** The context menu handler for this command instance. */
    protected I_CmsContextMenuHandler m_menuHandler;

    /**
     * Constructor.<p>
     */
    private CmsContextMenuDialog() {

        // nothing to do
    }

    /**
     * Returns the context menu command according to 
     * {@link org.opencms.gwt.client.ui.contextmenu.I_CmsHasContextMenuCommand}.<p>
     * 
     * @return the context menu command
     */
    public static I_CmsContextMenuCommand getContextMenuCommand() {

        return new CmsContextMenuDialog();
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand#execute(org.opencms.util.CmsUUID, org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler, org.opencms.gwt.shared.CmsContextMenuEntryBean)
     */
    public void execute(CmsUUID structureId, I_CmsContextMenuHandler handler, CmsContextMenuEntryBean menuEntryBean) {

        m_menuHandler = handler;
        int height = 400;
        int width = 300;
        if (menuEntryBean.getParams().containsKey(CmsMenuCommandParameters.PARAM_DIALOG_HEIGHT)) {
            height = CmsClientStringUtil.parseInt(menuEntryBean.getParams().get(
                CmsMenuCommandParameters.PARAM_DIALOG_HEIGHT));
        }
        if (menuEntryBean.getParams().containsKey(CmsMenuCommandParameters.PARAM_DIALOG_WIDTH)) {
            width = CmsClientStringUtil.parseInt(menuEntryBean.getParams().get(
                CmsMenuCommandParameters.PARAM_DIALOG_WIDTH));
        }
        String fileName = menuEntryBean.getParams().get(CmsMenuCommandParameters.PARAM_DIALOG_URI);
        CmsPopup popup = CmsFrameDialog.showFrameDialog(
            menuEntryBean.getLabel(),
            CmsCoreProvider.get().link(fileName),
            getDialogParameters(structureId, menuEntryBean),
            null);
        popup.setHeight(height);
        popup.setWidth(width);
        popup.addDialogClose(null);
        popup.center();
        exportClosingMethod(popup);
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand#getCommandIconClass()
     */
    public String getCommandIconClass() {

        //TODO: use better icon
        return org.opencms.gwt.client.ui.css.I_CmsImageBundle.INSTANCE.contextMenuIcons().edit();
    }

    /**
     * Executed on dialog close.<p>
     * @param reload <code>true</code> if the page should be reloaded
     */
    protected void onClose(boolean reload) {

        if (reload) {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                public void execute() {

                    String url = Window.Location.getHref();
                    m_menuHandler.leavePage(url);

                }
            });
        }
    }

    /**
     * Exports the close method to the window object, so it can be accessed from within the content editor iFrame.<p>
     * 
     * @param popup the popup instance 
     */
    private native void exportClosingMethod(final CmsPopup popup) /*-{
        var self = this;
        $wnd[@org.opencms.gwt.client.ui.contextmenu.CmsContextMenuDialog::CLOSING_METHOD_NAME] = function(
                reload) {
            popup.@org.opencms.gwt.client.ui.CmsPopup::hide()();
            self.@org.opencms.gwt.client.ui.contextmenu.CmsContextMenuDialog::onClose(Z)(reload);
            $wnd[@org.opencms.gwt.client.ui.contextmenu.CmsContextMenuDialog::CLOSING_METHOD_NAME] = null;
        };
    }-*/;

    /**
     * Generates the dialog parameters.<p>
     * 
     * @param structureId the structure id of the current content
     * @param menuEntryBean the context menu entry bean
     * @return the dialog parameters
     */
    private Map<String, String> getDialogParameters(CmsUUID structureId, CmsContextMenuEntryBean menuEntryBean) {

        HashMap<String, String> parameters = new HashMap<String, String>();
        if (structureId != null) {
            parameters.put(PARAM_CONTENT_STRUCTURE_ID, structureId.toString());
        }
        parameters.putAll(menuEntryBean.getParams());
        return parameters;
    }
}
