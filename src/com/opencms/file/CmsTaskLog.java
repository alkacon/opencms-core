/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsTaskLog.java,v $
* Date   : $Date: 2003/04/01 15:20:18 $
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

package com.opencms.file;


import com.opencms.core.I_CmsConstants;


/**
 * Describes a tasklog in the Cms.
 * 
 * @author Ruediger Gutfleisch
 * @version $Revision: 1.10 $ $Date: 2003/04/01 15:20:18 $
 */
public class CmsTaskLog implements I_CmsConstants {


    /**
     * The Id of the tasklog.
     */
    private int     m_Id = C_UNKNOWN_ID;
    
    /**
     * The Description of the tasklog.
     */
    private String  m_Comment=null; 
    
    /**
     * The Id of the corresponding task.
     */
    private int     m_Task = C_UNKNOWN_ID;
    
    /**
     * The Id Of the corresponding user.
     */
    private int     m_User = C_UNKNOWN_ID;
    
    /**
     * The Type of the TaskLog. 0=SystemLog, 1=UserLog
     */
    private int     m_Type = 0;
    private java.sql.Timestamp m_StartTime  =   null;
    
    /**
     * Constructor, creates a new CmsTaskLog object.
     */
    public CmsTaskLog(int id, String comment, int task, int user, 
                      java.sql.Timestamp starttime, int type){
        m_Id = id;
        m_Comment = comment;
        m_Task = task;
        m_User = user; 
        m_StartTime = starttime;
        m_Type = type;
    }
    /**
     * Compares the overgiven object with this object.
     * 
     * @return true, if the object is identically else it returns false.
     */
    public boolean equals(Object obj) {
        boolean equal=false;
        // check if the object is a CmsUser object
        if (obj instanceof CmsTaskLog) {
            // same ID than the current Task Object?
            if (((CmsTaskLog)obj).getId() == this.getId()){
                equal = true;
            }
        }
        return equal;
    }
    /**
     * Returns the comment of this task.
     * 
     * @return the comment of this task.
     */
    public String getComment() {
        return m_Comment; 
    }
    /**
     * Sets the comment of this task.
     */
    public void setComment(String value) {
        m_Comment = value; 
    }
    /**
     * Returns the id of this task.
     * 
     * @return the id of this task.
     */
    public int getId() {
        return m_Id; 
    }
    public java.sql.Timestamp getStartTime(){
        return m_StartTime;
    }
    public int getType(){
        return m_Type;
    }
    public int getUser(){
        return m_User;
    }
    /**
     * Returns a string-representation for this object.
     * This can be used for debugging.
     * 
     * @return string-representation for this object.
     */
    public String toString() {
        StringBuffer output=new StringBuffer();
        output.append("[TaskLog]:");
        output.append(" Id=");
        output.append(getId());
        output.append(" Comment=");
        output.append(getComment());
        output.append(" StartTime=");
        output.append(getStartTime());
        output.append(" User=");
        output.append(getUser());
        
        if(getType()== C_TASKLOG_SYSTEM) {
            output.append(" Type=System");
        }
        else {
            output.append(" Type=User");
        }
        return output.toString();
    }
}
