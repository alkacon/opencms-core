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

        /**
         * Constructor.<p>
         *
         * @param address the address
         */
        public LocationSuggestion(String address) {

            m_address = address;
        }

        /**
         * @see com.google.gwt.user.client.ui.SuggestOracle.Suggestion#getDisplayString()
         */
        public String getDisplayString() {

            return m_address;
        }

        /**
         * @see com.google.gwt.user.client.ui.SuggestOracle.Suggestion#getReplacementString()
         */
        public String getReplacementString() {

            return m_address;
        }

    }

    /** The location controller. */
    private CmsLocationController m_controller;

    /** The google geocoder instance. */
    private JavaScriptObject m_geocoder;

    /**
     * Constructor.<p>
     *
     * @param controller the location controller
     */
    public CmsLocationSuggestOracle(CmsLocationController controller) {

        m_controller = controller;
    }

    /**
     * Adds a location suggestion to the list.<p>
     *
     * @param suggestions the suggestions list
     * @param address the address
     */
    private static void addSuggestion(List<LocationSuggestion> suggestions, String address) {

        suggestions.add(new LocationSuggestion(address));
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
		var controller = this.@org.opencms.gwt.client.ui.input.location.CmsLocationSuggestOracle::m_controller;
		var pos = controller.@org.opencms.gwt.client.ui.input.location.CmsLocationController::getCurrentPosition()();
		if (this.@org.opencms.gwt.client.ui.input.location.CmsLocationSuggestOracle::m_geocoder == null) {
			this.@org.opencms.gwt.client.ui.input.location.CmsLocationSuggestOracle::m_geocoder = new $wnd.google.maps.places.AutocompleteService();
		}
		this.@org.opencms.gwt.client.ui.input.location.CmsLocationSuggestOracle::m_geocoder
				.getPlacePredictions(
						{
							'input' : query,
							'location' : pos,
							'radius' : 10000
						},
						function(results, status) {
							var suggestions = @org.opencms.gwt.client.ui.input.location.CmsLocationSuggestOracle::createSuggestList()();
							// check to see if we have at least one valid address
							if (results
									&& (status == $wnd.google.maps.places.PlacesServiceStatus.OK)) {
								for (var i = 0; i < results.length; i++) {
									var lat = 0;
									//results[i].geometry.location.lat();
									var lng = 0;
									//results[i].geometry.location.lng();
									var address = results[i].description;
									//results[i].formatted_address;
									@org.opencms.gwt.client.ui.input.location.CmsLocationSuggestOracle::addSuggestion(Ljava/util/List;Ljava/lang/String;)(suggestions, address);
								}
							}
							@org.opencms.gwt.client.ui.input.location.CmsLocationSuggestOracle::respond(Lcom/google/gwt/user/client/ui/SuggestOracle$Request;Ljava/util/List;Lcom/google/gwt/user/client/ui/SuggestOracle$Callback;)(request, suggestions, callback);
						});
    }-*/;

}
