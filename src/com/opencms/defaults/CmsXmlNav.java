/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/defaults/Attic/CmsXmlNav.java,v $
* Date   : $Date: 2003/08/14 15:37:26 $
* Version: $Revision: 1.55 $
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

import org.opencms.main.OpenCms;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.template.A_CmsXmlContent;
import com.opencms.template.CmsCacheDirectives;
import com.opencms.template.CmsXmlTemplateFile;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This class builds the default Navigation.
 *
 * @author Alexander Kandzior
 * @author Waruschan Babachan
 * @author Thomas Weckert
 * @version $Revision: 1.55 $ $Date: 2003/08/14 15:37:26 $
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
     * Builds the navigation.<p>
     *
     * @param cms CmsObject Object for accessing system resources
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document
     * @param resources a vector that contains the elements of navigation
     * @param userObject Hashtable with parameters
     * @return String that contains the navigation
     * @throws CmsException if something goes wrong
     */
    protected String buildNav(CmsObject cms, A_CmsXmlContent doc, Object userObject, List resources)
        throws CmsException {

        // get uri, currentfolder,servletpath and template file
        String requestedUri = cms.getRequestContext().getUri();
        // String currentFolder=cms.readPath(cms.getRequestContext().currentFolder());
        // String servletPath = cms.getRequestContext().getRequest().getServletUrl();
        CmsXmlTemplateFile template=(CmsXmlTemplateFile)doc;
        StringBuffer result = new StringBuffer();
        int size = resources.size();

        String navLink[] = new String[size];
        String navText[] = new String[size];
        float navPos[] = new float[size];
        // extract the navigation according to navigation position and nav text
        int max=extractNav(cms, resources, navLink, navText, navPos);
        // The arrays folderNames and folderTitles now contain all folders
        // that should appear in the nav.
        // Loop through all folders and generate output
        for (int i=0; i<max; i++) {
            template.setData("navtext", navText[i]);
            template.setData("navcount", new Integer(i+1).toString());
            template.setData("navlevel", new Integer(extractLevel(cms, navLink[i])).toString());
            // check whether it is a folder or file
            String link="";
            if (navLink[i].endsWith("/")) {
                // read the property of folder to find the link file.
                String navIndex=cms.readProperty(navLink[i], C_PROPERTY_NAVINDEX);
                // if there is not defined a property then take C_NAVINDEX constant
                if (navIndex==null) {
                    navIndex=C_NAVINDEX;
                }
                try {
                    cms.readFile(navLink[i] + navIndex);
                    link=navLink[i] + navIndex;
                    template.setData("navlink", OpenCms.getLinkManager().substituteLink(cms, navLink[i] + navIndex));
                } catch (CmsException e) {
                    template.setData("navlink", OpenCms.getLinkManager().substituteLink(cms, requestedUri));
                }
            } else {
                try {
                    cms.readFile(navLink[i]);
                    link=navLink[i];
                    template.setData("navlink", OpenCms.getLinkManager().substituteLink(cms, navLink[i]));
                } catch (CmsException e) {
                    template.setData("navlink", OpenCms.getLinkManager().substituteLink(cms, requestedUri));
                }
            }
            // Check if nav is current nav
            //if (navLink[i].equals(currentFolder) || navLink[i].equals(requestedUri)) {
            if (link.equals(requestedUri)) {
                result.append(template.getProcessedDataValue("navcurrent", this, userObject));
            } else {
                result.append(template.getProcessedDataValue("naventry", this, userObject));
            }
        }
        return result.toString();
    }
    
    /**
     * Builds the navigation that could be closed or opened.<p>
     *
     * @param cms CmsObject Object for accessing system resources
     * @param template Reference to the CmsXmTemplateFile object of the initiating XLM document
     * @param resources a vector that contains the elements of navigation
     * @param userObject Hashtable with parameters
     * @param requestedUri The absolute path of current requested file
     * @param currentFolder The currenet folder
     * @param servletPath The absolute path of servlet
     * @param level An Integer that shows how many folders must be displayed.
     * @param count the nav count
     * @return String that contains the navigation
     * @throws CmsException if soemthing goes wrong
     */
    protected String buildNavFold(CmsObject cms, CmsXmlTemplateFile template, Object userObject, List resources, String requestedUri, String currentFolder, String servletPath, int level, int[] count)
        throws CmsException {

        StringBuffer result = new StringBuffer();
        // arrays of navigation position, text and link
        int size = resources.size();
        String navLink[] = new String[size];
        String navText[] = new String[size];
        float navPos[] = new float[size];
        // extract the navigation according
        int max=extractNav(cms, resources, navLink, navText, navPos);
        if (max>0) {
            result.append(template.getProcessedDataValue("navstart", this, userObject));
            for (int i=0; i<max; i++) {
                count[0]++;
                template.setData("navtext", navText[i]);
                template.setData("navcount", new Integer(count[0]).toString());
                // this part is to set the level starting from specified level given as tagcontent
                // there it must be make a difference between extracted level and the given level
                int extractedLevel=extractLevel(cms, navLink[i]);
                template.setData("navlevel", new Integer(extractedLevel).toString());
                /*int rightLevel=extractedLevel;
                if (level!=0) {
                    rightLevel=(extractedLevel-level);
                    if (rightLevel>=0) {
                        rightLevel++;
                    }
                }
                template.setData("navlevel", new Integer(rightLevel).toString());*/
                String link="";
                // check whether the link is folder
                if (navLink[i].endsWith("/")) {
                    // read the property of link file
                    String navIndex=cms.readProperty(navLink[i], C_PROPERTY_NAVINDEX);
                    // if the property is not defined then take C_NAVINDEX constant
                    if (navIndex==null) {
                        navIndex=C_NAVINDEX;
                    }
                    // read the file, if the file does'nt exist then write the uri as a link
                    try {
                        cms.readFile(navLink[i] + navIndex);
                        link=navLink[i] + navIndex;
                        template.setData("navlink", OpenCms.getLinkManager().substituteLink(cms, navLink[i] + navIndex));
                    } catch (CmsException e) {
                        link=requestedUri;
                        template.setData("navlink", OpenCms.getLinkManager().substituteLink(cms, requestedUri));
                    }
                } else {
                    // read the file, if the file does'nt exist then write the uri as a link
                    try {
                        cms.readFile(navLink[i]);
                        link=navLink[i];
                        template.setData("navlink", OpenCms.getLinkManager().substituteLink(cms, navLink[i]));
                    } catch (CmsException e) {
                        link=requestedUri;
                        template.setData("navlink", OpenCms.getLinkManager().substituteLink(cms, requestedUri));
                    }
                }
                // Check if nav is current nav
                if (link.equals(requestedUri)) {
                    result.append(template.getProcessedDataValue("navcurrent", this, userObject));
                } else {
                    result.append(template.getProcessedDataValue("naventry", this, userObject));
                }
                // if the folder was clicked
                if (requestedUri.indexOf(navLink[i])!=-1) {
                    List all=cms.getSubFolders(navLink[i]);
                    List files=cms.getFilesInFolder(navLink[i]);
                    // register this folder for changes (if it is a folder!)
                    if (navLink[i].endsWith("/")) {
                        Vector vfsDeps = new Vector();
                        vfsDeps.add(cms.readFolder(navLink[i]));
                        registerVariantDeps(cms, template.getAbsoluteFilename(), null, null,
                                        (Hashtable)userObject, vfsDeps, null, null);
                    }
                    ((ArrayList)all).ensureCapacity(all.size() + files.size());
                    Iterator e = files.iterator();
                    while (e.hasNext()) {
                        all.add(e.next());
                    }
                    result.append(buildNavFold(cms, template, userObject, all, requestedUri, currentFolder, servletPath, level, count));
                }
            }
            result.append(template.getProcessedDataValue("navend", this, userObject));
        }

        return result.toString();

    }

    /**
     * Builds the tree of navigation.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param template Reference to the CmsXmTemplateFile object of the initiating XML document
     * @param resources a vector that contains the elements of navigation
     * @param userObject Hashtable with parameters
     * @param requestedUri The absolute path of current requested file
     * @param currentFolder The currenet folder
     * @param servletPath The absolute path of servlet
     * @param level The starting level
     * @param depth An Integer that shows how many folders must be displayed
     * @param depthIsNull a boolean that determines whether the depth is given in tagcontent
     * @param count the nav count
     * @return String that contains the navigation
     * @throws CmsException if something goes wrong
     */
    protected String buildNavTree(CmsObject cms, CmsXmlTemplateFile template, Object userObject, List resources, String requestedUri, String currentFolder, String servletPath, int level, int depth, boolean depthIsNull, int[] count)
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
        int max=extractNav(cms, resources, navLink, navText, navPos);
        // check wheather there is some elements
        if (max>0) {
            result.append(template.getProcessedDataValue("navstart", this, userObject));
            for (int i=0; i<max; i++) {
                // set the templates
                count[0]++;
                template.setData("navtext", navText[i]);
                template.setData("navcount", new Integer(count[0]).toString());
                // this part is to set the level starting from specified level given as tagcontent
                // there it must be make a difference between extracted level and the given level
                int extractedLevel=extractLevel(cms, navLink[i]);
                template.setData("navlevel", new Integer(extractedLevel).toString());
                /*int rightLevel=extractedLevel;
                if (level!=0) {
                    rightLevel=(extractedLevel-level);
                    if (rightLevel>=0) {
                        rightLevel++;
                    }
                }
                template.setData("navlevel", new Integer(rightLevel).toString());*/
                String link="";
                // Check whether the link is a folder
                if (navLink[i].endsWith("/")) {
                    // read the property of folder to find the link file.
                    String navIndex=cms.readProperty(navLink[i], C_PROPERTY_NAVINDEX);
                    // if there is not defined a property then take C_NAVINDEX constant
                    if (navIndex==null || (navIndex!=null && navIndex.equals(""))) {
                        navIndex=C_NAVINDEX;
                    }
                    // check whether the file exist, if not then the link is current uri
                    try {
                        cms.readFile(navLink[i] + navIndex);
                        link=navLink[i] + navIndex;
                        template.setData("navlink", OpenCms.getLinkManager().substituteLink(cms, navLink[i] + navIndex));
                    } catch (CmsException e) {
                        link=requestedUri;
                        template.setData("navlink", OpenCms.getLinkManager().substituteLink(cms, requestedUri));
                    }
                } else {
                    // check whether the file exist, if not then the link is current uri
                    try {
                        cms.readFile(navLink[i]);
                        link=navLink[i];
                        template.setData("navlink", OpenCms.getLinkManager().substituteLink(cms, navLink[i]));
                    } catch (CmsException e) {
                        link=requestedUri;
                        template.setData("navlink", OpenCms.getLinkManager().substituteLink(cms, requestedUri));
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
                if (depthIsNull || depth>=0) {
                    if (navLink[i].endsWith("/")) {
                        List all=cms.getSubFolders(navLink[i]);
                        List files=cms.getFilesInFolder(navLink[i]);
                        // register this folder for changes
                        Vector vfsDeps = new Vector();
                        vfsDeps.add(cms.readFolder(navLink[i]));
                        registerVariantDeps(cms, template.getAbsoluteFilename(), null, null,
                                        (Hashtable)userObject, vfsDeps, null, null);
                        ((ArrayList)all).ensureCapacity(all.size() + files.size());
                        Iterator e = files.iterator();
                        while (e.hasNext()) {
                            all.add(e.next());
                        }
                        result.append(buildNavTree(cms, template, userObject, all, requestedUri, currentFolder, servletPath, level, depth, depthIsNull, count));
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
     * @param cms CmsObject Object for accessing system resources
     * @param level The level of folder
     * @param exact this parameter determines wheater exact level of folder must be exctracted
     * @return String that contains the path of folder determind by level
     * @throws CmsException if something goes wrong
     */
    protected String extractFolder(CmsObject cms, int level, String exact)
        throws CmsException {

        // get uri and requested uri
        String navIndex=C_NAVINDEX;
        try {
            navIndex=cms.readProperty(cms.readAbsolutePath(cms.getRequestContext().currentFolder()), C_PROPERTY_NAVINDEX);
            navIndex=((navIndex==null || (navIndex!=null && navIndex.equals("")))?C_NAVINDEX:navIndex);
        } catch (Exception err) {
            navIndex=C_NAVINDEX;
        }
        String uri=cms.readAbsolutePath(cms.getRequestContext().currentFolder())+navIndex;
        String requestedUri=cms.getRequestContext().getUri();
        // get count of folder
        String currentFolder="/";
        StringTokenizer st = new StringTokenizer(cms.readAbsolutePath(cms.getRequestContext().currentFolder()), "/");
        int count=st.countTokens()+1;
        // if the level is negative then take the folder starting from
        // current folder otherwise take the folder starting from root
        if (level<0) {
            level=(-1)*level;
            level=count-level;
        }
        // increment count to get real level
        if (exact.equals("true") && (level<=0 || level>count || (level==count && requestedUri.equals(uri)))) {
            return "";
        }
        // ulrich.rueth@gmx.de: this seems to have been the reason for the navigation
        // not displaying correctly in top-level index.html files!
        //if (level==count && requestedUri.equals(uri)) {
        //    level--;
        //}
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
     * Extract the level of navigation.<p>
     *
     * @param cms Object for accessing system resources
     * @param folder the folder to extract the level
     * @return int that contains the level
     * @throws CmsException if something goes wrong
     */
    protected int extractLevel(CmsObject cms, String folder)
        throws CmsException {
        StringTokenizer st = new StringTokenizer(folder, "/");
        int count=st.countTokens();
        count=(count==0?1:count);
        return count;
    }
    /**
     * Extracts the navbar.<p>
     *
     * @param cms CmsObject Object for accessing system resources
     * @param resources a vector that contains the elements of navigation
     * @param navLink an array of navigation's link
     * @param navText an array of navigation's Text
     * @param navPos an array of position of navbar
     * @return The maximum number of navbars in navigation
     * @throws CmsException if something goes wrong
     */
    protected int extractNav(CmsObject cms, List resources, String[] navLink, String[] navText, float[] navPos)
        throws CmsException {

        String requestedUri = cms.getRequestContext().getUri();

        int size = resources.size();
        int max = 0;

        // First scan all subfolders of the root folder
        // for any navigation metainformations and store
        // the maximum position found
        for (int i=0; i<size; i++) {
            CmsResource currentResource = (CmsResource)resources.get(i);
            String path = cms.readAbsolutePath(currentResource);
            String pos = cms.readProperty(path, I_CmsConstants.C_PROPERTY_NAVPOS);
            String text = cms.readProperty(path, I_CmsConstants.C_PROPERTY_NAVTEXT);
            // Only list folders in the nav bar if they are not deleted!
            if (currentResource.getState() != I_CmsConstants.C_STATE_DELETED) {
                // don't list the temporary folders in the nav bar!
                if (pos != null && text != null && (!"".equals(pos)) && (!"".equals(text))
                     && ((!currentResource.getResourceName().startsWith(I_CmsConstants.C_TEMP_PREFIX)) || path.equals(requestedUri))) {
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
     * Gets the current folder.<p>
     *
     * @param cms CmsObject Object for accessing system resources
     * @param tagcontent Unused in this special case of a user method. Can be ignored
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document
     * @param userObject Hashtable with parameters
     * @return byte[] with the content of this subelement
     * @throws CmsException if something goes wrong
     */
    public Object getFolderCurrent(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
            throws CmsException {

        String currentFolder=cms.readAbsolutePath(cms.getRequestContext().currentFolder());
        currentFolder=cms.getRequestContext().getRequest().getServletUrl() + currentFolder;
        return currentFolder.getBytes();
    }
    /**
     * gets the parent folder.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param userObject Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @throws CmsException if something goes wrong
     */
    public Object getFolderParent(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
            throws CmsException {

        int level=1;
        // tagcontent determines the folder starting from parent folder.
        // if tagcontent is null, zero or negative, then the navigation of current
        // folder must be showed.
        String exact="false";
        // tagcontent determines the folder starting from root folder.
        // if tagcontent is null, then the navigation of root folder must be showed.
        if (!tagcontent.equals("")) {
            try {
                if (tagcontent.indexOf(",")!=-1) {
                    level=Integer.parseInt(tagcontent.substring(0, tagcontent.indexOf(",")));
                    exact=tagcontent.substring(tagcontent.indexOf(",")+1).toLowerCase();
                } else {
                    level=Integer.parseInt(tagcontent);
                }
            } catch (NumberFormatException e) {
                level=1;
                exact=tagcontent.toLowerCase();
                if (!exact.equals("true")) {
                    exact="false";
                }
            }
        }
        String currentFolder=extractFolder(cms, ((-1)*level), exact);
        if (currentFolder.equals(""))
            return "".getBytes();
        String parentFolder=cms.getRequestContext().getRequest().getServletUrl() + currentFolder;
        return parentFolder.getBytes();
    }
    /**
     * gets the root folder.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param tagcontent Unused in this special case of a user method. Can be ignored
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document
     * @param userObject Hashtable with parameters
     * @return byte[] with the content of this subelement
     * @throws CmsException if something goes wrong
     */
    public Object getFolderRoot(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
            throws CmsException {

        int level=1;
        String exact="false";
        // tagcontent determines the folder starting from root folder.
        // if tagcontent is null, then the navigation of root folder must be showed.
        if (!tagcontent.equals("")) {
            try {
                if (tagcontent.indexOf(",")!=-1) {
                    level=Integer.parseInt(tagcontent.substring(0, tagcontent.indexOf(",")));
                    exact=tagcontent.substring(tagcontent.indexOf(",")+1).toLowerCase();
                } else {
                    level=Integer.parseInt(tagcontent);
                }
            } catch (NumberFormatException e) {
                level=1;
                exact=tagcontent.toLowerCase();
                if (!exact.equals("true")) {
                    exact="false";
                }
            }
        }
        String currentFolder=extractFolder(cms, level, exact);
        if (currentFolder.equals(""))
            return "".getBytes();
        String rootFolder=cms.getRequestContext().getRequest().getServletUrl() + currentFolder;
        return rootFolder.getBytes();
    }
    /**
     * gets the navigation of current folder.<p>
     *
     * @param cms CmsObject Object for accessing system resources
     * @param tagcontent Unused in this special case of a user method. Can be ignored
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document
     * @param userObject Hashtable with parameters
     * @return byte[] with the content of this subelement
     * @throws CmsException if something goes wrong
     */
    public Object getNavCurrent(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
            throws CmsException {
        // template file
        CmsXmlTemplateFile template=(CmsXmlTemplateFile)doc;
        // check whether there exist entry datablock
        if (!template.hasData("naventry")) {
            return "".getBytes();
        }
        String currentFolder=cms.readAbsolutePath(cms.getRequestContext().currentFolder());
        int level=extractLevel(cms, currentFolder)+1;
        String exact="false";
        // tagcontent determines the folder starting from root folder.
        // if tagcontent is null, then the navigation of root folder must be showed.
        if (!tagcontent.equals("")) {
            try {
                if (tagcontent.indexOf(",")!=-1) {
                    level=Integer.parseInt(tagcontent.substring(0, tagcontent.indexOf(",")));
                    exact=tagcontent.substring(tagcontent.indexOf(",")+1).toLowerCase();
                } else {
                    level=Integer.parseInt(tagcontent);
                }
            } catch (NumberFormatException e) {
                level=extractLevel(cms, currentFolder)+1;
                exact=tagcontent.toLowerCase();
                if (!exact.equals("true")) {
                    exact="false";
                }
            }
        }
        // get the folder of level
        currentFolder="/";
        StringTokenizer st = new StringTokenizer(cms.readAbsolutePath(cms.getRequestContext().currentFolder()), "/");
        int count=st.countTokens()+1;
        if (exact.equals("true") && level!=count) {
            return "".getBytes();
        }
        while (st.hasMoreTokens()) {
            if (level>1) {
                currentFolder=currentFolder+st.nextToken()+"/";
                level--;
            } else {
                break;
            }
        }
        // register this folder for changes
        Vector vfsDeps = new Vector();
        vfsDeps.add(cms.readFolder(currentFolder));
        registerVariantDeps(cms, doc.getAbsoluteFilename(), null, null,
                        (Hashtable)userObject, vfsDeps, null, null);
        // get all resources in current folder
        List resources=cms.getSubFolders(currentFolder);
        List allFile=cms.getFilesInFolder(currentFolder);
        ((ArrayList)resources).ensureCapacity(resources.size() + allFile.size());
        Iterator e = allFile.iterator();
        while (e.hasNext()) {
            resources.add(e.next());
        }
        // if there is not exist current datablock then take the entry datablock
        if (!template.hasData("navcurrent")) {
            template.setData("navcurrent", template.getData("naventry"));
        }
        return buildNav(cms, doc, userObject, resources).getBytes();
    }
    /**
     * gets the navigation of files and folders,
     * by folders it is showed closed, if the folder is clicked then it is opened.<p>
     *
     * @param cms CmsObject Object for accessing system resources
     * @param tagcontent used in this special case of a user method. Can't be ignored
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document
     * @param userObject Hashtable with parameters
     * @return byte[] with the content of this subelement
     * @throws CmsException if something goes wrong
     */
    public Object getNavFold(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
            throws CmsException {
        // template file
        CmsXmlTemplateFile template=(CmsXmlTemplateFile)doc;
        // check whether there exist entry datablock
        if (!template.hasData("naventry")) {
            return "".getBytes();
        }
        int level=1;
        int[] count={0};
        String exact="false";
        // if level is zero or null or negative then all folders recursive must
        // be showed starting from root folder unless all folders stating from
        // specified level of parent folder.
        if (!tagcontent.equals("")) {
            try {
                if (tagcontent.indexOf(",")!=-1) {
                    level=Integer.parseInt(tagcontent.substring(0, tagcontent.indexOf(",")));
                    exact=tagcontent.substring(tagcontent.indexOf(",")+1).toLowerCase();
                } else {
                    level=Integer.parseInt(tagcontent);
                }
            } catch (NumberFormatException e) {
                level=1;
                exact=tagcontent.toLowerCase();
                if (!exact.equals("true")) {
                    exact="false";
                }
            }
        }
        // extract the folder
        String folder=extractFolder(cms, level, exact);
        if (folder.equals(""))
            return "".getBytes();
        //}
        // get uri, current folder, servletpath
        String requestedUri = cms.getRequestContext().getUri();
        String currentFolder=cms.readAbsolutePath(cms.getRequestContext().currentFolder());
        String servletPath = cms.getRequestContext().getRequest().getServletUrl();
        // register this folder for changes
        Vector vfsDeps = new Vector();
        vfsDeps.add(cms.readFolder(folder));
        registerVariantDeps(cms, doc.getAbsoluteFilename(), null, null,
                        (Hashtable)userObject, vfsDeps, null, null);
        // get all resources
        List resources=cms.getSubFolders(folder);
        List allFile=cms.getFilesInFolder(folder);
        ((ArrayList)resources).ensureCapacity(resources.size() + allFile.size());
        Iterator e = allFile.iterator();
        while (e.hasNext()) {
            resources.add(e.next());
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
        result=buildNavFold(cms, template, userObject, resources, requestedUri, currentFolder, servletPath, level, count);

        return result.getBytes();
    }
    /**
     * gets the navigation of specified level of parent folder.<p>
     *
     * @param cms CmsObject Object for accessing system resources
     * @param tagcontent used in this special case of a user method. Can't be ignored
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document
     * @param userObject Hashtable with parameters
     * @return byte[] with the content of this subelement
     * @throws CmsException if something goes wrong
     */
    public Object getNavParent(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
            throws CmsException {

        // template file
        CmsXmlTemplateFile template=(CmsXmlTemplateFile)doc;
        // check whether there exist entry datablock
        if (!template.hasData("naventry")) {
            return "".getBytes();
        }
        int level=1;
        String exact="";
        // tagcontent determines the folder starting from parent folder.
        // if tagcontent is null, zero or negative, then the navigation of current
        // folder must be showed.
        if (!tagcontent.equals("")) {
            try {
                if (tagcontent.indexOf(",")!=-1) {
                    level=Integer.parseInt(tagcontent.substring(0, tagcontent.indexOf(",")));
                    exact=tagcontent.substring(tagcontent.indexOf(",")+1).toLowerCase();
                } else {
                    level=Integer.parseInt(tagcontent);
                }
            } catch (NumberFormatException e) {
                level=1;
                exact=tagcontent.toLowerCase();
                if (!exact.equals("true")) {
                    exact="false";
                }
            }
        }
        // level is converted to negative number, so I can use the method
        // "extractFolder" for positive and negative numbers. Negative number
        // determines the parent folder level starting from current folder and
        // positive number determines the level starting ftom root folder.
        String currentFolder=extractFolder(cms, ((-1)*level), exact);
        if (currentFolder.equals(""))
            return "".getBytes();
        // register this folder for changes
        Vector vfsDeps = new Vector();
        vfsDeps.add(cms.readFolder(currentFolder));
        registerVariantDeps(cms, doc.getAbsoluteFilename(), null, null,
                        (Hashtable)userObject, vfsDeps, null, null);
        List resources=cms.getSubFolders(currentFolder);
        List allFile=cms.getFilesInFolder(currentFolder);

        ((ArrayList)resources).ensureCapacity(resources.size() + allFile.size());
        Iterator e = allFile.iterator();
        while (e.hasNext()) {
            resources.add(e.next());
        }
        // if there is not exist current datablock then take the entry datablock
        if (!template.hasData("navcurrent")) {
            template.setData("navcurrent", template.getData("naventry"));
        }
        return buildNav(cms, doc, userObject, resources).getBytes();
    }

    /**
     * gets the navigation of root folder or parent folder starting from root folder.<p>
     *
     * @param cms CmsObject Object for accessing system resources
     * @param tagcontent Unused in this special case of a user method. Can be ignored
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document
     * @param userObject Hashtable with parameters
     * @return byte[] with the content of this subelement
     * @throws CmsException if something goes wrong
     */
    public Object getNavRoot(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
            throws CmsException {

        // template file
        CmsXmlTemplateFile template=(CmsXmlTemplateFile)doc;
        // check whether there exist entry datablock
        if (!template.hasData("naventry")) {
            return "".getBytes();
        }
        int level=1;
        String exact="false";
        // tagcontent determines the folder starting from root folder.
        // if tagcontent is null, then the navigation of root folder must be showed.
        if (!tagcontent.equals("")) {
            try {
                if (tagcontent.indexOf(",")!=-1) {
                    level=Integer.parseInt(tagcontent.substring(0, tagcontent.indexOf(",")));
                    exact=tagcontent.substring(tagcontent.indexOf(",")+1).toLowerCase();
                } else {
                    level=Integer.parseInt(tagcontent);
                }
            } catch (NumberFormatException e) {
                level=1;
                exact=tagcontent.toLowerCase();
                if (!exact.equals("true")) {
                    exact="false";
                }
            }
        }
        String currentFolder=extractFolder(cms, level, exact);
        if (currentFolder.equals(""))
            return "".getBytes();
        // register this folder for changes
        Vector vfsDeps = new Vector();
        vfsDeps.add(cms.readFolder(currentFolder));
        registerVariantDeps(cms, doc.getAbsoluteFilename(), null, null,
                        (Hashtable)userObject, vfsDeps, null, null);
        // get all resources, it means all files and folders.
        List resources=cms.getSubFolders(currentFolder);
        List allFile=cms.getFilesInFolder(currentFolder);
        ((ArrayList)resources).ensureCapacity(resources.size() + allFile.size());
        Iterator e = allFile.iterator();
        while (e.hasNext()) {
            resources.add(e.next());
        }
        // if there is not exist current datablock then take the entry datablock
        if (!template.hasData("navcurrent")) {
            template.setData("navcurrent", template.getData("naventry"));
        }
        return buildNav(cms, doc, userObject, resources).getBytes();
    }
    
    /**
     * Builds the path from the root folder down to the current folder. Usage:
     * <p>
     * Build the path from the root folder down to the current folder:  
     * <pre>&lt;METHOD name="getNavPath"&gt;absolute,this,0&lt;/METHOD&gt;</pre>
     * <p>
     * Build the path from the root folder down to the parent of the current folder:
     * <pre>&lt;METHOD name="getNavPath"&gt;absolute,parent,0&lt;/METHOD&gt;</pre>
     * <p>
     * Build the path for the last three navigation levels down to the parent of the current folder:
     * <pre>&lt;METHOD name="getNavPath"&gt;relative,parent,3&lt;/METHOD&gt;</pre>
     * <p>
     * Build the path for the last three navigation levels down to the current folder: 
     * <pre>&lt;METHOD name="getNavPath"&gt;relative,this,3&lt;/METHOD&gt;</pre>
     * 
     * @param cms CmsObject to access the VFS
     * @param tagcontent includes the values for the start and end navigation level, comma separated
     * @param doc the XML template
     * @param userObject Hashtable with parameters (??)
     * @return byte[] the HTML of this element
     * @throws CmsException if something goes wrong
     */
    public Object getNavPath(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws CmsException {        
        // the result string holding the entire generated navigation path
        String navPath = "";

        // get the template file
        CmsXmlTemplateFile template = (CmsXmlTemplateFile)doc;
        
        // start absolute from root, or relative from the current folder?
        boolean startAbsolute = true;
        
        // the depth of the navigation path (= # of navigation levels)
        int requestedNavLevels = 1;
        
        // include the current folder in the path or not
        boolean includeCurrentFolder = true;
        
        // the navigation levels where we start and end to calculate the path
        int startLevel = 0, endLevel = 0;
      
        
        // if there is no "naventry" datablock in the template, return an empty string
        if (!template.hasData("naventry")) {
            return "".getBytes();
        }    
        
        // if there is no "navcurrent" datablock in the template, use the "naventry" instead
        if (!template.hasData("navcurrent")) {
            template.setData("navcurrent", template.getData("naventry"));
        }  
        
        // parse the tag content
        if (!"".equals(tagcontent)) {
            StringTokenizer tagContentTokenizer = new StringTokenizer(tagcontent, ",");
            
            if (tagContentTokenizer.countTokens()>=3) {
                // start absolute or relative?
                if (tagContentTokenizer.hasMoreTokens()) {
                    startAbsolute = tagContentTokenizer.nextToken().trim().equalsIgnoreCase("absolute");
                }
                
                // include the current folder or not?
                if (tagContentTokenizer.hasMoreTokens()) {
                    includeCurrentFolder = tagContentTokenizer.nextToken().trim().equalsIgnoreCase("this");
                }                
                
                // max. depth
                if (tagContentTokenizer.hasMoreTokens()) {
                    try {
                        requestedNavLevels = Integer.parseInt(tagContentTokenizer.nextToken());
                    } catch (Exception e) {
                        requestedNavLevels = 1;
                    }
                } 
            }                       
        }           
        
        // get the requested folder
        String requestedFolder = cms.readAbsolutePath(cms.getRequestContext().currentFolder());
        
        // calculate the end navigation level in the path
        endLevel = extractLevel(cms, requestedFolder);
        if (includeCurrentFolder) {
            endLevel++;
        }        
        
        // calculate the start navigation level in the path
        if (startAbsolute) {
            startLevel = 1;
        } else {
            startLevel = endLevel - requestedNavLevels + 1;
        }        
                
        for (int i=startLevel; i<=endLevel; i++) {
            String currentFolder = extractFolder(cms, i, "false");
            
            // register the current folder for changes in the beloved element cache
            Vector vfsDeps = new Vector();
            vfsDeps.add(cms.readFolder(currentFolder));
            registerVariantDeps(cms, doc.getAbsoluteFilename(), null, null, (Hashtable)userObject, vfsDeps, null, null);
            
            // add the current folder as the unique resource
            Vector resources = new Vector();       
            resources.addElement(cms.readFolder(currentFolder, false));                             
            
            // build the navigation for the current folder and append to the path
            String currentNav = buildNav(cms, doc, userObject, resources);
            navPath += currentNav;          
        }

        return navPath.getBytes();
    }   
    
    /**
     * Builds a catalogue or navigation sitemap. Usage: 
     * <pre>&lt;method name="getNavMap"&gt;1,3&lt;/method&gt;</pre> to build
     * a sitemap including everything between navigation levels 1 and 3.<p>
     * 
     * @param cms CmsObject to access the VFS
     * @param tagcontent includes the values for the start and end navigation level, comma separated
     * @param doc the XML template
     * @param userObject Hashtable with parameters (??)
     * @return byte[] the HTML of this element
     * @throws CmsException if something goes wrong
     */    
    public Object getNavMap(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws CmsException {                   
        // the result string holding the entire generated HTML of the navigation map
        String navMap = "";   
        
        // the navigation level where the map starts
        int startLevel = 1;
        
        // dito, where the map ends
        int endLevel = 2; 
        
        // get the template file
        CmsXmlTemplateFile template = (CmsXmlTemplateFile)doc;
        
        // if there is no "naventry" datablock in the template, return an empty string
        if (!template.hasData("naventry")) {
            return "".getBytes();
        }    
        
        // if there is no "navcurrent" datablock in the template, use the "naventry" instead
        if (!template.hasData("navcurrent")) {
            template.setData("navcurrent", template.getData("naventry"));
        }        
        
        
        // parse the tag content
        if (!"".equals(tagcontent)) {
            int commaIndex = tagcontent.indexOf(",");
            
            if (commaIndex!=-1 && tagcontent.length()>=3) {
                try {
                    startLevel = Integer.parseInt(tagcontent.substring(0, commaIndex));
                    endLevel = Integer.parseInt(tagcontent.substring(commaIndex+1));
                } catch (Exception e) {
                    // use default values in case of an exception
                    startLevel = 1;
                    endLevel = 2; 
                }
            }
        }
        
        // # of navigation levels needed to build the map
        int recursion = endLevel - startLevel;
        
        // extract the folder at the right depth where we want to start
        String currentFolder = extractFolder(cms, startLevel, "false");                         
        
        // build the map finally
        navMap = this.buildMap(cms, doc, userObject, currentFolder, "naventry", 0, recursion).toString();
        
        return navMap.getBytes();
    } 
    
    /**
     * This method invokes recursively itself to build a catalogue or map of the OpenCms
     * VFS structure. For each navigation level, it tries to process the datablock
     * naventry, naventrysub, naventrysubsub,...,naventry{n*times}sub for a depth of n levels.
     * 
     * @param cms CmsObject object for accessing the VFS
     * @param doc the XML template holding the datablock to process
     * @param userObject Hashtable with parameters
     * @param currentFolder the current folder for which we build the (sub) map 
     * @param datablock the name of the datablock in the XML template to generate the HTML for the current depth
     * @param currentResursionLevel the name says it all: the current recursion level (= curr. navigation level)
     * @param maxRecursionLevel dito, the max. recursion level (= navigation level depth)
     * @return byte[] the HTML of this element
     * @throws CmsException if something goes wrong
     */
    protected StringBuffer buildMap(CmsObject cms, A_CmsXmlContent doc, Object userObject, String currentFolder, String datablock, int currentResursionLevel, int maxRecursionLevel) throws CmsException {
        StringBuffer result = new StringBuffer();
        CmsXmlTemplateFile template = (CmsXmlTemplateFile)doc;
        boolean isFolder = false;
        
        // register the current folder for changes in the beloved element cache
        Vector vfsDeps = new Vector();
        vfsDeps.add(cms.readFolder(currentFolder));
        registerVariantDeps(cms, doc.getAbsoluteFilename(), null, null, (Hashtable)userObject, vfsDeps, null, null);        
     
        // collect all files and subfolders
        List resources = cms.getSubFolders(currentFolder);       
        List files = cms.getFilesInFolder(currentFolder);
        ((ArrayList)resources).ensureCapacity(resources.size() + files.size());
        
        Iterator allFiles = files.iterator();
        while (allFiles.hasNext()) {
            resources.add(allFiles.next());
        }    
        
        // evaluate the navigation properties properties
        int size = resources.size();
        String navLink[] = new String[size];
        String navText[] = new String[size];
        float navPos[] = new float[size];
        int navResourceCount = extractNav(cms, resources, navLink, navText, navPos);  
        
        for (int i=0; i<navResourceCount; i++) {
            isFolder = false;
            String currentNavLink = null;
            
            // set the navigation data/properties in the template
            template.setData("navtext", navText[i]);
            template.setData("navcount", new Integer(i+1).toString());
            template.setData("navlevel", new Integer(extractLevel(cms, navLink[i])).toString());
            
            if (navLink[i].endsWith("/")) {
                // the current resource is a folder
                isFolder = true;
                
                String navIndex = cms.readProperty(navLink[i], C_PROPERTY_NAVINDEX);
                if (navIndex==null) {
                    navIndex = C_NAVINDEX;
                }
                
                currentNavLink = navLink[i] + navIndex;
            } else {
                // the current resource is a file
                currentNavLink = navLink[i];
            }
            
            // test if the file exists or not to avoid broken links
            try {
                cms.readFile(currentNavLink);
                template.setData("navlink", OpenCms.getLinkManager().substituteLink(cms, currentNavLink));
            } catch (CmsException e) {
                template.setData("navlink", "#");
            }       
            
            // append the resource itself to the map
            result.append(template.getProcessedDataValue(datablock, this, userObject));
            
            // append the resource's submap, too
            if (isFolder && (currentResursionLevel + 1 <= maxRecursionLevel)) {
                String subMapNavEntryDatablock = "naventry";
                for (int j = 0; j <= currentResursionLevel; j++) {
                    subMapNavEntryDatablock += "sub";
                }
                
                // if there is no "naventrysubsub..." datablock in the template, use the "naventry" instead
                if (!template.hasData(subMapNavEntryDatablock)) {
                    subMapNavEntryDatablock = "naventry";
                }                

                StringBuffer subMap = this.buildMap(cms, doc, userObject, navLink[i], subMapNavEntryDatablock, currentResursionLevel+1, maxRecursionLevel);               
                result.append(subMap.toString());
            }
        }          
        
        return result;      
    }    
    
    /**
     * gets the navigation of folders recursive.<p>
     *
     * @param cms CmsObject Object for accessing system resources
     * @param tagcontent Unused in this special case of a user method. Can be ignored
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document
     * @param userObject Hashtable with parameters
     * @return byte[] with the content of this subelement
     * @throws CmsException if something goes wrong
     */
    public Object getNavTree(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
            throws CmsException {

        // template file
        CmsXmlTemplateFile template=(CmsXmlTemplateFile)doc;
        // check whether there exist entry datablock
        if (!template.hasData("naventry")) {
            return "".getBytes();
        }
        int level=1;
        int depth=0;
        int[] count={0};
        String exact="false";
        // if there is not any depth then it must not be tested in a if condition
        boolean depthIsNull=true;
        // if level is zero or null or negative then all folders recursive must
        // be showed starting from root folder unless all folders stating from
        // specified level of parent folder.
        if (!tagcontent.equals("")) {
            StringTokenizer st = new StringTokenizer(tagcontent, ",");
            String token1="", token2="", token3="";
            if (st.hasMoreTokens())
                token1=st.nextToken();
            if (st.hasMoreTokens())
                token2=st.nextToken();
            if (st.hasMoreTokens())
                token3=st.nextToken();
            // now assign tokens to real values
            if (!token3.equals(""))
                exact=token3.toLowerCase();
            if (!token2.equals("")) {
                try {
                    depth=Integer.parseInt(token2);
                } catch (NumberFormatException e) {
                    depth=0;
                    exact=token2.toLowerCase();
                    if (!exact.equals("true")) {
                        exact="false";
                    }
                }
            }
            if (!token1.equals("")) {
                try {
                    level=Integer.parseInt(token1);
                } catch (NumberFormatException e) {
                    level=1;
                    exact=token1.toLowerCase();
                    if (!exact.equals("true")) {
                        exact="false";
                    }
                }
            }
        }
        // if level is not entered or it is less than zero then folder is the root folder
        // otherwise the folder must be extracted accordeing to the entered level.
        String folder=extractFolder(cms, level, exact);
        if (folder.equals(""))
            return "".getBytes();
        if (depth>0) {
            depthIsNull=false;
        }
        // register this folder for changes
        Vector vfsDeps = new Vector();
        vfsDeps.add(cms.readFolder(folder));
        registerVariantDeps(cms, doc.getAbsoluteFilename(), null, null,
                        (Hashtable)userObject, vfsDeps, null, null);
        // get all folders in specified folder
        List resources=cms.getSubFolders(folder);
        // get all files in specified folder
        List allFile=cms.getFilesInFolder(folder);
        // get a vector of all files und folders
        ((ArrayList)resources).ensureCapacity(resources.size() + allFile.size());
        Iterator e = allFile.iterator();
        while (e.hasNext()) {
            resources.add(e.next());
        }
        // get the uri,servletpath and current folder
        String requestedUri = cms.getRequestContext().getUri();
        String currentFolder=cms.readAbsolutePath(cms.getRequestContext().currentFolder());
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
        result=buildNavTree(cms, template, userObject, resources, requestedUri, currentFolder, servletPath, level, depth, depthIsNull, count);
        return result.getBytes();
    }

    /**
     * gets a specified property of current folder.<p>
     *
     * @param cms A_CmsObject Object for accessing system resources
     * @param tagcontent Unused in this special case of a user method. Can be ignored
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document
     * @param userObject Hashtable with parameters
     * @return byte[] with the content of this subelement
     * @throws CmsException if something goes wrong
     */
    public Object getPropertyCurrent(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
        throws CmsException {
        String property="";
        // tagcontent must contain the property definition name.
        if (!tagcontent.equals("")) {
            String currentFolder=cms.readAbsolutePath(cms.getRequestContext().currentFolder());
            property=cms.readProperty(currentFolder, tagcontent);
            property=(property!=null?property:"");
            // register this folder for changes
            Vector vfsDeps = new Vector();
            vfsDeps.add(cms.readFolder(currentFolder));
            registerVariantDeps(cms, doc.getAbsoluteFilename(), null, null,
                            (Hashtable)userObject, vfsDeps, null, null);
        }
        return (property.getBytes());
    }


    /**
     * Gets a specified property of specified folder starting from current folder.<p>
     *
     * @param cms A_CmsObject Object for accessing system resources
     * @param tagcontent Unused in this special case of a user method. Can be ignored
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document
     * @param userObject Hashtable with parameters
     * @return byte[] with the content of this subelement
     * @throws CmsException if something goes wrong
     */
    public Object getPropertyParent(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
        throws CmsException {
        int level=1;
        String property="";
        // tagcontent determines the parent folder starting from current folder and
        // the property definition name sparated by a comma.
        String exact="false";
        // tagcontent determines the folder starting from root folder.
        // if tagcontent is null, then the navigation of root folder must be showed.
        if (!tagcontent.equals("")) {
            try {
                if (tagcontent.indexOf(",")!=-1) {
                    level=Integer.parseInt(tagcontent.substring(0, tagcontent.indexOf(",")));
                    exact=tagcontent.substring(tagcontent.indexOf(",")+1).toLowerCase();
                } else {
                    level=Integer.parseInt(tagcontent);
                }
            } catch (NumberFormatException e) {
                level=1;
                exact=tagcontent.toLowerCase();
                if (!exact.equals("true")) {
                    exact="false";
                }
            }
            // level is converted to negative number, so I can use the method
            // "extractFolder" for positive and negative numbers. Negative number
            // determines the parent folder level starting from current folder and
            // positive number determines the level starting ftom root folder.
            String currentFolder=extractFolder(cms, ((-1)*level), exact);
            if (currentFolder.equals(""))
                return "".getBytes();
            property=cms.readProperty(currentFolder, tagcontent.substring(tagcontent.indexOf(",")+1));
            property=(property!=null?property:"");
            // register this folder for changes
            Vector vfsDeps = new Vector();
            vfsDeps.add(cms.readFolder(currentFolder));
            registerVariantDeps(cms, doc.getAbsoluteFilename(), null, null,
                            (Hashtable)userObject, vfsDeps, null, null);
        }
        return (property.getBytes());
    }

    /**
     * Gets a specified property of specified folder starting from root.<p>
     *
     * @param cms A_CmsObject Object for accessing system resources
     * @param tagcontent Unused in this special case of a user method. Can be ignored
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document
     * @param userObject Hashtable with parameters
     * @return byte[] with the content of this subelement
     * @throws CmsException if something goes wrong
     */
    public Object getPropertyRoot(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
        throws CmsException {
        int level=1;
        String property="";
        String exact="false";
        // tagcontent determines the folder starting from root folder.
        // if tagcontent is null, then the navigation of root folder must be showed.
        if (!tagcontent.equals("")) {
            try {
                if (tagcontent.indexOf(",")!=-1) {
                    level=Integer.parseInt(tagcontent.substring(0, tagcontent.indexOf(",")));
                    exact=tagcontent.substring(tagcontent.indexOf(",")+1).toLowerCase();
                } else {
                    level=Integer.parseInt(tagcontent);
                }
            } catch (NumberFormatException e) {
                level=1;
                exact=tagcontent.toLowerCase();
                if (!exact.equals("true")) {
                    exact="false";
                }
            }
            String currentFolder=extractFolder(cms, level, exact);
            if (currentFolder.equals(""))
                return "".getBytes();
            property=cms.readProperty(currentFolder, tagcontent.substring(tagcontent.indexOf(",")+1));
            property=(property!=null?property:"");
            // register this folder for changes
            Vector vfsDeps = new Vector();
            vfsDeps.add(cms.readFolder(currentFolder));
            registerVariantDeps(cms, doc.getAbsoluteFilename(), null, null,
                            (Hashtable)userObject, vfsDeps, null, null);
        }
        return (property.getBytes());
    }


    /**
     * gets a specified property of uri.<p>
     *
     * @param cms A_CmsObject Object for accessing system resources
     * @param tagcontent Unused in this special case of a user method. Can be ignored
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document
     * @param userObject Hashtable with parameters
     * @return byte[] with the content of this subelement
     * @throws CmsException if somnething goes wrong
     */
    public Object getPropertyUri(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
        throws CmsException {
        String property="";
        String requestedUri = cms.getRequestContext().getUri();
        property=cms.readProperty(requestedUri, tagcontent);
        property=(property!=null?property:"");
        // register this folder for changes
        Vector vfsDeps = new Vector();
        vfsDeps.add(cms.readFileHeader(requestedUri));
        registerVariantDeps(cms, doc.getAbsoluteFilename(), null, null,
                        (Hashtable)userObject, vfsDeps, null, null);
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
        // we don't need the renewAfterPublish function becource we use the new dependencies feature.
        result.noAutoRenewAfterPublish();
        return result;
    }
    
    /**
     * This method retrieves max. three navigation levels per menu entry to 
     * build dynamic DHTML pop-up menus.<p>
     *
     * @param cms CmsObject Object for accessing system resources
     * @param tagcontent used in this special case of a user method. Can't be ignored
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document
     * @param userObject Hashtable with parameters
     * @return byte[] with the content of this subelement
     * @throws CmsException if something goes wrong
     */
    public Object getNavPop(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws CmsException {
        String currentFolder = "";
        StringBuffer sRet = new StringBuffer();
        // String currentFolder2 = cms.readPath(cms.getRequestContext().currentFolder());
        // int iLevel = extractLevel(cms, currentFolder2);
        int deep = 3;
        int level = -1;

        if (!tagcontent.equals("")) {
            try {
                StringTokenizer st = new StringTokenizer(tagcontent.toString(), ",");

                if (st.hasMoreTokens()) {
                    level = Integer.parseInt(st.nextToken().trim());

                }
                if (st.hasMoreTokens()) {
                    deep = Integer.parseInt(st.nextToken().trim());
                }
            } catch (Exception e) {
                throw new CmsException(e.getMessage());
            }
        }
        
        switch (level) {
            case -1 :
                currentFolder = cms.readAbsolutePath(cms.getRequestContext().currentFolder());
                break;
        
            case 0 :
                currentFolder = cms.readAbsolutePath(cms.rootFolder());
                break;
        
            default :
                if (extractFolder(cms, 1, "").equals("/")) {
                    currentFolder = extractFolder(cms, level + 1, "");
                } else {
                    currentFolder = extractFolder(cms, level, "");
                }
                break;
        }

        List resources = cms.getSubFolders(currentFolder);
        List allFile = cms.getFilesInFolder(currentFolder);
        ((ArrayList)resources).ensureCapacity(resources.size() + allFile.size());
        Iterator e = allFile.iterator();

        List resources2 = null;
        List allFile2 = null;
        Iterator e2 = null;
        Object oBuffer2 = null;

        List resources3 = null;
        List allFile3 = null;
        Iterator e3 = null;
        Object oBuffer1 = null;
        Object oBuffer3 = null;

        Vector resources4 = new Vector();
        ArrayList alLink = new ArrayList();
        ArrayList alPos = new ArrayList();
        ArrayList alLink2 = new ArrayList();
        ArrayList alPos2 = new ArrayList();

        while (e.hasNext()) {
            oBuffer1 = e.next();
            resources.add(oBuffer1);
        }

        sRet = buildNavPop(cms, doc, userObject, resources, null, 1, 0, 0, deep);

        if (deep > 1) {
            int size = resources.size();
            String navLink[] = new String[size];
            String navText[] = new String[size];
            float navPos[] = new float[size];

            int max = extractNav(cms, resources, navLink, navText, navPos);

            for (int iCount = 0; iCount < max; iCount++) {
                if (navLink[iCount].endsWith("/") || navLink[iCount].endsWith("\\")) {
                    alLink.add(navLink[iCount]);
                    alPos.add(new Integer(iCount));
                    resources2 = cms.getSubFolders(navLink[iCount]);
                    allFile2 = cms.getFilesInFolder(navLink[iCount]);
                    ((ArrayList)resources2).ensureCapacity(resources2.size() + allFile2.size());
                    e2 = allFile2.iterator();

                    while (e2.hasNext()) {
                        oBuffer2 = e2.next();
                        resources2.add(oBuffer2);
                    }

                    for (int iResCounter = 0; iResCounter < resources2.size(); iResCounter++) {
                        resources4.addElement(resources2.get(iResCounter));
                    }

                    sRet = buildNavPop(cms, doc, userObject, resources2, sRet, 2, iCount, 0, deep);

                    // these arrays are now initialized with the correct length
                    int size2 = resources2.size();
                    String navLink2[] = new String[size2];
                    String navText2[] = new String[size2];
                    float navPos2[] = new float[size2];

                    int max3 = extractNav(cms, resources2, navLink2, navText2, navPos2);

                    for (int iCount3 = 0; iCount3 < max3; iCount3++) {
                        alLink2.add(navLink2[iCount3]);
                        alPos2.add(new Integer(iCount3));
                    }

                }
            }
        }

        if (deep > 2) {
            int size2 = resources4.size();
            String[] navLink = new String[size2];
            String[] navText = new String[size2];
            float[] navPos = new float[size2];
            int iCounter3 = 0;

            int max2 = extractNav(cms, resources4, navLink, navText, navPos);

            for (int iCount = 0; iCount < max2; iCount++) {
                if (navLink[iCount].endsWith("/") || navLink[iCount].endsWith("\\")) {
                    resources3 = cms.getSubFolders(navLink[iCount]);
                    allFile3 = cms.getFilesInFolder(navLink[iCount]);
                    ((ArrayList)resources3).ensureCapacity(resources3.size() + allFile3.size());
                    e3 = allFile3.iterator();

                    while (e3.hasNext()) {
                        oBuffer3 = e3.next();
                        resources3.add(oBuffer3);
                    }

                    int iArrayPos = -1;
                    int iArrayPos2 = -1;
                    String sLinkDummy = "";

                    for (iCounter3 = 0; iCounter3 < alLink.size(); iCounter3++) {
                        sLinkDummy = navLink[iCount].substring(0, navLink[iCount].lastIndexOf("/"));
                        sLinkDummy = sLinkDummy.substring(0, sLinkDummy.lastIndexOf("/") + 1);
                        if (((String) alLink.get(iCounter3)).equals(sLinkDummy)) {
                            iArrayPos = ((Integer) alPos.get(iCounter3)).intValue();
                        }
                    }

                    for (iCounter3 = 0; iCounter3 < alLink2.size(); iCounter3++) {
                        sLinkDummy = navLink[iCount].substring(0, navLink[iCount].lastIndexOf("/") + 1);
                        if (((String) alLink2.get(iCounter3)).equals(sLinkDummy)) {
                            iArrayPos2 = ((Integer) alPos2.get(iCounter3)).intValue();
                        }
                    }

                    if (navText.length > 0) {
                        sRet = buildNavPop(cms, doc, userObject, resources3, sRet, 3, iArrayPos, iArrayPos2, deep);
                    }

                }
            }

        }

        return sRet.toString().getBytes();
    }

    /**
     * Builds the navigation customized with additional data block menueLevel, 
     * Level1Pos and Level2Pos to build dynamic DHTML pop-up menus by using
     * getNavPop.<p>
     *
     * @param cms CmsObject Object for accessing system resources
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document
     * @param userObject Hashtable with parameters
     * @param resources a vector that contains the elements of navigation
     * @param result2 ??
     * @param iDirLevel ??
     * @param lPos ??
     * @param lPos2 ?? 
     * @param iMaxDeep ??
     * @return String that contains the navigation
     * @throws CmsException if something goes wrong
     */
    protected StringBuffer buildNavPop(CmsObject cms, A_CmsXmlContent doc, Object userObject, List resources, StringBuffer result2, int iDirLevel, int lPos, int lPos2, int iMaxDeep) throws CmsException {
        String requestedUri = cms.getRequestContext().getUri();
        String currentFolder = cms.readAbsolutePath(cms.getRequestContext().currentFolder());
        String servletPath = cms.getRequestContext().getRequest().getServletUrl();

        CmsXmlTemplateFile xmlDataBlock = (CmsXmlTemplateFile) doc;
        StringBuffer result = null;
        if (result2 == null) {
            result = new StringBuffer();
        } else {
            result = result2;
        }

        int size = resources.size();

        String navLink[] = new String[size];
        String navText[] = new String[size];
        float navPos[] = new float[size];

        int max = extractNav(cms, resources, navLink, navText, navPos);

        // The arrays folderNames and folderTitles now contain all folders
        // that should appear in the nav.
        // Loop through all folders and generate output
        if (xmlDataBlock.hasData("navEntry")) {
            if (!xmlDataBlock.hasData("navCurrent")) {
                xmlDataBlock.setData("navCurrent", xmlDataBlock.getData("navEntry"));
            }

            xmlDataBlock.setData("menueLevel", iDirLevel + "");
            xmlDataBlock.setData("Level1Pos", lPos + 1 + "");
            xmlDataBlock.setData("Level2Pos", lPos2 + 1 + "");

            result.append(xmlDataBlock.getProcessedDataValue("navStart" + iDirLevel, this, userObject));

            boolean bHasSubFiles = false;
            for (int i = 0; i < max; i++) {
                bHasSubFiles = false;
                if (navLink[i].endsWith("/") || navLink[i].endsWith("\\")) {
                    List resources2 = cms.getSubFolders(navLink[i]);
                    List allFile2 = cms.getFilesInFolder(navLink[i]);
                    ((ArrayList)resources2).ensureCapacity(resources2.size() + allFile2.size());
                    Iterator e2 = allFile2.iterator();
                    while (e2.hasNext()) {
                        resources2.add(e2.next());
                    }
                    int size2 = resources2.size();
                    String navLink2[] = new String[size2];
                    String navText2[] = new String[size2];
                    float navPos2[] = new float[size2];

                    if (extractNav(cms, resources2, navLink2, navText2, navPos2) > 0) {
                        bHasSubFiles = true;
                    }

                }

                xmlDataBlock.setData("navText", navText[i]);
                xmlDataBlock.setData("count", new Integer(i + 1).toString());
                xmlDataBlock.setData("level", new Integer(extractLevel(cms, navLink[i])).toString());
                // this if condition is necessary because of url parameter,
                // if there is no filename then the parameters are ignored, so I
                // can't use e.g. ?cmsframe=body.
                if (navLink[i].endsWith("/")) {
                    String navIndex = cms.readProperty(navLink[i], C_PROPERTY_NAVINDEX);
                    if (navIndex == null) {
                        navIndex = C_NAVINDEX;
                    }
                    try {
                        cms.readFile(navLink[i] + navIndex);
                        xmlDataBlock.setData("navLink", servletPath + navLink[i] + navIndex);
                    } catch (CmsException e) {
                        xmlDataBlock.setData("navLink", servletPath + requestedUri);
                    }
                } else {
                    try {
                        cms.readFile(navLink[i]);
                        xmlDataBlock.setData("navLink", servletPath + navLink[i]);
                    } catch (CmsException e) {
                        xmlDataBlock.setData("navLink", servletPath + requestedUri);
                    }
                }
                // Check if nav is current nav
                xmlDataBlock.setData("data1", navLink[i]);
                xmlDataBlock.setData("data2", requestedUri);
                xmlDataBlock.setData("data3", currentFolder);

                String sFolder = "";
                String sFolder2 = "";
                sFolder = navLink[i];
                if (currentFolder.length() >= sFolder.length()) {
                    if (currentFolder.length() > sFolder.length()) {
                        sFolder2 = currentFolder.substring(0, sFolder.length());
                    } else {
                        sFolder2 = currentFolder.substring(0);
                    }
                }

                String sSubExten = "";
                if (bHasSubFiles && iDirLevel < iMaxDeep) {
                    sSubExten = "ws";
                }
                if (sFolder2.equals(sFolder) || navLink[i].equals(currentFolder) || navLink[i].equals(requestedUri)) {
                    result.append(xmlDataBlock.getProcessedDataValue("navCurrent" + iDirLevel + sSubExten, this, userObject));
                } else {
                    result.append(xmlDataBlock.getProcessedDataValue("navEntry" + iDirLevel + sSubExten, this, userObject));
                }

            }
            result.append(xmlDataBlock.getProcessedDataValue("navEnd" + iDirLevel, this, userObject));
        }

        return result;
    }

    /**
     * Can be used to build valid hrefs to switch the language. The parameter
     * list is contained in the body as a comma separated list:
     * <ul>
     * <li>language token to be replaced: <b>de</b>_lang_{foldername}/ -> <b>en</b>_lang_{foldername}/</li>
     * <li>token to split the language and the folder name, here _lang_</li>
     * <li>determines whether just the language token (0) or the entire folder name will be replaced (1)</li>
     * <li>URL of an error page if the page doesn't exist in the other language</li>
     * </ul>
     * 
     * @param cms CmsObject Object for accessing system resources
     * @param tagcontent the parameters for this tag
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document
     * @param userObject Hashtable with parameters
     * @return String or byte[] with the content of this subelement
     * @throws CmsException if something goes wrong
     */
    public Object getLanguagePath(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws CmsException {
        String sLanguageToken = "_lang_";
        String sLanguageIdent = "";
        String sNoFoundLink = "";
        String sTokenReplaceType = "0";
        String sLinkLeftSide = "/";
        String currentFolder = cms.getRequestContext().getUri();
        String sReturnPath = "";
        int iParamCount;
        String sParam = "";

        if (!tagcontent.equals("")) {
            try {
                StringTokenizer st = new StringTokenizer(tagcontent.toString(), ",");
                iParamCount = st.countTokens();

                if (iParamCount < 1) {
                    throw new CmsException("Not enough parameters!");
                }

                if (st.hasMoreTokens()) {
                    sLanguageIdent = st.nextToken().trim();
                }

                if (st.hasMoreTokens()) {
                    sParam = st.nextToken().trim();
                    if (!sParam.equals("")) {
                        sLanguageToken = sParam;
                    }
                }

                if (st.hasMoreTokens()) {
                    sParam = st.nextToken().trim();
                    if (!sParam.equals("")) {
                        sTokenReplaceType = sParam;
                    }
                }

                if (st.hasMoreTokens()) {
                    sParam = st.nextToken().trim();
                    if (!sParam.equals("")) {
                        sNoFoundLink = sParam;
                    }
                }

            } catch (Exception e) {
                throw new CmsException(e.getMessage());
            }

            int ifound = 0;

            if (sTokenReplaceType.equals("0")) {
                ifound = currentFolder.indexOf(sLanguageToken);
            } else {
                currentFolder = currentFolder.substring(1);
                ifound = currentFolder.indexOf("/");
            }

            if (ifound > (-1)) {
                String sTemp = currentFolder.substring(0, ifound);
                sLinkLeftSide = sTemp.substring(0, sTemp.lastIndexOf("/") + 1);
                sReturnPath = sLinkLeftSide + "" + sLanguageIdent + currentFolder.substring(ifound);

                //check if the resource exists
                boolean resourceExists = false;

                try {
                    CmsResource dummy = cms.readFileHeader(sReturnPath);

                    if (dummy != null) {
                        // the resource exists
                        resourceExists = true;
                    }
                } catch (CmsException e) {
                    // the resource doesn't exist
                    resourceExists = false;
                }

                if (!resourceExists) {
                    if (iParamCount > 3) {
                        sReturnPath = "/" + sNoFoundLink;
                    } else {
                        sReturnPath = sReturnPath.substring(0, sReturnPath.lastIndexOf("/") + 1);
                    }
                }
            } else {
                currentFolder = cms.getRequestContext().getUri();
            }
        } else {
            throw new CmsException("Not enough parameters!");
        }

        return cms.getRequestContext().getRequest().getServletUrl() + sReturnPath;
    }   
}
