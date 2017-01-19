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

package org.opencms.ui.error;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.util.Messages;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsMultiException;
import org.opencms.main.I_CmsThrowable;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.CmsLegacyApp;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.shared.CmsVaadinConstants;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.WrappedSession;
import com.vaadin.shared.Version;
import com.vaadin.ui.JavaScript;

/**
 * Displays the error page.<p>
 */
@Theme("opencms")
public class CmsErrorUI extends A_CmsUI {

    /** The serial version id. */
    private static final long serialVersionUID = -7274300240145879438L;

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsErrorUI.class);

    /** The throwable session attribute name. */
    private static final String THROWABLE = "THROWABLE";

    /** The path session attribute name. */
    private static final String PATH = "PATH";

    /** The error page path fragment. */
    public static final String ERROR_PAGE_PATH_FRAQUMENT = "errorpage/";

    /** The path to the requested page. */
    @SuppressWarnings("unused")
    private String m_requestedPage;

    /** The displayed exception. */
    private Throwable m_throwable;

    /**
     * Returns the error bootstrap page HTML.<p>
     *
     * @param cms the cms context
     * @param throwable the throwable
     * @param request the current request
     *
     * @return the error bootstrap page HTML
     */
    public static String getBootstrapPage(CmsObject cms, Throwable throwable, HttpServletRequest request) {

        try {
            setErrorAttributes(cms, throwable, request);

            byte[] pageBytes = CmsFileUtil.readFully(
                Thread.currentThread().getContextClassLoader().getResourceAsStream(
                    "org/opencms/ui/error/error-page.html"));
            String page = new String(pageBytes, "UTF-8");
            CmsMacroResolver resolver = new CmsMacroResolver();
            String context = OpenCms.getSystemInfo().getContextPath();
            String vaadinDir = CmsStringUtil.joinPaths(context, "VAADIN/");
            String vaadinVersion = Version.getFullVersion();
            String vaadinServlet = CmsStringUtil.joinPaths(context, "workplace/", ERROR_PAGE_PATH_FRAQUMENT);
            String vaadinBootstrap = CmsStringUtil.joinPaths(context, "VAADIN/vaadinBootstrap.js");

            resolver.addMacro("loadingHtml", CmsVaadinConstants.LOADING_INDICATOR_HTML);
            resolver.addMacro("vaadinDir", vaadinDir);
            resolver.addMacro("vaadinVersion", vaadinVersion);
            resolver.addMacro("vaadinServlet", vaadinServlet);
            resolver.addMacro("vaadinBootstrap", vaadinBootstrap);
            resolver.addMacro("title", "Error page");

            page = resolver.resolveMacros(page);
            return page;
        } catch (Exception e) {
            LOG.error("Failed to display error page.", e);
            return "<!--Error-->";
        }
    }

    /**
     * Sets the error attributes to the current session.<p>
     *
     * @param cms the cms context
     * @param throwable the throwable
     * @param request the current request
     */
    private static void setErrorAttributes(CmsObject cms, Throwable throwable, HttpServletRequest request) {

        String errorUri = CmsFlexController.getThrowableResourceUri(request);
        if (errorUri == null) {
            errorUri = cms.getRequestContext().getUri();
        }

        // try to get the exception root cause
        Throwable cause = CmsFlexController.getThrowable(request);
        if (cause == null) {
            cause = throwable;
        }

        request.getSession().setAttribute(THROWABLE, cause);
        request.getSession().setAttribute(PATH, errorUri);
    }

    /**
     * @see com.vaadin.ui.UI#init(com.vaadin.server.VaadinRequest)
     */
    @Override
    protected void init(VaadinRequest request) {

        readErrorAttributes();
        CmsErrorDialog.showErrorDialog(getErrorMessage(m_throwable), m_throwable, new Runnable() {

            public void run() {

                JavaScript.eval(
                    "if (window.parent && window.parent."
                        + CmsLegacyApp.VAR_IS_LEGACY_APP
                        + ") window.parent.location.reload();");
            }
        });
    }

    /**
     * Returns the error message to be displayed.<p>
     *
     * @param throwable the throwable
     *
     * @return the error message to be displayed
     */
    private String getErrorMessage(Throwable throwable) {

        StringBuffer result = new StringBuffer(512);

        result.append(
            CmsVaadinUtils.getMessageText(org.opencms.ui.components.Messages.GUI_ERROR_DIALOG_MESSAGE_0)).append(
                "<br />");

        // if a localized message is already set as a parameter, append it.
        result.append(getMessage(throwable));

        return result.toString();
    }

    /**
     * Returns the localized Message, if the argument is a CmsException, or
     * the message otherwise.<p>
     *
     * @param t the Throwable to get the message from
     *
     * @return returns the localized Message, if the argument is a CmsException, or
     * the message otherwise
     */
    private String getMessage(Throwable t) {

        if ((t instanceof I_CmsThrowable) && (((I_CmsThrowable)t).getMessageContainer() != null)) {
            StringBuffer result = new StringBuffer(256);
            if (t instanceof CmsMultiException) {
                CmsMultiException exc = (CmsMultiException)t;
                String message = exc.getMessage(getLocale());
                if (CmsStringUtil.isNotEmpty(message)) {
                    result.append(message);
                    result.append("<br />");
                }

            }

            I_CmsThrowable cmsThrowable = (I_CmsThrowable)t;
            result.append(cmsThrowable.getLocalizedMessage(getLocale()));
            return result.toString();
        } else {
            String message = t.getMessage();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(message)) {
                // no error message found (e.g. for NPE), provide default message text
                message = CmsVaadinUtils.getMessageText(Messages.GUI_ERROR_UNKNOWN_0);
            }
            return message;
        }
    }

    /**
     * Reads the error attributes from current session.<p>
     */
    private void readErrorAttributes() {

        WrappedSession session = getSession().getSession();
        m_requestedPage = (String)session.getAttribute(PATH);
        m_throwable = (Throwable)session.getAttribute(THROWABLE);

        // remove the attributes after read to keep the session clean
        session.removeAttribute(THROWABLE);
        session.removeAttribute(PATH);
    }
}
