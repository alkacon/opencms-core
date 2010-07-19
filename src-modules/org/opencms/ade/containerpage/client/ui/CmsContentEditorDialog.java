/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/CmsContentEditorDialog.java,v $
 * Date   : $Date: 2010/07/19 07:45:28 $
 * Version: $Revision: 1.8 $
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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.client.CmsContainerpageHandler;
import org.opencms.ade.containerpage.client.Messages;
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.containerpage.shared.CmsCntPageData;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.CmsIFrame;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.util.CmsDebugLog;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;

/**
 * Class to handle the content editor dialog.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.8 $
 * 
 * @since 8.0.0
 */
public final class CmsContentEditorDialog {

    /** Name of exported dialog close function. */
    private static final String CLOSING_METHOD_NAME = "cms_ade_containerpage_closeEditorDialog";

    /** Name attribute value for editor iFrame. */
    private static final String EDITOR_IFRAME_NAME = "cmsAdvancedDirectEditor";

    /** The dialog instance. */
    private static CmsContentEditorDialog INSTANCE;

    /** The currently edited element's id. */
    private String m_currentElementId;

    /** The currently edited element's site-path. */
    private String m_currentSitePath;

    /** the prefetched data. */
    private CmsCntPageData m_data;

    /** The popup instance. */
    private CmsPopup m_dialog;

    /** The container-page handler. */
    private CmsContainerpageHandler m_handler;

    /**
     * Hiding constructor.<p>
     * 
     * @param handler the container-page handler
     * @param data the prefetched data
     */
    private CmsContentEditorDialog(CmsContainerpageHandler handler, CmsCntPageData data) {

        m_handler = handler;
        m_data = data;
    }

    /**
     * Returns the dialogs instance.<p>
     * 
     * @return the dialog instance
     */
    public static CmsContentEditorDialog get() {

        return INSTANCE;
    }

    /**
     * Initializes the dialog.<p>
     * 
     * @param handler the container-page handler
     * @param data the prefetched data
     */
    public static void init(CmsContainerpageHandler handler, CmsCntPageData data) {

        if (INSTANCE == null) {
            INSTANCE = new CmsContentEditorDialog(handler, data);
        }
    }

    /**
     * Closes the dialog.<p>
     */
    static void closeEditDialog() {

        get().close();

    }

    /**
     * Opens the content editor dialog for the given element.<p>
     * 
     * @param elementId the element id
     * @param sitePath the element site-path
     */
    public void openEditDialog(String elementId, String sitePath) {

        if ((m_dialog != null) && m_dialog.isShowing()) {
            CmsDebugLog.getInstance().printLine("Dialog is already open, cannot open another one.");
            return;
        }

        m_currentElementId = elementId;
        m_currentSitePath = sitePath;
        m_dialog = new CmsPopup(Messages.get().key(Messages.GUI_DIALOG_CONTENTEDITOR_TITLE_0));
        m_dialog.addStyleName(I_CmsLayoutBundle.INSTANCE.contentEditorCss().contentEditor());

        int height = Window.getClientHeight() - 20;
        int width = Window.getClientWidth();
        width = (width < 1350) ? width - 50 : 1300;
        m_dialog.setSize(width, height, Unit.PX);
        m_dialog.setGlassEnabled(true);
        CmsIFrame editorFrame = new CmsIFrame(EDITOR_IFRAME_NAME, getEditorUrl(m_currentSitePath));

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
            m_handler.reloadElement(m_currentElementId);
            m_currentElementId = null;
            m_currentSitePath = null;
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
     * Convenience method to generate the editor URL.<p>
     * 
     * @param sitePath the element's site-path
     * 
     * @return the editor URL
     */
    private String getEditorUrl(String sitePath) {

        return CmsCoreProvider.get().link(m_data.getEditorUri())
            + "?resource="
            + sitePath
            + "&amp;directedit=true&amp;elementlanguage="
            + CmsCoreProvider.get().getLocale()
            + "&amp;backlink="
            + m_data.getBacklinkUri()
            + "&amp;redirect=true";
    }
}
