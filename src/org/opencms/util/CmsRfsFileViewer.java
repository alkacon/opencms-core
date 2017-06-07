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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.util;

import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Stack;

import org.apache.commons.logging.Log;

/**
 * The representation of a RFS file along with the settings to provide
 * access to certain portions (amount of lines) of it. <p>
 *
 * Most often the underlying file will be the OpenCms logfile. <p>
 *
 * The portion of the file that is shown is defined by a "window" of "windowSize" lines of text
 * at a position "windowPosition" which is an enumeration of windows in ascending order. <p>
 *
 * @since 6.0.0
 */
public class CmsRfsFileViewer implements Cloneable {

    /** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(CmsRfsFileViewer.class);

    /** Decides whether the view onto the underlying file via readFilePortion is enabled. */
    private boolean m_enabled;

    /** The character encoding of the underlying file. */
    private Charset m_fileEncoding;

    /** The path to the underlying file. */
    protected String m_filePath;

    /** The path to the root for all accessible files. */
    protected String m_rootPath;

    /**
     * If value is <code>true</code>, all setter methods will throw a
     * <code>{@link CmsRuntimeException}</code><p>.
     *
     * Only the method <code>{@link #clone()}</code> returns a clone that has set this
     * member to <code>false</code> allowing modification to take place.<p>
     */
    private boolean m_frozen;

    /**
     * If true the represented file is a standard OpenCms log file and may be displayed
     * in more convenient ways (in future versions) because the format is known.
     */
    private boolean m_isLogfile;

    /** The current window (numbered from zero to amount of possible different windows).  */
    protected int m_windowPos;

    /** The amount of lines to show. */
    protected int m_windowSize;

    /**
     * Creates an instance with default settings that tries to use the log file path obtained
     * from <code>{@link OpenCms}'s {@link org.opencms.main.CmsSystemInfo}</code> instance.<p>
     *
     * If the log file path is invalid or not configured correctly a logging is performed and the
     * path remains empty to allow user-specified file selection.<p>
     */
    public CmsRfsFileViewer() {

        if (OpenCms.getRunLevel() >= OpenCms.RUNLEVEL_3_SHELL_ACCESS) {
            m_rootPath = new File(OpenCms.getSystemInfo().getLogFileRfsPath()).getParent();
        }
        m_isLogfile = true;
        // system default charset: see http://java.sun.com/j2se/corejava/intl/reference/faqs/index.html#default-encoding
        m_fileEncoding = Charset.forName(new OutputStreamWriter(new ByteArrayOutputStream()).getEncoding());
        m_enabled = true;
        m_windowSize = 200;

    }

    /**
     * Internal helper that throws a <code>{@link CmsRuntimeException}</code> if the
     * configuration of this instance has been frozen ({@link #setFrozen(boolean)}).<p>
     *
     * @throws CmsRuntimeException if the configuration of this instance has been frozen
     *                             ({@link #setFrozen(boolean)})
     */
    private void checkFrozen() throws CmsRuntimeException {

        if (m_frozen) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_FILE_VIEW_SETTINGS_FROZEN_0));
        }
    }

    /**
     * Returns a clone of this file view settings that is not "frozen" and therefore allows modifications.<p>
     *
     * Every instance that plans to modify settings has to obtain a clone first that may be
     * modified. The original instance returned from
     * (<code>{@link org.opencms.workplace.CmsWorkplaceManager#getFileViewSettings()}</code>) will throw
     * a <code>{@link CmsRuntimeException}</code> for each setter invocation. <p>
     *
     * @return a clone of this file view settings that is not "frozen" and therefore allows modifications
     */
    @Override
    public Object clone() {

        // first run after installation: filePath & rootPath is null:
        if (m_filePath == null) {
            // below that runlevel the following call  will fail (not initialized from config yet):
            if (OpenCms.getRunLevel() >= OpenCms.RUNLEVEL_3_SHELL_ACCESS) {
                m_filePath = OpenCms.getSystemInfo().getLogFileRfsPath();
            }
        }
        if (m_rootPath == null) {
            if (OpenCms.getRunLevel() >= OpenCms.RUNLEVEL_3_SHELL_ACCESS) {
                m_rootPath = new File(OpenCms.getSystemInfo().getLogFileRfsPath()).getParent();
            }
        }
        CmsRfsFileViewer clone = new CmsRfsFileViewer();
        clone.m_rootPath = m_rootPath;
        try {
            // strings are immutable: no outside modification possible.
            clone.setFilePath(m_filePath);
        } catch (CmsRfsException e) {
            // will never happen because m_filePath was verified in setFilePath of this instance.
        } catch (CmsRuntimeException e) {
            // will never happen because m_filePath was verified in setFilePath of this instance.
        }
        clone.m_fileEncoding = m_fileEncoding;
        clone.m_isLogfile = m_isLogfile;
        clone.m_enabled = m_enabled;
        //clone.m_windowPos = m_windowPos;
        clone.setWindowSize(m_windowSize);
        // allow clone-modifications.
        clone.m_frozen = false;
        return clone;
    }

    /**
     * Returns the canonical name of the character encoding of the underlying file.<p>
     *
     * If no special choice is fed into
     * <code>{@link #setFileEncoding(String)}</code> before this call
     * always the system default character encoding is returned.<p>
     *
     * This value may be ignored outside and will be ignored inside if the
     * underlying does not contain textual content.<p>
     *
     * @return the canonical name of the character encoding of the underlying file
     */
    public String getFileEncoding() {

        return m_fileEncoding.name();
    }

    /**
     * Returns the path denoting the file that is accessed.<p>
     *
     * @return the path denoting the file that is accessed
     */
    public String getFilePath() {

        return m_filePath;
    }

    /**
     * Returns true if the view's internal file path points to a log file in standard OpenCms format.<p>
     *
     * @return true if the view's internal file path points to a log file in standard OpenCms format
     */
    public boolean getIsLogfile() {

        // method name is bean-convention of apache.commons.beanutils (unlike eclipse's convention for booleans)
        return m_isLogfile;
    }

    /**
     * Returns the start position of the current display.<p>
     *
     * This is a count of "windows" that
     * consist of viewable text with "windowSize" lines of text (for a non-standard log file) or
     * log-entries (for a standard log file).<p>
     *
     * @return the start position of the current display
     */
    public int getWindowPos() {

        return m_windowPos;
    }

    /**
     * Returns the path denoting the root folder for all accessible files.<p>
     *
     * @return the path denoting the root folder for all accessible files
     */
    public String getRootPath() {

        return m_rootPath;
    }

    /**
     * Get the amount of lines (or entries depending on whether a standard log file is shown)
     * to display per page. <p>
     *
     * @return the amount of lines to display per page
     */
    public int getWindowSize() {

        return m_windowSize;
    }

    /**
     * Returns true if this view upon the underlying file via
     * <code>{@link #readFilePortion()}</code> is enabled.<p>
     *
     *
     * @return true if this view upon the underlying file via
     * <code>{@link #readFilePortion()}</code> is enabled.<p>
     */
    public boolean isEnabled() {

        return m_enabled;
    }

    /**
     * Return the view portion of lines of text from the underlying file or an
     * empty String if <code>{@link #isEnabled()}</code> returns <code>false</code>.<p>
     *
     * @return the view portion of lines of text from the underlying file or an
     *         empty String if <code>{@link #isEnabled()}</code> returns <code>false</code>
     * @throws CmsRfsException if something goes wrong
     */
    public String readFilePortion() throws CmsRfsException {

        if (m_enabled) {
            // if we want to view the log file we have to set the internal m_windowPos to the last window
            // to view the end:
            int lines = -1;
            int startLine;
            if (m_isLogfile) {
                lines = scrollToFileEnd();
                // for logfile mode we show the last window of window size:
                // it could be possible that only 4 lines are in the last window
                // (e.g.: 123 lines with windowsize 10 -> last window has 3 lines)
                // so we ignore the window semantics and show the n last lines:
                startLine = lines - m_windowSize;
            } else {
                m_windowPos = 0;
                startLine = m_windowPos * m_windowSize;
            }
            LineNumberReader reader = null;
            try {
                // don't make the buffer too big, just big enough for windowSize lines (estimation: avg. of 200 characters per line)
                // to save reading too much (this optimizes to read the first windows, much later windows will be slower...)
                reader = new LineNumberReader(
                    new BufferedReader(new InputStreamReader(new FileInputStream(m_filePath), m_fileEncoding)),
                    m_windowSize * 200);
                int currentLine = 0;
                // skip the lines to the current window:
                while (startLine > currentLine) {
                    reader.readLine();
                    currentLine++;
                }
                StringBuffer result = new StringBuffer();
                String read = reader.readLine();

                // logfile treatment is different
                // we invert the lines: latest come first
                if (m_isLogfile) {
                    // stack is java hall of shame member... but standard
                    Stack<String> inverter = new Stack<String>();
                    for (int i = m_windowSize; (i > 0) && (read != null); i--) {
                        inverter.push(read);
                        read = reader.readLine();
                    }
                    // pop-off:
                    while (!inverter.isEmpty()) {
                        result.append(inverter.pop());
                        result.append('\n');
                    }
                } else {
                    for (int i = m_windowSize; (i > 0) && (read != null); i--) {
                        result.append(read);
                        result.append('\n');
                        read = reader.readLine();
                    }
                }
                return CmsEncoder.escapeXml(result.toString());
            } catch (IOException ioex) {
                CmsRfsException ex = new CmsRfsException(
                    Messages.get().container(Messages.ERR_FILE_ARG_ACCESS_1, m_filePath),
                    ioex);
                throw ex;
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            }
        } else {
            return Messages.get().getBundle().key(Messages.GUI_FILE_VIEW_NO_PREVIEW_0);
        }
    }

    /**
     * Internally sets the member <code>m_windowPos</code> to the last available
     * window of <code>m_windowSize</code> windows to let further calls to
     * <code>{@link #readFilePortion()}</code> display the end of the file. <p>
     *
     * This method is triggered when a new file is chosen
     * (<code>{@link #setFilePath(String)}</code>) because the amount of lines changes.
     * This method is also triggered when a different window size is chosen
     * (<code>{@link #setWindowSize(int)}</code>) because the amount of lines to display change.
     *
     * @return the amount of lines in the file to view
     */
    private int scrollToFileEnd() {

        int lines = 0;
        if (OpenCms.getRunLevel() < OpenCms.RUNLEVEL_3_SHELL_ACCESS) {
            // no scrolling if system not yet fully initialized
        } else {
            LineNumberReader reader = null;
            // shift the window position to the end of the file: this is expensive but OK for ocs logfiles as they
            // are ltd. to 2 MB
            try {
                reader = new LineNumberReader(
                    new BufferedReader(new InputStreamReader(new FileInputStream(m_filePath))));
                while (reader.readLine() != null) {
                    lines++;
                }
                reader.close();
                // if 11.75 windows are available, we don't want to end on window nr. 10
                int availWindows = (int)Math.ceil((double)lines / (double)m_windowSize);
                // we start with window 0
                m_windowPos = availWindows - 1;
            } catch (IOException ioex) {
                LOG.error("Unable to scroll file " + m_filePath + " to end. Ensure that it exists. ");
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Throwable f) {
                        LOG.info("Unable to close reader of file " + m_filePath, f);
                    }
                }
            }
        }
        return lines;
    }

    /**
     * Set the boolean that decides if the view to the underlying file via
     * <code>{@link #readFilePortion()}</code> is enabled.<p>
     *
     * @param preview the boolean that decides if the view to the underlying file via
     *        <code>{@link #readFilePortion()}</code> is enabled
     */
    public void setEnabled(boolean preview) {

        m_enabled = preview;
    }

    /**
     * Set the character encoding of the underlying file.<p>
     *
     * The given String has to match a valid char set name (canonical or alias)
     * of one of the system's supported <code>{@link Charset}</code> instances
     * (see <code>{@link Charset#forName(java.lang.String)}</code>).<p>
     *
     * This setting will be used for reading the file. This enables to correctly
     * display files with text in various encodings in UIs.<p>
     *
     * @param fileEncoding the character encoding of the underlying file to set
     */
    public void setFileEncoding(String fileEncoding) {

        checkFrozen();
        try {
            m_fileEncoding = Charset.forName(fileEncoding);
        } catch (IllegalCharsetNameException icne) {
            throw new CmsIllegalArgumentException(
                Messages.get().container(Messages.ERR_CHARSET_ILLEGAL_NAME_1, fileEncoding));
        } catch (UnsupportedCharsetException ucse) {
            throw new CmsIllegalArgumentException(
                Messages.get().container(Messages.ERR_CHARSET_UNSUPPORTED_1, fileEncoding));

        }

    }

    /**
     * Set the path in the real file system that points to the file
     * that should be displayed.<p>
     *
     * This method will only success if the file specified by the <code>path</code>
     * argument is valid within the file system, no folder and may be read by the
     * OpenCms process on the current platform.<p>
     *
     * @param path the path in the real file system that points to the file that should be displayed to set
     *
     * @throws CmsRuntimeException if the configuration of this instance has been frozen
     * @throws CmsRfsException if the given path is invalid, does not point to a file or cannot be accessed
     */
    public void setFilePath(String path) throws CmsRfsException, CmsRuntimeException {

        checkFrozen();

        if (path != null) {
            // leading whitespace from CmsComboWidget causes exception
            path = path.trim();
        }
        if (CmsStringUtil.isEmpty(path)) {
            throw new CmsRfsException(
                Messages.get().container(Messages.ERR_FILE_ARG_EMPTY_1, new Object[] {String.valueOf(path)}));
        }
        try {
            // just for validation :
            File file = new File(path);
            if (file.isDirectory()) {
                // if wrong configuration perform self healing:
                if (OpenCms.getRunLevel() == OpenCms.RUNLEVEL_2_INITIALIZING) {
                    // this deletes the illegal entry and will default to the log file path
                    m_filePath = null;
                    m_isLogfile = true;
                } else {
                    throw new CmsRfsException(
                        Messages.get().container(
                            Messages.ERR_FILE_ARG_IS_FOLDER_1,
                            new Object[] {String.valueOf(path)}));
                }
            } else if (!file.isFile()) {
                // if wrong configuration perform self healing:
                if (OpenCms.getRunLevel() == OpenCms.RUNLEVEL_2_INITIALIZING) {
                    // this deletes the illegal entry and will default to the log file path
                    m_filePath = null;
                    m_isLogfile = true;
                } else {
                    throw new CmsRfsException(
                        Messages.get().container(
                            Messages.ERR_FILE_ARG_NOT_FOUND_1,
                            new Object[] {String.valueOf(path)}));
                }

            } else if (!file.canRead()) {
                // if wrong configuration perform self healing:
                if (OpenCms.getRunLevel() == OpenCms.RUNLEVEL_2_INITIALIZING) {
                    // this deletes the illegal entry and will default to the log file path
                    m_filePath = null;
                    m_isLogfile = true;
                } else {
                    throw new CmsRfsException(
                        Messages.get().container(
                            Messages.ERR_FILE_ARG_NOT_READ_1,
                            new Object[] {String.valueOf(path)}));
                }
            } else if ((m_rootPath != null) && !file.getCanonicalPath().startsWith(m_rootPath)) {
                // if wrong configuration perform self healing:
                if (OpenCms.getRunLevel() == OpenCms.RUNLEVEL_2_INITIALIZING) {
                    // this deletes the illegal entry and will default to the log file path
                    m_filePath = null;
                    m_isLogfile = true;
                } else {
                    throw new CmsRfsException(
                        Messages.get().container(
                            Messages.ERR_FILE_ARG_NOT_READ_1,
                            new Object[] {String.valueOf(path)}));
                }
            } else {
                m_filePath = file.getCanonicalPath();
            }
        } catch (FileNotFoundException fnfe) {
            // if wrong configuration perform self healing:
            if (OpenCms.getRunLevel() == OpenCms.RUNLEVEL_2_INITIALIZING) {
                // this deletes the illegal entry and will default to the log file path
                m_filePath = null;
                m_isLogfile = true;
            } else {
                throw new CmsRfsException(
                    Messages.get().container(Messages.ERR_FILE_ARG_NOT_FOUND_1, new Object[] {String.valueOf(path)}),
                    fnfe);
            }
        } catch (IOException ioex) {
            // if wrong configuration perform self healing:
            if (OpenCms.getRunLevel() == OpenCms.RUNLEVEL_2_INITIALIZING) {
                // this deletes the illegal entry and will default to the log file path
                m_filePath = null;
                m_isLogfile = true;
            } else {
                throw new CmsRfsException(
                    Messages.get().container(Messages.ERR_FILE_ARG_ACCESS_1, new Object[] {String.valueOf(path)}),
                    ioex);
            }

        }
    }

    /**
     * Package friendly access that allows the <code>{@link org.opencms.workplace.CmsWorkplaceManager}</code>
     * to "freeze" this instance within the system-wide assignment in it's
     * <code>{@link org.opencms.workplace.CmsWorkplaceManager#setFileViewSettings(org.opencms.file.CmsObject, CmsRfsFileViewer)}</code> method.<p>
     *
     * @param frozen if true this instance will freeze and throw <code>CmsRuntimeExceptions</code> upon setter invocations
     *
     * @throws CmsRuntimeException if the configuration of this instance has been frozen
     *                             ({@link #setFrozen(boolean)})
     */
    public void setFrozen(boolean frozen) throws CmsRuntimeException {

        m_frozen = frozen;
    }

    /**
     * Set if the internal file is in standard log file format (true) or not (false).<p>
     *
     * If set to true the file might be
     * treated / displayed in a more convenient format than standard files in future.
     * Currently it is only inverted (last lines appear first) and only the last
     * 'Window Size' lines of the file are displayed.<p>
     *
     * Do not activate this (it is possible from the log file viewer settings in the workplace
     * administration) if your selected file is no log file: The display will confuse you and
     * be more expensive (imaging scrolling a 20 MB file to view the last 200 lines). <p>
     *
     * @param isLogfile determines if the internal file is in standard log file format (true) or not (false)
     *
     * @throws CmsRuntimeException if the configuration of this instance has been frozen
     *                             ({@link #setFrozen(boolean)})
     */
    public void setIsLogfile(boolean isLogfile) throws CmsRuntimeException {

        checkFrozen();
        m_isLogfile = isLogfile;
    }

    /**
     * Set the path in the real file system that points to the folder/tree
     * containing the log files.<p>
     *
     * This method will only success if the folder specified by the <code>path</code>
     * argument is valid within the file system.<p>
     *
     * @param path the path in the real file system that points to the folder containing the log files
     *
     * @throws CmsRuntimeException if the configuration of this instance has been frozen
     * @throws CmsRfsException if the given path is invalid
     */
    public void setRootPath(String path) throws CmsRfsException, CmsRuntimeException {

        checkFrozen();

        if (path != null) {
            // leading whitespace from CmsComboWidget causes exception
            path = path.trim();
        }
        if (CmsStringUtil.isEmpty(path)) {
            throw new CmsRfsException(
                Messages.get().container(Messages.ERR_FILE_ARG_EMPTY_1, new Object[] {String.valueOf(path)}));
        }
        try {
            // just for validation :
            File file = new File(path);
            if (file.exists()) {
                m_rootPath = file.getCanonicalPath();
            } else {
                // if wrong configuration perform self healing:
                if (OpenCms.getRunLevel() == OpenCms.RUNLEVEL_2_INITIALIZING) {
                    // this deletes the illegal entry
                    m_rootPath = new File(OpenCms.getSystemInfo().getLogFileRfsPath()).getParent();
                } else {

                    throw new CmsRfsException(
                        Messages.get().container(
                            Messages.ERR_FILE_ARG_NOT_FOUND_1,
                            new Object[] {String.valueOf(path)}));
                }
            }
        } catch (IOException ioex) {
            // if wrong configuration perform self healing:
            if (OpenCms.getRunLevel() == OpenCms.RUNLEVEL_2_INITIALIZING) {
                // this deletes the illegal entry and will default to the log file path
                m_rootPath = new File(OpenCms.getSystemInfo().getLogFileRfsPath()).getParent();
            } else {

                throw new CmsRfsException(
                    Messages.get().container(Messages.ERR_FILE_ARG_ACCESS_1, new Object[] {String.valueOf(path)}),
                    ioex);
            }
        }
    }

    /**
     * Sets the start position of the current display.<p>
     *
     * This is a count of "windows" that
     * consist of viewable text with "windowSize" lines of text (for a non-standard log file) or
     * log-entries (for a standard log file).<p>
     *
     * @param windowPos the start position of the current display to set
     *
     * @throws CmsRuntimeException if the configuration of this instance has been frozen
     *                             ({@link #setFrozen(boolean)})
     */
    public void setWindowPos(int windowPos) throws CmsRuntimeException {

        checkFrozen();
        m_windowPos = windowPos;
    }

    /**
     * Set the amount of lines (or entries depending on whether a standard log file is shown)
     * to display per page.<p>
     *
     * @param windowSize the amount of lines to display per page
     *
     * @throws CmsRuntimeException if the configuration of this instance has been frozen
     *                             ({@link #setFrozen(boolean)})
     */
    public void setWindowSize(int windowSize) throws CmsRuntimeException {

        checkFrozen();
        m_windowSize = windowSize;
    }
}