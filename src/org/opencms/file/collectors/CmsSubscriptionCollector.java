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

package org.opencms.file.collectors;

import org.opencms.db.CmsSubscriptionFilter;
import org.opencms.db.CmsSubscriptionReadMode;
import org.opencms.db.CmsVisitedByFilter;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.file.I_CmsResource;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * A collector that returns visited or subscribed resources depending on the current user and parameters.<p>
 *
 * The configuration of the collectors can be done in the parameter String using key value pairs,
 * separated by the <code>|</code> (pipe) symbol. The following configuration options are available:<p>
 * <ul>
 * <li><i>currentuser</i>: determines if the current user should be used to read visited or subscribed resources
 *     (not considered if the <code>user</code> parameter is used)</li>
 * <li><i>daysfrom</i>: the number of days subtracted from the current day specifying the start point in time from which a resource was visited</li>
 * <li><i>daysto</i>: the number of days subtracted from the current day specifying the end point in time to which a resource was visited</li>
 * <li><i>groups</i>: the users group names, separated by comma, to read subscribed resources for</li>
 * <li><i>includegroups</i>: the include groups flag to read subscribed resources also for the given or current users groups
 *     (not considered if the <code>groups</code> parameter is used)</li>
 * <li><i>includesubfolders</i>: the include subfolders flag to read subscribed resources also for the subfolders of the given parent folder
 *     (not considered if the <code>resource</code> parameter is not used)</li>
 * <li><i>mode</i>: the subscription read mode, can be <code>all</code>, <code>visited</code> or <code>unvisited</code> (default)</li>
 * <li><i>resource</i>: the resource, i.e. the parent folder from which the subscribed or visited resources should be read from</li>
 * <li><i>user</i>:<the user to read subscribed or visited resources for/li>
 * </ul>
 *
 * Example parameter String that can be used for the collector:<br/>
 * <code>currentuser=true|daysfrom=14|includegroups=true|mode=unvisited|resource=/demo_en/|includesubfolders=true</code><p>
 *
 * @since 8.0
 */
public class CmsSubscriptionCollector extends A_CmsResourceCollector {

    /** The collector parameter key for the current user flag (to set the user in the filters to the current user). */
    public static final String PARAM_KEY_CURRENTUSER = "currentuser";

    /**
     * The collector parameter key for the number of days subtracted from the current day
     * specifying the start point in time from which a resource was visited.
     */
    public static final String PARAM_KEY_DAYSFROM = "daysfrom";

    /**
     * The collector parameter key for the number of days subtracted from the current day
     * specifying the end point in time to which a resource was visited.<p>
     * If the parameter {@link #PARAM_KEY_DAYSFROM} is also used, the value of this key should be less than the value
     * set as {@link #PARAM_KEY_DAYSFROM} parameter.
     */
    public static final String PARAM_KEY_DAYSTO = "daysto";

    /** The collector parameter key for the users group names, separated by comma, to read subscribed resources for. */
    public static final String PARAM_KEY_GROUPS = "groups";

    /** The collector parameter key for the include groups flag to read subscribed resources also for the given or current users groups. */
    public static final String PARAM_KEY_INCLUDEGROUPS = "includegroups";

    /** The collector parameter key for the include subfolders flag to read subscribed resources also for the subfolders of the given parent folder. */
    public static final String PARAM_KEY_INCLUDESUBFOLDERS = "includesubfolders";

    /** The collector parameter key for the subscription read mode. */
    public static final String PARAM_KEY_MODE = "mode";

    /** The collector parameter key for the resource, i.e. the parent folder from which the subscribed or visited resources should be read from. */
    public static final String PARAM_KEY_RESOURCE = "resource";

    /** The collector parameter key for the user to read subscribed or visited resources for. */
    public static final String PARAM_KEY_USER = "user";

    /** Static array of the collectors implemented by this class. */
    private static final String[] COLLECTORS = {"allVisited", "allSubscribed", "allSubscribedDeleted"};

    /** Array list for fast collector name lookup. */
    private static final List<String> COLLECTORS_LIST = Collections.unmodifiableList(Arrays.asList(COLLECTORS));

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSubscriptionCollector.class);

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCollectorNames()
     */
    public List<String> getCollectorNames() {

        return COLLECTORS_LIST;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateLink(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getCreateLink(CmsObject cms, String collectorName, String param) {

        // this collector does not support creation of new resources
        return null;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateParam(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getCreateParam(CmsObject cms, String collectorName, String param) {

        // this collector does not support creation of new resources
        return null;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getResults(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public List<CmsResource> getResults(CmsObject cms, String collectorName, String param)
    throws CmsDataAccessException, CmsException {

        return getResults(cms, collectorName, param, -1);

    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getResults(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public List<CmsResource> getResults(CmsObject cms, String collectorName, String param, int numResults)
    throws CmsDataAccessException, CmsException {

        // if action is not set use default
        if (collectorName == null) {
            collectorName = COLLECTORS[0];
        }

        switch (COLLECTORS_LIST.indexOf(collectorName)) {
            case 0:
                // "allVisited"
                return getVisitedResources(cms, param, numResults);
            case 1:
                // "allSubscribed"
                return getSubscribedResources(cms, param, numResults);
            case 2:
                // "allSubscribedDeleted"
                return getSubscribedDeletedResources(cms, param, numResults);
            default:
                throw new CmsDataAccessException(
                    Messages.get().container(Messages.ERR_COLLECTOR_NAME_INVALID_1, collectorName));
        }
    }

    /**
     * Returns the subscribed deleted resources according to the collector parameter.<p>
     *
     * @param cms the current users context
     * @param param an optional collector parameter
     * @param numResults the number of results
     *
     * @return the subscribed deleted resources according to the collector parameter
     *
     * @throws CmsException if something goes wrong
     */
    protected List<CmsResource> getSubscribedDeletedResources(CmsObject cms, String param, int numResults)
    throws CmsException {

        Map<String, String> params = getParameters(param);
        CmsSubscriptionFilter filter = getSubscriptionFilter(cms, params);
        String parentPath = filter.getParentPath();
        if (CmsStringUtil.isNotEmpty(parentPath)) {
            parentPath = cms.getRequestContext().removeSiteRoot(parentPath);
        }

        List<I_CmsHistoryResource> deletedResources = OpenCms.getSubscriptionManager().readSubscribedDeletedResources(
            cms,
            filter.getUser(),
            Boolean.valueOf(params.get(PARAM_KEY_INCLUDEGROUPS)).booleanValue(),
            parentPath,
            filter.isIncludeSubFolders(),
            filter.getFromDate());

        // cast the history resources to CmsResource objects
        List<CmsResource> result = new ArrayList<CmsResource>(deletedResources.size());
        for (Iterator<I_CmsHistoryResource> i = deletedResources.iterator(); i.hasNext();) {
            I_CmsHistoryResource deletedResource = i.next();
            result.add((CmsResource)deletedResource);
        }
        if (numResults > 0) {
            result = shrinkToFit(result, numResults);
        }
        return result;
    }

    /**
     * Returns the subscribed resources according to the collector parameter.<p>
     *
     * @param cms the current users context
     * @param param an optional collector parameter
     * @param numResults the number of results
     *
     * @return the subscribed resources according to the collector parameter
     *
     * @throws CmsException if something goes wrong
     */
    protected List<CmsResource> getSubscribedResources(CmsObject cms, String param, int numResults)
    throws CmsException {

        List<CmsResource> result = OpenCms.getSubscriptionManager().readSubscribedResources(
            cms,
            getSubscriptionFilter(cms, param));
        Collections.sort(result, I_CmsResource.COMPARE_DATE_LAST_MODIFIED);
        if (numResults > 0) {
            result = shrinkToFit(result, numResults);
        }
        return result;
    }

    /**
     * Returns the configured subscription filter to use.<p>
     *
     * @param cms the current users context
     * @param params the optional collector parameters
     *
     * @return the configured subscription filter to use
     *
     * @throws CmsException if something goes wrong
     */
    protected CmsSubscriptionFilter getSubscriptionFilter(CmsObject cms, Map<String, String> params)
    throws CmsException {

        CmsSubscriptionFilter filter = new CmsSubscriptionFilter();

        // initialize the filter
        initVisitedByFilter(filter, cms, params, false);

        // set subscription filter specific parameters

        // determine the mode to read subscribed resources
        if (params.containsKey(PARAM_KEY_MODE)) {
            String modeName = params.get(PARAM_KEY_MODE);
            filter.setMode(CmsSubscriptionReadMode.modeForName(modeName));
        }

        // determine the groups to set in the filter
        if (params.containsKey(PARAM_KEY_GROUPS)) {
            List<String> groupNames = CmsStringUtil.splitAsList(params.get(PARAM_KEY_GROUPS), ',', true);
            for (Iterator<String> i = groupNames.iterator(); i.hasNext();) {
                String groupName = i.next();
                try {
                    CmsGroup group = cms.readGroup(groupName);
                    filter.addGroup(group);
                } catch (CmsException e) {
                    // error reading a group
                    LOG.error(
                        Messages.get().getBundle().key(
                            Messages.ERR_COLLECTOR_PARAM_INVALID_1,
                            PARAM_KEY_GROUPS + "=" + params.get(PARAM_KEY_GROUPS)));
                    throw e;
                }
            }
        }
        boolean includeUserGroups = Boolean.valueOf(params.get(PARAM_KEY_INCLUDEGROUPS)).booleanValue();
        if (filter.getGroups().isEmpty() && includeUserGroups) {
            // include the given or current users groups
            String userName = null;
            if (filter.getUser() != null) {
                userName = filter.getUser().getName();
            } else {
                userName = cms.getRequestContext().getCurrentUser().getName();
            }
            filter.setGroups(cms.getGroupsOfUser(userName, false));
        }

        return filter;
    }

    /**
     * Returns the configured subscription filter to use.<p>
     *
     * @param cms the current users context
     * @param param an optional collector parameter
     *
     * @return the configured subscription filter to use
     *
     * @throws CmsException if something goes wrong
     */
    protected CmsSubscriptionFilter getSubscriptionFilter(CmsObject cms, String param) throws CmsException {

        return getSubscriptionFilter(cms, getParameters(param));
    }

    /**
     * Returns the configured visited by filter to use.<p>
     *
     * @param cms the current users context
     * @param param an optional collector parameter
     *
     * @return the configured visited by filter to use
     *
     * @throws CmsException if something goes wrong
     */
    protected CmsVisitedByFilter getVisitedByFilter(CmsObject cms, String param) throws CmsException {

        CmsVisitedByFilter filter = new CmsVisitedByFilter();
        Map<String, String> params = getParameters(param);

        // initialize the filter
        initVisitedByFilter(filter, cms, params, true);

        return filter;
    }

    /**
     * Returns the visited resources according to the collector parameter.<p>
     *
     * @param cms the current users context
     * @param param an optional collector parameter
     * @param numResults the number of results
     *
     * @return the visited resources according to the collector parameter
     *
     * @throws CmsException if something goes wrong
     */
    protected List<CmsResource> getVisitedResources(CmsObject cms, String param, int numResults) throws CmsException {

        List<CmsResource> result = OpenCms.getSubscriptionManager().readResourcesVisitedBy(
            cms,
            getVisitedByFilter(cms, param));
        Collections.sort(result, I_CmsResource.COMPARE_DATE_LAST_MODIFIED);
        if (numResults > 0) {
            result = shrinkToFit(result, numResults);
        }
        return result;
    }

    /**
     * Returns the calculated time with the days delta using the base time.<p>
     *
     * @param baseTime the base time to calculate the returned time from
     * @param deltaDays the number of days which should be subtracted from the base time
     * @param key the parameter key name used for error messages
     * @param defaultTime the default time is used if there were errors calculating the resulting time
     *
     * @return the calculated time
     */
    private long getCalculatedTime(long baseTime, String deltaDays, String key, long defaultTime) {

        try {
            long days = Long.parseLong(deltaDays);
            long delta = 1000L * 60L * 60L * 24L * days;
            long result = baseTime - delta;
            if (result >= 0) {
                // result is a valid time stamp
                return result;
            }
        } catch (NumberFormatException e) {
            LOG.error(Messages.get().getBundle().key(Messages.ERR_COLLECTOR_PARAM_INVALID_1, key + "=" + deltaDays));
        }
        return defaultTime;
    }

    /**
     * Returns the collector parameters.<p>
     *
     * @param param the collector parameter
     *
     * @return the collector parameters
     */
    private Map<String, String> getParameters(String param) {

        if (CmsStringUtil.isNotEmpty(param)) {
            return CmsStringUtil.splitAsMap(param, "|", "=");
        }
        return Collections.emptyMap();
    }

    /**
     * Initializes the visited by filter from the parameters.<p>
     *
     * @param filter the filter to initialize
     * @param cms the current users context
     * @param params the collector parameters to configure the filter
     * @param forceSetUser flag to determine if a user has to be set in the filter
     *        (should be <code>true</code> for the visited by filter, <code>false</code> for the subscription filter)
     *
     * @throws CmsException if something goes wrong
     */
    private void initVisitedByFilter(
        CmsVisitedByFilter filter,
        CmsObject cms,
        Map<String, String> params,
        boolean forceSetUser) throws CmsException {

        // determine the user to set in the filter
        if (params.containsKey(PARAM_KEY_USER)) {
            try {
                CmsUser user = cms.readUser(params.get(PARAM_KEY_USER));
                filter.setUser(user);
            } catch (CmsException e) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.ERR_COLLECTOR_PARAM_USER_1, params.get(PARAM_KEY_USER)));
                throw e;
            }
        }
        boolean setCurrentUser = Boolean.valueOf(params.get(PARAM_KEY_CURRENTUSER)).booleanValue();
        if ((filter.getUser() == null) && (forceSetUser || setCurrentUser)) {
            // set current user
            filter.setUser(cms.getRequestContext().getCurrentUser());
        }

        // determine the time stamps to set in the filter
        long currentTime = System.currentTimeMillis();
        if (params.containsKey(PARAM_KEY_DAYSFROM)) {
            filter.setFromDate(getCalculatedTime(currentTime, params.get(PARAM_KEY_DAYSFROM), PARAM_KEY_DAYSFROM, 0L));
        }
        if (params.containsKey(PARAM_KEY_DAYSTO)) {
            filter.setToDate(
                getCalculatedTime(currentTime, params.get(PARAM_KEY_DAYSTO), PARAM_KEY_DAYSTO, Long.MAX_VALUE));
        }

        // determine if a parent folder should be used
        if (params.containsKey(PARAM_KEY_RESOURCE)) {
            try {
                CmsResource resource = cms.readResource(params.get(PARAM_KEY_RESOURCE));
                filter.setParentResource(resource);
                // check if the sub folders should be included
                if (params.containsKey(PARAM_KEY_INCLUDESUBFOLDERS)) {
                    boolean includeSubFolders = Boolean.valueOf(params.get(PARAM_KEY_INCLUDESUBFOLDERS)).booleanValue();
                    filter.setIncludeSubfolders(includeSubFolders);
                }
            } catch (CmsException e) {
                LOG.error(
                    Messages.get().getBundle().key(
                        Messages.ERR_COLLECTOR_PARAM_INVALID_1,
                        PARAM_KEY_RESOURCE + "=" + params.get(PARAM_KEY_RESOURCE)));
                throw e;
            }
        }
    }

}
