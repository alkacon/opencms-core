/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/report/Attic/CmsShellReport.java,v $
* Date   : $Date: 2002/05/24 12:51:09 $
* Version: $Revision: 1.1 $
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
 * Title:        OpenCms
 * Description: This report class is used for the shell. It stores nothing. It just
 *              prints everthing on System.out.
 * @author Hanjo Riege
 * @version 1.0
 */

public class CmsShellReport implements I_CmsReport {

    public CmsShellReport() {
    }
    /**
     * adds a predefined seperator.
     * @param int says which seperator should be used.
     */
    public void addSeperator(int sepNumber){
        System.out.println();
    }

    /**
     * adds the standard seperator.
     */
    public void addSeperator(){
        System.out.println();
    }

    /**
     * adds a new object to the report: String
     * @param String the new reportable Object.
     */
    public void addString(String value){
        System.out.print(value);
    }

    /**
     * adds a new object to the report: CmsPageLinks
     * @param CmsPageLinks.
     */
    public void addPageLinks(CmsPageLinks value){
        System.out.print(value.toString());
    }

    /**
     * Generates the report from nextIndex to end and sets the nextIndex to the end.
     */
    public String getReportUpdate(){
        return "";
    }
}