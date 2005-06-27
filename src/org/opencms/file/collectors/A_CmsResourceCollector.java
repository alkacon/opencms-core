/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/collectors/A_CmsResourceCollector.java,v $
 * Date   : $Date: 2005/06/27 23:22:23 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file.collectors;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.PrintfFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides some helpful base implementations for resource collector classes.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.7 $
 * 
 * @since 6.0.0 
 */
public abstract class A_CmsResourceCollector implements I_CmsResourceCollector {

    /** Format for file create parameter. */
    private static final PrintfFormat NUMBER_FORMAT = new PrintfFormat("%0.4d");

    /** The collector order of this collector. */
    protected int m_order;

    /** The hash code of this collector. */
    private int m_hashcode;

    /**
     * Constructor to initialize some default values.<p>
     */
    public A_CmsResourceCollector() {

        m_hashcode = getClass().getName().hashCode();
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object obj) {

        if (obj == this) {
            return 0;
        }
        if (obj instanceof I_CmsResourceCollector) {
            return getOrder() - ((I_CmsResourceCollector)obj).getOrder();
        }
        return 0;
    }

    /**
     * Two collectors are considered to be equal if they are sharing the same
     * implementation class.<p> 
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof I_CmsResourceCollector) {
            return getClass().getName().equals(obj.getClass().getName());
        }
        return false;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getOrder()
     */
    public int getOrder() {

        return m_order;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        return m_hashcode;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#setOrder(int)
     */
    public void setOrder(int order) {

        m_order = order;
    }

    /**
     * Returns the link to create a new XML content item in the folder pointed to by the parameter.<p>
     * 
     * @param cms the current CmsObject
     * @param param the folder name to use
     * 
     * @return the link to create a new XML content item in the folder
     * 
     * @throws CmsException if something goes wrong
     */
    protected String getCreateInFolder(CmsObject cms, String param) throws CmsException {

        CmsCollectorData data = new CmsCollectorData(param);

        String foldername = CmsResource.getFolderPath(data.getFileName());

        // must check ALL resources in folder because name dosen't care for type
        List resources = cms.readResources(foldername, CmsResourceFilter.ALL, false);

        // now create a list of all resources that just contains the file names
        List result = new ArrayList(resources.size());
        for (int i = 0; i < resources.size(); i++) {
            CmsResource resource = (CmsResource)resources.get(i);
            result.add(resource.getRootPath());
        }

        String fileName = cms.getRequestContext().addSiteRoot(data.getFileName());
        String checkName;
        String number;

        int j = 0;
        do {
            number = NUMBER_FORMAT.sprintf(++j);
            checkName = CmsStringUtil.substitute(fileName, "${number}", number);
        } while (result.contains(checkName));

        return cms.getRequestContext().removeSiteRoot(checkName);
    }

    /**
     * Shrinks a List to fit a maximum size.<p>
     * 
     * @param result a List
     * @param maxSize the maximum size of the List
     * 
     * @return the shrinked list
     */
    protected List shrinkToFit(List result, int maxSize) {

        if ((maxSize > 0) && (result.size() > maxSize)) {
            // cut off all items > count
            result = result.subList(0, maxSize);
        }

        return result;
    }
}