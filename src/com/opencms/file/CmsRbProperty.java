package com.opencms.file;

import java.io.*;

import com.opencms.core.*;

/**
 * Implementatrion of the I_CmsRbProperty interface, 
 * implementing a resource broker for propertys in the Cms.<BR/>
 * Only the system can access propertys. Propertys are for internal use
 * only. A property is a serializable object.
 * 
 * This class has package-visibility for security-reasons.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.5 $ $Date: 1999/12/21 15:08:47 $
 */
public class CmsRbProperty implements I_CmsRbProperty  {
	
    /**
     * The property access object which is required to access the
     * property database.
     */
    private I_CmsAccessProperty m_accessProperty;
    
    /**
     * Constructor, creates a new Cms Property Resource Broker.
     * 
     * @param AccessProperty The property access object.
     */
    public CmsRbProperty(I_CmsAccessProperty accessProperty)
    {
        m_accessProperty=accessProperty;
    }
        
     /**
	 * Creates a new a serializable object to the propertys.
	 * 
	 * @param name The name of the property.
	 * @param object The property-object.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public Serializable addProperty(String name, Serializable object)
         throws CmsException {
               
         return m_accessProperty.addProperty(name,object);
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
	 * @return object The property-object.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public Serializable writeProperty(String name, Serializable object)
        throws  CmsException {
         
        return m_accessProperty.writeProperty(name,object);
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
