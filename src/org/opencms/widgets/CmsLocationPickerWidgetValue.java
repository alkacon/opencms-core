/*
 * File   : $Source: /alkacon/cvs/alkacon/com.alkacon.opencms.v8.geomap/src/com/alkacon/opencms/v8/geomap/CmsGoogleMapWidgetValue.java,v $
 * Date   : $Date: 2011/02/16 13:05:25 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2008 Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.widgets;

import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import org.apache.commons.logging.Log;

/**
 * A value of the google map widget.<p>
 *
 * This is the parsed value of an element of the type <code>String</code> using the widget <code>GoogleMapWidget</code>.
 *
 * <code>lat:50.953412,lng:6.956534,zoom:13,width:400,height:300,mode:dynamic,type:hybrid</code><p>
 *
 * Available options are:
 * <ul>
 * <li><code>lat:50.953412</code>: the latitude</li>
 * <li><code>lng:6.956534</code>: the longitude</li>
 * <li><code>zoom:7</code>: initial zoom level</li>
 * <li><code>width:300</code>: map width in pixels or %</li>
 * <li><code>height:200</code>: map height in pixels or %</li>
 * <li><code>mode:'static'</code>: the front-end map's mode should be dynamic or static</li>
 * <li><code>type:'hybrid'</code>: the map type, which can be normal, hybrid, satellite and physical</li>
 * </ul>
 *
 * @author Michael Moossen
 *
 * @version $Revision: 1.1 $
 *
 * @since 7.0.5
 */
public class CmsLocationPickerWidgetValue {

    /**
     * Enumeration class for defining the map mode.<p>
     */
    public static enum MapMode {

        /** The dynamic map mode. */
        dynamicMode("dynamic"),

        /** The static map mode. */
        staticMode("static");

        /** The mode value. */
        private String m_modeValue;

        /**
         * Constructor.<p>
         *
         * @param modeValue the mode value
         */
        private MapMode(String modeValue) {

            m_modeValue = modeValue;
        }

        /**
         * Parses the client side mode representation, required as 'static' is a java keyword.<p>
         *
         * @param mode the mode name
         *
         * @return the mode
         */
        public static MapMode parseMode(String mode) {

            MapMode result = null;
            if (dynamicMode.getMode().equals(mode)) {
                result = dynamicMode;
            } else if (staticMode.getMode().equals(mode)) {
                result = staticMode;
            }
            return result;
        }

        /**
         * Returns the mode.<p>
         *
         * @return the mode
         */
        public String getMode() {

            return m_modeValue;
        }

        /**
         * Checks if <code>this</code> is {@link #dynamicMode}.<p>
         *
         * @return <code>true</code>, if <code>this</code> is {@link #dynamicMode}
         */
        public boolean isDynamic() {

            return this == dynamicMode;
        }

        /**
         * Checks if <code>this</code> is {@link #staticMode}.<p>
         *
         * @return <code>true</code>, if <code>this</code> is {@link #staticMode}
         */
        public boolean isStatic() {

            return this == staticMode;
        }
    }

    /**
     * Enumeration class for defining the map types.<p>
     */
    public static enum MapType {
        /** Hybrid map type. */
        hybrid,

        /** Road map type. */
        roadmap,

        /** Satellite image type. */
        satellite,

        /** Terrain map type. */
        terrain;

        /**
         * Checks if <code>this</code> is {@link #hybrid}.<p>
         *
         * @return <code>true</code>, if <code>this</code> is {@link #hybrid}
         */
        public boolean isHybrid() {

            return this == hybrid;
        }

        /**
         * Checks if <code>this</code> is {@link #roadmap}.<p>
         *
         * @return <code>true</code>, if <code>this</code> is {@link #roadmap}
         */
        public boolean isMap() {

            return this == roadmap;
        }

        /**
         * Checks if <code>this</code> is {@link #satellite}.<p>
         *
         * @return <code>true</code>, if <code>this</code> is {@link #satellite}
         */
        public boolean isSatellite() {

            return this == satellite;
        }

        /**
         * Checks if <code>this</code> is {@link #terrain}.<p>
         *
         * @return <code>true</code>, if <code>this</code> is {@link #terrain}
         */
        public boolean isTerrain() {

            return this == terrain;
        }
    }

    /** The default map height in pixels. */
    public static final int DEFAULT_HEIGHT = 300;

    /** The default latitude. */
    public static final float DEFAULT_LAT = 0;

    /** The default longitude. */
    public static final float DEFAULT_LNG = 0;

    /** The default map mode. */
    public static final MapMode DEFAULT_MODE = MapMode.dynamicMode;

    /** The default map type. */
    public static final MapType DEFAULT_TYPE = MapType.roadmap;

    /** The default map width in pixels. */
    public static final int DEFAULT_WIDTH = 400;

    /** The default zoom level. */
    public static final int DEFAULT_ZOOM = 10;

    /** Option height. */
    public static final String OPTION_HEIGHT = "height";

    /** Option lat. */
    public static final String OPTION_LAT = "lat";

    /** Option lng. */
    public static final String OPTION_LNG = "lng";

    /** Option mode. */
    public static final String OPTION_MODE = "mode";

    /** Option type. */
    public static final String OPTION_TYPE = "type";

    /** Option width. */
    public static final String OPTION_WIDTH = "width";

    /** Option zoom. */
    public static final String OPTION_ZOOM = "zoom";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLocationPickerWidgetValue.class);

    /** Map height value. */
    private int m_height;

    /** Map center latitude value. */
    private float m_lat;

    /** Map center longitude value. */
    private float m_lng;

    /** Map mode value. */
    private MapMode m_mode;

    /** Map type value. */
    private MapType m_type;

    /** Map width value. */
    private int m_width;

    /** Map zoom value. */
    private int m_zoom;

    /**
     * Creates a new empty widget option object.<p>
     */
    public CmsLocationPickerWidgetValue() {

        // initialize the members
        m_zoom = DEFAULT_ZOOM;
        m_height = DEFAULT_HEIGHT;
        m_width = DEFAULT_WIDTH;
        m_mode = DEFAULT_MODE;
        m_type = DEFAULT_TYPE;
        m_lat = DEFAULT_LAT;
        m_lng = DEFAULT_LNG;
    }

    /**
     * Creates a new widget value object, configured by the given value String.<p>
     *
     * @param value the value String to parse
     */
    public CmsLocationPickerWidgetValue(String value) {

        this();
        parseOptions(value);
    }

    /**
     * Returns the height.<p>
     *
     * @return the height
     */
    public int getHeight() {

        return m_height;
    }

    /**
     * Returns the lat.<p>
     *
     * @return the lat
     */
    public float getLat() {

        return m_lat;
    }

    /**
     * Returns the longitude.<p>
     *
     * @return the longitude
     */
    public float getLng() {

        return m_lng;
    }

    /**
     * Returns the mode.<p>
     *
     * @return the mode
     */
    public MapMode getMode() {

        return m_mode;
    }

    /**
     * Returns the type.<p>
     *
     * @return the type
     */
    public MapType getType() {

        return m_type;
    }

    /**
     * Returns the width.<p>
     *
     * @return the width
     */
    public int getWidth() {

        return m_width;
    }

    /**
     * Returns the zoom.<p>
     *
     * @return the zoom
     */
    public int getZoom() {

        return m_zoom;
    }

    /**
     * Sets the height.<p>
     *
     * @param height the height to set
     */
    public void setHeight(int height) {

        m_height = height;
    }

    /**
     * Sets the latitude.<p>
     *
     * @param lat the latitude to set
     */
    public void setLat(float lat) {

        m_lat = lat;
    }

    /**
     * Sets the longitude.<p>
     *
     * @param lng the longitude to set
     */
    public void setLng(float lng) {

        m_lng = lng;
    }

    /**
     * Sets the mode.<p>
     *
     * @param mode the mode to set
     */
    public void setMode(MapMode mode) {

        m_mode = mode;
    }

    /**
     * Sets the type.<p>
     *
     * @param type the type to set
     */
    public void setType(MapType type) {

        m_type = type;
    }

    /**
     * Sets the width.<p>
     *
     * @param width the width to set
     */
    public void setWidth(int width) {

        m_width = width;
    }

    /**
     * Sets the value that is wrapped.
     * The method is added for convenient usage of the class in JSPs.
     * In a formatter JSP you can use
     * <pre><code>
     * <jsp:useBean id="map" class="org.opencms.widgets.CmsLocationPickerWidgetValue" />
     * <jsp:setProperty name="map" property="wrappedValue" value="${content.value.Map}" />
     * </code></pre>
     * instead of setting the value directly via the constructor.
     * @param value The string value that should be wrapped as CmsLocationPickerWidgetValue.
     */
    public void setWrappedValue(final String value) {

        parseOptions(value);
    }

    /**
     * Sets the zoom.<p>
     *
     * @param zoom the zoom to set
     */
    public void setZoom(int zoom) {

        m_zoom = zoom;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        JSONObject json = new JSONObject();

        try {
            json.put(OPTION_LAT, getLat());
        } catch (JSONException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        try {
            json.put(OPTION_LNG, getLng());
        } catch (JSONException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        try {
            json.put(OPTION_ZOOM, getZoom());
        } catch (JSONException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        try {
            json.put(OPTION_WIDTH, getWidth());
        } catch (JSONException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        try {
            json.put(OPTION_HEIGHT, getHeight());
        } catch (JSONException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        try {
            json.put(OPTION_TYPE, getType().toString());
        } catch (JSONException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        try {
            json.put(OPTION_MODE, getMode().getMode());
        } catch (JSONException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }

        return json.toString();
    }

    /**
     * Parses the given configuration String.<p>
     *
     * @param configuration the configuration String to parse
     */
    protected void parseOptions(String configuration) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(configuration)) {
            return;
        }
        if (!configuration.startsWith("{")) {
            // add curly braces if not present
            configuration = "{" + configuration + "}";
        }
        try {
            JSONObject json = new JSONObject(configuration);
            if (json.has(OPTION_LAT)) {
                setLat((float)json.getDouble(OPTION_LAT));
            }
            if (json.has(OPTION_LNG)) {
                setLng((float)json.getDouble(OPTION_LNG));
            }
            if (json.has(OPTION_ZOOM)) {
                setZoom(json.getInt(OPTION_ZOOM));
            }
            if (json.has(OPTION_WIDTH)) {
                setWidth(json.getInt(OPTION_WIDTH));
            }
            if (json.has(OPTION_HEIGHT)) {
                setHeight(json.getInt(OPTION_HEIGHT));
            }
            if (json.has(OPTION_TYPE)) {
                setType(MapType.valueOf(json.getString(OPTION_TYPE)));
            }
            if (json.has(OPTION_MODE)) {
                // do not use value of, as the client side string is not equal to the enumeration mode name
                setMode(MapMode.parseMode(json.getString(OPTION_MODE)));
            }
        } catch (JSONException e) {
            // something went wrong
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            return;
        }
    }
}