package com.opencms.file;

/**
 * This interface describes a metainformation in the Cms.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.2 $ $Date: 1999/12/06 09:39:22 $
 */
public interface I_CmsMetaInformation
{
	/**
	 * Returns the value of this metainformation.
	 * 
	 * @return the value of this metainformation.
	 */
	public String getValue();
	
	/**
	 * Returns the id of a metainformation. This method has the package-visibility.
	 * 
	 * @return id The id of this metainformation.
	 */
	long getID();	
		
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
