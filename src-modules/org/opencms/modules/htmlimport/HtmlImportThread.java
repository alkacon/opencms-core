/*
 * File   :
 * Date   : 
 * Version: 
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

package org.opencms.modules.htmlimport;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.file.CmsObject;

/**
 * Thread for extended html import. <p>
 * 
 * @author Michael Emmerich ((m.emmerich@alkacon.com)
 */
public class HtmlImportThread extends Thread {

    /** the CmsObject to use */
    private CmsObject m_cms;
    
    /** reference to the HtmlImport */
    private HtmlImport m_htmlImport;
    
    /** the report for the output */
    private StringBuffer m_report;
    
    
    /**
     * Constructor, creates a new HtmlImportThreat.<p>
     * 
     * @param cms the current CmsObject
     * @param htmlImport reference to the HtmlImport object which is doing the actual import
     */
    public HtmlImportThread(CmsObject cms, HtmlImport htmlImport) {
      
        m_cms = cms;
        m_cms.getRequestContext().setUpdateSessionEnabled(false);
        m_report = new StringBuffer();
        m_htmlImport=htmlImport;
    }
    

    /**
     * Returns the part of the report that is ready.<p>
     *
     * @return the part of the report that is ready
     */
    public String getReportUpdate(){
        return new String(m_report);
    }    
    
    
    /**
     * The run method which starts the import process.<p>
     */
    public void run() {
        try {
             
            // do the actual import
            m_report.append("Extended HTML Import Start<br>");
            m_htmlImport.startImport(m_report);
            m_report.append("Extended HTML Import End<br>");
        }
        catch(CmsException e) {
            m_report.append(e);
            if(I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL) ) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, e.getMessage());
            }
        }
    }
   
}
