/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/applet/upload/UploadAppletFileChooser.java,v $
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

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFileChooser;

/**
 * A file chooser custom tailored for the OpenCms upload applet.
 * <p>
 * 
 * It will check the existance the selected files on the server and pops up a 
 * further modal dialog to check / uncheck whether files should be overwritten in 
 * case of collision.
 * <p> 
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.8 $
 * 
 */
public class UploadAppletFileChooser extends javax.swing.JFileChooser {

    /** Generated <code>serialVersion UID</code>. */
    private static final long serialVersionUID = 6473542662952983859L;

    /** The applet to work for. */
    protected FileUploadApplet m_applet;

    /** Access to the internal file chooser dialog object. */
    protected JDialog m_dialogAccessHack;

    /** Return value code. */
    protected int m_returnValue = ERROR_OPTION;

    /**
     * Creates a file chooser for the given upload applet.<p>
     * 
     * @param applet the upload applet peer for this file chooser
     */
    public UploadAppletFileChooser(FileUploadApplet applet) {

        m_applet = applet;
        addActionListener(new ActionListener() {

            /**
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent e) {

                String actionCommand = e.getActionCommand();
                if (actionCommand != null && actionCommand.indexOf("Cancel") == -1) {
                    File[] files = getSelectedFiles();
                    int rtv = m_applet.checkServerOverwrites(files);
                    if (rtv == ModalDialog.CANCEL_OPTION) {
                        m_returnValue = JFileChooser.CANCEL_OPTION;
                    } else if (rtv == ModalDialog.APPROVE_OPTION) {
                        m_returnValue = JFileChooser.APPROVE_OPTION;
                        UploadAppletFileChooser.this.m_dialogAccessHack.dispose();
                    } else {
                        m_returnValue = JFileChooser.ERROR_OPTION;
                    }
                } else {
                    m_returnValue = JFileChooser.CANCEL_OPTION;
                    UploadAppletFileChooser.this.m_dialogAccessHack.dispose();
                }

            }
        });
    }

    /**
     * @see javax.swing.JFileChooser#createDialog(java.awt.Component)
     */
    @Override
    protected JDialog createDialog(Component parent) throws HeadlessException {

        JDialog dialog = super.createDialog(parent);
        m_dialogAccessHack = dialog;
        // show the file selector dialog
        return dialog;
    }

    /**
     * @see javax.swing.JFileChooser#showDialog(java.awt.Component, java.lang.String)
     */
    @Override
    public int showDialog(Component parent, String approveButtonText) throws HeadlessException {

        if (approveButtonText != null) {
            setApproveButtonText(approveButtonText);
            setDialogType(CUSTOM_DIALOG);
        }
        m_dialogAccessHack = createDialog(parent);
        m_dialogAccessHack.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {

                m_returnValue = CANCEL_OPTION;
            }
        });
        m_returnValue = ERROR_OPTION;
        rescanCurrentDirectory();

        m_dialogAccessHack.setVisible(true);
        m_dialogAccessHack.dispose();
        m_dialogAccessHack = null;
        return m_returnValue;
    }
}
