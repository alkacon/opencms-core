/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/A_CmsWpElement.java,v $
 * Date   : $Date: 2000/03/09 16:46:06 $
 * Version: $Revision: 1.19 $
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

package com.opencms.workplace;

import com.opencms.core.*;
import com.opencms.file.*;
import com.opencms.template.*;

/**
 * Abstract class for all workplace elements.
 * <P>
 * Any class called by CmsXmlTemplateFile for handling special workplace
 * XML tags (e.g. <code>&lt;BUTTON&gt;</code> or <code>&lt;LABEL&gt;</code>)
 * has to extend this class.
 * <P>
 * This class contains basic functionality for loading and caching definition
 * files for workplace elements, such as <code>CmsXmlWpButtonsDefFile</code> or 
 * <code>CmsXmlWpLabelDefFile</code>
 * 
 * @author Alexander Lucas
 * @author Michael Emmerich
 * @version $Revision: 1.19 $ $Date: 2000/03/09 16:46:06 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public abstract class A_CmsWpElement implements I_CmsLogChannels, I_CmsWpElement, I_CmsWpConstants {
    
    /**
     * Reference to to buttons definition file
     */
    protected static CmsXmlWpButtonsDefFile m_buttondef = null;
    
    /**
     * Reference to icons definition file
     */
    protected static CmsXmlWpTemplateFile m_icondef = null;
	
    /**
     * Reference to projectlist definition file
     */
    protected static CmsXmlWpTemplateFile m_projectlistdef = null;
	
    /**
     * Reference to projectlist definition file
     */
    protected static CmsXmlWpTemplateFile m_tasklistdef = null;
    
     /**
     * Reference to the panel bar defintion file
     */
    protected static CmsXmlWpTemplateFile m_paneldef = null;
    
    /**
     * Reference to taskdocu definition file
     */
    protected static CmsXmlWpTemplateFile m_taskdocudef = null;    
    
	/**
     * Reference to projectlist definition file
     */
    protected static CmsXmlWpTemplateFile m_contextdef = null;
	
	/**
     * Reference to the label defintion file
     */
    protected static CmsXmlWpLabelDefFile m_labeldef = null;
 
     /**
     * Reference to the input defintion file
     */
    protected static CmsXmlWpInputDefFile m_inputdef = null;

    
     /**
     * Reference to the error defintion file
     */
    protected static CmsXmlWpErrorDefFile m_errordef = null;

     /**
     * Reference to the radio button defintion file
     */
    protected static CmsXmlWpRadioDefFile m_radiodef = null;
    
     /**
     * Reference to the box defintion file
     */
    protected static CmsXmlWpBoxDefFile m_boxdef = null;
    
    /**
     * Path to all worplace definition files (will be read once
     * from workplace.ini)
     */
    protected static String m_workplaceElementPath = null;
    
    
    /** Reference to the config file */
    private static CmsXmlWpConfigFile m_configFile = null;    

     /**
     * Gets a reference to the default config file.
     * The path to this file ist stored in <code>C_WORKPLACE_INI</code>
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @return Reference to the config file.
     * @exception CmsException
     */
    public CmsXmlWpConfigFile getConfigFile(A_CmsObject cms) throws CmsException {
        //if(m_configFile == null) {
            m_configFile = new CmsXmlWpConfigFile(cms);
        //}
        return m_configFile;
    }
    
    
    /**
     * Reads the buttons definition file.
     * @param cms The actual cms object
     * @return Reference to the buttons defintion file.
     * @exception CmsException
     */
    public CmsXmlWpButtonsDefFile getButtonDefinitions(A_CmsObject cms) throws CmsException {
        //if(m_buttondef == null) {
            if(m_workplaceElementPath == null) {
                CmsXmlWpConfigFile configFile = new CmsXmlWpConfigFile(cms);
                m_workplaceElementPath = configFile.getWorkplaceElementPath();
            }
            m_buttondef = new CmsXmlWpButtonsDefFile(cms, m_workplaceElementPath + C_BUTTONTEMPLATE);
        //}
        return m_buttondef;
    }
    
    /**
     * Reads the icons definition file.
     * @param cms The actual cms object
     * @return Reference to the icons defintion file.
     * @exception CmsException
     */
    public CmsXmlWpTemplateFile getIconDefinitions(A_CmsObject cms) throws CmsException {
        //if(m_icondef == null) {
            if(m_workplaceElementPath == null) {
                CmsXmlWpConfigFile configFile = new CmsXmlWpConfigFile(cms);
                m_workplaceElementPath = configFile.getWorkplaceElementPath();
            }
            m_icondef = new CmsXmlWpTemplateFile(cms, m_workplaceElementPath + C_ICON_TEMPLATEFILE);
        //}
        return m_icondef;
    }
	
    /**
     * Reads the projectlist definition file.
     * @param cms The actual cms object
     * @return Reference to the list defintion file.
     * @exception CmsException
     */
    public CmsXmlWpTemplateFile getProjectlistDefinitions(A_CmsObject cms) throws CmsException {
        //if(m_projectlistdef == null) {
            if(m_workplaceElementPath == null) {
                CmsXmlWpConfigFile configFile = new CmsXmlWpConfigFile(cms);
                m_workplaceElementPath = configFile.getWorkplaceElementPath();
            }
            m_projectlistdef = new CmsXmlWpTemplateFile(cms, m_workplaceElementPath + C_PROJECTLIST_TEMPLATEFILE);
        //}
        return m_projectlistdef;
    }
	
    /**
     * Reads the projectlist definition file.
     * @param cms The actual cms object
     * @return Reference to the list defintion file.
     * @exception CmsException
     */
    public CmsXmlWpTemplateFile getTaskListDefinitions(A_CmsObject cms) throws CmsException {
        //if(m_tasklistdef == null) {
            if(m_workplaceElementPath == null) {
                CmsXmlWpConfigFile configFile = new CmsXmlWpConfigFile(cms);
                m_workplaceElementPath = configFile.getWorkplaceElementPath();
            }
            m_tasklistdef = new CmsXmlWpTemplateFile(cms, m_workplaceElementPath + C_TASKLIST_TEMPLATEFILE);
        //}
        return m_tasklistdef;
    }
	
    /**
     * Reads the contextmenue definition file.
     * @param cms The actual cms object
     * @return Reference to the list defintion file.
     * @exception CmsException
     */
    public CmsXmlWpTemplateFile getContextmenueDefinitions(A_CmsObject cms) throws CmsException {
        //if(m_contextdef == null) {
            if(m_workplaceElementPath == null) {
                CmsXmlWpConfigFile configFile = new CmsXmlWpConfigFile(cms);
                m_workplaceElementPath = configFile.getWorkplaceElementPath();
            }
            m_contextdef = new CmsXmlWpTemplateFile(cms, m_workplaceElementPath + C_CONTEXTMENUE_TEMPLATEFILE);
        //}
        return m_contextdef;
    }	
	
	/**
     * Reads the label definition file.
     * @param cms The actual cms object
     * @return Reference to the label defintion file.
     * @exception CmsException
     */
     public CmsXmlWpLabelDefFile getLabelDefinitions(A_CmsObject cms) throws CmsException {
        //if(m_labeldef == null) {
            if(m_workplaceElementPath == null) {
                CmsXmlWpConfigFile configFile = new CmsXmlWpConfigFile(cms);
                m_workplaceElementPath = configFile.getWorkplaceElementPath();
            }
            m_labeldef = new CmsXmlWpLabelDefFile(cms, m_workplaceElementPath + C_LABELTEMPLATE);
        //}
        return m_labeldef;
    }
     
     /**
     * Reads the input field definition file.
     * @param cms The actual cms object
     * @return Reference to the label defintion file.
     * @exception CmsException
     */
     public CmsXmlWpInputDefFile getInputDefinitions(A_CmsObject cms) throws CmsException {
        //if(m_inputdef == null) {
            if(m_workplaceElementPath == null) {
                CmsXmlWpConfigFile configFile = new CmsXmlWpConfigFile(cms);
                m_workplaceElementPath = configFile.getWorkplaceElementPath();
            }
            m_inputdef = new CmsXmlWpInputDefFile(cms, m_workplaceElementPath + C_INPUTTEMPLATE);  
        //}
        return m_inputdef;
    }

     /**
     * Reads the panel bar definition file.
     * @param cms The actual cms object
     * @return Reference to the label defintion file.
     * @exception CmsException
     */
     public CmsXmlWpTemplateFile getPanelDefinitions(A_CmsObject cms) throws CmsException {
        //if(m_inputdef == null) {
            if(m_workplaceElementPath == null) {
                CmsXmlWpConfigFile configFile = new CmsXmlWpConfigFile(cms);
                m_workplaceElementPath = configFile.getWorkplaceElementPath();
            }
            m_paneldef = new CmsXmlWpTemplateFile(cms, m_workplaceElementPath + C_PANELTEMPLATE);  
        //}
        return m_paneldef;
    }
          
     /**
     * Reads the error definition file.
     * @param cms The actual cms object
     * @return Reference to the label defintion file.
     * @exception CmsException
     */
     public CmsXmlWpErrorDefFile getErrorDefinitions(A_CmsObject cms) throws CmsException {
        //if(m_errordef == null) {
            if(m_workplaceElementPath == null) {
                CmsXmlWpConfigFile configFile = new CmsXmlWpConfigFile(cms);
                m_workplaceElementPath = configFile.getWorkplaceElementPath();
            }
            m_errordef = new CmsXmlWpErrorDefFile(cms, m_workplaceElementPath + C_ERRORTEMPLATE);  
        //}
        return m_errordef;
    }

      /**
     * Reads the box definition file.
     * @param cms The actual cms object
     * @return Reference to the box defintion file.
     * @exception CmsException
     */
     public CmsXmlWpBoxDefFile getBoxDefinitions(A_CmsObject cms) throws CmsException {
        //if(m_boxdef == null) {
            if(m_workplaceElementPath == null) {
                CmsXmlWpConfigFile configFile = new CmsXmlWpConfigFile(cms);
                m_workplaceElementPath = configFile.getWorkplaceElementPath();
            }
            m_boxdef = new CmsXmlWpBoxDefFile(cms, m_workplaceElementPath + C_BOXTEMPLATE);  
        //}
        return m_boxdef;
    }
  
      /**
     * Reads the radiobutton definition file.
     * @param cms The actual cms object
     * @return Reference to the radiobutton defintion file.
     * @exception CmsException
     */
     public CmsXmlWpRadioDefFile getRadioDefinitions(A_CmsObject cms) throws CmsException {
        //if(m_radiodef == null) {
            if(m_workplaceElementPath == null) {
                CmsXmlWpConfigFile configFile = new CmsXmlWpConfigFile(cms);
                m_workplaceElementPath = configFile.getWorkplaceElementPath();
            }
            m_radiodef = new CmsXmlWpRadioDefFile(cms, m_workplaceElementPath + C_RADIOTEMPLATE);  
        //}
        return m_radiodef;
    }
     
     /**
     * Reads the task docu definition file.
     * @param cms The actual cms object
     * @return Reference to the list defintion file.
     * @exception CmsException
     */
    public CmsXmlWpTemplateFile getTaskDocuDefinitions(A_CmsObject cms) throws CmsException {
        //if(m_taskdocudef == null) {
            if(m_workplaceElementPath == null) {
                CmsXmlWpConfigFile configFile = new CmsXmlWpConfigFile(cms);
                m_workplaceElementPath = configFile.getWorkplaceElementPath();
            }
            m_taskdocudef = new CmsXmlWpTemplateFile(cms, m_workplaceElementPath + C_TASKDOCU_TEMPLATEFILE);
        //}
        return m_taskdocudef;
    }
   
     
    /**
     * Help method to print nice classnames in error messages
     * @return class name in [ClassName] format
     */
    protected String getClassName() {
        String name = getClass().getName();
        return "[" + name.substring(name.lastIndexOf(".") + 1) + "] ";
    }
    /**
     * Help method that handles any occuring exception by writing
     * an error message to the OpenCms logfile and throwing a 
     * CmsException of the type "unknown".
     * @param errorMessage String with the error message to be printed.
     * @exception CmsException
     */
    protected void throwException(String errorMessage) throws CmsException {
        throwException(errorMessage, CmsException.C_UNKNOWN_EXCEPTION);
    }

    /**
     * Help method that handles any occuring exception by writing
     * an error message to the OpenCms logfile and throwing a 
     * CmsException of the given type.
     * @param errorMessage String with the error message to be printed.
     * @param type Type of the exception to be thrown.
     * @exception CmsException
     */
    protected void throwException(String errorMessage, int type) throws CmsException {
        if(A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + errorMessage);
        }        throw new CmsException(errorMessage, type);
    }              
}
