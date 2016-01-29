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

package org.opencms.ui.apps;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.editors.I_CmsEditor;
import org.opencms.util.CmsUUID;

import org.apache.commons.logging.Log;

/**
 * The editor app. Will open the appropriate editor for a resource.<p>
 */
public class CmsEditor implements I_CmsWorkplaceApp {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsEditor.class);

    /** The resource id state prefix.  */
    public static final String RESOURCE_ID_PREFIX = "resourceId:";

    /** The resource id state prefix.  */
    public static final String RESOURCE_PATH_PREFIX = "resourcePath:";

    /** The back link prefix. */
    public static final String BACK_LINK_PREFIX = "backLink:";

    /** The back link prefix. */
    public static final String PLAIN_TEXT_PREFIX = "plainText:";

    /** The state separator. */
    public static final String STATE_SEPARATOR = ";;";

    /** The UUID length. */
    private static final int UUID_LENGTH = CmsUUID.getNullUUID().toString().length();

    /** The UI context. */
    private I_CmsAppUIContext m_context;

    /** The editor instance. */
    private I_CmsEditor m_editorInstance;

    /**
     * Returns the edit state for the given resource structure id.<p>
     *
     * @param resourceId the resource structure is
     * @param plainText if plain text/source editing is required
     * @param backLink the back link location
     *
     * @return the state
     */
    public static String getEditState(CmsUUID resourceId, boolean plainText, String backLink) {

        String state = CmsEditor.RESOURCE_ID_PREFIX
            + resourceId.toString()
            + CmsEditor.STATE_SEPARATOR
            + CmsEditor.PLAIN_TEXT_PREFIX
            + plainText
            + CmsEditor.STATE_SEPARATOR
            + CmsEditor.BACK_LINK_PREFIX
            + backLink;
        return state;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceApp#initUI(org.opencms.ui.apps.I_CmsAppUIContext)
     */
    public void initUI(I_CmsAppUIContext context) {

        m_context = context;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceApp#onStateChange(java.lang.String)
     */
    public void onStateChange(String state) {

        CmsUUID resId = getResourceIdFromState(state);
        String path = null;
        if (resId == null) {
            path = getResourcePathFromState(state);
        }

        CmsAppWorkplaceUi.get();
        CmsObject cms = A_CmsUI.getCmsObject();
        try {
            CmsResource resource;
            if (resId != null) {
                resource = cms.readResource(resId, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
            } else {
                resource = cms.readResource(path, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
            }
            I_CmsEditor editor = OpenCms.getWorkplaceAppManager().getEditorForResource(resource, isPlainText(state));
            if (editor != null) {
                m_editorInstance = editor.newInstance();
                m_editorInstance.initUI(m_context, resource, getBackLinkFromState(state));
            }

        } catch (CmsException e) {
            LOG.error("Error initializing the editor.", e);
            CmsErrorDialog.showErrorDialog(e);
        }
    }

    /**
     * Returns the back link info from the given state.<p>
     *
     * @param state the state
     *
     * @return the back link info
     */
    private String getBackLinkFromState(String state) {

        String result = null;
        int index = state.indexOf(BACK_LINK_PREFIX);
        if (index >= 0) {
            result = state.substring(index + BACK_LINK_PREFIX.length());
            index = result.indexOf(STATE_SEPARATOR);
            if (index > 0) {
                result = result.substring(0, index);
            }

        }
        return result;
    }

    /**
     * Returns the resource id form the given state.<p>
     *
     * @param state the state
     *
     * @return the resource id
     */
    private CmsUUID getResourceIdFromState(String state) {

        CmsUUID result = null;
        int index = state.indexOf(RESOURCE_ID_PREFIX);
        if (index >= 0) {
            String id = state.substring(
                index + RESOURCE_ID_PREFIX.length(),
                index + RESOURCE_ID_PREFIX.length() + UUID_LENGTH);
            if (CmsUUID.isValidUUID(id)) {
                result = new CmsUUID(id);
            }
        }
        return result;
    }

    /**
     * Returns the resource path from the given state.<p>
     *
     * @param state the state
     *
     * @return the resource path
     */
    private String getResourcePathFromState(String state) {

        String result = null;
        int index = state.indexOf(RESOURCE_PATH_PREFIX);
        if (index >= 0) {
            result = state.substring(index + RESOURCE_PATH_PREFIX.length());
            index = result.indexOf(STATE_SEPARATOR);
            if (index > 0) {
                result = result.substring(0, index);
            }

        }
        return result;
    }

    /**
     * Returns if plain text/source editing is requested
     *
     * @param state the state
     *
     * @return <code>true</code> if plain text/source editing is requested
     */
    private boolean isPlainText(String state) {

        return state.indexOf(PLAIN_TEXT_PREFIX + Boolean.TRUE.toString()) >= 0;
    }

}
