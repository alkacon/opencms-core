/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/defaults/Attic/CmsXmlNav.java,v $
* Date   : $Date: 2001/08/10 14:46:46 $
* Version: $Revision: 1.33 $
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

package com.opencms.defaults;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import javax.servlet.*;
import javax.servlet.http.*;

import java.util.*;

/**
 * This class builds the default Navigation.
 *
 * @author Alexander Kandzior
 * @author Waruschan Babachan
 * @version $Revision: 1.33 $ $Date: 2001/08/10 14:46:46 $
 */
public class CmsXmlNav extends A_CmsNavBase {

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
    public boolean isCacheable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return true;
    }



    /**
     * Builds the navigation.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param resources a vector that contains the elements of navigation.
     * @param userObj Hashtable with parameters.
     * @return String that contains the navigation.
     */
    protected String buildNav(CmsObject cms, A_CmsXmlContent doc,Object userObject, Vector resources)
        throws CmsException {

        // get uri, currentfolder,servletpath and template file
        String requestedUri = cms.getRequestContext().getUri();
        String currentFolder=cms.getRequestContext().currentFolder().getAbsolutePath();
        String servletPath = cms.getRequestContext().getRequest().getServletUrl();
        CmsXmlTemplateFile template=(CmsXmlTemplateFile)doc;
        StringBuffer result = new StringBuffer();

        int size = resources.size();

        String navLink[] = new String[size];
        String navText[] = new String[size];
        float navPos[] = new float[size];
        // extract the navigation according to navigation position and nav text
        int max=extractNav(cms,resources,navLink,navText,navPos);
        // The arrays folderNames and folderTitles now contain all folders
        // that should appear in the nav.
        // Loop through all folders and generate output
        for(int i=0; i<max; i++) {
            template.setData("navtext", navText[i]);
            template.setData("navcount", new Integer(i+1).toString());
            template.setData("navlevel", new Integer(extractLevel(cms,navLink[i])).toString());
            // check whether it is a folder or file
            if (navLink[i].endsWith("/")) {
                // read the property of folder to find the link file.
                String navIndex=cms.readProperty(navLink[i],C_PROPERTY_NAVINDEX);
                // if there is not defined a property then take C_NAVINDEX constant
                if (navIndex==null) {
                    navIndex=C_NAVINDEX;
                }
                try {
                    cms.readFile(navLink[i] + navIndex);
                    template.setData("navlink", servletPath + navLink[i] + navIndex);
                } catch (CmsException e) {
                    template.setData("navlink", servletPath + requestedUri);
                }
            } else {
                try {
                    cms.readFile(navLink[i]);
                    template.setData("navlink", servletPath + navLink[i]);
                } catch (CmsException e) {
                    template.setData("navlink", servletPath + requestedUri);
                }
            }
            // Check if nav is current nav
            if (navLink[i].equals(currentFolder) || navLink[i].equals(requestedUri)) {
                result.append(template.getProcessedDataValue("navcurrent", this, userObject));
            } else {
                result.append(template.getProcessedDataValue("naventry", this, userObject));
            }
        }
        return result.toString();
    }
    /**
     * Builds the navigation that could be closed or opened.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param doc Reference to the CmsXmTemplateFile object of the initiating XLM document.
     * @param resources a vector that contains the elements of navigation.
     * @param userObj Hashtable with parameters.
     * @param requestedUri The absolute path of current requested file.
     * @param currentFolder The currenet folder.
     * @param servletPath The absolute path of servlet
     * @param depth An Integer that shows how many folders must be displayed.
     * @return String that contains the navigation.
     */
    protected String buildNavFold(CmsObject cms, CmsXmlTemplateFile template, Object userObject, Vector resources, String requestedUri, String currentFolder, String servletPath,int level,int[] count)
        throws CmsException {

        StringBuffer result = new StringBuffer();
        // arrays of navigation position, text and link
        int size = resources.size();
        String navLink[] = new String[size];
        String navText[] = new String[size];
        float navPos[] = new float[size];
        // extract the navigation according
        int max=extractNav(cms,resources,navLink,navText,navPos);
        if (max>0) {
            result.append(template.getProcessedDataValue("navstart", this, userObject));
            for(int i=0; i<max; i++) {
                count[0]++;
                template.setData("navtext", navText[i]);
                template.setData("navcount", new Integer(count[0]).toString());
                // this part is to set the level starting from specified level given as tagcontent
                // there it must be make a difference between extracted level and the given level
                int extractedLevel=extractLevel(cms,navLink[i]);
                int rightLevel=extractedLevel;
                if (level!=0) {
                    rightLevel=(extractedLevel-level);
                    if (rightLevel>=0) {
                        rightLevel++;
                    }
                }
                template.setData("navlevel", new Integer(rightLevel).toString());
                // check whether the link is folder
                if (navLink[i].endsWith("/")) {
                    // read the property of link file
                    String navIndex=cms.readProperty(navLink[i],C_PROPERTY_NAVINDEX);
                    // if the property is not defined then take C_NAVINDEX constant
                    if (navIndex==null) {
                        navIndex=C_NAVINDEX;
                    }
                    // read the file, if the file does'nt exist then write the uri as a link
                    try {
                        cms.readFile(navLink[i] + navIndex);
                        template.setData("navlink", servletPath + navLink[i] + navIndex);
                    } catch (CmsException e) {
                        template.setData("navlink", servletPath + requestedUri);
                    }
                } else {
                    // read the file, if the file does'nt exist then write the uri as a link
                    try {
                        cms.readFile(navLink[i]);
                        template.setData("navlink", servletPath + navLink[i]);
                    } catch (CmsException e) {
                        template.setData("navlink", servletPath + requestedUri);
                    }
                }
                // Check if nav is current nav
                if (navLink[i].equals(currentFolder) || navLink[i].equals(requestedUri)) {
                    result.append(template.getProcessedDataValue("navcurrent", this, userObject));
                } else {
                    result.append(template.getProcessedDataValue("naventry", this, userObject));
                }
                // if the folder was clicked
                if (requestedUri.indexOf(navLink[i])!=-1) {
                    Vector all=cms.getSubFolders(navLink[i]);
                    Vector files=cms.getFilesInFolder(navLink[i]);
                    all.ensureCapacity(all.size() + files.size());
                    Enumeration e = files.elements();
                    while (e.hasMoreElements()) {
                        all.addElement(e.nextElement());
                    }
                    result.append(buildNavFold(cms,template,userObject,all,requestedUri,currentFolder,servletPath,level,count));
                }
            }
            result.append(template.getProcessedDataValue("navend", this, userObject));
        }

        return result.toString();

    }

    /**
     * Builds the tree of navigation.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param doc Reference to the CmsXmTemplateFile object of the initiating XML document.
     * @param resources a vector that contains the elements of navigation.
     * @param userObj Hashtable with parameters.
     * @param requestedUri The absolute path of current requested file.
     * @param currentFolder The currenet folder.
     * @param servletPath The absolute path of servlet
     * @param level The starting level.
     * @param depth An Integer that shows how many folders must be displayed.
     * @param depthIsNull a boolean that determines whether the depth is given in tagcontent
     * @return String that contains the navigation.
     */
    protected String buildNavTree(CmsObject cms, CmsXmlTemplateFile template, Object userObject, Vector resources, String requestedUri, String currentFolder, String servletPath,int level,int depth,boolean depthIsNull,int[] count)
        throws CmsException {

        StringBuffer result = new StringBuffer();
        // define some array for link,text and position of
        // the elements of navigation
        int size = resources.size();
        String navLink[] = new String[size];
        String navText[] = new String[size];
        float navPos[] = new float[size];
        // extract the navigation from the given resources, i.e. it will be showed
        // if the elements of that resources must be showed in navigation, i.e.
        // wheather navigation Text und navigation position are defined for an element
        int max=extractNav(cms,resources,navLink,navText,navPos);
        // check wheather there is some elements
        if (max>0) {
            result.append(template.getProcessedDataValue("navstart", this, userObject));
            for(int i=0; i<max; i++) {
                // set the templates
                count[0]++;
                template.setData("navtext", navText[i]);
                template.setData("navcount", new Integer(count[0]).toString());
                // this part is to set the level starting from specified level given as tagcontent
                // there it must be make a difference between extracted level and the given level
                int extractedLevel=extractLevel(cms,navLink[i]);
                int rightLevel=extractedLevel;
                if (level!=0) {
                    rightLevel=(extractedLevel-level);
                    if (rightLevel>=0) {
                        rightLevel++;
                    }
                }
                template.setData("navlevel", new Integer(rightLevel).toString());
                String link="";
                // Check whether the link is a folder
                if (navLink[i].endsWith("/")) {
                    // read the property of folder to find the link file.
                    String navIndex=cms.readProperty(navLink[i],C_PROPERTY_NAVINDEX);
                    // if there is not defined a property then take C_NAVINDEX constant
                    if (navIndex==null || (navIndex!=null && navIndex.equals(""))) {
                        navIndex=C_NAVINDEX;
                    }
                    // check whether the file exist, if not then the link is current uri
                    try {
                        cms.readFile(navLink[i] + navIndex);
                        link=navLink[i] + navIndex;
                        template.setData("navlink", servletPath + navLink[i] + navIndex);
                    } catch (CmsException e) {
                        link=requestedUri;
                        template.setData("navlink", servletPath + requestedUri );
                    }
                } else {
                    // check whether the file exist, if not then the link is current uri
                    try {
                        cms.readFile(navLink[i]);
                        link=navLink[i];
                        template.setData("navlink", servletPath + navLink[i] );
                    } catch (CmsException e) {
                        link=requestedUri;
                        template.setData("navlink", servletPath + requestedUri );
                    }
                }
                // Check if nav is current nav
                if (link.equals(requestedUri)) {
                    result.append(template.getProcessedDataValue("navcurrent", this, userObject));
                } else {
                    result.append(template.getProcessedDataValue("naventry", this, userObject));
                }
                // redurce the depth and test if it is now zero or is the depth
                // given as tagcontent or not.
                // if the depth is not given in tagcontent then the depth variable must be ignored
                // the user don't want to give depth otherwise the depth must be considered
                // because it must not be exceeded the limit that user has defined.
                depth--;
                if (depthIsNull || (depthIsNull==false && depth>=0)) {
                    if (navLink[i].endsWith("/")) {
                        Vector all=cms.getSubFolders(navLink[i]);
                        Vector files=cms.getFilesInFolder(navLink[i]);
                        all.ensureCapacity(all.size() + files.size());
                        Enumeration e = files.elements();
                        while (e.hasMoreElements()) {
                            all.addElement(e.nextElement());
                        }
                        result.append(buildNavTree(cms,template,userObject,all,requestedUri,currentFolder,servletPath,level,depth,depthIsNull,count));
                    }
                }
            }
            result.append(template.getProcessedDataValue("navend", this, userObject));
        }
        return result.toString();
    }
    /**
     * Builds the link to folder determined by level.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param level The level of folder.
     * @return String that contains the path of folder determind by level.
     */
    protected String extractFolder(CmsObject cms, int level)
        throws CmsException {

        String currentFolder="/";
        StringTokenizer st = new StringTokenizer(cms.getRequestContext().currentFolder().getAbsolutePath(),"/");
        int count=st.countTokens();
        // if the level is negative then take the folder starting from
        // current folder otherwise take the folder starting from root
        if (level<0) {
            level=(-1)*level;
            level=count-level;
        }
        while (st.hasMoreTokens()) {
            if (level>1) {
                currentFolder=currentFolder+st.nextToken()+"/";
                level--;
            } else {
                break;
            }
        }
        return currentFolder;
    }
    /**
     * extract the level of navigation.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @return int that contains the level.
     */
    protected int extractLevel(CmsObject cms, String folder)
        throws CmsException {
        StringTokenizer st = new StringTokenizer(folder,"/");
        return (st.countTokens());
    }
    /**
     * Extracts the navbar.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param resources a vector that contains the elements of navigation.
     * @param navLink an array of navigation's link.
     * @param navText an array of navigation's Text.
     * @param navPos an array of position of navbar.
     * @return The maximum number of navbars in navigation.
     */
    protected int extractNav(CmsObject cms, Vector resources, String[] navLink, String[] navText, float[] navPos)
        throws CmsException {

        String requestedUri = cms.getRequestContext().getUri();

        int size = resources.size();
        int max = 0;

        // First scan all subfolders of the root folder
        // for any navigation metainformations and store
        // the maximum position found
        for(int i=0; i<size; i++) {
            CmsResource currentResource = (CmsResource)resources.elementAt(i);
            String path = currentResource.getAbsolutePath();
            String pos = cms.readProperty(path, C_PROPERTY_NAVPOS);
            String text = cms.readProperty(path, C_PROPERTY_NAVTEXT);
            // Only list folders in the nav bar if they are not deleted!
            if (currentResource.getState() != C_STATE_DELETED) {
                // don't list the temporary folders in the nav bar!
                if (pos != null && text != null && (!"".equals(pos)) && (!"".equals(text))
                     && ((!currentResource.getName().startsWith(C_TEMP_PREFIX)) || path.equals(requestedUri))) {
                    navLink[max] = path;
                    navText[max] = text;
                    navPos[max] = new Float(pos).floatValue();
                    max++;
                }
            }
        }

        // Sort the navigation
        sortNav(max, navLink, navText, navPos);

        return max;
    }
    /**
     * gets the current folder.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getFolderCurrent(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
            throws CmsException {

        String currentFolder=cms.getRequestContext().currentFolder().getAbsolutePath();
        currentFolder=cms.getRequestContext().getRequest().getServletUrl() + currentFolder;
        return currentFolder.getBytes();
    }
    /**
     * gets the parent folder.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getFolderParent(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
            throws CmsException {

        int level=0;
        // tagcontent determines the folder starting from parent folder.
        // if tagcontent is null, zero or negative, then the navigation of current
        // folder must be showed.
        if (!tagcontent.equals("")) {
            try {
                level=Integer.parseInt(tagcontent);
            } catch(NumberFormatException e) {
                throw new CmsException(e.getMessage());
            }
        }
        String currentFolder="";
        if (level<=0) {
            currentFolder=cms.getRequestContext().currentFolder().getAbsolutePath();
        } else {
            // level is converted to negative number, so I can use the method
            // "extractFolder" for positive and negative numbers. Negative number
            // determines the parent folder level starting from current folder and
            // positive number determines the level starting ftom root folder.
            currentFolder=extractFolder(cms,((-1)*level));
        }
        String parentFolder=cms.getRequestContext().getRequest().getServletUrl() + currentFolder;
        return parentFolder.getBytes();
    }
    /**
     * gets the root folder.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getFolderRoot(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
            throws CmsException {

        int level=0;
        // tagcontent determines the folder starting from root folder.
        // if tagcontent is null, then the navigation of root folder must be showed.
        if (!tagcontent.equals("")) {
            try {
                level=Integer.parseInt(tagcontent);
            } catch(NumberFormatException e) {
                throw new CmsException(e.getMessage());
            }
        }
        String currentFolder="";
        if (level<=0) {
            currentFolder=cms.rootFolder().getAbsolutePath();
        } else {
            currentFolder=extractFolder(cms,level);
        }
        String rootFolder=cms.getRequestContext().getRequest().getServletUrl() + currentFolder;
        return rootFolder.getBytes();
    }
    /**
     * gets the navigation of current folder.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getNavCurrent(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
            throws CmsException {
        // template file
        CmsXmlTemplateFile template=(CmsXmlTemplateFile)doc;
        // check whether there exist entry datablock
        if (!template.hasData("naventry")) {
            return "".getBytes();
        }
        // current folder
        String currentFolder=cms.getRequestContext().currentFolder().getAbsolutePath();
        // get all resources in current folder
        Vector resources=cms.getSubFolders(currentFolder);
        Vector allFile=cms.getFilesInFolder(currentFolder);
        resources.ensureCapacity(resources.size() + allFile.size());
        Enumeration e = allFile.elements();
        while (e.hasMoreElements()) {
            resources.addElement(e.nextElement());
        }
        // if there is not exist current datablock then take the entry datablock
        if (!template.hasData("navcurrent")) {
            template.setData("navcurrent", template.getData("naventry"));
        }
        return buildNav(cms,doc,userObject,resources).getBytes();
    }
    /**
     * gets the navigation of files and folders,
     * by folders it is showed closed, if the folder is clicked then it is opened.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getNavFold(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
            throws CmsException {
        // template file
        CmsXmlTemplateFile template=(CmsXmlTemplateFile)doc;
        // check whether there exist entry datablock
        if (!template.hasData("naventry")) {
            return "".getBytes();
        }
        int level=0;
        int[] count={0};
        // if level is zero or null or negative then all folders recursive must
        // be showed starting from root folder unless all folders stating from
        // specified level of parent folder.
        if (!tagcontent.equals("")) {
            try {
                level=Integer.parseInt(tagcontent);
            } catch(NumberFormatException e) {
                throw new CmsException(e.getMessage());
            }
        }
        // extract the folder
        String folder="";
        if (level<=0) {
            folder=cms.rootFolder().getAbsolutePath();
        } else {
            folder=extractFolder(cms,level);
        }
        // get uri, current folder, servletpath
        String requestedUri = cms.getRequestContext().getUri();
        String currentFolder=cms.getRequestContext().currentFolder().getAbsolutePath();
        String servletPath = cms.getRequestContext().getRequest().getServletUrl();
        // get all resources
        Vector resources=cms.getSubFolders(folder);
        Vector allFile=cms.getFilesInFolder(folder);
        resources.ensureCapacity(resources.size() + allFile.size());
        Enumeration e = allFile.elements();
        while (e.hasMoreElements()) {
            resources.addElement(e.nextElement());
        }

        String result="";
        // check wheather xml data blocks are defined.
        if (!template.hasData("navcurrent")) {
            template.setData("navcurrent", template.getData("naventry"));
        }
        if (!template.hasData("navstart")) {
            template.setData("navstart", "");
        }
        if (!template.hasData("navend")) {
            template.setData("navend", "");
        }
        result=buildNavFold(cms,template,userObject,resources,requestedUri,currentFolder,servletPath,level,count);

        return result.getBytes();
    }
    /**
     * gets the navigation of specified level of parent folder.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getNavParent(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
            throws CmsException {

        // template file
        CmsXmlTemplateFile template=(CmsXmlTemplateFile)doc;
        // check whether there exist entry datablock
        if (!template.hasData("naventry")) {
            return "".getBytes();
        }
        int level=0;
        // tagcontent determines the folder starting from parent folder.
        // if tagcontent is null, zero or negative, then the navigation of current
        // folder must be showed.
        if (!tagcontent.equals("")) {
            try {
                level=Integer.parseInt(tagcontent);
            } catch(NumberFormatException e) {
                throw new CmsException(e.getMessage());
            }
        }
        String currentFolder="";
        if (level<=0) {
            currentFolder=cms.getRequestContext().currentFolder().getAbsolutePath();
        } else {
            // level is converted to negative number, so I can use the method
            // "extractFolder" for positive and negative numbers. Negative number
            // determines the parent folder level starting from current folder and
            // positive number determines the level starting ftom root folder.
            currentFolder=extractFolder(cms,((-1)*level));
        }
        Vector resources=cms.getSubFolders(currentFolder);
        Vector allFile=cms.getFilesInFolder(currentFolder);

        resources.ensureCapacity(resources.size() + allFile.size());
        Enumeration e = allFile.elements();
        while (e.hasMoreElements()) {
            resources.addElement(e.nextElement());
        }
        // if there is not exist current datablock then take the entry datablock
        if (!template.hasData("navcurrent")) {
            template.setData("navcurrent", template.getData("naventry"));
        }
        return buildNav(cms,doc,userObject,resources).getBytes();
    }

    /**
     * gets the navigation of root folder or parent folder starting from root folder.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getNavRoot(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
            throws CmsException {

        // template file
        CmsXmlTemplateFile template=(CmsXmlTemplateFile)doc;
        // check whether there exist entry datablock
        if (!template.hasData("naventry")) {
            return "".getBytes();
        }
        int level=0;
        // tagcontent determines the folder starting from root folder.
        // if tagcontent is null, then the navigation of root folder must be showed.
        if (!tagcontent.equals("")) {
            try {
                level=Integer.parseInt(tagcontent);
            } catch(NumberFormatException e) {
                throw new CmsException(e.getMessage());
            }
        }
        String currentFolder="";
        if (level<=0) {
            currentFolder=cms.rootFolder().getAbsolutePath();
        } else {
            currentFolder=extractFolder(cms,level);
        }
        // get all resources, it means all files and folders.
        Vector resources=cms.getSubFolders(currentFolder);
        Vector allFile=cms.getFilesInFolder(currentFolder);
        resources.ensureCapacity(resources.size() + allFile.size());
        Enumeration e = allFile.elements();
        while (e.hasMoreElements()) {
            resources.addElement(e.nextElement());
        }
        // if there is not exist current datablock then take the entry datablock
        if (!template.hasData("navcurrent")) {
            template.setData("navcurrent", template.getData("naventry"));
        }
        return buildNav(cms,doc,userObject,resources).getBytes();
    }
    /**
     * gets the navigation of folders recursive.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getNavTree(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
            throws CmsException {

        // template file
        CmsXmlTemplateFile template=(CmsXmlTemplateFile)doc;
        // check whether there exist entry datablock
        if (!template.hasData("naventry")) {
            return "".getBytes();
        }
        int level=0;
        int depth=0;
        int[] count={0};
        // if there is not any depth then it must not be tested in a if condition
        boolean depthIsNull=true;
        // if level is zero or null or negative then all folders recursive must
        // be showed starting from root folder unless all folders stating from
        // specified level of parent folder.
        if (!tagcontent.equals("")) {
            try {
                // comma shows that there is two parameters: level,depth
                // otherwise there is one parameter: level
                if (tagcontent.indexOf(",")!=-1) {
                    level=Integer.parseInt(tagcontent.substring(0,tagcontent.indexOf(",")));
                    depth=Integer.parseInt(tagcontent.substring(tagcontent.indexOf(",")+1));
                } else {
                    level=Integer.parseInt(tagcontent);
                }
            } catch(NumberFormatException e) {
                throw new CmsException(e.getMessage());
            }
        }
        // if level is not entered or it is less than zero then folder is the root folder
        // otherwise the folder must be extracted accordeing to the entered level.
        String folder="";
        if (level<=0) {
            folder=cms.rootFolder().getAbsolutePath();
        } else {
            folder=extractFolder(cms,level);
        }
        if (depth>0) {
            depthIsNull=false;
        }
        // get all folders in specified folder
        Vector resources=cms.getSubFolders(folder);
        // get all files in specified folder
        Vector allFile=cms.getFilesInFolder(folder);
        // get a vector of all files und folders
        resources.ensureCapacity(resources.size() + allFile.size());
        Enumeration e = allFile.elements();
        while (e.hasMoreElements()) {
            resources.addElement(e.nextElement());
        }
        // get the uri,servletpath and current folder
        String requestedUri = cms.getRequestContext().getUri();
        String currentFolder=cms.getRequestContext().currentFolder().getAbsolutePath();
        String servletPath = cms.getRequestContext().getRequest().getServletUrl();

        String result="";
        // check whether xml data blocks are defined.
        // The main datablock is entry, it must be defined, the others will get
        // the same datablock if they don't exist.
        if (!template.hasData("navcurrent")) {
            template.setData("navcurrent", template.getData("naventry"));
        }
        if (!template.hasData("navstart")) {
            template.setData("navstart", "");
        }
        if (!template.hasData("navend")) {
            template.setData("navend", "");
        }
        result=buildNavTree(cms,template,userObject,resources,requestedUri,currentFolder,servletPath,level,depth,depthIsNull,count);

        return result.getBytes();
    }

    /**
     * gets a specified property of current folder.
     *
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getPropertyCurrent(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
        throws CmsException {
        String property="";
        // tagcontent must contain the property definition name.
        if (!tagcontent.equals("")) {
            String currentFolder=cms.getRequestContext().currentFolder().getAbsolutePath();
            property=cms.readProperty(currentFolder, tagcontent);
            property=(property!=null?property:"");
        }
        return (property.getBytes());
    }


    /**
     * gets a specified property of specified folder starting from current folder.
     *
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getPropertyParent(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
        throws CmsException {
        int level=0;
        String property="";
        // tagcontent determines the parent folder starting from current folder and
        // the property definition name sparated by a comma.
        if (!tagcontent.equals("")) {
            try {
                level=Integer.parseInt(tagcontent.substring(0,tagcontent.indexOf(",")));
            } catch(NumberFormatException e) {
                throw new CmsException(e.getMessage());
            }
            String currentFolder="";
            if (level<=0) {
                currentFolder=cms.getRequestContext().currentFolder().getAbsolutePath();
            } else {
                // level is converted to negative number, so I can use the method
                // "extractFolder" for positive and negative numbers. Negative number
                // determines the parent folder level starting from current folder and
                // positive number determines the level starting ftom root folder.
                currentFolder=extractFolder(cms,((-1)*level));
            }
            property=cms.readProperty(currentFolder, tagcontent.substring(tagcontent.indexOf(",")+1));
            property=(property!=null?property:"");
        }
        return (property.getBytes());
    }

    /**
     * gets a specified property of specified folder starting from root.
     *
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getPropertyRoot(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
        throws CmsException {
        int level=0;
        String property="";
        // tagcontent determines the folder starting from root folder and
        // the property definition name sparated by a comma.
        if (!tagcontent.equals("")) {
            try {
                level=Integer.parseInt(tagcontent.substring(0,tagcontent.indexOf(",")));
            } catch(NumberFormatException e) {
                throw new CmsException(e.getMessage());
            }
            String currentFolder="";
            if (level<=0) {
                currentFolder=currentFolder=cms.rootFolder().getAbsolutePath();
            } else {
                currentFolder=extractFolder(cms,level);
            }
            property=cms.readProperty(currentFolder, tagcontent.substring(tagcontent.indexOf(",")+1));
            property=(property!=null?property:"");
        }
        return (property.getBytes());
    }


    /**
     * gets a specified property of uri.
     *
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getPropertyUri(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
        throws CmsException {
        String property="";
        String requestedUri = cms.getRequestContext().getUri();
        property=cms.readProperty(requestedUri, tagcontent);
        property=(property!=null?property:"");
        return (property.getBytes());
    }

    /**
     * gets the caching information from the current template class.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if this class may stream it's results, <EM>false</EM> otherwise.
     */
    public CmsCacheDirectives getCacheDirectives(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {

        // First build our own cache directives.
        CmsCacheDirectives result = new CmsCacheDirectives(true);
        result.setCacheUri(true);
        return result;
    }
}
