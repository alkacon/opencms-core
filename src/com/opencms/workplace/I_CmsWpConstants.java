package com.opencms.workplace;

/**
 * Interface defining all constants used in OpenCms
 * workplace classes and elements.
 * 
 * @author Alexander Lucas
 * @author Michael Emmerich
 * @version $Revision: 1.5 $ $Date: 2000/01/26 10:15:05 $
 */
public interface I_CmsWpConstants {

    // Filenames of special templates
    
    /** Name of the template containing button definitions */
    public static final String C_BUTTONTEMPLATE = "ButtonTemplate";
 
    /** Name of the template containing label definitions */
    public static final String C_LABELTEMPLATE = "labelTemplate";
    
     /**
     *  Name of the template containing input field definitions
     */
    public static final String C_INPUTTEMPLATE = "labelInput";
    
    
    // tag defnitions
    
   /**
    * Name of the label tag in the label definition template
    */
   public static final String C_TAG_LABEL="label";
   
   /**
    * Name if the label tag in the input definiton template
    */
   public static final String C_TAG_INPUTFIELD="inputfield";
    
    
    // Parameters for buttons
    
    /** Name of the button */
    public static final String C_BUTTON_NAME = "name";
    
    /** Action for the button */
    public static final String C_BUTTON_ACTION = "action";
    
    /** Alt text of the button */
    public static final String C_BUTTON_ALT = "alt";
    
    /** href text of the button */
    public static final String C_BUTTON_HREF = "href";

    
    // Parameters for labels
    
    /** Name of the value */
    public static final String C_LABEL_VALUE = "value";
    
        
    // Parameters for input fields
    
    /** Name of the input field */
    public static final String C_INPUT_NAME = "name";
    
    /** Style class of the input field  */
    public static final String C_INPUT_CLASS = "class";
    

    /**  Size of the input field  */
    public static final String C_INPUT_SIZE = "size";
    
    /**  Length of the input field  */
    public static final String C_INPUT_LENGTH = "length";    

    // Constants for language file control
            
    /** Prefix for button texts in the language file */
    public static final String C_LANG_BUTTON = "button";
    

    
}
