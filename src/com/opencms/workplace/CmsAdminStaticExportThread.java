/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminStaticExportThread.java,v $
* Date   : $Date: 2001/11/15 15:43:58 $
* Version: $Revision: 1.7 $
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

package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;
import java.util.*;
import java.io.*;

/**
 * Title:
 * Description: Thread to export resources static.
 * @author: Hanjo Riege
 * @version 1.0
 */

public class CmsAdminStaticExportThread extends Thread implements I_CmsConstants {

    private CmsObject m_cms;

    private I_CmsSession m_session;

    public CmsAdminStaticExportThread(CmsObject cms, I_CmsSession session) {
        m_cms = cms;
        m_session = session;

    }

    public void run() {
         // Dont try to get the session this way in a thread!
         // It will result in a NullPointerException sometimes.
         // !I_CmsSession session = m_cms.getRequestContext().getSession(true);
        boolean everythingOk  = true;
        String errormessage = "Error exporting resources:\n";
        try {
            // start the export
            m_cms.exportStaticResources(m_cms.getStaticExportStartPoints());

        }catch(CmsException e){
            everythingOk = false;
            errormessage += " "+e.getTypeText() +" "+e.getMessage() +"\n";
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(A_OpenCms.C_OPENCMS_CRITICAL,"error in static export "+e.getMessage());
            }
        }
        if(!everythingOk){
            m_session.putValue(C_SESSION_THREAD_ERROR, errormessage );
        }
    }

    /** Check whether some of the resources are redundant because a superfolder has also
      *  been selected or a file is included in a folder and change the parameter Vectors
      *
      * @param folderNames contains the full pathnames of all folders
      * @param fileNames contains the full pathnames of all files
      */

    private void checkRedundancies(Vector folderNames, Vector fileNames) {
        int i, j;
        if (folderNames == null) {
            return;
        }
        Vector redundant = new Vector();
        int n = folderNames.size();
        if (n > 1) {
            // otherwise no check needed, because there is only one resource
            for (i = 0; i < n; i++) {
                redundant.addElement(new Boolean(false));
            }
            for (i = 0; i < n - 1; i++) {
                for (j = i + 1; j < n; j++) {
                    if (((String) folderNames.elementAt(i)).length() < ((String) folderNames.elementAt(j)).length()) {
                        if (((String) folderNames.elementAt(j)).startsWith((String) folderNames.elementAt(i))) {
                            redundant.setElementAt(new Boolean(true), j);
                        }
                    } else {
                        if (((String) folderNames.elementAt(i)).startsWith((String) folderNames.elementAt(j))) {
                            redundant.setElementAt(new Boolean(true), i);
                        }
                    }
                }
            }
            for (i = n - 1; i >= 0; i--) {
                if (((Boolean) redundant.elementAt(i)).booleanValue()) {
                    folderNames.removeElementAt(i);
                }
            }
        }
        // now remove the files who are included automatically in a folder
        // otherwise there would be a zip exception
        for (i = fileNames.size() - 1; i >= 0; i--) {
            for (j = 0; j < folderNames.size(); j++) {
                if (((String) fileNames.elementAt(i)).startsWith((String) folderNames.elementAt(j))) {
                    fileNames.removeElementAt(i);
                }
            }
        }
    }

}