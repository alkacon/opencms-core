package com.opencms.file;

/**
 * This class describes a metadefinition in the Cms.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 1999/12/17 14:35:31 $
 */
public class CmsMetadefinition extends A_CmsMetadefinition {
	/**
	 * The name of this metadefinition.
	 */
	private String m_name = null;
	
	/**
	 * The resource-type for this metadefinition.
	 */
	private int m_resourceType;
	
	/**
	 * The type of this metadefinition.
	 */
	private int m_metadefinitionType;
		
	/**
	 * The id of this metadefinition.
	 */
	private int m_id;
	
	/**
	 * Creates a new CmsMetadefinition.
	 * 
	 * @param id The id of the metadefinition.
	 * @param name The name of the metadefinition.
	 * @param resourcetype The type of the resource for this metadefinition.
	 * @param type The type of the metadefinition (e.g. mandatory)
	 */
	CmsMetadefinition(int id, String name, int resourcetype, int type) {
		m_id = id;
		m_name = name;
		m_resourceType = resourcetype;
		m_metadefinitionType = type;
	}

	/**
	 * Returns the name of this metadefinition.
	 * 
	 * @return name The name of the metadefinition.
	 */
	public String getName() {
		return m_name;
	}
	
	/**
	 * Returns the id of a metadefinition. This method has the package-visibility.
	 * 
	 * @return id The id of this metadefinition.
	 */
	int getId() {
		return m_id;
	}
	
	/**
	 * Gets the resourcetype for this metadefinition.
	 * 
	 * @return the resourcetype of this metadefinition.
	 */
	public int getType() {
		return m_resourceType;
	}


	/**
	 * Gets the type for this metadefinition.
	 * The type may be C_METADEF_TYPE_NORMAL, C_METADEF_TYPE_OPTIONAL or
	 * C_METADEF_TYPE_MANDATORY.
	 * 
	 * @return the type of this metadefinition.
	 */
	public int getMetadefType() {
		return m_metadefinitionType;
	}
	
	/**
	 * Returns a string-representation for this object.
	 * This can be used for debugging.
	 * 
	 * @return string-representation for this object.
	 */
	public String toString() {
        StringBuffer output=new StringBuffer();
        output.append("[Metadefinition]:");
        output.append(m_name);
        output.append(" , Id=");
        output.append(m_id);
        return output.toString();
	}
	
	/**
	 * Compares the overgiven object with this object.
	 * 
	 * @return true, if the object is identically else it returns false.
	 */
	public boolean equals(Object obj) {
        boolean equal=false;
        // check if the object is a CmsMetadefinition object
        if (obj instanceof CmsMetadefinition) {
            // same ID than the current project?
            if (((CmsMetadefinition)obj).getId() == m_id){
                equal = true;
            }
        }
        return equal;
	}

}
