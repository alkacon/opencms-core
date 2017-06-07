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
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.I_CmsWindowCloseListener;
import org.opencms.ui.editors.I_CmsEditor;
import org.opencms.util.CmsUUID;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.vaadin.event.Action;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;

/**
 * The editor app. Will open the appropriate editor for a resource.<p>
 */
public class CmsEditor
implements I_CmsWorkplaceApp, ViewChangeListener, I_CmsWindowCloseListener, I_CmsHasShortcutActions {

    /** The serial version id. */
    private static final long serialVersionUID = 7503052469189004387L;

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

        try {
            backLink = URLEncoder.encode(backLink, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
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
     * Navigates to the back link target.<p>
     *
     * @param backlink the back link
     */
    public static void openBackLink(String backlink) {

        try {
            backlink = URLDecoder.decode(backlink, "UTF-8");
            String current = Page.getCurrent().getLocation().toString();
            if (current.contains("#")) {
                current = current.substring(0, current.indexOf("#"));
            }
            // check if the back link targets the workplace UI
            if (backlink.startsWith(current)) {
                // use the navigator to open the target
                String target = backlink.substring(backlink.indexOf("#") + 1);
                CmsAppWorkplaceUi.get().getNavigator().navigateTo(target);
            } else {
                // otherwise set the new location
                Page.getCurrent().setLocation(backlink);
            }
        } catch (UnsupportedEncodingException e) {
            // only in case of malformed charset
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * @see com.vaadin.navigator.ViewChangeListener#afterViewChange(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
     */
    public void afterViewChange(ViewChangeEvent event) {

        if (m_editorInstance instanceof ViewChangeListener) {
            ((ViewChangeListener)m_editorInstance).afterViewChange(event);
        }
    }

    /**
     * @see com.vaadin.navigator.ViewChangeListener#beforeViewChange(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
     */
    public boolean beforeViewChange(ViewChangeEvent event) {

        if (m_editorInstance instanceof ViewChangeListener) {
            return ((ViewChangeListener)m_editorInstance).beforeViewChange(event);
        } else {
            return true;
        }
    }

    /**
     * @see org.opencms.ui.apps.I_CmsHasShortcutActions#getShortcutActions()
     */
    public Map<Action, Runnable> getShortcutActions() {

        if (m_editorInstance instanceof I_CmsHasShortcutActions) {
            return ((I_CmsHasShortcutActions)m_editorInstance).getShortcutActions();
        } else {
            return Collections.EMPTY_MAP;
        }
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
        CmsObject cms = A_CmsUI.getCmsObject();
        final String backlink = getBackLinkFromState(state);
        try {
            CmsResource resource;
            if (resId != null) {
                resource = cms.readResource(resId, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
            } else {
                resource = cms.readResource(path, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
            }
            // make sure the user has the required role
            OpenCms.getRoleManager().checkRoleForResource(cms, CmsRole.ELEMENT_AUTHOR, cms.getSitePath(resource));
            I_CmsEditor editor = OpenCms.getWorkplaceAppManager().getEditorForResource(resource, isPlainText(state));
            if (editor != null) {
                m_editorInstance = editor.newInstance();
                m_editorInstance.initUI(m_context, resource, backlink);
            }

        } catch (Exception e) {
            LOG.error("Error initializing the editor.", e);
            CmsErrorDialog.showErrorDialog(e, new Runnable() {

                public void run() {

                    openBackLink(backlink);
                }
            });
        }
    }

    /**
     * @see org.opencms.ui.components.I_CmsWindowCloseListener#onWindowClose()
     */
    public void onWindowClose() {

        if (m_editorInstance instanceof I_CmsWindowCloseListener) {
            ((I_CmsWindowCloseListener)m_editorInstance).onWindowClose();
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
     * Returns if plain text/source editing is requested.
     *
     * @param state the state
     *
     * @return <code>true</code> if plain text/source editing is requested
     */
    private boolean isPlainText(String state) {

        return state.indexOf(PLAIN_TEXT_PREFIX + Boolean.TRUE.toString()) >= 0;
    }

}
