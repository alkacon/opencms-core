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

package org.opencms.ui.report;

import org.opencms.i18n.CmsEncoder;
import org.opencms.ui.shared.rpc.I_CmsReportClientRpc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * Widget that can be used to view a running report that is not generated specifically by an OpenCms report thread,
 * but from the text written to the stream provided by this widget.
 */
public class CmsStreamReportWidget extends CmsReportWidget {

    /**
     * Helper class which transfers the written data to the buffer of the report widget.
     */
    public class ReportStream extends ByteArrayOutputStream {

        /**
         * @see java.io.OutputStream#flush()
         */
        @SuppressWarnings("synthetic-access")
        @Override
        public void flush() {

            // since we wrap a PrintStream with autoflush around this,
            // flush is called after every line
            synchronized (m_buffer) {
                String content = "";
                byte[] data = toByteArray();
                try {
                    content = new String(data, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // ignore
                }
                m_buffer.append(content);
                reset();
                writeToDelegate(data);
            }

        }
    }

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** A second stream to write the data to. */
    private OutputStream m_delegateStream;

    /** Flag which signals that no further output will be written to the stream and that the report is finished. */
    private boolean m_finish;

    /** The buffer for the text which has been written to the stream, but not yet sent to the client. */
    private StringBuilder m_buffer = new StringBuilder();

    /** The stream whose input is written to the report. */
    private PrintStream m_out;

    /**
     * Creates a new instance.
     */
    public CmsStreamReportWidget() {

        super();

        try {
            m_out = new PrintStream(new ReportStream(), true, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // ignore
        }
    }

    /**
     * Call this after all output has been written to the stream.<p>
     *
     * This does not directly call the 'report finished' handlers, they will be only
     * called after the next RPC call from the client which fetches the report updates.
     */
    public void finish() {

        m_finish = true;
    }

    /**
     * Gets the stream.
     *
     * @return the stream
     */
    public PrintStream getStream() {

        return m_out;
    }

    /**
     * @see org.opencms.ui.report.CmsReportWidget#requestReportUpdate()
     */
    @Override
    public void requestReportUpdate() {

        String content = "";
        synchronized (m_buffer) {
            content = m_buffer.toString();
            m_buffer.setLength(0);
        }
        String html;
        if ((content.length() == 0) && m_finish) {
            html = null;
            try {
                runReportFinishedHandlers();
            } catch (Exception e) {
                // ignore
            }
        } else {
            html = convertOutputToHtml(content);
        }
        getRpcProxy(I_CmsReportClientRpc.class).handleReportUpdate(html);
    }

    /**
     * Sets a second stream to write the report output to (usually a log file).
     *
     * @param stream the second stream to write the data to
     */
    public void setDelegateStream(OutputStream stream) {

        m_delegateStream = stream;
    }

    /**
     * Converts the text stream data to HTML form.
     *
     * @param content the content to convert
     * @return the HTML version of the content
     */
    private String convertOutputToHtml(String content) {

        if (content.length() == 0) {
            return "";
        }
        StringBuilder buffer = new StringBuilder();
        for (String line : content.split("\n")) {
            buffer.append(CmsEncoder.escapeXml(line) + "<br>");
        }
        return buffer.toString();
    }

    /**
     * Writes data to delegate stream if it has been set.
     *
     * @param data the data to write
     */
    private void writeToDelegate(byte[] data) {

        if (m_delegateStream != null) {
            try {
                m_delegateStream.write(data);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
