/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsSearchForm.java,v $
* Date   : $Date: 2004/12/23 10:32:03 $
* Version: $Revision: 1.15 $
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.main.CmsException;

import com.opencms.core.I_CmsSession;
import com.opencms.legacy.CmsXmlTemplateLoader;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * News administration template class
 * <p>
 * Used both for displaying news administration overviews and
 * editing news.
 *
 * @author Edna Falkenhan
 * @version $Revision: 1.15 $ $Date: 2004/12/23 10:32:03 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */

public class CmsSearchForm extends CmsWorkplaceDefault {

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
        // get the session
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        String error = "";
        String reload = "";
        boolean luceneEnabled = false;

        // load the template file
        CmsXmlWpTemplateFile template = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms,
                templateFile, elementName, parameters, templateSelector);
        String action = (String)parameters.get("action")!=null?(String)parameters.get("action"):"";
        String propRestype = (String)parameters.get("proprestype");
        String propKey = (String)parameters.get("propkey");
        String propValue = (String)parameters.get("propvalue");
        String content = (String)parameters.get("content");
        String filename = (String)parameters.get("filename");

        // try to get values for the searchform from the session
        Hashtable filters = (Hashtable)session.getValue("ocms_search.allfilter");
        CmsSearchFormObject propFilter = null;
        CmsSearchFormObject filenameFilter = null;
        CmsSearchFormObject contentFilter = null;
        if(filters != null){
            propFilter = (CmsSearchFormObject)filters.get("property");
            filenameFilter = (CmsSearchFormObject)filters.get("filename");
            contentFilter = (CmsSearchFormObject)filters.get("content");
        }

        if(propFilter != null){
            if(propRestype == null){
                propRestype = propFilter.getValue01()!=null?propFilter.getValue01():"";
            }
            if(propKey == null){
                propKey = propFilter.getValue02()!=null?propFilter.getValue02():"";
            }
            if(propValue == null){
                propValue = propFilter.getValue03()!=null?propFilter.getValue03():"";
            }
        }

        if(filenameFilter != null){
            if(filename == null){
                filename = filenameFilter.getValue01()!=null?filenameFilter.getValue01():"";
            }
        }

        if(contentFilter != null){
            if(content == null){
                content = contentFilter.getValue01()!=null?contentFilter.getValue01():"";
            }
        }

        propRestype = propRestype!=null?propRestype:"";
        propKey = propKey!=null?propKey:"";
        propValue = propValue!=null?propValue:"";
        content = content!=null?content:"";
        filename = filename!=null?filename:"";

        // set the selectboxes
        this.getResourceTypes(cms, template, propRestype);
        this.getPropertyDefs(cms, template, propRestype, propKey);

        // create one CmsSearchFormObject for each search filter
        filters = new Hashtable();
        propFilter = new CmsSearchFormObject("property", propRestype, propKey, propValue);
        filters.put("property", propFilter);
        filenameFilter = new CmsSearchFormObject("filename", filename, "", "");
        filters.put("filename", filenameFilter);
        if(luceneEnabled){
            contentFilter = new CmsSearchFormObject("content", content, "", "");
            filters.put("content", contentFilter);
        }
        session.putValue("ocms_search.allfilter", filters);

        // put the search filter into the session
        if(!"".equals(action.trim())){
            // check if all filter values are set
            if (checkFilter(action, filters)){
                reload = "true";
                session.putValue("ocms_search.currentfilter", action);
            } else {
                error = template.getProcessedDataValue("errormessage");
            }
        }

        // set the values of the template
        template.setData("reload", reload);
        template.setData("error", error);
        template.setData("propvalue", propValue);
        template.setData("filename", filename);
        // set the formular elements depending on lucene
        if(luceneEnabled){
            template.setData("content", content);
            template.setData("searchform",template.getProcessedDataValue("luceneon"));
        } else {
            template.setData("searchform",template.getProcessedDataValue("luceneoff"));
        }

        // Finally start the processing
        byte[] retValue = startProcessing(cms, template, elementName, parameters, templateSelector);
        return retValue;
    }

    /**
     * Create the entries for selectbox for propertydefinitions
     *
     * @param cms The current CmsObject
     * @param lang The current language file
     * @param names The vector for the selectbox names
     * @param values The vector for the selectbox values
     * @param parameters The hashtable that contains the parameters from the request
     *
     * @return int The id of the selected entry
     */
    public Integer getPropertyDefs(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values,
            Hashtable parameters) throws CmsException {
        Integer retValue = new Integer(0);

        String resourcetype = (String)parameters.get("restype");
        String selectedDef = (String)parameters.get("propkey");
        names.add("---");
        values.add("");

        if((resourcetype != null) && (!"".equals(resourcetype.trim()))){
            List propdefs = cms.readAllPropertyDefinitions();

            int i = 0;
            Iterator j = propdefs.iterator();
            while (j.hasNext()) {
                names.add(((CmsPropertyDefinition)j.next()).getName());
                values.add(((CmsPropertyDefinition)j.next()).getName());
                if((((CmsPropertyDefinition)j.next()).getName()).equals(selectedDef)){
                    retValue = new Integer(i+1);
                }
                i++;
            }
        }
        return retValue;
    }

    /**
     *
     */
    private void getPropertyDefs(CmsObject cms, CmsXmlWpTemplateFile template, String restype, String selDef) throws CmsException{
        StringBuffer typeOptions = new StringBuffer();
        template.setData("name","---");
        template.setData("value","");
        template.setData("check","");
        typeOptions.append(template.getProcessedDataValue("selectoption",this));
        if((restype != null) && (!"".equals(restype.trim()))){
            List propdefs = cms.readAllPropertyDefinitions();
            Iterator i = propdefs.iterator();
            while (i.hasNext()) {
                String propdef = ((CmsPropertyDefinition)i.next()).getName();
                template.setData("name",propdef);
                template.setData("value",propdef);
                if(propdef.equals(selDef!=null?selDef:"")){
                    template.setData("check","selected");
                }else{
                    template.setData("check","");
                }
                typeOptions.append(template.getProcessedDataValue("selectoption",this));
            }
        }
        template.setData("propdefs",typeOptions.toString());
    }

    /**
     * Create the entries for selectbox for resourcetypes
     *
     * @param cms The current CmsObject
     * @param lang The current language file
     * @param names The vector for the selectbox names
     * @param values The vector for the selectbox values
     * @param parameters The hashtable that contains the parameters from the request
     *
     * @return int The id of the selected entry
     */
    public Integer getResourceTypes(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values,
            Hashtable parameters) throws CmsException {
        return new Integer(0);
    }

    /**
     *
     */
    private void getResourceTypes(CmsObject cms, CmsXmlWpTemplateFile template, String restype) throws CmsException{
    }

    /**
     * Checks if all necessary filtervalues are set
     * @param action The current filter
     * @param filters The hashtable that contains the CmsSearchFormObject's
     */
     private boolean checkFilter(String action, Hashtable filters){
        CmsSearchFormObject searchFilter = (CmsSearchFormObject)filters.get(action);
        if(searchFilter == null){
            return false;
        }
        if("".equals(searchFilter.getValue01())){
            return false;
        }
        if("property".equals(action)){
            if("".equals(searchFilter.getValue02())){
                return false;
            }
            if("".equals(searchFilter.getValue03())){
                return false;
            }
        }
        return true;
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
