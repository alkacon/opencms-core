/**
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

package com.opencms.defaults;

import com.opencms.defaults.master.*;
import com.opencms.core.*;
import com.opencms.file.*;
import com.opencms.workplace.*;

import java.util.*;

/**
 * Backoffice class with channel functionality
 * @author Michael Dernen
 */
public abstract class A_CmsChannelBackoffice extends A_CmsBackoffice {

    /**
     * UserMethod creates a selectBox for selected channels
     *
     * @param cms CmsObject Object for accessing system resources
     * @param lang CmsXmlLanguageFile
     * @param values Vector for the values in the selectBox
     * @param names Vector for the names in the selectBox
     * @param parameters Hashtable with all template class parameters.
     * @return Integer selected Index Value of selectBox
     * @throws com.opencms.core.CmsException in case of unrecoverable errors
     */
    public Integer getSelectedChannels(CmsObject cms,CmsXmlLanguageFile lang,Vector values,Vector names,Hashtable parameters)
                    throws CmsException{
        int retValue = -1;
        I_CmsSession session = cms.getRequestContext().getSession(true);
        CmsMasterContent cd = (CmsMasterContent)session.getValue(getContentDefinitionClass().getName());
        Vector channels = cd.getSelectedChannels();
        int size = channels.size();
        for (int i=0; i < size; i++) {
            values.add(channels.elementAt(i));
            names.add(channels.elementAt(i));
        }
        return new Integer(retValue);
    }
    
    /**
     * UserMethod creates a selectBox for available channels
     *
     * @param cms CmsObject Object for accessing system resources
     * @param lang CmsXmlLanguageFile
     * @param values Vector for the values in the selectBox
     * @param names Vector for the names in the selectBox
     * @param parameters Hashtable with all template class parameters.
     * @return Integer selected Index Value of selectBox
     * @throws com.opencms.core.CmsException in case of unrecoverable errors
     */
    public Integer getAvailableChannels(CmsObject cms,CmsXmlLanguageFile lang,Vector values,Vector names,Hashtable parameters)
                    throws CmsException{
        int retValue = -1;
        I_CmsSession session = cms.getRequestContext().getSession(true);
        CmsMasterContent cd = (CmsMasterContent)session.getValue(getContentDefinitionClass().getName());
        Vector channels = cd.getAvailableChannels(cms);
        int size = channels.size();
        for (int i=0; i < size; i++) {
            values.add(channels.elementAt(i));
            names.add(channels.elementAt(i));
        }
        return new Integer(retValue);
    }
}