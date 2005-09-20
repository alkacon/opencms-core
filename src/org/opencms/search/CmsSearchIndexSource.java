/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsSearchIndexSource.java,v $
 * Date   : $Date: 2005/09/20 15:39:06 $
 * Version: $Revision: 1.12.2.1 $
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

package org.opencms.search;

import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * A search index source is a description of a list of Cms resources
 * to be indexed.<p>
 * 
 * @author Thomas Weckert  
 * 
 * @version $Revision: 1.12.2.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsSearchIndexSource implements Comparable {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSearchIndexSource.class);

    /** A list of Cms resource types to be indexed. */
    private List m_documentTypes;

    /** The indexer. */
    private I_CmsIndexer m_indexer;

    /** The class name of the indexer. */
    private String m_indexerClassName;

    /** The logical key/name of this index. */
    private String m_name;

    /** A map of optional key/value parameters. */
    private Map m_params;

    /** A list of Cms resources to be indexed. */
    private List m_resourcesNames;

    /**
     * Creates a new CmsSearchIndexSource.<p>
     */
    public CmsSearchIndexSource() {

        m_params = new HashMap();
        m_resourcesNames = new ArrayList();
        m_documentTypes = new ArrayList();
    }

    /**
     * Adds a parameter.<p>
     * 
     * @param key the key/name of the parameter
     * @param value the value of the parameter
     */
    public void addConfigurationParameter(String key, String value) {

        m_params.put(key, value);
    }

    /**
     * Adds the key/name of a document type.<p>
     * 
     * @param key the key/name of a document type
     */
    public void addDocumentType(String key) {

        m_documentTypes.add(key);
    }

    /**
     * Adds the path of a Cms resource.<p>
     * 
     * @param resourceName the path of a Cms resource
     */
    public void addResourceName(String resourceName) {

        m_resourcesNames.add(resourceName);
    }

    /**
     * Compares the internal name Strings of this instance and the argument casted 
     * to this type. <p>
     * 
     * Note that this method only should return 0 for the statement 
     * <code>a.compareTo(a)</code> 
     * as the name of a indexsource has 
     * to be unique within OpenCms.<p>
     * 
     * @param o another indexsource.
     * 
     * @return the comparison result (as specified in {@link String#compareTo(java.lang.String)} for the 
     *         name member of both indexsource instances involved.
     * 
     * @throws ClassCastException if the given argument is not assignable from this class. 
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) throws ClassCastException {

        CmsSearchIndexSource other = (CmsSearchIndexSource)o;
        String otherName = other.getName();
        String myName = getName();
        return myName.compareTo(otherName);
    }

    /**
     * Implemented to be consistent with overridden method 
     * <code>{@link #compareTo(Object)}</code>.<p>
     * 
     * Note that this method only should return true for the statement 
     * <code>a.compareTo(a)</code> 
     * as the name of a indexsource has 
     * to be unique within OpenCms.<p>
     * 
     * @param obj another indexsource.
     * 
     * @return true if <code>{@link #compareTo(Object)}</code> with this argument returns 0, false else. 
     * 
     * @see java.lang.Object#equals(java.lang.Object) 
     */
    public boolean equals(Object obj) {

        boolean ret = false;
        try {
            int cp = compareTo(obj);
            ret = cp == 0;
        } catch (Exception e) {
            // remain false   
        }
        return ret;
    }

    /**
     * Returns the list of Cms resource types to be indexed.<p>
     *
     * @return the list of Cms resource types to be indexed
     */
    public List getDocumentTypes() {

        return m_documentTypes;
    }

    /**
     * Returns the indexer.<p>
     * 
     * @return the indexer
     */
    public I_CmsIndexer getIndexer() {

        return m_indexer;
    }

    /**
     * Returns the class name of the indexer.<p>
     *
     * @return the class name of the indexer
     */
    public String getIndexerClassName() {

        return m_indexerClassName;
    }

    /**
     * Returns the logical key/name of this search index source.<p>
     *
     * @return the logical key/name of this search index source
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the value for a specified parameter key.<p>
     * 
     * @param key the parameter key/name
     * @return the value for the specified parameter key
     */
    public String getParam(String key) {

        return (String)m_params.get(key);
    }

    /**
     * Returns the map of optional key/value parameters.<p>
     *
     * @return the map of optional key/value parameters
     */
    public Map getParams() {

        return m_params;
    }

    /**
     * Returns the list of Cms resources to be indexed.<p>
     *
     * @return the list of Cms resources to be indexed
     */
    public List getResourcesNames() {

        return m_resourcesNames;
    }

    /**
     * Overriden to be consistents with overridden method 
     * <code>{@link #equals(Object)}</code>. 
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        return m_name.hashCode();
    }

    /**
     * Removes the key/name of a document type.<p>
     * 
     * @param key the key/name of a document type 
     * 
     * @return true if the given key was contained before thus could be removed successfully, false else.
     */
    public boolean removeDocumentType(String key) {

        return m_documentTypes.remove(key);
    }

    /**
     * Sets the list of Cms resource types to be indexed.<p>
     *
     * @param documentTypes the list of Cms resource types to be indexed
     */
    public void setDocumentTypes(List documentTypes) {

        m_documentTypes = documentTypes;
    }

    /**
     * Sets the class name of the indexer.<p>
     * 
     * An Exception is thrown to allow GUI-display of wrong input.<p>
     *
     * @param indexerClassName the class name of the indexer 
     * 
     * @throws CmsIllegalArgumentException if the given String is not a fully qualified classname (within this Java VM)
     */
    public void setIndexerClassName(String indexerClassName) throws CmsIllegalArgumentException {

        try {
            m_indexer = (I_CmsIndexer)Class.forName(indexerClassName).newInstance();
            m_indexerClassName = indexerClassName;
        } catch (Exception exc) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().key(Messages.LOG_INDEXER_CREATION_FAILED_1, m_indexerClassName), exc);
            }
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_INDEXSOURCE_INDEXER_CLASS_NAME_2,
                indexerClassName,
                I_CmsIndexer.class.getName()));
        }
    }

    /**
     * Sets the logical key/name of this search index source.<p>
     *
     * @param name the logical key/name of this search index source
     * 
     * @throws CmsIllegalArgumentException if argument name is null, an empty or whitespace-only Strings 
     *         or already used for another indexsource's name. 
     */
    public void setName(String name) throws CmsIllegalArgumentException {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(name)) {
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_INDEXSOURCE_CREATE_MISSING_NAME_0));
        }
        // already used? Don't test this at xml-configuration time (no manager)
        if (OpenCms.getRunLevel() > OpenCms.RUNLEVEL_2_INITIALIZING) {
            CmsSearchManager mngr = OpenCms.getSearchManager();
            // don't test this if the indexsource is not new (widget invokes setName even if it was not changed) 
            if (mngr.getIndexSource(name) != this) {
                if (mngr.getSearchIndexSources().keySet().contains(name)) {
                    throw new CmsIllegalArgumentException(Messages.get().container(
                        Messages.ERR_INDEXSOURCE_CREATE_INVALID_NAME_1,
                        name));
                }
            }
        }
        m_name = name;
    }

    /**
     * Sets the map of optional key/value parameters.<p>
     *
     * @param params the map of optional key/value parameters
     */
    public void setParams(Map params) {

        m_params = params;
    }

    /**
     * Sets the list of Cms resources to be indexed.<p>
     *
     * @param resources the list of Cms resources (Strings) to be indexed
     */
    public void setResourcesNames(List resources) {

        m_resourcesNames = resources;
    }
}