/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/monitor/CmsMemoryMonitor.java,v $
 * Date   : $Date: 2003/11/10 10:21:16 $
 * Version: $Revision: 1.8 $
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
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.util.PrintfFormat;

import com.opencms.core.CmsException;
import com.opencms.defaults.CmsMail;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.util.Utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.collections.LRUMap;

/**
 * Monitors OpenCms memory consumtion.<p>
 * 
 * @version $Revision: 1.8 $ $Date: 2003/11/10 10:21:16 $
 * 
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 */
public class CmsMemoryMonitor implements I_CmsCronJob {
    
    /** set interval for clearing the caches to 15 minutes */
    private static final int C_INTERVAL_CLEAR = 1000 * 60 * 15;

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
    private boolean m_warningSendSinceLastEmail;
    
    /** flag for memory warning mail send */
    private boolean m_warningLoggedSinceLastLog;
    
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
        m_warningSendSinceLastEmail = false;
        m_warningLoggedSinceLastLog = false;
        m_lastEmailWarning = 0;
        m_lastEmailStatus = 0;       
        m_lastLogStatus = 0;
        m_lastLogWarning = 0;        
        m_lastClearCache = 0;
        m_monitoredObjects = new HashMap();
        
        m_emailSender = configuration.getString("memorymonitor.email.sender");
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
            OpenCms.getLog(this).warn("Clearing caches because memory consumption has reached a critical level");
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
    private String getCosts(Object obj) {
        if (obj instanceof CmsLruCache) {
            return Integer.toString(((CmsLruCache)obj).getObjectCosts());
        }
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
     * @param logformat if true use log file format
     * @return the total size of key strings
     */
    private String getKeySize(Object obj, boolean logformat) {
        Map map = null;
        int keySize = 0;
        try {
            map = (Map)obj;
            if (map != null) {
                for (Iterator i = map.keySet().iterator(); i.hasNext();) {
                    String s = (String)i.next();
                    keySize += (s.length() * 2);
                }
            }
        } catch (Exception exc) {
            keySize = -1;
        }
        if (keySize >= 0) {
            if (logformat) {
                return Integer.toString(keySize) + ", String size";
            } else {
                return Integer.toString(keySize);
            }
        } else {
            if (logformat) {
                return "-, String size";
            } else {
                return "-";
            }
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
        if (obj instanceof CmsLruCache) {
            return Integer.toString(((CmsLruCache)obj).getMaxCacheCosts());
        }
        if (obj instanceof LRUMap) { 
            return Integer.toString(((LRUMap)obj).getMaximumSize());
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
    private String getValueSize(Object obj) {

        Map map = null;
        int valueSize[] = {0, 0, 0, 0, 0, 0};
        int unresolved = 0;

        try {
            map = (Map)obj;
            if (map != null) {
                for (Iterator i = map.values().iterator(); i.hasNext();) {
                    Object value = i.next();
                    if (value instanceof I_CmsLruCacheObject) {
                        value = ((I_CmsLruCacheObject)value).getValue();
                    }
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
        if (map != null) {
            return Integer.toString(valueSize[0]) + ", byte[] size, " + Integer.toString(valueSize[1]) + ", String size, " + Integer.toString(valueSize[2]) + ", List items, " + Integer.toString(valueSize[3]) + ", Map items, " + Integer.toString(valueSize[4]) + ", CmsFiles, " + Integer.toString(valueSize[5]) + ", CmsResources/Folders, " + Integer.toString(unresolved) + ", unresolved";
        } else {
            return "-, byte[] size, " + "-, String size, " + "-, List items, " + "-, Map items, " + "-, CmsFiles, " + "-, CmsResources/Folders, " + "-, unresolved";
        }
    }

    /**
     * @see org.opencms.cron.I_CmsCronJob#launch(com.opencms.file.CmsObject, java.lang.String)
     */
    public String launch(CmsObject cms, String params) throws Exception {
        
        CmsMemoryMonitor monitor = OpenCms.getMemoryMonitor();

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
        if ((warning && m_warningSendSinceLastEmail) 
        || ((m_intervalEmail <= 0) && (System.currentTimeMillis() < (m_lastEmailWarning + m_intervalWarning)))) {
            // send only one warning email between regular status emails OR if status is disabled and warn interval has passed
            return;
        } else if ((! warning) && (m_intervalEmail <= 0)) {
            // if email iterval is <= 0 status email is disabled
            return;
        }
        String date = Utils.getNiceDate(System.currentTimeMillis());
        String subject;
        String content = "";
        if (warning) {
            m_warningSendSinceLastEmail = true;
            m_lastEmailWarning = System.currentTimeMillis(); 
            subject = "OpenCms Memory W A R N I N G [" + OpenCms.getServerName().toUpperCase() + "/" + date + "]";
            content += "W A R N I N G !\nOpenCms memory consumption on server " + OpenCms.getServerName().toUpperCase() + " has reached a critical level !\n\n"
                    + "The configured limit is " + m_maxUsagePercent + "%\n\n";
        } else {
            m_warningSendSinceLastEmail = false;
            m_lastEmailStatus = System.currentTimeMillis();
            subject = "OpenCms Memory Status [" + OpenCms.getServerName().toUpperCase() + "/" + date + "]";
        }
        
        long maxMemory = Runtime.getRuntime().maxMemory() / 1048576;
        long totalMemory = Runtime.getRuntime().totalMemory() / 1048576;
        long usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
        long freeMemory = maxMemory - usedMemory;
        long usage = usedMemory * 100 / maxMemory;
        
        content += "Memory usage report of OpenCms server " + OpenCms.getServerName().toUpperCase() + " at " + date + "\n\n" 
            + "Memory maximum heap size: " + maxMemory + " mb\n" 
            + "Memory current heap size: " + totalMemory + " mb\n\n" 
            + "Memory currently used   : " + usedMemory + " mb (" + usage + "%)\n"
            + "Memory currently unused : " + freeMemory + " mb\n\n";

        if (warning) {
            content += "*** Please take action NOW to ensure that no OutOfMemoryException occurs.\n\n";
        }
        
        content += "\nCurrent size of the caches:\n\n";
        
        List keyList = Arrays.asList(m_monitoredObjects.keySet().toArray());
        Collections.sort(keyList);       
        for (Iterator keys = keyList.iterator(); keys.hasNext();) {
            String key = (String)keys.next();        
            String shortKeys[] = key.split("\\.");
            String shortKey = shortKeys[shortKeys.length - 2] + "." + shortKeys[shortKeys.length - 1];
            PrintfFormat form = new PrintfFormat("%9s");
            Object obj = m_monitoredObjects.get(key);
            content += new PrintfFormat("%-42.42s").sprintf(shortKey) + "  " 
                    + "Entries: " + form.sprintf(getItems(obj)) + "   " 
                    + "Limit: " + form.sprintf(getLimit(obj)) + "\n";
        }
        
        String from = m_emailSender;
        String[] to = m_emailReceiver;        
        try {
            if (from != null && to != null) {
                CmsMail email = new CmsMail(from, to, subject, content, "text/plain");
                email.start();                
            }            
            if (OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info("Memory Monitor " + (warning?"warning":"status") + " email send");
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
        if ((warning && m_warningLoggedSinceLastLog) 
            || ((m_intervalLog <= 0) && (System.currentTimeMillis() < (m_lastLogWarning + m_intervalWarning)))) {
            // send only one warning email between regular status emails OR if status is disabled and warn interval has passed
            return;
        } else if ((! OpenCms.getLog(this).isDebugEnabled()) || ((! warning) && (m_intervalLog <= 0))) {
            // if email iterval is <= 0 status email is disabled
            return;
        }
        
        long maxMemory = Runtime.getRuntime().maxMemory() / 1048576;
        long totalMemory = Runtime.getRuntime().totalMemory() / 1048576;
        long usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
        long freeMemory = maxMemory - usedMemory;
        long usage = usedMemory * 100 / maxMemory;
        
        if (warning) {
            m_lastLogWarning = System.currentTimeMillis();
            m_warningLoggedSinceLastLog = true;
            OpenCms.getLog(this).warn("W A R N I N G Memory consumption of " + usage 
                 + "% has reached a critical level" 
                 + " (" + m_maxUsagePercent + "% configured)");
        } else {
            m_warningLoggedSinceLastLog = false;
            m_lastLogStatus = System.currentTimeMillis();
        }
                
        String memStatus = "Memory max: ," + maxMemory + " ," 
            + "total: ," + totalMemory + " ," 
            + "free: ," + freeMemory + " ," 
            + "used: ," + usedMemory;
                
        if (warning) {
            OpenCms.getLog(this).warn(memStatus);
        } else {        
            OpenCms.getLog(this).debug(memStatus);
            for (Iterator keys = m_monitoredObjects.keySet().iterator(); keys.hasNext();) {
                String key = (String)keys.next();
                Object obj = m_monitoredObjects.get(key);
                OpenCms.getLog(this).debug(",,,,,,, " 
                        + "Monitored: ," + key + ", " 
                        + "Type: ," + obj.getClass().getName() + ", " + Integer.toHexString(obj.hashCode()) + ", " 
                        + "Limit: ," + getLimit(obj) + ", " 
                        + "Mapped: ," + getItems(obj) + ", " 
                        + "Costs: ," + getCosts(obj) + ", " 
                        + "Keys: ," + getKeySize(obj, true) + ", " 
                        + "Values: ," + getValueSize(obj));
            }
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
