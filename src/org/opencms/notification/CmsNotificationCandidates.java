/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/notification/CmsNotificationCandidates.java,v $
 * Date   : $Date: 2005/10/19 09:45:12 $
 * Version: $Revision: 1.1.2.2 $
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

import javax.mail.MessagingException;

import org.apache.commons.logging.Log;

/**
 * The basic class for the content notification feature in OpenCms. Collects all resources that require a notification,
 * creates and sends notifications to their responsible users.<p/>
 * 
 * @author Jan Baudisch
 * 
 */
public class CmsNotificationCandidates {

    /** The resources which come into question for notifications of responsible users. */
    private List m_resources;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsNotificationCandidates.class);

    /** the CmsObject. */
    private CmsObject m_cms;

    /**
     * Collects all resources that will expire in short time, or will become valid, or are not modified since a long time.<p>
     * 
     * @param cms the CmsObject
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsNotificationCandidates(CmsObject cms)
    throws CmsException {

        m_resources = new ArrayList();
        m_cms = cms;
        m_cms.getRequestContext().setCurrentProject(m_cms.readProject(OpenCms.getSystemInfo().getNotificationProject()));
        String folder = "/";
        GregorianCalendar now = new GregorianCalendar(TimeZone.getDefault(), CmsLocaleManager.getDefaultLocale());
        now.setTimeInMillis(System.currentTimeMillis());
        GregorianCalendar inOneWeek = (GregorianCalendar)now.clone();
        inOneWeek.add(Calendar.WEEK_OF_YEAR, 1);
        Iterator resources;
        CmsResource resource;

        // read all files with the 'notification-interval' property set
        try {
            resources = m_cms.readResourcesWithProperty(folder, CmsPropertyDefinition.PROPERTY_NOTIFICATION_INTERVAL).iterator();
            while (resources.hasNext()) {
                resource = (CmsResource)resources.next();
                int notification_interval = Integer.parseInt(m_cms.readPropertyObject(
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

                for (int i = 0; i < 100 && intervalAfter.getTime().before(now.getTime()); i++) {
                    if (intervalBefore.getTime().after(now.getTime())) {
                        m_resources.add(new CmsExtendedNotificationCause(
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
        resources = m_cms.getResourcesInTimeRange(folder, 0, oneYearAgo.getTimeInMillis()).iterator();
        while (resources.hasNext()) {
            resource = (CmsResource)resources.next();
            m_resources.add(new CmsExtendedNotificationCause(
                resource,
                CmsExtendedNotificationCause.RESOURCE_OUTDATED,
                new Date(resource.getDateLastModified())));
        }

        // get all resources that will expire within the next week
        CmsResourceFilter resourceFilter = CmsResourceFilter.IGNORE_EXPIRATION.addRequireExpireBefore(inOneWeek.getTimeInMillis());
        resourceFilter = resourceFilter.addRequireExpireAfter(now.getTimeInMillis());
        resources = m_cms.readResources(folder, resourceFilter).iterator();
        while (resources.hasNext()) {
            resource = (CmsResource)resources.next();
            m_resources.add(new CmsExtendedNotificationCause(
                resource,
                CmsExtendedNotificationCause.RESOURCE_EXPIRES,
                new Date(resource.getDateExpired())));
        }

        // get all resources that will release within the next week
        resourceFilter = CmsResourceFilter.IGNORE_EXPIRATION.addRequireReleaseBefore(inOneWeek.getTimeInMillis());
        resourceFilter = resourceFilter.addRequireReleaseAfter(now.getTimeInMillis());
        resources = m_cms.readResources(folder, resourceFilter).iterator();
        while (resources.hasNext()) {
            resource = (CmsResource)resources.next();
            m_resources.add(new CmsExtendedNotificationCause(
                resource,
                CmsExtendedNotificationCause.RESOURCE_RELEASE,
                new Date(resource.getDateReleased())));
        }
    }

    /**
     * Returns a collection of CmsContentNotifications, one for each responsible that receives a notification.<p>
     * 
     * @return the list of CmsContentNotifications, one for each responsible that receives a notification
     * 
     * @throws CmsException if something goes wrong
     */
    protected Collection getContentNotifications() throws CmsException {

        // get all owners for the resource
        Iterator notificationCandidates = m_resources.iterator();
        Map result = new HashMap();
        while (notificationCandidates.hasNext()) {
            CmsExtendedNotificationCause resourceInfo = (CmsExtendedNotificationCause)notificationCandidates.next();
            CmsResource resource = resourceInfo.getResource();
            // skip, if content notification is not enabled for this resource
            String enableNotification = m_cms.readPropertyObject(
                resource,
                CmsPropertyDefinition.PROPERTY_ENABLE_NOTIFICATION,
                true).getValue();
            if (Boolean.valueOf(enableNotification).booleanValue()) {
                try {
                    Iterator responsibles = m_cms.readResponsibleUsers(resource).iterator();
                    while (responsibles.hasNext()) {
                        CmsUser responsible = (CmsUser)responsibles.next();

                        // check, if resultset already contains a content notification for the user
                        CmsContentNotification contentNotification = (CmsContentNotification)result.get(responsible);

                        // if not add a new content notification
                        if (contentNotification == null) {
                            contentNotification = new CmsContentNotification(responsible, m_cms);
                            result.put(responsible, contentNotification);
                        }
                        List resourcesForResponsible = contentNotification.getNotificationCauses();
                        if (resourcesForResponsible == null) {
                            resourcesForResponsible = new ArrayList();
                            contentNotification.setNotificationCauses(resourcesForResponsible);
                        }
                        resourcesForResponsible.add(resourceInfo);

                    }
                } catch (CmsException e) {
                    if (LOG.isInfoEnabled()) {
                        LOG.error(e);
                    }
                }
            }
        }
        return result.values();
    }

    /**
     * Sends all notifications to the responsible users.<p>
     * 
     * @return a string listing all responsibles that a notification was sent to
     * 
     * @throws CmsException if something goes wrong
     */
    public String notifyResponsibles() throws CmsException {

        Iterator notifications = filterConfirmedResources(getContentNotifications()).iterator();
        if (notifications.hasNext()) {
            StringBuffer result = new StringBuffer(Messages.get().key(Messages.LOG_NOTIFICATIONS_SENT_TO_0));
            result.append(' ');
            while (notifications.hasNext()) {
                CmsContentNotification contentNotification = (CmsContentNotification)notifications.next();
                result.append(contentNotification.getResponsible().getName());
                if (notifications.hasNext()) {
                    result.append(", ");
                }
                try {
                    contentNotification.send();
                } catch (MessagingException e) {
                    LOG.error(e);
                }
            }
            return result.toString();
        } else {
            return Messages.get().key(Messages.LOG_NO_NOTIFICATIONS_SENT_0);
        }
    }

    /**
     * Updates the resources that were confirmed by the user. That means deletes the resources that need not a
     * notification any more.
     * removes all resources which do not occur in the candidate list.<p>
     * 
     * @param resources the list of resources to remove from the set of confirmed resources
     * @return a new CmsConfirmedResources Object which all the resource removed
     */
    private Collection filterConfirmedResources(Collection contentNotifications) {

        Iterator notifications = contentNotifications.iterator();
        while (notifications.hasNext()) {
            CmsContentNotification contentNotification = (CmsContentNotification)notifications.next();
            CmsUser responsible = contentNotification.getResponsible();
            // check, if user was already notified
            List confirmedResourcesList = (List)responsible.getAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_CONFIRMED_RESOURCES);
            if (confirmedResourcesList == null) {
                confirmedResourcesList = new ArrayList();
                responsible.setAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_CONFIRMED_RESOURCES, new ArrayList());
            }

            List notificationCandidates = contentNotification.getNotificationCauses();

            List notificationResources = new ArrayList(notificationCandidates);
            // remove already confirmed resources            
            Iterator i = confirmedResourcesList.iterator();
            while (i.hasNext()) {
                Object o = i.next();
                if (notificationResources.contains(o)) {
                    notificationResources.remove(o);
                }
            }
            // filter confirmed resources
            i = new ArrayList(confirmedResourcesList).iterator();
            while (i.hasNext()) {
                Object o = i.next();
                if (!notificationCandidates.contains(o)) {
                    confirmedResourcesList.remove(o);
                }
            }
            contentNotification.setNotificationCauses(notificationResources);
            // Remove notification, if resource list is empty
            if (notificationCandidates.isEmpty()) {
                contentNotifications.remove(contentNotification);
            }
            try {
                m_cms.writeUser(responsible);
            } catch (CmsException e) {
                LOG.error(e);
            }
        }
        return contentNotifications;
    }
}