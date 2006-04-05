/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/CmsSimpleEditor.java,v $
 * Date   : $Date: 2005/07/06 12:45:07 $
 * Version: $Revision: 1.12 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import org.apache.commons.logging.Log;

/**
 * Creates the output for editing a resource (text or JSP files).<p> 
 * 
 * This class is extended by the LEdit class, so be careful when changing anything.<p>
 * 
 * The following files use this class:
 * <ul>
 * <li>/editors/simple/editor.jsp</li>
 * </ul>
 * <p>
 *
 * @author  Andreas Zahner 
 * 
 * @version $Revision: 1.12 $ 
 * 
 * @since 6.0.0 
 */
public class CmsSimpleEditor extends CmsEditor {

    /** Constant for the editor type, must be the same as the editors subfolder name in the VFS. */
    private static final String EDITOR_TYPE = "simple";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSimpleEditor.class);

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsSimpleEditor(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * @see org.opencms.workplace.editors.CmsEditor#actionClear(boolean)
     */
    public void actionClear(boolean forceUnlock) {

        boolean modified = Boolean.valueOf(getParamModified()).booleanValue();
        if (forceUnlock || !modified) {
            // unlock the resource when force unlock is true or resource was not modified
            try {
                getCms().unlockResource(getParamResource());
            } catch (CmsException e) {
                // should usually never happen
                if (LOG.isInfoEnabled()) {
                    LOG.info(e);
                }
            }
        }
    }

    /**
     * Performs the exit editor action.<p> 
     * 
     * @see org.opencms.workplace.editors.CmsEditor#actionExit()
     */
    public void actionExit() throws IOException, JspException, ServletException {

        if (getAction() == ACTION_CANCEL) {
            // save and exit was canceled
            return;
        }

        // unlock resource, if no modified
        actionClear(false);

        // close the editor
        actionClose();
    }

    /**
     * Performs the save content ation.<p>
     * 
     * @see org.opencms.workplace.editors.CmsEditor#actionSave()
     */
    public void actionSave() throws JspException {

        CmsFile editFile = null;
        try {
            editFile = getCms().readFile(getParamResource(), CmsResourceFilter.ALL);
            // ensure all chars in the content are valid for the selected encoding
            String decodedContent = CmsEncoder.adjustHtmlEncoding(decodeContent(getParamContent()), getFileEncoding());

            try {
                editFile.setContents(decodedContent.getBytes(getFileEncoding()));
            } catch (UnsupportedEncodingException e) {
                throw new CmsException(
                    Messages.get().container(Messages.ERR_INVALID_CONTENT_ENC_1, getParamResource()),
                    e);
            }
            // the file content might have been modified during the write operation
            CmsFile writtenFile = getCms().writeFile(editFile);
            try {
                decodedContent = new String(writtenFile.getContents(), getFileEncoding());
            } catch (UnsupportedEncodingException e) {
                throw new CmsException(
                    Messages.get().container(Messages.ERR_INVALID_CONTENT_ENC_1, getParamResource()),
                    e);
            }
            setParamContent(encodeContent(decodedContent));
            // set the modified parameter
            setParamModified(Boolean.TRUE.toString());
        } catch (CmsException e) {
            showErrorPage(e);
        }

        if (getAction() != ACTION_CANCEL) {
            // save successful, set save action         
            setAction(ACTION_SAVE);
        }
    }

    /**
     * @see org.opencms.workplace.editors.CmsEditor#getEditorResourceUri()
     */
    public String getEditorResourceUri() {

        return getSkinUri() + "editors/" + EDITOR_TYPE + "/";
    }

    /**
     * Initializes the editor content when openening the editor for the first time.<p>
     */
    protected void initContent() {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);
        // get the default encoding
        String content = getParamContent();
        if (CmsStringUtil.isNotEmpty(content)) {
            // content already read, must be decoded 
            setParamContent(decodeContent(content));
            return;
        } else {
            content = "";
        }

        try {
            // lock resource if autolock is enabled
            checkLock(getParamResource());
            CmsFile editFile = getCms().readFile(getParamResource(), CmsResourceFilter.ALL);
            try {
                content = new String(editFile.getContents(), getFileEncoding());
            } catch (UnsupportedEncodingException e) {
                throw new CmsException(
                    Messages.get().container(Messages.ERR_INVALID_CONTENT_ENC_1, getParamResource()),
                    e);
            }
        } catch (CmsException e) {
            // reading of file contents failed, show error dialog
            try {
                showErrorPage(this, e);
            } catch (JspException exc) {
                // should usually never happen
                if (LOG.isInfoEnabled()) {
                    LOG.info(exc);
                }
            }
        }
        setParamContent(content);
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(EDITOR_TYPE);

        // set the action for the JSP switch 
        if (EDITOR_SAVE.equals(getParamAction())) {
            setAction(ACTION_SAVE);
        } else if (EDITOR_SAVEEXIT.equals(getParamAction())) {
            setAction(ACTION_SAVEEXIT);
        } else if (EDITOR_EXIT.equals(getParamAction())) {
            setAction(ACTION_EXIT);
        } else if (EDITOR_SHOW.equals(getParamAction())) {
            setAction(ACTION_SHOW);
        } else if (EDITOR_SHOW_ERRORMESSAGE.equals(getParamAction())) {
            setAction(ACTION_SHOW_ERRORMESSAGE);
        } else {
            // initial call of editor
            setAction(ACTION_DEFAULT);
            initContent();
        }

        setParamContent(encodeContent(getParamContent()));
    }
}
