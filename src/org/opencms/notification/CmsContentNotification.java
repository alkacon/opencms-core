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

package org.opencms.notification;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessages;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplace;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.logging.Log;

/**
 * The E-Mail to be written to responsibles of resources.<p>
 */
public class CmsContentNotification extends A_CmsNotification {

    /** The path to the xml content with the subject, header and footer of the notification e-mail.<p> */
    public static final String NOTIFICATION_CONTENT = "notification/notification";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsContentNotification.class);

    /** The message bundle initialized with the locale of the reciever. */
    private CmsMessages m_messages;

    /**  The resources the responsible will be notified of, a list of CmsNotificationCauses. */
    private List<CmsExtendedNotificationCause> m_notificationCauses;

    /** The receiver of the notification. */
    private CmsUser m_responsible;

    /** Server name and opencms context. */
    private String m_serverAndContext = OpenCms.getSiteManager().getWorkplaceServer()
        + OpenCms.getSystemInfo().getOpenCmsContext();

    /** Uri of the workplace folder. */
    private String m_uriWorkplace = m_serverAndContext + CmsWorkplace.VFS_PATH_WORKPLACE;

    /** Uri of the workplace jsp. */
    private String m_uriWorkplaceJsp = m_serverAndContext + CmsWorkplace.JSP_WORKPLACE_URI;

    /**
     * Creates a new CmsContentNotification.<p>
     *
     * @param responsible the user that will be notified
     * @param cms the cms object to use
     */
    CmsContentNotification(CmsUser responsible, CmsObject cms) {

        super(cms, responsible);
        m_responsible = responsible;
    }

    /**
     * Returns true, if there exists an editor for a specific resource.<p>
     *
     * @param resource the resource to check if there exists an editor
     *
     * @return true if there exists an editor for the resource
     */
    public static boolean existsEditor(CmsResource resource) {

        int plainId;
        try {
            plainId = OpenCms.getResourceManager().getResourceType(
                CmsResourceTypePlain.getStaticTypeName()).getTypeId();
        } catch (CmsLoaderException e) {
            // this should really never happen
            plainId = CmsResourceTypePlain.getStaticTypeId();
        }
        if ((CmsResourceTypeJsp.isJsp(resource))
            || (resource.getTypeId() == plainId)
            || CmsResourceTypeXmlPage.isXmlPage(resource)) {
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
     * Creates the mail to be sent to the responsible user.<p>
     *
     * @return the mail to be sent to the responsible user
     */
    @Override
    protected String generateHtmlMsg() {

        // set the messages
        m_messages = Messages.get().getBundle(getLocale());

        StringBuffer htmlMsg = new StringBuffer();
        htmlMsg.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");
        htmlMsg.append("<tr><td colspan=\"5\"><br/>");

        GregorianCalendar tomorrow = new GregorianCalendar(TimeZone.getDefault(), CmsLocaleManager.getDefaultLocale());
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        List<CmsExtendedNotificationCause> outdatedResources = new ArrayList<CmsExtendedNotificationCause>();
        List<CmsExtendedNotificationCause> resourcesNextDay = new ArrayList<CmsExtendedNotificationCause>();
        List<CmsExtendedNotificationCause> resourcesNextWeek = new ArrayList<CmsExtendedNotificationCause>();

        // split all resources into three lists: the resources that expire, will be released or get outdated
        // within the next 24h, within the next week and the resources unchanged since a long time
        Iterator<CmsExtendedNotificationCause> notificationCauses = m_notificationCauses.iterator();
        while (notificationCauses.hasNext()) {
            CmsExtendedNotificationCause notificationCause = notificationCauses.next();
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
        appendResourceList(htmlMsg, resourcesNextDay, m_messages.key(Messages.GUI_WITHIN_NEXT_DAY_0));
        appendResourceList(htmlMsg, resourcesNextWeek, m_messages.key(Messages.GUI_WITHIN_NEXT_WEEK_0));
        appendResourceList(
            htmlMsg,
            outdatedResources,
            m_messages.key(
                Messages.GUI_FILES_NOT_UPDATED_1,
                String.valueOf(OpenCms.getSystemInfo().getNotificationTime())));

        htmlMsg.append("</td></tr></table>");
        String result = htmlMsg.toString();
        return result;
    }

    /**
     * Returns a list of CmsNotificationResourceInfos of the resources that will occur in the notification.<p>
     *
     * @return a list of CmsNotificationResourceInfos of the resources that will occur in the notification
     */
    protected List<CmsExtendedNotificationCause> getNotificationCauses() {

        return m_notificationCauses;
    }

    /**
     * @see org.opencms.notification.A_CmsNotification#getNotificationContent()
     */
    @Override
    protected String getNotificationContent() {

        return OpenCms.getSystemInfo().getConfigFilePath(m_cms, NOTIFICATION_CONTENT);
    }

    /**
     * Sets the resources.<p>
     *
     * @param resources a list of CmsNotificationResourceInfo's
     */
    protected void setNotificationCauses(List<CmsExtendedNotificationCause> resources) {

        m_notificationCauses = resources;
    }

    /**
     * Appends a link to confirm a resource, so that the responsible will not be notified any more.<p>
     *
     * @param buf the StringBuffer to append the html code to
     * @param notificationCause the information for specific resource
     */
    private void appendConfirmLink(StringBuffer buf, CmsExtendedNotificationCause notificationCause) {

        Map<String, String[]> params = new HashMap<String, String[]>();
        buf.append("<td>");
        try {
            String resourcePath = notificationCause.getResource().getRootPath();
            String siteRoot = OpenCms.getSiteManager().getSiteRoot(resourcePath);
            resourcePath = resourcePath.substring(siteRoot.length());
            buf.append("[<a href=\"");
            StringBuffer wpStartUri = new StringBuffer(m_uriWorkplace);
            wpStartUri.append("commons/confirm_content_notification.jsp?userId=");
            wpStartUri.append(m_responsible.getId());
            wpStartUri.append("&cause=");
            wpStartUri.append(notificationCause.getCause());
            wpStartUri.append("&resource=");
            wpStartUri.append(resourcePath);
            params.put(CmsWorkplace.PARAM_WP_START, new String[] {wpStartUri.toString()});
            params.put(
                CmsWorkplace.PARAM_WP_EXPLORER_RESOURCE,
                new String[] {CmsResource.getParentFolder(resourcePath)});
            params.put(CmsWorkplace.PARAM_WP_SITE, new String[] {siteRoot});
            CmsUUID projectId = getCmsObject().readProject(OpenCms.getSystemInfo().getNotificationProject()).getUuid();
            params.put(CmsWorkplace.PARAM_WP_PROJECT, new String[] {String.valueOf(projectId)});
            buf.append(CmsRequestUtil.appendParameters(m_uriWorkplaceJsp, params, true));
            buf.append("\">");
            buf.append(m_messages.key(Messages.GUI_CONFIRM_0));
            buf.append("</a>]");
        } catch (CmsException e) {
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage(), e);
            }
        }
        buf.append("</td>");
    }

    /**
     * Appends a link to edit the resource to a StringBuffer.<p>
     *
     * @param buf the StringBuffer to append the html code to.
     * @param notificationCause the information for specific resource.
     */
    private void appendEditLink(StringBuffer buf, CmsExtendedNotificationCause notificationCause) {

        buf.append("<td>");
        if (existsEditor(notificationCause.getResource())) {
            try {
                String resourcePath = notificationCause.getResource().getRootPath();
                String siteRoot = OpenCms.getSiteManager().getSiteRoot(resourcePath);
                resourcePath = resourcePath.substring(siteRoot.length());
                Map<String, String[]> params = new HashMap<String, String[]>();
                CmsUUID projectId = getCmsObject().readProject(
                    OpenCms.getSystemInfo().getNotificationProject()).getUuid();
                params.put(CmsWorkplace.PARAM_WP_PROJECT, new String[] {String.valueOf(projectId)});
                params.put(
                    CmsWorkplace.PARAM_WP_EXPLORER_RESOURCE,
                    new String[] {CmsResource.getParentFolder(resourcePath)});
                params.put(CmsWorkplace.PARAM_WP_SITE, new String[] {siteRoot});
                params.put(CmsDialog.PARAM_RESOURCE, new String[] {resourcePath});
                buf.append("[<a href=\"");
                buf.append(CmsRequestUtil.appendParameters(m_uriWorkplace + "editors/editor.jsp", params, false));
                buf.append("\">");
                buf.append(m_messages.key(Messages.GUI_EDIT_0));
                buf.append("</a>]");
            } catch (CmsException e) {
                if (LOG.isInfoEnabled()) {
                    LOG.info(e.getLocalizedMessage(), e);
                }
            }
        }
        buf.append("</td>");
    }

    /**
     * Appends a link to edit the notification settings of a resource to a StringBuffer.<p>
     *
     * @param buf the StringBuffer to append the html code to.
     * @param notificationCause the information for specific resource.
     */
    private void appendModifyLink(StringBuffer buf, CmsExtendedNotificationCause notificationCause) {

        Map<String, String[]> params = new HashMap<String, String[]>();
        buf.append("<td>");
        try {
            buf.append("[<a href=\"");
            String resourcePath = notificationCause.getResource().getRootPath();
            String siteRoot = OpenCms.getSiteManager().getSiteRoot(resourcePath);
            resourcePath = resourcePath.substring(siteRoot.length());
            StringBuffer wpStartUri = new StringBuffer(m_uriWorkplace);
            wpStartUri.append("commons/availability.jsp?resource=");
            wpStartUri.append(resourcePath);
            params.put(
                CmsWorkplace.PARAM_WP_EXPLORER_RESOURCE,
                new String[] {CmsResource.getParentFolder(resourcePath)});
            params.put(CmsWorkplace.PARAM_WP_START, new String[] {wpStartUri.toString()});
            params.put(CmsWorkplace.PARAM_WP_SITE, new String[] {siteRoot});
            CmsUUID projectId = getCmsObject().readProject(OpenCms.getSystemInfo().getNotificationProject()).getUuid();
            params.put(CmsWorkplace.PARAM_WP_PROJECT, new String[] {String.valueOf(projectId)});
            buf.append(CmsRequestUtil.appendParameters(m_uriWorkplaceJsp, params, true));
            buf.append("\">");
            buf.append(m_messages.key(Messages.GUI_MODIFY_0));
            buf.append("</a>]");
        } catch (CmsException e) {
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage(), e);
            }
        }
        buf.append("</td>");
    }

    /**
     * Appends a table showing a set of resources, and the cause of the notification.<p>
     *
     * @param htmlMsg html the StringBuffer to append the html code to
     * @param notificationCauseList the list of notification causes
     * @param header the title of the resource list
     */
    private void appendResourceList(
        StringBuffer htmlMsg,
        List<CmsExtendedNotificationCause> notificationCauseList,
        String header) {

        if (!notificationCauseList.isEmpty()) {
            htmlMsg.append("<tr><td colspan=\"5\"><br/><p style=\"margin-top:20px;margin-bottom:10px;\"><b>");
            htmlMsg.append(header);
            htmlMsg.append(
                "</b></p></td></tr><tr class=\"trow1\"><td><div style=\"padding-top:2px;padding-bottom:2px;\">");
            htmlMsg.append(m_messages.key(Messages.GUI_RESOURCE_0));
            htmlMsg.append("</div></td><td><div style=\"padding-top:2px;padding-bottom:2px;padding-left:10px;\">");
            htmlMsg.append(m_messages.key(Messages.GUI_SITE_0));
            htmlMsg.append("</div></td><td><div style=\"padding-top:2px;padding-bottom:2px;padding-left:10px;\">");
            htmlMsg.append(m_messages.key(Messages.GUI_ISSUE_0));
            htmlMsg.append("</div></td><td colspan=\"2\"/></tr>");
            Iterator<CmsExtendedNotificationCause> notificationCauses = notificationCauseList.iterator();
            for (int i = 0; notificationCauses.hasNext(); i++) {
                CmsExtendedNotificationCause notificationCause = notificationCauses.next();
                htmlMsg.append(buildNotificationListItem(notificationCause, (i % 2) + 2));
            }
        }
    }

    /**
     * Returns a string representation of this resource info.<p>
     *
     * @param notificationCause the notification cause
     * @param row the row number
     *
     * @return a string representation of this resource info
     */
    private String buildNotificationListItem(CmsExtendedNotificationCause notificationCause, int row) {

        StringBuffer result = new StringBuffer("<tr class=\"trow");
        result.append(row);
        result.append("\"><td width=\"100%\">");
        String resourcePath = notificationCause.getResource().getRootPath();
        String siteRoot = OpenCms.getSiteManager().getSiteRoot(resourcePath);
        resourcePath = resourcePath.substring(siteRoot.length());
        // append link, if page is available
        if ((notificationCause.getResource().getDateReleased() < System.currentTimeMillis())
            && (notificationCause.getResource().getDateExpired() > System.currentTimeMillis())) {

            Map<String, String[]> params = new HashMap<String, String[]>();
            params.put(CmsWorkplace.PARAM_WP_SITE, new String[] {siteRoot});
            params.put(CmsDialog.PARAM_RESOURCE, new String[] {resourcePath});
            result.append("<a href=\"");
            result.append(
                CmsRequestUtil.appendParameters(m_uriWorkplace + "commons/displayresource.jsp", params, false));
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
            result.append(m_messages.key(Messages.GUI_EXPIRES_AT_1, new Object[] {notificationCause.getDate()}));
            result.append("</div></td>");
            appendConfirmLink(result, notificationCause);
            appendModifyLink(result, notificationCause);
        } else if (notificationCause.getCause() == CmsExtendedNotificationCause.RESOURCE_RELEASE) {
            result.append(m_messages.key(Messages.GUI_RELEASE_AT_1, new Object[] {notificationCause.getDate()}));
            result.append("</div></td>");
            appendConfirmLink(result, notificationCause);
            appendModifyLink(result, notificationCause);
        } else if (notificationCause.getCause() == CmsExtendedNotificationCause.RESOURCE_UPDATE_REQUIRED) {
            result.append(m_messages.key(Messages.GUI_UPDATE_REQUIRED_1, new Object[] {notificationCause.getDate()}));
            result.append("</div></td>");
            appendConfirmLink(result, notificationCause);
            appendEditLink(result, notificationCause);
        } else {
            result.append(
                m_messages.key(
                    Messages.GUI_UNCHANGED_SINCE_1,
                    new Object[] {Integer.valueOf(CmsDateUtil.getDaysPassedSince(notificationCause.getDate()))}));
            result.append("</div></td>");
            appendConfirmLink(result, notificationCause);
            appendEditLink(result, notificationCause);
        }

        result.append("</tr>");

        return result.toString();
    }
}