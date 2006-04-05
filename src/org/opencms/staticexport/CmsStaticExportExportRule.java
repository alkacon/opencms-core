/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/CmsStaticExportExportRule.java,v $
 * Date   : $Date: 2005/07/08 17:42:47 $
 * Version: $Revision: 1.1 $
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

package org.opencms.staticexport;

import org.opencms.db.CmsPublishedResource;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Help class for storing of export-rules.<p>
 * 
 * @author Michael Moossen
 * @version $Revision: 1.1 $
 * @since 6.0.0
 */
public class CmsStaticExportExportRule {

    /** Description of the rule. */
    private String m_description;

    /** configured Rfs export path. */
    private List m_exportResources;

    /** List of regular expresions to determine if a relevant resource has been modified. */
    private List m_modifiedResources;

    /** Name of rule. */
    private String m_name;

    /**
     * Default constructor.<p>
     * 
     * @param name the name of the rule
     * @param description the description for the rule
     */
    public CmsStaticExportExportRule(String name, String description) {

        m_name = name;
        m_description = description;
        m_exportResources = new ArrayList();
        m_modifiedResources = new ArrayList();
    }

    /**
     * Full Constructor.<p>
     * 
     * @param name the name of the rule
     * @param description the description of the rule
     * @param modifiedResources a list of patterns to identify modified resources
     * @param exportResourcePatterns a list of strings to export resources
     */
    public CmsStaticExportExportRule(
        String name,
        String description,
        List modifiedResources,
        List exportResourcePatterns) {

        this(name, description);
        m_modifiedResources.addAll(modifiedResources);
        m_exportResources.addAll(exportResourcePatterns);
    }

    /**
     * Adds a export Resource expression.<p>
     *
     * @param exportResource the export Resource expression to add
     */
    public void addExportResourcePattern(String exportResource) {

        m_exportResources.add(exportResource);
    }

    /**
     * Adds a modified Resource regular expression.<p>
     *
     * @param modifiedRegex the modified Resource regular expression to add
     */
    public void addModifiedResource(String modifiedRegex) {

        m_modifiedResources.add(Pattern.compile(modifiedRegex));
    }

    /**
     * Returns the description.<p>
     *
     * @return the description
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Returns the export Resources list.<p>
     *
     * @return the export Resources list
     */
    public List getExportResourcePatterns() {

        return Collections.unmodifiableList(m_exportResources);
    }

    /**
     * Returns a set of <code>{@link CmsPublishedResource}</code> containing all resources specified by the
     * <code>&lt;export-resources&gt;</code> node of this rule.<p>  
     * 
     * @param cms the cms context
     * 
     * @return a set of matching resources
     * 
     * @throws CmsException if something goes wrong
     */
    public Set getExportResources(CmsObject cms) throws CmsException {

        Set resources = new HashSet();
        // now get all resources matching the export resource patterns. 
        // since the long min and max value
        // do not work with the sql timestamp function in the driver, we must calculate 
        // some different, but usable start and endtime values first

        //starttime to 01.01.1970
        long starttime = 0;
        // endtime to now plus one week
        long endtime = System.currentTimeMillis() + 604800000;

        Iterator itExpRes = m_exportResources.iterator();
        while (itExpRes.hasNext()) {
            String exportRes = (String)itExpRes.next();

            List vfsResources = cms.getResourcesInTimeRange(exportRes, starttime, endtime);
            // loop through the list and create the list of CmsPublishedResources
            Iterator itRes = vfsResources.iterator();
            while (itRes.hasNext()) {
                CmsResource vfsResource = (CmsResource)itRes.next();
                if ((vfsResource.getFlags() & CmsResource.FLAG_INTERNAL) == CmsResource.FLAG_INTERNAL) {
                    // skip internal files
                    continue;
                }
                CmsPublishedResource resource = new CmsPublishedResource(vfsResource);
                resources.add(resource);
            }
        }
        return resources;
    }

    /**
     * Returns the modified Resources list as list of <code>{@link Pattern}</code>.<p>
     * 
     * @return the modified Resources list as list of <code>{@link Pattern}</code>
     */
    public List getModifiedResources() {

        return Collections.unmodifiableList(m_modifiedResources);
    }

    /**
     * Returns the name.<p>
     *
     * @return the name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns a set of <code>{@link CmsPublishedResource}</code> objects specified by the
     * <code>&lt;export-resources&gt;</code> node of this rule, if the publishedResource 
     * matches a modified Resource regular expression.<p> 
     * 
     * @param cms the cms context
     * @param publishedResource a published resource to test
     * 
     * @return a set of matching resources, or <code>null</code> if resource does not match
     * 
     * @throws CmsException if something goes wrong
     */
    public Set getRelatedResources(CmsObject cms, CmsPublishedResource publishedResource) throws CmsException {

        if (match(publishedResource.getRootPath())) {
            return getExportResources(cms);
        }
        return null;
    }

    /**
     * Checks if a vfsName matches the given modified resource patterns.<p>
     * 
     * @param vfsName the vfs name of a resource to check
     * @return true if the name matches one of the given modified resource patterns
     */
    public boolean match(String vfsName) {

        for (int j = 0; j < m_modifiedResources.size(); j++) {
            Pattern pattern = (Pattern)m_modifiedResources.get(j);
            if (pattern.matcher(vfsName).matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer ret = new StringBuffer(getClass().getName());
        ret.append(":[");
        ret.append("name: ").append(m_name).append("; ");
        ret.append("description: ").append(m_description).append("; ");
        ret.append("modified patterns: ").append(m_modifiedResources).append("; ");
        ret.append("export resources: ").append(m_exportResources).append("; ");
        return ret.append("]").toString();
    }
}
