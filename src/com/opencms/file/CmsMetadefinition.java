package com.opencms.file;

/**
 * This class describes a metadefinition in the Cms.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.2 $ $Date: 2000/01/11 10:24:30 $
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
	 * Sets the type for this metadefinition.
	 * The type may be C_METADEF_TYPE_NORMAL, C_METADEF_TYPE_OPTIONAL or
	 * C_METADEF_TYPE_MANDATORY.
	 * 
	 * @param type The new type fot this metadefinition.
	 */
	public void setMetadefType(int type) {
		m_metadefinitionType = type;
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
        output.append(" , ResourceType=");
        output.append(getType());
        output.append(" , MetadefType=");
        output.append(getMetadefType());
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
