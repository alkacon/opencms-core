/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/report/Attic/A_CmsReportThread.java,v $
 * Date   : $Date: 2003/07/23 10:02:00 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2001  The OpenCms Group
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.opencms.report;

/** 
 * Provides a common Thread class for the reports.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com) 
 * 
 * @version $Revision: 1.2 $
 * @since 5.0
 */
public abstract class A_CmsReportThread extends Thread {
    
    /**
     * Constructs a new thread with the given name.<p>
     * 
     * @param name the name of the Thread
     */
    public A_CmsReportThread(String name) {
        super(name);
    }
    
    /**
     * Returns the part of the report that is ready for output.
     * 
     * @return the part of the report that is ready for output
     */
    public abstract String getReportUpdate();
    
    /**
     * Flag to indicate if broken links where found during the Thread opertation.<p>
     * 
     * Not all report Thread implementations need to check for broken links, 
     * the default implementation is to return <code>false</code>,
     * indicating that no broken links where found.<p> 
     * 
     * @return boolean true if broken links where found, false (default) otherwise 
     */
    public boolean brokenLinksFound() {
        return false;
    }
            
}
