/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/CmsSimplePageEditor.java,v $
 * Date   : $Date: 2005/06/27 23:22:23 $
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

import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.xml.page.CmsXmlPageFactory;

import java.util.Iterator;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import org.apache.commons.logging.Log;

/**
 * Creates the output for editing a CmsDefaultPage with the simple textarea editor.<p> 
 * 
 * The following editor uses this class:
 * <ul>
 * <li>/editors/simplehtml/editor.jsp
 * </ul>
 * <p>
 *
 * @author  Andreas Zahner 
 * 
 * @version $Revision: 1.12 $ 
 * 
 * @since 6.0.0 
 */
public class CmsSimplePageEditor extends CmsDefaultPageEditor {

    /** Constant for the editor type, must be the same as the editors subfolder name in the VFS. */
    private static final String EDITOR_TYPE = "simplehtml";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSimplePageEditor.class);

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsSimplePageEditor(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * @see org.opencms.workplace.editors.CmsDefaultPageEditor#buildGalleryButtons(CmsEditorDisplayOptions, int, Properties)
     */
    public String buildGalleryButtons(CmsEditorDisplayOptions options, int buttonStyle, Properties displayOptions) {

        StringBuffer result = new StringBuffer();
        Iterator galleries = OpenCms.getWorkplaceManager().getGalleries().keySet().iterator();
        while (galleries.hasNext()) {
            String galleryType = (String)galleries.next();
            if (options.showElement("gallery." + galleryType.replaceFirst("gallery", ""), displayOptions)) {
                if (result.length() == 0) {
                    result.append(", \"separator\"");
                }
                result.append(", \"" + galleryType + "\"");
            }
        }
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.editors.CmsEditor#getEditorResourceUri()
     */
    public String getEditorResourceUri() {

        return getSkinUri() + "editors/" + EDITOR_TYPE + "/";
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);

        // set the dialog type
        setParamDialogtype(EDITOR_TYPE);

        // Initialize a page object from the temporary file
        if (getParamTempfile() != null && !"null".equals(getParamTempfile())) {
            try {
                m_file = getCms().readFile(this.getParamTempfile(), CmsResourceFilter.ALL);
                m_page = CmsXmlPageFactory.unmarshal(getCms(), m_file);
            } catch (CmsException e) {
                // error during initialization
                try {
                    showErrorPage(this, e);
                } catch (JspException exc) {
                    // should usually never happen
                    if (LOG.isInfoEnabled()) {
                        LOG.info(exc);
                    }
                }
            }
        }

        // set the action for the JSP switch 
        if (EDITOR_SAVE.equals(getParamAction())) {
            setAction(ACTION_SAVE);
        } else if (EDITOR_SAVEEXIT.equals(getParamAction())) {
            setAction(ACTION_SAVEEXIT);
        } else if (EDITOR_SAVEACTION.equals(getParamAction())) {
            setAction(ACTION_SAVEACTION);
            try {
                actionDirectEdit();
            } catch (Exception e) {
                // should usually never happen
                if (LOG.isInfoEnabled()) {
                    LOG.info(e);
                }
            }
            setAction(ACTION_EXIT);
        } else if (EDITOR_EXIT.equals(getParamAction())) {
            setAction(ACTION_EXIT);
        } else if (EDITOR_CHANGE_ELEMENT.equals(getParamAction())) {
            setAction(ACTION_SHOW);
            actionChangeBodyElement();
            // prepare the content String for the editor
            prepareContent(false);
        } else if (EDITOR_CLEANUP.equals(getParamAction())) {
            setAction(ACTION_SHOW);
            actionCleanupBodyElement();
            // prepare the content String for the editor
            prepareContent(false);
        } else if (EDITOR_SHOW.equals(getParamAction())) {
            setAction(ACTION_SHOW);
            // prepare the content String for the editor
            prepareContent(false);
        } else if (EDITOR_PREVIEW.equals(getParamAction())) {
            setAction(ACTION_PREVIEW);
        } else {
            // initial call of editor, initialize page and page parameters
            setAction(ACTION_DEFAULT);
            try {
                // lock resource if autolock is enabled in configuration
                if ("true".equals(getParamDirectedit())) {
                    // set a temporary lock in direct edit mode
                    checkLock(getParamResource(), CmsLock.TEMPORARY);
                } else {
                    // set common lock
                    checkLock(getParamResource());
                }
                // create the temporary file
                setParamTempfile(createTempFile());
                // initialize a page object from the created temporary file
                m_file = getCms().readFile(this.getParamTempfile(), CmsResourceFilter.ALL);
                m_page = CmsXmlPageFactory.unmarshal(getCms(), m_file);
            } catch (CmsException e) {
                // error during initialization
                try {
                    showErrorPage(this, e);
                } catch (JspException exc) {
                    // should usually never happen
                    if (LOG.isInfoEnabled()) {
                        LOG.info(exc);
                    }
                }
            }
            // set the initial body language & name if not given in request parameters
            if (getParamElementlanguage() == null) {
                initBodyElementLanguage();
            }
            if (getParamElementname() == null) {
                initBodyElementName(null);
            }
            // initialize the editor content
            initContent();
            // prepare the content String for the editor
            prepareContent(false);
        }
    }

    /**
     * Manipulates the content String and removes leading and trailing white spaces.<p>
     * 
     * @param save if set to true, the content parameter is not updated
     * @return the prepared content String
     */
    protected String prepareContent(boolean save) {

        String content = getParamContent().trim();
        // ensure all chars in the content are valid for the selected encoding
        content = CmsEncoder.adjustHtmlEncoding(content, getFileEncoding());
        if (!save) {
            setParamContent(content);
        }
        return content;
    }

}
