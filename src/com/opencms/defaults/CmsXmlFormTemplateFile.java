/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/defaults/Attic/CmsXmlFormTemplateFile.java,v $
 * Date   : $Date: 2000/06/25 08:26:24 $
 * Version: $Revision: 1.2 $
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

package com.opencms.defaults;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.template.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import java.util.*;
import java.lang.reflect.*;

/**
 * Content definition for Workplace template files.
 * 
 * @author Alexander Lucas
 * @author Michael Emmerich
 * @version $Revision: 1.2 $ $Date: 2000/06/25 08:26:24 $
 */
public class CmsXmlFormTemplateFile extends CmsXmlTemplateFile implements I_CmsLogChannels {

    /** Name of the select box */
    public static final String C_SELECTBOX_NAME = "name";
    
    /** Size of the select box */
    public static final String C_SELECTBOX_SIZE = "size";

    /** Div flag of the select box */
    public static final String C_SELECTBOX_DIV = "div";
   
    /** Stylesheet class string of the select box */
    public static final String C_SELECTBOX_CLASS = "class";

    /** Stylesheet class name of the select box */
    public static final String C_SELECTBOX_CLASSNAME = "classname";

    /** Stylesheet class name of the select box */
    public static final String C_SELECTBOX_WIDTHNAME = "widthname";
    
    /** Width of the select box */
    public static final String C_SELECTBOX_WIDTH = "width";

    /** Onchange of the select box */
    public static final String C_SELECTBOX_ONCHANGE = "onchange";
    
    /** Method of the select box */
    public static final String C_SELECTBOX_METHOD = "method";        

    /** option name of the select box */
    public static final String C_SELECTBOX_OPTIONNAME = "name";
    
    /** option value of the select box */
    public static final String C_SELECTBOX_OPTIONVALUE = "value";
    
    // Parameters for radiobuttons
  
    /** Name of the radio buttons */
    public static final String C_RADIO_RADIONAME = "radioname";
    
    /** Name of the radio button value */
    public static final String C_RADIO_NAME = "name";

    /** Name of the radio button link */
    public static final String C_RADIO_LINK = "link";
  
    /** Name of the radio button image name */
    //public static final String C_RADIO_IMAGENAME = "image";

    /** Datablock conatining the image option*/
    //public static final String C_RADIO_IMAGEOPTION = "optionalimage";

    /** Datablock conatining the optional entry for the image*/
    //public static final String C_RADIO_IMAGEENTRY = "imageentry";

    /** Datablock conatining the "checked" option*/
    public static final String C_RADIO_SELECTEDOPTION = "optionalselected";

    /** Datablock conatining the optional entry for the "checked" option */
    public static final String C_RADIO_SELECTEDENTRY = "selectedentry";
    
    /** Method of the radio buttons */
    public static final String C_RADIO_METHOD = "method";

    /** Name of the radio ordering information */
    public static final String C_RADIO_ORDER = "order";
    
    
    /** Name of the select start tag in the input definiton template */
    public static final String C_TAG_SELECTBOX_START="selectbox.start";

    /** Name of the select div start tag in the input definiton template */
    public static final String C_TAG_SELECTBOX_START_DIV="selectbox.startdiv";
   
    /** Name of the select end tag in the input definiton template */
    public static final String C_TAG_SELECTBOX_END="selectbox.end";
   
    /** Name of the selectbox "class" option tag in the input definiton template */
    public static final String C_TAG_SELECTBOX_CLASS="selectbox.class";

    /** Name of the selectbox "class" option tag in the input definiton template */
    public static final String C_TAG_SELECTBOX_WIDTH="selectbox.width";    
    
    /** Name of the (select) option tag in the input definiton template */
    public static final String C_TAG_SELECTBOX_OPTION="selectbox.option";

    /** Name of the (select) selected option tag in the input definiton template */
    public static final String C_TAG_SELECTBOX_SELOPTION="selectbox.seloption";
    
    
    /**
     * Default constructor.
     */
    public CmsXmlFormTemplateFile() throws CmsException {
        super();
        registerMyTags();
    }
    
    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     * 
     * @param cms CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */        
    public CmsXmlFormTemplateFile(CmsObject cms, String filename) throws CmsException {
        super();
        registerMyTags();
        init(cms, filename);
    }

    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     * 
     * @param cms CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */        
    public CmsXmlFormTemplateFile(CmsObject cms, CmsFile file) throws CmsException {
        super();
        registerMyTags();
        init(cms, file);
    }            
            
    /**
     * Registers the special tags for processing with
     * processNode().
     */
    private void registerMyTags() {
        super.registerTag("SELECT", CmsXmlFormTemplateFile.class, "handleSelectTag", C_REGISTER_MAIN_RUN); 
        super.registerTag("RADIOBUTTON", CmsXmlFormTemplateFile.class, "handleRadiobuttonTag", C_REGISTER_MAIN_RUN); 
        //registerTag("SELECT", "com.opencms.workplace.CmsSelectBox");
    }    
    
    /**
     * Special registerTag method for this content definition class.
     * Any workplace XML tag will be registered with the superclass for handling with
     * the method <code>handleAnyTag()</code> in this class.
     * Then the tagname together with the name of the class for the template
     * element (e.g. <code>CmsButton</code> or <code>CmsLabel</code>) will be put in an internal Hashtable.
     * <P>
     * Every workplace element class used by this method has to implement the interface
     * <code>I_CmsWpElement</code>
     * 
     * @param tagname XML tag to be registered as a special workplace tag.
     * @param elementClassName Appropriate workplace element class name for this tag.
     * @see com.opencms.workplace.I_CmsWpElement
     */
    private void registerTag(String tagname, String elementClassName) {
        super.registerTag(tagname, CmsXmlFormTemplateFile.class, "handleAnyTag", C_REGISTER_MAIN_RUN); 
        //m_wpTags.put(tagname.toLowerCase(), elementClassName);
    }
        
    /**
     * Gets the expected tagname for the XML documents of this content type
     * @return Expected XML tagname.
     */
    public String getXmlDocumentTagName() {
        return "XMLTEMPLATE";
    }
    
    /**
     * Handles any occurence of any special workplace XML tag like <code>&lt;BUTTON&gt;</code> or 
     * <code>&lt;LABEL&gt;</code>. Looks up the appropriate workplace element class for the current
     * tag and calls the <code>handleSpecialWorkplaceTag()</code> method of this class.
     * <P>
     * Every workplace element class used by this method has to implement the interface
     * <code>I_CmsWpElement</code>
     * 
     * @param n XML element containing the current special workplace tag.
     * @param callingObject reference to the calling object.
     * @param userObj hashtable containig all user parameters.
     * @exception CmsException
     * @see com.opencms.workplace.I_CmsWpElement
     */
    /*public Object handleAnyTag(Element n, Object callingObject, Object userObj) throws CmsException {
        Object result = null;        
        I_CmsWpElement workplaceObject = null;        
        String tagname = n.getTagName().toLowerCase();
        String classname = null;
        
        classname = (String)m_wpTags.get(tagname);
        if(classname == null || "".equals(classname)) {
            throwException("Don't know which class handles " + tagname + " tags.");            
        }            
        
        Object loadedClass = CmsTemplateClassManager.getClassInstance(m_cms, classname);
        if(!(loadedClass instanceof I_CmsWpElement)) {
            throwException("Loaded class " + classname + " is not implementing I_CmsWpElement");            
        }
     
		processNode(n, m_mainProcessTags, null, callingObject, userObj);
		workplaceObject = (I_CmsWpElement)loadedClass;
        try {
             result = workplaceObject.handleSpecialWorkplaceTag(m_cms, n, this, callingObject, (Hashtable)userObj, m_languageFile);                
        } catch(Exception e) {
            String errorMessage = "Error while building workplace element \"" + tagname + "\": " + e;
            if(e instanceof CmsException) {
                if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + errorMessage);
                }
                throw (CmsException)e;
            } else {
                throwException(errorMessage, e);
            }            
        }
             return result; 
    } */                   

    
    public Object handleSelectTag(Element n, Object callingObject, Object userObj) throws CmsException {

        Hashtable parameters = (Hashtable)userObj;
        
        /** Here the different select box options will be stored */
        Vector values = new Vector();
        Vector names = new Vector();

        /** StringBuffer for the generated output */
        StringBuffer result = new StringBuffer();
        
        // Read selectbox parameters
        String selectClass = n.getAttribute(C_SELECTBOX_CLASS);
        String selectName = n.getAttribute(C_SELECTBOX_NAME);
        String selectMethod = n.getAttribute(C_SELECTBOX_METHOD);
        String selectWidth = n.getAttribute(C_SELECTBOX_WIDTH);
        String selectOnchange = n.getAttribute(C_SELECTBOX_ONCHANGE);
        String selectSize = n.getAttribute(C_SELECTBOX_SIZE);

		if( (selectSize == null) || (selectSize.length() == 0) ) {
			selectSize = "1";
		}

        
        // Get input definition file
        CmsXmlTemplateFile inputdef = new CmsXmlTemplateFile(m_cms, "/content/internal/HTMLFormDefs"); 
                        
        if(selectClass == null || "".equals(selectClass)) {
            inputdef.setData(C_SELECTBOX_CLASS, "");
        } else {
            inputdef.setData(C_SELECTBOX_CLASSNAME, selectClass);
            inputdef.setData(C_SELECTBOX_CLASS, inputdef.getProcessedData(C_TAG_SELECTBOX_CLASS));
        }

        if(selectWidth == null || "".equals(selectWidth)) {
            inputdef.setData(C_SELECTBOX_WIDTH, "");
        } else {
            inputdef.setData(C_SELECTBOX_WIDTHNAME, selectWidth);
            inputdef.setData(C_SELECTBOX_WIDTH, inputdef.getProcessedData(C_TAG_SELECTBOX_WIDTH));
        }
        
        inputdef.setData(C_SELECTBOX_NAME, selectName);
        inputdef.setData(C_SELECTBOX_ONCHANGE, selectOnchange);
        inputdef.setData(C_SELECTBOX_SIZE, selectSize);
        result.append(inputdef.getProcessedDataValue(C_TAG_SELECTBOX_START));
             
        // call the method for generating listbox elements
        Method groupsMethod = null;
        int selectedOption = 0;
        try {
            groupsMethod = callingObject.getClass().getMethod(selectMethod, new Class[] {CmsObject.class, Vector.class, Vector.class, Hashtable.class});
            selectedOption = ((Integer)groupsMethod.invoke(callingObject, new Object[] {m_cms, values, names, parameters})).intValue();
        } catch(NoSuchMethodException exc) {
            // The requested method was not found.
            throwException("Could not find method " + selectMethod + " in calling class " + callingObject.getClass().getName() + " for generating select box content.", CmsException.C_NOT_FOUND);
        } catch(InvocationTargetException targetEx) {
            // the method could be invoked, but throwed a exception
            // itself. Get this exception and throw it again.              
            Throwable e = targetEx.getTargetException();
            if(!(e instanceof CmsException)) {
                // Only print an error if this is NO CmsException
                e.printStackTrace();
                throwException("User method " + selectMethod + " in calling class " + callingObject.getClass().getName() + " throwed an exception. " + e, CmsException.C_UNKNOWN_EXCEPTION);
            } else {
                // This is a CmsException
                // Error printing should be done previously.
                throw (CmsException)e;
            }
        } catch(Exception exc2) {
            throwException("User method " + selectMethod + " in calling class " + callingObject.getClass().getName() + " was found but could not be invoked. " + exc2, CmsException.C_XML_NO_USER_METHOD);
        }
                      
        // check the returned elements and put them into option tags.
        // The element with index "selectedOption" has to get the "selected" tag.
        int numValues = values.size();
        // TODO: check, if this is neede: int numNames = names.size();
        
        for(int i=0; i<numValues; i++) {
            inputdef.setData(C_SELECTBOX_OPTIONNAME, (String)names.elementAt(i));
            inputdef.setData(C_SELECTBOX_OPTIONVALUE, (String)values.elementAt(i));
            if(i == selectedOption) {
                result.append(inputdef.getProcessedDataValue(C_TAG_SELECTBOX_SELOPTION));                
            } else { 
                result.append(inputdef.getProcessedDataValue(C_TAG_SELECTBOX_OPTION));                    
            }
        }
        
        // get the processed selectbox end sequence.
        result.append(inputdef.getProcessedDataValue(C_TAG_SELECTBOX_END));        
        return result.toString(); 
    }               
    
    
    public Object handleRadiobuttonTag(Element n, Object callingObject, Object userObj) throws CmsException {
    
        Hashtable parameters = (Hashtable)userObj;
        
        /** StringBuffer for the generated output */
        StringBuffer result = new StringBuffer();

        /** Here the different select box options will be stored */
        Vector values = new Vector();
        Vector names = new Vector();
        Integer returnObject = null;
        
        String radioName=n.getAttribute(C_RADIO_NAME);
        String radioMethod = n.getAttribute(C_RADIO_METHOD);
        String radioOrder = n.getAttribute(C_RADIO_ORDER);
             
        if(radioOrder == null || ((!"row".equals(radioOrder))&&(!"col".equals(radioOrder)))) {
            radioOrder = "col";
        }
      
        // call the method for generating listbox elements
        Method groupsMethod = null;
        int selectedOption = 0;
        
        try {
            groupsMethod = callingObject.getClass().getMethod(radioMethod, new Class[] {CmsObject.class, Vector.class, Vector.class, Hashtable.class});
            returnObject = (Integer)groupsMethod.invoke(callingObject, new Object[] {m_cms, names, values, parameters});
        } catch(NoSuchMethodException exc) {
            // The requested method was not found.
            throwException("Could not find radio button method " + radioMethod + " in calling class " + callingObject.getClass().getName() + " for generating select box content.", CmsException.C_NOT_FOUND);
        } catch(InvocationTargetException targetEx) {
            // the method could be invoked, but throwed a exception
            // itself. Get this exception and throw it again.              
            Throwable e = targetEx.getTargetException();
            if(!(e instanceof CmsException)) {
                // Only print an error if this is NO CmsException
                e.printStackTrace();
                throwException("Radio button method " + radioMethod + " in calling class " + callingObject.getClass().getName() + " throwed an exception. " + e, CmsException.C_UNKNOWN_EXCEPTION);
            } else {
                // This is a CmsException
                // Error printing should be done previously.
                throw (CmsException)e;
            }
        } catch(Exception exc2) {
            throwException("Radio button method " + radioMethod + " in calling class " + callingObject.getClass().getName() + " was found but could not be invoked. " + exc2, CmsException.C_XML_NO_USER_METHOD);
        }
 
        // If the radio button method returned a value, use it for preselecting an option
        if(returnObject != null) {
            selectedOption = ((Integer)returnObject).intValue();
        }

        // process the vectors with the elelmetns of the radio buttons to be displayed.
        int numValues = values.size();
        
        CmsXmlTemplateFile radiodef = new CmsXmlTemplateFile(m_cms, "/content/internal/HTMLFormDefs");
        for(int i=0; i<numValues; i++) {        
            // Set values for this radiobutton entry
            radiodef.setData(C_RADIO_RADIONAME, radioName);
            radiodef.setData(C_RADIO_NAME, (String)names.elementAt(i));
            radiodef.setData(C_RADIO_LINK, (String)values.elementAt(i));
            
            // Check, if this should be the preselected option
            if (i==selectedOption) {
                radiodef.setData(C_RADIO_SELECTEDENTRY, radiodef.getDataValue("radiobuttons." + C_RADIO_SELECTEDOPTION));
            } else {
                radiodef.setData(C_RADIO_SELECTEDENTRY, "");
            }            
            
            // Now get output for this option 
            if(radioOrder.equals("col")) {
                // Buttons should be displayed in one column 
                result.append(radiodef.getProcessedDataValue("radiobuttons.colentry",callingObject));
            } else {
                // Buttons should be displayed in a row.
                result.append(radiodef.getProcessedDataValue("radiobuttons.rowentry",callingObject));
            }                        
        }        
        return result.toString();
    }               
}
