/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.input.location.CmsLocationSuggestOracle.LocationSuggestion;
import org.opencms.util.CmsStringUtil;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.SuggestOracle;

/**
 * The location picker controller.<p>
 */
public class CmsLocationController {

    /** The URI of google maps API. */
    private static final String MAPS_URI = "https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false";

    /** The goe coder instance. */
    private JavaScriptObject m_geocoder;

    /** The map. */
    private JavaScriptObject m_map;

    /** The map marker. */
    private JavaScriptObject m_marker;

    /** The popup displaying the map. */
    private CmsPopup m_popup;

    /** The popup content widget. */
    private CmsLocationPopupContent m_popupContent;

    /** The current location value. */
    private CmsLocationValue m_value;

    /** The previous value. */
    private CmsLocationValue m_previousValue;

    /** The picker widget. */
    private CmsLocationPicker m_picker;

    /**
     * Constructor.<p>
     * 
     * @param picker the picker widget
     */
    public CmsLocationController(CmsLocationPicker picker) {

        m_picker = picker;
        m_value = CmsLocationValue.parse("{\"address\": \"London\", \"lat\": 51.5001524, \"lng\": -0.1262362, \"height\": 300, \"width\": 400, \"mode\": \"\", \"zoom\": 8}");
        m_previousValue = m_value.cloneValue();
    }

    /** Sets the location value as string.
     *  
     * @param value the string representation of the location value (JSON)
     **/
    public void setStringValue(String value) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
            m_value = CmsLocationValue.parse(value);
            m_previousValue = m_value.cloneValue();
            m_picker.displayValue(m_value);
            if ((m_popup != null) && m_popup.isVisible()) {
                m_popupContent.displayValues(m_value);
                updateMarkerPosition();
            }
        }
    }

    /**
     * Returns the JSON string representation of the current value.<p>
     * 
     * @return the JSON string representation
     */
    public String getStringValue() {

        return m_value.toJSONString();
    }

    /**
     * Returns the current location value.<p>
     * 
     * @return the location value
     */
    public CmsLocationValue getLocationValue() {

        return m_value;
    }

    /**
     * Called on address value change.<p>
     * 
     * @param address the new address
     */
    protected native void onAddressChange(String address) /*-{
                                                          var self = this;
                                                          this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_value.address = address;
                                                          this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_geocoder
                                                          .geocode(
                                                          {
                                                          'address' : address
                                                          },
                                                          function(results, status) {
                                                          // check to see if we have at least one valid address
                                                          if (!results
                                                          || (status != $wnd.google.maps.GeocoderStatus.OK)
                                                          || !results[0].formatted_address) {
                                                          alert("Address not found");
                                                          return;
                                                          }
                                                          var lat = results[0].geometry.location.lat();
                                                          var lng = results[0].geometry.location.lng();
                                                          self.@org.opencms.gwt.client.ui.input.location.CmsLocationController::setPosition(FFZ)(lat,lng,true);
                                                          });
                                                          }-*/;

    /**
     * Called on address suggestion selection.<p>
     * 
     * @param suggestion the selected suggestion
     */
    protected void onAddressChange(SuggestOracle.Suggestion suggestion) {

        LocationSuggestion location = (LocationSuggestion)suggestion;
        m_value.setAddress(location.getDisplayString());
        setPosition(location.getLatitude(), location.getLongitude(), true);
    }

    /**
     * Called on height value change.<p>
     * 
     * @param height the height
     */
    protected void onHeightChange(String height) {

        m_value.setHeight(height);
    }

    /**
     * Called on latitude value change.<p>
     * 
     * @param latitude the latitude
     */
    protected void onLatitudeChange(String latitude) {

        m_value.setLatitude(latitude);
        updateMarkerPosition();
    }

    /**
     * Called on longitude value change.<p>
     * 
     * @param longitude the longitude
     */
    protected void onLongitudeChange(String longitude) {

        m_value.setLongitude(longitude);
        updateMarkerPosition();
    }

    /**
     * Called on mode value change.<p>
     * 
     * @param mode the mode
     */
    protected void onModeChange(String mode) {

        m_value.setMode(mode);
    }

    /**
     * Called on width value change.<p>
     * 
     * @param width the width
     */
    protected void onWidthChange(String width) {

        m_value.setWidth(width);
    }

    /**
     * Called on zoom value change.<p>
     * 
     * @param zoom the zoom
     */
    protected native void onZoomChange(String zoom) /*-{
                                                    var z = parseInt(zoom);
                                                    if (!isNaN(z)) {
                                                                                                        this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_value.zoom = z;
                                                                                                        var map= this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_map;
                                                                                                        map.setZoom(z);
                                                                                                        var pos = this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::getCurrentPosition()();
                                                                                                        map.panTo(pos);
                                                                                                        map.setCenter(pos);
                                                    }
                                                    }-*/;

    /**
     * Opens the location picker popup.<p>
     */
    void openPopup() {

        if (m_popup == null) {
            m_popup = new CmsPopup("Pick a location", 1250);
            m_popupContent = new CmsLocationPopupContent(this, new CmsLocationSuggestOracle());
            m_popup.setMainContent(m_popupContent);
            m_popup.addDialogClose(new Command() {

                public void execute() {

                    fireChangeEventOnPicker(false);
                }
            });
        }
        m_popup.show();
        updateForm();
        initialize();
    }

    /**
     * Fires the value change event for the location picker.<p>
     * 
     * @param force <code>true</code> to always fire the event
     */
    void fireChangeEventOnPicker(boolean force) {

        String val = m_value.toJSONString();
        if (force || !val.equals(m_previousValue.toJSONString())) {
            m_previousValue = m_value.cloneValue();
            m_picker.displayValue(m_value);
            ValueChangeEvent.fire(m_picker, val);
        }
    }

    /**
     * Return a maps API position object according the the current location value.<p>
     * 
     * @return the position object
     */
    private native JavaScriptObject getCurrentPosition()/*-{
                                                        var val = this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_value;
                                                        return new $wnd.google.maps.LatLng(val.lat, val.lng);

                                                        }-*/;

    /**
     * Initializes the location picker.<p>
     */
    private void initialize() {

        if (isAPILoaded()) {
            initMap();
        } else {
            loadAPI();
        }
    }

    /**
     * Displays the map for the current location.<p>
     */
    private native void initMap() /*-{
                                  var mapOptions = {
                                  zoom : 8,
                                  center : new $wnd.google.maps.LatLng(-34.397, 150.644)
                                  };
                                  var popupContent = this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_popupContent;
                                  var canvas = popupContent.@org.opencms.gwt.client.ui.input.location.CmsLocationPopupContent::getMapCanvas()();
                                  this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_map = new $wnd.google.maps.Map(
                                  canvas, mapOptions);
                                  this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_geocoder = new $wnd.google.maps.Geocoder();
                                  this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::updateMarkerPosition()();
                                  }-*/;

    /**
     * Returns if the google maps API is already loaded to the window context.<p>
     * 
     * @return <code>true</code>  if the google maps API is already loaded to the window context
     */
    private native boolean isAPILoaded()/*-{
                                        return $wnd.google !== undefined && $wnd.google.maps !== undefined
                                        && $wnd.google.maps.Map !== undefined;
                                        }-*/;

    /**
     * Loads the google maps API and initializes the map afterwards.<p>
     */
    private native void loadAPI()/*-{
                                 var self = this;
                                 $wnd.showMap = function() {
                                 self.@org.opencms.gwt.client.ui.input.location.CmsLocationController::initMap()();
                                 }
                                 var script = $wnd.document.createElement('script');
                                 script.type = 'text/javascript';
                                 script.src = @org.opencms.gwt.client.ui.input.location.CmsLocationController::MAPS_URI
                                 + '&callback=showMap';
                                 $wnd.document.body.appendChild(script);
                                 }-*/;

    /**
     * Sets the position values and updates the map view.<p>
     * 
     * @param latitude the latitude
     * @param longitude the longitude
     * @param updateMap <code>true</code> to update the map
     */
    private void setPosition(float latitude, float longitude, boolean updateMap) {

        m_value.setLatitude(latitude);
        m_value.setLongitude(longitude);
        m_popupContent.displayValues(m_value);
        if (updateMap) {
            updateMarkerPosition();
        }
    }

    /**
     * Displays the current location value within the popup form.<p>
     */
    private void updateForm() {

        m_popupContent.displayValues(m_value);
    }

    /**
     * Updates the marker position according to the current location value.<p>
     */
    private native void updateMarkerPosition()/*-{
                                              var map = this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_map;
                                              var pos = this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::getCurrentPosition()();
                                              var marker=this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_marker;
                                              if (marker == null) {
                                              try {
                                              var marker = new $wnd.google.maps.Marker({
                                              position : pos,
                                              map : map,
                                              draggable : true
                                              });
                                              this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_marker = marker;
                                              var self = this;
                                              // handle marker dnd
                                              $wnd.google.maps.event
                                              .addListener(
                                              marker,
                                              "dragend",
                                              function() {
                                              var lat = marker.getPosition().lat();
                                              var lng = marker.getPosition().lng();
                                              self.@org.opencms.gwt.client.ui.input.location.CmsLocationController::setPosition(FFZ)(lat,lng,false);
                                              });
                                              } catch (e) {
                                              $wnd.alert(e);
                                              }
                                              } else {
                                              marker.setPosition(pos);
                                              }
                                              map.panTo(pos);
                                              map.setCenter(pos);
                                              }-*/;

}
