(function(cms) {
   var M = cms.messages;
   var loadingSign = null;
   var searchResultList = [];
   var /** Object */ searchParams = cms.search.searchParams = {
      'page': -1,
      'query': '',
      'path': '',
      'types': null,
      'hasMore': false,
      'totalResults': 0
   };
   /** The list of searchable resource types. */
   var /** Array */ searchTypes = cms.search.searchTypes = null;
   
   /**
    * Handler for search results, but not for the first page of results of a new search
    * @param {Object} ok status of the AJAX request
    * @param {Object} data the JSON data from the AJAX response.
    */
   var _handleResults = function(ok, data) {
      if (!ok) {
         loadingSign.stop();
      }
      cms.data.loadNecessarySubcontainerElements(data.elements, function(ok2, data2) {
         searchParams.page += 1;
         var searchResults = data.elements;
         for (var i = 0; i < searchResults.length; i++) {
            var result = searchResults[i];
            cms.data.elements[result.id] = result;
            _addResult(result);
         }
         searchParams.hasMore = data.hasmore;
         searchParams.totalResults = data.count;
         if (searchParams.totalResults > 0) {
            $('#cms-search-list').parent('*:hidden').css('display', 'block');
         } else {
            $('#cms-search-list').parent('*:visible').css('display', 'none');
         }
         $('#cms-search .ui-widget-shadow').css('height', $('#cms-search').outerHeight() + 1);
         loadingSign.stop();
      });
   }
   
   /**
    * Handler for the first page of search results of a new search.<p>
    *
    * @param {Object} ok
    * @param {Object} data
    */
   var _handleNewResults = function(ok, data) {
   
      if (!ok) {
         return;
      }
      _handleResults(ok, data);
   }
   
   /**
    * Adds an element to the list of search results in the DOM.<p>
    *
    * @param {Object} element the element which should be added to the search results.
    */
   var _addResult = function(/**Object*/result) {
   
      searchResultList.push(result.id);
      var $content = result.getContent('_DEFAULT_');
      var $inner = $('#cms-search-list');
      
      $('.cms-head', $content).append('<a class="cms-handle cms-move"></a>');
      $inner.append($content);
      
   }
   
   /**
    * Removes all search results from the DOM.<p>
    */
   var _clearResults = function() {
      searchResultList.length = 0;
      var $inner = $('#cms-search-list');
      $inner.empty();
      $('#cms-search .ui-widget-shadow').css('height', $('#cms-search').outerHeight() + 1);
   }
   
   /**
    * Handler for the results of the checkLastSearch function.<p>
    *
    * @param {boolean} ok the status of the AJAX call
    * @param {Object} data the JSON data from the AJAX call
    */
   var _handleLastSearch = function(/**boolean*/ok, /**Object*/ data) {
   
      if (!ok) {
         loadingSign.stop();
         return;
      }
      if (data.elements) {
         cms.data.loadNecessarySubcontainerElements(data.elements, function(ok2, data2) {
            searchParams.page = 1;
            searchParams.query = data.text;
            searchParams.path = data.location;
            $.each(searchParams.types, function() {
               this.checked = ($.inArray(this.name, data.type) >= 0);
            });
            
            _clearResults();
            var searchResults = data.elements;
            for (var i = 0; i < searchResults.length; i++) {
               var result = searchResults[i];
               cms.data.elements[result.id] = result;
               _addResult(result);
            }
            searchParams.hasMore = data.hasmore;
            searchParams.totalResults = data.count;
            if (searchParams.totalResults > 0) {
               $('#cms-search-list').parent('*:hidden').css('display', 'block');
            } else {
               $('#cms-search-list').parent('*:visible').css('display', 'none');
            }
            $('#cms-search .ui-widget-shadow').css('height', $('#cms-search').outerHeight() + 1);
            loadingSign.stop();
         });
      }
   }
   
   
   /**
    * Starts a new search.<p>
    *
    * @param {String} query the search query string
    * @param {Array} type the string which is a comma-separated list of types to which the search is restricted
    * @param {String} path the VFS path in which to search
    */
   var startNewSearch = cms.search.startNewSearch = function(/**String*/query, /**Array<String>*/ types, /**String*/ path) {
   
      searchParams.page = 0;
      searchParams.path = path;
      searchParams.types = types;
      searchParams.query = query;
      _clearResults();
      _resetScroll();
      cms.data.postJSON(cms.data.ACTION_SEARCH, {
         'action': cms.data.ACTION_SEARCH,
         'text': query,
         'type': _searchTypes(types),
         'location': path,
         'page': 0
      }, _handleNewResults);
   }
   
   /**
    * Continues the last search, e.g. when scrolling past the last loaded search result.<p>
    */
   var continueSearch = cms.search.continueSearch = function() {
      cms.data.postJSON(cms.data.ACTION_SEARCH, {
         'text': searchParams.query,
         'type': _searchTypes(searchParams.types),
         'location': searchParams.path,
         'page': searchParams.page
      }, _handleResults);
   }
   
   /**
    * Converts the client-side search types in the server-side format.<p>
    *
    * @param {Array} searchTypes the search types to convert
    */
   var _searchTypes = function(/**Array*/searchTypes) {
   
      var/**Array<String>*/ sendTypes = [];
      if (searchTypes) {
         $.each(searchTypes, function() {
            if (this.checked) {
               sendTypes.push(this.name);
            }
         });
      }
      return sendTypes;
   }
   
   /**
    * Sends the current search parameters to the server to check if they're the same
    * as the last search performed.<p>
    *
    * @param {Function} callback the callback that will be called after normal processing of the AJAX response
    */
   var checkLastSearch = cms.search.checkLastSearch = function(/**Function(boolean,Object)*/callback) {
      _resetScroll();
      cms.data.postJSON(cms.data.ACTION_LS, {
         'text': searchParams.query,
         'type': _searchTypes(searchParams.types),
         'location': searchParams.path,
         'page': 0
      }, function(ok, data) {
         _handleLastSearch(ok, data);
         callback(ok, data);
      });
   };
   
   /**
    * Saves the search parameters from the search dialog
    * @param {Object} searchParams the search parameters
    */
   var _saveSearchInput = function(/**Object*/searchParams) {
   
      var/**JQuery*/ $context = $('#' + cms.html.searchDialogId);
      searchParams.query = $('input.cms-search-query', $context).val();
      searchParams.types = [];
      $('input.cms-search-type', $context).each(function() {
         var /**JQuery*/ $checkbox = $(this);
         searchParams.types.push({
            'name': $checkbox.attr('value'),
            'checked': $checkbox.is(":checked")
         });
      });
      searchParams.path = $('input.cms-search-path', $context).val();
   };
   
   /**
    * Sets the input values in the search dialog to the previously saved values.
    */
   var _restoreSearchInput = function() {
   
      var/**JQuery*/ $context = $('#' + cms.html.searchDialogId);
      if (searchParams.query) {
         $('input.cms-search-query', $context).val(searchParams.query);
      }
      if (searchParams.types) {
         $.each(searchParams.types, function() {
            $('#' + cms.html.searchTypePrefix + this.name).attr('checked', this.checked);
         });
      }
      if (searchParams.path) {
         $('input.cms-search-path', $context).val(searchParams.path);
      }
   };
   
   var _setSearchMode = function(isAdvanced) {
      var $basic = $('#cms-search-dialog .cms-basic-search');
      var $advanced = $('#cms-search-dialog .cms-advanced-search');
      if (isAdvanced) {
         $basic.hide();
         $advanced.show();
      } else {
         $advanced.hide();
         $basic.show();
      }
   }
   
   /**
    * Initialize everything needed for the search.
    */
   var initSearch = cms.search.initSearch = function() {
      loadingSign = new cms.toolbar.LoadingSign(".cms-loading", 500, _showLoading, _hideLoading);
      var bodyEl = $(document.body);
      // scroll bar may not be at the top after reloading the page,  
      // which could cause multiple search result pages to be reloaded 
      $('#cms-search-list').closest('.cms-scrolling').scrollTop(0);
      
      var st = cms.search.searchTypes;
      bodyEl.append(cms.html.searchDialog(st));
      
      var $dialog = $('#cms-search-dialog');
      
      var _submitSearch = function() {
         if (!cms.util.validateForm($('form', $('#cms-search-dialog')))) {
            return;
         }
         $dialog.dialog('close');
         var /**Object*/ sp = {};
         _saveSearchInput(sp);
         loadingSign.start();
         startNewSearch(sp.query, sp.types, sp.path);
         _saveSearchInput(cms.search.searchParams);
      }
      
      $('#cms-search-query').keypress(function(event) {
         var keycode = event.keyCode ? event.keyCode : event.which;
         if (keycode == 13) {
            _submitSearch();
         }
      });
      
      $('#cms-search-dialog .cms-to-basic-search').click(function() {
         _setSearchMode(false);
      });
      
      $('#cms-search-dialog .cms-to-advanced-search').click(function() {
         _setSearchMode(true);
      });
      var buttons = [];
      buttons[M.SEARCH_DIALOG_BUTTON_SEARCH] = _submitSearch;
      buttons[M.SEARCH_DIALOG_BUTTON_CANCEL] = function() {
         $(this).dialog('close');
         $('form span.ade-error', $('#cms-search-dialog')).remove();
      };
      
      
      $('#cms-search-dialog').dialog({
         autoOpen: false,
         modal: true,
         zIndex: 99999,
         width: 340,
         title: M.SEARCH_DIALOG_TITLE,
         resizable: false,
         buttons: buttons
      });
      $('input', $('#cms-search-dialog')).customInput();
      
      $(".cms-search-tree").click(function() {
         window.open(cms.data.TREE_URL, '_blank', 'menubar=no');
      })
      
      $(".cms-search-button").click(function() {
         _restoreSearchInput();
         $('#cms-search-dialog').dialog('open');
         
      });
   };
   
   
   /**
    * Initializes the scroll handler for the search menu.
    */
   var initScrollHandler = cms.search.initScrollHandler = function() {
      if (!loadingSign) {
         loadingSign = new cms.toolbar.LoadingSign(".cms-loading", 500, _showLoading, _hideLoading);
      }
      var i = 0;
      var $inner = $('#cms-search-list');
      var $scrolling = $('#cms-search-list').closest('.cms-scrolling');
      $scrolling.scroll(function() {
         var delta = $inner.height() - $scrolling.height() - $scrolling.scrollTop();
         if (delta < 64 && !loadingSign.isLoading && searchParams.hasMore) {
            continueSearch();
            loadingSign.start();
         }
      });
   }
   
   /**
    * Resets the scroll bar of the search menu.
    */
   var _resetScroll = function() {
      var $scrolling = $('#cms-search-list').closest('.cms-scrolling');
      $scrolling.scroll();
      $scrolling.scrollTop(0);
      initScrollHandler();
   }
   
   /**
    * Returns the number of search results currently in the search menu.
    */
   var getNumberOfResults = cms.search.getNumberOfResults = function() {
      return $('#cms-search-list').children().size();
   }
   
   /**
    * Returns the estimated total number of search results.
    */
   var getTotalNumberOfResults = cms.search.getTotalNumberOfResults = function() {
      return searchParams.totalResults;
   }
   
   /**
    * Show the 'LOADING' text for the search menu.
    */
   var _showLoading = function() {
      $('.cms-loading').text(M.SEARCH_LOADING_SIGN);
   }
   
   /**
    * Hide the LOADING text for the search menu and display the number of search results loaded instead.
    */
   var _hideLoading = function() {
      $('.cms-loading').text(cms.util.format(M.SEARCH_NUM_RESULTS, cms.search.getNumberOfResults(), cms.search.getTotalNumberOfResults()));
   }
   
   
})(cms);
