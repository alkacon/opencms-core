package com.opencms.core;

/**
 * This exception is thrown for security reasons in the Cms.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.14 $ $Date: 2000/01/13 17:39:03 $
 */
public class CmsException extends Exception {
	
    /**
     * Stores the error code of the CmsException.
     */
    private int m_Type = 0;
	
    /**
     * Stores a forwared exception.
     */
    private Exception m_Exception = null;
	
    /**
     * Definition of error code for unknown exception.
     */
	public final static int C_UNKNOWN_EXCEPTION = 0;
    
    /**
    * Definition of error code for access denied exception.
    */
	public final static int C_NO_ACCESS = 1;
    
    /**
    * Definition of error code for not found exception.
    */
	public final static int C_NOT_FOUND = 2;
    
    /**
    * Definition of error code for bad name exception.
    */
	public final static int C_BAD_NAME = 3;
    
    /**
    * Definition of error code for sql exception.
    */
	public final static int C_SQL_ERROR = 4;
    
    /**
    * Definition of error code for not empty exception.
    */
	public final static int C_NOT_EMPTY = 5;
    
    /**
    * Definition of error code for no admin exception.
    */
	public final static int C_NOT_ADMIN = 6;
    
    /**
    * Definition of error code for serialization exception.
    */
	public final static int C_SERIALIZATION = 7;
    
    /**
    * Definition of error code for no group exception.
    */
    public final static int C_NO_GROUP = 8;
    
    /**
    * Definition of error code for group not empty exception.
    */ 
    public final static int C_GROUP_NOT_EMPTY = 9;
    
    /**
    * Definition of error code for no user exception.
    */
    public final static int C_NO_USER= 10;
    
    /**
    * Definition of error code for no default group exception.
    */
    public final static int C_NO_DEFAULT_GROUP= 11;

    /**
    * Definition of error code for file exists exception.
    */
    public final static int C_FILE_EXISTS= 12;
    
    /**
    * Definition of error code for locked resource.
    */
    public final static int C_LOCKED= 13;

     /**
    * Definition of error code filesystem error.
    */
    public final static int C_FILESYSTEM_ERROR= 14;
    
    /**
    * Definition of error code internal file.
    */
    public final static int C_INTERNAL_FILE= 15;
    
    /**
    * Definition of error code mandatory-metainfo.
    */
    public final static int C_MANDATORY_METAINFO= 16;

    /**
    * Definition of error code service unavailable.
    */
    public final static int C_SERVICE_UNAVAILABLE= 17;
    
    
	public final static String C_EXTXT[] = {
							"Unknown exception ",
							"Access denied ",
							"Not found ",
							"Bad name ",
							"Sql exception ",
							"Folder not empty ",
							"Admin access required ",
							"Serialization/Deserialization failed ",
                            "Unknown User Group ",
                            "Group not empty ",
                            "Unknown User ",
                            "No removal from Default Group ",
                            "Resource already exists",
                            "Locked Resource ",
                            "Filesystem exception ",
                            "Internal use only ",
                            "Metainformation is mandatory ",
                            "Service unavailable ",
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
	 * Contructs a CmsException with reserved error code
	 * <p>
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
	 * @param e Forwaarded general exception
	 */
	public CmsException(String s, Exception e){
		super(s);
		m_Exception = e;
	}
	
	 /** 
	 * Creates a CmsException with reserved error code and a forwarded other exception
	 * <p>
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
    
    /**
     * Overwrites the standart toString method.
     */
    public String toString(){
         StringBuffer output=new StringBuffer();
         output.append("[CmsException]: ");
         output.append(m_Type+" ");
         output.append(CmsException.C_EXTXT[m_Type]+"\n");
         output.append("Detailed Error: ");
         output.append(super.getMessage());

         return output.toString();
    }
  
}
