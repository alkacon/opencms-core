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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.editors;

import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceException;

import javax.servlet.jsp.JspException;

import org.apache.commons.logging.Log;

/**
 * Selects the dialog which should be displayed by OpenCms depending on the configuration value.<p>
 *
 * You can define the class of your editor selector in the OpenCms XML configuration files.
 * The following files use this class:
 * <ul>
 * <li>/jsp/editors/editor_main_html
 * </ul>
 * <p>
 *
 * @since 6.0.0
 *
 * @see org.opencms.workplace.editors.I_CmsEditorHandler
 */
public class CmsEditorSelector {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsEditorSelector.class);

    /** The jsp context. */
    private CmsJspActionElement m_jsp;

    /** The name of the resource to get the editor for. */
    private String m_paramResource;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsEditorSelector(CmsJspActionElement jsp) {

        setJsp(jsp);
        setParamResource(jsp.getRequest().getParameter(CmsDialog.PARAM_RESOURCE));
    }

    /**
     * Shows the error dialog when no valid editor is found and returns null for the editor URI.<p>
     *
     * @param jsp the instantiated CmsJspActionElement
     * @param t a throwable object, can be null
     */
    private static void showErrorDialog(CmsJspActionElement jsp, Throwable t) {

        CmsDialog wp = new CmsDialog(jsp);
        wp.setParamMessage(Messages.get().getBundle(wp.getLocale()).key(Messages.ERR_NO_EDITOR_FOUND_0));
        wp.fillParamValues(jsp.getRequest());
        try {
            wp.includeErrorpage(wp, t);
        } catch (JspException e) {
            LOG.debug(
                org.opencms.workplace.commons.Messages.get().getBundle().key(
                    org.opencms.workplace.commons.Messages.LOG_ERROR_INCLUDE_FAILED_1,
                    CmsWorkplace.FILE_DIALOG_SCREEN_ERRORPAGE),
                e);
        }
    }

    /**
     * Returns the uri of the dialog which will be displayed.<p>
     *
     * @return the uri of the property dialog
     */
    public String getSelectedEditorUri() {

        // get the handler class from the OpenCms runtime property
        I_CmsEditorHandler editorClass = OpenCms.getWorkplaceManager().getEditorHandler();

        // the resourcenameparameter could be encoded, so decode it
        String resource = getParamResource();
        resource = CmsEncoder.unescape(resource, CmsEncoder.ENCODING_UTF_8);
        if (editorClass == null) {
            // error getting the dialog class, return to file list
            return CmsWorkplace.FILE_EXPLORER_FILELIST;
        }
        // get the dialog URI from the class defined in the configuration
        String editorUri = null;
        try {
            editorUri = editorClass.getEditorUri(resource, getJsp());
            if (editorUri == null) {
                // no valid editor was found, show the error dialog
                throw new CmsWorkplaceException(Messages.get().container(Messages.ERR_NO_EDITOR_FOUND_0));
            }
        } catch (CmsException e) {
            showErrorDialog(getJsp(), e);
        }
        return editorUri;
    }

    /**
     * Returns the CmsJspActionElement.<p>
     *
     * @return the CmsJspActionElement
     */
    private CmsJspActionElement getJsp() {

        return m_jsp;
    }

    /**
     * Returns the resource parameter String.<p>
     *
     * @return the resource parameter String
     */
    private String getParamResource() {

        return m_paramResource;
    }

    /**
     * Sets the CmsJspActionElement.<p>
     *
     * @param jsp the CmsJspActionElement
     */
    private void setJsp(CmsJspActionElement jsp) {

        m_jsp = jsp;
    }

    /**
     * Sets the resource parameter String.<p>
     *
     * @param resource the resource parameter String
     */
    private void setParamResource(String resource) {

        m_paramResource = resource;
    }

}
