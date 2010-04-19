/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/CmsContentEditorDialog.java,v $
 * Date   : $Date: 2010/04/19 06:39:10 $
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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.client.CmsContainerpageDataProvider;
import org.opencms.ade.containerpage.client.Messages;
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.containerpage.client.util.CmsContainerpageProvider;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.CmsIFrame;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.util.CmsDebugLog;

import com.google.gwt.dom.client.Style.Unit;

/**
 * Class to handle the content editor dialog.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public final class CmsContentEditorDialog {

    /** Name of exported dialog close function. */
    @SuppressWarnings("unused")
    private static final String CLOSING_METHOD_NAME = "cms_ade_containerpage_closeEditorDialog";

    /** Name attribute value for editor iFrame. */
    private static final String EDITOR_IFRAME_NAME = "cmsAdvancedDirectEditor";

    /** The dialog instance. */
    private static CmsContentEditorDialog INSTANCE;

    /** The currently edited element's id. */
    private String m_currentElementId;

    /** The currently edited element's site-path. */
    private String m_currentSitePath;

    /** The popup instance. */
    private CmsPopup m_dialog;

    /**
     * Hiding constructor.<p>
     */
    private CmsContentEditorDialog() {

        // nothing to do
    }

    /**
     * Returns the dialogs instance.<p>
     * 
     * @return the dialog instance
     */
    public static CmsContentEditorDialog get() {

        if (INSTANCE == null) {
            INSTANCE = new CmsContentEditorDialog();
        }
        return INSTANCE;
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
        m_dialog = new CmsPopup(Messages.get().key(Messages.CONTENT_EDITOR_DIALOG_TITLE_0));
        m_dialog.addStyleName(I_CmsLayoutBundle.INSTANCE.contentEditorCss().contentEditor());
        m_dialog.setSize(800, 600, Unit.PX);

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
            CmsContainerpageDataProvider.get().reloadElement(m_currentElementId);
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

        return CmsCoreProvider.get().link(CmsContainerpageProvider.get().getEditorUri())
            + "?resource="
            + sitePath
            + "&amp;directedit=true&amp;elementlanguage="
            + CmsCoreProvider.get().getLocale()
            + "&amp;backlink="
            + CmsContainerpageProvider.get().getBacklinkUri()
            + "&amp;redirect=true";
    }
}
