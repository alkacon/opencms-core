package com.opencms.template;

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
 * @version $Revision: 1.5 $ $Date: 2000/02/11 18:46:20 $
 */
public class CmsRootTemplate implements I_CmsLogChannels {
    
    /**
     * Gets the processed content of the requested master template by calling
     * the given template class.
     * <P>
     * If the result is cacheable, the complete output will be stored
     * in the template cache for later re-use.
     * 
     * @param cms A_CmsObject object for accessing system resources.
     * @param templateClass Instance of the template class to be called.
     * @param masterTemplate CmsFile object of the master template file.
     * @param cache templateCache to be used.
     * @param parameters Hashtable with all template class parameters.
     * 
     * @return Byte array containing the results of the master template.
     */
    public byte[] getMasterTemplate(A_CmsObject cms, I_CmsTemplate templateClass, CmsFile masterTemplate,
            com.opencms.launcher.I_CmsTemplateCache cache, Hashtable parameters) throws CmsException {
        
        byte[] result;
        //String cacheKey = cms.getUrl();
        Object cacheKey = templateClass.getKey(cms, masterTemplate.getAbsolutePath(), parameters, null);
        
        if(templateClass.isCacheable(cms, masterTemplate.getAbsolutePath(), null, parameters, null)
                && cache.has(cacheKey) 
                && ! templateClass.shouldReload(cms, masterTemplate.getAbsolutePath(), null, parameters, null)) {
            result = cache.get(cacheKey);
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_INFO, "[CmsRootTemplate] page " + masterTemplate.getAbsolutePath() + " was read from cache.");                                                                 
            }
        } else {
            try {
                result = templateClass.getContent(cms, masterTemplate.getAbsolutePath(), null, parameters);
            } catch(CmsException e) {
                cache.clearCache(cacheKey);
                if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_INFO, "[CmsRootTemplate] Could not get contents of master template " + masterTemplate.getName());
                }
                throw e;
            }
            if(templateClass.isCacheable(cms, masterTemplate.getAbsolutePath(), null, parameters, null)) {
                cache.put(cacheKey, result);
            }
        }         
        return result;
    }
}
