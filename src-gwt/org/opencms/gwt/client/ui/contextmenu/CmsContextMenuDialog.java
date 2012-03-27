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
import org.opencms.gwt.client.ui.CmsIFrame;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsClientStringUtil;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsDomUtil.Method;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.gwt.shared.CmsMenuCommandParameters;
import org.opencms.util.CmsUUID;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

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

    /** The iFrame name prefix. */
    private static final String FRAME_NAME_PREFIX = "DIALOG_FRAME_";

    /** The context menu handler for this command instance. */
    protected I_CmsContextMenuHandler m_menuHandler;

    /** The popup instance. */
    private CmsPopup m_dialog;

    /** The form element. */
    private FormElement m_form;

    /** The name of the currently used iFrame. */
    private String m_iFrameName;

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
        if ((m_dialog != null) && m_dialog.isShowing()) {
            CmsDebugLog.getInstance().printLine("Dialog is already open, cannot open another one.");
            return;
        }
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
        m_iFrameName = FRAME_NAME_PREFIX + menuEntryBean.getName();
        exportClosingMethod();
        m_dialog = new CmsPopup(menuEntryBean.getLabel());
        m_dialog.addStyleName(I_CmsLayoutBundle.INSTANCE.contentEditorCss().contentEditor());
        m_dialog.removePadding();
        m_dialog.setHeight(height);
        m_dialog.setWidth(width);
        m_dialog.setGlassEnabled(true);
        CmsIFrame editorFrame = new CmsIFrame(m_iFrameName, "");
        m_dialog.addDialogClose(new Command() {

            /**
             * @see com.google.gwt.user.client.Command#execute()
             */
            public void execute() {

                onClose(false);
            }
        });
        m_dialog.add(editorFrame);
        m_dialog.center();
        m_form = generateForm(structureId, menuEntryBean);
        RootPanel.getBodyElement().appendChild(m_form);
        m_form.submit();
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand#getCommandIconClass()
     */
    public String getCommandIconClass() {

        //TODO: use better icon
        return org.opencms.gwt.client.ui.css.I_CmsImageBundle.INSTANCE.contextMenuIcons().edit();
    }

    /**
     * Execute
     * @param reload
     */
    protected void onClose(boolean reload) {

        if (m_dialog != null) {
            m_dialog.hide();
            m_dialog = null;
        }
        if (m_form != null) {
            m_form.removeFromParent();
            m_form = null;
        }
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
     */
    private native void exportClosingMethod() /*-{
        var self = this;
        $wnd[@org.opencms.gwt.client.ui.contextmenu.CmsContextMenuDialog::CLOSING_METHOD_NAME] = function(
                reload) {
            self.@org.opencms.gwt.client.ui.contextmenu.CmsContextMenuDialog::onClose(Z)(reload);
        };
    }-*/;

    /**
     * Generates the form to post to the dialog frame.<p>
     * 
     * @param structureId the structure id of the current content
     * @param menuEntryBean the context menu entry bean
     * 
     * @return the form element
     */
    private FormElement generateForm(CmsUUID structureId, CmsContextMenuEntryBean menuEntryBean) {

        String fileName = menuEntryBean.getParams().get(CmsMenuCommandParameters.PARAM_DIALOG_URI);
        // create a form to submit a post request to the editor JSP
        Map<String, String> formValues = new HashMap<String, String>();
        if (structureId != null) {
            formValues.put(PARAM_CONTENT_STRUCTURE_ID, structureId.toString());
        }
        formValues.putAll(menuEntryBean.getParams());
        FormElement formElement = CmsDomUtil.generateHiddenForm(
            CmsCoreProvider.get().link(fileName),
            Method.post,
            m_iFrameName,
            formValues);
        return formElement;
    }
}
