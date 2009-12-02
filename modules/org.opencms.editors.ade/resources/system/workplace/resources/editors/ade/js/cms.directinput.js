(function($) {
   /**
    * The OpenCms direct-input jQuery-plugin.
    *
    * Use following options:
    *
    * live: boolean (default=false)
    *     assigns the plugin via jQuery.live to all elements matching the selector
    * marginHack: boolean (default=false)
    *     if true a hack is used to avoid the IE7 margin-bug for input elements
    * inputClass: String (default='cms-direct-input')
    *     the css-class used for the input elements
    * setValue: function(element, input) (default=null)
    *     if set the given function will be executed when the user enters a new value.
    *     this will override the default _setValue function.
    * valueChanged: function() (default=null)
    *     If set, this function will be called after setValue.
    * onChange: function(element, input) (default=null)
    *     If set, this function will be called when the value changes on key-press 
    * onReset: function(element) (default=null)
    *     If set, this function will be called, when the value is reset by pressing 'ESC'
    *
    * @param {Object} options the options
    */
   $.fn.directInput = function(options) {
   
      var self = this;
      var opts = $.extend({}, $.fn.directInput.defaults, options);
      _init();
      return self;
      
      /** Initializes the plugin assigning the click-handler. */
      function _init() {
         if (opts.live) {
            self.live('click', _click);
         } else {
            self.click(_click);
         }
      };
      
      /** The click-handler. */
      function _click() {
         var elem = $(this);
         var previousValue = $.isFunction(opts.readValue) ? opts.readValue(elem) : elem.text();
         var input = $('<input name="directInput" type="text" class="' + opts.inputClass + '" value="' + previousValue + '" />');
         _copyCss(elem, input);
         input.insertBefore(elem);
         
         if (opts.marginHack && $.browser.msie && $.browser.version <= 7.0) {
            // HACK: to avoid the margin-left bug for input-elements in IE7 
            // (the problem occurs within elements where hasLayout: true and will introduce an 
            // inherited margin-left for the input-element)
            var parentMargin = parseInt(elem.parent().css('margin-left'));
            if (!isNaN(parentMargin)) {
            
               inputMargin = parseInt(input.css('margin-left'));
               if (isNaN(inputMargin)) {
                  inputMargin = -parentMargin;
                  
               } else {
                  inputMargin = inputMargin - parentMargin;
               }
               input.css('margin-left', inputMargin + 'px');
            }
         }
         
         elem.css('display', 'none');
         
         // setting the focus and selecting text
         input.get(0).focus();
         input.get(0).select();
         var valueChanged = false;
         // set the value on 'return' or 'enter'
         input.keypress(function(e) {
            if (e.which == 13) {
               if ($.isFunction(opts.setValue)) {
                  opts.setValue(elem, input);
               } else {
                  _setValue(elem, input);
               }
               opts.valueChanged();
            } else if (e.keyCode == 27) {
               elem.css('display', '');
               input.remove();
               if ($.isFunction(opts.onReset)){
                   opts.onReset(elem);
               }
            } else if ($.isFunction(opts.onChange) && !valueChanged && previousValue != input.val()){
                valueChanged = true;
                opts.onChange(elem, input);
            }
         });
         
         // set the value on loosing focus
         input.blur(function() {
            if ($.isFunction(opts.setValue)) {
               opts.setValue(elem, input);
            } else {
               _setValue(elem, input);
            }
            opts.valueChanged();
         });
      }
      
      /** 
       * Helper function to copy css-styles from one element to another.<p>
       *
       * @param {Object} orig the original element (jQuery-object)
       * @param {Object} target the target element (jQuery-object)
       */
      function _copyCss(orig, target) {
         // list of styles that will be copied. 'display' is left out intentionally.
         var styleNames = ['width', 'height', 'font-size', 'font-weight', 'font-family', 'line-height', 'color', 'padding-top', 'padding-right', 'padding-bottom', 'padding-left', 'margin-top', 'margin-right', 'margin-bottom', 'margin-left', 'display'];
         var styles = {};
         for (i = 0; i < styleNames.length; i++) {
            styles[styleNames[i]] = orig.css(styleNames[i]);
         }
         target.css(styles);
      }
      
      /**
       * Helper function to transfer the value of the input element to the original element.
       * Executes the changed(elem, value) function.<p>
       *
       * @param {Object} elem the original element
       * @param {Object} input the input element
       */
      function _setValue(elem, input) {
         var previous = $.isFunction(opts.readValue) ? opts.readValue(elem) : elem.text();
         var current = input.val();
         if (previous != current) {
            elem.text(current);
         }
         elem.css('display', '');
         input.remove();
      }
   }
   
   /**
    * Destroy direct-input.
    */
   $.fn.directInput.destroy = function() {
      return this.unbind('click');
   };
   
   /**
    * default options
    * optional: setValue (function)
    */
   $.fn.directInput.defaults = {
      live: false,
      inputClass: 'cms-direct-input',
      marginHack: false,
      valueChanged: function() {
            }
   };
})(jQuery);
