/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.upload.client.ui;

import org.opencms.ade.upload.shared.I_CmsUploadConstants;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.CmsIFrame;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsDomUtil.Method;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * A dialog which contains an IFRAME for displaying the upload hook JSP page.<p>
 */
public class CmsUploadHookDialog extends CmsPopup {

    /** The dialog height. */
    public static final int DIALOG_HEIGHT = 300;

    /** The name of the close function. */
    public static final String CLOSE_FUNCTION = "cmsCloseUploadHookDialog";

    /** The dialog width. */
    public static final int DIALOG_WIDTH = 200;

    /** The name of the IFrame used for displaying the upload hook page. */
    public static final String IFRAME_NAME = "upload_hook";

    /** The form used for the POST request whose response is rendered in the IFRAME. */
    private FormElement m_form;

    /**
     * Creates a new instance of the upload property dialog.<p>
     * 
     * @param title the title for the dialog popup 
     * @param hookUri the URI of the post-upload hook
     *  
     * @param uploadedFiles the list of files which have been uploaded 
     */
    protected CmsUploadHookDialog(String title, String hookUri, List<String> uploadedFiles) {

        super(title);
        setGlassEnabled(true);
        addStyleName(I_CmsLayoutBundle.INSTANCE.contentEditorCss().contentEditor());
        removePadding();
        setHeight(DIALOG_HEIGHT);
        setWidth(DIALOG_WIDTH);
        CmsIFrame frame = new CmsIFrame(IFRAME_NAME);
        frame.getElement().getStyle().setWidth(100, Unit.PCT);
        frame.getElement().getStyle().setHeight(100, Unit.PCT);
        String frameUri = CmsCoreProvider.get().link(hookUri);
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(I_CmsUploadConstants.PARAM_RESOURCES, Joiner.on(",").join(uploadedFiles));
        m_form = CmsDomUtil.generateHiddenForm(frameUri, Method.post, IFRAME_NAME, parameters);
        add(frame);
        installCloseFunction();
    }

    /**
     * Opens a new upload property dialog.<p>
     * 
     * @param title the title for the dialog popup 
     * @param uploadHook the URI of the upload hook page 
     * @param uploadedFiles the uploaded files
     * @param closeHandler the dialog close handler
     */
    public static void openDialog(
        String title,
        String uploadHook,
        List<String> uploadedFiles,
        CloseHandler<PopupPanel> closeHandler) {

        CmsUploadHookDialog dialog = new CmsUploadHookDialog(title, uploadHook, uploadedFiles);
        if (closeHandler != null) {
            dialog.addCloseHandler(closeHandler);
        }
        dialog.center();
        dialog.initializeFrame();
    }

    /**
     * This function is called from the page inside the iframe to close the dialog.<p>
     */
    public void doClose() {

        hide();
    }

    /**
     * Installs the Javascript function which should be called by the child iframe when the dialog should be closed.<p>
     */
    public native void installCloseFunction() /*-{
        var self = this;
        $wnd[@org.opencms.ade.upload.client.ui.CmsUploadHookDialog::CLOSE_FUNCTION] = function() {
            self.@org.opencms.ade.upload.client.ui.CmsUploadHookDialog::doClose()();
        };
    }-*/;

    /**
     * Initializes the IFRAME content of the dialog.<p>
     */
    protected void initializeFrame() {

        RootPanel.getBodyElement().appendChild(m_form);
        m_form.submit();
    }

}
