/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsTaskLog.java,v $
 * Date   : $Date: 2000/06/05 13:37:56 $
 * Version: $Revision: 1.5 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */


package com.opencms.file;

import com.opencms.core.*;
import java.util.*;


/**
 * This class describes a tasklog in the Cms.
 * 
 * @author Ruediger Gutfleisch
 * @version $Revision: 1.5 $ $Date: 2000/06/05 13:37:56 $
 */
public class CmsTaskLog implements I_CmsConstants {


	/**
	 * The Id of the tasklog.
	 */
	private int		m_Id = C_UNKNOWN_ID;
	
	/**
	 * The Description of the tasklog.
	 */
	private String	m_Comment=null; 
	
	/**
	 * The Id of the corresponding task.
	 */
	private int		m_Task = C_UNKNOWN_ID;
	
	/**
	 * The Id Of the corresponding user.
	 */
	private int		m_User = C_UNKNOWN_ID;
	
	/**
	 * The Type of the TaskLog. 0=SystemLog, 1=UserLog
	 */
	private int		m_Type = 0;
	private java.sql.Timestamp m_StartTime	=	null;
	
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
	 * Returns the id of this task.
	 * 
	 * @return the id of this task.
	 */
	public int getId() {
		return m_Id; 
	}
	
	/**
	 * Returns the name of this task.
	 * 
	 * @return the name of this task.
	 */
	public String getComment() {
		return m_Comment; 
	}
	
	public int getUser(){
		return m_User;
	}
	
	public java.sql.Timestamp getStartTime(){
		return m_StartTime;
	}
	
	public int getType(){
		return m_Type;
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
}
