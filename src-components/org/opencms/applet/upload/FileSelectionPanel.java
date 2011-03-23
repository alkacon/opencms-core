/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/applet/upload/FileSelectionPanel.java,v $
 * Date   : $Date: 2011/03/23 14:56:55 $
 * Version: $Revision: 1.8 $
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

package org.opencms.applet.upload;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * A panel that offers selection of paths. <p>
 * 
 * To make the arbitrary amount of selectable paths scrollable this consists of a scroll pane and and 
 * internal pane for displaying the selectable paths. <p>
 * 
 * This is not reusable. The API is tailored for the upload applet. <p>
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.8 $
 */
public class FileSelectionPanel extends JPanel {

    /**
     * Internal pane to show file selections. 
     * <p>
     * 
     * @author Achim Westermann
     * 
     * @version $Revision: 1.8 $
     */
    static class FileSelectionPane extends JPanel {

        /** Generated <code>serialVersionUID</code>. */
        private static final long serialVersionUID = -3040601958000155698L;

        /**
         * Displays the given paths with checkboxes.<p>
         * 
         * @param paths list of path Strings
         */
        public FileSelectionPane(List paths) {

            setLayout(new GridBagLayout());
            setBackground(Color.WHITE);
            GridBagConstraints gbc = new GridBagConstraints();
            Insets insets = new Insets(0, 2, 0, 2);
            gbc.insets = insets;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;
            gbc.weighty = 0;
            gbc.gridx = 0;
            gbc.gridy = 0;

            for (Iterator it = paths.iterator(); it.hasNext();) {
                add(new Row((String)it.next()), gbc);
                gbc.gridy++;
            }
            // add a dummy that may be enlarged: If this is ommited, a weighty of 0 will cause the lines to 
            // ignore the north anchor and be centered on the scree. A weighty of 1 will increase the spacing 
            // between each line.
            JPanel dummy = new JPanel();
            dummy.setBackground(Color.WHITE);
            gbc.weighty = 1;
            gbc.anchor = GridBagConstraints.SOUTH;
            add(dummy, gbc);
        }

    }

    /**
     * A single row for path selection with a checkbox and a path.<p>
     * 
     * @author Achim Westermann
     * 
     * @version $Revision: 1.8 $
     */
    static class Row extends JPanel {

        /** Generated <code>serialVersionUID</code>. */
        private static final long serialVersionUID = -1002437588400330504L;

        /** the checkbox to show.  */
        private JCheckBox m_checkBox;

        /** the path to show.  */
        private JLabel m_pathLabel;

        /**
         * Creates a row to select a file name.
         * <p> 
         * 
         * @param fileName the file name to make selectable
         */
        Row(final String fileName) {

            setBackground(Color.WHITE);
            m_checkBox = new JCheckBox();
            m_checkBox.setBackground(Color.WHITE);
            m_checkBox.setSelected(true);
            m_pathLabel = new JLabel(fileName);

            // layouting
            Dimension dimension = new Dimension(340, 20);
            m_pathLabel.setPreferredSize(dimension);
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            Insets insets = new Insets(0, 2, 0, 2);
            gbc.insets = insets;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 0.0;
            gbc.weighty = 1f;

            add(m_checkBox, gbc);

            gbc.gridx = 1;
            gbc.weightx = 1f;
            gbc.fill = GridBagConstraints.BOTH;
            add(m_pathLabel, gbc);
        }

        /**
         * Returns the checkBox.
         * <p>
         *
         * @return the checkBox
         */
        public JCheckBox getCheckBox() {

            return m_checkBox;
        }

        /**
         * Returns the pathLabel.
         * <p>
         *
         * @return the pathLabel
         */
        public JLabel getPathLabel() {

            return m_pathLabel;
        }

        /**
         * Sets the pathLabel.
         * <p>
         *
         * @param pathLabel the pathLabel to set
         */
        public void setPathLabel(JLabel pathLabel) {

            m_pathLabel = pathLabel;
        }

    }

    /** Generated <code>serialVersionUID</code>. */
    private static final long serialVersionUID = 7474825906798331943L;

    /** The absolute path prefix of the relative file paths displayed. */
    private String m_rootPath;

    /** The wrapped selection pane (which is made scrollabel by wrapping in a scroll pane).*/
    private FileSelectionPane m_selectionPane;

    /**
     * Creates a file selector with the given paths for selection. 
     * <p> 
     * 
     * @param paths  the paths to select from
     * @param rootPath the absolute path prefix of the relative file paths displayed
     */
    public FileSelectionPanel(List paths, String rootPath) {

        super(new GridLayout(1, 0));

        m_rootPath = rootPath.replace('\\', '/');
        m_selectionPane = new FileSelectionPane(paths);

        // layouting
        JScrollPane scrollPane = new JScrollPane(m_selectionPane);

        scrollPane.setBorder(BorderFactory.createEtchedBorder());

        add(scrollPane, BorderLayout.CENTER);
        applyComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

    }

    private void findFilesRecursive(File file, List files) {

        if (file.isFile()) {
            files.add(file);
        } else if (file.isDirectory()) {
            File[] children = file.listFiles();
            for (int i = 0; i < children.length; i++) {
                findFilesRecursive(children[i], files);
            }
        }
    }

    /**
     * Returns only the selected files (not the folders of this path.
     * <p> 
     * 
     * All files below the selected folders are returned too.
     * <p>
     *  
     * @return only the selected files (not the folders of this path
     */
    public File[] getSelectedFiles() {

        File file;
        List files = new ArrayList();
        boolean selected;
        Row row;
        Component[] rows = m_selectionPane.getComponents();
        // last is dummy component
        for (int i = rows.length - 2; i >= 0; i--) {

            row = (Row)rows[i];
            selected = row.getCheckBox().isSelected();
            if (selected) {
                file = new File(
                    new StringBuffer(m_rootPath).append("/").append(row.getPathLabel().getText()).toString());
                if (file.isFile()) {
                    files.add(file);
                } else if (file.isDirectory()) {
                    findFilesRecursive(file, files);
                }
            }
        }
        File[] result = (File[])files.toArray(new File[files.size()]);
        return result;
    }
}
