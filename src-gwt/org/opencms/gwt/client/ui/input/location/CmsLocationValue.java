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

package org.opencms.gwt.client.ui.input.location;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * The location value.<p>
 */
public final class CmsLocationValue extends JavaScriptObject {

    /**
     * Protected constructor required for JavaScript overlay objects.<p>
     */
    protected CmsLocationValue() {

    }

    /**
     * Parses the given JSON string.<p>
     *
     * @param value the value string to parse
     *
     * @return the location object
     */
    public static native CmsLocationValue parse(String value)/*-{
                                                             if (value.indexOf("{") != 0) {
                                                             // add curly braces if not present before parsing
                                                             value = "{" + value + "}";
                                                             }
                                                             try {
                                                             return JSON.parse(value);
                                                             } catch (e) {
                                                             return eval("("+value+")");
                                                             }
                                                             }-*/;

    /**
     * Clones the value object.<p>
     *
     * @return the clone
     */
    public native CmsLocationValue cloneValue()/*-{
                                               return {
                                               address : this.address,
                                               height : this.height,
                                               lat : this.lat,
                                               lng : this.lng,
                                               mode : this.mode,
                                               type : this.type,
                                               width : this.width,
                                               zoom : this.zoom
                                               };
                                               }-*/;

    /**
     * Returns the location address.<p>
     *
     * @return the address
     */
    public native String getAddress()/*-{
                                     return this.address !== undefined ? this.address : '';
                                     }-*/;

    /**
     * Returns the height.<p>
     *
     * @return the height
     */
    public native int getHeight()/*-{
                                 return this.height;
                                 }-*/;

    /**
     * Returns the latitude.<p>
     *
     * @return the latitude
     */
    public native float getLatitude()/*-{
                                     return this.lat;
                                     }-*/;

    /**
     * Returns the latitude string representation.<p>
     *
     * @return the latitude string representation
     */
    public native String getLatitudeString()/*-{
                                            return this.lat.toFixed(6);
                                            ;
                                            }-*/;

    /**
     * Returns the longitude.<p>
     *
     * @return the longitude
     */
    public native float getLongitude()/*-{
                                      return this.lng;
                                      }-*/;

    /**
     * Returns the longitude string representation.<p>
     *
     * @return the longitude string representation
     */
    public native String getLongitudeString()/*-{
                                             return this.lng.toFixed(6);
                                             }-*/;

    /**
     * Returns the mode.<p>
     *
     * @return the mode
     */
    public native String getMode()/*-{
                                  return this.mode;
                                  }-*/;

    /**
     * Returns the type.<p>
     *
     * @return the type
     */
    public native String getType()/*-{
                                  return this.type;
                                  }-*/;

    /**
     * Returns the width.<p>
     *
     * @return the width
     */
    public native int getWidth()/*-{
                                return this.width;
                                }-*/;

    /**
     * Returns the zoom.<p>
     *
     * @return the zoom
     */
    public native int getZoom()/*-{
                               return this.zoom;
                               }-*/;

    /**
     * Sets the address.<p>
     *
     * @param address the address
     */
    public native void setAddress(String address)/*-{
                                                 this.address = address;
                                                 }-*/;

    /**
     * Sets the height.<p>
     *
     * @param height the height
     */
    public native void setHeight(int height)/*-{
                                            this.height = height;
                                            }-*/;

    /**
     * Sets the height.<p>
     *
     * @param height the height
     */
    public native void setHeight(String height)/*-{
                                               var h = parseInt(height);
                                               this.height = isNaN(h) ? 0 : h;
                                               }-*/;

    /**
     * Sets the latitude.<p>
     *
     * @param latitude the latitude
     */
    public native void setLatitude(float latitude)/*-{
                                                  this.lat = latitude;
                                                  }-*/;

    /**
     * Sets the latitude.<p>
     *
     * @param latitude the latitude
     */
    public native void setLatitude(String latitude)/*-{
                                                   var lat = parseFloat(latitude);
                                                   this.lat = isNaN(lat) ? 0 : lat;
                                                   }-*/;

    /**
     * Sets the longitude.<p>
     *
     * @param longitude the longitude
     */
    public native void setLongitude(float longitude)/*-{
                                                    this.lng = longitude;
                                                    }-*/;

    /**
     * Sets the longitude.<p>
     *
     * @param longitude the longitude
     */
    public native void setLongitude(String longitude)/*-{
                                                     var lng = parseFloat(longitude);
                                                     this.lng = isNaN(lng) ? 0 : lng;
                                                     }-*/;

    /**
     * Sets the map mode.<p>
     *
     * @param mode the map mode
     */
    public native void setMode(String mode)/*-{
                                           this.mode = mode;
                                           }-*/;

    /**
     * Sets the map type.<p>
     *
     * @param type the map type
     */
    public native void setType(String type)/*-{
                                           this.type = type;
                                           }-*/;

    /**
     * Sets the width.<p>
     *
     * @param width the width
     */
    public native void setWidth(int width)/*-{
                                          this.width = width;
                                          }-*/;

    /**
     * Sets the width.<p>
     *
     * @param width the width
     */
    public native void setWidth(String width)/*-{
                                             var w = parseInt(width);
                                             this.width = isNaN(w) ? 0 : w;
                                             }-*/;

    /**
     * Sets the map zoom level.<p>
     *
     * @param zoom the zoom level
     */
    public native void setZoom(int zoom)/*-{
                                        this.zoom = zoom;
                                        }-*/;

    /**
     * Sets the map zoom level.<p>
     *
     * @param zoom the zoom level
     */
    public native void setZoom(String zoom)/*-{
                                           var z = parseInt(z);
                                           this.zoom = isNaN(z) ? 0 : z;
                                           }-*/;

    /**
     * Returns the JSON string representation of this value.<p>
     *
     * @return the JSON string representation
     */
    public native String toJSONString()/*-{
                                       return JSON.stringify(this);
                                       }-*/;

}
