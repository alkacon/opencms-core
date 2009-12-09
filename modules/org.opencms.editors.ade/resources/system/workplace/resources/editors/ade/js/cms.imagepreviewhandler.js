(function(cms) {

   ///// Content Handler Function definition //////////////////////////    
   /** Initial flag for the genrating of the values for the format select box. */
   var isInitFormatSelectBox = true;
   
   /** Default values for the max size of the image in the preview. */
   var defaultPreview = {
      'height': 295,
      'width': 550
   };
   
   /** String constants to use in html. */
   var keys = {
      'previewWidth': 'width',
      'previewHeight': 'height',
      'imageFormat': 'format'
   };
   
   /** Default format value for the drop down. */
   var defaultFormatOptions = 'original|user|free cropping|small|large';
   var defaultFormatValues = ['original', 'user', 'free', '200x?', '500x?'];
   
   /** Array with format values for the drop down. */
   var formatDropDown = [];
   
   /** An array with format values for image size calculation. */
   var formatSelections = [];
   
   /** The index of the selected item in the drop down. */
   var formatSelected = 0;
   
   /** The flag for the state of lock ration for selected image. */
   var lockRatio = true;
   
   /**
    * Displays the content of the preview item.
    *
    * @param {Object} itemData server response as JSON object with preview data
    */
   var showItemPreview = function(itemData) {
      if (isInitFormatSelectBox) {
         cms.galleries.getContentHandler(cms.imagepreviewhandler.imageContentTypeHandler['type'])['init']();
         isInitFormatSelectBox = false;
      }
      
      // displays the html preview
      $('.preview-area').append(itemData['previewdata']['itemhtml']);
      // read the json object for the active item
      var jsonForActiveImage = JSON.parse($('.preview-area').find("input[type='hidden']").val());
      
      // display editable properties
      showFormatEditArea(itemData['previewdata']['properties']);
      
      // display the image in the preview area
      showActiveItemPreviewArea(jsonForActiveImage, cms.galleries.activeItem['isInitial']);
      
      
      
      //bind click event to preview close button 
      $('#cms-preview div.close-icon').click(function() {
         if ($(this).hasClass('cms-properties-changed')) {
            cms.galleries.loadSearchResults();
            $(this).removeClass('cms-properties-changed');
         }
         $(this).parent().fadeOut('slow');
         
      });
      
      $('.button-bar button').live('mouseover', function() {
         $(this).toggleClass('ui-state-hover', true);
      }).live('mouseout', function() {
         $(this).toggleClass('ui-state-hover', false);
      });
      
      // fade in the preview      
      $('#cms-preview').fadeIn('slow');
      
      // change the initial flag 
      if (cms.galleries.activeItem['isInitial'] == true) {
         cms.galleries.activeItem['isInitial'] == false;
      }
   }
   
   /**
    * Generates html for the editable properties and add to the dom.
    *
    * @param {Object} itemProperties server response as JSON object with the editable properties
    */
   var showFormatEditArea = function(itemProperties) {
      // display editable properties
      cms.galleries.getContentHandler()['showEditArea'](itemProperties);
      
      // add button to select the edit area
      $('.edit-form').find('button-bar').append('<button name="showEditArea" disabled="true" class="ui-state-default ui-corner-all">\
                                                    <span class="cms-galleries-button">Edit resource properties</span>\
                                              </button>').append('<button name="showFormEditArea" disabled="false" class="ui-state-default ui-corner-all">\
                                                    <span class="cms-galleries-button">Edit image formats</span>\
                                                </button>');
      $('.edit-area').hide();
      
      // add format edit area to preview
      var target = $('<div class="edit-format-area"></div>').appendTo('#cms-preview');
      var buttonBar = $('<div class="button-bar"></div>').appendTo(target);
      if (cms.galleries.displaySelectButton()) {
         buttonBar.append('<button name="previewSelect" class="ui-state-default ui-corner-all">\
                                   <span class="cms-galleries-button cms-galleries-icon-apply cms-icon-text">Select</span>\
                             </button>');
         buttonBar.find('button[name="previewSelect"]').click(function() {
            var itemId = $(this).closest('#cms-preview').attr('alt');
            cms.galleries.getContentHandler('image')['setValues'][cms.galleries.initValues['dialogMode']](itemId, cms.galleries.initValues['fieldId']);
         });
      }
      
      // add button to select the edit area
      buttonBar.append('<button name="showEditArea" class="cms-right ui-state-default ui-corner-all">\
                                                    <span class="cms-galleries-button">Edit resource properties</span>\
                                              </button>').append('<button name="showFormEditArea" disabled="true" class="cms-right ui-state-default ui-corner-all">\
                                                    <span class="cms-galleries-button">Edit image formats</span>\
                                                </button>');
      
      // generate editable foramt form for width and height
      var form = $('<div class="edit-form"></div>');
      // width input field    
      $('<div class="cms-editable-field"></div>').attr('alt', keys['previewWidth']).appendTo(form).append('<span class="cms-item-title">Width:</span>').append('<span class="cms-item-edit" style=" width: 100px;"></span>');
      // height input field    
      $('<div class="cms-editable-field"></div>').attr('alt', keys['previewHeight']).appendTo(form).append('<span class="cms-item-title">Height:</span>').append('<span class="cms-item-edit" style=" width: 100px;"></span>');
      $(target).append(form);
      
      // add format select box in widget or editor mode
      if (cms.galleries.displaySelectButton()) {
         // drop down to select format
         $('<div class="cms-drop-down cms-editable-field"></div>').attr('alt', keys['imageFormat']).appendTo(form).append('<label class="cms-item-title">Format:</label>');
         form.find('.cms-drop-down label').after($.fn.selectBox('generate', {
            values: formatDropDown,
            width: 150,
            /* Binds format select options. */
            select: function($this, self, value, index) {
               changeFormat(index);
            }
         }));
      }
      
      
      // bind direct input to the editable fields
      $('.cms-item-edit').directInput({
         marginHack: true,
         live: false,
         setValue: cms.galleries.getContentHandler()['markChangedProperty'],
         onChange: function(element, input) {
            $('#previewSave').removeAttr('disabled');
         }
      });
   }
   
   /**
    * Calculates the scaling parameters depending on the configuration and
    * display the image.
    *
    * @param {Object} activeImageData the JSONobject with additional information for the image resource type
    * @param {Object} isInitial true, if true
    */
   var showActiveItemPreviewArea = function(/**JSONobject*/activeImageData, /**Boolean*/ isInitial) {
      if (activeImageData == "none") {
         return;
      }
      
      $.extend(cms.galleries.activeItem, activeImageData["activeimage"], {
         'newwidth': 0,
         'newheight': 0
      });
      
      var widthField = $('.edit-format-area').find('div[alt="' + keys["previewWidth"] + '"]').find('.cms-item-edit');
      var heightField = $('.edit-format-area').find('div[alt="' + keys["previewHeight"] + '"]').find('.cms-item-edit');
      
      if (isInitial == true) {
         // initial image loaded
         var cropIt = false;
         /*if (getScaleValue(initValues.scale, "cx") != "") {
          cropIt = true;
          activeItem.cropx = getScaleValueInt(initValues.scale, "cx");
          activeItem.cropy = getScaleValueInt(initValues.scale, "cy");
          activeItem.cropw = getScaleValueInt(initValues.scale, "cw");
          activeItem.croph = getScaleValueInt(initValues.scale, "ch");
          var cropParams = "cx:" + activeItem.cropx;
          cropParams += ",cy:" + activeItem.cropy;
          cropParams += ",cw:" + activeItem.cropw;
          cropParams += ",ch:" + activeItem.croph;
          activeItem.crop = cropParams;
          initValues.scale = removeScaleValue(initValues.scale, "cx");
          initValues.scale = removeScaleValue(initValues.scale, "cy");
          initValues.scale = removeScaleValue(initValues.scale, "cw");
          initValues.scale = removeScaleValue(initValues.scale, "ch");
          }*/
         if (cms.galleries.initValues.useformats == true) {
            if (cms.galleries.initValues.imgwidth != "?") {
               widthField.text(cms.galleries.initValues.imgwidth);
               if (cms.galleries.initValues.imgheight == "?") {
                  onSizeChanged("Width", cms.galleries.initValues.imgwidth, false, false);
                  if (cropIt == true) {
                     var newHeight = Math.round(cms.galleries.activeItem.croph / (cms.galleries.activeItem.cropw / cms.galleries.initValues.imgwidth));
                     if (newHeight != parseInt(heightField.text())) {
                        setLockRatio(false);
                        heightField.text(newHeight);
                        onSizeChanged("Height", newHeight, false, false);
                     }
                  }
               } else if (parseInt(heightField.text()) != cms.galleries.initValues.imgheight) {
                  setLockRatio(false);
                  heightField.text(cms.galleries.initValues.imgheight);
                  onSizeChanged("Height", cms.galleries.initValues.imgheight, false, false);
               }
            } else if (cms.galleries.initValues.imgheight != "?") {
               heightField.text(cms.galleries.initValues.imgheight);
               onSizeChanged("Height", cms.galleries.initValues.imgheight, false, false);
               if (cropIt == true) {
                  var newWidth = Math.round(cms.galleries.activeItem.cropw / (cms.galleries.activeItem.croph / cms.galleries.initValues.imgheight));
                  if (newWidth != parseInt(widthField.text())) {
                     setLockRatio(false);
                     widthField.text(newWidth);
                     onSizeChanged("Width", newHeight, false, false);
                  }
               }
            }
            if (cms.galleries.initValues.widgetmode == "simple" && cms.galleries.initValues.showformats == true) {
               // refresh the format select box
               refreshSelectBox(true);
            }
            if (cropIt == true) {
               setCropActive(true);
            } else {
               setCropActive(false);
            }
         } else {
            var sizeChanged = false;
            if (cms.galleries.initValues.imgwidth != "") {
               var newWidth = parseInt(cms.galleries.initValues.imgwidth);
               widthField.text(newWidth);
               cms.galleries.activeItem.newwidth = newWidth;
               sizeChanged = true;
            }
            if (cms.galleries.initValues.imgheight != "") {
               var newHeight = parseInt(cms.galleries.initValues.imgheight);
               heightField.text(newHeight);
               cms.galleries.activeItem.newheight = newHeight;
               sizeChanged = true;
            }
            if (sizeChanged == true) {
               var testW = cms.galleries.activeItem.newwidth > 0 ? cms.galleries.activeItem.newwidth : cms.galleries.activeItem.width;
               var testH = cms.galleries.activeItem.newheight > 0 ? cms.galleries.activeItem.newheight : cms.galleries.activeItem.height;
               var delta = testW / cms.galleries.activeItem.width;
               var calH = Math.round(cms.galleries.activeItem.height * delta);
               if (calH != testH) {
                  setLockRatio(false);
               }
            }
            setCropActive(cropIt);
            if (cms.galleries.initValues.widgetmode != "simple" || cms.galleries.initValues.showformats == true) {
               // refresh the format select box
               refreshSelectBox();
            } else if (cms.galleries.initValues.showformats == false) {
                        //$("#croplink").hide();
            }
         }
         //initValues.scale = removeScaleValue(initValues.scale, "w");
         //initValues.scale = removeScaleValue(initValues.scale, "h");
      } else {
         // image loaded by user selection
         //$tabs.data("disabled.tabs", []);
         //$tabs.tabs("select", 0);
         resetSizes();
         setCropActive(false);
         if (cms.galleries.initValues.useformats != true) {
                  //$("#croplink").hide();
         }
         if (cms.galleries.initValues.useformats == true && initValues.showformats == true) {
                  //$("#croplink").show();
         }
      }
      try {
            // do additional stuff with the active image if necessary
         //activeImageAdditionalActions(isInitial);
      } catch (e) {
            }
      //$('#previmgname').html(activeItem.title);
      //showItemInfo(-1, "detail", activeItem, true);
   }
   
   
   /* Enables and disabled links and inputs depending if image is cropped or not. */
   /**
    * Displays the image format editing fields depending on the config parameters.
    *
    * @param {Object} isCropped 'true' if image is cropped, 'false' otherwise
    * @param {Object} forceRefreshPreview if true the image preview will be refreshed
    */
   var setCropActive = function(isCropped, forceRefreshPreview) {
      if (cms.galleries.initValues.widgetmode == "simple" && cms.galleries.initValues.showformats == false) {
            // disable input fields and buttons for simple widget mode      
      
         //$('#txtWidth').get(0).disabled = true;
         //$('#txtHeight').get(0).disabled = true;
         //$('#formatselect').get(0).disabled = true;
         //$('#resetsize').hide();
         //$('#locksizes').hide();
         //$('#cropremove').hide();
         //$('#cropinfo').hide();
      } else {
         if (isCropped == true) {
            // cropping has been set, disable input fields and refresh view
            $('#txtWidth').get(0).disabled = true;
            $('#txtHeight').get(0).disabled = true;
            $('#formatselect').get(0).disabled = true;
            $('#resetsize').hide();
            $('#locksizes').hide();
            $('#cropremove').show();
            $('#cropinfo').show();
         } else {
            // cropping has been disabled, enable input fields and refresh view
            if (cms.galleries.initValues.useformats == true && cms.galleries.initValues.showformats != true) {
                        // using formats, calculate image for currently selected size
               //changeFormat();
            } else if (cms.galleries.initValues.useformats == false) {
                        // only enable if not using formats
               //$('#txtWidth').get(0).disabled = false;
               //$('#txtHeight').get(0).disabled = false;
            }
            //$('#formatselect').get(0).disabled = false;
            //$('#resetsize').show();
            //$('#locksizes').show();
            //$('#cropremove').hide();
            //$('#cropinfo').hide();
         }
      }
      if (isCropped != cms.galleries.activeItem.isCropped || (forceRefreshPreview != null && forceRefreshPreview == true)) {
         cms.galleries.activeItem.isCropped = isCropped;
         refreshActiveImagePreview();
      }
   }
   
   /**
    * Refreshes the image preview scaling parameters depending on the scale & crop settings
    * and displays the image in the preview area.
    */
   var refreshActiveImagePreview = function() {
      var scaleParams = "";
      if (cms.galleries.initValues.scale != null && cms.galleries.initValues.scale != "") {
         scaleParams = cms.galleries.initValues.scale;
      }
      var imgWidth = cms.galleries.activeItem.width;
      var imgHeight = cms.galleries.activeItem.height;
      var useSelectedDimensions = false;
      if (cms.galleries.activeItem.isCropped == true) {
         if (scaleParams != "") {
            scaleParams += ",";
         }
         scaleParams += cms.galleries.activeItem.crop;
         //imgWidth = getScaleValueInt(activeItem.crop, "cw");
         //imgHeight = getScaleValueInt(activeItem.crop, "ch");
      }
      
      if (cms.galleries.activeItem.newwidth > 0) {
         imgWidth = cms.galleries.activeItem.newwidth;
         useSelectedDimensions = true;
      }
      if (cms.galleries.activeItem.newheight > 0) {
         imgHeight = cms.galleries.activeItem.newheight;
         useSelectedDimensions = true;
      }
      if (cms.galleries.initValues.useformats != true ||
      (cms.galleries.initValues.useformats == true && (formatSelected.width == -1 || formatSelected.height == -1))) {
         setImageFormatFields(imgWidth, imgHeight);
      }
      var maxWidth = defaultPreview['width'];
      var maxHeight = defaultPreview['height'];
      if (cms.galleries.initValues.useformats == true) {
         var formatWidth = formatSelected.width;
         var formatHeight = formatSelected.height;
         if (formatWidth > -1) {
            if (formatWidth < maxWidth) {
               maxWidth = formatWidth;
            }
         }
         if (formatHeight > -1) {
            if (formatHeight < maxHeight) {
               maxHeight = formatHeight;
            }
         }
      }
      var newDimensions = calculateDimensions(imgWidth, imgHeight, maxWidth, maxHeight);
      if (newDimensions.scaleFactor != 1) {
         if (scaleParams != "") {
            scaleParams += ",";
         }
         scaleParams += "w:" + newDimensions.width;
         scaleParams += ",h:" + newDimensions.height;
      } else if (useSelectedDimensions == true) {
         if (scaleParams != "") {
            scaleParams += ",";
         }
         scaleParams += "w:" + imgWidth;
         scaleParams += ",h:" + imgHeight;
      }
      //$('.edit-format-area').find('div[alt="' + keys["previewWidth"] + '"]').find('.cms-item-edit').text(cms.galleries.activeItem.width);
      //$('.edit-format-area').find('div[alt="' + keys["previewHeight"] + '"]').find('.cms-item-edit').text(cms.galleries.activeItem.height);
      //var path = cms.galleries.activeItem.linkpath;
      if (scaleParams != "") {
         scaleParams = "?__scale=" + scaleParams;
      }
      $('.cms-image-preview').empty();
      $('.cms-image-preview').append('<img src="' + cms.galleries.activeItem['linkpath'] + scaleParams + '" />');
      //$("#imgpreview").html("<img src=\"" + path + scaleParams + "\" />");
   }
   
   /**
    * Calculates the image dimensions that can be shown in the preview area.
    *
    * @param {Object} imgWidth the width
    * @param {Object} imgHeight the height
    * @param {Object} maxWidth max width
    * @param {Object} maxHeight max height
    */
   var calculateDimensions = function(imgWidth, imgHeight, maxWidth, maxHeight) {
      var newWidth = imgWidth;
      var newHeight = imgHeight;
      var scaleFactor = 1;
      if (imgWidth > maxWidth || imgHeight > maxHeight) {
         if (imgWidth > maxWidth) {
            newWidth = maxWidth;
            scaleFactor = imgWidth / newWidth;
            newHeight = imgHeight / scaleFactor;
         }
         if (newHeight > maxHeight) {
            var tempHeight = newHeight;
            newHeight = maxHeight;
            scaleFactor = tempHeight / newHeight;
            newWidth = newWidth / scaleFactor;
         }
         scaleFactor = imgHeight / newHeight;
      }
      return new Object({
         "width": Math.round(newWidth),
         "height": Math.round(newHeight),
         "scaleFactor": scaleFactor
      });
   }
   
   /**
    * Fired when the reset size button is clicked or the original format from the drop down is selected.<p>
    */
   var resetSizes = function() {
      cms.galleries.activeItem.newwidth = -1;
      cms.galleries.activeItem.newheight = -1;
      if (cms.galleries.initValues.useformats == true) {
         // using formats, calculate image for currently selected size
         changeFormat();
      } else {
         // not using formats, lock ratio and use original image size
         lockRatio = true;
         //$('#locksizes').attr("title", LANG.IMGPREVIEW_SIZE_UNLOCK);
         //$('#locksizes').attr("class", "btnlocked");
         
         $('.edit-format-area').find('div[alt="' + keys["previewWidth"] + '"]').find('.cms-item-edit').text(cms.galleries.activeItem.width);
         $('.edit-format-area').find('div[alt="' + keys["previewHeight"] + '"]').find('.cms-item-edit').text(cms.galleries.activeItem.height);
         //$("#formatselect").get(0).selectedIndex = 0;
         refreshActiveImagePreview();
      }
      //$("#croplink").hide();
   }
   
   /* Initializes the options and values shown in the format select box. */
   var initFormatSelectBox = function() {
      var formatOptions, formatValues;
      if (cms.galleries.initValues.useformats == true) {
            // using a preselected format, just display information that cannot be changed
      
         //$("#txtWidth").get(0).disabled = true;
         //$("#txtHeight").get(0).disabled = true;
         //formatOptions = unescape(eval('window.opener.imgFmtNames' + initValues.hashid));
         //formatValues = eval('window.opener.imgFmts' + initValues.hashid);
      } else {
         // not using a format, image width and height can be adjusted
         formatOptions = defaultFormatOptions;
         formatValues = defaultFormatValues;
      }
      
      formatSelections = new Array(formatValues.length);
      for (var i = 0; i < formatValues.length; i++) {
         formatSelections[i] = new Object();
         var pos = formatOptions.indexOf("|");
         var currOptStr;
         if (pos != -1) {
            currOptStr = formatOptions.substring(0, pos);
            formatOptions = formatOptions.substring(pos + 1);
         } else {
            currOptStr = formatOptions;
         }
         pos = currOptStr.indexOf(":");
         if (pos != -1) {
            formatSelections[i].optionvalue = currOptStr.substring(0, pos);
            formatSelections[i].optionlabel = currOptStr.substring(pos + 1);
         } else {
            formatSelections[i].optionvalue = currOptStr;
            formatSelections[i].optionlabel = currOptStr;
         }
         pos = formatValues[i].indexOf("x");
         if (pos != -1) {
            formatSelections[i].type = "ocspecial";
            formatSelections[i].width = -1;
            formatSelections[i].height = -1;
            var pixels = formatValues[i].substring(0, pos);
            if (pixels != "?") {
               formatSelections[i].width = parseInt(pixels);
            }
            pixels = formatValues[i].substring(pos + 1);
            if (pixels != "?") {
               formatSelections[i].height = parseInt(pixels);
            }
         } else {
            formatSelections[i].type = formatValues[i];
            formatSelections[i].width = -1;
            formatSelections[i].height = -1;
         }
         var selected = "";
         if (cms.galleries.initValues.useformats == true && cms.galleries.initValues.formatname == formatSelections[i].optionvalue) {
            selected = " selected=\"selected\"";
            formatSelected = formatSelections[i];
         }
         var optionObject = {
            'value': formatSelections[i].optionvalue,
            'title': formatSelections[i].optionlabel
         };
         formatDropDown.push(optionObject);
      }
   }
   
   /* Called if a format is selected in the format select box. */
   var changeFormat = function(selectedIndex) {
      //var selected = $("#formatselect").get(0).selectedIndex;
      formatSelected = formatSelections[selectedIndex];
      
      if (formatSelected.type == "original") {
         // reset to original sizes
         resetSizes();
      } else if (formatSelected.type == "user") {
            // user defined format, nothing to do except remove cropping
         //$("#croplink").show();
      } else if (formatSelected.type == "free") {
            // free cropping;
         //$("#croplink").show();
      } else {
         // other format selected
         //$("#croplink").show();
         if (formatSelected.width != -1) {
            if (formatSelected.height != -1) {
               // we have a width and height, we also have to check the lock ratio
               $('.edit-format-area').find('div[alt="' + keys["previewWidth"] + '"]').find('.cms-item-edit').text(formatSelected.width);
               onSizeChanged("Width", formatSelected.width, false, false);
               var txtHeight = parseInt($('#txtHeight').get(0).value);
               if (txtHeight != formatSelected.height) {
                  setLockRatio(false);
                  $('.edit-format-area').find('div[alt="' + keys["previewHeight"] + '"]').find('.cms-item-edit').text(formatSelected.height)
                  //$('#txtHeight').get(0).value = formatSelected.height;
                  onSizeChanged("Height", formatSelected.height, true, false);
               }
            } else {
               // we have only the width, change it
               if (cms.galleries.initValues.useformats == true) {
                  setLockRatio(true);
               }
               $('.edit-format-area').find('div[alt="' + keys["previewWidth"] + '"]').find('.cms-item-edit').text(formatSelected.width)
               //$('#txtWidth').get(0).value = formatSelected.width;
               onSizeChanged("Width", formatSelected.width, true, false);
            }
         } else {
            if (formatSelected.height != -1) {
               // we only have a height value, change it
               if (cms.galleries.initValues.useformats == true) {
                  setLockRatio(true);
               }
               $('.edit-format-area').find('div[alt="' + keys["previewHeight"] + '"]').find('.cms-item-edit').text(formatSelected.height)
               //$('#txtHeight').get(0).value = formatSelected.height;
               onSizeChanged("Height", formatSelected.height, true, false);
            }
         }
      }
   }
   
   /* Fired when the width or height input texts change. */
   var onSizeChanged = function(dimension, value, refreshImage, refreshSelect) {
      // verifies if the aspect ratio has to be mantained
      if (lockRatio == true) {
         var e = dimension == 'Width' ? $('.edit-format-area').find('div[alt="' + keys["previewHeight"] + '"]').find('.cms-item-edit') : $('.edit-format-area').find('div[alt="' + keys["previewWidth"] + '"]').find('.cms-item-edit');
         
         if (value.length == 0 || isNaN(value)) {
            e.value = "";
            return;
         }
         var imgHeight = cms.galleries.activeItem.height;
         var imgWidth = cms.galleries.activeItem.width;
         
         if (dimension == 'Width') {
            value = value == 0 ? 0 : Math.round(imgHeight * (value / imgWidth));
         } else {
            value = value == 0 ? 0 : Math.round(imgWidth * (value / imgHeight));
         }
         if (!isNaN(value)) 
            e.text(value);
      }
      cms.galleries.activeItem.newwidth = parseInt($('.edit-format-area').find('div[alt="' + keys["previewWidth"] + '"]').find('.cms-item-edit').text());
      cms.galleries.activeItem.newheight = parseInt($('.edit-format-area').find('div[alt="' + keys["previewHeight"] + '"]').find('.cms-item-edit').text());
      if (refreshSelect == null || refreshSelect == true) {
         refreshSelectBox();
      }
      if (refreshImage == null || refreshImage == true) {
         refreshActiveImagePreview();
      }
   }
   
   /**
    * Enables or disables the lock ratio.<p>
    *
    * @param {Object} newRatio
    */
   var setLockRatio = function(/**Boolean*/newRatio) {
      lockRatio = newRatio;
      if (lockRatio == true) {
         var displayedWidth = $('.edit-format-area').find('div[alt="' + keys["previewWidth"] + '"]').find('.cms-item-edit').text();
         var displayedHeight = $('.edit-format-area').find('div[alt="' + keys["previewHeight"] + '"]').find('.cms-item-edit').text();
         if (displayedWidth.length > 0) {
         
            onSizeChanged('Width', displayedWidth);
         } else {
            onSizeChanged('Height', displayedHeight);
         }
         //$('#locksizes').attr("title", LANG.IMGPREVIEW_SIZE_UNLOCK);
         //$('#locksizes').attr("class", "btnlocked");
      } else {
            //$('#locksizes').attr("title", LANG.IMGPREVIEW_SIZE_LOCK);
         //$('#locksizes').attr("class", "btnunlocked");
      }
   }
   
   /* Refreshes the selected option of the format select box. */
   var refreshSelectBox = function(forceCalculateSelection) {
      var selectedIndex = -1;
      if (cms.galleries.initValues.useformats != true || forceCalculateSelection == true) {
         for (var i = 0; i < formatSelections.length; i++) {
            // check if the values match a format selection
            var currSelect = formatSelections[i];
            if (currSelect.type == "user") {
               selectedIndex = i;
               //$("#croplink").show();
            }
            if (currSelect.type == "free" && cms.galleries.activeItem.isCropped == true) {
               selectedIndex = i;
               //$("#croplink").show();
            } else if (currSelect.type == "original" &&
            (cms.galleries.activeItem.newwidth == 0 || cms.galleries.activeItem.newwidth == cms.galleries.activeItem.width) &&
            (cms.galleries.activeItem.newheight == 0 || cms.galleries.activeItem.newheight == cms.galleries.activeItem.height)) {
               selectedIndex = i;
               //$("#croplink").hide();
               break;
            } else if (currSelect.width == cms.galleries.activeItem.newwidth &&
            (currSelect.height == -1 || currSelect.height == cms.galleries.activeItem.newheight)) {
               selectedIndex = i;
               //$("#croplink").show();
               break;
            } else if (currSelect.height == cms.galleries.activeItem.newheight &&
            (currSelect.width == -1 || currSelect.width == cms.galleries.activeItem.newwidth)) {
               selectedIndex = i;
               //$("#croplink").show();
               break;
            }
         }
      }
      if (selectedIndex != -1) {
         if (formatSelections[selectedIndex].type == "user" && formatSelected != null && formatSelected.type == "free") {
            return;
         }
         formatSelected = formatSelections[selectedIndex];
         $('.edit-format-area').find('.cms-selectbox').selectBox('setValue', formatDropDown[selectedIndex]['value']);
      }
   }
   
   
   /**
    * Sets the width and height format fields of the preview image.
    * @param {Object} imgWidth image width
    * @param {Object} imgHeight image height
    */
   var setImageFormatFields = function(/**int*/imgWidth,/**int*/ imgHeight) {
      $('.edit-format-area').find('div[alt="' + keys["previewWidth"] + '"]').find('.cms-item-edit').text(imgWidth);
      $('.edit-format-area').find('div[alt="' + keys["previewHeight"] + '"]').find('.cms-item-edit').text(imgHeight);
   }
   
   /**
    * OK Button was pressed, stores the image information back in the editor fields.
    *
    * @param {Object} itemId the unique path to the resource
    * @param {Object} fieldId the id of the input field in the xml content
    */
   var setImagePath = function(/**String*/itemId, /**String*/ fieldId) {
      if (cms.galleries.initValues.widgetmode == "simple") {
         // simple image gallery widget
         if (fieldId != null && fieldId != "") {
            var imgField = window.opener.document.getElementById(fieldId);
            var imagePath = itemId;
            if (cms.galleries.activeItem.isCropped) {
               var newScale = "";
               if (cms.galleries.initValues.scale != null && cms.galleries.initValues.scale != "") {
                  newScale += ",";
               }
               newScale += "cx:" + cms.galleries.activeItem.cropx;
               newScale += ",cy:" + cms.galleries.activeItem.cropy;
               newScale += ",cw:" + cms.galleries.activeItem.cropw;
               newScale += ",ch:" + cms.galleries.activeItem.croph;
               
               cms.galleries.initValues.scale += newScale;
               
            } // remove cropping parameter
 else if (getScaleValue(cms.galleries.initValues.scale, "cx") != "") {
               cms.galleries.initValues.scale = removeScaleValue(cms.galleries.initValues.scale, "cx");
               cms.galleries.initValues.scale = removeScaleValue(cms.galleries.initValues.scale, "cy");
               cms.galleries.initValues.scale = removeScaleValue(cms.galleries.initValues.scale, "cw");
               cms.galleries.initValues.scale = removeScaleValue(cms.galleries.initValues.scale, "ch");
               
            }
            cms.galleries.initValues.scale = removeScaleValue(cms.galleries.initValues.scale, "w");
            cms.galleries.initValues.scale = removeScaleValue(cms.galleries.initValues.scale, "h");
            
            var newScale = "";
            var sizeChanged = false;
            // comma to separate the content
            if (cms.galleries.initValues.scale != null && cms.galleries.initValues.scale != "") {
               newScale += ",";
            }
            if (cms.galleries.activeItem.newwidth > 0 && cms.galleries.activeItem.width != cms.galleries.activeItem.newwidth) {
               sizeChanged = true;
               newScale += "w:" + cms.galleries.activeItem.newwidth;
            }
            if (cms.galleries.activeItem.newheight > 0 && cms.galleries.activeItem.height != cms.galleries.activeItem.newheight) {
               if (sizeChanged == true) {
                  newScale += ",";
               }
               sizeChanged = true;
               newScale += "h:" + cms.galleries.activeItem.newheight;
            }
            if (newScale.length > 1) {
               cms.galleries.initValues.scale += newScale;
            }
            
            if (cms.galleries.initValues.scale != null && cms.galleries.initValues.scale != "") {
               imagePath += "?__scale=";
               imagePath += cms.galleries.initValues.scale;
            }
            
            // write the path with request parameters to the input field
            imgField.value = imagePath;
            
         }
      } else {
            // widget mode: VFS image widget
         /*if (initValues.editedresource != null && initValues.editedresource != "") {
          var imgField = window.opener.document.getElementById("img." + initValues.fieldid);
          imgField.value = activeItem.sitepath;
          if (activeItem.isCropped) {
          var newScale = "";
          if (initValues.scale != null && initValues.scale != "" && initValues.scale.charAt(initValues.scale.length - 1) != ",") {
          newScale += ",";
          }
          newScale += "cx:" + activeItem.cropx;
          newScale += ",cy:" + activeItem.cropy;
          newScale += ",cw:" + activeItem.cropw;
          newScale += ",ch:" + activeItem.croph;
          initValues.scale += newScale;
          } else if (getScaleValue(initValues.scale, "cx") != "") {
          initValues.scale = removeScaleValue(initValues.scale, "cx");
          initValues.scale = removeScaleValue(initValues.scale, "cy");
          initValues.scale = removeScaleValue(initValues.scale, "cw");
          initValues.scale = removeScaleValue(initValues.scale, "ch");
          }
          if (initValues.useformats == true) {
          var formatBox = window.opener.document.getElementById("format." + initValues.fieldid);
          if (formatBox.selectedIndex != $("#formatselect").get(0).selectedIndex) {
          formatBox.selectedIndex = $("#formatselect").get(0).selectedIndex;
          window.opener.setImageFormat(initValues.fieldid, "imgFmts" + initValues.hashid);
          }
          }
          initValues.scale = removeScaleValue(initValues.scale, "w");
          initValues.scale = removeScaleValue(initValues.scale, "h");
          if (initValues.useformats != true || activeItem.isCropped) {
          var newScale = "";
          var sizeChanged = false;
          if (initValues.scale != null && initValues.scale != "" && initValues.scale.charAt(initValues.scale.length - 1) != ",") {
          newScale += ",";
          }
          if (activeItem.newwidth > 0 && activeItem.width != activeItem.newwidth) {
          sizeChanged = true;
          newScale += "w:" + activeItem.newwidth;
          }
          if (activeItem.newheight > 0 && activeItem.height != activeItem.newheight) {
          if (sizeChanged == true) {
          newScale += ",";
          }
          sizeChanged = true;
          newScale += "h:" + activeItem.newheight;
          }
          initValues.scale += newScale;
          }
          
          var scaleField = window.opener.document.getElementById("scale." + initValues.fieldid);
          scaleField.value = initValues.scale;
          var ratioField = window.opener.document.getElementById("imgrat." + initValues.fieldid);
          ratioField.value = activeItem.width / activeItem.height;
          }*/
      }
      try {
         // toggle preview icon if possible
         if (cms.galleries.initValues.widgetmode == "simple") {
            window.opener.checkPreview(cms.galleries.initValues.fieldid);
         } else {
            window.opener.checkVfsImagePreview(cms.galleries.initValues.fieldid);
         }
      } catch (e) {
            }
      window.close();
   }
   
   /**
    * Returns the integer value of the specified scale parameter.
    *
    * @param {Object} scale the given scale value
    * @param {Object} valueName the scale parameter as used for __scale, e.g. w,h,cw etc.
    */
   function getScaleValueInt(scale, valueName) {
      try {
         return parseInt(getScaleValue(scale, valueName));
      } catch (e) {
         return 0;
      }
   }
   
   /**
    * Returns the value of the specified scale parameter.
    *
    * @param {Object} scale the given scale value
    * @param {Object} valueName the scale parameter as used for __scale, e.g. w,h,cw etc.
    */
   function getScaleValue(scale, valueName) {
      if (scale == null) {
         return "";
      }
      var pos = scale.indexOf(valueName + ":");
      if (pos != -1) {
         // found value, return it
         if (pos > 0 && (valueName == "h" || valueName == "w")) {
            // special handling for "w" and "h", could also match "cw" and "ch"
            if (scale.charAt(pos - 1) == "c") {
               scale = scale.substring(pos + 1);
            }
         }
         var searchVal = new RegExp(valueName + ":\\d+,*", "");
         var result = scale.match(searchVal);
         if (result != null && result != "") {
            result = result.toString().substring(valueName.length + 1);
            if (result.indexOf(",") != -1) {
               result = result.substring(0, result.indexOf(","));
            }
            return result;
         }
      }
      return "";
   }
   
   /**
    * Removes the specified scale parameter value.
    *
    * @param {Object} scale the given scale value
    * @param {Object} valueName the scale parameter as used for __scale, e.g. w,h,cw etc.
    */
   function removeScaleValue(/**String*/scale, /**String*/ valueName) {
      if (scale == null) {
         return null;
      }
      var pos = scale.indexOf(valueName + ":");
      if (pos != -1) {
         // found value, remove it from scale string
         var scalePrefix = "";
         if (pos > 0 && (valueName == "h" || valueName == "w")) {
            // special handling for "w" and "h", could also match "cw" and "ch"
            if (scale.charAt(pos - 1) == "c") {
               scalePrefix = scale.substring(0, pos + 1);
               scale = scale.substring(pos + 1);
            }
         }
         if (scale.indexOf(valueName + ":") != -1) {
            var searchVal = new RegExp(valueName + ":\\d+,*", "");
            scale = scale.replace(searchVal, "");
         }
         scale = scalePrefix + scale;
      }
      return scale;
   }
   
   
   ///// Image Content Handler ////////////////              
   /**
    * Image handler to display the preview for images (resource id = 3).
    */
   var imageContentTypeHandler = cms.imagepreviewhandler.imageContentTypeHandler = {
      'type': 'image',
      'init': initFormatSelectBox,
      'openPreview': showItemPreview,
      'setValues': {
         'widget': setImagePath,
         'editor': 'test2'
      }
   };
   
   cms.galleries.addContentTypeHandler(cms.imagepreviewhandler.imageContentTypeHandler['type'], cms.imagepreviewhandler.imageContentTypeHandler);
   
})(cms);
