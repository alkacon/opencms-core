package com.opencms.core;

/**
 * This exception is thrown for security reasons in the Cms.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.3 $ $Date: 1999/12/14 18:02:13 $
 */
public class CmsException extends Exception {
	
    private int m_Type = 0;
	
    private Exception m_Exception = null;
	
	public final static int C_UNKNOWN_EXCEPTION = 0;
	public final static int C_NO_ACCESS = 1;
	public final static int C_NOT_FOUND = 2;
	public final static int C_BAD_NAME = 3;
	public final static int C_SQL_ERROR = 4;
	public final static int C_NOT_EMPTY = 5;
	public final static int C_NOT_ADMIN = 6;
	public final static int C_SERIALIZATION = 7;
    public final static int C_NO_GROUP = 8;

	public final static String C_EXTXT[] = {
							"Unknown exception",
							"Access denied: ",
							"Not found: ",
							"Bad name: ",
							"Sql exception: ",
							"Folder not empty: ",
							"Admin access required: ",
							"Serialization/Deserialization failed:",
                            "Unknown User Group:",
							};
    
	/** 
	 * Constructs a simple CmsException
	 */
	public CmsException() {
		super();
	}	
	
	 /** 
	 * Constructs a CmsException with a specified description.
	 * 
	 * @param s Exception description 
	 */
	public CmsException(String s) {
		super(s);
	}
	
	 /** 
	 * Contructs a  CmsException with reserved error code
	 * <p>
	 * Codes are: 
	 * <ul>
	 * <li> <b>0:</b> Unknown exception </li>
	 * <li> <b>1:</b> Access denied </li>
	 * <li> <b>2:</b> Not found </li>
	 * <li> <b>3:</b> Bad name </li>
	 * <li> <b>4:</b> Sql exception </li>
	 * <li> <b>5:</b> Folder not empty </li>
	 * <li> <b>6:</b> Admin access required </li>
	 * <li> <b>7:</b> No Base64 Code </li>
	 * </ul>
	 * 
	 * @param i Exception code
	 */
	public CmsException(int i) {
		super("CmsException ID: " + i);
		m_Type = i;
	}

	 /** 
	 * Constructs a  CmsException with reserved error code and additional information
	 * <p>
	 * Codes are: 
	 * <ul>
	 * <li> <b>0:</b> Unknown exception </li>
	 * <li> <b>1:</b> Access denied </li>
	 * <li> <b>2:</b> Not found </li>
	 * <li> <b>3:</b> Bad name </li>
	 * <li> <b>4:</b> Sql exception </li>
	 * <li> <b>5:</b> Folder not empty </li>
	 * <li> <b>6:</b> Admin access required </li>
	 * <li> <b>7:</b> No Base64 Code </li>
	 * </ul>
	 * 
	 * @param s Exception description
	 * @param i Exception code
	 */
	public CmsException(String s, int i) {
		super(s);
		m_Type = i;
	}	
	
	/** 
	 * Construtcs a CmsException  with a detail message and a forwarded other exception
	 * 
	 * @param s Exception description 
	 * @param e Forawarded general exception
	 */
	public CmsException(String s, Exception e){
		super(s);
		m_Exception = e;
	}
	
	 /** 
	 * Creates a CmsException with reserved error code and a forwarded other exception
	 * <p>
	 * Codes are: 
	 * <ul>
	 * <li> <b>0:</b> Unknown exception </li>
	 * <li> <b>1:</b> Access denied </li>
	 * <li> <b>2:</b> Not found </li>
	 * <li> <b>3:</b> Bad name </li>
	 * <li> <b>4:</b> Sql exception </li>
	 * <li> <b>5:</b> Folder not empty </li>
	 * <li> <b>6:</b> Admin access required </li>
	 * <li> <b>7:</b> No Base64 Code </li>
	 * </ul>
	 * 
	 * 
	 * @param i Exception code
	 * @param e Forawarded general exception
	 */
	public CmsException(int i, Exception e)	{
		super("CmsException ID: " + i);
		m_Type = i;
		m_Exception = e;
	}

	 /** 
	 * Creates a CmsException with reserved error code, a forwarded other exception and a detail message
	 * <p>
	 * Codes are: 
	 * <ul>
	 * <li> <b>0:</b> Unknown exception </li>
	 * <li> <b>1:</b> Access denied </li>
	 * <li> <b>2:</b> Not found </li>
	 * <li> <b>3:</b> Bad name </li>
	 * <li> <b>4:</b> Sql exception </li>
	 * <li> <b>5:</b> Folder not empty </li>
	 * <li> <b>6:</b> Admin access required </li>
	 * <li> <b>7:</b> No Base64 Code </li>
	 * </ul>
	 * 
	 * @param s Exception description 
	 * @param i Exception code
	 * @param e Forawarded general exception
	 */
	public CmsException(String s, int i, Exception e) {
		super(s);
		m_Type = i;
		m_Exception = e;
	}		
	
	/**
	 * Get the type of the CmsException.
	 * 
	 * @return Type of CmsException
	 */
	public int getType() {
		return m_Type;
	}
	
	/**
	 * Set an exception value.
	 * 
	 * @param value Exception
	 */
	public void setException(Exception value){
		m_Exception = value;
	}
	
	/**
	 * Get the exeption.
	 * 
	 * @return Exception.
	 */
	public Exception getException()	{
		return m_Exception;
	}
  
}
