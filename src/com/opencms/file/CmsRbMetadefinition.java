package com.opencms.file;

import java.util.*;

import com.opencms.core.*;

/**
 * This class describes a resource broker for projects in the Cms.<BR/>
 * 
 * This class has package-visibility for security-reasons.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 1999/12/21 15:08:47 $
 */
public class CmsRbMetadefinition implements I_CmsRbMetadefinition {
	
    /**
     * The metadefinition access object which is required to access the
     * meta database.
     */
    private I_CmsAccessMetadefinition m_accessMetadefinition;
    
    /**
     * Constructor, creates a new Cms Project Resource Broker.
     * 
     * @param accessProject The project access object.
     */
    public CmsRbMetadefinition(I_CmsAccessMetadefinition accessMetadefinition) {
        m_accessMetadefinition = accessMetadefinition;
    }
	
	/**
	 * Reads a metadefinition for the given resource type.
	 * 
	 * @param name The name of the metadefinition to read.
	 * @param type The resource type for which the metadefinition is valid.
	 * 
	 * @return metadefinition The metadefinition that corresponds to the overgiven
	 * arguments - or null if there is no valid metadefinition.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public A_CmsMetadefinition readMetadefinition(String name, int type)
		throws CmsException {
		return(m_accessMetadefinition.readMetadefinition(name, type));
	}
	
	/**
	 * Reads all metadefinitions for the given resource type.
	 * 
	 * @param resourcetype The resource type to read the metadefinitions for.
	 * 
	 * @return metadefinitions A Vector with metadefefinitions for the resource type.
	 * The Vector is maybe empty.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */	
	public Vector readAllMetadefinitions(int resourcetype)
		throws CmsException {
		return(m_accessMetadefinition.readAllMetadefinitions(resourcetype));
	}
	
	/**
	 * Reads all metadefinitions for the given resource type.
	 * 
	 * @param resourcetype The resource type to read the metadefinitions for.
	 * @param type The type of the metadefinition (normal|mandatory|optional).
	 * 
	 * @return metadefinitions A Vector with metadefefinitions for the resource type.
	 * The Vector is maybe empty.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */	
	public Vector readAllMetadefinitions(int resourcetype, int type)
		throws CmsException {
		return(m_accessMetadefinition.readAllMetadefinitions(resourcetype, type));
	}

	/**
	 * Creates the metadefinition for the resource type.<BR/>
	 * 
	 * Only the admin can do this.
	 * 
	 * @param name The name of the metadefinition to overwrite.
	 * @param resourcetype The resource-type for the metadefinition.
	 * @param type The type of the metadefinition (normal|mandatory|optional)
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public A_CmsMetadefinition createMetadefinition(String name, int resourcetype, 
													  int type)
		throws CmsException {
		return(m_accessMetadefinition.createMetadefinition(name, resourcetype, type));
	}
		
	/**
	 * Delete the metadefinition for the resource type.<BR/>
	 * 
	 * Only the admin can do this.
	 * 
	 * @param metadef The metadef to be deleted.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void deleteMetadefinition(A_CmsMetadefinition metadef)
		throws CmsException {
		m_accessMetadefinition.deleteMetadefinition(metadef);
	}
	
	/**
	 * Updates the metadefinition for the resource type.<BR/>
	 * 
	 * Only the admin can do this.
	 * 
	 * @param metadef The metadef to be deleted.
	 * 
	 * @return The metadefinition, that was written.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public A_CmsMetadefinition writeMetadefinition(A_CmsMetadefinition metadef)
		throws CmsException {
		return(m_accessMetadefinition.writeMetadefinition(metadef));
	}
	
	// now the stuff for metainformations

	/**
	 * Returns a Metainformation of a file or folder.
	 * 
	 * @param resource The resource of which the Metainformation has to be read.
	 * @param meta The Metadefinition-name of which the Metainformation has to be read.
	 * 
	 * @return metainfo The metainfo as string.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public String readMetainformation(A_CmsResource resource, String meta)
		throws CmsException {
		return(m_accessMetadefinition.readMetainformation(resource, meta));
	}	

	/**
	 * Writes a Metainformation for a file or folder.
	 * 
	 * @param resource The resource of which the Metainformation has to be read.
	 * @param meta The Metadefinition-name of which the Metainformation has to be set.
	 * @param value The value for the metainfo to be set.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeMetainformation(A_CmsResource resource, String meta,
											  String value)
		throws CmsException {
		m_accessMetadefinition.writeMetainformation(resource, meta, value);
	}

	/**
	 * Writes a couple of Metainformation for a file or folder.
	 * 
	 * @param resource The resource of which the Metainformation has to be read.
	 * @param metainfos A Hashtable with Metadefinition- metainfo-pairs as strings.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeMetainformations(A_CmsResource resource, Hashtable metainfos)
		throws CmsException {
		m_accessMetadefinition.writeMetainformations(resource, metainfos);
	}

	/**
	 * Returns a list of all Metainformations of a file or folder.
	 * 
	 * @param resource The resource of which the Metainformation has to be read.
	 * 
	 * @return Vector of Metainformation as Strings.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public Hashtable readAllMetainformations(A_CmsResource resource)
		throws CmsException {
		return(m_accessMetadefinition.readAllMetainformations(resource));
	}
	
	/**
	 * Deletes all Metainformation for a file or folder.
	 * 
	 * @param resource The resource of which the Metainformation has to be read.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteAllMetainformations(A_CmsResource resource)
		throws CmsException {
		m_accessMetadefinition.deleteAllMetainformations(resource);
	}

	/**
	 * Deletes a Metainformation for a file or folder.
	 * 
	 * @param resource The resource of which the Metainformation has to be read.
	 * @param meta The Metadefinition-name of which the Metainformation has to be set.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteMetainformation(A_CmsResource resource, String meta)
		throws CmsException {
		m_accessMetadefinition.deleteMetainformation(resource, meta);
	}
}
