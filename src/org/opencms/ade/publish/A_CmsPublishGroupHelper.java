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

package org.opencms.ade.publish;

import org.opencms.main.CmsLog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Helper class for splitting a publish list into publish groups.<p>
 *
 * @param <RESOURCE> the resource class type
 * @param <GROUP> the resource group class type
 *
 * @since 8.0.0
 */
public abstract class A_CmsPublishGroupHelper<RESOURCE, GROUP> {

    /** An enum representing the age of a publish list resource. */
    public enum GroupAge {
        /** group age constant. */
        medium, /** group age constant. */
        old, /** group age constant. */
        young
    }

    /**
     * Comparator used for sorting publish resources.<p>
     */
    public class SortingComparator implements Comparator<RESOURCE> {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(RESOURCE r1, RESOURCE r2) {

            if (r1 == r2) {
                return 0;
            }

            long date1 = getDateLastModified(r1);
            long date2 = getDateLastModified(r2);

            return (date1 > date2) ? -1 : (date1 < date2) ? 1 : 0;
        }
    }

    /** The gap between session groups. */
    protected static final int GROUP_SESSIONS_GAP = 8 * 60 * 60 * 1000;

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsPublishGroupHelper.class);

    /** The current locale. */
    private Locale m_locale;

    /**
     * Creates a new publish group helper for a given locale.<p>
     *
     * @param locale the locale to use
     */
    public A_CmsPublishGroupHelper(Locale locale) {

        m_locale = locale;
    }

    /**
     * Given a descending list of dates represented as longs, this method computes a map from the dates
     * to their age in (local) days.<p>
     *
     * @param sortedDates a descending list of dates represented as longs
     *
     * @return a map from dates to ages (measured in days)
     */
    public Map<Long, Integer> computeDays(List<Long> sortedDates) {

        if (sortedDates.isEmpty()) {
            return Collections.<Long, Integer> emptyMap();
        }
        Map<Long, Integer> days = new HashMap<Long, Integer>();
        long lastDate = System.currentTimeMillis();
        int dayCounter = 0;
        for (Long dateObj : sortedDates) {
            long date = dateObj.longValue();
            long dayDifference = getDayDifference(lastDate, date);
            dayCounter += dayDifference;
            lastDate = date;
            days.put(dateObj, Integer.valueOf(dayCounter));
        }
        return days;
    }

    /**
     * Computes a map from modification date to number of (local) days since the modification date.<p>
     *
     * @param resources a list of resources
     *
     * @return a map from modification dates to the number of days since the modification date
     */
    public Map<Long, Integer> computeDaysForResources(List<RESOURCE> resources) {

        Map<Long, Integer> result = computeDays(getModificationDates(resources));
        if (LOG.isDebugEnabled()) {
            for (RESOURCE res : resources) {
                LOG.debug(
                    "Resource "
                        + getRootPath(res)
                        + " is "
                        + result.get(Long.valueOf(getDateLastModified(res)))
                        + " days old.");
            }
        }
        return result;
    }

    /**
     * Gets the difference in days between to dates given as longs.<p>
     *
     * The first date must be later than the second date.
     *
     * @param first the first date
     * @param second the second date
     *
     * @return the difference between the two dates in days
     */
    public int getDayDifference(long first, long second) {

        Calendar firstDay = getStartOfDay(first);
        Calendar secondDay = getStartOfDay(second);
        int result = 0;
        if (first >= second) {
            while (firstDay.after(secondDay)) {
                firstDay.add(Calendar.DAY_OF_MONTH, -1);
                result += 1;
            }
        } else {
            while (secondDay.after(firstDay)) {
                secondDay.add(Calendar.DAY_OF_MONTH, -1);
                result -= 1;
            }
        }
        return result;
    }

    /**
     * Splits a list of resources into groups.<p>
     *
     * @param resources the list of resources
     *
     * @return the list of groups
     */
    public List<GROUP> getGroups(List<RESOURCE> resources) {

        List<RESOURCE> sortedResources = new ArrayList<RESOURCE>(resources);
        Collections.sort(sortedResources, new SortingComparator());
        Map<Long, Integer> daysMap = computeDaysForResources(sortedResources);
        Map<GroupAge, List<RESOURCE>> resourcesByAge = partitionPublishResourcesByAge(sortedResources, daysMap);
        List<List<RESOURCE>> youngGroups = partitionYoungResources(resourcesByAge.get(GroupAge.young));
        List<List<RESOURCE>> mediumGroups = partitionMediumResources(resourcesByAge.get(GroupAge.medium), daysMap);
        List<RESOURCE> oldGroup = resourcesByAge.get(GroupAge.old);
        List<GROUP> resultGroups = new ArrayList<GROUP>();
        for (List<RESOURCE> groupRes : youngGroups) {
            String name = getPublishGroupName(groupRes, GroupAge.young);
            resultGroups.add(createGroup(name, groupRes));
        }
        for (List<RESOURCE> groupRes : mediumGroups) {
            String name = getPublishGroupName(groupRes, GroupAge.medium);
            resultGroups.add(createGroup(name, groupRes));
        }
        if (!oldGroup.isEmpty()) {
            String oldName = getPublishGroupName(oldGroup, GroupAge.old);
            resultGroups.add(createGroup(oldName, oldGroup));
        }
        return resultGroups;
    }

    /**
     * Given a list of resources, this method returns a list of their modification dates.<p>
     *
     * @param resources a list of resources
     *
     * @return the modification dates of the resources, in the same order as the resources
     */
    public List<Long> getModificationDates(List<RESOURCE> resources) {

        List<Long> result = new ArrayList<Long>();
        for (RESOURCE res : resources) {
            result.add(Long.valueOf(getDateLastModified(res)));
        }
        return result;
    }

    /**
     * Returns the localized name for a given publish group based on its age.<p>
     *
     * @param resources the resources of the publish group
     * @param age the age of the publish group
     *
     * @return the localized name of the publish group
     */
    public String getPublishGroupName(List<RESOURCE> resources, GroupAge age) {

        long groupDate = getDateLastModified(resources.get(0));
        String groupName;
        switch (age) {
            case young:
                groupName = Messages.get().getBundle(m_locale).key(
                    Messages.GUI_GROUPNAME_SESSION_1,
                    new Date(groupDate));

                break;
            case medium:
                groupName = Messages.get().getBundle(m_locale).key(Messages.GUI_GROUPNAME_DAY_1, new Date(groupDate));
                break;
            case old:
            default:
                groupName = Messages.get().getBundle(m_locale).key(Messages.GUI_GROUPNAME_EVERYTHING_ELSE_0);
                break;
        }
        return groupName;
    }

    /**
     * Returns a calendar object representing the start of the day in which a given time lies.<p>
     *
     * @param time a long representing a time
     *
     * @return a calendar object which represents the day in which the time lies
     */
    public Calendar getStartOfDay(long time) {

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        Calendar result = Calendar.getInstance();
        result.set(Calendar.YEAR, year);
        result.set(Calendar.MONTH, month);
        result.set(Calendar.DAY_OF_MONTH, day);
        return result;
    }

    /**
     * Computes publish groups for a list of resources with age "medium".<p>
     *
     * @param resources the list of resources
     * @param days a map from modification dates to the number of days since the modification
     *
     * @return a list of publish groups
     */
    public List<List<RESOURCE>> partitionMediumResources(List<RESOURCE> resources, Map<Long, Integer> days) {

        if (resources.isEmpty()) {
            return Collections.<List<RESOURCE>> emptyList();
        }
        RESOURCE firstRes = resources.get(0);
        int lastDay = days.get(Long.valueOf(getDateLastModified(firstRes))).intValue();
        List<List<RESOURCE>> result = new ArrayList<List<RESOURCE>>();
        List<RESOURCE> currentGroup = new ArrayList<RESOURCE>();
        result.add(currentGroup);
        for (RESOURCE res : resources) {
            LOG.debug("Processing medium-aged resource " + getRootPath(res));
            int day = days.get(Long.valueOf(getDateLastModified(res))).intValue();
            if (day != lastDay) {
                LOG.debug("=== new group ===");
                currentGroup = new ArrayList<RESOURCE>();
                result.add(currentGroup);
            }
            lastDay = day;
            currentGroup.add(res);
        }
        return result;
    }

    /**
     * Partitions a list of resources by their age in (local) days since the last modification.<p>
     *
     * @param resources the list of resources to partition
     * @param days the map from modification dates to the number of (local) days since the modification
     *
     * @return a map from age enum values to the list of resources which fall into the corresponding age group
     */
    public Map<GroupAge, List<RESOURCE>> partitionPublishResourcesByAge(
        List<RESOURCE> resources,
        Map<Long, Integer> days) {

        List<RESOURCE> youngRes = new ArrayList<RESOURCE>();
        List<RESOURCE> mediumRes = new ArrayList<RESOURCE>();
        List<RESOURCE> oldRes = new ArrayList<RESOURCE>();
        for (RESOURCE res : resources) {
            int day = days.get(Long.valueOf(getDateLastModified(res))).intValue();
            List<RESOURCE> listToAddTo = null;
            if (day < 7) {
                listToAddTo = youngRes;
                LOG.debug("Classifying publish resource " + getRootPath(res) + " as young");
            } else if (day < 28) {
                listToAddTo = mediumRes;
                LOG.debug("Classifying publish resource " + getRootPath(res) + " as medium-aged");
            } else {
                listToAddTo = oldRes;
                LOG.debug("Classifying publish resource " + getRootPath(res) + " as old");
            }
            listToAddTo.add(res);
        }
        Map<GroupAge, List<RESOURCE>> result = new HashMap<GroupAge, List<RESOURCE>>();
        result.put(GroupAge.young, youngRes);
        result.put(GroupAge.medium, mediumRes);
        result.put(GroupAge.old, oldRes);
        return result;
    }

    /**
     * Partitions the list of young resources into publish groups.<p>
     *
     * @param resources the list of resources to partition
     *
     * @return a partition of the resources into publish groups
     */
    public List<List<RESOURCE>> partitionYoungResources(List<RESOURCE> resources) {

        if (resources.isEmpty()) {
            return Collections.<List<RESOURCE>> emptyList();
        }
        List<List<RESOURCE>> result = new ArrayList<List<RESOURCE>>();
        List<RESOURCE> currentGroup = new ArrayList<RESOURCE>();
        result.add(currentGroup);

        long lastDate = getDateLastModified(resources.get(0));
        for (RESOURCE res : resources) {
            LOG.debug("Processing young resource " + getRootPath(res));
            long resDate = getDateLastModified(res);
            if ((lastDate - resDate) > GROUP_SESSIONS_GAP) {
                LOG.debug("=== new group ===");
                currentGroup = new ArrayList<RESOURCE>();
                result.add(currentGroup);
            }
            lastDate = resDate;
            currentGroup.add(res);
        }
        return result;
    }

    /**
     * Creates a named group of resources.<p>
     *
     * @param name the name of the group
     * @param resources the resources which should be put in the group
     *
     * @return the named group
     */
    protected abstract GROUP createGroup(String name, List<RESOURCE> resources);

    /**
     * Gets the last modification date of a resource.<p>
     *
     * @param res the resource
     *
     * @return the last modification date of res
     */
    protected abstract long getDateLastModified(RESOURCE res);

    /**
     * Gets the root path of a resource.<p>
     *
     * @param res the resource
     *
     * @return the root path of res
     */
    protected abstract String getRootPath(RESOURCE res);
}
