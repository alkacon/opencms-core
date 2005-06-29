/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/workplace/broadcast/CmsSendEmailDialog.java,v $
 * Date   : $Date: 2005/06/29 20:16:25 $
 * Version: $Revision: 1.10 $
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

package org.opencms.workplace.tools.workplace.broadcast;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.CmsSessionInfo;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsTextareaWidget;
import org.opencms.workplace.CmsWidgetDialogParameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Dialog to edit an email to send in the administration view.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.10 $ 
 * 
 * @since 6.0.0 
 */
public class CmsSendEmailDialog extends A_CmsMessageDialog {

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "email";

    /** a warning about excluded users with no email. */
    private String m_excludedUsers = "";

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

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_msgInfo.getTo())) {
            setCommitErrors(Collections.singletonList(new CmsIllegalStateException(Messages.get().container(
                Messages.ERR_NO_SELECTED_USER_WITH_EMAIL_0))));
            return;
        }
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
     * Returns a warning if users have been excluded.<p>
     * 
     * @return a warning
     */
    public String getExcludedUsers() {

        return m_excludedUsers;
    }

    /**
     * Sets the warning message if users have been excluded.<p>
     * 
     * @param excludedUsers the warning message
     */
    public void setExcludedUsers(String excludedUsers) {

        m_excludedUsers = excludedUsers;
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#createDialogHtml(java.lang.String)
     */
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        result.append(createWidgetTableStart());
        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        int n = 4;
        getToNames(); // need it to fill the exclude users property 
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getExcludedUsers())) {
            n++;
        }
        if (dialog.equals(PAGES[0])) {
            // create the widgets for the first dialog page
            result.append(dialogBlockStart(key(Messages.GUI_MESSAGE_EDITOR_LABEL_HEADER_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, n - 1));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
            result.append(dialogBlockStart(key(Messages.GUI_MESSAGE_EDITOR_LABEL_CONTENT_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(n, n));
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

        setKeyPrefix(KEY_PREFIX);

        addWidget(new CmsWidgetDialogParameter(m_msgInfo, "from", PAGES[0], new CmsDisplayWidget()));
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getExcludedUsers())) {
            addWidget(new CmsWidgetDialogParameter(this, "excludedUsers", PAGES[0], new CmsDisplayWidget()));
        }
        addWidget(new CmsWidgetDialogParameter(m_msgInfo, "to", PAGES[0], new CmsDisplayWidget()));
        addWidget(new CmsWidgetDialogParameter(m_msgInfo, "cc", PAGES[0], "", new CmsInputWidget(), 0, 1));
        addWidget(new CmsWidgetDialogParameter(m_msgInfo, "subject", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_msgInfo, "msg", PAGES[0], new CmsTextareaWidget(12)));
    }

    /**
     * Returns a semicolon separated list of user names.<p>
     * 
     * @return a semicolon separated list of user names
     */
    protected String getToNames() {

        List excluded = new ArrayList();
        List users = new ArrayList();
        Iterator itIds = idsList().iterator();
        while (itIds.hasNext()) {
            String id = itIds.next().toString();
            CmsSessionInfo session = OpenCms.getSessionManager().getSessionInfo(id);
            if (session != null) {
                String userName = session.getUser().getFullName();
                String emailAddress = session.getUser().getEmail();
                if (!users.contains(userName)) {
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(emailAddress)) {
                        users.add(userName);
                    } else {
                        excluded.add(userName);
                    }
                }
            }
        }
        if (!excluded.isEmpty()) {
            StringBuffer text = new StringBuffer(500);
            text.append(Messages.get().container(Messages.GUI_EXCLUDED_USERS_WARNING_0).key(getLocale()));
            text.append("\n");
            Iterator it = excluded.iterator();
            while (it.hasNext()) {
                text.append("- ");
                text.append(it.next());
                text.append("\n");
            }
            setExcludedUsers(text.toString());
        }
        if (users.isEmpty()) {
            setCommitErrors(Collections.singletonList(new CmsIllegalStateException(Messages.get().container(
                Messages.ERR_NO_SELECTED_USER_WITH_EMAIL_0))));
            return "";
        }
        StringBuffer result = new StringBuffer(256);
        Iterator itUsers = users.iterator();
        while (itUsers.hasNext()) {
            result.append(itUsers.next().toString());
            if (itUsers.hasNext()) {
                result.append("; ");
            }
        }
        return result.toString();
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