/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/configuration/CmsDefaultUserSettings.java,v $
 * Date   : $Date: 2004/05/13 13:58:10 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
 
package org.opencms.configuration;

import org.opencms.db.CmsUserSettings;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.I_CmsConstants;

/**
 * Default user workplace settings, used as default values for worklace settings in the
 * user preferences.<p>
 *  
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.1 $
 */
public class CmsDefaultUserSettings extends CmsUserSettings {
   
    /** Parameter for buttonstlye text & image */
    private static int C_BUTTONSTYLE_TEXTIMAGE = 1;
    
    /** value for preserving siblings in copy dialog settings */
    private static String C_COPYMODE_PRESERVE = "preservesiblings";

    /** value for creating a sibling in copy dialog settings */
    private static String C_COPYMODE_SIBLING = "createsibling";

    /** value for deleteing siblings in delete dialog settings */
    private static String C_DELETEMODE_DELETE = "deletesiblings";
    
    /** value for publishing siblings in publish dialog settings */
    private static String C_PUBLISHMODE_SIBLINGS = "allsiblings"; 
    
    /** value for "true" */
    private static String C_TRUEVALUE = "true";
    
    /**  array of the possible "button styles */
    public  static final String[] m_buttonStyles = {
            "image",
            "textimage",
            "text",
    };

    /** array list for fast lookup */
    public static final java.util.List m_buttonStyle =
        java.util.Arrays.asList(m_buttonStyles);   

    /**  array of the "task startupfilter" nicenames */
    public  static final String[] m_startupFilterNames = {
            "mynewtasks",
            "mytasksformyroles",
            "alltasks",
            "myactivetasks",
            "myactivetasksformyroles",
            "allactivetasks",
            "mycompletedtasks",
            "mycompletedtasksformyroles",
            "allcompletedtasks",
            "newtaskscreatedbyme",
            "activetaskscreatedbyme",
            "completedtaskscreatedbyme"
    };
    
    /** array list for fast lookup */
    public static final java.util.List m_startupFilterName =
        java.util.Arrays.asList(m_startupFilterNames);  
    
        /**  array of the "task startupfilter" values */
    public  static final String[] m_startupFilterValues = {
            "a1",
            "b1",
            "c1",
            "a2",
            "b2",
            "c2",
            "a3",
            "b3",
            "c3",
            "d1",
            "d2",
            "d3"
    };
        
    /**
     * Sets the default copy mode when copying a file of the user.<p>
     * 
     * @param mode the default copy mode when copying a file of the user
     */
    public void setDialogCopyFileMode(String mode) {
          int copyMode = I_CmsConstants.C_COPY_AS_NEW;
          if (mode.equalsIgnoreCase(C_COPYMODE_SIBLING)) {
            copyMode = I_CmsConstants.C_COPY_AS_SIBLING;
          }
          setDialogCopyFileMode(copyMode);        
    }
    
    /**
     * Sets the default copy mode when copying a folder of the user.<p>
     * 
     * @param mode the default copy mode when copying a folder of the user
     */
    public void setDialogCopyFolderMode(String mode) {
        int copyMode = I_CmsConstants.C_COPY_AS_NEW;
        if (mode.equalsIgnoreCase(C_COPYMODE_SIBLING)) {
            copyMode = I_CmsConstants.C_COPY_AS_SIBLING;
        } else  if (mode.equalsIgnoreCase(C_COPYMODE_PRESERVE)) {
            copyMode = I_CmsConstants.C_COPY_PRESERVE_SIBLING;
        }
        setDialogCopyFolderMode(copyMode);           
    }
    
    
    /**
     * Sets the default setting for file deletion.<p>
     * 
     * @param mode the default setting for file deletion
     */
    public void setDialogDeleteFileMode(String mode) {
        int deleteMode = I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS;
        if (mode.equalsIgnoreCase(C_DELETEMODE_DELETE)) {
            deleteMode = I_CmsConstants.C_DELETE_OPTION_DELETE_SIBLINGS;
          }
        setDialogDeleteFileMode(deleteMode);
    }
    
     /**
     * Sets the default setting for direct publishing.<p>
     * 
     * @param mode the default setting for direct publishing
     */
    public void setDialogPublishSiblings(String mode) {
        boolean publishSiblings = false;
        if (mode.equalsIgnoreCase(C_PUBLISHMODE_SIBLINGS)) {
            publishSiblings = true;
          }
       setDialogPublishSiblings(publishSiblings);
    }
    
    
     /**
     * Sets the style of the direct edit buttons of the user.<p>
     * 
     * @param buttonstyle the style of the direct edit buttons of the user
     */
    public void setDirectEditButtonStyle(String buttonstyle) {
        int buttonstyleValue = C_BUTTONSTYLE_TEXTIMAGE;
        try {
            if (buttonstyle != null) {
                buttonstyleValue = m_buttonStyle.indexOf(buttonstyle);
            }
        } catch (Exception e) {
            // do nothing, use the default value
        }
        setDirectEditButtonStyle(buttonstyleValue);
    }  
    
    /**
     * Sets the style of the editor buttons of the user.<p>
     * 
     * @param buttonstyle the style of the editor buttons of the user
     */
    public void setEditorButtonStyle(String buttonstyle) {
        int buttonstyleValue = C_BUTTONSTYLE_TEXTIMAGE;
        try {
            if (buttonstyle != null) {
                buttonstyleValue = m_buttonStyle.indexOf(buttonstyle);
            }
        } catch (Exception e) {
            // do nothing, use the default value
        }
        setEditorButtonStyle(buttonstyleValue);
    }    
    
    
    /**
     * Sets the style of the explorer workplace buttons of the user.<p>
     * 
     * @param buttonstyle the style of the explorer workplace buttons of the user
     */
    public void setExplorerButtonStyle(String buttonstyle) {
        int buttonstyleValue = C_BUTTONSTYLE_TEXTIMAGE;
        try {
            if (buttonstyle != null) {
                buttonstyleValue = m_buttonStyle.indexOf(buttonstyle);
            }
        } catch (Exception e) {
            // do nothing, use the default value
        }
        setExplorerButtonStyle(buttonstyleValue);
    }
    
    
   /**
    * Sets the number of displayed files per page of the user.<p>
    * 
    * @param entries the number of displayed files per page of the user
    */
   public void setExplorerFileEntries(String entries) {
        try {
            setExplorerFileEntries(Integer.parseInt(entries));
        } catch (Throwable t) {
            // ignore this exception
        }
   }

   /**
    * Sets the workplace locale.<p> 
    * 
    * @param locale the workplace language default
    */
   public void setLocale(String locale) {
       // set the language     
       setLocale(CmsLocaleManager.getLocale(locale));
    }
   
     /**
     * Sets if the file creation date should be shown in explorer view.<p>
     * 
     * @param show true if the file creation date should be shown, otherwise false
     */
    public void setShowExplorerFileDateCreated(String show) {
        setShowExplorerFileDateCreated(Boolean.valueOf(show).booleanValue());
    }

    /**
     * Sets if the file last modified date should be shown in explorer view.<p>
     * 
     * @param show true if the file last modified date should be shown, otherwise false
     */
    public void setShowExplorerFileDateLastModified(String show) {
        setShowExplorerFileDateLastModified(Boolean.valueOf(show).booleanValue());
    }

    /**
     * Sets if the file locked by should be shown in explorer view.<p>
     * 
     * @param show true if the file locked by should be shown, otherwise false
     */
    public void setShowExplorerFileLockedBy(String show) {
         setShowExplorerFileLockedBy(Boolean.valueOf(show).booleanValue());
    }

    /**
     * Sets if the file permissions should be shown in explorer view.<p>
     * 
     * @param show true if the file permissions should be shown, otherwise false
     */
    public void setShowExplorerFilePermissions(String show) {
        setShowExplorerFilePermissions(Boolean.valueOf(show).booleanValue());
    }
    
    /**
     * Sets if the file size should be shown in explorer view.<p>
     * 
     * @param show true if the file size should be shown, otherwise false
     */
    public void setShowExplorerFileSize(String show) {
        setShowExplorerFileSize(Boolean.valueOf(show).booleanValue());
    }

    /**
     * Sets if the file state should be shown in explorer view.<p>
     * 
     * @param show true if the state size should be shown, otherwise false
     */
    public void setShowExplorerFileState(String show) {
        setShowExplorerFileState(Boolean.valueOf(show).booleanValue());
    }

    /**
     * Sets if the file title should be shown in explorer view.<p>
     * 
     * @param show true if the file title should be shown, otherwise false
     */
    public void setShowExplorerFileTitle(String show) {
        setShowExplorerFileTitle(Boolean.valueOf(show).booleanValue());
    }

    /**
     * Sets if the file type should be shown in explorer view.<p>
     * 
     * @param show true if the file type should be shown, otherwise false
     */
    public void setShowExplorerFileType(String show) {
        setShowExplorerFileType(Boolean.valueOf(show).booleanValue());
    }

    /**
     * Sets if the file creator should be shown in explorer view.<p>
     * 
     * @param show true if the file creator should be shown, otherwise false
     */
    public void setShowExplorerFileUserCreated(String show) {
        setShowExplorerFileUserCreated(Boolean.valueOf(show).booleanValue());
    }

   /**
    * Sets if the file last modified by should be shown in explorer view.<p>
    * 
    * @param show true if the file last modified by should be shown, otherwise false
    */
   public void setShowExplorerFileUserLastModified(String show) {
        setShowExplorerFileUserLastModified(Boolean.valueOf(show).booleanValue());
   }
    
     /**
     *  Sets if the lock dialog should be shown.<p>
     * 
     * @param mode true if the lock dialog should be shown, otherwise false
     */
    public void setShowLockDialog(String mode) {
        boolean showLock = false;
        if (mode.equalsIgnoreCase(C_TRUEVALUE)) {
            showLock = true;
          }
        setDialogShowLock(showLock);
    }
    
        /**
     * Sets if a message should be sent if the task is accepted.<p>
     * 
     * @param mode true if a message should be sent if the task is accepted, otherwise false
     */
    public void setTaskMessageAccepted(String mode) {
        boolean messageAccepted = false;
        if (mode.equalsIgnoreCase(C_TRUEVALUE)) {
            messageAccepted = true;
          }
        setTaskMessageAccepted(messageAccepted);             
    }

    /**
     * Sets if a message should be sent if the task is completed.<p>
     * 
     * @param mode true if a message should be sent if the task is completed, otherwise false
     */
    public void setTaskMessageCompleted(String mode) {
        boolean messageCompleted = false;
        if (mode.equalsIgnoreCase(C_TRUEVALUE)) {
            messageCompleted = true;
          }
        setTaskMessageCompleted(messageCompleted); 
    }

    /**
     * Sets if a message should be sent if the task is forwarded.<p>
     * 
     * @param mode true if a message should be sent if the task is forwarded, otherwise false
     */
    public void setTaskMessageForwarded(String mode) {
        boolean messageForwarded = false;
        if (mode.equalsIgnoreCase(C_TRUEVALUE)) {
            messageForwarded = true;
          }
        setTaskMessageForwarded(messageForwarded); 
    }

    /**
     * Sets if all role members should be informed about the task.<p>
     * 
     * @param mode true if all role members should be informed about the task, otherwise false
     */
    public void setTaskMessageMembers(String mode) {
        boolean informMembers = false;
        if (mode.equalsIgnoreCase(C_TRUEVALUE)) {
            informMembers = true;
          }
        setTaskMessageMembers(informMembers);        
    }
    
    
    /**
     * Sets if all projects should be shown in tasks view.<p>
     * 
     * @param mode true if all projects should be shown in tasks view, otherwise false
     */
    public void setTaskShowAllProjects(String mode) {
        boolean showProjetcs = false;
        if (mode.equalsIgnoreCase(C_TRUEVALUE)) {
            showProjetcs = true;
          }
        setTaskShowAllProjects(showProjetcs);
        
    }
    
    /**
     * Sets the startup filter for the tasks view.<p>
     * 
     * @param filter the startup filter for the tasks view
     */
    public void setTaskStartupFilterDefault(String filter) {
        int defaultFilter = 0;
        try {
            if (filter != null) {
                defaultFilter = m_startupFilterName.indexOf(filter);
            }
        } catch (Exception e) {
            // do nothing, use the default value
        }
        setTaskStartupFilter(m_startupFilterValues[defaultFilter]);
    }   
   
   
   
    /**
     * Sets the usage of the upload applet for the user user.<p>
     * 
     * @param applet "true" or "false" to flag the use of the applet
     */
    public void setUploadApplet(String applet) {
        // set the usage of the upload applet
        setUseUploadApplet(Boolean.valueOf(applet).booleanValue());  
    }
    
    
    /**
     * Sets the style of the  workplace buttons of the user.<p>
     * 
     * @param buttonstyle the style of the  workplace buttons of the user
     */
    public void setWorkplaceButtonStyle(String buttonstyle) {
        int buttonstyleValue = C_BUTTONSTYLE_TEXTIMAGE; 
        
        try {
            if (buttonstyle != null) {
                buttonstyleValue = m_buttonStyle.indexOf(buttonstyle);
            }
        } catch (Exception e) {
            // do nothing, use the default value
        }

        setWorkplaceButtonStyle(buttonstyleValue);
    }
}
