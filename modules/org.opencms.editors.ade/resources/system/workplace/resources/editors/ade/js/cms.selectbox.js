(function($){
    /**
     * The OpenCms select-box jQuery plugin.
     * 
     * Takes a classic select-box and replaces it with a styled version.
     * 
     * Use following options:
     * open: function(elem) called when select-box is opened
     * select: function(this, elem, value) called when a value is selected
     * 
     * The following methods can be called by $(selector).selectbox('method name', additionalValue):
     * 'getValue': returns the currently selected value 
     * 'getIndex': returns the currently selected index
     * 'setValue': sets the value to additionalValue
     * 
     * The following styles are necessary:
     * <style>
     *	div.cms-selectbox{
     *   		position:relative; 
     *          display:inline-block;
     *   		line-height: 16px;
     *   		width: 100px;
     *   	}
     *   	
     *   	div.cms-selectbox span.cms-select-option{
     *   		display:block;
     *   		padding: 2px 5px 3px;
     *   		border:none;
     *   	}
     *  	
     *   	div.cms-selector{
     *   		width: 100px;
     *   		position:absolute;
     *   		top:20px;
     *   		left:-1px;
     *   		display:none;
     *   	}
     *   	
     *   	div.cms-open div.cms-selector{
     *   		display: block;
     *   	}
     *   
     *   </style>
     *   <!--[if IE 7]>
     *   <style>
     *   div.display{
     *       zoom: 1;
     *       display: inline;
     *       }
     *   </style>
     *   <![endif]-->
     * 
     * 
     * @param {Object} options the options object or the method string
     * @param {Object} additional the additional value object
     */
$.fn.selectBox=function(options, additional){
		
		var self=this;
        var opts;
        if (typeof options == 'string' && options.indexOf('_')!=0){
            return eval(options+'(additional)');
        }
        
		opts=$.extend({}, $.fn.selectBox.defaults, options);
		_init();
		return self;
		
        function _init(){
			self.each(function(){
                
				var replacer=_generateReplacer(_getValues($(this)));
                replacer.insertBefore(this);
                $(this).data('replacer',replacer);
			}).hide();
			$(document.body).click(_close);
		}
		
		function _start(){
            $(this).toggleClass('cms-open');
            if ($.isFunction(opts.open) && $(this).hasClass('cms-open')){
                opts.open(this);
            }
            return false;
        }
        
		function _close(){
           $('div.cms-selectbox').removeClass('cms-open');
        }
        
		function _select(){
            var replacer=$(this).closest('.cms-selectbox');
            var value=$(this).attr('rel');
            replacer.find('span.cms-current-value').text($(this).text()).attr('rel',value);
            _close();
            if ($.isFunction(opts.select)){
                opts.select(this, replacer, value);
            }
            return false;
        }
        
        function _getValues(select){
            result=[];
            select.find('option').each(function(){
                result.push({value: $(this).val(), title: $(this).text()});
            });
            return result;
        }
        
        function _getValue(replacer){
            return replacer.find('span.cms-current-value').attr('rel');
        }
        
        function _getIndex(replacer){
            return ('.cms_select_option', replacer).index($('.cms-current-value', replacer));
        }
        
        function _generateReplacer(values){
            var replacer=$('<div class="cms-selectbox ui-state-default ui-corner-all"><span class="cms-select-opener ui-icon ui-icon-triangle-1-s"></span></div>')
			$('<span class="cms-current-value cms-select-option"></span>').appendTo(replacer).text(values[0].title).attr('rel', values[0].value);
			var selector=_generateSelector(values).appendTo(replacer);
			replacer.click(_start);
            $('span.cms-select-option', replacer).andSelf().hover(function(){$(this).addClass('ui-state-hover');}, function(){$(this).removeClass('ui-state-hover')});
            replacer.data('replacer', replacer);
            if (opts.width){
                replacer.width(opts.width);
                selector.width(opts.width);
            }
            return replacer;    
        }
        
        function _generateSelector(values){
            var selector=$('<div class="cms-selector ui-widget-content ui-corner-bottom"></div>');
			for (i=0; i<values.length; i++){
				$('<span class="cms-select-option"></span>')
                    .attr('rel', values[i].value).text(values[i].title)
                    .appendTo(selector)
                    .click(_select);
			}
            return selector;
        }
        
        function generate(options){
            opts=$.extend({}, $.fn.selectBox.defaults, options);
            if ($.isArray(opts.values)){
                return _generateReplacer(opts.values);
            }
        }
        
        function getValue(){
            return _getValue(self.data('replacer'));
        }
        
        function setValue(value){
            var replacer=self.data('replacer');
            selectSpan=replacer.find('span[rel="'+value+'"]:first');
            if (selectSpan.length) {
                replacer.find('span.cms-current-value').text(selectSpan.text()).attr('rel', value);
            };
        }
	};
    
	$.fn.selectBox.defaults={
        width: null,
		open: null,
        select: null
		
	}
})(jQuery);