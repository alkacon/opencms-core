package com.opencms.file;

import java.util.*;
import java.io.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.opencms.core.*;
import com.opencms.template.*;

/**
 * This interface describes the CMS database export.<BR/>
 * Exports Groups, Users and Files from database into XML file
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Michaela Schleich
 * @version $Revision: 1.1 $ $Date: 2000/02/11 19:01:54 $
 */

interface I_CmsDbExport {

	/**
	 * 
	 * decides what to export;
	 * 
	 */
	public void export()
		throws CmsException, IOException, Exception;
}