

package com.opencms.file;

import com.opencms.core.*;
import java.util.*;


/**
 * This class describes a tasklog in the Cms.
 * 
 * @author Ruediger Gutfleisch
 * @version $Revision: 1.1 $ $Date: 2000/01/11 17:41:33 $
 */
public class CmsTaskLog extends A_CmsTaskLog implements I_CmsConstants {


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
