/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsTouch.java,v $
 * Date   : $Date: 2004/01/06 17:06:05 $
 * Version: $Revision: 1.14 $
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
package org.opencms.workplace;

import com.opencms.core.CmsException;
import com.opencms.file.CmsResource;
import com.opencms.flex.jsp.CmsJspActionElement;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods for the touch resource(s) dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/jsp/dialogs/touch_html
 * </ul>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.14 $
 * 
 * @since 5.1
 */
public class CmsTouch extends CmsDialog {
    
    /** Value for the action: touch */
    public static final int ACTION_TOUCH = 100;
    
    /** The dialog type */
    public static final String DIALOG_TYPE = "touch";
    
    /** Request parameter name for timestamp.<p> */
    public static final String PARAM_NEWTIMESTAMP = "newtimestamp";
    /** Request parameter name for the recursive flag.<p> */
    public static final String PARAM_RECURSIVE = "recursive";
    
    private String m_paramRecursive;
    private String m_paramNewtimestamp;
    
    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsTouch(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsTouch(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }        
    
    
    /**
     * Returns the value of the recursive parameter, 
     * or null if this parameter was not provided.<p>
     * 
     * The recursive parameter on folders decides if all subresources
     * of the folder should be touched, too.<p>
     * 
     * @return the value of the recursive parameter
     */    
    public String getParamRecursive() {
        return m_paramRecursive;
    }

    /**
     * Sets the value of the recursive parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamRecursive(String value) {
        m_paramRecursive = value;
    }  
    
    /**
     * Returns the value of the new timestamp parameter, 
     * or null if this parameter was not provided.<p>
     * 
     * The timestamp parameter stores the new timestamp as String.<p>
     * 
     * @return the value of the new timestamp parameter
     */    
    public String getParamNewtimestamp() {
        return m_paramNewtimestamp;
    }

    /**
     * Sets the value of the new timestamp parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamNewtimestamp(String value) {
        m_paramNewtimestamp = value;
    }  
       
    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        
        // set the action for the JSP switch 
        if (DIALOG_TYPE.equals(getParamAction())) {
            setAction(ACTION_TOUCH);                            
        } else if (DIALOG_WAIT.equals(getParamAction())) {
            setAction(ACTION_WAIT);
        } else {                        
            setAction(ACTION_DEFAULT);
            // build title for touch dialog     
            setParamTitle(key("title.touch") + ": " + CmsResource.getName(getParamResource()));
        }      
    }
    
    /**
     * Creates the "recursive" checkbox for touching subresources of folders.<p>
     *  
     * @return the String with the checkbox input field or an empty String for folders.
     */
    public String buildCheckRecursive() {
        StringBuffer retValue = new StringBuffer(256);
        
        CmsResource res = null;
        try {
            res = getCms().readFileHeader(getParamResource());
        } catch (CmsException e) {
            return "";
        }    
        
        // show the checkbox only for folders
        if (res.isFolder()) {
            retValue.append("<tr>\n\t<td colspan=\"3\" style=\"white-space: nowrap;\" unselectable=\"on\">");
            retValue.append("<input type=\"checkbox\" name=\""+PARAM_RECURSIVE+"\" value=\"true\">&nbsp;"+key("input.changesubresources"));
            retValue.append("</td>\n</tr>\n");
        }
        return retValue.toString();
    }
    
    /**
     * Returns the current date and time as String formatted in localized pattern.<p>
     * 
     * @return the current date and time as String formatted in localized pattern
     */
    public String getCurrentDateTime() {
        // get the current date & time 
        Locale locale = new Locale(getSettings().getLanguage());
        TimeZone zone = TimeZone.getDefault();
        GregorianCalendar cal = new GregorianCalendar(zone, locale);
        cal.setTimeInMillis(System.currentTimeMillis());
        // format it nicely according to the localized pattern
        DateFormat df = new SimpleDateFormat(getCalendarJavaDateFormat(key("calendar.dateformat") + " " + key("calendar.timeformat")));
        return df.format(cal.getTime());
    }
        
    /**
     * Performs the resource touching, will be called by the JSP page.<p>
     * 
     * @throws JspException if problems including sub-elements occur
     */
    public void actionTouch() throws JspException {
        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
        try {
            if (performTouchOperation())  {
                // if no exception is caused and "true" is returned the touch operation was successful          
                getJsp().include(C_FILE_EXPLORER_FILELIST);
            } else  {
                // "false" returned, display "please wait" screen
                getJsp().include(C_FILE_DIALOG_SCREEN_WAIT);
            }    
        } catch (CmsException e) {
            // prepare common message part
            String message = "<p>\n" 
                + key("title.touch") + ": " + getParamResource() + "\n</p>\n"; 
                
            // error during touching, show error dialog
            setParamErrorstack(e.getStackTraceAsString());
            setParamMessage(message + key("error.message." + getParamDialogtype()));
            setParamReasonSuggestion(getErrorSuggestionDefault());
            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
      
        }
    }
    
    /**
     * Performs the resource touching.<p>
     * 
     * @return true, if the resource was touched, otherwise false
     * @throws CmsException if touching is not successful
     */
    private boolean performTouchOperation() throws CmsException {

        // on folder copy display "please wait" screen, not for simple file copy
        CmsResource sourceRes = getCms().readFileHeader(getParamResource());
        if (sourceRes.isFolder() && ! DIALOG_WAIT.equals(getParamAction())) {
            // return false, this will trigger the "please wait" screen
            return false;
        }

        // get the current resource name
        String filename = getParamResource();

        // get the new timestamp for the resource(s) from request parameter
        long timeStamp = -1;
        try {
            timeStamp = getCalendarDate(getParamNewtimestamp(), true);
        } catch (ParseException e) {
            throw new CmsException("Error in date/time String \"" + getParamNewtimestamp() + "\", cannot parse correct date value", CmsException.C_BAD_NAME, e);
        }
        
        // get the flag if the touch is recursive from request parameter
        boolean touchRecursive = "true".equalsIgnoreCase(getParamRecursive());     
  
        // now touch the resource(s)
        if (timeStamp != -1) {
            // lock resource if autolock is enabled
            checkLock(getParamResource());
            getCms().touch(filename, timeStamp, touchRecursive);      
        }
        return true;
    }
}
