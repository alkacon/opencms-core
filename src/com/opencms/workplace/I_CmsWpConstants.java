package com.opencms.workplace;

/**
 * Interface defining all constants used in OpenCms
 * workplace classes and elements.
 * 
 * @author Alexander Lucas
 * @author Michael Emmerich
 * @version $Revision: 1.3 $ $Date: 2000/01/26 09:40:49 $
 */
public interface I_CmsWpConstants {

    // Filenames of special templates
    
    /** Name of the template containing button definitions */
    public static final String C_BUTTONTEMPLATE = "ButtonTemplate";
 
    /** Name of the template containing label definitions */
    public static final String C_LABELTEMPLATE = "labelTemplate";
    
        
    // Parameters for buttons
    
    /** Name of the button */
    public static final String C_BUTTON_NAME = "name";
    
    /** Action for the button */
    public static final String C_BUTTON_ACTION = "action";
    
    /** Alt text of the button */
    public static final String C_BUTTON_ALT = "alt";

    
    
    // Parameters for labels
    
    /** Name of the value */
    public static final String C_LABEL_VALUE = "value";
    
    
    
    // Constants for language file control
    
    /** Prefix for button texts in the language file */
    public static final String C_LANG_BUTTON = "button";
    
}
