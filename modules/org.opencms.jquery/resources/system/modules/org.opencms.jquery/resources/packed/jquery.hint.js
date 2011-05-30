(function($) {

  /**
   * @author Remy Sharp
   * @url http://remysharp.com/2007/01/25/jquery-tutorial-text-box-hints/
   */
  $.fn.hint = function (blurClass, color) {
    if (!blurClass) { 
      blurClass = 'blur';
    }
    if (!color) {
      color = '#999999';
    }
    return this.each(function () {
      // get jQuery version of 'this'
      var $input = $(this);
      // capture the rest of the variable to allow for reuse
      var title = $input.attr('title'),
          $form = $(this.form),
          $win = $(window);

      function remove() {
        if ($input.val() === title && $input.hasClass(blurClass)) {
          $input.val('').removeClass(blurClass).css('color', '');
        }
      }

      // only apply logic if the element has the attribute
      if (title) { 
        // on blur, set value to title attr if text is blank
        $input.blur(function () {
          if (this.value === '') {
            $input.val(title).addClass(blurClass).css('color', color);
          }
        }).focus(remove).blur(); // now change all inputs to title
      
        // clear the pre-defined text when form is submitted
        $form.submit(remove);
        $win.unload(remove); // handles Firefox's autocomplete
      }
    });
  };
    
})(window.jQuery);

