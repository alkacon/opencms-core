/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/Attic/CmsHistory.java,v $
 * Date   : $Date: 2005/09/02 08:31:28 $
 * Version: $Revision: 1.20 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.opencms.workplace.commons;

import org.opencms.file.CmsBackupProject;
import org.opencms.file.CmsBackupResource;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods for displaying the history file dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/commons/history.jsp
 * </ul>
 * <p>
 *
 * @author  Armen Markarian 
 * 
 * @version $Revision: 1.20 $ 
 * 
 * @since 6.0.0 
 */
public class CmsHistory extends CmsDialog {

    /** Value for the action: restore the version. */
    public static final int ACTION_RESTORE_VERSION = 100;
    
    /** The dialog type. */
    public static final String DIALOG_TYPE = "history";
    
    /** Request parameter value for the action: restore the version. */
    public static final String DIALOG_RESTORE = "restore";
    
    /** Request parameter name for versionid. */
    public static final String PARAM_VERSIONID = "versionid";
    
    private String m_paramVersionid;
    
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsHistory(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsHistory(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }        
    
    /**
     * Performs the restore action, will be called by the JSP page.<p>
     * 
     * @throws JspException if including a JSP subelement is not successful
     */
    public void actionRestore() throws JspException {
        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);
        if (getParamVersionid() != null) {
            try {
                performRestoreOperation();
            } catch (Throwable e) {
                // Cms error defining property, show error dialog
                includeErrorpage(this, e);                  
            }    
        }              
        
        

    }  
    
    /**
     * Builds a list of Versions of the specified resource.<p>
     *  
     * @throws CmsException if operation was not successful
     * 
     * @return the HTML String for the Version list
     */
    public String buildVersionList() throws CmsException {
        
        StringBuffer result = new StringBuffer();
        String resource = getParamResource();
        // build the resource version rows
        if (resource != null) {
            result.append("<table border=\"0\" width=\"100%\" cellpadding=\"1\" cellspacing=\"1\">\n");
            result.append("<tr>\n");
            result.append("\t<td style=\"width:10%;\" class=\"textbold\">");
            // Version No
            result.append(key(Messages.GUI_LABEL_VERSION_0));
            result.append("</td>\n");
            result.append("\t<td style=\"width:10%;\" class=\"textbold\">");
            // Type
            result.append(key(Messages.GUI_LABEL_TYPE_0));
            result.append("</td>\n");   
            result.append("\t<td style=\"width:20%;\" class=\"textbold\">");
            // Publised at
            result.append(key(Messages.GUI_HISTORY_LAST_PUBLISHED_AT_0));
            result.append("</td>\n");  
            result.append("\t<td style=\"width:20%;\" class=\"textbold\">");
            // Edited at
            result.append(key(Messages.GUI_HISTORY_LAST_EDITED_AT_0));
            result.append("</td>\n"); 
            result.append("\t<td style=\"width:20%;\" class=\"textbold\">");
            // Edited by
            result.append(key(Messages.GUI_HISTORY_EDITED_BY_0));
            result.append("</td>\n");
            result.append("\t<td style=\"width:10%;\" class=\"textbold\">");
            // Size
            result.append(key(Messages.GUI_LABEL_SIZE_0));
            result.append("</td>\n");
            result.append("\t<td style=\"text-align:right; width:10%;\" class=\"textbold\">");
            result.append("&nbsp;");
            result.append("</td>\n");   
            result.append("</tr>\n");
            result.append("<tr><td colspan=\"8\"><span style=\"height: 6px;\">&nbsp;</span></td></tr>\n");
            
            // get all backup resources and build the table rows
            List backupFileHeaders = getCms().readAllBackupFileHeaders(resource);           
            Iterator i = backupFileHeaders.iterator();
            while (i.hasNext()) {
                CmsBackupResource file = (CmsBackupResource)i.next();                
                // the tagId for get the Backup project 
                int tagId = file.getTagId();
                int version = file.getVersionId();
                CmsBackupProject project = getCms().readBackupProject(tagId);
                int versionId = project.getVersionId();
                String filetype = OpenCms.getResourceManager().getResourceType(file.getTypeId()).getTypeName();
                long dateLastModified = file.getDateLastModified();                
                long dateLastPublished = project.getPublishingDate();
                // last edited by
                String userName = "";
                try {
                    userName = getCms().readUser(file.getUserLastModified()).getName();
                } catch (CmsException e) {
                    userName = file.getLastModifiedByName();
                }
                result.append("<tr>\n");
                result.append("\t<td>");
                result.append(version);
                result.append("</td>\n");   
                result.append("\t<td>");
                result.append("<img src=\"");
                result.append(getSkinUri());
                result.append("filetypes/");
                result.append(filetype);
                result.append(".gif\">");                
                result.append("</td>\n");   
                result.append("\t<td>");                
                result.append(getMessages().getDateTime(dateLastPublished));
                result.append("</td>\n");   
                result.append("\t<td>");
                result.append(getMessages().getDateTime(dateLastModified));
                result.append("</td>\n");   
                result.append("\t<td>");
                result.append(userName);
                result.append("</td>\n");   
                result.append("\t<td>");
                result.append(file.getLength());
                result.append("</td>\n");   
                result.append("<td align=\"right\">");                
                result.append("<table class=\"buttonbackground\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n");
                result.append("\t<tr>\n");
                result.append(button("javascript:viewVersion('"+file.getRootPath()+"','"+versionId+"')", null, "preview.png", "messagebox.title.viewresource", 0));
                result.append(button("javascript:restore('"+versionId+"')", null, "publish.png", "messagebox.title.restoreresource", 0));                
                result.append("</tr>\n");
                result.append("</table>\n");
                result.append("</td>\n");
                result.append("</tr>\n");                           
            }             
            result.append("</table>\n");
        }
        
        return result.toString();
    }   
    
    /**
     * Returns the paramVersionid.<p>
     *
     * @return the paramVersionid
     */
    public String getParamVersionid() {

        return m_paramVersionid;
    }
    
    /**
     * Sets the paramVersionid.<p>
     *
     * @param paramVersionid the paramVersionid to set
     */
    public void setParamVersionid(String paramVersionid) {

        m_paramVersionid = paramVersionid;
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
        if (DIALOG_RESTORE.equals(getParamAction())) {
            setAction(ACTION_RESTORE_VERSION);                            
        } else if (DIALOG_CANCEL.equals(getParamAction())) {          
            setAction(ACTION_CANCEL);
        } else {                        
            setAction(ACTION_DEFAULT);
            // build title for copy dialog     
            setParamTitle(key(Messages.GUI_HISTORY_OF_1, new Object[] {CmsResource.getName(getParamResource())}));
        }      
    } 
    
    /**
     * Restores a backed up resource version.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    protected void performRestoreOperation() throws CmsException {

        CmsResource res = getCms().readResource(getParamResource());
        String resourcename = getCms().getSitePath(res);
        int tagId = Integer.parseInt(getParamVersionid());
        checkLock(getParamResource());
        getCms().restoreResourceBackup(resourcename, tagId);
    }
    
   
}
