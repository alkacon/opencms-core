/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/scheduler/CmsEditScheduledJobInfoDialog.java,v $
 * Date   : $Date: 2005/05/19 12:55:53 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.tools.scheduler;

import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.scheduler.CmsSchedulerException;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Dialog to edit new and existing scheduled jobs in the administration.<p>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * 
 * @version $Revision: 1.3 $
 * @since 5.9.1
 */
public class CmsEditScheduledJobInfoDialog extends CmsWidgetDialog {

    /** The dialog type. */
    public static final String DIALOG_TYPE = "editjob";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1", "page2"};  
    
    /** Request parameter name for the job id. */
    public static final String PARAM_JOBID = "jobid";
    
    /** Request parameter name for the job name. */
    public static final String PARAM_JOBNAME = "jobname";

    /** The job info object that is edited on this dialog. */
    private CmsScheduledJobInfo m_jobInfo;
    
    private String m_paramJobid;
    private String m_paramJobname;
    
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsEditScheduledJobInfoDialog(CmsJspActionElement jsp) {

        super(jsp);
    }       

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsEditScheduledJobInfoDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }
    
    /**
     * Saves the edited scheduled job.<p>
     */
    public void actionSave() {
        
        List errors = new ArrayList();

        try {
            OpenCms.getScheduleManager().scheduleJob(getCms(), m_jobInfo);
            getSettings().setHtmlList(null);
            // update the XML configuration
            OpenCms.writeConfiguration(CmsSystemConfiguration.class);
        } catch (CmsRoleViolationException e) {
            errors.add(e);    
        } catch (CmsSchedulerException e) {
            errors.add(e);
        }
        setOtherErrors(errors);
    }
    
    /**
     * Returns the job id parameter.<p>
     * 
     * @return the job id parameter
     */
    public String getParamJobid() {

        return m_paramJobid;
    }
    
    /**
     * Returns the job name parameter.<p>
     * 
     * @return the job name parameter
     */
    public String getParamJobname() {

        return m_paramJobname;
    }
    
    /**
     * Sets the job id parameter.<p>
     * 
     * @param jobid the job id parameter
     */
    public void setParamJobid(String jobid) {

        m_paramJobid = jobid;
    }
    
    /**
     * Sets the job name parameter.<p>
     * 
     * @param jobname the job name parameter
     */
    public void setParamJobname(String jobname) {

        m_paramJobname = jobname;
    }
    
    /**
     * Creates the dialog HTML for all defined widgets of the named dialog (page).<p>  
     * 
     * @param dialog the dialog (page) to get the HTML for
     * @return the dialog HTML for all defined widgets of the named dialog (page)
     */
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        // create table
        result.append(createWidgetTableStart());
        
        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());
        
        if (dialog.equals(PAGES[0])) {
            result.append(createDialogRowsHtml(0, 4));
            result.append(dialogBlockStart("User context"));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(5, 11));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        } else if (dialog.equals(PAGES[1])) {
            result.append(dialogBlockStart("Parameters"));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(12, 12));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }
        
        // close table
        result.append(createWidgetTableEnd());

        return result.toString();
    }

    /**
     * Creates the list of widgets for this dialog.<p>
     */
    protected void defineWidgets() {

        initScheduledJobObject();
        // required to read the default values for the optional context parameters
        CmsContextInfo dC = new CmsContextInfo();
        
        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "jobName", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "className", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "cronExpression", PAGES[0], new CmsInputWidget()));       
        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "reuseInstance", PAGES[0], new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "active", PAGES[0], new CmsCheckboxWidget()));

        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "contextInfo.userName", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "contextInfo.projectName", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "contextInfo.siteRoot", dC.getSiteRoot(), PAGES[0], new CmsVfsFileWidget(), 0, 1));
        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "contextInfo.requestedUri", dC.getRequestedUri(), PAGES[0], new CmsVfsFileWidget(), 0, 1));
        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "contextInfo.localeName", dC.getLocaleName(), PAGES[0], new CmsInputWidget(), 0, 1));
        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "contextInfo.encoding", dC.getEncoding(), PAGES[0], new CmsInputWidget(), 0, 1));
        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "contextInfo.remoteAddr", dC.getRemoteAddr(), PAGES[0], new CmsInputWidget(), 0, 1));  
        
        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "parameters", PAGES[1], new CmsInputWidget())); 
    }
    
    /**
     * @see org.opencms.workplace.CmsDialog#getCancelAction()
     */
//    public String getCancelAction() {
//
//        // set the default action
//        setParamPage((String)getPages().get(0));
//        
//        return DIALOG_SET;
//    }
    
    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    protected String[] getPageArray() {

        return PAGES;
    }
    
    /**
     * Initializes the scheduled job object to work with depending on the dialog state and request parameters.<p>
     */
    protected void initScheduledJobObject() {
        
        Object o;
        
        boolean setActive = false;
        
        if (CmsStringUtil.isEmpty(getParamAction()) || CmsDialog.DIALOG_INITIAL.equals(getParamAction())) {
            // this is the initial dialog call
            if (CmsStringUtil.isNotEmpty(getParamJobid())) {
                // edit an existing job, get it from manager
                setActive = OpenCms.getScheduleManager().getJob(getParamJobid()).isActive();
                o = OpenCms.getScheduleManager().getJob(getParamJobid()).clone();
            } else {
                // create a new job
                o = null;
            } 
        } else {
            // this is not the initial call, get job object from session
            o = getDialogObject();
        }
        
        if (!(o instanceof CmsScheduledJobInfo)) {
            // create a new job info
            m_jobInfo = new CmsScheduledJobInfo();
            m_jobInfo.setContextInfo(new CmsContextInfo());
        } else {
            // reuse job info object stored in session
            m_jobInfo = (CmsScheduledJobInfo)o;            
        }
        
        if (setActive) {
            m_jobInfo.setActive(true);
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);

        super.initWorkplaceRequestValues(settings, request);
        
        // save the current state of the job (may be changed because of the widget values)
        setDialogObject(m_jobInfo);        
    }
}
