/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/notification/CmsContentNotification.java,v $
 * Date   : $Date: 2005/09/16 08:51:27 $
 * Version: $Revision: 1.1.2.1 $
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

package org.opencms.notification;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.mail.CmsHtmlMail;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSiteManager;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsRequestUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsFrameset;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.mail.MessagingException;

import org.apache.commons.logging.Log;

/**
 * The E-Mail to be written to responsibles of resources.<p>
 * 
 * @author Jan Baudisch
 */
public class CmsContentNotification extends CmsHtmlMail {

    /** The locale of the reiceiver of the content notification.<p> */
    private Locale m_locale;

    /** The xml-content to read subject, header and footer of the notification.<p> */
    private CmsXmlContent m_mailContent;

    /** 
     * The resources the responsible will be notified of, a list of CmsNotificationCauses.<p>
     */
    private List m_notificationCauses;

    /**
     * The receiver of the notification.<p>
     */
    private CmsUser m_responsible;

    /** The CmsObject. */
    private CmsObject m_cms;

    /** The path to the xml content with the subject, header and footer of the notification e-mail.<p> */
    public static final String NOTIFICATION_CONTENT = "/system/workplace/admin/notification/notification";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsContentNotification.class);

    /** Server name and opencms context. */
    private String m_serverAndContext = OpenCms.getSiteManager().getWorkplaceServer()
        + OpenCms.getSystemInfo().getOpenCmsContext();

    /** Uri of the workplace jsp. */
    private String m_uriWorkplaceJsp = m_serverAndContext + CmsFrameset.JSP_WORKPLACE_URI;

    /** Uri of the workplace folder. */
    private String m_uriWorkplace = m_serverAndContext + CmsWorkplace.VFS_PATH_WORKPLACE;

    /**
     * Creates a new CmsContentNotification.<p>
     * 
     * @param responsible the user that will be notified
     * @param cms the cms object to use
     */
    CmsContentNotification(CmsUser responsible, CmsObject cms) {

        m_responsible = responsible;
        m_cms = cms;

    }

    /**
     * Returns a string representation of this resource info.<p>
     * 
     * @return a string representation of this resource info
     */
    private String buildNotificationListItem(CmsExtendedNotificationCause notificationCause, int row) {

        StringBuffer result = new StringBuffer("<tr class=\"trow");
        result.append(row);
        result.append("\"><td width=\"100%\">");
        String resourcePath = notificationCause.getResource().getRootPath();
        String siteRoot = CmsSiteManager.getSiteRoot(resourcePath);
        resourcePath = resourcePath.substring(siteRoot.length());
        // append link, if page is available
        if (notificationCause.getResource().getDateReleased() < System.currentTimeMillis()
            && notificationCause.getResource().getDateExpired() > System.currentTimeMillis()) {

            Map params = new HashMap();
            params.put(CmsWorkplace.PARAM_WP_SITE, siteRoot);
            params.put(CmsDialog.PARAM_RESOURCE, resourcePath);
            result.append("<a href=\"");
            result.append(CmsRequestUtil.appendParameters(m_uriWorkplace + "commons/displayresource.jsp", params, false));
            result.append("\">");
            result.append(resourcePath);
            result.append("</a>");
        } else {
            result.append(resourcePath);
        }
        result.append("</td><td><div style=\"white-space:nowrap;padding-left:10px;padding-right:10px;\">");
        result.append(siteRoot);
        result.append("</td><td><div style=\"white-space:nowrap;padding-left:10px;padding-right:10px;\">");
        if (notificationCause.getCause() == CmsExtendedNotificationCause.RESOURCE_EXPIRES) {
            result.append(Messages.get().key(
                m_locale,
                Messages.GUI_EXPIRES_AT_1,
                new Object[] {notificationCause.getDate()}));
            result.append("</div></td>");
            appendConfirmLink(result, notificationCause);
            appendModifyLink(result, notificationCause);
        } else if (notificationCause.getCause() == CmsExtendedNotificationCause.RESOURCE_RELEASE) {
            result.append(Messages.get().key(
                m_locale,
                Messages.GUI_RELEASE_AT_1,
                new Object[] {notificationCause.getDate()}));
            result.append("</div></td>");
            appendConfirmLink(result, notificationCause);
            appendModifyLink(result, notificationCause);
        } else if (notificationCause.getCause() == CmsExtendedNotificationCause.RESOURCE_UPDATE_REQUIRED) {
            result.append(Messages.get().key(
                m_locale,
                Messages.GUI_UPDATE_REQUIRED_1,
                new Object[] {notificationCause.getDate()}));
            result.append("</div></td>");
            appendConfirmLink(result, notificationCause);
            appendEditLink(result, notificationCause);
        } else {
            result.append(Messages.get().key(
                m_locale,
                Messages.GUI_UNCHANGED_SINCE_1,
                new Object[] {new Integer(CmsDateUtil.getDaysPassedSince(notificationCause.getDate()))}));
            result.append("</div></td>");
            appendConfirmLink(result, notificationCause);
            appendEditLink(result, notificationCause);
        }

        result.append("</tr>");

        return result.toString();
    }

    /**
     * Returns true, if there exists an editor for a specific resource.<p>
     * 
     * @param resource the resource to check if there exists an editor
     * 
     * @return true if there exists an editor for the resource
     */
    public static boolean existsEditor(CmsResource resource) {

        if ((resource.getTypeId() == CmsResourceTypeJsp.getStaticTypeId())
            || (resource.getTypeId() == CmsResourceTypePlain.getStaticTypeId())
            || (resource.getTypeId() == CmsResourceTypeXmlPage.getStaticTypeId())) {
            return true;
        }
        return false;
    }

    /**
     * Returns the responsible.<p>
     *
     * @return the responsible
     */
    public CmsUser getResponsible() {

        return m_responsible;
    }

    /**
     * 
     * @see org.apache.commons.mail.Email#send()
     */
    public void send() throws MessagingException {

        try {
            m_mailContent = CmsXmlContentFactory.unmarshal(m_cms, m_cms.readFile(
                NOTIFICATION_CONTENT,
                CmsResourceFilter.ALL));
            List locales = m_mailContent.getLocales();
            Locale userLocale = new CmsUserSettings(m_responsible).getLocale();
            if (locales.contains(userLocale)) {
                // mail is localized in the user locale, use that
                m_locale = userLocale;
            } else if (locales.contains(OpenCms.getWorkplaceManager().getDefaultLocale())) {
                // mail is localized in the system default locale, use that
                m_locale = OpenCms.getWorkplaceManager().getDefaultLocale();
            } else {
                // use any localization
                m_locale = (Locale)locales.get(0);
            }

            addTo(m_responsible.getEmail(), m_responsible.getFirstname() + ' ' + m_responsible.getLastname());

            setSubject(m_mailContent.getStringValue(m_cms, "Subject", m_locale));
            setHtmlMsg(generateHtmlMsg());
            super.send();
        } catch (CmsException e) {
            LOG.error(e);
        }
    }

    /**
     * Creates the mail to be sent to the responsible user.<p>
     * 
     * @return the mail to be sent to the responsible user
     * @throws CmsException if something goes wrong
     */
    protected String generateHtmlMsg() throws CmsException {

        StringBuffer htmlMsg = new StringBuffer("<html><head><style type=\"text/css\">");
        htmlMsg.append("<!-- body { font-family: Verdana, Arial, Helvetica, sans-serif; background-color:#ffefdb; }");
        htmlMsg.append("a {color:#b22222;} table { white-space: nowrap; font-size: x-small; } tr.trow1 { background-color: #cdc0b0; } ");
        htmlMsg.append("tr.trow2 { background-color: #eedfcc; } tr.trow3 { background-color: #ffefdb; } a { text-decoration:none }--></style>");
        htmlMsg.append("</head><body><span style='font-size:8.0pt;'> <table border=\"0\" cellpadding=\"0\" ");
        htmlMsg.append("cellspacing=\"0\" width=\"100%\"><tr><td colspan=\"5\"><br/>");
        m_mailContent = CmsXmlContentFactory.unmarshal(m_cms, m_cms.readFile(NOTIFICATION_CONTENT));
        CmsMacroResolver macroResolver = new CmsMacroResolver();
        macroResolver.addMacro("firstname", m_responsible.getFirstname());
        macroResolver.addMacro("lastname", m_responsible.getLastname());
        htmlMsg.append(CmsMacroResolver.resolveMacros(
            m_mailContent.getStringValue(m_cms, "Header", m_locale),
            macroResolver));
        htmlMsg.append("<br/></td>");

        GregorianCalendar tomorrow = new GregorianCalendar(TimeZone.getDefault(), CmsLocaleManager.getDefaultLocale());
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        List outdatedResources = new ArrayList();
        List resourcesNextDay = new ArrayList();
        List resourcesNextWeek = new ArrayList();

        // split all resources into three lists: the resources that expire, will be released or get outdated
        // within the next 24h, within the next week and the resources unchanged since a long time
        Iterator notificationCauses = m_notificationCauses.iterator();
        while (notificationCauses.hasNext()) {
            CmsExtendedNotificationCause notificationCause = (CmsExtendedNotificationCause)notificationCauses.next();
            if (notificationCause.getCause() == CmsExtendedNotificationCause.RESOURCE_OUTDATED) {
                outdatedResources.add(notificationCause);
            } else if (notificationCause.getDate().before(tomorrow.getTime())) {
                resourcesNextDay.add(notificationCause);
            } else {
                resourcesNextWeek.add(notificationCause);
            }
        }
        Collections.sort(resourcesNextDay);
        Collections.sort(resourcesNextWeek);
        Collections.sort(outdatedResources);
        appendResourceList(htmlMsg, resourcesNextDay, Messages.get().key(m_locale, Messages.GUI_WITHIN_NEXT_DAY_0));
        appendResourceList(htmlMsg, resourcesNextWeek, Messages.get().key(m_locale, Messages.GUI_WITHIN_NEXT_WEEK_0));
        appendResourceList(htmlMsg, outdatedResources, Messages.get().key(
            m_locale,
            Messages.GUI_FILES_NOT_UPDATED_1,
            new Object[] {String.valueOf(OpenCms.getSystemInfo().getNotificationTime())}));
        htmlMsg.append("<tr><td colspan=\"5\"><br/>");
        htmlMsg.append(m_mailContent.getStringValue(m_cms, "Footer", m_locale));
        htmlMsg.append("</td></tr></table></span></body></html>");
        String result = htmlMsg.toString();
        return result;
    }

    /**
     * Returns a list of CmsNotificationResourceInfos of the resources that will occur in the notification.<p>
     * 
     * @return a list of CmsNotificationResourceInfos of the resources that will occur in the notification
     */
    protected List getNotificationCauses() {

        return m_notificationCauses;
    }

    /**
     * Sets the resources.<p>
     * 
     * @param resources a list of CmsNotificationResourceInfo's
     */
    protected void setNotificationCauses(List resources) {

        m_notificationCauses = resources;
    }

    /**
     * Appends a table showing a set of resources, and the cause of the notification.<p>
     * 
     * @param htmlMsg html the StringBuffer to append the html code to
     * @param notificationCauseList the list of notification causes
     * @param header the title of the resource list 
     */
    private void appendResourceList(StringBuffer htmlMsg, List notificationCauseList, String header) {

        if (!notificationCauseList.isEmpty()) {
            htmlMsg.append("<tr><td colspan=\"5\"><br/><p style=\"margin-top:20px;margin-bottom:10px;\"><b>");
            htmlMsg.append(header);
            htmlMsg.append("</b></p></td></tr><tr class=\"trow1\"><td><div style=\"padding-top:2px;padding-bottom:2px;\">");
            htmlMsg.append(Messages.get().key(m_locale, Messages.GUI_RESOURCE_0));
            htmlMsg.append("</div></td><td><div style=\"padding-top:2px;padding-bottom:2px;padding-left:10px;\">");
            htmlMsg.append(Messages.get().key(m_locale, Messages.GUI_SITE_0));
            htmlMsg.append("</div></td><td><div style=\"padding-top:2px;padding-bottom:2px;padding-left:10px;\">");
            htmlMsg.append(Messages.get().key(m_locale, Messages.GUI_ISSUE_0));
            htmlMsg.append("</div></td><td colspan=\"2\"/></tr>");
            Iterator notificationCauses = notificationCauseList.iterator();
            for (int i = 0; notificationCauses.hasNext(); i++) {
                CmsExtendedNotificationCause notificationCause = (CmsExtendedNotificationCause)notificationCauses.next();
                htmlMsg.append(buildNotificationListItem(notificationCause, (i % 2) + 2));
            }
        }
    }

    /** 
     * Appends a link to confirm a resource, so that the responsible will not be notified any more.<p>
     * 
     * @param html the StringBuffer to append the html code to
     * @param notificationCause the information for specific resource
     */
    private void appendConfirmLink(StringBuffer html, CmsExtendedNotificationCause notificationCause) {

        Map params = new HashMap();
        html.append("<td>");
        try {
            String resourcePath = notificationCause.getResource().getRootPath();
            String siteRoot = CmsSiteManager.getSiteRoot(resourcePath);
            resourcePath = resourcePath.substring(siteRoot.length());
            html.append("[<a href=\"");
            StringBuffer wpStartUri = new StringBuffer(m_uriWorkplace);
            wpStartUri.append("commons/confirm_content_notification.jsp?userId=");
            wpStartUri.append(m_responsible.getId());
            wpStartUri.append("&cause=");
            wpStartUri.append(notificationCause.getCause());
            wpStartUri.append("&resource=");
            wpStartUri.append(resourcePath);
            params.put(CmsFrameset.PARAM_WP_START, wpStartUri.toString());
            params.put(CmsWorkplace.PARAM_WP_EXPLORER_RESOURCE, CmsResource.getParentFolder(resourcePath));
            params.put(CmsWorkplace.PARAM_WP_SITE, siteRoot);
            int projectId = m_cms.readProject(OpenCms.getSystemInfo().getNotificationProject()).getId();
            params.put(CmsWorkplace.PARAM_WP_PROJECT, String.valueOf(projectId));
            html.append(CmsRequestUtil.appendParameters(m_uriWorkplaceJsp, params, true));
            html.append("\">");
            html.append(Messages.get().key(m_locale, Messages.GUI_CONFIRM_0));
            html.append("</a>]");
        } catch (CmsException e) {
            if (LOG.isInfoEnabled()) {
                LOG.info(e);
            }
        }
        html.append("</td>");
    }

    /** 
     * Appends a link to edit the notification settings of a resource to a StringBuffer.<p>
     * 
     * @param html the StringBuffer to append the html code to.
     * @param notificationCause the information for specific resource.
     */
    private void appendModifyLink(StringBuffer html, CmsExtendedNotificationCause notificationCause) {

        Map params = new HashMap();
        html.append("<td>");
        try {
            html.append("[<a href=\"");
            String resourcePath = notificationCause.getResource().getRootPath();
            String siteRoot = CmsSiteManager.getSiteRoot(resourcePath);
            resourcePath = resourcePath.substring(siteRoot.length());
            StringBuffer wpStartUri = new StringBuffer(m_uriWorkplace);
            wpStartUri.append("commons/availability.jsp?resource=");
            wpStartUri.append(resourcePath);
            params.put(CmsWorkplace.PARAM_WP_EXPLORER_RESOURCE, CmsResource.getParentFolder(resourcePath));
            params.put(CmsFrameset.PARAM_WP_START, wpStartUri.toString());
            params.put(CmsWorkplace.PARAM_WP_SITE, siteRoot);
            int projectId = m_cms.readProject(OpenCms.getSystemInfo().getNotificationProject()).getId();
            params.put(CmsWorkplace.PARAM_WP_PROJECT, String.valueOf(projectId));
            html.append(CmsRequestUtil.appendParameters(m_uriWorkplaceJsp, params, true));
            html.append("\">");
            html.append(Messages.get().key(m_locale, Messages.GUI_MODIFY_0));
            html.append("</a>]");
        } catch (CmsException e) {
            if (LOG.isInfoEnabled()) {
                LOG.info(e);
            }
        }
        html.append("</td>");
    }

    /** 
     * Appends a link to edit the resource to a StringBuffer.<p>
     * 
     * @param html the StringBuffer to append the html code to.
     * @param notificationCause the information for specific resource.
     */
    private void appendEditLink(StringBuffer html, CmsExtendedNotificationCause notificationCause) {

        html.append("<td>");
        if (existsEditor(notificationCause.getResource())) {
            try {
                String resourcePath = notificationCause.getResource().getRootPath();
                String siteRoot = CmsSiteManager.getSiteRoot(resourcePath);
                resourcePath = resourcePath.substring(siteRoot.length());
                Map params = new HashMap();
                int projectId = m_cms.readProject(OpenCms.getSystemInfo().getNotificationProject()).getId();
                params.put(CmsWorkplace.PARAM_WP_PROJECT, String.valueOf(projectId));
                params.put(CmsWorkplace.PARAM_WP_EXPLORER_RESOURCE, CmsResource.getParentFolder(resourcePath));
                params.put(CmsWorkplace.PARAM_WP_SITE, siteRoot);
                params.put(CmsDialog.PARAM_RESOURCE, resourcePath);
                html.append("[<a href=\"");
                html.append(CmsRequestUtil.appendParameters(m_uriWorkplace + "editors/editor.jsp", params, false));
                html.append("\">");
                html.append(Messages.get().key(m_locale, Messages.GUI_EDIT_0));
                html.append("</a>]");
            } catch (CmsException e) {
                if (LOG.isInfoEnabled()) {
                    LOG.info(e);
                }
            }
        }
        html.append("</td>");
    }
}
