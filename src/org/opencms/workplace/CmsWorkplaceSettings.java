/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsWorkplaceSettings.java,v $
 * Date   : $Date: 2003/07/04 07:25:16 $
 * Version: $Revision: 1.4 $
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

import com.opencms.file.CmsUser;


/**
 * Object to conveniently access and modify the state of the workplace for a user,
 * will be stored in the session of a user.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.4 $
 * 
 * @since 5.1
 */
public class CmsWorkplaceSettings {
    
    private String m_language;
    private CmsWorkplaceMessages m_messages;
    private int m_project;
    private String m_group;
    private CmsUser m_user; 
    private String m_currentView;
    private String m_explorerFolder;
    private String m_explorerMode;
    private int m_explorerPage = 1;
    private boolean m_explorerContext = true;
    private int m_explorerChecksum = -1;
    private String m_explorerFlaturl;
    private String m_fileUri;
    private String m_detailView;
    
    private String m_errorTitle;
    private String m_errorMessage;
    private String m_errorReason;
    private String m_errorSuggestion;
    private String m_errorDetails;
        
    /**
     * Constructor, only package visible.<p>
     */
    CmsWorkplaceSettings() {}
    
    /**
     * Returns the currently selected user language.<p>
     * 
     * @return the currently selected user language
     */
    public synchronized String getLanguage() {
        return m_language;
    }

    /**
     * Sets the selected user language.<p>
     * 
     * @param value the selected user language
     */
    public synchronized void setLanguage(String value) {
        m_language = value;
    }

    /**
     * Returns the initialized workplace messages for the current user.<p>
     * 
     * @return the initialized workplace messages for the current user
     */
    public CmsWorkplaceMessages getMessages() {
        return m_messages;
    }

    /**
     * Sets the workplace messages for the current user.<p>
     * 
     * @param messages the workplace messages for the current user
     */
    public synchronized void setMessages(CmsWorkplaceMessages messages) {
        m_messages = messages;
    }
    

    /**
     * Returns the current view selected in the workplace.<p>
     * 
     * @return the current view selected in the workplace 
     */
    public synchronized String getCurrentView() {
        return m_currentView;
    }

    /**
     * Sets the view for the workplace.<p>
     * 
     * @param string the view for the workplace
     */
    public synchronized void setCurrentView(String string) {
        m_currentView = string;
    }

    /**
     * Returns the currently selected default group of the workplace user.<p>
     * 
     * @return the currently selected default group of the workplace user
     */
    public synchronized String getGroup() {
        return m_group;
    }

    /**
     * Sets the default group of the workplace user.<p>
     * 
     * @param group the default group of the workplace user
     */
    public synchronized void setGroup(String group) {
        m_group = group;
    }

    /**
     * Returns the currently selected project of the workplace user.<p> 
     * 
     * @return the currently selected project of the workplace user
     */
    public synchronized int getProject() {
        return m_project;
    }

    /**
     * Sets the currently selected project of the workplace user.<p>
     * 
     * @param project the currently selected project of thw workplace user
     */
    public synchronized void setProject(int project) {
        m_project = project;
    }

    /**
     * Returns the current workplace user.<p>
     * 
     * @return the current workplace user
     */
    public synchronized CmsUser getUser() {
        return m_user;
    }

    /**
     * Sets the current workplace user.<p>
     * 
     * @param user the current workplace user
     */
    public synchronized void setUser(CmsUser user) {
        m_user = user;
    }
        
    /**
     * Returns the current folder to be displayed in the explorer.<p>
     * 
     * @return the current folder to be displayed in the explorer
     */
    public synchronized String getExplorerFolder() {
        return m_explorerFolder;
    }
    
    /**
     * Sets the current folder to be displayed in the explorer.<p>
     * 
     * @param value the current folder to be displayed in the explorer
     */
    public synchronized void setExplorerFolder(String value) {
        m_explorerFolder = value;
    }
    
    /**
     * Returns the current explorer mode.<p> 
     * 
     * @return the current explorer mode
     */
    public synchronized String getExplorerMode() {
        return m_explorerMode;
    }

    /**
     * Sets the current explorer mode.<p>
     * 
     * @param value the current explorer mode
     */
    public synchronized void setExplorerMode(String value) {
        m_explorerMode = value;
    }

    /**
     * Returns the currently selected page in the explorer view.<p>
     * 
     * @return the currently selected page in the explorer view
     */
    public synchronized int getExplorerPage() {
        return m_explorerPage;
    }

    /**
     * Sets the currently selected page in the explorer view.<p>
     * 
     * @param page the currently selected page in the explorer view
     */
    public synchronized void setExplorerPage(int page) {
        m_explorerPage = page;
    }

    /**
     * Returns the explorer checksum.<p>
     * 
     * @return the explorer checksum
     */
    public int getExplorerChecksum() {
        return m_explorerChecksum;
    }

    /**
     * Sets the explorer checksum.<p>
     * 
     * @param value the explorer checksum
     */
    public void setExplorerChecksum(int value) {
        m_explorerChecksum = value;
    }

    /**
     * Returns true if the explorer context menues are enabled.<p>
     * 
     * @return true if the explorer context menues are enabled
     */
    public boolean getExplorerContext() {
        return m_explorerContext;
    }

    /**
     * Controls if the explorer context menues are enabled
     * 
     * @param value if true, the explorer context menues are enabled
     */
    public void setExplorerContext(boolean value) {
        m_explorerContext = value;
    }

    /**
     * Returns the explorer flat url.<p>
     *  
     * @return the explorer flat url
     */
    public String getExplorerFlaturl() {
        return m_explorerFlaturl;
    }

    /**
     * Sets the explorer flat url.<p>
     * 
     * @param value the explorer flat url
     */
    public void setExplorerFlaturl(String value) {
        m_explorerFlaturl = value;
    }
    
    /**
     * Returns the current file name which is selected.<p>
     * 
     * @return the file URI
     */
    public String getFileUri() {
        return m_fileUri;
    }
    
    /**
     * Sets the current file name which is selected.<p>
     * 
     * @param value the current file name in the vfs
     */
    public void setFileUri(String value) {
        m_fileUri = value;
    }
    
    /**
     * Returns the current detail grade of the view.<p>
     *  
     * @return value of the details.
     */
    public String getDetailView() {
        return m_detailView;
    }
    
    /**
     * Sets the current detail grade of the view.<p>
     * 
     * @param value the current details.
     */
    public void setDetailView(String value) {
        m_detailView = value;
    }
    
    /**
     * Returns the current title for the error page.<p>
     *  
     * @return value of the title.
     */
    public String getErrorTitle() {
        return m_errorTitle;
    }

    /**
     * Sets the current title for the error page.<p>
     * 
     * @param value the current title.
     */
    public void setErrorTitle(String value) {
        m_errorTitle = value;
    }
    
    /**
     * Returns the current message for the error page.<p>
     *  
     * @return value of the message.
     */
    public String getErrorMessage() {
        return m_errorMessage;
    }

    /**
     * Sets the current message for the error page.<p>
     * 
     * @param value the current message.
     */
    public void setErrorMessage(String value) {
        m_errorMessage = value;
    }
    
    /**
     * Returns the current reason for the error page.<p>
     *  
     * @return value of the reason.
     */
    public String getErrorReason() {
        return m_errorReason;
    }

    /**
     * Sets the current reason for the error page.<p>
     * 
     * @param value the current reason.
     */
    public void setErrorReason(String value) {
        m_errorReason = value;
    }
    
    /**
     * Returns the current suggestion for the error page.<p>
     *  
     * @return value of the suggestion.
     */
    public String getErrorSuggestion() {
        return m_errorSuggestion;
    }

    /**
     * Sets the current suggestion for the error page.<p>
     * 
     * @param value the current suggestion.
     */
    public void setErrorSuggestion(String value) {
        m_errorSuggestion = value;
    }

    /**
     * Returns the current details for the error page.<p>
     *  
     * @return value of the details.
     */
    public String getErrorDetails() {
        return m_errorDetails;
    }

    /**
     * Sets the current details for the error page.<p>
     * 
     * @param value the current details.
     */
    public void setErrorDetails(String value) {
        m_errorDetails = value;
    }
        
}
