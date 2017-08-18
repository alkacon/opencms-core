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

package org.opencms.jsp.util;

import org.opencms.util.CmsStringUtil;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides convenient functions to calculate the with of bootstrap columns.<p>
 */
public class CmsJspBootstrapBean {

    /** The calculated grid columns width in percent (initial value is 100% for all). */
    protected double[] m_column = {100.0D, 100.0D, 100.0D, 100.0D, 100.0D};

    /** The bootstrap gutter size. */
    private int m_gutter = 30;

    /** The grid columns width in percent. */
    protected int[] m_gridSize = {375, 750, 970, 1170, 1400};

    /** The CSS string this bootstrap bean was initialized with. */
    private String m_css;

    /** The array of parent CSS classes. */
    private String[] m_cssArray;

    /** Indicates if this bootstrap bean was initialized with at least one column. */
    private boolean m_initialized;

    /** public empty constructor for use on JSP.<p> */
    public CmsJspBootstrapBean() {

        // NOOP
    }

    /**
     * Add a new layer of grid information.<p>
     *
     * @param gridCols an array that holds the grid column width information
     */
    public void addLayer(int[] gridCols) {

        m_initialized = true;
        for (int i = 0; i < 5; i++) {
            m_column[i] *= gridCols[i] / 12.0D;
        }
    }

    /**
     * Add a new layer of grid information.<p>
     *
     * @param gridCss the CSS that holds the grid column width information
     *
     * @return <code>true</code> if the layer contained grid classes relevant for the size calculation
     */
    public boolean addLayer(String gridCss) {

        int xs = -1;
        int sm = -1;
        int md = -1;
        int lg = -1;
        int xl = -1;

        String[] items = gridCss.toLowerCase().split("\\s+");
        for (String i : items) {
            if (i.startsWith("col-")) {
                String iSub = i.substring(4);
                if (iSub.startsWith("xs-")) {
                    xs = parseCol(i);
                } else if (iSub.startsWith("sm-")) {
                    sm = parseCol(i);
                } else if (iSub.startsWith("md-")) {
                    md = parseCol(i);
                } else if (iSub.startsWith("lg-")) {
                    lg = parseCol(i);
                } else if (iSub.startsWith("xl-")) {
                    xl = parseCol(i);
                }
            } else if (i.startsWith("hidden-")) {
                String iSub = i.substring(7);
                if (iSub.startsWith("xs")) {
                    xs = 0;
                } else if (iSub.startsWith("sm")) {
                    sm = 0;
                } else if (iSub.startsWith("md")) {
                    md = 0;
                } else if (iSub.startsWith("lg")) {
                    lg = 0;
                } else if (iSub.startsWith("xl")) {
                    xl = 0;
                }
            }
        }

        int last = 12;
        xs = xs < 0 ? last : xs;
        last = xs <= 0 ? last : xs;
        sm = sm < 0 ? last : sm;
        last = sm <= 0 ? last : sm;
        md = md < 0 ? last : md;
        last = md <= 0 ? last : md;
        lg = lg < 0 ? last : lg;
        last = lg <= 0 ? last : lg;
        xl = xl < 0 ? last : xl;

        boolean newLayer = (last != 12);
        if (newLayer) {
            int[] result = {xs, sm, md, lg, xl};
            addLayer(result);
        }
        return newLayer;
    }

    /**
     * Returns the CSS this bean was initialized width.<p>
     *
     * @return the CSS this bean was initialized width
     */
    public String getCss() {

        if ((m_css == null) && (m_cssArray != null)) {
            StringBuffer result = new StringBuffer(128);
            for (int i = 0; i < m_cssArray.length; i++) {
                if (i > 0) {
                    result.append(':');
                }
                result.append(m_cssArray[i]);
            }
            m_css = result.toString();
        }
        return m_css;
    }

    /**
     * Returns the array of parent CSS classes.<p>
     *
     * @return the array of parent CSS classes
     */
    public String[] getCssArray() {

        return m_cssArray;
    }

    /**
     * Returns the gutter size this bean was initialized width.<p>
     *
     * @return the gutter size this bean was initialized width
     */
    public int getGutter() {

        return m_gutter;
    }

    /**
     * Returns <code>true</code> if this bootstrap bean has calculated grid information.<p>
     *
     * @return <code>true</code> if this bootstrap bean has calculated grid information
     */
    public boolean getIsInitialized() {

        return m_initialized;
    }

    /**
     * Returns the column pixel width calculated for the LG screen size.<p>
     *
     * @return the column pixel width calculated for the LG screen size
     */
    public int getSizeLg() {

        return getSize(3);
    }

    /**
     * Returns the column pixel width calculated for the MD screen size.<p>
     *
     * @return the column pixel width calculated for the MD screen size
     */
    public int getSizeMd() {

        return getSize(2);
    }

    /**
     * Returns the column pixel width calculated for the SM screen size.<p>
     *
     * @return the column pixel width calculated for the SM screen size
     */
    public int getSizeSm() {

        return getSize(1);
    }

    /**
     * Returns the column pixel width calculated for the XL screen size.<p>
     *
     * @return the column pixel width calculated for the XL screen size
     */
    public int getSizeXl() {

        return getSize(4);
    }

    /**
     * Returns the column pixel width calculated for the XS screen size.<p>
     *
     * @return the column pixel width calculated for the XS screen size
     */
    public int getSizeXs() {

        return getSize(0);
    }

    /**
     * Initialize the CSS used for this bootstrap bean.<p>
     *
     * @param css the CSS used for this bootstrap bean
     */
    public void setCss(String css) {

        if (css != null) {
            m_css = css;
            List<String> layers = CmsStringUtil.splitAsList(css, ':', true);
            m_cssArray = new String[layers.size()];
            int last = layers.size();
            for (String layer : layers) {
                addLayer(layer);
                m_cssArray[--last] = layer;
            }
        }
    }

    /**
     * Sets the array of parent CSS classes.<p>
     *
     * @param cssArray the array of parent CSS classes to set
     */
    public void setCssArray(String[] cssArray) {

        if ((cssArray != null) && (cssArray.length > 0)) {
            List<String> result = new ArrayList<String>();
            for (String layer : cssArray) {
                if (layer.equals("#")) {
                    // start layer reached, following layers are 'real' get request parameters
                    break;
                } else {
                    if (addLayer(layer)) {
                        result.add(layer);
                    }
                }
            }
            m_cssArray = result.toArray(new String[result.size()]);
        }
    }

    /**
     * Sets the gutter used in the bootstrap grid.<p>
     *
     * @param gutter the gutter used in the bootstrap grid
     */
    public void setGutter(int gutter) {

        m_gutter = gutter;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        return "xs="
            + getSizeXs()
            + "px("
            + nf.format(m_column[0])
            + "%) sm="
            + getSizeSm()
            + "px("
            + nf.format(m_column[1])
            + "%) md="
            + getSizeMd()
            + "px("
            + nf.format(m_column[2])
            + "%) lg="
            + getSizeLg()
            + "px("
            + nf.format(m_column[3])
            + "%) xl="
            + getSizeXl()
            + "px("
            + nf.format(m_column[4])
            + "%)";
    }

    /**
     * Calculate the pixel size of a grid column.<p>
     *
     * @param gridIndex the grid index to get the size for
     *
     * @return the pixel size of a grid column
     */
    protected int getSize(int gridIndex) {

        int gridSize = m_gridSize[gridIndex];
        double spaceFactor = m_column[gridIndex];

        int result = (int)Math.round((gridSize * spaceFactor) / 100.0);
        result = result > m_gutter ? result - m_gutter : 0;
        return result;
    }

    /**
     * Parses a bootstrap column CSS class like 'col-md-5'.<p>
     *
     * @param colStr the bootstrap column CSS class to parse
     *
     * @return the number part of the CSS class
     */
    protected int parseCol(String colStr) {

        int result;
        String number = colStr.substring(colStr.lastIndexOf('-') + 1);
        try {
            result = Integer.valueOf(number).intValue();
        } catch (NumberFormatException e) {
            result = -1;
        }
        return result;
    }
}