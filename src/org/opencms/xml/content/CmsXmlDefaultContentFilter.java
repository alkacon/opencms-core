/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/content/Attic/CmsXmlDefaultContentFilter.java,v $
 * Date   : $Date: 2004/08/03 07:19:04 $
 * Version: $Revision: 1.1 $
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
 * @version $Revision: 1.1 $
 * @since 5.5.0
 */
public class CmsXmlDefaultContentFilter implements I_CmsXmlContentFilter {

    /** format for create filter. */
    private static final PrintfFormat C_FORMAT_NUMBER = new PrintfFormat("%0.4d");

    /** Static array of the possible filters. */
    private static final String[] m_filterNames = {"allInFolder"};

    /** array list for fast lookup. */
    private static final List m_filters = Arrays.asList(m_filterNames);

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentFilter#getCreateLink(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getCreateLink(CmsObject cms, String filterName, String param) throws CmsException {

        // if action is not set use default
        if (filterName == null) {
            filterName = m_filterNames[0];
        }

        switch (m_filters.indexOf(filterName)) {
            case 0:
                return getAllInFolderCreate(cms, param);
            default:
                throw new CmsException("Invalid XML content filter selected: " + filterName);
        }
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
                return getAllInFolder(cms, param);
            default:
                throw new CmsException("Invalid XML content filter selected: " + filterName);                    
        }
    }

    /**
     * Returns all XML content objects in the folder pointed to by the parameter.<p>
     * 
     * @param cms the current CmsObject
     * @param param the folder name to use
     * @return all XML content objects in the folder
     * @throws CmsException if something goes wrong
     */
    protected List getAllInFolder(CmsObject cms, String param) throws CmsException {

        int pos1 = param.indexOf('|');
        if (pos1 == -1) {
            throw new CmsException("Malformed filter parameter '" + param + "'");
        }

        String foldername = CmsResource.getFolderPath(param.substring(0, pos1));
        int type = Integer.valueOf(param.substring(pos1 + 1)).intValue();

        List result;
        String siteRoot = cms.getRequestContext().getSiteRoot();
        int prefix = siteRoot.length();
        cms.getRequestContext().saveSiteRoot();
        try {
            cms.getRequestContext().setSiteRoot("/");
            List resources = cms.getFilesInFolder(foldername);
            result = new ArrayList(resources.size());

            Iterator i = resources.iterator();
            while (i.hasNext()) {
                CmsResource resource = (CmsResource)i.next();
                if (resource.getTypeId() != type) {
                    continue;
                }
                result.add(cms.getSitePath(resource).substring(prefix));
            }
        } finally {
            cms.getRequestContext().restoreSiteRoot();
        }

        Collections.sort(result);
        Collections.reverse(result);

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
    protected String getAllInFolderCreate(CmsObject cms, String param) throws CmsException {

        int pos1 = param.indexOf('|');
        if (pos1 == -1) {
            throw new CmsException("Malformed filter parameter '" + param + "'");
        }

        String path = param.substring(0, pos1);
        String foldername = CmsResource.getFolderPath(path);

        List result;
        cms.getRequestContext().saveSiteRoot();
        try {
            cms.getRequestContext().setSiteRoot("/");

            List resources = cms.getFilesInFolder(foldername, CmsResourceFilter.ALL);
            result = new ArrayList(resources.size());

            for (int i = 0; i < resources.size(); i++) {
                // must check ALL resources in folder because named don't care for type
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
            checkName = CmsStringUtil.substitute(path, "${number}", number);
        } while (result.contains(checkName));

        return checkName.substring(cms.getRequestContext().getSiteRoot().length());
    }
}