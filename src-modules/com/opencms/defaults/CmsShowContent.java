/**
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

package com.opencms.defaults;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import com.opencms.legacy.CmsLegacyException;
import com.opencms.template.A_CmsXmlContent;
import com.opencms.template.CmsCacheDirectives;
import com.opencms.template.CmsXmlTemplate;
import com.opencms.template.CmsXmlTemplateFile;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Generic class for showing information encapsulated by contentdefinition objects.
 * There are mainly two user methods in this template class for this purpose:
 * getEntry and getList. The getEntry method accepts an id or read it from
 * the url parameters (it has to be named id to be found) if given
 * and set datablocks inside the template with
 * values returned by all public get-methods of the contentdefinition that
 * returns a String and have no parameters. This method must be called in the
 * template before acccessing any of this data with process tags.
 * The getList method builds a list of contentdefinition entries. It invokes
 * a filtermethod defined inside a datablock named &lt;filtermethod&gt;.
 * The user parameter of type String passed to the filtermethod is taken from
 * the url parameters or from a default value. The getList method accepts a name
 * and a defaut value in a comma seperated String in this way : name, defaultvalue.
 * The entrys for the list will be filled by appending a processed datablock
 * with the name listentry. Every get-method will be invoked for every entry
 * unless a tag named &lt;method&gt; is given where a commaseperated list of
 * exactly typed get-method names can be given that results in invoking only
 * this methods (for performance improvement if not all values are needed and
 * many get-methods are given). The filtermethod is assumed to have the following
 * signature: filtermehodname(CmsObject, String). If one wants to use filtermethods
 * with other signatures he has to derive from this class and override the
 * method invokeFilterMethod where the filtermethod is actually invoked.
 * For both methods the fully qualified classname of the contentdefinition
 * has to be stated in a datablock with the name &lt;contentdefinition_class&gt;.
 * Note: the method getUniqueId(CmsObject) will be invoked for the contentdefinition
 * class in any case, and the datablock uniqueid can be used in the template to refer
 * to the id of an contentdefinition object
 * @author Michael Dernen
 *
 * changed: the tagcontent parameter for getList may only use filterparameter0 to
 *          filterparameter9.
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsShowContent extends CmsXmlTemplate {

    /**
     *  The name of the datablock containing the contentdefinition's fully qualified classname.
     */
     protected static final String C_CONTENTDEFINITION_CLASS_DATABLOCK =
        "contentdefinition_class";

    /**
     *  The name of the datablock containing the name of the filtermethod to use.
     */
     protected static final String C_FILTERMETHOD_DATABLOCK =
        "filtermethod";

    /**
     * replaces the tagcontent for usermethod "getList"
     */
    protected static final String C_FILTER_PARAMETERS_START = "filterparameter";

    /**
     *  The name of the datablock to define which get-methods to invoke.
     *  If a datablock with this name is not given all get-methods will be invoked.
     */
     protected static final String C_METHODS_TO_USE_DATABLOCK =
        "methods";

    /**
     * The name of the datablock to define a listentry.
     */
     protected static final String C_LISTENTRY_DATABLOCK = "listentry";

     /**
     * Error message for a missing id parameter.
     */
     protected static final String C_MISSING_ID_PARAMETER =
        "Missing id parameter: "
        + "You have to provide a parameter id (as tagcontent or url parameter) to specify the "
        + "dataentry you want to show";

    /**
     * Error message if the constructor in case the contentdefinition class throws an exception.
     */
     protected static final String C_CONSTRUCTOR_THROWED_EXCEPTION =
        "Failed to create contentdefinition object: "
        + "The constructor of the contentdefinition class throwed an exception";

    /**
     * Error message in case the constructor is not a subclass of A_CmsContentDefinition.
     */
     protected static final String C_CONSTRUCTOR_IS_NOT_SUBCLASS_OF_A_CMSCONTENTDEFINITION =
        "Your contentdefinition class is not a subclass of the abstract A_CmsContentDefinition class";

    /**
     * Error message in case a non-numerical id parameter is given.
     */
     protected static final String C_NON_NUMERICAL_ID_PARAMETER =
        "The parameter id is not a numerical value";

    /**
    * Error text that will be set inside the template if a get-method throws an exception.
    */
    protected static final String C_ERROR_TEXT = "ERROR";

    /**
     * The name of the id parameter.
     */
     protected static final String C_ID_PARAMETER = "id";

    /**
     * Usermethod to fill template datablocks with values of a contentdefinition's get-methods.
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent the tagcontent passed to the method. This can be an id
     * value which will be used if the parameter id is not found parameters passed
     * with the request (url parameters).
     * @param doc reference to the A_CmsXmlContent object of the initiating XLM document
     * @param userObject normally the Hashtable with the url parameters
     * @return String or byte[] with the content of this subelement
     * @throws org.opencms.main.CmsException in case of unrecoverable errors
     */
    public Object getEntry(CmsObject cms, String tagcontent,
            A_CmsXmlContent doc, Object userObject) throws CmsException {

        CmsXmlTemplateFile template = (CmsXmlTemplateFile)doc;

        String paramId = null;
        if (tagcontent != null && !tagcontent.trim().equals("")) {
            // if a tagcontent is given take it as the id of the entry to be shown
            paramId = tagcontent;
        } else {
            // get the id of the entry to be shown from the url parameters
            paramId = (String)((Hashtable)userObject).get(C_ID_PARAMETER);
        }
        Integer id = null;
        if (paramId != null) {
            // try to convert the id to an Integer object
            try {
                id = new Integer(paramId);
            } catch (NumberFormatException e) {
                // throw exception if the id is not numeric
                throw new CmsLegacyException(C_NON_NUMERICAL_ID_PARAMETER, CmsLegacyException.C_UNKNOWN_EXCEPTION);
            }
        } else {
            // throw exception if id is not given (not as a tagcontent or url parameter)
            throw new CmsLegacyException(C_MISSING_ID_PARAMETER);
        }
        // first get the contentdefinition's classname from a datablock
        String cdClassname = template.getDataValue(C_CONTENTDEFINITION_CLASS_DATABLOCK);
        try {
            ArrayList getMethods = null;
            // try to get class object might throws ClassNotFoundException
            Class cdClass = Class.forName(cdClassname);
            // might throws NoSuchMethodException
            Constructor constructor = cdClass.getConstructor(new Class[] {CmsObject.class, Integer.class});
            // might throws InvocationTargetException, ClassCastException
            A_CmsContentDefinition cdObject = (A_CmsContentDefinition)constructor.newInstance(new Object[]{cms, id});
            // register the CD for the variantdependencies
            Vector cdVec = new Vector();
            cdVec.add(cdObject);
            registerVariantDeps(cms, doc.getAbsoluteFilename(), null, null,
                                (Hashtable)userObject, null, cdVec, null);
            boolean showIt = true;
            if (cdObject.isTimedContent()) {
                I_CmsTimedContentDefinition curTimed = (I_CmsTimedContentDefinition)cdObject;
                long currentTime = System.currentTimeMillis();
                if (((curTimed.getPublicationDate() != 0) && (currentTime < curTimed.getPublicationDate()))
                        || ((curTimed.getPurgeDate() != 0) && (currentTime > curTimed.getPurgeDate()))) {
                    showIt = false;
                }
            }
            if (!showIt) {
                //  TODO: read an datablock from the template and set all the proccesstags with it then remove this exception
                throw new CmsLegacyException("requested content is not valid.");
            }
            if (template.hasData(C_METHODS_TO_USE_DATABLOCK)) {
                // if the datablock methods is set inside the template
                // only take the methods that are listed in this datablock
                // (the methods should be listed as a comma seperated list of names)
                String datablockContent = template.getDataValue(C_METHODS_TO_USE_DATABLOCK);
                StringTokenizer tokenizer = new StringTokenizer(datablockContent, ",");
                int tokens = tokenizer.countTokens();
                String[] names = new String[tokens];
                for (int i=0; i < tokens; i++) {
                    names[i] = tokenizer.nextToken().trim();
                }
                getMethods = getGetMethodsByName(cdClass, names);
            } else {
                // get all public getMethods that return a String and have no parameters
                getMethods = getGetMethods(cdClass);
            }
            try {
                Method getUniqueIdMethod = cdClass.getMethod("getUniqueId", new Class[] {CmsObject.class});
                template.setData("uniqueid", (String)getUniqueIdMethod.invoke(cdObject, new Object[] {cms}));
            } catch (Exception e) { }
            setDatablocks(template, cdObject, getMethods);
        } catch (InvocationTargetException e) {
            // the constructor has throwed an exception, InvocationTargetExceptions of the egt-methods
            // will be catched inside the setDatablocks method and cannot propagate to this point
            throw new CmsLegacyException(C_CONSTRUCTOR_THROWED_EXCEPTION, e.getTargetException());
        } catch (ClassCastException e) {
            // in this case the cast to A_CmsContentDefinition failed
            throw new CmsLegacyException(C_CONSTRUCTOR_IS_NOT_SUBCLASS_OF_A_CMSCONTENTDEFINITION, e);
        } catch (Exception e) {
            // rethrow any other exception
            if (e instanceof CmsException) {
                throw (CmsException)e;
            } else {
                // encapsulate in CmsException
                throw new CmsLegacyException (e.getMessage(), e);
            }
        }
        return "";
    }

    /**
     * User-method to build a list of contentdefinition entries.
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent comma separated name, default value pair. The name
     * is the name of an url parameter which value will be passed to the
     * filtermethod if given. If the url parameter is not found the default
     * value will be used as a userparameter for the filtermethod.
     * @param doc reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param userObject normally the Hashtable with url parameters.
     * @return String or byte[] with the content of this subelement.
     * @throws org.opencms.main.CmsException in case of unrecoverable errors
     */
    public Object getList(CmsObject cms, String tagcontent,
            A_CmsXmlContent doc, Object userObject) throws CmsException {
        StringBuffer list = new StringBuffer();
        ArrayList getMethods = null;
        Hashtable parameters = (Hashtable) userObject;
        CmsXmlTemplateFile template = (CmsXmlTemplateFile) doc;
        String contentDefinitionName = template.getDataValue(C_CONTENTDEFINITION_CLASS_DATABLOCK);
        String filterMethodName = template.getDataValue(C_FILTERMETHOD_DATABLOCK);

        try {
            // get contentdefinition class might throws ClassNotFoundException
            Class cdClass = Class.forName(contentDefinitionName);
            //register the class for the dependencies
            Vector theClass = new Vector();
            theClass.add(cdClass);
            Vector allCdClasses = new Vector();
            // get getFilterMethods method might throws NoSuchMethodException, IllegalAccessException
            String userParameter = getUserParameter(parameters, tagcontent);
            Vector cdObjects =  invokeFilterMethod(cms, cdClass, filterMethodName, userParameter);
            if (template.hasData(C_METHODS_TO_USE_DATABLOCK)) {
                // if the datablock methods is set inside the template
                // only take the methods that are listed in this datablock
                // (the methods should be listed as a comma seperated list of names)
                String datablockContent = template.getDataValue(C_METHODS_TO_USE_DATABLOCK);
                StringTokenizer tokenizer = new StringTokenizer(datablockContent, ",");
                int tokens = tokenizer.countTokens();
                String[] names = new String[tokens];
                for (int i=0; i < tokens; i++) {
                    names[i] = tokenizer.nextToken().trim();
                }
                getMethods = getGetMethodsByName(cdClass, names);
            } else {
                // get all public getMethods that return a String and have no parameters
                getMethods = getGetMethods(cdClass);
            }
            // walk through Vector and fill content
            int size = cdObjects.size();
            long currentTime = System.currentTimeMillis();
            for (int i=0; i < size; i++) {
                boolean showIt = true;
                A_CmsContentDefinition curCont = (A_CmsContentDefinition)cdObjects.elementAt(i);
                if (curCont.isTimedContent()) {
                    allCdClasses.add(curCont);
                    I_CmsTimedContentDefinition curTimed = (I_CmsTimedContentDefinition)curCont;
                    if (((curTimed.getPublicationDate() != 0) && (currentTime < curTimed.getPublicationDate()))
                            || ((curTimed.getPurgeDate() != 0) && (currentTime > curTimed.getPurgeDate()))) {
                        showIt = false;
                    }
                }
                if (showIt) {
                    try {
                        Method getUniqueIdMethod = cdClass.getMethod("getUniqueId", new Class[] {CmsObject.class});
                        template.setData("uniqueid", (String)getUniqueIdMethod.invoke(curCont, new Object[] {cms}));
                    } catch (Exception e) {
                    }
                    setDatablocks(template, curCont, getMethods);
                    list.append(template.getProcessedDataValue(C_LISTENTRY_DATABLOCK, this));
                }
            }
            //register the classes for the dependencies
            registerVariantDeps(cms, doc.getAbsoluteFilename(), null, null, parameters, null, allCdClasses, theClass);
        } catch (Exception e) {
            if (e instanceof CmsException) {
                throw (CmsException) e;
            } else {
                throw new CmsLegacyException (e.getMessage(), CmsLegacyException.C_UNKNOWN_EXCEPTION, e);
            }
        }
        return list.toString();
    }

    /**
     * Gets the userparameter from tagcontent of the getList method-tag or from url parameters.
     * The tagcontent should contain the name of an url parameter and a default
     * value for this parameter. For example: name,B.
     * If the url parameter can be fetched from the parameters Hashtable
     * this value will be returned otherwise the default value
     * (in this example B). If the tagcontent contains a String without
     * any commas this value will be taken as the userparameter.
     * @param parameters Hashtable with the url parameters
     * @param tagcontent String with the content of the method tag
     * @return String with the value of the userparameter
     * @throws CmsException if something goes wrong
     */
   protected String getUserParameter (Hashtable parameters, String tagcontent) throws CmsException {
        String userparameter = "";
        String parameterName = null;
        String parameterValue = null;
        if (tagcontent != null) {
            int index = tagcontent.indexOf(",");
            if (index != -1) {
                parameterName = tagcontent.substring(0, index);
                // todo: check also if the length is ok and if the last char is in {0,1,..,9}
                if (!(parameterName.startsWith(C_FILTER_PARAMETERS_START))) {
                    throw new CmsLegacyException("The filterparameter has to be \""
                            +C_FILTER_PARAMETERS_START+"N\" where 0 <= N <= 9.");
                }
                parameterValue = (String)parameters.get(parameterName);
                if (parameterValue != null) {
                    userparameter = parameterValue;
                } else if (tagcontent.length() > index+1) {
                    userparameter = tagcontent.substring(index+1);
                }
            } else {
                return tagcontent;
            }
        }
        return userparameter;
    }

    /**
    * This methods collects all "getXYZ" methods of the contentdefinition.
    * @param cdClass the class object of the contentdefinition class
    * @return ArrayList of java.lang.reflect.Method objects
    */
    protected ArrayList getGetMethods (Class cdClass) {
        // the Vector of methods to return
        ArrayList getMethods = new ArrayList();
        // get an array of all public member methods
        Method[] methods = cdClass.getMethods();
        Method m    = null;
        String name = null;
        //get all public get-methods which return a String
        for (int i=0; i < methods.length; i++) {
            m    = methods[i];
            name = m.getName().toLowerCase();
            //now extract all methods whose name starts with a "get"
            if (name.startsWith("get")) {
                //only take methods that have no parameter and return a String
                if (m.getReturnType().equals(String.class) && m.getParameterTypes().length == 0) {
                    getMethods.add(m);
                }
            }
         }
        return getMethods;
    }

    /**
    * This methods collects all get-methods with the names specified in the
    * String array names. All these methods has to be without parameters and
    * should return a String (if they don't return a String there might
    * be thrown an exception, when the return value will be casted to a String
    * in the method setDatablocks which will result in setting an error text
    * instead of the output of the method inside the template).<p>
    * 
    * @param cdClass the class object of the contentdefinition class
    * @param names array of method names
    * @return ArrayList of java.lang.reflect.Method objects
    * @throws NoSuchMethodException in case of unrecoverable errors
    */
    protected ArrayList getGetMethodsByName (Class cdClass, String[] names)
            throws NoSuchMethodException {
        // the list of methods to return
        ArrayList getMethods = new ArrayList();
        Class[] argTypes = new Class[0];
        for (int i=0; i < names.length; i++) {
            getMethods.add(cdClass.getMethod(names[i], argTypes));
        }
        return getMethods;
    }

    /**
    * This method automatically fills all datablocks in the template that fit
    * to a special name scheme.
    * A datablock named "xyz" is filled with the value of a "getXYZ" method
    * form the content defintion.
    * @param template The template file of this template
    * @param contentDefinition The actual content defintion object
    * @param methods A vector of java.lang.reflect.Method objects with all "getXYZ" methods to be used
    * @throws org.opencms.main.CmsException in case of unrecoverable erros
    */
    protected void setDatablocks(CmsXmlTemplateFile template,
            A_CmsContentDefinition contentDefinition,
            ArrayList methods) throws CmsException {
        String datablockName= null;
        Method method = null;
        int size = methods.size();
        Object[] args = new Object[0];
        for (int i=0; i < size; i++) {
            // get the method name
            method = (Method)methods.get(i);
            //get the datablock name - the methodname without the leading "get"
            datablockName = (method.getName().substring(3)).toLowerCase();
            //check if the datablock name ends with a "string" if so, remove it from the datablockname
            if (datablockName.endsWith("string")) {
                datablockName = datablockName.substring(0, datablockName.lastIndexOf("string"));
            }
            // now call the method to get the value
            try {
                template.setData(datablockName, (String)method.invoke(contentDefinition, args));
            } catch (Exception e) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Error during automatic call method '" + method.getName(), e);
                }
                // set datablock with error text to indicate that calling the get-method failed
                template.setData(datablockName, C_ERROR_TEXT);
            }
        } // for
    }

    /**
     * Invokes the filtermethod and returns a Vector of contentdefinition
     * objects returned by the filtermethod.
     * If you want to use filtermethods with other sinatures you have to override
     * this method in your own derived subclass.<p>
     * 
     * @param cms the cms object
     * @param cdClass Class object of the ContentDefinition class
     * @param name the name of the filtermethod to invoke
     * @param userparameter the value of the userparameetr which will be passed to the filtermethod
     * @return Vector of contentdefinition objects
     * @throws NoSuchMethodException if the filtermethod doesn't exist
     * @throws InvocationTargetException if the invoked filtermethod throws an exception itself
     * @throws IllegalAccessException if the filtermethod is inaccessible
     */
     protected Vector invokeFilterMethod (CmsObject cms, Class cdClass, String name, String userparameter)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = cdClass.getMethod(name, new Class[] {CmsObject.class, String.class});
        return (Vector)method.invoke(cdClass, new Object[] {cms, userparameter});
     }

    /**
    * Gets the caching information from the current template class.
    *
    * @param cms CmsObject Object for accessing system resources
    * @param templateFile Filename of the template file
    * @param elementName Element name of this template in our parent template
    * @param parameters Hashtable with all template class parameters
    * @param templateSelector template section that should be processed
    * @return object with caching information
    */
    public CmsCacheDirectives getCacheDirectives(CmsObject cms, String templateFile,
                String elementName, Hashtable parameters, String templateSelector) {

        CmsCacheDirectives result = new CmsCacheDirectives(true, false, false, false, false);
        result.setCacheUri(true);
        result.noAutoRenewAfterPublish();
        Vector para = new Vector();
        para.add(C_ID_PARAMETER);
        for (int i=0; i < 10; i++) {
            para.add(C_FILTER_PARAMETERS_START + i);
        }
        result.setCacheParameters(para);

        return result;
    }
}