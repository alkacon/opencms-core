/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/report/A_CmsReport.java,v $
 * Date   : $Date: 2003/10/09 07:58:41 $
 * Version: $Revision: 1.2 $
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

package org.opencms.report;

import com.opencms.flex.util.CmsMessages;

import java.util.ArrayList;
import java.util.List;

/**
 * Base report class.<p> 
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)  
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.2 $
 */
public abstract class A_CmsReport implements I_CmsReport {
    
    protected static final long C_SECONDS = 1000;
    protected static final long C_MINUTES = 1000 * 60;
    protected static final long C_HOURS = 1000 * 60 * 60;    

    /** Localized message access object */
    private List m_messages;
    
    /** Runtime of the report */
    private long m_starttime;
    
    /**
     * @see org.opencms.report.I_CmsReport#addBundle(java.lang.String, java.lang.String)
     */
    public void addBundle(String bundleName, String locale) {
        CmsMessages msg = new CmsMessages(bundleName, locale);
        if (m_messages.contains(msg)) {
            m_messages.remove(msg);
        }
        m_messages.add(msg);   
    }
     
    /**
     * @see org.opencms.report.I_CmsReport#getRuntime()
     */     
    public long getRuntime() {
        return System.currentTimeMillis() - m_starttime;
    }
    
    /**
     * Initializes some member variables for this report.<p>
     */
    protected void init() {
        m_starttime = System.currentTimeMillis();
        m_messages = new ArrayList();
    }
    
    /**
     * @see org.opencms.report.I_CmsReport#key(java.lang.String)
     */
    public String key(String keyName) {
        for (int i=0, l=m_messages.size(); i < l; i++) {
            CmsMessages msg = (CmsMessages)m_messages.get(i);
            String key = msg.key(keyName, (i < (l-1)));
            if (key != null) {
                return key;
            }
        }         
        // if not found, check in 
        return CmsMessages.formatUnknownKey(keyName);
    }
    
    /**
     * @see org.opencms.report.I_CmsReport#printRuntime()
     */
    public String formatRuntime() {
        long runtime = getRuntime();
        long seconds = (runtime / C_SECONDS) % 60;
        long minutes = (runtime / C_MINUTES) % 60;
        long hours = runtime / C_HOURS;
        StringBuffer strBuf = new StringBuffer();

        if (hours < 10) {
            strBuf.append('0');
        }
        strBuf.append(hours);

        strBuf.append(':');

        if (minutes < 10) {
            strBuf.append('0');
        }
        strBuf.append(minutes);

        strBuf.append(':');

        if (seconds < 10) {
            strBuf.append('0');
        }
        strBuf.append(seconds);
        
        return strBuf.toString();
    }
    
    /**
     * @see org.opencms.report.I_CmsReport#resetRuntime()
     */
    public void resetRuntime() {
        m_starttime = System.currentTimeMillis();
    }
}