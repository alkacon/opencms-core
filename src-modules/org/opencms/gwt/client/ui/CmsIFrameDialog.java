/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsIFrameDialog.java,v $
 * Date   : $Date: 2010/07/26 06:31:32 $
 * Version: $Revision: 1.1 $
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

package org.opencms.gwt.client.ui;

import org.opencms.ade.containerpage.client.CmsContainerpageHandler;
import org.opencms.ade.containerpage.client.Messages;
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.util.CmsDebugLog;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;

public class CmsIFrameDialog {

    /** Name of exported dialog close function. */
    private static final String CLOSING_METHOD_NAME = "cms_ade_containerpage_closeEditorDialog";

    /** Name attribute value for editor iFrame. */
    private static final String DIALOG_IFRAME_NAME = "cmsAdvancedDirectEditor";

    /** The dialog instance. */
    private static CmsIFrameDialog INSTANCE;

    /** The popup instance. */
    private CmsPopup m_dialog;

    /** The container-page handler. */
    private CmsContainerpageHandler m_handler;

    /** The path to the JSP that should be opened in the IFrame. */
    private String m_jspPath;

    /** The path to the resource. */
    private String m_resourcePath;

    /**
     * Hiding constructor.<p>
     * 
     * @param handler the container-page handler
     */
    private CmsIFrameDialog(CmsContainerpageHandler handler) {

        m_handler = handler;

    }

    /**
     * Returns the dialogs instance.<p>
     * 
     * @return the dialog instance
     */
    public static CmsIFrameDialog get() {

        return INSTANCE;
    }

    /**
     * Initializes the dialog.<p>
     * 
     * @param handler the container-page handler
     */
    public static void init(CmsContainerpageHandler handler) {

        if (INSTANCE == null) {
            INSTANCE = new CmsIFrameDialog(handler);
        }
    }

    /**
     * Closes the dialog.<p>
     */
    static void closeDialog() {

        get().close();

    }

    /**
     * Opens the content editor dialog for the given element.<p>
     * 
     * @param resourcePath the resource path
     * @param jspPath the path to the JSP to open in the iframe
     * @param backlink the backlink for this dialog
     */
    public void openDialog(String jspPath, String resourcePath, String backlink) {

        if ((m_dialog != null) && m_dialog.isShowing()) {
            CmsDebugLog.getInstance().printLine("Dialog is already open, cannot open another one.");
            return;
        }

        m_jspPath = jspPath;
        m_resourcePath = resourcePath;
        m_dialog = new CmsPopup(Messages.get().key(Messages.GUI_DIALOG_CONTENTEDITOR_TITLE_0));
        m_dialog.addStyleName(I_CmsLayoutBundle.INSTANCE.contentEditorCss().contentEditor());

        int height = Window.getClientHeight() - 20;
        int width = Window.getClientWidth();
        width = (width < 1350) ? width - 50 : 1300;
        m_dialog.setSize(width, height, Unit.PX);
        m_dialog.setGlassEnabled(true);
        CmsIFrame editorFrame = new CmsIFrame(DIALOG_IFRAME_NAME, getUrl(backlink));

        m_dialog.add(editorFrame);
        m_dialog.center();
        m_dialog.show();
        exportClosingMethod();
    }

    /**
     * Closes the dialog.<p>
     */
    private void close() {

        if (m_dialog != null) {
            m_dialog.hide();
            m_dialog = null;
            m_jspPath = null;
            m_resourcePath = null;
        }
    }

    /**
     * Exports the close method to the window object, so it can be accessed from within the content editor iFrame.<p>
     */
    private native void exportClosingMethod() /*-{
        $wnd[@org.opencms.ade.containerpage.client.ui.CmsContentEditorDialog::CLOSING_METHOD_NAME]=function(){
        @org.opencms.ade.containerpage.client.ui.CmsContentEditorDialog::closeEditDialog()();
        };
    }-*/;

    /**
     * Convenience method to generate the URL for the dialog.<p>
     * 
     * @param backlink the backlink for this dialog
     * 
     * @return the editor URL
     */
    private String getUrl(String backlink) {

        return m_jspPath
            + "?resource="
            + m_resourcePath
            + "&directedit=true"
            + "&elementlanguage="
            + CmsCoreProvider.get().getLocale()
            + "&backlink="
            + backlink
            + "&redirect=true"
            + "&closelink="
            + CmsCoreProvider.get().link(backlink);
    }
}
