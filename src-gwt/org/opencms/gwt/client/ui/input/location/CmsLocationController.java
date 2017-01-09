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

import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.rpc.CmsLog;
import org.opencms.gwt.client.ui.CmsAlertDialog;
import org.opencms.gwt.client.ui.CmsErrorDialog;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.util.CmsClientStringUtil;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.SuggestOracle;

/**
 * The location picker controller.<p>
 */
public class CmsLocationController {

    /** Flag indicating the API is currently being loaded. */
    private static boolean loadingApi;

    /** Flag indicating the dynamic error style has been inserted. */
    private static boolean m_hasInsertedDynamicStyle;

    /** The main API param. */
    private static final String MAPS_MAIN_PARAM = "v=3.exp&sensor=false";

    /** The places library param. */
    private static final String MAPS_PLACES_PARAM = "libraries=places";

    /** The URI of google maps API. */
    private static final String MAPS_URI = "https://maps.googleapis.com/maps/api/js";

    /** The callback to be executed once the API is loaded. */
    private static List<Command> onApiReady = new ArrayList<Command>();

    /** The parsed configuration JSON object. */
    private JavaScriptObject m_config;

    /** The previous value. */
    private CmsLocationValue m_currentValue;

    /** The current location value. */
    private CmsLocationValue m_editValue;

    /** The goe coder instance. */
    private JavaScriptObject m_geocoder;

    /** The map. */
    private JavaScriptObject m_map;

    /** The map marker. */
    private JavaScriptObject m_marker;

    /** The picker widget. */
    private CmsLocationPicker m_picker;

    /** The popup displaying the map. */
    private CmsPopup m_popup;

    /** The popup content widget. */
    private CmsLocationPopupContent m_popupContent;

    /** The preview map. */
    private JavaScriptObject m_previewMap;

    /** The preview map marker. */
    private JavaScriptObject m_previewMarker;

    /**
     * Constructor.<p>
     *
     * @param picker the picker widget
     * @param configuration the widget configuration
     */
    public CmsLocationController(CmsLocationPicker picker, String configuration) {
        parseConfig(configuration);
        initDynamicStyle();
        m_picker = picker;
        m_editValue = CmsLocationValue.parse(
            "{\"address\": \"London\", \"lat\": 51.5001524, \"lng\": -0.1262362, \"height\": 300, \"width\": 400, \"mode\": \"\", \"type\":\"roadmap\", \"zoom\": 8}");
        m_currentValue = m_editValue.cloneValue();
    }

    /**
     * Called once the API is loaded.<p>
     */
    private static void apiReady() {

        loadingApi = false;
        for (Command callback : onApiReady) {
            callback.execute();
        }
        onApiReady.clear();
    }

    /**
     * Returns the available map modes.<p>
     *
     * @return the available map modes
     */
    private static Map<String, String> getModeItems() {

        Map<String, String> modes = new LinkedHashMap<String, String>();
        modes.put("dynamic", Messages.get().key(Messages.GUI_LOCATION_DYNAMIC_0));
        modes.put("static", Messages.get().key(Messages.GUI_LOCATION_STATIC_0));
        return modes;
    }

    /**
     * Returns the available map types.<p>
     *
     * @return the available map types
     */
    private static Map<String, String> getTypeItems() {

        Map<String, String> types = new LinkedHashMap<String, String>();
        types.put("roadmap", Messages.get().key(Messages.GUI_LOCATION_ROADMAP_0));
        types.put("hybrid", Messages.get().key(Messages.GUI_LOCATION_HYBRID_0));
        types.put("satellite", Messages.get().key(Messages.GUI_LOCATION_SATELLITE_0));
        types.put("terrain", Messages.get().key(Messages.GUI_LOCATION_TERRAIN_0));
        return types;
    }

    /**
     * Returns the available zoom levels.<p>
     *
     * @return the available zoom levels
     */
    private static Map<String, String> getZoomItems() {

        Map<String, String> zoomItems = new LinkedHashMap<String, String>();
        for (int i = 0; i < 21; i++) {
            String value = String.valueOf(i);
            zoomItems.put(value, value);
        }
        return zoomItems;
    }

    /**
     * Returns if the google maps API is already loaded to the window context.<p>
     *
     * @return <code>true</code>  if the google maps API is already loaded to the window context
     */
    private static boolean isApiLoaded() {

        return isMainApiLoaded() && isPlacesApiLoaded();
    }

    /**
     * Returns if the google maps API is already loaded to the window context.<p>
     *
     * @return <code>true</code>  if the google maps API is already loaded to the window context
     */
    private static native boolean isMainApiLoaded()/*-{
		var result = $wnd.google !== undefined
				&& $wnd.google.maps !== undefined
				&& $wnd.google.maps.Map !== undefined;
		return result;
    }-*/;

    /**
     * Returns if the google maps API is already loaded to the window context.<p>
     *
     * @return <code>true</code>  if the google maps API is already loaded to the window context
     */
    private static native boolean isPlacesApiLoaded()/*-{
		var result = $wnd.google !== undefined
				&& $wnd.google.maps !== undefined
				&& $wnd.google.maps.places !== undefined;
		return result;
    }-*/;

    /**
     * Returns the current location value.<p>
     *
     * @return the location value
     */
    public CmsLocationValue getLocationValue() {

        return m_editValue;
    }

    /**
     * Returns the JSON string representation of the current value.<p>
     *
     * @return the JSON string representation
     */
    public String getStringValue() {

        return m_currentValue == null ? "" : m_currentValue.toJSONString();
    }

    /** Sets the location value as string.
     *
     * @param value the string representation of the location value (JSON)
     **/
    public void setStringValue(String value) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
            try {
                m_editValue = CmsLocationValue.parse(value);
                m_currentValue = m_editValue.cloneValue();
                displayValue();
                if ((m_popup != null) && m_popup.isVisible()) {
                    m_popupContent.displayValues(m_editValue);
                    updateMarkerPosition();
                }
            } catch (Exception e) {
                CmsLog.log(e.getLocalizedMessage() + "\n" + CmsClientStringUtil.getStackTrace(e, "\n"));
            }
        } else {
            m_currentValue = null;
            displayValue();
        }
    }

    /**
     * Return a maps API position object according the the current location value.<p>
     *
     * @return the position object
     */
    protected native JavaScriptObject getCurrentPosition()/*-{
		var val = this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_editValue;
		return new $wnd.google.maps.LatLng(val.lat, val.lng);

    }-*/;

    /**
     * Called on address value change.<p>
     *
     * @param address the new address
     */
    protected native void onAddressChange(String address) /*-{
		var self = this;
		this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_editValue.address = address;
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
							self.@org.opencms.gwt.client.ui.input.location.CmsLocationController::setPosition(FFZZ)(lat,lng,true,false);
						});
    }-*/;

    /**
     * Called on address suggestion selection.<p>
     *
     * @param suggestion the selected suggestion
     */
    protected void onAddressChange(SuggestOracle.Suggestion suggestion) {

        try {
            onAddressChange(suggestion.getDisplayString());
        } catch (Throwable t) {
            CmsErrorDialog.handleException(t);
        }
    }

    /**
     * Cancels the location selection.<p>
     */
    protected void onCancel() {

        m_popup.hide();
        if (m_currentValue != null) {
            m_editValue = m_currentValue;
        }
        displayValue();
    }

    /**
     * Called on height value change.<p>
     *
     * @param height the height
     */
    protected void onHeightChange(String height) {

        m_editValue.setHeight(height);
    }

    /**
     * Called on latitude value change.<p>
     *
     * @param latitude the latitude
     */
    protected void onLatitudeChange(String latitude) {

        try {
            m_editValue.setLatitude(latitude);
            updateMarkerPosition();
            updateAddress();
        } catch (Throwable t) {
            CmsErrorDialog.handleException(t);
        }
    }

    /**
     * Called on longitude value change.<p>
     *
     * @param longitude the longitude
     */
    protected void onLongitudeChange(String longitude) {

        try {
            m_editValue.setLongitude(longitude);
            updateMarkerPosition();
            updateAddress();
        } catch (Throwable t) {
            CmsErrorDialog.handleException(t);
        }
    }

    /**
     * Called on mode value change.<p>
     *
     * @param mode the mode
     */
    protected void onModeChange(String mode) {

        m_editValue.setMode(mode);
    }

    /**
     * Sets the selected location value.<p>
     */
    protected void onOk() {

        ensureFormattedAddress();
    }

    /**
     * Ensures the preview map has the right size and is centered.<p>
     */
    protected native void onPreviewResize()/*-{
		var map = this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_previewMap;
		if (map != null) {
			$wnd.google.maps.event.trigger(map, 'resize');
			var pos = this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::getCurrentPosition()();
			map.setCenter(pos);
		}
    }-*/;

    /**
     * Called on map type change.<p>
     *
     * @param type the map type
     */
    protected native void onTypeChange(String type)/*-{
		this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_editValue.type = type;
		this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_map
				.setMapTypeId(type);
    }-*/;

    /**
     * Called on width value change.<p>
     *
     * @param width the width
     */
    protected void onWidthChange(String width) {

        m_editValue.setWidth(width);
    }

    /**
     * Called on zoom value change.<p>
     *
     * @param zoom the zoom
     */
    protected native void onZoomChange(String zoom) /*-{
		var z = parseInt(zoom);
		if (!isNaN(z)) {
			this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_editValue.zoom = z;
			var map = this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_map;
			map.setZoom(z);
			var pos = this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::getCurrentPosition()();
			map.panTo(pos);
			map.setCenter(pos);
		}
    }-*/;

    /**
    * Fires the value change event for the location picker.<p>
    *
    * @param force <code>true</code> to always fire the event
    */
    void fireChangeEventOnPicker(boolean force) {

        String val = m_editValue.toJSONString();
        if (force || (m_currentValue == null) || !val.equals(m_currentValue.toJSONString())) {
            m_currentValue = m_editValue.cloneValue();
            displayValue();
            ValueChangeEvent.fire(m_picker, val);
        }
    }

    /**
     * Displays the map for the current location.<p>
     */
    native void initMap() /*-{
		try {
			if (this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_map == null) {
				var value = this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_editValue;
				var type = (value.type == null || value.type == "") ? "roadmap"
						: value.type;
				var zoom = parseInt(value.zoom);
				if (isNaN(zoom)) {
					zoom = 8;
				}

				var mapOptions = {
					zoom : zoom,
					mapTypeId : type,
					center : new $wnd.google.maps.LatLng(-34.397, 150.644),
					streetViewControl : false
				};
				var popupContent = this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_popupContent;
				var canvas = popupContent.@org.opencms.gwt.client.ui.input.location.CmsLocationPopupContent::getMapCanvas()();
				this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_map = new $wnd.google.maps.Map(
						canvas, mapOptions);
				this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_geocoder = new $wnd.google.maps.Geocoder();
			}
		} catch (e) {
			$wnd.console.log(e.message);
		}
		this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::updateMarkerPosition()();
    }-*/;

    /**
     * Opens the location picker popup.<p>
     */
    void openPopup() {

        if (hasError()) {
            CmsAlertDialog alert = new CmsAlertDialog(
                Messages.get().key(Messages.ERR_TITLE_ERROR_0),
                Messages.get().key(Messages.ERR_LOCATION_MISSING_API_KEY_1, "<br />"));
            alert.center();
            return;
        }
        try {
            if (m_popup == null) {
                m_popup = new CmsPopup(Messages.get().key(Messages.GUI_LOCATION_DIALOG_TITLE_0), hasMap() ? 1020 : 420);
                m_popupContent = new CmsLocationPopupContent(
                    this,
                    new CmsLocationSuggestOracle(this),
                    getModeItems(),
                    getTypeItems(),
                    getZoomItems());
                setFieldVisibility();
                m_popup.setMainContent(m_popupContent);
                m_popup.addDialogClose(null);
            }
            m_popup.center();
            m_popup.show();
            initialize();
            updateForm();
        } catch (Throwable t) {
            CmsErrorDialog.handleException(t);
        }
    }

    /**
     * Shows the map preview.<p>
     */
    native void showMapPreview() /*-{
		var map = this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_previewMap;
		var value = this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_editValue;
		var pos = this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::getCurrentPosition()();
		var type = (value.type == null || value.type == "") ? "roadmap"
				: value.type;
		var zoom = parseInt(value.zoom);
		if (isNaN(zoom)) {
			zoom = 8;
		}
		if (map == null) {
			var picker = this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_picker;
			var canvas = picker.@org.opencms.gwt.client.ui.input.location.CmsLocationPicker::getMapPreview()();
			var mapOptions = {
				zoom : zoom,
				mapTypeId : type,
				center : pos,
				draggable : false,
				disableDefaultUi : true,
				disableDoubleClickZoom : true,
				mapTypeControl : false,
				zoomControl : false,
				streetViewControl : false,
				scrollwheel : false,
				keyboardShortcuts : false
			};
			map = new $wnd.google.maps.Map(canvas, mapOptions);
			this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_previewMap = map;
		} else {
			map.setZoom(zoom);
			map.setMapTypeId(type);

		}
		var marker = this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_previewMarker;
		if (marker == null) {
			var marker = new $wnd.google.maps.Marker({
				position : pos,
				map : map,
				draggable : false
			});
			this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_previewMarker = marker;
		} else {
			marker.setPosition(pos);
		}
		$wnd.google.maps.event.trigger(map, 'resize');
		map.setCenter(pos);
    }-*/;

    /**
     * Displays the values within the picker widget.<p>
     */
    private void displayValue() {

        if (m_currentValue == null) {
            m_picker.displayValue("");
            m_picker.setPreviewVisible(false);
            m_picker.setLocationInfo(Collections.<String, String> emptyMap());
        } else {
            m_picker.displayValue(m_editValue.getAddress());
            Map<String, String> infos = new LinkedHashMap<String, String>();
            if (hasLatLng()) {
                infos.put(Messages.get().key(Messages.GUI_LOCATION_LATITUDE_0), m_editValue.getLatitudeString());
                infos.put(Messages.get().key(Messages.GUI_LOCATION_LONGITUDE_0), m_editValue.getLongitudeString());
            }
            if (hasSize()) {
                infos.put(
                    Messages.get().key(Messages.GUI_LOCATION_SIZE_0),
                    m_editValue.getWidth() + " x " + m_editValue.getHeight());
            }
            if (hasType()) {
                infos.put(Messages.get().key(Messages.GUI_LOCATION_TYPE_0), m_editValue.getType());
            }
            if (hasMode()) {
                infos.put(Messages.get().key(Messages.GUI_LOCATION_MODE_0), m_editValue.getMode());
            }
            m_picker.setLocationInfo(infos);
            m_picker.setPreviewVisible(true);
            if (isApiLoaded()) {
                showMapPreview();
            } else {
                onApiReady(new Command() {

                    public void execute() {

                        showMapPreview();
                    }
                });
            }
        }
    }

    /**
     * Checks the current address with the google geocoder to ensure a well formatted address.<p>
     * Will close the picker popup afterwards.<p>
     */
    private native void ensureFormattedAddress()/*-{
		var self = this;
		var address = self.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_editValue.address;
		if (address != null && address.trim().length > 0) {
			self.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_geocoder
					.geocode(
							{
								'address' : address
							},
							function(results, status) {
								// check to see if we have at least one valid address
								if (results
										&& (status == $wnd.google.maps.GeocoderStatus.OK)
										&& results[0].formatted_address) {
									self.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_editValue.address = results[0].formatted_address;
									var lat = results[0].geometry.location
											.lat();
									var lng = results[0].geometry.location
											.lng();
									self.@org.opencms.gwt.client.ui.input.location.CmsLocationController::setPosition(FFZZ)(lat,lng,true,false);
								}
								self.@org.opencms.gwt.client.ui.input.location.CmsLocationController::fireChangeAndClose()();
							});
		}
		self.@org.opencms.gwt.client.ui.input.location.CmsLocationController::fireChangeAndClose()();
    }-*/;

    /**
     * Fires the value change event and closes the picker popup.<p>
     */
    private void fireChangeAndClose() {

        fireChangeEventOnPicker(false);
        m_popup.hide();
    }

    /**
     * Returns the Google API key.<p>
     *
     * @return the Google API key
     */
    private native String getAPIKey()/*-{
		return this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_config.apiKey;
    }-*/;

    /**
     * Returns the value display string.<p>
     *
     * @return the value
     */
    private String getDisplayString() {

        return Messages.get().key(
            Messages.GUI_LOCATION_DISPLAY_3,
            m_editValue.getAddress(),
            m_editValue.getLatitudeString(),
            m_editValue.getLongitudeString());
    }

    /**
     * Evaluates if the address field is configured.<p>
     *
     * @return <code>true</code> if the address field is configured
     */
    private native boolean hasAddress()/*-{
		return this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_config.edit
				.indexOf('address') != -1;
    }-*/;

    /**
     * Evaluates if the Google API key is configured.<p>
     *
     * @return <code>true</code> if the Google API key is configured
     */
    private native boolean hasAPIKey()/*-{
		return this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_config != null
				&& this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_config.apiKey != null;
    }-*/;

    /**
     * Checks whether the error element is present in the preview DIV or the preview DIV is empty.<p>
     *
     * @return <code>true</code> in case of an error state
     */
    private boolean hasError() {

        return (CmsDomUtil.querySelector(".gm-err-content", m_picker.getMapPreview()) != null)
                     || (((m_picker.getMapPreview().getFirstChildElement() == null)
                     || CmsStringUtil.isEmptyOrWhitespaceOnly(
                    m_picker.getMapPreview().getFirstChildElement().getInnerHTML())));
    }

    /**
     * Evaluates if the lat. lng. fields are configured.<p>
     *
     * @return <code>true</code> if the lat. lng. fields are configured
     */
    private native boolean hasLatLng()/*-{
		return this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_config.edit
				.indexOf('coords') != -1;
    }-*/;

    /**
     * Evaluates if the map field is configured.<p>
     *
     * @return <code>true</code> if the map field is configured
     */
    private native boolean hasMap()/*-{
		return this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_config.edit
				.indexOf('map') != -1;
    }-*/;

    /**
     * Evaluates if the mode field is configured.<p>
     *
     * @return <code>true</code> if the mode field is configured
     */
    private native boolean hasMode()/*-{
		return this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_config.edit
				.indexOf('mode') != -1;
    }-*/;

    /**
     * Evaluates if the size fields are configured.<p>
     *
     * @return <code>true</code> if the size fields are configured
     */
    private native boolean hasSize()/*-{
		return this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_config.edit
				.indexOf('size') != -1;
    }-*/;

    /**
     * Evaluates if the type field is configured.<p>
     *
     * @return <code>true</code> if the type field is configured
     */
    private native boolean hasType()/*-{
		return this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_config.edit
				.indexOf('type') != -1;
    }-*/;

    /**
     * Evaluates if the zoom field is configured.<p>
     *
     * @return <code>true</code> if the zoom field is configured
     */
    private native boolean hasZoom()/*-{
		return this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_config.edit
				.indexOf('zoom') != -1;
    }-*/;

    /**
     * Adds a CSS style rule to display the localized error message on missing API key.<p>
     */
    private void initDynamicStyle() {

        if (!m_hasInsertedDynamicStyle) {
            String message = Messages.get().key(Messages.ERR_LOCATION_MISSING_API_KEY_1, "\\a");
            message.replace("<br />", "\\\\a");
            CmsDebugLog.consoleLog("Setting missing key message: " + message);
            CmsDomUtil.addDynamicStyleRule(
                "div.gm-err-content:after, div.mapPreview > div > div:empty:after{ content:'"
                    + message
                    + "' !important; }");
            m_hasInsertedDynamicStyle = true;
        }
    }

    /**
     * Initializes the location picker.<p>
     */
    private void initialize() {

        if (isApiLoaded()) {
            initMap();
        } else {
            onApiReady(new Command() {

                public void execute() {

                    initMap();
                }
            });
        }
    }

    /**
     * Loads the google maps API and initializes the map afterwards.<p>
     */
    private native void loadApi()/*-{
		$wnd.cmsLocationPickerApiReady = function() {
			@org.opencms.gwt.client.ui.input.location.CmsLocationController::apiReady()();
		}
		var uri = @org.opencms.gwt.client.ui.input.location.CmsLocationController::MAPS_URI;
		uri += '?'
				+ @org.opencms.gwt.client.ui.input.location.CmsLocationController::MAPS_MAIN_PARAM
				+ '&'
				+ @org.opencms.gwt.client.ui.input.location.CmsLocationController::MAPS_PLACES_PARAM;
		if (this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::hasAPIKey()()) {
			uri += '&key='
					+ this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::getAPIKey()();
		}
		uri += '&callback=cmsLocationPickerApiReady';

		var script = $wnd.document.createElement('script');
		script.type = 'text/javascript';
		script.src = uri;
		$wnd.document.body.appendChild(script);
    }-*/;

    /**
     * Adds a callback to be executed once the API is ready. Will be executed right away if the API is already loaded.<p>
     *
     * @param callback the callback
     */
    private void onApiReady(Command callback) {

        if (isApiLoaded()) {
            callback.execute();
        } else {
            onApiReady.add(callback);
            if (!loadingApi) {
                loadingApi = true;
                loadApi();
            }
        }
    }

    /**
     * Parses the configuration string.<p>
     *
     * @param configuration the configuration
     */
    private native void parseConfig(String configuration)/*-{
		this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_config = JSON
				.parse(configuration);
    }-*/;

    /**
     * Sets all editable fields visible.<p>
     */
    private void setFieldVisibility() {

        m_popupContent.setMapVisible(hasMap());
        m_popupContent.setAddressVisible(hasAddress());
        m_popupContent.setLatLngVisible(hasLatLng());
        m_popupContent.setSizeVisible(hasSize());
        m_popupContent.setTypeVisible(hasType());
        m_popupContent.setModeVisible(hasMode());
        m_popupContent.setZoomVisible(hasZoom());
    }

    /**
     * Sets the position values and updates the map view.<p>
     *
     * @param latitude the latitude
     * @param longitude the longitude
     * @param updateMap <code>true</code> to update the map
     * @param updateAddress <code>true</code> to update the address from the new position data
     */
    private void setPosition(float latitude, float longitude, boolean updateMap, boolean updateAddress) {

        m_editValue.setLatitude(latitude);
        m_editValue.setLongitude(longitude);
        m_popupContent.displayValues(m_editValue);
        if (updateMap) {
            updateMarkerPosition();
        }
        if (updateAddress) {
            updateAddress();
        }
    }

    /**
     * Updates the address according to the current position data.<p>
     */
    private native void updateAddress()/*-{
		var self = this;
		var pos = this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::getCurrentPosition()();
		// try to evaluate the address from the current position
		this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_geocoder
				.geocode(
						{
							'latLng' : pos
						},
						function(results, status) {
							var address = "";
							// check that everything is ok
							if (status == $wnd.google.maps.GeocoderStatus.OK
									&& results[0]
									&& results[0].formatted_address) {
								// set the new address
								address = results[0].formatted_address;
							}
							if (address != self.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_editValue.address) {
								self.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_editValue.address = address;
								self.@org.opencms.gwt.client.ui.input.location.CmsLocationController::updateForm()();
							}
						});
    }-*/;

    /**
     * Displays the current location value within the popup form.<p>
     */
    private void updateForm() {

        m_popupContent.displayValues(m_editValue);
    }

    /**
     * Updates the marker position according to the current location value.<p>
     */
    private native void updateMarkerPosition()/*-{
		var map = this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_map;
		var pos = this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::getCurrentPosition()();
		var marker = this.@org.opencms.gwt.client.ui.input.location.CmsLocationController::m_marker;
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
									self.@org.opencms.gwt.client.ui.input.location.CmsLocationController::setPosition(FFZZ)(lat,lng,false,true);
								});
			} catch (e) {
				$wnd.alert(e.message);
			}
		} else {
			marker.setPosition(pos);
		}
		map.panTo(pos);
		map.setCenter(pos);
    }-*/;

}
