package com.opencms.core;

public class CmsDuplicateKeyException extends Exception {
	
    /** 
	 * Throws a simple CmsDuplicateKeyException
	 */
	public CmsDuplicateKeyException()
	{
		super();
	}	

	/** 
	 * Throws a  CmsDuplicateKeyException
	 * 
	 * @param s Exception description 
	 */
	public CmsDuplicateKeyException(String s)
	{
		super(s);
	}	
}
