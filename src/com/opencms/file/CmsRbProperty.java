package com.opencms.file;

import java.io.*;

import com.opencms.core.*;

/**
 * Implementatrion of the I_CmsRbProperty interface, 
 * implementing a resource broker for propertys in the Cms.<BR/>
 * Only the system can access propertys. Propertys are for internal use
 * only. A property is a serializable object.
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.2 $ $Date: 1999/12/15 16:43:21 $
 */
public class CmsRbProperty extends A_CmsRbProperty  {
	
    /**
     * The property access object which is required to access the
     * property database.
     */
    private A_CmsAccessProperty m_accessProperty;
    
    /**
     * Constructor, creates a new Cms Property Resource Broker.
     * 
     * @param AccessProperty The property access object.
     */
    public CmsRbProperty(A_CmsAccessProperty accessProperty)
    {
        m_accessProperty=accessProperty;
    }
    
	/**
	 * Reads a serializable object from the propertys.
	 * 
	 * @param name The name of the property.
	 * 
	 * @return object The property-object.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public Serializable readProperty(String name) 
        throws CmsException {
        
        return m_accessProperty.readProperty(name);
     }

	/**
	 * Writes a serializable object to the propertys.
	 * 
	 * @param name The name of the property.
	 * @param object The property-object.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 * @exception CmsDuplicateKeyException Throws CmsDuplicateKeyException if something goes wrong.
	 */
	 public void writeProperty(String name, Serializable object)
        throws CmsDuplicateKeyException, CmsException {
         
        m_accessProperty.writeProperty(name,object);
     }

	/**
	 * Deletes a serializable object from the propertys.
	 * 
	 * @param name The name of the property.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public void deleteProperty(String name)
        throws CmsException {
        
        m_accessProperty.deleteProperty(name);
    }
}
