/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/monitor/CmsMemoryMonitor.java,v $
 * Date   : $Date: 2003/11/05 17:45:28 $
 * Version: $Revision: 1.1 $
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
import org.opencms.cache.CmsLruHashMap;
import org.opencms.cache.I_CmsLruCacheObject;
import org.opencms.cron.I_CmsCronJob;
import org.opencms.main.OpenCms;

import com.opencms.file.CmsFile;
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @version $Revision: 1.1 $ $Date: 2003/11/05 17:45:28 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public class CmsMemoryMonitor implements I_CmsCronJob {

    static HashMap m_monitoredObjects;
    
    static boolean m_initialized = false;
    
    static int m_interval = -1;
    
    static long m_lastRun = 0;
    
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
    public final String launch(CmsObject cms, String parameter) throws Exception {
    
        CmsMemoryMonitor mm = OpenCms.getMemoryMonitor();
        
        if (parameter != null && parameter.startsWith("interval="))
            m_interval = Integer.parseInt(parameter.substring(9));
            
        if (m_initialized && (System.currentTimeMillis() - m_lastRun) > m_interval) {
            mm.logStatistics();
            m_lastRun = System.currentTimeMillis();
        }
        
        return "";    
    }   
     
    /**
     * Logs the current memory statistics of the monitored objects.<p>
     */
    private void logStatistics() {
        
        if (!OpenCms.getLog(this).isDebugEnabled())
            return;
        
        OpenCms.getLog(this).debug(", "
            + "Memory total: ," + Runtime.getRuntime().totalMemory() / 1048576 + " ,"
            + "free: ," + Runtime.getRuntime().freeMemory() / 1048576);    
                    
        for (Iterator keys = m_monitoredObjects.keySet().iterator(); keys.hasNext();) {
            
            String key = (String)keys.next();
            Object obj = m_monitoredObjects.get(key);
            
            OpenCms.getLog(this).debug(",,,,, " 
                + "Monitored: ," + key + ", " 
                + "Type: ," + obj.getClass().getName() + ", " + Integer.toHexString(obj.hashCode()) + ", "
                + "Limit: ," + getLimit(obj) + ", "
                + "Mapped: ," + getItems(obj) + ", "  
                + "Costs: ," + getCosts(obj) + ", "
                + "Keys: ," + getKeySize(obj) + ", "
                + "Values: ," + getValueSize(obj) 
                );   
        }
    }

    /**
     * Returns the max costs for all items within a monitored object.<p>
     * obj must be of type CmsLruCache, CmsLruHashMap
     * 
     * @param obj the object
     * @return max cost limit or "-"
     */
    private String getLimit (Object obj) {
    
        if (obj instanceof CmsLruCache)
            return Integer.toString(((CmsLruCache)obj).getMaxCacheCosts());
            
        if (obj instanceof CmsLruHashMap)
            return Integer.toString(((CmsLruHashMap)obj).getLruCache().getMaxCacheCosts());
            
        return "-";    
    }   
     
    /**
     * Returns the number of items within a monitored object.<p>
     * obj must be of type CmsLruCache, CmsLruHashMap or Map
     * 
     * @param obj the object
     * @return the number of items or "-"
     */
    private String getItems (Object obj) {
        
        if (obj instanceof CmsLruCache)
            return Integer.toString(((CmsLruCache)obj).size());
        
        if (obj instanceof CmsLruHashMap)
            return Integer.toString(((CmsLruHashMap)obj).size());
           
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
    private String getKeySize (Object obj) {
    
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
    private String getValueSize (Object obj) {
        
        Map map = null;
        int valueSize[] = {0, 0, 0, 0, 0, 0};
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
            return 
                Integer.toString(valueSize[0]) + ", byte[] size, " 
                + Integer.toString(valueSize[1]) + ", String size, "
                + Integer.toString(valueSize[2]) + ", List items, "
                + Integer.toString(valueSize[3]) + ", Map items, "
                + Integer.toString(valueSize[4]) + ", CmsFiles, "
                + Integer.toString(valueSize[5]) + ", CmsResources/Folders, "
                + Integer.toString(unresolved) + ", unresolved";
        else
            return "-, byte[] size, " + "-, String size, " + "-, List items, " + "-, Map items, " + "-, CmsFiles, " + "-, CmsResources/Folders, "+ "-, unresolved";
    }
     
    /**
     * Returns the cache costs of a monitored object.<p>
     * obj must be of type CmsLruCache or CmsLruHashMap
     * 
     * @param obj the object
     * @return the cache costs or "-"
     */
    private String getCosts (Object obj) {
        
        if (obj instanceof CmsLruCache)
            return Integer.toString(((CmsLruCache)obj).getObjectCosts());
            
         
        if (obj instanceof CmsLruHashMap)
            return Integer.toString(((CmsLruHashMap)obj).getLruCache().getObjectCosts());
                
        return "-";
    }
    
}
