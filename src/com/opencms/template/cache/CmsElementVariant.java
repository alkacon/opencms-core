/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/cache/Attic/CmsElementVariant.java,v $
* Date   : $Date: 2005/02/18 14:23:16 $
* Version: $Revision: 1.15 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001-2005  The OpenCms Group
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

import java.io.UnsupportedEncodingException;
import java.util.Vector;

/**
 * An instance of CmsElementVariant stores a single cached variant for an
 * element. This is the generated output (content) of an element. This cache
 * stores all generated strings of this element and all links to other elements.
 *
 * @author Andreas Schouten
 * @author Alexander Lucas
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsElementVariant {

    /**
     * The content of this variant. In this vector object of type String
     * and of CmsElementLink can be stored.
     */
    Vector m_content;

    /**
     * The dependencies of this variant. In this vector objects of type String
     * can be stored. These Strings are resources in the vfs or the cos. If one
     * of the resources change this variant is decleared void.
     */
    Vector m_dependencies;

    /**
     * The date when this variant must be new generated. Only used if it is not 0.
     */
    private long m_nextTimeout = 0;

    /**
     * Marker that indicates if this variant was exported before.
     */
    private boolean m_exported = false;

    /**
     * Creates a new empty variant for an element.
     */
    public CmsElementVariant() {
        m_content = new Vector();
    }

    /**
     * Adds static content to this variant.
     * @param staticContent - part of the variant. A peace static content of
     * type string.
     */
    public void add(String staticContent) {
        m_content.add(staticContent);
    }

    /**
     * Adds static content to this variant.
     * @param staticContent - part of the variant. A peace static content of
     * type byte-array.
     */
    public void add(byte[] staticContent) {
    	m_content.add(staticContent);
    }

    /**
     * Adds static content to this variant.
     * @param staticContent - part of the variant. A peace static content of
     * type byte-array.
     */
    public void add(byte[] staticContent, String encoding) {
        try {
            m_content.add(new String(staticContent, encoding));
        } catch (UnsupportedEncodingException uee) {
            m_content.add(staticContent);
        }
    }

    /**
     * Adds an element-link to this variant.
     * @param elementLink - part of the variant. A link to another element.
     */
    public void add(CmsElementLink elementLink) {
        m_content.add(elementLink);
    }

    /**
     * Adds an method-link to this variant.
     * @param methodLink - part of the variant. A link to an method.
     */
    public void add(CmsMethodLink methodLink) {
        m_content.add(methodLink);
    }

    /**
     * Get the number of objects in this variant.
     */
    public int size() {
        return m_content.size();
    }

    /**
     * Returns a peace of this variant. It can be of the type String, byte[] or
     * CmsElementLink.
     * @param i - the index to the vector of variant-pieces.
     */
    public Object get(int i) {
        return m_content.get(i);
    }

    /**
     * Sets the dependencies Vector for this Variant.
     * @param dependencies A Vector of Strings.
     */
    public void setDependencies(Vector dependencies){
        m_dependencies = dependencies;
    }

    /**
     * Returns true if this variant was allready exported.
     */
    public boolean wasExported(){
        return m_exported;
    }

    /**
     * Sets the marker exported to true. Used when this variant is created in
     * export modus.
     */
    public void setExported(){
        m_exported = true;
    }

    /**
     * Gets the dependencies Vector of this Variant.
     *
     * @return dependencies A Vector of Strings.
     */
    public Vector getDependencies(){
        return m_dependencies;
    }

    /**
     * Add a dependencies Vector to this.
     *
     * @param depVariant The Vector with the dependencies.
     */
    public void addDependencies(Vector depVariant){
        if(m_dependencies == null){
            m_dependencies = depVariant;
        }else if (depVariant != null){
            // both vectors not null, we have to merge
            for (int i = 0; i < depVariant.size(); i++){
                m_dependencies.add(depVariant.elementAt(i));
            }
        }
    }

    /**
     * Get a string representation of this variant.
     * @return String representation.
     */
    public String toString() {
        int len = m_content.size();
        StringBuffer result = new StringBuffer("[CmsElementVariant] (" + len + ") :");
        for(int i=0; i<len; i++) {
            Object o = m_content.elementAt(i);
            if(o instanceof byte[] || o instanceof String) {
                result.append("TXT");
            } else {
                result.append("(");
                result.append(o.toString());
                result.append(")");
            }
            if(i < len-1) result.append("-");
        }
        return result.toString();
    }

    /**
     * Merges the time when this variant has to be new generated.
     * Sets it to the minimum of the old and the new value, whereby 0 don't count.
     * @param timeout. The date as a long.
     */
    public void mergeNextTimeout(long timeout){

        if(m_nextTimeout == 0 || timeout == 0){
            if(m_nextTimeout < timeout){
                m_nextTimeout = timeout;
            }
        }else{
            if(m_nextTimeout > timeout){
                m_nextTimeout = timeout;
            }
        }
    }
    /**
     * Returns the time when this variant has to be new generated.
     * @return timeout. The date as a long.
     */
    public long getNextTimeout(){
        return m_nextTimeout;
    }
    /**
     * Returns true if this variant has an expiration date.
     */
    public boolean isTimeCritical(){
        return m_nextTimeout != 0;
    }
}