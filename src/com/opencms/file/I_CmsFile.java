package com.opencms.file;

import java.io.*;

/**
 * This interface describes a file in the Cms.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 1999/12/03 11:57:10 $
 */
public interface I_CmsFile extends I_CmsResource { 	

	/**
	 * Sets the content of this file.
	 * 
	 * @param value the content of this file.
	 */
    public void setContents(byte[] value);
	
	/**
	 * Gets the content of this file.
	 * 
	 * @return the content of this file.
	 */
 	public byte[] getContents();
	
	/**
	 * Gets the length of the content (filesize).
	 * 
	 * @return the length of the content.
	 */
    public long getLength();
	
	/**
	 * Gets the file-extension.
	 * 
	 * @return the file extension. If this file has no extension, it returns 
	 * a empty string ("").
	 */
    public String getExtension();
    
	// the following methods are not used, because the functionality is handled by
	// a I_CmsObjectBase:
	/*
	public boolean delete();
    public boolean exists();
    public boolean mkdir();
    public boolean mkdirs();
    // What about this
	// No! Use CmsObject!
    public boolean renameTo( I_CmsFile dest );
    public String[] list();
	*/
}
