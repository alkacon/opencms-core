/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/applet/upload/ModalDialog.java,v $
 * Date   : $Date: 2011/03/23 14:56:56 $
 * Version: $Revision: 1.10 $
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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Base class for dialogs with ok and cancel buttons, 
 * support for modality and custom UI components. <p>
 *  
 * This is a try for a better design approach to modal dialogs than offered in
 * the java development kit: <br>
 * The service of a modal dialog that offers cancel and ok is separated from the
 * retrieval of data of such a dialog. The component that queries the data from
 * this service is freely choosable. It may be passed to the contstructor and
 * will be returned from <code>{@link #showDialog()}</code>. The client code then is sure
 * that the modal dialog has been confirmed by the human interactor and may
 * query this component for input: it knows about the component that was used to
 * query inputs.
 * <p>
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.10 $
 * 
 */
public class ModalDialog extends JDialog {

    // ********************************
    // ***** Dialog Return Values *****
    // ********************************

    /**
     * Return value if approve (yes, ok) is chosen.
     */
    public static final int APPROVE_OPTION = 0;

    /**
     * Return value if cancel is chosen.
     */
    public static final int CANCEL_OPTION = 1;

    /**
     * Return value if an error occurred.
     */
    public static final int ERROR_OPTION = -1;

    /** Generated <code>serialVersionUID</code>. */
    private static final long serialVersionUID = 6915311633181971117L;

    /** The text of the cancel button. */
    private String m_cancelText = "Cancel";

    /** The ui controls and model to interact with. */
    private JComponent m_controlPanel;

    /** The text of the ok button. */
    private String m_okText = "OK";

    /** One of the option constants. */
    protected int m_returnValue;

    /**
     * Creates a modal dialog.
     * <p>
     * 
     * @param component the parent <code>Component</code> for the dialog
     * @param title the String containing the dialog's title
     * @param okText the text for the OK button        
     * @param cancelText the text for the Cancel button
     * @param controlComponent the UI component that is additionally shown and returned from {@link #showDialog()}
     */
    public ModalDialog(
        final Component component,
        final String title,
        String okText,
        String cancelText,
        final JComponent controlComponent) {

        super(JOptionPane.getFrameForComponent(component), title, true);
        m_controlPanel = controlComponent;
        m_cancelText = cancelText;
        m_okText = okText;

        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.add(m_controlPanel);

        // Window listeners:
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(final WindowEvent e) {

                Window w = e.getWindow();
                w.setVisible(false);
            }
        });
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentHidden(final ComponentEvent e) {

                Window w = (Window)e.getComponent();
                w.dispose();
            }
        });

        // Cancel / OK Buttons.
        JPanel okCancelPanel = new JPanel();
        okCancelPanel.setLayout(new BoxLayout(okCancelPanel, BoxLayout.X_AXIS));
        okCancelPanel.add(Box.createHorizontalGlue());
        JButton ok = new JButton(m_okText);
        ok.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {

                m_returnValue = APPROVE_OPTION;
                setVisible(false);
            }
        });
        okCancelPanel.add(ok);
        okCancelPanel.add(Box.createHorizontalGlue());
        JButton cancel = new JButton(m_cancelText);
        cancel.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {

                m_returnValue = CANCEL_OPTION;
                setVisible(false);
            }
        });
        okCancelPanel.add(cancel);
        okCancelPanel.add(Box.createHorizontalGlue());
        // add ok / cancel to ui:
        contentPane.add(okCancelPanel);
        setSize(new Dimension(300, 200));
    }

    /**
     * Returns the controlPanel.<p>
     *
     * @return the controlPanel
     */
    public JComponent getControlPanel() {

        return m_controlPanel;
    }

    /**
     * Returns the returnValue.<p>
     *
     * @return the returnValue
     */
    public int getReturnValue() {

        return m_returnValue;
    }

    /**
     * Shows a modal dialog and blocks until the dialog is hidden.
     * <p>
     * If the user presses the "OK" button, then this method hides/disposes the
     * dialog and returns the custom component that queries for user input. If the
     * user presses the "Cancel" button or closes the dialog without pressing
     * "OK", then this method hides/disposes the dialog and returns
     * <code>null</code>.
     * <p>
     * 
     * 
     * @return the custom component given to the constructor with it's new
     *         settings or <code>null</code> if the user opted out.
     * 
     * @exception HeadlessException if GraphicsEnvironment.isHeadless() returns true.
     * 
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public JComponent showDialog() throws HeadlessException {

        // blocks until user brings dialog down...
        setVisible(true);
        return m_controlPanel;
    }

}
