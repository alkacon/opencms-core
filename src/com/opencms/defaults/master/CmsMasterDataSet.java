/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/defaults/master/Attic/CmsMasterDataSet.java,v $
* Date   : $Date: 2004/07/08 15:21:14 $
* Version: $Revision: 1.10 $
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

import org.opencms.main.I_CmsConstants;
import org.opencms.util.CmsUUID;


import java.io.Serializable;
import java.util.Vector;

/**
 * This class defines the dataset of the master module. It carries a lot of
 * generic data-fileds which can be used for a special Module. This class
 * should only be used within this mastermodule.
 *
 * @author A. Schouten $
 * $Revision: 1.10 $
 * $Date: 2004/07/08 15:21:14 $
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsMasterDataSet implements Serializable {

    //// members for "core-handling" ////

    /** The primary key of this master module. */
    public CmsUUID m_masterId;

    /** Defines the "type" of the module */
    public int m_subId;

    /** The id of the owner */
    public CmsUUID m_userId;

    /** The id of the group */
    public CmsUUID m_groupId;

    /** The project, this cd belongs to */
    public int m_lockedInProject;

    /** The generated project id (indicates, if the entry belongs to the
     *  current project) */
    public int m_projectId;

    /** The state of the cd (unchanged/changed/new/deleted) */
    public int m_state;

    /** The user-id who had locked this ressource or -1 */
    public CmsUUID m_lockedBy;

    /** The user who has lastly changed this cd */
    public CmsUUID m_lastModifiedBy;

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
     * Creates a new CmsMasterDataSet by cloning
     */
    public Object clone(){
        CmsMasterDataSet retValue = new CmsMasterDataSet();
        retValue.m_accessFlags = this.m_accessFlags;
        retValue.m_channel = this.m_channel;
        retValue.m_channelToAdd = this.m_channelToAdd;
        retValue.m_channelToDelete = this.m_channelToDelete;
        retValue.m_dataBig = this.m_dataBig;
        retValue.m_dataDate = this.m_dataDate;
        retValue.m_dataInt = this.m_dataInt;
        retValue.m_dataMedium = this.m_dataMedium;
        retValue.m_dataReference = this.m_dataReference;
        retValue.m_dataSmall = this.m_dataSmall;
        retValue.m_dateCreated = this.m_dateCreated;
        retValue.m_dateLastModified = this.m_dateLastModified;
        retValue.m_feedFilename = this.m_feedFilename;
        retValue.m_feedId = this.m_feedId;
        retValue.m_feedReference = this.m_feedReference;
        retValue.m_flags = this.m_flags;
        retValue.m_groupId = this.m_groupId;
        retValue.m_groupName = this.m_groupName;
        retValue.m_lastModifiedBy = this.m_lastModifiedBy;
        retValue.m_lastModifiedByName = this.m_lastModifiedByName;
        retValue.m_lockedBy = this.m_lockedBy;
        retValue.m_lockedInProject = this.m_lockedInProject;
        retValue.m_masterId = this.m_masterId;
        retValue.m_media = this.m_media;
        retValue.m_mediaToAdd = this.m_mediaToAdd;
        retValue.m_mediaToDelete = this.m_mediaToDelete;
        retValue.m_mediaToUpdate = this.m_mediaToUpdate;
        retValue.m_projectId = this.m_projectId;
        retValue.m_publicationDate = this.m_publicationDate;
        retValue.m_purgeDate = this.m_purgeDate;
        retValue.m_state = this.m_state;
        retValue.m_subId = this.m_subId;
        retValue.m_title = this.m_title;
        retValue.m_userId = this.m_userId;
        retValue.m_userName = this.m_userName;
        retValue.m_versionId = this.m_versionId;
        return retValue;
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