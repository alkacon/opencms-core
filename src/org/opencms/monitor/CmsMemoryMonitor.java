/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/monitor/CmsMemoryMonitor.java,v $
 * Date   : $Date: 2003/11/07 17:29:00 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
package org.opencms.monitor;

import org.opencms.cache.CmsLruCache;
import org.opencms.cache.I_CmsLruCacheObject;
import org.opencms.cron.I_CmsCronJob;
import org.opencms.main.CmsEvent;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;

import com.opencms.core.CmsException;
import com.opencms.defaults.CmsMail;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.util.Utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.LRUMap;

import source.org.apache.java.util.Configurations;

/**
 * Monitors OpenCms memory consumtion.<p>
 * 
 * @version $Revision: 1.5 $ $Date: 2003/11/07 17:29:00 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 */
public class CmsMemoryMonitor implements I_CmsCronJob {

    HashMap m_monitoredObjects;

    boolean m_initialized = false;

    int m_interval = -1;

    int m_maxUsage = -1;

    long m_lastRun = 0;

    /** the time of the last email send */
    long m_lastEmail = 0;

    /** the interval for sending emails */
    int m_emailInt = -1;

    /** sender for status emails */
    String m_emailSender;

    /** receivers fro status emails */
    String[] m_emailReceiver;

    /** flag for emergency mail send */
    boolean m_emergencyMail = false;

    /**
     * Adds a new object to the monitor.<p>
     * 
     * @param objectName name of the object
     * @param object the object for monitoring
     */
    public void register(String objectName, Object object) {

        if (!m_initialized) {
            m_monitoredObjects = new HashMap();
            m_initialized = true;
        }
        m_monitoredObjects.put(objectName, object);

    }

    /**
     * Initalizes the Memory Monitor.<p>
     * @param config the OpenCms configurations
     * @return CmsMemoryMonitor 
     */
    public static CmsMemoryMonitor initialize(Configurations config) {
        CmsMemoryMonitor monitor = new CmsMemoryMonitor();
        monitor.m_emailSender = config.getString("memorymonitor.email.sender");
        monitor.m_emailReceiver = config.getStringArray("memorymonitor.email.receiver");
        monitor.m_emailInt = config.getInteger("memorymonitor.email.interval") * 60000;
        monitor.m_interval = config.getInteger("memorymonitor.interval") * 60000;
        monitor.m_maxUsage = config.getInteger("memorymonitor.maxUsage");

        monitor.m_monitoredObjects = new HashMap();
        monitor.m_initialized = true;
        return monitor;
    }

    /**
     * Returns if monitoring is enabled.<p>
     * 
     * @return true if monitoring is enabled
     */
    public boolean enabled() {
        return true;
    }

    /**
     * @see org.opencms.cron.I_CmsCronJob#launch(com.opencms.file.CmsObject, java.lang.String)
     */
    public final String launch(CmsObject cms, String params) throws Exception {

        CmsMemoryMonitor mm = OpenCms.getMemoryMonitor();

        if (mm.m_initialized && (System.currentTimeMillis() - mm.m_lastRun) > mm.m_interval) {
            mm.logStatistics();
            mm.checkMemory();
            mm.m_lastRun = System.currentTimeMillis();
        }

        // check if we must send the regular email
        if (mm.m_initialized && (System.currentTimeMillis() - mm.m_lastEmail) > mm.m_emailInt) {
            mm.m_lastEmail = System.currentTimeMillis();
            mm.sendEmail();
        }

        return "";
    }

    /**
     * Sends an status email with OpenCms Memory information.<p>
     */
    private void sendEmail() {
        String from = m_emailSender;
        String[] to = m_emailReceiver;
        String subject = "OpenCms Status " + Utils.getNiceDate(System.currentTimeMillis());
        String content = "Memory usage of OpenCms at " + Utils.getNiceDate(System.currentTimeMillis()) + ":" + "Memory max: ," + (Runtime.getRuntime().maxMemory() / 1048576) + " ," + "total: ," + (Runtime.getRuntime().totalMemory() / 1048576) + " ," + "free: ," + (Runtime.getRuntime().freeMemory() / 1048576);

        try {
            if (from != null && to != null) {
                CmsMail email = new CmsMail(from, to, subject, content, "text/plain");
                email.start();
                m_emergencyMail = false;
            }
        } catch (CmsException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks the memory used by monitored caches.<p>
     * If the used memory exceeds a given percentage of the maximum memory,
     * caches are cleared and a garbage collection is requested
     */
    private void checkMemory() {

        long freeMemory = Runtime.getRuntime().freeMemory();
        long totalMemory = Runtime.getRuntime().totalMemory();
        long maxMemory = Runtime.getRuntime().maxMemory();
        long usedMemory = totalMemory - freeMemory;
        long usage = usedMemory * 100 / maxMemory;

        if ((m_maxUsage > 0) && (usage > m_maxUsage)) {

            String warning = "Memory usage of " + usage + "% exceeds max usage of " + m_maxUsage + "%, clearing caches.";
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn(warning);
            }

            OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_CLEAR_CACHES, Collections.EMPTY_MAP, false));
            System.gc();

            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn("" + "Memory max: ," + maxMemory / 1048576 + " ," + "total: ," + totalMemory / 1048576 + " ," + "free: ," + freeMemory / 1048576 + " ," + "used: ," + usedMemory / 1048576);

                // this is a critical situation, so send an emergency mail if this has not happend before
                if (!m_emergencyMail) {
                    m_emergencyMail = true;
                    String from = m_emailSender;
                    String[] to = m_emailReceiver;
                    String subject = "OpenCms Memory Warning " + Utils.getNiceDate(System.currentTimeMillis());
                    String content = warning + "Memory usage of OpenCms at " + Utils.getNiceDate(System.currentTimeMillis()) + ":" + "Memory max: ," + (Runtime.getRuntime().maxMemory() / 1048576) + " ," + "total: ," + (Runtime.getRuntime().totalMemory() / 1048576) + " ," + "free: ," + (Runtime.getRuntime().freeMemory() / 1048576);

                    try {
                        if (from != null && to != null) {
                            CmsMail email = new CmsMail(from, to, subject, content, "text/plain");
                            email.start();
                        }
                    } catch (CmsException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    /**
     * Logs the current memory statistics of the monitored objects.<p>
     */
    private void logStatistics() {

        if (!OpenCms.getLog(this).isDebugEnabled())
            return;

        OpenCms.getLog(this).debug(", " + "Memory max: ," + Runtime.getRuntime().maxMemory() / 1048576 + " ," + "total: ," + Runtime.getRuntime().totalMemory() / 1048576 + " ," + "free: ," + Runtime.getRuntime().freeMemory() / 1048576 + " ," + "used: ," + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576);

        for (Iterator keys = m_monitoredObjects.keySet().iterator(); keys.hasNext();) {

            String key = (String)keys.next();
            Object obj = m_monitoredObjects.get(key);

            OpenCms.getLog(this).debug(",,,,,,, " + "Monitored: ," + key + ", " + "Type: ," + obj.getClass().getName() + ", " + Integer.toHexString(obj.hashCode()) + ", " + "Limit: ," + getLimit(obj) + ", " + "Mapped: ," + getItems(obj) + ", " + "Costs: ," + getCosts(obj) + ", " + "Keys: ," + getKeySize(obj) + ", " + "Values: ," + getValueSize(obj));
        }
    }

    /**
     * Returns the max costs for all items within a monitored object.<p>
     * obj must be of type CmsLruCache, CmsLruHashMap
     * 
     * @param obj the object
     * @return max cost limit or "-"
     */
    private String getLimit(Object obj) {

        if (obj instanceof CmsLruCache)
            return Integer.toString(((CmsLruCache)obj).getMaxCacheCosts());

        if (obj instanceof LRUMap)
            return Integer.toString(((LRUMap)obj).getMaximumSize());

        return "-";
    }

    /**
     * Returns the number of items within a monitored object.<p>
     * obj must be of type CmsLruCache, CmsLruHashMap or Map
     * 
     * @param obj the object
     * @return the number of items or "-"
     */
    private String getItems(Object obj) {

        if (obj instanceof CmsLruCache)
            return Integer.toString(((CmsLruCache)obj).size());

        if (obj instanceof Map)
            return Integer.toString(((Map)obj).size());

        return "-";
    }

    /**
     * Returns the total size of key strings within a monitored object.<p>
     * obj must be of type map, the keys must be of type String.
     * 
     * @param obj the object
     * @return the total size of key strings
     */
    private String getKeySize(Object obj) {

        Map map = null;
        int keySize = 0;

        try {

            map = (Map)obj;
            if (map != null) {
                for (Iterator i = map.keySet().iterator(); i.hasNext();) {
                    String s = (String)i.next();
                    keySize += s.length();
                }
            }

        } catch (Exception exc) {
            keySize = -1;
        }

        if (keySize >= 0)
            return Integer.toString(keySize) + ", String size";
        else
            return "-, String size";
    }

    /**
     * Returns the value sizes of value objects within the monitored object.<p>
     * obj must be of type map
     * 
     * @param obj the object 
     * @return the value sizes of value objects or "-"-fields
     */
    private String getValueSize(Object obj) {

        Map map = null;
        int valueSize[] = { 0, 0, 0, 0, 0, 0 };
        int unresolved = 0;

        try {

            map = (Map)obj;
            if (map != null) {
                for (Iterator i = map.values().iterator(); i.hasNext();) {
                    Object value = i.next();
                    if (value instanceof I_CmsLruCacheObject)
                        value = ((I_CmsLruCacheObject)value).getValue();

                    if (value instanceof byte[]) {
                        valueSize[0] += ((byte[])value).length;
                        continue;
                    }

                    if (value instanceof String) {
                        valueSize[1] += ((String)value).length();
                        continue;
                    }

                    if (value instanceof List) {
                        valueSize[2] += ((List)value).size();
                        continue;
                    }

                    if (value instanceof Map) {
                        valueSize[3] += ((Map)value).size();
                        continue;
                    }

                    if (value instanceof CmsFile) {
                        CmsFile f = (CmsFile)value;
                        int l = f.getContents().length;
                        valueSize[4] += (l > 0) ? l : 1;
                        continue;
                    }

                    if (value instanceof CmsFolder || value instanceof CmsResource) {
                        valueSize[5] += 1;
                        continue;
                    }

                    unresolved++;
                }
            }
        } catch (Exception exc) {
            // noop
        }

        if (map != null)
            return Integer.toString(valueSize[0]) + ", byte[] size, " + Integer.toString(valueSize[1]) + ", String size, " + Integer.toString(valueSize[2]) + ", List items, " + Integer.toString(valueSize[3]) + ", Map items, " + Integer.toString(valueSize[4]) + ", CmsFiles, " + Integer.toString(valueSize[5]) + ", CmsResources/Folders, " + Integer.toString(unresolved) + ", unresolved";
        else
            return "-, byte[] size, " + "-, String size, " + "-, List items, " + "-, Map items, " + "-, CmsFiles, " + "-, CmsResources/Folders, " + "-, unresolved";
    }

    /**
     * Returns the cache costs of a monitored object.<p>
     * obj must be of type CmsLruCache 
     * 
     * @param obj the object
     * @return the cache costs or "-"
     */
    private String getCosts(Object obj) {

        if (obj instanceof CmsLruCache)
            return Integer.toString(((CmsLruCache)obj).getObjectCosts());

        return "-";
    }

}
