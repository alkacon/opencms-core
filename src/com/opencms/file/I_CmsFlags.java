package com.opencms.file;

/**
 * This interface declares some constants (flags), 
 * which are used in the database.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.3 $ $Date: 1999/12/07 17:25:04 $
 */
public interface I_CmsFlags
{
	/**
	 * This flag is set for enabled entrys in the database.
	 * (GROUP_FLAGS for example)
	 */
	public static final int C_FLAG_ENABLED = 0;
	
	/**
	 * This flag is set for disabled entrys in the database.
	 * (GROUP_FLAGS for example)
	 */
	public static final int C_FLAG_DISABLED = 1;
}
