package com.opencms.modules.search.lucene;

/*
* File   : $Source: /alkacon/cvs/opencms/modules/searchlucene/src/com/opencms/modules/search/lucene/Attic/CmsAdminLucene.java,v $
* Date   : $Date: 2003/03/25 14:46:01 $
* Version: $Revision: 1.2 $
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
import com.opencms.boot.*;
import com.opencms.file.*;
import com.opencms.template.cache.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;
import java.util.*;
import java.io.*;
import javax.servlet.http.*;
import com.opencms.workplace.*;
/**
 * Template class for displaying OpenCms workplace Lucene administration.
 * <P>
 *
 * @author g.huhn
 * @version $Revision: 1.2 $ $Date: 2003/03/25 14:46:01 $
 */
public class CmsAdminLucene extends com.opencms.workplace.CmsWorkplaceDefault {

    // the debug flag
    private final static boolean debug = false;
    private static Thread threadArray[];

    /**
     * Gets the content of a defined section in a given template file and its subtemplates
     * with the given parameters.
     *
     * @see getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters)
     * @param cms CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */

    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {

        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);

        // any debug action?
        String info = (String)parameters.get("info");
        if(info != null && "dep_out".equals(info)){}

        // get the parameter
        String action = (String)parameters.get("action");
        //get the modul-properties
        boolean active = OpenCms.getRegistry().getModuleParameterBoolean("com.opencms.modules.search.lucene",
                    ControlLucene.C_ACTIVE);
        boolean indexPDFs = OpenCms.getRegistry().getModuleParameterBoolean("com.opencms.modules.search.lucene",
                    ControlLucene.C_INDEXPDFS);

        if (debug) System.err.println("action="+action);
        if((action == null) || ("".equals(action))){
            // first call, fill the process tags
            Vector files=cms.getAllExportLinks();
            String exportUrl="";
            int html=0, pdf=0;
            for (int i=0;i<files.size();i++) {
                exportUrl=(String)files.get(i);
                if (exportUrl.indexOf(".htm")!=-1) html++;
                else if(exportUrl.indexOf(".pdf")!=-1) pdf++;
            }

            xmlTemplateDocument.setData("htmlfiles",  html+"");
            //
            if (indexPDFs) xmlTemplateDocument.setData("pdffiles", pdf+"");
            else xmlTemplateDocument.setData("pdffiles", " - ");
            if (!active) templateSelector = "not_active";

            //don't start indexing if thread ControlLucene.C_INDEXING allready exists
            ThreadGroup top = Thread.currentThread().getThreadGroup();
            while ( top.getParent() != null ) top = top.getParent();
            threadArray = new Thread[ top.activeCount() ];
            top.enumerate( threadArray );
            for ( int i = 0; i < threadArray.length; i++ ){
                if (threadArray[i]!=null){
                    if (debug) System.out.println(threadArray[i].getName());
                    if (threadArray[i].getName().equalsIgnoreCase(ControlLucene.C_INDEXING)){
                        templateSelector = "allready_indexing";
                    }
                }
            }
        }else{
            // action! index complete Project by lucene
            if (action!=null && action.equalsIgnoreCase("create"))
                    ControlLucene.indexProject(cms);
            if (action!=null && action.equalsIgnoreCase("delete"))
                    ControlLucene.createIndexDirectory();
            if (action!=null && action.equalsIgnoreCase("stop")){
                for ( int i = 0; i < threadArray.length; i++ ){
                    if (threadArray[i]!=null){
                        if (debug) System.out.println(threadArray[i].getName());
                        if (threadArray[i].getName().equalsIgnoreCase(ControlLucene.C_INDEXING)){
                            threadArray[i].interrupt();
                            try {
                                threadArray[i].join();
                            }
                            catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            ControlLucene.createIndexDirectory();
                        }
                    }
                }
            }
            templateSelector = "done";
        }
        if (debug) System.err.println("templateSelector="+templateSelector);
        // Now load the template file and start the processing
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters,
                templateSelector);
    }

    /**
     * Checks if the current user is <strong>administrator</strong> and the element cache is activ.
     * <P>
     * This method is used by workplace icons to decide whether the icon should
     * be activated or not. Icons will use this method if the attribute <code>method="isElementcacheAdmin"</code>
     * is defined in the <code>&lt;ICON&gt;</code> tag.
     *
     * @param cms CmsObject Object for accessing system resources <em>(not used here)</em>.
     * @param lang reference to the currently valid language file <em>(not used here)</em>.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return <code>true</code> if the current project is the online project, <code>false</code> otherwise.
     * @exception CmsException if there were errors while accessing project data.
     */

    public Boolean isElementcacheAdmin(CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) throws CmsException {
        CmsRequestContext reqCont = cms.getRequestContext();
        return new Boolean(reqCont.isAdmin() &&  (reqCont.getElementCache() != null));
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