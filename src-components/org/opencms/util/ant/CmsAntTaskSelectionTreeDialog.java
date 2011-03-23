/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/util/ant/CmsAntTaskSelectionTreeDialog.java,v $
 * Date   : $Date: 2011/03/23 14:56:57 $
 * Version: $Revision: 1.11 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.util.ant;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * This is a highly configurable Swing GUI dialog for selection.
 * <p>
 * 
 * @author Michael Moossen (original non-tree version)
 * @author Achim Westermann (modified tree version)
 * 
 * @version $Revision: 1.11 $
 * 
 * @since 6.1.6
 * 
 * @see CmsAntTaskSelectionPrompt
 */
public class CmsAntTaskSelectionTreeDialog extends JDialog implements ActionListener {

    /** Constant for border width. */
    private static final int C_BORDER_SIZE = 10;

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -8439685952987222098L;

    /** Aborted flag. */
    protected boolean m_aborted;

    /** The list of all module names. * */
    private List m_allModuleList;

    /** Border. */
    private final Border m_border = BorderFactory.createEmptyBorder(C_BORDER_SIZE, C_BORDER_SIZE, 0, C_BORDER_SIZE);

    /** Panel for buttons. */
    private final JPanel m_buttons = new JPanel();

    /** Cancel button. */
    private final JButton m_cancel = new JButton("Cancel");

    /** Main Panel. */
    private final JPanel m_content = new JPanel();

    /** Label for prompt. */
    private JLabel m_label;

    /** Ok button. */
    private final JButton m_ok = new JButton("Ok");

    /** Associated ant task. */
    private final CmsAntTaskSelectionTreePrompt m_promptTask;

    /** Select all button. */
    private final JButton m_selAll = new JButton("All");

    /** Select none button. */
    private final JButton m_selNone = new JButton("None");

    /** The tree for selection of sets of modudles. . */
    private JTree m_tree;

    /**
     * Default Constructor.
     * <p>
     * 
     * @param promptTask the <code>{@link CmsAntTaskSelectionPrompt}</code> object.
     *            <p>
     */
    public CmsAntTaskSelectionTreeDialog(CmsAntTaskSelectionTreePrompt promptTask) {

        super((JFrame)null, promptTask.getTitle(), true);
        m_promptTask = promptTask;

        m_label = new JLabel(m_promptTask.getPrompt());

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent e) {

                setVisible(false);
            }
        });

        getRootPane().setDefaultButton(m_ok);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        m_label.setBorder(m_border);
        if (!m_promptTask.isSingleSelection()) {
            JPanel p1 = new JPanel();
            p1.add(new JLabel("Select: "));
            m_selAll.addActionListener(this);
            p1.add(m_selAll);
            m_selNone.addActionListener(this);
            p1.add(m_selNone);
            JPanel p = new JPanel(new BorderLayout());
            p.add(m_label, BorderLayout.NORTH);
            p.add(p1, BorderLayout.SOUTH);
            contentPane.add(p);
        } else {
            getContentPane().add(m_label);
        }

        JScrollPane scrollpane = new JScrollPane(m_content);
        scrollpane.setBorder(m_border);

        scrollpane.setPreferredSize(new Dimension(300, 800));
        // parse the String-list to a clean list as it is not only used for tree-creation but for
        // tree-selection determination too:
        this.parseModuleList();
        TreeModel treeModel = createTree();
        m_tree = new SelectionTree();
        m_tree.setModel(treeModel);
        m_tree.setRootVisible(false);
        m_tree.setShowsRootHandles(true);
        expandTree(new TreePath(treeModel.getRoot()));
        selectDefaultNodes((DefaultMutableTreeNode)treeModel.getRoot(), "", new TreePath(treeModel.getRoot()));

        // layout: let the tree start in upper left edge instead of being centered within the pane
        // - it would start to move as it expands and changes bounds.
        m_content.setLayout(new GridLayout(1, 1));
        m_content.add(m_tree);
        m_content.setBorder(BorderFactory.createLoweredBevelBorder());

        contentPane.add(scrollpane);

        m_buttons.setBorder(BorderFactory.createEmptyBorder(
            C_BORDER_SIZE,
            C_BORDER_SIZE,
            C_BORDER_SIZE / 2,
            C_BORDER_SIZE));
        m_ok.addActionListener(this);
        m_buttons.add(m_ok);
        m_cancel.addActionListener(this);
        m_buttons.add(m_cancel);
        getContentPane().add(m_buttons, BorderLayout.SOUTH);

        pack();
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {

        Object source = e.getSource();
        if (source == m_ok) {
            m_aborted = false;
            setVisible(false);
        } else if (source == m_cancel) {
            m_aborted = true;
            setVisible(false);
        } else if (source == m_selAll) {
            m_tree.setSelectionPath(new TreePath(m_tree.getModel().getRoot()));
        } else if (source == m_selNone) {
            m_tree.clearSelection();
        }
        m_tree.invalidate();
        // m_tree.cancelEditing();
        m_tree.repaint();
    }

    /**
     * Returns <code>null</code> if the dialog was canceled, or a list of selected items if not.
     * <p>
     * 
     * @return the user selection
     */
    public String getSelection() {

        center();
        setVisible(true);

        // Ret is the complete String with all modules separated by ... look at that constant
        StringBuffer ret = new StringBuffer();
        // TODO query the selected paths for all subnodes.
        TreePath[] pathArr = m_tree.getSelectionPaths();
        // avoid NPE but skip loop:
        if (pathArr == null) {
            pathArr = new TreePath[0];
        }
        TreePath path;
        StringBuffer pathString;
        DefaultMutableTreeNode node;

        // pathString is the path string of the selected TreePath from the tree.
        // it may be a leaf (single selection) or not. In the latter case iteration
        // continues to all subnodes to add all reachable leafs as module names.
        // Furthermore every non-leaf-node may be a module name like: org.opencms.workplace and
        // org.opencms.workplace.tools.content...
        for (int i = 0; i < pathArr.length; i++) {
            pathString = new StringBuffer();
            path = pathArr[i];
            // build the path string to the selected path:
            Object[] entries = path.getPath();
            // skip "root"
            for (int j = 1; j < entries.length; j++) {
                pathString.append(entries[j]);
                if (j < entries.length - 1) {
                    pathString.append(".");
                }
            }
            node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if (node.isLeaf()) {
                ret.append(pathString.toString());
                ret.append(CmsAntTaskSelectionTreePrompt.LIST_SEPARATOR);
            } else {
                // first look, wether this is already a module, even if not leaf, (e.g.
                // org.opencms.workplace <-> org.opencms.workplace.tools.accounts...)
                if (m_allModuleList.contains(pathString.toString())) {
                    ret.append(pathString.toString());
                    ret.append(CmsAntTaskSelectionTreePrompt.LIST_SEPARATOR);
                } else {
                    // nop
                }
                // search all leaf nodes and append subpaths:

                ret.append(getSubpaths(node, pathString.toString()));
            }

        }
        dispose();
        if (m_aborted || (ret.toString().trim().length() < CmsAntTaskSelectionTreePrompt.LIST_SEPARATOR.length())) {
            return null;
        } else {
            return ret.toString();
        }
    }

    /**
     * Centers the dialog on the screen.
     * <p>
     * 
     * If the size of the dialog exceeds that of the screen, then the size of the dialog is reset to
     * the size of the screen.
     * <p>
     */
    private void center() {

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension window = getSize();
        // ensure that no parts of the dialog will be off-screen
        int height = window.height;
        int width = window.width;
        if (window.height > screen.height) {
            window.height = screen.height;
            height = screen.height - 50;
            width = width + 50;
        }
        if (window.width > screen.width) {
            window.width = screen.width;
        }
        int xCoord = (screen.width / 2 - window.width / 2);
        int yCoord = (screen.height / 2 - window.height / 2);
        setLocation(xCoord, yCoord);
        setSize(width, height);
    }

    private TreeModel createTree() {

        // for every entry: cut into paths, build a tree path that collates equal paths.
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        TreeModel tm = new DefaultTreeModel(root);
        Iterator itModules = m_allModuleList.iterator();
        StringTokenizer itPath;
        Enumeration childEnum;
        String pathElement;
        DefaultMutableTreeNode node, child;
        boolean found = false;
        while (itModules.hasNext()) {
            itPath = new StringTokenizer((String)itModules.next(), ".");
            node = root;
            while (itPath.hasMoreTokens()) {
                // is this node already there?
                pathElement = itPath.nextToken();
                childEnum = node.children();
                found = false;
                while (childEnum.hasMoreElements()) {
                    child = (DefaultMutableTreeNode)childEnum.nextElement();
                    if (pathElement.equals(child.getUserObject())) {

                        // found node for path String
                        // reuse old path, descend and continue with next path element.
                        node = child;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    // did not break, node was not found
                    child = new DefaultMutableTreeNode();
                    child.setUserObject(pathElement);
                    node.add(child);
                    node = child;
                }
            }
        }
        return tm;

    }

    /**
     * Recursive depth first traversal that stops at expansion level and expands those paths.
     * <p>
     * 
     * @param treePath the current path in recursion
     */
    private void expandTree(TreePath treePath) {

        if (treePath.getPathCount() == m_promptTask.getExpansionLevels()) {
            m_tree.expandPath(treePath);

        } else {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)treePath.getLastPathComponent();
            Enumeration children = treeNode.children();
            while (children.hasMoreElements()) {
                expandTree(treePath.pathByAddingChild(children.nextElement()));
            }
        }

    }

    private String getSubpaths(TreeNode node, String parentPath) {

        Enumeration children = node.children();
        TreeNode child;
        String path = parentPath;
        StringBuffer ret = new StringBuffer();
        while (children.hasMoreElements()) {
            child = (TreeNode)children.nextElement();
            if (parentPath.length() == 0) {
                path = child.toString();
            } else {
                path = parentPath + "." + child.toString();
            }
            if (child.isLeaf()) {
                ret.append(path);
                ret.append(CmsAntTaskSelectionTreePrompt.LIST_SEPARATOR);
            } else {
                ret.append(getSubpaths(child, path));
            }
        }
        return ret.toString();
    }

    /**
     * Parses the string of comma separated module names obtained from the ant script into the
     * internal list of module names for better access in several locations.
     * <p>
     * 
     */
    private void parseModuleList() {

        m_allModuleList = new LinkedList();
        StringTokenizer itPaths = new StringTokenizer(
            m_promptTask.getAllValues(),
            CmsAntTaskSelectionPrompt.LIST_SEPARATOR);

        String token;
        while (itPaths.hasMoreElements()) {
            token = itPaths.nextToken().trim();
            m_allModuleList.add(token);
        }
    }

    /**
     * Recursivley selects the nodes that are qualified by the default selections.
     * 
     * @param node the current Node
     * @param path the node path
     * @param treePath the tree path
     */
    private void selectDefaultNodes(DefaultMutableTreeNode node, String path, TreePath treePath) {

        // allow root property to be set:
        String defaultString = m_promptTask.getDefaultValue();
        if ("root".equalsIgnoreCase(defaultString.trim())) {
            if (node == m_tree.getModel().getRoot()) {
                m_tree.setSelectionPath(treePath);
            }
        } else {
            StringTokenizer tokenizer = new StringTokenizer(defaultString, CmsAntTaskSelectionTreePrompt.LIST_SEPARATOR);
            String defaultEntry;
            while (tokenizer.hasMoreTokens()) {
                defaultEntry = tokenizer.nextToken();
                // don't print in recursions for path
                if (node.getLevel() == 0) {
                    System.out.println("Preselection: " + defaultEntry);
                }
                if (defaultEntry.equals(path)) {
                    m_tree.addSelectionPath(treePath);
                    return;
                }
            }
            Enumeration children = node.children();
            DefaultMutableTreeNode subNode;
            String subPath;
            while (children.hasMoreElements()) {
                subPath = path;
                if (subPath.length() != 0) {
                    subPath += ".";
                }
                subNode = (DefaultMutableTreeNode)children.nextElement();
                subPath += subNode.toString();
                selectDefaultNodes(subNode, subPath, treePath.pathByAddingChild(subNode));
            }

        }
    }
}