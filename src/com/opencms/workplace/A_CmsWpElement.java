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
 * @version $Revision: 1.8 $ $Date: 2000/01/28 11:40:33 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public abstract class A_CmsWpElement implements I_CmsLogChannels, I_CmsWpElement, I_CmsWpConstants {
    
    /**
     * Reference to to buttons definition file
     */
    protected static CmsXmlWpButtonsDefFile m_buttondef = null;
    
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
     * Path to all worplace definition files (will be read once
     * from workplace.ini)
     */
    protected static String m_workplaceElementPath = null;
        
    /**
     * Reads the buttons definition file.
     * @param cms The actual cms object
     * @return Reference to the buttons defintion file.
     * @exception CmsException
     */
    public CmsXmlWpButtonsDefFile getButtonDefinitions(A_CmsObject cms) throws CmsException {
        if(m_buttondef == null) {
            if(m_workplaceElementPath == null) {
                CmsXmlWpConfigFile configFile = new CmsXmlWpConfigFile(cms);
                m_workplaceElementPath = configFile.getWorkplaceElementPath();
            }
            m_buttondef = new CmsXmlWpButtonsDefFile(cms, m_workplaceElementPath + C_BUTTONTEMPLATE);
        }
        return m_buttondef;
    }
    
    /**
     * Reads the label definition file.
     * @param cms The actual cms object
     * @return Reference to the label defintion file.
     * @exception CmsException
     */
     public CmsXmlWpLabelDefFile getLabelDefinitions(A_CmsObject cms) throws CmsException {
        if(m_labeldef == null) {
            if(m_workplaceElementPath == null) {
                CmsXmlWpConfigFile configFile = new CmsXmlWpConfigFile(cms);
                m_workplaceElementPath = configFile.getWorkplaceElementPath();
            }
            m_labeldef = new CmsXmlWpLabelDefFile(cms, m_workplaceElementPath + C_LABELTEMPLATE);
        }
        return m_labeldef;
    }
     
     /**
     * Reads the input field definition file.
     * @param cms The actual cms object
     * @return Reference to the label defintion file.
     * @exception CmsException
     */
     public CmsXmlWpInputDefFile getInputDefinitions(A_CmsObject cms) throws CmsException {
        if(m_inputdef == null) {
            if(m_workplaceElementPath == null) {
                CmsXmlWpConfigFile configFile = new CmsXmlWpConfigFile(cms);
                m_workplaceElementPath = configFile.getWorkplaceElementPath();
            }
            m_inputdef = new CmsXmlWpInputDefFile(cms, m_workplaceElementPath + C_INPUTTEMPLATE);  
        }
        return m_inputdef;
    }
     
     /**
     * Reads the error definition file.
     * @param cms The actual cms object
     * @return Reference to the label defintion file.
     * @exception CmsException
     */
     public CmsXmlWpErrorDefFile getErrorDefinitions(A_CmsObject cms) throws CmsException {
        if(m_errordef == null) {
            if(m_workplaceElementPath == null) {
                CmsXmlWpConfigFile configFile = new CmsXmlWpConfigFile(cms);
                m_workplaceElementPath = configFile.getWorkplaceElementPath();
            }
            m_errordef = new CmsXmlWpErrorDefFile(cms, m_workplaceElementPath + C_ERRORTEMPLATE);  
        }
        return m_errordef;
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
