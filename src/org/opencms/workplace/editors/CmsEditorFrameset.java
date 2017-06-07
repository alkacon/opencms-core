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

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceSettings;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * Helper class to create the editor frameset.<p>
 *
 * The following files use this class:
 * <ul>
 * <li>/jsp/editors/editor_html
 * </ul>
 * <p>
 *
 * @since 6.0.0
 */
public class CmsEditorFrameset extends CmsEditor {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsEditorFrameset.class);

    /** The title to be displayed in the editor. */
    private String m_paramEditorTitle;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsEditorFrameset(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Deletes the temporary file and unlocks the edited resource when in direct edit mode.<p>
     *
     * This method is needed in the editor close help frame, which is called when the user presses
     * the "back" button or closes the browser window when editing a page.<p>
     *
     * @param forceUnlock if true, the resource will be unlocked anyway
     */
    @Override
    public void actionClear(boolean forceUnlock) {

        // delete the temporary file
        deleteTempFile();
        if (Boolean.valueOf(getParamDirectedit()).booleanValue() || forceUnlock) {
            // unlock the resource when in direct edit mode or force unlock is true
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
     * @see org.opencms.workplace.editors.CmsEditor#actionExit()
     */
    @Override
    public final void actionExit() {

        // do nothing
    }

    /**
     * @see org.opencms.workplace.editors.CmsEditor#actionSave()
     */
    @Override
    public final void actionSave() {

        // do nothing
    }

    /**
     * @see org.opencms.workplace.editors.CmsEditor#getEditorResourceUri()
     */
    @Override
    public final String getEditorResourceUri() {

        // return empty String
        return "";
    }

    /**
     * Returns the editor title.<p>
     *
     * @return the editor title
     */
    public String getParamEditorTitle() {

        if (CmsStringUtil.isEmpty(m_paramEditorTitle)) {
            return key(Messages.GUI_EDITOR_TITLE_PREFIX_0) + " " + getParamResource();
        }
        return m_paramEditorTitle;
    }

    /**
     * Sets the editor title.<p>
     *
     * @param editorTitle the editor title to set
     */
    public void setParamEditorTitle(String editorTitle) {

        m_paramEditorTitle = editorTitle;
    }

    /**
     * @see org.opencms.workplace.editors.CmsEditor#initContent()
     */
    @Override
    protected final void initContent() {

        // do nothing
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(settings, request);

        if (getDialogRealUri().endsWith("editor.jsp")) {
            // check the required permissions to edit the resource only in the main frame
            if (!checkResourcePermissions(CmsPermissionSet.ACCESS_WRITE, false)) {
                // not write permissions in the folder, close editor
                try {
                    actionClose();
                } catch (Exception e) {
                    // should usually never happen
                    if (LOG.isInfoEnabled()) {
                        LOG.info(e);
                    }
                }
            }
        }
    }
}
