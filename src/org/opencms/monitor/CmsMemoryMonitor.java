/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/monitor/CmsMemoryMonitor.java,v $
 * Date   : $Date: 2004/06/07 12:44:05 $
 * Version: $Revision: 1.26 $
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
import org.opencms.cron.I_CmsCronJob;
import org.opencms.flex.CmsFlexCache.CmsFlexCacheVariation;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsSessionInfoManager;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.main.OpenCmsCore;
import org.opencms.main.OpenCmsSessionManager;
import org.opencms.security.CmsAccessControlList;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsMail;
import org.opencms.util.CmsUUID;
import org.opencms.util.PrintfFormat;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;

import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.collections.map.LRUMap;

/**
 * Monitors OpenCms memory consumtion.<p>
 * 
 * @version $Revision: 1.26 $ $Date: 2004/06/07 12:44:05 $
 * 
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 */
public class CmsMemoryMonitor implements I_CmsCronJob {
    
    /** set interval for clearing the caches to 10 minutes */
    private static final int C_INTERVAL_CLEAR = 1000 * 60 * 10;
    
    /** max depth for object size recursion */
    private static final int C_MAX_DEPTH = 5;

    private static boolean m_currentlyRunning = false;
    
    /** receivers fro status emails */
    private String[] m_emailReceiver;

    /** sender for status emails */
    private String m_emailSender;

    /** the interval to use for sending emails */
    private int m_intervalEmail;

    /** the interval to use for the logging */
    private int m_intervalLog;

    /** the interval to use for warnings if status is disabled */
    private int m_intervalWarning;
    
    /** the time the caches where last cleared */
    private long m_lastClearCache;    
    
    /** the time the last status email was send */
    private long m_lastEmailStatus;

    /** the time the last warning email was send */
    private long m_lastEmailWarning;
    
    /** the time the last status log was written */
    private long m_lastLogStatus;
    
    /** the time the last warning log was written */
    private long m_lastLogWarning;    

    /** memory limit that triggers a warning */
    private int m_maxUsagePercent;

    /** contains the object to be monitored */
    private Map m_monitoredObjects;

    /** flag for memory warning mail send */
    private boolean m_warningSendSinceLastStatus;
    
    /** flag for memory warning mail send */
    private boolean m_warningLoggedSinceLastStatus;
    
    /**
     * Empty constructor, required by OpenCms scheduler.<p>
     */
    public CmsMemoryMonitor() {
        // empty
    }
    
    /**
     * Creates a new monitor with the provided configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    public CmsMemoryMonitor(ExtendedProperties configuration) {
        m_warningSendSinceLastStatus = false;
        m_warningLoggedSinceLastStatus = false;
        m_lastEmailWarning = 0;
        m_lastEmailStatus = 0;       
        m_lastLogStatus = 0;
        m_lastLogWarning = 0;        
        m_lastClearCache = 0;
        m_monitoredObjects = new HashMap();
        
        m_emailSender = configuration.getString("memorymonitor.email.sender", null);
        m_emailReceiver = configuration.getStringArray("memorymonitor.email.receiver");
        m_intervalEmail = configuration.getInteger("memorymonitor.email.interval", 0) * 60000;
        m_intervalLog = configuration.getInteger("memorymonitor.log.interval", 0) * 60000;
        m_intervalWarning = configuration.getInteger("memorymonitor.warning.interval", 360) * 60000;
        m_maxUsagePercent = configuration.getInteger("memorymonitor.maxUsagePercent", 90);
        
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". MM interval log      : " + (m_intervalLog / 60000) + " min");
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". MM interval email    : " + (m_intervalEmail / 60000) + " min");
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". MM interval warning  : " + (m_intervalWarning / 60000) + " min");
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". MM max usage         : " + m_maxUsagePercent + "%");
            if ((m_emailReceiver == null) || (m_emailSender == null)) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". MM email             : disabled");
            } else {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". MM email sender      : " + m_emailSender);
                for (int i=0, s=m_emailReceiver.length; i < s; i++) {
                    OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". MM email receiver    : " + (i+1) + " - " + m_emailReceiver[i]);                    
                }
            }
        }  
        
        if (OpenCms.getLog(this).isDebugEnabled()) {
            // this will happen only once during system startup
            OpenCms.getLog(this).debug(", New instance of CmsMemoryMonitor created at " + (new Date(System.currentTimeMillis())));
        }
        
    }

    /**
     * Initalizes the Memory Monitor.<p>
     * 
     * @param configuration the OpenCms configurations
     * @return the initialized CmsMemoryMonitor 
     */
    public static CmsMemoryMonitor initialize(ExtendedProperties configuration) {
        return new CmsMemoryMonitor(configuration);
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
        if (OpenCms.getLog(this).isWarnEnabled()) {
            OpenCms.getLog(this).warn(", Clearing caches because memory consumption has reached a critical level");
        }        
        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_CLEAR_CACHES, Collections.EMPTY_MAP, false));
        System.gc();       
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
     * Returns the total size of key strings within a monitored object.<p>
     * obj must be of type map, the keys must be of type String.
     * 
     * @param obj the object
     * @return the total size of key strings
     */
    private long getKeySize(Object obj) {
        
        if (obj instanceof Map) {
            return getKeySize ((Map)obj, 1);
        }
        
        return 0;
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
            for (int i=0, s=values.length; i<s; i++) {
                
                Object obj = values[i];
                
                if (obj instanceof Map && depth < C_MAX_DEPTH) {
                    keySize += getKeySize((Map)obj, depth+1);
                    continue;
                }
            }
            values = null;
            
            Object[] keys = map.keySet().toArray();           
            for (int i=0, s=keys.length; i<s; i++) {
                
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
            if (OpenCms.getLog(this).isDebugEnabled()) {
                OpenCms.getLog(this).debug("Caught throwable "+t.getMessage());
            } 
        }
        
        return keySize;
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
            for (int i=0, s=values.length; i<s; i++) {
                
                Object obj = values[i];
                
                if (obj instanceof CmsAccessControlList) {
                    obj = ((CmsAccessControlList)obj).getPermissionMap();
                }
                
                if (obj instanceof CmsFlexCacheVariation) {
                    obj = ((CmsFlexCacheVariation)obj).m_map;
                }
                
                if (obj instanceof Map && depth < C_MAX_DEPTH) {
                    totalSize += getValueSize((Map)obj, depth+1);
                    continue;
                }
                
                if (obj instanceof List && depth < C_MAX_DEPTH) {
                    totalSize += getValueSize((List)obj, depth+1);
                    continue;
                }
                
                totalSize += getMemorySize(obj);
            }
        } catch (ConcurrentModificationException e) {
            // this might happen since even the .toArray() method internally creates an iterator
        } catch (Throwable t) {
            // catch all other exceptions otherwise the whole monitor will stop working
            if (OpenCms.getLog(this).isDebugEnabled()) {
                OpenCms.getLog(this).debug("Caught throwable "+t.getMessage());
            } 
        }
        
        return totalSize;
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
            for (int i=0, s=values.length; i<s; i++) {
                
                Object obj = values[i];
                
                if (obj instanceof CmsAccessControlList) {
                    obj = ((CmsAccessControlList)obj).getPermissionMap();
                }
                
                if (obj instanceof CmsFlexCacheVariation) {
                    obj = ((CmsFlexCacheVariation)obj).m_map;
                }
                
                if (obj instanceof Map && depth < C_MAX_DEPTH) {
                    totalSize += getValueSize((Map)obj, depth+1);
                    continue;
                }
                
                if (obj instanceof List && depth < C_MAX_DEPTH) {
                    totalSize += getValueSize((List)obj, depth+1);
                    continue;
                }
                
                totalSize += getMemorySize(obj);
            }
        } catch (ConcurrentModificationException e) {
            // this might happen since even the .toArray() method internally creates an iterator
        } catch (Throwable t) {
            // catch all other exceptions otherwise the whole monitor will stop working
            if (OpenCms.getLog(this).isDebugEnabled()) {
                OpenCms.getLog(this).debug("Caught throwable "+t.getMessage());
            } 
        }
              
        return totalSize;
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
        
        // System.err.println("Unresolved: " + obj.getClass().getName());
        return 8;
    }
    
    /**a
     * @see org.opencms.cron.I_CmsCronJob#launch(org.opencms.file.CmsObject, java.lang.String)
     */
    public String launch(CmsObject cms, String params) throws Exception {
        
        CmsMemoryMonitor monitor = OpenCms.getMemoryMonitor();

        if (m_currentlyRunning) {
            return "";
        } else {
            m_currentlyRunning = true;
        }
        
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

        m_currentlyRunning = false;
        return "";
    }
    
    /**
     * Returns true if the system runs low on memory.<p>
     * 
     * @return true if the system runs low on memory
     */
    public boolean lowMemory() {
        long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long usage = usedMemory * 100 / Runtime.getRuntime().maxMemory();
        return ((m_maxUsagePercent > 0) && (usage > m_maxUsagePercent));
    }

    /**
     * Sends a warning or status email with OpenCms Memory information.<p>
     * 
     * @param warning if true, send a memory warning email 
     */
    private void monitorSendEmail(boolean warning) {
        if ((m_emailSender == null) || (m_emailReceiver == null)) {
            // send no mails if not fully configured
            return;
        } else if (warning && (m_warningSendSinceLastStatus && !((m_intervalEmail <= 0) && (System.currentTimeMillis() < (m_lastEmailWarning + m_intervalWarning))))) {
            // send no warning email if no status email has been send since the last warning
            // if status is disabled, send no warn email if warn interval has not passed
            return;
        } else if ((! warning) && (m_intervalEmail <= 0)) {
            // if email iterval is <= 0 status email is disabled
            return;
        }
        String date = CmsDateUtil.getDateTimeShort(System.currentTimeMillis());
        String subject;
        String content = "";
        if (warning) {
            m_warningSendSinceLastStatus = true;
            m_lastEmailWarning = System.currentTimeMillis(); 
            subject = "OpenCms Memory W A R N I N G [" + OpenCms.getSystemInfo().getServerName().toUpperCase() + "/" + date + "]";
            content += "W A R N I N G !\nOpenCms memory consumption on server " + OpenCms.getSystemInfo().getServerName().toUpperCase() + " has reached a critical level !\n\n"
                    + "The configured limit is " + m_maxUsagePercent + "%\n\n";
        } else {
            m_warningSendSinceLastStatus = false;
            m_lastEmailStatus = System.currentTimeMillis();
            subject = "OpenCms Memory Status [" + OpenCms.getSystemInfo().getServerName().toUpperCase() + "/" + date + "]";
        }
        
        long maxMemory = Runtime.getRuntime().maxMemory() / 1048576;
        long totalMemory = Runtime.getRuntime().totalMemory() / 1048576;
        long usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
        long freeMemory = maxMemory - usedMemory;
        long usage = usedMemory * 100 / maxMemory;
        
        content += "Memory usage report of OpenCms server " + OpenCms.getSystemInfo().getServerName().toUpperCase() + " at " + date + "\n\n" 
            + "Memory maximum heap size: " + maxMemory + " mb\n" 
            + "Memory current heap size: " + totalMemory + " mb\n\n" 
            + "Memory currently used   : " + usedMemory + " mb (" + usage + "%)\n"
            + "Memory currently unused : " + freeMemory + " mb\n\n\n";

        if (warning) {
            content += "*** Please take action NOW to ensure that no OutOfMemoryException occurs.\n\n\n";
        }
        
        OpenCmsSessionManager sm = OpenCmsCore.getInstance().getSessionManager();
        CmsSessionInfoManager cs = OpenCms.getSessionInfoManager();
        
        if ((sm != null) && (cs != null)) {
            content += "Current status of the sessions:\n\n";
            content += "Logged in users          : " + cs.getLoggedInUsers().size() + "\n";
            content += "Currently active sessions: " + sm.getCurrentSessions() + "\n";
            content += "Total created sessions   : " + sm.getTotalSessions() + "\n\n\n";
        }
        
        sm = null;
        cs = null;
        
        content += "Current status of the caches:\n\n";        
        List keyList = Arrays.asList(m_monitoredObjects.keySet().toArray());
        Collections.sort(keyList);
        long totalSize = 0;
        for (Iterator keys = keyList.iterator(); keys.hasNext();) {
            String key = (String)keys.next();        
            String shortKeys[] = key.split("\\.");
            String shortKey = shortKeys[shortKeys.length - 2] + "." + shortKeys[shortKeys.length - 1];
            PrintfFormat form = new PrintfFormat("%9s");
            Object obj = m_monitoredObjects.get(key);
            
            long size = getKeySize(obj) + getValueSize(obj) + getCosts(obj);
            totalSize += size;
            
            content += new PrintfFormat("%-42.42s").sprintf(shortKey) + "  " 
                    + "Entries: " + form.sprintf(getItems(obj)) + "   " 
                    + "Limit: " + form.sprintf(getLimit(obj)) + "   "
                    + "Size: " + form.sprintf(Long.toString(size)) + "\n";
        }
        content += "\nTotal size of cache memory monitored: " + totalSize + " (" + totalSize / 1048576 + ")\n\n";
        
        String from = m_emailSender;
        String[] to = m_emailReceiver;        
        try {
            if (from != null && to != null) {
                CmsMail email = new CmsMail(from, to, subject, content, "text/plain");
                email.start();                
            }            
            if (OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info(", Memory Monitor " + (warning?"warning":"status") + " email send");
            }
        } catch (CmsException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Write a warning or status log entry with OpenCms Memory information.<p>
     * 
     * @param warning if true, write a memory warning log entry 
     */
    private void monitorWriteLog(boolean warning) {
        if (! OpenCms.getLog(this).isWarnEnabled()) {
            // we need at last warn level for this output
            return;
        } else if ((! warning) && (! OpenCms.getLog(this).isDebugEnabled())) {
            // if not warning we need debug level
            return;
        } else if (warning && (m_warningLoggedSinceLastStatus && !(((m_intervalLog <= 0) && (System.currentTimeMillis() < (m_lastLogWarning + m_intervalWarning)))))) {
            // write no warning log if no status log has been written since the last warning
            // if status is disabled, log no warn entry if warn interval has not passed
            return;
        } else if ((! warning) && (m_intervalLog <= 0)) {
            // if log iterval is <= 0 status log is disabled
            return;
        }
        
        long maxMemory = Runtime.getRuntime().maxMemory() / 1048576;
        long totalMemory = Runtime.getRuntime().totalMemory() / 1048576;
        long usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
        long freeMemory = maxMemory - usedMemory;
        long usage = usedMemory * 100 / maxMemory;
        
        if (warning) {
            m_lastLogWarning = System.currentTimeMillis();
            m_warningLoggedSinceLastStatus = true;
            OpenCms.getLog(this).warn(", W A R N I N G Memory consumption of " + usage 
                 + "% has reached a critical level" 
                 + " (" + m_maxUsagePercent + "% configured)");
        } else {
            m_warningLoggedSinceLastStatus = false;
            m_lastLogStatus = System.currentTimeMillis();
        }

        String memStatus = ", Memory max: " + maxMemory + " mb  " 
            + "total: " + totalMemory + " mb  " 
            + "free: " + freeMemory + " mb  " 
            + "used: " + usedMemory + " mb  " 
            + "percent: " + usage + "%  " 
            + "limit: " + m_maxUsagePercent + "%  ";
                
        if (warning) {
            OpenCms.getLog(this).warn(memStatus);
        } else {            
            
            OpenCms.getLog(this).debug(", Memory monitor log for server " + OpenCms.getSystemInfo().getServerName().toUpperCase());
            
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
                OpenCms.getLog(this).debug(",, " 
                        + "Monitored:, " + name1.sprintf(key) + ", " 
                        + "Type:, " + name2.sprintf(obj.getClass().getName()) + ", " 
                        + "Entries:, " + form.sprintf(getItems(obj)) + ", " 
                        + "Limit:, " + form.sprintf(getLimit(obj)) + ", " 
                        + "Size:, " + form.sprintf(Long.toString(size)));
            }
            memStatus += "size monitored: " + totalSize + " (" + totalSize / 1048576 + ")";
            OpenCms.getLog(this).debug(memStatus);
            
            OpenCmsSessionManager sm = OpenCmsCore.getInstance().getSessionManager();
            CmsSessionInfoManager cs = OpenCms.getSessionInfoManager();
            
            if ((sm != null) && (cs != null)) {
                OpenCms.getLog(this).debug(", Sessions users: " + cs.getLoggedInUsers().size() + " current: " + sm.getCurrentSessions() + " total: " + sm.getTotalSessions());
            }
            
            sm = null;
            cs = null;            
        }                
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
}
