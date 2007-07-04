
package org.opencms.relations;

import java.util.List;

/**
 * Relation validator entry information bean.<p> 
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 6.5.5
 */
public final class CmsRelationValidatorInfoEntry {

    /** The original entry name. */
    private String m_entryName;
    /** The broken relation sources. */
    private List m_relations;
    /** The resource name. */
    private String m_resourceName;
    /** The site name. */
    private String m_siteName;
    /** The site root. */
    private String m_siteRoot;

    /**
     * Default constructor.<p>
     * 
     * @param entryName the entry name
     * @param resourceName the resource name
     * @param siteName the site title
     * @param siteRoot the site root
     * @param relations the broken relation source list
     */
    public CmsRelationValidatorInfoEntry(
        String entryName,
        String resourceName,
        String siteName,
        String siteRoot,
        List relations) {

        m_entryName = entryName;
        m_resourceName = resourceName;
        m_siteName = siteName;
        m_siteRoot = siteRoot;
        m_relations = relations;
    }

    /**
     * Returns the entry Name.<p>
     *
     * @return the entry Name
     */
    public String getName() {

        return m_entryName;
    }

    /**
     * Returns all the relations for this entry.<p>
     * 
     * @return a list of {@link CmsRelation} objects
     */
    public List getRelations() {

        return m_relations;
    }

    /**
     * Returns the resource name.<p>
     *
     * @return the resource name
     */
    public String getResourceName() {

        return m_resourceName;
    }

    /**
     * Returns the site name.<p>
     *
     * @return the site name
     */
    public String getSiteName() {

        return m_siteName;
    }

    /**
     * Returns the site root.<p>
     *
     * @return the site root
     */
    public String getSiteRoot() {

        return m_siteRoot;
    }
}