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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.SuggestOracle;

/**
 * A suggest oracle for locations based on the google maps API.<p>
 */
public class CmsLocationSuggestOracle extends SuggestOracle {

    /**
     * The location suggestion data.<p>
     */
    public static class LocationSuggestion implements SuggestOracle.Suggestion {

        /** The address. */
        private String m_address;

        /** The latitude. */
        private float m_latitude;

        /** The longitude. */
        private float m_longitude;

        /**
         * Constructor.<p>
         * 
         * @param address the address
         * @param latitude the latitude
         * @param longitude the longitude
         */
        public LocationSuggestion(String address, float latitude, float longitude) {

            m_address = address;
            m_latitude = latitude;
            m_longitude = longitude;
        }

        /**
         * Returns the address.<p>
         * 
         * @return the address
         */
        public String getAddress() {

            return m_address;
        }

        /**
         * @see com.google.gwt.user.client.ui.SuggestOracle.Suggestion#getDisplayString()
         */
        public String getDisplayString() {

            return getAddress();
        }

        /**
         * Returns the latitude.<p>
         * 
         * @return the latitude
         */
        public float getLatitude() {

            return m_latitude;
        }

        /**
         * Returns the longitude.<p>
         * 
         * @return the longitude
         */
        public float getLongitude() {

            return m_longitude;
        }

        /**
         * @see com.google.gwt.user.client.ui.SuggestOracle.Suggestion#getReplacementString()
         */
        public String getReplacementString() {

            return getAddress();
        }

    }

    /** The google geocoder instance. */
    private JavaScriptObject m_geocoder;

    /**
     * Adds a location suggestion to the list.<p>
     * 
     * @param suggestions the suggestions list
     * @param address the address
     * @param latitude the latitude
     * @param longitude the longitude
     */
    private static void addSuggestion(
        List<LocationSuggestion> suggestions,
        String address,
        float latitude,
        float longitude) {

        suggestions.add(new LocationSuggestion(address, latitude, longitude));
    }

    /**
     * Creates a location suggestions list.<p>
     * 
     * @return the location suggestions list
     */
    private static List<LocationSuggestion> createSuggestList() {

        return new ArrayList<LocationSuggestion>();
    }

    /**
     * Executes the suggestions callback.<p>
     * 
     * @param request the suggestions request
     * @param suggestions the suggestions
     * @param callback the callback
     */
    private static void respond(Request request, List<LocationSuggestion> suggestions, Callback callback) {

        callback.onSuggestionsReady(request, new Response(suggestions));
    }

    /**
     * @see com.google.gwt.user.client.ui.SuggestOracle#requestSuggestions(com.google.gwt.user.client.ui.SuggestOracle.Request, com.google.gwt.user.client.ui.SuggestOracle.Callback)
     */
    @Override
    public native void requestSuggestions(final Request request, final Callback callback) /*-{
                                                                                          var query = request.@com.google.gwt.user.client.ui.SuggestOracle.Request::getQuery()();
                                                                                          if (this.@org.opencms.gwt.client.ui.input.location.CmsLocationSuggestOracle::m_geocoder == null) {
                                                                                          this.@org.opencms.gwt.client.ui.input.location.CmsLocationSuggestOracle::m_geocoder = new $wnd.google.maps.Geocoder();
                                                                                          }
                                                                                          this.@org.opencms.gwt.client.ui.input.location.CmsLocationSuggestOracle::m_geocoder
                                                                                          .geocode(
                                                                                          {
                                                                                          'address' : query
                                                                                          },
                                                                                          function(results, status) {
                                                                                          var suggestions = @org.opencms.gwt.client.ui.input.location.CmsLocationSuggestOracle::createSuggestList()();
                                                                                          // check to see if we have at least one valid address
                                                                                          if (results
                                                                                          && (status == $wnd.google.maps.GeocoderStatus.OK)) {
                                                                                          for (var i = 0; i < results.length; i++) {
                                                                                          var lat = results[i].geometry.location
                                                                                          .lat();
                                                                                          var lng = results[i].geometry.location
                                                                                          .lng();
                                                                                          var address = results[i].formatted_address;
                                                                                          @org.opencms.gwt.client.ui.input.location.CmsLocationSuggestOracle::addSuggestion(Ljava/util/List;Ljava/lang/String;FF)(suggestions, address, lat, lng);
                                                                                          }
                                                                                          }
                                                                                          @org.opencms.gwt.client.ui.input.location.CmsLocationSuggestOracle::respond(Lcom/google/gwt/user/client/ui/SuggestOracle$Request;Ljava/util/List;Lcom/google/gwt/user/client/ui/SuggestOracle$Callback;)(request, suggestions, callback);
                                                                                          });
                                                                                          }-*/;

}
