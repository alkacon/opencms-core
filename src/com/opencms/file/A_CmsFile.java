package com.opencms.file;

import java.io.*;

/**
 * This abstract class describes a file in the Cms.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 1999/12/13 16:29:59 $
 */
public abstract class A_CmsFile extends A_CmsResource { 	

	/**
	 * Sets the content of this file.
	 * 
	 * @param value the content of this file.
	 */
    abstract public void setContents(byte[] value);
	
	/**
	 * Gets the content of this file.
	 * 
	 * @return the content of this file.
	 */
 	abstract public byte[] getContents();
	
	/**
	 * Gets the length of the content (filesize).
	 * 
	 * @return the length of the content.
	 */
    abstract public long getLength();
	
	/**
	 * Gets the file-extension.
	 * 
	 * @return the file extension. If this file has no extension, it returns 
	 * a empty string ("").
	 */
    abstract public String getExtension();
    
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
