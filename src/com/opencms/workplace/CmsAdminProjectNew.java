package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import java.util.*;

import javax.servlet.http.*;

/**
 * Template class for displaying OpenCms workplace admin project screens.
 * <P>
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 2000/02/03 11:04:13 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsAdminProjectNew extends CmsWorkplaceDefault implements I_CmsConstants {

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
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "getting content of element " + ((elementName==null)?"<root>":elementName));
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "template file is: " + templateFile);
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "selected template section is: " + ((templateSelector==null)?"<default>":templateSelector));
        }
		
		// read the parameters
        A_CmsRequestContext reqCont = cms.getRequestContext();
        String newName = (String)parameters.get(C_PROJECTNEW_NAME);
        String newGroup = (String)parameters.get(C_PROJECTNEW_GROUP);
        String newDescription = (String)parameters.get(C_PROJECTNEW_DESCRIPTION);
        String newManagerGroup = (String)parameters.get(C_PROJECTNEW_MANAGERGROUP);
        String newFolder = (String)parameters.get(C_PROJECTNEW_FOLDER);
		
		CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);

		// is there any data? 
		if( (newName != null) && (newGroup != null) &&  (newDescription != null) && 
			(newManagerGroup != null) && (newFolder != null) ) {
			// Yes: create new Project
			try {
				cms.createProject(newName, newDescription, newGroup);
				// change the current project
				reqCont.setCurrentProject(newName);
				// copy the resource the the project
				cms.copyResourceToProject(newFolder);
				templateSelector = C_PROJECTNEW_DONE;
			} catch(CmsException exc) {
				templateSelector = C_PROJECTNEW_ERROR;
			}
		}
		// Now load the template file and start the processing
		return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }
	
    /**
     * Gets all groups, that may work for a project.
     * <P>
     * The given vectors <code>names</code> and <code>values</code> will 
     * be filled with the appropriate information to be used for building
     * a select box.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @return Index representing the current value in the vectors.
     * @exception CmsException
     */
    public Integer getGroups(A_CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values) 
		throws CmsException {
		// get all groups
		Vector groups = cms.getGroups();

		// fill the names and values
		for(int z = 0; z < groups.size(); z++) {
			names.addElement(((A_CmsGroup)groups.elementAt(z)).getName());
			values.addElement(((A_CmsGroup)groups.elementAt(z)).getName());
		}
		
		// no current group, set index to -1
        return new Integer(-1);
    }

    /**
     * Gets all groups, that may manage a project.
     * <P>
     * The given vectors <code>names</code> and <code>values</code> will 
     * be filled with the appropriate information to be used for building
     * a select box.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @return Index representing the current value in the vectors.
     * @exception CmsException
     */
    public Integer getManagerGroups(A_CmsObject cms, CmsXmlLanguageFile lang, 
									Vector names, Vector values) 
		throws CmsException {
		// get all groups
		Vector groups = cms.getGroups();

		// fill the names and values
		for(int z = 0; z < groups.size(); z++) {
			names.addElement(((A_CmsGroup)groups.elementAt(z)).getName());
			values.addElement(((A_CmsGroup)groups.elementAt(z)).getName());
		}
		
		// no current group, set index to -1
        return new Integer(-1);
    }

    /**
     * Gets all folders, from the cms.
     * <P>
     * The given vectors <code>names</code> and <code>values</code> will 
     * be filled with the appropriate information to be used for building
     * a select box.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @return Index representing the current value in the vectors.
     * @exception CmsException
     */
    public Integer getAllFolders(A_CmsObject cms, CmsXmlLanguageFile lang, 
									Vector names, Vector values) 
		throws CmsException {

		// add the root
		names.addElement(C_ROOT);
		values.addElement(C_ROOT);
		// get all folders		
		getFolders(cms, names, values, C_ROOT);
		// no current folder, set index to -1
        return new Integer(-1);
    }
	
	private void getFolders(A_CmsObject cms, Vector names, Vector values, 
							String currentFolder) 
		throws CmsException {
		
		Vector folders = cms.getSubFolders(currentFolder);

		String folder;

		// fill the names and values
		for(int z = 0; z < folders.size(); z++) {
			folder = ((A_CmsResource)folders.elementAt(z)).getAbsolutePath();
			names.addElement(folder);
			values.addElement(folder);
			getFolders(cms, names, values, folder);
		}
	}

    /**
     * Gets the selected folders.
     * <P>
     * The given vectors <code>names</code> and <code>values</code> will 
     * be filled with the appropriate information to be used for building
     * a select box.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @return Index representing the current value in the vectors.
     * @exception CmsException
     */
    public Integer getSelectedFolders(A_CmsObject cms, CmsXmlLanguageFile lang, 
									Vector names, Vector values) 
		throws CmsException {
		// no current folder, set index to -1
        return new Integer(-1);
    }
}
