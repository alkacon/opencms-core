/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/cache/Attic/CmsElementLocator.java,v $
* Date   : $Date: 2003/08/18 15:11:21 $
* Version: $Revision: 1.29 $
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

import org.opencms.loader.CmsXmlTemplateLoader;
import org.opencms.main.OpenCms;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.CmsException;
import com.opencms.file.CmsObject;
import com.opencms.template.CmsMethodCacheDirectives;
import com.opencms.template.I_CmsTemplate;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * The ElementLocator is used to receive CmsElement-Objects. It is the Cache for
 * these CmsElement-Objects. The CmsElement-Objects are stored in memory or -
 * if they are notc used a long time - written to an external database. The
 * locator manages all the reading, writing and management of the CmsElement's.
 *
 * @author Andreas Schouten
 * @author Alexander Lucas
 */
public class CmsElementLocator {

    /**
     * A hashtable to store the elements.
     */
    private CmsLruCache m_elements;

    /**
     * link to the extern dependencies vector
     */
    private Hashtable m_dependenciesExtern = null;

    /**
     * The default constructor for this locator.
     */
    CmsElementLocator(int cacheSize) {
        if(cacheSize < 2){
            cacheSize = 50000;
        }
        m_elements = new CmsLruCache(cacheSize);
    }

    /**
     * Adds a new Element to this locator.
     * This method is kept private and must not be used from outside.
     * New elements automatically are generated and stored by the Locator,
     * so no one really needs to use this method.
     * @param descriptor - the descriptor for this element.
     * @param element - the Element to put in this locator.
     */
    private void put(CmsElementDescriptor desc, A_CmsElement element) {
        Vector removedElement =  m_elements.put(desc, element);
        if(removedElement != null && m_dependenciesExtern != null){
            // look if the element is critical and if clear the m_dependenciesExtern table
            removeElementFromDependencies((CmsElementDescriptor)removedElement.firstElement(),
                                            (A_CmsElement)removedElement.lastElement());
        }
    }

    /**
     * Deletes all variantdependenciesEntries of an Element from the extern dependencies table.
     *
     * @param the descriptor of the element.
     * @param the element itself.
     */
    public void removeElementFromDependencies(CmsElementDescriptor desc, A_CmsElement element){
        if(element.hasDependenciesVariants()){
            Vector variantKeys = element.getAllVariantKeys();
            String cacheStart = desc.getClassName() +"|"+ desc.getTemplateName() +"|";
            for(int i=0; i<variantKeys.size(); i++){
                String key = (String)variantKeys.elementAt(i);
                removeVariantFromDependencies(cacheStart + key, element.getVariant(key));
            }
        }
    }

    /**
     * Deletes all variantdependenciesEntries of an Variant from the extern dependencies table.
     *
     * @param key the compleate entry in the table like "classname|template|variantcachekey"
     * @param the variant
     */
    public void removeVariantFromDependencies(String key, CmsElementVariant variant){

        if(variant != null){
            Vector variantDeps = variant.getDependencies();
            if(variantDeps != null){
                for(int j=0; j<variantDeps.size(); j++){
                    Vector externEntrys = (Vector)m_dependenciesExtern.get(variantDeps.elementAt(j));
                    if(externEntrys != null){
                        externEntrys.removeElement(key);
                    }
                }
            }
        }
    }

    /**
     * Gets a Elements from this locator.
     * @param desc - the descriptor to locate the element.
     * @return the element that was found.
     */
    public A_CmsElement get(CmsObject cms, CmsElementDescriptor desc, Hashtable parameters) throws CmsException{
        A_CmsElement result;
        result = (A_CmsElement)m_elements.get(desc);
        if(result == null) {
            // the element was not found in the element cache
            // we have to generate it
            I_CmsTemplate cmsTemplate = null;
            // look if it is an methode element
            if("METHOD".equals(desc.getTemplateName())){
                String orgClassName = desc.getClassName();
                String className = orgClassName.substring(0,orgClassName.lastIndexOf("."));
                String methodName = orgClassName.substring(orgClassName.lastIndexOf(".")+1);
                try {
                    cmsTemplate = (I_CmsTemplate)com.opencms.template.CmsTemplateClassManager.getClassInstance(className);
                    CmsMethodCacheDirectives mcd = (CmsMethodCacheDirectives)cmsTemplate.getClass().getMethod(
                                                    "getMethodCacheDirectives", new Class[] {
                                                    CmsObject.class, String.class}).invoke(cmsTemplate,
                                                    new Object[] {cms, methodName});
                    result = new CmsMethodElement(className, methodName, mcd,
                             CmsXmlTemplateLoader.getElementCache(cms).getVariantCachesize());
                    put(desc, result);
                } catch(Throwable e) {
                    if(OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
                        OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, toString() + " Could not initialize method element for class \"" + className  + "\". ");
                        OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, e.toString());
                        return null;
                    }
                }
            }else{
                try {
                    cmsTemplate = (I_CmsTemplate)com.opencms.template.CmsTemplateClassManager.getClassInstance(desc.getClassName());
                    result = cmsTemplate.createElement(cms, desc.getTemplateName(), parameters);
                    put(desc, result);
                } catch(Throwable e) {
                    if(OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
                        OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, toString() + " Could not initialize (sub-)element for class \"" + desc.getClassName() + "\". ");
                        OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, e.toString());
                        throw new CmsException("Could not initialize (sub-)element for class \"" +
                                             desc.getClassName() + "\". " +e.toString() , CmsException.C_XML_WRONG_TEMPLATE_CLASS);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Gets the Information of max size and size for the cache.
     *
     * @return a Vector whith informations about the size of the cache.
     */
    public Vector getCacheInfo(){
        return m_elements.getCacheInfo();
    }

    /**
     * for debbuging only. Prints information about the cache system.
     *
     * @param selector Selects which info is printed.
     */
    public void printCacheInfo(int selector){

        if(selector == 1){
            // info about the dependencies stores
            System.err.println("");
            System.err.println("");
            System.err.println("=======================");
            System.err.println("The dependencies stores");
            System.err.println("=======================");
            System.err.println("");
            System.err.println("=======================");
            System.err.println("The extern Hashtable:");
            System.err.println("=======================");
            int countExtern = 0;
            int countIntern = 0;
            if(m_dependenciesExtern != null){
                Enumeration enu = m_dependenciesExtern.keys();
                int count = 1;
                System.err.println("");
                while(enu.hasMoreElements()){
                    String key = (String)enu.nextElement();
                    System.err.println("<"+count+"> "+key);
                    Vector entrysVector = (Vector)m_dependenciesExtern.get(key);
                    if(entrysVector == null){
                        System.err.println("        Vector is null.");
                    }else{
                        if(entrysVector.size() == 0){
                            System.err.println("        Vector is empty.");
                        }
                        for(int i=0; i<entrysVector.size(); i++){
                            System.err.println("    ("+i+") "+(String)entrysVector.elementAt(i));
                            countExtern++;
                        }
                    }
                    System.err.println("");
                    count++;
                }
            }else{
                System.err.println("... is null!");
            }
            System.err.println("");
            System.err.println("===================================");
            System.err.println("The values in the element Variants:");
            System.err.println("===================================");
            //first we need all elements
            Vector elementKeys = m_elements.getAllKeys();
            for(int i=0; i<elementKeys.size(); i++){
                A_CmsElement element = (A_CmsElement)m_elements.get(elementKeys.elementAt(i));
                if(element.hasDependenciesVariants()){
                    System.err.println("");
                    System.err.println("<"+i+"> element:"+element.toString());
                    Vector variants = element.getAllVariantKeys();
                    if(variants == null || variants.size() == 0){
                        System.err.println("    no variants.");
                    }else{
                        // now all variants from this elemetn
                        for(int j=0; j<variants.size(); j++){
                            CmsElementVariant vari = element.getVariant(variants.elementAt(j));
                            System.err.println("");
                            System.err.println("        ("+j+")variant:"+(String)variants.elementAt(j));
                            System.err.println("                timed:"+vari.isTimeCritical() + " nextTimeOut:"+vari.getNextTimeout() );
                            Vector currentDeps = vari.getDependencies();
                            if(currentDeps == null || currentDeps.size() == 0){
                                System.err.println("                no dependencies in this element");
                            }else{
                                for(int k=0; k<currentDeps.size(); k++){
                                    System.err.println("                ["+k+"] "+(String)currentDeps.elementAt(k));
                                    countIntern++;
                                }
                                System.err.println("");
                            }
                        }
                    }
                }
            }
            System.err.println("");
            System.err.println("==================================");
            System.err.println("==== found in Extern store: "+countExtern);
            System.err.println("===================================");
            System.err.println("==== found in Intern store: "+countIntern);
            System.err.println("===================================");
            System.err.println("");
        }
    }

    /**
     * deletes all elements in the cache that depend on one of the invalid Templates.
     * @param invalidTemplates A vector with the ablolute path of the templates (String)
     */
    public void cleanupElementCache(Vector invalidTemplates){

        cleanupExternDependencies(m_elements.deleteElementsAfterPublish());
        for(int i=0; i < invalidTemplates.size(); i++){
            cleanupExternDependencies(
                    m_elements.deleteElementsByTemplate((String)invalidTemplates.elementAt(i)));
        }
    }

    /**
     * Clears the cache from unvalid variants. It looks for each entry in the invalidResources
     * if there are variants that depend on it. If so this variant has to be deleted and
     * the extern dependencies table is updated.
     *
     * @param invalidResources A vector of Strings with the entrys to compare to the
     *          externDependencies Hashtable. These entrys are resources in the vfs or
     *          the cos that are changed or deleted.
     */
    public void cleanupDependencies(Vector invalidResources){

        if(invalidResources != null){
            for(int i=0; i<invalidResources.size(); i++){
                Enumeration extKeys = m_dependenciesExtern.keys();
                String aktInvalid = (String)invalidResources.elementAt(i);
                while(extKeys.hasMoreElements()){
                    String current = (String)extKeys.nextElement();
                    if(aktInvalid.startsWith(current) || current.startsWith(aktInvalid)){
                        Vector variantsToDelete = (Vector)m_dependenciesExtern.get(current);
                        if(variantsToDelete != null){
                            // delete all the variants in this vector
                            for(int j=0; j < variantsToDelete.size(); j++){
                                String variantKey = (String)variantsToDelete.elementAt(j);
                                // get the element for this variant
                                StringTokenizer tocy = new StringTokenizer(variantKey, "|", false);
                                String classname = tocy.nextToken();
                                String templatename = tocy.nextToken();
                                String cacheDirectivesKey = tocy.nextToken();
                                CmsElementDescriptor desc  = new CmsElementDescriptor(classname, templatename);
                                A_CmsElement currentElement = (A_CmsElement)m_elements.get(desc);
                                if(currentElement != null){
                                    removeVariantFromDependencies(variantKey, currentElement.getVariant(cacheDirectivesKey));
                                    currentElement.removeVariant(cacheDirectivesKey);
                                }
                            }
                        }
                    }
                }
            }
            // now remove all empty entrys in the extern table
            Enumeration extKeys = m_dependenciesExtern.keys();
            while(extKeys.hasMoreElements()){
                String currentKey = (String)extKeys.nextElement();
                Vector currentValue = (Vector)m_dependenciesExtern.get(currentKey);
                if(currentValue == null || currentValue.size() == 0){
                    m_dependenciesExtern.remove(currentKey);
                }
            }
        }
    }

    /**
     * Removes the elements from the extern Dependencies table.
     *
     * @param elements. A Vector with the elements to be removed. This Vector
     *          contains Vectors. Each of this vectors contains two objects.
     *          The first one is the CmsElementDescriptor of the element and
     *          the second one is the element itself(A_CmsElement).
     */
    private void cleanupExternDependencies(Vector elements){
        if(elements != null){
            for(int i=0; i<elements.size(); i++){
                Vector actElement = (Vector)elements.elementAt(i);
                removeElementFromDependencies((CmsElementDescriptor)actElement.firstElement(),
                                                (A_CmsElement)actElement.lastElement());
            }
        }
    }

    /**
     * Clears the cache compleatly.
     */
    public void clearCache(){
        m_elements.clearCache();
        if(m_dependenciesExtern != null){
            m_dependenciesExtern.clear();
        }
    }

    /**
     * TODO: there should be only one way to get this vector. remove the way through the
     * cms Object?
     */
    public Hashtable getExternDependencies(){
        return m_dependenciesExtern;
    }

    /**
     * sets the extern dependencies vector used to keep the dep information syncron.
     */
    public void setExternDependencies(Hashtable externDeps){
        m_dependenciesExtern = externDeps;
    }

}