package com.opencms.workplace;

import com.opencms.core.*;
import com.opencms.file.*;

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
 * @version $Revision: 1.3 $ $Date: 2000/01/26 09:40:49 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public abstract class A_CmsWpElement implements I_CmsWpElement, I_CmsWpConstants {
    
    /**
     * Reference to to buttons definition file
     */
    protected static CmsXmlWpButtonsDefFile m_buttondef = null;
    
    /**
     * Reference to the label defintion file
     */
    protected static CmsXmlWpLabelDefFile m_labeldef = null;
    
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
}
