/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/jobs/Attic/CmsContextInfoDetailsFormatter.java,v $
 * Date   : $Date: 2005/05/10 14:15:24 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.tools.jobs;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsContextInfo;
import org.opencms.workplace.list.I_CmsListFormatter;

import java.util.Locale;

/**
 * This list item detail formatter creates a two column table to represent a context info object.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com)
 * @version $Revision: 1.1 $
 * @since 5.7.3
 */
public class CmsContextInfoDetailsFormatter implements I_CmsListFormatter {

    /** User message header. */
    private CmsMessageContainer m_userMessage;
    /** Project message header. */
    private CmsMessageContainer m_projectMessage;
    /** RootSite message header. */
    private CmsMessageContainer m_rootSiteMessage;
    /** Locale message header. */
    private CmsMessageContainer m_localeMessage;
    /** Remote message header. */
    private CmsMessageContainer m_remoteAddrMessage;
    /** Encoding message header. */
    private CmsMessageContainer m_encodingMessage;
    /** Request message header. */
    private CmsMessageContainer m_requestedURIMessage;

    /**
     * Default constructor.<p>
     */
    public CmsContextInfoDetailsFormatter() {
        //noop
    }

    /**
     * @see org.opencms.workplace.list.I_CmsListFormatter#format(java.lang.Object, java.util.Locale)
     */
    public String format(Object data, Locale locale) {

        String userMessage = m_userMessage.key(locale);
        String projectMessage = m_projectMessage.key(locale);
        String localeMessage = m_localeMessage.key(locale);
        String rootSiteMessage = m_rootSiteMessage.key(locale);
        String requestedURIMessage = m_requestedURIMessage.key(locale);
        String remoteAddrMessage = m_remoteAddrMessage.key(locale);
        String encodingMessage = m_encodingMessage.key(locale);
        CmsContextInfo info = (CmsContextInfo)data;
        StringBuffer html = new StringBuffer(512);
        html.append("<table border='0' cellspacing='0' cellpadding='0'>\n");
        html.append("\t<tr>\n");
        html.append("\t\t<td width='150' align='right' style='vertical-align:top;'>\n");
        html.append("\t\t\t<strong>");
        html.append(userMessage);
        html.append("</strong>&nbsp;:&nbsp;\n");
        html.append("\t\t</td>\n");
        html.append("\t\t<td style='padding-left: 10px'>\n");
        html.append("\t\t\t");
        html.append(info.getUserName());
        html.append("\n");
        html.append("\t\t</td>\n");
        html.append("\t</tr>\n");
        html.append("\t<tr>\n");
        html.append("\t\t<td width='150' align='right' style='vertical-align:top;'>\n");
        html.append("\t\t\t<strong>");
        html.append(projectMessage);
        html.append("</strong>&nbsp;:&nbsp;\n");
        html.append("\t\t</td>\n");
        html.append("\t\t<td style='padding-left: 10px'>\n");
        html.append("\t\t\t");
        html.append(info.getProjectName());
        html.append("\n");
        html.append("\t\t</td>\n");
        html.append("\t</tr>\n");
        html.append("\t<tr>\n");
        html.append("\t\t<td width='150' align='right' style='vertical-align:top;'>\n");
        html.append("\t\t\t<strong>");
        html.append(localeMessage);
        html.append("</strong>&nbsp;:&nbsp;\n");
        html.append("\t\t</td>\n");
        html.append("\t\t<td style='padding-left: 10px'>\n");
        html.append("\t\t\t");
        html.append(info.getLocaleName());
        html.append("\n");
        html.append("\t\t</td>\n");
        html.append("\t</tr>\n");
        html.append("\t<tr>\n");
        html.append("\t\t<td width='150' align='right' style='vertical-align:top;'>\n");
        html.append("\t\t\t<strong>");
        html.append(rootSiteMessage);
        html.append("</strong>&nbsp;:&nbsp;\n");
        html.append("\t\t</td>\n");
        html.append("\t\t<td style='padding-left: 10px'>\n");
        html.append("\t\t\t");
        html.append(info.getSiteRoot());
        html.append("\n");
        html.append("\t\t</td>\n");
        html.append("\t</tr>\n");
        html.append("\t<tr>\n");
        html.append("\t\t<td width='150' align='right' style='vertical-align:top;'>\n");
        html.append("\t\t\t<strong>");
        html.append(requestedURIMessage);
        html.append("</strong>&nbsp;:&nbsp;\n");
        html.append("\t\t</td>\n");
        html.append("\t\t<td style='padding-left: 10px'>\n");
        html.append("\t\t\t");
        html.append(info.getRequestedUri());
        html.append("\n");
        html.append("\t\t</td>\n");
        html.append("\t</tr>\n");
        html.append("\t<tr>\n");
        html.append("\t\t<td width='150' align='right' style='vertical-align:top;'>\n");
        html.append("\t\t\t<strong>");
        html.append(remoteAddrMessage);
        html.append("</strong>&nbsp;:&nbsp;\n");
        html.append("\t\t</td>\n");
        html.append("\t\t<td style='padding-left: 10px'>\n");
        html.append("\t\t\t");
        html.append(info.getRemoteAddr());
        html.append("\n");
        html.append("\t\t</td>\n");
        html.append("\t</tr>\n");
        html.append("\t<tr>\n");
        html.append("\t\t<td width='150' align='right' style='vertical-align:top;'>\n");
        html.append("\t\t\t<strong>");
        html.append(encodingMessage);
        html.append("</strong>&nbsp;:&nbsp;\n");
        html.append("\t\t</td>\n");
        html.append("\t\t<td style='padding-left: 10px'>\n");
        html.append("\t\t\t");
        html.append(info.getEncoding());
        html.append("\n");
        html.append("\t\t</td>\n");
        html.append("\t</tr>\n");
        html.append("</table>\n");
        return html.toString();
    }
    /**
     * Sets the encoding Message.<p>
     *
     * @param encodingMessage the encoding Message to set
     */
    public void setEncodingMessage(CmsMessageContainer encodingMessage) {

        m_encodingMessage = encodingMessage;
    }
    /**
     * Sets the locale Message.<p>
     *
     * @param localeMessage the locale Message to set
     */
    public void setLocaleMessage(CmsMessageContainer localeMessage) {

        m_localeMessage = localeMessage;
    }
    /**
     * Sets the project Message.<p>
     *
     * @param projectMessage the project Message to set
     */
    public void setProjectMessage(CmsMessageContainer projectMessage) {

        m_projectMessage = projectMessage;
    }
    /**
     * Sets the remote Address Message.<p>
     *
     * @param remoteAddrMessage the remote Address Message to set
     */
    public void setRemoteAddrMessage(CmsMessageContainer remoteAddrMessage) {

        m_remoteAddrMessage = remoteAddrMessage;
    }
    /**
     * Sets the requested URI Message.<p>
     *
     * @param requestedURIMessage the requested URI Message to set
     */
    public void setRequestedURIMessage(CmsMessageContainer requestedURIMessage) {

        m_requestedURIMessage = requestedURIMessage;
    }
    /**
     * Sets the rootSiteMessage.<p>
     *
     * @param rootSiteMessage the rootSiteMessage to set
     */
    public void setRootSiteMessage(CmsMessageContainer rootSiteMessage) {

        m_rootSiteMessage = rootSiteMessage;
    }
    /**
     * Sets the userMessage.<p>
     *
     * @param userMessage the userMessage to set
     */
    public void setUserMessage(CmsMessageContainer userMessage) {

        m_userMessage = userMessage;
    }
}