
package org.opencms.i18n;

import java.util.LinkedHashMap;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A list based resource bundle that with increased visibility of some key methods.<p>
 * 
 * @since 8.0.1 
 * 
 * @see org.opencms.i18n.CmsResourceBundleLoader
 */
public class CmsListResourceBundle extends ListResourceBundle implements I_CmsResourceBundle {

    /** The locale to use. */
    protected Locale m_locale;

    /** The configured resource key / value pairs in a Map. */
    private Map<String, String> m_bundleMap;

    /** The configured resource key / value pairs as Objects. */
    private Object[][] m_bundleObjects;

    /**
     * Create a new list resource bundle for the XML.<p>
     */
    public CmsListResourceBundle() {

        m_bundleMap = new LinkedHashMap<String, String>();
    }

    /**
     * Create a new list resource bundle as copy of an existing one.<p> 
     * 
     * @param bundleMap the resource bundle map
     * @param bundleObjects the resource bundle object to copy
     */
    private CmsListResourceBundle(Map<String, String> bundleMap, Object[][] bundleObjects) {

        m_bundleMap = bundleMap;
        m_bundleObjects = bundleObjects;
    }

    /**
     * Adds a message to this list bundle.<p>
     * 
     * Please note:
     * All additions after the initial call to {@link #getContents()} are ignored.<p>
     * 
     * @param key the message key
     * @param value the message itself
     */
    public void addMessage(String key, String value) {

        if (m_bundleMap != null) {
            m_bundleMap.put(key, value);
        }
    }

    /**
     * Returns a typed clone of this resource bundle.<p>
     * 
     * This is required in order to make sure the objects in the permanent cache of the 
     * list based resource bundles which are usually read from the XML are never changed.<p> 
     * 
     * @return a typed clone of this resource bundle
     */
    public CmsListResourceBundle getClone() {

        return new CmsListResourceBundle(m_bundleMap, m_bundleObjects);
    }

    /**
     * @see java.util.ListResourceBundle#getContents()
     */
    @Override
    public Object[][] getContents() {

        if ((m_bundleObjects == null) && (m_bundleMap != null)) {
            // fill object array based on map
            m_bundleObjects = new String[m_bundleMap.size()][2];
            int i = 0;
            for (Map.Entry<String, String> entry : m_bundleMap.entrySet()) {
                m_bundleObjects[i][0] = entry.getKey();
                m_bundleObjects[i][1] = entry.getValue();
                i++;
            }
            // remove the map, we don't need it anymore
            m_bundleMap = null;
        }
        return m_bundleObjects;
    }

    /**
     * @see java.util.ResourceBundle#getLocale()
     */
    @Override
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * Sets the locale used for this resource bundle.<p>
     * 
     * @param l the locale to set
     */
    public void setLocale(Locale l) {

        m_locale = l;
    }

    /**
     * @see java.util.ResourceBundle#setParent(java.util.ResourceBundle)
     */
    @Override
    public void setParent(ResourceBundle p) {

        super.setParent(p);
    }
}