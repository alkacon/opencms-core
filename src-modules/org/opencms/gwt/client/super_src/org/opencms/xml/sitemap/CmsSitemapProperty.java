
package org.opencms.xml.sitemap;

/**
 * An enum containing keys for sitemap properties.<p>
 * 
 *  @author Georg Westenberger
 *  
 *  @version $Revision: 1.1 $
 *  
 *  @since 8.0.0
 */
public enum CmsSitemapProperty {

    /** <code>navigation</code> property name. */
    navigation("navigation"),
    /** <code>sitemap</code> property name. */
    sitemap("sitemap"),
    /** <code>template</code> property name. */
    template("template"),
    /** <code>template-inherited</code> property name. */
    templateInherited("template-inherited");

    /** The name of the property. */
    private final String m_name;

    /**
     * Default constructor.<p>
     * 
     * @param name the name of the property
     */
    private CmsSitemapProperty(String name) {

        m_name = name;
    }

    /**
     * Returns the name of the property.<p>
     * 
     * @return the name of the property
     */
    public String getName() {

        return m_name;
    }

    /**
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {

        return m_name;
    }
}