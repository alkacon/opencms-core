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

package org.opencms.ui.dialogs;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.util.CmsStringUtil;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Component;

/**
 * Separate UI for VAADIN based dialog embedded into a GWT module.<p>
 */
@Theme("opencms")
public class CmsEmbeddedDialogsUI extends A_CmsUI {

    /** The dialogs path fragment. */
    public static final String DIALOGS_PATH = "dialogs/";

    /** The available dialog classes by id. */
    private static Map<String, Class<? extends Component>> m_availableDialogs;

    /** The serial version id. */
    private static final long serialVersionUID = 1201184887611215370L;

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
     * @see org.opencms.ui.A_CmsUI#init(com.vaadin.server.VaadinRequest)
     */
    @Override
    protected void init(VaadinRequest request) {

        super.init(request);
        final I_CmsDialogContext context = new CmsEmbeddedDialogContext(Collections.<CmsResource> emptyList());
        try {
            OpenCms.getRoleManager().checkRole(getCmsObject(), CmsRole.ELEMENT_AUTHOR);

            try {
                Component dialog = getDialog(request, context);
                String title;
                if (dialog instanceof I_CmsHasTitle) {
                    title = ((I_CmsHasTitle)dialog).getTitle(getLocale());
                } else {
                    title = CmsVaadinUtils.getMessageText(org.opencms.ui.dialogs.Messages.GUI_DIALOG_0);
                }
                context.start(title, dialog);
            } catch (Throwable e) {
                CmsErrorDialog.showErrorDialog("Could not instantiate requested dialog", e, new Runnable() {

                    public void run() {

                        context.finish(null);
                    }
                });
            }
        } catch (CmsRoleViolationException ex) {
            CmsErrorDialog.showErrorDialog(
                CmsVaadinUtils.getMessageText(Messages.GUI_WORKPLACE_ACCESS_DENIED_TITLE_0),
                ex,
                new Runnable() {

                    public void run() {

                        context.finish(null);

                    }
                });
        }
    }

    /**
     * Creates a dialog instance.<p>
     *
     * @param context the dialog context
     * @param dialogClass the dialog class
     *
     * @return the instance
     *
     * @throws Exception in case instantiating the dialog fails
     */
    Component createDialogInstance(I_CmsDialogContext context, Class<? extends Component> dialogClass)
    throws Exception {

        Constructor<? extends Component> constructor = dialogClass.getConstructor(I_CmsDialogContext.class);
        return constructor.newInstance(context);
    }

    /**
     * Returns the requested dialog instance.<p>
     *
     * @param request the VAADIN request
     * @param context the dialog context
     *
     * @return the dialog instance
     *
     * @throws Exception in case instantiating the dialog fails
     */
    private Component getDialog(VaadinRequest request, I_CmsDialogContext context) throws Exception {

        if (m_availableDialogs == null) {
            Map<String, Class<? extends Component>> dialogs = new HashMap<String, Class<? extends Component>>();
            dialogs.put(CmsUserDataDialog.DIALOG_ID, CmsUserDataDialog.class);
            m_availableDialogs = dialogs;
        }
        String dialogId = getDialogId(request);
        Component result = null;
        if (m_availableDialogs.containsKey(dialogId)) {
            result = createDialogInstance(context, m_availableDialogs.get(dialogId));
        }
        return result;
    }

    /**
     * Returns the dialog id extracted from the requested path.<p>
     *
     * @param request the request
     *
     * @return the id
     */
    private String getDialogId(VaadinRequest request) {

        String path = request.getPathInfo();
        // remove the leading slash
        return path != null ? path.substring(1) : null;
    }
}
