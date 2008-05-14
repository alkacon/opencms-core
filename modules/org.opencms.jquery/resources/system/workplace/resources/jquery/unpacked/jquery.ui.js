/*
 * jQuery UI @VERSION
 *
 * Copyright (c) 2008 Paul Bakaus (ui.jquery.com)
 * Dual licensed under the MIT (MIT-LICENSE.txt)
 * and GPL (GPL-LICENSE.txt) licenses.
 *
 * http://docs.jquery.com/UI
 *
 * $Date: 2008/05/14 15:34:12 $
 * $Rev: 5419 $
 */
;(function($) {
	
	$.ui = {
		plugin: {
			add: function(module, option, set) {
				var proto = $.ui[module].prototype;
				for(var i in set) {
					proto.plugins[i] = proto.plugins[i] || [];
					proto.plugins[i].push([option, set[i]]);
				}
			},
			call: function(instance, name, args) {
				var set = instance.plugins[name];
				if(!set) { return; }
				
				for (var i = 0; i < set.length; i++) {
					if (instance.options[set[i][0]]) {
						set[i][1].apply(instance.element, args);
					}
				}
			}	
		},
		cssCache: {},
		css: function(name) {
			if ($.ui.cssCache[name]) { return $.ui.cssCache[name]; }
			var tmp = $('<div class="ui-resizable-gen">').addClass(name).css({position:'absolute', top:'-5000px', left:'-5000px', display:'block'}).appendTo('body');
			
			//if (!$.browser.safari)
				//tmp.appendTo('body'); 
			
			//Opera and Safari set width and height to 0px instead of auto
			//Safari returns rgba(0,0,0,0) when bgcolor is not set
			$.ui.cssCache[name] = !!(
				(!(/auto|default/).test(tmp.css('cursor')) || (/^[1-9]/).test(tmp.css('height')) || (/^[1-9]/).test(tmp.css('width')) || 
				!(/none/).test(tmp.css('backgroundImage')) || !(/transparent|rgba\(0, 0, 0, 0\)/).test(tmp.css('backgroundColor')))
			);
			try { $('body').get(0).removeChild(tmp.get(0));	} catch(e){}
			return $.ui.cssCache[name];
		},
		disableSelection: function(e) {
			e.unselectable = "on";
			e.onselectstart = function() { return false; };
			if (e.style) { e.style.MozUserSelect = "none"; }
		},
		enableSelection: function(e) {
			e.unselectable = "off";
			e.onselectstart = function() { return true; };
			if (e.style) { e.style.MozUserSelect = ""; }
		},
		hasScroll: function(e, a) {
			var scroll = /top/.test(a||"top") ? 'scrollTop' : 'scrollLeft', has = false;
			if (e[scroll] > 0) return true; e[scroll] = 1;
			has = e[scroll] > 0 ? true : false; e[scroll] = 0;
			return has;
		}
	};
	
	
	/** jQuery core modifications and additions **/
	
	var _remove = $.fn.remove;
	$.fn.remove = function() {
		$("*", this).add(this).trigger("remove");
		return _remove.apply(this, arguments );
	};
	
	// $.widget is a factory to create jQuery plugins
	// taking some boilerplate code out of the plugin code
	// created by Scott GonzÃ¡lez and JÃ¶rn Zaefferer
	function getter(namespace, plugin, method) {
		var methods = $[namespace][plugin].getter || [];
		methods = (typeof methods == "string" ? methods.split(/,?\s+/) : methods);
		return ($.inArray(method, methods) != -1);
	};
	
	var widgetPrototype = {
		init: function() {},
		destroy: function() {},
		
		getData: function(e, key) {
			return this.options[key];
		},
		setData: function(e, key, value) {
			this.options[key] = value;
		},
		
		enable: function() {
			this.setData(null, 'disabled', false);
		},
		disable: function() {
			this.setData(null, 'disabled', true);
		}
	};
	
	$.widget = function(name, prototype) {
		var namespace = name.split(".")[0];
		name = name.split(".")[1];
		// create plugin method
		$.fn[name] = function(options, data) {
			var isMethodCall = (typeof options == 'string'),
				args = arguments;
			
			if (isMethodCall && getter(namespace, name, options)) {
				var instance = $.data(this[0], name);
				return (instance ? instance[options](data) : undefined); 
			}
			
			return this.each(function() {
				var instance = $.data(this, name);
				if (!instance) {
					$.data(this, name, new $[namespace][name](this, options));
				} else if (isMethodCall) {
					instance[options].apply(instance, $.makeArray(args).slice(1));
				}
			});
		};
		
		// create widget constructor
		$[namespace][name] = function(element, options) {
			var self = this;
			
			this.options = $.extend({}, $[namespace][name].defaults, options);
			this.element = $(element)
				.bind('setData.' + name, function(e, key, value) {
					return self.setData(e, key, value);
				})
				.bind('getData.' + name, function(e, key) {
					return self.getData(e, key);
				})
				.bind('remove', function() {
					return self.destroy();
				});
			this.init();
		};
		
		// add widget prototype
		$[namespace][name].prototype = $.extend({}, widgetPrototype, prototype);
	};
	
	
	/** Mouse Interaction Plugin **/
	
	$.widget("ui.mouse", {
		init: function() {
			var self = this;
			
			this.element
				.bind('mousedown.mouse', function() { return self.click.apply(self, arguments); })
				.bind('mouseup.mouse', function() { (self.timer && clearInterval(self.timer)); })
				.bind('click.mouse', function() { if(self.initialized) { self.initialized = false; return false; } });
			//Prevent text selection in IE
			if ($.browser.msie) {
				this.unselectable = this.element.attr('unselectable');
				this.element.attr('unselectable', 'on');
			}
		},
		destroy: function() {
			this.element.unbind('.mouse').removeData("mouse");
			($.browser.msie && this.element.attr('unselectable', this.unselectable));
		},
		trigger: function() { return this.click.apply(this, arguments); },
		click: function(e) {
		
			if(    e.which != 1 //only left click starts dragging
				|| $.inArray(e.target.nodeName.toLowerCase(), this.options.dragPrevention || []) != -1 // Prevent execution on defined elements
				|| (this.options.condition && !this.options.condition.apply(this.options.executor || this, [e, this.element])) //Prevent execution on condition
			) { return true; }
		
			var self = this;
			this.initialized = false;
			var initialize = function() {
				self._MP = { left: e.pageX, top: e.pageY }; // Store the click mouse position
				$(document).bind('mouseup.mouse', function() { return self.stop.apply(self, arguments); });
				$(document).bind('mousemove.mouse', function() { return self.drag.apply(self, arguments); });
		
				if(!self.initalized && Math.abs(self._MP.left-e.pageX) >= self.options.distance || Math.abs(self._MP.top-e.pageY) >= self.options.distance) {
					(self.options.start && self.options.start.call(self.options.executor || self, e, self.element));
					(self.options.drag && self.options.drag.call(self.options.executor || self, e, this.element)); //This is actually not correct, but expected
					self.initialized = true;
				}
			};

			if(this.options.delay) {
				if(this.timer) { clearInterval(this.timer); }
				this.timer = setTimeout(initialize, this.options.delay);
			} else {
				initialize();
			}
				
			return false;
			
		},
		stop: function(e) {
			
			if(!this.initialized) {
				return $(document).unbind('mouseup.mouse').unbind('mousemove.mouse');
			}

			(this.options.stop && this.options.stop.call(this.options.executor || this, e, this.element));
			
			$(document).unbind('mouseup.mouse').unbind('mousemove.mouse');
			return false;
			
		},
		drag: function(e) {

			var o = this.options;
			if ($.browser.msie && !e.button) {
				return this.stop.call(this, e); // IE mouseup check
			}
			
			if(!this.initialized && (Math.abs(this._MP.left-e.pageX) >= o.distance || Math.abs(this._MP.top-e.pageY) >= o.distance)) {
				(o.start && o.start.call(o.executor || this, e, this.element));
				this.initialized = true;
			} else {
				if(!this.initialized) { return false; }
			}

			(o.drag && o.drag.call(this.options.executor || this, e, this.element));
			return false;
			
		}
	});
	
})(jQuery);
/*
 * jQuery UI Draggable
 *
 * Copyright (c) 2008 Paul Bakaus
 * Dual licensed under the MIT (MIT-LICENSE.txt)
 * and GPL (GPL-LICENSE.txt) licenses.
 * 
 * http://docs.jquery.com/UI/Draggables
 *
 * Depends:
 *	ui.core.js
 *
 * Revision: $Id: jquery.ui.js,v 1.1 2008/05/14 15:34:12 m.moossen Exp $
 */

;(function($) {
	
	$.widget("ui.draggable", {
		init: function() {
			
			//Initialize needed constants
			var o = this.options;

			//Initialize mouse events for interaction
			this.element.mouse({
				executor: this,
				delay: o.delay,
				distance: o.distance,
				dragPrevention: o.cancel,
				start: this.start,
				stop: this.stop,
				drag: this.drag,
				condition: function(e) {
					var handle = !this.options.handle || !$(this.options.handle, this.element).length ? true : false;
					if(!handle) $(this.options.handle, this.element).each(function() { if(this == e.target) handle = true; });
					return !(e.target.className.indexOf("ui-resizable-handle") != -1 || this.options.disabled) && handle;
				}
			});
			
			//Position the node
			if(o.helper == 'original' && !(/(relative|absolute|fixed)/).test(this.element.css('position')))
				this.element.css('position', 'relative');
			
		},
		start: function(e) {

			var o = this.options;
			if($.ui.ddmanager) $.ui.ddmanager.current = this;
			
			//Create and append the visible helper
			this.helper = $.isFunction(o.helper) ? $(o.helper.apply(this.element[0], [e])) : (o.helper == 'clone' ? this.element.clone() : this.element);
			if(!this.helper.parents('body').length) this.helper.appendTo((o.appendTo == 'parent' ? this.element[0].parentNode : o.appendTo));
			if(!this.helper.css("position") || this.helper.css("position") == "static") this.helper.css("position", "absolute");

			/*
			 * - Position generation -
			 * This block generates everything position related - it's the core of draggables.
			 */
			
			this.margins = {																				//Cache the margins
				left: (parseInt(this.element.css("marginLeft"),10) || 0),
				top: (parseInt(this.element.css("marginTop"),10) || 0)
			};		

			this.cssPosition = this.helper.css("position");													//Store the helper's css position
			this.offset = this.element.offset();															//The element's absolute position on the page
			this.offset = {																					//Substract the margins from the element's absolute offset
				top: this.offset.top - this.margins.top,
				left: this.offset.left - this.margins.left
			};

			this.offset.click = {																			//Where the click happened, relative to the element
				left: e.pageX - this.offset.left,
				top: e.pageY - this.offset.top
			};
	
			this.offsetParent = this.helper.offsetParent(); var po = this.offsetParent.offset();			//Get the offsetParent and cache its position
			this.offset.parent = {																			//Store its position plus border
				top: po.top + (parseInt(this.offsetParent.css("borderTopWidth"),10) || 0),
				left: po.left + (parseInt(this.offsetParent.css("borderLeftWidth"),10) || 0)
			};
	
			var p = this.element.position();																//This is a relative to absolute position minus the actual position calculation - only used for relative positioned helpers
			this.offset.relative = this.cssPosition == "relative" ? {
				top: p.top - (parseInt(this.helper.css("top"),10) || 0) + this.offsetParent[0].scrollTop,
				left: p.left - (parseInt(this.helper.css("left"),10) || 0) + this.offsetParent[0].scrollLeft
			} : { top: 0, left: 0 };
		
			this.originalPosition = this.generatePosition(e);												//Generate the original position
			this.helperProportions = { width: this.helper.outerWidth(), height: this.helper.outerHeight() };//Cache the helper size

			if(o.cursorAt) {
				if(o.cursorAt.left != undefined) this.offset.click.left = o.cursorAt.left;
				if(o.cursorAt.right != undefined) this.offset.click.left = this.helperProportions.width - o.cursorAt.right;
				if(o.cursorAt.top != undefined) this.offset.click.top = o.cursorAt.top;
				if(o.cursorAt.bottom != undefined) this.offset.click.top = this.helperProportions.height - o.cursorAt.bottom;
			}


			/*
			 * - Position constraining -
			 * Here we prepare position constraining like grid and containment.
			 */	
			
			if(o.containment) {
				if(o.containment == 'parent') o.containment = this.helper[0].parentNode;
				if(o.containment == 'document') this.containment = [0,0,$(document).width(), ($(document).height() || document.body.parentNode.scrollHeight)];
				if(!(/^(document|window|parent)$/).test(o.containment)) {
					var ce = $(o.containment)[0];
					var co = $(o.containment).offset();

					this.containment = [
						co.left + (parseInt($(ce).css("borderLeftWidth"),10) || 0) - this.offset.relative.left - this.offset.parent.left,
						co.top + (parseInt($(ce).css("borderTopWidth"),10) || 0) - this.offset.relative.top - this.offset.parent.top,
						co.left+Math.max(ce.scrollWidth,ce.offsetWidth) - (parseInt($(ce).css("borderLeftWidth"),10) || 0) - this.offset.relative.left - this.offset.parent.left - this.helperProportions.width - this.margins.left - (parseInt(this.element.css("marginRight"),10) || 0),
						co.top+Math.max(ce.scrollHeight,ce.offsetHeight) - (parseInt($(ce).css("borderTopWidth"),10) || 0) - this.offset.relative.top - this.offset.parent.top - this.helperProportions.height - this.margins.top - (parseInt(this.element.css("marginBottom"),10) || 0)
					];
				}
			}


			//Call plugins and callbacks
			this.propagate("start", e);

			this.helperProportions = { width: this.helper.outerWidth(), height: this.helper.outerHeight() };//Recache the helper size
			if ($.ui.ddmanager && !o.dropBehaviour) $.ui.ddmanager.prepareOffsets(this, e);

			return false;

		},
		convertPositionTo: function(d, pos) {
			if(!pos) pos = this.position;
			var mod = d == "absolute" ? 1 : -1;
			return {
				top: (
					pos.top																	// the calculated relative position
					+ this.offset.relative.top	* mod										// Only for relative positioned nodes: Relative offset from element to offset parent
					+ this.offset.parent.top * mod											// The offsetParent's offset without borders (offset + border)
					- (this.cssPosition == "fixed" ? 0 : this.offsetParent[0].scrollTop) * mod	// The offsetParent's scroll position, not if the element is fixed
					+ this.margins.top * mod												//Add the margin (you don't want the margin counting in intersection methods)
				),
				left: (
					pos.left																// the calculated relative position
					+ this.offset.relative.left	* mod										// Only for relative positioned nodes: Relative offset from element to offset parent
					+ this.offset.parent.left * mod											// The offsetParent's offset without borders (offset + border)
					- (this.cssPosition == "fixed" ? 0 : this.offsetParent[0].scrollLeft) * mod	// The offsetParent's scroll position, not if the element is fixed
					+ this.margins.left * mod												//Add the margin (you don't want the margin counting in intersection methods)
				)
			};
		},
		generatePosition: function(e) {
			
			var o = this.options;
			var position = {
				top: (
					e.pageY																	// The absolute mouse position
					- this.offset.click.top													// Click offset (relative to the element)
					- this.offset.relative.top												// Only for relative positioned nodes: Relative offset from element to offset parent
					- this.offset.parent.top												// The offsetParent's offset without borders (offset + border)
					+ (this.cssPosition == "fixed" ? 0 : this.offsetParent[0].scrollTop)	// The offsetParent's scroll position, not if the element is fixed
				),
				left: (
					e.pageX																	// The absolute mouse position
					- this.offset.click.left												// Click offset (relative to the element)
					- this.offset.relative.left												// Only for relative positioned nodes: Relative offset from element to offset parent
					- this.offset.parent.left												// The offsetParent's offset without borders (offset + border)
					+ (this.cssPosition == "fixed" ? 0 : this.offsetParent[0].scrollLeft)	// The offsetParent's scroll position, not if the element is fixed
				)
			};

			if(!this.originalPosition) return position;										//If we are not dragging yet, we won't check for options
			
			
			/*
			 * - Position constraining -
			 * Constrain the position to a mix of grid, containment.
			 */
			if(this.containment) {
				if(position.left < this.containment[0]) position.left = this.containment[0];
				if(position.top < this.containment[1]) position.top = this.containment[1];
				if(position.left > this.containment[2]) position.left = this.containment[2];
				if(position.top > this.containment[3]) position.top = this.containment[3];
			}
			
			if(o.grid) {
				var top = this.originalPosition.top + Math.round((position.top - this.originalPosition.top) / o.grid[1]) * o.grid[1];
				position.top = this.containment ? (!(top < this.containment[1] || top > this.containment[3]) ? top : (!(top < this.containment[1]) ? top - o.grid[1] : top + o.grid[1])) : top;

				var left = this.originalPosition.left + Math.round((position.left - this.originalPosition.left) / o.grid[0]) * o.grid[0];
				position.left = this.containment ? (!(left < this.containment[0] || left > this.containment[2]) ? left : (!(left < this.containment[0]) ? left - o.grid[0] : left + o.grid[0])) : left;
			}
			
			return position;
		},
		drag: function(e) {

			//Compute the helpers position
			this.position = this.generatePosition(e);
			this.positionAbs = this.convertPositionTo("absolute");

			//Call plugins and callbacks and use the resulting position if something is returned		
			this.position = this.propagate("drag", e) || this.position;
			
			if(!this.options.axis || this.options.axis == "x") this.helper[0].style.left = this.position.left+'px';
			if(!this.options.axis || this.options.axis == "y") this.helper[0].style.top = this.position.top+'px';
			if($.ui.ddmanager) $.ui.ddmanager.drag(this, e);
			return false;

		},
		stop: function(e) {
		
			//If we are using droppables, inform the manager about the drop
			if ($.ui.ddmanager && !this.options.dropBehaviour)
				$.ui.ddmanager.drop(this, e);
				
			if(this.options.revert) {
				var self = this;
				$(this.helper).animate(this.originalPosition, parseInt(this.options.revert, 10) || 500, function() {
					self.propagate("stop", e);
					self.clear();
				});
			} else {
				this.propagate("stop", e);
				this.clear();
			}

			return false;
			
		},
		clear: function() {
			if(this.options.helper != 'original' && !this.cancelHelperRemoval) this.helper.remove();
			if($.ui.ddmanager) $.ui.ddmanager.current = null;
			this.helper = null;
			this.cancelHelperRemoval = false;
		},
		
		// From now on bulk stuff - mainly helpers
		plugins: {},
		ui: function(e) {
			return {
				helper: this.helper,
				position: this.position,
				absolutePosition: this.positionAbs,
				options: this.options			
			};
		},
		propagate: function(n,e) {
			$.ui.plugin.call(this, n, [e, this.ui()]);
			return this.element.triggerHandler(n == "drag" ? n : "drag"+n, [e, this.ui()], this.options[n]);
		},
		destroy: function() {
			if(!this.element.data('draggable')) return;
			this.element.removeData("draggable").unbind(".draggable").mouse("destroy");
		},
		enable: function() {
			this.options.disabled = false;
		},
		disable: function() {
			this.options.disabled = true;
		}
	});
	
	$.ui.draggable.defaults = {
		helper: "original",
		appendTo: "parent",
		cancel: ['input','textarea','button','select','option'],
		distance: 1,
		delay: 0
	};
	
	
	$.ui.plugin.add("draggable", "cursor", {
		start: function(e, ui) {
			var t = $('body');
			if (t.css("cursor")) ui.options._cursor = t.css("cursor");
			t.css("cursor", ui.options.cursor);
		},
		stop: function(e, ui) {
			if (ui.options._cursor) $('body').css("cursor", ui.options._cursor);
		}
	});

	$.ui.plugin.add("draggable", "zIndex", {
		start: function(e, ui) {
			var t = $(ui.helper);
			if(t.css("zIndex")) ui.options._zIndex = t.css("zIndex");
			t.css('zIndex', ui.options.zIndex);
		},
		stop: function(e, ui) {
			if(ui.options._zIndex) $(ui.helper).css('zIndex', ui.options._zIndex);
		}
	});

	$.ui.plugin.add("draggable", "opacity", {
		start: function(e, ui) {
			var t = $(ui.helper);
			if(t.css("opacity")) ui.options._opacity = t.css("opacity");
			t.css('opacity', ui.options.opacity);
		},
		stop: function(e, ui) {
			if(ui.options._opacity) $(ui.helper).css('opacity', ui.options._opacity);
		}
	});
	
	$.ui.plugin.add("draggable", "iframeFix", {
		start: function(e, ui) {
			$(ui.options.iframeFix === true ? "iframe" : ui.options.iframeFix).each(function() {					
				$('<div class="ui-draggable-iframeFix" style="background: #fff;"></div>')
				.css({
					width: this.offsetWidth+"px", height: this.offsetHeight+"px",
					position: "absolute", opacity: "0.001", zIndex: 1000
				})
				.css($(this).offset())
				.appendTo("body");
			});
		},
		stop: function(e, ui) {
			$("div.DragDropIframeFix").each(function() { this.parentNode.removeChild(this); }); //Remove frame helpers	
		}
	});
	
	$.ui.plugin.add("draggable", "scroll", {
		start: function(e, ui) {
			var o = ui.options;
			var i = $(this).data("draggable");
			o.scrollSensitivity	= o.scrollSensitivity || 20;
			o.scrollSpeed		= o.scrollSpeed || 20;

			i.overflowY = function(el) {
				do { if(/auto|scroll/.test(el.css('overflow')) || (/auto|scroll/).test(el.css('overflow-y'))) return el; el = el.parent(); } while (el[0].parentNode);
				return $(document);
			}(this);
			i.overflowX = function(el) {
				do { if(/auto|scroll/.test(el.css('overflow')) || (/auto|scroll/).test(el.css('overflow-x'))) return el; el = el.parent(); } while (el[0].parentNode);
				return $(document);
			}(this);
			
			if(i.overflowY[0] != document && i.overflowY[0].tagName != 'HTML') i.overflowYOffset = i.overflowY.offset();
			if(i.overflowX[0] != document && i.overflowX[0].tagName != 'HTML') i.overflowXOffset = i.overflowX.offset();
			
		},
		drag: function(e, ui) {
			
			var o = ui.options;
			var i = $(this).data("draggable");

			if(i.overflowY[0] != document && i.overflowY[0].tagName != 'HTML') {
				if((i.overflowYOffset.top + i.overflowY[0].offsetHeight) - e.pageY < o.scrollSensitivity)
					i.overflowY[0].scrollTop = i.overflowY[0].scrollTop + o.scrollSpeed;
				if(e.pageY - i.overflowYOffset.top < o.scrollSensitivity)
					i.overflowY[0].scrollTop = i.overflowY[0].scrollTop - o.scrollSpeed;
								
			} else {
				if(e.pageY - $(document).scrollTop() < o.scrollSensitivity)
					$(document).scrollTop($(document).scrollTop() - o.scrollSpeed);
				if($(window).height() - (e.pageY - $(document).scrollTop()) < o.scrollSensitivity)
					$(document).scrollTop($(document).scrollTop() + o.scrollSpeed);
			}
			
			if(i.overflowX[0] != document && i.overflowX[0].tagName != 'HTML') {
				if((i.overflowXOffset.left + i.overflowX[0].offsetWidth) - e.pageX < o.scrollSensitivity)
					i.overflowX[0].scrollLeft = i.overflowX[0].scrollLeft + o.scrollSpeed;
				if(e.pageX - i.overflowXOffset.left < o.scrollSensitivity)
					i.overflowX[0].scrollLeft = i.overflowX[0].scrollLeft - o.scrollSpeed;
			} else {
				if(e.pageX - $(document).scrollLeft() < o.scrollSensitivity)
					$(document).scrollLeft($(document).scrollLeft() - o.scrollSpeed);
				if($(window).width() - (e.pageX - $(document).scrollLeft()) < o.scrollSensitivity)
					$(document).scrollLeft($(document).scrollLeft() + o.scrollSpeed);
			}

		}
	});
	
	$.ui.plugin.add("draggable", "snap", {
		start: function(e, ui) {
			
			var inst = $(this).data("draggable");
			inst.snapElements = [];
			$(ui.options.snap === true ? '.ui-draggable' : ui.options.snap).each(function() {
				var $t = $(this); var $o = $t.offset();
				if(this != inst.element[0]) inst.snapElements.push({
					item: this,
					width: $t.outerWidth(), height: $t.outerHeight(),
					top: $o.top, left: $o.left
				});
			});
			
		},
		drag: function(e, ui) {

			var inst = $(this).data("draggable");
			var d = ui.options.snapTolerance || 20;
			var x1 = ui.absolutePosition.left, x2 = x1 + inst.helperProportions.width,
				y1 = ui.absolutePosition.top, y2 = y1 + inst.helperProportions.height;

			for (var i = inst.snapElements.length - 1; i >= 0; i--){

				var l = inst.snapElements[i].left, r = l + inst.snapElements[i].width, 
					t = inst.snapElements[i].top, b = t + inst.snapElements[i].height;

				//Yes, I know, this is insane ;)
				if(!((l-d < x1 && x1 < r+d && t-d < y1 && y1 < b+d) || (l-d < x1 && x1 < r+d && t-d < y2 && y2 < b+d) || (l-d < x2 && x2 < r+d && t-d < y1 && y1 < b+d) || (l-d < x2 && x2 < r+d && t-d < y2 && y2 < b+d))) continue;

				if(ui.options.snapMode != 'inner') {
					var ts = Math.abs(t - y2) <= 20;
					var bs = Math.abs(b - y1) <= 20;
					var ls = Math.abs(l - x2) <= 20;
					var rs = Math.abs(r - x1) <= 20;
					if(ts) ui.position.top = inst.convertPositionTo("relative", { top: t - inst.helperProportions.height, left: 0 }).top;
					if(bs) ui.position.top = inst.convertPositionTo("relative", { top: b, left: 0 }).top;
					if(ls) ui.position.left = inst.convertPositionTo("relative", { top: 0, left: l - inst.helperProportions.width }).left;
					if(rs) ui.position.left = inst.convertPositionTo("relative", { top: 0, left: r }).left;
				}
				
				if(ui.options.snapMode != 'outer') {
					var ts = Math.abs(t - y1) <= 20;
					var bs = Math.abs(b - y2) <= 20;
					var ls = Math.abs(l - x1) <= 20;
					var rs = Math.abs(r - x2) <= 20;
					if(ts) ui.position.top = inst.convertPositionTo("relative", { top: t, left: 0 }).top;
					if(bs) ui.position.top = inst.convertPositionTo("relative", { top: b - inst.helperProportions.height, left: 0 }).top;
					if(ls) ui.position.left = inst.convertPositionTo("relative", { top: 0, left: l }).left;
					if(rs) ui.position.left = inst.convertPositionTo("relative", { top: 0, left: r - inst.helperProportions.width }).left;
				}

			};
		}
	});
	
	$.ui.plugin.add("draggable", "connectToSortable", {
		start: function(e,ui) {
			var inst = $(this).data("draggable");
			inst.sortable = $.data($(ui.options.connectToSortable)[0], 'sortable');
			inst.sortableOffset = inst.sortable.element.offset();
			inst.sortableOuterWidth = inst.sortable.element.outerWidth();
			inst.sortableOuterHeight = inst.sortable.element.outerHeight();
			if(inst.sortable.options.revert) inst.sortable.shouldRevert = true;
		},
		stop: function(e,ui) {
			//If we are still over the sortable, we fake the stop event of the sortable, but also remove helper
			var instDraggable = $(this).data("draggable");
			var inst = instDraggable.sortable;
			
			if(inst.isOver) {
				inst.isOver = 0;
				instDraggable.cancelHelperRemoval = true; //Don't remove the helper in the draggable instance
				inst.cancelHelperRemoval = false; //Remove it in the sortable instance (so sortable plugins like revert still work)
				if(inst.shouldRevert) inst.options.revert = true; //revert here
				inst.stop(e);
				inst.options.helper = "original";
			}
		},
		drag: function(e,ui) {
			//This is handy: We reuse the intersectsWith method for checking if the current draggable helper
			//intersects with the sortable container
			var instDraggable = $(this).data("draggable");
			var inst = instDraggable.sortable;
			instDraggable.position.absolute = ui.absolutePosition; //Sorry, this is an ugly API fix
			
			if(inst.intersectsWith.call(instDraggable, {
				left: instDraggable.sortableOffset.left, top: instDraggable.sortableOffset.top,
				width: instDraggable.sortableOuterWidth, height: instDraggable.sortableOuterHeight
			})) {
				//If it intersects, we use a little isOver variable and set it once, so our move-in stuff gets fired only once
				if(!inst.isOver) {
					inst.isOver = 1;
					
					//Cache the width/height of the new helper
					var height = inst.options.placeholderElement ? $(inst.options.placeholderElement, $(inst.options.items, inst.element)).innerHeight() : $(inst.options.items, inst.element).innerHeight();
					var width = inst.options.placeholderElement ? $(inst.options.placeholderElement, $(inst.options.items, inst.element)).innerWidth() : $(inst.options.items, inst.element).innerWidth();

					//Now we fake the start of dragging for the sortable instance,
					//by cloning the list group item, appending it to the sortable and using it as inst.currentItem
					//We can then fire the start event of the sortable with our passed browser event, and our own helper (so it doesn't create a new one)
					inst.currentItem = $(this).clone().appendTo(inst.element);
					inst.options.helper = function() { return ui.helper[0]; };
					inst.start(e);
					
					//Because the browser event is way off the new appended portlet, we modify a couple of variables to reflect the changes
					inst.clickOffset.top = instDraggable.offset.click.top;
					inst.clickOffset.left = instDraggable.offset.click.left;
					inst.offset.left -= ui.absolutePosition.left - inst.position.absolute.left;
					inst.offset.top -= ui.absolutePosition.top - inst.position.absolute.top;
					
					//Do a nifty little helper animation: Animate it to the portlet's size (just takes the first 'li' element in the sortable now)
					inst.helperProportions = {width: width, height: height}; //We have to reset the helper proportions, because we are doing our animation there
					ui.helper.animate({height: height, width: width}, 500);
					instDraggable.propagate("toSortable", e);
				
				}

				//Provided we did all the previous steps, we can fire the drag event of the sortable on every draggable drag, when it intersects with the sortable
				if(inst.currentItem) inst.drag(e);
				
			} else {
				
				//If it doesn't intersect with the sortable, and it intersected before,
				//we fake the drag stop of the sortable, but make sure it doesn't remove the helper by using cancelHelperRemoval
				if(inst.isOver) {
					inst.isOver = 0;
					inst.cancelHelperRemoval = true;
					inst.options.revert = false; //No revert here
					inst.stop(e);
					inst.options.helper = "original";
					
					//Now we remove our currentItem, the list group clone again, and the placeholder, and animate the helper back to it's original size
					inst.currentItem.remove();
					inst.placeholder.remove();
					
					ui.helper.animate({ height: this.innerHeight(), width: this.innerWidth() }, 500);
					instDraggable.propagate("fromSortable", e);
				}
				
			};
		}
	});
	
	$.ui.plugin.add("draggable", "stack", {
		start: function(e,ui) {
			var group = $.makeArray($(ui.options.stack.group)).sort(function(a,b) {
				return (parseInt($(a).css("zIndex")) || ui.options.stack.min) - (parseInt($(b).css("zIndex")) || ui.options.stack.min);
			});
			
			$(group).each(function(i) {
				this.style.zIndex = ui.options.stack.min + i;
			});
			
			this[0].style.zIndex = ui.options.stack.min + group.length;
		}
	});
	
})(jQuery);/*
 * jQuery UI Droppable
 *
 * Copyright (c) 2008 Paul Bakaus
 * Dual licensed under the MIT (MIT-LICENSE.txt)
 * and GPL (GPL-LICENSE.txt) licenses.
 * 
 * http://docs.jquery.com/UI/Droppables
 *
 * Depends:
 *	ui.core.js
 *	ui.draggable.js
 *
 * Revision: $Id: jquery.ui.js,v 1.1 2008/05/14 15:34:12 m.moossen Exp $
 */

;(function($) {

	$.widget("ui.droppable", {
		init: function() {
	
			this.element.addClass("ui-droppable");
			this.isover = 0; this.isout = 1;
			
			//Prepare the passed options
			var o = this.options, accept = o.accept;
			o = $.extend(o, {
				accept: o.accept && o.accept.constructor == Function ? o.accept : function(d) {
					return $(d).is(accept);
				}
			});
			
			//Store the droppable's proportions
			this.proportions = { width: this.element.outerWidth(), height: this.element.outerHeight() };
			
			// Add the reference and positions to the manager
			$.ui.ddmanager.droppables.push(this);
			
		},
		plugins: {},
		ui: function(c) {
			return {
				instance: this,
				draggable: (c.currentItem || c.element),
				helper: c.helper,
				position: c.position,
				absolutePosition: c.positionAbs,
				options: this.options,
				element: this.element
			};
		},
		destroy: function() {
			var drop = $.ui.ddmanager.droppables;
			for ( var i = 0; i < drop.length; i++ )
				if ( drop[i] == this )
					drop.splice(i, 1);
			
			this.element
				.removeClass("ui-droppable ui-droppable-disabled")
				.removeData("droppable")
				.unbind(".droppable");
		},
		enable: function() {
			this.element.removeClass("ui-droppable-disabled");
			this.options.disabled = false;
		},
		disable: function() {
			this.element.addClass("ui-droppable-disabled");
			this.options.disabled = true;
		},
		over: function(e) {
			
			var draggable = $.ui.ddmanager.current;
			if (!draggable || (draggable.currentItem || draggable.element)[0] == this.element[0]) return; // Bail if draggable and droppable are same element
			
			if (this.options.accept.call(this.element,(draggable.currentItem || draggable.element))) {
				$.ui.plugin.call(this, 'over', [e, this.ui(draggable)]);
				this.element.triggerHandler("dropover", [e, this.ui(draggable)], this.options.over);
			}
			
		},
		out: function(e) {
			
			var draggable = $.ui.ddmanager.current;
			if (!draggable || (draggable.currentItem || draggable.element)[0] == this.element[0]) return; // Bail if draggable and droppable are same element
			
			if (this.options.accept.call(this.element,(draggable.currentItem || draggable.element))) {
				$.ui.plugin.call(this, 'out', [e, this.ui(draggable)]);
				this.element.triggerHandler("dropout", [e, this.ui(draggable)], this.options.out);
			}
			
		},
		drop: function(e,custom) {
			
			var draggable = custom || $.ui.ddmanager.current;
			if (!draggable || (draggable.currentItem || draggable.element)[0] == this.element[0]) return false; // Bail if draggable and droppable are same element
			
			var childrenIntersection = false;
			this.element.find(".ui-droppable").each(function() {
				var inst = $.data(this, 'droppable');
				if(inst.options.greedy && $.ui.intersect(draggable, $.extend(inst, { offset: inst.element.offset() }), inst.options.tolerance)) {
					childrenIntersection = true; return false;
				}
			});
			if(childrenIntersection) return false;
			
			if(this.options.accept.call(this.element,(draggable.currentItem || draggable.element))) {
				$.ui.plugin.call(this, 'drop', [e, this.ui(draggable)]);
				this.element.triggerHandler("drop", [e, this.ui(draggable)], this.options.drop);
				return true;
			}
			
			return false;
			
		},
		activate: function(e) {
			
			var draggable = $.ui.ddmanager.current;
			$.ui.plugin.call(this, 'activate', [e, this.ui(draggable)]);
			if(draggable) this.element.triggerHandler("dropactivate", [e, this.ui(draggable)], this.options.activate);
			
		},
		deactivate: function(e) {
			
			var draggable = $.ui.ddmanager.current;
			$.ui.plugin.call(this, 'deactivate', [e, this.ui(draggable)]);
			if(draggable) this.element.triggerHandler("dropdeactivate", [e, this.ui(draggable)], this.options.deactivate);
			
		}
	});
	
	$.extend($.ui.droppable, {
		defaults: {
			disabled: false,
			tolerance: 'intersect'
		}
	});
	
	$.ui.intersect = function(draggable, droppable, toleranceMode) {
		
		if (!droppable.offset) return false;
		
		var x1 = (draggable.positionAbs || draggable.position.absolute).left, x2 = x1 + draggable.helperProportions.width,
			y1 = (draggable.positionAbs || draggable.position.absolute).top, y2 = y1 + draggable.helperProportions.height;
		var l = droppable.offset.left, r = l + droppable.proportions.width,
			t = droppable.offset.top, b = t + droppable.proportions.height;
		
		switch (toleranceMode) {
			case 'fit':
				
				if(!((y2-(draggable.helperProportions.height/2) > t && y1 < t) || (y1 < b && y2 > b) || (x2 > l && x1 < l) || (x1 < r && x2 > r))) return false;
				
				if(y2-(draggable.helperProportions.height/2) > t && y1 < t) return 1; //Crosses top edge
				if(y1 < b && y2 > b) return 2; //Crosses bottom edge
				if(x2 > l && x1 < l) return 1; //Crosses left edge
				if(x1 < r && x2 > r) return 2; //Crosses right edge
				
				//return (l < x1 && x2 < r
				//	&& t < y1 && y2 < b);
				break;
			case 'intersect':
				return (l < x1 + (draggable.helperProportions.width / 2) // Right Half
					&& x2 - (draggable.helperProportions.width / 2) < r // Left Half
					&& t < y1 + (draggable.helperProportions.height / 2) // Bottom Half
					&& y2 - (draggable.helperProportions.height / 2) < b ); // Top Half
				break;
			case 'pointer':
				return (l < ((draggable.positionAbs || draggable.position.absolute).left + (draggable.clickOffset || draggable.offset.click).left) && ((draggable.positionAbs || draggable.position.absolute).left + (draggable.clickOffset || draggable.offset.click).left) < r
					&& t < ((draggable.positionAbs || draggable.position.absolute).top + (draggable.clickOffset || draggable.offset.click).top) && ((draggable.positionAbs || draggable.position.absolute).top + (draggable.clickOffset || draggable.offset.click).top) < b);
				break;
			case 'touch':
				return (
						(y1 >= t && y1 <= b) ||	// Top edge touching
						(y2 >= t && y2 <= b) ||	// Bottom edge touching
						(y1 < t && y2 > b)		// Surrounded vertically
					) && (
						(x1 >= l && x1 <= r) ||	// Left edge touching
						(x2 >= l && x2 <= r) ||	// Right edge touching
						(x1 < l && x2 > r)		// Surrounded horizontally
					);
				break;
			default:
				return false;
				break;
			}
		
	};
	
	/*
		This manager tracks offsets of draggables and droppables
	*/
	$.ui.ddmanager = {
		current: null,
		droppables: [],
		prepareOffsets: function(t, e) {
			
			var m = $.ui.ddmanager.droppables;
			var type = e ? e.type : null; // workaround for #2317
			for (var i = 0; i < m.length; i++) {
				
				if(m[i].options.disabled || (t && !m[i].options.accept.call(m[i].element,(t.currentItem || t.element)))) continue;
				m[i].visible = m[i].element.is(":visible"); if(!m[i].visible) continue; //If the element is not visible, continue
				m[i].offset = m[i].element.offset();
				m[i].proportions = { width: m[i].element.outerWidth(), height: m[i].element.outerHeight() };
				
				if(type == "dragstart" || type == "sortactivate") m[i].activate.call(m[i], e); //Activate the droppable if used directly from draggables
			}
			
		},
		drop: function(draggable, e) {
			
			var dropped = false;
			$.each($.ui.ddmanager.droppables, function() {
				
				if(!this.options) return;
				if (!this.options.disabled && this.visible && $.ui.intersect(draggable, this, this.options.tolerance))
					dropped = this.drop.call(this, e);
				
				if (!this.options.disabled && this.visible && this.options.accept.call(this.element,(draggable.currentItem || draggable.element))) {
					this.isout = 1; this.isover = 0;
					this.deactivate.call(this, e);
				}
				
			});
			return dropped;
			
		},
		drag: function(draggable, e) {
			
			//If you have a highly dynamic page, you might try this option. It renders positions every time you move the mouse.
			if(draggable.options.refreshPositions) $.ui.ddmanager.prepareOffsets(draggable, e);
			
			//Run through all droppables and check their positions based on specific tolerance options
			$.each($.ui.ddmanager.droppables, function() {
				
				if(this.disabled || this.greedyChild || !this.visible) return;
				var intersects = $.ui.intersect(draggable, this, this.options.tolerance);
				
				var c = !intersects && this.isover == 1 ? 'isout' : (intersects && this.isover == 0 ? 'isover' : null);
				if(!c) return;
				
				var parentInstance;
				if (this.options.greedy) {
					var parent = this.element.parents('.ui-droppable:eq(0)');
					if (parent.length) {
						parentInstance = $.data(parent[0], 'droppable');
						parentInstance.greedyChild = (c == 'isover' ? 1 : 0);
					}
				}
				
				// we just moved into a greedy child
				if (parentInstance && c == 'isover') {
					parentInstance['isover'] = 0;
					parentInstance['isout'] = 1;
					parentInstance.out.call(parentInstance, e);
				}
				
				this[c] = 1; this[c == 'isout' ? 'isover' : 'isout'] = 0;
				this[c == "isover" ? "over" : "out"].call(this, e);
				
				// we just moved out of a greedy child
				if (parentInstance && c == 'isout') {
					parentInstance['isout'] = 0;
					parentInstance['isover'] = 1;
					parentInstance.over.call(parentInstance, e);
				}
			});
			
		}
	};
	
/*
 * Droppable Extensions
 */
	
	$.ui.plugin.add("droppable", "activeClass", {
		activate: function(e, ui) {
			$(this).addClass(ui.options.activeClass);
		},
		deactivate: function(e, ui) {
			$(this).removeClass(ui.options.activeClass);
		},
		drop: function(e, ui) {
			$(this).removeClass(ui.options.activeClass);
		}
	});
	
	$.ui.plugin.add("droppable", "hoverClass", {
		over: function(e, ui) {
			$(this).addClass(ui.options.hoverClass);
		},
		out: function(e, ui) {
			$(this).removeClass(ui.options.hoverClass);
		},
		drop: function(e, ui) {
			$(this).removeClass(ui.options.hoverClass);
		}
	});
 
})(jQuery);
/*
 * jQuery UI Resizable
 *
 * Copyright (c) 2008 Paul Bakaus
 * Dual licensed under the MIT (MIT-LICENSE.txt)
 * and GPL (GPL-LICENSE.txt) licenses.
 * 
 * http://docs.jquery.com/UI/Resizables
 *
 * Depends:
 *	ui.core.js
 *
 * Revision: $Id: jquery.ui.js,v 1.1 2008/05/14 15:34:12 m.moossen Exp $
 */
;(function($) {
	
	$.widget("ui.resizable", {
		init: function() {

			var self = this, o = this.options;
			
			
			var elpos = this.element.css('position'); // simulate .ui-resizable { position: relative; }
			this.element.addClass("ui-resizable").css({ position: /static/.test(elpos) ? 'relative' : elpos });
			
			$.extend(o, {
				_aspectRatio: !!(o.aspectRatio),
				proxy: o.proxy || o.ghost || o.animate ? 'proxy' : null,
				knobHandles: o.knobHandles === true ? 'ui-resizable-knob-handle' : o.knobHandles
			});

			//Default Theme
			var aBorder = '1px solid #DEDEDE';
	
			o.defaultTheme = {
				'ui-resizable': { display: 'block' },
				'ui-resizable-handle': { position: 'absolute', background: '#F2F2F2', fontSize: '0.1px' },
				'ui-resizable-n': { cursor: 'n-resize', height: '4px', left: '0px', right: '0px', borderTop: aBorder },
				'ui-resizable-s': { cursor: 's-resize', height: '4px', left: '0px', right: '0px', borderBottom: aBorder },
				'ui-resizable-e': { cursor: 'e-resize', width: '4px', top: '0px', bottom: '0px', borderRight: aBorder },
				'ui-resizable-w': { cursor: 'w-resize', width: '4px', top: '0px', bottom: '0px', borderLeft: aBorder },
				'ui-resizable-se': { cursor: 'se-resize', width: '4px', height: '4px', borderRight: aBorder, borderBottom: aBorder },
				'ui-resizable-sw': { cursor: 'sw-resize', width: '4px', height: '4px', borderBottom: aBorder, borderLeft: aBorder },
				'ui-resizable-ne': { cursor: 'ne-resize', width: '4px', height: '4px', borderRight: aBorder, borderTop: aBorder },
				'ui-resizable-nw': { cursor: 'nw-resize', width: '4px', height: '4px', borderLeft: aBorder, borderTop: aBorder }
			};
			
			o.knobTheme = {
				'ui-resizable-handle': { background: '#F2F2F2', border: '1px solid #808080', height: '8px', width: '8px' },
				'ui-resizable-n': { cursor: 'n-resize', top: '-4px', left: '45%' },
				'ui-resizable-s': { cursor: 's-resize', bottom: '-4px', left: '45%' },
				'ui-resizable-e': { cursor: 'e-resize', right: '-4px', top: '45%' },
				'ui-resizable-w': { cursor: 'w-resize', left: '-4px', top: '45%' },
				'ui-resizable-se': { cursor: 'se-resize', right: '-4px', bottom: '-4px' },
				'ui-resizable-sw': { cursor: 'sw-resize', left: '-4px', bottom: '-4px' },
				'ui-resizable-nw': { cursor: 'nw-resize', left: '-4px', top: '-4px' },
				'ui-resizable-ne': { cursor: 'ne-resize', right: '-4px', top: '-4px' }
			};
			
			o._nodeName = this.element[0].nodeName;
		
			//Wrap the element if it cannot hold child nodes
			if(o._nodeName.match(/textarea|input|select|button|img/i)) {
				var el = this.element;
				
				//Opera fixing relative position
				if (/relative/.test(el.css('position')) && $.browser.opera)
					el.css({ position: 'relative', top: 'auto', left: 'auto' });
				
				//Create a wrapper element and set the wrapper to the new current internal element
				el.wrap(
					$('<div class="ui-wrapper"	style="overflow: hidden;"></div>').css( {
						position: el.css('position'),
						width: el.outerWidth(),
						height: el.outerHeight(),
						top: el.css('top'),
						left: el.css('left')
					})
				);
				
				var oel = this.element; this.element = this.element.parent();
		
				//Move margins to the wrapper
				this.element.css({ marginLeft: oel.css("marginLeft"), marginTop: oel.css("marginTop"),
					marginRight: oel.css("marginRight"), marginBottom: oel.css("marginBottom")
				});
		
				oel.css({ marginLeft: 0, marginTop: 0, marginRight: 0, marginBottom: 0});
		
				//Prevent Safari textarea resize
				if ($.browser.safari && o.preventDefault) oel.css('resize', 'none');
		
				o.proportionallyResize = oel.css({ position: 'static', zoom: 1, display: 'block' });
				
				// avoid IE jump
				this.element.css({ margin: oel.css('margin') });
				
				// fix handlers offset
				this._proportionallyResize();
			}
			
			if(!o.handles) o.handles = !$('.ui-resizable-handle', this.element).length ? "e,s,se" : { n: '.ui-resizable-n', e: '.ui-resizable-e', s: '.ui-resizable-s', w: '.ui-resizable-w', se: '.ui-resizable-se', sw: '.ui-resizable-sw', ne: '.ui-resizable-ne', nw: '.ui-resizable-nw' };
			if(o.handles.constructor == String) {
		
				if(o.handles == 'all') o.handles = 'n,e,s,w,se,sw,ne,nw';
		
				var n = o.handles.split(","); o.handles = {};
		
				o.zIndex = o.zIndex || 1000;
				
				// insertions are applied when don't have theme loaded
				var insertionsDefault = {
					handle: 'position: absolute; display: none; overflow:hidden;',
					n: 'top: 0pt; width:100%;',
					e: 'right: 0pt; height:100%;',
					s: 'bottom: 0pt; width:100%;',
					w: 'left: 0pt; height:100%;',
					se: 'bottom: 0pt; right: 0px;',
					sw: 'bottom: 0pt; left: 0px;',
					ne: 'top: 0pt; right: 0px;',
					nw: 'top: 0pt; left: 0px;'
				};
		
				for(var i = 0; i < n.length; i++) {
					var handle = jQuery.trim(n[i]), dt = o.defaultTheme, hname = 'ui-resizable-'+handle, loadDefault = !$.ui.css(hname) && !o.knobHandles, userKnobClass = $.ui.css('ui-resizable-knob-handle'), 
								allDefTheme = $.extend(dt[hname], dt['ui-resizable-handle']), allKnobTheme = $.extend(o.knobTheme[hname], !userKnobClass ? o.knobTheme['ui-resizable-handle'] : {});
					
					// increase zIndex of sw, se, ne, nw axis
					var applyZIndex = /sw|se|ne|nw/.test(handle) ? { zIndex: ++o.zIndex } : {};
					
					var defCss = (loadDefault ? insertionsDefault[handle] : ''), 
						axis = $(['<div class="ui-resizable-handle ', hname, '" style="', defCss, insertionsDefault.handle, '"></div>'].join('')).css( applyZIndex );
					o.handles[handle] = '.ui-resizable-'+handle;
					
					this.element.append(
						//Theme detection, if not loaded, load o.defaultTheme
						axis.css( loadDefault ? allDefTheme : {} )
							// Load the knobHandle css, fix width, height, top, left...
							.css( o.knobHandles ? allKnobTheme : {} ).addClass(o.knobHandles ? 'ui-resizable-knob-handle' : '').addClass(o.knobHandles)
					);
				}
				
				if (o.knobHandles) this.element.addClass('ui-resizable-knob').css( !$.ui.css('ui-resizable-knob') ? { /*border: '1px #fff dashed'*/ } : {} );
			}

			this._renderAxis = function(target) {
				target = target || this.element;
		
				for(var i in o.handles) {
					if(o.handles[i].constructor == String) 
						o.handles[i] = $(o.handles[i], this.element).show();
		
					if (o.transparent)
						o.handles[i].css({opacity:0});
		
					//Apply pad to wrapper element, needed to fix axis position (textarea, inputs, scrolls)
					if (this.element.is('.ui-wrapper') && 
						o._nodeName.match(/textarea|input|select|button/i)) {
		
						var axis = $(o.handles[i], this.element), padWrapper = 0;
		
						//Checking the correct pad and border
						padWrapper = /sw|ne|nw|se|n|s/.test(i) ? axis.outerHeight() : axis.outerWidth();
		
						//The padding type i have to apply...
						var padPos = [ 'padding', 
							/ne|nw|n/.test(i) ? 'Top' :
							/se|sw|s/.test(i) ? 'Bottom' : 
							/^e$/.test(i) ? 'Right' : 'Left' ].join(""); 
		
						if (!o.transparent)
							target.css(padPos, padWrapper);
		
						this._proportionallyResize();
					}
					if(!$(o.handles[i]).length) continue;
				}
			};
			
			this._renderAxis(this.element);
			o._handles = $('.ui-resizable-handle', self.element);
			
			if (o.disableSelection)
				o._handles.each(function(i, e) { $.ui.disableSelection(e); });
			
			//Matching axis name
			o._handles.mouseover(function() {
				if (!o.resizing) {
					if (this.className) 
						var axis = this.className.match(/ui-resizable-(se|sw|ne|nw|n|e|s|w)/i);
					//Axis, default = se
					self.axis = o.axis = axis && axis[1] ? axis[1] : 'se';
				}
			});
					
			//If we want to auto hide the elements
			if (o.autohide) {
				o._handles.hide();
				$(self.element).addClass("ui-resizable-autohide").hover(function() {
					$(this).removeClass("ui-resizable-autohide");
					o._handles.show();
				},
				function(){
					if (!o.resizing) {
						$(this).addClass("ui-resizable-autohide");
						o._handles.hide();
					}
				});
			}
		
			//Initialize mouse events for interaction
			this.element.mouse({
				executor: this,
				delay: 0,
				distance: 0,
				dragPrevention: ['input','textarea','button','select','option'],
				start: this.start,
				stop: this.stop,
				drag: this.drag,
				condition: function(e) {
					if(this.disabled) return false;
					for(var i in this.options.handles) {
						if($(this.options.handles[i])[0] == e.target) return true;
					}
					return false;
				}
			});
			
		},
		plugins: {},
		ui: function() {
			return {
				instance: this,
				axis: this.options.axis,
				options: this.options
			};
		},
		_renderProxy: function() {
			var el = this.element, o = this.options;
			this.elementOffset = el.offset();
	
			if(o.proxy) {
				this.helper = this.helper || $('<div style="overflow:hidden;"></div>');
	
				// fix ie6 offset
				var ie6 = $.browser.msie && $.browser.version < 7, ie6offset = (ie6 ? 1 : 0),
				pxyoffset = ( ie6 ? 2 : -1 );
	
				this.helper.addClass(o.proxy).css({
					width: el.outerWidth() + pxyoffset,
					height: el.outerHeight() + pxyoffset,
					position: 'absolute',
					left: this.elementOffset.left - ie6offset +'px',
					top: this.elementOffset.top - ie6offset +'px',
					zIndex: ++o.zIndex
				});
				
				this.helper.appendTo("body");
	
				if (o.disableSelection)
					$.ui.disableSelection(this.helper.get(0));
	
			} else {
				this.helper = el; 
			}
		},
		propagate: function(n,e) {
			$.ui.plugin.call(this, n, [e, this.ui()]);
			this.element.triggerHandler(n == "resize" ? n : ["resize", n].join(""), [e, this.ui()], this.options[n]);
		},
		destroy: function() {
			var el = this.element, wrapped = el.children(".ui-resizable").get(0),
			
			_destroy = function(exp) {
				$(exp).removeClass("ui-resizable ui-resizable-disabled")
					.mouse("destroy").removeData("resizable").unbind(".resizable").find('.ui-resizable-handle').remove();
			};
			
			_destroy(el);
			
			if (el.is('.ui-wrapper') && wrapped) {
				el.parent().append(
					$(wrapped).css({
						position: el.css('position'),
						width: el.outerWidth(),
						height: el.outerHeight(),
						top: el.css('top'),
						left: el.css('left')
					})
				).end().remove();
				
				_destroy(wrapped);
			}
		},
		enable: function() {
			this.element.removeClass("ui-resizable-disabled");
			this.disabled = false;
		},
		disable: function() {
			this.element.addClass("ui-resizable-disabled");
			this.disabled = true;
		},
		start: function(e) {
			var o = this.options, iniPos = this.element.position(), el = this.element, 
				num = function(v) { return parseInt(v, 10) || 0; }, ie6 = $.browser.msie && $.browser.version < 7;
			o.resizing = true;
			o.documentScroll = { top: $(document).scrollTop(), left: $(document).scrollLeft() };
	
			// bugfix #1749
			if (el.is('.ui-draggable') || (/absolute/).test(el.css('position'))) {
				
				// sOffset decides if document scrollOffset will be added to the top/left of the resizable element
				var sOffset = $.browser.msie && !o.containment && (/absolute/).test(el.css('position')) && !(/relative/).test(el.parent().css('position'));
				var dscrollt = sOffset ? o.documentScroll.top : 0, dscrolll = sOffset ? o.documentScroll.left : 0;
				
				el.css({ position: 'absolute', top: (iniPos.top + dscrollt), left: (iniPos.left + dscrolll) });
			}
			
			//Opera fixing relative position
			if (/relative/.test(el.css('position')) && $.browser.opera)
				el.css({ position: 'relative', top: 'auto', left: 'auto' });
	
			this._renderProxy();
	
			var curleft = num(this.helper.css('left')), curtop = num(this.helper.css('top'));
			
			//Store needed variables
			this.offset = this.helper.offset();
			this.position = { left: curleft, top: curtop };
			this.size = o.proxy || ie6 ? { width: el.outerWidth(), height: el.outerHeight() } : { width: el.width(), height: el.height() };
			this.originalSize = o.proxy || ie6 ? { width: el.outerWidth(), height: el.outerHeight() } : { width: el.width(), height: el.height() };
			this.originalPosition = { left: curleft, top: curtop };
			this.sizeDiff = { width: el.outerWidth() - el.width(), height: el.outerHeight() - el.height() };
			this.originalMousePosition = { left: e.pageX, top: e.pageY };
			
			//Aspect Ratio
			o.aspectRatio = (typeof o.aspectRatio == 'number') ? o.aspectRatio : ((this.originalSize.height / this.originalSize.width)||1);
	
			if (o.preserveCursor)
				$('body').css('cursor', this.axis + '-resize');
				
			this.propagate("start", e);
			return false;
		},
		stop: function(e) {
			this.options.resizing = false;
			var o = this.options, num = function(v) { return parseInt(v, 10) || 0; }, self = this;
	
			if(o.proxy) {
				var pr = o.proportionallyResize, ista = pr && (/textarea/i).test(pr.get(0).nodeName), 
							soffseth = ista && $.ui.hasScroll(pr.get(0), 'left') /* TODO - jump height */ ? 0 : self.sizeDiff.height,
								soffsetw = ista ? 0 : self.sizeDiff.width;
			
				var style = { width: (self.size.width - soffsetw), height: (self.size.height - soffseth) },
						left = parseInt(self.element.css('left'), 10) + (self.position.left - self.originalPosition.left), 
							top = parseInt(self.element.css('top'), 10) + (self.position.top - self.originalPosition.top);
				
				if (!o.animate)
					this.element.css($.extend(style, { top: top, left: left }));
				
				if (o.proxy && !o.animate) this._proportionallyResize();
				this.helper.remove();
			}

			if (o.preserveCursor)
			$('body').css('cursor', 'auto');
	
			this.propagate("stop", e);	
			return false;
		},
		drag: function(e) {
			//Increase performance, avoid regex
			var el = this.helper, o = this.options, props = {},
				self = this, smp = this.originalMousePosition, a = this.axis;

			var dx = (e.pageX-smp.left)||0, dy = (e.pageY-smp.top)||0;
			var trigger = this.change[a];
			if (!trigger) return false;
			
			// Calculate the attrs that will be change
			var data = trigger.apply(this, [e, dx, dy]), ie6 = $.browser.msie && $.browser.version < 7, csdif = this.sizeDiff;
			
			if (o._aspectRatio || e.shiftKey)
				data = this._updateRatio(data, e);
			
			data = this._respectSize(data, e);
			
			this.propagate("resize", e);
			
			el.css({
				top: this.position.top + "px", left: this.position.left + "px", 
				width: this.size.width + "px", height: this.size.height + "px"
			});
			
			if (!o.proxy && o.proportionallyResize)
				this._proportionallyResize();
			
			this._updateCache(data);
			
			return false;
		},
		
		_updateCache: function(data) {
			var o = this.options;
			this.offset = this.helper.offset();
			if (data.left) this.position.left = data.left;
			if (data.top) this.position.top = data.top;
			if (data.height) this.size.height = data.height;
			if (data.width) this.size.width = data.width;
		},
		
		_updateRatio: function(data, e) {
			var o = this.options, cpos = this.position, csize = this.size, a = this.axis;
			
			if (data.height) data.width = Math.round(csize.height / o.aspectRatio);
			else if (data.width) data.height = Math.round(csize.width * o.aspectRatio);
			
			if (a == 'sw') {
				data.left = cpos.left + (csize.width - data.width);
				data.top = null;
			}
			if (a == 'nw') { 
				data.top = cpos.top + (csize.height - data.height);
				data.left = cpos.left + (csize.width - data.width);
			}
			
			return data;
		},
		
		_respectSize: function(data, e) {
			
			var el = this.helper, o = this.options, pRatio = o._aspectRatio || e.shiftKey, a = this.axis, 
					ismaxw = data.width && o.maxWidth && o.maxWidth < data.width, ismaxh = data.height && o.maxHeight && o.maxHeight < data.height,
						isminw = data.width && o.minWidth && o.minWidth > data.width, isminh = data.height && o.minHeight && o.minHeight > data.height;
			
			if (isminw) data.width = o.minWidth;
			if (isminh) data.height = o.minHeight;
			if (ismaxw) data.width = o.maxWidth;
			if (ismaxh) data.height = o.maxHeight;
			
			var dw = this.originalPosition.left + this.originalSize.width, dh = this.position.top + this.size.height;
			var cw = /sw|nw|w/.test(a), ch = /nw|ne|n/.test(a);
			
			if (isminw && cw) data.left = dw - o.minWidth;
			if (ismaxw && cw) data.left = dw - o.maxWidth;
			if (isminh && ch)	data.top = dh - o.minHeight;
			if (ismaxh && ch)	data.top = dh - o.maxHeight;
			
			// fixing jump error on top/left - bug #2330
			var isNotwh = !data.width && !data.height;
			if (isNotwh && !data.left && data.top) data.top = null;
			else if (isNotwh && !data.top && data.left) data.left = null;
			
			return data;
		},
		
		_proportionallyResize: function() {
			var o = this.options;
			if (!o.proportionallyResize) return;
			var prel = o.proportionallyResize, el = this.helper || this.element;
			
			if (!o.borderDif) {
				var b = [prel.css('borderTopWidth'), prel.css('borderRightWidth'), prel.css('borderBottomWidth'), prel.css('borderLeftWidth')],
					p = [prel.css('paddingTop'), prel.css('paddingRight'), prel.css('paddingBottom'), prel.css('paddingLeft')];
				
				o.borderDif = $.map(b, function(v, i) {
					var border = parseInt(v,10)||0, padding = parseInt(p[i],10)||0;
					return border + padding; 
				});
			}
			prel.css({
				height: (el.height() - o.borderDif[0] - o.borderDif[2]) + "px",
				width: (el.width() - o.borderDif[1] - o.borderDif[3]) + "px"
			});
		},
		
		change: {
			e: function(e, dx, dy) {
				return { width: this.originalSize.width + dx };
			},
			w: function(e, dx, dy) {
				var o = this.options, cs = this.originalSize, sp = this.originalPosition;
				return { left: sp.left + dx, width: cs.width - dx };
			},
			n: function(e, dx, dy) {
				var o = this.options, cs = this.originalSize, sp = this.originalPosition;
				return { top: sp.top + dy, height: cs.height - dy };
			},
			s: function(e, dx, dy) {
				return { height: this.originalSize.height + dy };
			},
			se: function(e, dx, dy) {
				return $.extend(this.change.s.apply(this, arguments), this.change.e.apply(this, [e, dx, dy]));
			},
			sw: function(e, dx, dy) {
				return $.extend(this.change.s.apply(this, arguments), this.change.w.apply(this, [e, dx, dy]));
			},
			ne: function(e, dx, dy) {
				return $.extend(this.change.n.apply(this, arguments), this.change.e.apply(this, [e, dx, dy]));
			},
			nw: function(e, dx, dy) {
				return $.extend(this.change.n.apply(this, arguments), this.change.w.apply(this, [e, dx, dy]));
			}
		}
	});
	
	$.extend($.ui.resizable, {
		defaults: {
			preventDefault: true,
			transparent: false,
			minWidth: 10,
			minHeight: 10,
			aspectRatio: false,
			disableSelection: true,
			preserveCursor: true,
			autohide: false,
			knobHandles: false
		}
	});

/*
 * Resizable Extensions
 */

	$.ui.plugin.add("resizable", "containment", {
		
		start: function(e, ui) {
			var o = ui.options, self = ui.instance, el = self.element;
			var oc = o.containment,	ce = (oc instanceof jQuery) ? oc.get(0) : (/parent/.test(oc)) ? el.parent().get(0) : oc;
			if (!ce) return;
			
			if (/document/.test(oc) || oc == document) {
				self.containerOffset = { left: 0, top: 0 };

				self.parentData = { 
					element: $(document), left: 0, top: 0, width: $(document).width(),
					height: $(document).height() || document.body.parentNode.scrollHeight
				};
			}
			
			// i'm a node, so compute top, left, right, bottom
			else{
				self.containerOffset = $(ce).offset();
				self.containerSize = { height: $(ce).innerHeight(), width: $(ce).innerWidth() };
			
				var co = self.containerOffset, ch = self.containerSize.height,	cw = self.containerSize.width, 
							width = ($.ui.hasScroll(ce, "left") ? ce.scrollWidth : cw ), height = ($.ui.hasScroll(ce) ? ce.scrollHeight : ch);
			
				self.parentData = { 
					element: ce, left: co.left, top: co.top, width: width, height: height
				};
			}
		},
		
		resize: function(e, ui) {
			var o = ui.options, self = ui.instance, ps = self.containerSize, 
						co = self.containerOffset, cs = self.size, cp = self.position,
							pRatio = o._aspectRatio || e.shiftKey;
			
			if (cp.left < (o.proxy ? co.left : 0)) {
				self.size.width = self.size.width + (o.proxy ? (self.position.left - co.left) : self.position.left);
				if (pRatio) self.size.height = self.size.width * o.aspectRatio;
				self.position.left = o.proxy ? co.left : 0;
			}
			
			if (cp.top < (o.proxy ? co.top : 0)) {
				self.size.height = self.size.height + (o.proxy ? (self.position.top - co.top) : self.position.top);
				if (pRatio) self.size.width = self.size.height / o.aspectRatio;
				self.position.top = o.proxy ? co.top : 0;
			}
			
			var woset = (o.proxy ? self.offset.left - co.left : self.position.left) + self.sizeDiff.width, 
						hoset = (o.proxy ? self.offset.top - co.top : self.position.top) + self.sizeDiff.height;
			
			if (woset + self.size.width >= self.parentData.width) {
				self.size.width = self.parentData.width - woset;
				if (pRatio) self.size.height = self.size.width * o.aspectRatio;
			}
			
			if (hoset + self.size.height >= self.parentData.height) {
				self.size.height = self.parentData.height - hoset;
				if (pRatio) self.size.width = self.size.height / o.aspectRatio;
			}
		}
	});
	
	$.ui.plugin.add("resizable", "grid", {
		
		resize: function(e, ui) {
			var o = ui.options, self = ui.instance, cs = self.size, os = self.originalSize, op = self.originalPosition, a = self.axis, ratio = o._aspectRatio || e.shiftKey;
			o.grid = typeof o.grid == "number" ? [o.grid, o.grid] : o.grid;
			var ox = Math.round((cs.width - os.width) / o.grid[0]) * o.grid[0], oy = Math.round((cs.height - os.height) / o.grid[1]) * o.grid[1];
			
			if (/^(se|s|e)$/.test(a)) {
				self.size.width = os.width + ox;
				self.size.height = os.height + oy;
			}
			else if (/^(ne)$/.test(a)) {
				self.size.width = os.width + ox;
				self.size.height = os.height + oy;
				self.position.top = op.top - oy;
			}
			else if (/^(sw)$/.test(a)) {
				self.size.width = os.width + ox;
				self.size.height = os.height + oy;
				self.position.left = op.left - ox;
			}
			else {
				self.size.width = os.width + ox;
				self.size.height = os.height + oy;
				self.position.top = op.top - oy;
				self.position.left = op.left - ox;
			}
		}
		
	});
	
	$.ui.plugin.add("resizable", "animate", {
		
		stop: function(e, ui) {
			var o = ui.options, self = ui.instance;

			var pr = o.proportionallyResize, ista = pr && (/textarea/i).test(pr.get(0).nodeName), 
							soffseth = ista && $.ui.hasScroll(pr.get(0), 'left') /* TODO - jump height */ ? 0 : self.sizeDiff.height,
								soffsetw = ista ? 0 : self.sizeDiff.width;
			
			var style = { width: (self.size.width - soffsetw), height: (self.size.height - soffseth) },
						left = parseInt(self.element.css('left'), 10) + (self.position.left - self.originalPosition.left), 
							top = parseInt(self.element.css('top'), 10) + (self.position.top - self.originalPosition.top); 
			
			self.element.animate(
				$.extend(style, { top: top, left: left }),
				{ 
					duration: o.animateDuration || "slow", 
					easing: o.animateEasing || "swing", 
					step: function() {
						if (pr) pr.css({ width: self.element.css('width'), height: self.element.css('height') });
					}
				}
			);
		}
		
	});
	
	$.ui.plugin.add("resizable", "ghost", {
		
		start: function(e, ui) {
			var o = ui.options, self = ui.instance, pr = o.proportionallyResize, cs = self.size;
			
			if (!pr) self.ghost = self.element.clone();
			else self.ghost = pr.clone();
			
			self.ghost.css(
				{ opacity: .25, display: 'block', position: 'relative', height: cs.height, width: cs.width, margin: 0, left: 0, top: 0 }
			)
			.addClass('ui-resizable-ghost').addClass(typeof o.ghost == 'string' ? o.ghost : '');
			
			self.ghost.appendTo(self.helper);
			
		},
		
		resize: function(e, ui){
			var o = ui.options, self = ui.instance, pr = o.proportionallyResize;
			
			if (self.ghost) self.ghost.css({ position: 'relative', height: self.size.height, width: self.size.width });
			
		},
		
		stop: function(e, ui){
			var o = ui.options, self = ui.instance, pr = o.proportionallyResize;
			if (self.ghost && self.helper) self.helper.get(0).removeChild(self.ghost.get(0));
		}
		
	});

})(jQuery);
/*
 * jQuery UI Selectable
 *
 * Copyright (c) 2008 Richard D. Worth (rdworth.org)
 * Dual licensed under the MIT (MIT-LICENSE.txt)
 * and GPL (GPL-LICENSE.txt) licenses.
 * 
 * http://docs.jquery.com/UI/Selectables
 *
 * Depends:
 *	ui.core.js
 *
 * Revision: $Id: jquery.ui.js,v 1.1 2008/05/14 15:34:12 m.moossen Exp $
 */
;(function($) {

	$.widget("ui.selectable", {
		init: function() {
			var instance = this;
			
			this.element.addClass("ui-selectable");
			
			this.element.bind("setData.selectable", function(event, key, value){
				instance.options[key] = value;
			}).bind("getData.selectable", function(event, key){
				return instance.options[key];
			});
			
			this.dragged = false;
	
			// cache selectee children based on filter
			var selectees;
			this.refresh = function() {
				selectees = $(instance.options.filter, instance.element[0]);
				selectees.each(function() {
					var $this = $(this);
					var pos = $this.offset();
					$.data(this, "selectable-item", {
						element: this,
						$element: $this,
						left: pos.left,
						top: pos.top,
						right: pos.left + $this.width(),
						bottom: pos.top + $this.height(),
						startselected: false,
						selected: $this.hasClass('ui-selected'),
						selecting: $this.hasClass('ui-selecting'),
						unselecting: $this.hasClass('ui-unselecting')
					});
				});
			};
			this.refresh();
	
			this.selectees = selectees.addClass("ui-selectee");
	
			//Initialize mouse interaction
			this.element.mouse({
				executor: this,
				appendTo: 'body',
				delay: 0,
				distance: 0,
				dragPrevention: ['input','textarea','button','select','option'],
				start: this.start,
				stop: this.stop,
				drag: this.drag,
				condition: function(e) {
					var isSelectee = false;
					$(e.target).parents().andSelf().each(function() {
						if($.data(this, "selectable-item")) isSelectee = true;
					});
					return this.options.keyboard ? !isSelectee : true;
				}
			});
			
			this.helper = $(document.createElement('div')).css({border:'1px dotted black'});
		},
		toggle: function() {
			if(this.disabled){
				this.enable();
			} else {
				this.disable();
			}
		},
		destroy: function() {
			this.element
				.removeClass("ui-selectable ui-selectable-disabled")
				.removeData("selectable")
				.unbind(".selectable")
				.mouse("destroy");
		},
		enable: function() {
			this.element.removeClass("ui-selectable-disabled");
			this.disabled = false;
		},
		disable: function() {
			this.element.addClass("ui-selectable-disabled");
			this.disabled = true;
		},
		start: function(ev, element) {
			
			this.opos = [ev.pageX, ev.pageY];
			
			if (this.disabled)
				return;

			var options = this.options;

			this.selectees = $(options.filter, element);

			// selectable START callback
			this.element.triggerHandler("selectablestart", [ev, {
				"selectable": element,
				"options": options
			}], options.start);

			$('body').append(this.helper);
			// position helper (lasso)
			this.helper.css({
				"z-index": 100,
				"position": "absolute",
				"left": ev.clientX,
				"top": ev.clientY,
				"width": 0,
				"height": 0
			});

			if (options.autoRefresh) {
				this.refresh();
			}

			this.selectees.filter('.ui-selected').each(function() {
				var selectee = $.data(this, "selectable-item");
				selectee.startselected = true;
				if (!ev.ctrlKey) {
					selectee.$element.removeClass('ui-selected');
					selectee.selected = false;
					selectee.$element.addClass('ui-unselecting');
					selectee.unselecting = true;
					// selectable UNSELECTING callback
					$(this.element).triggerHandler("selectableunselecting", [ev, {
						selectable: element,
						unselecting: selectee.element,
						options: options
					}], options.unselecting);
				}
			});
		},
		drag: function(ev, element) {
			this.dragged = true;
			
			if (this.disabled)
				return;

			var options = this.options;

			var x1 = this.opos[0], y1 = this.opos[1], x2 = ev.pageX, y2 = ev.pageY;
			if (x1 > x2) { var tmp = x2; x2 = x1; x1 = tmp; }
			if (y1 > y2) { var tmp = y2; y2 = y1; y1 = tmp; }
			this.helper.css({left: x1, top: y1, width: x2-x1, height: y2-y1});

			this.selectees.each(function() {
				var selectee = $.data(this, "selectable-item");
				//prevent helper from being selected if appendTo: selectable
				if (!selectee || selectee.element == element)
					return;
				var hit = false;
				if (options.tolerance == 'touch') {
					hit = ( !(selectee.left > x2 || selectee.right < x1 || selectee.top > y2 || selectee.bottom < y1) );
				} else if (options.tolerance == 'fit') {
					hit = (selectee.left > x1 && selectee.right < x2 && selectee.top > y1 && selectee.bottom < y2);
				}

				if (hit) {
					// SELECT
					if (selectee.selected) {
						selectee.$element.removeClass('ui-selected');
						selectee.selected = false;
					}
					if (selectee.unselecting) {
						selectee.$element.removeClass('ui-unselecting');
						selectee.unselecting = false;
					}
					if (!selectee.selecting) {
						selectee.$element.addClass('ui-selecting');
						selectee.selecting = true;
						// selectable SELECTING callback
						$(this.element).triggerHandler("selectableselecting", [ev, {
							selectable: element,
							selecting: selectee.element,
							options: options
						}], options.selecting);
					}
				} else {
					// UNSELECT
					if (selectee.selecting) {
						if (ev.ctrlKey && selectee.startselected) {
							selectee.$element.removeClass('ui-selecting');
							selectee.selecting = false;
							selectee.$element.addClass('ui-selected');
							selectee.selected = true;
						} else {
							selectee.$element.removeClass('ui-selecting');
							selectee.selecting = false;
							if (selectee.startselected) {
								selectee.$element.addClass('ui-unselecting');
								selectee.unselecting = true;
							}
							// selectable UNSELECTING callback
							$(this.element).triggerHandler("selectableunselecting", [ev, {
								selectable: element,
								unselecting: selectee.element,
								options: options
							}], options.unselecting);
						}
					}
					if (selectee.selected) {
						if (!ev.ctrlKey && !selectee.startselected) {
							selectee.$element.removeClass('ui-selected');
							selectee.selected = false;

							selectee.$element.addClass('ui-unselecting');
							selectee.unselecting = true;
							// selectable UNSELECTING callback
							$(this.element).triggerHandler("selectableunselecting", [ev, {
								selectable: element,
								unselecting: selectee.element,
								options: options
							}], options.unselecting);
						}
					}
				}
			});
		},
		stop: function(ev, element) {
			this.dragged = false;
			
			var options = this.options;

			$('.ui-unselecting', this.element).each(function() {
				var selectee = $.data(this, "selectable-item");
				selectee.$element.removeClass('ui-unselecting');
				selectee.unselecting = false;
				selectee.startselected = false;
				$(this.element).triggerHandler("selectableunselected", [ev, {
					selectable: element,
					unselected: selectee.element,
					options: options
				}], options.unselected);
			});
			$('.ui-selecting', this.element).each(function() {
				var selectee = $.data(this, "selectable-item");
				selectee.$element.removeClass('ui-selecting').addClass('ui-selected');
				selectee.selecting = false;
				selectee.selected = true;
				selectee.startselected = true;
				$(this.element).triggerHandler("selectableselected", [ev, {
					selectable: element,
					selected: selectee.element,
					options: options
				}], options.selected);
			});
			$(this.element).triggerHandler("selectablestop", [ev, {
				selectable: element,
				options: this.options
			}], this.options.stop);
			
			this.helper.remove();
		}
	});
	
	$.ui.selectable.defaults = {
		appendTo: 'body',
		autoRefresh: true,
		filter: '*',
		tolerance: 'touch'
	};
	
})(jQuery);
/*
 * jQuery UI Sortable
 *
 * Copyright (c) 2008 Paul Bakaus
 * Dual licensed under the MIT (MIT-LICENSE.txt)
 * and GPL (GPL-LICENSE.txt) licenses.
 * 
 * http://docs.jquery.com/UI/Sortables
 *
 * Depends:
 *	ui.core.js
 *
 * Revision: $Id: jquery.ui.js,v 1.1 2008/05/14 15:34:12 m.moossen Exp $
 */
;(function($) {
	
	function contains(a, b) { 
	    var safari2 = $.browser.safari && $.browser.version < 522; 
	    if (a.contains && !safari2) { 
	        return a.contains(b); 
	    } 
	    if (a.compareDocumentPosition) 
	        return !!(a.compareDocumentPosition(b) & 16); 
	    while (b = b.parentNode) 
	          if (b == a) return true; 
	    return false; 
	};
	
	$.widget("ui.sortable", {
		init: function() {

			var o = this.options;
			this.containerCache = {};
			this.element.addClass("ui-sortable");
		
			//Get the items
			this.refresh();
	
			//Let's determine if the items are floating
			this.floating = this.items.length ? (/left|right/).test(this.items[0].item.css('float')) : false;
			
			//Let's determine the parent's offset
			if(!(/(relative|absolute|fixed)/).test(this.element.css('position'))) this.element.css('position', 'relative');
			this.offset = this.element.offset();
	
			//Initialize mouse events for interaction
			this.element.mouse({
				executor: this,
				delay: o.delay,
				distance: o.distance || 1,
				dragPrevention: o.prevention ? o.prevention.toLowerCase().split(',') : ['input','textarea','button','select','option'],
				start: this.start,
				stop: this.stop,
				drag: this.drag,
				condition: function(e) {
	
					if(this.options.disabled || this.options.type == 'static') return false;
	
					//Find out if the clicked node (or one of its parents) is a actual item in this.items
					var currentItem = null, nodes = $(e.target).parents().each(function() {	
						if($.data(this, 'sortable-item')) {
							currentItem = $(this);
							return false;
						}
					});
					if($.data(e.target, 'sortable-item')) currentItem = $(e.target);
					
					if(!currentItem) return false;	
					if(this.options.handle) {
						var validHandle = false;
						$(this.options.handle, currentItem).each(function() { if(this == e.target) validHandle = true; });
						if(!validHandle) return false;
					}
						
					this.currentItem = currentItem;
					return true;
	
				}
			});
			
		},
		plugins: {},
		ui: function(inst) {
			return {
				helper: (inst || this)["helper"],
				placeholder: (inst || this)["placeholder"] || $([]),
				position: (inst || this)["position"].current,
				absolutePosition: (inst || this)["position"].absolute,
				instance: this,
				options: this.options,
				element: this.element,
				item: (inst || this)["currentItem"],
				sender: inst ? inst.element : null
			};		
		},
		propagate: function(n,e,inst) {
			$.ui.plugin.call(this, n, [e, this.ui(inst)]);
			this.element.triggerHandler(n == "sort" ? n : "sort"+n, [e, this.ui(inst)], this.options[n]);
		},
		serialize: function(o) {
			
			var items = $(this.options.items, this.element).not('.ui-sortable-helper'); //Only the items of the sortable itself
			var str = []; o = o || {};
			
			items.each(function() {
				var res = ($(this).attr(o.attribute || 'id') || '').match(o.expression || (/(.+)[-=_](.+)/));
				if(res) str.push((o.key || res[1])+'[]='+(o.key ? res[1] : res[2]));
			});
			
			return str.join('&');
			
		},
		toArray: function(attr) {
			var items = $(this.options.items, this.element).not('.ui-sortable-helper'); //Only the items of the sortable itself
			var ret = [];

			items.each(function() { ret.push($(this).attr(attr || 'id')); });
			return ret;
		},
		enable: function() {
			this.element.removeClass("ui-sortable-disabled");
			this.options.disabled = false;
		},
		disable: function() {
			this.element.addClass("ui-sortable-disabled");
			this.options.disabled = true;
		},
		/* Be careful with the following core functions */
		intersectsWith: function(item) {
			
			var x1 = this.position.absolute.left, x2 = x1 + this.helperProportions.width,
			y1 = this.position.absolute.top, y2 = y1 + this.helperProportions.height;
			var l = item.left, r = l + item.width, 
			t = item.top, b = t + item.height;

			if(this.options.tolerance == "pointer") {
				return (y1 + this.clickOffset.top > t && y1 + this.clickOffset.top < b && x1 + this.clickOffset.left > l && x1 + this.clickOffset.left < r);
			} else {
			
				return (l < x1 + (this.helperProportions.width / 2) // Right Half
					&& x2 - (this.helperProportions.width / 2) < r // Left Half
					&& t < y1 + (this.helperProportions.height / 2) // Bottom Half
					&& y2 - (this.helperProportions.height / 2) < b ); // Top Half
			
			}
			
		},
		intersectsWithEdge: function(item) {	
			var x1 = this.position.absolute.left, x2 = x1 + this.helperProportions.width,
				y1 = this.position.absolute.top, y2 = y1 + this.helperProportions.height;
			var l = item.left, r = l + item.width, 
				t = item.top, b = t + item.height;

			if(this.options.tolerance == "pointer") {

				if(!(y1 + this.clickOffset.top > t && y1 + this.clickOffset.top < b && x1 + this.clickOffset.left > l && x1 + this.clickOffset.left < r)) return false;
				
				if(this.floating) {
					if(x1 + this.clickOffset.left > l && x1 + this.clickOffset.left < l + item.width/2) return 2;
					if(x1 + this.clickOffset.left > l+item.width/2 && x1 + this.clickOffset.left < r) return 1;
				} else {
					if(y1 + this.clickOffset.top > t && y1 + this.clickOffset.top < t + item.height/2) return 2;
					if(y1 + this.clickOffset.top > t+item.height/2 && y1 + this.clickOffset.top < b) return 1;
				}

			} else {
			
				if (!(l < x1 + (this.helperProportions.width / 2) // Right Half
					&& x2 - (this.helperProportions.width / 2) < r // Left Half
					&& t < y1 + (this.helperProportions.height / 2) // Bottom Half
					&& y2 - (this.helperProportions.height / 2) < b )) return false; // Top Half
				
				if(this.floating) {
					if(x2 > l && x1 < l) return 2; //Crosses left edge
					if(x1 < r && x2 > r) return 1; //Crosses right edge
				} else {
					if(y2 > t && y1 < t) return 1; //Crosses top edge
					if(y1 < b && y2 > b) return 2; //Crosses bottom edge
				}
			
			}
			
			return false;
			
		},
		//This method checks approximately if the item is dragged in a container, but doesn't touch any items
		inEmptyZone: function(container) {

			if(!$(container.options.items, container.element).length) {
				return container.options.dropOnEmpty ? true : false;
			};

			var last = $(container.options.items, container.element).not('.ui-sortable-helper'); last = $(last[last.length-1]);
			var top = last.offset()[this.floating ? 'left' : 'top'] + last[0][this.floating ? 'offsetWidth' : 'offsetHeight'];
			return (this.position.absolute[this.floating ? 'left' : 'top'] > top);
		},
		refresh: function() {
			this.refreshItems();
			this.refreshPositions();
		},
		refreshItems: function() {
			
			this.items = [];
			this.containers = [this];
			var items = this.items;
			var queries = [$(this.options.items, this.element)];
			
			if(this.options.connectWith) {
				for (var i = this.options.connectWith.length - 1; i >= 0; i--){
					var cur = $(this.options.connectWith[i]);
					for (var j = cur.length - 1; j >= 0; j--){
						var inst = $.data(cur[j], 'sortable');
						if(inst && !inst.options.disabled) {
							queries.push($(inst.options.items, inst.element));
							this.containers.push(inst);
						}
					};
				};
			}

			for (var i = queries.length - 1; i >= 0; i--){
				queries[i].each(function() {
					$.data(this, 'sortable-item', true); // Data for target checking (mouse manager)
					items.push({
						item: $(this),
						width: 0, height: 0,
						left: 0, top: 0
					});
				});
			};

		},
		refreshPositions: function(fast) {
			for (var i = this.items.length - 1; i >= 0; i--){
				var t = this.items[i].item;
				if(!fast) this.items[i].width = (this.options.toleranceElement ? $(this.options.toleranceElement, t) : t).outerWidth();
				if(!fast) this.items[i].height = (this.options.toleranceElement ? $(this.options.toleranceElement, t) : t).outerHeight();
				var p = (this.options.toleranceElement ? $(this.options.toleranceElement, t) : t).offset();
				this.items[i].left = p.left;
				this.items[i].top = p.top;
			};
			for (var i = this.containers.length - 1; i >= 0; i--){
				var p =this.containers[i].element.offset();
				this.containers[i].containerCache.left = p.left;
				this.containers[i].containerCache.top = p.top;
				this.containers[i].containerCache.width	= this.containers[i].element.outerWidth();
				this.containers[i].containerCache.height = this.containers[i].element.outerHeight();
			};
		},
		destroy: function() {
			this.element
				.removeClass("ui-sortable ui-sortable-disabled")
				.removeData("sortable")
				.unbind(".sortable")
				.mouse("destroy");
			
			for ( var i = this.items.length - 1; i >= 0; i-- )
				this.items[i].item.removeData("sortable-item");
		},
		createPlaceholder: function(that) {
			(that || this).placeholderElement = this.options.placeholderElement ? $(this.options.placeholderElement, (that || this).currentItem) : (that || this).currentItem;
			(that || this).placeholder = $('<div></div>')
				.addClass(this.options.placeholder)
				.appendTo('body')
				.css({ position: 'absolute' })
				.css((that || this).placeholderElement.offset())
				.css({ width: (that || this).placeholderElement.outerWidth(), height: (that || this).placeholderElement.outerHeight() })
				;
		},
		contactContainers: function(e) {
			for (var i = this.containers.length - 1; i >= 0; i--){

				if(this.intersectsWith(this.containers[i].containerCache)) {
					if(!this.containers[i].containerCache.over) {
						

						if(this.currentContainer != this.containers[i]) {
							
							//When entering a new container, we will find the item with the least distance and append our item near it
							var dist = 10000; var itemWithLeastDistance = null; var base = this.position.absolute[this.containers[i].floating ? 'left' : 'top'];
							for (var j = this.items.length - 1; j >= 0; j--) {
								if(!contains(this.containers[i].element[0], this.items[j].item[0])) continue;
								var cur = this.items[j][this.containers[i].floating ? 'left' : 'top'];
								if(Math.abs(cur - base) < dist) {
									dist = Math.abs(cur - base); itemWithLeastDistance = this.items[j];
								}
							}
							
							//We also need to exchange the placeholder
							if(this.placeholder) this.placeholder.remove();
							if(this.containers[i].options.placeholder) {
								this.containers[i].createPlaceholder(this);
							} else {
								this.placeholder = null; this.placeholderElement = null;
							}
							
							
							itemWithLeastDistance ? this.rearrange(e, itemWithLeastDistance) : this.rearrange(e, null, this.containers[i].element);
							this.propagate("change", e); //Call plugins and callbacks
							this.containers[i].propagate("change", e, this); //Call plugins and callbacks
							this.currentContainer = this.containers[i];

						}
						
						this.containers[i].propagate("over", e, this);
						this.containers[i].containerCache.over = 1;
					}
				} else {
					if(this.containers[i].containerCache.over) {
						this.containers[i].propagate("out", e, this);
						this.containers[i].containerCache.over = 0;
					}
				}
				
			};			
		},
		start: function(e,el) {
		
			var o = this.options;
			this.currentContainer = this;
			this.refresh();

			//Create and append the visible helper
			this.helper = typeof o.helper == 'function' ? $(o.helper.apply(this.element[0], [e, this.currentItem])) : this.currentItem.clone();
			if(!this.helper.parents('body').length) this.helper.appendTo(o.appendTo || this.currentItem[0].parentNode); //Add the helper to the DOM if that didn't happen already
			this.helper.css({ position: 'absolute', clear: 'both' }).addClass('ui-sortable-helper'); //Position it absolutely and add a helper class
			
			//Prepare variables for position generation
			$.extend(this, {
				offsetParent: this.helper.offsetParent(),
				offsets: {
					absolute: this.currentItem.offset()
				},
				mouse: {
					start: { top: e.pageY, left: e.pageX }
				},
				margins: {
					top: parseInt(this.currentItem.css("marginTop")) || 0,
					left: parseInt(this.currentItem.css("marginLeft")) || 0
				}
			});
			
			//The relative click offset
			this.offsets.parent = this.offsetParent.offset();
			this.clickOffset = { left: e.pageX - this.offsets.absolute.left, top: e.pageY - this.offsets.absolute.top };
			
			this.originalPosition = {
				left: this.offsets.absolute.left - this.offsets.parent.left - this.margins.left,
				top: this.offsets.absolute.top - this.offsets.parent.top - this.margins.top
			}
			
			//Generate a flexible offset that will later be subtracted from e.pageX/Y
			//I hate margins - they need to be removed before positioning the element absolutely..
			this.offset = {
				left: e.pageX - this.originalPosition.left,
				top: e.pageY - this.originalPosition.top
			};

			//Save the first time position
			$.extend(this, {
				position: {
					current: { top: e.pageY - this.offset.top, left: e.pageX - this.offset.left },
					absolute: { left: e.pageX - this.clickOffset.left, top: e.pageY - this.clickOffset.top },
					dom: this.currentItem.prev()[0]
				}
			});

			//If o.placeholder is used, create a new element at the given position with the class
			if(o.placeholder) this.createPlaceholder();

			this.propagate("start", e); //Call plugins and callbacks
			this.helperProportions = { width: this.helper.outerWidth(), height: this.helper.outerHeight() }; //Save and store the helper proportions

			//If we have something in cursorAt, we'll use it
			if(o.cursorAt) {
				if(o.cursorAt.top != undefined || o.cursorAt.bottom != undefined) {
					this.offset.top -= this.clickOffset.top - (o.cursorAt.top != undefined ? o.cursorAt.top : (this.helperProportions.height - o.cursorAt.bottom));
					this.clickOffset.top = (o.cursorAt.top != undefined ? o.cursorAt.top : (this.helperProportions.height - o.cursorAt.bottom));
				}
				if(o.cursorAt.left != undefined || o.cursorAt.right != undefined) {
					this.offset.left -= this.clickOffset.left - (o.cursorAt.left != undefined ? o.cursorAt.left : (this.helperProportions.width - o.cursorAt.right));
					this.clickOffset.left = (o.cursorAt.left != undefined ? o.cursorAt.left : (this.helperProportions.width - o.cursorAt.right));
				}
			}

			if(this.options.placeholder != 'clone') $(this.currentItem).css('visibility', 'hidden'); //Set the original element visibility to hidden to still fill out the white space
			for (var i = this.containers.length - 1; i >= 0; i--) { this.containers[i].propagate("activate", e, this); } //Post 'activate' events to possible containers
			
			//Prepare possible droppables
			if($.ui.ddmanager) $.ui.ddmanager.current = this;
			if ($.ui.ddmanager && !o.dropBehaviour) $.ui.ddmanager.prepareOffsets(this, e);

			this.dragging = true;
			return false;
			
		},
		stop: function(e) {

			this.propagate("stop", e); //Call plugins and trigger callbacks
			if(this.position.dom != this.currentItem.prev()[0]) this.propagate("update", e); //Trigger update callback if the DOM position has changed
			if(!contains(this.element[0], this.currentItem[0])) { //Node was moved out of the current element
				this.propagate("remove", e);
				for (var i = this.containers.length - 1; i >= 0; i--){
					if(contains(this.containers[i].element[0], this.currentItem[0])) {
						this.containers[i].propagate("update", e, this);
						this.containers[i].propagate("receive", e, this);
					}
				};
			};
			
			//Post events to containers
			for (var i = this.containers.length - 1; i >= 0; i--){
				this.containers[i].propagate("deactivate", e, this);
				if(this.containers[i].containerCache.over) {
					this.containers[i].propagate("out", e, this);
					this.containers[i].containerCache.over = 0;
				}
			}
			
			//If we are using droppables, inform the manager about the drop
			if ($.ui.ddmanager && !this.options.dropBehaviour) $.ui.ddmanager.drop(this, e);
			
			this.dragging = false;
			if(this.cancelHelperRemoval) return false;
			$(this.currentItem).css('visibility', '');
			if(this.placeholder) this.placeholder.remove();
			this.helper.remove();

			return false;
			
		},
		drag: function(e) {

			//Compute the helpers position
			this.position.current = { top: e.pageY - this.offset.top, left: e.pageX - this.offset.left };
			this.position.absolute = { left: e.pageX - this.clickOffset.left, top: e.pageY - this.clickOffset.top };

			//Rearrange
			for (var i = this.items.length - 1; i >= 0; i--) {
				var intersection = this.intersectsWithEdge(this.items[i]);
				if(!intersection) continue;
				
				if(this.items[i].item[0] != this.currentItem[0] //cannot intersect with itself
					&&	this.currentItem[intersection == 1 ? "next" : "prev"]()[0] != this.items[i].item[0] //no useless actions that have been done before
					&&	!contains(this.currentItem[0], this.items[i].item[0]) //no action if the item moved is the parent of the item checked
					&& (this.options.type == 'semi-dynamic' ? !contains(this.element[0], this.items[i].item[0]) : true)
				) {
					
					this.direction = intersection == 1 ? "down" : "up";
					this.rearrange(e, this.items[i]);
					this.propagate("change", e); //Call plugins and callbacks
					break;
				}
			}
			
			//Post events to containers
			this.contactContainers(e);
			
			//Interconnect with droppables
			if($.ui.ddmanager) $.ui.ddmanager.drag(this, e);

			this.propagate("sort", e); //Call plugins and callbacks
			this.helper.css({ left: this.position.current.left+'px', top: this.position.current.top+'px' }); // Stick the helper to the cursor
			return false;
			
		},
		rearrange: function(e, i, a) {
			a ? a.append(this.currentItem) : i.item[this.direction == 'down' ? 'before' : 'after'](this.currentItem);
			this.refreshPositions(true); //Precompute after each DOM insertion, NOT on mousemove
			if(this.placeholderElement) this.placeholder.css(this.placeholderElement.offset());
			if(this.placeholderElement && this.placeholderElement.is(":visible")) this.placeholder.css({ width: this.placeholderElement.outerWidth(), height: this.placeholderElement.outerHeight() });
		}
	});
	
	$.extend($.ui.sortable, {
		getter: "serialize toArray",
		defaults: {
			items: '> *',
			zIndex: 1000
		}
	});

	
/*
 * Sortable Extensions
 */

	$.ui.plugin.add("sortable", "cursor", {
		start: function(e, ui) {
			var t = $('body');
			if (t.css("cursor")) ui.options._cursor = t.css("cursor");
			t.css("cursor", ui.options.cursor);
		},
		stop: function(e, ui) {
			if (ui.options._cursor) $('body').css("cursor", ui.options._cursor);
		}
	});

	$.ui.plugin.add("sortable", "zIndex", {
		start: function(e, ui) {
			var t = ui.helper;
			if(t.css("zIndex")) ui.options._zIndex = t.css("zIndex");
			t.css('zIndex', ui.options.zIndex);
		},
		stop: function(e, ui) {
			if(ui.options._zIndex) $(ui.helper).css('zIndex', ui.options._zIndex);
		}
	});

	$.ui.plugin.add("sortable", "opacity", {
		start: function(e, ui) {
			var t = ui.helper;
			if(t.css("opacity")) ui.options._opacity = t.css("opacity");
			t.css('opacity', ui.options.opacity);
		},
		stop: function(e, ui) {
			if(ui.options._opacity) $(ui.helper).css('opacity', ui.options._opacity);
		}
	});


	$.ui.plugin.add("sortable", "revert", {
		stop: function(e, ui) {
			var self = ui.instance;
			self.cancelHelperRemoval = true;
			var cur = self.currentItem.offset();
			var op = self.helper.offsetParent().offset();
			if(ui.instance.options.zIndex) ui.helper.css('zIndex', ui.instance.options.zIndex); //Do the zIndex again because it already was resetted by the plugin above on stop

			//Also animate the placeholder if we have one
			if(ui.instance.placeholder) ui.instance.placeholder.animate({ opacity: 'hide' }, parseInt(ui.options.revert, 10) || 500);
			
			
			ui.helper.animate({
				left: cur.left - op.left - self.margins.left,
				top: cur.top - op.top - self.margins.top
			}, parseInt(ui.options.revert, 10) || 500, function() {
				self.currentItem.css('visibility', 'visible');
				window.setTimeout(function() {
					if(self.placeholder) self.placeholder.remove();
					self.helper.remove();
					if(ui.options._zIndex) ui.helper.css('zIndex', ui.options._zIndex);
				}, 50);
			});
		}
	});

	
	$.ui.plugin.add("sortable", "containment", {
		start: function(e, ui) {

			var o = ui.options;
			if((o.containment.left != undefined || o.containment.constructor == Array) && !o._containment) return;
			if(!o._containment) o._containment = o.containment;

			if(o._containment == 'parent') o._containment = this[0].parentNode;
			if(o._containment == 'sortable') o._containment = this[0];
			if(o._containment == 'document') {
				o.containment = [
					0,
					0,
					$(document).width(),
					($(document).height() || document.body.parentNode.scrollHeight)
				];
			} else { //I'm a node, so compute top/left/right/bottom

				var ce = $(o._containment);
				var co = ce.offset();

				o.containment = [
					co.left,
					co.top,
					co.left+(ce.outerWidth() || ce[0].scrollWidth),
					co.top+(ce.outerHeight() || ce[0].scrollHeight)
				];
			}

		},
		sort: function(e, ui) {

			var o = ui.options;
			var h = ui.helper;
			var c = o.containment;
			var self = ui.instance;
			var borderLeft = (parseInt(self.offsetParent.css("borderLeftWidth"), 10) || 0);
			var borderRight = (parseInt(self.offsetParent.css("borderRightWidth"), 10) || 0);
			var borderTop = (parseInt(self.offsetParent.css("borderTopWidth"), 10) || 0);
			var borderBottom = (parseInt(self.offsetParent.css("borderBottomWidth"), 10) || 0);
			
			if(c.constructor == Array) {
				if((self.position.absolute.left < c[0])) self.position.current.left = c[0] - self.offsets.parent.left - self.margins.left;
				if((self.position.absolute.top < c[1])) self.position.current.top = c[1] - self.offsets.parent.top - self.margins.top;
				if(self.position.absolute.left - c[2] + self.helperProportions.width >= 0) self.position.current.left = c[2] - self.offsets.parent.left - self.helperProportions.width - self.margins.left - borderLeft - borderRight;
				if(self.position.absolute.top - c[3] + self.helperProportions.height >= 0) self.position.current.top = c[3] - self.offsets.parent.top - self.helperProportions.height - self.margins.top - borderTop - borderBottom;
			} else {
				if((ui.position.left < c.left)) self.position.current.left = c.left;
				if((ui.position.top < c.top)) self.position.current.top = c.top;
				if(ui.position.left - self.offsetParent.innerWidth() + self.helperProportions.width + c.right + borderLeft + borderRight >= 0) self.position.current.left = self.offsetParent.innerWidth() - self.helperProportions.width - c.right - borderLeft - borderRight;
				if(ui.position.top - self.offsetParent.innerHeight() + self.helperProportions.height + c.bottom + borderTop + borderBottom >= 0) self.position.current.top = self.offsetParent.innerHeight() - self.helperProportions.height - c.bottom - borderTop - borderBottom;
			}

		}
	});

	$.ui.plugin.add("sortable", "axis", {
		sort: function(e, ui) {
			var o = ui.options;
			if(o.constraint) o.axis = o.constraint; //Legacy check
			o.axis == 'x' ? ui.instance.position.current.top = ui.instance.originalPosition.top : ui.instance.position.current.left = ui.instance.originalPosition.left;
		}
	});

	$.ui.plugin.add("sortable", "scroll", {
		start: function(e, ui) {
			var o = ui.options;
			o.scrollSensitivity	= o.scrollSensitivity || 20;
			o.scrollSpeed		= o.scrollSpeed || 20;

			ui.instance.overflowY = function(el) {
				do { if((/auto|scroll/).test(el.css('overflow')) || (/auto|scroll/).test(el.css('overflow-y'))) return el; el = el.parent(); } while (el[0].parentNode);
				return $(document);
			}(this);
			ui.instance.overflowX = function(el) {
				do { if((/auto|scroll/).test(el.css('overflow')) || (/auto|scroll/).test(el.css('overflow-x'))) return el; el = el.parent(); } while (el[0].parentNode);
				return $(document);
			}(this);
			
			if(ui.instance.overflowY[0] != document && ui.instance.overflowY[0].tagName != 'HTML') ui.instance.overflowYstart = ui.instance.overflowY[0].scrollTop;
			if(ui.instance.overflowX[0] != document && ui.instance.overflowX[0].tagName != 'HTML') ui.instance.overflowXstart = ui.instance.overflowX[0].scrollLeft;
			
		},
		sort: function(e, ui) {
			
			var o = ui.options;
			var i = ui.instance;

			if(i.overflowY[0] != document && i.overflowY[0].tagName != 'HTML') {
				if(i.overflowY[0].offsetHeight - (ui.position.top - i.overflowY[0].scrollTop + i.clickOffset.top) < o.scrollSensitivity)
					i.overflowY[0].scrollTop = i.overflowY[0].scrollTop + o.scrollSpeed;
				if((ui.position.top - i.overflowY[0].scrollTop + i.clickOffset.top) < o.scrollSensitivity)
					i.overflowY[0].scrollTop = i.overflowY[0].scrollTop - o.scrollSpeed;				
			} else {
				//$(document.body).append('<p>'+(e.pageY - $(document).scrollTop())+'</p>');
				if(e.pageY - $(document).scrollTop() < o.scrollSensitivity)
					$(document).scrollTop($(document).scrollTop() - o.scrollSpeed);
				if($(window).height() - (e.pageY - $(document).scrollTop()) < o.scrollSensitivity)
					$(document).scrollTop($(document).scrollTop() + o.scrollSpeed);
			}
			
			if(i.overflowX[0] != document && i.overflowX[0].tagName != 'HTML') {
				if(i.overflowX[0].offsetWidth - (ui.position.left - i.overflowX[0].scrollLeft + i.clickOffset.left) < o.scrollSensitivity)
					i.overflowX[0].scrollLeft = i.overflowX[0].scrollLeft + o.scrollSpeed;
				if((ui.position.top - i.overflowX[0].scrollLeft + i.clickOffset.left) < o.scrollSensitivity)
					i.overflowX[0].scrollLeft = i.overflowX[0].scrollLeft - o.scrollSpeed;				
			} else {
				if(e.pageX - $(document).scrollLeft() < o.scrollSensitivity)
					$(document).scrollLeft($(document).scrollLeft() - o.scrollSpeed);
				if($(window).width() - (e.pageX - $(document).scrollLeft()) < o.scrollSensitivity)
					$(document).scrollLeft($(document).scrollLeft() + o.scrollSpeed);
			}
			
			//ui.instance.recallOffset(e);
			i.offset = {
				left: i.mouse.start.left - i.originalPosition.left + (i.overflowXstart !== undefined ? i.overflowXstart - i.overflowX[0].scrollLeft : 0),
				top: i.mouse.start.top - i.originalPosition.top + (i.overflowYstart !== undefined ? i.overflowYstart - i.overflowX[0].scrollTop : 0)
			};

		}
	});

})(jQuery);
;(function($) {
  
  $.effects.blind = function(o) {

    return this.queue(function() {

      // Create element
      var el = $(this), props = ['position','top','left'];
      
      // Set options
      var mode = $.effects.setMode(el, o.options.mode || 'hide'); // Set Mode
      var direction = o.options.direction || 'vertical'; // Default direction
      
      // Adjust
      $.effects.save(el, props); el.show(); // Save & Show
      var wrapper = $.effects.createWrapper(el).css({overflow:'hidden'}); // Create Wrapper
      var ref = (direction == 'vertical') ? 'height' : 'width';
      var distance = (direction == 'vertical') ? wrapper.height() : wrapper.width();
      if(mode == 'show') wrapper.css(ref, 0); // Shift
      
      // Animation
      var animation = {};
      animation[ref] = mode == 'show' ? distance : 0;
     
      // Animate
      wrapper.animate(animation, o.duration, o.options.easing, function() {
        if(mode == 'hide') el.hide(); // Hide
        $.effects.restore(el, props); $.effects.removeWrapper(el); // Restore
        if(o.callback) o.callback.apply(this, arguments); // Callback
        el.dequeue();
      });
      
    });
    
  };
  
})(jQuery);;(function($) {

  $.effects.bounce = function(o) {

    return this.queue(function() {

      // Create element
      var el = $(this), props = ['position','top','left'];

      // Set options
      var mode = $.effects.setMode(el, o.options.mode || 'effect'); // Set Mode
      var direction = o.options.direction || 'up'; // Default direction
      var distance = o.options.distance || 20; // Default distance
      var times = o.options.times || 5; // Default # of times
      var speed = o.duration || 250; // Default speed per bounce
      if (/show|hide/.test(mode)) props.push('opacity'); // Avoid touching opacity to prevent clearType and PNG issues in IE

      // Adjust
      $.effects.save(el, props); el.show(); // Save & Show
      $.effects.createWrapper(el); // Create Wrapper
      var ref = (direction == 'up' || direction == 'down') ? 'top' : 'left';
      var motion = (direction == 'up' || direction == 'left') ? 'pos' : 'neg';
      var distance = o.options.distance || (ref == 'top' ? el.outerHeight({margin:true}) / 3 : el.outerWidth({margin:true}) / 3);
      if (mode == 'show') el.css('opacity', 0).css(ref, motion == 'pos' ? -distance : distance); // Shift
      if (mode == 'hide') distance = distance / (times * 2);
      if (mode != 'hide') times--;
      
      // Animate
      if (mode == 'show') { // Show Bounce
        var animation = {opacity: 1};
        animation[ref] = (motion == 'pos' ? '+=' : '-=') + distance;
        el.animate(animation, speed / 2, o.options.easing);
        distance = distance / 2;
        times--;
      };
      for (var i = 0; i < times; i++) { // Bounces
        var animation1 = {}, animation2 = {};
        animation1[ref] = (motion == 'pos' ? '-=' : '+=') + distance;
        animation2[ref] = (motion == 'pos' ? '+=' : '-=') + distance;
        el.animate(animation1, speed / 2, o.options.easing).animate(animation2, speed / 2, o.options.easing);
        distance = (mode == 'hide') ? distance * 2 : distance / 2;
      };
      if (mode == 'hide') { // Last Bounce
        var animation = {opacity: 0};
        animation[ref] = (motion == 'pos' ? '-=' : '+=')  + distance;
        el.animate(animation, speed / 2, o.options.easing, function(){
          el.hide(); // Hide
          $.effects.restore(el, props); $.effects.removeWrapper(el); // Restore
          if(o.callback) o.callback.apply(this, arguments); // Callback
        });
      } else {
        var animation1 = {}, animation2 = {};
        animation1[ref] = (motion == 'pos' ? '-=' : '+=') + distance;
        animation2[ref] = (motion == 'pos' ? '+=' : '-=') + distance;
        el.animate(animation1, speed / 2, o.options.easing).animate(animation2, speed / 2, o.options.easing, function(){
          $.effects.restore(el, props); $.effects.removeWrapper(el); // Restore
          if(o.callback) o.callback.apply(this, arguments); // Callback
        });
      };
      el.queue('fx', function() { el.dequeue(); });
      el.dequeue();
    });
    
  };

})(jQuery);;(function($) {
  
  $.effects.clip = function(o) {

    return this.queue(function() {

      // Create element
      var el = $(this), props = ['position','top','left','width','height'];
      
      // Set options
      var mode = $.effects.setMode(el, o.options.mode || 'hide'); // Set Mode
      var direction = o.options.direction || 'vertical'; // Default direction
      
      // Adjust
      $.effects.save(el, props); el.show(); // Save & Show
      $.effects.createWrapper(el).css({overflow:'hidden'}); // Create Wrapper
      var ref = {
        size: (direction == 'vertical') ? 'height' : 'width',
        position: (direction == 'vertical') ? 'top' : 'left'
      };
      var distance = (direction == 'vertical') ? el.height() : el.width();
      if(mode == 'show') { el.css(ref.size, 0); el.css(ref.position, distance / 2); } // Shift
      
      // Animation
      var animation = {};
      animation[ref.size] = mode == 'show' ? distance : 0;
      animation[ref.position] = mode == 'show' ? 0 : distance / 2;
        
      // Animate
      el.animate(animation, { queue: false, duration: o.duration, easing: o.options.easing, complete: function() {
        if(mode == 'hide') el.hide(); // Hide
        $.effects.restore(el, props); $.effects.removeWrapper(el); // Restore
        if(o.callback) o.callback.apply(this, arguments); // Callback
        el.dequeue();
      }}); 
      
    });
    
  };
  
})(jQuery);;(function($) {

  $.effects = $.effects || {}; //Add the 'ec' scope

  $.extend($.effects, {
    save: function(el, set) {
      for(var i=0;i<set.length;i++) {
        if(set[i] !== null) $.data(el[0], "ec.storage."+set[i], el.css(set[i]));
      }
    },
    restore: function(el, set) {
      for(var i=0;i<set.length;i++) {
        if(set[i] !== null) el.css(set[i], $.data(el[0], "ec.storage."+set[i]));
      }
    },
    setMode: function(el, mode) {
      if (mode == 'toggle') mode = el.is(':hidden') ? 'show' : 'hide'; // Set for toggle
      return mode;
    },
    getBaseline: function(origin, original) { // Translates a [top,left] array into a baseline value
      // this should be a little more flexible in the future to handle a string & hash
      var y, x;
      switch (origin[0]) {
        case 'top': y = 0; break;
        case 'middle': y = 0.5; break;
        case 'bottom': y = 1; break;
        default: y = origin[0] / original.height;
      };
      switch (origin[1]) {
        case 'left': x = 0; break;
        case 'center': x = 0.5; break;
        case 'right': x = 1; break;
        default: x = origin[1] / original.width;
      };
      return {x: x, y: y};
    },
    createWrapper: function(el) {
      if (el.parent().attr('id') == 'fxWrapper')
        return el;
      var props = {width: el.outerWidth({margin:true}), height: el.outerHeight({margin:true}), 'float': el.css('float')};
      el.wrap('<div id="fxWrapper"></div>');
      var wrapper = el.parent();
      if (el.css('position') == 'static'){
        wrapper.css({position: 'relative'});
        el.css({position: 'relative'});
      } else {
        var top = parseInt(el.css('top'), 10); if (top.constructor != Number) top = 'auto';
        var left = parseInt(el.css('left'), 10); if (left.constructor != Number) left = 'auto';
        wrapper.css({ position: el.css('position'), top: top, left: left, zIndex: el.css('z-index') }).show();
        el.css({position: 'relative', top:0, left:0});
      }
      wrapper.css(props);
      return wrapper;
    },
    removeWrapper: function(el) {
      if (el.parent().attr('id') == 'fxWrapper')
        return el.parent().replaceWith(el);
      return el;
    },
    setTransition: function(el, list, factor, val) {
      val = val || {};
      $.each(list,function(i, x){
        unit = el.cssUnit(x);
        if (unit[0] > 0) val[x] = unit[0] * factor + unit[1];
      });
      return val;
    },
    animateClass: function(value, duration, easing, callback) {

      var cb = (typeof easing == "function" ? easing : (callback ? callback : null));
      var ea = (typeof easing == "object" ? easing : null);

      this.each(function() {

        var offset = {}; var that = $(this); var oldStyleAttr = that.attr("style") || '';
        if(typeof oldStyleAttr == 'object') oldStyleAttr = oldStyleAttr["cssText"]; /* Stupidly in IE, style is a object.. */
        if(value.toggle) { that.hasClass(value.toggle) ? value.remove = value.toggle : value.add = value.toggle; }

        //Let's get a style offset
        var oldStyle = $.extend({}, (document.defaultView ? document.defaultView.getComputedStyle(this,null) : this.currentStyle));
        if(value.add) that.addClass(value.add); if(value.remove) that.removeClass(value.remove);
        var newStyle = $.extend({}, (document.defaultView ? document.defaultView.getComputedStyle(this,null) : this.currentStyle));
        if(value.add) that.removeClass(value.add); if(value.remove) that.addClass(value.remove);

        // The main function to form the object for animation
        for(var n in newStyle) {
          if( typeof newStyle[n] != "function" && newStyle[n] /* No functions and null properties */
          && n.indexOf("Moz") == -1 && n.indexOf("length") == -1 /* No mozilla spezific render properties. */
          && newStyle[n] != oldStyle[n] /* Only values that have changed are used for the animation */
          && (n.match(/color/i) || (!n.match(/color/i) && !isNaN(parseInt(newStyle[n],10)))) /* Only things that can be parsed to integers or colors */
          && (oldStyle.position != "static" || (oldStyle.position == "static" && !n.match(/left|top|bottom|right/))) /* No need for positions when dealing with static positions */
          ) offset[n] = newStyle[n];
        }

        that.animate(offset, duration, ea, function() { // Animate the newly constructed offset object
          // Change style attribute back to original. For stupid IE, we need to clear the damn object.
          if(typeof $(this).attr("style") == 'object') { $(this).attr("style")["cssText"] = ""; $(this).attr("style")["cssText"] = oldStyleAttr; } else $(this).attr("style", oldStyleAttr);
          if(value.add) $(this).addClass(value.add); if(value.remove) $(this).removeClass(value.remove);
          if(cb) cb.apply(this, arguments);
        });

      });
    }
  });

  //Extend the methods of jQuery
  $.fn.extend({
    //Save old methods
    _show: $.fn.show,
    _hide: $.fn.hide,
    __toggle: $.fn.toggle,
    _addClass: $.fn.addClass,
    _removeClass: $.fn.removeClass,
    _toggleClass: $.fn.toggleClass,
    // New ec methods
    effect: function(fx,o,speed,callback) {
      return $.effects[fx] ? $.effects[fx].call(this, {method: fx, options: o || {}, duration: speed, callback: callback }) : null;
    },
    show: function() {
      if(!arguments[0] || (arguments[0].constructor == Number || /(slow|normal|fast)/.test(arguments[0])))
        return this._show.apply(this, arguments);
      else {
        var o = arguments[1] || {}; o['mode'] = 'show';
        return this.effect.apply(this, [arguments[0], o, arguments[2] || o.duration, arguments[3] || o.callback]);
      }
    },
    hide: function() {
      if(!arguments[0] || (arguments[0].constructor == Number || /(slow|normal|fast)/.test(arguments[0])))
        return this._hide.apply(this, arguments);
      else {
        var o = arguments[1] || {}; o['mode'] = 'hide';
        return this.effect.apply(this, [arguments[0], o, arguments[2] || o.duration, arguments[3] || o.callback]);
      }
    },
    toggle: function(){
      if(!arguments[0] || (arguments[0].constructor == Number || /(slow|normal|fast)/.test(arguments[0])) || (arguments[0].constructor == Function))
        return this.__toggle.apply(this, arguments);
      else {
        var o = arguments[1] || {}; o['mode'] = 'toggle';
        return this.effect.apply(this, [arguments[0], o, arguments[2] || o.duration, arguments[3] || o.callback]);
      }
    },
    addClass: function(classNames,speed,easing,callback) {
      return speed ? $.effects.animateClass.apply(this, [{ add: classNames },speed,easing,callback]) : this._addClass(classNames);
    },
    removeClass: function(classNames,speed,easing,callback) {
      return speed ? $.effects.animateClass.apply(this, [{ remove: classNames },speed,easing,callback]) : this._removeClass(classNames);
    },
    toggleClass: function(classNames,speed,easing,callback) {
      return speed ? $.effects.animateClass.apply(this, [{ toggle: classNames },speed,easing,callback]) : this._toggleClass(classNames);
    },
    morph: function(remove,add,speed,easing,callback) {
      return $.effects.animateClass.apply(this, [{ add: add, remove: remove },speed,easing,callback]);
    },
    switchClass: function() {
      this.morph.apply(this, arguments);
    },
    // helper functions
    cssUnit: function(key) {
      var style = this.css(key), val = [];
      $.each( ['em','px','%','pt'], function(i, unit){
        if(style.indexOf(unit) > 0)
          val = [parseFloat(style), unit];
      });
      return val;
    }
  });

  /*
   * jQuery Color Animations
   * Copyright 2007 John Resig
   * Released under the MIT and GPL licenses.
   */

    // We override the animation for all of these color styles
    jQuery.each(['backgroundColor', 'borderBottomColor', 'borderLeftColor', 'borderRightColor', 'borderTopColor', 'color', 'outlineColor'], function(i,attr){
        jQuery.fx.step[attr] = function(fx){
            if ( fx.state == 0 ) {
                fx.start = getColor( fx.elem, attr );
                fx.end = getRGB( fx.end );
            }

            fx.elem.style[attr] = "rgb(" + [
                Math.max(Math.min( parseInt((fx.pos * (fx.end[0] - fx.start[0])) + fx.start[0]), 255), 0),
                Math.max(Math.min( parseInt((fx.pos * (fx.end[1] - fx.start[1])) + fx.start[1]), 255), 0),
                Math.max(Math.min( parseInt((fx.pos * (fx.end[2] - fx.start[2])) + fx.start[2]), 255), 0)
            ].join(",") + ")";
        }
    });

    // Color Conversion functions from highlightFade
    // By Blair Mitchelmore
    // http://jquery.offput.ca/highlightFade/

    // Parse strings looking for color tuples [255,255,255]
    function getRGB(color) {
        var result;

        // Check if we're already dealing with an array of colors
        if ( color && color.constructor == Array && color.length == 3 )
            return color;

        // Look for rgb(num,num,num)
        if (result = /rgb\(\s*([0-9]{1,3})\s*,\s*([0-9]{1,3})\s*,\s*([0-9]{1,3})\s*\)/.exec(color))
            return [parseInt(result[1]), parseInt(result[2]), parseInt(result[3])];

        // Look for rgb(num%,num%,num%)
        if (result = /rgb\(\s*([0-9]+(?:\.[0-9]+)?)\%\s*,\s*([0-9]+(?:\.[0-9]+)?)\%\s*,\s*([0-9]+(?:\.[0-9]+)?)\%\s*\)/.exec(color))
            return [parseFloat(result[1])*2.55, parseFloat(result[2])*2.55, parseFloat(result[3])*2.55];

        // Look for #a0b1c2
        if (result = /#([a-fA-F0-9]{2})([a-fA-F0-9]{2})([a-fA-F0-9]{2})/.exec(color))
            return [parseInt(result[1],16), parseInt(result[2],16), parseInt(result[3],16)];

        // Look for #fff
        if (result = /#([a-fA-F0-9])([a-fA-F0-9])([a-fA-F0-9])/.exec(color))
            return [parseInt(result[1]+result[1],16), parseInt(result[2]+result[2],16), parseInt(result[3]+result[3],16)];

        // Look for rgba(0, 0, 0, 0) == transparent in Safari 3
        if (result = /rgba\(0, 0, 0, 0\)/.exec(color))
            return colors['transparent']

        // Otherwise, we're most likely dealing with a named color
        return colors[jQuery.trim(color).toLowerCase()];
    }

    function getColor(elem, attr) {
        var color;

        do {
            color = jQuery.curCSS(elem, attr);

            // Keep going until we find an element that has color, or we hit the body
            if ( color != '' && color != 'transparent' || jQuery.nodeName(elem, "body") )
                break;

            attr = "backgroundColor";
        } while ( elem = elem.parentNode );

        return getRGB(color);
    };

    // Some named colors to work with
    // From Interface by Stefan Petre
    // http://interface.eyecon.ro/

    var colors = {
        aqua:[0,255,255],
        azure:[240,255,255],
        beige:[245,245,220],
        black:[0,0,0],
        blue:[0,0,255],
        brown:[165,42,42],
        cyan:[0,255,255],
        darkblue:[0,0,139],
        darkcyan:[0,139,139],
        darkgrey:[169,169,169],
        darkgreen:[0,100,0],
        darkkhaki:[189,183,107],
        darkmagenta:[139,0,139],
        darkolivegreen:[85,107,47],
        darkorange:[255,140,0],
        darkorchid:[153,50,204],
        darkred:[139,0,0],
        darksalmon:[233,150,122],
        darkviolet:[148,0,211],
        fuchsia:[255,0,255],
        gold:[255,215,0],
        green:[0,128,0],
        indigo:[75,0,130],
        khaki:[240,230,140],
        lightblue:[173,216,230],
        lightcyan:[224,255,255],
        lightgreen:[144,238,144],
        lightgrey:[211,211,211],
        lightpink:[255,182,193],
        lightyellow:[255,255,224],
        lime:[0,255,0],
        magenta:[255,0,255],
        maroon:[128,0,0],
        navy:[0,0,128],
        olive:[128,128,0],
        orange:[255,165,0],
        pink:[255,192,203],
        purple:[128,0,128],
        violet:[128,0,128],
        red:[255,0,0],
        silver:[192,192,192],
        white:[255,255,255],
        yellow:[255,255,0],
        transparent: [255,255,255]
    };
    

/*
 * jQuery Easing v1.3 - http://gsgd.co.uk/sandbox/jquery/easing/
 *
 * Uses the built in easing capabilities added In jQuery 1.1
 * to offer multiple easing options
 *
 * TERMS OF USE - jQuery Easing
 * 
 * Open source under the BSD License. 
 * 
 * Copyright Ã‚Â© 2008 George McGinley Smith
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of 
 * conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list 
 * of conditions and the following disclaimer in the documentation and/or other materials 
 * provided with the distribution.
 * 
 * Neither the name of the author nor the names of contributors may be used to endorse 
 * or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 *  COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 *  GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED 
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
 * OF THE POSSIBILITY OF SUCH DAMAGE. 
 *
*/

// t: current time, b: begInnIng value, c: change In value, d: duration
jQuery.easing['jswing'] = jQuery.easing['swing'];

jQuery.extend( jQuery.easing,
{
	def: 'easeOutQuad',
	swing: function (x, t, b, c, d) {
		//alert(jQuery.easing.default);
		return jQuery.easing[jQuery.easing.def](x, t, b, c, d);
	},
	easeInQuad: function (x, t, b, c, d) {
		return c*(t/=d)*t + b;
	},
	easeOutQuad: function (x, t, b, c, d) {
		return -c *(t/=d)*(t-2) + b;
	},
	easeInOutQuad: function (x, t, b, c, d) {
		if ((t/=d/2) < 1) return c/2*t*t + b;
		return -c/2 * ((--t)*(t-2) - 1) + b;
	},
	easeInCubic: function (x, t, b, c, d) {
		return c*(t/=d)*t*t + b;
	},
	easeOutCubic: function (x, t, b, c, d) {
		return c*((t=t/d-1)*t*t + 1) + b;
	},
	easeInOutCubic: function (x, t, b, c, d) {
		if ((t/=d/2) < 1) return c/2*t*t*t + b;
		return c/2*((t-=2)*t*t + 2) + b;
	},
	easeInQuart: function (x, t, b, c, d) {
		return c*(t/=d)*t*t*t + b;
	},
	easeOutQuart: function (x, t, b, c, d) {
		return -c * ((t=t/d-1)*t*t*t - 1) + b;
	},
	easeInOutQuart: function (x, t, b, c, d) {
		if ((t/=d/2) < 1) return c/2*t*t*t*t + b;
		return -c/2 * ((t-=2)*t*t*t - 2) + b;
	},
	easeInQuint: function (x, t, b, c, d) {
		return c*(t/=d)*t*t*t*t + b;
	},
	easeOutQuint: function (x, t, b, c, d) {
		return c*((t=t/d-1)*t*t*t*t + 1) + b;
	},
	easeInOutQuint: function (x, t, b, c, d) {
		if ((t/=d/2) < 1) return c/2*t*t*t*t*t + b;
		return c/2*((t-=2)*t*t*t*t + 2) + b;
	},
	easeInSine: function (x, t, b, c, d) {
		return -c * Math.cos(t/d * (Math.PI/2)) + c + b;
	},
	easeOutSine: function (x, t, b, c, d) {
		return c * Math.sin(t/d * (Math.PI/2)) + b;
	},
	easeInOutSine: function (x, t, b, c, d) {
		return -c/2 * (Math.cos(Math.PI*t/d) - 1) + b;
	},
	easeInExpo: function (x, t, b, c, d) {
		return (t==0) ? b : c * Math.pow(2, 10 * (t/d - 1)) + b;
	},
	easeOutExpo: function (x, t, b, c, d) {
		return (t==d) ? b+c : c * (-Math.pow(2, -10 * t/d) + 1) + b;
	},
	easeInOutExpo: function (x, t, b, c, d) {
		if (t==0) return b;
		if (t==d) return b+c;
		if ((t/=d/2) < 1) return c/2 * Math.pow(2, 10 * (t - 1)) + b;
		return c/2 * (-Math.pow(2, -10 * --t) + 2) + b;
	},
	easeInCirc: function (x, t, b, c, d) {
		return -c * (Math.sqrt(1 - (t/=d)*t) - 1) + b;
	},
	easeOutCirc: function (x, t, b, c, d) {
		return c * Math.sqrt(1 - (t=t/d-1)*t) + b;
	},
	easeInOutCirc: function (x, t, b, c, d) {
		if ((t/=d/2) < 1) return -c/2 * (Math.sqrt(1 - t*t) - 1) + b;
		return c/2 * (Math.sqrt(1 - (t-=2)*t) + 1) + b;
	},
	easeInElastic: function (x, t, b, c, d) {
		var s=1.70158;var p=0;var a=c;
		if (t==0) return b;  if ((t/=d)==1) return b+c;  if (!p) p=d*.3;
		if (a < Math.abs(c)) { a=c; var s=p/4; }
		else var s = p/(2*Math.PI) * Math.asin (c/a);
		return -(a*Math.pow(2,10*(t-=1)) * Math.sin( (t*d-s)*(2*Math.PI)/p )) + b;
	},
	easeOutElastic: function (x, t, b, c, d) {
		var s=1.70158;var p=0;var a=c;
		if (t==0) return b;  if ((t/=d)==1) return b+c;  if (!p) p=d*.3;
		if (a < Math.abs(c)) { a=c; var s=p/4; }
		else var s = p/(2*Math.PI) * Math.asin (c/a);
		return a*Math.pow(2,-10*t) * Math.sin( (t*d-s)*(2*Math.PI)/p ) + c + b;
	},
	easeInOutElastic: function (x, t, b, c, d) {
		var s=1.70158;var p=0;var a=c;
		if (t==0) return b;  if ((t/=d/2)==2) return b+c;  if (!p) p=d*(.3*1.5);
		if (a < Math.abs(c)) { a=c; var s=p/4; }
		else var s = p/(2*Math.PI) * Math.asin (c/a);
		if (t < 1) return -.5*(a*Math.pow(2,10*(t-=1)) * Math.sin( (t*d-s)*(2*Math.PI)/p )) + b;
		return a*Math.pow(2,-10*(t-=1)) * Math.sin( (t*d-s)*(2*Math.PI)/p )*.5 + c + b;
	},
	easeInBack: function (x, t, b, c, d, s) {
		if (s == undefined) s = 1.70158;
		return c*(t/=d)*t*((s+1)*t - s) + b;
	},
	easeOutBack: function (x, t, b, c, d, s) {
		if (s == undefined) s = 1.70158;
		return c*((t=t/d-1)*t*((s+1)*t + s) + 1) + b;
	},
	easeInOutBack: function (x, t, b, c, d, s) {
		if (s == undefined) s = 1.70158; 
		if ((t/=d/2) < 1) return c/2*(t*t*(((s*=(1.525))+1)*t - s)) + b;
		return c/2*((t-=2)*t*(((s*=(1.525))+1)*t + s) + 2) + b;
	},
	easeInBounce: function (x, t, b, c, d) {
		return c - jQuery.easing.easeOutBounce (x, d-t, 0, c, d) + b;
	},
	easeOutBounce: function (x, t, b, c, d) {
		if ((t/=d) < (1/2.75)) {
			return c*(7.5625*t*t) + b;
		} else if (t < (2/2.75)) {
			return c*(7.5625*(t-=(1.5/2.75))*t + .75) + b;
		} else if (t < (2.5/2.75)) {
			return c*(7.5625*(t-=(2.25/2.75))*t + .9375) + b;
		} else {
			return c*(7.5625*(t-=(2.625/2.75))*t + .984375) + b;
		}
	},
	easeInOutBounce: function (x, t, b, c, d) {
		if (t < d/2) return jQuery.easing.easeInBounce (x, t*2, 0, c, d) * .5 + b;
		return jQuery.easing.easeOutBounce (x, t*2-d, 0, c, d) * .5 + c*.5 + b;
	}
});

/*
 *
 * TERMS OF USE - EASING EQUATIONS
 * 
 * Open source under the BSD License. 
 * 
 * Copyright Ã‚Â© 2001 Robert Penner
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of 
 * conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list 
 * of conditions and the following disclaimer in the documentation and/or other materials 
 * provided with the distribution.
 * 
 * Neither the name of the author nor the names of contributors may be used to endorse 
 * or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 *  COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 *  GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED 
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
 * OF THE POSSIBILITY OF SUCH DAMAGE. 
 *
 */

})(jQuery);;(function($) {
  
  $.effects.drop = function(o) {

    return this.queue(function() {

      // Create element
      var el = $(this), props = ['position','top','left','opacity'];
      
      // Set options
      var mode = $.effects.setMode(el, o.options.mode || 'hide'); // Set Mode
      var direction = o.options.direction || 'left'; // Default Direction
      
      // Adjust
      $.effects.save(el, props); el.show(); // Save & Show
      $.effects.createWrapper(el); // Create Wrapper
      var ref = (direction == 'up' || direction == 'down') ? 'top' : 'left';
      var motion = (direction == 'up' || direction == 'left') ? 'pos' : 'neg';
      var distance = o.options.distance || (ref == 'top' ? el.outerHeight({margin:true}) / 2 : el.outerWidth({margin:true}) / 2);
      if (mode == 'show') el.css('opacity', 0).css(ref, motion == 'pos' ? -distance : distance); // Shift
      
      // Animation
      var animation = {opacity: mode == 'show' ? 1 : 0};
      animation[ref] = (mode == 'show' ? (motion == 'pos' ? '+=' : '-=') : (motion == 'pos' ? '-=' : '+=')) + distance;
      
      // Animate
      el.animate(animation, { queue: false, duration: o.duration, easing: o.options.easing, complete: function() {
        if(mode == 'hide') el.hide(); // Hide
        $.effects.restore(el, props); $.effects.removeWrapper(el); // Restore
        if(o.callback) o.callback.apply(this, arguments); // Callback
        el.dequeue();
      }});
      
    });
    
  };
  
})(jQuery);
;(function($) {
  
  $.effects.explode = function(o) {

    return this.queue(function() {

		var rows = o.options.pieces ? Math.round(Math.sqrt(o.options.pieces)) : 3;
		var cells = o.options.pieces ? Math.round(Math.sqrt(o.options.pieces)) : 3;
		
		o.options.mode = o.options.mode == 'toggle' ? ($(this).is(':visible') ? 'hide' : 'show') : o.options.mode;
		var el = $(this).show().css('visibility', 'hidden');
		var offset = el.offset();
		var width = el.outerWidth();
		var height = el.outerHeight();

		for(var i=0;i<rows;i++) { // =
			for(var j=0;j<cells;j++) { // ||
				el
					.clone()
					.appendTo('body')
					.wrap('<div></div>')
					.css({
						position: 'absolute',
						visibility: 'visible',
						left: -j*(width/cells),
						top: -i*(height/rows)
					})
					.parent()
					.addClass('ec-explode')
					.css({
						position: 'absolute',
						overflow: 'hidden',
						width: width/cells,
						height: height/rows,
						left: offset.left + j*(width/cells) + (o.options.mode == 'show' ? (j-Math.floor(cells/2))*(width/cells) : 0),
						top: offset.top + i*(height/rows) + (o.options.mode == 'show' ? (i-Math.floor(rows/2))*(height/rows) : 0),
						opacity: o.options.mode == 'show' ? 0 : 1
					}).animate({
						left: offset.left + j*(width/cells) + (o.options.mode == 'show' ? 0 : (j-Math.floor(cells/2))*(width/cells)),
						top: offset.top + i*(height/rows) + (o.options.mode == 'show' ? 0 : (i-Math.floor(rows/2))*(height/rows)),
						opacity: o.options.mode == 'show' ? 1 : 0
					}, o.duration || 500);
			}
		}

		// Set a timeout, to call the callback approx. when the other animations have finished
		setTimeout(function() {
			
			o.options.mode == 'show' ? el.css({ visibility: 'visible' }) : el.css({ visibility: 'visible' }).hide();
        	if(o.callback) o.callback.apply(el[0]); // Callback
        	el.dequeue();
        	
        	$('.ec-explode').remove();
			
		}, o.duration || 500);
		
      
    });
    
  };
  
})(jQuery);
;(function($) {
  
  $.effects.fade = function(o) {

    return this.queue(function() {
      
      // Create element
      var el = $(this), props = ['opacity'];
      
      // Set options
      var mode = $.effects.setMode(el, o.options.mode || 'effect'); // Set Mode
      if (mode == 'toggle') mode = el.is(':hidden') ? 'show' : 'hide'; // Set for toggle
      var opacity = o.options.opacity || 0; // Default fade opacity
      var original_opacity = el.css('opacity');
      
      // Adjust
      $.effects.save(el, props); el.show(); // Save & Show
      if(mode == 'show') el.css({opacity: 0}); // Shift
      
      // Animation
      var animation = {opacity: mode == 'show' ? original_opacity : opacity};
      
      // Animate
      el.animate(animation, { queue: false, duration: o.duration, easing: o.options.easing, complete: function() {
        if(mode == 'hide') el.hide(); // Hide
        if(mode == 'hide') $.effects.restore(el, props); // Restore
        if(o.callback) o.callback.apply(this, arguments); // Callback
        el.dequeue();
      }});
      
    });
    
  };
  
})(jQuery);;(function($) {
  
  $.effects.fold = function(o) {

    return this.queue(function() {

      // Create element
      var el = $(this), props = ['position','top','left'];
      
      // Set options
      var mode = $.effects.setMode(el, o.options.mode || 'hide'); // Set Mode
      var size = o.options.size || 15; // Default fold size
      
      // Adjust
      $.effects.save(el, props); el.show(); // Save & Show
      var wrapper = $.effects.createWrapper(el).css({overflow:'hidden'}); // Create Wrapper
      var ref = (mode == 'show') ? ['width', 'height'] : ['height', 'width'];
      var distance = (mode == 'show') ? [wrapper.width(), wrapper.height()] : [wrapper.height(), wrapper.width()];
      if(mode == 'show') wrapper.css({height: size, width: 0}); // Shift
      
      // Animation
      var animation1 = {}, animation2 = {};
      animation1[ref[0]] = mode == 'show' ? distance[0] : size;
      animation2[ref[1]] = mode == 'show' ? distance[1] : 0;
      
      // Animate
      wrapper.animate(animation1, o.duration / 2, o.options.easing)
      .animate(animation2, o.duration / 2, o.options.easing, function() {
        if(mode == 'hide') el.hide(); // Hide
        $.effects.restore(el, props); $.effects.removeWrapper(el); // Restore
        if(o.callback) o.callback.apply(this, arguments); // Callback
        el.dequeue();
      });
      
    });
    
  };
  
})(jQuery);;(function($) {
  
  $.effects.highlight = function(o) {

    return this.queue(function() {
      
      // Create element
      var el = $(this), props = ['backgroundImage','backgroundColor','opacity'];
      
      // Set options
      var mode = $.effects.setMode(el, o.options.mode || 'show'); // Set Mode
      var color = o.options.color || "#ffff99"; // Default highlight color
      
      // Adjust
      $.effects.save(el, props); el.show(); // Save & Show
      el.css({backgroundImage: 'none', backgroundColor: color}); // Shift
      
      // Animation
      var animation = {backgroundColor: $.data(this, "ec.storage.backgroundColor")};
      if (mode == "hide") animation['opacity'] = 0;
      
      // Animate
      el.animate(animation, { queue: false, duration: o.duration, easing: o.options.easing, complete: function() {
        if(mode == "hide") el.hide();
        $.effects.restore(el, props);
	    if (mode == "show" && jQuery.browser.msie) this.style.removeAttribute('filter'); 
        if(o.callback) o.callback.apply(this, arguments);
        el.dequeue();
      }});
      
    });
    
  };
  
})(jQuery);
;(function($) {
  
  $.effects.pulsate = function(o) {

    return this.queue(function() {
      
      // Create element
      var el = $(this);
      
      // Set options
      var mode = $.effects.setMode(el, o.options.mode || 'show'); // Set Mode
      var times = o.options.times || 5; // Default # of times
      
      // Adjust
      if (mode != 'hide') times--;
      if (el.is(':hidden')) { // Show fadeIn
        el.css('opacity', 0);
        el.show(); // Show
        el.animate({opacity: 1}, o.duration / 2, o.options.easing);
        times--;
      }
      
      // Animate
      for (var i = 0; i < times; i++) { // Pulsate
        el.animate({opacity: 0}, o.duration / 2, o.options.easing).animate({opacity: 1}, o.duration / 2, o.options.easing);
      };
      if (mode == 'hide') { // Last Pulse
        el.animate({opacity: 0}, o.duration / 2, o.options.easing, function(){
          el.hide(); // Hide
          if(o.callback) o.callback.apply(this, arguments); // Callback
        });
      } else {
        el.animate({opacity: 0}, o.duration / 2, o.options.easing).animate({opacity: 1}, o.duration / 2, o.options.easing, function(){
          if(o.callback) o.callback.apply(this, arguments); // Callback
        });
      };
      el.queue('fx', function() { el.dequeue(); });
      el.dequeue();
    });
    
  };
  
})(jQuery);
;(function($) {
  
  $.effects.puff = function(o) {
  
    return this.queue(function() {
  
      // Create element
      var el = $(this);
    
      // Set options
      var mode = $.effects.setMode(el, o.options.mode || 'hide'); // Set Mode
      var percent = parseInt(o.options.percent) || 150; // Set default puff percent
      o.options.fade = true; // It's not a puff if it doesn't fade! :)
      var original = {height: el.height(), width: el.width()}; // Save original
    
      // Adjust
      var factor = percent / 100;
      el.from = (mode == 'hide') ? original : {height: original.height * factor, width: original.width * factor};
    
      // Animation
      o.options.from = el.from;
      o.options.percent = (mode == 'hide') ? percent : 100;
      o.options.mode = mode;
    
      // Animate
      el.effect('scale', o.options, o.duration, o.callback);
      el.dequeue();
    });
    
  };

  $.effects.scale = function(o) {
    
    return this.queue(function() {
    
      // Create element
      var el = $(this);

      // Set options
      var mode = $.effects.setMode(el, o.options.mode || 'effect'); // Set Mode
      var percent = parseInt(o.options.percent) || (parseInt(o.options.percent) == 0 ? 0 : (mode == 'hide' ? 0 : 100)); // Set default scaling percent
      var direction = o.options.direction || 'both'; // Set default axis
      var origin = o.options.origin; // The origin of the scaling
      if (mode != 'effect') { // Set default origin and restore for show/hide
        origin = origin || ['middle','center'];
        o.options.restore = true;
      }
      var original = {height: el.height(), width: el.width()}; // Save original
      el.from = o.options.from || (mode == 'show' ? {height: 0, width: 0} : original); // Default from state
    
      // Adjust
      var factor = { // Set scaling factor
        y: direction != 'horizontal' ? (percent / 100) : 1,
        x: direction != 'vertical' ? (percent / 100) : 1
      };
      el.to = {height: original.height * factor.y, width: original.width * factor.x}; // Set to state
      if (origin) { // Calculate baseline shifts
        var baseline = $.effects.getBaseline(origin, original);
        el.from.top = (original.height - el.from.height) * baseline.y;
        el.from.left = (original.width - el.from.width) * baseline.x;
        el.to.top = (original.height - el.to.height) * baseline.y;
        el.to.left = (original.width - el.to.width) * baseline.x;
      };
      if (o.options.fade) { // Fade option to support puff
        if (mode == 'show') {el.from.opacity = 0; el.to.opacity = 1;};
        if (mode == 'hide') {el.from.opacity = 1; el.to.opacity = 0;};
      };
    
      // Animation
      o.options.from = el.from; o.options.to = el.to;
    
      // Animate
      el.effect('size', o.options, o.duration, o.callback);
      el.dequeue();
    });
    
  };
  
  $.effects.size = function(o) {

    return this.queue(function() {
      
      // Create element
      var el = $(this), props = ['position','top','left','width','height','overflow','opacity'];
      var props1 = ['position','overflow','opacity']; // Always restore
      var props2 = ['width','height','overflow']; // Copy for children
      var cProps = ['fontSize'];
      var vProps = ['borderTopWidth', 'borderBottomWidth', 'paddingTop', 'paddingBottom'];
      var hProps = ['borderLeftWidth', 'borderRightWidth', 'paddingLeft', 'paddingRight'];
      
      // Set options
      var mode = $.effects.setMode(el, o.options.mode || 'effect'); // Set Mode
      var restore = o.options.restore || false; // Default restore
      var scale = o.options.scale || 'both'; // Default scale mode
      var original = {height: el.height(), width: el.width()}; // Save original
      el.from = o.options.from || original; // Default from state
      el.to = o.options.to || original; // Default to state
      
      // Adjust
      var factor = { // Set scaling factor
        from: {y: el.from.height / original.height, x: el.from.width / original.width},
        to: {y: el.to.height / original.height, x: el.to.width / original.width}
      };
      if (scale == 'box' || scale == 'both') { // Scale the css box
        if (factor.from.y != factor.to.y) { // Vertical props scaling
          props = props.concat(vProps);
          el.from = $.effects.setTransition(el, vProps, factor.from.y, el.from);
          el.to = $.effects.setTransition(el, vProps, factor.to.y, el.to);
        };
        if (factor.from.x != factor.to.x) { // Horizontal props scaling
          props = props.concat(hProps);
          el.from = $.effects.setTransition(el, hProps, factor.from.x, el.from);
          el.to = $.effects.setTransition(el, hProps, factor.to.x, el.to);
        };
      };
      if (scale == 'content' || scale == 'both') { // Scale the content
        if (factor.from.y != factor.to.y) { // Vertical props scaling
          props = props.concat(cProps);
          el.from = $.effects.setTransition(el, cProps, factor.from.y, el.from);
          el.to = $.effects.setTransition(el, cProps, factor.to.y, el.to);
        };
      };
      $.effects.save(el, restore ? props : props1); el.show(); // Save & Show
      $.effects.createWrapper(el); // Create Wrapper
      el.css('overflow','hidden').css(el.from); // Shift
      
      // Animate
      if (scale == 'content' || scale == 'both') { // Scale the children
        vProps = vProps.concat(['marginTop','marginBottom']).concat(cProps); // Add margins/font-size
        hProps = hProps.concat(['marginLeft','marginRight']); // Add margins
        props2 = props.concat(vProps).concat(hProps); // Concat
        el.find("*[width]").each(function(){
          child = $(this);
          if (restore) $.effects.save(child, props2);
          var c_original = {height: child.height(), width: child.width()}; // Save original
          child.from = {height: c_original.height * factor.from.y, width: c_original.width * factor.from.x};
          child.to = {height: c_original.height * factor.to.y, width: c_original.width * factor.to.x};
          if (factor.from.y != factor.to.y) { // Vertical props scaling
            child.from = $.effects.setTransition(child, vProps, factor.from.y, child.from);
            child.to = $.effects.setTransition(child, vProps, factor.to.y, child.to);
          };
          if (factor.from.x != factor.to.x) { // Horizontal props scaling
            child.from = $.effects.setTransition(child, hProps, factor.from.x, child.from);
            child.to = $.effects.setTransition(child, hProps, factor.to.x, child.to);
          };
          child.css(child.from); // Shift children
          child.animate(child.to, o.duration, o.options.easing, function(){
            if (restore) $.effects.restore(child, props2); // Restore children
          }); // Animate children
        });
      };
      
      // Animate
      el.animate(el.to, { queue: false, duration: o.duration, easing: o.options.easing, complete: function() {
        if(mode == 'hide') el.hide(); // Hide
        $.effects.restore(el, restore ? props : props1); $.effects.removeWrapper(el); // Restore
        if(o.callback) o.callback.apply(this, arguments); // Callback
        el.dequeue();
      }}); 
      
    });

  };
  
})(jQuery);
;(function($) {
  
  $.effects.shake = function(o) {

    return this.queue(function() {

      // Create element
      var el = $(this), props = ['position','top','left'];
      
      // Set options
      var mode = $.effects.setMode(el, o.options.mode || 'effect'); // Set Mode
      var direction = o.options.direction || 'left'; // Default direction
      var distance = o.options.distance || 20; // Default distance
      var times = o.options.times || 3; // Default # of times
      var speed = o.duration || o.options.duration || 140; // Default speed per shake
      
      // Adjust
      $.effects.save(el, props); el.show(); // Save & Show
      $.effects.createWrapper(el); // Create Wrapper
      var ref = (direction == 'up' || direction == 'down') ? 'top' : 'left';
      var motion = (direction == 'up' || direction == 'left') ? 'pos' : 'neg';
      
      // Animation
      var animation = {}, animation1 = {}, animation2 = {};
      animation[ref] = (motion == 'pos' ? '-=' : '+=')  + distance;
      animation1[ref] = (motion == 'pos' ? '+=' : '-=')  + distance * 2;
      animation2[ref] = (motion == 'pos' ? '-=' : '+=')  + distance * 2;
      
      // Animate
      el.animate(animation, speed, o.options.easing);
      for (var i = 1; i < times; i++) { // Shakes
        el.animate(animation1, speed, o.options.easing).animate(animation2, speed, o.options.easing);
      };
      el.animate(animation1, speed, o.options.easing).
      animate(animation, speed / 2, o.options.easing, function(){ // Last shake
        $.effects.restore(el, props); $.effects.removeWrapper(el); // Restore
        if(o.callback) o.callback.apply(this, arguments); // Callback
      });
      el.queue('fx', function() { el.dequeue(); });
      el.dequeue();
    });
    
  };
  
})(jQuery);;(function($) {
  
  $.effects.slide = function(o) {

    return this.queue(function() {

      // Create element
      var el = $(this), props = ['position','top','left'];
      
      // Set options
      var mode = $.effects.setMode(el, o.options.mode || 'show'); // Set Mode
      var direction = o.options.direction || 'left'; // Default Direction
      
      // Adjust
      $.effects.save(el, props); el.show(); // Save & Show
      $.effects.createWrapper(el).css({overflow:'hidden'}); // Create Wrapper
      var ref = (direction == 'up' || direction == 'down') ? 'top' : 'left';
      var motion = (direction == 'up' || direction == 'left') ? 'pos' : 'neg';
      var distance = o.options.distance || (ref == 'top' ? el.outerHeight({margin:true}) : el.outerWidth({margin:true}));
      if (mode == 'show') el.css(ref, motion == 'pos' ? -distance : distance); // Shift
      
      // Animation
      var animation = {};
      animation[ref] = (mode == 'show' ? (motion == 'pos' ? '+=' : '-=') : (motion == 'pos' ? '-=' : '+=')) + distance;
      
      // Animate
      el.animate(animation, { queue: false, duration: o.duration, easing: o.options.easing, complete: function() {
        if(mode == 'hide') el.hide(); // Hide
        $.effects.restore(el, props); $.effects.removeWrapper(el); // Restore
        if(o.callback) o.callback.apply(this, arguments); // Callback
        el.dequeue();
      }});
      
    });
    
  };
  
})(jQuery);;(function($) {
  
  $.effects.transfer = function(o) {

    return this.queue(function() {

      // Create element
      var el = $(this);
      
      // Set options
      var mode = $.effects.setMode(el, o.options.mode || 'effect'); // Set Mode
      var target = $(o.options.to); // Find Target
      var position = el.position();
	  var transfer = $('<div id="fxTransfer"></div>').appendTo(document.body)
      
      // Set target css
      transfer.addClass(o.options.className);
      transfer.css({
        top: position['top'],
        left: position['left'],
        height: el.outerHeight({margin:true}) - parseInt(transfer.css('borderTopWidth')) - parseInt(transfer.css('borderBottomWidth')),
        width: el.outerWidth({margin:true}) - parseInt(transfer.css('borderLeftWidth')) - parseInt(transfer.css('borderRightWidth')),
        position: 'absolute'
      });
      
      // Animation
      position = target.position();
      animation = {
        top: position['top'],
        left: position['left'],
        height: target.outerHeight() - parseInt(transfer.css('borderTopWidth')) - parseInt(transfer.css('borderBottomWidth')),
        width: target.outerWidth() - parseInt(transfer.css('borderLeftWidth')) - parseInt(transfer.css('borderRightWidth'))
      };
      
      // Animate
      transfer.animate(animation, o.duration, o.options.easing, function() {
        transfer.remove(); // Remove div
        if(o.callback) o.callback.apply(this, arguments); // Callback
        el.dequeue();
      }); 
      
    });
    
  };
  
})(jQuery);
(function(){
/*
 * jQuery 1.2.4a - New Wave Javascript
 *
 * Copyright (c) 2008 John Resig (jquery.com)
 * Dual licensed under the MIT (MIT-LICENSE.txt)
 * and GPL (GPL-LICENSE.txt) licenses.
 *
 * $Date: 2008/05/14 15:34:12 $
 * $Rev: 5390 $
 */

// Map over jQuery in case of overwrite
var _jQuery = window.jQuery,
// Map over the $ in case of overwrite	
	_$ = window.$;

var jQuery = window.jQuery = window.$ = function( selector, context ) {
	// The jQuery object is actually just the init constructor 'enhanced'
	return new jQuery.fn.init( selector, context );
};

// A simple way to check for HTML strings or ID strings
// (both of which we optimize for)
var quickExpr = /^[^<]*(<(.|\s)+>)[^>]*$|^#(\w+)$/,

// Is it a simple selector
	isSimple = /^.[^:#\[\.]*$/;

jQuery.fn = jQuery.prototype = {
	init: function( selector, context ) {
		// Make sure that a selection was provided
		selector = selector || document;

		// Handle $(DOMElement)
		if ( selector.nodeType ) {
			this[0] = selector;
			this.length = 1;
			return this;

		// Handle HTML strings
		} else if ( typeof selector == "string" ) {
			// Are we dealing with HTML string or an ID?
			var match = quickExpr.exec( selector );

			// Verify a match, and that no context was specified for #id
			if ( match && (match[1] || !context) ) {

				// HANDLE: $(html) -> $(array)
				if ( match[1] )
					selector = jQuery.clean( [ match[1] ], context );

				// HANDLE: $("#id")
				else {
					var elem = document.getElementById( match[3] );

					// Make sure an element was located
					if ( elem )
						// Handle the case where IE and Opera return items
						// by name instead of ID
						if ( elem.id != match[3] )
							return jQuery().find( selector );

						// Otherwise, we inject the element directly into the jQuery object
						else {
							this[0] = elem;
							this.length = 1;
							return this;
						}

					else
						selector = [];
				}

			// HANDLE: $(expr, [context])
			// (which is just equivalent to: $(content).find(expr)
			} else
				return new jQuery( context ).find( selector );

		// HANDLE: $(function)
		// Shortcut for document ready
		} else if ( jQuery.isFunction( selector ) )
			return new jQuery( document )[ jQuery.fn.ready ? "ready" : "load" ]( selector );
		
		return this.setArray(jQuery.makeArray(selector));
	},
	
	// The current version of jQuery being used
	jquery: "1.2.4a",

	// The number of elements contained in the matched element set
	size: function() {
		return this.length;
	},
	
	// The number of elements contained in the matched element set
	length: 0,

	// Get the Nth element in the matched element set OR
	// Get the whole matched element set as a clean array
	get: function( num ) {
		return num == undefined ?

			// Return a 'clean' array
			jQuery.makeArray( this ) :

			// Return just the object
			this[ num ];
	},
	
	// Take an array of elements and push it onto the stack
	// (returning the new matched element set)
	pushStack: function( elems ) {
		// Build a new jQuery matched element set
		var ret = jQuery( elems );

		// Add the old object onto the stack (as a reference)
		ret.prevObject = this;

		// Return the newly-formed element set
		return ret;
	},
	
	// Force the current matched set of elements to become
	// the specified array of elements (destroying the stack in the process)
	// You should use pushStack() in order to do this, but maintain the stack
	setArray: function( elems ) {
		// Resetting the length to 0, then using the native Array push
		// is a super-fast way to populate an object with array-like properties
		this.length = 0;
		Array.prototype.push.apply( this, elems );
		
		return this;
	},

	// Execute a callback for every element in the matched set.
	// (You can seed the arguments with an array of args, but this is
	// only used internally.)
	each: function( callback, args ) {
		return jQuery.each( this, callback, args );
	},

	// Determine the position of an element within 
	// the matched set of elements
	index: function( elem ) {
		var ret = -1;

		// Locate the position of the desired element
		this.each(function(i){
			if ( this == elem )
				ret = i;
		});

		return ret;
	},

	attr: function( name, value, type ) {
		var options = name;
		
		// Look for the case where we're accessing a style value
		if ( name.constructor == String )
			if ( value == undefined )
				return this.length && jQuery[ type || "attr" ]( this[0], name ) || undefined;

			else {
				options = {};
				options[ name ] = value;
			}
		
		// Check to see if we're setting style values
		return this.each(function(i){
			// Set all the styles
			for ( name in options )
				jQuery.attr(
					type ?
						this.style :
						this,
					name, jQuery.prop( this, options[ name ], type, i, name )
				);
		});
	},

	css: function( key, value ) {
		// ignore negative width and height values
		if ( (key == 'width' || key == 'height') && parseFloat(value) < 0 )
			value = undefined;
		return this.attr( key, value, "curCSS" );
	},

	text: function( text ) {
		if ( typeof text != "object" && text != null )
			return this.empty().append( (this[0] && this[0].ownerDocument || document).createTextNode( text ) );

		var ret = "";

		jQuery.each( text || this, function(){
			jQuery.each( this.childNodes, function(){
				if ( this.nodeType != 8 )
					ret += this.nodeType != 1 ?
						this.nodeValue :
						jQuery.fn.text( [ this ] );
			});
		});

		return ret;
	},

	wrapAll: function( html ) {
		if ( this[0] )
			// The elements to wrap the target around
			jQuery( html, this[0].ownerDocument )
				.clone()
				.insertBefore( this[0] )
				.map(function(){
					var elem = this;

					while ( elem.firstChild )
						elem = elem.firstChild;

					return elem;
				})
				.append(this);

		return this;
	},

	wrapInner: function( html ) {
		return this.each(function(){
			jQuery( this ).contents().wrapAll( html );
		});
	},

	wrap: function( html ) {
		return this.each(function(){
			jQuery( this ).wrapAll( html );
		});
	},

	append: function() {
		return this.domManip(arguments, true, false, function(elem){
			if (this.nodeType == 1)
				this.appendChild( elem );
		});
	},

	prepend: function() {
		return this.domManip(arguments, true, true, function(elem){
			if (this.nodeType == 1)
				this.insertBefore( elem, this.firstChild );
		});
	},
	
	before: function() {
		return this.domManip(arguments, false, false, function(elem){
			this.parentNode.insertBefore( elem, this );
		});
	},

	after: function() {
		return this.domManip(arguments, false, true, function(elem){
			this.parentNode.insertBefore( elem, this.nextSibling );
		});
	},

	end: function() {
		return this.prevObject || jQuery( [] );
	},

	find: function( selector ) {
		var elems = jQuery.map(this, function(elem){
			return jQuery.find( selector, elem );
		});

		return this.pushStack( /[^+>] [^+>]/.test( selector ) || selector.indexOf("..") > -1 ?
			jQuery.unique( elems ) :
			elems );
	},

	clone: function( events ) {
		// Do the clone
		var ret = this.map(function(){
			if ( jQuery.browser.msie && !jQuery.isXMLDoc(this) ) {
				// IE copies events bound via attachEvent when
				// using cloneNode. Calling detachEvent on the
				// clone will also remove the events from the orignal
				// In order to get around this, we use innerHTML.
				// Unfortunately, this means some modifications to 
				// attributes in IE that are actually only stored 
				// as properties will not be copied (such as the
				// the name attribute on an input).
				var clone = this.cloneNode(true),
					container = document.createElement("div");
				container.appendChild(clone);
				return jQuery.clean([container.innerHTML])[0];
			} else
				return this.cloneNode(true);
		});

		// Need to set the expando to null on the cloned set if it exists
		// removeData doesn't work here, IE removes it from the original as well
		// this is primarily for IE but the data expando shouldn't be copied over in any browser
		var clone = ret.find("*").andSelf().each(function(){
			if ( this[ expando ] != undefined )
				this[ expando ] = null;
		});
		
		// Copy the events from the original to the clone
		if ( events === true )
			this.find("*").andSelf().each(function(i){
				if (this.nodeType == 3)
					return;
				var events = jQuery.data( this, "events" );

				for ( var type in events )
					for ( var handler in events[ type ] )
						jQuery.event.add( clone[ i ], type, events[ type ][ handler ], events[ type ][ handler ].data );
			});

		// Return the cloned set
		return ret;
	},

	filter: function( selector ) {
		return this.pushStack(
			jQuery.isFunction( selector ) &&
			jQuery.grep(this, function(elem, i){
				return selector.call( elem, i );
			}) ||

			jQuery.multiFilter( selector, this ) );
	},

	not: function( selector ) {
		if ( selector.constructor == String )
			// test special case where just one selector is passed in
			if ( isSimple.test( selector ) )
				return this.pushStack( jQuery.multiFilter( selector, this, true ) );
			else
				selector = jQuery.multiFilter( selector, this );

		var isArrayLike = selector.length && selector[selector.length - 1] !== undefined && !selector.nodeType;
		return this.filter(function() {
			return isArrayLike ? jQuery.inArray( this, selector ) < 0 : this != selector;
		});
	},

	add: function( selector ) {
		return !selector ? this : this.pushStack( jQuery.merge( 
			this.get(),
			selector.constructor == String ? 
				jQuery( selector ).get() :
				selector.length != undefined && (!selector.nodeName || jQuery.nodeName(selector, "form")) ?
					selector : [selector] ) );
	},

	is: function( selector ) {
		return !!selector && jQuery.multiFilter( selector, this ).length > 0;
	},

	hasClass: function( selector ) {
		return this.is( "." + selector );
	},
	
	val: function( value ) {
		if ( value == undefined ) {

			if ( this.length ) {
				var elem = this[0];

				// We need to handle select boxes special
				if ( jQuery.nodeName( elem, "select" ) ) {
					var index = elem.selectedIndex,
						values = [],
						options = elem.options,
						one = elem.type == "select-one";
					
					// Nothing was selected
					if ( index < 0 )
						return null;

					// Loop through all the selected options
					for ( var i = one ? index : 0, max = one ? index + 1 : options.length; i < max; i++ ) {
						var option = options[ i ];

						if ( option.selected ) {
							// Get the specifc value for the option
							value = jQuery.browser.msie && !option.attributes.value.specified ? option.text : option.value;
							
							// We don't need an array for one selects
							if ( one )
								return value;
							
							// Multi-Selects return an array
							values.push( value );
						}
					}
					
					return values;
					
				// Everything else, we just grab the value
				} else
					return (this[0].value || "").replace(/\r/g, "");

			}

			return undefined;
		}

		return this.each(function(){
			if ( this.nodeType != 1 )
				return;

			if ( value.constructor == Array && /radio|checkbox/.test( this.type ) )
				this.checked = (jQuery.inArray(this.value, value) >= 0 ||
					jQuery.inArray(this.name, value) >= 0);

			else if ( jQuery.nodeName( this, "select" ) ) {
				var values = value.constructor == Array ?
					value :
					[ value ];

				jQuery( "option", this ).each(function(){
					this.selected = (jQuery.inArray( this.value, values ) >= 0 ||
						jQuery.inArray( this.text, values ) >= 0);
				});

				if ( !values.length )
					this.selectedIndex = -1;

			} else
				this.value = value;
		});
	},
	
	html: function( value ) {
		return value == undefined ?
			(this.length ?
				this[0].innerHTML :
				null) :
			this.empty().append( value );
	},

	replaceWith: function( value ) {
		return this.after( value ).remove();
	},

	eq: function( i ) {
		return this.slice( i, i + 1 );
	},

	slice: function() {
		return this.pushStack( Array.prototype.slice.apply( this, arguments ) );
	},

	map: function( callback ) {
		return this.pushStack( jQuery.map(this, function(elem, i){
			return callback.call( elem, i, elem );
		}));
	},

	andSelf: function() {
		return this.add( this.prevObject );
	},

	data: function( key, value ){
		var parts = key.split(".");
		parts[1] = parts[1] ? "." + parts[1] : "";

		if ( value === undefined ) {
			var data = this.triggerHandler("getData" + parts[1] + "!", [parts[0]]);
			
			if ( data === undefined && this.length )
				data = jQuery.data( this[0], key );

			return data === undefined && parts[1] ?
				this.data( parts[0] ) :
				data;
		} else
			return this.trigger("setData" + parts[1] + "!", [parts[0], value]).each(function(){
				jQuery.data( this, key, value );
			});
	},

	removeData: function( key ){
		return this.each(function(){
			jQuery.removeData( this, key );
		});
	},
	
	domManip: function( args, table, reverse, callback ) {
		var clone = this.length > 1, elems; 

		return this.each(function(){
			if ( !elems ) {
				elems = jQuery.clean( args, this.ownerDocument );

				if ( reverse )
					elems.reverse();
			}

			var obj = this;

			if ( table && jQuery.nodeName( this, "table" ) && jQuery.nodeName( elems[0], "tr" ) )
				obj = this.getElementsByTagName("tbody")[0] || this.appendChild( this.ownerDocument.createElement("tbody") );

			var scripts = jQuery( [] );

			jQuery.each(elems, function(){
				var elem = clone ?
					jQuery( this ).clone( true )[0] :
					this;

				// execute all scripts after the elements have been injected
				if ( jQuery.nodeName( elem, "script" ) ) {
					scripts = scripts.add( elem );
				} else {
					// Remove any inner scripts for later evaluation
					if ( elem.nodeType == 1 )
						scripts = scripts.add( jQuery( "script", elem ).remove() );

					// Inject the elements into the document
					callback.call( obj, elem );
				}
			});

			scripts.each( evalScript );
		});
	}
};

// Give the init function the jQuery prototype for later instantiation
jQuery.fn.init.prototype = jQuery.fn;

function evalScript( i, elem ) {
	if ( elem.src )
		jQuery.ajax({
			url: elem.src,
			async: false,
			dataType: "script"
		});

	else
		jQuery.globalEval( elem.text || elem.textContent || elem.innerHTML || "" );

	if ( elem.parentNode )
		elem.parentNode.removeChild( elem );
}

function now(){
	return +new Date;
}

jQuery.extend = jQuery.fn.extend = function() {
	// copy reference to target object
	var target = arguments[0] || {}, i = 1, length = arguments.length, deep = false, options;

	// Handle a deep copy situation
	if ( target.constructor == Boolean ) {
		deep = target;
		target = arguments[1] || {};
		// skip the boolean and the target
		i = 2;
	}

	// Handle case when target is a string or something (possible in deep copy)
	if ( typeof target != "object" && typeof target != "function" )
		target = {};

	// extend jQuery itself if only one argument is passed
	if ( length == i ) {
		target = this;
		--i;
	}

	for ( ; i < length; i++ )
		// Only deal with non-null/undefined values
		if ( (options = arguments[ i ]) != null )
			// Extend the base object
			for ( var name in options ) {
				var src = target[ name ], copy = options[ name ]; 
				
				// Prevent never-ending loop
				if ( target === copy )
					continue;

				// Recurse if we're merging object values
				if ( deep && copy && typeof copy == "object" && src && !copy.nodeType )
					target[ name ] = jQuery.extend( deep, src, copy );

				// Don't bring in undefined values
				else if ( copy !== undefined )
					target[ name ] = copy;

			}

	// Return the modified object
	return target;
};

var expando = "jQuery" + now(), uuid = 0, windowData = {},

// exclude the following css properties to add px
	exclude = /z-?index|font-?weight|opacity|zoom|line-?height/i,
// cache getComputedStyle
	getComputedStyle = document.defaultView && document.defaultView.getComputedStyle;

jQuery.extend({
	noConflict: function( deep ) {
		window.$ = _$;

		if ( deep )
			window.jQuery = _jQuery;

		return jQuery;
	},

	// See test/unit/core.js for details concerning this function.
	isFunction: function( fn ) {
		return !!fn && typeof fn != "string" && !fn.nodeName && 
			fn.constructor != Array && /function/i.test( fn + "" );
	},
	
	// check if an element is in a (or is an) XML document
	isXMLDoc: function( elem ) {
		return elem.documentElement && !elem.body ||
			elem.tagName && elem.ownerDocument && !elem.ownerDocument.body;
	},

	// Evalulates a script in a global context
	globalEval: function( data ) {
		data = jQuery.trim( data );

		if ( data ) {
			// Inspired by code by Andrea Giammarchi
			// http://webreflection.blogspot.com/2007/08/global-scope-evaluation-and-dom.html
			var head = document.getElementsByTagName("head")[0] || document.documentElement,
				script = document.createElement("script");

			script.type = "text/javascript";
			if ( jQuery.browser.msie )
				script.text = data;
			else
				script.appendChild( document.createTextNode( data ) );

			head.appendChild( script );
			head.removeChild( script );
		}
	},

	nodeName: function( elem, name ) {
		return elem.nodeName && elem.nodeName.toUpperCase() == name.toUpperCase();
	},
	
	cache: {},
	
	data: function( elem, name, data ) {
		elem = elem == window ?
			windowData :
			elem;

		var id = elem[ expando ];

		// Compute a unique ID for the element
		if ( !id ) 
			id = elem[ expando ] = ++uuid;

		// Only generate the data cache if we're
		// trying to access or manipulate it
		if ( name && !jQuery.cache[ id ] )
			jQuery.cache[ id ] = {};
		
		// Prevent overriding the named cache with undefined values
		if ( data !== undefined )
			jQuery.cache[ id ][ name ] = data;
		
		// Return the named cache data, or the ID for the element	
		return name ?
			jQuery.cache[ id ][ name ] :
			id;
	},
	
	removeData: function( elem, name ) {
		elem = elem == window ?
			windowData :
			elem;

		var id = elem[ expando ];

		// If we want to remove a specific section of the element's data
		if ( name ) {
			if ( jQuery.cache[ id ] ) {
				// Remove the section of cache data
				delete jQuery.cache[ id ][ name ];

				// If we've removed all the data, remove the element's cache
				name = "";

				for ( name in jQuery.cache[ id ] )
					break;

				if ( !name )
					jQuery.removeData( elem );
			}

		// Otherwise, we want to remove all of the element's data
		} else {
			// Clean up the element expando
			try {
				delete elem[ expando ];
			} catch(e){
				// IE has trouble directly removing the expando
				// but it's ok with using removeAttribute
				if ( elem.removeAttribute )
					elem.removeAttribute( expando );
			}

			// Completely remove the data cache
			delete jQuery.cache[ id ];
		}
	},

	// args is for internal usage only
	each: function( object, callback, args ) {
		if ( args ) {
			if ( object.length == undefined ) {
				for ( var name in object )
					if ( callback.apply( object[ name ], args ) === false )
						break;
			} else
				for ( var i = 0, length = object.length; i < length; i++ )
					if ( callback.apply( object[ i ], args ) === false )
						break;

		// A special, fast, case for the most common use of each
		} else {
			if ( object.length == undefined ) {
				for ( var name in object )
					if ( callback.call( object[ name ], name, object[ name ] ) === false )
						break;
			} else
				for ( var i = 0, length = object.length, value = object[0]; 
					i < length && callback.call( value, i, value ) !== false; value = object[++i] ){}
		}

		return object;
	},
	
	prop: function( elem, value, type, i, name ) {
			// Handle executable functions
			if ( jQuery.isFunction( value ) )
				value = value.call( elem, i );
				
			// Handle passing in a number to a CSS property
			return value && value.constructor == Number && type == "curCSS" && !exclude.test( name ) ?
				value + "px" :
				value;
	},

	className: {
		// internal only, use addClass("class")
		add: function( elem, classNames ) {
			jQuery.each((classNames || "").split(/\s+/), function(i, className){
				if ( elem.nodeType == 1 && !jQuery.className.has( elem.className, className ) )
					elem.className += (elem.className ? " " : "") + className;
			});
		},

		// internal only, use removeClass("class")
		remove: function( elem, classNames ) {
			if (elem.nodeType == 1)
				elem.className = classNames != undefined ?
					jQuery.grep(elem.className.split(/\s+/), function(className){
						return !jQuery.className.has( classNames, className );	
					}).join(" ") :
					"";
		},

		// internal only, use is(".class")
		has: function( elem, className ) {
			return jQuery.inArray( className, (elem.className || elem).toString().split(/\s+/) ) > -1;
		}
	},

	// A method for quickly swapping in/out CSS properties to get correct calculations
	swap: function( elem, options, callback ) {
		var old = {};
		// Remember the old values, and insert the new ones
		for ( var name in options ) {
			old[ name ] = elem.style[ name ];
			elem.style[ name ] = options[ name ];
		}

		callback.call( elem );

		// Revert the old values
		for ( var name in options )
			elem.style[ name ] = old[ name ];
	},

	css: function( elem, name, force ) {
		if ( name == "width" || name == "height" ) {
			var val, props = { position: "absolute", visibility: "hidden", display:"block" }, which = name == "width" ? [ "Left", "Right" ] : [ "Top", "Bottom" ];
		
			function getWH() {
				val = name == "width" ? elem.offsetWidth : elem.offsetHeight;
				var padding = 0, border = 0;
				jQuery.each( which, function() {
					padding += parseFloat(jQuery.curCSS( elem, "padding" + this, true)) || 0;
					border += parseFloat(jQuery.curCSS( elem, "border" + this + "Width", true)) || 0;
				});
				val -= Math.round(padding + border);
			}
		
			if ( jQuery(elem).is(":visible") )
				getWH();
			else
				jQuery.swap( elem, props, getWH );
			
			return Math.max(0, val);
		}
		
		return jQuery.curCSS( elem, name, force );
	},

	curCSS: function( elem, name, force ) {
		var ret;

		// A helper method for determining if an element's values are broken
		function color( elem ) {
			if ( !jQuery.browser.safari )
				return false;
			
			// getComputedStyle is cached
			var ret = getComputedStyle( elem, null );
			return !ret || ret.getPropertyValue("color") == "";
		}

		// We need to handle opacity special in IE
		if ( name == "opacity" && jQuery.browser.msie ) {
			ret = jQuery.attr( elem.style, "opacity" );

			return ret == "" ?
				"1" :
				ret;
		}
		// Opera sometimes will give the wrong display answer, this fixes it, see #2037
		if ( jQuery.browser.opera && name == "display" ) {
			var save = elem.style.outline;
			elem.style.outline = "0 solid black";
			elem.style.outline = save;
		}
		
		// Make sure we're using the right name for getting the float value
		if ( name.match( /float/i ) )
			name = styleFloat;

		if ( !force && elem.style && elem.style[ name ] )
			ret = elem.style[ name ];

		else if ( getComputedStyle ) {

			// Only "float" is needed here
			if ( name.match( /float/i ) )
				name = "float";

			name = name.replace( /([A-Z])/g, "-$1" ).toLowerCase();

			var computedStyle = getComputedStyle( elem, null );

			if ( computedStyle && !color( elem ) )
				ret = computedStyle.getPropertyValue( name );

			// If the element isn't reporting its values properly in Safari
			// then some display: none elements are involved
			else {
				var swap = [], stack = [], a = elem, i = 0;

				// Locate all of the parent display: none elements
				for ( ; a && color(a); a = a.parentNode )
					stack.unshift(a);

				// Go through and make them visible, but in reverse
				// (It would be better if we knew the exact display type that they had)
				for ( ; i < stack.length; i++ )
					if ( color( stack[ i ] ) ) {
						swap[ i ] = stack[ i ].style.display;
						stack[ i ].style.display = "block";
					}

				// Since we flip the display style, we have to handle that
				// one special, otherwise get the value
				ret = name == "display" && swap[ stack.length - 1 ] != null ?
					"none" :
					( computedStyle && computedStyle.getPropertyValue( name ) ) || "";

				// Finally, revert the display styles back
				for ( i = 0; i < swap.length; i++ )
					if ( swap[ i ] != null )
						stack[ i ].style.display = swap[ i ];
			}

			// We should always get a number back from opacity
			if ( name == "opacity" && ret == "" )
				ret = "1";

		} else if ( elem.currentStyle ) {
			var camelCase = name.replace(/\-(\w)/g, function(all, letter){
				return letter.toUpperCase();
			});

			ret = elem.currentStyle[ name ] || elem.currentStyle[ camelCase ];

			// From the awesome hack by Dean Edwards
			// http://erik.eae.net/archives/2007/07/27/18.54.15/#comment-102291

			// If we're not dealing with a regular pixel number
			// but a number that has a weird ending, we need to convert it to pixels
			if ( !/^\d+(px)?$/i.test( ret ) && /^\d/.test( ret ) ) {
				// Remember the original values
				var style = elem.style.left, runtimeStyle = elem.runtimeStyle.left;

				// Put in the new values to get a computed value out
				elem.runtimeStyle.left = elem.currentStyle.left;
				elem.style.left = ret || 0;
				ret = elem.style.pixelLeft + "px";

				// Revert the changed values
				elem.style.left = style;
				elem.runtimeStyle.left = runtimeStyle;
			}
		}

		return ret;
	},
	
	clean: function( elems, context ) {
		var ret = [];
		context = context || document;
		// !context.createElement fails in IE with an error but returns typeof 'object'
		if (typeof context.createElement == 'undefined') 
			context = context.ownerDocument || context[0] && context[0].ownerDocument || document;

		jQuery.each(elems, function(i, elem){
			if ( !elem )
				return;

			if ( elem.constructor == Number )
				elem += '';
			
			// Convert html string into DOM nodes
			if ( typeof elem == "string" ) {
				// Fix "XHTML"-style tags in all browsers
				elem = elem.replace(/(<(\w+)[^>]*?)\/>/g, function(all, front, tag){
					return tag.match(/^(abbr|br|col|img|input|link|meta|param|hr|area|embed)$/i) ?
						all :
						front + "></" + tag + ">";
				});

				// Trim whitespace, otherwise indexOf won't work as expected
				var tags = jQuery.trim( elem ).toLowerCase(), div = context.createElement("div");

				var wrap =
					// option or optgroup
					!tags.indexOf("<opt") &&
					[ 1, "<select multiple='multiple'>", "</select>" ] ||
					
					!tags.indexOf("<leg") &&
					[ 1, "<fieldset>", "</fieldset>" ] ||
					
					tags.match(/^<(thead|tbody|tfoot|colg|cap)/) &&
					[ 1, "<table>", "</table>" ] ||
					
					!tags.indexOf("<tr") &&
					[ 2, "<table><tbody>", "</tbody></table>" ] ||
					
				 	// <thead> matched above
					(!tags.indexOf("<td") || !tags.indexOf("<th")) &&
					[ 3, "<table><tbody><tr>", "</tr></tbody></table>" ] ||
					
					!tags.indexOf("<col") &&
					[ 2, "<table><tbody></tbody><colgroup>", "</colgroup></table>" ] ||

					// IE can't serialize <link> and <script> tags normally
					jQuery.browser.msie &&
					[ 1, "div<div>", "</div>" ] ||
					
					[ 0, "", "" ];

				// Go to html and back, then peel off extra wrappers
				div.innerHTML = wrap[1] + elem + wrap[2];
				
				// Move to the right depth
				while ( wrap[0]-- )
					div = div.lastChild;
				
				// Remove IE's autoinserted <tbody> from table fragments
				if ( jQuery.browser.msie ) {
					
					// String was a <table>, *may* have spurious <tbody>
					var tbody = !tags.indexOf("<table") && tags.indexOf("<tbody") < 0 ?
						div.firstChild && div.firstChild.childNodes :
						
						// String was a bare <thead> or <tfoot>
						wrap[1] == "<table>" && tags.indexOf("<tbody") < 0 ?
							div.childNodes :
							[];
				
					for ( var j = tbody.length - 1; j >= 0 ; --j )
						if ( jQuery.nodeName( tbody[ j ], "tbody" ) && !tbody[ j ].childNodes.length )
							tbody[ j ].parentNode.removeChild( tbody[ j ] );
					
					// IE completely kills leading whitespace when innerHTML is used	
					if ( /^\s/.test( elem ) )	
						div.insertBefore( context.createTextNode( elem.match(/^\s*/)[0] ), div.firstChild );
				
				}
				
				elem = jQuery.makeArray( div.childNodes );
			}

			if ( elem.length === 0 && (!jQuery.nodeName( elem, "form" ) && !jQuery.nodeName( elem, "select" )) )
				return;

			if ( elem[0] == undefined || jQuery.nodeName( elem, "form" ) || elem.options )
				ret.push( elem );

			else
				ret = jQuery.merge( ret, elem );

		});

		return ret;
	},
	
	attr: function( elem, name, value ) {
		// don't set attributes on text and comment nodes
		if (!elem || elem.nodeType == 3 || elem.nodeType == 8)
			return undefined;

		var fix = jQuery.isXMLDoc( elem ) ?
			{} :
			jQuery.props;

		// Safari mis-reports the default selected property of a hidden option
		// Accessing the parent's selectedIndex property fixes it
		if ( name == "selected" && jQuery.browser.safari )
			elem.parentNode.selectedIndex;
		
		// Certain attributes only work when accessed via the old DOM 0 way
		if ( fix[ name ] ) {
			if ( value != undefined )
				elem[ fix[ name ] ] = value;

			return elem[ fix[ name ] ];

		} else if ( jQuery.browser.msie && name == "style" )
			return jQuery.attr( elem.style, "cssText", value );

		else if ( value == undefined && jQuery.browser.msie && jQuery.nodeName( elem, "form" ) && (name == "action" || name == "method") )
			return elem.getAttributeNode( name ).nodeValue;

		// IE elem.getAttribute passes even for style
		else if ( elem.tagName ) {

			if ( value != undefined ) {
				// We can't allow the type property to be changed (since it causes problems in IE)
				if ( name == "type" && jQuery.nodeName( elem, "input" ) && elem.parentNode )
					throw "type property can't be changed";

				// convert the value to a string (all browsers do this but IE) see #1070
				elem.setAttribute( name, "" + value );
			}

			if ( jQuery.browser.msie && /href|src/.test( name ) && !jQuery.isXMLDoc( elem ) ) 
				return elem.getAttribute( name, 2 );

			return elem.getAttribute( name );

		// elem is actually elem.style ... set the style
		} else {
			// IE actually uses filters for opacity
			if ( name == "opacity" && jQuery.browser.msie ) {
				if ( value != undefined ) {
					// IE has trouble with opacity if it does not have layout
					// Force it by setting the zoom level
					elem.zoom = 1; 
	
					// Set the alpha filter to set the opacity
					elem.filter = (elem.filter || "").replace( /alpha\([^)]*\)/, "" ) +
						(parseFloat( value ).toString() == "NaN" ? "" : "alpha(opacity=" + value * 100 + ")");
				}
	
				return elem.filter && elem.filter.indexOf("opacity=") >= 0 ?
					(parseFloat( elem.filter.match(/opacity=([^)]*)/)[1] ) / 100).toString() :
					"";
			}

			name = name.replace(/-([a-z])/ig, function(all, letter){
				return letter.toUpperCase();
			});

			if ( value != undefined )
				elem[ name ] = value;

			return elem[ name ];
		}
	},
	
	trim: function( text ) {
		return (text || "").replace( /^\s+|\s+$/g, "" );
	},

	makeArray: function( array ) {
		var ret = [];

		if( array != undefined ){
			var i = array.length;
			//the window, strings and functions also have 'length'
			if( i != null && !array.split && array != window && !array.call )
				while( i )
					ret[--i] = array[i];
			else
				ret[0] = array;
		}

		return ret;
	},

	inArray: function( elem, array ) {
		for ( var i = 0, length = array.length; i < length; i++ )
			if ( array[ i ] == elem )
				return i;

		return -1;
	},

	merge: function( first, second ) {
		// We have to loop this way because IE & Opera overwrite the length
		// expando of getElementsByTagName

		// Also, we need to make sure that the correct elements are being returned
		// (IE returns comment nodes in a '*' query)
		if ( jQuery.browser.msie ) {
			for ( var i = 0; second[ i ]; i++ )
				if ( second[ i ].nodeType != 8 )
					first.push( second[ i ] );

		} else
			for ( var i = 0; second[ i ]; i++ )
				first.push( second[ i ] );

		return first;
	},

	unique: function( array ) {
		var ret = [], done = {};

		try {

			for ( var i = 0, length = array.length; i < length; i++ ) {
				var id = jQuery.data( array[ i ] );

				if ( !done[ id ] ) {
					done[ id ] = true;
					ret.push( array[ i ] );
				}
			}

		} catch( e ) {
			ret = array;
		}

		return ret;
	},

	grep: function( elems, callback, inv ) {
		var ret = [];

		// Go through the array, only saving the items
		// that pass the validator function
		for ( var i = 0, length = elems.length; i < length; i++ )
			if ( !inv && callback( elems[ i ], i ) || inv && !callback( elems[ i ], i ) )
				ret.push( elems[ i ] );

		return ret;
	},

	map: function( elems, callback ) {
		var ret = [];

		// Go through the array, translating each of the items to their
		// new value (or values).
		for ( var i = 0, length = elems.length; i < length; i++ ) {
			var value = callback( elems[ i ], i );

			if ( value !== null && value != undefined ) {
				if ( value.constructor != Array )
					value = [ value ];

				ret = ret.concat( value );
			}
		}

		return ret;
	}
});

var userAgent = navigator.userAgent.toLowerCase();

// Figure out what browser is being used
jQuery.browser = {
	version: (userAgent.match( /.+(?:rv|it|ra|ie)[\/: ]([\d.]+)/ ) || [])[1],
	safari: /webkit/.test( userAgent ),
	opera: /opera/.test( userAgent ),
	msie: /msie/.test( userAgent ) && !/opera/.test( userAgent ),
	mozilla: /mozilla/.test( userAgent ) && !/(compatible|webkit)/.test( userAgent )
};

var styleFloat = jQuery.browser.msie ?
	"styleFloat" :
	"cssFloat";
	
jQuery.extend({
	// Check to see if the W3C box model is being used
	boxModel: !jQuery.browser.msie || document.compatMode == "CSS1Compat",
	
	props: {
		"for": "htmlFor",
		"class": "className",
		"float": styleFloat,
		cssFloat: styleFloat,
		styleFloat: styleFloat,
		innerHTML: "innerHTML",
		className: "className",
		value: "value",
		disabled: "disabled",
		checked: "checked",
		readonly: "readOnly",
		selected: "selected",
		maxlength: "maxLength",
		selectedIndex: "selectedIndex",
		defaultValue: "defaultValue",
		tagName: "tagName",
		nodeName: "nodeName"
	}
});

jQuery.each({
	parent: function(elem){return elem.parentNode;},
	parents: function(elem){return jQuery.dir(elem,"parentNode");},
	next: function(elem){return jQuery.nth(elem,2,"nextSibling");},
	prev: function(elem){return jQuery.nth(elem,2,"previousSibling");},
	nextAll: function(elem){return jQuery.dir(elem,"nextSibling");},
	prevAll: function(elem){return jQuery.dir(elem,"previousSibling");},
	siblings: function(elem){return jQuery.sibling(elem.parentNode.firstChild,elem);},
	children: function(elem){return jQuery.sibling(elem.firstChild);},
	contents: function(elem){return jQuery.nodeName(elem,"iframe")?elem.contentDocument||elem.contentWindow.document:jQuery.makeArray(elem.childNodes);}
}, function(name, fn){
	jQuery.fn[ name ] = function( selector ) {
		var ret = jQuery.map( this, fn );

		if ( selector && typeof selector == "string" )
			ret = jQuery.multiFilter( selector, ret );

		return this.pushStack( jQuery.unique( ret ) );
	};
});

jQuery.each({
	appendTo: "append",
	prependTo: "prepend",
	insertBefore: "before",
	insertAfter: "after",
	replaceAll: "replaceWith"
}, function(name, original){
	jQuery.fn[ name ] = function() {
		var args = arguments;

		return this.each(function(){
			for ( var i = 0, length = args.length; i < length; i++ )
				jQuery( args[ i ] )[ original ]( this );
		});
	};
});

jQuery.each({
	removeAttr: function( name ) {
		jQuery.attr( this, name, "" );
		if (this.nodeType == 1) 
			this.removeAttribute( name );
	},

	addClass: function( classNames ) {
		jQuery.className.add( this, classNames );
	},

	removeClass: function( classNames ) {
		jQuery.className.remove( this, classNames );
	},

	toggleClass: function( classNames ) {
		jQuery.className[ jQuery.className.has( this, classNames ) ? "remove" : "add" ]( this, classNames );
	},

	remove: function( selector ) {
		if ( !selector || jQuery.filter( selector, [ this ] ).r.length ) {
			// Prevent memory leaks
			jQuery( "*", this ).add(this).each(function(){
				jQuery.event.remove(this);
				jQuery.removeData(this);
			});
			if (this.parentNode)
				this.parentNode.removeChild( this );
		}
	},

	empty: function() {
		// Remove element nodes and prevent memory leaks
		jQuery( ">*", this ).remove();
		
		// Remove any remaining nodes
		while ( this.firstChild )
			this.removeChild( this.firstChild );
	}
}, function(name, fn){
	jQuery.fn[ name ] = function(){
		return this.each( fn, arguments );
	};
});

jQuery.each([ "Height", "Width" ], function(i, name){
	var type = name.toLowerCase();
	
	jQuery.fn[ type ] = function( size ) {
		// Get window width or height
		return this[0] == window ?
			// Opera reports document.body.client[Width/Height] properly in both quirks and standards
			jQuery.browser.opera && document.body[ "client" + name ] || 
			
			// Safari reports inner[Width/Height] just fine (Mozilla and Opera include scroll bar widths)
			jQuery.browser.safari && window[ "inner" + name ] ||
			
			// Everyone else use document.documentElement or document.body depending on Quirks vs Standards mode
			document.compatMode == "CSS1Compat" && document.documentElement[ "client" + name ] || document.body[ "client" + name ] :
		
			// Get document width or height
			this[0] == document ?
				// Either scroll[Width/Height] or offset[Width/Height], whichever is greater
				Math.max( 
					Math.max(document.body["scroll" + name], document.documentElement["scroll" + name]), 
					Math.max(document.body["offset" + name], document.documentElement["offset" + name]) 
				) :

				// Get or set width or height on the element
				size == undefined ?
					// Get width or height on the element
					(this.length ? jQuery.css( this[0], type ) : null) :

					// Set the width or height on the element (default to pixels if value is unitless)
					this.css( type, size.constructor == String ? size : size + "px" );
	};
});
var chars = jQuery.browser.safari && parseInt(jQuery.browser.version) < 417 ?
		"(?:[\\w*_-]|\\\\.)" :
		"(?:[\\w\u0128-\uFFFF*_-]|\\\\.)",
	quickChild = new RegExp("^>\\s*(" + chars + "+)"),
	quickID = new RegExp("^(" + chars + "+)(#)(" + chars + "+)"),
	quickClass = new RegExp("^([#.]?)(" + chars + "*)");

jQuery.extend({
	expr: {
		"": function(a,i,m){return m[2]=="*"||jQuery.nodeName(a,m[2]);},
		"#": function(a,i,m){return a.getAttribute("id")==m[2];},
		":": {
			// Position Checks
			lt: function(a,i,m){return i<m[3]-0;},
			gt: function(a,i,m){return i>m[3]-0;},
			nth: function(a,i,m){return m[3]-0==i;},
			eq: function(a,i,m){return m[3]-0==i;},
			first: function(a,i){return i==0;},
			last: function(a,i,m,r){return i==r.length-1;},
			even: function(a,i){return i%2==0;},
			odd: function(a,i){return i%2;},

			// Child Checks
			"first-child": function(a){return a.parentNode.getElementsByTagName("*")[0]==a;},
			"last-child": function(a){return jQuery.nth(a.parentNode.lastChild,1,"previousSibling")==a;},
			"only-child": function(a){return !jQuery.nth(a.parentNode.lastChild,2,"previousSibling");},

			// Parent Checks
			parent: function(a){return a.firstChild;},
			empty: function(a){return !a.firstChild;},

			// Text Check
			contains: function(a,i,m){return (a.textContent||a.innerText||jQuery(a).text()||"").indexOf(m[3])>=0;},

			// Visibility
			visible: function(a){return "hidden"!=a.type&&jQuery.css(a,"display")!="none"&&jQuery.css(a,"visibility")!="hidden";},
			hidden: function(a){return "hidden"==a.type||jQuery.css(a,"display")=="none"||jQuery.css(a,"visibility")=="hidden";},

			// Form attributes
			enabled: function(a){return !a.disabled;},
			disabled: function(a){return a.disabled;},
			checked: function(a){return a.checked;},
			selected: function(a){return a.selected||jQuery.attr(a,"selected");},

			// Form elements
			text: function(a){return "text"==a.type;},
			radio: function(a){return "radio"==a.type;},
			checkbox: function(a){return "checkbox"==a.type;},
			file: function(a){return "file"==a.type;},
			password: function(a){return "password"==a.type;},
			submit: function(a){return "submit"==a.type;},
			image: function(a){return "image"==a.type;},
			reset: function(a){return "reset"==a.type;},
			button: function(a){return "button"==a.type||jQuery.nodeName(a,"button");},
			input: function(a){return /input|select|textarea|button/i.test(a.nodeName);},

			// :has()
			has: function(a,i,m){return jQuery.find(m[3],a).length;},

			// :header
			header: function(a){return /h\d/i.test(a.nodeName);},

			// :animated
			animated: function(a){return jQuery.grep(jQuery.timers,function(fn){return a==fn.elem;}).length;}
		}
	},
	
	// The regular expressions that power the parsing engine
	parse: [
		// Match: [@value='test'], [@foo]
		/^(\[) *@?([\w-]+) *([!*$^~=]*) *('?"?)(.*?)\4 *\]/,

		// Match: :contains('foo')
		/^(:)([\w-]+)\("?'?(.*?(\(.*?\))?[^(]*?)"?'?\)/,

		// Match: :even, :last-chlid, #id, .class
		new RegExp("^([:.#]*)(" + chars + "+)")
	],

	multiFilter: function( expr, elems, not ) {
		var old, cur = [];

		while ( expr && expr != old ) {
			old = expr;
			var f = jQuery.filter( expr, elems, not );
			expr = f.t.replace(/^\s*,\s*/, "" );
			cur = not ? elems = f.r : jQuery.merge( cur, f.r );
		}

		return cur;
	},

	find: function( t, context ) {
		// Quickly handle non-string expressions
		if ( typeof t != "string" )
			return [ t ];

		// check to make sure context is a DOM element or a document
		if ( context && context.nodeType != 1 && context.nodeType != 9)
			return [ ];

		// Set the correct context (if none is provided)
		context = context || document;

		// Initialize the search
		var ret = [context], done = [], last, nodeName;

		// Continue while a selector expression exists, and while
		// we're no longer looping upon ourselves
		while ( t && last != t ) {
			var r = [];
			last = t;

			t = jQuery.trim(t);

			var foundToken = false,

			// An attempt at speeding up child selectors that
			// point to a specific element tag
				re = quickChild,
				
				m = re.exec(t);

			if ( m ) {
				nodeName = m[1].toUpperCase();

				// Perform our own iteration and filter
				for ( var i = 0; ret[i]; i++ )
					for ( var c = ret[i].firstChild; c; c = c.nextSibling )
						if ( c.nodeType == 1 && (nodeName == "*" || c.nodeName.toUpperCase() == nodeName) )
							r.push( c );

				ret = r;
				t = t.replace( re, "" );
				if ( t.indexOf(" ") == 0 ) continue;
				foundToken = true;
			} else {
				re = /^([>+~])\s*(\w*)/i;

				if ( (m = re.exec(t)) != null ) {
					r = [];

					var merge = {};
					nodeName = m[2].toUpperCase();
					m = m[1];

					for ( var j = 0, rl = ret.length; j < rl; j++ ) {
						var n = m == "~" || m == "+" ? ret[j].nextSibling : ret[j].firstChild;
						for ( ; n; n = n.nextSibling )
							if ( n.nodeType == 1 ) {
								var id = jQuery.data(n);

								if ( m == "~" && merge[id] ) break;
								
								if (!nodeName || n.nodeName.toUpperCase() == nodeName ) {
									if ( m == "~" ) merge[id] = true;
									r.push( n );
								}
								
								if ( m == "+" ) break;
							}
					}

					ret = r;

					// And remove the token
					t = jQuery.trim( t.replace( re, "" ) );
					foundToken = true;
				}
			}

			// See if there's still an expression, and that we haven't already
			// matched a token
			if ( t && !foundToken ) {
				// Handle multiple expressions
				if ( !t.indexOf(",") ) {
					// Clean the result set
					if ( context == ret[0] ) ret.shift();

					// Merge the result sets
					done = jQuery.merge( done, ret );

					// Reset the context
					r = ret = [context];

					// Touch up the selector string
					t = " " + t.substr(1,t.length);

				} else {
					// Optimize for the case nodeName#idName
					var re2 = quickID;
					var m = re2.exec(t);
					
					// Re-organize the results, so that they're consistent
					if ( m ) {
						m = [ 0, m[2], m[3], m[1] ];

					} else {
						// Otherwise, do a traditional filter check for
						// ID, class, and element selectors
						re2 = quickClass;
						m = re2.exec(t);
					}

					m[2] = m[2].replace(/\\/g, "");

					var elem = ret[ret.length-1];

					// Try to do a global search by ID, where we can
					if ( m[1] == "#" && elem && elem.getElementById && !jQuery.isXMLDoc(elem) ) {
						// Optimization for HTML document case
						var oid = elem.getElementById(m[2]);
						
						// Do a quick check for the existence of the actual ID attribute
						// to avoid selecting by the name attribute in IE
						// also check to insure id is a string to avoid selecting an element with the name of 'id' inside a form
						if ( (jQuery.browser.msie||jQuery.browser.opera) && oid && typeof oid.id == "string" && oid.id != m[2] )
							oid = jQuery('[@id="'+m[2]+'"]', elem)[0];

						// Do a quick check for node name (where applicable) so
						// that div#foo searches will be really fast
						ret = r = oid && (!m[3] || jQuery.nodeName(oid, m[3])) ? [oid] : [];
					} else {
						// We need to find all descendant elements
						for ( var i = 0; ret[i]; i++ ) {
							// Grab the tag name being searched for
							var tag = m[1] == "#" && m[3] ? m[3] : m[1] != "" || m[0] == "" ? "*" : m[2];

							// Handle IE7 being really dumb about <object>s
							if ( tag == "*" && ret[i].nodeName.toLowerCase() == "object" )
								tag = "param";

							r = jQuery.merge( r, ret[i].getElementsByTagName( tag ));
						}

						// It's faster to filter by class and be done with it
						if ( m[1] == "." )
							r = jQuery.classFilter( r, m[2] );

						// Same with ID filtering
						if ( m[1] == "#" ) {
							var tmp = [];

							// Try to find the element with the ID
							for ( var i = 0; r[i]; i++ )
								if ( r[i].getAttribute("id") == m[2] ) {
									tmp = [ r[i] ];
									break;
								}

							r = tmp;
						}

						ret = r;
					}

					t = t.replace( re2, "" );
				}

			}

			// If a selector string still exists
			if ( t ) {
				// Attempt to filter it
				var val = jQuery.filter(t,r);
				ret = r = val.r;
				t = jQuery.trim(val.t);
			}
		}

		// An error occurred with the selector;
		// just return an empty set instead
		if ( t )
			ret = [];

		// Remove the root context
		if ( ret && context == ret[0] )
			ret.shift();

		// And combine the results
		done = jQuery.merge( done, ret );

		return done;
	},

	classFilter: function(r,m,not){
		m = " " + m + " ";
		var tmp = [];
		for ( var i = 0; r[i]; i++ ) {
			var pass = (" " + r[i].className + " ").indexOf( m ) >= 0;
			if ( !not && pass || not && !pass )
				tmp.push( r[i] );
		}
		return tmp;
	},

	filter: function(t,r,not) {
		var last;

		// Look for common filter expressions
		while ( t && t != last ) {
			last = t;

			var p = jQuery.parse, m;

			for ( var i = 0; p[i]; i++ ) {
				m = p[i].exec( t );

				if ( m ) {
					// Remove what we just matched
					t = t.substring( m[0].length );

					m[2] = m[2].replace(/\\/g, "");
					break;
				}
			}

			if ( !m )
				break;

			// :not() is a special case that can be optimized by
			// keeping it out of the expression list
			if ( m[1] == ":" && m[2] == "not" )
				// optimize if only one selector found (most common case)
				r = isSimple.test( m[3] ) ?
					jQuery.filter(m[3], r, true).r :
					jQuery( r ).not( m[3] );

			// We can get a big speed boost by filtering by class here
			else if ( m[1] == "." )
				r = jQuery.classFilter(r, m[2], not);

			else if ( m[1] == "[" ) {
				var type = m[3];
				
				// special case, filter by exact name
				if ( !not && m[2] == 'name' && type == '=' )
					r = jQuery.grep( document.getElementsByName(m[5]), function(elem){
						return jQuery.inArray( elem, r ) != -1;	
					});
				else {
					for ( var i = 0, rl = r.length, tmp = []; i < rl; i++ ) {
						var a = r[i], z = a[ jQuery.props[m[2]] || m[2] ];
						
						if ( z == null || /href|src|selected/.test(m[2]) )
							z = jQuery.attr(a,m[2]) || '';
	
						if ( (type == "" && !!z ||
							 type == "=" && z == m[5] ||
							 type == "!=" && z != m[5] ||
							 type == "^=" && z && !z.indexOf(m[5]) ||
							 type == "$=" && z.substr(z.length - m[5].length) == m[5] ||
							 (type == "*=" || type == "~=") && z.indexOf(m[5]) >= 0) ^ not )
								tmp.push( a );
					}					
					r = tmp;
				}

			// We can get a speed boost by handling nth-child here
			} else if ( m[1] == ":" && m[2] == "nth-child" ) {
				var merge = {}, tmp = [],
					// parse equations like 'even', 'odd', '5', '2n', '3n+2', '4n-1', '-n+6'
					test = /(-?)(\d*)n((?:\+|-)?\d*)/.exec(
						m[3] == "even" && "2n" || m[3] == "odd" && "2n+1" ||
						!/\D/.test(m[3]) && "0n+" + m[3] || m[3]),
					// calculate the numbers (first)n+(last) including if they are negative
					first = (test[1] + (test[2] || 1)) - 0, last = test[3] - 0;
 
				// loop through all the elements left in the jQuery object
				for ( var i = 0, rl = r.length; i < rl; i++ ) {
					var node = r[i], parentNode = node.parentNode, id = jQuery.data(parentNode);

					if ( !merge[id] ) {
						var c = 1;

						for ( var n = parentNode.firstChild; n; n = n.nextSibling )
							if ( n.nodeType == 1 )
								n.nodeIndex = c++;

						merge[id] = true;
					}

					var add = false;

					if ( first == 0 ) {
						if ( node.nodeIndex == last )
							add = true;
					} else if ( (node.nodeIndex - last) % first == 0 && (node.nodeIndex - last) / first >= 0 )
						add = true;

					if ( add ^ not )
						tmp.push( node );
				}

				r = tmp;

			// Otherwise, find the expression to execute
			} else {
				var fn = jQuery.expr[ m[1] ];
				if ( typeof fn == "object" )
					fn = fn[ m[2] ];

				if ( typeof fn == "string" )
					fn = eval("false||function(a,i){return " + fn + ";}");

				// Execute it against the current filter
				r = jQuery.grep( r, function(elem, i){
					return fn(elem, i, m, r);
				}, not );
			}
		}

		// Return an array of filtered elements (r)
		// and the modified expression string (t)
		return { r: r, t: t };
	},

	dir: function( elem, dir ){
		var matched = [],
			cur = elem[dir];
		while ( cur && cur != document ) {
			if ( cur.nodeType == 1 )
				matched.push( cur );
			cur = cur[dir];
		}
		return matched;
	},
	
	nth: function(cur,result,dir,elem){
		result = result || 1;
		var num = 0;

		for ( ; cur; cur = cur[dir] )
			if ( cur.nodeType == 1 && ++num == result )
				break;

		return cur;
	},
	
	sibling: function( n, elem ) {
		var r = [];

		for ( ; n; n = n.nextSibling ) {
			if ( n.nodeType == 1 && n != elem )
				r.push( n );
		}

		return r;
	}
});
/*
 * A number of helper functions used for managing events.
 * Many of the ideas behind this code orignated from 
 * Dean Edwards' addEvent library.
 */
jQuery.event = {

	// Bind an event to an element
	// Original by Dean Edwards
	add: function(elem, types, handler, data) {
		if ( elem.nodeType == 3 || elem.nodeType == 8 )
			return;

		// For whatever reason, IE has trouble passing the window object
		// around, causing it to be cloned in the process
		if ( jQuery.browser.msie && elem.setInterval )
			elem = window;

		// Make sure that the function being executed has a unique ID
		if ( !handler.guid )
			handler.guid = this.guid++;
			
		// if data is passed, bind to handler 
		if( data != undefined ) { 
			// Create temporary function pointer to original handler 
			var fn = handler; 

			// Create unique handler function, wrapped around original handler 
			handler = this.proxy( fn, function() { 
				// Pass arguments and context to original handler 
				return fn.apply(this, arguments); 
			});

			// Store data in unique handler 
			handler.data = data;

			// Set the guid of unique handler to the same of original handler, so it can be removed 
			handler.guid = fn.guid;
		}

		// Init the element's event structure
		var events = jQuery.data(elem, "events") || jQuery.data(elem, "events", {}),
			handle = jQuery.data(elem, "handle") || jQuery.data(elem, "handle", function(){
				// Handle the second event of a trigger and when
				// an event is called after a page has unloaded
				if ( typeof jQuery != "undefined" && !jQuery.event.triggered )
					return jQuery.event.handle.apply(arguments.callee.elem, arguments);
			});
		// Add elem as a property of the handle function
		// This is to prevent a memory leak with non-native
		// event in IE.
		handle.elem = elem;
			
		// Handle multiple events separated by a space
		// jQuery(...).bind("mouseover mouseout", fn);
		jQuery.each(types.split(/\s+/), function(index, type) {
			// Namespaced event handlers
			var parts = type.split(".");
			type = parts[0];
			handler.type = parts[1];

			// Get the current list of functions bound to this event
			var handlers = events[type];

			// Init the event handler queue
			if (!handlers) {
				handlers = events[type] = {};
	
				// Check for a special event handler
				// Only use addEventListener/attachEvent if the special
				// events handler returns false
				if ( !jQuery.event.special[type] || jQuery.event.special[type].setup.call(elem) === false ) {
					// Bind the global event handler to the element
					if (elem.addEventListener)
						elem.addEventListener(type, handle, false);
					else if (elem.attachEvent)
						elem.attachEvent("on" + type, handle);
				}
			}

			// Add the function to the element's handler list
			handlers[handler.guid] = handler;

			// Keep track of which events have been used, for global triggering
			jQuery.event.global[type] = true;
		});
		
		// Nullify elem to prevent memory leaks in IE
		elem = null;
	},

	guid: 1,
	global: {},

	// Detach an event or set of events from an element
	remove: function(elem, types, handler) {
		// don't do events on text and comment nodes
		if ( elem.nodeType == 3 || elem.nodeType == 8 )
			return;

		var events = jQuery.data(elem, "events"), ret, index;

		if ( events ) {
			// Unbind all events for the element
			if ( types == undefined || (typeof types == "string" && types.charAt(0) == ".") )
				for ( var type in events )
					this.remove( elem, type + (types || "") );
			else {
				// types is actually an event object here
				if ( types.type ) {
					handler = types.handler;
					types = types.type;
				}
				
				// Handle multiple events seperated by a space
				// jQuery(...).unbind("mouseover mouseout", fn);
				jQuery.each(types.split(/\s+/), function(index, type){
					// Namespaced event handlers
					var parts = type.split(".");
					type = parts[0];
					
					if ( events[type] ) {
						// remove the given handler for the given type
						if ( handler )
							delete events[type][handler.guid];
			
						// remove all handlers for the given type
						else
							for ( handler in events[type] )
								// Handle the removal of namespaced events
								if ( !parts[1] || events[type][handler].type == parts[1] )
									delete events[type][handler];

						// remove generic event handler if no more handlers exist
						for ( ret in events[type] ) break;
						if ( !ret ) {
							if ( !jQuery.event.special[type] || jQuery.event.special[type].teardown.call(elem) === false ) {
								if (elem.removeEventListener)
									elem.removeEventListener(type, jQuery.data(elem, "handle"), false);
								else if (elem.detachEvent)
									elem.detachEvent("on" + type, jQuery.data(elem, "handle"));
							}
							ret = null;
							delete events[type];
						}
					}
				});
			}

			// Remove the expando if it's no longer used
			for ( ret in events ) break;
			if ( !ret ) {
				var handle = jQuery.data( elem, "handle" );
				if ( handle ) handle.elem = null;
				jQuery.removeData( elem, "events" );
				jQuery.removeData( elem, "handle" );
			}
		}
	},

	trigger: function(type, data, elem, donative, extra) {
		// Clone the incoming data, if any
		data = jQuery.makeArray(data);

		if ( type.indexOf("!") >= 0 ) {
			type = type.slice(0, -1);
			var exclusive = true;
		}

		// Handle a global trigger
		if ( !elem ) {
			// Only trigger if we've ever bound an event for it
			if ( this.global[type] )
				jQuery("*").add([window, document]).trigger(type, data);

		// Handle triggering a single element
		} else {
			// don't do events on text and comment nodes
			if ( elem.nodeType == 3 || elem.nodeType == 8 )
				return undefined;

			var val, ret, fn = jQuery.isFunction( elem[ type ] || null ),
				// Check to see if we need to provide a fake event, or not
				event = !data[0] || !data[0].preventDefault;
			
			// Pass along a fake event
			if ( event ) {
				data.unshift({ 
					type: type, 
					target: elem, 
					preventDefault: function(){}, 
					stopPropagation: function(){}, 
					timeStamp: now()
				});
				data[0][expando] = true; // no need to fix fake event
			}

			// Enforce the right trigger type
			data[0].type = type;
			if ( exclusive )
				data[0].exclusive = true;

			// Trigger the event, it is assumed that "handle" is a function
			var handle = jQuery.data(elem, "handle"); 
			if ( handle ) 
				val = handle.apply( elem, data );

			// Handle triggering native .onfoo handlers (and on links since we don't call .click() for links)
			if ( (!fn || (jQuery.nodeName(elem, 'a') && type == "click")) && elem["on"+type] && elem["on"+type].apply( elem, data ) === false )
				val = false;

			// Extra functions don't get the custom event object
			if ( event )
				data.shift();

			// Handle triggering of extra function
			if ( extra && jQuery.isFunction( extra ) ) {
				// call the extra function and tack the current return value on the end for possible inspection
				ret = extra.apply( elem, val == null ? data : data.concat( val ) );
				// if anything is returned, give it precedence and have it overwrite the previous value
				if (ret !== undefined)
					val = ret;
			}

			// Trigger the native events (except for clicks on links)
			if ( fn && donative !== false && val !== false && !(jQuery.nodeName(elem, 'a') && type == "click") ) {
				this.triggered = true;
				try {
					elem[ type ]();
				// prevent IE from throwing an error for some hidden elements
				} catch (e) {}
			}

			this.triggered = false;
		}

		return val;
	},

	handle: function(event) {
		// returned undefined or false
		var val, ret, namespace, all, handlers;

		event = arguments[0] = jQuery.event.fix( event || window.event );

		// Namespaced event handlers
		namespace = event.type.split(".");
		event.type = namespace[0];
		namespace = namespace[1];
		all = !namespace && !event.exclusive; //cache this now, all = true means, any handler

		handlers = ( jQuery.data(this, "events") || {} )[event.type];

		for ( var j in handlers ) {
			var handler = handlers[j];

			// Filter the functions by class
			if ( all || handler.type == namespace ) {
				// Pass in a reference to the handler function itself
				// So that we can later remove it
				event.handler = handler;
				event.data = handler.data;
				
				ret = handler.apply( this, arguments );

				if ( val !== false )
					val = ret;

				if ( ret === false ) {
					event.preventDefault();
					event.stopPropagation();
				}
			}
		}

		return val;
	},

	fix: function(event) {
		if ( event[expando] == true ) 
			return event;
		
		// store a copy of the original event object 
		// and "clone" to set read-only properties
		var originalEvent = event;
		event = { originalEvent: originalEvent };
		var props = "altKey attrChange attrName bubbles button cancelable charCode clientX clientY ctrlKey currentTarget data detail eventPhase fromElement handler keyCode metaKey newValue pageX pageY prevValue relatedNode relatedTarget screenX screenY shiftKey srcElement target timeStamp toElement type view wheelDelta which".split(" ");
		for ( var i=props.length; i; i-- )
			event[ props[i] ] = originalEvent[ props[i] ];
		
		// Mark it as fixed
		event[expando] = true;
		
		// add preventDefault and stopPropagation since 
		// they will not work on the clone
		event.preventDefault = function() {
			// if preventDefault exists run it on the original event
			if (originalEvent.preventDefault)
				originalEvent.preventDefault();
			// otherwise set the returnValue property of the original event to false (IE)
			originalEvent.returnValue = false;
		};
		event.stopPropagation = function() {
			// if stopPropagation exists run it on the original event
			if (originalEvent.stopPropagation)
				originalEvent.stopPropagation();
			// otherwise set the cancelBubble property of the original event to true (IE)
			originalEvent.cancelBubble = true;
		};
		
		// Fix timeStamp
		event.timeStamp = event.timeStamp || now();
		
		// Fix target property, if necessary
		if ( !event.target )
			event.target = event.srcElement || document; // Fixes #1925 where srcElement might not be defined either
				
		// check if target is a textnode (safari)
		if ( event.target.nodeType == 3 )
			event.target = event.target.parentNode;

		// Add relatedTarget, if necessary
		if ( !event.relatedTarget && event.fromElement )
			event.relatedTarget = event.fromElement == event.target ? event.toElement : event.fromElement;

		// Calculate pageX/Y if missing and clientX/Y available
		if ( event.pageX == null && event.clientX != null ) {
			var doc = document.documentElement, body = document.body;
			event.pageX = event.clientX + (doc && doc.scrollLeft || body && body.scrollLeft || 0) - (doc.clientLeft || 0);
			event.pageY = event.clientY + (doc && doc.scrollTop || body && body.scrollTop || 0) - (doc.clientTop || 0);
		}
			
		// Add which for key events
		if ( !event.which && ((event.charCode || event.charCode === 0) ? event.charCode : event.keyCode) )
			event.which = event.charCode || event.keyCode;
		
		// Add metaKey to non-Mac browsers (use ctrl for PC's and Meta for Macs)
		if ( !event.metaKey && event.ctrlKey )
			event.metaKey = event.ctrlKey;

		// Add which for click: 1 == left; 2 == middle; 3 == right
		// Note: button is not normalized, so don't use it
		if ( !event.which && event.button )
			event.which = (event.button & 1 ? 1 : ( event.button & 2 ? 3 : ( event.button & 4 ? 2 : 0 ) ));
			
		return event;
	},
	
	proxy: function( fn, proxy ){
		// Set the guid of unique handler to the same of original handler, so it can be removed 
		proxy.guid = fn.guid = fn.guid || proxy.guid || this.guid++;
		return proxy;//so proxy can be declared as an argument
	},
	
	special: {
		ready: {
			setup: function() {
				// Make sure the ready event is setup
				bindReady();
				return;
			},
			
			teardown: function() { return; }
		},
		
		mouseenter: {
			setup: function() {
				if ( jQuery.browser.msie ) return false;
				jQuery(this).bind("mouseover", jQuery.event.special.mouseenter.handler);
				return true;
			},
		
			teardown: function() {
				if ( jQuery.browser.msie ) return false;
				jQuery(this).unbind("mouseover", jQuery.event.special.mouseenter.handler);
				return true;
			},
			
			handler: function(event) {
				// If we actually just moused on to a sub-element, ignore it
				if ( withinElement(event, this) ) return true;
				// Execute the right handlers by setting the event type to mouseenter
				event.type = "mouseenter";
				return jQuery.event.handle.apply(this, arguments);
			}
		},
	
		mouseleave: {
			setup: function() {
				if ( jQuery.browser.msie ) return false;
				jQuery(this).bind("mouseout", jQuery.event.special.mouseleave.handler);
				return true;
			},
		
			teardown: function() {
				if ( jQuery.browser.msie ) return false;
				jQuery(this).unbind("mouseout", jQuery.event.special.mouseleave.handler);
				return true;
			},
			
			handler: function(event) {
				// If we actually just moused on to a sub-element, ignore it
				if ( withinElement(event, this) ) return true;
				// Execute the right handlers by setting the event type to mouseleave
				event.type = "mouseleave";
				return jQuery.event.handle.apply(this, arguments);
			}
		}
	}
};

jQuery.fn.extend({
	bind: function( type, data, fn ) {
		return type == "unload" ? this.one(type, data, fn) : this.each(function(){
			jQuery.event.add( this, type, fn || data, fn && data );
		});
	},
	
	one: function( type, data, fn ) {
		var one = jQuery.event.proxy( fn || data, function(event) {
			jQuery(this).unbind(event, one);
			return (fn || data).apply( this, arguments );
		});
		return this.each(function(){
			jQuery.event.add( this, type, one, fn && data);
		});
	},

	unbind: function( type, fn ) {
		return this.each(function(){
			jQuery.event.remove( this, type, fn );
		});
	},

	trigger: function( type, data, fn ) {
		return this.each(function(){
			jQuery.event.trigger( type, data, this, true, fn );
		});
	},

	triggerHandler: function( type, data, fn ) {
		return this[0] && jQuery.event.trigger( type, data, this[0], false, fn );
	},

	toggle: function( fn ) {
		// Save reference to arguments for access in closure
		var args = arguments, i = 1;

		// link all the functions, so any of them can unbind this click handler
		while( i < args.length )
			jQuery.event.proxy( fn, args[i++] );

		return this.click( jQuery.event.proxy( fn, function(event) {
			// Figure out which function to execute
			this.lastToggle = ( this.lastToggle || 0 ) % i;
			
			// Make sure that clicks stop
			event.preventDefault();
			
			// and execute the function
			return args[ this.lastToggle++ ].apply( this, arguments ) || false;
		}));
	},

	hover: function(fnOver, fnOut) {
		return this.bind('mouseenter', fnOver).bind('mouseleave', fnOut);
	},
	
	ready: function(fn) {
		// Attach the listeners
		bindReady();

		// If the DOM is already ready
		if ( jQuery.isReady )
			// Execute the function immediately
			fn.call( document, jQuery );
			
		// Otherwise, remember the function for later
		else
			// Add the function to the wait list
			jQuery.readyList.push( function() { return fn.call(this, jQuery); } );
	
		return this;
	}
});

jQuery.extend({
	isReady: false,
	readyList: [],
	// Handle when the DOM is ready
	ready: function() {
		// Make sure that the DOM is not already loaded
		if ( !jQuery.isReady ) {
			// Remember that the DOM is ready
			jQuery.isReady = true;
			
			// If there are functions bound, to execute
			if ( jQuery.readyList ) {
				// Execute all of them
				jQuery.each( jQuery.readyList, function(){
					this.apply( document );
				});
				
				// Reset the list of functions
				jQuery.readyList = null;
			}
		
			// Trigger any bound ready events
			jQuery(document).triggerHandler("ready");
		}
	}
});

var readyBound = false;

function bindReady(){
	if ( readyBound ) return;
	readyBound = true;

	// Mozilla, Opera (see further below for it) and webkit nightlies currently support this event
	if ( document.addEventListener && !jQuery.browser.opera)
		// Use the handy event callback
		document.addEventListener( "DOMContentLoaded", jQuery.ready, false );
	
	// If IE is used and is not in a frame
	// Continually check to see if the document is ready
	if ( jQuery.browser.msie && window == top ) (function(){
		if (jQuery.isReady) return;
		try {
			// If IE is used, use the trick by Diego Perini
			// http://javascript.nwbox.com/IEContentLoaded/
			document.documentElement.doScroll("left");
		} catch( error ) {
			setTimeout( arguments.callee, 0 );
			return;
		}
		// and execute any waiting functions
		jQuery.ready();
	})();

	if ( jQuery.browser.opera )
		document.addEventListener( "DOMContentLoaded", function () {
			if (jQuery.isReady) return;
			for (var i = 0; i < document.styleSheets.length; i++)
				if (document.styleSheets[i].disabled) {
					setTimeout( arguments.callee, 0 );
					return;
				}
			// and execute any waiting functions
			jQuery.ready();
		}, false);

	if ( jQuery.browser.safari ) {
		var numStyles;
		(function(){
			if (jQuery.isReady) return;
			if ( document.readyState != "loaded" && document.readyState != "complete" ) {
				setTimeout( arguments.callee, 0 );
				return;
			}
			if ( numStyles === undefined )
				numStyles = jQuery("style, link[rel=stylesheet]").length;
			if ( document.styleSheets.length != numStyles ) {
				setTimeout( arguments.callee, 0 );
				return;
			}
			// and execute any waiting functions
			jQuery.ready();
		})();
	}

	// A fallback to window.onload, that will always work
	jQuery.event.add( window, "load", jQuery.ready );
}

jQuery.each( ("blur,focus,load,resize,scroll,unload,click,dblclick," +
	"mousedown,mouseup,mousemove,mouseover,mouseout,change,select," + 
	"submit,keydown,keypress,keyup,error").split(","), function(i, name){
	
	// Handle event binding
	jQuery.fn[name] = function(fn){
		return fn ? this.bind(name, fn) : this.trigger(name);
	};
});

// Checks if an event happened on an element within another element
// Used in jQuery.event.special.mouseenter and mouseleave handlers
var withinElement = function(event, elem) {
	// Check if mouse(over|out) are still within the same parent element
	var parent = event.relatedTarget;
	// Traverse up the tree
	while ( parent && parent != elem ) try { parent = parent.parentNode; } catch(error) { parent = elem; }
	// Return true if we actually just moused on to a sub-element
	return parent == elem;
};

// Prevent memory leaks in IE
// And prevent errors on refresh with events like mouseover in other browsers
// Window isn't included so as not to unbind existing unload events
jQuery(window).bind("unload", function() {
	jQuery("*").add(document).unbind();
});
jQuery.fn.extend({
	load: function( url, params, callback ) {
		if ( jQuery.isFunction( url ) )
			return this.bind("load", url);

		var off = url.indexOf(" ");
		if ( off >= 0 ) {
			var selector = url.slice(off, url.length);
			url = url.slice(0, off);
		}

		callback = callback || function(){};

		// Default to a GET request
		var type = "GET";

		// If the second parameter was provided
		if ( params )
			// If it's a function
			if ( jQuery.isFunction( params ) ) {
				// We assume that it's the callback
				callback = params;
				params = null;

			// Otherwise, build a param string
			} else {
				params = jQuery.param( params );
				type = "POST";
			}

		var self = this;

		// Request the remote document
		jQuery.ajax({
			url: url,
			type: type,
			dataType: "html",
			data: params,
			complete: function(res, status){
				// If successful, inject the HTML into all the matched elements
				if ( status == "success" || status == "notmodified" )
					// See if a selector was specified
					self.html( selector ?
						// Create a dummy div to hold the results
						jQuery("<div/>")
							// inject the contents of the document in, removing the scripts
							// to avoid any 'Permission Denied' errors in IE
							.append(res.responseText.replace(/<script(.|\s)*?\/script>/g, ""))

							// Locate the specified elements
							.find(selector) :

						// If not, just inject the full result
						res.responseText );

				self.each( callback, [res.responseText, status, res] );
			}
		});
		return this;
	},

	serialize: function() {
		return jQuery.param(this.serializeArray());
	},
	serializeArray: function() {
		return this.map(function(){
			return jQuery.nodeName(this, "form") ?
				jQuery.makeArray(this.elements) : this;
		})
		.filter(function(){
			return this.name && !this.disabled && 
				(this.checked || /select|textarea/i.test(this.nodeName) || 
					/text|hidden|password/i.test(this.type));
		})
		.map(function(i, elem){
			var val = jQuery(this).val();
			return val == null ? null :
				val.constructor == Array ?
					jQuery.map( val, function(val, i){
						return {name: elem.name, value: val};
					}) :
					{name: elem.name, value: val};
		}).get();
	}
});

// Attach a bunch of functions for handling common AJAX events
jQuery.each( "ajaxStart,ajaxStop,ajaxComplete,ajaxError,ajaxSuccess,ajaxSend".split(","), function(i,o){
	jQuery.fn[o] = function(f){
		return this.bind(o, f);
	};
});

var jsc = now();

jQuery.extend({
	get: function( url, data, callback, type ) {
		// shift arguments if data argument was ommited
		if ( jQuery.isFunction( data ) ) {
			callback = data;
			data = null;
		}
		
		return jQuery.ajax({
			type: "GET",
			url: url,
			data: data,
			success: callback,
			dataType: type
		});
	},

	getScript: function( url, callback ) {
		return jQuery.get(url, null, callback, "script");
	},

	getJSON: function( url, data, callback ) {
		return jQuery.get(url, data, callback, "json");
	},

	post: function( url, data, callback, type ) {
		if ( jQuery.isFunction( data ) ) {
			callback = data;
			data = {};
		}

		return jQuery.ajax({
			type: "POST",
			url: url,
			data: data,
			success: callback,
			dataType: type
		});
	},

	ajaxSetup: function( settings ) {
		jQuery.extend( jQuery.ajaxSettings, settings );
	},

	ajaxSettings: {
		global: true,
		type: "GET",
		timeout: 0,
		contentType: "application/x-www-form-urlencoded",
		processData: true,
		async: true,
		data: null,
		username: null,
		password: null,
		accepts: {
			xml: "application/xml, text/xml",
			html: "text/html",
			script: "text/javascript, application/javascript",
			json: "application/json, text/javascript",
			text: "text/plain",
			_default: "*/*"
		}
	},
	
	// Last-Modified header cache for next request
	lastModified: {},

	ajax: function( s ) {
		var jsonp, jsre = /=\?(&|$)/g, status, data;

		// Extend the settings, but re-extend 's' so that it can be
		// checked again later (in the test suite, specifically)
		s = jQuery.extend(true, s, jQuery.extend(true, {}, jQuery.ajaxSettings, s));

		// convert data if not already a string
		if ( s.data && s.processData && typeof s.data != "string" )
			s.data = jQuery.param(s.data);

		// Handle JSONP Parameter Callbacks
		if ( s.dataType == "jsonp" ) {
			if ( s.type.toLowerCase() == "get" ) {
				if ( !s.url.match(jsre) )
					s.url += (s.url.match(/\?/) ? "&" : "?") + (s.jsonp || "callback") + "=?";
			} else if ( !s.data || !s.data.match(jsre) )
				s.data = (s.data ? s.data + "&" : "") + (s.jsonp || "callback") + "=?";
			s.dataType = "json";
		}

		// Build temporary JSONP function
		if ( s.dataType == "json" && (s.data && s.data.match(jsre) || s.url.match(jsre)) ) {
			jsonp = "jsonp" + jsc++;

			// Replace the =? sequence both in the query string and the data
			if ( s.data )
				s.data = (s.data + "").replace(jsre, "=" + jsonp + "$1");
			s.url = s.url.replace(jsre, "=" + jsonp + "$1");

			// We need to make sure
			// that a JSONP style response is executed properly
			s.dataType = "script";

			// Handle JSONP-style loading
			window[ jsonp ] = function(tmp){
				data = tmp;
				success();
				complete();
				// Garbage collect
				window[ jsonp ] = undefined;
				try{ delete window[ jsonp ]; } catch(e){}
				if ( head )
					head.removeChild( script );
			};
		}

		if ( s.dataType == "script" && s.cache == null )
			s.cache = false;

		if ( s.cache === false && s.type.toLowerCase() == "get" ) {
			var ts = now();
			// try replacing _= if it is there
			var ret = s.url.replace(/(\?|&)_=.*?(&|$)/, "$1_=" + ts + "$2");
			// if nothing was replaced, add timestamp to the end
			s.url = ret + ((ret == s.url) ? (s.url.match(/\?/) ? "&" : "?") + "_=" + ts : "");
		}

		// If data is available, append data to url for get requests
		if ( s.data && s.type.toLowerCase() == "get" ) {
			s.url += (s.url.match(/\?/) ? "&" : "?") + s.data;

			// IE likes to send both get and post data, prevent this
			s.data = null;
		}

		// Watch for a new set of requests
		if ( s.global && ! jQuery.active++ )
			jQuery.event.trigger( "ajaxStart" );

		// If we're requesting a remote document
		// and trying to load JSON or Script with a GET
		if ( (!s.url.indexOf("http") || !s.url.indexOf("//")) && s.dataType == "script" && s.type.toLowerCase() == "get" ) {
			var head = document.getElementsByTagName("head")[0];
			var script = document.createElement("script");
			script.src = s.url;
			if (s.scriptCharset)
				script.charset = s.scriptCharset;

			// Handle Script loading
			if ( !jsonp ) {
				var done = false;

				// Attach handlers for all browsers
				script.onload = script.onreadystatechange = function(){
					if ( !done && (!this.readyState || 
							this.readyState == "loaded" || this.readyState == "complete") ) {
						done = true;
						success();
						complete();
						head.removeChild( script );
					}
				};
			}

			head.appendChild(script);

			// We handle everything using the script element injection
			return undefined;
		}

		var requestDone = false;

		// Create the request object; Microsoft failed to properly
		// implement the XMLHttpRequest in IE7, so we use the ActiveXObject when it is available
		var xml = window.ActiveXObject ? new ActiveXObject("Microsoft.XMLHTTP") : new XMLHttpRequest();

		// Open the socket
		xml.open(s.type, s.url, s.async, s.username, s.password);

		// Need an extra try/catch for cross domain requests in Firefox 3
		try {
			// Set the correct header, if data is being sent
			if ( s.data )
				xml.setRequestHeader("Content-Type", s.contentType);

			// Set the If-Modified-Since header, if ifModified mode.
			if ( s.ifModified )
				xml.setRequestHeader("If-Modified-Since",
					jQuery.lastModified[s.url] || "Thu, 01 Jan 1970 00:00:00 GMT" );

			// Set header so the called script knows that it's an XMLHttpRequest
			xml.setRequestHeader("X-Requested-With", "XMLHttpRequest");

			// Set the Accepts header for the server, depending on the dataType
			xml.setRequestHeader("Accept", s.dataType && s.accepts[ s.dataType ] ?
				s.accepts[ s.dataType ] + ", */*" :
				s.accepts._default );
		} catch(e){}

		// Allow custom headers/mimetypes
		if ( s.beforeSend && s.beforeSend(xml, s) === false ) {
			// cleanup active request counter
			s.global && jQuery.active--;
			// close opended socket
			xml.abort();
			return false;
		}
		
		if ( s.global )
			jQuery.event.trigger("ajaxSend", [xml, s]);

		// Wait for a response to come back
		var onreadystatechange = function(isTimeout){
			// The transfer is complete and the data is available, or the request timed out
			if ( !requestDone && xml && (xml.readyState == 4 || isTimeout == "timeout") ) {
				requestDone = true;
				
				// clear poll interval
				if (ival) {
					clearInterval(ival);
					ival = null;
				}
				
				status = isTimeout == "timeout" && "timeout" ||
					!jQuery.httpSuccess( xml ) && "error" ||
					s.ifModified && jQuery.httpNotModified( xml, s.url ) && "notmodified" ||
					"success";

				if ( status == "success" ) {
					// Watch for, and catch, XML document parse errors
					try {
						// process the data (runs the xml through httpData regardless of callback)
						data = jQuery.httpData( xml, s.dataType );
					} catch(e) {
						status = "parsererror";
					}
				}

				// Make sure that the request was successful or notmodified
				if ( status == "success" ) {
					// Cache Last-Modified header, if ifModified mode.
					var modRes;
					try {
						modRes = xml.getResponseHeader("Last-Modified");
					} catch(e) {} // swallow exception thrown by FF if header is not available
	
					if ( s.ifModified && modRes )
						jQuery.lastModified[s.url] = modRes;

					// JSONP handles its own success callback
					if ( !jsonp )
						success();	
				} else
					jQuery.handleError(s, xml, status);

				// Fire the complete handlers
				complete();

				// Stop memory leaks
				if ( s.async )
					xml = null;
			}
		};
		
		if ( s.async ) {
			// don't attach the handler to the request, just poll it instead
			var ival = setInterval(onreadystatechange, 13); 

			// Timeout checker
			if ( s.timeout > 0 )
				setTimeout(function(){
					// Check to see if the request is still happening
					if ( xml ) {
						// Cancel the request
						xml.abort();
	
						if( !requestDone )
							onreadystatechange( "timeout" );
					}
				}, s.timeout);
		}
			
		// Send the data
		try {
			xml.send(s.data);
		} catch(e) {
			jQuery.handleError(s, xml, null, e);
		}
		
		// firefox 1.5 doesn't fire statechange for sync requests
		if ( !s.async )
			onreadystatechange();

		function success(){
			// If a local callback was specified, fire it and pass it the data
			if ( s.success )
				s.success( data, status );

			// Fire the global callback
			if ( s.global )
				jQuery.event.trigger( "ajaxSuccess", [xml, s] );
		}

		function complete(){
			// Process result
			if ( s.complete )
				s.complete(xml, status);

			// The request was completed
			if ( s.global )
				jQuery.event.trigger( "ajaxComplete", [xml, s] );

			// Handle the global AJAX counter
			if ( s.global && ! --jQuery.active )
				jQuery.event.trigger( "ajaxStop" );
		}
		
		// return XMLHttpRequest to allow aborting the request etc.
		return xml;
	},

	handleError: function( s, xml, status, e ) {
		// If a local callback was specified, fire it
		if ( s.error ) s.error( xml, status, e );

		// Fire the global callback
		if ( s.global )
			jQuery.event.trigger( "ajaxError", [xml, s, e] );
	},

	// Counter for holding the number of active queries
	active: 0,

	// Determines if an XMLHttpRequest was successful or not
	httpSuccess: function( r ) {
		try {
			// IE error sometimes returns 1223 when it should be 204 so treat it as success, see #1450
			return !r.status && location.protocol == "file:" ||
				( r.status >= 200 && r.status < 300 ) || r.status == 304 || r.status == 1223 ||
				jQuery.browser.safari && r.status == undefined;
		} catch(e){}
		return false;
	},

	// Determines if an XMLHttpRequest returns NotModified
	httpNotModified: function( xml, url ) {
		try {
			var xmlRes = xml.getResponseHeader("Last-Modified");

			// Firefox always returns 200. check Last-Modified date
			return xml.status == 304 || xmlRes == jQuery.lastModified[url] ||
				jQuery.browser.safari && xml.status == undefined;
		} catch(e){}
		return false;
	},

	httpData: function( r, type ) {
		var ct = r.getResponseHeader("content-type"),
			xml = type == "xml" || !type && ct && ct.indexOf("xml") >= 0,
			data = xml ? r.responseXML : r.responseText;

		if ( xml && data.documentElement.tagName == "parsererror" )
			throw "parsererror";

		// If the type is "script", eval it in global context
		if ( type == "script" )
			jQuery.globalEval( data );

		// Get the JavaScript object, if JSON is used.
		if ( type == "json" )
			data = eval("(" + data + ")");

		return data;
	},

	// Serialize an array of form elements or a set of
	// key/values into a query string
	param: function( a ) {
		var s = [];

		// If an array was passed in, assume that it is an array
		// of form elements
		if ( a.constructor == Array || a.jquery )
			// Serialize the form elements
			jQuery.each( a, function(){
				s.push( encodeURIComponent(this.name) + "=" + encodeURIComponent( this.value ) );
			});

		// Otherwise, assume that it's an object of key/value pairs
		else
			// Serialize the key/values
			for ( var j in a )
				// If the value is an array then the key names need to be repeated
				if ( a[j] && a[j].constructor == Array )
					jQuery.each( a[j], function(){
						s.push( encodeURIComponent(j) + "=" + encodeURIComponent( this ) );
					});
				else
					s.push( encodeURIComponent(j) + "=" + encodeURIComponent( a[j] ) );

		// Return the resulting serialization
		return s.join("&").replace(/%20/g, "+");
	}

});
jQuery.fn.extend({
	show: function(speed,callback){
		return speed ?
			this.animate({
				height: "show", width: "show", opacity: "show"
			}, speed, callback) :
			
			this.filter(":hidden").each(function(){
				this.style.display = this.oldblock || "";
				if ( jQuery.css(this,"display") == "none" ) {
					var elem = jQuery("<" + this.tagName + " />").appendTo("body");
					this.style.display = elem.css("display");
					// handle an edge condition where css is - div { display:none; } or similar
					if (this.style.display == "none")
						this.style.display = "block";
					elem.remove();
				}
			}).end();
	},
	
	hide: function(speed,callback){
		return speed ?
			this.animate({
				height: "hide", width: "hide", opacity: "hide"
			}, speed, callback) :
			
			this.filter(":visible").each(function(){
				this.oldblock = this.oldblock || jQuery.css(this,"display");
				this.style.display = "none";
			}).end();
	},

	// Save the old toggle function
	_toggle: jQuery.fn.toggle,
	
	toggle: function( fn, fn2 ){
		return jQuery.isFunction(fn) && jQuery.isFunction(fn2) ?
			this._toggle.apply( this, arguments ) :
			fn ?
				this.animate({
					height: "toggle", width: "toggle", opacity: "toggle"
				}, fn, fn2) :
				this.each(function(){
					jQuery(this)[ jQuery(this).is(":hidden") ? "show" : "hide" ]();
				});
	},
	
	slideDown: function(speed,callback){
		return this.animate({height: "show"}, speed, callback);
	},
	
	slideUp: function(speed,callback){
		return this.animate({height: "hide"}, speed, callback);
	},

	slideToggle: function(speed, callback){
		return this.animate({height: "toggle"}, speed, callback);
	},
	
	fadeIn: function(speed, callback){
		return this.animate({opacity: "show"}, speed, callback);
	},
	
	fadeOut: function(speed, callback){
		return this.animate({opacity: "hide"}, speed, callback);
	},
	
	fadeTo: function(speed,to,callback){
		return this.animate({opacity: to}, speed, callback);
	},
	
	animate: function( prop, speed, easing, callback ) {
		var optall = jQuery.speed(speed, easing, callback);

		return this[ optall.queue === false ? "each" : "queue" ](function(){
			if ( this.nodeType != 1)
				return false;

			var opt = jQuery.extend({}, optall), p,
				hidden = jQuery(this).is(":hidden"), self = this;
			
			for ( p in prop ) {
				if ( prop[p] == "hide" && hidden || prop[p] == "show" && !hidden )
					return jQuery.isFunction(opt.complete) && opt.complete.apply(this);

				if ( p == "height" || p == "width" ) {
					// Store display property
					opt.display = jQuery.css(this, "display");

					// Make sure that nothing sneaks out
					opt.overflow = this.style.overflow;
				}
			}

			if ( opt.overflow != null )
				this.style.overflow = "hidden";

			opt.curAnim = jQuery.extend({}, prop);
			
			jQuery.each( prop, function(name, val){
				var e = new jQuery.fx( self, opt, name );

				if ( /toggle|show|hide/.test(val) )
					e[ val == "toggle" ? hidden ? "show" : "hide" : val ]( prop );
				else {
					var parts = val.toString().match(/^([+-]=)?([\d+-.]+)(.*)$/),
						start = e.cur(true) || 0;

					if ( parts ) {
						var end = parseFloat(parts[2]),
							unit = parts[3] || "px";

						// We need to compute starting value
						if ( unit != "px" ) {
							self.style[ name ] = (end || 1) + unit;
							start = ((end || 1) / e.cur(true)) * start;
							self.style[ name ] = start + unit;
						}

						// If a +=/-= token was provided, we're doing a relative animation
						if ( parts[1] )
							end = ((parts[1] == "-=" ? -1 : 1) * end) + start;

						e.custom( start, end, unit );
					} else
						e.custom( start, val, "" );
				}
			});

			// For JS strict compliance
			return true;
		});
	},
	
	queue: function(type, fn){
		if ( jQuery.isFunction(type) || ( type && type.constructor == Array )) {
			fn = type;
			type = "fx";
		}

		if ( !type || (typeof type == "string" && !fn) )
			return queue( this[0], type );

		return this.each(function(){
			if ( fn.constructor == Array )
				queue(this, type, fn);
			else {
				queue(this, type).push( fn );
			
				if ( queue(this, type).length == 1 )
					fn.apply(this);
			}
		});
	},

	stop: function(clearQueue, gotoEnd){
		var timers = jQuery.timers;

		if (clearQueue)
			this.queue([]);

		this.each(function(){
			// go in reverse order so anything added to the queue during the loop is ignored
			for ( var i = timers.length - 1; i >= 0; i-- )
				if ( timers[i].elem == this ) {
					if (gotoEnd)
						// force the next step to be the last
						timers[i](true);
					timers.splice(i, 1);
				}
		});

		// start the next in the queue if the last step wasn't forced
		if (!gotoEnd)
			this.dequeue();

		return this;
	}

});

var queue = function( elem, type, array ) {
	if ( elem ){
	
		type = type || "fx";
	
		var q = jQuery.data( elem, type + "queue" );
	
		if ( !q || array )
			q = jQuery.data( elem, type + "queue", jQuery.makeArray(array) );

	}
	return q;
};

jQuery.fn.dequeue = function(type){
	type = type || "fx";

	return this.each(function(){
		var q = queue(this, type);

		q.shift();

		if ( q.length )
			q[0].apply( this );
	});
};

jQuery.extend({
	
	speed: function(speed, easing, fn) {
		var opt = speed && speed.constructor == Object ? speed : {
			complete: fn || !fn && easing || 
				jQuery.isFunction( speed ) && speed,
			duration: speed,
			easing: fn && easing || easing && easing.constructor != Function && easing
		};

		opt.duration = (opt.duration && opt.duration.constructor == Number ? 
			opt.duration : 
			jQuery.fx.speeds[opt.duration]) || jQuery.fx.speeds.def;
	
		// Queueing
		opt.old = opt.complete;
		opt.complete = function(){
			if ( opt.queue !== false )
				jQuery(this).dequeue();
			if ( jQuery.isFunction( opt.old ) )
				opt.old.apply( this );
		};
	
		return opt;
	},
	
	easing: {
		linear: function( p, n, firstNum, diff ) {
			return firstNum + diff * p;
		},
		swing: function( p, n, firstNum, diff ) {
			return ((-Math.cos(p*Math.PI)/2) + 0.5) * diff + firstNum;
		}
	},
	
	timers: [],
	timerId: null,

	fx: function( elem, options, prop ){
		this.options = options;
		this.elem = elem;
		this.prop = prop;

		if ( !options.orig )
			options.orig = {};
	}

});

jQuery.fx.prototype = {

	// Simple function for setting a style value
	update: function(){
		if ( this.options.step )
			this.options.step.apply( this.elem, [ this.now, this ] );

		(jQuery.fx.step[this.prop] || jQuery.fx.step._default)( this );

		// Set display property to block for height/width animations
		if ( this.prop == "height" || this.prop == "width" )
			this.elem.style.display = "block";
	},

	// Get the current size
	cur: function(force){
		if ( this.elem[this.prop] != null && this.elem.style[this.prop] == null )
			return this.elem[ this.prop ];

		var r = parseFloat(jQuery.css(this.elem, this.prop, force));
		return r && r > -10000 ? r : parseFloat(jQuery.curCSS(this.elem, this.prop)) || 0;
	},

	// Start an animation from one number to another
	custom: function(from, to, unit){
		this.startTime = now();
		this.start = from;
		this.end = to;
		this.unit = unit || this.unit || "px";
		this.now = this.start;
		this.pos = this.state = 0;
		this.update();

		var self = this;
		function t(gotoEnd){
			return self.step(gotoEnd);
		}

		t.elem = this.elem;

		jQuery.timers.push(t);

		if ( jQuery.timerId == null ) {
			jQuery.timerId = setInterval(function(){
				var timers = jQuery.timers;
				
				for ( var i = 0; i < timers.length; i++ )
					if ( !timers[i]() )
						timers.splice(i--, 1);

				if ( !timers.length ) {
					clearInterval( jQuery.timerId );
					jQuery.timerId = null;
				}
			}, 13);
		}
	},

	// Simple 'show' function
	show: function(){
		// Remember where we started, so that we can go back to it later
		this.options.orig[this.prop] = jQuery.attr( this.elem.style, this.prop );
		this.options.show = true;

		// Begin the animation
		this.custom(0, this.cur());

		// Make sure that we start at a small width/height to avoid any
		// flash of content
		if ( this.prop == "width" || this.prop == "height" )
			this.elem.style[this.prop] = "1px";
		
		// Start by showing the element
		jQuery(this.elem).show();
	},

	// Simple 'hide' function
	hide: function(){
		// Remember where we started, so that we can go back to it later
		this.options.orig[this.prop] = jQuery.attr( this.elem.style, this.prop );
		this.options.hide = true;

		// Begin the animation
		this.custom(this.cur(), 0);
	},

	// Each step of an animation
	step: function(gotoEnd){
		var t = now();

		if ( gotoEnd || t > this.options.duration + this.startTime ) {
			this.now = this.end;
			this.pos = this.state = 1;
			this.update();

			this.options.curAnim[ this.prop ] = true;

			var done = true;
			for ( var i in this.options.curAnim )
				if ( this.options.curAnim[i] !== true )
					done = false;

			if ( done ) {
				if ( this.options.display != null ) {
					// Reset the overflow
					this.elem.style.overflow = this.options.overflow;
				
					// Reset the display
					this.elem.style.display = this.options.display;
					if ( jQuery.css(this.elem, "display") == "none" )
						this.elem.style.display = "block";
				}

				// Hide the element if the "hide" operation was done
				if ( this.options.hide )
					this.elem.style.display = "none";

				// Reset the properties, if the item has been hidden or shown
				if ( this.options.hide || this.options.show )
					for ( var p in this.options.curAnim )
						jQuery.attr(this.elem.style, p, this.options.orig[p]);
			}

			// If a callback was provided, execute it
			if ( done && jQuery.isFunction( this.options.complete ) )
				// Execute the complete function
				this.options.complete.apply( this.elem );

			return false;
		} else {
			var n = t - this.startTime;
			this.state = n / this.options.duration;

			// Perform the easing function, defaults to swing
			this.pos = jQuery.easing[this.options.easing || (jQuery.easing.swing ? "swing" : "linear")](this.state, n, 0, 1, this.options.duration);
			this.now = this.start + ((this.end - this.start) * this.pos);

			// Perform the next step of the animation
			this.update();
		}

		return true;
	}

};

jQuery.extend( jQuery.fx, {
	speeds:{
		slow: 600,  
 		fast: 200,
 		def: 400 //default speed
	},
	step: {
		scrollLeft: function(fx){
			fx.elem.scrollLeft = fx.now;
		},
	
		scrollTop: function(fx){
			fx.elem.scrollTop = fx.now;
		},
	
		opacity: function(fx){
			jQuery.attr(fx.elem.style, "opacity", fx.now);
		},
	
		_default: function(fx){
			fx.elem.style[ fx.prop ] = fx.now + fx.unit;
		}
	}
});
// The Offset Method
// Originally By Brandon Aaron, part of the Dimension Plugin
// http://jquery.com/plugins/project/dimensions
jQuery.fn.offset = function() {
	var left = 0, top = 0, elem = this[0], results;
	
	if ( elem ) with ( jQuery.browser ) {
		var parent       = elem.parentNode, 
		    offsetChild  = elem,
		    offsetParent = elem.offsetParent, 
		    doc          = elem.ownerDocument,
		    safari2      = safari && parseInt(version) < 522 && !/adobeair/i.test(userAgent),
		    css          = jQuery.curCSS,
		    fixed        = css(elem, "position") == "fixed";
	
		// Use getBoundingClientRect if available
		if ( elem.getBoundingClientRect ) {
			var box = elem.getBoundingClientRect();
		
			// Add the document scroll offsets
			add(box.left + Math.max(doc.documentElement.scrollLeft, doc.body.scrollLeft),
				box.top  + Math.max(doc.documentElement.scrollTop,  doc.body.scrollTop));
		
			// IE adds the HTML element's border, by default it is medium which is 2px
			// IE 6 and 7 quirks mode the border width is overwritable by the following css html { border: 0; }
			// IE 7 standards mode, the border is always 2px
			// This border/offset is typically represented by the clientLeft and clientTop properties
			// However, in IE6 and 7 quirks mode the clientLeft and clientTop properties are not updated when overwriting it via CSS
			// Therefore this method will be off by 2px in IE while in quirksmode
			add( -doc.documentElement.clientLeft, -doc.documentElement.clientTop );
	
		// Otherwise loop through the offsetParents and parentNodes
		} else {
		
			// Initial element offsets
			add( elem.offsetLeft, elem.offsetTop );
			
			// Get parent offsets
			while ( offsetParent ) {
				// Add offsetParent offsets
				add( offsetParent.offsetLeft, offsetParent.offsetTop );
			
				// Mozilla and Safari > 2 does not include the border on offset parents
				// However Mozilla adds the border for table or table cells
				if ( mozilla && !/^t(able|d|h)$/i.test(offsetParent.tagName) || safari && !safari2 )
					border( offsetParent );
					
				// Add the document scroll offsets if position is fixed on any offsetParent
				if ( !fixed && css(offsetParent, "position") == "fixed" )
					fixed = true;
			
				// Set offsetChild to previous offsetParent unless it is the body element
				offsetChild  = /^body$/i.test(offsetParent.tagName) ? offsetChild : offsetParent;
				// Get next offsetParent
				offsetParent = offsetParent.offsetParent;
			}
		
			// Get parent scroll offsets
			while ( parent && parent.tagName && !/^body|html$/i.test(parent.tagName) ) {
				// Remove parent scroll UNLESS that parent is inline or a table to work around Opera inline/table scrollLeft/Top bug
				if ( !/^inline|table.*$/i.test(css(parent, "display")) )
					// Subtract parent scroll offsets
					add( -parent.scrollLeft, -parent.scrollTop );
			
				// Mozilla does not add the border for a parent that has overflow != visible
				if ( mozilla && css(parent, "overflow") != "visible" )
					border( parent );
			
				// Get next parent
				parent = parent.parentNode;
			}
		
			// Safari <= 2 doubles body offsets with a fixed position element/offsetParent or absolutely positioned offsetChild
			// Mozilla doubles body offsets with a non-absolutely positioned offsetChild
			if ( (safari2 && (fixed || css(offsetChild, "position") == "absolute")) || 
				(mozilla && css(offsetChild, "position") != "absolute") )
					add( -doc.body.offsetLeft, -doc.body.offsetTop );
			
			// Add the document scroll offsets if position is fixed
			if ( fixed )
				add(Math.max(doc.documentElement.scrollLeft, doc.body.scrollLeft),
					Math.max(doc.documentElement.scrollTop,  doc.body.scrollTop));
		}

		// Return an object with top and left properties
		results = { top: top, left: left };
	}

	function border(elem) {
		add( jQuery.curCSS(elem, "borderLeftWidth", true), jQuery.curCSS(elem, "borderTopWidth", true) );
	}

	function add(l, t) {
		left += parseInt(l) || 0;
		top += parseInt(t) || 0;
	}

	return results;
};


jQuery.fn.extend({
	position: function() {
		var left = 0, top = 0, elem = this[0], offset, parentOffset, offsetParent, results;
		
		if (elem) {
			// Get *real* offsetParent
			offsetParent = this.offsetParent();
			
			// Get correct offsets
			offset       = this.offset();
			parentOffset = offsetParent.offset();
			
			// Subtract element margins
			offset.top  -= parseInt( jQuery.curCSS(elem, 'marginTop', true) ) || 0;
			offset.left -= parseInt( jQuery.curCSS(elem, 'marginLeft', true) ) || 0;
			
			// Add offsetParent borders
			parentOffset.top  += parseInt( jQuery.curCSS(offsetParent[0], 'borderTopWidth', true) ) || 0;
			parentOffset.left += parseInt( jQuery.curCSS(offsetParent[0], 'borderLeftWidth', true) ) || 0;
			
			// Subtract the two offsets
			results = {
				top:  offset.top  - parentOffset.top,
				left: offset.left - parentOffset.left
			};
		}
		
		return results;
	},
	
	offsetParent: function() {
		var offsetParent = this[0].offsetParent;
		while ( offsetParent && (!/^body|html$/i.test(offsetParent.tagName) && jQuery.css(offsetParent, 'position') == 'static') )
			offsetParent = offsetParent.offsetParent;
		return jQuery(offsetParent);
	}
});


// Create scrollLeft and scrollTop methods
jQuery.each( ['Left', 'Top'], function(i, name) {
	jQuery.fn[ 'scroll' + name ] = function(val) {
		if (!this[0]) return;
		
		return val != undefined ?
		
			// Set the scroll offset
			this.each(function() {
				this == window || this == document ?
					window.scrollTo( 
						name == 'Left' ? val : jQuery(window)[ 'scrollLeft' ](),
						name == 'Top'  ? val : jQuery(window)[ 'scrollTop'  ]()
					) :
					this[ 'scroll' + name ] = val;
			}) :
			
			// Return the scroll offset
			this[0] == window || this[0] == document ?
				self[ (name == 'Left' ? 'pageXOffset' : 'pageYOffset') ] ||
					jQuery.boxModel && document.documentElement[ 'scroll' + name ] ||
					document.body[ 'scroll' + name ] :
				this[0][ 'scroll' + name ];
	};
});
// Create innerHeight, innerWidth, outerHeight and outerWidth methods
jQuery.each([ "Height", "Width" ], function(i, name){

	var tl = name == "Height" ? "Top"    : "Left",  // top or left
		br = name == "Height" ? "Bottom" : "Right"; // bottom or right
	
	// innerHeight and innerWidth
	jQuery.fn["inner" + name] = function(){
		return this[ name.toLowerCase() ]() + 
			num(this, "padding" + tl) + 
			num(this, "padding" + br);
	};
	
	// outerHeight and outerWidth
	jQuery.fn["outer" + name] = function(margin) {
		return this["inner" + name]() + 
			num(this, "border" + tl + "Width") +
			num(this, "border" + br + "Width") +
			(!!margin ? 
				num(this, "margin" + tl) + num(this, "margin" + br) : 0);
	};
	
});

function num(elem, prop) {
	elem = elem.jquery ? elem[0] : elem;
	return elem && parseInt( jQuery.curCSS(elem, prop, true), 10 ) || 0;
}
})();
/*
 * jQuery UI Accordion
 * 
 * Copyright (c) 2007, 2008 JÃ¶rn Zaefferer
 * Dual licensed under the MIT (MIT-LICENSE.txt)
 * and GPL (GPL-LICENSE.txt) licenses.
 *
 * http://docs.jquery.com/UI/Accordion
 *
 * Depends:
 *	ui.core.js
 *
 * Revision: $Id: jquery.ui.js,v 1.1 2008/05/14 15:34:12 m.moossen Exp $
 */

;(function($) {

	$.widget("ui.accordion", {
		init: function() {
			var options = this.options;
			
			if ( options.navigation ) {
				var current = this.element.find("a").filter(options.navigationFilter);
				if ( current.length ) {
					if ( current.filter(options.header).length ) {
						options.active = current;
					} else {
						options.active = current.parent().parent().prev();
						current.addClass("current");
					}
				}
			}
			
			// calculate active if not specified, using the first header
			options.headers = this.element.find(options.header);
			options.active = findActive(options.headers, options.active);
			
			if (!this.element.hasClass("ui-accordion")) {
				this.element.addClass("ui-accordion");
				$("<span class='ui-accordion-left'/>").insertBefore(options.headers);
				$("<span class='ui-accordion-right'/>").appendTo(options.headers);
				options.headers.addClass("ui-accordion-header").attr("tabindex", "0");
			}
			
			var maxHeight;
			if ( options.fillSpace ) {
				maxHeight = this.element.parent().height();
				options.headers.each(function() {
					maxHeight -= $(this).outerHeight();
				});
				var maxPadding = 0;
				options.headers.next().each(function() {
					maxPadding = Math.max(maxPadding, $(this).innerHeight() - $(this).height());
				}).height(maxHeight - maxPadding);
			} else if ( options.autoHeight ) {
				maxHeight = 0;
				options.headers.next().each(function() {
					maxHeight = Math.max(maxHeight, $(this).outerHeight());
				}).height(maxHeight);
			}
		
			options.headers
				.not(options.active || "")
				.next()
				.hide();
			options.active.parent().andSelf().addClass(options.selectedClass);
			
			if (options.event) {
				this.element.bind((options.event) + ".accordion", clickHandler);
			}
		},
		activate: function(index) {
			// call clickHandler with custom event
			clickHandler.call(this.element[0], {
				target: findActive( this.options.headers, index )[0]
			});
		},
		destroy: function() {
			this.options.headers.next().css("display", "");
			if ( this.options.fillSpace || this.options.autoHeight ) {
				this.options.headers.next().css("height", "");
			}
			$.removeData(this.element[0], "accordion");
			this.element.removeClass("ui-accordion").unbind(".accordion");
		}
	});
	
	function scopeCallback(callback, scope) {
		return function() {
			return callback.apply(scope, arguments);
		};
	};
	
	function completed(cancel) {
		// if removed while animated data can be empty
		if (!$.data(this, "accordion")) {
			return;
		}
		
		var instance = $.data(this, "accordion");
		var options = instance.options;
		options.running = cancel ? 0 : --options.running;
		if ( options.running ) {
			return;
		}
		if ( options.clearStyle ) {
			options.toShow.add(options.toHide).css({
				height: "",
				overflow: ""
			});
		}
		$(this).triggerHandler("accordionchange", [options.data], options.change);
	}
	
	function toggle(toShow, toHide, data, clickedActive, down) {
		var options = $.data(this, "accordion").options;
		options.toShow = toShow;
		options.toHide = toHide;
		options.data = data;
		var complete = scopeCallback(completed, this);
		
		// count elements to animate
		options.running = toHide.size() === 0 ? toShow.size() : toHide.size();
		
		if ( options.animated ) {
			if ( !options.alwaysOpen && clickedActive ) {
				$.ui.accordion.animations[options.animated]({
					toShow: jQuery([]),
					toHide: toHide,
					complete: complete,
					down: down,
					autoHeight: options.autoHeight
				});
			} else {
				$.ui.accordion.animations[options.animated]({
					toShow: toShow,
					toHide: toHide,
					complete: complete,
					down: down,
					autoHeight: options.autoHeight
				});
			}
		} else {
			if ( !options.alwaysOpen && clickedActive ) {
				toShow.toggle();
			} else {
				toHide.hide();
				toShow.show();
			}
			complete(true);
		}
	}
	
	function clickHandler(event) {
		var options = $.data(this, "accordion").options;
		if (options.disabled) {
			return false;
		}
		
		// called only when using activate(false) to close all parts programmatically
		if ( !event.target && !options.alwaysOpen ) {
			options.active.parent().andSelf().toggleClass(options.selectedClass);
			var toHide = options.active.next(),
				data = {
					instance: this,
					options: options,
					newHeader: jQuery([]),
					oldHeader: options.active,
					newContent: jQuery([]),
					oldContent: toHide
				},
				toShow = (options.active = $([]));
			toggle.call(this, toShow, toHide, data );
			return false;
		}
		// get the click target
		var clicked = $(event.target);
		
		// due to the event delegation model, we have to check if one
		// of the parent elements is our actual header, and find that
		if ( clicked.parents(options.header).length ) {
			while ( !clicked.is(options.header) ) {
				clicked = clicked.parent();
			}
		}
		
		var clickedActive = clicked[0] == options.active[0];
		
		// if animations are still active, or the active header is the target, ignore click
		if (options.running || (options.alwaysOpen && clickedActive)) {
			return false;
		}
		if (!clicked.is(options.header)) {
			return;
		}
		
		// switch classes
		options.active.parent().andSelf().toggleClass(options.selectedClass);
		if ( !clickedActive ) {
			clicked.parent().andSelf().addClass(options.selectedClass);
		}
		
		// find elements to show and hide
		var toShow = clicked.next(),
			toHide = options.active.next(),
			//data = [clicked, options.active, toShow, toHide],
			data = {
				instance: this,
				options: options,
				newHeader: clicked,
				oldHeader: options.active,
				newContent: toShow,
				oldContent: toHide
			},
			down = options.headers.index( options.active[0] ) > options.headers.index( clicked[0] );
		
		options.active = clickedActive ? $([]) : clicked;
		toggle.call(this, toShow, toHide, data, clickedActive, down );
	
		return false;
	};
	
	function findActive(headers, selector) {
		return selector != undefined
			? typeof selector == "number"
				? headers.filter(":eq(" + selector + ")")
				: headers.not(headers.not(selector))
			: selector === false
				? $([])
				: headers.filter(":eq(0)");
	}
	
	$.extend($.ui.accordion, {
		defaults: {
			selectedClass: "selected",
			alwaysOpen: true,
			animated: 'slide',
			event: "click",
			header: "a",
			autoHeight: true,
			running: 0,
			navigationFilter: function() {
				return this.href.toLowerCase() == location.href.toLowerCase();
			}
		},
		animations: {
			slide: function(options, additions) {
				options = $.extend({
					easing: "swing",
					duration: 300
				}, options, additions);
				if ( !options.toHide.size() ) {
					options.toShow.animate({height: "show"}, options);
					return;
				}
				var hideHeight = options.toHide.height(),
					showHeight = options.toShow.height(),
					difference = showHeight / hideHeight;
				options.toShow.css({ height: 0, overflow: 'hidden' }).show();
				options.toHide.filter(":hidden").each(options.complete).end().filter(":visible").animate({height:"hide"},{
					step: function(now) {
						var current = (hideHeight - now) * difference;
						if ($.browser.msie || $.browser.opera) {
							current = Math.ceil(current);
						}
						options.toShow.height( current );
					},
					duration: options.duration,
					easing: options.easing,
					complete: function() {
						if ( !options.autoHeight ) {
							options.toShow.css("height", "auto");
						}
						options.complete();
					}
				});
			},
			bounceslide: function(options) {
				this.slide(options, {
					easing: options.down ? "bounceout" : "swing",
					duration: options.down ? 1000 : 200
				});
			},
			easeslide: function(options) {
				this.slide(options, {
					easing: "easeinout",
					duration: 700
				});
			}
		}
	});
	
	// deprecated, use accordion("activate", index) instead
	$.fn.activate = function(index) {
		return this.accordion("activate", index);
	};

})(jQuery);
/* jQuery UI Date Picker v3.4.3 (previously jQuery Calendar)
   Written by Marc Grabanski (m@marcgrabanski.com) and Keith Wood (kbwood@virginbroadband.com.au).

   Copyright (c) 2007 Marc Grabanski (http://marcgrabanski.com/code/ui-datepicker)
   Dual licensed under the MIT (MIT-LICENSE.txt)
   and GPL (GPL-LICENSE.txt) licenses.
   Date: 09-03-2007  */
   
;(function($) { // hide the namespace

/* Date picker manager.
   Use the singleton instance of this class, $.datepicker, to interact with the date picker.
   Settings for (groups of) date pickers are maintained in an instance object
   (DatepickerInstance), allowing multiple different settings on the same page. */

function Datepicker() {
	this.debug = false; // Change this to true to start debugging
	this._nextId = 0; // Next ID for a date picker instance
	this._inst = []; // List of instances indexed by ID
	this._curInst = null; // The current instance in use
	this._disabledInputs = []; // List of date picker inputs that have been disabled
	this._datepickerShowing = false; // True if the popup picker is showing , false if not
	this._inDialog = false; // True if showing within a "dialog", false if not
	this.regional = []; // Available regional settings, indexed by language code
	this.regional[''] = { // Default regional settings
		clearText: 'Clear', // Display text for clear link
		clearStatus: 'Erase the current date', // Status text for clear link
		closeText: 'Close', // Display text for close link
		closeStatus: 'Close without change', // Status text for close link
		prevText: '&#x3c;Prev', // Display text for previous month link
		prevStatus: 'Show the previous month', // Status text for previous month link
		nextText: 'Next&#x3e;', // Display text for next month link
		nextStatus: 'Show the next month', // Status text for next month link
		currentText: 'Today', // Display text for current month link
		currentStatus: 'Show the current month', // Status text for current month link
		monthNames: ['January','February','March','April','May','June',
			'July','August','September','October','November','December'], // Names of months for drop-down and formatting
		monthNamesShort: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'], // For formatting
		monthStatus: 'Show a different month', // Status text for selecting a month
		yearStatus: 'Show a different year', // Status text for selecting a year
		weekHeader: 'Wk', // Header for the week of the year column
		weekStatus: 'Week of the year', // Status text for the week of the year column
		dayNames: ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'], // For formatting
		dayNamesShort: ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'], // For formatting
		dayNamesMin: ['Su','Mo','Tu','We','Th','Fr','Sa'], // Column headings for days starting at Sunday
		dayStatus: 'Set DD as first week day', // Status text for the day of the week selection
		dateStatus: 'Select DD, M d', // Status text for the date selection
		dateFormat: 'mm/dd/yy', // See format options on parseDate
		firstDay: 0, // The first day of the week, Sun = 0, Mon = 1, ...
		initStatus: 'Select a date', // Initial Status text on opening
		isRTL: false // True if right-to-left language, false if left-to-right
	};
	this._defaults = { // Global defaults for all the date picker instances
		showOn: 'focus', // 'focus' for popup on focus,
			// 'button' for trigger button, or 'both' for either
		showAnim: 'show', // Name of jQuery animation for popup
		defaultDate: null, // Used when field is blank: actual date,
			// +/-number for offset from today, null for today
		appendText: '', // Display text following the input box, e.g. showing the format
		buttonText: '...', // Text for trigger button
		buttonImage: '', // URL for trigger button image
		buttonImageOnly: false, // True if the image appears alone, false if it appears on a button
		closeAtTop: true, // True to have the clear/close at the top,
			// false to have them at the bottom
		mandatory: false, // True to hide the Clear link, false to include it
		hideIfNoPrevNext: false, // True to hide next/previous month links
			// if not applicable, false to just disable them
		changeMonth: true, // True if month can be selected directly, false if only prev/next
		changeYear: true, // True if year can be selected directly, false if only prev/next
		yearRange: '-10:+10', // Range of years to display in drop-down,
			// either relative to current year (-nn:+nn) or absolute (nnnn:nnnn)
		changeFirstDay: true, // True to click on day name to change, false to remain as set
		showOtherMonths: false, // True to show dates in other months, false to leave blank
		showWeeks: false, // True to show week of the year, false to omit
		calculateWeek: this.iso8601Week, // How to calculate the week of the year,
			// takes a Date and returns the number of the week for it
		shortYearCutoff: '+10', // Short year values < this are in the current century,
			// > this are in the previous century, 
			// string value starting with '+' for current year + value
		showStatus: false, // True to show status bar at bottom, false to not show it
		statusForDate: this.dateStatus, // Function to provide status text for a date -
			// takes date and instance as parameters, returns display text
		minDate: null, // The earliest selectable date, or null for no limit
		maxDate: null, // The latest selectable date, or null for no limit
		speed: 'normal', // Speed of display/closure
		beforeShowDay: null, // Function that takes a date and returns an array with
			// [0] = true if selectable, false if not,
			// [1] = custom CSS class name(s) or '', e.g. $.datepicker.noWeekends
		beforeShow: null, // Function that takes an input field and
			// returns a set of custom settings for the date picker
		onSelect: null, // Define a callback function when a date is selected
		onClose: null, // Define a callback function when the datepicker is closed
		numberOfMonths: 1, // Number of months to show at a time
		stepMonths: 1, // Number of months to step back/forward
		rangeSelect: false, // Allows for selecting a date range on one date picker
		rangeSeparator: ' - ' // Text between two dates in a range
	};
	$.extend(this._defaults, this.regional['']);
	this._datepickerDiv = $('<div id="datepicker_div"></div>');
}

$.extend(Datepicker.prototype, {
	/* Class name added to elements to indicate already configured with a date picker. */
	markerClassName: 'hasDatepicker',

	/* Debug logging (if enabled). */
	log: function () {
		if (this.debug)
			console.log.apply('', arguments);
	},
	
	/* Register a new date picker instance - with custom settings. */
	_register: function(inst) {
		var id = this._nextId++;
		this._inst[id] = inst;
		return id;
	},

	/* Retrieve a particular date picker instance based on its ID. */
	_getInst: function(id) {
		return this._inst[id] || id;
	},

	/* Override the default settings for all instances of the date picker. 
	   @param  settings  object - the new settings to use as defaults (anonymous object)
	   @return the manager object */
	setDefaults: function(settings) {
		extendRemove(this._defaults, settings || {});
		return this;
	},

	/* Attach the date picker to a jQuery selection.
	   @param  target    element - the target input field or division or span
	   @param  settings  object - the new settings to use for this date picker instance (anonymous) */
	_attachDatepicker: function(target, settings) {
		// check for settings on the control itself - in namespace 'date:'
		var inlineSettings = null;
		for (attrName in this._defaults) {
			var attrValue = target.getAttribute('date:' + attrName);
			if (attrValue) {
				inlineSettings = inlineSettings || {};
				try {
					inlineSettings[attrName] = eval(attrValue);
				} catch (err) {
					inlineSettings[attrName] = attrValue;
				}
			}
		}
		var nodeName = target.nodeName.toLowerCase();
		var instSettings = (inlineSettings ? 
			$.extend(settings || {}, inlineSettings || {}) : settings);
		if (nodeName == 'input') {
			var inst = (inst && !inlineSettings ? inst :
				new DatepickerInstance(instSettings, false));
			this._connectDatepicker(target, inst);
		} else if (nodeName == 'div' || nodeName == 'span') {
			var inst = new DatepickerInstance(instSettings, true);
			this._inlineDatepicker(target, inst);
		}
	},

	/* Detach a datepicker from its control.
	   @param  target    element - the target input field or division or span */
	_destroyDatepicker: function(target) {
		var nodeName = target.nodeName.toLowerCase();
		var calId = target._calId;
		target._calId = null;
		var $target = $(target);
		if (nodeName == 'input') {
			$target.siblings('.datepicker_append').replaceWith('').end()
				.siblings('.datepicker_trigger').replaceWith('').end()
				.removeClass(this.markerClassName)
				.unbind('focus', this._showDatepicker)
				.unbind('keydown', this._doKeyDown)
				.unbind('keypress', this._doKeyPress);
			var wrapper = $target.parents('.datepicker_wrap');
			if (wrapper)
				wrapper.replaceWith(wrapper.html());
		} else if (nodeName == 'div' || nodeName == 'span')
			$target.removeClass(this.markerClassName).empty();
		if ($('input[_calId=' + calId + ']').length == 0)
			// clean up if last for this ID
			this._inst[calId] = null;
	},

	/* Enable the date picker to a jQuery selection.
	   @param  target    element - the target input field or division or span */
	_enableDatepicker: function(target) {
		target.disabled = false;
		$(target).siblings('button.datepicker_trigger').each(function() { this.disabled = false; }).end()
			.siblings('img.datepicker_trigger').css({opacity: '1.0', cursor: ''});
		this._disabledInputs = $.map(this._disabledInputs,
			function(value) { return (value == target ? null : value); }); // delete entry
	},

	/* Disable the date picker to a jQuery selection.
	   @param  target    element - the target input field or division or span */
	_disableDatepicker: function(target) {
		target.disabled = true;
		$(target).siblings('button.datepicker_trigger').each(function() { this.disabled = true; }).end()
			.siblings('img.datepicker_trigger').css({opacity: '0.5', cursor: 'default'});
		this._disabledInputs = $.map($.datepicker._disabledInputs,
			function(value) { return (value == target ? null : value); }); // delete entry
		this._disabledInputs[$.datepicker._disabledInputs.length] = target;
	},

	/* Is the first field in a jQuery collection disabled as a datepicker?
	   @param  target    element - the target input field or division or span
	   @return boolean - true if disabled, false if enabled */
	_isDisabledDatepicker: function(target) {
		if (!target)
			return false;
		for (var i = 0; i < this._disabledInputs.length; i++) {
			if (this._disabledInputs[i] == target)
				return true;
		}
		return false;
	},

	/* Update the settings for a date picker attached to an input field or division.
	   @param  target  element - the target input field or division or span
	   @param  name    string - the name of the setting to change or
	                   object - the new settings to update
	   @param  value   any - the new value for the setting (omit if above is an object) */
	_changeDatepicker: function(target, name, value) {
		var settings = name || {};
		if (typeof name == 'string') {
			settings = {};
			settings[name] = value;
		}
		if (inst = this._getInst(target._calId)) {
			extendRemove(inst._settings, settings);
			this._updateDatepicker(inst);
		}
	},

	/* Set the dates for a jQuery selection.
	   @param  target   element - the target input field or division or span
	   @param  date     Date - the new date
	   @param  endDate  Date - the new end date for a range (optional) */
	_setDateDatepicker: function(target, date, endDate) {
		if (inst = this._getInst(target._calId)) {
			inst._setDate(date, endDate);
			this._updateDatepicker(inst);
		}
	},

	/* Get the date(s) for the first entry in a jQuery selection.
	   @param  target  element - the target input field or division or span
	   @return Date - the current date or
	           Date[2] - the current dates for a range */
	_getDateDatepicker: function(target) {
		var inst = this._getInst(target._calId);
		return (inst ? inst._getDate() : null);
	},

	/* Handle keystrokes. */
	_doKeyDown: function(e) {
		var inst = $.datepicker._getInst(this._calId);
		if ($.datepicker._datepickerShowing)
			switch (e.keyCode) {
				case 9:  $.datepicker._hideDatepicker(null, '');
						break; // hide on tab out
				case 13: $.datepicker._selectDay(inst, inst._selectedMonth, inst._selectedYear,
							$('td.datepicker_daysCellOver', inst._datepickerDiv)[0]);
						return false; // don't submit the form
						break; // select the value on enter
				case 27: $.datepicker._hideDatepicker(null, inst._get('speed'));
						break; // hide on escape
				case 33: $.datepicker._adjustDate(inst,
							(e.ctrlKey ? -1 : -inst._get('stepMonths')), (e.ctrlKey ? 'Y' : 'M'));
						break; // previous month/year on page up/+ ctrl
				case 34: $.datepicker._adjustDate(inst,
							(e.ctrlKey ? +1 : +inst._get('stepMonths')), (e.ctrlKey ? 'Y' : 'M'));
						break; // next month/year on page down/+ ctrl
				case 35: if (e.ctrlKey) $.datepicker._clearDate(inst);
						break; // clear on ctrl+end
				case 36: if (e.ctrlKey) $.datepicker._gotoToday(inst);
						break; // current on ctrl+home
				case 37: if (e.ctrlKey) $.datepicker._adjustDate(inst, -1, 'D');
						break; // -1 day on ctrl+left
				case 38: if (e.ctrlKey) $.datepicker._adjustDate(inst, -7, 'D');
						break; // -1 week on ctrl+up
				case 39: if (e.ctrlKey) $.datepicker._adjustDate(inst, +1, 'D');
						break; // +1 day on ctrl+right
				case 40: if (e.ctrlKey) $.datepicker._adjustDate(inst, +7, 'D');
						break; // +1 week on ctrl+down
			}
		else if (e.keyCode == 36 && e.ctrlKey) // display the date picker on ctrl+home
			$.datepicker._showDatepicker(this);
	},

	/* Filter entered characters - based on date format. */
	_doKeyPress: function(e) {
		var inst = $.datepicker._getInst(this._calId);
		var chars = $.datepicker._possibleChars(inst._get('dateFormat'));
		var chr = String.fromCharCode(e.charCode == undefined ? e.keyCode : e.charCode);
		return e.ctrlKey || (chr < ' ' || !chars || chars.indexOf(chr) > -1);
	},

	/* Attach the date picker to an input field. */
	_connectDatepicker: function(target, inst) {
		var input = $(target);
		if (input.is('.' + this.markerClassName))
			return;
		var appendText = inst._get('appendText');
		var isRTL = inst._get('isRTL');
		if (appendText) {
			if (isRTL)
				input.before('<span class="datepicker_append">' + appendText);
			else
				input.after('<span class="datepicker_append">' + appendText);
		}
		var showOn = inst._get('showOn');
		if (showOn == 'focus' || showOn == 'both') // pop-up date picker when in the marked field
			input.focus(this._showDatepicker);
		if (showOn == 'button' || showOn == 'both') { // pop-up date picker when button clicked
			input.wrap('<span class="datepicker_wrap">');
			var buttonText = inst._get('buttonText');
			var buttonImage = inst._get('buttonImage');
			var trigger = $(inst._get('buttonImageOnly') ? 
				$('<img>').addClass('datepicker_trigger').attr({ src: buttonImage, alt: buttonText, title: buttonText }) :
				$('<button>').addClass('datepicker_trigger').attr({ type: 'button' }).html(buttonImage != '' ? 
						$('<img>').attr({ src:buttonImage, alt:buttonText, title:buttonText }) : buttonText));
			if (isRTL)
				input.before(trigger);
			else
				input.after(trigger);
			trigger.click(function() {
				if ($.datepicker._datepickerShowing && $.datepicker._lastInput == target)
					$.datepicker._hideDatepicker();
				else
					$.datepicker._showDatepicker(target);
			});
        }
		input.addClass(this.markerClassName).keydown(this._doKeyDown).keypress(this._doKeyPress)
			.bind("setData.datepicker", function(event, key, value) {
				inst._settings[key] = value;
			}).bind("getData.datepicker", function(event, key) {
				return inst._get(key);
			});
		input[0]._calId = inst._id;
	},

	/* Attach an inline date picker to a div. */
	_inlineDatepicker: function(target, inst) {
		var input = $(target);
		if (input.is('.' + this.markerClassName))
			return;
		input.addClass(this.markerClassName).append(inst._datepickerDiv)
			.bind("setData.datepicker", function(event, key, value){
				inst._settings[key] = value;
			}).bind("getData.datepicker", function(event, key){
				return inst._get(key);
			});
		input[0]._calId = inst._id;
		this._updateDatepicker(inst);
	},

	/* Tidy up after displaying the date picker. */
	_inlineShow: function(inst) {
		var numMonths = inst._getNumberOfMonths(); // fix width for dynamic number of date pickers
		inst._datepickerDiv.width(numMonths[1] * $('.datepicker', inst._datepickerDiv[0]).width());
	}, 

	/* Pop-up the date picker in a "dialog" box.
	   @param  input     element - ignored
	   @param  dateText  string - the initial date to display (in the current format)
	   @param  onSelect  function - the function(dateText) to call when a date is selected
	   @param  settings  object - update the dialog date picker instance's settings (anonymous object)
	   @param  pos       int[2] - coordinates for the dialog's position within the screen or
	                     event - with x/y coordinates or
	                     leave empty for default (screen centre)
	   @return the manager object */
	_dialogDatepicker: function(input, dateText, onSelect, settings, pos) {
		var inst = this._dialogInst; // internal instance
		if (!inst) {
			inst = this._dialogInst = new DatepickerInstance({}, false);
			this._dialogInput = $('<input type="text" size="1" style="position: absolute; top: -100px;"/>');
			this._dialogInput.keydown(this._doKeyDown);
			$('body').append(this._dialogInput);
			this._dialogInput[0]._calId = inst._id;
		}
		extendRemove(inst._settings, settings || {});
		this._dialogInput.val(dateText);

		this._pos = (pos ? (pos.length ? pos : [pos.pageX, pos.pageY]) : null);
		if (!this._pos) {
			var browserWidth = window.innerWidth || document.documentElement.clientWidth ||	document.body.clientWidth;
			var browserHeight = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
			var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;
			var scrollY = document.documentElement.scrollTop || document.body.scrollTop;
			this._pos = // should use actual width/height below
				[(browserWidth / 2) - 100 + scrollX, (browserHeight / 2) - 150 + scrollY];
		}

		// move input on screen for focus, but hidden behind dialog
		this._dialogInput.css('left', this._pos[0] + 'px').css('top', this._pos[1] + 'px');
		inst._settings.onSelect = onSelect;
		this._inDialog = true;
		this._datepickerDiv.addClass('datepicker_dialog');
		this._showDatepicker(this._dialogInput[0]);
		if ($.blockUI)
			$.blockUI(this._datepickerDiv);
		return this;
	},

	/* Pop-up the date picker for a given input field.
	   @param  input  element - the input field attached to the date picker or
	                  event - if triggered by focus */
	_showDatepicker: function(input) {
		input = input.target || input;
		if (input.nodeName.toLowerCase() != 'input') // find from button/image trigger
			input = $('input', input.parentNode)[0];
		if ($.datepicker._isDisabledDatepicker(input) || $.datepicker._lastInput == input) // already here
			return;
		var inst = $.datepicker._getInst(input._calId);
		var beforeShow = inst._get('beforeShow');
		extendRemove(inst._settings, (beforeShow ? beforeShow.apply(input, [input, inst]) : {}));
		$.datepicker._hideDatepicker(null, '');
		$.datepicker._lastInput = input;
		inst._setDateFromField(input);
		if ($.datepicker._inDialog) // hide cursor
			input.value = '';
		if (!$.datepicker._pos) { // position below input
			$.datepicker._pos = $.datepicker._findPos(input);
			$.datepicker._pos[1] += input.offsetHeight; // add the height
		}
		var isFixed = false;
		$(input).parents().each(function() {
			isFixed |= $(this).css('position') == 'fixed';
		});
		if (isFixed && $.browser.opera) { // correction for Opera when fixed and scrolled
			$.datepicker._pos[0] -= document.documentElement.scrollLeft;
			$.datepicker._pos[1] -= document.documentElement.scrollTop;
		}
		inst._datepickerDiv.css('position', ($.datepicker._inDialog && $.blockUI ?
			'static' : (isFixed ? 'fixed' : 'absolute')))
			.css({ left: $.datepicker._pos[0] + 'px', top: $.datepicker._pos[1] + 'px' });
		$.datepicker._pos = null;
		inst._rangeStart = null;
		$.datepicker._updateDatepicker(inst);
		if (!inst._inline) {
			var speed = inst._get('speed');
			var postProcess = function() {
				$.datepicker._datepickerShowing = true;
				$.datepicker._afterShow(inst);
			};
			var showAnim = inst._get('showAnim') || 'show';
			inst._datepickerDiv[showAnim](speed, postProcess);
			if (speed == '')
				postProcess();
			if (inst._input[0].type != 'hidden')
				inst._input[0].focus();
			$.datepicker._curInst = inst;
		}
	},

	/* Generate the date picker content. */
	_updateDatepicker: function(inst) {
		inst._datepickerDiv.empty().append(inst._generateDatepicker());
		var numMonths = inst._getNumberOfMonths();
		if (numMonths[0] != 1 || numMonths[1] != 1)
			inst._datepickerDiv.addClass('datepicker_multi');
		else
			inst._datepickerDiv.removeClass('datepicker_multi');

		if (inst._get('isRTL'))
			inst._datepickerDiv.addClass('datepicker_rtl');
		else
			inst._datepickerDiv.removeClass('datepicker_rtl');

		if (inst._input && inst._input[0].type != 'hidden')
			$(inst._input[0]).focus();
	},

	/* Tidy up after displaying the date picker. */
	_afterShow: function(inst) {
		var numMonths = inst._getNumberOfMonths(); // fix width for dynamic number of date pickers
		inst._datepickerDiv.width(numMonths[1] * $('.datepicker', inst._datepickerDiv[0])[0].offsetWidth);
		if ($.browser.msie && parseInt($.browser.version) < 7) { // fix IE < 7 select problems
			$('iframe.datepicker_cover').css({width: inst._datepickerDiv.width() + 4,
				height: inst._datepickerDiv.height() + 4});
		}
		// re-position on screen if necessary
		var isFixed = inst._datepickerDiv.css('position') == 'fixed';
		var pos = inst._input ? $.datepicker._findPos(inst._input[0]) : null;
		var browserWidth = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
		var browserHeight = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
		var scrollX = (isFixed ? 0 : document.documentElement.scrollLeft || document.body.scrollLeft);
		var scrollY = (isFixed ? 0 : document.documentElement.scrollTop || document.body.scrollTop);
		// reposition date picker horizontally if outside the browser window
		if ((inst._datepickerDiv.offset().left + inst._datepickerDiv.width() -
				(isFixed && $.browser.msie ? document.documentElement.scrollLeft : 0)) >
				(browserWidth + scrollX)) {
			inst._datepickerDiv.css('left', Math.max(scrollX,
				pos[0] + (inst._input ? $(inst._input[0]).width() : null) - inst._datepickerDiv.width() -
				(isFixed && $.browser.opera ? document.documentElement.scrollLeft : 0)) + 'px');
		}
		// reposition date picker vertically if outside the browser window
		if ((inst._datepickerDiv.offset().top + inst._datepickerDiv.height() -
				(isFixed && $.browser.msie ? document.documentElement.scrollTop : 0)) >
				(browserHeight + scrollY) ) {
			inst._datepickerDiv.css('top', Math.max(scrollY,
				pos[1] - (this._inDialog ? 0 : inst._datepickerDiv.height()) -
				(isFixed && $.browser.opera ? document.documentElement.scrollTop : 0)) + 'px');
		}
	},
	
	/* Find an object's position on the screen. */
	_findPos: function(obj) {
        while (obj && (obj.type == 'hidden' || obj.nodeType != 1)) {
            obj = obj.nextSibling;
        }
        var position = $(obj).offset();
	    return [position.left, position.top];
	},

	/* Hide the date picker from view.
	   @param  input  element - the input field attached to the date picker
	   @param  speed  string - the speed at which to close the date picker */
	_hideDatepicker: function(input, speed) {
		var inst = this._curInst;
		if (!inst)
			return;
		var rangeSelect = inst._get('rangeSelect');
		if (rangeSelect && this._stayOpen) {
			this._selectDate(inst, inst._formatDate(
				inst._currentDay, inst._currentMonth, inst._currentYear));
		}
		this._stayOpen = false;
		if (this._datepickerShowing) {
			speed = (speed != null ? speed : inst._get('speed'));
			var showAnim = inst._get('showAnim');
			inst._datepickerDiv[(showAnim == 'slideDown' ? 'slideUp' :
				(showAnim == 'fadeIn' ? 'fadeOut' : 'hide'))](speed, function() {
				$.datepicker._tidyDialog(inst);
			});
			if (speed == '')
				this._tidyDialog(inst);
			var onClose = inst._get('onClose');
			if (onClose) {
				onClose.apply((inst._input ? inst._input[0] : null),
					[inst._getDate(), inst]);  // trigger custom callback
			}
			this._datepickerShowing = false;
			this._lastInput = null;
			inst._settings.prompt = null;
			if (this._inDialog) {
				this._dialogInput.css({ position: 'absolute', left: '0', top: '-100px' });
				if ($.blockUI) {
					$.unblockUI();
					$('body').append(this._datepickerDiv);
				}
			}
			this._inDialog = false;
		}
		this._curInst = null;
	},

	/* Tidy up after a dialog display. */
	_tidyDialog: function(inst) {
		inst._datepickerDiv.removeClass('datepicker_dialog').unbind('.datepicker');
		$('.datepicker_prompt', inst._datepickerDiv).remove();
	},

	/* Close date picker if clicked elsewhere. */
	_checkExternalClick: function(event) {
		if (!$.datepicker._curInst)
			return;
		var $target = $(event.target);
		if (($target.parents("#datepicker_div").length == 0) &&
				($target.attr('class') != 'datepicker_trigger') &&
				$.datepicker._datepickerShowing && !($.datepicker._inDialog && $.blockUI)) {
			$.datepicker._hideDatepicker(null, '');
		}
	},

	/* Adjust one of the date sub-fields. */
	_adjustDate: function(id, offset, period) {
		var inst = this._getInst(id);
		inst._adjustDate(offset, period);
		this._updateDatepicker(inst);
	},

	/* Action for current link. */
	_gotoToday: function(id) {
		var date = new Date();
		var inst = this._getInst(id);
		inst._selectedDay = date.getDate();
		inst._drawMonth = inst._selectedMonth = date.getMonth();
		inst._drawYear = inst._selectedYear = date.getFullYear();
		this._adjustDate(inst);
	},

	/* Action for selecting a new month/year. */
	_selectMonthYear: function(id, select, period) {
		var inst = this._getInst(id);
		inst._selectingMonthYear = false;
		inst[period == 'M' ? '_drawMonth' : '_drawYear'] =
			select.options[select.selectedIndex].value - 0;
		this._adjustDate(inst);
	},

	/* Restore input focus after not changing month/year. */
	_clickMonthYear: function(id) {
		var inst = this._getInst(id);
		if (inst._input && inst._selectingMonthYear && !$.browser.msie)
			inst._input[0].focus();
		inst._selectingMonthYear = !inst._selectingMonthYear;
	},

	/* Action for changing the first week day. */
	_changeFirstDay: function(id, day) {
		var inst = this._getInst(id);
		inst._settings.firstDay = day;
		this._updateDatepicker(inst);
	},

	/* Action for selecting a day. */
	_selectDay: function(id, month, year, td) {
		if ($(td).is('.datepicker_unselectable'))
			return;
		var inst = this._getInst(id);
		var rangeSelect = inst._get('rangeSelect');
		if (rangeSelect) {
			if (!this._stayOpen) {
				$('.datepicker td').removeClass('datepicker_currentDay');
				$(td).addClass('datepicker_currentDay');
			} 
			this._stayOpen = !this._stayOpen;
		}
		inst._selectedDay = inst._currentDay = $('a', td).html();
		inst._selectedMonth = inst._currentMonth = month;
		inst._selectedYear = inst._currentYear = year;
		this._selectDate(id, inst._formatDate(
			inst._currentDay, inst._currentMonth, inst._currentYear));
		if (this._stayOpen) {
			inst._endDay = inst._endMonth = inst._endYear = null;
			inst._rangeStart = new Date(inst._currentYear, inst._currentMonth, inst._currentDay);
			this._updateDatepicker(inst);
		}
		else if (rangeSelect) {
			inst._endDay = inst._currentDay;
			inst._endMonth = inst._currentMonth;
			inst._endYear = inst._currentYear;
			inst._selectedDay = inst._currentDay = inst._rangeStart.getDate();
			inst._selectedMonth = inst._currentMonth = inst._rangeStart.getMonth();
			inst._selectedYear = inst._currentYear = inst._rangeStart.getFullYear();
			inst._rangeStart = null;
			if (inst._inline)
				this._updateDatepicker(inst);
		}
	},

	/* Erase the input field and hide the date picker. */
	_clearDate: function(id) {
		var inst = this._getInst(id);
		if (inst._get('mandatory'))
			return;
		this._stayOpen = false;
		inst._endDay = inst._endMonth = inst._endYear = inst._rangeStart = null;
		this._selectDate(inst, '');
	},

	/* Update the input field with the selected date. */
	_selectDate: function(id, dateStr) {
		var inst = this._getInst(id);
		dateStr = (dateStr != null ? dateStr : inst._formatDate());
		if (inst._rangeStart)
			dateStr = inst._formatDate(inst._rangeStart) + inst._get('rangeSeparator') + dateStr;
		if (inst._input)
			inst._input.val(dateStr);
		var onSelect = inst._get('onSelect');
		if (onSelect)
			onSelect.apply((inst._input ? inst._input[0] : null), [dateStr, inst]);  // trigger custom callback
		else if (inst._input)
			inst._input.trigger('change'); // fire the change event
		if (inst._inline)
			this._updateDatepicker(inst);
		else if (!this._stayOpen) {
			this._hideDatepicker(null, inst._get('speed'));
			this._lastInput = inst._input[0];
			if (typeof(inst._input[0]) != 'object')
				inst._input[0].focus(); // restore focus
			this._lastInput = null;
		}
	},

	/* Set as beforeShowDay function to prevent selection of weekends.
	   @param  date  Date - the date to customise
	   @return [boolean, string] - is this date selectable?, what is its CSS class? */
	noWeekends: function(date) {
		var day = date.getDay();
		return [(day > 0 && day < 6), ''];
	},
	
	/* Set as calculateWeek to determine the week of the year based on the ISO 8601 definition.
	   @param  date  Date - the date to get the week for
	   @return  number - the number of the week within the year that contains this date */
	iso8601Week: function(date) {
		var checkDate = new Date(date.getFullYear(), date.getMonth(), date.getDate(), (date.getTimezoneOffset() / -60));
		var firstMon = new Date(checkDate.getFullYear(), 1 - 1, 4); // First week always contains 4 Jan
		var firstDay = firstMon.getDay() || 7; // Day of week: Mon = 1, ..., Sun = 7
		firstMon.setDate(firstMon.getDate() + 1 - firstDay); // Preceding Monday
		if (firstDay < 4 && checkDate < firstMon) { // Adjust first three days in year if necessary
			checkDate.setDate(checkDate.getDate() - 3); // Generate for previous year
			return $.datepicker.iso8601Week(checkDate);
		} else if (checkDate > new Date(checkDate.getFullYear(), 12 - 1, 28)) { // Check last three days in year
			firstDay = new Date(checkDate.getFullYear() + 1, 1 - 1, 4).getDay() || 7;
			if (firstDay > 4 && (checkDate.getDay() || 7) < firstDay - 3) { // Adjust if necessary
				checkDate.setDate(checkDate.getDate() + 3); // Generate for next year
				return $.datepicker.iso8601Week(checkDate);
			}
		}
		return Math.floor(((checkDate - firstMon) / 86400000) / 7) + 1; // Weeks to given date
	},
	
	/* Provide status text for a particular date.
	   @param  date  the date to get the status for
	   @param  inst  the current datepicker instance
	   @return  the status display text for this date */
	dateStatus: function(date, inst) {
		return $.datepicker.formatDate(inst._get('dateStatus'), date, inst._getFormatConfig());
	},

	/* Parse a string value into a date object.
	   The format can be combinations of the following:
	   d  - day of month (no leading zero)
	   dd - day of month (two digit)
	   D  - day name short
	   DD - day name long
	   m  - month of year (no leading zero)
	   mm - month of year (two digit)
	   M  - month name short
	   MM - month name long
	   y  - year (two digit)
	   yy - year (four digit)
	   '...' - literal text
	   '' - single quote

	   @param  format           String - the expected format of the date
	   @param  value            String - the date in the above format
	   @param  settings  Object - attributes include:
	                     shortYearCutoff  Number - the cutoff year for determining the century (optional)
	                     dayNamesShort    String[7] - abbreviated names of the days from Sunday (optional)
	                     dayNames         String[7] - names of the days from Sunday (optional)
	                     monthNamesShort  String[12] - abbreviated names of the months (optional)
	                     monthNames       String[12] - names of the months (optional)
	   @return  Date - the extracted date value or null if value is blank */
	parseDate: function (format, value, settings) {
		if (format == null || value == null)
			throw 'Invalid arguments';
		value = (typeof value == 'object' ? value.toString() : value + '');
		if (value == '')
			return null;
		var shortYearCutoff = (settings ? settings.shortYearCutoff : null) || this._defaults.shortYearCutoff;
		var dayNamesShort = (settings ? settings.dayNamesShort : null) || this._defaults.dayNamesShort;
		var dayNames = (settings ? settings.dayNames : null) || this._defaults.dayNames;
		var monthNamesShort = (settings ? settings.monthNamesShort : null) || this._defaults.monthNamesShort;
		var monthNames = (settings ? settings.monthNames : null) || this._defaults.monthNames;
		var year = -1;
		var month = -1;
		var day = -1;
		var literal = false;
		// Check whether a format character is doubled
		var lookAhead = function(match) {
			var matches = (iFormat + 1 < format.length && format.charAt(iFormat + 1) == match);
			if (matches)
				iFormat++;
			return matches;	
		};
		// Extract a number from the string value
		var getNumber = function(match) {
			lookAhead(match);
			var size = (match == 'y' ? 4 : 2);
			var num = 0;
			while (size > 0 && iValue < value.length &&
					value.charAt(iValue) >= '0' && value.charAt(iValue) <= '9') {
				num = num * 10 + (value.charAt(iValue++) - 0);
				size--;
			}
			if (size == (match == 'y' ? 4 : 2))
				throw 'Missing number at position ' + iValue;
			return num;
		};
		// Extract a name from the string value and convert to an index
		var getName = function(match, shortNames, longNames) {
			var names = (lookAhead(match) ? longNames : shortNames);
			var size = 0;
			for (var j = 0; j < names.length; j++)
				size = Math.max(size, names[j].length);
			var name = '';
			var iInit = iValue;
			while (size > 0 && iValue < value.length) {
				name += value.charAt(iValue++);
				for (var i = 0; i < names.length; i++)
					if (name == names[i])
						return i + 1;
				size--;
			}
			throw 'Unknown name at position ' + iInit;
		};
		// Confirm that a literal character matches the string value
		var checkLiteral = function() {
			if (value.charAt(iValue) != format.charAt(iFormat))
				throw 'Unexpected literal at position ' + iValue;
			iValue++;
		};
		var iValue = 0;
		for (var iFormat = 0; iFormat < format.length; iFormat++) {
			if (literal)
				if (format.charAt(iFormat) == "'" && !lookAhead("'"))
					literal = false;
				else
					checkLiteral();
			else
				switch (format.charAt(iFormat)) {
					case 'd':
						day = getNumber('d');
						break;
					case 'D': 
						getName('D', dayNamesShort, dayNames);
						break;
					case 'm': 
						month = getNumber('m');
						break;
					case 'M':
						month = getName('M', monthNamesShort, monthNames); 
						break;
					case 'y':
						year = getNumber('y');
						break;
					case "'":
						if (lookAhead("'"))
							checkLiteral();
						else
							literal = true;
						break;
					default:
						checkLiteral();
				}
		}
		if (year < 100) {
			year += new Date().getFullYear() - new Date().getFullYear() % 100 +
				(year <= shortYearCutoff ? 0 : -100);
		}
		var date = new Date(year, month - 1, day);
		if (date.getFullYear() != year || date.getMonth() + 1 != month || date.getDate() != day) {
			throw 'Invalid date'; // E.g. 31/02/*
		}
		return date;
	},

	/* Format a date object into a string value.
	   The format can be combinations of the following:
	   d  - day of month (no leading zero)
	   dd - day of month (two digit)
	   D  - day name short
	   DD - day name long
	   m  - month of year (no leading zero)
	   mm - month of year (two digit)
	   M  - month name short
	   MM - month name long
	   y  - year (two digit)
	   yy - year (four digit)
	   '...' - literal text
	   '' - single quote

	   @param  format    String - the desired format of the date
	   @param  date      Date - the date value to format
	   @param  settings  Object - attributes include:
	                     dayNamesShort    String[7] - abbreviated names of the days from Sunday (optional)
	                     dayNames         String[7] - names of the days from Sunday (optional)
	                     monthNamesShort  String[12] - abbreviated names of the months (optional)
	                     monthNames       String[12] - names of the months (optional)
	   @return  String - the date in the above format */
	formatDate: function (format, date, settings) {
		if (!date)
			return '';
		var dayNamesShort = (settings ? settings.dayNamesShort : null) || this._defaults.dayNamesShort;
		var dayNames = (settings ? settings.dayNames : null) || this._defaults.dayNames;
		var monthNamesShort = (settings ? settings.monthNamesShort : null) || this._defaults.monthNamesShort;
		var monthNames = (settings ? settings.monthNames : null) || this._defaults.monthNames;
		// Check whether a format character is doubled
		var lookAhead = function(match) {
			var matches = (iFormat + 1 < format.length && format.charAt(iFormat + 1) == match);
			if (matches)
				iFormat++;
			return matches;	
		};
		// Format a number, with leading zero if necessary
		var formatNumber = function(match, value) {
			return (lookAhead(match) && value < 10 ? '0' : '') + value;
		};
		// Format a name, short or long as requested
		var formatName = function(match, value, shortNames, longNames) {
			return (lookAhead(match) ? longNames[value] : shortNames[value]);
		};
		var output = '';
		var literal = false;
		if (date) {
			for (var iFormat = 0; iFormat < format.length; iFormat++) {
				if (literal)
					if (format.charAt(iFormat) == "'" && !lookAhead("'"))
						literal = false;
					else
						output += format.charAt(iFormat);
				else
					switch (format.charAt(iFormat)) {
						case 'd':
							output += formatNumber('d', date.getDate()); 
							break;
						case 'D': 
							output += formatName('D', date.getDay(), dayNamesShort, dayNames);
							break;
						case 'm': 
							output += formatNumber('m', date.getMonth() + 1); 
							break;
						case 'M':
							output += formatName('M', date.getMonth(), monthNamesShort, monthNames); 
							break;
						case 'y':
							output += (lookAhead('y') ? date.getFullYear() : 
								(date.getYear() % 100 < 10 ? '0' : '') + date.getYear() % 100);
							break;
						case "'":
							if (lookAhead("'"))
								output += "'";
							else
								literal = true;
							break;
						default:
							output += format.charAt(iFormat);
					}
			}
		}
		return output;
	},

	/* Extract all possible characters from the date format. */
	_possibleChars: function (format) {
		var chars = '';
		var literal = false;
		for (var iFormat = 0; iFormat < format.length; iFormat++)
			if (literal)
				if (format.charAt(iFormat) == "'" && !lookAhead("'"))
					literal = false;
				else
					chars += format.charAt(iFormat);
			else
				switch (format.charAt(iFormat)) {
					case 'd' || 'm' || 'y':
						chars += '0123456789'; 
						break;
					case 'D' || 'M':
						return null; // Accept anything
					case "'":
						if (lookAhead("'"))
							chars += "'";
						else
							literal = true;
						break;
					default:
						chars += format.charAt(iFormat);
				}
		return chars;
	}
});

/* Individualised settings for date picker functionality applied to one or more related inputs.
   Instances are managed and manipulated through the Datepicker manager. */
function DatepickerInstance(settings, inline) {
	this._id = $.datepicker._register(this);
	this._selectedDay = 0; // Current date for selection
	this._selectedMonth = 0; // 0-11
	this._selectedYear = 0; // 4-digit year
	this._drawMonth = 0; // Current month at start of datepicker
	this._drawYear = 0;
	this._input = null; // The attached input field
	this._inline = inline; // True if showing inline, false if used in a popup
	this._datepickerDiv = (!inline ? $.datepicker._datepickerDiv :
		$('<div id="datepicker_div_' + this._id + '" class="datepicker_inline">'));
	// customise the date picker object - uses manager defaults if not overridden
	this._settings = extendRemove(settings || {}); // clone
	if (inline)
		this._setDate(this._getDefaultDate());
}

$.extend(DatepickerInstance.prototype, {
	/* Get a setting value, defaulting if necessary. */
	_get: function(name) {
		return this._settings[name] !== undefined ? this._settings[name] : $.datepicker._defaults[name];
	},

	/* Parse existing date and initialise date picker. */
	_setDateFromField: function(input) {
		this._input = $(input);
		var dateFormat = this._get('dateFormat');
		var dates = this._input ? this._input.val().split(this._get('rangeSeparator')) : null; 
		this._endDay = this._endMonth = this._endYear = null;
		var date = defaultDate = this._getDefaultDate();
		if (dates.length > 0) {
			var settings = this._getFormatConfig();
			if (dates.length > 1) {
				date = $.datepicker.parseDate(dateFormat, dates[1], settings) || defaultDate;
				this._endDay = date.getDate();
				this._endMonth = date.getMonth();
				this._endYear = date.getFullYear();
			}
			try {
				date = $.datepicker.parseDate(dateFormat, dates[0], settings) || defaultDate;
			} catch (e) {
				$.datepicker.log(e);
				date = defaultDate;
			}
		}
		this._selectedDay = date.getDate();
		this._drawMonth = this._selectedMonth = date.getMonth();
		this._drawYear = this._selectedYear = date.getFullYear();
		this._currentDay = (dates[0] ? date.getDate() : 0);
		this._currentMonth = (dates[0] ? date.getMonth() : 0);
		this._currentYear = (dates[0] ? date.getFullYear() : 0);
		this._adjustDate();
	},
	
	/* Retrieve the default date shown on opening. */
	_getDefaultDate: function() {
		var date = this._determineDate('defaultDate', new Date());
		var minDate = this._getMinMaxDate('min', true);
		var maxDate = this._getMinMaxDate('max');
		date = (minDate && date < minDate ? minDate : date);
		date = (maxDate && date > maxDate ? maxDate : date);
		return date;
	},

	/* A date may be specified as an exact value or a relative one. */
	_determineDate: function(name, defaultDate) {
		var offsetNumeric = function(offset) {
			var date = new Date();
			date.setDate(date.getDate() + offset);
			return date;
		};
		var offsetString = function(offset, getDaysInMonth) {
			var date = new Date();
			var matches = /^([+-]?[0-9]+)\s*(d|D|w|W|m|M|y|Y)?$/.exec(offset);
			if (matches) {
				var year = date.getFullYear();
				var month = date.getMonth();
				var day = date.getDate();
				switch (matches[2] || 'd') {
					case 'd' : case 'D' :
						day += (matches[1] - 0); break;
					case 'w' : case 'W' :
						day += (matches[1] * 7); break;
					case 'm' : case 'M' :
						month += (matches[1] - 0); 
						day = Math.min(day, getDaysInMonth(year, month));
						break;
					case 'y': case 'Y' :
						year += (matches[1] - 0);
						day = Math.min(day, getDaysInMonth(year, month));
						break;
				}
				date = new Date(year, month, day);
			}
			return date;
		};
		var date = this._get(name);
		return (date == null ? defaultDate :
			(typeof date == 'string' ? offsetString(date, this._getDaysInMonth) :
			(typeof date == 'number' ? offsetNumeric(date) : date)));
	},

	/* Set the date(s) directly. */
	_setDate: function(date, endDate) {
		this._selectedDay = this._currentDay = date.getDate();
		this._drawMonth = this._selectedMonth = this._currentMonth = date.getMonth();
		this._drawYear = this._selectedYear = this._currentYear = date.getFullYear();
		if (this._get('rangeSelect')) {
			if (endDate) {
				this._endDay = endDate.getDate();
				this._endMonth = endDate.getMonth();
				this._endYear = endDate.getFullYear();
			} else {
				this._endDay = this._currentDay;
				this._endMonth = this._currentMonth;
				this._endYear = this._currentYear;
			}
		}
		this._adjustDate();
	},

	/* Retrieve the date(s) directly. */
	_getDate: function() {
		var startDate = (!this._currentYear || (this._input && this._input.val() == '') ? null :
			new Date(this._currentYear, this._currentMonth, this._currentDay));
		if (this._get('rangeSelect')) {
			return [startDate, (!this._endYear ? null :
				new Date(this._endYear, this._endMonth, this._endDay))];
		} else
			return startDate;
	},

	/* Generate the HTML for the current state of the date picker. */
	_generateDatepicker: function() {
		var today = new Date();
		today = new Date(today.getFullYear(), today.getMonth(), today.getDate()); // clear time
		var showStatus = this._get('showStatus');
		var isRTL = this._get('isRTL');
		// build the date picker HTML
		var clear = (this._get('mandatory') ? '' :
			'<div class="datepicker_clear"><a onclick="jQuery.datepicker._clearDate(' + this._id + ');"' + 
			(showStatus ? this._addStatus(this._get('clearStatus') || '&#xa0;') : '') + '>' +
			this._get('clearText') + '</a></div>');
		var controls = '<div class="datepicker_control">' + (isRTL ? '' : clear) +
			'<div class="datepicker_close"><a onclick="jQuery.datepicker._hideDatepicker();"' +
			(showStatus ? this._addStatus(this._get('closeStatus') || '&#xa0;') : '') + '>' +
			this._get('closeText') + '</a></div>' + (isRTL ? clear : '')  + '</div>';
		var prompt = this._get('prompt');
		var closeAtTop = this._get('closeAtTop');
		var hideIfNoPrevNext = this._get('hideIfNoPrevNext');
		var numMonths = this._getNumberOfMonths();
		var stepMonths = this._get('stepMonths');
		var isMultiMonth = (numMonths[0] != 1 || numMonths[1] != 1);
		var minDate = this._getMinMaxDate('min', true);
		var maxDate = this._getMinMaxDate('max');
		var drawMonth = this._drawMonth;
		var drawYear = this._drawYear;
		if (maxDate) {
			var maxDraw = new Date(maxDate.getFullYear(),
				maxDate.getMonth() - numMonths[1] + 1, maxDate.getDate());
			maxDraw = (minDate && maxDraw < minDate ? minDate : maxDraw);
			while (new Date(drawYear, drawMonth, 1) > maxDraw) {
				drawMonth--;
				if (drawMonth < 0) {
					drawMonth = 11;
					drawYear--;
				}
			}
		}
		// controls and links
		var prev = '<div class="datepicker_prev">' + (this._canAdjustMonth(-1, drawYear, drawMonth) ? 
			'<a onclick="jQuery.datepicker._adjustDate(' + this._id + ', -' + stepMonths + ', \'M\');"' +
			(showStatus ? this._addStatus(this._get('prevStatus') || '&#xa0;') : '') + '>' +
			this._get('prevText') + '</a>' :
			(hideIfNoPrevNext ? '' : '<label>' + this._get('prevText') + '</label>')) + '</div>';
		var next = '<div class="datepicker_next">' + (this._canAdjustMonth(+1, drawYear, drawMonth) ?
			'<a onclick="jQuery.datepicker._adjustDate(' + this._id + ', +' + stepMonths + ', \'M\');"' +
			(showStatus ? this._addStatus(this._get('nextStatus') || '&#xa0;') : '') + '>' +
			this._get('nextText') + '</a>' :
			(hideIfNoPrevNext ? '>' : '<label>' + this._get('nextText') + '</label>')) + '</div>';
		var html = (prompt ? '<div class="datepicker_prompt">' + prompt + '</div>' : '') +
			(closeAtTop && !this._inline ? controls : '') +
			'<div class="datepicker_links">' + (isRTL ? next : prev) +
			(this._isInRange(today) ? '<div class="datepicker_current">' +
			'<a onclick="jQuery.datepicker._gotoToday(' + this._id + ');"' +
			(showStatus ? this._addStatus(this._get('currentStatus') || '&#xa0;') : '') + '>' +
			this._get('currentText') + '</a></div>' : '') + (isRTL ? prev : next) + '</div>';
		var showWeeks = this._get('showWeeks');
		for (var row = 0; row < numMonths[0]; row++)
			for (var col = 0; col < numMonths[1]; col++) {
				var selectedDate = new Date(drawYear, drawMonth, this._selectedDay);
				html += '<div class="datepicker_oneMonth' + (col == 0 ? ' datepicker_newRow' : '') + '">' +
					this._generateMonthYearHeader(drawMonth, drawYear, minDate, maxDate,
					selectedDate, row > 0 || col > 0) + // draw month headers
					'<table class="datepicker" cellpadding="0" cellspacing="0"><thead>' + 
					'<tr class="datepicker_titleRow">' +
					(showWeeks ? '<td>' + this._get('weekHeader') + '</td>' : '');
				var firstDay = this._get('firstDay');
				var changeFirstDay = this._get('changeFirstDay');
				var dayNames = this._get('dayNames');
				var dayNamesShort = this._get('dayNamesShort');
				var dayNamesMin = this._get('dayNamesMin');
				for (var dow = 0; dow < 7; dow++) { // days of the week
					var day = (dow + firstDay) % 7;
					var status = this._get('dayStatus') || '&#xa0;';
					status = (status.indexOf('DD') > -1 ? status.replace(/DD/, dayNames[day]) :
						status.replace(/D/, dayNamesShort[day]));
					html += '<td' + ((dow + firstDay + 6) % 7 >= 5 ? ' class="datepicker_weekEndCell"' : '') + '>' +
						(!changeFirstDay ? '<span' :
						'<a onclick="jQuery.datepicker._changeFirstDay(' + this._id + ', ' + day + ');"') + 
						(showStatus ? this._addStatus(status) : '') + ' title="' + dayNames[day] + '">' +
						dayNamesMin[day] + (changeFirstDay ? '</a>' : '</span>') + '</td>';
				}
				html += '</tr></thead><tbody>';
				var daysInMonth = this._getDaysInMonth(drawYear, drawMonth);
				if (drawYear == this._selectedYear && drawMonth == this._selectedMonth) {
					this._selectedDay = Math.min(this._selectedDay, daysInMonth);
				}
				var leadDays = (this._getFirstDayOfMonth(drawYear, drawMonth) - firstDay + 7) % 7;
				var currentDate = (!this._currentDay ? new Date(9999, 9, 9) :
					new Date(this._currentYear, this._currentMonth, this._currentDay));
				var endDate = this._endDay ? new Date(this._endYear, this._endMonth, this._endDay) : currentDate;
				var printDate = new Date(drawYear, drawMonth, 1 - leadDays);
				var numRows = (isMultiMonth ? 6 : Math.ceil((leadDays + daysInMonth) / 7)); // calculate the number of rows to generate
				var beforeShowDay = this._get('beforeShowDay');
				var showOtherMonths = this._get('showOtherMonths');
				var calculateWeek = this._get('calculateWeek') || $.datepicker.iso8601Week;
				var dateStatus = this._get('statusForDate') || $.datepicker.dateStatus;
				for (var dRow = 0; dRow < numRows; dRow++) { // create date picker rows
					html += '<tr class="datepicker_daysRow">' +
						(showWeeks ? '<td class="datepicker_weekCol">' + calculateWeek(printDate) + '</td>' : '');
					for (var dow = 0; dow < 7; dow++) { // create date picker days
						var daySettings = (beforeShowDay ?
							beforeShowDay.apply((this._input ? this._input[0] : null), [printDate]) : [true, '']);
						var otherMonth = (printDate.getMonth() != drawMonth);
						var unselectable = otherMonth || !daySettings[0] ||
							(minDate && printDate < minDate) || (maxDate && printDate > maxDate);
						html += '<td class="datepicker_daysCell' +
							((dow + firstDay + 6) % 7 >= 5 ? ' datepicker_weekEndCell' : '') + // highlight weekends
							(otherMonth ? ' datepicker_otherMonth' : '') + // highlight days from other months
							(printDate.getTime() == selectedDate.getTime() && drawMonth == this._selectedMonth ?
							' datepicker_daysCellOver' : '') + // highlight selected day
							(unselectable ? ' datepicker_unselectable' : '') +  // highlight unselectable days
							(otherMonth && !showOtherMonths ? '' : ' ' + daySettings[1] + // highlight custom dates
							(printDate.getTime() >= currentDate.getTime() && printDate.getTime() <= endDate.getTime() ?  // in current range
							' datepicker_currentDay' : '') + // highlight selected day
							(printDate.getTime() == today.getTime() ? ' datepicker_today' : '')) + '"' + // highlight today (if different)
							(unselectable ? '' : ' onmouseover="jQuery(this).addClass(\'datepicker_daysCellOver\');' +
							(!showStatus || (otherMonth && !showOtherMonths) ? '' : 'jQuery(\'#datepicker_status_' +
							this._id + '\').html(\'' + (dateStatus.apply((this._input ? this._input[0] : null),
							[printDate, this]) || '&#xa0;') +'\');') + '"' +
							' onmouseout="jQuery(this).removeClass(\'datepicker_daysCellOver\');' +
							(!showStatus || (otherMonth && !showOtherMonths) ? '' : 'jQuery(\'#datepicker_status_' +
							this._id + '\').html(\'&#xa0;\');') + '" onclick="jQuery.datepicker._selectDay(' +
							this._id + ',' + drawMonth + ',' + drawYear + ', this);"') + '>' + // actions
							(otherMonth ? (showOtherMonths ? printDate.getDate() : '&#xa0;') : // display for other months
							(unselectable ? printDate.getDate() : '<a>' + printDate.getDate() + '</a>')) + '</td>'; // display for this month
						printDate.setDate(printDate.getDate() + 1);
					}
					html += '</tr>';
				}
				drawMonth++;
				if (drawMonth > 11) {
					drawMonth = 0;
					drawYear++;
				}
				html += '</tbody></table></div>';
			}
		html += (showStatus ? '<div style="clear: both;"></div><div id="datepicker_status_' + this._id + 
			'" class="datepicker_status">' + (this._get('initStatus') || '&#xa0;') + '</div>' : '') +
			(!closeAtTop && !this._inline ? controls : '') +
			'<div style="clear: both;"></div>' + 
			($.browser.msie && parseInt($.browser.version) < 7 && !this._inline ? 
			'<iframe src="javascript:false;" class="datepicker_cover"></iframe>' : '');
		return html;
	},
	
	/* Generate the month and year header. */
	_generateMonthYearHeader: function(drawMonth, drawYear, minDate, maxDate, selectedDate, secondary) {
		minDate = (this._rangeStart && minDate && selectedDate < minDate ? selectedDate : minDate);
		var showStatus = this._get('showStatus');
		var html = '<div class="datepicker_header">';
		// month selection
		var monthNames = this._get('monthNames');
		if (secondary || !this._get('changeMonth'))
			html += monthNames[drawMonth] + '&#xa0;';
			
		else {
			var inMinYear = (minDate && minDate.getFullYear() == drawYear);
			var inMaxYear = (maxDate && maxDate.getFullYear() == drawYear);
			html += '<select class="datepicker_newMonth" ' +
				'onchange="jQuery.datepicker._selectMonthYear(' + this._id + ', this, \'M\');" ' +
				'onclick="jQuery.datepicker._clickMonthYear(' + this._id + ');"' +
				(showStatus ? this._addStatus(this._get('monthStatus') || '&#xa0;') : '') + '>';
			for (var month = 0; month < 12; month++) {
				if ((!inMinYear || month >= minDate.getMonth()) &&
						(!inMaxYear || month <= maxDate.getMonth())) {
					html += '<option value="' + month + '"' +
						(month == drawMonth ? ' selected="selected"' : '') +
						'>' + monthNames[month] + '</option>';
				}
			}
			html += '</select>';
		}
		// year selection
		if (secondary || !this._get('changeYear'))
			html += drawYear;
		else {
			// determine range of years to display
			var years = this._get('yearRange').split(':');
			var year = 0;
			var endYear = 0;
			if (years.length != 2) {
				year = drawYear - 10;
				endYear = drawYear + 10;
			} else if (years[0].charAt(0) == '+' || years[0].charAt(0) == '-') {
				year = drawYear + parseInt(years[0], 10);
				endYear = drawYear + parseInt(years[1], 10);
			} else {
				year = parseInt(years[0], 10);
				endYear = parseInt(years[1], 10);
			}
			year = (minDate ? Math.max(year, minDate.getFullYear()) : year);
			endYear = (maxDate ? Math.min(endYear, maxDate.getFullYear()) : endYear);
			html += '<select class="datepicker_newYear" ' +
				'onchange="jQuery.datepicker._selectMonthYear(' + this._id + ', this, \'Y\');" ' +
				'onclick="jQuery.datepicker._clickMonthYear(' + this._id + ');"' +
				(showStatus ? this._addStatus(this._get('yearStatus') || '&#xa0;') : '') + '>';
			for (; year <= endYear; year++) {
				html += '<option value="' + year + '"' +
					(year == drawYear ? ' selected="selected"' : '') +
					'>' + year + '</option>';
			}
			html += '</select>';
		}
		html += '</div>'; // Close datepicker_header
		return html;
	},

	/* Provide code to set and clear the status panel. */
	_addStatus: function(text) {
		return ' onmouseover="jQuery(\'#datepicker_status_' + this._id + '\').html(\'' + text + '\');" ' +
			'onmouseout="jQuery(\'#datepicker_status_' + this._id + '\').html(\'&#xa0;\');"';
	},

	/* Adjust one of the date sub-fields. */
	_adjustDate: function(offset, period) {
		var year = this._drawYear + (period == 'Y' ? offset : 0);
		var month = this._drawMonth + (period == 'M' ? offset : 0);
		var day = Math.min(this._selectedDay, this._getDaysInMonth(year, month)) +
			(period == 'D' ? offset : 0);
		var date = new Date(year, month, day);
		// ensure it is within the bounds set
		var minDate = this._getMinMaxDate('min', true);
		var maxDate = this._getMinMaxDate('max');
		date = (minDate && date < minDate ? minDate : date);
		date = (maxDate && date > maxDate ? maxDate : date);
		this._selectedDay = date.getDate();
		this._drawMonth = this._selectedMonth = date.getMonth();
		this._drawYear = this._selectedYear = date.getFullYear();
	},
	
	/* Determine the number of months to show. */
	_getNumberOfMonths: function() {
		var numMonths = this._get('numberOfMonths');
		return (numMonths == null ? [1, 1] : (typeof numMonths == 'number' ? [1, numMonths] : numMonths));
	},

	/* Determine the current maximum date - ensure no time components are set - may be overridden for a range. */
	_getMinMaxDate: function(minMax, checkRange) {
		var date = this._determineDate(minMax + 'Date', null);
		if (date) {
			date.setHours(0);
			date.setMinutes(0);
			date.setSeconds(0);
			date.setMilliseconds(0);
		}
		return date || (checkRange ? this._rangeStart : null);
	},

	/* Find the number of days in a given month. */
	_getDaysInMonth: function(year, month) {
		return 32 - new Date(year, month, 32).getDate();
	},

	/* Find the day of the week of the first of a month. */
	_getFirstDayOfMonth: function(year, month) {
		return new Date(year, month, 1).getDay();
	},

	/* Determines if we should allow a "next/prev" month display change. */
	_canAdjustMonth: function(offset, curYear, curMonth) {
		var numMonths = this._getNumberOfMonths();
		var date = new Date(curYear, curMonth + (offset < 0 ? offset : numMonths[1]), 1);
		if (offset < 0)
			date.setDate(this._getDaysInMonth(date.getFullYear(), date.getMonth()));
		return this._isInRange(date);
	},

	/* Is the given date in the accepted range? */
	_isInRange: function(date) {
		// during range selection, use minimum of selected date and range start
		var newMinDate = (!this._rangeStart ? null :
			new Date(this._selectedYear, this._selectedMonth, this._selectedDay));
		newMinDate = (newMinDate && this._rangeStart < newMinDate ? this._rangeStart : newMinDate);
		var minDate = newMinDate || this._getMinMaxDate('min');
		var maxDate = this._getMinMaxDate('max');
		return ((!minDate || date >= minDate) && (!maxDate || date <= maxDate));
	},
	
	/* Provide the configuration settings for formatting/parsing. */
	_getFormatConfig: function() {
		var shortYearCutoff = this._get('shortYearCutoff');
		shortYearCutoff = (typeof shortYearCutoff != 'string' ? shortYearCutoff :
			new Date().getFullYear() % 100 + parseInt(shortYearCutoff, 10));
		return {shortYearCutoff: shortYearCutoff,
			dayNamesShort: this._get('dayNamesShort'), dayNames: this._get('dayNames'),
			monthNamesShort: this._get('monthNamesShort'), monthNames: this._get('monthNames')};
	},

	/* Format the given date for display. */
	_formatDate: function(day, month, year) {
		if (!day) {
			this._currentDay = this._selectedDay;
			this._currentMonth = this._selectedMonth;
			this._currentYear = this._selectedYear;
		}
		var date = (day ? (typeof day == 'object' ? day : new Date(year, month, day)) :
			new Date(this._currentYear, this._currentMonth, this._currentDay));
		return $.datepicker.formatDate(this._get('dateFormat'), date, this._getFormatConfig());
	}
});

/* jQuery extend now ignores nulls! */
function extendRemove(target, props) {
	$.extend(target, props);
	for (var name in props)
		if (props[name] == null)
			target[name] = null;
	return target;
};

/* Invoke the datepicker functionality.
   @param  options  String - a command, optionally followed by additional parameters or
                    Object - settings for attaching new datepicker functionality
   @return  jQuery object */
$.fn.datepicker = function(options){
	var otherArgs = Array.prototype.slice.call(arguments, 1);
	if (typeof options == 'string' && (options == 'isDisabled' || options == 'getDate')) {
		return $.datepicker['_' + options + 'Datepicker'].apply($.datepicker, [this[0]].concat(otherArgs));
	}
	return this.each(function() {
		typeof options == 'string' ?
			$.datepicker['_' + options + 'Datepicker'].apply($.datepicker, [this].concat(otherArgs)) :
			$.datepicker._attachDatepicker(this, options);
	});
};

$.datepicker = new Datepicker(); // singleton instance
	
/* Initialise the date picker. */
$(document).ready(function() {
	$(document.body).append($.datepicker._datepickerDiv)
		.mousedown($.datepicker._checkExternalClick);
});

})(jQuery);/*
 * jQuery UI Dialog
 *
 * Copyright (c) 2008 Richard D. Worth (rdworth.org)
 * Dual licensed under the MIT (MIT-LICENSE.txt)
 * and GPL (GPL-LICENSE.txt) licenses.
 * 
 * http://docs.jquery.com/UI/Dialog
 *
 * Depends:
 *	ui.core.js
 *	ui.draggable.js
 *	ui.resizable.js
 *
 * Revision: $Id: jquery.ui.js,v 1.1 2008/05/14 15:34:12 m.moossen Exp $
 */
;(function($) {
	
	var setDataSwitch = {
		"dragStart": "start.draggable",
		"drag": "drag.draggable",
		"dragStop": "stop.draggable",
		"maxHeight": "maxHeight.resizable",
		"minHeight": "minHeight.resizable",
		"maxWidth": "maxWidth.resizable",
		"minWidth": "minWidth.resizable",
		"resizeStart": "start.resizable",
		"resize": "drag.resizable",
		"resizeStop": "stop.resizable"
	};
	
	$.widget("ui.dialog", {
		init: function() {
			var self = this;
			var options = this.options;
			
			var uiDialogContent = $(this.element).addClass('ui-dialog-content');
			
			if (!uiDialogContent.parent().length) {
				uiDialogContent.appendTo('body');
			}
			uiDialogContent
				.wrap(document.createElement('div'))
				.wrap(document.createElement('div'));
			var uiDialogContainer = uiDialogContent.parent().addClass('ui-dialog-container').css({position: 'relative'});
			var uiDialog = this.uiDialog = uiDialogContainer.parent().hide()
				.addClass('ui-dialog')
				.css({position: 'absolute', width: options.width, height: options.height, overflow: 'hidden'}); 
	
			var classNames = uiDialogContent.attr('className').split(' ');
	
			// Add content classes to dialog, to inherit theme at top level of element
			$.each(classNames, function(i, className) {
				if (className != 'ui-dialog-content')
					uiDialog.addClass(className);
			});
	
			if ($.fn.resizable) {
				uiDialog.append('<div class="ui-resizable-n ui-resizable-handle"></div>')
					.append('<div class="ui-resizable-s ui-resizable-handle"></div>')
					.append('<div class="ui-resizable-e ui-resizable-handle"></div>')
					.append('<div class="ui-resizable-w ui-resizable-handle"></div>')
					.append('<div class="ui-resizable-ne ui-resizable-handle"></div>')
					.append('<div class="ui-resizable-se ui-resizable-handle"></div>')
					.append('<div class="ui-resizable-sw ui-resizable-handle"></div>')
					.append('<div class="ui-resizable-nw ui-resizable-handle"></div>');
				uiDialog.resizable({
					maxWidth: options.maxWidth,
					maxHeight: options.maxHeight,
					minWidth: options.minWidth,
					minHeight: options.minHeight,
					start: options.resizeStart,
					resize: options.resize,
					stop: function(e, ui) {
						options.resizeStop && options.resizeStop.apply(this, arguments);
						$.ui.dialog.overlay.resize();
					}
				});
				if (!options.resizable)
					uiDialog.resizable('disable');
			}
	
			uiDialogContainer.prepend('<div class="ui-dialog-titlebar"></div>');
			var uiDialogTitlebar = $('.ui-dialog-titlebar', uiDialogContainer);
			this.uiDialogTitlebar = uiDialogTitlebar;
			var title = (options.title) ? options.title : (uiDialogContent.attr('title')) ? uiDialogContent.attr('title') : '';
			uiDialogTitlebar.append('<span class="ui-dialog-title">' + title + '</span>');
			uiDialogTitlebar.append('<a href="#" class="ui-dialog-titlebar-close"><span>X</span></a>');
			this.uiDialogTitlebarClose = $('.ui-dialog-titlebar-close', uiDialogTitlebar)
				.hover(function() { $(this).addClass('ui-dialog-titlebar-close-hover'); }, 
					function() { $(this).removeClass('ui-dialog-titlebar-close-hover'); }
				)
				.mousedown(function(ev) {
					ev.stopPropagation();
				})
				.click(function() {
					self.close();
					return false;
				});
			
			// setting tabindex makes the div focusable
			// setting outline to 0 prevents a border on focus in Mozilla
			uiDialog.attr('tabindex', -1).css('outline', 0).keydown(function(ev) {
				if (options.closeOnEscape) {
					var ESC = 27;
					ev.keyCode && ev.keyCode == ESC && self.close();
				}
			});
			
			var hasButtons = false;
			$.each(options.buttons, function() { return !(hasButtons = true); });
			if (hasButtons) {
				var uiDialogButtonPane = $('<div class="ui-dialog-buttonpane"/>')
					.appendTo(uiDialog);
				$.each(options.buttons, function(name, fn) {
					$(document.createElement('button'))
						.text(name)
						.click(function() { fn.apply(self.element, arguments) })
						.appendTo(uiDialogButtonPane);
				});
			}
			
			if ($.fn.draggable) {
				uiDialog.draggable({
					handle: '.ui-dialog-titlebar',
					start: function(e, ui) {
						self.activate();
						options.dragStart && options.dragStart.apply(this, arguments);
					},
					drag: options.drag,
					stop: function(e, ui) {
						options.dragStop && options.dragStop.apply(this, arguments);
						$.ui.dialog.overlay.resize();
					}
				});
				if (!options.draggable)
					uiDialog.draggable('disable')
			}
		
			uiDialog.mousedown(function() {
				self.activate();
			});
			uiDialogTitlebar.click(function() {
				self.activate();
			});
			
			options.bgiframe && $.fn.bgiframe && uiDialog.bgiframe();
			
			if (options.autoOpen) {
				this.open();
			};
		},
		
		setData: function(event, key, value){
			setDataSwitch[key] && this.uiDialog.data(setDataSwitch[key], value);
			switch (key) {
				case "draggable":
					this.uiDialog.draggable(value ? 'enable' : 'disable');
					break;
				case "height":
					this.uiDialog.height(value);
					break;
				case "position":
					this.position(value);
					break;
				case "resizable":
					this.uiDialog.resizable(value ? 'enable' : 'disable');
					break;
				case "title":
					$(".ui-dialog-title", this.uiDialogTitlebar).text(value);
					break;
				case "width":
					this.uiDialog.width(value);
					break;
			}
			this.options[key] = value;
		},
		
		position: function(pos) {
			var wnd = $(window), doc = $(document), minTop = top = doc.scrollTop(), left = doc.scrollLeft();
			if ($.inArray(pos, ['center','top','right','bottom','left']) >= 0) {
				pos = [pos == 'right' || pos == 'left' ? pos : 'center', pos == 'top' || pos == 'bottom' ? pos : 'middle'];
			}
			if (pos.constructor != Array) {
				pos == ['center', 'middle']
			}
			if (pos[0].constructor == Number) {
				left += pos[0];
			} else {
				switch (pos[0]) {
					case 'left':
						left += 0;
						break;
					case 'right':
						left += (wnd.width()) - (this.uiDialog.width());
						break;
					case 'center':
					default:
						left += (wnd.width() / 2) - (this.uiDialog.width() / 2);
				}
			}
			if (pos[1].constructor == Number) {
				top += pos[1];
			} else {
				switch (pos[1]) {
					case 'top':
						top += 0;
						break;
					case 'bottom':
						top += (wnd.height()) - (this.uiDialog.height());
						break;
					case 'middle':
					default:
						top += (wnd.height() / 2) - (this.uiDialog.height() / 2);
				}
			}
			top = top < minTop ? minTop : top;
			this.uiDialog.css({top: top, left: left});
		},
			
		open: function() {
			this.overlay = this.options.modal ? new $.ui.dialog.overlay(self) : null;
			this.uiDialog.appendTo('body');
			this.position(this.options.position);
			this.uiDialog.show();
			this.moveToTop();
			this.activate();
			
			// CALLBACK: open
			var openEV = null;
			var openUI = {
				options: this.options
			};
			this.uiDialogTitlebarClose.focus();
			$(this.element).triggerHandler("dialogopen", [openEV, openUI], this.options.open);
		},
	
		activate: function() {
			// Move modeless dialogs to the top when they're activated. Even
			// if there is a modal dialog in the window, the modeless dialog
			// should be on top because it must have been opened after the modal
			// dialog. Modal dialogs don't get moved to the top because that
			// would make any modeless dialogs that it spawned unusable until
			// the modal dialog is closed.
			!this.options.modal && this.moveToTop();
		},
			
		moveToTop: function() {
			var maxZ = this.options.zIndex, options = this.options;
			$('.ui-dialog:visible').each(function() {
				maxZ = Math.max(maxZ, parseInt($(this).css('z-index'), 10) || options.zIndex);
			});
			this.overlay && this.overlay.$el.css('z-index', ++maxZ);
			this.uiDialog.css('z-index', ++maxZ);
		},
			
		close: function() {
			this.overlay && this.overlay.destroy();
			this.uiDialog.hide();

			// CALLBACK: close
			var closeEV = null;
			var closeUI = {
				options: this.options
			};
			$(this.element).triggerHandler("dialogclose", [closeEV, closeUI], this.options.close);
			$.ui.dialog.overlay.resize();
		},
		
		destroy: function() {
			this.overlay && this.overlay.destroy();
			this.uiDialog.hide();
			$(this.element).unbind('.dialog').removeClass('ui-dialog-content').hide().appendTo('body');
			this.uiDialog.remove();
			$.removeData(this.element, "dialog");
		}
	});
	
	$.extend($.ui.dialog, {
		defaults: {
			autoOpen: true,
			bgiframe: false,
			buttons: {},
			closeOnEscape: true,
			draggable: true,
			height: 200,
			minHeight: 100,
			minWidth: 150,
			modal: false,
			overlay: {},
			position: 'center',
			resizable: true,
			width: 300,
			zIndex: 1000
		},
		
		overlay: function(dialog) {
			this.$el = $.ui.dialog.overlay.create(dialog);
		}
	});
	
	$.extend($.ui.dialog.overlay, {
		instances: [],
		events: $.map('focus,mousedown,mouseup,keydown,keypress,click'.split(','),
			function(e) { return e + '.dialog-overlay'; }).join(' '),
		create: function(dialog) {
			if (this.instances.length === 0) {
				// prevent use of anchors and inputs
				$('a, :input').bind(this.events, function() {
					// allow use of the element if inside a dialog and
					// - there are no modal dialogs
					// - there are modal dialogs, but we are in front of the topmost modal
					var allow = false;
					var $dialog = $(this).parents('.ui-dialog');
					if ($dialog.length) {
						var $overlays = $('.ui-dialog-overlay');
						if ($overlays.length) {
							var maxZ = parseInt($overlays.css('z-index'), 10);
							$overlays.each(function() {
								maxZ = Math.max(maxZ, parseInt($(this).css('z-index'), 10));
							});
							allow = parseInt($dialog.css('z-index'), 10) > maxZ;
						} else {
							allow = true;
						}
					}
					return allow;
				});
				
				// allow closing by pressing the escape key
				$(document).bind('keydown.dialog-overlay', function(e) {
					var ESC = 27;
					e.keyCode && e.keyCode == ESC && dialog.close(); 
				});
				
				// handle window resize
				$(window).bind('resize.dialog-overlay', $.ui.dialog.overlay.resize);
			}
			
			var $el = $('<div/>').appendTo(document.body)
				.addClass('ui-dialog-overlay').css($.extend({
					borderWidth: 0, margin: 0, padding: 0,
					position: 'absolute', top: 0, left: 0,
					width: this.width(),
					height: this.height()
				}, dialog.options.overlay));
			
			dialog.options.bgiframe && $.fn.bgiframe && $el.bgiframe();
			
			this.instances.push($el);
			return $el;
		},
		
		destroy: function($el) {
			this.instances.splice($.inArray(this.instances, $el), 1);
			
			if (this.instances.length === 0) {
				$('a, :input').add([document, window]).unbind('.dialog-overlay');
			}
			
			$el.remove();
		},
		
		height: function() {
			if ($.browser.msie && $.browser.version < 7) {
				var scrollHeight = Math.max(
					document.documentElement.scrollHeight,
					document.body.scrollHeight
				);
				var offsetHeight = Math.max(
					document.documentElement.offsetHeight,
					document.body.offsetHeight
				);
				
				if (scrollHeight < offsetHeight) {
					return $(window).height() + 'px';
				} else {
					return scrollHeight + 'px';
				}
			} else {
				return $(document).height() + 'px';
			}
		},
		
		width: function() {
			if ($.browser.msie && $.browser.version < 7) {
				var scrollWidth = Math.max(
					document.documentElement.scrollWidth,
					document.body.scrollWidth
				);
				var offsetWidth = Math.max(
					document.documentElement.offsetWidth,
					document.body.offsetWidth
				);
				
				if (scrollWidth < offsetWidth) {
					return $(window).width() + 'px';
				} else {
					return scrollWidth + 'px';
				}
			} else {
				return $(document).width() + 'px';
			}
		},
		
		resize: function() {
			/* If the dialog is draggable and the user drags it past the
			 * right edge of the window, the document becomes wider so we
			 * need to stretch the overlay. If the user then drags the
			 * dialog back to the left, the document will become narrower,
			 * so we need to shrink the overlay to the appropriate size.
			 * This is handled by shrinking the overlay before setting it
			 * to the full document size.
			 */
			var $overlays = $([]);
			$.each($.ui.dialog.overlay.instances, function() {
				$overlays = $overlays.add(this);
			});
			
			$overlays.css({
				width: 0,
				height: 0
			}).css({
				width: $.ui.dialog.overlay.width(),
				height: $.ui.dialog.overlay.height()
			});
		}
	});
	
	$.extend($.ui.dialog.overlay.prototype, {
		destroy: function() {
			$.ui.dialog.overlay.destroy(this.$el);
		}
	});

})(jQuery);
/*
 * jQuery UI Slider
 *
 * Copyright (c) 2008 Paul Bakaus
 * Dual licensed under the MIT (MIT-LICENSE.txt)
 * and GPL (GPL-LICENSE.txt) licenses.
 * 
 * http://docs.jquery.com/UI/Slider
 *
 * Depends:
 *	ui.core.js
 *
 * Revision: $Id: jquery.ui.js,v 1.1 2008/05/14 15:34:12 m.moossen Exp $
 */
;(function($) {

	$.widget("ui.slider", {
		init: function() {
			var self = this;
			this.element.addClass("ui-slider");
			this.initBoundaries();
			
			// Initialize mouse and key events for interaction
			this.handle = $(this.options.handle, this.element);
			if (!this.handle.length) {
				self.handle = self.generated = $(self.options.handles || [0]).map(function() {
					var handle = $("<div/>").addClass("ui-slider-handle").appendTo(self.element);
					if (this.id)
						handle.attr("id", this.id);
					return handle[0];
				});
			}
			$(this.handle)
				.mouse({
					executor: this,
					delay: this.options.delay,
					distance: this.options.distance,
					dragPrevention: this.options.prevention ? this.options.prevention.toLowerCase().split(',') : ['input','textarea','button','select','option'],
					start: this.start,
					stop: this.stop,
					drag: this.drag,
					condition: function(e, handle) {
						if(!this.disabled) {
							if(this.currentHandle) this.blur(this.currentHandle);
							this.focus(handle,1);
							return !this.disabled;
						}
					}
				})
				.wrap('<a href="javascript:void(0)" style="cursor:default;"></a>')
				.parent()
					.bind('focus', function(e) { self.focus(this.firstChild); })
					.bind('blur', function(e) { self.blur(this.firstChild); })
					.bind('keydown', function(e) {
						if(/(37|38|39|40)/.test(e.keyCode)) {
							self.moveTo({
								x: /(37|39)/.test(e.keyCode) ? (e.keyCode == 37 ? '-' : '+') + '=' + self.oneStep(1) : null,
								y: /(38|40)/.test(e.keyCode) ? (e.keyCode == 38 ? '-' : '+') + '=' + self.oneStep(2) : null
							}, this.firstChild);
						}
					})
			;
			
			// Prepare dynamic properties for later use
			this.actualSize = { width: this.element.outerWidth() , height: this.element.outerHeight() };
			
			// Bind the click to the slider itself
			this.element.bind('mousedown.slider', function(e) {
				self.click.apply(self, [e]);
				self.currentHandle.data("mouse").trigger(e);
				self.firstValue = self.firstValue + 1; //This is for always triggering the change event
			});
			
			// Move the first handle to the startValue
			$.each(this.options.handles || [], function(index, handle) {
				self.moveTo(handle.start, index, true);
			});
			if (!isNaN(this.options.startValue))
				this.moveTo(this.options.startValue, 0, true);

			this.previousHandle = $(this.handle[0]); //set the previous handle to the first to allow clicking before selecting the handle
			if(this.handle.length == 2 && this.options.range) this.createRange();
		},
		
		setData: function(event, key, value) {
			this.options[key] = value;
			if (/min|max|steps/.test(key)) {
				this.initBoundaries();
			}
		},
		
		initBoundaries: function() {
			var element = this.element[0];
			var o = this.options;
			$.extend(o, {
				axis: o.axis || (element.offsetWidth < element.offsetHeight ? 'vertical' : 'horizontal'),
				max: !isNaN(parseInt(o.max,10)) ? { x: parseInt(o.max, 10), y: parseInt(o.max, 10) } : ({ x: o.max && o.max.x || 100, y: o.max && o.max.y || 100 }),
				min: !isNaN(parseInt(o.min,10)) ? { x: parseInt(o.min, 10), y: parseInt(o.min, 10) } : ({ x: o.min && o.min.x || 0, y: o.min && o.min.y || 0 })
			});
			//Prepare the real maxValue
			o.realMax = {
				x: o.max.x - o.min.x,
				y: o.max.y - o.min.y
			};
			//Calculate stepping based on steps
			o.stepping = {
				x: o.stepping && o.stepping.x || parseInt(o.stepping, 10) || (o.steps ? o.realMax.x/(o.steps.x || parseInt(o.steps, 10) || o.realMax.x) : 0),
				y: o.stepping && o.stepping.y || parseInt(o.stepping, 10) || (o.steps ? o.realMax.y/(o.steps.y || parseInt(o.steps, 10) || o.realMax.y) : 0)
			};
		},
		plugins: {},
		createRange: function() {
			this.rangeElement = $('<div></div>')
				.addClass('ui-slider-range')
				.css({ position: 'absolute' })
				.appendTo(this.element);
			this.updateRange();
		},
		updateRange: function() {
				var prop = this.options.axis == "vertical" ? "top" : "left";
				var size = this.options.axis == "vertical" ? "height" : "width";
				this.rangeElement.css(prop, parseInt($(this.handle[0]).css(prop),10) + this.handleSize(0, this.options.axis == "vertical" ? 2 : 1)/2);
				this.rangeElement.css(size, parseInt($(this.handle[1]).css(prop),10) - parseInt($(this.handle[0]).css(prop),10));
		},
		getRange: function() {
			return this.rangeElement ? this.convertValue(parseInt(this.rangeElement.css(this.options.axis == "vertical" ? "height" : "width"),10)) : null;
		},
		ui: function(e) {
			return {
				instance: this,
				options: this.options,
				handle: this.currentHandle,
				value: this.options.axis != "both" || !this.options.axis ? Math.round(this.value(null,this.options.axis == "vertical" ? 2 : 1)) : {
					x: Math.round(this.value(null,1)),
					y: Math.round(this.value(null,2))
				},
				range: this.getRange()
			};
		},
		propagate: function(n,e) {
			$.ui.plugin.call(this, n, [e, this.ui()]);
			this.element.triggerHandler(n == "slide" ? n : "slide"+n, [e, this.ui()], this.options[n]);
		},
		destroy: function() {
			this.element
				.removeClass("ui-slider ui-slider-disabled")
				.removeData("slider")
				.unbind(".slider");
			this.handle.mouse("destroy");
			this.generated && this.generated.remove();
		},
		enable: function() {
			this.element.removeClass("ui-slider-disabled");
			this.disabled = false;
		},
		disable: function() {
			this.element.addClass("ui-slider-disabled");
			this.disabled = true;
		},
		focus: function(handle,hard) {
			this.currentHandle = $(handle).addClass('ui-slider-handle-active');
			if (hard)
				this.currentHandle.parent()[0].focus();
		},
		blur: function(handle) {
			$(handle).removeClass('ui-slider-handle-active');
			if(this.currentHandle && this.currentHandle[0] == handle) { this.previousHandle = this.currentHandle; this.currentHandle = null; };
		},
		value: function(handle, axis) {
			if(this.handle.length == 1) this.currentHandle = this.handle;
			if(!axis) axis = this.options.axis == "vertical" ? 2 : 1;
			
			var value = ((parseInt($(handle != undefined && handle !== null ? this.handle[handle] || handle : this.currentHandle).css(axis == 1 ? "left" : "top"),10) / (this.actualSize[axis == 1 ? "width" : "height"] - this.handleSize(handle,axis))) * this.options.realMax[axis == 1 ? "x" : "y"]) + this.options.min[axis == 1 ? "x" : "y"];
			
			var o = this.options;
			if (o.stepping[axis == 1 ? "x" : "y"]) {
				value = Math.round(value / o.stepping[axis == 1 ? "x" : "y"]) * o.stepping[axis == 1 ? "x" : "y"];
			}
			return value;
		},
		convertValue: function(value,axis) {
			if(!axis) axis = this.options.axis == "vertical" ? 2 : 1;
			return this.options.min[axis == 1 ? "x" : "y"] + (value / (this.actualSize[axis == 1 ? "width" : "height"] - this.handleSize(null,axis))) * this.options.realMax[axis == 1 ? "x" : "y"];
		},
		translateValue: function(value,axis) {
			if(!axis) axis = this.options.axis == "vertical" ? 2 : 1;
			return ((value - this.options.min[axis == 1 ? "x" : "y"]) / this.options.realMax[axis == 1 ? "x" : "y"]) * (this.actualSize[axis == 1 ? "width" : "height"] - this.handleSize(null,axis));
		},
		handleSize: function(handle,axis) {
			if(!axis) axis = this.options.axis == "vertical" ? 2 : 1;
			return $(handle != undefined && handle !== null ? this.handle[handle] : this.currentHandle)[0][axis == 1 ? "offsetWidth" : "offsetHeight"];	
		},
		click: function(e) {
			// This method is only used if:
			// - The user didn't click a handle
			// - The Slider is not disabled
			// - There is a current, or previous selected handle (otherwise we wouldn't know which one to move)
			
			var pointer = [e.pageX,e.pageY];
			
			var clickedHandle = false;
			this.handle.each(function() {
				if(this == e.target)
					clickedHandle = true;
			});
			if (clickedHandle || this.disabled || !(this.currentHandle || this.previousHandle))
				return;

			// If a previous handle was focussed, focus it again
			if (!this.currentHandle && this.previousHandle)
				this.focus(this.previousHandle, true);
			
			// Move focussed handle to the clicked position
			this.offset = this.element.offset();
			
			// propagate only for distance > 0, otherwise propagation is done my drag
			this.moveTo({
				y: this.convertValue(e.pageY - this.offset.top - this.currentHandle.outerHeight()/2),
				x: this.convertValue(e.pageX - this.offset.left - this.currentHandle.outerWidth()/2)
			}, null, !this.options.distance);
		},
		start: function(e, handle) {
		
			var o = this.options;
			
			// This is a especially ugly fix for strange blur events happening on mousemove events
			if (!this.currentHandle)
				this.focus(this.previousHandle, true); 

			this.offset = this.element.offset();
			this.handleOffset = this.currentHandle.offset();
			this.clickOffset = { top: e.pageY - this.handleOffset.top, left: e.pageX - this.handleOffset.left };
			this.firstValue = this.value();
			
			this.propagate('start', e);
			return false;
						
		},
		stop: function(e) {
			this.propagate('stop', e);
			if (this.firstValue != this.value())
				this.propagate('change', e);
			this.focus(this.currentHandle, true); //This is a especially ugly fix for strange blur events happening on mousemove events
			return false;
		},
		
		oneStep: function(axis) {
			if(!axis) axis = this.options.axis == "vertical" ? 2 : 1;
			return this.options.stepping[axis == 1 ? "x" : "y"] ? this.options.stepping[axis == 1 ? "x" : "y"] : (this.options.realMax[axis == 1 ? "x" : "y"] / this.actualSize[axis == 1 ? "width" : "height"]) * 5;
		},
		
		translateRange: function(value,axis) {
			if (this.rangeElement) {
				if (this.currentHandle[0] == this.handle[0] && value >= this.translateValue(this.value(1),axis))
					value = this.translateValue(this.value(1,axis) - this.oneStep(axis), axis);
				if (this.currentHandle[0] == this.handle[1] && value <= this.translateValue(this.value(0),axis))
					value = this.translateValue(this.value(0,axis) + this.oneStep(axis));
			}
			if (this.options.handles) {
				var handle = this.options.handles[this.handleIndex()];
				if (value < this.translateValue(handle.min,axis)) {
					value = this.translateValue(handle.min,axis);
				} else if (value > this.translateValue(handle.max,axis)) {
					value = this.translateValue(handle.max,axis);
				}
			}
			return value;
		},
		
		handleIndex: function() {
			return this.handle.index(this.currentHandle[0])
		},
		
		translateLimits: function(value,axis) {
			if(!axis) axis = this.options.axis == "vertical" ? 2 : 1;
			if (value >= this.actualSize[axis == 1 ? "width" : "height"] - this.handleSize(null,axis))
				value = this.actualSize[axis == 1 ? "width" : "height"] - this.handleSize(null,axis);
			if (value <= 0)
				value = 0;
			return value;
		},
		
		drag: function(e, handle) {

			var o = this.options;
			var position = { top: e.pageY - this.offset.top - this.clickOffset.top, left: e.pageX - this.offset.left - this.clickOffset.left};
			if(!this.currentHandle) this.focus(this.previousHandle, true); //This is a especially ugly fix for strange blur events happening on mousemove events

			position.left = this.translateLimits(position.left,1);
			position.top = this.translateLimits(position.top,2);
			
			if (o.stepping.x) {
				var value = this.convertValue(position.left,1);
				value = Math.round(value / o.stepping.x) * o.stepping.x;
				position.left = this.translateValue(value, 1);	
			}
			if (o.stepping.y) {
				var value = this.convertValue(position.top,2);
				value = Math.round(value / o.stepping.y) * o.stepping.y;
				position.top = this.translateValue(value, 2);	
			}
			
			position.left = this.translateRange(position.left, 1);
			position.top = this.translateRange(position.top, 2);

			if(o.axis != "vertical") this.currentHandle.css({ left: position.left });
			if(o.axis != "horizontal") this.currentHandle.css({ top: position.top });
			
			if (this.rangeElement)
				this.updateRange();
			this.propagate('slide', e);
			return false;
		},
		
		moveTo: function(value, handle, noPropagation) {
			var o = this.options;
			if (handle == undefined && !this.currentHandle && this.handle.length != 1)
				return false; //If no handle has been passed, no current handle is available and we have multiple handles, return false
			if (handle == undefined && !this.currentHandle)
				handle = 0; //If only one handle is available, use it
			if (handle != undefined)
				this.currentHandle = this.previousHandle = $(this.handle[handle] || handle);



			if(value.x !== undefined && value.y !== undefined) {
				var x = value.x;
				var y = value.y;
			} else {
				var x = value, y = value;
			}

			if(x && x.constructor != Number) {
				var me = /^\-\=/.test(x), pe = /^\+\=/.test(x);
				if (me) {
					x = this.value(null,1) - parseInt(x.replace('-=', ''), 10);
				} else if (pe) {
					x = this.value(null,1) + parseInt(x.replace('+=', ''), 10);
				}
			}
			
			if(y && y.constructor != Number) {
				var me = /^\-\=/.test(y), pe = /^\+\=/.test(y);
				if (me) {
					y = this.value(null,2) - parseInt(y.replace('-=', ''), 10);
				} else if (pe) {
					y = this.value(null,2) + parseInt(y.replace('+=', ''), 10);
				}
			}

			if(o.axis != "vertical" && x) {
				if(o.stepping.x) x = Math.round(x / o.stepping.x) * o.stepping.x;
				x = this.translateValue(x, 1);
				x = this.translateLimits(x, 1);
				x = this.translateRange(x, 1);
				this.currentHandle.css({ left: x });
			}

			if(o.axis != "horizontal" && y) {
				if(o.stepping.y) y = Math.round(y / o.stepping.y) * o.stepping.y;
				y = this.translateValue(y, 2);
				y = this.translateLimits(y, 2);
				y = this.translateRange(y, 2);
				this.currentHandle.css({ top: y });
			}
			
			if (this.rangeElement)
				this.updateRange();
			
			if (!noPropagation) {
				this.propagate('start', null);
				this.propagate('stop', null);
				this.propagate('change', null);
				this.propagate("slide", null);
			}
		}
	});

	$.ui.slider.getter = "value";
	
	$.ui.slider.defaults = {
		handle: ".ui-slider-handle",
		distance: 1
	};

})(jQuery);
/*
 * jQuery UI Tabs
 *
 * Copyright (c) 2007, 2008 Klaus Hartl (stilbuero.de)
 * Dual licensed under the MIT (MIT-LICENSE.txt)
 * and GPL (GPL-LICENSE.txt) licenses.
 *
 * http://docs.jquery.com/UI/Tabs
 *
 * Depends:
 *	ui.core.js
 *
 * Revision: $Id: jquery.ui.js,v 1.1 2008/05/14 15:34:12 m.moossen Exp $
 */
;(function($) {
	
	$.widget("ui.tabs", {
		init: function() {
			var self = this;
	
			this.options.event += '.tabs'; // namespace event
	
			$(this.element).bind('setData.tabs', function(event, key, value) {
				if ((/^selected/).test(key))
					self.select(value);
				else {
					self.options[key] = value;
					self.tabify();
				}
			}).bind('getData.tabs', function(event, key) {
				return self.options[key];
			});
	
			// create tabs
			this.tabify(true);
		},
		length: function() {
			return this.$tabs.length;
		},
		tabId: function(a) {
			return a.title && a.title.replace(/\s/g, '_').replace(/[^A-Za-z0-9\-_:\.]/g, '')
				|| this.options.idPrefix + $.data(a);
		},
		ui: function(tab, panel) {
			return {
				instance: this,
				options: this.options,
				tab: tab,
				panel: panel
			};
		},
		tabify: function(init) {

			this.$lis = $('li:has(a[href])', this.element);
			this.$tabs = this.$lis.map(function() { return $('a', this)[0]; });
			this.$panels = $([]);

			var self = this, o = this.options;

			this.$tabs.each(function(i, a) {
				// inline tab
				if (a.hash && a.hash.replace('#', '')) // Safari 2 reports '#' for an empty hash
					self.$panels = self.$panels.add(a.hash);
				// remote tab
				else if ($(a).attr('href') != '#') { // prevent loading the page itself if href is just "#"
					$.data(a, 'href.tabs', a.href); // required for restore on destroy
					$.data(a, 'load.tabs', a.href); // mutable
					var id = self.tabId(a);
					a.href = '#' + id;
					var $panel = $('#' + id);
					if (!$panel.length) {
						$panel = $(o.panelTemplate).attr('id', id).addClass(o.panelClass)
							.insertAfter( self.$panels[i - 1] || self.element );
						$panel.data('destroy.tabs', true);
					}
					self.$panels = self.$panels.add( $panel );
				}
				// invalid tab href
				else
					o.disabled.push(i + 1);
			});

			if (init) {

				// attach necessary classes for styling if not present
				$(this.element).hasClass(o.navClass) || $(this.element).addClass(o.navClass);
				this.$panels.each(function() {
					var $this = $(this);
					$this.hasClass(o.panelClass) || $this.addClass(o.panelClass);
				});

				// Try to retrieve selected tab:
				// 1. from fragment identifier in url if present
				// 2. from cookie
				// 3. from selected class attribute on <li>
				// 4. otherwise use given "selected" option
				// 5. check if tab is disabled
				this.$tabs.each(function(i, a) {
					if (location.hash) {
						if (a.hash == location.hash) {
							o.selected = i;
							// prevent page scroll to fragment
							//if (($.browser.msie || $.browser.opera) && !o.remote) {
							if ($.browser.msie || $.browser.opera) {
								var $toShow = $(location.hash), toShowId = $toShow.attr('id');
								$toShow.attr('id', '');
								setTimeout(function() {
									$toShow.attr('id', toShowId); // restore id
								}, 500);
							}
							scrollTo(0, 0);
							return false; // break
						}
					} else if (o.cookie) {
						var index = parseInt($.cookie('ui-tabs' + $.data(self.element)),10);
						if (index && self.$tabs[index]) {
							o.selected = index;
							return false; // break
						}
					} else if ( self.$lis.eq(i).hasClass(o.selectedClass) ) {
						o.selected = i;
						return false; // break
					}
				});

				// highlight selected tab
				this.$panels.addClass(o.hideClass);
				this.$lis.removeClass(o.selectedClass);
				if (o.selected !== null) {
					this.$panels.eq(o.selected).show().removeClass(o.hideClass); // use show and remove class to show in any case no matter how it has been hidden before
					this.$lis.eq(o.selected).addClass(o.selectedClass);
					
					// seems to be expected behavior that the show callback is fired
					var onShow = function() {
					    $(self.element).triggerHandler('tabsshow',
					        [self.ui(self.$tabs[o.selected], self.$panels[o.selected])], o.show);
					}; 

                    // load if remote tab
    				if ($.data(this.$tabs[o.selected], 'load.tabs'))
    					this.load(o.selected, onShow);
    				// just trigger show event
    				else
    				    onShow();
    					
				}

				// Take disabling tabs via class attribute from HTML
				// into account and update option properly...
				o.disabled = $.unique(o.disabled.concat(
					$.map(this.$lis.filter('.' + o.disabledClass),
						function(n, i) { return self.$lis.index(n); } )
				)).sort();
				
				// clean up to avoid memory leaks in certain versions of IE 6
				$(window).bind('unload', function() {
					self.$tabs.unbind('.tabs');
					self.$lis = self.$tabs = self.$panels = null;
				});

			}

			// disable tabs
			for (var i = 0, li; li = this.$lis[i]; i++)
				$(li)[$.inArray(i, o.disabled) != -1 && !$(li).hasClass(o.selectedClass) ? 'addClass' : 'removeClass'](o.disabledClass);

			// reset cache if switching from cached to not cached
			if (o.cache === false)
				this.$tabs.removeData('cache.tabs');
			
			// set up animations
			var hideFx, showFx, baseFx = { 'min-width': 0, duration: 1 }, baseDuration = 'normal';
			if (o.fx && o.fx.constructor == Array)
				hideFx = o.fx[0] || baseFx, showFx = o.fx[1] || baseFx;
			else
				hideFx = showFx = o.fx || baseFx;

			// reset some styles to maintain print style sheets etc.
			var resetCSS = { display: '', overflow: '', height: '' };
			if (!$.browser.msie) // not in IE to prevent ClearType font issue
				resetCSS.opacity = '';

			// Hide a tab, animation prevents browser scrolling to fragment,
			// $show is optional.
			function hideTab(clicked, $hide, $show) {
				$hide.animate(hideFx, hideFx.duration || baseDuration, function() { //
					$hide.addClass(o.hideClass).css(resetCSS); // maintain flexible height and accessibility in print etc.
					if ($.browser.msie && hideFx.opacity)
						$hide[0].style.filter = '';
					if ($show)
						showTab(clicked, $show, $hide);
				});
			}

			// Show a tab, animation prevents browser scrolling to fragment,
			// $hide is optional.
			function showTab(clicked, $show, $hide) {
				if (showFx === baseFx)
					$show.css('display', 'block'); // prevent occasionally occuring flicker in Firefox cause by gap between showing and hiding the tab panels
				$show.animate(showFx, showFx.duration || baseDuration, function() {
					$show.removeClass(o.hideClass).css(resetCSS); // maintain flexible height and accessibility in print etc.
					if ($.browser.msie && showFx.opacity)
						$show[0].style.filter = '';

					// callback
					$(self.element).triggerHandler('tabsshow',
					    [self.ui(clicked, $show[0])], o.show);

				});
			}

			// switch a tab
			function switchTab(clicked, $li, $hide, $show) {
				/*if (o.bookmarkable && trueClick) { // add to history only if true click occured, not a triggered click
					$.ajaxHistory.update(clicked.hash);
				}*/
				$li.addClass(o.selectedClass)
					.siblings().removeClass(o.selectedClass);
				hideTab(clicked, $hide, $show);
			}

			// attach tab event handler, unbind to avoid duplicates from former tabifying...
			this.$tabs.unbind('.tabs').bind(o.event, function() {

				//var trueClick = e.clientX; // add to history only if true click occured, not a triggered click
				var $li = $(this).parents('li:eq(0)'),
					$hide = self.$panels.filter(':visible'),
					$show = $(this.hash);

				// If tab is already selected and not unselectable or tab disabled or 
				// or is already loading or click callback returns false stop here.
				// Check if click handler returns false last so that it is not executed
				// for a disabled or loading tab!
				if (($li.hasClass(o.selectedClass) && !o.unselect)
					|| $li.hasClass(o.disabledClass) 
					|| $(this).hasClass(o.loadingClass)
					|| $(self.element).triggerHandler('tabsselect', [self.ui(this, $show[0])], o.select) === false
					) {
					this.blur();
					return false;
				}

				self.options.selected = self.$tabs.index(this);

				// if tab may be closed
				if (o.unselect) {
					if ($li.hasClass(o.selectedClass)) {
						self.options.selected = null;
						$li.removeClass(o.selectedClass);
						self.$panels.stop();
						hideTab(this, $hide);
						this.blur();
						return false;
					} else if (!$hide.length) {
						self.$panels.stop();
						var a = this;
						self.load(self.$tabs.index(this), function() {
							$li.addClass(o.selectedClass).addClass(o.unselectClass);
							showTab(a, $show);
						});
						this.blur();
						return false;
					}
				}

				if (o.cookie)
					$.cookie('ui-tabs' + $.data(self.element), self.options.selected, o.cookie);

				// stop possibly running animations
				self.$panels.stop();

				// show new tab
				if ($show.length) {

					// prevent scrollbar scrolling to 0 and than back in IE7, happens only if bookmarking/history is enabled
					/*if ($.browser.msie && o.bookmarkable) {
						var showId = this.hash.replace('#', '');
						$show.attr('id', '');
						setTimeout(function() {
							$show.attr('id', showId); // restore id
						}, 0);
					}*/

					var a = this;
					self.load(self.$tabs.index(this), $hide.length ? 
						function() {
							switchTab(a, $li, $hide, $show);
						} :
						function() {
							$li.addClass(o.selectedClass);
							showTab(a, $show);
						}
					);

					// Set scrollbar to saved position - need to use timeout with 0 to prevent browser scroll to target of hash
					/*var scrollX = window.pageXOffset || document.documentElement && document.documentElement.scrollLeft || document.body.scrollLeft || 0;
					var scrollY = window.pageYOffset || document.documentElement && document.documentElement.scrollTop || document.body.scrollTop || 0;
					setTimeout(function() {
						scrollTo(scrollX, scrollY);
					}, 0);*/

				} else
					throw 'jQuery UI Tabs: Mismatching fragment identifier.';

				// Prevent IE from keeping other link focussed when using the back button
				// and remove dotted border from clicked link. This is controlled in modern
				// browsers via CSS, also blur removes focus from address bar in Firefox
				// which can become a usability and annoying problem with tabsRotate.
				if ($.browser.msie)
					this.blur();

				//return o.bookmarkable && !!trueClick; // convert trueClick == undefined to Boolean required in IE
				return false;

			});

			// disable click if event is configured to something else
			if (!(/^click/).test(o.event))
				this.$tabs.bind('click.tabs', function() { return false; });

		},
		add: function(url, label, index) {
			if (index == undefined) 
				index = this.$tabs.length; // append by default

			var o = this.options;
			var $li = $(o.tabTemplate.replace(/#\{href\}/, url).replace(/#\{label\}/, label));
			$li.data('destroy.tabs', true);

			var id = url.indexOf('#') == 0 ? url.replace('#', '') : this.tabId( $('a:first-child', $li)[0] );

			// try to find an existing element before creating a new one
			var $panel = $('#' + id);
			if (!$panel.length) {
				$panel = $(o.panelTemplate).attr('id', id)
					.addClass(o.panelClass).addClass(o.hideClass);
				$panel.data('destroy.tabs', true);
			}
			if (index >= this.$lis.length) {
				$li.appendTo(this.element);
				$panel.appendTo(this.element[0].parentNode);
			} else {
				$li.insertBefore(this.$lis[index]);
				$panel.insertBefore(this.$panels[index]);
			}
			
			o.disabled = $.map(o.disabled,
				function(n, i) { return n >= index ? ++n : n });
				
			this.tabify();

			if (this.$tabs.length == 1) {
				$li.addClass(o.selectedClass);
				$panel.removeClass(o.hideClass);
				var href = $.data(this.$tabs[0], 'load.tabs');
				if (href)
					this.load(index, href);
			}

			// callback
			$(this.element).triggerHandler('tabsadd',
				[this.ui(this.$tabs[index], this.$panels[index])], o.add
			);
		},
		remove: function(index) {
			var o = this.options, $li = this.$lis.eq(index).remove(),
				$panel = this.$panels.eq(index).remove();

			// If selected tab was removed focus tab to the right or
			// in case the last tab was removed the tab to the left.
			if ($li.hasClass(o.selectedClass) && this.$tabs.length > 1)
				this.select(index + (index + 1 < this.$tabs.length ? 1 : -1));

			o.disabled = $.map($.grep(o.disabled, function(n, i) { return n != index; }),
				function(n, i) { return n >= index ? --n : n });

			this.tabify();

			// callback
			$(this.element).triggerHandler('tabsremove',
				[this.ui($li.find('a')[0], $panel[0])], o.remove
			);
		},
		enable: function(index) {
			var o = this.options;
			if ($.inArray(index, o.disabled) == -1)
				return;
				
			var $li = this.$lis.eq(index).removeClass(o.disabledClass);
			if ($.browser.safari) { // fix disappearing tab (that used opacity indicating disabling) after enabling in Safari 2...
				$li.css('display', 'inline-block');
				setTimeout(function() {
					$li.css('display', 'block');
				}, 0);
			}

			o.disabled = $.grep(o.disabled, function(n, i) { return n != index; });

			// callback
			$(this.element).triggerHandler('tabsenable',
				[this.ui(this.$tabs[index], this.$panels[index])], o.enable
			);

		},
		disable: function(index) {
			var self = this, o = this.options;
			if (index != o.selected) { // cannot disable already selected tab
				this.$lis.eq(index).addClass(o.disabledClass);

				o.disabled.push(index);
				o.disabled.sort();

				// callback
				$(this.element).triggerHandler('tabsdisable',
					[this.ui(this.$tabs[index], this.$panels[index])], o.disable
				);
			}
		},
		select: function(index) {
			if (typeof index == 'string')
				index = this.$tabs.index( this.$tabs.filter('[href$=' + index + ']')[0] );
			this.$tabs.eq(index).trigger(this.options.event);
		},
		load: function(index, callback) { // callback is for internal usage only
			
			var self = this, o = this.options, $a = this.$tabs.eq(index), a = $a[0],
					bypassCache = callback == undefined || callback === false, url = $a.data('load.tabs');

			callback = callback || function() {};
			
			// no remote or from cache - just finish with callback
			if (!url || ($.data(a, 'cache.tabs') && !bypassCache)) {
				callback();
				return;
			}

			// load remote from here on
			if (o.spinner) {
				var $span = $('span', a);
				$span.data('label.tabs', $span.html()).html('<em>' + o.spinner + '</em>');
			}
			var finish = function() {
				self.$tabs.filter('.' + o.loadingClass).each(function() {
					$(this).removeClass(o.loadingClass);
					if (o.spinner) {
						var $span = $('span', this);
						$span.html($span.data('label.tabs')).removeData('label.tabs');
					}
				});
				self.xhr = null;
			};
			var ajaxOptions = $.extend({}, o.ajaxOptions, {
				url: url,
				success: function(r, s) {
					$(a.hash).html(r);
					finish();
					
					if (o.cache)
						$.data(a, 'cache.tabs', true); // if loaded once do not load them again

					// callbacks
					$(self.element).triggerHandler('tabsload',
						[self.ui(self.$tabs[index], self.$panels[index])], o.load
					);
					o.ajaxOptions.success && o.ajaxOptions.success(r, s);
					
					// This callback is required because the switch has to take
					// place after loading has completed. Call last in order to 
					// fire load before show callback...
					callback();
				}
			});
			if (this.xhr) {
				// terminate pending requests from other tabs and restore tab label
				this.xhr.abort();
				finish();
			}
			$a.addClass(o.loadingClass);
			setTimeout(function() { // timeout is again required in IE, "wait" for id being restored
				self.xhr = $.ajax(ajaxOptions);
			}, 0);

		},
		url: function(index, url) {
			this.$tabs.eq(index).removeData('cache.tabs').data('load.tabs', url);
		},
		destroy: function() {
			var o = this.options;
			$(this.element).unbind('.tabs')
				.removeClass(o.navClass).removeData('tabs');
			this.$tabs.each(function() {
				var href = $.data(this, 'href.tabs');
				if (href)
					this.href = href;
				var $this = $(this).unbind('.tabs');
				$.each(['href', 'load', 'cache'], function(i, prefix) {
					$this.removeData(prefix + '.tabs');
				});
			});
			this.$lis.add(this.$panels).each(function() {
				if ($.data(this, 'destroy.tabs'))
					$(this).remove();
				else
					$(this).removeClass([o.selectedClass, o.unselectClass,
						o.disabledClass, o.panelClass, o.hideClass].join(' '));
			});
		}
	});
	
	$.ui.tabs.defaults = {
		// basic setup
		selected: 0,
		unselect: false,
		event: 'click',
		disabled: [],
		cookie: null, // e.g. { expires: 7, path: '/', domain: 'jquery.com', secure: true }
		// TODO history: false,

		// Ajax
		spinner: 'Loading…',
		cache: false,
		idPrefix: 'ui-tabs-',
		ajaxOptions: {},

		// animations
		fx: null, // e.g. { height: 'toggle', opacity: 'toggle', duration: 200 }

		// templates
		tabTemplate: '<li><a href="#{href}"><span>#{label}</span></a></li>',
		panelTemplate: '<div></div>',

		// CSS classes
		navClass: 'ui-tabs-nav',
		selectedClass: 'ui-tabs-selected',
		unselectClass: 'ui-tabs-unselect',
		disabledClass: 'ui-tabs-disabled',
		panelClass: 'ui-tabs-panel',
		hideClass: 'ui-tabs-hide',
		loadingClass: 'ui-tabs-loading'
	};
	
	$.ui.tabs.getter = "length";

/*
 * Tabs Extensions
 */

	/*
	 * Rotate
	 */
	$.extend($.ui.tabs.prototype, {
		rotation: null,
		rotate: function(ms, continuing) {
			
			continuing = continuing || false;
			
			var self = this, t = this.options.selected;
			
			function start() {
				self.rotation = setInterval(function() {
					t = ++t < self.$tabs.length ? t : 0;
					self.select(t);
				}, ms);	
			}
			
			function stop(e) {
				if (!e || e.clientX) { // only in case of a true click
					clearInterval(self.rotation);
				}
			}
			
			// start interval
			if (ms) {
				start();
				if (!continuing)
					this.$tabs.bind(this.options.event, stop);
				else
					this.$tabs.bind(this.options.event, function() {
						stop();
						t = self.options.selected;
						start();
					});
			}
			// stop interval
			else {
				stop();
				this.$tabs.unbind(this.options.event, stop);
			}
		}
	});

})(jQuery);
/* jQuery UI Date Picker v3.4.3 (previously jQuery Calendar)
   Written by Marc Grabanski (m@marcgrabanski.com) and Keith Wood (kbwood@virginbroadband.com.au).

   Copyright (c) 2007 Marc Grabanski (http://marcgrabanski.com/code/ui-datepicker)
   Dual licensed under the MIT (MIT-LICENSE.txt)
   and GPL (GPL-LICENSE.txt) licenses.
   Date: 09-03-2007  */
   
;(function($) { // hide the namespace

/* Date picker manager.
   Use the singleton instance of this class, $.datepicker, to interact with the date picker.
   Settings for (groups of) date pickers are maintained in an instance object
   (DatepickerInstance), allowing multiple different settings on the same page. */

function Datepicker() {
	this.debug = false; // Change this to true to start debugging
	this._nextId = 0; // Next ID for a date picker instance
	this._inst = []; // List of instances indexed by ID
	this._curInst = null; // The current instance in use
	this._disabledInputs = []; // List of date picker inputs that have been disabled
	this._datepickerShowing = false; // True if the popup picker is showing , false if not
	this._inDialog = false; // True if showing within a "dialog", false if not
	this.regional = []; // Available regional settings, indexed by language code
	this.regional[''] = { // Default regional settings
		clearText: 'Clear', // Display text for clear link
		clearStatus: 'Erase the current date', // Status text for clear link
		closeText: 'Close', // Display text for close link
		closeStatus: 'Close without change', // Status text for close link
		prevText: '&#x3c;Prev', // Display text for previous month link
		prevStatus: 'Show the previous month', // Status text for previous month link
		nextText: 'Next&#x3e;', // Display text for next month link
		nextStatus: 'Show the next month', // Status text for next month link
		currentText: 'Today', // Display text for current month link
		currentStatus: 'Show the current month', // Status text for current month link
		monthNames: ['January','February','March','April','May','June',
			'July','August','September','October','November','December'], // Names of months for drop-down and formatting
		monthNamesShort: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'], // For formatting
		monthStatus: 'Show a different month', // Status text for selecting a month
		yearStatus: 'Show a different year', // Status text for selecting a year
		weekHeader: 'Wk', // Header for the week of the year column
		weekStatus: 'Week of the year', // Status text for the week of the year column
		dayNames: ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'], // For formatting
		dayNamesShort: ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'], // For formatting
		dayNamesMin: ['Su','Mo','Tu','We','Th','Fr','Sa'], // Column headings for days starting at Sunday
		dayStatus: 'Set DD as first week day', // Status text for the day of the week selection
		dateStatus: 'Select DD, M d', // Status text for the date selection
		dateFormat: 'mm/dd/yy', // See format options on parseDate
		firstDay: 0, // The first day of the week, Sun = 0, Mon = 1, ...
		initStatus: 'Select a date', // Initial Status text on opening
		isRTL: false // True if right-to-left language, false if left-to-right
	};
	this._defaults = { // Global defaults for all the date picker instances
		showOn: 'focus', // 'focus' for popup on focus,
			// 'button' for trigger button, or 'both' for either
		showAnim: 'show', // Name of jQuery animation for popup
		defaultDate: null, // Used when field is blank: actual date,
			// +/-number for offset from today, null for today
		appendText: '', // Display text following the input box, e.g. showing the format
		buttonText: '...', // Text for trigger button
		buttonImage: '', // URL for trigger button image
		buttonImageOnly: false, // True if the image appears alone, false if it appears on a button
		closeAtTop: true, // True to have the clear/close at the top,
			// false to have them at the bottom
		mandatory: false, // True to hide the Clear link, false to include it
		hideIfNoPrevNext: false, // True to hide next/previous month links
			// if not applicable, false to just disable them
		changeMonth: true, // True if month can be selected directly, false if only prev/next
		changeYear: true, // True if year can be selected directly, false if only prev/next
		yearRange: '-10:+10', // Range of years to display in drop-down,
			// either relative to current year (-nn:+nn) or absolute (nnnn:nnnn)
		changeFirstDay: true, // True to click on day name to change, false to remain as set
		showOtherMonths: false, // True to show dates in other months, false to leave blank
		showWeeks: false, // True to show week of the year, false to omit
		calculateWeek: this.iso8601Week, // How to calculate the week of the year,
			// takes a Date and returns the number of the week for it
		shortYearCutoff: '+10', // Short year values < this are in the current century,
			// > this are in the previous century, 
			// string value starting with '+' for current year + value
		showStatus: false, // True to show status bar at bottom, false to not show it
		statusForDate: this.dateStatus, // Function to provide status text for a date -
			// takes date and instance as parameters, returns display text
		minDate: null, // The earliest selectable date, or null for no limit
		maxDate: null, // The latest selectable date, or null for no limit
		speed: 'normal', // Speed of display/closure
		beforeShowDay: null, // Function that takes a date and returns an array with
			// [0] = true if selectable, false if not,
			// [1] = custom CSS class name(s) or '', e.g. $.datepicker.noWeekends
		beforeShow: null, // Function that takes an input field and
			// returns a set of custom settings for the date picker
		onSelect: null, // Define a callback function when a date is selected
		onClose: null, // Define a callback function when the datepicker is closed
		numberOfMonths: 1, // Number of months to show at a time
		stepMonths: 1, // Number of months to step back/forward
		rangeSelect: false, // Allows for selecting a date range on one date picker
		rangeSeparator: ' - ' // Text between two dates in a range
	};
	$.extend(this._defaults, this.regional['']);
	this._datepickerDiv = $('<div id="datepicker_div"></div>');
}

$.extend(Datepicker.prototype, {
	/* Class name added to elements to indicate already configured with a date picker. */
	markerClassName: 'hasDatepicker',

	/* Debug logging (if enabled). */
	log: function () {
		if (this.debug)
			console.log.apply('', arguments);
	},
	
	/* Register a new date picker instance - with custom settings. */
	_register: function(inst) {
		var id = this._nextId++;
		this._inst[id] = inst;
		return id;
	},

	/* Retrieve a particular date picker instance based on its ID. */
	_getInst: function(id) {
		return this._inst[id] || id;
	},

	/* Override the default settings for all instances of the date picker. 
	   @param  settings  object - the new settings to use as defaults (anonymous object)
	   @return the manager object */
	setDefaults: function(settings) {
		extendRemove(this._defaults, settings || {});
		return this;
	},

	/* Attach the date picker to a jQuery selection.
	   @param  target    element - the target input field or division or span
	   @param  settings  object - the new settings to use for this date picker instance (anonymous) */
	_attachDatepicker: function(target, settings) {
		// check for settings on the control itself - in namespace 'date:'
		var inlineSettings = null;
		for (attrName in this._defaults) {
			var attrValue = target.getAttribute('date:' + attrName);
			if (attrValue) {
				inlineSettings = inlineSettings || {};
				try {
					inlineSettings[attrName] = eval(attrValue);
				} catch (err) {
					inlineSettings[attrName] = attrValue;
				}
			}
		}
		var nodeName = target.nodeName.toLowerCase();
		var instSettings = (inlineSettings ? 
			$.extend(settings || {}, inlineSettings || {}) : settings);
		if (nodeName == 'input') {
			var inst = (inst && !inlineSettings ? inst :
				new DatepickerInstance(instSettings, false));
			this._connectDatepicker(target, inst);
		} else if (nodeName == 'div' || nodeName == 'span') {
			var inst = new DatepickerInstance(instSettings, true);
			this._inlineDatepicker(target, inst);
		}
	},

	/* Detach a datepicker from its control.
	   @param  target    element - the target input field or division or span */
	_destroyDatepicker: function(target) {
		var nodeName = target.nodeName.toLowerCase();
		var calId = target._calId;
		target._calId = null;
		var $target = $(target);
		if (nodeName == 'input') {
			$target.siblings('.datepicker_append').replaceWith('').end()
				.siblings('.datepicker_trigger').replaceWith('').end()
				.removeClass(this.markerClassName)
				.unbind('focus', this._showDatepicker)
				.unbind('keydown', this._doKeyDown)
				.unbind('keypress', this._doKeyPress);
			var wrapper = $target.parents('.datepicker_wrap');
			if (wrapper)
				wrapper.replaceWith(wrapper.html());
		} else if (nodeName == 'div' || nodeName == 'span')
			$target.removeClass(this.markerClassName).empty();
		if ($('input[_calId=' + calId + ']').length == 0)
			// clean up if last for this ID
			this._inst[calId] = null;
	},

	/* Enable the date picker to a jQuery selection.
	   @param  target    element - the target input field or division or span */
	_enableDatepicker: function(target) {
		target.disabled = false;
		$(target).siblings('button.datepicker_trigger').each(function() { this.disabled = false; }).end()
			.siblings('img.datepicker_trigger').css({opacity: '1.0', cursor: ''});
		this._disabledInputs = $.map(this._disabledInputs,
			function(value) { return (value == target ? null : value); }); // delete entry
	},

	/* Disable the date picker to a jQuery selection.
	   @param  target    element - the target input field or division or span */
	_disableDatepicker: function(target) {
		target.disabled = true;
		$(target).siblings('button.datepicker_trigger').each(function() { this.disabled = true; }).end()
			.siblings('img.datepicker_trigger').css({opacity: '0.5', cursor: 'default'});
		this._disabledInputs = $.map($.datepicker._disabledInputs,
			function(value) { return (value == target ? null : value); }); // delete entry
		this._disabledInputs[$.datepicker._disabledInputs.length] = target;
	},

	/* Is the first field in a jQuery collection disabled as a datepicker?
	   @param  target    element - the target input field or division or span
	   @return boolean - true if disabled, false if enabled */
	_isDisabledDatepicker: function(target) {
		if (!target)
			return false;
		for (var i = 0; i < this._disabledInputs.length; i++) {
			if (this._disabledInputs[i] == target)
				return true;
		}
		return false;
	},

	/* Update the settings for a date picker attached to an input field or division.
	   @param  target  element - the target input field or division or span
	   @param  name    string - the name of the setting to change or
	                   object - the new settings to update
	   @param  value   any - the new value for the setting (omit if above is an object) */
	_changeDatepicker: function(target, name, value) {
		var settings = name || {};
		if (typeof name == 'string') {
			settings = {};
			settings[name] = value;
		}
		if (inst = this._getInst(target._calId)) {
			extendRemove(inst._settings, settings);
			this._updateDatepicker(inst);
		}
	},

	/* Set the dates for a jQuery selection.
	   @param  target   element - the target input field or division or span
	   @param  date     Date - the new date
	   @param  endDate  Date - the new end date for a range (optional) */
	_setDateDatepicker: function(target, date, endDate) {
		if (inst = this._getInst(target._calId)) {
			inst._setDate(date, endDate);
			this._updateDatepicker(inst);
		}
	},

	/* Get the date(s) for the first entry in a jQuery selection.
	   @param  target  element - the target input field or division or span
	   @return Date - the current date or
	           Date[2] - the current dates for a range */
	_getDateDatepicker: function(target) {
		var inst = this._getInst(target._calId);
		return (inst ? inst._getDate() : null);
	},

	/* Handle keystrokes. */
	_doKeyDown: function(e) {
		var inst = $.datepicker._getInst(this._calId);
		if ($.datepicker._datepickerShowing)
			switch (e.keyCode) {
				case 9:  $.datepicker._hideDatepicker(null, '');
						break; // hide on tab out
				case 13: $.datepicker._selectDay(inst, inst._selectedMonth, inst._selectedYear,
							$('td.datepicker_daysCellOver', inst._datepickerDiv)[0]);
						return false; // don't submit the form
						break; // select the value on enter
				case 27: $.datepicker._hideDatepicker(null, inst._get('speed'));
						break; // hide on escape
				case 33: $.datepicker._adjustDate(inst,
							(e.ctrlKey ? -1 : -inst._get('stepMonths')), (e.ctrlKey ? 'Y' : 'M'));
						break; // previous month/year on page up/+ ctrl
				case 34: $.datepicker._adjustDate(inst,
							(e.ctrlKey ? +1 : +inst._get('stepMonths')), (e.ctrlKey ? 'Y' : 'M'));
						break; // next month/year on page down/+ ctrl
				case 35: if (e.ctrlKey) $.datepicker._clearDate(inst);
						break; // clear on ctrl+end
				case 36: if (e.ctrlKey) $.datepicker._gotoToday(inst);
						break; // current on ctrl+home
				case 37: if (e.ctrlKey) $.datepicker._adjustDate(inst, -1, 'D');
						break; // -1 day on ctrl+left
				case 38: if (e.ctrlKey) $.datepicker._adjustDate(inst, -7, 'D');
						break; // -1 week on ctrl+up
				case 39: if (e.ctrlKey) $.datepicker._adjustDate(inst, +1, 'D');
						break; // +1 day on ctrl+right
				case 40: if (e.ctrlKey) $.datepicker._adjustDate(inst, +7, 'D');
						break; // +1 week on ctrl+down
			}
		else if (e.keyCode == 36 && e.ctrlKey) // display the date picker on ctrl+home
			$.datepicker._showDatepicker(this);
	},

	/* Filter entered characters - based on date format. */
	_doKeyPress: function(e) {
		var inst = $.datepicker._getInst(this._calId);
		var chars = $.datepicker._possibleChars(inst._get('dateFormat'));
		var chr = String.fromCharCode(e.charCode == undefined ? e.keyCode : e.charCode);
		return e.ctrlKey || (chr < ' ' || !chars || chars.indexOf(chr) > -1);
	},

	/* Attach the date picker to an input field. */
	_connectDatepicker: function(target, inst) {
		var input = $(target);
		if (input.is('.' + this.markerClassName))
			return;
		var appendText = inst._get('appendText');
		var isRTL = inst._get('isRTL');
		if (appendText) {
			if (isRTL)
				input.before('<span class="datepicker_append">' + appendText);
			else
				input.after('<span class="datepicker_append">' + appendText);
		}
		var showOn = inst._get('showOn');
		if (showOn == 'focus' || showOn == 'both') // pop-up date picker when in the marked field
			input.focus(this._showDatepicker);
		if (showOn == 'button' || showOn == 'both') { // pop-up date picker when button clicked
			input.wrap('<span class="datepicker_wrap">');
			var buttonText = inst._get('buttonText');
			var buttonImage = inst._get('buttonImage');
			var trigger = $(inst._get('buttonImageOnly') ? 
				$('<img>').addClass('datepicker_trigger').attr({ src: buttonImage, alt: buttonText, title: buttonText }) :
				$('<button>').addClass('datepicker_trigger').attr({ type: 'button' }).html(buttonImage != '' ? 
						$('<img>').attr({ src:buttonImage, alt:buttonText, title:buttonText }) : buttonText));
			if (isRTL)
				input.before(trigger);
			else
				input.after(trigger);
			trigger.click(function() {
				if ($.datepicker._datepickerShowing && $.datepicker._lastInput == target)
					$.datepicker._hideDatepicker();
				else
					$.datepicker._showDatepicker(target);
			});
        }
		input.addClass(this.markerClassName).keydown(this._doKeyDown).keypress(this._doKeyPress)
			.bind("setData.datepicker", function(event, key, value) {
				inst._settings[key] = value;
			}).bind("getData.datepicker", function(event, key) {
				return inst._get(key);
			});
		input[0]._calId = inst._id;
	},

	/* Attach an inline date picker to a div. */
	_inlineDatepicker: function(target, inst) {
		var input = $(target);
		if (input.is('.' + this.markerClassName))
			return;
		input.addClass(this.markerClassName).append(inst._datepickerDiv)
			.bind("setData.datepicker", function(event, key, value){
				inst._settings[key] = value;
			}).bind("getData.datepicker", function(event, key){
				return inst._get(key);
			});
		input[0]._calId = inst._id;
		this._updateDatepicker(inst);
	},

	/* Tidy up after displaying the date picker. */
	_inlineShow: function(inst) {
		var numMonths = inst._getNumberOfMonths(); // fix width for dynamic number of date pickers
		inst._datepickerDiv.width(numMonths[1] * $('.datepicker', inst._datepickerDiv[0]).width());
	}, 

	/* Pop-up the date picker in a "dialog" box.
	   @param  input     element - ignored
	   @param  dateText  string - the initial date to display (in the current format)
	   @param  onSelect  function - the function(dateText) to call when a date is selected
	   @param  settings  object - update the dialog date picker instance's settings (anonymous object)
	   @param  pos       int[2] - coordinates for the dialog's position within the screen or
	                     event - with x/y coordinates or
	                     leave empty for default (screen centre)
	   @return the manager object */
	_dialogDatepicker: function(input, dateText, onSelect, settings, pos) {
		var inst = this._dialogInst; // internal instance
		if (!inst) {
			inst = this._dialogInst = new DatepickerInstance({}, false);
			this._dialogInput = $('<input type="text" size="1" style="position: absolute; top: -100px;"/>');
			this._dialogInput.keydown(this._doKeyDown);
			$('body').append(this._dialogInput);
			this._dialogInput[0]._calId = inst._id;
		}
		extendRemove(inst._settings, settings || {});
		this._dialogInput.val(dateText);

		this._pos = (pos ? (pos.length ? pos : [pos.pageX, pos.pageY]) : null);
		if (!this._pos) {
			var browserWidth = window.innerWidth || document.documentElement.clientWidth ||	document.body.clientWidth;
			var browserHeight = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
			var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;
			var scrollY = document.documentElement.scrollTop || document.body.scrollTop;
			this._pos = // should use actual width/height below
				[(browserWidth / 2) - 100 + scrollX, (browserHeight / 2) - 150 + scrollY];
		}

		// move input on screen for focus, but hidden behind dialog
		this._dialogInput.css('left', this._pos[0] + 'px').css('top', this._pos[1] + 'px');
		inst._settings.onSelect = onSelect;
		this._inDialog = true;
		this._datepickerDiv.addClass('datepicker_dialog');
		this._showDatepicker(this._dialogInput[0]);
		if ($.blockUI)
			$.blockUI(this._datepickerDiv);
		return this;
	},

	/* Pop-up the date picker for a given input field.
	   @param  input  element - the input field attached to the date picker or
	                  event - if triggered by focus */
	_showDatepicker: function(input) {
		input = input.target || input;
		if (input.nodeName.toLowerCase() != 'input') // find from button/image trigger
			input = $('input', input.parentNode)[0];
		if ($.datepicker._isDisabledDatepicker(input) || $.datepicker._lastInput == input) // already here
			return;
		var inst = $.datepicker._getInst(input._calId);
		var beforeShow = inst._get('beforeShow');
		extendRemove(inst._settings, (beforeShow ? beforeShow.apply(input, [input, inst]) : {}));
		$.datepicker._hideDatepicker(null, '');
		$.datepicker._lastInput = input;
		inst._setDateFromField(input);
		if ($.datepicker._inDialog) // hide cursor
			input.value = '';
		if (!$.datepicker._pos) { // position below input
			$.datepicker._pos = $.datepicker._findPos(input);
			$.datepicker._pos[1] += input.offsetHeight; // add the height
		}
		var isFixed = false;
		$(input).parents().each(function() {
			isFixed |= $(this).css('position') == 'fixed';
		});
		if (isFixed && $.browser.opera) { // correction for Opera when fixed and scrolled
			$.datepicker._pos[0] -= document.documentElement.scrollLeft;
			$.datepicker._pos[1] -= document.documentElement.scrollTop;
		}
		inst._datepickerDiv.css('position', ($.datepicker._inDialog && $.blockUI ?
			'static' : (isFixed ? 'fixed' : 'absolute')))
			.css({ left: $.datepicker._pos[0] + 'px', top: $.datepicker._pos[1] + 'px' });
		$.datepicker._pos = null;
		inst._rangeStart = null;
		$.datepicker._updateDatepicker(inst);
		if (!inst._inline) {
			var speed = inst._get('speed');
			var postProcess = function() {
				$.datepicker._datepickerShowing = true;
				$.datepicker._afterShow(inst);
			};
			var showAnim = inst._get('showAnim') || 'show';
			inst._datepickerDiv[showAnim](speed, postProcess);
			if (speed == '')
				postProcess();
			if (inst._input[0].type != 'hidden')
				inst._input[0].focus();
			$.datepicker._curInst = inst;
		}
	},

	/* Generate the date picker content. */
	_updateDatepicker: function(inst) {
		inst._datepickerDiv.empty().append(inst._generateDatepicker());
		var numMonths = inst._getNumberOfMonths();
		if (numMonths[0] != 1 || numMonths[1] != 1)
			inst._datepickerDiv.addClass('datepicker_multi');
		else
			inst._datepickerDiv.removeClass('datepicker_multi');

		if (inst._get('isRTL'))
			inst._datepickerDiv.addClass('datepicker_rtl');
		else
			inst._datepickerDiv.removeClass('datepicker_rtl');

		if (inst._input && inst._input[0].type != 'hidden')
			$(inst._input[0]).focus();
	},

	/* Tidy up after displaying the date picker. */
	_afterShow: function(inst) {
		var numMonths = inst._getNumberOfMonths(); // fix width for dynamic number of date pickers
		inst._datepickerDiv.width(numMonths[1] * $('.datepicker', inst._datepickerDiv[0])[0].offsetWidth);
		if ($.browser.msie && parseInt($.browser.version) < 7) { // fix IE < 7 select problems
			$('iframe.datepicker_cover').css({width: inst._datepickerDiv.width() + 4,
				height: inst._datepickerDiv.height() + 4});
		}
		// re-position on screen if necessary
		var isFixed = inst._datepickerDiv.css('position') == 'fixed';
		var pos = inst._input ? $.datepicker._findPos(inst._input[0]) : null;
		var browserWidth = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
		var browserHeight = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
		var scrollX = (isFixed ? 0 : document.documentElement.scrollLeft || document.body.scrollLeft);
		var scrollY = (isFixed ? 0 : document.documentElement.scrollTop || document.body.scrollTop);
		// reposition date picker horizontally if outside the browser window
		if ((inst._datepickerDiv.offset().left + inst._datepickerDiv.width() -
				(isFixed && $.browser.msie ? document.documentElement.scrollLeft : 0)) >
				(browserWidth + scrollX)) {
			inst._datepickerDiv.css('left', Math.max(scrollX,
				pos[0] + (inst._input ? $(inst._input[0]).width() : null) - inst._datepickerDiv.width() -
				(isFixed && $.browser.opera ? document.documentElement.scrollLeft : 0)) + 'px');
		}
		// reposition date picker vertically if outside the browser window
		if ((inst._datepickerDiv.offset().top + inst._datepickerDiv.height() -
				(isFixed && $.browser.msie ? document.documentElement.scrollTop : 0)) >
				(browserHeight + scrollY) ) {
			inst._datepickerDiv.css('top', Math.max(scrollY,
				pos[1] - (this._inDialog ? 0 : inst._datepickerDiv.height()) -
				(isFixed && $.browser.opera ? document.documentElement.scrollTop : 0)) + 'px');
		}
	},
	
	/* Find an object's position on the screen. */
	_findPos: function(obj) {
        while (obj && (obj.type == 'hidden' || obj.nodeType != 1)) {
            obj = obj.nextSibling;
        }
        var position = $(obj).offset();
	    return [position.left, position.top];
	},

	/* Hide the date picker from view.
	   @param  input  element - the input field attached to the date picker
	   @param  speed  string - the speed at which to close the date picker */
	_hideDatepicker: function(input, speed) {
		var inst = this._curInst;
		if (!inst)
			return;
		var rangeSelect = inst._get('rangeSelect');
		if (rangeSelect && this._stayOpen) {
			this._selectDate(inst, inst._formatDate(
				inst._currentDay, inst._currentMonth, inst._currentYear));
		}
		this._stayOpen = false;
		if (this._datepickerShowing) {
			speed = (speed != null ? speed : inst._get('speed'));
			var showAnim = inst._get('showAnim');
			inst._datepickerDiv[(showAnim == 'slideDown' ? 'slideUp' :
				(showAnim == 'fadeIn' ? 'fadeOut' : 'hide'))](speed, function() {
				$.datepicker._tidyDialog(inst);
			});
			if (speed == '')
				this._tidyDialog(inst);
			var onClose = inst._get('onClose');
			if (onClose) {
				onClose.apply((inst._input ? inst._input[0] : null),
					[inst._getDate(), inst]);  // trigger custom callback
			}
			this._datepickerShowing = false;
			this._lastInput = null;
			inst._settings.prompt = null;
			if (this._inDialog) {
				this._dialogInput.css({ position: 'absolute', left: '0', top: '-100px' });
				if ($.blockUI) {
					$.unblockUI();
					$('body').append(this._datepickerDiv);
				}
			}
			this._inDialog = false;
		}
		this._curInst = null;
	},

	/* Tidy up after a dialog display. */
	_tidyDialog: function(inst) {
		inst._datepickerDiv.removeClass('datepicker_dialog').unbind('.datepicker');
		$('.datepicker_prompt', inst._datepickerDiv).remove();
	},

	/* Close date picker if clicked elsewhere. */
	_checkExternalClick: function(event) {
		if (!$.datepicker._curInst)
			return;
		var $target = $(event.target);
		if (($target.parents("#datepicker_div").length == 0) &&
				($target.attr('class') != 'datepicker_trigger') &&
				$.datepicker._datepickerShowing && !($.datepicker._inDialog && $.blockUI)) {
			$.datepicker._hideDatepicker(null, '');
		}
	},

	/* Adjust one of the date sub-fields. */
	_adjustDate: function(id, offset, period) {
		var inst = this._getInst(id);
		inst._adjustDate(offset, period);
		this._updateDatepicker(inst);
	},

	/* Action for current link. */
	_gotoToday: function(id) {
		var date = new Date();
		var inst = this._getInst(id);
		inst._selectedDay = date.getDate();
		inst._drawMonth = inst._selectedMonth = date.getMonth();
		inst._drawYear = inst._selectedYear = date.getFullYear();
		this._adjustDate(inst);
	},

	/* Action for selecting a new month/year. */
	_selectMonthYear: function(id, select, period) {
		var inst = this._getInst(id);
		inst._selectingMonthYear = false;
		inst[period == 'M' ? '_drawMonth' : '_drawYear'] =
			select.options[select.selectedIndex].value - 0;
		this._adjustDate(inst);
	},

	/* Restore input focus after not changing month/year. */
	_clickMonthYear: function(id) {
		var inst = this._getInst(id);
		if (inst._input && inst._selectingMonthYear && !$.browser.msie)
			inst._input[0].focus();
		inst._selectingMonthYear = !inst._selectingMonthYear;
	},

	/* Action for changing the first week day. */
	_changeFirstDay: function(id, day) {
		var inst = this._getInst(id);
		inst._settings.firstDay = day;
		this._updateDatepicker(inst);
	},

	/* Action for selecting a day. */
	_selectDay: function(id, month, year, td) {
		if ($(td).is('.datepicker_unselectable'))
			return;
		var inst = this._getInst(id);
		var rangeSelect = inst._get('rangeSelect');
		if (rangeSelect) {
			if (!this._stayOpen) {
				$('.datepicker td').removeClass('datepicker_currentDay');
				$(td).addClass('datepicker_currentDay');
			} 
			this._stayOpen = !this._stayOpen;
		}
		inst._selectedDay = inst._currentDay = $('a', td).html();
		inst._selectedMonth = inst._currentMonth = month;
		inst._selectedYear = inst._currentYear = year;
		this._selectDate(id, inst._formatDate(
			inst._currentDay, inst._currentMonth, inst._currentYear));
		if (this._stayOpen) {
			inst._endDay = inst._endMonth = inst._endYear = null;
			inst._rangeStart = new Date(inst._currentYear, inst._currentMonth, inst._currentDay);
			this._updateDatepicker(inst);
		}
		else if (rangeSelect) {
			inst._endDay = inst._currentDay;
			inst._endMonth = inst._currentMonth;
			inst._endYear = inst._currentYear;
			inst._selectedDay = inst._currentDay = inst._rangeStart.getDate();
			inst._selectedMonth = inst._currentMonth = inst._rangeStart.getMonth();
			inst._selectedYear = inst._currentYear = inst._rangeStart.getFullYear();
			inst._rangeStart = null;
			if (inst._inline)
				this._updateDatepicker(inst);
		}
	},

	/* Erase the input field and hide the date picker. */
	_clearDate: function(id) {
		var inst = this._getInst(id);
		if (inst._get('mandatory'))
			return;
		this._stayOpen = false;
		inst._endDay = inst._endMonth = inst._endYear = inst._rangeStart = null;
		this._selectDate(inst, '');
	},

	/* Update the input field with the selected date. */
	_selectDate: function(id, dateStr) {
		var inst = this._getInst(id);
		dateStr = (dateStr != null ? dateStr : inst._formatDate());
		if (inst._rangeStart)
			dateStr = inst._formatDate(inst._rangeStart) + inst._get('rangeSeparator') + dateStr;
		if (inst._input)
			inst._input.val(dateStr);
		var onSelect = inst._get('onSelect');
		if (onSelect)
			onSelect.apply((inst._input ? inst._input[0] : null), [dateStr, inst]);  // trigger custom callback
		else if (inst._input)
			inst._input.trigger('change'); // fire the change event
		if (inst._inline)
			this._updateDatepicker(inst);
		else if (!this._stayOpen) {
			this._hideDatepicker(null, inst._get('speed'));
			this._lastInput = inst._input[0];
			if (typeof(inst._input[0]) != 'object')
				inst._input[0].focus(); // restore focus
			this._lastInput = null;
		}
	},

	/* Set as beforeShowDay function to prevent selection of weekends.
	   @param  date  Date - the date to customise
	   @return [boolean, string] - is this date selectable?, what is its CSS class? */
	noWeekends: function(date) {
		var day = date.getDay();
		return [(day > 0 && day < 6), ''];
	},
	
	/* Set as calculateWeek to determine the week of the year based on the ISO 8601 definition.
	   @param  date  Date - the date to get the week for
	   @return  number - the number of the week within the year that contains this date */
	iso8601Week: function(date) {
		var checkDate = new Date(date.getFullYear(), date.getMonth(), date.getDate(), (date.getTimezoneOffset() / -60));
		var firstMon = new Date(checkDate.getFullYear(), 1 - 1, 4); // First week always contains 4 Jan
		var firstDay = firstMon.getDay() || 7; // Day of week: Mon = 1, ..., Sun = 7
		firstMon.setDate(firstMon.getDate() + 1 - firstDay); // Preceding Monday
		if (firstDay < 4 && checkDate < firstMon) { // Adjust first three days in year if necessary
			checkDate.setDate(checkDate.getDate() - 3); // Generate for previous year
			return $.datepicker.iso8601Week(checkDate);
		} else if (checkDate > new Date(checkDate.getFullYear(), 12 - 1, 28)) { // Check last three days in year
			firstDay = new Date(checkDate.getFullYear() + 1, 1 - 1, 4).getDay() || 7;
			if (firstDay > 4 && (checkDate.getDay() || 7) < firstDay - 3) { // Adjust if necessary
				checkDate.setDate(checkDate.getDate() + 3); // Generate for next year
				return $.datepicker.iso8601Week(checkDate);
			}
		}
		return Math.floor(((checkDate - firstMon) / 86400000) / 7) + 1; // Weeks to given date
	},
	
	/* Provide status text for a particular date.
	   @param  date  the date to get the status for
	   @param  inst  the current datepicker instance
	   @return  the status display text for this date */
	dateStatus: function(date, inst) {
		return $.datepicker.formatDate(inst._get('dateStatus'), date, inst._getFormatConfig());
	},

	/* Parse a string value into a date object.
	   The format can be combinations of the following:
	   d  - day of month (no leading zero)
	   dd - day of month (two digit)
	   D  - day name short
	   DD - day name long
	   m  - month of year (no leading zero)
	   mm - month of year (two digit)
	   M  - month name short
	   MM - month name long
	   y  - year (two digit)
	   yy - year (four digit)
	   '...' - literal text
	   '' - single quote

	   @param  format           String - the expected format of the date
	   @param  value            String - the date in the above format
	   @param  settings  Object - attributes include:
	                     shortYearCutoff  Number - the cutoff year for determining the century (optional)
	                     dayNamesShort    String[7] - abbreviated names of the days from Sunday (optional)
	                     dayNames         String[7] - names of the days from Sunday (optional)
	                     monthNamesShort  String[12] - abbreviated names of the months (optional)
	                     monthNames       String[12] - names of the months (optional)
	   @return  Date - the extracted date value or null if value is blank */
	parseDate: function (format, value, settings) {
		if (format == null || value == null)
			throw 'Invalid arguments';
		value = (typeof value == 'object' ? value.toString() : value + '');
		if (value == '')
			return null;
		var shortYearCutoff = (settings ? settings.shortYearCutoff : null) || this._defaults.shortYearCutoff;
		var dayNamesShort = (settings ? settings.dayNamesShort : null) || this._defaults.dayNamesShort;
		var dayNames = (settings ? settings.dayNames : null) || this._defaults.dayNames;
		var monthNamesShort = (settings ? settings.monthNamesShort : null) || this._defaults.monthNamesShort;
		var monthNames = (settings ? settings.monthNames : null) || this._defaults.monthNames;
		var year = -1;
		var month = -1;
		var day = -1;
		var literal = false;
		// Check whether a format character is doubled
		var lookAhead = function(match) {
			var matches = (iFormat + 1 < format.length && format.charAt(iFormat + 1) == match);
			if (matches)
				iFormat++;
			return matches;	
		};
		// Extract a number from the string value
		var getNumber = function(match) {
			lookAhead(match);
			var size = (match == 'y' ? 4 : 2);
			var num = 0;
			while (size > 0 && iValue < value.length &&
					value.charAt(iValue) >= '0' && value.charAt(iValue) <= '9') {
				num = num * 10 + (value.charAt(iValue++) - 0);
				size--;
			}
			if (size == (match == 'y' ? 4 : 2))
				throw 'Missing number at position ' + iValue;
			return num;
		};
		// Extract a name from the string value and convert to an index
		var getName = function(match, shortNames, longNames) {
			var names = (lookAhead(match) ? longNames : shortNames);
			var size = 0;
			for (var j = 0; j < names.length; j++)
				size = Math.max(size, names[j].length);
			var name = '';
			var iInit = iValue;
			while (size > 0 && iValue < value.length) {
				name += value.charAt(iValue++);
				for (var i = 0; i < names.length; i++)
					if (name == names[i])
						return i + 1;
				size--;
			}
			throw 'Unknown name at position ' + iInit;
		};
		// Confirm that a literal character matches the string value
		var checkLiteral = function() {
			if (value.charAt(iValue) != format.charAt(iFormat))
				throw 'Unexpected literal at position ' + iValue;
			iValue++;
		};
		var iValue = 0;
		for (var iFormat = 0; iFormat < format.length; iFormat++) {
			if (literal)
				if (format.charAt(iFormat) == "'" && !lookAhead("'"))
					literal = false;
				else
					checkLiteral();
			else
				switch (format.charAt(iFormat)) {
					case 'd':
						day = getNumber('d');
						break;
					case 'D': 
						getName('D', dayNamesShort, dayNames);
						break;
					case 'm': 
						month = getNumber('m');
						break;
					case 'M':
						month = getName('M', monthNamesShort, monthNames); 
						break;
					case 'y':
						year = getNumber('y');
						break;
					case "'":
						if (lookAhead("'"))
							checkLiteral();
						else
							literal = true;
						break;
					default:
						checkLiteral();
				}
		}
		if (year < 100) {
			year += new Date().getFullYear() - new Date().getFullYear() % 100 +
				(year <= shortYearCutoff ? 0 : -100);
		}
		var date = new Date(year, month - 1, day);
		if (date.getFullYear() != year || date.getMonth() + 1 != month || date.getDate() != day) {
			throw 'Invalid date'; // E.g. 31/02/*
		}
		return date;
	},

	/* Format a date object into a string value.
	   The format can be combinations of the following:
	   d  - day of month (no leading zero)
	   dd - day of month (two digit)
	   D  - day name short
	   DD - day name long
	   m  - month of year (no leading zero)
	   mm - month of year (two digit)
	   M  - month name short
	   MM - month name long
	   y  - year (two digit)
	   yy - year (four digit)
	   '...' - literal text
	   '' - single quote

	   @param  format    String - the desired format of the date
	   @param  date      Date - the date value to format
	   @param  settings  Object - attributes include:
	                     dayNamesShort    String[7] - abbreviated names of the days from Sunday (optional)
	                     dayNames         String[7] - names of the days from Sunday (optional)
	                     monthNamesShort  String[12] - abbreviated names of the months (optional)
	                     monthNames       String[12] - names of the months (optional)
	   @return  String - the date in the above format */
	formatDate: function (format, date, settings) {
		if (!date)
			return '';
		var dayNamesShort = (settings ? settings.dayNamesShort : null) || this._defaults.dayNamesShort;
		var dayNames = (settings ? settings.dayNames : null) || this._defaults.dayNames;
		var monthNamesShort = (settings ? settings.monthNamesShort : null) || this._defaults.monthNamesShort;
		var monthNames = (settings ? settings.monthNames : null) || this._defaults.monthNames;
		// Check whether a format character is doubled
		var lookAhead = function(match) {
			var matches = (iFormat + 1 < format.length && format.charAt(iFormat + 1) == match);
			if (matches)
				iFormat++;
			return matches;	
		};
		// Format a number, with leading zero if necessary
		var formatNumber = function(match, value) {
			return (lookAhead(match) && value < 10 ? '0' : '') + value;
		};
		// Format a name, short or long as requested
		var formatName = function(match, value, shortNames, longNames) {
			return (lookAhead(match) ? longNames[value] : shortNames[value]);
		};
		var output = '';
		var literal = false;
		if (date) {
			for (var iFormat = 0; iFormat < format.length; iFormat++) {
				if (literal)
					if (format.charAt(iFormat) == "'" && !lookAhead("'"))
						literal = false;
					else
						output += format.charAt(iFormat);
				else
					switch (format.charAt(iFormat)) {
						case 'd':
							output += formatNumber('d', date.getDate()); 
							break;
						case 'D': 
							output += formatName('D', date.getDay(), dayNamesShort, dayNames);
							break;
						case 'm': 
							output += formatNumber('m', date.getMonth() + 1); 
							break;
						case 'M':
							output += formatName('M', date.getMonth(), monthNamesShort, monthNames); 
							break;
						case 'y':
							output += (lookAhead('y') ? date.getFullYear() : 
								(date.getYear() % 100 < 10 ? '0' : '') + date.getYear() % 100);
							break;
						case "'":
							if (lookAhead("'"))
								output += "'";
							else
								literal = true;
							break;
						default:
							output += format.charAt(iFormat);
					}
			}
		}
		return output;
	},

	/* Extract all possible characters from the date format. */
	_possibleChars: function (format) {
		var chars = '';
		var literal = false;
		for (var iFormat = 0; iFormat < format.length; iFormat++)
			if (literal)
				if (format.charAt(iFormat) == "'" && !lookAhead("'"))
					literal = false;
				else
					chars += format.charAt(iFormat);
			else
				switch (format.charAt(iFormat)) {
					case 'd' || 'm' || 'y':
						chars += '0123456789'; 
						break;
					case 'D' || 'M':
						return null; // Accept anything
					case "'":
						if (lookAhead("'"))
							chars += "'";
						else
							literal = true;
						break;
					default:
						chars += format.charAt(iFormat);
				}
		return chars;
	}
});

/* Individualised settings for date picker functionality applied to one or more related inputs.
   Instances are managed and manipulated through the Datepicker manager. */
function DatepickerInstance(settings, inline) {
	this._id = $.datepicker._register(this);
	this._selectedDay = 0; // Current date for selection
	this._selectedMonth = 0; // 0-11
	this._selectedYear = 0; // 4-digit year
	this._drawMonth = 0; // Current month at start of datepicker
	this._drawYear = 0;
	this._input = null; // The attached input field
	this._inline = inline; // True if showing inline, false if used in a popup
	this._datepickerDiv = (!inline ? $.datepicker._datepickerDiv :
		$('<div id="datepicker_div_' + this._id + '" class="datepicker_inline">'));
	// customise the date picker object - uses manager defaults if not overridden
	this._settings = extendRemove(settings || {}); // clone
	if (inline)
		this._setDate(this._getDefaultDate());
}

$.extend(DatepickerInstance.prototype, {
	/* Get a setting value, defaulting if necessary. */
	_get: function(name) {
		return this._settings[name] !== undefined ? this._settings[name] : $.datepicker._defaults[name];
	},

	/* Parse existing date and initialise date picker. */
	_setDateFromField: function(input) {
		this._input = $(input);
		var dateFormat = this._get('dateFormat');
		var dates = this._input ? this._input.val().split(this._get('rangeSeparator')) : null; 
		this._endDay = this._endMonth = this._endYear = null;
		var date = defaultDate = this._getDefaultDate();
		if (dates.length > 0) {
			var settings = this._getFormatConfig();
			if (dates.length > 1) {
				date = $.datepicker.parseDate(dateFormat, dates[1], settings) || defaultDate;
				this._endDay = date.getDate();
				this._endMonth = date.getMonth();
				this._endYear = date.getFullYear();
			}
			try {
				date = $.datepicker.parseDate(dateFormat, dates[0], settings) || defaultDate;
			} catch (e) {
				$.datepicker.log(e);
				date = defaultDate;
			}
		}
		this._selectedDay = date.getDate();
		this._drawMonth = this._selectedMonth = date.getMonth();
		this._drawYear = this._selectedYear = date.getFullYear();
		this._currentDay = (dates[0] ? date.getDate() : 0);
		this._currentMonth = (dates[0] ? date.getMonth() : 0);
		this._currentYear = (dates[0] ? date.getFullYear() : 0);
		this._adjustDate();
	},
	
	/* Retrieve the default date shown on opening. */
	_getDefaultDate: function() {
		var date = this._determineDate('defaultDate', new Date());
		var minDate = this._getMinMaxDate('min', true);
		var maxDate = this._getMinMaxDate('max');
		date = (minDate && date < minDate ? minDate : date);
		date = (maxDate && date > maxDate ? maxDate : date);
		return date;
	},

	/* A date may be specified as an exact value or a relative one. */
	_determineDate: function(name, defaultDate) {
		var offsetNumeric = function(offset) {
			var date = new Date();
			date.setDate(date.getDate() + offset);
			return date;
		};
		var offsetString = function(offset, getDaysInMonth) {
			var date = new Date();
			var matches = /^([+-]?[0-9]+)\s*(d|D|w|W|m|M|y|Y)?$/.exec(offset);
			if (matches) {
				var year = date.getFullYear();
				var month = date.getMonth();
				var day = date.getDate();
				switch (matches[2] || 'd') {
					case 'd' : case 'D' :
						day += (matches[1] - 0); break;
					case 'w' : case 'W' :
						day += (matches[1] * 7); break;
					case 'm' : case 'M' :
						month += (matches[1] - 0); 
						day = Math.min(day, getDaysInMonth(year, month));
						break;
					case 'y': case 'Y' :
						year += (matches[1] - 0);
						day = Math.min(day, getDaysInMonth(year, month));
						break;
				}
				date = new Date(year, month, day);
			}
			return date;
		};
		var date = this._get(name);
		return (date == null ? defaultDate :
			(typeof date == 'string' ? offsetString(date, this._getDaysInMonth) :
			(typeof date == 'number' ? offsetNumeric(date) : date)));
	},

	/* Set the date(s) directly. */
	_setDate: function(date, endDate) {
		this._selectedDay = this._currentDay = date.getDate();
		this._drawMonth = this._selectedMonth = this._currentMonth = date.getMonth();
		this._drawYear = this._selectedYear = this._currentYear = date.getFullYear();
		if (this._get('rangeSelect')) {
			if (endDate) {
				this._endDay = endDate.getDate();
				this._endMonth = endDate.getMonth();
				this._endYear = endDate.getFullYear();
			} else {
				this._endDay = this._currentDay;
				this._endMonth = this._currentMonth;
				this._endYear = this._currentYear;
			}
		}
		this._adjustDate();
	},

	/* Retrieve the date(s) directly. */
	_getDate: function() {
		var startDate = (!this._currentYear || (this._input && this._input.val() == '') ? null :
			new Date(this._currentYear, this._currentMonth, this._currentDay));
		if (this._get('rangeSelect')) {
			return [startDate, (!this._endYear ? null :
				new Date(this._endYear, this._endMonth, this._endDay))];
		} else
			return startDate;
	},

	/* Generate the HTML for the current state of the date picker. */
	_generateDatepicker: function() {
		var today = new Date();
		today = new Date(today.getFullYear(), today.getMonth(), today.getDate()); // clear time
		var showStatus = this._get('showStatus');
		var isRTL = this._get('isRTL');
		// build the date picker HTML
		var clear = (this._get('mandatory') ? '' :
			'<div class="datepicker_clear"><a onclick="jQuery.datepicker._clearDate(' + this._id + ');"' + 
			(showStatus ? this._addStatus(this._get('clearStatus') || '&#xa0;') : '') + '>' +
			this._get('clearText') + '</a></div>');
		var controls = '<div class="datepicker_control">' + (isRTL ? '' : clear) +
			'<div class="datepicker_close"><a onclick="jQuery.datepicker._hideDatepicker();"' +
			(showStatus ? this._addStatus(this._get('closeStatus') || '&#xa0;') : '') + '>' +
			this._get('closeText') + '</a></div>' + (isRTL ? clear : '')  + '</div>';
		var prompt = this._get('prompt');
		var closeAtTop = this._get('closeAtTop');
		var hideIfNoPrevNext = this._get('hideIfNoPrevNext');
		var numMonths = this._getNumberOfMonths();
		var stepMonths = this._get('stepMonths');
		var isMultiMonth = (numMonths[0] != 1 || numMonths[1] != 1);
		var minDate = this._getMinMaxDate('min', true);
		var maxDate = this._getMinMaxDate('max');
		var drawMonth = this._drawMonth;
		var drawYear = this._drawYear;
		if (maxDate) {
			var maxDraw = new Date(maxDate.getFullYear(),
				maxDate.getMonth() - numMonths[1] + 1, maxDate.getDate());
			maxDraw = (minDate && maxDraw < minDate ? minDate : maxDraw);
			while (new Date(drawYear, drawMonth, 1) > maxDraw) {
				drawMonth--;
				if (drawMonth < 0) {
					drawMonth = 11;
					drawYear--;
				}
			}
		}
		// controls and links
		var prev = '<div class="datepicker_prev">' + (this._canAdjustMonth(-1, drawYear, drawMonth) ? 
			'<a onclick="jQuery.datepicker._adjustDate(' + this._id + ', -' + stepMonths + ', \'M\');"' +
			(showStatus ? this._addStatus(this._get('prevStatus') || '&#xa0;') : '') + '>' +
			this._get('prevText') + '</a>' :
			(hideIfNoPrevNext ? '' : '<label>' + this._get('prevText') + '</label>')) + '</div>';
		var next = '<div class="datepicker_next">' + (this._canAdjustMonth(+1, drawYear, drawMonth) ?
			'<a onclick="jQuery.datepicker._adjustDate(' + this._id + ', +' + stepMonths + ', \'M\');"' +
			(showStatus ? this._addStatus(this._get('nextStatus') || '&#xa0;') : '') + '>' +
			this._get('nextText') + '</a>' :
			(hideIfNoPrevNext ? '>' : '<label>' + this._get('nextText') + '</label>')) + '</div>';
		var html = (prompt ? '<div class="datepicker_prompt">' + prompt + '</div>' : '') +
			(closeAtTop && !this._inline ? controls : '') +
			'<div class="datepicker_links">' + (isRTL ? next : prev) +
			(this._isInRange(today) ? '<div class="datepicker_current">' +
			'<a onclick="jQuery.datepicker._gotoToday(' + this._id + ');"' +
			(showStatus ? this._addStatus(this._get('currentStatus') || '&#xa0;') : '') + '>' +
			this._get('currentText') + '</a></div>' : '') + (isRTL ? prev : next) + '</div>';
		var showWeeks = this._get('showWeeks');
		for (var row = 0; row < numMonths[0]; row++)
			for (var col = 0; col < numMonths[1]; col++) {
				var selectedDate = new Date(drawYear, drawMonth, this._selectedDay);
				html += '<div class="datepicker_oneMonth' + (col == 0 ? ' datepicker_newRow' : '') + '">' +
					this._generateMonthYearHeader(drawMonth, drawYear, minDate, maxDate,
					selectedDate, row > 0 || col > 0) + // draw month headers
					'<table class="datepicker" cellpadding="0" cellspacing="0"><thead>' + 
					'<tr class="datepicker_titleRow">' +
					(showWeeks ? '<td>' + this._get('weekHeader') + '</td>' : '');
				var firstDay = this._get('firstDay');
				var changeFirstDay = this._get('changeFirstDay');
				var dayNames = this._get('dayNames');
				var dayNamesShort = this._get('dayNamesShort');
				var dayNamesMin = this._get('dayNamesMin');
				for (var dow = 0; dow < 7; dow++) { // days of the week
					var day = (dow + firstDay) % 7;
					var status = this._get('dayStatus') || '&#xa0;';
					status = (status.indexOf('DD') > -1 ? status.replace(/DD/, dayNames[day]) :
						status.replace(/D/, dayNamesShort[day]));
					html += '<td' + ((dow + firstDay + 6) % 7 >= 5 ? ' class="datepicker_weekEndCell"' : '') + '>' +
						(!changeFirstDay ? '<span' :
						'<a onclick="jQuery.datepicker._changeFirstDay(' + this._id + ', ' + day + ');"') + 
						(showStatus ? this._addStatus(status) : '') + ' title="' + dayNames[day] + '">' +
						dayNamesMin[day] + (changeFirstDay ? '</a>' : '</span>') + '</td>';
				}
				html += '</tr></thead><tbody>';
				var daysInMonth = this._getDaysInMonth(drawYear, drawMonth);
				if (drawYear == this._selectedYear && drawMonth == this._selectedMonth) {
					this._selectedDay = Math.min(this._selectedDay, daysInMonth);
				}
				var leadDays = (this._getFirstDayOfMonth(drawYear, drawMonth) - firstDay + 7) % 7;
				var currentDate = (!this._currentDay ? new Date(9999, 9, 9) :
					new Date(this._currentYear, this._currentMonth, this._currentDay));
				var endDate = this._endDay ? new Date(this._endYear, this._endMonth, this._endDay) : currentDate;
				var printDate = new Date(drawYear, drawMonth, 1 - leadDays);
				var numRows = (isMultiMonth ? 6 : Math.ceil((leadDays + daysInMonth) / 7)); // calculate the number of rows to generate
				var beforeShowDay = this._get('beforeShowDay');
				var showOtherMonths = this._get('showOtherMonths');
				var calculateWeek = this._get('calculateWeek') || $.datepicker.iso8601Week;
				var dateStatus = this._get('statusForDate') || $.datepicker.dateStatus;
				for (var dRow = 0; dRow < numRows; dRow++) { // create date picker rows
					html += '<tr class="datepicker_daysRow">' +
						(showWeeks ? '<td class="datepicker_weekCol">' + calculateWeek(printDate) + '</td>' : '');
					for (var dow = 0; dow < 7; dow++) { // create date picker days
						var daySettings = (beforeShowDay ?
							beforeShowDay.apply((this._input ? this._input[0] : null), [printDate]) : [true, '']);
						var otherMonth = (printDate.getMonth() != drawMonth);
						var unselectable = otherMonth || !daySettings[0] ||
							(minDate && printDate < minDate) || (maxDate && printDate > maxDate);
						html += '<td class="datepicker_daysCell' +
							((dow + firstDay + 6) % 7 >= 5 ? ' datepicker_weekEndCell' : '') + // highlight weekends
							(otherMonth ? ' datepicker_otherMonth' : '') + // highlight days from other months
							(printDate.getTime() == selectedDate.getTime() && drawMonth == this._selectedMonth ?
							' datepicker_daysCellOver' : '') + // highlight selected day
							(unselectable ? ' datepicker_unselectable' : '') +  // highlight unselectable days
							(otherMonth && !showOtherMonths ? '' : ' ' + daySettings[1] + // highlight custom dates
							(printDate.getTime() >= currentDate.getTime() && printDate.getTime() <= endDate.getTime() ?  // in current range
							' datepicker_currentDay' : '') + // highlight selected day
							(printDate.getTime() == today.getTime() ? ' datepicker_today' : '')) + '"' + // highlight today (if different)
							(unselectable ? '' : ' onmouseover="jQuery(this).addClass(\'datepicker_daysCellOver\');' +
							(!showStatus || (otherMonth && !showOtherMonths) ? '' : 'jQuery(\'#datepicker_status_' +
							this._id + '\').html(\'' + (dateStatus.apply((this._input ? this._input[0] : null),
							[printDate, this]) || '&#xa0;') +'\');') + '"' +
							' onmouseout="jQuery(this).removeClass(\'datepicker_daysCellOver\');' +
							(!showStatus || (otherMonth && !showOtherMonths) ? '' : 'jQuery(\'#datepicker_status_' +
							this._id + '\').html(\'&#xa0;\');') + '" onclick="jQuery.datepicker._selectDay(' +
							this._id + ',' + drawMonth + ',' + drawYear + ', this);"') + '>' + // actions
							(otherMonth ? (showOtherMonths ? printDate.getDate() : '&#xa0;') : // display for other months
							(unselectable ? printDate.getDate() : '<a>' + printDate.getDate() + '</a>')) + '</td>'; // display for this month
						printDate.setDate(printDate.getDate() + 1);
					}
					html += '</tr>';
				}
				drawMonth++;
				if (drawMonth > 11) {
					drawMonth = 0;
					drawYear++;
				}
				html += '</tbody></table></div>';
			}
		html += (showStatus ? '<div style="clear: both;"></div><div id="datepicker_status_' + this._id + 
			'" class="datepicker_status">' + (this._get('initStatus') || '&#xa0;') + '</div>' : '') +
			(!closeAtTop && !this._inline ? controls : '') +
			'<div style="clear: both;"></div>' + 
			($.browser.msie && parseInt($.browser.version) < 7 && !this._inline ? 
			'<iframe src="javascript:false;" class="datepicker_cover"></iframe>' : '');
		return html;
	},
	
	/* Generate the month and year header. */
	_generateMonthYearHeader: function(drawMonth, drawYear, minDate, maxDate, selectedDate, secondary) {
		minDate = (this._rangeStart && minDate && selectedDate < minDate ? selectedDate : minDate);
		var showStatus = this._get('showStatus');
		var html = '<div class="datepicker_header">';
		// month selection
		var monthNames = this._get('monthNames');
		if (secondary || !this._get('changeMonth'))
			html += monthNames[drawMonth] + '&#xa0;';
			
		else {
			var inMinYear = (minDate && minDate.getFullYear() == drawYear);
			var inMaxYear = (maxDate && maxDate.getFullYear() == drawYear);
			html += '<select class="datepicker_newMonth" ' +
				'onchange="jQuery.datepicker._selectMonthYear(' + this._id + ', this, \'M\');" ' +
				'onclick="jQuery.datepicker._clickMonthYear(' + this._id + ');"' +
				(showStatus ? this._addStatus(this._get('monthStatus') || '&#xa0;') : '') + '>';
			for (var month = 0; month < 12; month++) {
				if ((!inMinYear || month >= minDate.getMonth()) &&
						(!inMaxYear || month <= maxDate.getMonth())) {
					html += '<option value="' + month + '"' +
						(month == drawMonth ? ' selected="selected"' : '') +
						'>' + monthNames[month] + '</option>';
				}
			}
			html += '</select>';
		}
		// year selection
		if (secondary || !this._get('changeYear'))
			html += drawYear;
		else {
			// determine range of years to display
			var years = this._get('yearRange').split(':');
			var year = 0;
			var endYear = 0;
			if (years.length != 2) {
				year = drawYear - 10;
				endYear = drawYear + 10;
			} else if (years[0].charAt(0) == '+' || years[0].charAt(0) == '-') {
				year = drawYear + parseInt(years[0], 10);
				endYear = drawYear + parseInt(years[1], 10);
			} else {
				year = parseInt(years[0], 10);
				endYear = parseInt(years[1], 10);
			}
			year = (minDate ? Math.max(year, minDate.getFullYear()) : year);
			endYear = (maxDate ? Math.min(endYear, maxDate.getFullYear()) : endYear);
			html += '<select class="datepicker_newYear" ' +
				'onchange="jQuery.datepicker._selectMonthYear(' + this._id + ', this, \'Y\');" ' +
				'onclick="jQuery.datepicker._clickMonthYear(' + this._id + ');"' +
				(showStatus ? this._addStatus(this._get('yearStatus') || '&#xa0;') : '') + '>';
			for (; year <= endYear; year++) {
				html += '<option value="' + year + '"' +
					(year == drawYear ? ' selected="selected"' : '') +
					'>' + year + '</option>';
			}
			html += '</select>';
		}
		html += '</div>'; // Close datepicker_header
		return html;
	},

	/* Provide code to set and clear the status panel. */
	_addStatus: function(text) {
		return ' onmouseover="jQuery(\'#datepicker_status_' + this._id + '\').html(\'' + text + '\');" ' +
			'onmouseout="jQuery(\'#datepicker_status_' + this._id + '\').html(\'&#xa0;\');"';
	},

	/* Adjust one of the date sub-fields. */
	_adjustDate: function(offset, period) {
		var year = this._drawYear + (period == 'Y' ? offset : 0);
		var month = this._drawMonth + (period == 'M' ? offset : 0);
		var day = Math.min(this._selectedDay, this._getDaysInMonth(year, month)) +
			(period == 'D' ? offset : 0);
		var date = new Date(year, month, day);
		// ensure it is within the bounds set
		var minDate = this._getMinMaxDate('min', true);
		var maxDate = this._getMinMaxDate('max');
		date = (minDate && date < minDate ? minDate : date);
		date = (maxDate && date > maxDate ? maxDate : date);
		this._selectedDay = date.getDate();
		this._drawMonth = this._selectedMonth = date.getMonth();
		this._drawYear = this._selectedYear = date.getFullYear();
	},
	
	/* Determine the number of months to show. */
	_getNumberOfMonths: function() {
		var numMonths = this._get('numberOfMonths');
		return (numMonths == null ? [1, 1] : (typeof numMonths == 'number' ? [1, numMonths] : numMonths));
	},

	/* Determine the current maximum date - ensure no time components are set - may be overridden for a range. */
	_getMinMaxDate: function(minMax, checkRange) {
		var date = this._determineDate(minMax + 'Date', null);
		if (date) {
			date.setHours(0);
			date.setMinutes(0);
			date.setSeconds(0);
			date.setMilliseconds(0);
		}
		return date || (checkRange ? this._rangeStart : null);
	},

	/* Find the number of days in a given month. */
	_getDaysInMonth: function(year, month) {
		return 32 - new Date(year, month, 32).getDate();
	},

	/* Find the day of the week of the first of a month. */
	_getFirstDayOfMonth: function(year, month) {
		return new Date(year, month, 1).getDay();
	},

	/* Determines if we should allow a "next/prev" month display change. */
	_canAdjustMonth: function(offset, curYear, curMonth) {
		var numMonths = this._getNumberOfMonths();
		var date = new Date(curYear, curMonth + (offset < 0 ? offset : numMonths[1]), 1);
		if (offset < 0)
			date.setDate(this._getDaysInMonth(date.getFullYear(), date.getMonth()));
		return this._isInRange(date);
	},

	/* Is the given date in the accepted range? */
	_isInRange: function(date) {
		// during range selection, use minimum of selected date and range start
		var newMinDate = (!this._rangeStart ? null :
			new Date(this._selectedYear, this._selectedMonth, this._selectedDay));
		newMinDate = (newMinDate && this._rangeStart < newMinDate ? this._rangeStart : newMinDate);
		var minDate = newMinDate || this._getMinMaxDate('min');
		var maxDate = this._getMinMaxDate('max');
		return ((!minDate || date >= minDate) && (!maxDate || date <= maxDate));
	},
	
	/* Provide the configuration settings for formatting/parsing. */
	_getFormatConfig: function() {
		var shortYearCutoff = this._get('shortYearCutoff');
		shortYearCutoff = (typeof shortYearCutoff != 'string' ? shortYearCutoff :
			new Date().getFullYear() % 100 + parseInt(shortYearCutoff, 10));
		return {shortYearCutoff: shortYearCutoff,
			dayNamesShort: this._get('dayNamesShort'), dayNames: this._get('dayNames'),
			monthNamesShort: this._get('monthNamesShort'), monthNames: this._get('monthNames')};
	},

	/* Format the given date for display. */
	_formatDate: function(day, month, year) {
		if (!day) {
			this._currentDay = this._selectedDay;
			this._currentMonth = this._selectedMonth;
			this._currentYear = this._selectedYear;
		}
		var date = (day ? (typeof day == 'object' ? day : new Date(year, month, day)) :
			new Date(this._currentYear, this._currentMonth, this._currentDay));
		return $.datepicker.formatDate(this._get('dateFormat'), date, this._getFormatConfig());
	}
});

/* jQuery extend now ignores nulls! */
function extendRemove(target, props) {
	$.extend(target, props);
	for (var name in props)
		if (props[name] == null)
			target[name] = null;
	return target;
};

/* Invoke the datepicker functionality.
   @param  options  String - a command, optionally followed by additional parameters or
                    Object - settings for attaching new datepicker functionality
   @return  jQuery object */
$.fn.datepicker = function(options){
	var otherArgs = Array.prototype.slice.call(arguments, 1);
	if (typeof options == 'string' && (options == 'isDisabled' || options == 'getDate')) {
		return $.datepicker['_' + options + 'Datepicker'].apply($.datepicker, [this[0]].concat(otherArgs));
	}
	return this.each(function() {
		typeof options == 'string' ?
			$.datepicker['_' + options + 'Datepicker'].apply($.datepicker, [this].concat(otherArgs)) :
			$.datepicker._attachDatepicker(this, options);
	});
};

$.datepicker = new Datepicker(); // singleton instance
	
/* Initialise the date picker. */
$(document).ready(function() {
	$(document.body).append($.datepicker._datepickerDiv)
		.mousedown($.datepicker._checkExternalClick);
});

})(jQuery);