package com.opencms.workplace;

/**
 * Interface defining all constants used in OpenCms
 * workplace classes and elements.
 * 
 * @author Alexander Lucas
 * @author Michael Emmerich
 * @version $Revision: 1.11 $ $Date: 2000/01/27 15:01:43 $
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
    public static final String C_INPUTTEMPLATE = "inputTemplate";
    
     /**
     *  Name of the template containing error field definitions
     */
    public static final String C_ERRORTEMPLATE = "errorTemplate";    
    
    
    // tag defnitions
    
   /**
    * Name of the label tag in the label definition template
    */
   public static final String C_TAG_LABEL="label";
   
   /**
    * Name of the label tag in the input definiton template
    */
   public static final String C_TAG_INPUTFIELD="inputfield";
    
   /**
    * Name of the password tag in the input definiton template
    */
   public static final String C_TAG_PASSWORD="password";
   
   /**
    * Name of the startup tag in the input definiton template
    */
   public static final String C_TAG_STARTUP="STARTUP";
      
    /**
    * Name of the submitbutton tag in the button definiton template
    */
   public static final String C_TAG_SUBMITBUTTON="submitbutton";

    /**
    * Name if the errorbox tag in the error definiton template
    */
   public static final String C_TAG_ERRORBOX="errorbox";

   /**
    * Name of the select tag in the input definiton template
    */
   public static final String C_TAG_SELECTBOX_HEADER="selectbox.start";

   /**
    * Name of the (select) option tag in the input definiton template
    */
   public static final String C_TAG_SELECTBOX_OPTION="selectbox.option";

   /**
    * Name of the (select) selected option tag in the input definiton template
    */
   public static final String C_TAG_SELECTBOX_SELOPTION="selectbox.seloption";
      
    /**
    * Name if the error´page tag in the error definiton template
    */
   public static final String C_TAG_ERRORPAGE="errorpage";
   
   
    // Parameters for buttons
    
    /** Name of the button */
    public static final String C_BUTTON_NAME = "name";
    
    /** Action for the button */
    public static final String C_BUTTON_ACTION = "action";
    
    /** Alt text of the button */
    public static final String C_BUTTON_ALT = "alt";
    
    /** href text of the button */
    public static final String C_BUTTON_HREF = "href";

    /** Value of the button */
    public static final String C_BUTTON_VALUE = "value";
    
    /** Style of the button */
    public static final String C_BUTTON_STYLE = "class";
    
    /** width of the button */
    public static final String C_BUTTON_WIDTH = "width";
    
    
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

    
    
    // Parameters for error boxes and error pages
    
    /** Title of the error box */
    public static final String C_ERROR_TITLE = "title";
    
    /** Message of the error box */
    public static final String C_ERROR_MESSAGE = "message";

     /** Reason of the error box */
    public static final String C_ERROR_REASON = "reason";
    
     /** Suggestion of the error box */
    public static final String C_ERROR_SUGGESTION = "suggestion";    

    /** Link of the error box */
    public static final String C_ERROR_LINK = "ref";    


    
    // Parameters for select boxes and error pages
    
    /** Name of the select box */
    public static final String C_SELECTBOX_NAME = "name";
    
    /** Value of the select box */
    public static final String C_SELECTBOX_VALUE = "value";
        
    /** Static text in the error box */
    public static final String C_ERROR_MSG_REASON = "msgreason";    
   
    /** Button label of the error box */
    public static final String C_ERROR_MSG_BUTTON = "msgbutton";    
    
    // Constants for language file control
            
    /** Prefix for button texts in the language file */
    public static final String C_LANG_BUTTON = "button";
    

    
}
