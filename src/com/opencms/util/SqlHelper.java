package com.opencms.util;

import com.opencms.core.*;
import java.sql.*;

/**
 * This is a helper class for sql queries.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.5 $ $Date: 2000/02/10 17:13:41 $
 */
public class SqlHelper {
	
	/**
	 * The number of maximum retries to read the timestamp
	 */
	private static final int C_MAX_RETRIES = 10;
	
	/**
	 * This method tries to get the timestamp several times, because there 
	 * is a timing-problem in the actual mysql-driver.
	 * 
	 * @param result The resultset to get the stamp from.
	 * @param column The column to read the timestamp from.
	 * @return the Timestamp.
	 * @exception Throws Exception, if something goes wrong.
	 */
	public static final Timestamp getTimestamp( ResultSet result, String column ) 
		throws Exception {
		int i = 0;
		for( ; ; ) {
			try {
				return(result.getTimestamp(column));
			} catch(Exception exc) {
				i++;
				if( i >= C_MAX_RETRIES ) {
					throw exc;
				} else {
					A_OpenCms.log(I_CmsLogChannels.C_MODULE_INFO, "Trying to get timestamp " + column + " #" + i);					
				}
			}
		}
	}
}
