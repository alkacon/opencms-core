/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminProperties.java,v $
* Date   : $Date: 2003/09/17 08:31:28 $
* Version: $Revision: 1.26 $
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

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.opencms.core.CmsException;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsPropertydefinition;
import com.opencms.file.I_CmsResourceType;
import com.opencms.template.A_CmsXmlContent;
import com.opencms.template.CmsXmlTemplateFile;
import com.opencms.util.Encoder;
import com.opencms.util.Utils;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Template class for displaying OpenCms workplace admin properties
 * <P>
 *
 * @author Mario Stanke
 * @version $Revision: 1.26 $ $Date: 2003/09/17 08:31:28 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */

public class CmsAdminProperties extends CmsWorkplaceDefault {


    /** XML datablock tag used for setting the resource type name */
    private static final String C_TAG_RESTYPE = "restype";


    /** XML datablock tag used for setting an entry in the list of datatypes */
    private static final String C_TYPELISTENTRY = "typelistentry";


    /** XML datablock tag used for setting all collected entries */
    private static final String C_TAG_ALLENTRIES = "allentries";


    /** XML datablock tag used for getting a processed resource type entry */
    private static final String C_TAG_RESTYPEENTRY = "restypeentry";


    /** XML datablock tag used for getting a processed separator entry */
    private static final String C_TAG_SEPARATORENTRY = "separatorentry";


    /** XML datablock tag used for getting the complete and processed content to be returned */
    private static final String C_TAG_SCROLLERCONTENT = "scrollercontent";

    /**
     * Gets the content of a defined section in a given template file and its subtemplates
     * with the given parameters.
     *
     * @see #getContent(CmsObject, String, String, Hashtable, String)
     * @param cms CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */

    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {
        if(OpenCms.getLog(CmsLog.CHANNEL_WORKPLACE_XML).isWarnEnabled() && C_DEBUG) {
            OpenCms.getLog(CmsLog.CHANNEL_WORKPLACE_XML).warn(this.getClassName() + "getting content of element "
                    + ((elementName == null) ? "<root>" : elementName));
            OpenCms.getLog(CmsLog.CHANNEL_WORKPLACE_XML).warn(this.getClassName() + "template file is: "
                    + templateFile);
            OpenCms.getLog(CmsLog.CHANNEL_WORKPLACE_XML).warn(this.getClassName() + "selected template section is: "
                    + ((templateSelector == null) ? "<default>" : templateSelector));
        }

        //CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
        //CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);
        CmsXmlLanguageFile lang = xmlTemplateDocument.getLanguageFile();
        String action = (String)parameters.get("action");
        String resTypeName = (String)parameters.get("restype");
        String propDefName = (String)parameters.get("propdef");
        xmlTemplateDocument.setData("RESTYPE", resTypeName);
        if("new".equals(action)) {
            templateSelector = "newtype";
            String name = (String)parameters.get("NAME");
            if(name == null || name.equals("")) {


            // form has not yet been submitted
            }
            else {
                try {
                    cms.createPropertydefinition(name, cms.getResourceTypeId(resTypeName));
                    templateSelector = "";
                }
                catch(CmsException e) {
                    StringBuffer errmesg = new StringBuffer();
                    errmesg.append(lang.getLanguageValue("error.reason.newprop1") + " '"
                            + name + "' " + lang.getLanguageValue("error.reason.newprop2")
                            + " '" + resTypeName + "' "
                            + lang.getLanguageValue("error.reason.newprop3") + "\n\n");
                    errmesg.append(Utils.getStackTrace(e));
                    xmlTemplateDocument.setData("NEWDETAILS", errmesg.toString());
                    templateSelector = "newerror";
                }
            }
        }
        else {
            if("delete".equals(action)) {
                if("true".equals(parameters.get("sure"))) {

                    // the user is sure to delete the property definition
                    cms.deletePropertydefinition(propDefName, cms.getResourceTypeId(resTypeName));
                    templateSelector = "";
                }
                else {
                    templateSelector = "RUsuredelete";
                }
                xmlTemplateDocument.setData("PROPERTY_NAME", propDefName);
            }
        }

        // Now load the template file and start the processing
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters,
                templateSelector);
    }

    /**
     * Used by the <code>&lt;PREFSSCROLLER&gt;</code> tag for getting
     * the content of the scroller window.
     * <P>
     * Gets all available resource types and returns a list
     * using the datablocks defined in the own template file.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param lang reference to the currently valid language file
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index representing the user's current filter view in the vectors.
     * @throws CmsException
     */

    public String getDatatypes(CmsObject cms, A_CmsXmlContent doc, CmsXmlLanguageFile lang,
            Hashtable parameters, Object callingObj) throws CmsException {
        StringBuffer result = new StringBuffer();
        CmsXmlTemplateFile templateFile = (CmsXmlTemplateFile)doc;
        List allResTypes = cms.getAllResourceTypes();
        Iterator i = allResTypes.iterator();

        // Loop through all resource types
        while(i.hasNext()) {
            I_CmsResourceType currResType = (I_CmsResourceType)i.next();
            result.append(getResourceEntry(cms, doc, lang, parameters, callingObj, currResType));
            if(i.hasNext()) {
                result.append(templateFile.getProcessedDataValue(C_TAG_SEPARATORENTRY,
                        callingObj));
            }
        }
        templateFile.setData(C_TAG_ALLENTRIES, result.toString());
        return templateFile.getProcessedDataValue(C_TAG_SCROLLERCONTENT, callingObj);
    }

    /**
     *
     * gets the HTML code for entry in the lists of resources.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param doc the template file which is used
     * @param lang reference to the currently valid language file
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @param callingObject Object for accessing system resources.
     * @param resType resource type (file type)
     * @return String which holds a HTML table
     * @throws CmsException
     */

    private String getResourceEntry(CmsObject cms, A_CmsXmlContent doc,
            CmsXmlLanguageFile lang, Hashtable parameters, Object callingObject,
            I_CmsResourceType resType) throws CmsException {
        StringBuffer output = new StringBuffer();
        CmsXmlWpTemplateFile templateFile = (CmsXmlWpTemplateFile)doc;
        Vector properties = cms.readAllPropertydefinitions(resType.getResourceTypeName());
        templateFile.setData(C_TAG_RESTYPE, resType.getResourceTypeName());

        templateFile.setData(C_TAG_RESTYPE + "_esc",
                Encoder.escapeWBlanks(resType.getResourceTypeName(),
                cms.getRequestContext().getEncoding()));
        output.append(templateFile.getProcessedDataValue(C_TAG_RESTYPEENTRY, callingObject));
        for(int z = 0;z < properties.size();z++) {
            CmsPropertydefinition propdef = (CmsPropertydefinition)properties.elementAt(z);
            templateFile.setData("PROPERTY_NAME", propdef.getName());
            templateFile.setData("PROPERTY_NAME_ESC", Encoder.escapeWBlanks(propdef.getName(),
                cms.getRequestContext().getEncoding()));
            output.append(templateFile.getProcessedDataValue(C_TYPELISTENTRY, callingObject));
        }
        return output.toString();
    }

    /**
     * Indicates if the results of this class are cacheable.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */

    public boolean isCacheable(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) {
        return false;
    }
}
