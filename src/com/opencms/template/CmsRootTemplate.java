package com.opencms.template;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/CmsRootTemplate.java,v $
 * Date   : $Date: 2001/01/11 10:47:52 $
 * Version: $Revision: 1.21 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import com.opencms.core.*;
import com.opencms.launcher.*;
import com.opencms.file.*;
import javax.servlet.http.*;
import java.util.*;

/**
 * Represents an "empty" page or screen that should be filled with
 * the content of a master template.
 * <P>
 * Every launcher uses this canonical root the invoke the output
 * generation of the master template class to be used.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.21 $ $Date: 2001/01/11 10:47:52 $
 */
public class CmsRootTemplate implements I_CmsLogChannels, I_CmsConstants {
	
	/**
	 * Gets the processed content of the requested master template by calling
	 * the given template class.
	 * <P>
	 * If the result is cacheable, the complete output will be stored
	 * in the template cache for later re-use.
	 * 
	 * @param cms CmsObject object for accessing system resources.
	 * @param templateClass Instance of the template class to be called.
	 * @param masterTemplate CmsFile object of the master template file.
	 * @param cache templateCache to be used.
	 * @param parameters Hashtable with all template class parameters.
	 * 
	 * @return Byte array containing the results of the master template.
	 */
	public byte[] getMasterTemplate(CmsObject cms, I_CmsTemplate templateClass, CmsFile masterTemplate,
			com.opencms.launcher.I_CmsTemplateCache cache, Hashtable parameters) throws CmsException {
		
		byte[] result;
		//String cacheKey = cms.getUrl();
		Object cacheKey = templateClass.getKey(cms, masterTemplate.getAbsolutePath(), parameters, null);
	
		boolean cacheable = templateClass.isCacheable(cms, masterTemplate.getAbsolutePath(), C_ROOT_TEMPLATE_NAME, parameters, null);
				
		if(cacheable
				&& cache.has(cacheKey) 
				&& ! templateClass.shouldReload(cms, masterTemplate.getAbsolutePath(), C_ROOT_TEMPLATE_NAME, parameters, null)) {
			result = cache.get(cacheKey);
		} else {
			try {
				result = templateClass.getContent(cms, masterTemplate.getAbsolutePath(), C_ROOT_TEMPLATE_NAME, parameters);
		  } catch(CmsException e) {
				cache.clearCache(cacheKey);
				if(A_OpenCms.isLogging()) {
					A_OpenCms.log(C_OPENCMS_INFO, "[CmsRootTemplate] Could not get contents of master template " + masterTemplate.getName());
				}
				throw e;
			}
			if(cacheable) {
				cache.put(cacheKey, result);
			}
	   }         

		I_CmsResponse resp = cms.getRequestContext().getResponse();

		// was there already a cache-control header set?
		if(!resp.containsHeader("Cache-Control")) {
			// only if the resource is cacheable and if the current project is online,
			// then the browser may cache the resource
			if( cacheable && cms.getRequestContext().currentProject().equals(cms.onlineProject())) {
			    // set max-age to 5 minutes. In this time a proxy may cache this content.
			    resp.setHeader("Cache-Control", "max-age=300");
			} else {
				// set the http-header to pragma no-cache.
				//HTTP 1.1
				resp.setHeader("Cache-Control", "no-cache");
				//HTTP 1.0
				resp.setHeader("Pragma","no-cache");		
			}
		}
		
		return result;
	}
}
