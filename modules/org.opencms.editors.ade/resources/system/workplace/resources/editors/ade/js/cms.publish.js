(function(cms) {
   var checkboxCount = 0;
   
   var publishDialogId = cms.publish.publishDialogId = 'publishlist';
   
   var M = cms.messages;
   var STATE_CHANGED = 'C';
   var STATE_DELETED = 'D';
   var STATE_NEW = 'N';
   var WAIT_GIF_URL = cms.data.SKIN_URI + 'editors/ade/css/images/loading.gif';
   var classPublishItem = 'cms-publish-item';
   var classPublishRow = 'cms-publish-row';
   var classPublishCheckbox = 'cms-publish-checkbox';
   var classRemoveButton = 'cms-publish-remove-button';
   var classToRemove = 'cms-publish-toremove';
   var classKeep = 'cms-publish-keep';
   var classPublishDialog = 'cms-publish-dialog';
   var buttonHeight = 24;
   
   /**
    * Initializes the project list if it isn't already initialized and then calls a callback
    * @param {Object} callback the callback to be called after the project list initialization, or instantly if the project list has already been initialized
    */
   var initProjects = cms.publish.initProjects = function(callback) {
      if (!cms.publish.projects) {
         getProjects(function(ok, data) {
            if (!ok) {
               cms.publish.projects = [];
               callback();
            } else {
               cms.publish.projects = data.projects;
               callback();
            }
         })
      } else {
         callback();
      }
   }
   
   /**
    * Default button creation function for the publish dialog
    * @param {Object} label the label of the button
    */
   var dialogButton = function(label) {
      return $('<button/>').text(label).css('min-width', '80px').height(buttonHeight).addClass('ui-state-default ui-corner-all');
   }
   
   
   /**  
    * The url for server requests.
    * @see /system/workplace/editors/ade/include.txt
    */
   var /** String */ SERVER_URL = cms.publish.SERVER_URL;
   
   /**
    * Returns the icon for a resource
    * @param {Object} item the resource item
    */
   var _getItemIcon = function(item) {
      var resourceIcon = item.icon;
      return $('<img  class="cms-publish-icon"></img>').attr('src', resourceIcon);
   }
   
   var problemIconClasses = {
      locked: 'cms-icon-locked',
      published: 'cms-icon-published',
      permissions: 'cms-icon-permission',
      brokenlink: 'cms-icon-brokenlink',
      related: 'cms-icon-relation'
   }
   
   var problemClasses = {
      locked: 'cms-publish-problem-locked',
      published: 'cms-publish-problem-published',
      permissions: 'cms-publish-problem-permission'
   }
   
   
   /**
    * Checks all publish checkboxes inside a DOM element
    * @param {Object} $parent
    */
   var _checkAllCheckboxes = function($parent) {
      var u = cms.util;
      $.each(u.Checkbox.getCheckboxes($parent), u.bindFn(u.Checkbox.prototype.setCheckedIfEnabled, [true]));
   }
   
   /**
    * Unchecks all publish checkboxes inside a DOM element
    * @param {Object} $parent
    */
   var _uncheckAllCheckboxes = function($parent) {
      $('.' + classPublishCheckbox, $parent).each(function() {
         this.checked = false;
      })
   }
   
   /**
    * Collects the rel attributes (which represent resource ids) from the DOM elements in a jQuery object
    * @param {Object} $items the jQuery object
    */
   var _collectIds = function($items) {
      var result = [];
      $items.each(function() {
         result.push($(this).attr('rel'));
      });
      return result;
   }
   
   $(function() {
      $('.' + classRemoveButton).liveData('click', 'click');
      $('.' + classPublishRow).live('mouseover', function() {
         $(this).find('.' + classRemoveButton).show();
      });
      $('.' + classPublishRow).live('mouseout', function() {
         $(this).find('.' + classRemoveButton).hide();
      });
      $('.' + classRemoveButton).live('click', function() {
         var $row = $(this).closest('.'+classPublishRow);
         var $removeButton = $(this);
         var checkbox = cms.util.Checkbox.getCheckboxes($row)[0];
         var isRemove = $removeButton.hasClass('cms-icon-publish-remove');
         $row.chooseClass(isRemove, classToRemove, classKeep);
         $removeButton.chooseClass(isRemove, 'cms-icon-publish-unremove', 'cms-icon-publish-remove');
         if (isRemove) {
            checkbox.setEnabled(false);
            checkbox.setChecked(false);
            $removeButton.attr('title', 'Unremove');
         } else {
            $removeButton.attr('title', 'Remove')
            if (!$row.hasClass('cms-has-info')) {
               checkbox.setEnabled(true);
            }
         }
      });
   });
   
   var PublishDialog = cms.publish.PublishDialog = function(project) {
      this.project = project ? project : '';
      this.checkboxes = [];
   }
   
   
   
   PublishDialog.prototype = {
      setData: function(data) {
            },
      goToLinkCheckState: function(data) {
            },
      
      /**
       * Starts loading the publish list.
       *
       * The publish list dialog will be opened asynchronously when the publish list is loaded.
       */
      start: function() {
         $('button[name=publish]').addClass('cms-deactivated');
         var self = this;
         self.getDialog().empty();
         self.goToWaitState();
         getPublishOptions(function(ok, data) {
            self.checkedRelated = !!data.related;
            self.checkedSiblings = !!data.siblings;
            self.project = data.project ? data.project : '';
            if (ok) {
               getPublishList(self.checkedRelated, self.checkedSiblings, self.project, function(ok, data) {
               
                  if (ok) {
                     self.goToMainState(data.groups);
                  } else {
                     self.destroy();
                  }
               });
            }
         });
      },
      
      /**
       * Returns the dialog, and creates it if it doesn't already exist
       */
      getDialog: function() {
         var self = this;
         if (!this.$dialog) {
            if ($('#' + classPublishDialog).size() == 0) {
               $('<div id="' + classPublishDialog + '"></div>').appendTo('body');
            }
            var $dlg = $('#' + classPublishDialog);
            this.$dialog = $dlg;
            $dlg.dialog({
               autoOpen: true,
               title: 'Publish',
               modal: true,
               zIndex: 9999,
               width: 750,
               close: function() {
                  self.destroy();
               },
               position: 'top',
               resizable: true
            });
         }
         return this.$dialog;
      },
      
      destroy: function() {
         var self = this;
         self.$dialog.dialog('destroy');
         if (self.finish) {
            self.finish();
         }
         $('button[name=publish]').removeClass('cms-deactivated');
      },
      
      /**
       * Changes the publish list parameters and loads the new publish list for the changed parameters.
       * @param {Object} related flag indicating whether related resources should be included
       * @param {Object} siblings flag indicating whether siblings should be included
       * @param {Object} project the project UUID for which the publish list should be retrieved, or the empty string
       * for the list of the current user's changed resources
       */
      updateData: function(related, siblings, project) {
         var self = this;
         self.goToWaitState();
         getPublishList(related, siblings, project, function(ok, data) {
            if (ok) {
               self.goToMainState(data.groups);
            }
         })
         self.checkedRelated = related;
         self.checkedSiblings = siblings;
         self.project = project;
      },
      
      
      /**
       * Enters the main (publish list) state of the dialog.
       * @param {Object} data the data to use for the publish dialog
       */
      goToMainState: function(data) {
      
         var self = this;
         
         var util = cms.util;
         self.checkboxes = [];
         self.problemCount = 0;
         var $dlg = this.getDialog();
         
         $dlg.empty();
         if (data.length == 0) {
            var $projectSelector = self.createProjectSelector().css('float', 'right');
            $('<div></div>').text('There are no resources to publish.').append($projectSelector).appendTo($dlg);
            dialogButton('OK').css('clear', 'both').css('margin-top', '75px').css('float', 'right').click(function() {
               self.destroy();
            }).appendTo($dlg);
            return;
         }
         
         this.$topPanel = $('<div></div>').appendTo($dlg);
         this.$topPanel.css('margin-bottom', '30px');
         $('<span></span>').text('Select: ').appendTo(self.$topPanel);
         var $selectAll = dialogButton('All').appendTo(self.$topPanel).click(function() {
            $.each(util.Checkbox.getCheckboxes(self.$mainPanel), util.bindFn(util.Checkbox.prototype.setChecked, [true]));
         });
         var $selectNone = dialogButton('None').appendTo(self.$topPanel).click(function() {
            $.each(util.Checkbox.getCheckboxes(self.$mainPanel), util.bindFn(util.Checkbox.prototype.setChecked, [false]));
         });
         var $projectSelector = self.createProjectSelector().css('float', 'right').appendTo(self.$topPanel);
         var $projectSelectLabel = $('<span></span>').text('Publish list: ').css({
            'float': 'right',
            'margin-right': '5px',
            'margin-top': '3px'
         }).appendTo(self.$topPanel);
         if ($.browser.msie) {
            $projectSelector.css('margin-top', '-20px');
            $projectSelectLabel.css('margin-top', '-20px');
         }
         var $scrollPanel = $('<div/>').addClass('cms-list-scrolling ui-corner-all cms-publish-scrolling').css('position', 'relative').appendTo($dlg);
         this.$mainPanel = $('<div></div>').addClass('cms-publish-main').appendTo($scrollPanel);
         this.$checkboxes = $('<div></div>').appendTo($dlg);
         var relatedCheckbox = new cms.util.Checkbox();
         relatedCheckbox.$dom.css('float', 'left');
         $('<div/>').css('clear', 'both').append(relatedCheckbox.$dom).append($('<span/>').text('Publish related resources')).appendTo(this.$checkboxes);
         relatedCheckbox.setChecked(!!self.checkedRelated);
         
         var siblingsCheckbox = new cms.util.Checkbox();
         siblingsCheckbox.$dom.css('float', 'left');
         $('<div/>').css('clear', 'both').append(siblingsCheckbox.$dom).append($('<span/>').text('Publish siblings')).appendTo(this.$checkboxes);
         siblingsCheckbox.setChecked(!!self.checkedSiblings);
         
         var _updateState = function() {
            self.saveState();
            self.updateData(relatedCheckbox.getChecked(), siblingsCheckbox.getChecked(), self.project);
         }
         
         // since the checkbox click handlers are live events, just binding _updateState as a click
         // handler would not work because it would be executed before the live event was triggered.
         relatedCheckbox.$dom.click(cms.util.defer(_updateState));
         siblingsCheckbox.$dom.click(cms.util.defer(_updateState));
         
         
         this.$bottomPanel = $('<div></div>').appendTo($dlg).css({
            'margin-top': '20px',
            'margin-left': '20px'
         });
         var $cancel = dialogButton('Cancel').css('float', 'right').appendTo(self.$bottomPanel).click(function() {
            self.destroy();
         });
         var $publish = dialogButton('Publish').css('float', 'right').appendTo(self.$bottomPanel).click(function() {
            self.resourcesToPublish = self.getResourcesToPublish();
            self.resourcesToRemove = self.getResourcesToRemove();
            self.startPublish();
         });
         
         
         for (var i = 0; i < data.length; i++) {
            this.addGroup(data[i]);
         }
         if (self.problemCount > 0) {
            var $problemDiv = $('<div/>').text(cms.util.format('There are {0} resources with problems in the publish list.', "" + self.problemCount)).css('margin-bottom', '10px').insertAfter($scrollPanel);
            $('<span class="cms-publish-warning/>').css('float', 'left').css('margin-right', '10px').prependTo($problemDiv);
         }
         self.restoreState();
      },
      
      /**
       * Adds a group of resources to publish to the publish list in the dialog.
       * @param {Object} group an object with a name property (string) and a resources property (array of resources)
       */
      addGroup: function(group) {
         var $main = this.$mainPanel;
         var $group = $('<ul/>').appendTo($main);
         var $selectAll = $('<button/>').addClass('cms-publish-select-button ui-state-default ui-corner-all').text('All');
         var $selectNone = $('<button/>').addClass('cms-publish-select-button ui-state-default ui-corner-all').text('None');
         if ($.browser.msie) {
            $selectAll.css('margin-top', '-22px');
            $selectNone.css('margin-top', '-22px');
         }
         var _setAllChecked = function(checked) {
            var checkboxes = cms.util.Checkbox.getCheckboxes($group);
            $.each(checkboxes, cms.util.bindFn(cms.util.Checkbox.prototype.setCheckedIfEnabled, [checked]));
         };
         $selectAll.click(cms.util.bindFn(_setAllChecked, [true]));
         $selectNone.click(cms.util.bindFn(_setAllChecked, [false]));
         $('<p/>').addClass('cms-publish-group-header').text(group.name).append($selectNone).append($selectAll).insertBefore($group);
         for (var i = 0; i < group.resources.length; i++) {
            this.addResource(group.resources[i], false, false, $group);
         }
      },
      
      /**
       * Adds a resource to the publish list in the dialog.
       * @param {Object} resource the resource to be added
       * @param {Boolean} isRelated a flag that indicates whether the resource is merely a related resource of publish list resources
       * @param {Boolean} isLinkCheck if true, the item will be shown in link check mode, i.e. it will lack the buttons from the normal publish list
       * @param {Object} $parent the parent DOM object to which the resource entry should be appended
       */
      addResource: function(resource, isRelated, isLinkCheck, $parent) {
         var self = this;
         var $row = $(resource.itemhtml).appendTo($parent);
         $row.addClass(classKeep).addClass(classPublishRow).addClass('cms-list-with-checkbox');
         $row.attr('rel', resource.id);
         if (isRelated) {
            $row.css('margin-left', '60px');
         }
         if (!resource.title) {
            $row.find('.cms-list-title').append($('<span/>').text('[no title]'));
         }
         if (resource.info) {
            $('<span></span>').addClass('cms-publish-warning').css({
               'position': 'absolute',
               'top': '8px',
               'right': '40px',
               'float': 'right',
               'z-index': '1'
            }).attr('title', resource.info).appendTo($row.find('.cms-list-itemcontent'));
            self.problemCount++;
         }
         
         if (!isLinkCheck) {
            var checkboxStyle = {
               'visibility': 'hidden',
               'margin-left': '8px',
               'margin-top': '8px',
               'float': 'left'
            };
            var checkboxId = 'pub' + (checkboxCount++);
            var checkbox = new cms.util.Checkbox($('<span/>'));
            checkbox.$dom.css({
               'float': 'left',
               'margin-top': '6px'
            });
            checkbox.resourceId = resource.id;
            self.checkboxes.push(checkbox);
            if (!resource.info) {
               checkbox.$dom.prependTo($row);
            }
            var $removeButton = $('<span/>').addClass(classRemoveButton).attr('rel', resource.id);
            $removeButton.css('display', 'none');
            
            $removeButton.addClass('cms-icon-publish-remove');
            
            var removeButtonState = 0;
            $removeButton.attr('title', 'Remove');
            if (resource.info) {
               $row.addClass('cms-has-info');
            }
            if (resource.removable && !self.project) {
               $row.find('.cms-list-itemcontent').prepend($removeButton);
            }
         }
         if (resource.related) {
            var related = resource.related;
            for (var i = 0; i < resource.related.length; i++) {
               var subResource = related[i];
               self.addResource(subResource, true, isLinkCheck, $parent);
            }
         }
      },
      
      /**
       * Starts publishing the checked items from the publish list.
       * @param {Object} force
       */
      startPublish: function(force) {
         var self = this;
         self.goToWaitState();
         publishResources(self.resourcesToPublish, self.resourcesToRemove, force, function(ok, data) {
            if (!ok) {
               //cms.util.dialogAlert(data.error, "Error");
               self.start();
               return;
            }
            if (data.resources) {
               self.goToLinkCheckState(data);
            } else {
               self.destroy();
            }
         });
      },
      
      /**
       * Creates the project selector for the publish dialog.
       */
      createProjectSelector: function() {
         var self = this;
         var projects = cms.publish.projects;
         var userListId = '';
         var userListLabel = 'My changes';
         var values = [];
         values.push({
            value: userListId,
            title: userListLabel
         });
         for (var i = 0; i < projects.length; i++) {
            values.push({
               value: projects[i].id,
               title: projects[i].name
            });
         }
         var $select = $.fn.selectBox('generate', {
            'values': values,
            select: function($this, replacer, value) {
               self.updateData(self.checkedRelated, self.checkedSiblings, value);
            }
         });
         $select.selectBox('setValue', self.project);
         return $select;
      },
      
      /**
       * Sets the publish checkbox of the publish list item with a given rel attribute to unchecked.
       * @param {Object} key the rel attribute value of the checkbox
       */
      uncheckByRel: function(key) {
         var self = this;
         var checkboxes = cms.util.Checkbox.getCheckboxes(self.$mainPanel);
         $.each(checkboxes, function() {
            this.setChecked(false);
            
         })
         
         $('.' + classPublishCheckbox + '[rel=' + key + ']', self.$mainPanel).each(function() {
            if (this.checked) {
               $(this).trigger('click');
            }
         });
      },
      
      /**
       * Sets the status of a publish item with a given rel attribute to 'remove'.
       * @param {Object} key the rel attribute value of the publish list item
       */
      removeByRel: function(key) {
         var self = this;
         $('.' + classRemoveButton + '[rel=' + key + ']').trigger('click');
      },
      
      /**
       * Saves the state of the publish checkboxes and the 'remove' statuses from the publish dialog.
       */
      saveState: function() {
         var self = this;
         var checkboxes = cms.util.Checkbox.getUncheckedCheckboxes(self.$mainPanel);
         var unchecked = $.map(checkboxes, function(checkbox) {
            return checkbox.resourceId;
         });
         var toRemove = _collectIds($('.' + classPublishRow + '.' + classToRemove, self.$mainPanel));
         self.selectState = {
            unchecked: unchecked,
            toRemove: toRemove
         };
      },
      
      
      /**
       * Sets the state of the publish checkboxes and 'remove' statuses of the dialog to the state they were in
       * when the saveState method was called.
       */
      restoreState: function() {
         var self = this;
         _checkAllCheckboxes(self.$mainPanel);
         
         if (self.selectState) {
            var unchecked = cms.util.stringArrayToObject(self.selectState.unchecked);
            var toRemove = self.selectState.toRemove;
            cms.util.Checkbox.getCheckboxes()
            
            $.each(cms.util.Checkbox.getCheckboxes(self.$mainPanel), function() {
               if (unchecked[this.resourceId]) {
                  this.setChecked(false);
               }
            });
            
            for (var j = 0; j < toRemove.length; j++) {
               self.removeByRel(toRemove[j]);
            }
         }
      },
      
      /**
       * Gets the ids for resources that should be published from the publish dialog.
       */
      getResourcesToPublish: function() {
         var $main = this.$mainPanel;
         var self = this;
         var checkboxes = cms.util.Checkbox.getCheckboxes($main);
         var getResourceIdIfChecked = function(checkboxObj) {
            if (checkboxObj.getChecked()) {
               return checkboxObj.resourceId;
            } else {
               return [];
            }
         }
         return $.map(cms.util.Checkbox.getCheckboxes(self.$mainPanel), getResourceIdIfChecked);
      },
      
      /**
       * Gets the resource ids for resources that should be removed from the publish list from the publish dialog.
       */
      getResourcesToRemove: function() {
         var $main = this.$mainPanel;
         var $toRemove = $('.' + classToRemove + ' ,  .' + problemClasses.published, $main);
         var resourcesToRemove = [];
         $toRemove.each(function() {
            resourcesToRemove.push($(this).attr('rel'));
         });
         return resourcesToRemove;
      },
      
      /**
       * Changes the publish dialog to the wait state.
       */
      goToWaitState: function() {
         var self = this;
         var $dialog = this.getDialog();
         if (self.$mainPanel) {
            self.$mainPanel.css('visibility', 'hidden');
         }
         var w = $dialog.width();
         var h = $dialog.height();
         var $overlay = $('<div/>').width('100%').height('100%').css({
            'position': 'absolute',
            'background-color': '#000000',
            'top': '0px',
            'left': '0px',
            'z-index': 2,
            'background-color': 'transparent'
         }).prependTo($dialog);
         var self = this;
         //$dialog.children().css('visibility', 'hidden');
         $('<img></img>').attr('src', WAIT_GIF_URL).css({
            'position': 'absolute',
            'left': w / 2 + 'px',
            'top': h / 2 + 'px'
         }).appendTo($dialog);
      },
      
      /**
       * Enters the link check state of
       * @param {Object} data
       */
      goToLinkCheckState: function(data) {
         var resources = data.resources;
         var self = this;
         var $dlg = this.getDialog();
         $dlg.empty();
         $('<div></div>').text('The following links will be broken:').appendTo($dlg);
         var $scrollPanel = $('<div/>').addClass('cms-publish-scrolling').css('position', 'relative').appendTo($dlg);
         var $linkCheckPanel = self.$linkCheckPanel = $('<ul/>').appendTo($scrollPanel);
         
         var $linkCheckButtons = $('<div></div>').css('clear', 'both').appendTo($dlg);
         var $backButton = dialogButton('Back').css('float', 'right').appendTo($linkCheckButtons);
         var $cancelButton = dialogButton('Cancel').css('float', 'right').appendTo($linkCheckButtons);
         var $forceButton = dialogButton('Publish').css('float', 'right');
         if (data.canPublish) {
            $forceButton.appendTo($linkCheckButtons);
         }
         $backButton.click(function() {
            self.updateData(self.checkedRelated, self.checkedSiblings, self.project);
         });
         $cancelButton.click(function() {
            self.destroy();
         });
         $forceButton.click(function() {
            self.startPublish(true);
         });
         
         $.each(resources, function() {
            self.addResource(this, false, true, $linkCheckPanel);
         });
      }
   }
   
   /**
    * AJAX call for getting the publish list from the server
    */
   var getPublishList = function(related, siblings, project, callback) {
   
      var params = {
         related: related,
         siblings: siblings
      }
      if ((project != null) && (project != '')) {
         params.project = project;
      }
      postJSON('publish_list', params, callback, false, 240000)
   }
   
   /**
    * AJAX call for publishing resources.
    */
   var publishResources = function(resources, removeResources, force, callback) {
   
      var params = {
         'resources': resources,
         'remove-resources': removeResources,
         'force': force
      }
      postJSON('publish', params, callback, false, 240000);
   }
   
   var getProjects = function(callback) {
      postJSON('projects', {}, callback);
   }
   
   var getPublishOptions = function(callback) {
      postJSON('publish_options', {}, callback);
   }
   
   /**
    * Generic function for posting JSON data to the server.
    *
    * @param {String} action a string to tell the server what to do with the data
    * @param {Object} data the JSON data
    * @param {Function} afterPost the callback that should be called after the server replied
    * @param {boolean} async optional flag to indicate is the request should synchronized or not, by default it is not
    * @param {int} timeout optional timeout in millisecs, default is #AJAX_TIMEOUT
    */
   var postJSON = /** void */ function(/** String */action, /** Object */ data, /** void Function(boolean, Object) */ afterPost, /** boolean */ sync, /** int */ timeout) {
   
      cms.comm.postJSON(SERVER_URL, {
         'action': action,
         'data': JSON.stringify(data)
      }, afterPost, sync, timeout);
   }
   
})(cms);
