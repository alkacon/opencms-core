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

import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.mail.EmailException;

/**
 * The basic class for the content notification feature in OpenCms. Collects all resources that require a notification,
 * creates and sends notifications to their responsible users.<p/>
 *
 */
public class CmsNotificationCandidates {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsNotificationCandidates.class);

    /** the CmsObject. */
    private CmsObject m_cms;

    /** The resources which come into question for notifications of responsible users. */
    private List<CmsExtendedNotificationCause> m_resources;

    /**
     * Collects all resources that will expire in short time, or will become valid, or are not modified since a long time.<p>
     *
     * @param cms the CmsObject
     *
     * @throws CmsException if something goes wrong
     */
    public CmsNotificationCandidates(CmsObject cms)
    throws CmsException {

        m_resources = new ArrayList<CmsExtendedNotificationCause>();
        m_cms = cms;
        m_cms.getRequestContext().setCurrentProject(
            m_cms.readProject(OpenCms.getSystemInfo().getNotificationProject()));
        String folder = "/";
        GregorianCalendar now = new GregorianCalendar(TimeZone.getDefault(), CmsLocaleManager.getDefaultLocale());
        now.setTimeInMillis(System.currentTimeMillis());
        GregorianCalendar inOneWeek = (GregorianCalendar)now.clone();
        inOneWeek.add(Calendar.WEEK_OF_YEAR, 1);
        Iterator<CmsResource> resources;
        CmsResource resource;

        // read all files with the 'notification-interval' property set
        try {
            resources = m_cms.readResourcesWithProperty(
                folder,
                CmsPropertyDefinition.PROPERTY_NOTIFICATION_INTERVAL).iterator();
            while (resources.hasNext()) {
                resource = resources.next();
                int notification_interval = Integer.parseInt(
                    m_cms.readPropertyObject(
                        resource,
                        CmsPropertyDefinition.PROPERTY_NOTIFICATION_INTERVAL,
                        true).getValue());
                GregorianCalendar intervalBefore = new GregorianCalendar(
                    TimeZone.getDefault(),
                    CmsLocaleManager.getDefaultLocale());
                intervalBefore.setTimeInMillis(resource.getDateLastModified());
                intervalBefore.add(Calendar.DAY_OF_YEAR, notification_interval);
                GregorianCalendar intervalAfter = (GregorianCalendar)intervalBefore.clone();
                intervalAfter.add(Calendar.WEEK_OF_YEAR, -1);

                for (int i = 0; (i < 100) && intervalAfter.getTime().before(now.getTime()); i++) {
                    if (intervalBefore.getTime().after(now.getTime())) {
                        m_resources.add(
                            new CmsExtendedNotificationCause(
                                resource,
                                CmsExtendedNotificationCause.RESOURCE_UPDATE_REQUIRED,
                                intervalBefore.getTime()));
                    }
                    intervalBefore.add(Calendar.DAY_OF_YEAR, notification_interval);
                    intervalAfter.add(Calendar.DAY_OF_YEAR, notification_interval);
                }
            }
        } catch (CmsDbEntryNotFoundException e) {
            // no resources with property 'notification-interval', ignore
        }

        // read all files that were not modified longer than the max notification-time
        GregorianCalendar oneYearAgo = (GregorianCalendar)now.clone();
        oneYearAgo.add(Calendar.DAY_OF_YEAR, -OpenCms.getSystemInfo().getNotificationTime());
        // create a resource filter to get the resources with
        CmsResourceFilter filter = CmsResourceFilter.IGNORE_EXPIRATION.addRequireLastModifiedBefore(
            oneYearAgo.getTimeInMillis());
        resources = m_cms.readResources(folder, filter).iterator();
        while (resources.hasNext()) {
            resource = resources.next();
            m_resources.add(
                new CmsExtendedNotificationCause(
                    resource,
                    CmsExtendedNotificationCause.RESOURCE_OUTDATED,
                    new Date(resource.getDateLastModified())));
        }

        // get all resources that will expire within the next week
        CmsResourceFilter resourceFilter = CmsResourceFilter.IGNORE_EXPIRATION.addRequireExpireBefore(
            inOneWeek.getTimeInMillis());
        resourceFilter = resourceFilter.addRequireExpireAfter(now.getTimeInMillis());
        resources = m_cms.readResources(folder, resourceFilter).iterator();
        while (resources.hasNext()) {
            resource = resources.next();
            m_resources.add(
                new CmsExtendedNotificationCause(
                    resource,
                    CmsExtendedNotificationCause.RESOURCE_EXPIRES,
                    new Date(resource.getDateExpired())));
        }

        // get all resources that will release within the next week
        resourceFilter = CmsResourceFilter.IGNORE_EXPIRATION.addRequireReleaseBefore(inOneWeek.getTimeInMillis());
        resourceFilter = resourceFilter.addRequireReleaseAfter(now.getTimeInMillis());
        resources = m_cms.readResources(folder, resourceFilter).iterator();
        while (resources.hasNext()) {
            resource = resources.next();
            m_resources.add(
                new CmsExtendedNotificationCause(
                    resource,
                    CmsExtendedNotificationCause.RESOURCE_RELEASE,
                    new Date(resource.getDateReleased())));
        }
    }

    /**
     * Sends all notifications to the responsible users.<p>
     *
     * @return a string listing all responsibles that a notification was sent to
     *
     * @throws CmsException if something goes wrong
     */
    public String notifyResponsibles() throws CmsException {

        Iterator<CmsContentNotification> notifications = filterConfirmedResources(getContentNotifications()).iterator();
        if (notifications.hasNext()) {
            StringBuffer result = new StringBuffer(
                Messages.get().getBundle().key(Messages.LOG_NOTIFICATIONS_SENT_TO_0));
            result.append(' ');
            while (notifications.hasNext()) {
                CmsContentNotification contentNotification = notifications.next();
                result.append(contentNotification.getResponsible().getName());
                if (notifications.hasNext()) {
                    result.append(", ");
                }
                try {
                    contentNotification.send();
                } catch (EmailException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            return result.toString();
        } else {
            return Messages.get().getBundle().key(Messages.LOG_NO_NOTIFICATIONS_SENT_0);
        }
    }

    /**
     * Returns a collection of CmsContentNotifications, one for each responsible that receives a notification.<p>
     *
     * @return the list of CmsContentNotifications, one for each responsible that receives a notification
     *
     * @throws CmsException if something goes wrong
     */
    protected Collection<CmsContentNotification> getContentNotifications() throws CmsException {

        // get all owners for the resource
        Iterator<CmsExtendedNotificationCause> notificationCandidates = m_resources.iterator();
        Map<CmsUser, CmsContentNotification> result = new HashMap<CmsUser, CmsContentNotification>();
        while (notificationCandidates.hasNext()) {
            CmsExtendedNotificationCause resourceInfo = notificationCandidates.next();
            CmsResource resource = resourceInfo.getResource();
            // skip, if content notification is not enabled for this resource
            String enableNotification = m_cms.readPropertyObject(
                resource,
                CmsPropertyDefinition.PROPERTY_ENABLE_NOTIFICATION,
                true).getValue();
            if (Boolean.valueOf(enableNotification).booleanValue()) {
                try {
                    Iterator<CmsUser> responsibles = m_cms.readResponsibleUsers(resource).iterator();
                    while (responsibles.hasNext()) {
                        CmsUser responsible = responsibles.next();
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(responsible.getEmail())) {
                            // check, if resultset already contains a content notification for the user
                            CmsContentNotification contentNotification = result.get(responsible);

                            // if not add a new content notification
                            if (contentNotification == null) {
                                contentNotification = new CmsContentNotification(responsible, m_cms);
                                result.put(responsible, contentNotification);
                            }
                            List<CmsExtendedNotificationCause> resourcesForResponsible = contentNotification.getNotificationCauses();
                            if (resourcesForResponsible == null) {
                                resourcesForResponsible = new ArrayList<CmsExtendedNotificationCause>();
                                contentNotification.setNotificationCauses(resourcesForResponsible);
                            }
                            resourcesForResponsible.add(resourceInfo);
                        }
                    }
                } catch (CmsException e) {
                    if (LOG.isInfoEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            }
        }
        return result.values();
    }

    /**
     * Updates the resources that were confirmed by the user. That means deletes the resources that need not a
     * notification any more.
     * removes all resources which do not occur in the candidate list.<p>
     *
     * @param contentNotifications the list of {@link CmsContentNotification} objects to remove from the set of confirmed resources
     * @return a new CmsConfirmedResources Object which all the resource removed
     */
    private Collection<CmsContentNotification> filterConfirmedResources(
        Collection<CmsContentNotification> contentNotifications) {

        Iterator<CmsContentNotification> notifications = contentNotifications.iterator();
        while (notifications.hasNext()) {
            CmsContentNotification contentNotification = notifications.next();
            CmsUser responsible = contentNotification.getResponsible();
            // check, if user was already notified
            List<?> confirmedResourcesList = (List<?>)responsible.getAdditionalInfo(
                CmsUserSettings.ADDITIONAL_INFO_CONFIRMED_RESOURCES);
            if (confirmedResourcesList == null) {
                confirmedResourcesList = new ArrayList<Object>();
                responsible.setAdditionalInfo(
                    CmsUserSettings.ADDITIONAL_INFO_CONFIRMED_RESOURCES,
                    new ArrayList<Object>());
            }

            List<CmsExtendedNotificationCause> notificationCandidates = contentNotification.getNotificationCauses();

            List<CmsExtendedNotificationCause> notificationResources = new ArrayList<CmsExtendedNotificationCause>(
                notificationCandidates);
            // remove already confirmed resources
            Iterator<?> i = confirmedResourcesList.iterator();
            while (i.hasNext()) {
                Object o = i.next();
                if (notificationResources.contains(o)) {
                    notificationResources.remove(o);
                }
            }
            // filter confirmed resources
            i = new ArrayList<Object>(confirmedResourcesList).iterator();
            while (i.hasNext()) {
                Object o = i.next();
                if (!notificationCandidates.contains(o)) {
                    confirmedResourcesList.remove(o);
                }
            }

            if (notificationResources.isEmpty()) {
                // Remove notification, if resource list is empty
                contentNotifications.remove(contentNotification);
            } else {
                contentNotification.setNotificationCauses(notificationResources);
            }
            try {
                m_cms.writeUser(responsible);
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return contentNotifications;
    }
}