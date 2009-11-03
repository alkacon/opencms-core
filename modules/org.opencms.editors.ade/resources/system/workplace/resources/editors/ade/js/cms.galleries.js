(function(cms) {

    /** html-id for tabs. */
    var idTabs = cms.galleries.idTabs = 'tabs';
    
    /** html-id for the tab with search results. */
    var idTabResult = cms.galleries.idTabResult = 'tabs-result';
    
    /** html-class for the inner of the scrolled list with items. */
    var classScrollingInner = cms.galleries.classScrollingInner = 'cms-list-scrolling-innner';
    
    /** html-class for hovered list item elements. */
    var classListItemHover = cms.galleries.classListItemHover = 'cms-list-item-hover';
    
    /** html-class for active list item elements. */
    var classListItemActive = cms.galleries.classListItemActive = 'cms-list-item-active';
    
    /** html-class for the item title in the list. */
    var classListItemTitle = cms.galleries.classListItemTitle = 'cms-list-title';
    
    /** html-class for the panel above or under the list of items. */
    var classListOptions = cms.galleries.classListOptions = 'cms-list-options';
    
    /** Array with available search criteria. */
    var keysSearchObject = cms.galleries.keysSearchObject = ['types', 'galleries', 'categories', 'searchquery'];
    
    /** Map of key words for the criteria buttons on the result tab. */
    var criteriaStr = cms.galleries.criteriaStr = {
        types: ['Type: ', 'Types: '],
        galleries: ['Gallery: ', 'Galleries: '],
        categories: ['Category: ', 'Categories: '],
        searchquery: ['Search query: ', 'Seach queries: ']
    
    };
    
    /** html fragment for the tab with the types' list. */
    var htmlTabTypesSceleton = cms.galleries.htmlTabTypesSceleton = '<div id="tabs-types">\
                <div id="typesoptions" class="ui-widget ' + cms.galleries.classListOptions + '">\
                    <span class="cms-drop-down">\
                        <label>Sort by:</label>\
                        <select name="types" size="1">\
                            <option value="Name">Name</option>\
                            <option value="Date">Date</option>\
                        </select>\
                    </span>\
                    <span class="cms-ft-search"><label>Search:</label><input type="text"/></span>\
                </div>\
                <div id="types" class="cms-list-scrolling ui-corner-all">\
                    <ul class="cms-list-scrolling-innner"></ul>\
                </div>\
                <div id="typesbuttons" class="' +
    cms.galleries.classListOptions +
    '">\
                    <button class="ui-state-default ui-corner-all">Select</button>\
                </div>\
              </div>';
    
    /** html fragment for the tab with the galleries' list. */
    var htmlTabGalleriesSceleton = cms.galleries.htmlTabGalleriesSceleton = '<div id="tabs-galleries">\
                <div id="galleriesoptions" class="ui-widget ' + cms.galleries.classListOptions + '">\
                    <span class="cms-drop-down">\
                        <label>Sort by:</label>\
                        <select name="galleries" size="1">\
                            <option value="Name">Name</option>\
                            <option value="Date">Date</option>\
                            <option value="Type">Type</option>\
                        </select>\
                    </span>\
                    <span class="cms-ft-search"><label>Search:</label><input type="text"/></span>\
                </div>\
                <div id="galleries" class="cms-list-scrolling ui-corner-all">\
                    <ul class="cms-list-scrolling-innner"></ul>\
                </div>\
                <div id="galleriesbuttons" class="' +
    cms.galleries.classListOptions +
    '">\
                    <button class="ui-state-default ui-corner-all">Select</button>\
                </div>\
              </div>';
    
    /** html fragment for the tab with the categories' list. */
    var htmlTabCategoriesSceleton = cms.galleries.htmlTabCategoriesSceleton = '<div id="tabs-categories">\
                <div id="categoriesoptions" class="ui-widget ' + cms.galleries.classListOptions + '">\
                    <span class="cms-drop-down">\
                        <label>Sort by:</label>\
                        <select name="types" size="1">\
                            <option value="Name">Name</option>\
                            <option value="hierarchy">Hierarchy</option>\
                        </select>\
                    </span>\
                    <span class="cms-ft-search"><label>Search:</label><input type="text"/></span>\
                </div>\
                <div id="categories" class="cms-list-scrolling ui-corner-all">\
                    <ul class="cms-list-scrolling-innner"></ul>\
                </div>\
                <div id="categoriesbuttons" class="' +
    cms.galleries.classListOptions +
    '">\
                    <button class="ui-state-default ui-corner-all">Select</button>\
                </div>\
              </div>';
    
    /** html fragment for the <li> in the galleries list. */
    var listGalleryElement = cms.galleries.listGalleryElement = function(itemTitle, itemUrl) {
    
        return $('<li></li>').addClass('cms-list').attr('id', itemUrl).append('<div class="cms-list-checkbox"></div>\
                         <div class="cms-list-item ui-widget-content ui-state-default ui-corner-all">\
                             <div class="cms-list-image"></div>\
                             <div class="cms-list-itemcontent">\
                                 <div class="' + cms.galleries.classListItemTitle + '">' + itemTitle + '</div>\
                                 <div class="cms-list-url">' +
        itemUrl +
        '</div>\
                             </div>\
                         </div>');
    }
    
    /** html fragment for the <li> in the types list. */
    var listTypeElement = cms.galleries.listTypeElement = function(itemTitle, itemId, itemDesc) {
    
        return $('<li></li>').addClass('cms-list').attr('id', itemId).append('<div class="cms-list-checkbox"></div>\
                         <div class="cms-list-item ui-widget-content ui-state-default ui-corner-all">\
                             <div class="cms-list-image"></div>\
                             <div class="cms-list-itemcontent">\
                                 <div class="' + cms.galleries.classListItemTitle + '">' + itemTitle + '</div>\
                                 <div class="cms-list-url">' +
        itemDesc +
        '</div>\
                             </div>\
                         </div>');
    }
    
    /** html fragment for the <li> in the categories list. */
    var listCategoryElement = cms.galleries.listCategoryElement = function(itemTitle, itemUrl, itemLevel) {
    
        return $('<li></li>').addClass('cms-list').attr('id', itemUrl).append('<div class="cms-list-checkbox"></div>\
                         <div class="cms-list-item ui-widget-content ui-state-default ui-corner-all">\
                             <div class="cms-list-image"></div>\
                             <div class="cms-list-itemcontent">\
                                 <div class="' + cms.galleries.classListItemTitle + '">' + itemTitle + '</div>\
                                 <div class="cms-list-url">' +
        itemUrl +
        '</div>\
                             </div>\
                         </div>');
    }
    
    /**
     * Map of selected search criteria.
     * 
     * types: array of resource ids for the available resource types
     * galleries: array of paths to the available galleries
     * categories: array of paths to the available categories
     * searchquery: the search key word
     * page: the page number of the requested result page
     * isChanged: map of flags indicating if one of the search creteria is changed and should be taken into account
     */
    var searchObject = cms.galleries.searchObject = {
        types: [],
        galleries: [],
        categories: [],
        searchquery: null,
        page: 1,
        isChanged: {
            types: true,
            galleries: true,
            categories: false,
            searchquery: false
        }
    };
    
    /**
     * Dummy content
     */
    var dummyGalleries = cms.galleries.dummyGalleries = [{
        title: 'Gallery 1',
        path: 'url/to/gallery1/',
        icon: '../../filetypes/downloadgallery.gif'
    }, {
        title: 'Gallery 2',
        path: 'url/to/gallery2/',
        icon: '../../filetypes/downloadgallery.gif'
    }, {
        title: 'Gallery 3',
        path: 'url/to/gallery3/',
        icon: '../../filetypes/downloadgallery.gif'
    }, {
        title: 'Gallery 4',
        path: 'url/to/gallery4/',
        icon: '../../filetypes/imagegallery.gif'
    }, {
        title: 'Gallery 5',
        path: 'url/to/gallery5/',
        icon: '../../filetypes/imagegallery.gif'
    }, {
        title: 'Gallery 6',
        path: 'url/to/gallery6/',
        icon: '../../filetypes/imagegallery.gif'
    }, {
        title: 'Gallery 7',
        path: 'url/to/gallery7/',
        icon: '../../filetypes/imagegallery.gif'
    }, {
        title: 'Gallery 8',
        path: 'url/to/gallery8/',
        icon: '../../filetypes/imagegallery.gif'
    }];
    
    /**
     * Dummy content
     */
    var dummyCategories = cms.galleries.dummyCategories = [{
        title: 'Category 1',
        path: 'url/to/Category1/',
        icon: '../../filetypes/folder.gif',
        level: 0
    }, {
        title: 'Category 2',
        path: 'url/to/Category2/',
        icon: '../../filetypes/folder.gif',
        level: 1
    }, {
        title: 'Category 3',
        path: 'url/to/Category3/',
        icon: '../../filetypes/folder.gif',
        level: 2
    }, {
        title: 'Category 4',
        path: 'url/to/Category4/',
        icon: '../../filetypes/folder.gif',
        level: 1
    }, {
        title: 'Category 5',
        path: 'url/to/Category5/',
        icon: '../../filetypes/folder.gif',
        level: 0
    }, {
        title: 'Category 6',
        path: 'url/to/Category6/',
        icon: '../../filetypes/folder.gif',
        level: 1
    }, {
        title: 'Category 7',
        path: 'url/to/Category7/',
        icon: '../../filetypes/folder.gif',
        level: 2
    }, {
        title: 'Category 8',
        path: 'url/to/gallery8/',
        icon: '../../filetypes/folder.gif',
        level: 3
    }];
    
    /**
     * Dummy content
     */
    var dummyTypes = cms.galleries.dummyTypes = [{
        title: 'Type 1',
        id: 'id1',
        desc: 'url/to/type1/'
    }, {
        title: 'Type 2',
        id: 'id2',
        desc: 'url/to/type2/'
    }, {
        title: 'Type 3',
        id: 'id3',
        desc: 'url/to/type3/'
    }, {
        title: 'Type 4',
        id: 'id4',
        desc: 'url/to/type4/'
    }, {
        title: 'Type 5',
        id: 'id5',
        desc: 'url/to/type5/'
    
    }, {
        title: 'Type 6',
        id: 'id6',
        desc: 'url/to/type6/'
    }, {
        title: 'Type 7',
        id: 'id7',
        desc: 'url/to/type7/'
    }, {
        title: 'Type 8',
        id: 'id8',
        desc: 'url/to/type8/'
    }];
    
    /**
     * Add html for search criteria to result tab
     * 
     * @param {String} content the nice-list of items for given search criteria 
     * @param {String} searchCriteria the given search criteria
     */
    var addCreteriaToTab = cms.galleries.addCreteriaToTab = function(/** String*/content, /** String*/ searchCriteria) {
        var target = $('<span id="selected' + searchCriteria + '" class="cms-searchquery ui-widget-content ui-state-hover ui-corner-all"></span>').appendTo($('#' + cms.galleries.idTabResult));
        target.append('<span class="cms-search-title">' + content + '</span>').append('<span class="cms-search-remove">&nbsp;</span>');
    }
    
    /**
     * Fills the list in the search criteria tabs.
     * 
     */
    var fillCriteriaTabs = cms.galleries.fillCriteriaTabs = function() {
    
        cms.galleries.fillTypes(cms.galleries.dummyTypes);
        cms.galleries.fillGalleries(cms.galleries.dummyGalleries);
        cms.galleries.fillCategories(cms.galleries.dummyCategories);               
        
        // TODO: go through html and the search object and mark the already selected search criteria     
    }
    
    /** 
     * Creates the HTML for the list of available categories from the given JSON array data.
     *
     * Comment: this is an adjusted copy from galleryfunctions.js of the old galleries
     *
     * @param {Object} JSON object with categories
     */
    var fillCategories = cms.galleries.fillCategories = function(data) {
        var categories = data;
        
        // add the types to the list
        for (var i = 0; i < categories.length; i++) {
            var currCategory = categories[i];
            
            $('#categories > ul').append(listCategoryElement(currCategory.title, currCategory.path, currCategory.level));
        }
        // set isChanged flag, so the search will be send to server
        cms.galleries.searchObject.isChanged.categories = true;
    }    
    
    /** 
     * Creates the HTML for the list of available galleries from the given JSON array data.
     *
     * Comment: this is an adjusted copy from galleryfunctions.js of the old galleries
     *
     * @param {Object} JSON object with categories
     */
    var fillGalleries = cms.galleries.fillGalleries = function(data) {
        var galleries = data;
        
        // add the galleries to the list
        for (var i = 0; i < galleries.length; i++) {
            var currGall = galleries[i];
            
            $('#galleries > ul').append(listGalleryElement(currGall.title, currGall.path));
        }
        // set isChanged flag, so the search will be send to server
        cms.galleries.searchObject.isChanged.galleries = true;             
    }
       
    /**
     * Adds the search criteria html to the result tab.
     */
    var fillResultTab = cms.galleries.fillResultTab = function() {
    
        $.each(cms.galleries.keysSearchObject, function() {
            var searchCriteria = this;
            if (cms.galleries.searchObject.isChanged[searchCriteria]) {
            
                var singleSelect = cms.galleries.criteriaStr[searchCriteria][0];
                var multipleSelect = cms.galleries.criteriaStr[searchCriteria][1];
                var titles = '';
                // remove criteria button from result tab
                $('#selected' + searchCriteria).remove();
                var selectedLis = $('#' + searchCriteria).find('.' + cms.galleries.classListItemActive).find('.' + cms.galleries.classListItemTitle);
                // if any search criteria is selected
                if (selectedLis.length == 1) {
                    titles = singleSelect.concat($(selectedLis[0]).text());
                    cms.galleries.addCreteriaToTab(titles, searchCriteria);
                } else if (selectedLis.length > 1) {
                    $.each(selectedLis, function() {
                        if (titles.length == 0) {
                            titles = multipleSelect.concat($(this).text());
                        } else {
                            titles = titles.concat(", ").concat($(this).text());
                        }
                    });
                    cms.galleries.addCreteriaToTab(titles, searchCriteria);
                    // TODO: trigger send search criterias to server
                    //.....
                }
                cms.galleries.searchObject.isChanged[searchCriteria] = false;
            }
        });
    }
    
    /** 
     * Creates the HTML for the list of available types from the given JSON array data.
     *
     * Comment: this is an adjusted copy from galleryfunctions.js of the old galleries
     *
     * @param {Object} JSON object with resource types
     */
    var fillTypes = cms.galleries.fillTypes = function(data) {
        var types = data;
        
        // add the types to the list
        for (var i = 0; i < types.length; i++) {
            var currType = types[i];
            
            $('#types > ul').append(listTypeElement(currType.title, currType.id, currType.desc));
        }
        // set isChanged flag, so the search will be send to server
        cms.galleries.searchObject.isChanged.types = true;             
    }
    
    /**
     * Callback function for the one click event in the gallery list
     */
    var clickListItem = cms.galleries.clickListItem = function() {
    
        // id of the li tag and type of search 
        var itemId = $(this).attr('id');
        var itemCriteria = $(this).closest('div').attr('id');
        
        // adjust the active status of the gallery in the gallery list        
        var index = $.inArray(itemId, cms.galleries.searchObject[itemCriteria]);
        // case 1: gallery is selected, -> deselect the gallery on second click
        if (index != -1) {
            // remove gallery path from search object
            cms.galleries.searchObject[itemCriteria].splice(index, 1);
            // remove highlighting
            $(this).removeClass(cms.galleries.classListItemActive);
            
            // set isChanged flag, so the search will be send to server
            cms.galleries.searchObject.isChanged[itemCriteria] = true;
            
            
            // case 2: gallery is not selected yet, -> select the gallery on click
        } else {
            // push the gallery path to the search object
            cms.galleries.searchObject[itemCriteria].push(itemId);
            // add highlighting
            $(this).addClass(cms.galleries.classListItemActive);
            
            $('#' + cms.galleries.idTabs).tabs("enable", 0);
            
            // set isChanged flag, so the search will be send to server
            cms.galleries.searchObject.isChanged[itemCriteria] = true;
            
        }        
    }
    
    /**
     * Callback function for the double click event in the search criteria list
     */
    var dblclickListItem = cms.galleries.dblclickListItem = function() {
    
        // id of the li tag and type of search 
        var itemId = $(this).attr('id');
        var itemCriteria = $(this).closest('div').attr('id');
        
        // remove all selected items from search object
        cms.galleries.removeItemsFromSearchObject(itemCriteria);
        // add selected item to search object and set highlighting
        cms.galleries.searchObject[itemCriteria].push(itemId);
        $(this).addClass(cms.galleries.classListItemActive);
        // set isChanged flag, so the search will be send to server on 'select' tab event
        cms.galleries.searchObject.isChanged[itemCriteria] = true;
        
        //switch to result tab index = 0
        $('#' + cms.galleries.idTabs).tabs("enable", 0);
        $('#' + cms.galleries.idTabs).tabs('select', 0);
    }
    
    /**
     * Init function for search/add dialog.
     */
    var initAddDialog = cms.galleries.initAddDialog = function () {
       // add galleries tab html
	   $('#' + cms.galleries.idTabs)
            .append(cms.galleries.htmlTabTypesSceleton)
            .append(cms.galleries.htmlTabGalleriesSceleton)
            .append(cms.galleries.htmlTabCategoriesSceleton);
                         
       // bind the select tab event, fill the content of the result tab on selection
       $('#' + cms.galleries.idTabs).tabs({
            select: function(event, ui) {
                // if result tab is selected
                if (ui.index == 0 ) {
                    cms.galleries.fillResultTab();    
                }                
            }
       });
                             
         
       cms.galleries.fillCriteriaTabs();
       $('#' + cms.galleries.idTabs).tabs("select", 1);
	   $('#' + cms.galleries.idTabs).tabs("disable", 0);
       
       // bind all other events at the end          
       // bind click, dbclick and hover events on items in criteria lists
       $('li.cms-list').live('dblclick', cms.galleries.dblclickListItem)
      	    .live('click', cms.galleries.clickListItem)   
            .hover(function() {
                $(this).addClass(cms.galleries.classListItemHover);
            }, function() {
               $(this).removeClass(cms.galleries.classListItemHover);
       });
         
       // bind click events to remove search criteria html from result tab            
       $('span.cms-search-remove').live('click',cms.galleries.removeCriteria);
       
       // bind the hover and click events to the ok button under the criteria lists    
       $('.' + cms.galleries.classListOptions + ' button')
           .hover(
                function() {                   
                   $(this).addClass('ui-state-hover');
                },
                function() {                    
                   $(this).removeClass('ui-state-hover');                   
                })
           .click(function() {                   
               //switch to result tab index = 0
               $('#' + cms.galleries.idTabs).tabs("enable", 0);
               $('#' + cms.galleries.idTabs).tabs('select', 0);  
        });  
    }
    
    /**
     * Removes all selected items from the search object and adjust highlighting
     * 
     * @param {String} searchCriteria key
     */
    var removeItemsFromSearchObject = cms.galleries.removeItemsFromSearchObject = function(/**String*/searchCriteria) {
        // if at least one item is selected
        if (cms.galleries.searchObject[searchCriteria].length > 0) {
            //remove highlighting for all selected items in the list
            $('#' + searchCriteria + ' li.cms-list').removeClass(cms.galleries.classListItemActive);
            // remove all items from search object                                                     
            cms.galleries.searchObject[searchCriteria] = [];
        }
    }

    /**
     * Removes the html of the search criteria from the result tab and from search object
     */
    var removeCriteria = cms.galleries.removeCriteria = function() {
        var parentId = $(this).parent().attr('id');
        $.each(cms.galleries.keysSearchObject, function() {
            var selectedId = 'selected' + this;
            if (parentId == selectedId) {
                $('#' + selectedId).remove();
                cms.galleries.removeItemsFromSearchObject(this);
            }
        });

        // TODO: refresh the list of search results on the result tab
    }

})(cms);