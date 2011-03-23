/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/util/ant/SelectionTree.java,v $
 * Date   : $Date: 2011/03/23 14:56:57 $
 * Version: $Revision: 1.9 $
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * A proprietary {@link javax.swing.JTree} utilization for visuals checkbox selection.
 * <p>
 * 
 * It displays a tree with:
 * <ul>
 * <li> Multiple node selection (windos Look and Feel: press STRG) </li>
 * <li> Subsequent selection on the UI: it appears that all subnodes of a node are selected too</li>
 * <li>{@link javax.swing.tree.TreeSelectionModel#DISCONTIGUOUS_TREE_SELECTION} </li>
 * <li> Custom node UI with checkboxes. </li>
 * </ul>
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.9 $
 * 
 */
public class SelectionTree extends JTree {

    /**
     * 
     * Custom cell renderer that displays a checkbox.
     * <p>
     * 
     * @author Achim Westermann
     * 
     * @version $Revision: 1.9 $
     * 
     * @since 6.1.6
     * 
     */
    class CheckBoxCellRenderer extends DefaultTreeCellRenderer {

        /** Generated servial version UID. * */
        private static final long serialVersionUID = -4329469376335457482L;

        /**
         * 
         * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree,
         *      java.lang.Object, boolean, boolean, boolean, int, boolean)
         */
        @Override
        public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean isSelected,
            boolean expanded,
            boolean leaf,
            int row,
            boolean focus) {

            super.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, focus);
            return new TreeCellUI((DefaultMutableTreeNode)value, selected);
        }
    }

    /**
     * 
     * {@link TreeSelectionListener} that clears the selections on toggeled paths.
     * <p>
     * 
     * This is only needed because of the proprietary UI - display of selected subnodes that are not
     * selected within the tree model itself.
     * <p>
     * 
     * @author Achim Westermann
     * 
     * @version $Revision: 1.9 $
     * 
     * @since 6.1.6
     * 
     */
    class SubsequentSelection implements TreeSelectionListener {

        /**
         * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
         */
        public void valueChanged(TreeSelectionEvent e) {

            // DefaultMutableTreeNode node =
            // (DefaultMutableTreeNode)SelectionTree.this.getLastSelectedPathComponent();
            // clear the old subselections:
            SelectionTree.this.clearToggledPaths();

        }
    }

    /**
     * 
     * Custom component containing the default tree cell component along with a checkbox.
     * <p>
     * 
     * @author Achim Westermann
     * 
     * @version $Revision: 1.9 $
     * 
     * @since 6.1.6
     * 
     */
    class TreeCellUI extends JComponent {

        /** Generated serial version UID. * */
        private static final long serialVersionUID = -1315044645298979088L;

        /** The checkbox to use. * */
        private JCheckBox m_checkBox;

        /**
         * Constructor with the corresponding tree node and the selection flag.
         * <p>
         * 
         * @param node the corresponding tree node.
         * 
         * @param selected flag that specifies the state of the internal checkbox to show.
         */
        public TreeCellUI(DefaultMutableTreeNode node, boolean selected) {

            JLabel label = new JLabel();
            label.setText(node.getUserObject().toString());
            this.m_checkBox = new JCheckBox();
            this.m_checkBox.setSelected(selected || this.searchSelected(node));
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            this.add(this.m_checkBox);
            this.add(label);
            m_checkBox.setBackground(UIManager.getLookAndFeel().getDefaults().getColor("window"));
            // invalidate all subnodes:
        }

        private boolean searchSelected(DefaultMutableTreeNode node) {

            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)SelectionTree.this.getLastSelectedPathComponent();
            if (selectedNode == null) {
                return false;
            }
            return (selectedNode.isNodeDescendant(node));
        }
    }

    /** Generated serial version UID. * */
    private static final long serialVersionUID = -3627379509871776708L;

    /**
     * Defcon.
     */
    public SelectionTree() {

        super();
        super.setCellRenderer(new CheckBoxCellRenderer());
        this.selectionModel.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        this.selectionModel.addTreeSelectionListener(new SubsequentSelection());
        // this.setShowsRootHandles(false);
    }

    /**
     * For testing purposes.
     * <p>
     * 
     * @param args unused command line args.
     */
    public static void main(String[] args) {

        DefaultMutableTreeNode node = new DefaultMutableTreeNode("root");
        DefaultMutableTreeNode a = new DefaultMutableTreeNode("a");
        DefaultMutableTreeNode b = new DefaultMutableTreeNode("b");
        DefaultMutableTreeNode c = new DefaultMutableTreeNode("c");
        DefaultMutableTreeNode a1 = new DefaultMutableTreeNode("1");
        DefaultMutableTreeNode a2 = new DefaultMutableTreeNode("2");
        DefaultMutableTreeNode a3 = new DefaultMutableTreeNode("3");
        DefaultMutableTreeNode b1 = new DefaultMutableTreeNode("1");
        DefaultMutableTreeNode b2 = new DefaultMutableTreeNode("2");
        DefaultMutableTreeNode b3 = new DefaultMutableTreeNode("3");
        DefaultMutableTreeNode c1 = new DefaultMutableTreeNode("1");
        DefaultMutableTreeNode c2 = new DefaultMutableTreeNode("2");
        DefaultMutableTreeNode c3 = new DefaultMutableTreeNode("3");

        node.add(a);
        node.add(b);
        node.add(c);
        a.add(a1);
        a.add(a2);
        a.add(a3);
        b.add(b1);
        b.add(b2);
        b.add(b3);
        c.add(c1);
        c.add(c2);
        c.add(c3);

        JFrame frame = new JFrame("SelectionTree");
        SelectionTree tree = new SelectionTree();
        tree.setModel(new DefaultTreeModel(node));
        frame.getContentPane().add(tree);
        frame.setSize(new Dimension(200, 800));
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {

                System.exit(0);
            }
        });
        frame.setVisible(true);

    }

    /**
     * @see javax.swing.JTree#clearToggledPaths()
     */
    @Override
    public void clearToggledPaths() {

        // TODO: Auto-generated method stub
        super.clearToggledPaths();
    }

    /**
     * 
     * @see javax.swing.JTree#removeSelectionPath(javax.swing.tree.TreePath)
     */
    @Override
    public void removeSelectionPath(TreePath path) {

        // unselect the current TreeCell:
        super.removeSelectionPath(path);
        // unselect the TreeCell rendererer checkboxes in subsequent entries:
        // this.clearToggledPaths();
    }

    /**
     * 
     * @see javax.swing.JTree#setCellRenderer(javax.swing.tree.TreeCellRenderer)
     */
    @Override
    public void setCellRenderer(TreeCellRenderer x) {

        // nop
    }

    /**
     * @see javax.swing.JTree#setSelectionPath(javax.swing.tree.TreePath)
     */
    @Override
    public void setSelectionPath(TreePath path) {

        super.setSelectionPath(path);
        this.clearToggledPaths();
    }

}
