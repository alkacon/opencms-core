/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsTaskList.java,v $
* Date   : $Date: 2004/06/07 12:44:05 $
* Version: $Revision: 1.25 $
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

import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.util.CmsDateUtil;
import org.opencms.workflow.CmsTask;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsUser;
import com.opencms.template.A_CmsXmlContent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Vector;

import org.w3c.dom.Element;

/**
 * Class for building task list. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;tasklist&gt;</code>.
 * 
 * @author Andreas Schouten
 * @author Mario Stanke
 * @version $Revision: 1.25 $ $Date: 2004/06/07 12:44:05 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */

public class CmsTaskList extends A_CmsWpElement implements I_CmsWpElement {
    
    /**
     * Handling of the special workplace <CODE>&lt;TASKLIST&gt;</CODE> tags.
     * <P>
     * Returns the processed code with the actual elements.
     * <P>
     * Projectlists can be referenced in any workplace template by <br>
     * <CODE>&lt;TASKLIST method="methodname"/&gt;</CODE>
     * 
     * @param cms CmsObject Object for accessing resources.
     * @param n XML element containing the <code>&lt;ICON&gt;</code> tag.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param callingObject reference to the calling object <em>(not used here)</em>.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed button.
     * @throws CmsException
     */
    
    public Object handleSpecialWorkplaceTag(CmsObject cms, Element n, A_CmsXmlContent doc, Object callingObject, 
            Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
        
        // Get list definition values
        CmsXmlWpTemplateFile listdef = getTaskListDefinitions(cms);
        CmsRequestContext context = cms.getRequestContext();
        String listMethod = n.getAttribute("method");
        
        // call the method for generating projectlist elements
        Method callingMethod = null;
        Vector list = new Vector();
        try {
            callingMethod = callingObject.getClass().getMethod(listMethod, new Class[] {
                CmsObject.class, CmsXmlLanguageFile.class
            });
            list = (Vector)callingMethod.invoke(callingObject, new Object[] {
                cms, lang
            });
        }
        catch(NoSuchMethodException exc) {
            
            // The requested method was not found.
            throwException("Could not find method " + listMethod + " in calling class " + callingObject.getClass().getName() 
                    + " for generating lasklist content.", CmsException.C_NOT_FOUND);
        }
        catch(InvocationTargetException targetEx) {
            
            // the method could be invoked, but throwed a exception            
            // itself. Get this exception and throw it again.              
            Throwable e = targetEx.getTargetException();
            if(!(e instanceof CmsException)) {
                
                // Only print an error if this is NO CmsException
                throwException("User method " + listMethod + " in calling class " + callingObject.getClass().getName() 
                        + " throwed an exception. " + e, CmsException.C_UNKNOWN_EXCEPTION);
            }
            else {
                
                // This is a CmsException                
                // Error printing should be done previously.
                throw (CmsException)e;
            }
        }
        catch(Exception exc2) {
            throwException("User method " + listMethod + " in calling class " + callingObject.getClass().getName() 
                    + " was found but could not be invoked. " + exc2, CmsException.C_XML_NO_USER_METHOD);
        }
        
        /** StringBuffer for the generated output */
        StringBuffer result = new StringBuffer();
        String priority;
        String projectname;
        String stateIcon;
        String style;
        String contextmenu;
        long startTime;
        long timeout;
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date(System.currentTimeMillis()));
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        GregorianCalendar newcal = new GregorianCalendar(cal.get(Calendar.YEAR), 
                cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        long now = newcal.getTime().getTime();
        for(int i = 0;i < list.size();i++) {
            
            // get the actual project
            CmsTask task = (CmsTask)list.elementAt(i);
            CmsProject project = null;
            projectname = "?";
            try {
                project = cms.readProject(task);
            }
            catch(Exception exc) {
                
                // no project - continue with next task
                continue;
            }
            if((project == null) || (project.getFlags() == I_CmsConstants.C_PROJECT_STATE_ARCHIVE)) {
                
                // project was published - continue
                continue;
            }
            projectname = project.getName();
            priority = listdef.getProcessedDataValue("priority" + task.getPriority(), callingObject);
            startTime = task.getStartTime().getTime();
            timeout = task.getTimeOut().getTime();
            listdef.setData("taskid", task.getId() + "");
            listdef.setData("count", i + "");
            
            // making the context menus depending on the state of the task and             
            // the role of the user
            CmsUser owner = null;
            String ownerName = "";
            try {
                owner = cms.readOwner(task);
                ownerName = owner.getName();
            }
            catch(Exception exc) {
                
            
            // ignore the exception
            }
            CmsUser editor = null;
            try {
                editor = cms.readAgent(task);
            }
            catch(Exception exc) {
                
            
            // ignore the exception
            }
            CmsGroup role = null;
            String roleName = "";
            try {
                role = cms.readGroup(task);
                roleName = role.getName();
            }
            catch(Exception exc) {
                
            
            // ignore the exception
            }
            boolean isOwner = context.currentUser().equals(owner);
            boolean isEditor = context.currentUser().equals(editor);
            boolean isInRole = false;
            try {
                isInRole = cms.userInGroup(context.currentUser().getName(), roleName);
            }
            catch(Exception exc) {
                
            
            // ignore the exception
            }
            
            // now decide which contex menu is appropriate
            if(task.getState() == I_CmsConstants.C_TASK_STATE_ENDED) {
                if(isOwner) {
                    contextmenu = "task1";
                }
                else {
                    if(isEditor) {
                        contextmenu = "task2";
                    }
                    else {
                        if(isInRole) {
                            contextmenu = "task3";
                        }
                        else {
                            contextmenu = "task3";
                        }
                    }
                }
                listdef.setData("contextmenu", contextmenu);
                stateIcon = listdef.getProcessedDataValue("ok", callingObject);
                style = listdef.getProcessedDataValue("style_ok", callingObject);
            }
            else {
                if(task.getPercentage() == 0) {
                    if(isOwner && isEditor) {
                        contextmenu = "task4";
                    }
                    else {
                        if(isOwner) {
                            contextmenu = "task5";
                        }
                        else {
                            if(isEditor) {
                                contextmenu = "task6";
                            }
                            else {
                                if(isInRole) {
                                    contextmenu = "task7";
                                }
                                else {
                                    contextmenu = "task8";
                                }
                            }
                        }
                    }
                    listdef.setData("contextmenu", contextmenu);
                    if(timeout < now) {
                        stateIcon = listdef.getProcessedDataValue("alert", callingObject);
                        style = listdef.getProcessedDataValue("style_alert", callingObject);
                    }
                    else {
                        stateIcon = listdef.getProcessedDataValue("new", callingObject);
                        style = listdef.getProcessedDataValue("style_new", callingObject);
                    }
                }
                else {
                    if(isOwner && isEditor) {
                        contextmenu = "task9";
                    }
                    else {
                        if(isOwner) {
                            contextmenu = "task10";
                        }
                        else {
                            if(isEditor) {
                                contextmenu = "task11";
                            }
                            else {
                                if(isInRole) {
                                    contextmenu = "task12";
                                }
                                else {
                                    contextmenu = "task13";
                                }
                            }
                        }
                    }
                    listdef.setData("contextmenu", contextmenu);
                    if(timeout < now) {
                        stateIcon = listdef.getProcessedDataValue("alert", callingObject);
                        style = listdef.getProcessedDataValue("style_alert", callingObject);
                    }
                    else {
                        stateIcon = listdef.getProcessedDataValue("activ", callingObject);
                        style = listdef.getProcessedDataValue("style_activ", callingObject);
                    }
                }
            }
            String agent = "";
            String group = "";
            String due = "";
            String from = "";
            try {
                agent = cms.readAgent(task).getName();
            }
            catch(Exception exc) {
                
            
            // ignore the exception
            }
            try {
                group = cms.readGroup(task).getName();
            }
            catch(Exception exc) {
                
            
            // ignore the exception
            }
            try {
                due = CmsDateUtil.getDateShort(timeout);
            }
            catch(Exception exc) {
                
            
            // ignore the exception
            }
            try {
                from = CmsDateUtil.getDateShort(startTime);
            }
            catch(Exception exc) {
                
            
            // ignore the exception
            }
            
            // get the processed list.
            listdef.setData("stateicon", stateIcon);
            listdef.setData("style", style);
            listdef.setData("priority", priority);
            listdef.setData("taskid", task.getId() + "");
            listdef.setData("task", task.getName());
            listdef.setData("foruser", agent);
            listdef.setData("forrole", group);
            listdef.setData("actuator", ownerName);
            listdef.setData("due", due);
            listdef.setData("from", from);
            listdef.setData("project", projectname);
            result.append(listdef.getProcessedDataValue("defaulttasklist", callingObject, parameters));
        }
        return result.toString();
    }
    
    /**
     * Indicates if the results of this class are cacheable.
     * 
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */
    
    public boolean isCacheable(CmsObject cms, String templateFile, String elementName, 
            Hashtable parameters, String templateSelector) {
        return false;
    }
}
