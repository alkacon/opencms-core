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

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsResourceTypeConfig;
import org.opencms.ade.contenteditor.shared.CmsEditorConstants;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspTagEdit;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.I_CmsWindowCloseListener;
import org.opencms.ui.editors.I_CmsEditor;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.editors.CmsXmlContentEditor;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.vaadin.event.Action;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServletRequest;

/**
 * The editor app. Will open the appropriate editor for a resource.<p>
 */
public class CmsEditor
implements I_CmsWorkplaceApp, ViewChangeListener, I_CmsWindowCloseListener, I_CmsHasShortcutActions {

    /** The back link prefix. */
    public static final String BACK_LINK_PREFIX = "backLink";

    /** The back link prefix. */
    public static final String PLAIN_TEXT_PREFIX = "plainText";

    /** The resource id state prefix.  */
    public static final String RESOURCE_ID_PREFIX = "resourceId";

    /** The resource id state prefix.  */
    public static final String RESOURCE_PATH_PREFIX = "resourcePath";

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsEditor.class);

    /** The serial version id. */
    private static final long serialVersionUID = 7503052469189004387L;

    /** The UI context. */
    private I_CmsAppUIContext m_context;

    /** The editor instance. */
    private I_CmsEditor m_editorInstance;

    /**
     * Returns the edit link for given resource structure id.<p>
     *
     * @param structureId the resource structure is
     * @param plainText if plain text/source editing is required
     * @param backLink the back link location
     *
     * @return the state
     */
    public static String getEditLink(CmsUUID structureId, boolean plainText, String backLink) {

        return CmsVaadinUtils.getWorkplaceLink(
            org.opencms.ui.apps.CmsEditorConfiguration.APP_ID,
            getEditState(structureId, plainText, backLink));
    }

    /**
     * Returns the edit state for the given resource structure id.<p>
     *
     * @param structureId the resource structure id
     * @param plainText if plain text/source editing is required
     * @param backLink the back link location
     *
     * @return the state
     */
    public static String getEditState(CmsUUID structureId, boolean plainText, String backLink) {

        String state = "";
        state = A_CmsWorkplaceApp.addParamToState(state, CmsEditor.RESOURCE_ID_PREFIX, structureId.toString());
        state = A_CmsWorkplaceApp.addParamToState(state, CmsEditor.PLAIN_TEXT_PREFIX, String.valueOf(plainText));
        state = A_CmsWorkplaceApp.addParamToState(state, CmsEditor.BACK_LINK_PREFIX, backLink);
        return state;
    }

    /**
     * Returns the edit state for the given resource structure id.<p>
     *
     * @param cms the cms context
     * @param resourceType the resource type to create
     * @param contextPath the context path
     * @param modelFilePath the model file path
     * @param plainText if plain text/source editing is required
     * @param backLink the back link location
     *
     * @return the state
     */
    public static String getEditStateForNew(
        CmsObject cms,
        I_CmsResourceType resourceType,
        String contextPath,
        String modelFilePath,
        boolean plainText,
        String backLink) {

        String state = "";

        state = A_CmsWorkplaceApp.addParamToState(
            state,
            CmsXmlContentEditor.PARAM_NEWLINK,
            CmsJspTagEdit.getNewLink(cms, resourceType, contextPath));

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(modelFilePath)) {
            state = A_CmsWorkplaceApp.addParamToState(state, CmsWorkplace.PARAM_MODELFILE, modelFilePath);
            state = A_CmsWorkplaceApp.addParamToState(
                state,
                CmsEditorConstants.PARAM_MODE,
                CmsEditorConstants.MODE_COPY);
        }
        state = A_CmsWorkplaceApp.addParamToState(state, CmsEditor.RESOURCE_PATH_PREFIX, contextPath);
        state = A_CmsWorkplaceApp.addParamToState(state, CmsEditor.PLAIN_TEXT_PREFIX, String.valueOf(plainText));
        state = A_CmsWorkplaceApp.addParamToState(state, CmsEditor.BACK_LINK_PREFIX, backLink);

        return state;
    }

    /**
     * Navigates to the back link target.<p>
     *
     * @param backlink the back link
     */
    public static void openBackLink(String backlink) {

        if (!CmsRequestUtil.checkBacklink(backlink, ((VaadinServletRequest)VaadinRequest.getCurrent()))) {
            backlink = CmsVaadinUtils.getWorkplaceLink();
        }

        String current = Page.getCurrent().getLocation().toString();
        if (current.contains("#")) {
            current = current.substring(0, current.indexOf("#"));
        }
        // check if the back link targets the workplace UI
        if (backlink.startsWith(current)) {
            // use the navigator to open the target
            String target = backlink.substring(backlink.indexOf("#") + 1);
            String decodedTarget = CmsEncoder.decode(target);
            CmsAppWorkplaceUi.get().getNavigator().navigateTo(decodedTarget);
        } else {
            // otherwise set the new location
            Page.getCurrent().setLocation(backlink);
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
            CmsResource resource = null;
            if (resId != null) {
                resource = cms.readResource(resId, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
            } else if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(path)) {
                resource = cms.readResource(path, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
            }
            Map<String, String> params = A_CmsWorkplaceApp.getParamsFromState(state);
            String newLink = params.get(CmsXmlContentEditor.PARAM_NEWLINK);
            I_CmsEditor editor = null;
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(newLink)) {

                // make sure the user has the required role
                OpenCms.getRoleManager().checkRoleForResource(cms, CmsRole.ELEMENT_AUTHOR, cms.getSitePath(resource));
                editor = OpenCms.getWorkplaceAppManager().getEditorForResource(cms, resource, isPlainText(state));
            } else {
                String typeName = CmsJspTagEdit.getTypeFromNewLink(newLink);
                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(typeName);
                editor = OpenCms.getWorkplaceAppManager().getEditorForType(type, isPlainText(state));
                String rootPath = CmsJspTagEdit.getRootPathFromNewLink(newLink);
                CmsADEConfigData data = OpenCms.getADEManager().lookupConfiguration(cms, rootPath);
                CmsResourceTypeConfig typeConfig = data.getResourceType(typeName);
                if (!typeConfig.checkCreatable(cms, rootPath)) {
                    throw new RuntimeException();
                }

            }
            if (editor != null) {
                m_editorInstance = editor.newInstance();

                params.remove(BACK_LINK_PREFIX);
                params.remove(RESOURCE_ID_PREFIX);
                params.remove(RESOURCE_PATH_PREFIX);
                params.remove(PLAIN_TEXT_PREFIX);
                m_editorInstance.initUI(m_context, resource, backlink, params);
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

        return A_CmsWorkplaceApp.getParamFromState(state, BACK_LINK_PREFIX);
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
        String id = A_CmsWorkplaceApp.getParamFromState(state, RESOURCE_ID_PREFIX);
        if (CmsUUID.isValidUUID(id)) {
            result = new CmsUUID(id);
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

        return A_CmsWorkplaceApp.getParamFromState(state, RESOURCE_PATH_PREFIX);
    }

    /**
     * Returns if plain text/source editing is requested.
     *
     * @param state the state
     *
     * @return <code>true</code> if plain text/source editing is requested
     */
    private boolean isPlainText(String state) {

        String val = A_CmsWorkplaceApp.getParamFromState(state, PLAIN_TEXT_PREFIX);
        return Boolean.parseBoolean(val);
    }

}
