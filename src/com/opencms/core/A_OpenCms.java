package com.opencms.core;

/**
* Abstract class for the main class of the OpenCms system. 
* <p>
* Currently the main class does <b>not</b> extend this class
* and it is only used for logging purposes.
* 
* @author Alexander Lucas
* @version $Revision: 1.1 $ $Date: 2000/01/13 13:42:44 $  
* 
*/
public class A_OpenCms implements I_CmsLogChannels {

    /**
     * Checks if the system logging is active.
     * @return <code>true</code> if the logging is active, <code>false</code> otherwise.
     */
    public static boolean isLogging() {
        return true;
    }
    
    /**
     * Logs a message into the OpenCms logfile
     * @param channel The channel the message is logged into
     * @message The message to be logged,
     */
    public static void log(String channel, String message) {
        System.err.println(message);
    }
}
