/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/util/Attic/CmsLruHashMap.java,v $
 * Date   : $Date: 2003/03/18 01:48:05 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
package com.opencms.flex.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A hash table based implementation of the Map interface with limited size 
 * and a "last-recently-used" cache policy of the mapped key/values.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.8 $
 * @see CmsFlexLruCache
 * @see I_CmsFlexLruCacheObject
 */
public class CmsLruHashMap extends HashMap {
    
    /** LRU cache to organize the cached objects efficient. */
    private CmsFlexLruCache m_LruCache;


    /**
     * Creates a LRU hash map with an initial capacity of (16), a load factor 
     * of (0.75), and a max. size of cacheable objects of (36).<p>
     */
    public CmsLruHashMap() {
        this( 16, (float)0.75, 36 );
    }

    /**
     * Creates a LRU hash map with a given initial capacity, a load factor 
     * of (0.75), and a max. size of cacheable objects of (initialCapacity*3*0.75).<p>
     * 
     * @param initialCapacity the initial capacity of the hash map
     */
    public CmsLruHashMap( int initialCapacity ) {
        this( initialCapacity, (float)0.75, (int)(initialCapacity*3*0.75) );
    }
    
    /**
     * Creates a LRU hash map with a given initial capacity, a given load factor, 
     * and a max. size of cacheable objects of (initialCapacity*3*loadFactor).<p>
     * 
     * @param initialCapacity the initial capacity of the hash map
     * @param loadFactor the load factor of the hash map before it is rehashed
     */    
    public CmsLruHashMap( int initialCapacity, float loadFactor ) {
        this( initialCapacity, loadFactor, (int)(initialCapacity*3*loadFactor) );
    }
    
    /**
     * Creates a LRU hash map with a given initial capacity, a load factor of (0.75), 
     * and a given max. size of cacheable objects.<p>
     * 
     * @param initialCapacity the initial capacity of the hash map
     * @param maxLruSize the max. count of cacheable objects
     */    
    public CmsLruHashMap( int initialCapacity, int maxLruSize ) {
        this( initialCapacity, (float)0.75, maxLruSize );
    }
    
    /**
     * Creates a LRU hash map with a given initial capacity, a given load factor, 
     * and a given max. size of cacheable objects.<p>
     * 
     * @param initialCapacity the initial capacity of the hash map
     * @param loadFactor the load factor of the hash map before it is rehashed
     * @param maxLruSize the max. count of cacheable objects
     */
    public CmsLruHashMap( int initialCapacity, float loadFactor, int maxLruSize ) {
        super( initialCapacity, loadFactor );
        this.m_LruCache = new CmsFlexLruCache( maxLruSize, (int)(maxLruSize*0.75), -1, false );
    }
      
    
    /**
     * Removes all objects from this map.
     */
    public void clear() {
        this.m_LruCache.clear();
        super.clear();
    }
    
    /**
     * Clears any allocated resources during finalization.<p>
     */
    protected void finalize() throws Throwable {
        this.clear();
        this.m_LruCache = null;
        super.finalize();
    }
    
    /**
     * Returns the value to which the specified key is mapped in this identity 
     * hash map, or null if the map contains no mapping for this key.<p>
     * 
     * A return 
     * value of null does not necessarily indicate that the map contains no 
     * mapping for the key; it is also possible that the map explicitly maps 
     * the key to null. The containsKey method may be used to distinguish 
     * these two cases. 
     * 
     * @param key the key to look up
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get( Object key ) {
        CmsLruCacheObject cachedObject = (CmsLruCacheObject)super.get( key );
        
        if (cachedObject!=null) {
            this.m_LruCache.touch( cachedObject );  
            return (Object)cachedObject.getValue();
        }
        
        return null;
    }
    
    /**
     * Associates the specified value with the specified key in this map.<p>
     * 
     * If the map previously contained a mapping for this key, the old value is 
     * replaced. 
     * 
     * @param key the key to store 
     * @param value the value to store
     *
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put( Object key, Object value ) {         
        CmsLruCacheObject cachedObject = new CmsLruCacheObject( value, key, this );
        
        if (this.m_LruCache.add(cachedObject)) {
            CmsLruCacheObject previousMapping = (CmsLruCacheObject)super.put( key, (CmsLruCacheObject)cachedObject );
                
            if (previousMapping!=null) {
                return (Object)previousMapping.getValue();
            }
        }
        
        return null;    
    }
    
    /**
     * Copies all of the mappings from the specified map to this map.<p>
     * 
     * These mappings will replace any mappings that this map had for any of the 
     * keys currently in the specified map.
     * 
     * @param t the map to store
     *
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll( Map t ) {
        Iterator allKeys = t.keySet().iterator();
        
        while (allKeys.hasNext()) {
            String nextKey = (String)allKeys.next();
            this.put( nextKey, t.get(nextKey) );
        }

    }
    
    /**
     * Removes the mapping for this key from this map if present.<p>
     *
     * @param key the key to remove
     *
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove( Object key ) {
        CmsLruCacheObject cachedObject = (CmsLruCacheObject)super.remove( key );
        
        if (cachedObject!=null) {
            this.m_LruCache.remove( cachedObject );
            return (Object)cachedObject.getValue();    
        }
        
        return null;    
    }
    
    /**
     * Returns the internal cache used for the LRU policy.<p>
     * 
     * @return the internal cache used for the LRU policy
     */
    public CmsFlexLruCache getLruCache() {
        return this.m_LruCache;
    }
    

    /**
     * An object saved under the LRU policy in a CmsLruHashMap.<p>
     *
     *
     * @see CmsFlexLruCache
     * @see I_CmsFlexLruCacheObject
     * @see CmsLruHashMap
     */
    class CmsLruCacheObject extends Object implements I_CmsFlexLruCacheObject {
        
        private CmsLruHashMap m_ParentHashMap;
        private Object m_ParentKey;
        private Object m_Value;
        
        /** Pointer to the next cache entry in the LRU cache. */
        private I_CmsFlexLruCacheObject m_Next;
        
        /** Pointer to the previous cache entry in the LRU cache. */
        private I_CmsFlexLruCacheObject m_Previous;
        
        /**
         * Constuctor.<p>
         * 
         * @param theInitialValue the initial value
         * @param theParentKey the parent key
         * @param theParentHashMap the parent cache 
         */
        public CmsLruCacheObject( Object theInitialValue, Object theParentKey, CmsLruHashMap theParentHashMap ) {
            this.m_Next = this.m_Previous = null;
            this.m_Value = theInitialValue;
            this.m_ParentKey = theParentKey;
            this.m_ParentHashMap = theParentHashMap;
        }        
            
        /**
         * @see java.lang.Object#finalize()
         */
        protected void finalize() throws Throwable {
            this.m_Next = this.m_Previous = null;
            this.m_Value = null;  
            this.m_ParentKey = null;
            this.m_ParentHashMap = null;          
        }
        
        /**
         * @see com.opencms.flex.util.I_CmsFlexLruCacheObject#setNextLruObject(com.opencms.flex.util.I_CmsFlexLruCacheObject)
         */
        public void setNextLruObject( I_CmsFlexLruCacheObject theNextObject ) {
            this.m_Next = theNextObject;
        }
        
        /**
         * @see com.opencms.flex.util.I_CmsFlexLruCacheObject#getNextLruObject()
         */
        public I_CmsFlexLruCacheObject getNextLruObject() {
            return this.m_Next;
        }
        
        /**
         * @see com.opencms.flex.util.I_CmsFlexLruCacheObject#setPreviousLruObject(com.opencms.flex.util.I_CmsFlexLruCacheObject)
         */
        public void setPreviousLruObject( I_CmsFlexLruCacheObject thePreviousObject ) {
            this.m_Previous = thePreviousObject;
        }
         
        /**
         * @see com.opencms.flex.util.I_CmsFlexLruCacheObject#getPreviousLruObject()
         */
        public I_CmsFlexLruCacheObject getPreviousLruObject() {
            return this.m_Previous;
        }
        
        /**
         * @see com.opencms.flex.util.I_CmsFlexLruCacheObject#addToLruCache()
         */
        public void addToLruCache() {
            // NOOP
        }
        
        /**
         * @see com.opencms.flex.util.I_CmsFlexLruCacheObject#removeFromLruCache()
         */
        public void removeFromLruCache() {
            this.m_ParentHashMap.remove( this.m_ParentKey );
        }
        
        /**
         * @see com.opencms.flex.util.I_CmsFlexLruCacheObject#getLruCacheCosts()
         */
        public int getLruCacheCosts() {
            return 1;
        }   
           
        /**
         * Returns the value.<p>
         * 
         * @return the value
         */
        public Object getValue() {
            return m_Value;
        }

        /**
         * Sets the value.<p>
         * 
         * @param value the value to set
         */
        public void setValue(Object value) {
            m_Value = value;
        }

    }
}
