/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/linkmanagement/Attic/LinkChecker.java,v $
* Date   : $Date: 2003/08/18 10:50:48 $
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
package com.opencms.linkmanagement;

import org.opencms.main.OpenCms;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.report.I_CmsReport;
import com.opencms.template.CmsTemplateClassManager;
import com.opencms.template.CmsXmlControlFile;
import com.opencms.template.CmsXmlTemplate;
import com.opencms.template.CmsXmlTemplateFile;

import java.util.Vector;

/**
 * Extracts all links (anchors) out of a OpenCms page.
 *
 * @author Hanjo Riege
 * @version 1.0
 */

public class LinkChecker {

    public LinkChecker() {
    }

    /**
     * Checks the content of the page and extracts all links that are on this page.
     * Used by the linkmanagement to save the links in the database.
     *
     * @param cms The CmsObject.
     * @param page The name(getAbsolutePath) of the page
     * @return The compleate CmsPageLinks object for this page.
     */
    public CmsPageLinks extractLinks(CmsObject cms, String page) throws CmsException{

        // first lets get the prefix of the page name (we need it later)
        CmsResource resource = cms.readFileHeader(page);
        String rootName = cms.getRequestContext().getSiteRoot();
        //String rootName = cms.getRootName(page);
        
        // get the pages content
        String bodyFileName = null;
        String bodyClassName = null;
        CmsXmlTemplateFile bodyTemplateFile = null;

        boolean isSimplePage = (null != cms.readProperty(page, I_CmsConstants.C_PROPERTY_TEMPLATE));
        
        if (isSimplePage) {
            
            CmsXmlTemplate bodyClassObject = (CmsXmlTemplate)CmsTemplateClassManager.getClassInstance(cms, I_CmsConstants.C_XML_CONTROL_DEFAULT_CLASS);
            bodyTemplateFile = bodyClassObject.getOwnTemplateFile(cms, page, I_CmsConstants.C_XML_BODY_ELEMENT, null, null);

        } else {
        
            CmsXmlControlFile pageControlFile = new CmsXmlControlFile(cms, page);
            if(pageControlFile.isElementTemplateDefined(I_CmsConstants.C_XML_BODY_ELEMENT)){
                bodyFileName = pageControlFile.getElementTemplate(I_CmsConstants.C_XML_BODY_ELEMENT);
                bodyFileName = pageControlFile.validateBodyPath(cms, bodyFileName, resource);
            }
            if(pageControlFile.isElementClassDefined(I_CmsConstants.C_XML_BODY_ELEMENT)){
                bodyClassName = pageControlFile.getElementClass(I_CmsConstants.C_XML_BODY_ELEMENT);
            }
            CmsXmlTemplate bodyClassObject = (CmsXmlTemplate)CmsTemplateClassManager.getClassInstance(cms, bodyClassName);
            bodyTemplateFile = bodyClassObject.getOwnTemplateFile(cms,bodyFileName, I_CmsConstants.C_XML_BODY_ELEMENT, null, null);
        
        }
        
        Vector result = bodyTemplateFile.getAllLinkTagValues();
        // we have to cleanup the result. Relative links will be inserted as absolute links
        // and we cut the parameters.
        Vector cleanResult = new Vector(result.size());
        for(int i=0; i<result.size(); i++){
            String work = (String)result.elementAt(i);
            // first the parameters
            int paraStart = work.indexOf("?");
            if(paraStart >= 0){
                work = work.substring(0, paraStart);
            }
            // dont forget the anker links
            int ankerStart = work.indexOf("#");
            if(ankerStart >= 0){
                work = work.substring(0, ankerStart);
            }
            // here is something for the future: if link starts with /// it is the full name of the resource
            if(work.startsWith("///")){
                work = work.substring(2);
            }else{
                // now for relative links
                work = OpenCms.getLinkManager().getAbsoluteUri(work, page);
                // now we need the site prefix (lets take it from the page itself)
                work = rootName +work;
            }
            cleanResult.add(work);
        }
        return new CmsPageLinks(cms.readFileHeader(page).getResourceId(), cleanResult);
    }

    /**
     * This Method checks if the online project has broken links when the project with
     * the projectId is published (if projectId = onlineProjectId it simply checks the
     * online project).
     *
     * @param cms The CmsObject.
     * @param projectId The id of the project to be published.
     * @param report A cmsReport object for logging while the method is still running.
     *
     * The report is filled with a CmsPageLinks object for each page containing broken links
     *          this CmsPageLinks object contains all links on the page withouth a valid target.
     */
    public void checkProject(CmsObject cms, int projectId, I_CmsReport report) throws CmsException {
        
        // TODO the link management is disabled and has to be rebuilt on a link<->UUID basis instead of link<->resourcename
        return;
        
        /*
        CmsResource currentResource = null;
        int i = 0;
        
        if (projectId == CmsObject.C_PROJECT_ONLINE_ID) {
            // lets check only the online project
            Vector result = cms.getOnlineBrokenLinks();
            for (i = 0; i < result.size(); i++) {
                report.println((CmsPageLinks) result.elementAt(i));
            }
        } else {
            // we are in a project. First get the changed, new ,deleted resources
            Vector deleted = cms.readProjectView(projectId, "deleted");
            Vector changed = cms.readProjectView(projectId, "changed");
            Vector newRes = cms.readProjectView(projectId, "new");
            
            for (i=0;i<deleted.size();i++) {
                currentResource = (CmsResource) deleted.elementAt(i);
                cms.readAbsolutePath(currentResource);
            }
            
            for (i=0;i<changed.size();i++) {
                currentResource = (CmsResource) changed.elementAt(i);
                cms.readAbsolutePath(currentResource);
            }  
            
            for (i=0;i<newRes.size();i++) {
                currentResource = (CmsResource) newRes.elementAt(i);
                cms.readAbsolutePath(currentResource);
            } 
            
            cms.getBrokenLinks(projectId, report, changed, deleted, newRes);
        }
        */
    }
    
}