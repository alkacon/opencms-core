/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/workplace/broadcast/CmsSendEmailDialog.java,v $
 * Date   : $Date: 2005/06/16 14:40:58 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.tools.workplace.broadcast;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsSessionInfo;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsTextareaWidget;
import org.opencms.workplace.CmsWidgetDialogParameter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Dialog to edit an email to send in the administration view.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com)
 * 
 * @version $Revision: 1.3 $
 * @since 5.9.1
 */
public class CmsSendEmailDialog extends A_CmsMessageDialog {

    /** localized messages Keys prefix. */
    public static final String C_KEY_PREFIX = "email";

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsSendEmailDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSendEmailDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Commits the edited project to the db.<p>
     */
    public void actionCommit() {

        List errors = new ArrayList();

        try {
            m_msgInfo.setTo(getEmailAddresses());
            m_msgInfo.sendEmail(getCms());
        } catch (Throwable t) {
            errors.add(t);
        } finally {
            m_msgInfo.setTo(getToNames());
        }
        // set the list of errors to display when saving failed
        setCommitErrors(errors);
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#createDialogHtml(java.lang.String)
     */
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        result.append(createWidgetTableStart());
        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {
            // create the widgets for the first dialog page
            result.append(dialogBlockStart(key(Messages.GUI_MESSAGE_EDITOR_LABEL_HEADER_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 3));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
            result.append(dialogBlockStart(key(Messages.GUI_MESSAGE_EDITOR_LABEL_CONTENT_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(4, 4));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * Creates the list of widgets for this dialog.<p>
     */
    protected void defineWidgets() {

        // initialize the project object to use for the dialog
        initMessageObject();

        setKeyPrefix(C_KEY_PREFIX);

        addWidget(new CmsWidgetDialogParameter(m_msgInfo, "from", PAGES[0], new CmsDisplayWidget()));
        addWidget(new CmsWidgetDialogParameter(m_msgInfo, "to", PAGES[0], new CmsDisplayWidget()));
        addWidget(new CmsWidgetDialogParameter(m_msgInfo, "cc", PAGES[0], "", new CmsInputWidget(), 0, 1));
        addWidget(new CmsWidgetDialogParameter(m_msgInfo, "subject", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_msgInfo, "msg", PAGES[0], new CmsTextareaWidget(12)));
    }

    /**
     * Returns a semicolon separated list of email addresses.<p>
     * 
     * @return a semicolon separated list of email addresses
     */
    private String getEmailAddresses() {

        List emails = new ArrayList();
        Iterator itIds = idsList().iterator();
        while (itIds.hasNext()) {
            String id = itIds.next().toString();
            CmsSessionInfo session = OpenCms.getSessionManager().getSessionInfo(id);
            if (session != null) {
                String emailAddress = session.getUser().getEmail();
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(emailAddress) && !emails.contains(emailAddress)) {
                    emails.add(emailAddress);
                }
            }
        }
        StringBuffer result = new StringBuffer(256);
        Iterator itEmails = emails.iterator();
        while (itEmails.hasNext()) {
            result.append(itEmails.next().toString());
            if (itEmails.hasNext()) {
                result.append("; ");
            }
        }
        return result.toString();
    }
}