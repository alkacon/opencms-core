package com.opencms.core;

/**
 * Common interface for OpenCms logging.
 * Constants used for logging purposes are defined here.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.1 $ $Date: 2000/01/13 13:40:38 $
 */
public interface I_CmsLogChannels { 
    /** Debugging messages */
    public static final String C_OPENCMS_DEBUG = "opencms_debug";

    /** Informational messages */
    public static final String C_OPENCMS_INFO = "open_info";    
    
    /** Critical messages that stop further processing */
    public static final String C_OPENCMS_CRITICAL = "opencms_critical";    
    
    
    /** Debugging messages */
    public static final String C_MODULE_DEBUG = "module_debug";

    /** Informational messages */
    public static final String C_MODULE_INFO = "module_info";    
    
    /** Critical messages that stop further processing */
    public static final String C_MODULE_CRITICAL = "module_critical";         
}
