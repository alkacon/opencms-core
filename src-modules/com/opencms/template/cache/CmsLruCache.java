/*
* File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/template/cache/Attic/CmsLruCache.java,v $
* Date   : $Date: 2005/05/17 13:47:27 $
* Version: $Revision: 1.1 $
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

import java.util.Vector;

/**
 * This class implements a LRU cache. It uses a Hashtable algorithm with the
 * chaining method for collision handling. The sequence of the Objects is stored in
 * an extra chain. Each object has a pointer to the previous and next object in this
 * chain. If an object is inserted or used it is set to the tail of the chain. If an
 * object has to be remouved it will be the head object. Only works with more than one element.
 *
 * @author Hanjo Riege
 * @version 1.0
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */

public class CmsLruCache {

    // enables the login. Just for debugging.
    private static final boolean C_DEBUG = false;

    // the array to store everthing
    private CacheItem[] m_cache;

    // the capacity of the cache
    private int m_maxSize;

    // the aktual size of the cache
    private int m_size = 0;

    // the head of the time sequence
    private CacheItem head;

    // the tail of the time sequence
    private CacheItem tail;

    static class CacheItem {
        Object key;
        Object value;

        // the link for the collision hanling
        CacheItem chain;

        // links for the time sequence chain
        CacheItem previous;
        CacheItem next;
    }

    /**
     * Constructor
     * @param size The size of the cache.
     */
    public CmsLruCache(int size) {
        if(C_DEBUG){
            System.err.println("--LruCache started with "+size);
        }
        m_cache = new CacheItem[size];
        m_maxSize = size;
    }

    /**
     * inserts a new object in the cache. If it is there already the value is updated.
     * @param key The key to find the object.
     * @param value The object.
     * @return if the cache is full the deleted object, null otherwise.
     */
    public synchronized Vector put (Object key, Object value){

        int hashIndex = (key.hashCode() & 0x7FFFFFFF) % m_maxSize;
        CacheItem item = m_cache[hashIndex];
        CacheItem newItem = null;
        Vector returnValue = null;
        if(C_DEBUG){
            System.err.println("put in cache:   "+key);
        }
        if(item != null){
            // there is a item allready. Collision.
            // lets look if the new item was inserted before
            while(item.chain != null){
                if(item.key.equals(key)){
                    item.value = value;
                    // TODO: put it to the end of the chain
                    return null;
                }
                item = item.chain;
            }
            if(item.key.equals(key)){
                item.value = value;
                //TODO: put it on the end of the chain
                return null;
            }
            if(m_size >= m_maxSize){
                // cache full, we have to remove the old head
                CacheItem helper = head.next;
                if (item == head){
                    newItem = item;
                    returnValue = new Vector(2);
                    returnValue.add(0, item.key);
                    returnValue.add(1, item.value);
                }else{
                    newItem = head;
                    returnValue = new Vector(2);
                    returnValue.add(0, head.key);
                    returnValue.add(1, head.value);
                    removeFromTable(head);
                    newItem.chain = null;
                    item.chain = newItem;
                }
                newItem.next = null;
                head = helper;
                head.previous = null;
            }else{
                m_size++;
                newItem = new CacheItem();
                item.chain = newItem;
            }
        }else{
            // oh goody, a free place for the new item
            if(head != null){
                if(m_size >= m_maxSize){
                    // cache full, we have to remove the old head
                    CacheItem helper = head.next;
                    newItem = head;
                    returnValue = new Vector(2);
                    returnValue.add(0, head.key);
                    returnValue.add(1, head.value);
                    removeFromTable(head);
                    newItem.next = null;
                    newItem.chain = null;
                    head = helper;
                    head.previous = null;
                }else{
                    m_size++;
                    newItem = new CacheItem();
                }
            }else{
                // first item in the chain
                newItem = new CacheItem();
                m_size++;
                head = newItem;
                tail = newItem;
            }
            item = m_cache[hashIndex] = newItem;
        }
        // the new Item is in the array and in the chain. Fill it.
        newItem.key = key;
        newItem.value = value;
        if(tail != newItem){
            tail.next = newItem;
            newItem.previous = tail;
            tail = newItem;
        }
        return returnValue;
    }

    /**
     * returns the value to the key or null if the key is not in the cache. The found
     * element has to line up behind the others (set to the tail).
     * @param key The key for the object.
     * @return The value.
     */
    public synchronized Object get(Object key){
        int hashIndex = (key.hashCode() & 0x7FFFFFFF) % m_maxSize;
        CacheItem item = m_cache[hashIndex];
        if(C_DEBUG){
            System.err.println("get from Cache: "+key);
            //checkCondition();
        }
        while (item != null){
            if(item.key.equals(key)){
                // got it
                if(item != tail){
                    // hinten anstellen
                    if(item != head){
                        item.previous.next = item.next;
                    }else{
                        head = head.next;
                    }
                    item.next.previous = item.previous;
                    tail.next = item;
                    item.previous = tail;
                    tail = item;
                    tail.next = null;
                }
                return item.value;
            }
            item = item.chain;
        }
        if(C_DEBUG){
            System.err.println("    not found in Cache!!!!");
        }
        return null;
    }

    /**
     * removes on item from the cache.
     * @param key .The key to find the item.
     */
    public void remove(Object key){
        if(key != null){
            int hashIndex = (key.hashCode() & 0x7FFFFFFF) % m_maxSize;
            CacheItem item = m_cache[hashIndex];
            while (item != null){
                if(item.key.equals(key)){
                    // got it
                    removeItem(item);
                }
                item = item.chain;
            }
        }
    }

    /**
     * deletes one item from the cache. Not from the sequence chain.
     * @param oldItem The item to be deleted.
     */
    private void removeFromTable(CacheItem oldItem){
        if(C_DEBUG){
            System.err.println(" --remove from chaincache: "+oldItem.key);
        }
        int hashIndex = ((oldItem.key).hashCode() & 0x7FFFFFFF) % m_maxSize;
        CacheItem item = m_cache[hashIndex];
        if(item == oldItem){
            m_cache[hashIndex] = item.chain;
        }else{
            if(item != null){
                while(item.chain != null){
                    if(item.chain == oldItem){
                        item.chain = item.chain.chain;
                        return;
                    }
                    item = item.chain;
                }
            }
        }
    }

    /**
     * removes one item from the cache and from the sequence chain.
     */
    private synchronized void removeItem(CacheItem item){

        if(C_DEBUG){
            System.err.println("--remove item from cache: "+item.key);
        }
        if(m_size < 2){
            clearCache();
        }else{
            //first remove it from the hashtable
            removeFromTable(item);
            // now from the sequence chain
            if((item != head) && (item != tail)){
                item.previous.next = item.next;
                item.next.previous = item.previous;
            }else{
                if(item == head){
                    head = item.next;
                    head.previous = null;
                }
                if (item == tail){
                    tail = item.previous;
                    tail.next = null;
                }
            }
            m_size--;
        }
    }

    /**
     * Deletes all elements that depend on the template.
     * use only if the cache is for elements.
     *
     * @return Vector a Vector with deleted items. Each item is a Vector with two elements: key and value.
     */
    public synchronized Vector deleteElementsByTemplate(String templateName){
        CacheItem item = head;
        Vector ret = new Vector();
        while (item != null){
            if(templateName.equals(((CmsElementDescriptor)item.key).getTemplateName())){
                Vector actItem = new Vector();
                actItem.add(0, item.key);
                actItem.add(1, item.value);
                ret.add(actItem);
                removeItem(item);
            }
            item = item.next;
        }
        return ret;
    }

    /**
     * Deletes all elements that depend on the class.
     * use only if this cache is for elements.
     * @return Vector a Vector with deleted items. Each item is a Vector with two elements: key and value.
     */
    public synchronized Vector deleteElementsByClass(String className){
        CacheItem item = head;
        Vector ret = new Vector();
        while (item != null){
            if(className.equals(((CmsElementDescriptor)item.key).getClassName())){
                Vector actItem = new Vector();
                actItem.add(0, item.key);
                actItem.add(1, item.value);
                ret.add(actItem);
                removeItem(item);
            }
            item = item.next;
        }
        return ret;
    }

    /**
     * Deletes elements after publish. All elements that depend on the
     * uri and all element that say so have to be removed.
     * use only if this cache is for elements.
     * @return Vector a Vector with deleted items. Each item is a Vector with two elements: key and value.
     */
    public synchronized Vector deleteElementsAfterPublish(){
        CacheItem item = head;
        Vector ret = new Vector();
        while (item != null){
            try{
                if (((A_CmsElement)item.value).getCacheDirectives().shouldRenew()){
                    Vector actItem = new Vector();
                    actItem.add(0, item.key);
                    actItem.add(1, item.value);
                    ret.add(actItem);
                    removeItem(item);
                }
            }catch(NullPointerException e){
                // cachedirectives are null, so we delete this Element anyway.
                Vector actItem = new Vector();
                actItem.add(0, item.key);
                actItem.add(1, item.value);
                ret.add(actItem);
                removeItem(item);
            }
            item = item.next;
        }
        return ret;
    }

    /**
     * Deletes the uri from the Cache. Use only if this is the cache for uris.
     *
     */
    public synchronized void deleteUri(String uri){
        CacheItem item = head;
        while (item != null){
            if(uri.equals(((CmsUriDescriptor)item.key).getKey())){
                removeItem(item);
                // found the uri, ready.
                return;
            }
            item = item.next;
        }
    }

    /**
     * Clears the cache completly.
     */
    public synchronized void clearCache(){
        if(C_DEBUG){
            System.err.println("--LruCache restarted");
        }
        m_cache = new CacheItem[m_maxSize];
        m_size = 0;
        head = null;
        tail = null;
    }

    /**
     * Gets the Information of max size and size for the cache.
     *
     * @return a Vector whith informations about the size of the cache.
     */
    public Vector getCacheInfo(){

        if(C_DEBUG){
            System.err.println("...Cache size should be:"+ m_size + " by a max size of "+m_maxSize);
            int count = 0;
            CacheItem item = head;
            while (item != null){
                count++;
                item = item.next;
            }
            System.err.println("...Cache size is:"+count);
        }
        Vector info = new Vector();
        info.addElement(new Integer(m_maxSize ));
        info.addElement(new Integer(m_size));
        return info;
    }

    /**
     * gets all keys in the cache.
     * @return a vector with the keys.
     */
    public synchronized Vector getAllKeys(){
        Vector ret = new Vector();
        CacheItem item = head;
        while (item != null){
            ret.add(item.key);
            item = item.next;
        }
        return ret;
    }

    /*
     * used for debuging only. Checks if the Cache is in a valid condition.
    private void checkCondition(){
        System.err.println("");
        System.err.println("-- Verify condition of Cache");
        System.err.println("--size: "+m_size);
        CacheItem item = head;
        int count = 1;
        System.err.println("--");
        System.err.println("--testing content from head to tail:");
        while(item!=null){
            System.err.println("    --"+count+". "+item.key);
            count++;
            item=item.next;
        }
        System.err.println("");
        System.err.println("--now from tail to head:");
        item = tail;
        count--;
        while(item!=null){
            System.err.println("    --"+count+". "+item.key);
            count--;
            item=item.previous;
        }
        System.err.println("--now what is realy in cache:");
        count = 1;
        for (int i=0; i<m_maxSize; i++){
            item = m_cache[i];
            if(item == null){
//                System.err.print("    element "+i+" ");
//                System.err.println(" null");
            }else{
                System.err.print("    element "+i+" ");
                System.err.println(" count="+count++ +" "+item.key);
                while(item.chain != null){
                    item = item.chain;
                    System.err.println("        chainelement "+" count="+count++ +" "+item.key);
                }
            }
        }
        System.err.println("--test ready!!");
        System.err.println("");

    }
    */
}