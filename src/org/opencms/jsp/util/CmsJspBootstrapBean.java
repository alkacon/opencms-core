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

import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsStringUtil;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Transformer;

/**
 * Provides convenient functions to calculate the with of bootstrap columns.<p>
 */
public class CmsJspBootstrapBean {

    /**
     * Provides a Map to access ratio scaled versions of the current image.<p>
     */
    public class CmsBootstrapGridTransformer implements Transformer {

        /** The selected grid index. */
        private int m_gridIndex;

        /**
         * Create a new transformer for the selected grid index.<p>
         *
         * @param gridIndex the selected grid index
         */
        public CmsBootstrapGridTransformer(int gridIndex) {

            m_gridIndex = gridIndex;
        }

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        @Override
        public Object transform(Object input) {

            int size = ((Long)input).intValue();
            return String.valueOf(getSize(size, m_column[m_gridIndex]));
        }
    }

    /** The grid columns width in percent. */
    protected double[] m_column = {100.0D, 100.0D, 100.0D, 100.0D, 100.0D};

    /** The bootstrap gutter size. */
    private int m_gutter = 30;

    /** Contains the XS calculated result. */
    private Map<Long, String> m_sizeXs;
    /** Contains the SM calculated result. */
    private Map<Long, String> m_sizeSm;
    /** Contains the MD calculated result. */
    private Map<Long, String> m_sizeMd;
    /** Contains the LG calculated result. */
    private Map<Long, String> m_sizeLg;
    /** Contains the XL calculated result. */
    private Map<Long, String> m_sizeXl;

    /** The CSS string this bootstrap bean was initialized with. */
    private String m_css;

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

        for (int i = 0; i < 5; i++) {
            m_column[i] *= gridCols[i] / 12.0D;
        }
    }

    /**
     * Add a new layer of grid information.<p>
     *
     * @param gridCss the CSS that holds the grid column width information
     */
    public void addLayer(String gridCss) {

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

        int[] result = {xs, sm, md, lg, xl};

        addLayer(result);
    }

    /**
     * Returns the CSS this bean was initialized width.<p>
     *
     * @return the CSS this bean was initialized width
     */
    public String getCss() {

        return m_css;
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
     * Return a lazy initialized map that contains the LG column size.<p>
     *
     * @return a lazy initialized map that contains the LG column size
     */
    public Map<Long, String> getSizeLg() {

        if (m_sizeLg == null) {
            m_sizeLg = CmsCollectionsGenericWrapper.createLazyMap(new CmsBootstrapGridTransformer(3));
        }
        return m_sizeLg;
    }

    /**
     * Return a lazy initialized map that contains the MD column size.<p>
     *
     * @return a lazy initialized map that contains the MD column size
     */
    public Map<Long, String> getSizeMd() {

        if (m_sizeMd == null) {
            m_sizeMd = CmsCollectionsGenericWrapper.createLazyMap(new CmsBootstrapGridTransformer(2));
        }
        return m_sizeMd;
    }

    /**
     * Return a lazy initialized map that contains the SM column size.<p>
     *
     * @return a lazy initialized map that contains the SM column size
     */
    public Map<Long, String> getSizeSm() {

        if (m_sizeSm == null) {
            m_sizeSm = CmsCollectionsGenericWrapper.createLazyMap(new CmsBootstrapGridTransformer(1));
        }
        return m_sizeSm;
    }

    /**
     * Return a lazy initialized map that contains the XL column size.<p>
     *
     * @return a lazy initialized map that contains the XL column size
     */
    public Map<Long, String> getSizeXl() {

        if (m_sizeXl == null) {
            m_sizeXl = CmsCollectionsGenericWrapper.createLazyMap(new CmsBootstrapGridTransformer(4));
        }
        return m_sizeXl;
    }

    /**
     * Return a lazy initialized map that contains the XS column size.<p>
     *
     * @return a lazy initialized map that contains the XS column size
     */
    public Map<Long, String> getSizeXs() {

        if (m_sizeXs == null) {
            m_sizeXs = CmsCollectionsGenericWrapper.createLazyMap(new CmsBootstrapGridTransformer(0));
        }
        return m_sizeXs;
    }

    /**
     * Initialize the CSS used for this bootstrap bean.<p>
     *
     * @param css the CSS used for this bootstrap bean
     */
    public void setCss(String css) {

        m_css = css;
        List<String> layers = CmsStringUtil.splitAsList(css, ':', true);
        for (String layer : layers) {
            addLayer(layer);
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

        return "xs: "
            + m_column[0]
            + " sm: "
            + m_column[1]
            + " md: "
            + m_column[2]
            + " lg: "
            + m_column[3]
            + " xl: "
            + m_column[4];
    }

    /**
     * Calculate the pixel size of a grid column.<p>
     *
     * @param maxSize the allowed maximum pixel size for this grid
     * @param spaceFactor the space factor calculated from the column width
     *
     * @return the pixel size of a grid column
     */
    protected int getSize(int maxSize, double spaceFactor) {

        return (int)Math.ceil((maxSize * spaceFactor) / 100.0);
    }

    /**
     * Pares a bootstrap column CSS class like 'col-md-5'.<p>
     *
     * @param colStr the bootstrap column CSS class to parse
     *
     * @return the number part of the CSS class
     */
    protected int parseCol(String colStr) {

        String number = colStr.substring(7);
        int result;
        try {
            result = Integer.valueOf(number).intValue();
        } catch (NumberFormatException e) {
            result = -1;
        }
        return result;
    }
}