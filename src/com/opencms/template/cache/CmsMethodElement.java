/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/cache/Attic/CmsMethodElement.java,v $
* Date   : $Date: 2003/08/14 15:37:25 $
* Version: $Revision: 1.17 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.template.cache;

import org.opencms.main.OpenCms;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.CmsException;
import com.opencms.file.CmsObject;
import com.opencms.template.A_CmsCacheDirectives;
import com.opencms.template.A_CmsXmlContent;
import com.opencms.template.CmsCacheDirectives;
import com.opencms.template.CmsMethodCacheDirectives;
import com.opencms.template.CmsProcessedString;
import com.opencms.template.I_CmsTemplate;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;

/**
 * An instance of CmsMethodElement represents an special method Element in the OpenCms
 * element cache area. It contains all informations to generate the output of this
 * method. It also stores the variants of once generated content to speed up
 * performance.
 *
 * @author Hanjo Riege
 * @version 1.0
 */

public class CmsMethodElement extends A_CmsElement {

    /**
     * the name of the methode.
     */
    private String m_methodName;

    /**
     * Constructor for an element with the given class and template name.
     */
    public CmsMethodElement(String className, String methodName, CmsMethodCacheDirectives mcd, int variantCachesize) {
        m_methodName = methodName;
        init(className, methodName, mcd, variantCachesize);
    }

    /**
     *  checks the proxy public and the proxy private cache settings
     *  of this element and all subelements. This is a Methodelement so there are no subelements.
     *  @param cms the cms object.
     *  @param proxySettings The CacheDirectives to merge the own CacheDriectives with.
     *  @param parameters A Hashtable with the parameters.
     */
    public void checkProxySettings(CmsObject cms, CmsCacheDirectives proxySettings, Hashtable parameters){

        proxySettings.merge(m_cacheDirectives);
    }

    /**
     * Get the content of this element.
     * @param elementCache Entry point for the element cache
     * @param cms CmsObject for accessing system resources
     * @param elDefs Definitions of this element's subelements
     * @param parameters All parameters of this request
     * @param methodParameter contains the parameter for the methode (the tagcontent in the xmlfile).
     * @return Byte array with the processed content of this element.
     * @throws CmsException
     */
    public byte[] getContent(CmsElementCache elementCache, CmsObject cms, CmsElementDefinitionCollection elDefs, String elementName, Hashtable parameters, String methodParameter) throws CmsException  {
        String result = null;

        // get our own cache directives
        A_CmsCacheDirectives cd = getCacheDirectives();
        
        if (cd == null) {
            // the XmlTemplate implementation is faulty, let assume no caching
            cd = new CmsMethodCacheDirectives(false);
        }
        
        // cacheKey with the methodeParameter so we have variantes for each parameter
        String cacheKey = cd.getCacheKey(cms, parameters);
        if (cacheKey != null){
            cacheKey += methodParameter;
        }

        CmsElementVariant variant = null;

        if(cd.isInternalCacheable()){
            if(cd.isTimeCritical() && (m_timestamp < cd.getTimeout().getLastChange())){
                clearVariantCache();
            }else{
                variant = getVariant(cacheKey);
            }
            if(variant != null){
                result = (String)variant.get(0);
            }
        }
        if(variant == null){
            // this methode was not found in the variant cache
            // we have to generate it by calling the methode in the template class

            // Get template class.
            I_CmsTemplate templateClass = null;
            try {
                templateClass = getTemplateClass(cms, m_className);
            } catch(Throwable e) {
                if(OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
                    OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, toString() + " Could not load my template class \"" + m_className + "\". ");
                    OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, e.toString());
                    return e.toString().getBytes();
                }
            }
            // now call the method
            Object methodResult = null;
            try{
                methodResult = templateClass.getClass().getMethod(m_methodName, new Class[] {
                                    CmsObject.class, String.class, A_CmsXmlContent.class,
                                     Object.class}).invoke(templateClass,
                                     new Object[] {cms, methodParameter, null, parameters});
            }catch(NoSuchMethodException exc) {
                throwException("[CmsMethodElemtent] User method " + m_methodName + " was not found in class " + templateClass.getClass().getName() + ".", CmsException.C_XML_NO_USER_METHOD);
            }catch(InvocationTargetException targetEx) {
                // the method could be invoked, but throwed a exception
                // itself. Get this exception and throw it again.
                Throwable e = targetEx.getTargetException();
                if(!(e instanceof CmsException)) {
                    // Only print an error if this is NO CmsException
                    throwException("User method " + m_methodName + " throwed an exception. " + e, CmsException.C_UNKNOWN_EXCEPTION);
                }else {
                    // This is a CmsException
                    // Error printing should be done previously.
                    throw (CmsException)e;
                }
            }catch(Exception exc2) {
                throwException("User method " + m_methodName + " was found but could not be invoked. " + exc2, CmsException.C_XML_NO_USER_METHOD);
            }
            if(methodResult != null){
                if(methodResult instanceof String){
                   	result = (String)methodResult;
                }else if(methodResult instanceof byte[]){
                	try {
	                    result = new String((byte[])methodResult, cms.getRequestContext().getEncoding());
                    } catch (UnsupportedEncodingException uee) {
                        throw new CmsException(CmsException.C_LOADER_ERROR, uee);
                    }
                }else if(methodResult instanceof Integer){
                    result = ((Integer)methodResult).toString();
                }else if(methodResult instanceof CmsProcessedString){
                    // result stays null but we have to write to the variant cache
                    variant = new CmsElementVariant();
                    variant.add(((CmsProcessedString)methodResult).toString());
                    addVariant(cacheKey, variant);
                }else {
                    throwException("User method " + m_methodName + " in class " + templateClass.getClass().getName() + " returned an unsupported Object: " + methodResult.getClass().getName(), CmsException.C_XML_PROCESS_ERROR);
                }
            }
            if((result != null)&&(cacheKey != null)&&(cd.isInternalCacheable())){
                variant = new CmsElementVariant();
                variant.add(result);
                addVariant(cacheKey, variant);
            }
        }
        if (result == null) return null;
        try {
	        return result.getBytes(cms.getRequestContext().getEncoding());
        } catch (UnsupportedEncodingException uee) {
            throw new CmsException(CmsException.C_LOADER_ERROR, uee);
        }
    }

    /**
     * checks the read access. The methode elements can be read by everyone. So we
     * don't have to check something here.
     * @param cms The cms Object for reading groups.
     * @throws CmsException if no read access.
     */
    public void checkReadAccess(CmsObject cms) throws CmsException{
    }

    /**
     * Help method that handles any occuring exception by writing
     * an error message to the OpenCms logfile and throwing a
     * CmsException of the given type.
     * @param errorMessage String with the error message to be printed.
     * @param type Type of the exception to be thrown.
     * @throws CmsException
     */
    protected void throwException(String errorMessage, int type) throws CmsException {
        if(OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL) ) {
            OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, errorMessage);
        }
        throw new CmsException(errorMessage, type);
    }

}