package com.opencms.file;

/**
 * This interface describes a metadefinition in the Cms.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.2 $ $Date: 1999/12/06 09:39:22 $
 */
public interface I_CmsMetaDefinition
{

	/**
	 * This constant signs a normal "classic" metadefinition.
	 */
	static final int C_METADEF_TYPE_NORMAL		= 0;

	/**
	 * This constant signs a optional metadefinition.
	 * These metadefinitions will be shown at creation-time for a resource.
	 * They may be filled out by the user.
	 */
	static final int C_METADEF_TYPE_OPTIONAL	= 1;

	/**
	 * This constant signs a mandatory metadefinition.
	 * These metadefinitions will be shown at creation-time for a resource.
	 * They must be filled out by the user, else the resource will not be created.
	 */
	static final int C_METADEF_TYPE_MANDATORY	= 2;

	/**
	 * Returns the name of this metadefinition.
	 * 
	 * @return name The name of the metadefinition.
	 */
	public String getName();
	
	/**
	 * Returns the id of a metadefinition. This method has the package-visibility.
	 * 
	 * @return id The id of this metadefinition.
	 */
	long getID();
	
	/**
	 * Sets the resourcetype for this metadefinition.
	 * 
	 * @param resourcetype the new type for this metadefinition.
	 */
	public void setType(I_CmsResourceType type);
	
	/**
	 * Gets the resourcetype for this metadefinition.
	 * 
	 * @return the resourcetype of this metadefinition.
	 */
	public I_CmsResourceType getType();

	/**
	 * Sets the type for this metadefinition.
	 * The type may be C_METADEF_TYPE_NORMAL, C_METADEF_TYPE_OPTIONAL or
	 * C_METADEF_TYPE_MANDATORY.
	 * 
	 * @param type the new type for this metadefinition.
	 */
	public void setMetadefType(int metadefType);
	
	/**
	 * Gets the type for this metadefinition.
	 * The type may be C_METADEF_TYPE_NORMAL, C_METADEF_TYPE_OPTIONAL or
	 * C_METADEF_TYPE_MANDATORY.
	 * 
	 * @return the type of this metadefinition.
	 */
	public int getMetadefType();
	
	/**
	 * Returns a string-representation for this object.
	 * This can be used for debugging.
	 * 
	 * @return string-representation for this object.
	 */
	public String toString();
	
	/**
	 * Compares the overgiven object with this object.
	 * 
	 * @return true, if the object is identically else it returns false.
	 */
    public boolean equals(Object obj);

	/**
	 * Returns the hashcode for this object.
	 * 
	 * @return the hashcode for this object.
	 */
    public int hashCode();    

}
