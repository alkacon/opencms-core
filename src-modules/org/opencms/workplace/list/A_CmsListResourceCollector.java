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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.list;

import org.opencms.ade.publish.CmsCollectorPublishListHelper;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.collectors.I_CmsResourceCollector;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.I_CmsContentLoadCollectorInfo;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.commons.CmsProgressThread;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;

/**
 * Collector to provide {@link CmsResource} objects for a explorer List.<p>
 *
 * @since 6.1.0
 */
public abstract class A_CmsListResourceCollector implements I_CmsListResourceCollector {

    /** VFS path to use for a dummy resource object. */
    public static final String VFS_PATH_NONE = "none";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsListResourceCollector.class);

    /** The collector parameter. */
    protected String m_collectorParameter;

    /** List item cache. */
    protected Map<String, CmsListItem> m_liCache = new HashMap<String, CmsListItem>();

    /** Resource cache. */
    protected Map<String, CmsResource> m_resCache = new HashMap<String, CmsResource>();

    /** Cache for resource list result. */
    protected List<CmsResource> m_resources;

    /** The workplace object where the collector is used from. */
    private A_CmsListExplorerDialog m_wp;

    /**
     * Constructor, creates a new list collector.<p>
     *
     * @param wp the workplace object where the collector is used from
     */
    protected A_CmsListResourceCollector(A_CmsListExplorerDialog wp) {

        m_wp = wp;
        CmsListState state = (wp != null ? wp.getListStateForCollector() : new CmsListState());
        if (state.getPage() < 1) {
            state.setPage(1);
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state.getColumn())) {
            state.setColumn(A_CmsListExplorerDialog.LIST_COLUMN_NAME);
        }
        if (state.getOrder() == null) {
            state.setOrder(CmsListOrderEnum.ORDER_ASCENDING);
        }
        if (state.getFilter() == null) {
            state.setFilter("");
        }
        m_collectorParameter = I_CmsListResourceCollector.PARAM_PAGE
            + I_CmsListResourceCollector.SEP_KEYVAL
            + state.getPage();
        m_collectorParameter += I_CmsListResourceCollector.SEP_PARAM
            + I_CmsListResourceCollector.PARAM_SORTBY
            + I_CmsListResourceCollector.SEP_KEYVAL
            + state.getColumn();
        m_collectorParameter += I_CmsListResourceCollector.SEP_PARAM
            + I_CmsListResourceCollector.PARAM_ORDER
            + I_CmsListResourceCollector.SEP_KEYVAL
            + state.getOrder();
        m_collectorParameter += I_CmsListResourceCollector.SEP_PARAM
            + I_CmsListResourceCollector.PARAM_FILTER
            + I_CmsListResourceCollector.SEP_KEYVAL
            + state.getFilter();
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(I_CmsResourceCollector arg0) {

        return 0;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateLink(org.opencms.file.CmsObject)
     */
    public String getCreateLink(CmsObject cms) {

        return null;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateLink(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getCreateLink(CmsObject cms, String collectorName, String param) {

        return null;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateParam(org.opencms.file.CmsObject)
     */
    public String getCreateParam(CmsObject cms) {

        return null;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateParam(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getCreateParam(CmsObject cms, String collectorName, String param) {

        return null;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateTypeId(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public int getCreateTypeId(CmsObject cms, String collectorName, String param) {

        return -1;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getDefaultCollectorName()
     */
    public String getDefaultCollectorName() {

        return getCollectorNames().get(0);
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getDefaultCollectorParam()
     */
    public String getDefaultCollectorParam() {

        return m_collectorParameter;
    }

    /**
     * Returns a list of list items from a list of resources.<p>
     *
     * @param parameter the collector parameter or <code>null</code> for default.<p>
     *
     * @return a list of {@link CmsListItem} objects
     *
     * @throws CmsException if something goes wrong
     */
    public List<CmsListItem> getListItems(String parameter) throws CmsException {

        synchronized (this) {
            if (parameter == null) {
                parameter = m_collectorParameter;
            }
            Map<String, String> params = CmsStringUtil.splitAsMap(
                parameter,
                I_CmsListResourceCollector.SEP_PARAM,
                I_CmsListResourceCollector.SEP_KEYVAL);
            CmsListState state = getState(params);
            List<CmsResource> resources = getInternalResources(getWp().getCms(), params);
            List<CmsListItem> ret = new ArrayList<CmsListItem>();
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(
                        Messages.LOG_COLLECTOR_PROCESS_ITEMS_START_1,
                        Integer.valueOf(resources.size())));
            }
            getWp().applyColumnVisibilities();
            CmsHtmlList list = getWp().getList();

            // check if progress should be set in the thread
            CmsProgressThread thread = null;
            int progressOffset = 0;
            if (Thread.currentThread() instanceof CmsProgressThread) {
                thread = (CmsProgressThread)Thread.currentThread();
                progressOffset = thread.getProgress();
            }

            CmsListColumnDefinition colPermissions = list.getMetadata().getColumnDefinition(
                A_CmsListExplorerDialog.LIST_COLUMN_PERMISSIONS);
            boolean showPermissions = (colPermissions.isVisible() || colPermissions.isPrintable());
            CmsListColumnDefinition colDateLastMod = list.getMetadata().getColumnDefinition(
                A_CmsListExplorerDialog.LIST_COLUMN_DATELASTMOD);
            boolean showDateLastMod = (colDateLastMod.isVisible() || colDateLastMod.isPrintable());
            CmsListColumnDefinition colUserLastMod = list.getMetadata().getColumnDefinition(
                A_CmsListExplorerDialog.LIST_COLUMN_USERLASTMOD);
            boolean showUserLastMod = (colUserLastMod.isVisible() || colUserLastMod.isPrintable());
            CmsListColumnDefinition colDateCreate = list.getMetadata().getColumnDefinition(
                A_CmsListExplorerDialog.LIST_COLUMN_DATECREATE);
            boolean showDateCreate = (colDateCreate.isVisible() || colDateCreate.isPrintable());
            CmsListColumnDefinition colUserCreate = list.getMetadata().getColumnDefinition(
                A_CmsListExplorerDialog.LIST_COLUMN_USERCREATE);
            boolean showUserCreate = (colUserCreate.isVisible() || colUserCreate.isPrintable());
            CmsListColumnDefinition colDateRel = list.getMetadata().getColumnDefinition(
                A_CmsListExplorerDialog.LIST_COLUMN_DATEREL);
            boolean showDateRel = (colDateRel.isVisible() || colDateRel.isPrintable());
            CmsListColumnDefinition colDateExp = list.getMetadata().getColumnDefinition(
                A_CmsListExplorerDialog.LIST_COLUMN_DATEEXP);
            boolean showDateExp = (colDateExp.isVisible() || colDateExp.isPrintable());
            CmsListColumnDefinition colState = list.getMetadata().getColumnDefinition(
                A_CmsListExplorerDialog.LIST_COLUMN_STATE);
            boolean showState = (colState.isVisible() || colState.isPrintable());
            CmsListColumnDefinition colLockedBy = list.getMetadata().getColumnDefinition(
                A_CmsListExplorerDialog.LIST_COLUMN_LOCKEDBY);
            boolean showLockedBy = (colLockedBy.isVisible() || colLockedBy.isPrintable());
            CmsListColumnDefinition colSite = list.getMetadata().getColumnDefinition(
                A_CmsListExplorerDialog.LIST_COLUMN_SITE);
            boolean showSite = (colSite.isVisible() || colSite.isPrintable());

            // get content
            Iterator<CmsResource> itRes = resources.iterator();
            int count = 0;
            while (itRes.hasNext()) {
                // set progress in thread
                if (thread != null) {
                    count++;
                    if (thread.isInterrupted()) {
                        throw new CmsIllegalStateException(
                            org.opencms.workplace.commons.Messages.get().container(
                                org.opencms.workplace.commons.Messages.ERR_PROGRESS_INTERRUPTED_0));
                    }
                    thread.setProgress(((count * 40) / resources.size()) + progressOffset);
                    thread.setDescription(
                        org.opencms.workplace.commons.Messages.get().getBundle(thread.getLocale()).key(
                            org.opencms.workplace.commons.Messages.GUI_PROGRESS_PUBLISH_STEP2_2,
                            Integer.valueOf(count),
                            Integer.valueOf(resources.size())));
                }

                Object obj = itRes.next();
                if (!(obj instanceof CmsResource)) {
                    ret.add(getDummyListItem(list));
                    continue;
                }
                CmsResource resource = (CmsResource)obj;
                CmsListItem item = m_liCache.get(resource.getStructureId().toString());
                if (item == null) {
                    item = createResourceListItem(
                        resource,
                        list,
                        showPermissions,
                        showDateLastMod,
                        showUserLastMod,
                        showDateCreate,
                        showUserCreate,
                        showDateRel,
                        showDateExp,
                        showState,
                        showLockedBy,
                        showSite);
                    m_liCache.put(resource.getStructureId().toString(), item);
                }
                ret.add(item);
            }
            CmsListMetadata metadata = list.getMetadata();
            if (metadata != null) {
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(state.getFilter())) {
                    // filter
                    ret = metadata.getSearchAction().filter(ret, state.getFilter());
                }
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(state.getColumn())) {
                    if ((metadata.getColumnDefinition(state.getColumn()) != null)
                        && metadata.getColumnDefinition(state.getColumn()).isSorteable()) {
                        // sort
                        I_CmsListItemComparator c = metadata.getColumnDefinition(
                            state.getColumn()).getListItemComparator();
                        Collections.sort(ret, c.getComparator(state.getColumn(), getWp().getLocale()));
                        if (state.getOrder().equals(CmsListOrderEnum.ORDER_DESCENDING)) {
                            Collections.reverse(ret);
                        }
                    }
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(
                        Messages.LOG_COLLECTOR_PROCESS_ITEMS_END_1,
                        Integer.valueOf(ret.size())));
            }
            return ret;
        }
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getOrder()
     */
    public int getOrder() {

        return 0;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsCollectorPublishListProvider#getPublishResources(org.opencms.file.CmsObject, org.opencms.gwt.shared.I_CmsContentLoadCollectorInfo)
     */
    public Set<CmsResource> getPublishResources(final CmsObject cms, final I_CmsContentLoadCollectorInfo info)
    throws CmsException {

        int collectorLimit = NumberUtils.toInt(
            OpenCms.getADEManager().getParameters(cms).get(CmsGwtConstants.COLLECTOR_PUBLISH_LIST_LIMIT),
            DEFAULT_LIMIT);
        CmsCollectorPublishListHelper helper = new CmsCollectorPublishListHelper(cms, info, collectorLimit);
        return helper.getPublishListFiles();

    }

    /**
     * Returns the resource for the given item.<p>
     *
     * @param cms the cms object
     * @param item the item
     *
     * @return the resource
     */
    public CmsResource getResource(CmsObject cms, CmsListItem item) {

        CmsResource res = m_resCache.get(item.getId());
        if (res == null) {
            CmsUUID id = new CmsUUID(item.getId());
            if (!id.isNullUUID()) {
                try {
                    res = cms.readResource(id, CmsResourceFilter.ALL);
                    m_resCache.put(item.getId(), res);
                } catch (CmsException e) {
                    // should never happen
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            }
        }
        return res;
    }

    /**
     * Returns all, unsorted and unfiltered, resources.<p>
     *
     * Be sure to cache the resources.<p>
     *
     * @param cms the cms object
     * @param params the parameter map
     *
     * @return a list of {@link CmsResource} objects
     *
     * @throws CmsException if something goes wrong
     */
    public abstract List<CmsResource> getResources(CmsObject cms, Map<String, String> params) throws CmsException;

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getResults(org.opencms.file.CmsObject)
     */
    public List<CmsResource> getResults(CmsObject cms) throws CmsException {

        return getResults(cms, getDefaultCollectorName(), m_collectorParameter);
    }

    /**
     * The parameter must follow the syntax "page:nr" where nr is the number of the page to be displayed.<p>
     *
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getResults(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public List<CmsResource> getResults(CmsObject cms, String collectorName, String parameter) throws CmsException {

        synchronized (this) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_COLLECTOR_GET_RESULTS_START_0));
            }
            if (parameter == null) {
                parameter = m_collectorParameter;
            }
            List<CmsResource> resources = new ArrayList<CmsResource>();
            if (getWp().getList() != null) {
                Iterator<CmsListItem> itItems = getListItems(parameter).iterator();
                while (itItems.hasNext()) {
                    CmsListItem item = itItems.next();
                    resources.add(getResource(cms, item));
                }
            } else {
                Map<String, String> params = CmsStringUtil.splitAsMap(
                    parameter,
                    I_CmsListResourceCollector.SEP_PARAM,
                    I_CmsListResourceCollector.SEP_KEYVAL);
                resources = getInternalResources(cms, params);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(
                        Messages.LOG_COLLECTOR_GET_RESULTS_END_1,
                        Integer.valueOf(resources.size())));
            }
            return resources;
        }
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getResults(org.opencms.file.CmsObject, java.lang.String, java.lang.String, int)
     */
    public List<CmsResource> getResults(CmsObject cms, String collectorName, String params, int numResults)
    throws CmsException {

        List<CmsResource> result = getResults(cms, collectorName, params);
        if ((numResults > 0) && (result.size() > numResults)) {
            return Lists.newArrayList(result.subList(0, numResults));
        }

        return result;
    }

    /**
     * Returns the workplace object.<p>
     *
     * @return the workplace object
     */
    public A_CmsListExplorerDialog getWp() {

        return m_wp;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#setDefaultCollectorName(java.lang.String)
     */
    public void setDefaultCollectorName(String collectorName) {

        // ignore
    }

    /**
     * The parameter must follow the syntax "mode|projectId" where mode is either "new", "changed", "deleted"
     * or "modified" and projectId is the id of the project to be displayed.<p>
     *
     * @see org.opencms.file.collectors.I_CmsResourceCollector#setDefaultCollectorParam(java.lang.String)
     */
    public void setDefaultCollectorParam(String param) {

        m_collectorParameter = param;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#setOrder(int)
     */
    public void setOrder(int order) {

        // ignore
    }

    /**
     * Sets the current display page.<p>
     *
     * @param page the new display page
     */
    public void setPage(int page) {

        if (m_collectorParameter != null) {
            int pos = m_collectorParameter.indexOf(I_CmsListResourceCollector.PARAM_PAGE);
            if (pos >= 0) {
                String params = "";
                int endPos = m_collectorParameter.indexOf(I_CmsListResourceCollector.SEP_PARAM, pos);
                if (pos > 0) {
                    pos -= I_CmsListResourceCollector.SEP_PARAM.length(); // remove also the SEP_PARAM
                    params += m_collectorParameter.substring(0, pos);
                }
                if (endPos >= 0) {
                    if (pos == 0) {
                        endPos += I_CmsListResourceCollector.SEP_PARAM.length(); // remove also the SEP_PARAM
                    }
                    params += m_collectorParameter.substring(endPos, m_collectorParameter.length());
                }
                m_collectorParameter = params;
            }
        }
        if (m_collectorParameter == null) {
            m_collectorParameter = "";
        } else if (m_collectorParameter.length() > 0) {
            m_collectorParameter += I_CmsListResourceCollector.SEP_PARAM;
        }
        m_collectorParameter += I_CmsListResourceCollector.PARAM_PAGE + I_CmsListResourceCollector.SEP_KEYVAL + page;
        synchronized (this) {
            m_resources = null;
        }
    }

    /**
     * Returns a list item created from the resource information, differs between valid resources and invalid resources.<p>
     *
     * @param resource the resource to create the list item from
     * @param list the list
     * @param showPermissions if to show permissions
     * @param showDateLastMod if to show the last modification date
     * @param showUserLastMod if to show the last modification user
     * @param showDateCreate if to show the creation date
     * @param showUserCreate if to show the creation date
     * @param showDateRel if to show the date released
     * @param showDateExp if to show the date expired
     * @param showState if to show the state
     * @param showLockedBy if to show the lock user
     * @param showSite if to show the site
     *
     * @return a list item created from the resource information
     */
    protected CmsListItem createResourceListItem(
        CmsResource resource,
        CmsHtmlList list,
        boolean showPermissions,
        boolean showDateLastMod,
        boolean showUserLastMod,
        boolean showDateCreate,
        boolean showUserCreate,
        boolean showDateRel,
        boolean showDateExp,
        boolean showState,
        boolean showLockedBy,
        boolean showSite) {

        CmsListItem item = list.newItem(resource.getStructureId().toString());
        // get an initialized resource utility
        CmsResourceUtil resUtil = getWp().getResourceUtil();
        resUtil.setResource(resource);
        item.set(A_CmsListExplorerDialog.LIST_COLUMN_NAME, resUtil.getPath());
        item.set(A_CmsListExplorerDialog.LIST_COLUMN_ROOT_PATH, resUtil.getFullPath());
        item.set(A_CmsListExplorerDialog.LIST_COLUMN_TITLE, resUtil.getTitle());
        item.set(A_CmsListExplorerDialog.LIST_COLUMN_TYPE, resUtil.getResourceTypeName());
        item.set(A_CmsListExplorerDialog.LIST_COLUMN_SIZE, resUtil.getSizeString());
        if (showPermissions) {
            item.set(A_CmsListExplorerDialog.LIST_COLUMN_PERMISSIONS, resUtil.getPermissionString());
        }
        if (showDateLastMod) {
            item.set(A_CmsListExplorerDialog.LIST_COLUMN_DATELASTMOD, new Date(resource.getDateLastModified()));
        }
        if (showUserLastMod) {
            item.set(A_CmsListExplorerDialog.LIST_COLUMN_USERLASTMOD, resUtil.getUserLastModified());
        }
        if (showDateCreate) {
            item.set(A_CmsListExplorerDialog.LIST_COLUMN_DATECREATE, new Date(resource.getDateCreated()));
        }
        if (showUserCreate) {
            item.set(A_CmsListExplorerDialog.LIST_COLUMN_USERCREATE, resUtil.getUserCreated());
        }
        if (showDateRel) {
            item.set(A_CmsListExplorerDialog.LIST_COLUMN_DATEREL, new Date(resource.getDateReleased()));
        }
        if (showDateExp) {
            item.set(A_CmsListExplorerDialog.LIST_COLUMN_DATEEXP, new Date(resource.getDateExpired()));
        }
        if (showState) {
            item.set(A_CmsListExplorerDialog.LIST_COLUMN_STATE, resUtil.getStateName());
        }
        if (showLockedBy) {
            item.set(A_CmsListExplorerDialog.LIST_COLUMN_LOCKEDBY, resUtil.getLockedByName());
        }
        if (showSite) {
            item.set(A_CmsListExplorerDialog.LIST_COLUMN_SITE, resUtil.getSiteTitle());
        }
        setAdditionalColumns(item, resUtil);
        return item;
    }

    /**
     * Returns a dummy list item.<p>
     *
     * @param list the list object to create the entry for
     *
     * @return a dummy list item
     */
    protected CmsListItem getDummyListItem(CmsHtmlList list) {

        CmsListItem item = list.newItem(CmsUUID.getNullUUID().toString());
        item.set(A_CmsListExplorerDialog.LIST_COLUMN_NAME, "");
        item.set(A_CmsListExplorerDialog.LIST_COLUMN_TITLE, "");
        item.set(A_CmsListExplorerDialog.LIST_COLUMN_TYPE, "");
        item.set(A_CmsListExplorerDialog.LIST_COLUMN_SIZE, "");
        item.set(A_CmsListExplorerDialog.LIST_COLUMN_PERMISSIONS, "");
        item.set(A_CmsListExplorerDialog.LIST_COLUMN_DATELASTMOD, new Date());
        item.set(A_CmsListExplorerDialog.LIST_COLUMN_USERLASTMOD, "");
        item.set(A_CmsListExplorerDialog.LIST_COLUMN_DATECREATE, new Date());
        item.set(A_CmsListExplorerDialog.LIST_COLUMN_USERCREATE, "");
        item.set(A_CmsListExplorerDialog.LIST_COLUMN_DATEREL, new Date());
        item.set(A_CmsListExplorerDialog.LIST_COLUMN_DATEEXP, new Date());
        item.set(A_CmsListExplorerDialog.LIST_COLUMN_STATE, "");
        item.set(A_CmsListExplorerDialog.LIST_COLUMN_LOCKEDBY, "");
        return item;
    }

    /**
     * Wrapper method for caching the result of {@link #getResources(CmsObject, Map)}.<p>
     *
     * @param cms the cms object
     * @param params the parameter map
     *
     * @return the result of {@link #getResources(CmsObject, Map)}
     *
     * @throws CmsException if something goes wrong
     */
    protected List<CmsResource> getInternalResources(CmsObject cms, Map<String, String> params) throws CmsException {

        synchronized (this) {
            if (m_resources == null) {
                m_resources = getResources(cms, params);
                Iterator<CmsResource> it = m_resources.iterator();
                while (it.hasNext()) {
                    CmsResource resource = it.next();
                    m_resCache.put(resource.getStructureId().toString(), resource);
                }
            }
        }
        return m_resources;
    }

    /**
     * Returns the list of resource names from the parameter map.<p>
     *
     * @param params the parameter map
     *
     * @return the list of resource names
     *
     * @see I_CmsListResourceCollector#PARAM_RESOURCES
     */
    protected List<String> getResourceNamesFromParam(Map<String, String> params) {

        String resourcesParam = "/";
        if (params.containsKey(I_CmsListResourceCollector.PARAM_RESOURCES)) {
            resourcesParam = params.get(I_CmsListResourceCollector.PARAM_RESOURCES);
        }
        if (resourcesParam.length() == 0) {
            return Collections.emptyList();
        }
        return CmsStringUtil.splitAsList(resourcesParam, "#");
    }

    /**
     * Returns the state of the parameter map.<p>
     *
     * @param params the parameter map
     *
     * @return the state of the list from the parameter map
     */
    protected CmsListState getState(Map<String, String> params) {

        CmsListState state = new CmsListState();
        try {
            state.setPage(Integer.parseInt(params.get(I_CmsListResourceCollector.PARAM_PAGE)));
        } catch (Throwable e) {
            // ignore
        }
        try {
            state.setOrder(CmsListOrderEnum.valueOf(params.get(I_CmsListResourceCollector.PARAM_ORDER)));
        } catch (Throwable e) {
            // ignore
        }
        try {
            state.setFilter(params.get(I_CmsListResourceCollector.PARAM_FILTER));
        } catch (Throwable e) {
            // ignore
        }
        try {
            state.setColumn(params.get(I_CmsListResourceCollector.PARAM_SORTBY));
        } catch (Throwable e) {
            // ignore
        }
        return state;
    }

    /**
     * Set additional column entries for a resource.<p>
     *
     * Overwrite this method to set additional column entries.<p>
     *
     * @param item the current list item
     * @param resUtil the resource util object for getting the info from
     */
    protected abstract void setAdditionalColumns(CmsListItem item, CmsResourceUtil resUtil);

    /**
     * Sets the resources parameter.<p>
     *
     * @param resources the list of resource names to use
     */
    protected void setResourcesParam(List<String> resources) {

        m_collectorParameter += I_CmsListResourceCollector.SEP_PARAM
            + I_CmsListResourceCollector.PARAM_RESOURCES
            + I_CmsListResourceCollector.SEP_KEYVAL;
        if (resources == null) {
            // search anywhere
            m_collectorParameter += "/";
        } else {
            m_collectorParameter += CmsStringUtil.collectionAsString(resources, "#");
        }
    }
}
