package com.opencms.template;

import com.opencms.core.*;
import com.opencms.launcher.*;
import com.opencms.file.*;
import javax.servlet.http.*;
import java.util.*;

public class CmsRootTemplate implements I_CmsLogChannels {
	public byte[] getMasterTemplate(A_CmsObject cms, I_CmsTemplate templateClass, CmsFile masterTemplate, I_CmsTemplateCache cache, Hashtable parameters) throws CmsException {
        
        byte[] result;
        //String cacheKey = cms.getUrl();
        Object cacheKey = templateClass.getKey(cms, masterTemplate.getAbsolutePath(), parameters);
        
        if(templateClass.isCacheable(cms, masterTemplate.getAbsolutePath(), parameters)
                && cache.has(cacheKey) 
                && ! templateClass.shouldReload(cms, masterTemplate.getAbsolutePath(), parameters)) {
            result = cache.get(cacheKey);
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_INFO, "[CmsRootTemplate] page " + masterTemplate.getAbsolutePath() + " was read from cache");                                                                 
            }
        } else {
            try {
                result = templateClass.getContent(cms, masterTemplate.getAbsolutePath(), null, parameters);
            } catch(CmsException e) {
                cache.clearCache(cacheKey);
                throw e;
            }
            if(templateClass.isCacheable(cms, masterTemplate.getAbsolutePath(), parameters)) {
                cache.put(cacheKey, result);
            }
        }         
        return result;
    }
}
