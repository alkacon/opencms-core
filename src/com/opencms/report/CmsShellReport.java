/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/report/Attic/CmsShellReport.java,v $
 * Date   : $Date: 2002/12/12 18:41:36 $
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

import com.opencms.linkmanagement.CmsPageLinks;

/**
 * Report class used for the shell.<p>
 * 
 * It stores nothing. It just prints everthing to <code>System.out</code>.
 * 
 * @author Hanjo Riege
 * @author Alexander Kandzior (a.kandzior@alkacon.com) 
 *  
 * @version $Revision: 1.2 $
 */
public class CmsShellReport implements I_CmsReport {

    /**
     * Empty default constructor. 
     * 
     * @see java.lang.Object#Object()
     */
    public CmsShellReport() {
    }

    /**
     * @see com.opencms.report.I_CmsReport#addSeperator(java.lang.String)
     */
    public void addSeperator(String message) {
        System.out.println();
    }
    
    /**
     * @see com.opencms.report.I_CmsReport#addSeperator(java.lang.String, java.lang.String)
     */
    public void addSeperator(String message, String info) {
        System.out.println();
    }
        
    /**
     * @see com.opencms.report.I_CmsReport#addSeperator()
     */
    public void addSeperator(){
        System.out.println();
    }

    /**
     * @see com.opencms.report.I_CmsReport#addString(java.lang.String)
     */
    public void print(String value){
        System.out.print(value);
    }
    
    /**
     * @see com.opencms.report.I_CmsReport#println(java.lang.String)
     */
    public void println(String value) {
        System.out.println(value);
    }
        
    /**
     * @see com.opencms.report.I_CmsReport#print(java.lang.String, int)
     */
    public void print(String value, int format) {
        System.out.print(value);        
    }
    
    /**
     * @see com.opencms.report.I_CmsReport#println(java.lang.String, int)
     */
    public void println(String value, int format) {
        System.out.println(value);        
    }
    
    /**
     * @see com.opencms.report.I_CmsReport#getReportUpdate()
     */
    public String getReportUpdate(){
        return "";
    }
    
    /**
     * @see com.opencms.report.I_CmsReport#println(java.lang.Throwable)
     */
    public void println(Throwable t) {
        System.out.println(t.getMessage());
        t.printStackTrace(System.out);
    }
    
    /**
     * @see com.opencms.report.I_CmsReport#println(com.opencms.linkmanagement.CmsPageLinks)
     */
    public void println(CmsPageLinks value) {
        System.out.println(value.toString());
    }
    
    /**
     * @see com.opencms.report.I_CmsReport#hasBrokenLinks()
     */
    public boolean hasBrokenLinks() {
        return false;
    }
    
    /**
     * @see com.opencms.report.I_CmsReport#key(java.lang.String)
     */
    public String key(String keyName) {
        return null;
    }
}