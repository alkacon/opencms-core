
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/defaults/Attic/CmsXmlFormTemplateFile.java,v $
* Date   : $Date: 2001/07/31 12:03:36 $
* Version: $Revision: 1.7 $
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
 * Template content definition for generating HTML forms.
 * This is an extension of the default template content definition.
 * Special tags for handling HTML form elements are added here.
 * See the handleXxxTag Methods for more details.
 *
 * @author Alexander Lucas
 * @version $Revision: 1.7 $ $Date: 2001/07/31 12:03:36 $
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

    /** Stylesheet class string of the radio button */
    public static final String C_RADIO_CLASS = "class";

    /** Stylesheet class name of the radio button */
    public static final String C_RADIO_CLASSNAME = "classname";

    /** Datablock conatining the "checked" option*/
    public static final String C_RADIO_SELECTEDOPTION = "optionalselected";

    /** Datablock conatining the optional entry for the "checked" option */
    public static final String C_RADIO_SELECTEDENTRY = "selectedentry";

    /** Method of the radio buttons */
    public static final String C_RADIO_METHOD = "method";

    /** Name of the radio ordering information */
    public static final String C_RADIO_ORDER = "order";

    /** Name of the select start tag in the input definiton template */
    public static final String C_TAG_SELECTBOX_START = "selectbox.start";

    /** Name of the select div start tag in the input definiton template */
    public static final String C_TAG_SELECTBOX_START_DIV = "selectbox.startdiv";

    /** Name of the select end tag in the input definiton template */
    public static final String C_TAG_SELECTBOX_END = "selectbox.end";

    /** Name of the selectbox "class" option tag in the input definiton template */
    public static final String C_TAG_SELECTBOX_CLASS = "selectbox.class";

    /** Name of the selectbox "width" option tag in the input definiton template */
    public static final String C_TAG_SELECTBOX_WIDTH = "selectbox.width";

    /** Name of the (select) option tag in the input definiton template */
    public static final String C_TAG_SELECTBOX_OPTION = "selectbox.option";

    /** Name of the (select) selected option tag in the input definiton template */
    public static final String C_TAG_SELECTBOX_SELOPTION = "selectbox.seloption";

    /** Name of the radio "class" option tag in the input definiton template */
    public static final String C_TAG_RADIO_CLASS = "radiobuttons.class";

    /** Name of the radion column entry tag in the input definiton template */
    public static final String C_TAG_RADIO_COLENTRY = "radiobuttons.colentry";

    /** Name of the radion row entry tag in the input definiton template */
    public static final String C_TAG_RADIO_ROWENTRY = "radiobuttons.rowentry";

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
    public CmsXmlFormTemplateFile(CmsObject cms, CmsFile file) throws CmsException {
        super();
        registerMyTags();
        init(cms, file);
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
     * Gets the expected tagname for the XML documents of this content type
     * @return Expected XML tagname.
     */
    public String getXmlDocumentTagName() {
        return "XMLTEMPLATE";
    }

    /**
     * Handles any occurence of the special XML tag <code>&lt;RADIOBUTTON&gt;</code> for
     * generating HTML form radio buttons.
     * <P>
     * The definition of a HTML radio button will be taken from /content/internal/HTMLFormDefs.
     * If the file is missing, this method will crash. Ensure this file is created and filled
     * with all required XML tags.
     * <P>
     * Radio buttons can be generated by adding the special XML tag
     * <code>&lt;RADIOBUTTON class="myClass" method="myMethod" name="myName"/&gt;</code>
     * to the template file. This tag will be replaced with the correspondig group of radio buttons
     * while processing the template file. The <code>class</code> parameter is optional.
     * The <code>method</code> parameter will be used to look up a user defined method
     * in the template class assigned to the template file. This method should look like
     * <br/>
     * <code>public Integer myMethod(CmsObject cms, Vector names, Vector values, Hashtable parameters)</code><br/>
     * and will be used to get the content of the requested radion button group. The vectors <code>names</code>
     * and <code>values</code> should be filled with the appropriate values. The return value should be an
     * Integer containing the pre-selected radio button or -1, if no value should be pre-selected.
     *
     * @param n XML element containing the current special workplace tag.
     * @param callingObject reference to the calling object.
     * @param userObj hashtable containig all user parameters.
     * @exception CmsException
     */
    public Object handleRadiobuttonTag(Element n, Object callingObject, Object userObj) throws CmsException {
        Hashtable parameters = (Hashtable)userObj;

        // StringBuffer for the generated output
        StringBuffer result = new StringBuffer();

        // Here the different select box options will be stored
        Vector values = new Vector();
        Vector names = new Vector();
        Integer returnObject = null;

        // Read radiobutton parameters
        String radioClass = n.getAttribute(C_SELECTBOX_CLASS);
        String radioName = n.getAttribute(C_RADIO_NAME);
        String radioMethod = n.getAttribute(C_RADIO_METHOD);
        String radioOrder = n.getAttribute(C_RADIO_ORDER);
        if(radioOrder == null || ((!"row".equals(radioOrder)) && (!"col".equals(radioOrder)))) {
            radioOrder = "col";
        }

        // call the method for generating listbox elements
        Method groupsMethod = null;
        int selectedOption = 0;
        try {
            groupsMethod = callingObject.getClass().getMethod(radioMethod, new Class[] {
                CmsObject.class, Vector.class, Vector.class, Hashtable.class
            });
            returnObject = (Integer)groupsMethod.invoke(callingObject, new Object[] {
                m_cms, names, values, parameters
            });
        }
        catch(NoSuchMethodException exc) {

            // The requested method was not found.
            throwException("Could not find radio button method " + radioMethod + " in calling class " + callingObject.getClass().getName()
                    + " for generating select box content.", CmsException.C_NOT_FOUND);
        }
        catch(InvocationTargetException targetEx) {

            // the method could be invoked, but throwed a exception
            // itself. Get this exception and throw it again.
            Throwable e = targetEx.getTargetException();
            if(!(e instanceof CmsException)) {

                // Only print an error if this is NO CmsException
                throwException("Radio button method " + radioMethod + " in calling class " + callingObject.getClass().getName()
                        + " throwed an exception. " + e, CmsException.C_UNKNOWN_EXCEPTION);
            }
            else {

                // This is a CmsException
                // Error printing should be done previously.
                throw (CmsException)e;
            }
        }
        catch(Exception exc2) {
            throwException("Radio button method " + radioMethod + " in calling class " + callingObject.getClass().getName()
                    + " was found but could not be invoked. " + exc2, CmsException.C_XML_NO_USER_METHOD);
        }

        // If the radio button method returned a value, use it for preselecting an option
        if(returnObject != null) {
            selectedOption = ((Integer)returnObject).intValue();
        }

        // process the vectors with the elelmetns of the radio buttons to be displayed.
        int numValues = values.size();
        CmsXmlTemplateFile radiodef = new CmsXmlTemplateFile(m_cms, "/content/internal/HTMLFormDefs");
        if(radioClass == null || "".equals(radioClass)) {
            radiodef.setData(C_RADIO_CLASS, "");
        }
        else {
            radiodef.setData(C_RADIO_CLASSNAME, radioClass);
            radiodef.setData(C_RADIO_CLASS, radiodef.getProcessedData(C_TAG_RADIO_CLASS));
        }
        for(int i = 0;i < numValues;i++) {

            // Set values for this radiobutton entry
            radiodef.setData(C_RADIO_RADIONAME, radioName);
            radiodef.setData(C_RADIO_NAME, (String)names.elementAt(i));
            radiodef.setData(C_RADIO_LINK, (String)values.elementAt(i));

            // Check, if this should be the preselected option
            if(i == selectedOption) {
                radiodef.setData(C_RADIO_SELECTEDENTRY, radiodef.getDataValue("radiobuttons." + C_RADIO_SELECTEDOPTION));
            }
            else {
                radiodef.setData(C_RADIO_SELECTEDENTRY, "");
            }

            // Now get output for this option
            if(radioOrder.equals("col")) {

                // Buttons should be displayed in one column
                result.append(radiodef.getProcessedDataValue(C_TAG_RADIO_COLENTRY, callingObject));
            }
            else {

                // Buttons should be displayed in a row.
                result.append(radiodef.getProcessedDataValue(C_TAG_RADIO_ROWENTRY, callingObject));
            }
        }
        return result.toString();
    }

    /**
     * Handles any occurence of the special XML tag <code>&lt;SELECT&gt;</code> for
     * generating HTML form select boxes.
     * <P>
     * The definition of a HTML selectbox will be taken from /content/internal/HTMLFormDefs.
     * If the file is missing, this method will crash. Ensure this file is created and filled
     * with all required XML tags.
     * <P>
     * Select boxes can be generated by adding the special XML tag
     * <code>&lt;SELECT class="myClass" method="myMethod" name="myName" onchange="..." size="..."/&gt;</code>
     * to the template file. This tag will be replaced with the correspondig select box
     * while processing the template file. The <code>class</code> parameter is optional and
     * The <code>method</code> parameter will be used to look up a user defined method
     * in the template class assigned to the template file. This method should look like
     * <br/>
     * <code>public Integer myMethod(CmsObject cms, Vector names, Vector values, Hashtable parameters)</code><br/>
     * and will be used to get the content of the requested select box group. The vectors <code>names</code>
     * and <code>values</code> should be filled with the appropriate values. The return value should be an
     * Integer containing the pre-selected option or -1, if no value should be pre-selected.
     *
     * @param n XML element containing the current special workplace tag.
     * @param callingObject reference to the calling object.
     * @param userObj hashtable containig all user parameters.
     * @exception CmsException
     */
    public Object handleSelectTag(Element n, Object callingObject, Object userObj) throws CmsException {
        Hashtable parameters = (Hashtable)userObj;

        // Here the different select box options will be stored
        Vector values = new Vector();
        Vector names = new Vector();

        // StringBuffer for the generated output *
        StringBuffer result = new StringBuffer();

        // Read selectbox parameters
        String selectClass = n.getAttribute(C_SELECTBOX_CLASS);
        String selectName = n.getAttribute(C_SELECTBOX_NAME);
        String selectMethod = n.getAttribute(C_SELECTBOX_METHOD);
        String selectWidth = n.getAttribute(C_SELECTBOX_WIDTH);
        String selectOnchange = n.getAttribute(C_SELECTBOX_ONCHANGE);
        String selectSize = n.getAttribute(C_SELECTBOX_SIZE);
        if((selectSize == null) || (selectSize.length() == 0)) {
            selectSize = "1";
        }

        // Get input definition file
        CmsXmlTemplateFile inputdef = new CmsXmlTemplateFile(m_cms, "/content/internal/HTMLFormDefs");

        // Set the prefix string of the select box
        if(selectClass == null || "".equals(selectClass)) {
            inputdef.setData(C_SELECTBOX_CLASS, "");
        }
        else {
            inputdef.setData(C_SELECTBOX_CLASSNAME, selectClass);
            inputdef.setData(C_SELECTBOX_CLASS, inputdef.getProcessedData(C_TAG_SELECTBOX_CLASS));
        }
        if(selectWidth == null || "".equals(selectWidth)) {
            inputdef.setData(C_SELECTBOX_WIDTH, "");
        }
        else {
            inputdef.setData(C_SELECTBOX_WIDTHNAME, selectWidth);
            inputdef.setData(C_SELECTBOX_WIDTH, inputdef.getProcessedData(C_TAG_SELECTBOX_WIDTH));
        }
        inputdef.setData(C_SELECTBOX_NAME, selectName);
        inputdef.setData(C_SELECTBOX_ONCHANGE, selectOnchange);
        inputdef.setData(C_SELECTBOX_SIZE, selectSize);

        // move the prefix string of the select box to the result StringBuffer
        result.append(inputdef.getProcessedDataValue(C_TAG_SELECTBOX_START));

        // call the method for generating listbox elements
        Method groupsMethod = null;
        int selectedOption = 0;
        try {
            groupsMethod = callingObject.getClass().getMethod(selectMethod, new Class[] {
                CmsObject.class, Vector.class, Vector.class, Hashtable.class
            });
            selectedOption = ((Integer)groupsMethod.invoke(callingObject, new Object[] {
                m_cms, values, names, parameters
            })).intValue();
        }
        catch(NoSuchMethodException exc) {

            // The requested method was not found.
            throwException("Could not find method " + selectMethod + " in calling class " + callingObject.getClass().getName()
                    + " for generating select box content.", CmsException.C_NOT_FOUND);
        }
        catch(InvocationTargetException targetEx) {

            // the method could be invoked, but throwed a exception
            // itself. Get this exception and throw it again.
            Throwable e = targetEx.getTargetException();
            if(!(e instanceof CmsException)) {

                // Only print an error if this is NO CmsException
                throwException("User method " + selectMethod + " in calling class " + callingObject.getClass().getName()
                        + " throwed an exception. " + e, CmsException.C_UNKNOWN_EXCEPTION);
            }
            else {

                // This is a CmsException
                // Error printing should be done previously.
                throw (CmsException)e;
            }
        }
        catch(Exception exc2) {
            throwException("User method " + selectMethod + " in calling class " + callingObject.getClass().getName()
                    + " was found but could not be invoked. " + exc2, CmsException.C_XML_NO_USER_METHOD);
        }

        // check the returned elements and put them into option tags.
        // The element with index "selectedOption" has to get the "selected" tag.
        int numValues = values.size();
        for(int i = 0;i < numValues;i++) {
            inputdef.setData(C_SELECTBOX_OPTIONNAME, (String)names.elementAt(i));
            inputdef.setData(C_SELECTBOX_OPTIONVALUE, (String)values.elementAt(i));
            if(i == selectedOption) {
                result.append(inputdef.getProcessedDataValue(C_TAG_SELECTBOX_SELOPTION));
            }
            else {
                result.append(inputdef.getProcessedDataValue(C_TAG_SELECTBOX_OPTION));
            }
        }

        // get the processed selectbox end sequence.
        result.append(inputdef.getProcessedDataValue(C_TAG_SELECTBOX_END));
        return result.toString();
    }

    /**
     * Registers the special tags for processing with
     * processNode().
     */
    private void registerMyTags() {
        super.registerTag("SELECT", CmsXmlFormTemplateFile.class, "handleSelectTag", C_REGISTER_MAIN_RUN);
        super.registerTag("RADIOBUTTON", CmsXmlFormTemplateFile.class, "handleRadiobuttonTag", C_REGISTER_MAIN_RUN);
        // just to be sure this is in the workplace view always aktiv
        super.registerTag("ELEMENT", CmsXmlTemplateFile.class, "handleElementTag", C_REGISTER_MAIN_RUN);
    }
}
