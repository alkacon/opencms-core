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

package org.opencms.ui.dialogs;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.gwt.shared.I_CmsAutoBeanFactory;
import org.opencms.gwt.shared.I_CmsEmbeddedDialogInfo;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext.ContextType;
import org.opencms.ui.actions.I_CmsWorkplaceAction;
import org.opencms.ui.apps.CmsEditorConfiguration;
import org.opencms.ui.apps.CmsPageEditorConfiguration;
import org.opencms.ui.apps.CmsSitemapEditorConfiguration;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.extensions.CmsEmbeddedDialogExtension;
import org.opencms.ui.dialogs.permissions.CmsPrincipalSelectDialog;
import org.opencms.ui.shared.rpc.I_CmsEmbeddingServerRpc;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;

/**
 * Separate UI for VAADIN based dialog embedded into a GWT module.<p>
 */
@Theme("opencms")
public class CmsEmbeddedDialogsUI extends A_CmsUI implements I_CmsEmbeddingServerRpc {

    /** The dialogs path fragment. */
    public static final String DIALOGS_PATH = "dialogs/";

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsEmbeddedDialogsUI.class);

    /** The serial version id. */
    private static final long serialVersionUID = 1201184887611215370L;

    /** The auto bean factory for the dialog configuration. */
    private static I_CmsAutoBeanFactory m_beanFactory = AutoBeanFactorySource.create(I_CmsAutoBeanFactory.class);

    /**
     * The dialog context of the currently opened dialog.<p>
     */
    CmsEmbeddedDialogContext m_currentContext;

    /** The extension used to communicate with the client. */
    protected CmsEmbeddedDialogExtension m_extension;

    /**
     * Returns the context path for embedded dialogs.<p>
     *
     * @return the context path for embedded dialogs
     */
    public static String getEmbeddedDialogsContextPath() {

        return CmsStringUtil.joinPaths(OpenCms.getSystemInfo().getWorkplaceContext(), DIALOGS_PATH);
    }

    /**
     * @see com.vaadin.ui.AbstractComponent#getLocale()
     */
    @Override
    public Locale getLocale() {

        CmsObject cms = getCmsObject();
        return OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsEmbeddingServerRpc#loadDialog(java.lang.String)
     */
    public void loadDialog(String dialogInfo) {

        Throwable t = null;
        String errorMessage = null;
        I_CmsEmbeddedDialogInfo info = AutoBeanCodex.decode(
            m_beanFactory,
            I_CmsEmbeddedDialogInfo.class,
            dialogInfo).as();

        try {
            OpenCms.getRoleManager().checkRole(getCmsObject(), CmsRole.ELEMENT_AUTHOR);
            if (CmsPrincipalSelectDialog.DIALOG_ID.equals(info.getDialogId())) {
                m_currentContext = new CmsEmbeddedDialogContext(
                    CmsPrincipalSelectDialog.DIALOG_ID,
                    m_extension,
                    null,
                    null,
                    info.getParameters());
                CmsPrincipalSelectDialog.openEmbeddedDialogV2(m_currentContext, info.getParameters(), true);
            } else {
                try {
                    List<String> resources = info.getStructureIds();
                    List<CmsResource> resourceList = new ArrayList<>();
                    for (String uuid : resources) {
                        if (CmsUUID.isValidUUID(uuid)) {
                            resourceList.add(getCmsObject().readResource(new CmsUUID(uuid), CmsResourceFilter.ALL));
                        }

                    }
                    String typeParam = info.getContextType();
                    boolean isEditor = Boolean.parseBoolean(info.getParameters().get("editor"));
                    ContextType type;
                    String appId = "";
                    try {
                        type = ContextType.valueOf(typeParam);
                        if (ContextType.containerpageToolbar.equals(type)) {
                            appId = CmsPageEditorConfiguration.APP_ID;
                        } else if (ContextType.sitemapToolbar.equals(type)) {
                            appId = CmsSitemapEditorConfiguration.APP_ID;
                        } else if (isEditor) {
                            appId = CmsEditorConfiguration.APP_ID;
                        }
                    } catch (Exception e) {
                        type = ContextType.appToolbar;
                        LOG.error("Could not parse context type parameter " + typeParam);
                    }

                    m_currentContext = new CmsEmbeddedDialogContext(
                        appId,
                        m_extension,
                        type,
                        resourceList,
                        info.getParameters());
                    I_CmsWorkplaceAction action = getAction(info.getDialogId());
                    if (action.isActive(m_currentContext)) {
                        action.executeAction(m_currentContext);
                    } else {
                        errorMessage = CmsVaadinUtils.getMessageText(Messages.GUI_WORKPLACE_ACCESS_DENIED_TITLE_0);
                    }
                } catch (Throwable e) {
                    t = e;
                    errorMessage = CmsVaadinUtils.getMessageText(
                        org.opencms.ui.dialogs.Messages.ERR_DAILOG_INSTANTIATION_FAILED_1,
                        info.getDialogId());
                }
            }
        } catch (CmsRoleViolationException ex) {
            t = ex;
            errorMessage = CmsVaadinUtils.getMessageText(Messages.GUI_WORKPLACE_ACCESS_DENIED_TITLE_0);
        }
        if (errorMessage != null) {
            CmsErrorDialog.showErrorDialog(errorMessage, t, new Runnable() {

                public void run() {

                    m_currentContext = new CmsEmbeddedDialogContext(
                        "",
                        m_extension,
                        null,
                        Collections.<CmsResource> emptyList(),
                        info.getParameters());
                    m_currentContext.finish((Collection<CmsUUID>)null);
                }
            });
        }

    }

    /**
     * @see org.opencms.ui.A_CmsUI#reload()
     */
    @Override
    public void reload() {

        if (m_currentContext != null) {
            m_currentContext.reload();
        }
    }

    /**
     * @see org.opencms.ui.A_CmsUI#init(com.vaadin.server.VaadinRequest)
     */
    @Override
    protected void init(VaadinRequest request) {

        super.init(request);
        try {
            OpenCms.getRoleManager().checkRole(getCmsObject(), CmsRole.ELEMENT_AUTHOR);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        m_extension = new CmsEmbeddedDialogExtension(this);
        m_extension.getClientRPC().initServerRpc();
    }

    /**
     * Returns the dialog action for a given dialog id.
     *
     * @param dialogId the dialog id
     *
     * @return the dialog action
     *
     * @throws Exception in case instantiating the action fails
     */
    private I_CmsWorkplaceAction getAction(String dialogId) throws Exception {

        @SuppressWarnings("unchecked")
        Class<I_CmsWorkplaceAction> actionClass = (Class<I_CmsWorkplaceAction>)getClass().getClassLoader().loadClass(
            dialogId);
        return actionClass.newInstance();
    }

}
