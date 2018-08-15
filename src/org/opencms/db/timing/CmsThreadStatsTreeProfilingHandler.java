/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.db.timing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.google.common.collect.Lists;

/**
 * Builds up a tree whose nodes correspond to stack trace lines of the threads calling this
 * profiling handler. The resulting tree can be dumped as XML.<p>
 */
public class CmsThreadStatsTreeProfilingHandler implements I_CmsProfilingHandler {

    /**
     * The tree node.<p>
     */
    public static class Node {

        /** The key identifying the node among its siblings. */
        private Object m_key;

        /** The counted nanos for driver calls. */
        private long m_nanos;

        /** The driver call count. */
        private int m_count;

        /** The cumulative nanos for driver calls. */
        private long m_cumulativeNanos;

        /** The cumulative driver call count. */
        private int m_cumulativeCount;

        /** The children of this node, with their respective keys as map keys. */
        private Map<Object, Node> m_children = new HashMap<>();

        /**
         * Creates a new node.<p>
         *
         * @param key the key for this node
         */
        public Node(Object key) {

            m_key = key;
        }

        /**
         * Computes the cumulative stats for a tree and dumps it to an XML document.<p>
         *
         * @param node the root node
         * @return the resulting XML document
         */
        public static Document dumpTree(Node node) {

            node.computeCumulativeData();
            Document doc = DocumentHelper.createDocument();
            Element root = doc.addElement("root");
            node.dump(root);
            return doc;
        }

        /**
         * Updates the count / nanos for this node.<p>
         *
         * @param nanos the nanoseconds to add
         */
        public void addCall(long nanos) {

            m_count += 1;
            m_nanos += nanos;
        }

        /**
         * Gets the child for a given key, or adds it if it doesn't exist yet.<p>
         *
         * @param key the key
         * @return the child
         */
        public Node addOrGetChild(Object key) {

            Node child = m_children.get(key);
            if (child == null) {
                child = new Node(key);
                m_children.put(key, child);
            }
            return child;

        }

        /**
         * Gets the descendant for a given path, or adds it if it doesn't exist yet.<p>
         *
         * @param path the path
         * @return the descendant
         */
        public Node addOrGetDescendant(List<?> path) {

            Node current = this;
            for (Object key : path) {
                current = current.addOrGetChild(key);
            }
            return current;
        }

        /**
         * Computes the cumulative driver call count and nanos.<p>
         */
        public void computeCumulativeData() {

            m_cumulativeCount = m_count;
            m_cumulativeNanos = m_nanos;
            for (Node child : m_children.values()) {
                child.computeCumulativeData();
                m_cumulativeNanos += child.getCumulativeNanos();
                m_cumulativeCount += child.getCumulativeCount();
            }
        }

        /**
         * Computes XML structure for this node and its descendants and appends it to a given element.<p>
         *
         * @param parent the parent element
         */
        public void dump(Element parent) {

            Element elem = parent.addElement("location").addAttribute("key", "" + m_key).addAttribute(
                "count",
                "" + m_cumulativeCount).addAttribute("millis", "" + (m_cumulativeNanos / 1000000));

            // Sort children so the "most expensive" ones go first
            List<Node> children = new ArrayList<>(m_children.values());
            children.sort(new Comparator<Node>() {

                @SuppressWarnings("synthetic-access")
                public int compare(Node o1, Node o2) {

                    return -Long.compare(o1.getCumulativeNanos(), o2.getCumulativeNanos());
                }
            });
            for (Node child : children) {
                child.dump(elem);
            }
        }

        /**
         * Gets the key for the node.<p>
         *
         * @return tbe key
         */
        public Object getKey() {

            return m_key;
        }

        /**
         * Gets the cumulative driver call count (does not automatically update).<p>
         *
         * @return the cumulative driver call count
         */
        private int getCumulativeCount() {

            return m_cumulativeCount;
        }

        /**
         * Gets the cumulative driver call nanos (does not automatically update).<p>
         *
         * @return the cumulative driver call nanos
         */
        private long getCumulativeNanos() {

            return m_cumulativeNanos;
        }

    }

    /** The root node. */
    private Node m_root = new Node("ROOT");

    /** Flag which records whether we have measured data yet. */
    private boolean m_hasData;

    /**
     * Dumps the tree as XML.<p>
     *
     * @return the tree in XML format
     */
    public String dump() {

        try {
            Document doc = Node.dumpTree(m_root);
            OutputFormat outformat = OutputFormat.createPrettyPrint();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            outformat.setEncoding("UTF-8");
            XMLWriter writer = new XMLWriter(buffer, outformat);
            writer.write(doc);
            writer.flush();
            return new String(buffer.toByteArray(), "UTF-8");
        } catch (IOException e) {
            // CAn't happen
            return null;
        }
    }

    /**
     * Returns true if we received any data yet.<p>
     *
     * @return true if the handler has received data
     */
    public boolean hasData() {

        return m_hasData;
    }

    /**
     * @see org.opencms.db.timing.I_CmsProfilingHandler#putTime(java.lang.String, long)
     */
    public synchronized void putTime(String key, long nanos) {

        m_hasData = true;
        StackTraceElement[] traceElems = (new Throwable()).getStackTrace();
        List<StackTraceElement> trace = Arrays.asList(traceElems);

        List<Object> keys = new ArrayList<>();
        // Since this handler is used during startup, which mostly happens in a single thread,
        // we want to distinguish calls from different threads, e.g. scheduled jobs.
        keys.add("THREAD " + Thread.currentThread().getId());
        keys.addAll(Lists.reverse(trace));
        // Cut off proxy-specific garbage from stack trace
        keys = keys.subList(0, keys.size() - 3);
        Node child = m_root.addOrGetDescendant(keys);
        child.addCall(nanos);
    }

}
