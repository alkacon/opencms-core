/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/defaults/master/Attic/CmsMasterDataSet.java,v $
* Date   : $Date: 2002/01/25 08:04:54 $
* Version: $Revision: 1.5 $
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

package com.opencms.defaults.master;

import java.io.Serializable;
import java.util.Vector;
import com.opencms.core.I_CmsConstants;

/**
 * This class defines the dataset of the master module. It carries a lot of
 * generic data-fileds which can be used for a special Module. This class
 * should only be used within this mastermodule.
 *
 * @author A. Schouten $
 * $Revision: 1.5 $
 * $Date: 2002/01/25 08:04:54 $
 */
public class CmsMasterDataSet implements Serializable {

    //// members for "core-handling" ////

    /** The primary key of this master module. */
    public int m_masterId;

    /** Defines the "type" of the module */
    public int m_subId;

    /** The id of the owner */
    public int m_userId;

    /** The id of the group */
    public int m_groupId;

    /** The project, this cd belongs to */
    public int m_lockedInProject;

    /** The generated project id (indicates, if the entry belongs to the
     *  current project) */
    public int m_projectId;

    /** The state of the cd (unchanged/changed/new/deleted) */
    public int m_state;

    /** The user-id who had locked this ressource or -1 */
    public int m_lockedBy;

    /** The user who has lastly changed this cd */
    public int m_lastModifiedBy;

    /** The date when this cd was created as long-value */
    public long m_dateCreated;

    /** The date of the last modification of this cd */
    public long m_dateLastModified;

    public int m_accessFlags;

    //// memebers for "publication-handling" ////

    /** the start-date this cd should be shown in the frontend */
    public long m_publicationDate;

    /** the ending-date this cd should be shown in the frontend */
    public long m_purgeDate;

    //// named "data members" ////

    /** flags of this cd */
    public int m_flags;

    /** member for feed's */
    public int m_feedId;

    /** member for feed's */
    public int m_feedReference;

    /** member for feed's */
    public String m_feedFilename;

    /** The title of this cd */
    public String m_title;

    //// data members for history ////

    /** The version-number of this record in history */
    public int m_versionId = I_CmsConstants.C_UNKNOWN_ID;

    /** The owner as String */
    public String m_userName;

    /** The group as String */
    public String m_groupName;

    /** The user who has changed this the last time as String */
    public String m_lastModifiedByName;

    //// generic "data members" ////

    /** data memeber for String values type VARCHAR2(4000) */
    public String[] m_dataBig = new String[10];

    /** data memeber for String values type VARCHAR2(2000) */
    public String[] m_dataMedium = new String[10];

    /** data memeber for String values type VARCHAR2(500) */
    public String[] m_dataSmall = new String[40];

    /** data memeber for Date values */
    public long[] m_dataDate = new long[5];

    /** data memeber for Integer values type int */
    public int[] m_dataInt = new int[10];

    /** data refernces of type int */
    public int[] m_dataReference = new int[10];

    //// Media Data ////

    /** The media objects registered for update */
    public Vector m_mediaToUpdate = new Vector();

    /** The media objects registered for deletion */
    public Vector m_mediaToDelete = new Vector();

    /** The media objects registered for deletion */
    public Vector m_mediaToAdd = new Vector();

    /** The current media objects */
    public Vector m_media = null;

    //// Channel Data ////
    /** The channel objects registered for deletion */
    public Vector m_channelToDelete = new Vector();

    /** The channel objects registered for deletion */
    public Vector m_channelToAdd = new Vector();

    /** The current channel objects */
    public Vector m_channel = null;

    /**
     * The default constructor should only be visible within this package.
     */
    public CmsMasterDataSet() {
    }

    /**
     * Returns all member-data in a long String. This can be used for debugging.
     */
    public String toString() {
        StringBuffer returnValue = new StringBuffer();
        returnValue.append(this.getClass().getName() + "{");
        returnValue.append("m_masterId="+m_masterId+";");
        returnValue.append("m_subId="+m_subId+";");
        returnValue.append("m_userId="+m_userId+";");
        returnValue.append("m_groupId="+m_groupId+";");
        returnValue.append("m_title="+m_title+";");
        returnValue.append("m_projectId="+m_projectId+";");
        returnValue.append("m_state="+m_state+";");
        returnValue.append("m_lockedBy="+m_lockedBy+";");
        returnValue.append("m_lastModifiedBy="+m_lastModifiedBy+";");
        returnValue.append("m_dateCreated="+m_dateCreated+";");
        returnValue.append("m_dateLastModified="+m_dateLastModified+";");
        returnValue.append("m_publicationDate="+m_publicationDate+";");
        returnValue.append("m_purgeDate="+m_purgeDate+";");
        returnValue.append("m_flags="+m_flags+";");
        returnValue.append("m_feedId="+m_feedId+";");
        returnValue.append("m_feedReference="+m_feedReference+";");
        returnValue.append("m_feedFilename="+m_feedFilename+";");
        returnValue.append("];");
        returnValue.append("m_dataBig=[");
        helperToString(m_dataBig, returnValue);
        returnValue.append("];");
        returnValue.append("m_dataMedium=[");
        helperToString(m_dataMedium, returnValue);
        returnValue.append("];");
        returnValue.append("m_dataSmall=[");
        helperToString(m_dataSmall, returnValue);
        returnValue.append("];");
        returnValue.append("m_dataInt=[");
        for(int i = 0; i < m_dataInt.length; i++) {
            returnValue.append((i==0?"":",") + m_dataInt[i] );
        }
        returnValue.append("];");
        returnValue.append("m_dataReference=[");
        for(int i = 0; i < m_dataReference.length; i++) {
            returnValue.append((i==0?"":",") + m_dataInt[i] );
        }
        returnValue.append("]}");
        return returnValue.toString();
    }

     /**
     * Helper method for toString().
     * @param a array of object for output.
     * @param StringBuffer the buffer to append the return value.
     */
    private void helperToString(Object[] array, StringBuffer buffer) {
        for(int i = 0; i < array.length; i++) {
            buffer.append((i==0?"":",") + array[i] );
        }
    }
}