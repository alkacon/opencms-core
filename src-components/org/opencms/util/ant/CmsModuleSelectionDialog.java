/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/util/ant/Attic/CmsModuleSelectionDialog.java,v $
 * Date   : $Date: 2005/02/16 11:43:02 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

/**
 * This is a swing gui dialog for OpenCms module selection.<p>
 * 
 * @author <a href="mailto:m.moossen@alkacon.com">Michael Moossen</a> 
 * @version $Revision: 1.1 $
 * @since 6.0
 */
public class CmsModuleSelectionDialog extends JDialog implements ActionListener {

    private static final int BORDER_SIZE = 10;

    /** Aborted flag. */
    protected boolean m_aborted = true;

    private String[] m_allList = null;
    private Border m_border = BorderFactory.createEmptyBorder(BORDER_SIZE, BORDER_SIZE, 0, BORDER_SIZE);
    private JPanel m_buttons = new JPanel();
    private JButton m_cancel = new JButton("Cancel");
    private JPanel m_content = new JPanel();
    private String[] m_defList = null;

    private final CmsAntTaskModulePrompt m_modulePrompt;
    private JCheckBox[] m_modules = null;
    private JButton m_ok = new JButton("Ok");
    private JLabel m_prompt = new JLabel("Please select the modules to process:");
    private JScrollPane m_view = new JScrollPane(m_content);

    /**
     * Default Ctor.<p>
     * 
     * @param modulePrompt the <code>{@link CmsAntTaskModulePrompt}</code> object.<p>
     */
    public CmsModuleSelectionDialog(CmsAntTaskModulePrompt modulePrompt) {

        super((JFrame)null, "Module Selection", true);
        m_modulePrompt = modulePrompt;

        m_allList = m_modulePrompt.getAllModules().split(CmsAntTaskModulePrompt.LIST_SEPARATOR);
        m_defList = getDefaultList();
        m_modules = new JCheckBox[m_modulePrompt.getAllModules().split(CmsAntTaskModulePrompt.LIST_SEPARATOR).length];
        
        addWindowListener(new WindowAdapter() {

            public void windowClosed(WindowEvent e) {

                m_aborted = true;
                hide();
            }
        });

        getRootPane().setDefaultButton(m_ok);

        m_prompt.setBorder(m_border);
        getContentPane().add(m_prompt, BorderLayout.NORTH);

        m_view.setBorder(m_border);
        m_content.setLayout(new GridLayout(m_modules.length, 1));
        for (int i = 0; i < m_modules.length; i++) {
            m_modules[i] = new JCheckBox(m_allList[i].trim(), firstPositionOfItemInArray(m_defList, m_allList[i]) != -1);
            m_content.add(m_modules[i]);
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

        m_aborted = !e.getActionCommand().equals(m_ok.getText());
        hide();
    }

    /**
     * Returns <code>null</code> if the dialog was canceled, 
     * or a list of selected modules if not.<p>
     * 
     * @return the user selection. 
     */
    public String getModuleSelection() {

        center();
        show();

        String ret = "";
        for (int i = 0; i < m_modules.length; i++) {
            if (m_modules[i].isSelected()) {
                ret += m_modules[i].getText() + CmsAntTaskModulePrompt.LIST_SEPARATOR;
            }
        }
        if (m_aborted || ret.trim().length() < CmsAntTaskModulePrompt.LIST_SEPARATOR.length()) {
            dispose();
            return null;
        }
        dispose();
        return ret.substring(0, ret.length() - CmsAntTaskModulePrompt.LIST_SEPARATOR.length());
    }

    /**
     * centers the dialog on the screen.<p>
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

    private String[] getDefaultList() {

        if (m_modulePrompt.getDefaultValue() == null || m_modulePrompt.getDefaultValue().trim().equals("")) {
            return m_allList;
        }
        return m_modulePrompt.getDefaultValue().split(CmsAntTaskModulePrompt.LIST_SEPARATOR);
    }
}