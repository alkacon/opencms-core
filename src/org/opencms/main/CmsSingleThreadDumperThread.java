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

package org.opencms.main;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.google.common.collect.Lists;

/**
 * Profiling thread used for the startup process.<p>
 *
 * Periodically creates thread dumps for a single thread, saves them zo a ZIP file, and also
 * generates a summary XML files presenting the stack frames with with their sample counts as a tree
 * structure.<p>
 */
public class CmsSingleThreadDumperThread extends Thread {

    /**
     * Node for the summary tree generated from the thread dumps.<p>
     *
     * The tree keeps track of sample counts for stack trace frames.
     */
    public static class SampleNode {

        /** The map of children, with their keys used as map keys. */
        private Map<Object, SampleNode> m_children = new LinkedHashMap<>();

        /** The key for the node. */
        private Object m_key;

        /** The sample count. */
        private long m_samples;

        /**
         * Creates a new node.<p>
         *
         * @param key the key of the node, identifying it among its siblings
         */
        public SampleNode(Object key) {

            m_key = key;
        }

        /**
         * Compare nodes by descending sample count.<p>
         *
         * @param a first node
         * @param b second node
         * @return the comparison result
         */
        public static int compareBySamplesDescending(SampleNode a, SampleNode b) {

            return -Long.compare(a.getSamples(), b.getSamples());
        }

        /**
         * Increment sample count for all nodes along the given path.<p>
         *
         * @param root the root node
         * @param path a sequence of keys constituting a path in the tree
         */
        public static void incrementPath(SampleNode root, List<?> path) {

            List<SampleNode> nodes = nodesForPath(root, path);
            for (SampleNode node : nodes) {
                node.increment();
            }

        }

        /**
         * Given a path consisting of a list of node keys, this method collects all nodes along that path,
         * including the given root node, and creates nodes if they don't exist in the tree yet.<p>
         *
         * @param root the root of the tree
         * @param path the path
         *
         * @return the sequence of nodes along the path
         */
        public static List<SampleNode> nodesForPath(SampleNode root, List<?> path) {

            List<SampleNode> result = new ArrayList<>();
            result.add(root);
            SampleNode current = root;
            for (Object key : path) {
                current = current.getOrAddChild(key);
                result.add(current);
            }
            return result;
        }

        /**
         * Dumps the tree node to XML.<p>
         *
         * @param parent the parent XML to append the XML to
         */
        public void appendToXml(Element parent) {

            Element element = parent.addElement("location").addAttribute("key", "" + m_key).addAttribute(
                "samples",
                "" + m_samples);
            for (SampleNode child : m_children.values()) {
                child.appendToXml(element);
            }
        }

        /**
         * Gets the key of the node.<p>
         *
         * @return the key of the node
         */
        public Object getKey() {

            return m_key;
        }

        /**
         * Gets the child node for the given key, creating it it it doesn't exist yet.<p>
         *
         * @param key the key
         * @return the child for the key
         */
        public SampleNode getOrAddChild(Object key) {

            SampleNode child = m_children.get(key);
            if (child == null) {
                child = new SampleNode(key);
                m_children.put(key, child);
            }
            return child;
        }

        /**
         * Returns the sample count.<p>
         *
         * @return the sample count
         */
        public long getSamples() {

            return m_samples;
        }

        /**
         * Check if this node has children.<p>
         *
         * @return true if this node has children
         */
        public boolean hasChildren() {

            return m_children.size() > 0;
        }

        /**
         * Increments the sample count for this node.<p>
         */
        public void increment() {

            m_samples += 1;
        }

        /**
         * Sorts the children of this node by descending sample count.<p>
         */
        public void sortChildren() {

            ArrayList<SampleNode> children = new ArrayList<>(m_children.values());
            children.sort(SampleNode::compareBySamplesDescending);
            m_children.clear();
            for (SampleNode node : children) {
                m_children.put(node.getKey(), node);
            }
        }

        /**
         * Recursively sorts this node's and all its descendants' children.<p>
         */
        public void sortTree() {

            sortChildren();
            for (SampleNode child : m_children.values()) {
                child.sortTree();
            }
        }

    }

    /** The file name to save the zip file to. */
    private String m_filename;

    /** The file name for the summary file. */
    private String m_summaryFilename;

    /** The id of the thread to monitor. */
    private long m_threadId;

    /** The thread management bean. */
    private ThreadMXBean m_threadMx;

    /**
     * Creates a new instance.<p>
     *
     * @param filename the name of the zip file to generate
     * @param summaryFilename the name of the summary file to generate
     * @param id the id of the thread to monitor
     */
    public CmsSingleThreadDumperThread(String filename, String summaryFilename, long id) {

        super("" + CmsSingleThreadDumperThread.class.getName());
        m_threadMx = ManagementFactory.getThreadMXBean();
        m_threadId = id;
        m_filename = filename;
        m_summaryFilename = summaryFilename;
        // If startup process fails, we don't want this thread to hang around
        setDaemon(true);
    }

    /**
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {

        int count = 1;
        SampleNode root = new SampleNode("ROOT");

        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(m_filename))) {
            while (!isInterrupted()) {
                ThreadInfo info = m_threadMx.getThreadInfo(m_threadId, Integer.MAX_VALUE);
                List<StackTraceElement> path = Lists.reverse(Arrays.asList(info.getStackTrace()));
                SampleNode.incrementPath(root, path);
                StringBuffer buffer = new StringBuffer();
                buffer.append(formatThreadInfo(info));
                byte[] dumpData = buffer.toString().getBytes("UTF-8");
                ZipEntry entry = new ZipEntry("dump_" + count + ".txt");
                zip.putNextEntry(entry);
                zip.write(dumpData);
                count += 1;
                Thread.sleep(10);
            }

        } catch (InterruptedException e) {
            return;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            saveSummaryXml(root);
        }
    }

    /**
     * Formats the thread information.<p>
     *
     * @param info thread information bean
     * @return the formatted thread info
     */
    String formatThreadInfo(ThreadInfo info) {

        StringBuilder sb = new StringBuilder(
            "\"" + info.getThreadName() + "\"" + " Id=" + info.getThreadId() + " " + info.getThreadState());
        if (info.getLockName() != null) {
            sb.append(" on " + info.getLockName());
        }
        if (info.getLockOwnerName() != null) {
            sb.append(" owned by \"" + info.getLockOwnerName() + "\" Id=" + info.getLockOwnerId());
        }
        if (info.isSuspended()) {
            sb.append(" (suspended)");
        }
        if (info.isInNative()) {
            sb.append(" (in native)");
        }
        sb.append('\n');
        int i = 0;
        for (; (i < info.getStackTrace().length); i++) {
            StackTraceElement ste = info.getStackTrace()[i];
            sb.append("\tat " + ste.toString());
            sb.append('\n');
            if ((i == 0) && (info.getLockInfo() != null)) {
                Thread.State ts = info.getThreadState();
                switch (ts) {
                    case BLOCKED:
                        sb.append("\t-  blocked on " + info.getLockInfo());
                        sb.append('\n');
                        break;
                    case WAITING:
                        sb.append("\t-  waiting on " + info.getLockInfo());
                        sb.append('\n');
                        break;
                    case TIMED_WAITING:
                        sb.append("\t-  waiting on " + info.getLockInfo());
                        sb.append('\n');
                        break;
                    default:
                }
            }

            for (MonitorInfo mi : info.getLockedMonitors()) {
                if (mi.getLockedStackDepth() == i) {
                    sb.append("\t-  locked " + mi);
                    sb.append('\n');
                }
            }
        }
        return sb.toString();

    }

    /**
     * Saves the stack trace summary tree starting from the given root node to an XML file.<p>
     *
     * @param root the root node
     */
    private void saveSummaryXml(SampleNode root) {

        root.sortTree();
        Document doc = DocumentHelper.createDocument();
        Element rootElem = doc.addElement("root");
        root.appendToXml(rootElem);
        OutputFormat outformat = OutputFormat.createPrettyPrint();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        outformat.setEncoding("UTF-8");
        try {
            XMLWriter writer = new XMLWriter(buffer, outformat);
            writer.write(doc);
            writer.flush();
            try (FileOutputStream fos = new FileOutputStream(m_summaryFilename)) {
                fos.write(buffer.toByteArray());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
