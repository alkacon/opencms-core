/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/A_CmsWpElement.java,v $
* Date   : $Date: 2004/02/13 13:41:44 $
* Version: $Revision: 1.45 $
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

import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import org.opencms.file.CmsObject;

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
 * @version $Revision: 1.45 $ $Date: 2004/02/13 13:41:44 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */

public abstract class A_CmsWpElement implements I_CmsWpElement, I_CmsWpConstants {


    /**
     * Reference to to buttons definition file
     */
    protected static CmsXmlWpButtonsDefFile m_buttondef = null;


    /**
     * Reference to icons definition file
     */
    protected static CmsXmlWpTemplateFile m_icondef = null;


    /**
     * Reference to backbutton definition file
     */
    protected static CmsXmlWpTemplateFile m_backbuttondef = null;


    /**
     * Reference to projectlist definition file
     */
    protected static CmsXmlWpTemplateFile m_projectlistdef = null;


    /**
     * Reference to modulelist definition file
     */
    protected static CmsXmlWpTemplateFile m_modulelistdef = null;


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
    protected static CmsXmlWpTemplateFile m_errordef = null;


    /**
     * Reference to the radio button defintion file
     */
    protected static CmsXmlWpTemplateFile m_radiodef = null;


    /**
     * Reference to the box defintion file
     */
    protected static CmsXmlWpBoxDefFile m_boxdef = null;


    /**
     * Reference to the box defintion file
     */
    protected static CmsXmlWpTemplateFile m_prefsscrollerdef = null;
    

    /** Reference to the config file */
    private static CmsXmlWpConfigFile m_configFile = null;

    /**
     * Reads the backbutton definition file.
     * @param cms The actual cms object
     * @return Reference to the backbutton defintion file.
     * @throws CmsException
     */

    public CmsXmlWpTemplateFile getBackbuttonDefinitions(CmsObject cms) throws CmsException {

        m_backbuttondef = new CmsXmlWpTemplateFile(cms, C_VFS_PATH_DEFAULT_INTERNAL
                + C_ADMIN_BACK_BUTTON);

        return m_backbuttondef;
    }

    /**
     * Reads the box definition file.
     * @param cms The actual cms object
     * @return Reference to the box defintion file.
     * @throws CmsException
     */

    public CmsXmlWpBoxDefFile getBoxDefinitions(CmsObject cms) throws CmsException {

        m_boxdef = new CmsXmlWpBoxDefFile(cms, C_VFS_PATH_DEFAULT_INTERNAL + C_BOXTEMPLATE);
        return m_boxdef;
    }

    /**
     * Reads the buttons definition file.
     * @param cms The actual cms object
     * @return Reference to the buttons defintion file.
     * @throws CmsException
     */

    public CmsXmlWpButtonsDefFile getButtonDefinitions(CmsObject cms) throws CmsException {

        m_buttondef = new CmsXmlWpButtonsDefFile(cms, C_VFS_PATH_DEFAULT_INTERNAL
                + C_BUTTONTEMPLATE);

        return m_buttondef;
    }

    /**
     * Gets a reference to the default config file.
     * The path to this file ist stored in <code>C_WORKPLACE_INI</code>
     *
     * @param cms CmsObject Object for accessing system resources.
     * @return Reference to the config file.
     * @throws CmsException
     */
    public CmsXmlWpConfigFile getConfigFile(CmsObject cms) throws CmsException {
        m_configFile = new CmsXmlWpConfigFile(cms);
        return m_configFile;
    }

    /**
     * Reads the contextmenue definition file.
     * @param cms The actual cms object
     * @return Reference to the list defintion file.
     * @throws CmsException
     */
    public CmsXmlWpTemplateFile getContextmenueDefinitions(CmsObject cms) throws CmsException {
        m_contextdef = new CmsXmlWpTemplateFile(cms, C_VFS_PATH_DEFAULT_INTERNAL + C_CONTEXTMENUE_TEMPLATEFILE);
        return m_contextdef;
    }

    /**
     * Reads the error definition file.
     * @param cms The actual cms object
     * @return Reference to the label defintion file.
     * @throws CmsException
     */

    public CmsXmlWpTemplateFile getErrorDefinitions(CmsObject cms) throws CmsException {

        m_errordef = new CmsXmlWpTemplateFile(cms, C_VFS_PATH_DEFAULT_INTERNAL + C_ERRORTEMPLATE);
        return m_errordef;
    }

    /**
     * Reads the icons definition file.
     * @param cms The actual cms object
     * @return Reference to the icons defintion file.
     * @throws CmsException
     */

    public CmsXmlWpTemplateFile getIconDefinitions(CmsObject cms) throws CmsException {

        m_icondef = new CmsXmlWpTemplateFile(cms, C_VFS_PATH_DEFAULT_INTERNAL + C_ICON_TEMPLATEFILE);
        return m_icondef;
    }

    /**
     * Reads the input field definition file.
     * @param cms The actual cms object
     * @return Reference to the label defintion file.
     * @throws CmsException
     */

    public CmsXmlWpInputDefFile getInputDefinitions(CmsObject cms) throws CmsException {

        m_inputdef = new CmsXmlWpInputDefFile(cms, C_VFS_PATH_DEFAULT_INTERNAL + C_INPUTTEMPLATE);
        return m_inputdef;
    }

    /**
     * Reads the label definition file.
     * @param cms The actual cms object
     * @return Reference to the label defintion file.
     * @throws CmsException
     */

    public CmsXmlWpLabelDefFile getLabelDefinitions(CmsObject cms) throws CmsException {

        m_labeldef = new CmsXmlWpLabelDefFile(cms, C_VFS_PATH_DEFAULT_INTERNAL + C_LABELTEMPLATE);
        return m_labeldef;
    }

    /**
     * Reads the modulelist definition file.
     * @param cms The actual cms object
     * @return Reference to the list defintion file.
     * @throws CmsException
     */

    public CmsXmlWpTemplateFile getModulelistDefinitions(CmsObject cms) throws CmsException {

        m_modulelistdef = new CmsXmlWpTemplateFile(cms, C_VFS_PATH_DEFAULT_INTERNAL
                + C_MODULELIST_TEMPLATEFILE);
        return m_modulelistdef;
    }

    /**
     * Reads the panel bar definition file.
     * @param cms The actual cms object
     * @return Reference to the label defintion file.
     * @throws CmsException
     */

    public CmsXmlWpTemplateFile getPanelDefinitions(CmsObject cms) throws CmsException {

        m_paneldef = new CmsXmlWpTemplateFile(cms, C_VFS_PATH_DEFAULT_INTERNAL + C_PANELTEMPLATE);
        return m_paneldef;
    }

    /**
     * Reads the preferences scroller definition file.
     * @param cms The actual cms object
     * @return Reference to the list defintion file.
     * @throws CmsException
     */

    public CmsXmlWpTemplateFile getPrefsScrollerDefinitions(CmsObject cms) throws CmsException {

        m_prefsscrollerdef = new CmsXmlWpTemplateFile(cms, C_VFS_PATH_DEFAULT_INTERNAL
                + C_PREFSSCROLLER_TEMPLATEFILE);
        return m_prefsscrollerdef;
    }

    /**
     * Reads the projectlist definition file.
     * @param cms The actual cms object
     * @return Reference to the list defintion file.
     * @throws CmsException
     */

    public CmsXmlWpTemplateFile getProjectlistDefinitions(CmsObject cms) throws CmsException {

        m_projectlistdef = new CmsXmlWpTemplateFile(cms, C_VFS_PATH_DEFAULT_INTERNAL
                + C_PROJECTLIST_TEMPLATEFILE);
        return m_projectlistdef;
    }

    /**
     * Reads the radiobutton definition file.
     * @param cms The actual cms object
     * @return Reference to the radiobutton defintion file.
     * @throws CmsException
     */

    public CmsXmlWpTemplateFile getRadioDefinitions(CmsObject cms) throws CmsException {

        m_radiodef = new CmsXmlWpTemplateFile(cms, C_VFS_PATH_DEFAULT_INTERNAL + C_RADIOTEMPLATE);
        return m_radiodef;
    }

    /**
     * Reads the task docu definition file.
     * @param cms The actual cms object
     * @return Reference to the list defintion file.
     * @throws CmsException
     */

    public CmsXmlWpTemplateFile getTaskDocuDefinitions(CmsObject cms) throws CmsException {

        m_taskdocudef = new CmsXmlWpTemplateFile(cms, C_VFS_PATH_DEFAULT_INTERNAL
                + C_TASKDOCU_TEMPLATEFILE);
        return m_taskdocudef;
    }

    /**
     * Reads the projectlist definition file.
     * @param cms The actual cms object
     * @return Reference to the list defintion file.
     * @throws CmsException
     */

    public CmsXmlWpTemplateFile getTaskListDefinitions(CmsObject cms) throws CmsException {

        m_tasklistdef = new CmsXmlWpTemplateFile(cms, C_VFS_PATH_DEFAULT_INTERNAL
                + C_TASKLIST_TEMPLATEFILE);
        return m_tasklistdef;
    }

    /**
     * Help method that handles any occuring exception by writing
     * an error message to the OpenCms logfile and throwing a
     * CmsException of the given type.
     * @param errorMessage String with the error message to be printed.
     * @param type Type of the exception to be thrown.
     * @throws CmsException
     */

    protected void throwException(String errorMessage, int type) throws CmsException {
        if(OpenCms.getLog(this).isWarnEnabled() ) {
            OpenCms.getLog(this).warn(errorMessage);
        }
        throw new CmsException(errorMessage, type);
    }

    /**
     * Help method that handles any occuring exception by writing
     * an error message to the OpenCms logfile and throwing a
     * CmsException of the type "unknown".
     * @param errorMessage String with the error message to be printed.
     * @throws CmsException
     */

    protected void throwException(String errorMessage) throws CmsException {
        throwException(errorMessage, CmsException.C_UNKNOWN_EXCEPTION);
    }
}
