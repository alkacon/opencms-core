package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import java.util.*;

import javax.servlet.http.*;

/**
 * Template class for displaying OpenCms workplace main screen.
 * <P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.4 $ $Date: 2000/02/07 08:55:50 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsWpMain extends CmsWorkplaceDefault {

    private Vector m_viewNames = null;
    private Vector m_viewLinks = null;
    
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
        String newView = (String)parameters.get(C_PARA_VIEW);
        
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
        
        // Check if the user requested a new view
        if(newView != null && !("".equals(newView))) {
            HttpSession session = ((HttpServletRequest)reqCont.getRequest().getOriginalRequest()).getSession(true);
            session.putValue(C_PARA_VIEW, newView);
        }
        
        // Now load the template file and start the processing
        CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }            
        
    /**
     * Gets the currently logged in user.
     * <P>
     * Used for displaying information in the 'foot' frame of the workplace.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Additional parameter passed to the method <em>(not used here)</em>.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document <em>(not used here)</em>.  
     * @param userObj Hashtable with parameters <em>(not used here)</em>.
     * @return String containing the current user.
     * @exception CmsException
     */
    public Object getUser(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) 
            throws CmsException {
        A_CmsRequestContext reqContext = cms.getRequestContext();
        A_CmsUser currentUser = reqContext.currentUser();
        return currentUser.getName();
    }    
    
    /**
     * Gets all groups of the currently logged in user.
     * <P>
     * The given vectors <code>names</code> and <code>values</code> will 
     * be filled with the appropriate information to be used for building
     * a select box.
     * <P>
     * Both <code>names</code> and <code>values</code> will contain
     * the group names after returning from this method.
     * <P>
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param lang reference to the currently valid language file
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index representing the user's current group in the vectors.
     * @exception CmsException
     */
    public Integer getGroups(A_CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) 
            throws CmsException {

        // Get a vector of all of the user's groups by asking the request context
        A_CmsRequestContext reqCont = cms.getRequestContext();
        A_CmsGroup currentGroup = reqCont.currentGroup();
        Vector allGroups = cms.getGroupsOfUser(reqCont.currentUser().getName());
        
        // Now loop through all groups and fill the result vectors
        int numGroups = allGroups.size();
        int currentGroupNum = 0;
        for(int i=0; i<numGroups; i++) {
            A_CmsGroup loopGroup = (A_CmsGroup)allGroups.elementAt(i);
            String loopGroupName = loopGroup.getName();
            values.addElement(loopGroupName);
            names.addElement(loopGroupName);
            if(loopGroup.equals(currentGroup)) {
                // Fine. The group of this loop is the user's current group. Save it!
                currentGroupNum = i;
            }
        }
        return new Integer(currentGroupNum);
    }

    /**
     * Gets all projects of the currently logged in user.
     * <P>
     * The given vectors <code>names</code> and <code>values</code> will 
     * be filled with the appropriate information to be used for building
     * a select box.
     * <P>
     * Both <code>names</code> and <code>values</code> will contain
     * the project names after returning from this method.
     * <P>
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param lang reference to the currently valid language file
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index representing the user's current project in the vectors.
     * @exception CmsException
     */
    public Integer getProjects(A_CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) 
            throws CmsException {
        // Get all project information
        A_CmsRequestContext reqCont = cms.getRequestContext();
        A_CmsProject currentProject = reqCont.currentProject();
        Vector allProjects = cms.getAllAccessibleProjects();
        
        // Now loop through all projects and fill the result vectors
        int numProjects = allProjects.size();
        int currentProjectNum = 0;
        for(int i=0; i<numProjects; i++) {
            A_CmsProject loopProject = (A_CmsProject)allProjects.elementAt(i);
            String loopProjectName = loopProject.getName();
            values.addElement(loopProjectName);
            names.addElement(loopProjectName);
            if(loopProject.equals(currentProject)) {
                // Fine. The project of this loop is the user's current project. Save it!
                currentProjectNum = i;
            }
        }
        return new Integer(currentProjectNum);
    }
    
    /**
     * Gets all views available in the workplace screen.
     * <P>
     * The given vectors <code>names</code> and <code>values</code> will 
     * be filled with the appropriate information to be used for building
     * a select box.
     * <P>
     * <code>names</code> will contain language specific view descriptions
     * and <code>values</code> will contain the correspondig URL for each
     * of these views after returning from this method.
     * <P>
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param lang reference to the currently valid language file
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index representing the user's current workplace view in the vectors.
     * @exception CmsException
     */
    public Integer getViews(A_CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) 
            throws CmsException {
        
        // Let's see if we have a session
        A_CmsRequestContext reqCont = cms.getRequestContext();
        HttpSession session = ((HttpServletRequest)reqCont.getRequest().getOriginalRequest()).getSession(false);

        // If there ist a session, let's see if it has a view stored
        String currentView = null;
        if(session != null) {
            currentView = (String)session.getValue(C_PARA_VIEW);
        }        
        
        // Check if the list of available views is not yet loaded from the workplace.ini
        if(m_viewNames == null || m_viewLinks == null) {
            m_viewNames = new Vector();
            m_viewLinks = new Vector();

            CmsXmlWpConfigFile configFile = new CmsXmlWpConfigFile(cms);            
            configFile.getViews(m_viewNames, m_viewLinks);            
        }
        
        // OK. Now m_viewNames and m_viewLinks contail all available
        // view information.
        // Loop through the vectors and fill the result vectors.
        int currentViewIndex = 0;
        int numViews = m_viewNames.size();        
        for(int i=0; i<numViews; i++) {
            String loopValue = (String)m_viewLinks.elementAt(i);
            String loopName = (String)m_viewNames.elementAt(i);
            values.addElement(loopValue);
            names.addElement(lang.getLanguageValue("select." + loopName));
            if(loopValue.equals(currentView)) {
                currentViewIndex = i;
            }
        }
        return new Integer(currentViewIndex);
    }
}
