/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/CmsRfsFileViewer.java,v $
 * Date   : $Date: 2005/06/15 12:51:24 $
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

package org.opencms.util;

import org.opencms.file.CmsRfsException;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * @author  Achim Westermann (a.westermann@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 6.0
 */
public class CmsRfsFileViewer implements Cloneable {

    /**
     * An index for a file from line numbers to character positions 
     * in the underlying streams of {@link Reader} implementations.<p> 
     * 
     * It enables to skip big parts of the underlying file when trying to access 
     * file content at later line numbers of a file. <p>
     * 
     */
    private static final class CmsRfsFileLineIndexInfo {

        /**
         * A worker for building a line break index for a file.<p> 
         */
        private final class LineIndexer implements Runnable {

            private File m_file;

            /**
             * Creates a new unstarted instance.<p> 
             * 
             * @param f the file to build an index on the linebreaks
             */
            private LineIndexer(File f) {

                m_file = f;
            }

            /**
             * Does the work of building the line break index.<p>
             * 
             * @see java.lang.Runnable#run()
             */
            public void run() {

                m_buildingIndex = true;
                try {
                    long linePos = 0;
                    LineNumberReader reader = new LineNumberReader(new BufferedReader(new InputStreamReader(
                        new FileInputStream(m_file))));
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        linePos += line.length();
                        CmsRfsFileLineIndexInfo.this.addLineBreakPosition(linePos);
                        synchronized (CmsRfsFileLineIndexInfo.this) {
                            CmsRfsFileLineIndexInfo.this.notify();
                        }
                    }

                } catch (IOException e) {
                    LOG.error(e.toString());
                }
                m_buildingIndex = false;
                synchronized (CmsRfsFileLineIndexInfo.this) {
                    CmsRfsFileLineIndexInfo.this.notify();
                }
            }
        }

        /** As long as the LineIndexer Thread builds this is true.  */
        protected boolean m_buildingIndex;

        /**
         * The internal list with break positions in bytes.<p>
         * 
         * Fast for random access which is 
         * used for the query for the n<sup>th</sup> line break.<p>
         */
        private List m_breakPositions;

        /**
         * Constructor with location of the RFS - file to build an line number index for.<p>
         * 
         * @param filePath the underlying file to build an index on line breaks for.
         */
        protected CmsRfsFileLineIndexInfo(String filePath) {

            // first line at 0.
            m_breakPositions = new ArrayList(200);
            addLineBreakPosition(0);
            // use a ThreadPool if available. 
            LineIndexer indexer = new LineIndexer(new File(filePath));
            new Thread(indexer).start();
        }

        /**
         * Returns the file offset to position zero in characters of this break or 
         * offset to the character position where the last 10 lines begin if the 
         * requested linebreak is higher than the amount of lines in the underlying file.<p>
         * 
         * @param breakNumber the number defining that this break position marks the  n<sup>th</sup> break 
         *        in the underlying file.  
         * 
         * @return the file offset to position zero in characters of this break or 
         *         offset to the character position where the last 10 lines begin if the 
         *         requested linebreak is higher than the amount of lines in the underlying file
         */
        public long getLineBreakPosition(int breakNumber) {

            int sz = 0;
            synchronized (m_breakPositions) {
                sz = m_breakPositions.size();
            }
            while (breakNumber > sz && m_buildingIndex) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    LOG.warn(e);
                }
                synchronized (m_breakPositions) {
                    sz = m_breakPositions.size();
                }
            }
            if (breakNumber <= sz) {
                // valid number of line...
                return ((Number)m_breakPositions.get(breakNumber)).longValue();
            } else {
                // out of range: don't throw exception, return last 10 lines.
                return ((Number)m_breakPositions.get(sz - 10)).longValue();
            }
        }

        /**
         * Adds a further line break position to the internal <code>List</code> that marks 
         * the "list.size()<sup>th</sup>" line break at position <code>breakPosition</code>.<p>
         * 
         * As a result of this policy the order of calls to this method has to be synchronized with ascending 
         * break positions. Therefore this mehod is private and only intended for an internal line indexing Thread.<p> 
         * 
         * @param breakPosition a position in the character stream (<code>{@link Reader}</code>) where the lates line 
         *                      break was found. 
         */
        protected void addLineBreakPosition(long breakPosition) {

            synchronized (m_breakPositions) {
                m_breakPositions.add(new Long(breakPosition));
            }
        }
    }

    /** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(CmsRfsFileViewer.class);

    /**
     *  Maps file paths to instances of 
     * <code>{@link CmsRfsFileLineIndexInfo}</code> instances.
     **/
    private Map m_fileName2lineIndex;

    /** The path to the underlying file. */
    private String m_filePath;

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
    private int m_windowPos;

    /** The amount of lines to show. */
    private int m_windowSize;

    /**
     * Creates an instance with default settings that tries to use the log file path obtained 
     * from <code>{@link OpenCms}'s {@link org.opencms.main.CmsSystemInfo}</code> instance.<p>
     * 
     * If the log file path is invalid or not configured correctly a logging is performed and the 
     * path remains empty to allow user-specified file selection.<p>
     */
    public CmsRfsFileViewer() {

        try {
            setFilePath(OpenCms.getSystemInfo().getLogFileRfsPath());
            m_isLogfile = true;
            m_fileName2lineIndex = new HashMap();
            m_filePath = OpenCms.getSystemInfo().getLogFileRfsPath();
            m_windowSize = 200;
        } catch (CmsRfsException ex) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(ex.getLocalizedMessage());
            }
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
    public Object clone() {

        CmsRfsFileViewer clone = new CmsRfsFileViewer();
        // strings are immutable: no outside modification possible.
        clone.m_filePath = m_filePath;
        clone.m_isLogfile = m_isLogfile;
        clone.m_windowPos = m_windowPos;
        clone.m_windowSize = m_windowSize;
        // allow clone-modifications. 
        clone.m_frozen = false;
        return clone;
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
     * Get the amount of lines (or entries depending on wether a standard log file is shown) 
     * to display per page. <p>
     * 
     * @return the amount of lines to display per page
     */
    public int getWindowSize() {

        return m_windowSize;
    }

    /**
     * Return the view portion of lines of text from the underlying file.<p>
     * 
     * @return the view portion of lines of text from the underlying file
     * @throws CmsRfsException if something goes wrong
     */
    public String readFilePortion() throws CmsRfsException {

        CmsRfsFileLineIndexInfo lineInfo = initIndexer(m_filePath);
        try {
            Reader reader = new InputStreamReader(new FileInputStream(m_filePath));
            long skip = lineInfo.getLineBreakPosition(m_windowPos * m_windowSize);
            reader.skip(skip);
            LineNumberReader lineReader = new LineNumberReader(reader);
            StringBuffer ret = new StringBuffer();
            String read = lineReader.readLine();
            for (int i = m_windowSize; i > 0 && read != null; i--) {
                ret.append(lineReader.readLine());
                ret.append('\n');
                read = lineReader.readLine();
            }
            return CmsEncoder.escapeXml(ret.toString());
        } catch (IOException ioex) {
            CmsRfsException ex = new CmsRfsException(Messages.get().container(
                Messages.ERR_FILE_ARG_ACCESS_1,
                m_filePath), ioex);
            throw ex;
        }
    }

    /**
     * Set the path in the real file system that points to the file 
     * that should be displayed.<p>
     * 
     * This method will only suceed if the file specified by the <code>path</code> 
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
        if (CmsStringUtil.isEmpty(path)) {
            throw new CmsRfsException(Messages.get().container(
                Messages.ERR_FILE_ARG_EMPTY_1,
                new Object[] {String.valueOf(path)}));
        }
        try {
            // just for validation :
            File file = new File(path);
            if (file.isDirectory()) {
                throw new CmsRfsException(Messages.get().container(
                    Messages.ERR_FILE_ARG_IS_FOLDER_1,
                    new Object[] {String.valueOf(path)}));
            } else if (!file.isFile()) {
                throw new CmsRfsException(Messages.get().container(
                    Messages.ERR_FILE_ARG_NOT_FOUND_1,
                    new Object[] {String.valueOf(path)}));

            } else if (!file.canRead()) {
                throw new CmsRfsException(Messages.get().container(
                    Messages.ERR_FILE_ARG_NOT_READ_1,
                    new Object[] {String.valueOf(path)}));
            } else {
                m_filePath = file.getCanonicalPath();
            }
        } catch (FileNotFoundException fnfe) {
            throw new CmsRfsException(Messages.get().container(
                Messages.ERR_FILE_ARG_NOT_FOUND_1,
                new Object[] {String.valueOf(path)}), fnfe);

        } catch (IOException ioex) {

            throw new CmsRfsException(Messages.get().container(
                Messages.ERR_FILE_ARG_ACCESS_1,
                new Object[] {String.valueOf(path)}), ioex);

        }
    }

    /**
     * Package friendly access that allows the <code>{@link org.opencms.workplace.CmsWorkplaceManager}</code> 
     * to "freeze" this instance within the system-wide assignment in it's 
     * <code>{@link org.opencms.workplace.CmsWorkplaceManager#setFileViewSettings(org.opencms.file.CmsObject, CmsRfsFileViewer)}</code> method.<p>
     * 
     * @param frozen if true this instance will freeze and throw <code>CmsRuntimeExceptions</code> upon setter invocations  
     * @throws CmsRuntimeException if the configuration of this instance has been frozen 
     *                             ({@link #setFrozen(boolean)})
     * 
     */
    public void setFrozen(boolean frozen) throws CmsRuntimeException {

        m_frozen = frozen;
    }

    /**
     * Set if the internal file is in standard logfile format (true) or not (false).<p>  
     * 
     * If set to true the file might be 
     * treated / displayed in a more convenient format than standard files in future.<p>
     * 
     * @param isLogfile determines if the internal file is in standard logfile format (true) or not (false)
     * @throws CmsRuntimeException if the configuration of this instance has been frozen 
     *                             ({@link #setFrozen(boolean)})
     */
    public void setIsLogfile(boolean isLogfile) throws CmsRuntimeException {

        checkFrozen();
        m_isLogfile = isLogfile;
    }

    /**
     * Sets the start position of the current display.<p>
     * 
     * This is a count of "windows" that 
     * consist of viewable text with "windowSize" lines of text (for a non-standard log file) or 
     * log-entries (for a standard log file).<p>
     * 
     * @param windowPos the start position of the current display to set 
     * @throws CmsRuntimeException if the configuration of this instance has been frozen 
     *                             ({@link #setFrozen(boolean)})
     */
    public void setWindowPos(int windowPos) throws CmsRuntimeException {

        checkFrozen();
        m_windowPos = windowPos;
    }

    /**
     * Set the amount of lines (or entries depending on wether a standard log file is shown) 
     * to display per page.<p>
     * 
     * @param windowSize the amount of lines to display per page 
     * @throws CmsRuntimeException if the configuration of this instance has been frozen 
     *                             ({@link #setFrozen(boolean)})
     * 
     */
    public void setWindowSize(int windowSize) throws CmsRuntimeException {

        checkFrozen();
        m_windowSize = windowSize;
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
     * Return a line number index for the given file path.<p>
     * 
     * Implementation allows control of eventual caching of line number indexes 
     * for files. <p>
     * 
     * 
     * @param filePath the String denoting a valid path to a file in the real file system 
     * @return a line number index for the given file path
     */
    private CmsRfsFileLineIndexInfo initIndexer(String filePath) {

        synchronized (m_fileName2lineIndex) {
            CmsRfsFileLineIndexInfo ret = (CmsRfsFileLineIndexInfo)m_fileName2lineIndex.get(filePath);
            if (ret != null) {
                // nop
            } else {
                // create a new instance, this will be slow: 
                ret = new CmsRfsFileLineIndexInfo(filePath);

            }
            return ret;
        }
    }
}