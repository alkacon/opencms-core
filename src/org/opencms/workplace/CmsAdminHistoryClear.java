/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsAdminHistoryClear.java,v $
 * Date   : $Date: 2003/09/11 14:29:16 $
 * Version: $Revision: 1.3 $
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
import com.opencms.file.CmsRegistry;
import com.opencms.flex.jsp.CmsJspActionElement;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods for the history clear dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/system/workplace/administration/history/clearhistory/index.html
 * </ul>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.3 $
 * 
 * @since 5.1
 */
public class CmsAdminHistoryClear extends CmsDialog {
    
    public static final int DEBUG = 0;
    
    public static final int ACTION_SAVE_EDIT = 300;
    
    public static final String DIALOG_SAVE_EDIT = "saveedit";
    
    public static final String DIALOG_TYPE = "historyclear";

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsAdminHistoryClear(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsAdminHistoryClear(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
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
        if (DIALOG_SAVE_EDIT.equals(getParamAction())) {
            setAction(ACTION_SAVE_EDIT);
        } else { 
            // set the default action               
            setAction(ACTION_DEFAULT); 
            setParamTitle(key("label.admin.history.clear"));
        }      
    } 
    
    /**
     * Builds the HTML for the history settings input form.<p>
     * 
     * @param startYear the start year for the year select box
     * @param endYear the end year for the year select box
     * @return the HTML code for the history settings input form
     */
    public String buildClearForm(int startYear, int endYear) {
        StringBuffer retValue = new StringBuffer(512);
        CmsRegistry reg = null;
        int maxVersions = -1;
        boolean histEnabled = false;
        try {
            reg = getCms().getRegistry();
            histEnabled = reg.getBackupEnabled();
            maxVersions = reg.getMaximumBackupVersions();
        } catch (CmsException e) { }
        
        // append settings info or disabled message if history is disabled
        retValue.append(dialogBlockStart(key("label.admin.history.settings")));
        if (histEnabled) {
            retValue.append(maxVersions + " ");
            retValue.append(key("input.history.clear.versioninfo"));
            retValue.append("<br>" + key("input.history.clear.selectinfo"));
        } else {
            retValue.append(key("input.history.clear.disabledinfo"));
        }
        retValue.append(dialogBlockEnd());
        retValue.append(dialogSpacer());
        
        // append input fields if history is enabled
        if (histEnabled) {
            retValue.append("<table border=\"0\">\n");
            retValue.append("<tr>\n");
            retValue.append("<td>" + key("input.history.clear.number") + "</td>\n");
            retValue.append("<td>" + buildSelectVersions(null) + "</td>\n");
            retValue.append("</tr>\n");
            retValue.append("<tr><td colspan=\"2\">&nbsp;</td></tr>\n");
            retValue.append("<tr>\n");
            retValue.append("<td>" + key("input.history.clear.date") + "</td>\n");
            retValue.append("<td>");
            retValue.append(buildSelectDay(null) +  "&nbsp;");
            retValue.append(buildSelectMonth(null) +  "&nbsp;");
            retValue.append(buildSelectYear(null, startYear, endYear) +  "&nbsp;");
            retValue.append("</td>\n");
            retValue.append("</tr>\n");
            retValue.append("</table>\n");
        }
        
        return retValue.toString(); 
    }
    
    /**
     * Builds the HTML code for a select box of days.<p>
     * 
     * @param attributes optional additional attributes of the select tag
     * @return the HTML code for a select box of days
     */
    public String buildSelectDay(String attributes) {
        return buildSelectNumbers("day", attributes, 1, 31);
    }
    
    /**
     * Creates the HTML code for a select box with integer values.<p>
     * 
     * @param fieldName the name of the select box
     * @param attributes the optional tag attributes
     * @param startValue the start integer value for the options
     * @param endValue the end integer value for the options
     * @return the HTML code for the select box
     */
    private String buildSelectNumbers(String fieldName, String attributes, int startValue, int endValue) {
        StringBuffer retValue = new StringBuffer(512);
        
        retValue.append("<select name=\"" + fieldName + "\"");
        if (attributes != null) {
            retValue.append(" "+attributes);
        }
        retValue.append(">\n");
        retValue.append("\t<option value=\"\" selected=\"selected\">" + key("input.history.clear.select") + "</option>\n");
        for (int i=startValue; i<=endValue; i++) {
            retValue.append("\t<option value=\""+i+"\">"+i+"</option>\n");
        }
        retValue.append("</select>\n");
        
        return retValue.toString();
    }
    
    /**
     * Builds the HTML code for a select box of months.<p>
     * 
     * @param attributes optional additional attributes of the select tag
     * @return the HTML code for a select box of months
     */
    public String buildSelectMonth(String attributes) {
        StringBuffer retValue = new StringBuffer(512);
        Locale locale = new Locale(getSettings().getLanguage());
        Calendar cal = new GregorianCalendar(locale);
        DateFormat df = new SimpleDateFormat("MMMM", locale);

        retValue.append("<select name=\"month\"");
        if (attributes != null) {
            retValue.append(" "+attributes);
        }
        retValue.append(">\n");
        retValue.append("\t<option value=\"\" selected=\"selected\">" + key("input.history.clear.select") + "</option>\n");
        for (int i=0; i<12; i++) {
            cal.set(Calendar.MONTH, i);
            retValue.append("\t<option value=\""+(i+1)+"\">"+df.format(cal.getTime())+"</option>\n");
        }
        retValue.append("</select>\n");

        return retValue.toString();
    }
    
    /**
     * Builds the HTML code for a select box of years.<p>
     * 
     * @param attributes optional additional attributes of the select tag
     * @param startyear the starting year for the options
     * @param endyear the ending year for the options
     * @return the HTML code for a select box of years
     */
    public String buildSelectYear(String attributes, int startyear, int endyear) {
        return buildSelectNumbers("year", attributes, startyear, endyear);
    }
    
    /**
     * Build the HTML code for a select box of versions to keep.<p>
     * 
     * @param attributes optional additional attributes of the select tag
     * @return the HTML code for a select box of versions
     */
    public String buildSelectVersions(String attributes) {
        int versions = 0;
        try {
            versions = getCms().getRegistry().getMaximumBackupVersions();
        } catch (CmsException e) { }
        return buildSelectNumbers("versions", attributes, 0 , versions);
    }
    
    /**
     * Performs the change of the history settings, this method is called by the JSP.<p>
     * 
     * @param request the HttpServletRequest
     * @throws JspException if something goes wrong
     */
    public void actionEdit(HttpServletRequest request) throws JspException {
        try {
            performEditOperation(request);
            // set the request parameters before returning to the overview
            getCms().getRequestContext().getResponse().sendCmsRedirect(getAdministrationBackLink());              
        } catch (CmsException e) {
            // error defining property, show error dialog
            setParamErrorstack(e.getStackTraceAsString());
            setParamReasonSuggestion(getErrorSuggestionDefault());
            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
        } catch (IOException exc) {
          getJsp().include(C_FILE_EXPLORER_FILELIST);
        }
    }
    
    /**
     * Performs the change of the history settings.<p>
     * 
     * @param request the HttpServletRequest
     * @return true if everything was ok
     * @throws CmsException if something goes wrong
     */
    private boolean performEditOperation(HttpServletRequest request) throws CmsException {
        // get the delete information from the request parameters
        String paramVersions = request.getParameter("versions");
        String paramDay = request.getParameter("day");
        String paramMonth = request.getParameter("month");
        String paramYear = request.getParameter("year");
        
        // check the submitted values        
        int versions = 0;
        long timeStamp = 0;
        boolean useVersions = false;
        try {
            versions = Integer.parseInt(paramVersions);
            useVersions = true;
        } catch (NumberFormatException e) {
            // no int value submitted, check date fields
            try {
                int day = Integer.parseInt(paramDay);
                int month = Integer.parseInt(paramMonth) - 1;
                int year = Integer.parseInt(paramYear);
                Calendar cal = new GregorianCalendar();
                cal.set(year, month, day);
                timeStamp = cal.getTimeInMillis();
            } catch (NumberFormatException ex) {
                // no date values submitted, throw exception
                throw new CmsException("Invalid arguments. Check the input fields of the dialog", CmsException.C_BAD_NAME, ex);
            }
        }
        
        // set the timeStamp one day to the future to delete versions
        if (useVersions) {
            timeStamp = System.currentTimeMillis() + 86400000;
        }
        
        if (DEBUG > 0) System.err.println("Versions: "+versions+"\nDate: "+timeStamp);
        
        // delete the backup files
        getCms().deleteBackups(timeStamp, versions);
             
        return true;
    }

}
