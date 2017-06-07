/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.staticexport;

import org.opencms.site.CmsSite;
import org.opencms.util.CmsStringUtil;

import java.util.Comparator;

/**
 * A bean for a export name. Combines the export name with the site information<p>
 */
public class CmsExportname {

    /**
     * A export name comparator.<p>
     */
    public static class CmsExportNameComparator implements Comparator<CmsExportname> {

        /** A slash comparator. */
        private CmsStringUtil.CmsSlashComparator m_comp = new CmsStringUtil.CmsSlashComparator();

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(CmsExportname o1, CmsExportname o2) {

            if (((o1.getSite() == null) && (o2.getSite() == null))
                || ((o1.getSite() == null) && (o2.getSite() != null))
                || ((o1.getSite() != null) && (o2.getSite() == null))
                || o1.getSite().getSiteRoot().equals(o2.getSite().getSiteRoot())) {
                return m_comp.compare(o1.getExportname(), o2.getExportname());
            } else {
                int siteComp = o1.getSite().getSiteRoot().compareTo(o2.getSite().getSiteRoot());
                if (siteComp == 0) {
                    return m_comp.compare(o1.getExportname(), o2.getExportname());
                }
                return siteComp;
            }
        }
    }

    /** The value of the exportname property. */
    private String m_exportname;

    /**
     * The according site.<p>
     */
    private CmsSite m_site;

    /**
     * Constructor with parameters.<p>
     * 
     * @param exportname the export name
     * @param site the site object
     */
    public CmsExportname(String exportname, CmsSite site) {

        m_exportname = exportname;
        m_site = site;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CmsExportname other = (CmsExportname)obj;
        if (m_exportname == null) {
            if (other.m_exportname != null) {
                return false;
            }
        } else if (!m_exportname.equals(other.m_exportname)) {
            return false;
        }
        if (m_site == null) {
            if (other.m_site != null) {
                return false;
            }
        } else if (!m_site.equals(other.m_site)) {
            return false;
        }
        return true;
    }

    /**
     * Returns the value of the 'exportname' property.<p>
     * 
     * @return the value of the 'exportname' property
     */
    public String getExportname() {

        return m_exportname;
    }

    /**
     * Returns the site.<p>
     * 
     * @return the site
     */
    public CmsSite getSite() {

        return m_site;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((m_exportname == null) ? 0 : m_exportname.hashCode());
        result = (prime * result) + ((m_site == null) ? 0 : m_site.hashCode());
        return result;
    }

    /**
     * Sets the export name.<p>
     * 
     * @param exportname the value to set the export name to
     */
    public void setExportname(String exportname) {

        m_exportname = exportname;
    }

    /**
     * Sets the site.<p>
     * 
     * @param site the site to set
     */
    public void setSite(CmsSite site) {

        m_site = site;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "CmsExportName [m_exportname=" + m_exportname + ", m_site=" + m_site + "]";
    }
}
