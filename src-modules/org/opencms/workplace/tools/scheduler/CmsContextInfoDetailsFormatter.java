/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/scheduler/CmsContextInfoDetailsFormatter.java,v $
 * Date   : $Date: 2005/06/23 14:27:27 $
 * Version: $Revision: 1.8 $
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

package org.opencms.workplace.tools.scheduler;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsContextInfo;
import org.opencms.workplace.list.I_CmsListFormatter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * This list item detail formatter creates a two column table to represent a context info object.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.8 $
 * 
 * @since 6.0.0
 */
public class CmsContextInfoDetailsFormatter implements I_CmsListFormatter {

    /** Cache for localized messages. */
    private Map m_cache = new HashMap();
    /** Encoding message header. */
    private CmsMessageContainer m_encodingMessage;
    /** Locale message header. */
    private CmsMessageContainer m_localeMessage;
    /** Project message header. */
    private CmsMessageContainer m_projectMessage;
    /** Remote message header. */
    private CmsMessageContainer m_remoteAddrMessage;
    /** Request message header. */
    private CmsMessageContainer m_requestedURIMessage;
    /** RootSite message header. */
    private CmsMessageContainer m_rootSiteMessage;

    /** User message header. */
    private CmsMessageContainer m_userMessage;

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

        Map cache = (Map)m_cache.get(locale);
        if (cache == null) {
            cache = new HashMap();
            cache.put(m_userMessage, m_userMessage.key(locale));
            cache.put(m_projectMessage, m_projectMessage.key(locale));
            cache.put(m_localeMessage, m_localeMessage.key(locale));
            cache.put(m_rootSiteMessage, m_rootSiteMessage.key(locale));
            cache.put(m_requestedURIMessage, m_requestedURIMessage.key(locale));
            cache.put(m_remoteAddrMessage, m_remoteAddrMessage.key(locale));
            cache.put(m_encodingMessage, m_encodingMessage.key(locale));
            m_cache.put(locale, cache);
        }
        String userMessage = (String)cache.get(m_userMessage);
        String projectMessage = (String)cache.get(m_projectMessage);
        String localeMessage = (String)cache.get(m_localeMessage);
        String rootSiteMessage = (String)cache.get(m_rootSiteMessage);
        String requestedURIMessage = (String)cache.get(m_requestedURIMessage);
        String remoteAddrMessage = (String)cache.get(m_remoteAddrMessage);
        String encodingMessage = (String)cache.get(m_encodingMessage);
        CmsContextInfo info = (CmsContextInfo)data;
        StringBuffer html = new StringBuffer(512);
        html.append("<table border='0' cellspacing='0' cellpadding='0'>\n");
        html.append("\t<tr>\n");
        html.append("\t\t<td width='150' align='right' class='listdetailhead'>\n");
        html.append("\t\t\t");
        html.append(userMessage);
        html.append("&nbsp;:&nbsp;\n");
        html.append("\t\t</td>\n");
        html.append("\t\t<td class='listdetailitem'>\n");
        html.append("\t\t\t");
        html.append(info.getUserName());
        html.append("\n");
        html.append("\t\t</td>\n");
        html.append("\t</tr>\n");
        html.append("\t<tr>\n");
        html.append("\t\t<td width='150' align='right' class='listdetailhead'>\n");
        html.append("\t\t\t");
        html.append(projectMessage);
        html.append("&nbsp;:&nbsp;\n");
        html.append("\t\t</td>\n");
        html.append("\t\t<td class='listdetailitem'>\n");
        html.append("\t\t\t");
        html.append(info.getProjectName());
        html.append("\n");
        html.append("\t\t</td>\n");
        html.append("\t</tr>\n");
        html.append("\t<tr>\n");
        html.append("\t\t<td width='150' align='right' class='listdetailhead'>\n");
        html.append("\t\t\t");
        html.append(localeMessage);
        html.append("&nbsp;:&nbsp;\n");
        html.append("\t\t</td>\n");
        html.append("\t\t<td class='listdetailitem'>\n");
        html.append("\t\t\t");
        html.append(info.getLocaleName());
        html.append("\n");
        html.append("\t\t</td>\n");
        html.append("\t</tr>\n");
        html.append("\t<tr>\n");
        html.append("\t\t<td width='150' align='right' class='listdetailhead'>\n");
        html.append("\t\t\t");
        html.append(rootSiteMessage);
        html.append("&nbsp;:&nbsp;\n");
        html.append("\t\t</td>\n");
        html.append("\t\t<td class='listdetailitem'>\n");
        html.append("\t\t\t");
        html.append(info.getSiteRoot());
        html.append("\n");
        html.append("\t\t</td>\n");
        html.append("\t</tr>\n");
        html.append("\t<tr>\n");
        html.append("\t\t<td width='150' align='right' class='listdetailhead'>\n");
        html.append("\t\t\t");
        html.append(requestedURIMessage);
        html.append("&nbsp;:&nbsp;\n");
        html.append("\t\t</td>\n");
        html.append("\t\t<td class='listdetailitem'>\n");
        html.append("\t\t\t");
        html.append(info.getRequestedUri());
        html.append("\n");
        html.append("\t\t</td>\n");
        html.append("\t</tr>\n");
        html.append("\t<tr>\n");
        html.append("\t\t<td width='150' align='right' class='listdetailhead'>\n");
        html.append("\t\t\t");
        html.append(remoteAddrMessage);
        html.append("&nbsp;:&nbsp;\n");
        html.append("\t\t</td>\n");
        html.append("\t\t<td class='listdetailitem'>\n");
        html.append("\t\t\t");
        html.append(info.getRemoteAddr());
        html.append("\n");
        html.append("\t\t</td>\n");
        html.append("\t</tr>\n");
        html.append("\t<tr>\n");
        html.append("\t\t<td width='150' align='right' class='listdetailhead'>\n");
        html.append("\t\t\t");
        html.append(encodingMessage);
        html.append("&nbsp;:&nbsp;\n");
        html.append("\t\t</td>\n");
        html.append("\t\t<td class='listdetailitem'>\n");
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