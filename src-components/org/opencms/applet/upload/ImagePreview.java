/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/applet/upload/ImagePreview.java,v $
 * Date   : $Date: 2005/06/23 10:47:10 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.applet.upload;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;

/**
 * Image preview for the file select box.<p>
 * 
 * Based on the Java 1.4 example.<p>
 * 
 * @author Michael Emmerich 
 * 
 * @version $Revision: 1.7 $ 
 * 
 * @since 6.0.0 
 */
public class ImagePreview extends JComponent implements PropertyChangeListener {

    private static final int C_MODE_EMPTY = 0;
    private static final int C_MODE_IMAGE = 1;
    private static final int C_MODE_TEXT = 2;

    private File m_file;
    private Font m_font;
    private String m_messageNoPreview;
    private int m_mode;
    private String[] m_text;
    private ImageIcon m_thumbnail;

    /**
     * Constructor, creates a new ImagePreview.<p>
     *
     * @param fc The fileselector box
     * @param messageNoPreview localized message for no preview
     */
    public ImagePreview(JFileChooser fc, String messageNoPreview) {

        setPreferredSize(new Dimension(200, 100));
        fc.addPropertyChangeListener(this);
        m_font = new java.awt.Font("Verdana", Font.PLAIN, 9);
        m_messageNoPreview = messageNoPreview;
        m_mode = C_MODE_EMPTY;
    }

    /**
     * Loads the image for the preview.<p>
     */
    public void loadImage() {

        if (m_file == null) {
            m_thumbnail = null;
            return;
        }
        // load the image
        ImageIcon tmpIcon = new ImageIcon(m_file.getPath());
        if (tmpIcon != null) {
            if (tmpIcon.getIconWidth() > 190) {
                //  scale it to the maximum width of 190 pixel if newscessary 
                m_thumbnail = new ImageIcon(tmpIcon.getImage().getScaledInstance(190, -1, Image.SCALE_DEFAULT));
            } else { //no need to miniaturize
                m_thumbnail = tmpIcon;
            }
        }
    }

    /**
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    public void paintComponent(Graphics g) {

        if (m_mode == C_MODE_IMAGE) {
            // if we are in image mode, draw the image
            if (m_thumbnail == null) {
                loadImage();
            }
            if (m_thumbnail != null) {
                int x = getWidth() / 2 - m_thumbnail.getIconWidth() / 2;
                int y = getHeight() / 2 - m_thumbnail.getIconHeight() / 2;

                if (y < 0) {
                    y = 0;
                }

                if (x < 5) {
                    x = 5;
                }
                m_thumbnail.paintIcon(this, g, x, y);
            }
        } else if (m_mode == C_MODE_TEXT) {
            // if we are in text mode, draw the text preview          
            g.setColor(Color.white);
            g.fillRect(10, 0, getWidth() - 1, getHeight() - 1);
            g.setColor(Color.black);
            g.drawRect(10, 0, getWidth() - 11, getHeight() - 1);
            g.setFont(m_font);
            for (int i = 0; i < 35; i++) {
                if (m_text[i] != null) {
                    g.drawString(m_text[i], 13, (i + 1) * 11);
                }
            }
        } else {
            g.setColor(Color.black);
            g.setFont(m_font);
            g.drawString(m_messageNoPreview, 30, getHeight() / 2);
        }
    }

    /**
     * Exchange the image if a new file is selected in the fileselector box.<p>
     * 
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent e) {

        boolean update = false;
        String prop = e.getPropertyName();

        if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop)) {
            // if the directory changed, don't show an image
            m_file = null;
            m_text = null;
            update = true;
            m_mode = C_MODE_EMPTY;

        } else if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
            // if a file was selected, find out which one
            m_file = (File)e.getNewValue();
            m_mode = getMode();
            update = true;
        }

        // update the preview accordingly
        if (update) {
            m_thumbnail = null;
            if (isShowing()) {
                if (m_mode == (C_MODE_IMAGE)) {
                    loadImage();
                    repaint();
                } else if (m_mode == (C_MODE_TEXT)) {
                    loadText();
                    repaint();
                } else {
                    repaint();
                }
            }
        }
    }

    /**
     * Gets the preview draw mode, depending on the file extension.<p>
     * 
     * @return preview draw mode
     */
    private int getMode() {

        int mode = C_MODE_EMPTY;
        String extension = FileUploadUtils.getExtension(m_file);
        if (extension != null) {
            if ((extension.equals("gif"))
                || (extension.equals("jpg"))
                || (extension.equals("jpeg"))
                || (extension.equals("png"))) {
                mode = C_MODE_IMAGE;
            } else if ((extension.equals("txt"))
                || (extension.equals("ini"))
                || (extension.equals("bat"))
                || (extension.equals("java") || (extension.equals("sys")))) {
                mode = C_MODE_TEXT;
            }
        }
        return mode;
    }

    /**
     * Reads the first 10 lines of e text file.<p>
     */
    private void loadText() {

        m_text = new String[35];
        BufferedReader fileStream = null;
        try {
            fileStream = new BufferedReader(new FileReader(m_file));
            for (int i = 0; i < 35; i++) {
                m_text[i] = fileStream.readLine();
            }
        } catch (Exception e) {
            // ignore
        } finally {
            try {
                if (fileStream != null) {
                    fileStream.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }
}