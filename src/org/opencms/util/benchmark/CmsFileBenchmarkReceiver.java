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

package org.opencms.util.benchmark;

import org.opencms.main.CmsLog;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;

/**
 * Appends benchmark samples to a file.
 */
public class CmsFileBenchmarkReceiver implements CmsBenchmarkTable.Receiver {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFileBenchmarkReceiver.class);

    /** The target file path. */
    private String m_path;

    /** The benchmark group. */
    private String m_group;

    /** The benchmark name. */
    private String m_benchmark;

    /**
     * Creates a new instance and configures it via the Java system properties.<p>
     *
     * The following properties are used:
     * <ul>
     * <li>opencms.benchmark.file: the output file
     * <li>opencms.benchmark.name: the benchmark name
     * <li>opencms.benchmark.group: the benchmark group
     * </ul>
     */
    public CmsFileBenchmarkReceiver() {

        Properties prop = System.getProperties();
        String prefix = "opencms.benchmark.";
        m_path = prop.getProperty(prefix + "file");
        m_benchmark = prop.getProperty(prefix + "name");
        m_group = prop.getProperty(prefix + "group");
    }

    /**
     * Creates a new instance.
     *
     * @param path the output file path
     * @param benchmark the benchmark name
     * @param group the benchmark group
     */
    public CmsFileBenchmarkReceiver(String path, String benchmark, String group) {

        m_path = path;
        m_group = group;
        m_benchmark = benchmark;
    }

    /**
     * @see org.opencms.util.benchmark.CmsBenchmarkTable.Receiver#receiveSample(java.lang.String, long)
     */
    public synchronized void receiveSample(String sampleName, long sampleTime) {

        try (FileWriter fw = new FileWriter(m_path, true)) {
            String line = System.currentTimeMillis()
                + " "
                + m_benchmark
                + " "
                + m_group
                + " "
                + sampleName
                + " "
                + sampleTime
                + "\n";
            fw.write(line);
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            e.printStackTrace();
        }

    }

}
