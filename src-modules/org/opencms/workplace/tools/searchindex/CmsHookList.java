/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/searchindex/CmsHookList.java,v $
 * Date   : $Date: 2005/09/20 15:39:06 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.searchindex;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A list intended for sublclassing that triggers "listlet" operations that may 
 * access a on a "peer" object 
 * that is provided by a template method to implement in subclasses. <p>
 * 
 * This is inteded to react on modifications on <code>{@link java.util.List}</code> instances 
 * performed by <code>{@link org.opencms.workplace.CmsWidgetDialogParameter}</code> instances 
 * linked to them. Using normal list implementations makes it impossible to intervene in those 
 * list modification by the widget technology.<p>
 * 
 * "Listlet" operations are operations that are triggered upon modification of this 
 * list. They are called "on&lt;methodName(&gt;[e]d(&lt;peerObject&gt;, &lt;argList&gt;)" 
 * where &lt;methodName&gt; is the name of the original list operation that took place, 
 * "[e]d" stands for the past (operation took place), &lt;peerObject&gt; is the 
 * given class to perform reactions on (see constructors) and 
 *   &lt;argList&gt; are the arguments of the orginal list method in that order. <p>
 * 
 * Currently only the operations used by <code>{@link org.opencms.workplace.CmsWidgetDialog}</code> 
 * (see implementation of <code>{@link org.opencms.workplace.CmsWidgetDialog#actionToggleElement()}</code>) 
 * are supported and sufficient for this purpose. More general usability enforces extending 
 * the pattern shown here. <p>
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.1.2.1 $
 * 
 * @since 6.0.0
 */
public abstract class CmsHookList extends LinkedList {

    /** The object operations are made upon. This design cries for 1.5 generics. **/
    private Object m_peer;

    /**
     * Creates an empty list. <p>
     * 
     * Subclasses should increase "safety by design" by narrowing the type of peer.<p> 
     * 
     * @param peer the object reactions on operations shall be made on in the "listlet" methods of subclasses 
     * 
     */
    public CmsHookList(Object peer) {

        super();
        m_peer = peer;
    }

    /**
     * Creates a list filled with all elements of the given argument. <p>
     * 
     * Subclasses should increase "safety by design" by narrowing the type of peer.<p> 
     * 
     * @param peer the object reactions on operations shall be made on in the "listlet" methods of subclasses 
     * 
     * @param c a collection with all values for this list
     */
    public CmsHookList(Object peer, Collection c) {

        super(c);
        m_peer = peer;
    }

    /**
     * 
     * @see java.util.List#add(int, java.lang.Object)
     */
    public void add(int index, Object element) {

        super.add(index, element);
        this.onAdded(m_peer, index, element);

    }

    /**
     * 
     * @see java.util.Collection#add(java.lang.Object)
     */
    public boolean add(Object o) {

        if (super.add(o)) {
            this.onAdded(m_peer, o);
            return true;
        }
        return false;
    }

    /**
     * 
     * @see java.util.Collection#clear()
     */
    public void clear() {

        onClear(m_peer);
        super.clear();
        onCleared(m_peer);
    }

    /**
     * 
     * @see java.util.List#get(int)
     */
    public Object get(int index) {

        Object ret = super.get(index);
        onGetCall(m_peer, index);
        return ret;
    }

    /**
     * 
     * @see java.util.Collection#iterator()
     */
    public Iterator iterator() {

        Iterator it = super.iterator();
        onIteratorCall(m_peer);
        return it;
    }

    /**
     * 
     * @see java.util.List#remove(int)
     */
    public Object remove(int index) {

        Object ret = null;
        // get an IndexOutOfBoundsException just like list interfaces contract
        ret = super.remove(index);
        return ret;
    }

    /**
     * React on the performed operation <code>{@link java.util.List#add(int, java.lang.Object)}</code> 
     * by informing argument peer. <p>
     * 
     * @param peer the object reactions on operations shall be made on in this "listlet" method 
     * @param index the index the element was added at
     * @param element the element that was added
     */
    protected abstract void onAdded(Object peer, int index, Object element);

    /**
     * React on the performed operation <code>{@link java.util.List#add(java.lang.Object)}</code> 
     * by informing argument peer. <p>
     * 
     * @param peer the object reactions on operations shall be made on in this "listlet" method 
     * @param o the element that was successfully added 
     */
    protected abstract void onAdded(Object peer, Object o);

    /**
     * React on the operation to come <code>{@link java.util.List#clear()}</code> 
     * by informing argument peer. <p>
     * 
     * This is called before the actual clear operation takes place.<p>
     * 
     * @param peer the object reactions on operations shall be made on in this "listlet" method 
     */
    protected abstract void onClear(Object peer);

    /**
     * React on the performed operation <code>{@link java.util.List#clear()}</code> 
     * by informing argument peer. <p>
     * 
     * This is called after the actual clear operation has taken place.<p>
     * 
     * @param peer the object reactions on operations shall be made on in this "listlet" method 
     */
    protected abstract void onCleared(Object peer);

    /**
     * React on the performed operation <code>{@link java.util.List#get(int)}</code> 
     * by informing argument peer. <p> 
     * 
     * Note that the call reult is only obtained in this instance but not given to the 
     * requesting client when this handler is invoked.<p>
     * 
     * @param peer the object reactions on operations shall be made on in this "listlet" method 
     * @param index the index of the Object to get
     */
    protected abstract void onGetCall(Object peer, int index);

    /**
     * React on the performed operation <code>{@link java.util.List#iterator()}</code> 
     * by informing argument peer. <p> 
     * 
     * Note that the iterator is only obtained but not given to the requesting 
     * client when this handler is invoked.<p>
     * 
     * @param peer the object reactions on operations shall be made on in this "listlet" method 
     */
    protected abstract void onIteratorCall(Object peer);

    /**
     * React on the performed operation <code>{@link java.util.List#remove(int)}</code> 
     * by informing argument peer. <p>
     * 
     * This is only invoked if the list operation was successful.<p>
     * 
     * @param peer the object reactions on operations shall be made on in this "listlet" method 
     * @param index the index where the value has been removed
     */
    protected abstract void onRemoved(Object peer, int index);

}
