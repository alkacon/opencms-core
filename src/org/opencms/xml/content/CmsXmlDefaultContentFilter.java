/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/content/Attic/CmsXmlDefaultContentFilter.java,v $
 * Date   : $Date: 2004/10/18 18:10:21 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.xml.content;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.PrintfFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A default filter to generates list of XML content objects from the VFS.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.5 $
 * @since 5.5.0
 */
public class CmsXmlDefaultContentFilter extends A_CmsXmlContentFilter {

    /**
     * Data structure for the filter data, parsed form the filter parameters.<p>
     */
    protected class CmsFilterData {

        /** The display count. */
        private int m_count;

        /** The filename. */
        private String m_fileName;

        /** The file type. */
        private int m_type;

        /**
         * Creates a new filter data set.<p>
         * 
         * @param data the data to parse
         */
        public CmsFilterData(String data) {

            if (data == null) {
                throw new IllegalArgumentException(
                    "Filter requires a parameter in the form '/sites/default/myfolder/file_${number}.html|11|4'");
            }

            int pos1 = data.indexOf('|');
            if (pos1 == -1) {
                throw new IllegalArgumentException("Malformed filter parameter '" + data + "'");
            }

            int pos2 = data.indexOf('|', pos1 + 1);
            if (pos2 == -1) {
                pos2 = data.length();
                m_count = 0;
            } else {
                m_count = Integer.valueOf(data.substring(pos2 + 1)).intValue();
            }

            m_fileName = data.substring(0, pos1);
            m_type = Integer.valueOf(data.substring(pos1 + 1, pos2)).intValue();
        }

        /**
         * Returns the count.<p>
         *
         * @return the count
         */
        public int getCount() {

            return m_count;
        }

        /**
         * Returns the file name.<p>
         *
         * @return the file name
         */
        public String getFileName() {

            return m_fileName;
        }

        /**
         * Returns the type.<p>
         *
         * @return the type
         */
        public int getType() {

            return m_type;
        }
    }

    /** Format for create filter. */
    private static final PrintfFormat C_FORMAT_NUMBER = new PrintfFormat("%0.4d");

    /** Static array of the possible filters. */
    private static final String[] m_filterNames = {
        "singleFile",
        "allInFolder",
        "allInFolderDateReleasedDesc",
        "allInSubTree",
        "allInSubTreeDateReleasedDesc"};

    /** Array list for fast filter name lookup. */
    private static final List m_filters = Collections.unmodifiableList(Arrays.asList(m_filterNames));

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentFilter#getCreateLink(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getCreateLink(CmsObject cms, String filterName, String param) throws CmsException {

        // if action is not set, use default action
        if (filterName == null) {
            filterName = m_filterNames[0];
        }

        switch (m_filters.indexOf(filterName)) {
            case 0:
                // "singleFile"
                return null;
            case 1:
                // "allInFolder"
                return getCreateInFolder(cms, param);
            case 2:
                // "allInFolderDateReleasedDesc"
                return getCreateInFolder(cms, param);
            case 3:
                // "allInSubTree"
                return null;
            case 4:
                // "allInSubTreeDateReleasedDesc"
                return null;
            default:
                throw new CmsException("Invalid XML content filter selected: " + filterName);
        }
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentFilter#getFilterNames()
     */
    public List getFilterNames() {

        return m_filters;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentFilter#getFilterResults(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public List getFilterResults(CmsObject cms, String filterName, String param) throws CmsException {

        // if action is not set use default
        if (filterName == null) {
            filterName = m_filterNames[0];
        }

        switch (m_filters.indexOf(filterName)) {
            case 0:
                // "singleFile"
                return getSingleFile(cms, param);
            case 1:
                // "allInFolder"
                return getAllInFolder(cms, param, false);
            case 2:
                // "allInFolderDateReleasedDesc"
                return allInFolderDateReleasedDesc(cms, param, true);
            case 3:
                // "allInSubTree"
                return getAllInFolder(cms, param, false);
            case 4:
                // "allInSubTreeDateReleasedDesc"
                return allInFolderDateReleasedDesc(cms, param, true);
            default:
                throw new CmsException("Invalid XML content filter selected: " + filterName);
        }
    }

    /**
     * Returns a List of all XML content objects in the folder pointed to by the parameter 
     * sorted by the release date, descending.<p>
     * 
     * @param cms the current CmsObject
     * @param param the folder name to use
     * @param tree if true, look in folder and all child folders, if false, look only in given folder
     * @return a List of all XML content objects in the folder pointed to by the parameter 
     *      sorted by the release date, descending
     * 
     * @throws CmsException if something goes wrong
     */
    protected List allInFolderDateReleasedDesc(CmsObject cms, String param, boolean tree) throws CmsException {

        CmsFilterData data = new CmsFilterData(param);
        String foldername = CmsResource.getFolderPath(data.getFileName());

        List result;
        String siteRoot = cms.getRequestContext().getSiteRoot();
        int prefix = siteRoot.length();
        cms.getRequestContext().saveSiteRoot();
        try {
            cms.getRequestContext().setSiteRoot("/");

            CmsResourceFilter filter = CmsResourceFilter.DEFAULT.addRequireType(data.getType());
            List resources = cms.readResources(foldername, filter, tree);

            Collections.sort(resources, CmsResource.C_DATE_RELEASED_COMPARATOR);
            Collections.reverse(resources);
            result = new ArrayList(resources.size());

            Iterator i = resources.iterator();
            while (i.hasNext()) {
                CmsResource resource = (CmsResource)i.next();
                // cut site prefix from result
                result.add(resource.getRootPath().substring(prefix));
            }
        } finally {
            cms.getRequestContext().restoreSiteRoot();
        }

        if ((data.getCount() > 0) && (result.size() > data.getCount())) {
            // cut off all items > count
            result = result.subList(0, data.getCount());
        }        
        
        return result;
    }

    /**
     * Returns all XML content objects in the folder pointed to by the parameter.<p>
     * 
     * @param cms the current CmsObject
     * @param param the folder name to use
     * @param tree if true, look in folder and all child folders, if false, look only in given folder
     * 
     * @return all XML content objects in the folder
     * 
     * @throws CmsException if something goes wrong
     */
    protected List getAllInFolder(CmsObject cms, String param, boolean tree) throws CmsException {

        CmsFilterData data = new CmsFilterData(param);
        String foldername = CmsResource.getFolderPath(data.getFileName());

        List result;
        String siteRoot = cms.getRequestContext().getSiteRoot();
        int prefix = siteRoot.length();
        cms.getRequestContext().saveSiteRoot();
        try {
            cms.getRequestContext().setSiteRoot("/");

            CmsResourceFilter filter = CmsResourceFilter.DEFAULT.addRequireType(data.getType());
            List resources = cms.readResources(foldername, filter, tree);
            result = new ArrayList(resources.size());

            Iterator i = resources.iterator();
            while (i.hasNext()) {
                CmsResource resource = (CmsResource)i.next();
                result.add(cms.getSitePath(resource).substring(prefix));
            }
        } finally {
            cms.getRequestContext().restoreSiteRoot();
        }

        Collections.sort(result);
        Collections.reverse(result);
        
        if ((data.getCount() > 0) && (result.size() > data.getCount())) {
            // cut off all items > count
            result = result.subList(0, data.getCount());
        }

        return result;
    }

    /**
     * Returns the link to create a new XML content item in the folder pointed to by the parameter.<p>
     * 
     * @param cms the current CmsObject
     * @param param the folder name to use
     * @return the link to create a new XML content item in the folder
     * @throws CmsException if something goes wrong
     */
    protected String getCreateInFolder(CmsObject cms, String param) throws CmsException {

        CmsFilterData data = new CmsFilterData(param);

        String foldername = CmsResource.getFolderPath(data.getFileName());

        List result;
        cms.getRequestContext().saveSiteRoot();
        try {
            cms.getRequestContext().setSiteRoot("/");

            // must check ALL resources in folder because name dosen't care for type
            CmsResourceFilter filter = CmsResourceFilter.DEFAULT;
            List resources = cms.readResources(foldername, filter, false);
            result = new ArrayList(resources.size());

            for (int i = 0; i < resources.size(); i++) {
                CmsResource resource = (CmsResource)resources.get(i);
                result.add(cms.getSitePath(resource));
            }
        } finally {
            cms.getRequestContext().restoreSiteRoot();
        }

        String checkName;
        String number;

        int j = 0;
        do {
            number = C_FORMAT_NUMBER.sprintf(++j);
            checkName = CmsStringUtil.substitute(data.getFileName(), "${number}", number);
        } while (result.contains(checkName));

        return checkName.substring(cms.getRequestContext().getSiteRoot().length());
    }

    /**
     * Returns a List with a single file name containing given parameter.<p>
     * 
     * @param cms the current CmsObject
     * @param param the name of the file to load
     * @return a List with a single file name containing given parameter
     */
    protected List getSingleFile(CmsObject cms, String param) {

        if ((param == null) || (cms == null)) {
            throw new IllegalArgumentException("Filter requires a parameter in the form '/myfolder/file.html'");
        }

        // create a list and return it
        ArrayList result = new ArrayList(1);
        result.add(param);
        return result;
    }
}