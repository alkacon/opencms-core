package source.org.apache.java.util;

/*
 * Copyright (c) 1997-1999 The Java Apache Project.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. All advertising materials mentioning features or use of this
 *    software must display the following acknowledgment:
 *    "This product includes software developed by the Java Apache 
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *
 * 4. The names "Apache JServ", "Apache JServ Servlet Engine" and 
 *    "Java Apache Project" must not be used to endorse or promote products 
 *    derived from this software without prior written permission.
 *
 * 5. Products derived from this software may not be called "Apache JServ"
 *    nor may "Apache" nor "Apache JServ" appear in their names without 
 *    prior written permission of the Java Apache Project.
 *
 * 6. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the Java Apache 
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *    
 * THIS SOFTWARE IS PROVIDED BY THE JAVA APACHE PROJECT "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JAVA APACHE PROJECT OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Java Apache Group. For more information
 * on the Java Apache Project and the Apache JServ Servlet Engine project,
 * please see <http://java.apache.org/>.
 *
 */

import java.util.Vector;

/**
 * Implements single queue.
 * <p>
 *
 * Extremely simplified queue implementation, cut to fit into the background
 * logging.
 *
 * @author <a href="vt@freehold.crocodile.org">Vadim Tkachenko</a>
 * @version $Revision: 1.5 $ $Date: 2003/07/12 11:29:22 $
 */
public class SimpleQueue {
    /**
     * Data holder.
     */
    private Vector m_queue;

    /**
     * Default constructor.
     */
    public SimpleQueue() {
        m_queue = new Vector();
    }
    /**
     * Get the object waiting in the queue.
     * Asynchronous.
     * @return null if the queue is empty.
     */
    public synchronized Object get() {
        Object found = peekAtHead();
        if (found != null) {
            m_queue.removeElementAt(0);
        } else {
            return found;
        }

        return found;
    }
    /**
     * Find out if the queue is empty.
     * @return true if the queue is empty
     */
    public boolean isEmpty() {
        return m_queue.isEmpty();
    }
    /**
     * Get the object at the head of the queue.
     * @return null if the queue is empty.
     */
    public synchronized Object peekAtHead() {
        if (m_queue.isEmpty()) {
            return null;
        }

        return m_queue.elementAt(0);
    }
    /**
     * Put the object into the queue.
     *
     * @param toPut the object to put
     */
    public synchronized void put(Object toPut) {
        m_queue.addElement(toPut);
        notify();
    }

    /**
     * Get the number of elements in the queue.
     * @return the number of elements in the queue
     */
    public int size() {
        return m_queue.size();
    }
    /**
     * Wait until the object appears in the queue, then return it.
     * @return The object from the queue.
     * @throws InterruptedException if this thread was interrupted by another thread.
     * @see #get
     */
    public synchronized Object waitObject() throws InterruptedException {
        while (isEmpty()) {
            wait();
        }
        return get();
    }
}
