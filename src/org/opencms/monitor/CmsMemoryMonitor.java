/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/monitor/CmsMemoryMonitor.java,v $
 * Date   : $Date: 2005/06/13 10:00:02 $
 * Version: $Revision: 1.51 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.file.CmsFile;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.flex.CmsFlexCache.CmsFlexCacheVariation;
import org.opencms.mail.CmsMailTransport;
import org.opencms.mail.CmsSimpleMail;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsSessionManager;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.I_CmsScheduledJob;
import org.opencms.security.CmsAccessControlList;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.util.PrintfFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.mail.internet.InternetAddress;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;

/**
 * Monitors OpenCms memory consumtion.<p>
 * 
 * @version $Revision: 1.51 $ $Date: 2005/06/13 10:00:02 $
 * 
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 */
public class CmsMemoryMonitor implements I_CmsScheduledJob {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMemoryMonitor.class);
    
    /** Set interval for clearing the caches to 10 minutes. */
    private static final int C_INTERVAL_CLEAR = 1000 * 60 * 10;

    /** Maximum depth for object size recursion. */
    private static final int C_MAX_DEPTH = 5;

    /** Flag indicating if monitor is currently running. */
    private static boolean m_currentlyRunning;

    /** The memory monitor configuration. */
    private CmsMemoryMonitorConfiguration m_configuration;

    /** Interval in which emails are send. */
    private int m_intervalEmail;

    /** Interval in which the log is written. */
    private int m_intervalLog;

    /** Interval between 2 warnings. */
    private int m_intervalWarning;

    /** The time the caches were last cleared. */
    private long m_lastClearCache;

    /** The time the last status email was send. */
    private long m_lastEmailStatus;

    /** The time the last warning email was send. */
    private long m_lastEmailWarning;

    /** The time the last status log was written. */
    private long m_lastLogStatus;

    /** The time the last warning log was written. */
    private long m_lastLogWarning;

    /** The number of times the log entry was written. */
    private int m_logCount;

    /** Memory percentage to reach to go to warning level. */
    private int m_maxUsagePercent;

    /** Contains the object to be monitored. */
    private Map m_monitoredObjects;

    /** Flag for memory warning mail send. */
    private boolean m_warningLoggedSinceLastStatus;

    /** Flag for memory warning mail send. */
    private boolean m_warningSendSinceLastStatus;
    
    /** The average memory status. */
    private CmsMemoryStatus m_memoryAverage;
    
    /** The current memory status. */
    private CmsMemoryStatus m_memoryCurrent;
    
    /**
     * Empty constructor, required by OpenCms scheduler.<p>
     */
    public CmsMemoryMonitor() {

        m_monitoredObjects = new HashMap();
    }

    /**
     * Returns the size of objects that are instances of
     * <code>byte[]</code>, <code>String</code>, <code>CmsFile</code>,<code>I_CmsLruCacheObject</code>.<p>
     * For other objects, a size of 0 is returned.
     * 
     * @param obj the object
     * @return the size of the object 
     */
    public static int getMemorySize(Object obj) {

        if (obj instanceof I_CmsMemoryMonitorable) {
            return ((I_CmsMemoryMonitorable)obj).getMemorySize();
        }

        if (obj instanceof byte[]) {
            // will always be a total of 16 + 8
            return 8 + (int)(Math.ceil(((byte[])obj).length / 16.0) * 16.0);
        }

        if (obj instanceof String) {
            // will always be a total of 16 + 24
            return 24 + (int)(Math.ceil(((String)obj).length() / 8.0) * 16.0);
        }

        if (obj instanceof CmsFile) {
            CmsFile f = (CmsFile)obj;
            if (f.getContents() != null) {
                return f.getContents().length + 1024;
            } else {
                return 1024;
            }
        }

        if (obj instanceof CmsUUID) {
            return 184; // worst case if UUID String has been generated
        }

        if (obj instanceof CmsPermissionSet) {
            return 16; // two ints
        }

        if (obj instanceof CmsResource) {
            return 1024; // estimated size
        }

        if (obj instanceof CmsUser) {
            return 2048; // estimated size
        }

        if (obj instanceof CmsGroup) {
            return 512; // estimated size
        }

        if (obj instanceof CmsProject) {
            return 512;
        }

        if (obj instanceof Boolean) {
            return 8; // one boolean
        }
        
        if (obj instanceof CmsProperty) {
            int size = 8;
            
            CmsProperty property = (CmsProperty)obj;
            size += getMemorySize(property.getName());
            
            if (property.getResourceValue() != null) {
                size += getMemorySize(property.getResourceValue());
            }
            
            if (property.getStructureValue() != null) {
                size += getMemorySize(property.getStructureValue());
            }
            
            return size;
        }
        
        if (obj instanceof CmsPropertyDefinition) {
            int size = 8;
            
            CmsPropertyDefinition propDef = (CmsPropertyDefinition)obj;
            size += getMemorySize(propDef.getName());
            size += getMemorySize(propDef.getId());
            
            return size;
        }

        // System.err.println("Unresolved: " + obj.getClass().getName());
        return 8;
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
     * Returns the configuration.<p>
     *
     * @return the configuration
     */
    public CmsMemoryMonitorConfiguration getConfiguration() {

        return m_configuration;
    }

    /**
     * Returns the log count.<p>
     *
     * @return the log count
     */
    public int getLogCount() {

        return m_logCount;
    }

    /**
     * Initializes the monitor with the provided configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    public void initialize(CmsMemoryMonitorConfiguration configuration) {

        m_memoryAverage = new CmsMemoryStatus();
        m_memoryCurrent = new CmsMemoryStatus();
        
        m_warningSendSinceLastStatus = false;
        m_warningLoggedSinceLastStatus = false;
        m_lastEmailWarning = 0;
        m_lastEmailStatus = 0;
        m_lastLogStatus = 0;
        m_lastLogWarning = 0;
        m_lastClearCache = 0;
        m_configuration = configuration;

        m_intervalWarning = 720 * 60000;
        m_maxUsagePercent = 90;

        m_intervalEmail = m_configuration.getEmailInterval() * 1000;
        m_intervalLog = m_configuration.getLogInterval() * 1000;

        if (m_configuration.getWarningInterval() > 0) {
            m_intervalWarning = m_configuration.getWarningInterval();
        }
        m_intervalWarning *= 1000;

        if (m_configuration.getMaxUsagePercent() > 0) {
            m_maxUsagePercent = m_configuration.getMaxUsagePercent();
        }

        if (CmsLog.INIT.isInfoEnabled()) {
            
            CmsLog.INIT.info(Messages.get().key(Messages.LOG_MM_INTERVAL_LOG_1, new Integer(m_intervalLog / 1000)));
            CmsLog.INIT.info(Messages.get().key(Messages.LOG_MM_INTERVAL_EMAIL_1, new Integer(m_intervalEmail / 1000)));
            CmsLog.INIT.info(Messages.get().key(Messages.LOG_MM_INTERVAL_WARNING_1, new Integer(m_intervalWarning / 1000)));
            CmsLog.INIT.info(Messages.get().key(Messages.LOG_MM_INTERVAL_MAX_USAGE_1, new Integer(m_intervalWarning / 1000)));

            if ((m_configuration.getEmailReceiver() == null) || (m_configuration.getEmailSender() == null)) {
                CmsLog.INIT.info(Messages.get().key(Messages.LOG_MM_EMAIL_DISABLED_0));
            } else {
                CmsLog.INIT.info(Messages.get().key(Messages.LOG_MM_EMAIL_SENDER_1, m_configuration.getEmailSender()));
                Iterator i = m_configuration.getEmailReceiver().iterator();
                int n = 0;
                while (i.hasNext()) {
                    CmsLog.INIT.info(Messages.get().key(Messages.LOG_MM_EMAIL_RECEIVER_2, new Integer(n+1), i.next()));
                    n++;
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            // this will happen only once during system startup
            
            LOG.debug(Messages.get().key(Messages.LOG_MM_CREATED_1, new Date(System.currentTimeMillis())));
        }

    }

    /**
     * @see org.opencms.scheduler.I_CmsScheduledJob#launch(CmsObject, Map)
     */
    public String launch(CmsObject cms, Map parameters) throws Exception {

        CmsMemoryMonitor monitor = OpenCms.getMemoryMonitor();

        // make sure job is not launched twice
        if (m_currentlyRunning) {
            return null;
        }         

        try {
            m_currentlyRunning = true;
            
            // update the memory status
            monitor.updateStatus();
            
            // check if the system is in a low memory condition
            if (monitor.lowMemory()) {
                // log warning
                monitor.monitorWriteLog(true);
                // send warning email
                monitor.monitorSendEmail(true);
                // clean up caches     
                monitor.clearCaches();
            }
    
            // check if regular a log entry must be written
            if ((System.currentTimeMillis() - monitor.m_lastLogStatus) > monitor.m_intervalLog) {
                monitor.monitorWriteLog(false);
            }
    
            // check if the memory status email must be send
            if ((System.currentTimeMillis() - monitor.m_lastEmailStatus) > monitor.m_intervalEmail) {
                monitor.monitorSendEmail(false);
            }
        } finally {            
            // make sure state is reset even if an error occurs, 
            // otherwise MM will not be executed after an error
            m_currentlyRunning = false;
        }
        
        return null;
    }

    /**
     * Updatres the memory information of the memory monitor.<p> 
     */
    private void updateStatus() {
        
        m_memoryCurrent.update();
        m_memoryAverage.calculateAverage(m_memoryCurrent);        
    }
    
    /**
     * Returns true if the system runs low on memory.<p>
     * 
     * @return true if the system runs low on memory
     */
    public boolean lowMemory() {

        return ((m_maxUsagePercent > 0) && (m_memoryCurrent.getUsage() > m_maxUsagePercent));
    }

    /**
     * Adds a new object to the monitor.<p>
     * 
     * @param objectName name of the object
     * @param object the object for monitoring
     */
    public void register(String objectName, Object object) {

        m_monitoredObjects.put(objectName, object);
    }

    /**
     * Clears the OpenCms caches.<p> 
     */
    private void clearCaches() {

        if ((m_lastClearCache + C_INTERVAL_CLEAR) > System.currentTimeMillis()) {
            // if the cache has already been cleared less then 15 minutes ago we skip this because 
            // clearing the caches to often will hurt system performance and the 
            // setup seems to be in trouble anyway
            return;
        }
        m_lastClearCache = System.currentTimeMillis();
        if (LOG.isWarnEnabled()) {
            LOG.warn(Messages.get().key(Messages.LOG_CLEAR_CACHE_MEM_CONS_0));
        } 
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, Collections.EMPTY_MAP));
        System.gc();
    }

    /**
     * Returns the cache costs of a monitored object.<p>
     * obj must be of type CmsLruCache 
     * 
     * @param obj the object
     * @return the cache costs or "-"
     */
    private long getCosts(Object obj) {

        long costs = 0;
        if (obj instanceof CmsLruCache) {
            costs = ((CmsLruCache)obj).getObjectCosts();
            if (costs < 0) {
                costs = 0;
            }
        }

        return costs;
    }

    /**
     * Returns the number of items within a monitored object.<p>
     * obj must be of type CmsLruCache, CmsLruHashMap or Map
     * 
     * @param obj the object
     * @return the number of items or "-"
     */
    private String getItems(Object obj) {

        if (obj instanceof CmsLruCache) {
            return Integer.toString(((CmsLruCache)obj).size());
        }
        if (obj instanceof Map) {
            return Integer.toString(((Map)obj).size());
        }
        return "-";
    }

    /**
     * Returns the total size of key strings within a monitored map.<p>
     * the keys must be of type String.
     * 
     * @param map the map
     * @param depth the max recursion depth for calculation the size
     * @return total size of key strings
     */
    private long getKeySize(Map map, int depth) {

        long keySize = 0;
        try {
            Object[] values = map.values().toArray();
            for (int i = 0, s = values.length; i < s; i++) {

                Object obj = values[i];

                if (obj instanceof Map && depth < C_MAX_DEPTH) {
                    keySize += getKeySize((Map)obj, depth + 1);
                    continue;
                }
            }
            values = null;

            Object[] keys = map.keySet().toArray();
            for (int i = 0, s = keys.length; i < s; i++) {

                Object obj = keys[i];

                if (obj instanceof String) {
                    String st = (String)obj;
                    keySize += (st.length() * 2);
                }
            }
        } catch (ConcurrentModificationException e) {
            // this might happen since even the .toArray() method internally creates an iterator
        } catch (Throwable t) {
            // catch all other exceptions otherwise the whole monitor will stop working
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.LOG_CAUGHT_THROWABLE_1, t.getMessage()));
            }
        }

        return keySize;
    }

    /**
     * Returns the total size of key strings within a monitored object.<p>
     * obj must be of type map, the keys must be of type String.
     * 
     * @param obj the object
     * @return the total size of key strings
     */
    private long getKeySize(Object obj) {

        if (obj instanceof Map) {
            return getKeySize((Map)obj, 1);
        }

        return 0;
    }

    /**
     * Returns the max costs for all items within a monitored object.<p>
     * obj must be of type CmsLruCache, CmsLruHashMap
     * 
     * @param obj the object
     * @return max cost limit or "-"
     */
    private String getLimit(Object obj) {

        if (obj instanceof CmsLruCache) {
            return Integer.toString(((CmsLruCache)obj).getMaxCacheCosts());
        }
        if (obj instanceof LRUMap) {
            return Integer.toString(((LRUMap)obj).maxSize());
        }

        return "-";
    }

    /**
     * Returns the total value size of a list object.<p>
     * 
     * @param listValue the list object
     * @param depth the max recursion depth for calculation the size
     * @return the size of the list object
     */
    private long getValueSize(List listValue, int depth) {

        long totalSize = 0;
        try {
            Object[] values = listValue.toArray();
            for (int i = 0, s = values.length; i < s; i++) {

                Object obj = values[i];

                if (obj instanceof CmsAccessControlList) {
                    obj = ((CmsAccessControlList)obj).getPermissionMap();
                }

                if (obj instanceof CmsFlexCacheVariation) {
                    obj = ((CmsFlexCacheVariation)obj).m_map;
                }

                if (obj instanceof Map && depth < C_MAX_DEPTH) {
                    totalSize += getValueSize((Map)obj, depth + 1);
                    continue;
                }

                if (obj instanceof List && depth < C_MAX_DEPTH) {
                    totalSize += getValueSize((List)obj, depth + 1);
                    continue;
                }

                totalSize += getMemorySize(obj);
            }
        } catch (ConcurrentModificationException e) {
            // this might happen since even the .toArray() method internally creates an iterator
        } catch (Throwable t) {
            // catch all other exceptions otherwise the whole monitor will stop working
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.LOG_CAUGHT_THROWABLE_1, t.getMessage()));
            }
        }

        return totalSize;
    }

    /**
     * Returns the total value size of a map object.<p>
     * 
     * @param mapValue the map object
     * @param depth the max recursion depth for calculation the size
     * @return the size of the map object
     */
    private long getValueSize(Map mapValue, int depth) {

        long totalSize = 0;
        try {
            Object[] values = mapValue.values().toArray();
            for (int i = 0, s = values.length; i < s; i++) {

                Object obj = values[i];

                if (obj instanceof CmsAccessControlList) {
                    obj = ((CmsAccessControlList)obj).getPermissionMap();
                }

                if (obj instanceof CmsFlexCacheVariation) {
                    obj = ((CmsFlexCacheVariation)obj).m_map;
                }

                if (obj instanceof Map && depth < C_MAX_DEPTH) {
                    totalSize += getValueSize((Map)obj, depth + 1);
                    continue;
                }

                if (obj instanceof List && depth < C_MAX_DEPTH) {
                    totalSize += getValueSize((List)obj, depth + 1);
                    continue;
                }

                totalSize += getMemorySize(obj);
            }
        } catch (ConcurrentModificationException e) {
            // this might happen since even the .toArray() method internally creates an iterator
        } catch (Throwable t) {
            // catch all other exceptions otherwise the whole monitor will stop working
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.LOG_CAUGHT_THROWABLE_1, t.getMessage()));
            }
        }

        return totalSize;
    }

    /**
     * Returns the value sizes of value objects within the monitored object.<p>
     * obj must be of type map
     * 
     * @param obj the object 
     * @return the value sizes of value objects or "-"-fields
     */
    private long getValueSize(Object obj) {

        if (obj instanceof CmsLruCache) {
            return ((CmsLruCache)obj).size();
        }

        if (obj instanceof Map) {
            return getValueSize((Map)obj, 1);
        }

        if (obj instanceof List) {
            return getValueSize((List)obj, 1);
        }

        try {
            return getMemorySize(obj);
        } catch (Exception exc) {
            return 0;
        }
    }

    /**
     * Sends a warning or status email with OpenCms Memory information.<p>
     * 
     * @param warning if true, send a memory warning email 
     */
    private void monitorSendEmail(boolean warning) {

        if ((m_configuration.getEmailSender() == null) || (m_configuration.getEmailReceiver() == null)) {
            // send no mails if not fully configured
            return;
        } else if (warning 
            && (m_warningSendSinceLastStatus 
                && !((m_intervalEmail <= 0) 
                    && (System.currentTimeMillis() < (m_lastEmailWarning + m_intervalWarning))))) {
            // send no warning email if no status email has been send since the last warning
            // if status is disabled, send no warn email if warn interval has not passed
            return;
        } else if ((!warning) && (m_intervalEmail <= 0)) {
            // if email iterval is <= 0 status email is disabled
            return;
        }
        String date = CmsDateUtil.getDateTimeShort(System.currentTimeMillis());
        String subject;
        String content = "";
        if (warning) {
            m_warningSendSinceLastStatus = true;
            m_lastEmailWarning = System.currentTimeMillis();
            subject = "OpenCms Memory W A R N I N G ["
                + OpenCms.getSystemInfo().getServerName().toUpperCase()
                + "/"
                + date
                + "]";
            content += "W A R N I N G !\nOpenCms memory consumption on server "
                + OpenCms.getSystemInfo().getServerName().toUpperCase()
                + " has reached a critical level !\n\n"
                + "The configured limit is "
                + m_maxUsagePercent
                + "%\n\n";
        } else {
            m_warningSendSinceLastStatus = false;
            m_lastEmailStatus = System.currentTimeMillis();
            subject = "OpenCms Memory Status ["
                + OpenCms.getSystemInfo().getServerName().toUpperCase()
                + "/"
                + date
                + "]";
        }

        content += "Memory usage report of OpenCms server "
            + OpenCms.getSystemInfo().getServerName().toUpperCase()
            + " at "
            + date
            + "\n\n"
            + "Memory maximum heap size: "
            + m_memoryCurrent.getMaxMemory()
            + " mb\n"
            + "Memory current heap size: "
            + m_memoryCurrent.getTotalMemory()
            + " mb\n\n"
            + "Memory currently used   : "
            + m_memoryCurrent.getUsedMemory()
            + " mb ("
            + m_memoryCurrent.getUsage()
            + "%)\n"
            + "Memory currently unused : "
            + m_memoryCurrent.getFreeMemory()
            + " mb\n\n\n";

        if (warning) {
            content += "*** Please take action NOW to ensure that no OutOfMemoryException occurs.\n\n\n";
        }

        CmsSessionManager sm = OpenCms.getSessionManager();

        if (sm != null) {
            content += "Current status of the sessions:\n\n";
            content += "Logged in users          : " + sm.getSessionCountAuthenticated() + "\n";
            content += "Currently active sessions: " + sm.getSessionCountCurrent() + "\n";
            content += "Total created sessions   : " + sm.getSessionCountTotal() + "\n\n\n";
        }

        sm = null;

        content += "Current status of the caches:\n\n";
        List keyList = Arrays.asList(m_monitoredObjects.keySet().toArray());
        Collections.sort(keyList);
        long totalSize = 0;
        for (Iterator keys = keyList.iterator(); keys.hasNext();) {
            String key = (String)keys.next();
            String[] shortKeys = key.split("\\.");
            String shortKey = shortKeys[shortKeys.length - 2] + '.' + shortKeys[shortKeys.length - 1];
            PrintfFormat form = new PrintfFormat("%9s");
            Object obj = m_monitoredObjects.get(key);

            long size = getKeySize(obj) + getValueSize(obj) + getCosts(obj);
            totalSize += size;

            content += new PrintfFormat("%-42.42s").sprintf(shortKey)
                + "  "
                + "Entries: "
                + form.sprintf(getItems(obj))
                + "   "
                + "Limit: "
                + form.sprintf(getLimit(obj))
                + "   "
                + "Size: "
                + form.sprintf(Long.toString(size))
                + "\n";
        }
        content += "\nTotal size of cache memory monitored: " + totalSize + " (" + totalSize / 1048576 + ")\n\n";

        String from = m_configuration.getEmailSender();
        List receivers = new ArrayList();
        List receiverEmails = m_configuration.getEmailReceiver();
        try {
            if (from != null && receiverEmails != null && !receiverEmails.isEmpty()) {
                Iterator i = receiverEmails.iterator();
                while (i.hasNext()) {
                    receivers.add(new InternetAddress((String)i.next()));
                }
                CmsSimpleMail email = new CmsSimpleMail();
                email.setFrom(from);
                email.setTo(receivers);
                email.setSubject(subject);
                email.setMsg(content);
                new CmsMailTransport(email).send();
            }
            if (LOG.isInfoEnabled()) {
                if (warning) {
                    LOG.info(Messages.get().key(Messages.LOG_MM_WARNING_EMAIL_SENT_0));
                } else {
                    LOG.info(Messages.get().key(Messages.LOG_MM_STATUS_EMAIL_SENT_0));
                }    
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Write a warning or status log entry with OpenCms Memory information.<p>
     * 
     * @param warning if true, write a memory warning log entry 
     */
    private void monitorWriteLog(boolean warning) {
        
        if (!LOG.isWarnEnabled()) {
            // we need at last warn level for this output
            return;
        } else if ((!warning) && (!LOG.isInfoEnabled())) {
            // if not warning we need info level
            return;
        } else if (warning
            && (m_warningLoggedSinceLastStatus 
                && !(((m_intervalLog <= 0) 
                    && (System.currentTimeMillis() < (m_lastLogWarning + m_intervalWarning)))))) {
            // write no warning log if no status log has been written since the last warning
            // if status is disabled, log no warn entry if warn interval has not passed
            return;
        } else if ((!warning) && (m_intervalLog <= 0)) {
            // if log iterval is <= 0 status log is disabled
            return;
        }

        if (warning) {
            m_lastLogWarning = System.currentTimeMillis();
            m_warningLoggedSinceLastStatus = true;
            LOG.warn(Messages.get().key(Messages.LOG_MM_WARNING_MEM_CONSUME_2, new Long(m_memoryCurrent.getUsage()), 
                new Integer(m_maxUsagePercent)));
        } else {
            m_warningLoggedSinceLastStatus = false;
            m_lastLogStatus = System.currentTimeMillis();
        }

        if (warning) {
            LOG.warn(Messages.get().key(Messages.LOG_MM_WARNING_MEM_STATUS_6, new Object[] {
                new Long(m_memoryCurrent.getMaxMemory()),
                new Long(m_memoryCurrent.getTotalMemory()),
                new Long(m_memoryCurrent.getFreeMemory()),
                new Long(m_memoryCurrent.getUsedMemory()), 
                new Long(m_memoryCurrent.getUsage()),
                new Integer(m_maxUsagePercent)}));
        } else {

            m_logCount++;
            LOG.info(Messages.get().key(Messages.LOG_MM_LOG_INFO_2, 
                OpenCms.getSystemInfo().getServerName().toUpperCase(), String.valueOf(m_logCount)));

            List keyList = Arrays.asList(m_monitoredObjects.keySet().toArray());
            Collections.sort(keyList);
            long totalSize = 0;
            for (Iterator keys = keyList.iterator(); keys.hasNext();) {

                String key = (String)keys.next();
                Object obj = m_monitoredObjects.get(key);

                long size = getKeySize(obj) + getValueSize(obj) + getCosts(obj);
                totalSize += size;

                PrintfFormat name1 = new PrintfFormat("%-80s");
                PrintfFormat name2 = new PrintfFormat("%-50s");
                PrintfFormat form = new PrintfFormat("%9s");
                LOG.info(Messages.get().key(Messages.LOG_MM_NOWARN_STATUS_5, new Object[] {
                    name1.sprintf(key), 
                    name2.sprintf(obj.getClass().getName()),
                    form.sprintf(getItems(obj)), 
                    form.sprintf(getLimit(obj)),
                    form.sprintf(Long.toString(size))}));
            }
            
            LOG.info(
                Messages.get().key(Messages.LOG_MM_WARNING_MEM_STATUS_6, new Object[] {
                    new Long(m_memoryCurrent.getMaxMemory()),
                    new Long(m_memoryCurrent.getTotalMemory()),
                    new Long(m_memoryCurrent.getFreeMemory()),
                    new Long(m_memoryCurrent.getUsedMemory()), 
                    new Long(m_memoryCurrent.getUsage()),
                    new Integer(m_maxUsagePercent), 
                    new Long(totalSize),
                    new Long(totalSize / 1048576)
                })

            );
            LOG.info(Messages.get().key(Messages.LOG_MM_WARNING_MEM_STATUS_AVG_6, new Object[] {
                new Long(m_memoryAverage.getMaxMemory()),
                new Long(m_memoryAverage.getTotalMemory()),
                new Long(m_memoryAverage.getFreeMemory()),
                new Long(m_memoryAverage.getUsedMemory()), 
                new Long(m_memoryAverage.getUsage()),
                new Integer(m_memoryAverage.getCount())}));

            CmsSessionManager sm = OpenCms.getSessionManager();

            if (sm != null) {
                LOG.info(Messages.get().key(Messages.LOG_MM_SESSION_STAT_3, 
                    String.valueOf(sm.getSessionCountAuthenticated()), String.valueOf(sm.getSessionCountCurrent()), 
                    String.valueOf(sm.getSessionCountTotal())));
            }
            sm = null;
            
            LOG.info(Messages.get().key(Messages.LOG_MM_STARTUP_TIME_2, 
                CmsDateUtil.getDateTimeShort(OpenCms.getSystemInfo().getStartupTime()), 
                CmsStringUtil.formatRuntime(OpenCms.getSystemInfo().getRuntime())));            
        }
    }
}
