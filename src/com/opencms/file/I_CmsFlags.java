package com.opencms.file;

/**
 * This interface declares some constants (flags), 
 * which are used in the database.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 1999/12/03 11:57:10 $
 */
public interface I_CmsFlags
{
	/**
	 * This flag is set for enabled entrys in the database.
	 * (GROUP_FLAGS for example)
	 */
	public static long C_FLAG_ENABLED = 0;
	
	/**
	 * This flag is set for disabled entrys in the database.
	 * (GROUP_FLAGS for example)
	 */
	public static long C_FLAG_DISABLED = 1;
}
