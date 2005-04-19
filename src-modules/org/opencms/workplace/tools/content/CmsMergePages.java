/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/content/CmsMergePages.java,v $
 * Date   : $Date: 2005/04/19 17:20:51 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.content;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsReport;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.xml.page.CmsXmlPage;
import org.opencms.xml.page.CmsXmlPageFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods for the merge pages dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/administration/properties/merge/index.html
 * </ul>
 *
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.4 $
 * 
 */
public class CmsMergePages extends CmsReport {

    /** A constant representing the select option all templates. */
    public static final String C_ALL = "ALL";
    /** The dialog type. */
    public static final String DIALOG_TYPE = "mergepages";

    /** Request parameter name for the first folder to merge. */
    public static final String PARAM_FOLDER1 = "folder1";
    /** Request parameter name for the second folder to merge. */
    public static final String PARAM_FOLDER2 = "folder2";
    
    /** Key for pages found in folder 1 exclusivly. */
    public static final int C_FOLDER1_EXCLUSIVE = 0;

    /** Key for pages found in folder 2 exclusivly. */
    public static final int C_FOLDER2_EXCLUSIVE = 1;

    /** Key for pages found in both folders as siblings. */
    public static final int C_FOLDERS_SIBLING = 2;

    /** Key for pages found in both folders as individual resources. */
    public static final int C_FOLDERS_EQUALNAMES = 3;

    /** Key for pages found in both folders but as different types. */
    public static final int C_FOLDERS_DIFFERENTTYPES = 4;
    
    /** the cms object. */
    private CmsObject m_cms;
    /** the error message. */
    private String m_errorMessage;

    /** The first folder to merge. */
    private String m_paramFolder1;
    /** The second folder to merge. */
    private String m_paramFolder2;    
    /** the report for the output. */
    private I_CmsReport m_report;
    
    /** List of pages found in folder 1 exclusivly. */
    private List m_folder1Exclusive;
    
    /** List of pages found in folder 2 exclusivly. */
    private List m_folder2Exclusive;
    
    /** List of pages found in both folders as siblings. */
    private List m_foldersSibling;
    
    /** List of pages found in both folders as individual resources. */
    private List m_foldersEqualnames;

    /** List of pages found in  both folders but as different types. */
    private List m_foldersDifferenttypes;
    
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsMergePages(CmsJspActionElement jsp) {

        super(jsp);
        m_folder1Exclusive = new ArrayList();
        m_folder2Exclusive = new ArrayList();
        m_foldersSibling = new ArrayList();
        m_foldersEqualnames = new ArrayList();
        m_foldersDifferenttypes = new ArrayList();
    }


    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param cms the current CmsObject
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsMergePages(CmsObject cms, PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
        m_cms = cms;
    }

    /**
     * Merges the specified resources.<p>
     * 
     * @param report the cms report
     */
    public void actionMerge(I_CmsReport report) {

        m_report = report;
        m_report.println(m_report.key("report.mergepages.headline"), I_CmsReport.C_FORMAT_HEADLINE);
        
        try {
            // collect all pages and sort them depending on their state
            collectResources();
            // merge all pages that can be merged
            mergePages();
            // cleanup
            cleanup();
        } catch (CmsException e) {
            m_report.println(e.toString(), I_CmsReport.C_FORMAT_ERROR);
        }
        
    }
    
    /**
     * Cleanup all internal storages.<p>    *
     */
    private void cleanup() {
        m_folder1Exclusive = null;
        m_folder2Exclusive = null;
        m_foldersSibling = null;
        m_foldersEqualnames = null;
        m_foldersDifferenttypes = null;
    }
    
    
    /**
     * Merges those pages in the two merge folders that have the same name and are no siblings of each other yet.<p>
     * @throws CmsException if something goes wrong
     */
    private void mergePages() throws CmsException {
        int size = m_foldersEqualnames.size();
        if (size > 0) {
            m_report.println(m_report.key("report.mergepages.mergepages") , I_CmsReport.C_FORMAT_HEADLINE);
            String defaultLocale = CmsLocaleManager.getDefaultLocale().toString();
            String locale2 = m_cms.readPropertyObject(getParamFolder2(), "locale", true).getValue(defaultLocale);                        
                   
            // lock the source and the target folder
            m_report.print(m_report.key("report.mergepages.lock") + getParamFolder1() , I_CmsReport.C_FORMAT_NOTE); 
            m_report.print(m_report.key("report.dots"), I_CmsReport.C_FORMAT_NOTE);
            m_cms.lockResource(getParamFolder1());
            m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
    
            m_report.print(m_report.key("report.mergepages.lock") + getParamFolder2() , I_CmsReport.C_FORMAT_NOTE); 
            m_report.print(m_report.key("report.dots"), I_CmsReport.C_FORMAT_NOTE);
            m_cms.lockResource(getParamFolder2());
            m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
            
            // now loop through all collected resources
            m_report.print(size + " "+m_report.key("report.mergepages.pages"),  I_CmsReport.C_FORMAT_HEADLINE);
            m_report.println(m_report.key("report.dots"),  I_CmsReport.C_FORMAT_HEADLINE);
            int count = 1;
            Iterator i = m_foldersEqualnames.iterator();
            while (i.hasNext()) {
                String resFolder1Name = (String)i.next();
                try {
                    String resFolder2Name = getResourceNameInOtherFolder(resFolder1Name, getParamFolder1(), getParamFolder2());
                    m_report.print("( " + count++ + " / " + size + " ) " + m_report.key("report.mergepages.scanning") + " " + resFolder1Name + " <-> " + resFolder2Name , I_CmsReport.C_FORMAT_NOTE);
                    m_report.println(m_report.key("report.dots"),  I_CmsReport.C_FORMAT_NOTE);
 
                    // get the content of the resource in folder1  
                    String locale = m_cms.readPropertyObject(resFolder1Name, "locale", true).getValue(defaultLocale);                        
                    m_report.print(m_report.key("report.mergepages.readcontent") + " "+ resFolder1Name + ": Locale = "+locale, I_CmsReport.C_FORMAT_NOTE);
                    m_report.print(m_report.key("report.dots"),  I_CmsReport.C_FORMAT_NOTE);
                    CmsResource resFolder1 = m_cms.readResource(resFolder1Name, CmsResourceFilter.IGNORE_EXPIRATION);
                    CmsFile fileFolder1 = CmsFile.upgrade(resFolder1, m_cms);
                    CmsXmlPage pageFolder1 = CmsXmlPageFactory.unmarshal(m_cms, fileFolder1);            
                    m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
                    
                    // get the content of the resource in folder2
                    locale = m_cms.readPropertyObject(resFolder2Name, "locale", true).getValue(defaultLocale);                                    
                    m_report.print(m_report.key("report.mergepages.readcontent") + " "+ resFolder2Name + ": Locale = "+locale, I_CmsReport.C_FORMAT_NOTE);
                    m_report.print(m_report.key("report.dots"),  I_CmsReport.C_FORMAT_NOTE);
                    CmsResource resFolder2 = m_cms.readResource(resFolder2Name, CmsResourceFilter.IGNORE_EXPIRATION);
                    CmsFile fileFolder2 = CmsFile.upgrade(resFolder2, m_cms);
                    CmsXmlPage pageFolder2 = CmsXmlPageFactory.unmarshal(m_cms, fileFolder2);            
                    m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);    
                    
                    // now get all the text elements from the resource in folder 2 which match the the locale of folder 2
                    Locale loc = new Locale(locale2);
                    List textElements2 = pageFolder2.getNames(loc);
                    Iterator j = textElements2.iterator();
                    while (j.hasNext()) {
                        String textElementName = (String) j.next();
                        m_report.print(m_report.key("report.mergepages.gettextelement") + " "+ textElementName, I_CmsReport.C_FORMAT_NOTE);
                        m_report.print(m_report.key("report.dots"),  I_CmsReport.C_FORMAT_NOTE);
                        // get the text element from the resource in folder 2...
                        String textElement = pageFolder2.getValue(textElementName, loc).getStringValue(m_cms);
                        // and set it in the resource in folder 1...
                        // WARNING: An existing content will be overwritten!
                        if (!pageFolder1.hasValue(textElementName, loc)) {
                            pageFolder1.addValue(textElementName, loc);
                        }
                        pageFolder1.setStringValue(m_cms, textElementName, loc, textElement);                
                        m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);   
                    }
                    // the resource in folder 1 now has all text elements in both locales, so update it in the vfs          
                    m_report.print(m_report.key("report.mergepages.writecontent") + " "+ resFolder1Name, I_CmsReport.C_FORMAT_NOTE);
                    m_report.print(m_report.key("report.dots"),  I_CmsReport.C_FORMAT_NOTE);
                    fileFolder1.setContents(pageFolder1.marshal());
                    m_cms.writeFile(fileFolder1);
                    m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK); 
                    
                    // save all properties from the resource in folder2
                    m_report.print(m_report.key("report.mergepages.saveproperties") + " "+ resFolder2Name, I_CmsReport.C_FORMAT_NOTE);
                    m_report.print(m_report.key("report.dots"),  I_CmsReport.C_FORMAT_NOTE);
                    List properties = m_cms.readPropertyObjects(resFolder2Name, false);
                    //m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK); 
        
                    // the next thing to do is to delete the old resource in folder 2
                    m_report.print(m_report.key("report.mergepages.deletefile") + " "+ resFolder2Name, I_CmsReport.C_FORMAT_NOTE);
                    m_report.print(m_report.key("report.dots"), I_CmsReport.C_FORMAT_NOTE);
                    m_cms.deleteResource(resFolder2Name, I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);
                    m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK); 
                    
                    // copy a sibling of the resource from folder 1 to folder 2 
                    m_report.print(m_report.key("report.mergepages.copyfile") + " "+ resFolder1Name + " -> " + resFolder2Name, I_CmsReport.C_FORMAT_NOTE);
                    m_report.print(m_report.key("report.dots"),  I_CmsReport.C_FORMAT_NOTE);
                    m_cms.copyResource(resFolder1Name, resFolder2Name, I_CmsConstants.C_COPY_AS_SIBLING);
                    //m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
                    
                    // restore the properties at the sibling in folder 2
                    m_report.print(m_report.key("report.mergepages.restoreproperties") + " "+ resFolder2Name, I_CmsReport.C_FORMAT_NOTE);
                    m_report.print(m_report.key("report.dots"),  I_CmsReport.C_FORMAT_NOTE);
                    m_cms.writePropertyObjects(resFolder2Name, properties);
                    m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
                    
                    resFolder1 = null;
                    resFolder2 = null;
                    fileFolder1 = null;
                    fileFolder2 = null;
                    pageFolder1 = null;
                    pageFolder2 = null;
                    
                } catch (CmsException e) {
                    m_report.println(e.toString(), I_CmsReport.C_FORMAT_ERROR); 
                }
              
            }
            // lock the source and the target folder
            m_report.print(m_report.key("report.mergepages.unlock") + getParamFolder1() , I_CmsReport.C_FORMAT_NOTE); 
            m_report.print(m_report.key("report.dots"), I_CmsReport.C_FORMAT_NOTE);
            m_cms.unlockResource(getParamFolder1());
            m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
    
            m_report.print(m_report.key("report.mergepages.unlock") + getParamFolder2() , I_CmsReport.C_FORMAT_NOTE); 
            m_report.print(m_report.key("report.dots"), I_CmsReport.C_FORMAT_NOTE);
            m_cms.unlockResource(getParamFolder2());
            m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
            
            m_report.print(m_report.key("report.dots"),  I_CmsReport.C_FORMAT_HEADLINE);
            m_report.println(m_report.key("report.mergepages.done"), I_CmsReport.C_FORMAT_HEADLINE);
        }
    }
    
    
    
    /**
     * Collect all pages in the folders to merge and sort them depending on the required action to do.<p>
     * 
     * The method will create several lists. Each list contains the resource names of pages
     * and will be used in further steps of the merging process.
     * <ul>
     * <li>List m_folder1Exclusive: contains all pages which are exclusivly found in folder 1</li>
     * <li>List m_folder2Exclusive: contains all pages which are exclusivly found in folder 2</li>
     * <li>List m_foldersSibling: contains all pages which can be found in both folders and are siblings of each other </li>
     * <li>List m_foldersEqualnames: contains all pages which can be found in both folders and are no siblings of each other</li>
     * <li>List m_foldersDifferenttypes: contains all pages which can be found in both folders but are of different types</li>
     * </ul>
     * @throws CmsException
     */
    private void collectResources() throws CmsException {
        String defaultLocale = CmsLocaleManager.getDefaultLocale().toString();
        String locale1 = m_cms.readPropertyObject(getParamFolder1(), "locale", true).getValue(defaultLocale);
        String locale2 = m_cms.readPropertyObject(getParamFolder2(), "locale", true).getValue(defaultLocale);
        m_report.println(m_report.key("report.mergepages.folder")+ " " + getParamFolder1() + ": Locale = "+locale1);
        m_report.println(m_report.key("report.mergepages.folder")+ " " + getParamFolder2() + ": Locale = "+locale2);
           
        // collect all resources in folder 1
        collectFolder(getParamFolder1(), getParamFolder2(), 1);
        // collect all resources in folder 2
        collectFolder(getParamFolder2(), getParamFolder1(), 2);
        
        // report the results of the collection
        m_report.println(m_report.key("report.mergepages.collectionresult") , I_CmsReport.C_FORMAT_HEADLINE);        
        m_report.println(m_report.key("report.mergepages.action0") , I_CmsReport.C_FORMAT_HEADLINE);
        reportList(m_folder1Exclusive, false);
        m_report.println(m_report.key("report.mergepages.action1") , I_CmsReport.C_FORMAT_HEADLINE);
        reportList(m_folder2Exclusive, false);
        m_report.println(m_report.key("report.mergepages.action2") , I_CmsReport.C_FORMAT_HEADLINE);
        reportList(m_foldersSibling, false);
        m_report.println(m_report.key("report.mergepages.action3") , I_CmsReport.C_FORMAT_HEADLINE);
        reportList(m_foldersEqualnames, true);  
        m_report.println(m_report.key("report.mergepages.action4") , I_CmsReport.C_FORMAT_HEADLINE);
        reportList(m_foldersDifferenttypes, false);
  
    }
    
    
    /**
     * Creates a report list of all resources in one of the collected lists.<p>
     * 
     * @param collected the list to create the output from
     * @param doReport flag to enable detailed report
     */
    private void reportList(List collected, boolean doReport) {
        int size = collected.size();
        // now loop through all collected resources
        m_report.print(size + " "+m_report.key("report.mergepages.pages"),  I_CmsReport.C_FORMAT_HEADLINE);
        m_report.println(m_report.key("report.dots"),  I_CmsReport.C_FORMAT_HEADLINE);
        if (doReport) {
            int count = 1;
            
            Iterator i = collected.iterator();
            while (i.hasNext()) {
                String resName = (String)i.next();
                m_report.println("( " + count++ + " / " + size + " ) " + m_report.key("report.mergepages.scanning") + " " + resName, I_CmsReport.C_FORMAT_NOTE);            
            }
        }
        m_report.print(m_report.key("report.dots"),  I_CmsReport.C_FORMAT_HEADLINE);
        m_report.println(m_report.key("report.mergepages.done"), I_CmsReport.C_FORMAT_HEADLINE);
    }
    
    
    /**
     * Collect all pages in a folders  and sort them depending on the required action to do.<p>
     * 
     * @param sourceMergeFolder the source merge folder to collect all pages from
     * @param targetMergefolder the target merge folder to compare to
     * @param currentFolder integer value (1 or 2) showing if the source folder is folder 1 or folder 2
     * @throws CmsException if something goes wrong
     */
    private void collectFolder(String sourceMergeFolder, String targetMergefolder, int currentFolder) throws CmsException {
        m_report.println(m_report.key("report.mergepages.scanfolder") + " " + sourceMergeFolder , I_CmsReport.C_FORMAT_HEADLINE);
        //get the list of all resources in the source merge folder
        CmsResourceFilter filter = CmsResourceFilter.IGNORE_EXPIRATION.addRequireType(CmsResourceTypeXmlPage.getStaticTypeId());        
        List folderResources = m_cms.readResources(sourceMergeFolder, filter, true);
        Iterator i = folderResources.iterator();
        int size = folderResources.size();
        // now loop through all resources and check them against those in the target merge folder
        m_report.print(size + " "+m_report.key("report.mergepages.pages"),  I_CmsReport.C_FORMAT_HEADLINE);
        m_report.println(m_report.key("report.dots"),  I_CmsReport.C_FORMAT_HEADLINE);
        int count = 1;
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            String resName = m_cms.getSitePath(res);
            m_report.print("( " + count++ + " / " + size + " ) " + m_report.key("report.mergepages.scanning") + " " + resName, I_CmsReport.C_FORMAT_NOTE);
            m_report.print(m_report.key("report.dots"));
            // now analyse the page and calculate the action to do
            int action = analyse(res, sourceMergeFolder, targetMergefolder, currentFolder);
            // add the name of the resource to the correct list
            switch (action) {
                case C_FOLDER1_EXCLUSIVE:   
                    m_folder1Exclusive.add(resName);
                    break;
                case C_FOLDER2_EXCLUSIVE:   
                    m_folder2Exclusive.add(resName);
                    break;
                case C_FOLDERS_SIBLING:   
                    if (!m_foldersSibling.contains(getResourceNameInOtherFolder(resName, sourceMergeFolder, targetMergefolder))) {
                        m_foldersSibling.add(resName);
                    }
                    break;
                case C_FOLDERS_EQUALNAMES:   
                    if (!m_foldersEqualnames.contains(getResourceNameInOtherFolder(resName, sourceMergeFolder, targetMergefolder))) {                        
                        m_foldersEqualnames.add(resName);
                    }
                    break;
                case C_FOLDERS_DIFFERENTTYPES:  
                    if (!m_foldersDifferenttypes.contains(getResourceNameInOtherFolder(resName, sourceMergeFolder, targetMergefolder))) {                                                
                        m_foldersDifferenttypes.add(resName);
                    }
                    break;                    
                default:
                    break;
            }                                  
            res=null;
            m_report.println(m_report.key("report.mergepages.action" + action), I_CmsReport.C_FORMAT_OK);
        }
        folderResources = null;
        m_report.print(m_report.key("report.dots"),  I_CmsReport.C_FORMAT_HEADLINE);
        m_report.println(m_report.key("report.mergepages.done"), I_CmsReport.C_FORMAT_HEADLINE);
        
    }
    
    /**
     * Gets the name of a resource in the other merge folder.<p>
     * 
     * @param resName the complete path of a resource
     * @param sourceMergeFolder the path to the source merge folder
     * @param targetMergefolder the path to the target merge folder
     * @return the name of a resource in the other merge folder
     */
    private String getResourceNameInOtherFolder(String resName, String sourceMergeFolder, String targetMergefolder) {
        // get the resourcename of the resouce to test without the source merge folder
        String resourcename = resName.substring(sourceMergeFolder.length());
        // get the complete path of the resource in the other merge folder
        return  targetMergefolder + resourcename;
    }
    
    /**
     * Analyses a page in the source morge folder and tests if a resouce with the same name exists in the target merge folder.<p>
     * 
     * The method then calcualtes a action for further processing of this page, possible values are:
     * <ul>
     * <li>C_FOLDER1_EXCLUSIVE: exclusivly found in folder 1</li>
     * <li>C_FOLDER2_EXCLUSIVE: exclusivly found in folder 2</li>
     * <li>C_FOLDERS_SIBLING: found in both folders as siblings of each other </li>
     * <li>C_FOLDERS_EQUALNAMES: found in both folders as individual resources</li>
     * <li>C_FOLDERS_DIFFERENTTYPES: found in both folders as different types</li>    
     * </ul>
     * @param res the resource to test
     * @param sourceMergeFolder the path to the source merge folder
     * @param targetMergefolder the path to the target merge folder
     * @param currentFolder integer value (1 or 2) showing if the source folder is folder 1 or folder 2
     * @return value of the action to do with this page
     */
    private int analyse(CmsResource res, String sourceMergeFolder, String targetMergefolder, int currentFolder) {
        int retValue = -1;
        String resourcenameOther = getResourceNameInOtherFolder(m_cms.getSitePath(res), sourceMergeFolder,  targetMergefolder);
        try {
           CmsResource otherRes = m_cms.readResource(resourcenameOther, CmsResourceFilter.IGNORE_EXPIRATION);
           // there was a resource with the same name in the other merge folder
           // now check if it is already a sibling of the current resource
           if (res.getResourceId().equals(otherRes.getResourceId())) {
               // it is a sibling, so set the action to "sibling already";
               retValue = C_FOLDERS_SIBLING;
           } else {
               // it is no sibling, now test if it has the same resource type than the oringinal resource
               if (res.getTypeId() == otherRes.getTypeId()) {
                   // both resources have the same type, so set the action to  "same name". Only those resources can be merged
                   retValue = C_FOLDERS_EQUALNAMES;
               } else {
                   // both resources have different types, so set the action to "different types"
                   retValue = C_FOLDERS_DIFFERENTTYPES;
               }
           }
        } catch (CmsException e) {
            // the resource was not found, so set the action mode to "found only in the source folder"
            if (currentFolder == 1) {
                retValue = C_FOLDER1_EXCLUSIVE;
            } else {
                retValue = C_FOLDER2_EXCLUSIVE;
            }
        }
        
        return retValue;
    }
    
    
    
    /**
     * Performs the move report, will be called by the JSP page.<p>
     * 
     * @throws JspException if problems including sub-elements occur
     */
    public void actionReport() throws JspException {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
        switch (getAction()) {
            case ACTION_REPORT_END:
                actionCloseDialog();
                break;
            case ACTION_REPORT_UPDATE:
                setParamAction(REPORT_UPDATE);
                getJsp().include(C_FILE_REPORT_OUTPUT);
                break;
            case ACTION_REPORT_BEGIN:
            case ACTION_CONFIRMED:
            default:
                CmsMergePagesThread thread = new CmsMergePagesThread(getCms(), this);
                setParamAction(REPORT_BEGIN);
                setParamThread(thread.getUUID().toString());
                getJsp().include(C_FILE_REPORT_OUTPUT);
                break;
        }
    }
    

 
    /**
     * @see org.opencms.workplace.CmsWorkplace#getCms()
     */
    public CmsObject getCms() {

        if (m_cms == null) {
            return super.getCms();
        }

        return m_cms;
    }

    /**
     * Returns the errorMessage.<p>
     *
     * @return the errorMessage
     */
    public String getErrorMessage() {

        if (CmsStringUtil.isEmpty(m_errorMessage)) {
            return "";
        }

        return m_errorMessage;
    }

    /**
     * Returns the second folder.<p>
     *
     * @return the folder
     */
    public String getParamFolder2() {

        return m_paramFolder2;
    }


    /**
     * Returns the first folder.<p>
     *
     * @return the folder
     */
    public String getParamFolder1() {

        return m_paramFolder1;
    }

    

    /**
     * Sets the errorMessage.<p>
     *
     * @param errorMessage the errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {

        m_errorMessage = errorMessage;
    }

    /**
     * Sets the second folder to merge.<p>
     *
     * @param folder2 the second folder name to set
     */
    public void setParamFolder2(String folder2) {

        m_paramFolder2 = folder2;
    }


    /**
     * Sets the first folder to merge.<p>
     *
     * @param folder1 the first folder name to set
     */
    public void setParamFolder1(String folder1) {

        m_paramFolder1 = folder1;
    }
    


    /**
     * Does validate the request parameters and returns a buffer with error messages.<p>
     * @param cms the current cms object
     * If there were no error messages, the buffer is empty.<p>
     */
    public void validateParameters(CmsObject cms) {

        StringBuffer validationErrors = new StringBuffer();
        if (CmsStringUtil.isEmpty(getParamFolder1())) {
            validationErrors.append("- Please select a the first folder for merging.<br>");
        } else {
            try {
                cms.readResource(getParamFolder1());                
            } catch (CmsException e) {
                validationErrors.append("- First folder is nor readable or does not exist.<br>");
            }
        }
        if (CmsStringUtil.isEmpty(getParamFolder2())) {
            validationErrors.append("- Please select a the second folder for merging..<br>");
        } else {
            try {
                cms.readResource(getParamFolder2());                
            } catch (CmsException e) {
                validationErrors.append("- Second folder is nor readable or does not exist.<br>");
            }
        }
        if (getParamFolder1().equals(getParamFolder2())) {
            validationErrors.append("- Both folders must be different.<br>");
        }
        
        setErrorMessage(validationErrors.toString());
    }    

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch 
        // set the action for the JSP switch 
        if (DIALOG_CONFIRMED.equals(getParamAction())) {
            setAction(ACTION_CONFIRMED);
        } else if (DIALOG_OK.equals(getParamAction())) {
            setAction(ACTION_OK);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else if (REPORT_UPDATE.equals(getParamAction())) {
            setAction(ACTION_REPORT_UPDATE);
        } else if (REPORT_BEGIN.equals(getParamAction())) {
            setAction(ACTION_REPORT_BEGIN);
        } else if (REPORT_END.equals(getParamAction())) {
            setAction(ACTION_REPORT_END);
        } else {
            setAction(ACTION_DEFAULT);
            // add the title for the dialog 
            setParamTitle(key("title.mergepages"));
        }
    }



  
}