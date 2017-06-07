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

package org.opencms.workplace.tools.workplace.rfsfile;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.util.CmsRfsFileViewer;
import org.opencms.workplace.CmsWidgetDialog;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Extending this class enables different
 * <code>{@link org.opencms.workplace.CmsWidgetDialog}</code> implementations to
 * share the access to a file in the RFS via the member {@link #m_logView}.<p>
 *
 * Here the support for the init / commit lifecycle of this RFS file access is
 * added transparently.<p>
 *
 * @since 6.0.0
 */
public abstract class A_CmsRfsFileWidgetDialog extends CmsWidgetDialog {

    /** The pages array for possible multi-page dialogs. This is a dummy. */
    public static String[] PAGES = {"page1"};

    /** The bean that accesses the underlying file in portions. */
    protected CmsRfsFileViewer m_logView;

    /**
     * Initializes the dialog object: a <code>{@link CmsRfsFileViewer}</code> bean that
     * is shared amongst all related dialog classes (subclasses of this class). <p>
     *
     * @param jsp the bundle of all request-response related information
     */
    public A_CmsRfsFileWidgetDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Delegates to the 2nd constructor. <p>
     *
     * @param context a PageContext
     * @param req the HttpServletRequest
     * @param res the HttpServletResponse
     */
    public A_CmsRfsFileWidgetDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Commits the <code>{@link CmsRfsFileViewer}</code> to the
     * <code>{@link org.opencms.workplace.CmsWorkplaceManager}</code>. <p>
     *
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    @Override
    public void actionCommit() {

        List<Throwable> errors = new ArrayList<Throwable>();
        try {
            OpenCms.getWorkplaceManager().setFileViewSettings(getCms(), m_logView);
        } catch (CmsRoleViolationException e) {
            errors.add(e);
        }
        // set the list of errors to display when saving failed
        setCommitErrors(errors);
    }

    /**
     * Subclasses have to invoke this method with <code>super.defineWidgets()</code>
     * as here the internal bean <code>{@link #m_logView}</code> is retrieved. <p>
     *
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    @Override
    protected void defineWidgets() {

        initLogfileViewBean();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * Initializes the login message object for this dialog.<p>
     *
     * The {@link CmsRfsFileViewer} instance is obtained from the
     * <code>{@link org.opencms.workplace.CmsWorkplaceManager}</code>.
     */
    protected void initLogfileViewBean() {

        // clone to get a modifiable (not frozen) instance.
        m_logView = (CmsRfsFileViewer)OpenCms.getWorkplaceManager().getFileViewSettings().clone();
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    @Override
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // also include top-level messages to allow the admin navigation access messages of the module top-package
        // that is shared with other tools.
        addMessages(org.opencms.workplace.tools.workplace.Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }
}