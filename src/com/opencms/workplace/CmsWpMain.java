package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import java.util.*;

/**
 * Common template class for displaying OpenCms workplace main screen.
 * <P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.2 $ $Date: 2000/01/28 11:43:37 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsWpMain extends CmsWorkplaceDefault {
           
    /**
     * Indicates if the results of this class are cacheable.
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */
    public boolean isCacheable(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return false;
    }
    

    /**
     * Gets the content of a defined section in a given template file and its subtemplates
     * with the given parameters. 
     * 
     * @see getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters)
     * @param cms A_CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        if(C_DEBUG && A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_DEBUG, "[CmsXmlTemplate] getting content of element " + ((elementName==null)?"<root>":elementName));
            A_OpenCms.log(C_OPENCMS_DEBUG, "[CmsXmlTemplate] template file is: " + templateFile);
            A_OpenCms.log(C_OPENCMS_DEBUG, "[CmsXmlTemplate] selected template section is: " + ((templateSelector==null)?"<default>":templateSelector));
        }

        A_CmsRequestContext reqCont = cms.getRequestContext();
        String newGroup = (String)parameters.get("group");
        String newProject = (String)parameters.get("project");
        
        // Check if the user requested a group change
        if(newGroup != null && !("".equals(newGroup))) {
            if(!(newGroup.equals(reqCont.currentGroup().getName()))) {
                reqCont.setCurrentGroup(newGroup);
            }
        }                            

        // Check if the user requested a project change
        if(newProject != null && !("".equals(newProject))) {
            if(!(newProject.equals(reqCont.currentProject().getName()))) {
                reqCont.setCurrentProject(newProject);
            }
        }                            
        
        CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }            
    
    
    public Object getInformation(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) 
            throws CmsException {
        Hashtable parameters = (Hashtable)userObj;
        A_CmsRequestContext reqContext = cms.getRequestContext();
        A_CmsUser currentUser = reqContext.currentUser();
        return currentUser.getName();
    }    

    
    public Integer getGroups(A_CmsObject cms, Vector names, Vector values) 
            throws CmsException {
        A_CmsRequestContext reqCont = cms.getRequestContext();
        A_CmsGroup currentGroup = reqCont.currentGroup();
        A_CmsUser currentUser = reqCont.currentUser();
        Vector allGroups = cms.getGroupsOfUser(currentUser.getName());
        
        int numGroups = allGroups.size();
        int currentGroupNum = 0;
        for(int i=0; i<numGroups; i++) {
            A_CmsGroup loopGroup = (A_CmsGroup)allGroups.elementAt(i);
            String loopGroupName = loopGroup.getName();
            values.addElement(loopGroupName);
            names.addElement(loopGroupName);
            if(loopGroup.equals(currentGroup)) {
                currentGroupNum = i;
            }
        }
        return new Integer(currentGroupNum);
    }



    public Integer getProjects(A_CmsObject cms, Vector names, Vector values) 
            throws CmsException {
        A_CmsRequestContext reqCont = cms.getRequestContext();
        A_CmsProject currentProject = reqCont.currentProject();
        Vector allProjects = cms.getAllAccessibleProjects();
        
        int numProjects = allProjects.size();
        int currentProjectNum = 0;
        for(int i=0; i<numProjects; i++) {
            A_CmsProject loopProject = (A_CmsProject)allProjects.elementAt(i);
            String loopProjectName = loopProject.getName();
            values.addElement(loopProjectName);
            names.addElement(loopProjectName);
            if(loopProject.equals(currentProject)) {
                currentProjectNum = i;
            }
        }
        return new Integer(currentProjectNum);
    }
    


}
