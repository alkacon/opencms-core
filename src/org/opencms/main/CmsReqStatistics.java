/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.main;

import org.opencms.db.CmsDbException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Statistics object for request performance.<p>
 */
public final class CmsReqStatistics {

    /** The static log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsReqStatistics.class);

    /** The thread local request statistics. */
    private static final ThreadLocal<CmsReqStatistics> PER_THREAD_STATISTICS = new ThreadLocal<CmsReqStatistics>();

    /** Time to init the cmsobject. */
    private long m_cmsInitTime;

    /** The endtime of the statistics object. */
    private long m_endtime;

    /** Time for the single steps during cms object initinalizazion. */
    private Map<String, Long> m_initCmsObjectTime;

    /** Time to init the resource. */
    private long m_initResourceTime;

    /** Time to load and display the resource. */
    private long m_loadResourceTime;

    /** The creationtime of the statistics object. */
    private long m_starttime;

    /** The uri of the requested resource. */
    private String m_uri;

    /**
     * Constructor, creates and initializes a new CmsReqStatistics.<p>
     */
    private CmsReqStatistics() {

        m_starttime = System.currentTimeMillis();
        m_endtime = 0;
        m_cmsInitTime = 0;
        m_initResourceTime = 0;
        m_loadResourceTime = 0;
        m_initCmsObjectTime = new HashMap<String, Long>();
    }

    /**
     * Sets the endtime of a CmsObject init step.<p>
     * @param name name of the handler
     */
    public static void endCmsInit(String name) {

        if (LOG.isDebugEnabled()) {
            CmsReqStatistics stats = PER_THREAD_STATISTICS.get();
            if (stats != null) {
                Long startTime = stats.m_initCmsObjectTime.get(name);
                if (startTime != null) {
                    long time = System.currentTimeMillis() - startTime.longValue();
                    stats.m_initCmsObjectTime.put(name, new Long(time));
                }
            }
        }
    }

    /**
     * Sets the timestamp after CmsObject initialization.<p>
     */
    public static void setCmsObjectInitTime() {

        if (LOG.isDebugEnabled()) {
            CmsReqStatistics stats = PER_THREAD_STATISTICS.get();
            if (stats != null) {
                stats.m_cmsInitTime = System.currentTimeMillis() - stats.m_starttime;
            }
        }
    }

    /**
     * Sets the endtime timestamp and logs the statistics. The statistics object will be cleared afterwards.<p>
     */
    public static void setEndTime() {

        if (LOG.isDebugEnabled()) {
            CmsReqStatistics stats = PER_THREAD_STATISTICS.get();
            if (stats != null) {
                stats.m_endtime = System.currentTimeMillis() - stats.m_starttime;
                LOG.debug(stats.getSummary());
                if (stats.getCmsInitTime() > 200) {
                    LOG.debug(stats.getCmsInitInfo());
                }
                for (String poolname : OpenCms.getSqlManager().getDbPoolUrls()) {
                    try {
                        LOG.debug(stats.getConn(poolname));
                    } catch (CmsException e) {
                        LOG.error(e);
                    }
                }
                PER_THREAD_STATISTICS.set(null);
                stats.clear();
            }
        }
    }

    /**
     * Clears the statistics object.<p>
     */
    private void clear() {

        m_initCmsObjectTime = null;
        m_uri = null;
    }

    /**
     * Sets the timestamp after CmsResource initialization.<p>
     */
    public static void setInitResoueceTime() {

        if (LOG.isDebugEnabled()) {
            CmsReqStatistics stats = PER_THREAD_STATISTICS.get();
            if (stats != null) {
                stats.m_initResourceTime = System.currentTimeMillis() - stats.m_starttime;
            }
        }
    }

    /**
     * Sets the timestamp after resource load and display.<p>
     */
    public static void setLoadResoueceTime() {

        if (LOG.isDebugEnabled()) {
            CmsReqStatistics stats = PER_THREAD_STATISTICS.get();
            if (stats != null) {
                stats.m_loadResourceTime = System.currentTimeMillis() - stats.m_starttime;
            }
        }
    }

    /**
     * Sets the starttime of a CmsObject init step.<p>
     * @param name name of the handler
     */
    public static void startCmsInit(String name) {

        if (LOG.isDebugEnabled()) {
            CmsReqStatistics stats = PER_THREAD_STATISTICS.get();
            if (stats != null) {
                stats.m_initCmsObjectTime.put(name, new Long(System.currentTimeMillis()));
            }
        }
    }

    /**
     * Starts request statistics for a requested URL.<p>
     * 
     * @param requestedUrl the requested URL
     */
    public static void startStatistics(String requestedUrl) {

        if (LOG.isDebugEnabled()) {
            CmsReqStatistics stats = new CmsReqStatistics();
            stats.m_uri = requestedUrl;
            PER_THREAD_STATISTICS.set(stats);
        }
    }

    /**
     * Gets the CmsObject init information.<p>
     * @return string containing the CmsObject init  info
     */
    public String getCmsInitInfo() {

        StringBuffer buf = new StringBuffer();
        String tmp;

        List<String> initTimes = new ArrayList<String>(m_initCmsObjectTime.keySet());
        Collections.sort(initTimes);
        for (String initTime : initTimes) {
            buf.append("[");
            buf.append(initTime.substring(initTime.lastIndexOf(":") + 1));
            buf.append("][");
            tmp = "0000" + m_initCmsObjectTime.get(initTime);
            tmp = tmp.substring(tmp.length() - 4);
            buf.append(tmp);
            buf.append("]");
        }

        return buf.toString();
    }

    /**
     * Gets the total time used for CmsObject init.<p>
     * @return total time used for CmsObject init
     */
    public long getCmsInitTime() {

        return m_cmsInitTime;
    }

    /**
     * Gets the connection summary.<p>
     * 
     * @param poolname name of the connection pool
     * @return string containing the summary info
     * @throws CmsDbException if accessing the db pools fails
     */
    public String getConn(String poolname) throws CmsDbException {

        StringBuffer buf = new StringBuffer();
        String tmp;

        buf.append("ACTIVE[");
        tmp = "0000" + OpenCms.getSqlManager().getActiveConnections(poolname);
        tmp = tmp.substring(tmp.length() - 5);
        buf.append(tmp);
        buf.append("] IDLE[");
        tmp = "00000000" + OpenCms.getSqlManager().getIdleConnections(poolname);
        tmp = tmp.substring(tmp.length() - 5);
        buf.append(tmp);
        buf.append("] POOL[");
        buf.append(poolname);
        buf.append("]");

        return buf.toString();
    }

    /**
     * Gets the statistics summary.<p>
     * @return string containing the summary info
     */
    public String getSummary() {

        StringBuffer buf = new StringBuffer();
        String tmp;

        buf.append("TOTAL[");
        tmp = "00000000" + m_endtime;
        tmp = tmp.substring(tmp.length() - 8);
        buf.append(tmp);
        buf.append("] CI[");
        tmp = "00000000" + m_cmsInitTime;
        tmp = tmp.substring(tmp.length() - 8);
        buf.append(tmp);
        buf.append("] IR[");
        tmp = "00000000" + m_initResourceTime;
        tmp = tmp.substring(tmp.length() - 8);
        buf.append(tmp);
        buf.append("] LR[");
        tmp = "00000000" + m_loadResourceTime;
        tmp = tmp.substring(tmp.length() - 8);
        buf.append(tmp);
        buf.append("] URI='");
        buf.append(m_uri);
        buf.append("'");

        return buf.toString();
    }

    /**
     * Gets the total time used of the statistics object.<p>
     * @return total time used by the object
     */
    public long getTime() {

        return m_endtime;
    }

}
