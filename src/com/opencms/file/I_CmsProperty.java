package com.opencms.file;

import java.io.*;

/**
 * This interface describes a property in the Cms.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 1999/12/06 13:57:29 $
 */
public interface I_CmsProperty
{
	/**
	 * Returns the serializable object.
	 * 
	 * @return the serializable object.
	 */
	public Serializable getValue();
	
	/**
	 * Sets the serializable object.
	 * 
	 * @param value The serializeable object, to be set.
	 */
	public void setValue(Serializable value);
	
	/**
	 * Returns the name of the property.
	 * 
	 * @return the name of the property.
	 */
	public String getName();

	/**
	 * Returns the id of a property. This method has the package-visibility.
	 * 
	 * @return id The id of this property.
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
