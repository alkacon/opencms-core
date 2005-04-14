/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/util/ant/CmsAntTaskSelectionDialog.java,v $
 * Date   : $Date: 2005/04/14 10:53:15 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.util.ant;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;

/**
 * This is a highly configurable Swing GUI dialog for selection.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.3 $
 * @since 5.7.3
 * 
 * @see CmsAntTaskSelectionPrompt
 */
public class CmsAntTaskSelectionDialog extends JDialog implements ActionListener {

    private static final int BORDER_SIZE = 10;

    /** Aborted flag. */
    protected boolean m_aborted = true;

    private String[] m_allList = null;
    private String[] m_defList = null;
    private final CmsAntTaskSelectionPrompt m_promptTask;
    
    private final Border m_border = BorderFactory.createEmptyBorder(BORDER_SIZE, BORDER_SIZE, 0, BORDER_SIZE);
    private JLabel m_label = null;
    private final JPanel m_content = new JPanel();
    private final JScrollPane m_view = new JScrollPane(m_content);
    private JToggleButton[] m_selections = null;
    private final JPanel m_buttons = new JPanel();
    private final JButton m_ok = new JButton("Ok");
    private final JButton m_cancel = new JButton("Cancel");
    private final JButton m_selAll = new JButton("All");
    private final JButton m_selNone = new JButton("None");

    /**
     * Default Constructor.<p>
     * 
     * @param promptTask the <code>{@link CmsAntTaskSelectionPrompt}</code> object.<p>
     */
    public CmsAntTaskSelectionDialog(CmsAntTaskSelectionPrompt promptTask) {

        super((JFrame)null, promptTask.getTitle(), true);
        m_promptTask = promptTask;

        m_allList = m_promptTask.getAllValues().split(CmsAntTaskSelectionPrompt.LIST_SEPARATOR);
        m_defList = getDefaultList();
        m_label = new JLabel(m_promptTask.getPrompt());
        
        addWindowListener(new WindowAdapter() {

            public void windowClosed(WindowEvent e) {

                m_aborted = true;
                setVisible(false);
            }
        });

        getRootPane().setDefaultButton(m_ok);

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
            getContentPane().add(p, BorderLayout.NORTH);
        } else {
            getContentPane().add(m_label, BorderLayout.NORTH);
        }

        m_view.setBorder(m_border);
        m_selections = new JToggleButton[m_promptTask.getAllValues().split(CmsAntTaskSelectionPrompt.LIST_SEPARATOR).length];
        m_content.setLayout(new GridLayout(m_selections.length, 1));
        for (int i = 0; i < m_selections.length; i++) {
            if (m_promptTask.isSingleSelection()) {
                m_selections[i] = new JRadioButton(m_allList[i].trim(), firstPositionOfItemInArray(m_defList, m_allList[i]) != -1);
            } else {
                m_selections[i] = new JCheckBox(m_allList[i].trim(), firstPositionOfItemInArray(m_defList, m_allList[i]) != -1);
            }
            m_content.add(m_selections[i]);
        }
        if (m_promptTask.isSingleSelection()) {
            ButtonGroup group = new ButtonGroup();
            for (int i = 0; i < m_selections.length; i++) {
                group.add(m_selections[i]);
            }
        } 
        getContentPane().add(m_view, BorderLayout.CENTER);

        m_buttons.setBorder(BorderFactory.createEmptyBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE / 2, BORDER_SIZE));
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

        if (e.getActionCommand().equals(m_ok.getText()) || e.getActionCommand().equals(m_cancel.getText())) {
            m_aborted = !e.getActionCommand().equals(m_ok.getText());
            setVisible(false);
        }  else if (e.getActionCommand().equals(m_selAll.getText())) {
            for (int i = 0; i < m_selections.length; i++) {
                m_selections[i].setSelected(true);
            }
        }  else if (e.getActionCommand().equals(m_selNone.getText())) {
            for (int i = 0; i < m_selections.length; i++) {
                m_selections[i].setSelected(false);
            }
        } 
    }

    /**
     * Returns <code>null</code> if the dialog was canceled, 
     * or a list of selected items if not.<p>
     * 
     * @return the user selection
     */
    public String getSelection() {

        center();
        setVisible(true);

        String ret = "";
        for (int i = 0; i < m_selections.length; i++) {
            if (m_selections[i].isSelected()) {
                ret += m_selections[i].getText() + CmsAntTaskSelectionPrompt.LIST_SEPARATOR;
            }
        }
        if (m_aborted || ret.trim().length() < CmsAntTaskSelectionPrompt.LIST_SEPARATOR.length()) {
            dispose();
            return null;
        }
        dispose();
        return ret.substring(0, ret.length() - CmsAntTaskSelectionPrompt.LIST_SEPARATOR.length());
    }

    /**
     * Centers the dialog on the screen.<p>
     *
     * If the size of the dialog exceeds that of the screen, 
     * then the size of the dialog is reset to the size of the screen.<p>
     */
    private void center() {

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension window = getSize();
        //ensure that no parts of the dialog will be off-screen
        if (window.height > screen.height) {
            window.height = screen.height;
        }
        if (window.width > screen.width) {
            window.width = screen.width;
        }
        int xCoord = (screen.width / 2 - window.width / 2);
        int yCoord = (screen.height / 2 - window.height / 2);
        setLocation(xCoord, yCoord);
    }

    /**
     * Looks for the position of a string in an array of string,
     * performing triming and taking into account the null cases.<p>
     * 
     * @param array the string array to search in
     * @param item the item to search for
     * 
     * @return the position of the item in the array or -1 if not found
     */
    private int firstPositionOfItemInArray(String[] array, String item) {

        for (int i = 0; i < array.length; i++) {
            if (array[i] == null) {
                if (item == null) {
                    return i;
                }
            } else {
                if (item != null && array[i].trim().equals(item.trim())) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Returns the array of items selected by default, if no one is given all items will be considered.<p>
     * 
     * @return the array of items selected by default
     */
    private String[] getDefaultList() {

        if (m_promptTask.getDefaultValue() == null || m_promptTask.getDefaultValue().trim().equals("")) {
            return m_allList;
        }
        return m_promptTask.getDefaultValue().split(CmsAntTaskSelectionPrompt.LIST_SEPARATOR);
    }
}