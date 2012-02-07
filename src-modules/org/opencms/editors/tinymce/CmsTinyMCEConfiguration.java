package org.opencms.editors.tinymce;

import javax.servlet.http.HttpSession;


/**
 * The configuration is passed to the external TinyMCE Javascript configuration file.<p>
 * 
 * This is done by using the current users session and storing an instance of the configuration object in the session.<p>
 * 
 */

public class CmsTinyMCEConfiguration {

	    /** Session attribute name to use when storing an object instance in the session. */
	    public static final String SESSION_ATTRIBUTE = "__tinymceconfig";

	    /** The path of the edited resource. */
	    private String m_resourcePath;

	    /** The URI of the CSS style sheet to use for the editor. */
	    private String m_uriStyleSheet;

	    /**
	     * Constructor without parameters.<p>
	     */
	    public CmsTinyMCEConfiguration() {

	        // nothing to do here
	    }

	    /**
	     * Returns the configuration object stored in the given session.<p>
	     * 
	     * Before returning the found object, the attribute is removed from the given session.<p>
	     * 
	     * @param session the session containing the configuration
	     * @return the configuration object stored in the given session or a new configuration object if no object was found
	     */
	    public static CmsTinyMCEConfiguration getConfiguration(HttpSession session) {

	        Object o = session.getAttribute(SESSION_ATTRIBUTE);
	        if (o != null && o instanceof CmsTinyMCEConfiguration) {
	            session.removeAttribute(SESSION_ATTRIBUTE);
	            return (CmsTinyMCEConfiguration)o;
	        }
	        return new CmsTinyMCEConfiguration();
	    }

	    /**
	     * Returns the path of the edited resource.<p>
	     * 
	     * @return the path of the edited resource
	     */
	    public String getResourcePath() {

	        return m_resourcePath;
	    }

	    /**
	     * Returns the URI of the CSS style sheet to use for the editor.<p>
	     * 
	     * @return the URI of the CSS style sheet to use for the editor
	     */
	    public String getUriStyleSheet() {

	        return m_uriStyleSheet;
	    }

	    /**
	     * Stores the configuration in the given session.<p>
	     * 
	     * @param session the session to store the configuration
	     */
	    public void setConfiguration(HttpSession session) {

	        session.setAttribute(SESSION_ATTRIBUTE, this);
	    }

	    /**
	     * Sets the path of the edited resource.<p>
	     * 
	     * @param resourcePath the path of the edited resource
	     */
	    public void setResourcePath(String resourcePath) {

	        m_resourcePath = resourcePath;
	    }

	    /**
	     * Sets the URI of the CSS style sheet to use for the editor.<p>
	     * 
	     * @param uriStyleSheet the URI of the CSS style sheet to use for the editor
	     */
	    public void setUriStyleSheet(String uriStyleSheet) {

	        m_uriStyleSheet = uriStyleSheet;
	    }
}
