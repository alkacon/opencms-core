/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminStaticExport.java,v $
* Date   : $Date: 2003/09/05 12:22:25 $
* Version: $Revision: 1.27 $
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

import org.opencms.main.OpenCms;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsObject;
import org.opencms.report.A_CmsReportThread;
import org.opencms.threads.*;

import com.opencms.util.Encoder;

import java.util.Hashtable;
import java.util.Vector;

import org.apache.oro.text.perl.Perl5Util;

/**
 * Template class for displaying OpenCms workplace admin static export.
 * <P>
 *
 * @author Hanjo Riege
 * @version $Revision: 1.27 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */

public class CmsAdminStaticExport extends CmsWorkplaceDefault {

    private static String C_STATICEXPORT_THREAD = "static_export_thread";

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
        if(C_DEBUG && OpenCms.isLogging(C_OPENCMS_DEBUG)) {
            OpenCms.log(C_OPENCMS_DEBUG, this.getClassName()+"getting content of element "+((elementName == null) ? "<root>" : elementName));
            OpenCms.log(C_OPENCMS_DEBUG, this.getClassName()+"template file is: " + templateFile);
            OpenCms.log(C_OPENCMS_DEBUG, this.getClassName()+"selected template section is: "+((templateSelector == null) ? "<default>" : templateSelector));
        }

        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);
        I_CmsSession session = cms.getRequestContext().getSession(true);
        CmsXmlLanguageFile lang = xmlTemplateDocument.getLanguageFile();

        // get the parameters
        String action = (String)parameters.get("action");

        // here we show the report updates when the threads are allready running.
        if("showResult".equals(action)){
            // ok. Thread is started and we shoud show the report information.
            A_CmsReportThread doTheWork = (A_CmsReportThread)session.getValue(C_STATICEXPORT_THREAD);
            //still working?
            if(doTheWork.isAlive()){
                xmlTemplateDocument.setData("endMethod", "");
                xmlTemplateDocument.setData("text", "");
            }else{
                xmlTemplateDocument.setData("endMethod", xmlTemplateDocument.getDataValue("endMethod"));
                xmlTemplateDocument.setData("autoUpdate","");
                xmlTemplateDocument.setData("text", lang.getLanguageValue("staticexport.label.exportend"));
            }
            xmlTemplateDocument.setData("data", doTheWork.getReportUpdate());
            return startProcessing(cms, xmlTemplateDocument, elementName, parameters, "updateReport");
        }
        if(action == null || "dynPrint".equals(action)) {
            // This is an initial request of the static export page
            Vector exportStartPoints = null;
            String allStartPoints = "";
            if(exportStartPoints != null){
                for(int i=0; i<exportStartPoints.size(); i++){
                    xmlTemplateDocument.setData("entry", (String)exportStartPoints.elementAt(i));
                    allStartPoints += xmlTemplateDocument.getProcessedDataValue("exportpoint");
                }
            }
            xmlTemplateDocument.setData("exportpoints", allStartPoints);
            xmlTemplateDocument.setData("path", OpenCms.getStaticExportManager().getExportPath());
        }

        // special feature to test the regular expressions
        if((action != null) && ("regTest".equals(action))) {
            String sub = (String)parameters.get("sub");
            String link = "";
            String regExpr = "";
            String result = "";
            if(sub != null && "true".equals(sub)){
                link = (String)parameters.get("link");
                regExpr = (String)parameters.get("regExpr");
                try{
                    Perl5Util subClass = new Perl5Util();
                    result = subClass.substitute(regExpr, link);
                }catch(Exception e){
                    result = "error: "+ e.getMessage();
                }
            }
            xmlTemplateDocument.setData("link", Encoder.escape(link,
                cms.getRequestContext().getEncoding()));
            xmlTemplateDocument.setData("regExpr", Encoder.escape(regExpr,
                cms.getRequestContext().getEncoding()));
            xmlTemplateDocument.setData("result", Encoder.escape(result,
                cms.getRequestContext().getEncoding()));
            return startProcessing(cms, xmlTemplateDocument, elementName, parameters, "regTest");
        }

        // first we look if the thread is allready running
        if((action != null) && ("working".equals(action))) {
            // still working?
            A_CmsReportThread doTheWork = (A_CmsReportThread)session.getValue(C_STATICEXPORT_THREAD);
            if(doTheWork.isAlive()) {
                String time = (String)parameters.get("time");
                int wert = Integer.parseInt(time);
                wert += 20;
                xmlTemplateDocument.setData("time", "" + wert);
                return startProcessing(cms, xmlTemplateDocument, elementName, parameters, "wait");
            }else {
                // thread has come to an end, was there an error?
                String errordetails = (String)session.getValue(C_SESSION_THREAD_ERROR);
                session.removeValue(C_SESSION_THREAD_ERROR);
                if(errordetails == null) {
                    // export ready
                    return startProcessing(cms, xmlTemplateDocument, elementName, parameters, "done");
                }else {
                    // get errorpage:
                    xmlTemplateDocument.setData("details", errordetails);
                    return startProcessing(cms, xmlTemplateDocument, elementName, parameters, "error");
                }
            }
        }
        if("export".equals(action)) {

            // start the thread for export
            // first clear the session entry if necessary
            if(session.getValue(C_SESSION_THREAD_ERROR) != null) {
                session.removeValue(C_SESSION_THREAD_ERROR);
            }
            A_CmsReportThread doExport = new CmsStaticExportThread(cms);
            doExport.start();
            session.putValue(C_STATICEXPORT_THREAD , doExport);
            xmlTemplateDocument.setData("time", "10");
            templateSelector = "showresult";
        }

        // Now load the template file and start the processing
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
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

    /**
     * Returns <code>true</code> if the staticExport is enabled, 
     * the current project is the online project
     * and the current user is in the project manager group.<p>
     * 
     * This method is used by workplace icons to decide whether the icon should
     * be activated or not. Icons will use this method if the attribute <code>method="isExportActive"</code>
     * is defined in the <code>&lt;ICON&gt;</code> tag.
     *
     * @param cms for accessing system resources <em>(not used here)</em>
     * @param lang reference to the currently valid language file <em>(not used here)</em>
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>
     * @return (see method description)
     * @throws CmsException if there were errors while accessing project data
     */
    public Boolean isExportActive(CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) throws CmsException {
        boolean isProMan = isProjectManager(cms, lang, parameters).booleanValue();
        return new Boolean(OpenCms.getStaticExportManager().isStaticExportEnabled() && isProMan);
    }

    /**
     * Returns <code>true</code> if the staticExport is enabled, 
     * the current project is the online project
     * and the current user is in the administrator group.<p>
     * 
     * This method is used by workplace icons to decide whether the icon should
     * be activated or not. Icons will use this method if the attribute <code>method="isExportActiveAdmin"</code>
     * is defined in the <code>&lt;ICON&gt;</code> tag.
     *
     * @param cms for accessing system resources <em>(not used here)</em>
     * @param lang reference to the currently valid language file <em>(not used here)</em>
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>
     * @return (see method description)
     * @throws CmsException if there were errors while accessing project data
     */
    public Boolean isExportActiveAdmin(CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) throws CmsException {
        boolean isAdmin = isAdmin(cms, lang, parameters).booleanValue();
        return new Boolean(OpenCms.getStaticExportManager().isStaticExportEnabled() && isAdmin);
    }
}