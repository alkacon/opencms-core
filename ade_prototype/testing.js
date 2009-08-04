$.extend($.ui.sortable.prototype, {
   _uiHash: function(inst) {
      var self = inst || this;
      return {
         helper: self.helper,
         placeholder: self.placeholder || $([]),
         position: self.position,
         absolutePosition: self.positionAbs, //deprecated
         offset: self.positionAbs,
         item: self.currentItem,
         sender: inst ? inst.element : null,
         self: self
      };
   },
   _mouseStop: function(event, noPropagation) {
   
      if (!event) 
         return;
      
      //If we are using droppables, inform the manager about the drop
      if ($.ui.ddmanager && !this.options.dropBehaviour) 
         $.ui.ddmanager.drop(this, event);
      
      if (this.options.revert) {
         var self = this;
         var cur;
         if (self.placeholder.css('display') == 'none') {
            cur = self.cmsStartOffset;
         } else {
            cur = self.placeholder.offset();
         }
         
         self.reverting = true;
         
         $(this.helper).animate({
            left: cur.left - this.offset.parent.left - self.margins.left + (this.offsetParent[0] == document.body ? 0 : this.offsetParent[0].scrollLeft),
            top: cur.top - this.offset.parent.top - self.margins.top + (this.offsetParent[0] == document.body ? 0 : this.offsetParent[0].scrollTop)
         }, parseInt(this.options.revert, 10) || 500, function() {
            self._clear(event);
         });
      } else {
         this._clear(event, noPropagation);
      }
      
      return false;
      
   }
})

var log = function(s) {
   $("body").append("<p>" + s + "</p>");
}

var dump = function(s) {
   $("body").append("<pre>" + $.dump(s) + "</pre>");
}

var cms_elements_list = {
   'item_001': {
      'resource_id': 'item_001',
      'nav_text': 'XML',
      'title': 'XML based contents',
      'file': 'xml_contents.xml',
      'date': '4/4/2009 5:30 PM',
      'user': 'Admin',
      'type': 'news',
      'contents': {
         'col1_content': '<div class="box box_schema2" rel="item_001"><h4>XML based contents</h4><div class="boxbody"><p>In this section, you find additinal examples for xml based contents (news and events) demonstrating how to manage structured contents.</p><p>&nbsp;</p><p>These contents are displayed in various list boxes, where the selection and the ordering of the contents shown inside is done by a so-called "collector". At this page, for example, the collector provides the two most-recent news or events, only.</p></div></div>',
         'col3_content': '<div class="box box_schema2" rel="item_001"><h4>XML based contents</h4><div class="boxbody"><p>In this section, you find additinal examples for xml based contents (news and events) demonstrating how to manage structured contents.</p><p>&nbsp;</p><p>These contents are displayed in various list boxes, where the selection and the ordering of the contents shown inside is done by a so-called "collector". At this page, for example, the collector provides the two most-recent news or events, only.</p></div></div>',
         'col2_content': '<div class="box box_schema2" rel="item_001"><h4>XML based contents</h4><div class="boxbody"><p>In this section, you find additinal examples for xml based contents (news and events) demonstrating how to manage structured contents.</p><p>&nbsp;</p><p>These contents are displayed in various list boxes, where the selection and the ordering of the contents shown inside is done by a so-called "collector". At this page, for example, the collector provides the two most-recent news or events, only.</p></div></div>'
      }
   },
   'item_002': {
      'resource_id': 'item_002',
      'nav_text': 'Direct edit',
      'title': 'Direct edit',
      'file': 'direct_edit.xml',
      'date': '4/4/2009 5:30 PM',
      'user': 'Admin',
      'type': 'news',
      'contents': {
         'col1_content': '<div class="box box_schema2" rel="item_002"><h4>Direct edit</h4><div class="boxbody"><p>By utilizing the direct edit feature, you can create new or edit already existing XML contents.</p></div></div>',
         'col3_content': '<div class="box box_schema2" rel="item_002"><h4>Direct edit</h4><div class="boxbody"><p>By utilizing the direct edit feature, you can create new or edit already existing XML contents.</p></div></div>',
         'col2_content': '<div class="box box_schema2" rel="item_002"><h4>Direct edit</h4><div class="boxbody"><p>By utilizing the direct edit feature, you can create new or edit already existing XML contents.</p></div></div>'
      }
   },
   'item_003': {
      'resource_id': 'item_003',
      'nav_text': 'Login',
      'title': 'Login box',
      'file': 'login.xml',
      'date': '4/4/2009 5:30 PM',
      'user': 'Admin',
      'type': 'news',
      'contents': {
         'col1_content': '<div class="box box_schema4" rel="item_003"><h4>Login</h4><div class="boxbody"><p><b>Logged in as:</b></p><form method="get" action="/opencms/opencms/demo_en/extra/login.html" class="loginform"><div class="boxform">(Admin)</div><div class="boxform"><input name="action" value="logoff" type="hidden"/><input name="requestedResource" value="/demo_en/today/index.html" type="hidden"/><input class="button" value="Logoff" type="submit"/></div></form></div></div>',
         'col3_content': '<div class="box box_schema4" rel="item_003"><h4>Login</h4><div class="boxbody"><p><b>Logged in as:</b></p><form method="get" action="/opencms/opencms/demo_en/extra/login.html" class="loginform"><div class="boxform">(Admin)</div><div class="boxform"><input name="action" value="logoff" type="hidden"/><input name="requestedResource" value="/demo_en/today/index.html" type="hidden"/><input class="button" value="Logoff" type="submit"/></div></form></div></div>',
         'col2_content': '<div class="box box_schema4" rel="item_003"><h4>Login</h4><div class="boxbody"><p><b>Logged in as:</b></p><form method="get" action="/opencms/opencms/demo_en/extra/login.html" class="loginform"><div class="boxform">(Admin)</div><div class="boxform"><input name="action" value="logoff" type="hidden"/><input name="requestedResource" value="/demo_en/today/index.html" type="hidden"/><input class="button" value="Logoff" type="submit"/></div></form></div></div>'
      }
   },
   'item_004': {
      'resource_id': 'item_004',
      'nav_text': 'Login',
      'title': 'Login description',
      'file': 'login_description.xml',
      'date': '4/4/2009 5:30 PM',
      'user': 'Admin',
      'type': 'news',
      'contents': {
         'col1_content': '<div class="box box_schema4" rel="item_004"><h4>Login</h4><div class="boxbody"><p>Via the login page or the login box, you can enter your credentials in order to access the protected section (extranet).</p><p>&nbsp;</p><p>Furthermore, since this login switches to the "Offline Project" (a working copy of all contents of the site), the user can edit the contents using the "direct edit" buttons.</p><p>&nbsp;</p><p>If you are already logged in, you can logout here.</p></div></div>',
         'col3_content': '<div class="box box_schema4" rel="item_004"><h4>Login</h4><div class="boxbody"><p>Via the login page or the login box, you can enter your credentials in order to access the protected section (extranet).</p><p>&nbsp;</p><p>Furthermore, since this login switches to the "Offline Project" (a working copy of all contents of the site), the user can edit the contents using the "direct edit" buttons.</p><p>&nbsp;</p><p>If you are already logged in, you can logout here.</p></div></div>',
         'col2_content': '<div class="box box_schema4" rel="item_004"><h4>Login</h4><div class="boxbody"><p>Via the login page or the login box, you can enter your credentials in order to access the protected section (extranet).</p><p>&nbsp;</p><p>Furthermore, since this login switches to the "Offline Project" (a working copy of all contents of the site), the user can edit the contents using the "direct edit" buttons.</p><p>&nbsp;</p><p>If you are already logged in, you can logout here.</p></div></div>'
      }
   },
   'item_005': {
      'resource_id': 'item_005',
      'nav_text': 'Photo album',
      'title': 'Photo album',
      'file': 'photo_album.xml',
      'date': '4/4/2009 5:30 PM',
      'user': 'Admin',
      'type': 'news',
      'contents': {
         'col1_content': '<div class="box box_schema4" rel="item_005"><h4>Photo album</h4><div class="boxbody boxbody_listentry"><div class="left"><img src="flower-demo-Dateien/Workshop.jpg" alt="Cooking with flowers"/></div><p>A photo album renders overview pages showing images of a distinct folder. By clicking on them, the album renders the selected image.</p></div></div>',
         'col3_content': '<div class="box box_schema4" rel="item_005"><h4>Photo album</h4><div class="boxbody boxbody_listentry"><div class="left"><img src="flower-demo-Dateien/Workshop.jpg" alt="Cooking with flowers"/></div><p>A photo album renders overview pages showing images of a distinct folder. By clicking on them, the album renders the selected image.</p></div></div>',
         'col2_content': '<div class="box box_schema4" rel="item_005"><h4>Photo album</h4><div class="boxbody boxbody_listentry"><div class="left"><img src="flower-demo-Dateien/Workshop.jpg" alt="Cooking with flowers"/></div><p>A photo album renders overview pages showing images of a distinct folder. By clicking on them, the album renders the selected image.</p></div></div>'
      }
   },
   'item_006': {
      'resource_id': 'item_006',
      'nav_text': 'News',
      'title': 'Recent news',
      'file': 'news_list.xml',
      'date': '4/4/2009 5:30 PM',
      'user': 'Admin',
      'type': 'news_list',
      'contents': {
         'col1_content': '<div class="box box_schema3" rel="item_006"><h4>Recent news</h4><div class="boxbody"><div class="boxbody_listentry"><h5><a href="http://localhost:8080/opencms/opencms/demo_en/today/news/news_0005.html">Cooking with flowers</a></h5><p><small>Jan 5, 2009</small></p>\
							<div class="left"><img src="flower-demo-Dateien/Erdbeeren.jpg" alt="Cooking with flowers" width="100" height="75"/></div><div><p>New edition will be released in 2009. More than 100 new recipes and lots of useful tips for planting.</p>\
<p>Jekka McVicar, expert herb grower and best-selling author, examines\
50 flowers, with tips on how to grow them in your garden, or in\
containers, and when to harvest. Showing which parts of the flowers are\
the tastiest and which should be removed, Jekka provides at least 2\
recipes for each flower.</p></div></div>\
					<div class="boxbody_listentry"><h5><a href="http://localhost:8080/opencms/opencms/demo_en/today/news/news_0007.html">Transportationbag for Christmastrees</a></h5><p><small>Dec 10, 2008</small></p><div class="left"><img src="flower-demo-Dateien/Christmastree.jpg" alt="Transportationbag for Christmastrees" width="100" height="133"/></div><div><p>Since this season, carrying bags for christmas trees are available.</p>\
<p>The advantage of these bags is clear. Well stored trees cannot soil\
cars, clothes and houses. But this bag is also useful for a needle-free\
removement of the tree. The manufacturer offers also the possibility to\
print advertisements on the bags- this can ba a good campaign for the\
christmastree vendors.</p></div></div></div></div>'         ,
         'col3_content': '<div class="box box_schema3" rel="item_006"><h4>Recent news</h4><div class="boxbody"><div class="boxbody_listentry"><h5><a href="http://localhost:8080/opencms/opencms/demo_en/today/news/news_0005.html">Cooking with flowers</a></h5><p><small>Jan 5, 2009</small></p>\
							<div class="left"><img src="flower-demo-Dateien/Erdbeeren.jpg" alt="Cooking with flowers" width="100" height="75"/></div><div><p>New edition will be released in 2009. More than 100 new recipes and lots of useful tips for planting.</p>\
<p>Jekka McVicar, expert herb grower and best-selling author, examines\
50 flowers, with tips on how to grow them in your garden, or in\
containers, and when to harvest. Showing which parts of the flowers are\
the tastiest and which should be removed, Jekka provides at least 2\
recipes for each flower.</p></div></div>\
					<div class="boxbody_listentry"><h5><a href="http://localhost:8080/opencms/opencms/demo_en/today/news/news_0007.html">Transportationbag for Christmastrees</a></h5><p><small>Dec 10, 2008</small></p><div class="left"><img src="flower-demo-Dateien/Christmastree.jpg" alt="Transportationbag for Christmastrees" width="100" height="133"/></div><div><p>Since this season, carrying bags for christmas trees are available.</p>\
<p>The advantage of these bags is clear. Well stored trees cannot soil\
cars, clothes and houses. But this bag is also useful for a needle-free\
removement of the tree. The manufacturer offers also the possibility to\
print advertisements on the bags- this can ba a good campaign for the\
christmastree vendors.</p></div></div></div></div>'         ,
         'col2_content': '<div class="box box_schema3" rel="item_006"><h4>Recent news</h4><div class="boxbody"><div class="boxbody_listentry"><h5><a href="http://localhost:8080/opencms/opencms/demo_en/today/news/news_0005.html">Cooking with flowers</a></h5><p><small>Jan 5, 2009</small></p>\
							<div class="left"><img src="flower-demo-Dateien/Erdbeeren.jpg" alt="Cooking with flowers" width="100" height="75"/></div><div><p>New edition will be released in 2009. More than 100 new recipes and lots of useful tips for planting.</p>\
<p>Jekka McVicar, expert herb grower and best-selling author, examines\
50 flowers, with tips on how to grow them in your garden, or in\
containers, and when to harvest. Showing which parts of the flowers are\
the tastiest and which should be removed, Jekka provides at least 2\
recipes for each flower.</p></div></div>\
					<div class="boxbody_listentry"><h5><a href="http://localhost:8080/opencms/opencms/demo_en/today/news/news_0007.html">Transportationbag for Christmastrees</a></h5><p><small>Dec 10, 2008</small></p><div class="left"><img src="flower-demo-Dateien/Christmastree.jpg" alt="Transportationbag for Christmastrees" width="100" height="133"/></div><div><p>Since this season, carrying bags for christmas trees are available.</p>\
<p>The advantage of these bags is clear. Well stored trees cannot soil\
cars, clothes and houses. But this bag is also useful for a needle-free\
removement of the tree. The manufacturer offers also the possibility to\
print advertisements on the bags- this can ba a good campaign for the\
christmastree vendors.</p></div></div></div></div>'
      }
   },
   'item_007': {
      'resource_id': 'item_007',
      'nav_text': 'Flower of the year',
      'title': 'Flower of the year 2009',
      'file': 'flower_of_year.xml',
      'date': '4/4/2009 5:30 PM',
      'user': 'Admin',
      'type': 'news',
      'contents': {
         'bottom_cont': '<div class="box box_schema3" rel="item_007"><h4>Flower of the year 2009</h4><div class="boxbody boxbody_listentry"><div class="left"><img src="flower-demo-Dateien/Wegwarte.jpg" alt="Wegwarte" /></div><div><p>The flower of the year 2009 is the common chicory. Common chicory (Cichorium intybus) is a bushy perennial herb with blue, lavender, or occasionally white flowers.</p></div></div></div>'
      }
   },
   'item_008': {
      'resource_id': 'item_008',
      'nav_text': 'Flowerservice',
      'title': 'New Flowerservice!',
      'file': 'flowerservice.xml',
      'date': '4/4/2009 5:30 PM',
      'user': 'Admin',
      'type': 'news',
      'contents': {
         'bottom_cont': '<div class="box box_schema3" rel="item_008"><h4>New Flowerservice!</h4><div class="boxbody boxbody_listentry"><div class="left"><img src="flower-demo-Dateien/GartenII.jpg" alt="Wegwarte" /></div><div><p>Berlin - In the hope of a bloomy summer trade lots of German nurseries offer their customers an All- Inclusive Package. Beside an extensive client counselling...</p></div></div></div>'
      }
   },
   'item_009': {
      'resource_id': 'item_009',
      'nav_text': 'Ikebana',
      'title': 'Ikebana - flowers a gateway to life',
      'file': 'ikebana.xml',
      'date': '4/4/2009 5:30 PM',
      'user': 'Admin',
      'type': 'news',
      'contents': {
         'bottom_cont': '<div class="box box_schema3" rel="item_009"><h4>Ikebana - flowers a gateway to life</h4><div class="boxbody"><p>For interested women and men who want to open their view on the harmony\
and beauty of flowers.\
The event takes place at Missionshaus Hofstetten.\
Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam\
nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat,\
sed diam voluptua. ...</p></div></div>'
      }
   },
   'item_010': {
      'resource_id': 'item_010',
      'nav_text': 'Differences in Botany',
      'title': 'Differences in Botany',
      'file': '/demo_en/site/news/news_01.html',
      'date': '4/4/2009 5:30 PM',
      'user': 'Admin',
      'type': 'news',
      'contents': {
         'col1_content': '<div class="box box_schema4"  rel="item_010"><h4>Differences in Botany</h4><div class="boxbody"><p>Plants are living organisms belonging to the kingdom Plantae. They include familiar organisms such as trees, herbs, bushes, grasses, vines, ferns, mosses, and green algae. About 350,000 species of plants, defined as seed plants, bryophytes, ferns and fern allies, are estimated to exist currently.</p></div></div>',
         'col3_content': '<div class="box box_schema4"  rel="item_010"><h4>Differences in Botany</h4><div class="boxbody"><p>Plants are living organisms belonging to the kingdom Plantae. They include familiar organisms such as trees, herbs, bushes, grasses, vines, ferns, mosses, and green algae. About 350,000 species of plants, defined as seed plants, bryophytes, ferns and fern allies, are estimated to exist currently.</p></div></div>',
         'col2_content': '<div class="box box_schema4"  rel="item_010"><h4>Differences in Botany</h4><div class="boxbody"><p>Plants are living organisms belonging to the kingdom Plantae. They include familiar organisms such as trees, herbs, bushes, grasses, vines, ferns, mosses, and green algae. About 350,000 species of plants, defined as seed plants, bryophytes, ferns and fern allies, are estimated to exist currently.</p></div></div>',
         'bottom_cont': '<div class="box box_schema4"  rel="item_010"><h4>Differences in Botany</h4><div class="boxbody"><p>Plants are living organisms belonging to the kingdom Plantae. They include familiar organisms such as trees, herbs, bushes, grasses, vines, ferns, mosses, and green algae. About 350,000 species of plants, defined as seed plants, bryophytes, ferns and fern allies, are estimated to exist currently.</p></div></div>'
      }
   },
   'item_011': {
      'resource_id': 'item_011',
      'nav_text': 'Flower workshop',
      'title': 'Flower workshop in Bonn, Germany',
      'file': '/demo_en/site/events/event_01.html',
      'date': '4/4/2009 5:30 PM',
      'user': 'Admin',
      'type': 'event',
      'contents': {
         'col1_content': '<div class="box box_schema2">\
							<h4>Flower workshop in Bonn, Germany</h4>\
							<div class="boxbody boxbody_listentry">\
								<div class="left"><img src="flower-demo-Dateien/Ikebana.jpg" alt="Cooking with flowers"/></div>\
								<p>September 25, 2008 Flower bonding is an art. Who wants to know how it works can try it under professional instruction in a flower workshop at Thursday, September 25, 2008 in Bonn. The workshop is free and takes place from 3 PM to 6 PM in the foyer of the Stadthaus, Berliner Platz 2 in Bonn, Germany. ...</p>\
							</div>\
						</div>',
         'col3_content': '<div class="box box_schema2">\
							<h4>Flower workshop in Bonn, Germany</h4>\
							<div class="boxbody boxbody_listentry">\
								<div class="left"><img src="flower-demo-Dateien/Ikebana.jpg" alt="Cooking with flowers"/></div>\
								<p>September 25, 2008 Flower bonding is an art. Who wants to know how it works can try it under professional instruction in a flower workshop at Thursday, September 25, 2008 in Bonn. The workshop is free and takes place from 3 PM to 6 PM in the foyer of the Stadthaus, Berliner Platz 2 in Bonn, Germany. ...</p>\
							</div>\
						</div>',
         'col2_content': '<div class="box box_schema2">\
							<h4>Flower workshop in Bonn, Germany</h4>\
							<div class="boxbody boxbody_listentry">\
								<div class="left"><img src="flower-demo-Dateien/Ikebana.jpg" alt="Cooking with flowers"/></div>\
								<p>September 25, 2008 Flower bonding is an art. Who wants to know how it works can try it under professional instruction in a flower workshop at Thursday, September 25, 2008 in Bonn. The workshop is free and takes place from 3 PM to 6 PM in the foyer of the Stadthaus, Berliner Platz 2 in Bonn, Germany. ...</p>\
							</div>\
						</div>',
         'bottom_cont': '<div class="box box_schema2">\
							<h4>Flower workshop in Bonn, Germany</h4>\
							<div class="boxbody boxbody_listentry">\
								<div class="left"><img src="flower-demo-Dateien/Ikebana.jpg" alt="Cooking with flowers"/></div>\
								<p>September 25, 2008 Flower bonding is an art. Who wants to know how it works can try it under professional instruction in a flower workshop at Thursday, September 25, 2008 in Bonn. The workshop is free and takes place from 3 PM to 6 PM in the foyer of the Stadthaus, Berliner Platz 2 in Bonn, Germany. ...</p>\
							</div>\
						</div>'
      }
   },
   'item_012': {
      'resource_id': 'item_012',
      'nav_text': 'Rose',
      'title': 'Rose',
      'file': '/demo_en/site/news/news_02.html',
      'date': '4/4/2009 5:30 PM',
      'user': 'Admin',
      'type': 'news',
      'contents': {
         'col1_content': '<div class="box box_schema3">\
							<h4>Rose</h4>\
							<div class="boxbody">\
								<p>A rose is a perennial flowering shrub or vine of the genus Rosa, within the family Rosaceae, that contains over 100 species.</p>\
							</div>\
						</div>',
         'col3_content': '<div class="box box_schema3">\
							<h4>Rose</h4>\
							<div class="boxbody">\
								<p>A rose is a perennial flowering shrub or vine of the genus Rosa, within the family Rosaceae, that contains over 100 species.</p>\
							</div>\
						</div>',
         'col2_content': '<div class="box box_schema3">\
							<h4>Rose</h4>\
							<div class="boxbody">\
								<p>A rose is a perennial flowering shrub or vine of the genus Rosa, within the family Rosaceae, that contains over 100 species.</p>\
							</div>\
						</div>',
         'bottom_cont': '<div class="box box_schema3">\
							<h4>Rose</h4>\
							<div class="boxbody">\
								<p>A rose is a perennial flowering shrub or vine of the genus Rosa, within the family Rosaceae, that contains over 100 species.</p>\
							</div>\
						</div>'
      }
   },
   'item_013': {
      'resource_id': 'item_001',
      'nav_text': '',
      'title': '',
      'file': '',
      'date': '4/4/2009 5:30 PM',
      'user': 'Admin',
      'type': 'news',
      'contents': {
         'col1_content': '',
         'col3_content': '',
         'col2_content': '',
         'bottom_cont': ''
      }
   },
   'item_014': {
      'resource_id': 'item_001',
      'nav_text': '',
      'title': '',
      'file': '',
      'date': '4/4/2009 5:30 PM',
      'user': 'Admin',
      'type': 'news',
      'contents': {
         'col1_content': '',
         'col3_content': '',
         'col2_content': '',
         'bottom_cont': ''
      }
   },
   'item_015': {
      'resource_id': 'item_001',
      'nav_text': '',
      'title': '',
      'file': '',
      'date': '4/4/2009 5:30 PM',
      'user': 'Admin',
      'type': 'news',
      'contents': {
         'col1_content': '',
         'col3_content': '',
         'col2_content': '',
         'bottom_cont': ''
      }
   }
};


var over;
var favorites = ['item_010', 'item_011', 'item_012'];
var recent = [];
var recentSize = 3;
var cancel = false;
var oldBodyMarginTop = 0;
var zIndexMap = {};
var addToRecent = function(itemId) {
   addUnique(recent, itemId, recentSize);
}
var resetRecentList = function() {
   $("#recentlist li.cms-item").remove();
   var $recentlist = $("#recent_list_items");
   for (var i = 0; i < recent.length; i++) {
      $recentlist.append(createItemFavListHtml(cms_elements_list[recent[i]]));
   }
}

var fixZIndex = function(currentId, zmap) {
   if (!$.browser.msie) 
      return;
   var z;
   for (var key in zmap) {
      if (key == currentId) {
         z = 9999;
      } else {
         z = zmap[key];
      }
      setZIndex(key, z);
   }
}

var setZIndex = function(id, z) {
   $('#' + id).css('z-index', z);
}

var removeToolbar = function() {
   $('#toolbar').remove();
   $(document.body).css('margin-top', oldBodyMarginTop + 'px');
}

var hideToolbar = function() {
   $(document.body).animate({
      marginTop: oldBodyMarginTop + 'px'
   }, 200, 'swing', function() {
      $('#show-button').show(50);
   });
   
   return false;
}


var toggleToolbar = function() {
   var button = $('#show-button');
   if (button.hasClass('toolbar_hidden')) {
      $('#toolbar').fadeIn(100);
      $(document.body).animate({
         marginTop: oldBodyMarginTop + 34 + 'px'
      }, 200, 'swing');
      button.removeClass('toolbar_hidden');
   } else {
      $(document.body).animate({
         marginTop: oldBodyMarginTop + 'px'
      }, 200, 'swing');
      $('#toolbar').fadeOut(100);
      button.addClass('toolbar_hidden');
   }
   return false;
}

var addToolbar = function() {
   addRecentList();
   var bodyEl = $(document.body).css('position', 'relative');
   oldBodyMarginTop = bodyEl.offset().top;
   var offsetLeft = bodyEl.offset().left;
   
   bodyEl.append('<div id="toolbar"><div class="ui-widget-shadow"></div><div id="toolbar_background" class="ui-widget-header"><div id="toolbar_content"><button name="Reset" title="Reset" class="left ui-state-default ui-corner-all"><span class="ui-icon cms-icon-reset"/>&nbsp;</button><button name="Edit" title="Edit" class="left ui-state-default ui-corner-all"><span class="ui-icon cms-icon-edit"/>&nbsp;</button><button name="Move" title="Move" class="left ui-state-default ui-corner-all"><span class="ui-icon cms-icon-move"/>&nbsp;</button><button name="Delete" title="Delete" class="left ui-state-default ui-corner-all"><span class="ui-icon cms-icon-delete"/>&nbsp;</button><button name="Add" title="Add" class="left wide ui-state-default ui-corner-all"><span class="ui-icon cms-icon-add" /><span class="cms-button-text">Add</span></button><button name="New" title="New" class="left wide ui-state-default ui-corner-all"><span class="ui-icon cms-icon-new" /><span class="cms-button-text">New</span></button><button name="Favorites" title="Favorites" class="left wide ui-state-default ui-corner-all"><span class="ui-icon cms-icon-favorites" /><span class="cms-button-text">Favorites</span></button><button name="Recent" title="Recent" class="left wide ui-state-default ui-corner-all"><span class="ui-icon cms-icon-recent" /><span class="cms-button-text">Recent</span></button><button name="Save" title="Save"  class="right ui-state-default ui-corner-all"><span class="ui-icon cms-icon-save"/>&nbsp;</button><button name="Sitemap" title="Sitemap" class="right ui-state-default ui-corner-all"><span class="ui-icon cms-icon-sitemap"/>&nbsp;</button><button name="Publish" title="Publish" class="right ui-state-default ui-corner-all"><span class="ui-icon cms-icon-publish"/>&nbsp;</button></div></div></div>');
   bodyEl.append('<button id="show-button" title="toggle toolbar" class="ui-state-default ui-corner-all"><span class="ui-icon cms-icon-logo"/></button>');
   $('#show-button').click(toggleToolbar);
   $('button[name="Move"]').click(toggleMove);
   $('button[name="Delete"]').click(toggleDelete);
   $('button[name="Publish"]').click(showPublishList);
   $('button[name="Favorites"]').click(function() {
      toggleList(this, "favoritelist");
   });
   $('button[name="Recent"]').click(function() {
      toggleList(this, "recentlist");
   });
   $('#toolbar button, #show-button').mouseover(function() {
      $(this).addClass('ui-state-hover');
   }).mouseout(function() {
      $(this).removeClass('ui-state-hover');
   });
   bodyEl.animate({
      marginTop: oldBodyMarginTop + 34 + 'px'
   }, 200);
   $('#publishlist').dialog({
      buttons: {
         "Cancel": function() {
            $(this).dialog("close");
         },
         "Publish": function() {
            $(this).dialog("close");
         }
      },
      width: 340,
      title: "Publish",
      modal: true,
      autoOpen: false,
      draggable: false,
      resizable: false,
      position: ['center', 20],
      close: function() {
         $('button[name="Publish"]').removeClass('ui-state-active');
      },
      zIndex: 10000
   });
   $('#publishlist span.cms-check-icon').click(function() {
      $(this).toggleClass('cms-check-icon-inactive')
   });
   initFavDialog();
};



var movePreparation = function(event) {
   $(this).unbind('mouseenter').unbind('mouseleave').addClass('cms-trigger');
   hoverOut();
   $('a.cms-move:not(.cms-trigger)').hide();
}


var moveEnd = function(event) {
   $('a.cms-move').show();
   $(this).hover(function() {
      hoverIn($(this).parent(), 2)
   }, hoverOut).removeClass('cms-trigger');
}

var toggleDelete = function(el) {
   var button = $(this);
   
   if (button.hasClass('ui-state-active')) {
      // disabling delete mode
      $('a.cms-delete').remove();
      button.removeClass('ui-state-active');
   } else {
      $('button.ui-state-active').trigger('click');
      
      // enabling delete mode
      $(deleteitems).each(function() {
         var elem = $(this).css('position', 'relative');
         $('<a class="cms-handle cms-delete"></a>').appendTo(elem).hover(function() {
            hoverIn(elem, 2)
         }, hoverOut).click(deleteItem);
      });
      button.addClass('ui-state-active');
   }
};

var toggleMove = function(el) {
   var button = $(this);
   
   
   if (button.hasClass('ui-state-active')) {
      // disabling move-mode
      $(sortlist + ', #favorite_list_items').sortable('destroy');
      var list = $('#favoritelist');
      $('li.cms-item, button', list).css('display', 'block');
      list.css('display', 'none');
      list.get(0).style.visibility = '';
      $('#favorite_list_items').get(0).style.height = '';
      resetFavList();
      $('a.cms-move').remove();
      button.removeClass('ui-state-active');
   } else {
      $('button.ui-state-active').trigger('click');
      // enabling move mode
      $(sortitems).each(function() {
         var elem = $(this).css('position', 'relative');
         $('<a class="cms-handle cms-move"></a>').appendTo(elem).hover(function() {
            hoverIn(elem, 2)
         }, hoverOut).mousedown(movePreparation).mouseup(moveEnd);
      });
      
      var list = $('#favoritelist');
      var favbutton = $('button[name="Favorites"]');
      $('li.cms-item, button', list).css('display', 'none');
      list.appendTo('#toolbar_content').css({
         top: 35,
         left: favbutton.position().left - 217,
         display: 'block',
         visibility: 'hidden'
      });
      $('#favorite_list_items').css('height', '37px');
      $('div.ui-widget-shadow', list).css({
         top: 0,
         left: -4,
         width: list.outerWidth() + 8,
         height: list.outerHeight() + 2,
         border: '0px solid',
         opacity: 0.6
      });
      
      
      
      $(sortlist).children('*:visible').css('position', 'relative');
      $(sortlist + ', #favorite_list_items').sortable({
         connectWith: sortlist + ', #favorite_list_items',
         placeholder: 'placeholder',
         dropOnEmpty: true,
         start: startAdd,
         beforeStop: beforeStopFunction,
         over: overAdd,
         out: outAdd,
         tolerance: 'pointer',
         opacity: 0.7,
         stop: stopAdd,
         cursorAt: {
            right: 10,
            top: 10
         },
         zIndex: 20000,
         handle: 'a.cms-move',
         items: sortitems,
         revert: true,
         deactivate: function(event, ui) {
            $('#favorite_list_items li').hide(200);
            $('#favoritelist').css('visibility', 'hidden');
            if ($.browser.msie) {
               setTimeout("$(sortitems).css('display','block')", 10);
            }
         }
      });
      //		list.css('display', 'none');
      
      button.addClass('ui-state-active');
   }
};

var sortmenus = "#favoritelist ul, #recentlist ul"
var menuHandles = "#favoritelist a.cms-move, #recentlist a.cms-move"
var menus = "#favoritelist, #recentlist";
var currentMenu = "favoritelist";
var currentMenuItems = "favorite_list_items";


var toggleList = function(buttonElem, newMenu) {
   var button = $(buttonElem);
   var newMenuItems = $('#' + newMenu).find("ul").attr('id');
   if (button.hasClass('ui-state-active')) {
   
      $(sortlist + ', ' + sortmenus).sortable('destroy');
      $(menuHandles).remove();
      $(menus).hide();
      button.removeClass('ui-state-active');
   } else {
      resetRecentList();
      currentMenu = newMenu;
      currentMenuItems = newMenuItems
      $('button.ui-state-active').trigger('click');
      
      // enabling move-mode
      //* current menu
      list = $('#' + currentMenu);
      $('.cms-head', list).each(function() {
         var elem = $(this);
         $('<a class="cms-handle cms-move"></a>').appendTo(elem);
      });
      list.appendTo('#toolbar_content').css({
         /*position : 'fixed',*/
         top: 35,
         left: $(buttonElem).position().left - 217
      }).slideDown(100, function() {
         $('div.ui-widget-shadow', list).css({
            top: 0,
            left: -4,
            width: list.outerWidth() + 8,
            height: list.outerHeight() + 2,
            border: '0px solid',
            opacity: 0.6
         });
      });
      $(sortlist).children('*:visible').css('position', 'relative');
      //* current menu
      $(sortlist + ', #' + currentMenuItems).sortable({
         //* current menu
         connectWith: sortlist + ', #' + currentMenuItems,
         placeholder: 'placeholder',
         dropOnEmpty: true,
         start: startAdd,
         beforeStop: beforeStopFunction,
         over: overAdd,
         out: outAdd,
         tolerance: 'pointer',
         opacity: 0.7,
         stop: stopAdd,
         cursorAt: {
            right: 15,
            top: 10
         },
         handle: 'a.cms-move',
         items: sortitems + ', li.cms-item',
         revert: 100,
         deactivate: function(event, ui) {
            $('a.cms-move', $(this)).removeClass('cms-trigger');
            if ($.browser.msie) {
               setTimeout("$(sortitems).css('display','block')", 10);
            }
         }
      });
      button.addClass('ui-state-active');
   }
};




var toggleTree = function() {
   var button = $(this);
   if (button.hasClass('ui-state-active')) {
      // disabling tree mode
      $("#tree-list").jTree.destroy();
      $("#tree-list").css('display', 'none');
      button.removeClass('ui-state-active');
   } else {
      $('button.ui-state-active').trigger('click');
      // enabling tree-mode
      $('<div></div>').appendTo(document.body).addClass('ui-widget-overlay').css({
         width: $(document).width(),
         height: $(document).height(),
         zIndex: 11000,
         opacity: 0.8
      });
      $('<a class="cms-handle cms-move"></a>').appendTo('#tree-list .cms-head');
      $('#tree-list').css('display', 'block');
      $("#tree-list").jTree({
         showHelper: true,
         hOpacity: 0.5,
         hBg: "transparent",
         hColor: "#222",
         pBorder: "none",
         pBg: "#EEE url(images/placeholder-bg.gif) no-repeat scroll 0px 0px",
         pColor: "#222",
         pHeight: "37px",
         snapBack: 1200,
         childOff: 40,
         handle: "a.cms-move",
         onDrop: treeDrop,
         beforeArea: ".cms-navtext",
         intoArea: ".cms-file-icon",
         afterArea: ".cms-title"
      
      });
      
      $("#jTreeHelper").addClass('cms-item-list');
      button.addClass('ui-state-active');
   }
};

var toggleXXX = function() {
   var button = $(this);
   if (button.hasClass('ui-state-active')) {
      // disabling move mode
      
      button.removeClass('ui-state-active');
   } else {
      $('button.ui-state-active').trigger('click');
      // enabling move-mode
      
      button.addClass('ui-state-active');
   }
};

var showPublishList = function() {
   var button = $(this);
   if (button.hasClass('ui-state-active')) {
      // disabling move mode
      
      button.removeClass('ui-state-active');
   } else {
      $('button.ui-state-active').trigger('click');
      // enabling move-mode
      $('#publishlist').dialog('open');
      button.addClass('ui-state-active');
   }
   
}

var deleteItem = function() {
   hoverOut();
   addToRecent($(this).parent().attr('rel'));
   $(this).parent().remove();
   
}





var startAdd = function(event, ui) {

   ui.self.cmsStartContainerId = ui.self.currentItem.parent().attr('id');
   //	if (ui.self.cmsStartContainerId!='favorite_list_items'){
   //		$('#favoritelist').css('display', 'block');
   //		ui.self._refreshItems(event);
   //	}
   ui.self.cmsHoverList = '#' + ui.self.cmsStartContainerId;
   ui.self.cmsCurrentContainerId = ui.self.cmsStartContainerId;
   ui.self.cmsResource_id = ui.self.currentItem.attr('rel');
   if (ui.self.cmsResource_id && cms_elements_list[ui.self.cmsResource_id]) {
      ui.self.cmsItem = cms_elements_list[ui.self.cmsResource_id];
      ui.self.cmsStartOffset = ui.placeholder.offset();
      ui.self.cmsHelpers = {};
      ui.self.cmsOrgPlaceholder = ui.placeholder.clone().insertBefore(ui.placeholder);
      ui.self.cmsOrgPlaceholder.addClass(ui.self.currentItem.attr('class')).css({
         'background-color': 'gray',
         'display': 'none',
         'height': ui.self.currentItem.height()
      });
      zIndexMap = {};
      for (container_name in ui.self.cmsItem.contents) {
         var zIndex = $('#' + container_name).css('z-index');
         zIndexMap[container_name] = zIndex;
         if (container_name != ui.self.cmsStartContainerId) {
            ui.self.cmsHoverList += ', #' + container_name;
            ui.self.cmsHelpers[container_name] = $(ui.self.cmsItem.contents[container_name]).appendTo('#' + container_name).css({
               'display': 'none',
               'position': 'absolute',
               'zIndex': ui.self.options.zIndex
            }).addClass('ui-sortable-helper');
            if (ui.self.cmsStartContainerId != currentMenuItems) {
               // if we aren't starting from the favorite list, call movePreparation on the handle
               // (hides all other handles)
               $('<a class="cms-handle cms-move"></a>').appendTo(ui.self.cmsHelpers[container_name]).mousedown(movePreparation).mouseup(moveEnd);
            } else {
               $('<a class="cms-handle cms-move"></a>').appendTo(ui.self.cmsHelpers[container_name]);
            }
            
            
         } else {
            ui.self.cmsHelpers[container_name] = ui.self.helper;
            ui.self.cmsOver = true;
         }
      }
      if (isMenuContainer(ui.self.cmsStartContainerId)) {
         ui.self.cmsHelpers[currentMenuItems] = ui.self.helper;
         var elem = $(document.createElement('div')).addClass("placeholder" + " ui-sortable-placeholder box").css('display', 'none');
         ui.placeholder.replaceWith(elem);
         ui.self.placeholder = elem;
         
         $('.cms-additional', ui.self.currentItem).hide();
         if (!$('#cms_appendbox').length) {
            $(document.body).append('<div id="cms_appendbox"></div>');
         }
         ui.self.helper.appendTo('#cms_appendbox');
         ui.self._cacheHelperProportions();
         ui.self._adjustOffsetFromHelper(ui.self.options.cursorAt);
         ui.self.refreshPositions(true);
         $('#' + ui.self.cmsStartContainerId).closest('.cms-item-list').css('display', 'none');
         ui.self.cmsOver = false;
      } else {
      
         fixZIndex(ui.self.cmsStartContainerId, zIndexMap);
         
         // show drop zone for new favorites
         var list_item = '<li class="cms-item"  rel="' + ui.self.cmsResource_id + '"><div class=" ui-widget-content"><div class="cms-head ui-state-hover"><div class="cms-navtext"><a class="left ui-icon ui-icon-triangle-1-e"></a>' + ui.self.cmsItem.nav_text + '</div><span class="cms-title">' + ui.self.cmsItem.title + '</span><span class="cms-file-icon"></span><a class="cms-handle cms-move"></a></div><div class="cms-additional"><div alt="File: ' + ui.self.cmsItem.file + '"><span class="left">File:</span>' + ui.self.cmsItem.file + '</div><div alt="Date: ' + ui.self.cmsItem.date + '"><span class="left">Date:</span>' + ui.self.cmsItem.date + '</div><div alt="User: ' + ui.self.cmsItem.user + '"><span class="left">User:</span>' + ui.self.cmsItem.user + '</div><div alt="Type: ' + ui.self.cmsItem.type + '"><span class="left">Type:</span>' + ui.self.cmsItem.type + '</div></div></div></li>';
         ui.self.cmsHelpers['favorite_list_items'] = $(list_item).appendTo('#favorite_list_items').css({
            'display': 'none',
            'position': 'absolute',
            'zIndex': ui.self.options.zIndex
         }).addClass('ui-sortable-helper');
         $('#favoritelist').css('visibility', 'visible');
      }
      ui.self.placeholder.addClass(ui.self.currentItem.attr('class')).css({
         'background-color': 'blue',
         'border': 'solid 2px black',
         'height': ui.helper.height()
      });
      $(ui.self.cmsHoverList).each(function() {
         hoverInner($(this), 2);
      });
      
   } else {
      $(sortlist).sortable('cancel');
   }
}

var beforeStopFunction = function(event, ui) {
   if (!ui.self.cmsOver) 
      cancel = true;
   else 
      cancel = false;
}

var isMenuContainer = function(id) {
   return id == 'favorite_list_items' || id == 'recent_list_items';
}

var clearAttributes = function(elem, attrs) {
   var ie = $.browser.msie;
   for (var i = 0; i < attrs.length; i++) {
      if (ie) {
         elem.removeAttribute(attrs[i]);
      } else {
         elem[attrs[i]] = '';
      }
   }
}

var stopAdd = function(event, ui) {
   fixZIndex(null, zIndexMap);
   if (cancel) {
      cancel = false;
      
      if (isMenuContainer(ui.self.cmsStartContainerId)) {
         // show favorite list again after dragging a favorite from it.
         $('#' + currentMenu).css('display', 'block');
      }
      
      $(this).sortable('cancel');
      ui.self.cmsOrgPlaceholder.remove();
      
      
   } else {
   
      if (ui.self.cmsStartContainerId == currentMenuItems) {
      
         ui.self.cmsOrgPlaceholder.replaceWith(ui.self.cmsHelpers[currentMenuItems]);
         ui.self.cmsHelpers[currentMenuItems].removeClass('ui-sortable-helper');
         clearAttributes(ui.self.cmsHelpers[currentMenuItems].get(0).style, ['width', 'height', 'top', 'left', 'position', 'opacity', 'zIndex', 'display']);
         $('a.cms-move', ui.self.currentItem).remove();
         $('button.ui-state-active').trigger('click');
      } else {
      
         if (ui.self.cmsCurrentContainerId == 'favorite_list_items') {
         
            addUnique(favorites, ui.self.cmsResource_id);
         }
         ui.self.cmsOrgPlaceholder.remove();
      }
      addToRecent(ui.self.cmsResource_id);
   }
   for (container_name in ui.self.cmsHelpers) {
   
      if (container_name != ui.self.cmsCurrentContainerId && !(ui.self.cmsStartContainerId == container_name && isMenuContainer(container_name))) {
         if (container_name == ui.self.cmsStartContainerId && ui.self.cmsCurrentContainerId == 'favorite_list_items') {
            ui.self.cmsHelpers[container_name].removeClass('ui-sortable-helper');
            // reset position (?) of helper that was dragged to favorites,
            // but don't remove it
            clearAttributes(ui.self.cmsHelpers[container_name].get(0).style, ['width', 'height', 'top', 'left', 'opacity', 'zIndex', 'display']);
            
            ui.self.cmsHelpers[container_name].get(0).style.position = 'relative';
            if ($.browser.msie) 
               ui.self.cmsHelpers[container_name].get(0).style.removeAttribute('filter');
         } else {
            // remove helper 
            
            ui.self.cmsHelpers[container_name].remove();
            
            
         }
      }
   }
   
   $(ui.self.cmsHoverList).removeClass('show-sortable');
   
   hoverOut();
   
   
   clearAttributes(ui.self.currentItem.get(0).style, ['top', 'left', 'zIndex', 'display']);
   if ($.browser.msie) {
      ui.self.currentItem.get(0).style.removeAttribute('filter');
      
      
      //ui.self.currentItem.get(0).style.removeAttribute('position');
   
   
   
   } else if (ui.self.currentItem) {
   
   
      //ui.self.currentItem.get(0).style.position='';
      ui.self.currentItem.get(0).style.opacity = '';
   }
   
   
}

/**
 * sertzsrthzs
 * @param {Event} event fff
 * @param {} ui
 */
var overAdd = function(event, ui) {

   var elem = event.target ? event.target : event.srcElement;
   var elemId = $(elem).attr('id');
   var reDoHover = !ui.self.cmsOver;
   if (ui.self.cmsStartContainerId != elemId && ui.self.cmsStartContainerId != 'favorite_list_items' && ui.self.cmsStartContainerId != 'recent_list_items') {
      // show pacelholder in start container if dragging over a different container, but not from favorites or recent
      ui.self.cmsOrgPlaceholder.css({
         'display': 'block',
         'border': 'dotted 2px black'
      });
   } else {
      // hide placeholder (otherwise both the gray and blue boxes would be shown)
      ui.self.cmsOrgPlaceholder.css('display', 'none');
   }
   if (ui.self.cmsHelpers[elemId]) {
      fixZIndex(elemId, zIndexMap);
      ui.placeholder.css('display', 'block');
      ui.self.cmsOver = true;
      if (elemId != ui.self.cmsCurrentContainerId) {
      
         ui.self.cmsCurrentContainerId = elemId;
         
         reDoHover = true;
         // hide dragged helper, display helper for container instead
         ui.self.helper.css('display', 'none');
         ui.self.helper = ui.self.cmsHelpers[elemId].css('display', 'block');
         ui.self.currentItem = ui.self.cmsHelpers[elemId];
         ui.self.helper.width(ui.placeholder.width());
         ui.self.helper.height('auto');
         
         ui.self._cacheHelperProportions();
         ui.self._adjustOffsetFromHelper(ui.self.options.cursorAt);
         ui.self.refreshPositions(true);
         
         
      }
      
      ui.placeholder.height(ui.self.helper.height());
   } else {
      ui.placeholder.css('display', 'none');
      ui.self.cmsOver = false;
   }
   if (elemId == 'favorite_list_items' && ui.placeholder.parent().attr('id') != elemId) 
      ui.placeholder.appendTo(elem);
   
   
   
   
   if (reDoHover) {
      hoverOut();
      $(ui.self.cmsHoverList).each(function() {
         hoverInner($(this), 2);
      });
   }
   
   
   
}



var outAdd = function(event, ui) {
   if (ui.self.helper) {
      if (ui.self.cmsStartContainerId != ui.self.cmsCurrentContainerId) {
         ui.self.cmsCurrentContainerId = ui.self.cmsStartContainerId;
         fixZIndex(ui.self.cmsStartContainerId, zIndexMap);
         ui.self.helper.css('display', 'none');
         ui.self.helper = ui.self.cmsHelpers[ui.self.cmsCurrentContainerId].css('display', 'block');
         ui.self.currentItem = ui.self.cmsHelpers[ui.self.cmsCurrentContainerId];
         ui.self._cacheHelperProportions();
         ui.self._adjustOffsetFromHelper(ui.self.options.cursorAt);
         ui.self.refreshPositions(true);
      }
      ui.placeholder.css('display', 'none');
      if (ui.self.cmsStartContainerId != 'favorite_list_items') {
         ui.self.cmsOrgPlaceholder.css({
            'display': 'block',
            'border': 'solid 2px black'
         });
      }
      ui.self.cmsOver = false;
      hoverOut();
      $(ui.self.cmsHoverList).each(function() {
         hoverInner($(this), 2);
      });
   }
   
   
}


var hoverIn = function(elem, hOff) {

   var position = getElementPosition(elem);
   var tHeight = elem.outerHeight();
   var tWidth = elem.outerWidth();
   var hWidth = 2;
   var lrHeight = tHeight + 2 * (hOff + hWidth);
   var btWidth = tWidth + 2 * (hOff + hWidth);
   var tlrTop = position.top - (hOff + hWidth);
   var tblLeft = position.left - (hOff + hWidth);
   // top
   $('<div class="hovering hovering-top"></div>').height(hWidth).width(btWidth).css('top', tlrTop).css('left', tblLeft).appendTo(document.body);
   
   // right
   $('<div class="hovering hovering-right"></div>').height(lrHeight).width(hWidth).css('top', tlrTop).css('left', position.left + tWidth + hOff).appendTo(document.body);
   // left
   $('<div class="hovering hovering-left"></div>').height(lrHeight).width(hWidth).css('top', tlrTop).css('left', tblLeft).appendTo(document.body);
   // bottom
   $('<div class="hovering hovering-bottom"></div>').height(hWidth).width(btWidth).css('top', position.top + tHeight + hOff).css('left', tblLeft).appendTo(document.body);
   
   
   
}

var hoverInner = function(elem, hOff) {

   var position = {
      left: 'x',
      top: 'x'
   };
   var bottom = 'x';
   var right = 'x';
   
   $(elem.children('*:visible')).each(function() {
      var el = $(this);
      if (!el.hasClass('ui-sortable-helper')) {
         var pos = getElementPosition(el);
         position.left = (position.left == 'x' || pos.left < position.left) ? pos.left : position.left;
         position.top = (position.top == 'x' || pos.top < position.top) ? pos.top : position.top;
         bottom = (bottom == 'x' || bottom < (pos.top + el.outerHeight())) ? pos.top + el.outerHeight() : bottom;
         right = (right == 'x' || right < (pos.left + el.outerWidth())) ? pos.left + el.outerWidth() : right;
      }
   });
   var tHeight = bottom - position.top;
   var tWidth = right - position.left;
   var elemPos = getElementPosition(elem);
   
   if (bottom == 'x') {
      tHeight = 25;
      tWidth = elem.innerWidth();
      position = elemPos;
   }
   
   var hWidth = 2;
   
   var inner = {
      top: position.top - (elemPos.top + hOff),
      left: position.left - (elemPos.left + hOff),
      height: tHeight + 2 * hOff,
      width: tWidth + 2 * hOff
   };
   // inner
   $('<div class="show-sortable" style="position: absolute; z-index:0; top: ' + inner.top + 'px; left: ' + inner.left + 'px; height: ' + inner.height + 'px; width: ' + inner.width + 'px;"></div>').prependTo(elem);
   
   
   // top
   $('<div class="hovering hovering-top"></div>').height(hWidth).width(tWidth + 2 * (hOff + hWidth)).css('top', position.top - (hOff + hWidth)).css('left', position.left - (hOff + hWidth)).appendTo(document.body);
   // right
   $('<div class="hovering hovering-right"></div>').height(tHeight + 2 * (hOff + hWidth)).width(hWidth).css('top', position.top - (hOff + hWidth)).css('left', position.left + tWidth + hOff).appendTo(document.body);
   // left
   $('<div class="hovering hovering-left"></div>').height(tHeight + 2 * (hOff + hWidth)).width(hWidth).css('top', position.top - (hOff + hWidth)).css('left', position.left - (hOff + hWidth)).appendTo(document.body);
   // bottom
   $('<div class="hovering hovering-bottom"></div>').height(hWidth).width(tWidth + 2 * (hOff + hWidth)).css('top', position.top + tHeight + hOff).css('left', position.left - (hOff + hWidth)).appendTo(document.body);
   
}
var hoverOut = function() {
   $('div.hovering, div.show-sortable').remove();
   
}




var getElementPosition = function(elem) {
   var position = {
      left: 0,
      top: 0
   };
   var offset = elem.offset();
   if ($(document.body).css('position') == 'relative' || $(document.body).css('position') == 'absolute') {
      position.left = offset.left - $(document.body).offset().left;
      position.top = offset.top - $(document.body).offset().top;
   } else {
      position.left = offset.left;
      position.top = offset.top;
   }
   return position;
}
var serialize = function() {


   var ser = {
      'container': []
   };
   $(sortlist).each(function(i) {
      ser.container[i] = {
         'id': $(this).attr('id'),
         'elements': []
      };
      $(this).children().each(function(ie, elem) {
         ser.container[i].elements[ie] = {
            'id': $(elem).attr('id'),
            'index': ie
         }
      });
   });
   $('body').append('<p>' + JSON.stringify(ser) + '</p>');
   
};

var getSerializeString = function() {
   var ser = {
      'container': []
   };
   $(sortlist).each(function(i) {
      ser.container[i] = {
         'id': $(this).attr('id'),
         'elements': []
      };
      $(this).children().each(function(ie, elem) {
         ser.container[i].elements[ie] = {
            'id': $(elem).attr('id'),
            'index': ie
         }
      });
   });
   return JSON.stringify(ser);
}



var collapse = function() {
   $(this).closest('li').toggleClass('cms-collapsed');
};

var treeDrop = function(event) {
   var elem = event.target ? event.target : event.srcElement;
   $('ul:empty', elem).remove();
   $('li a.cms-collapse-icon').each(function() {
   
      if (1 != $(this).closest('li').children('ul').length) {
         this.parentNode.removeChild(this);
      }
   });
   
   $('a.cms-collapse-icon', elem).unbind('click', collapse);
   $('li.last', elem).removeClass('last');
   $('ul', elem).andSelf().each(function() {
      $(this).children('li').filter(':last').addClass('last');
   });
   
   $('li:has(ul)', elem).children('div.ui-widget-content:not(:has(a.cms-collapse-icon))').append('<a class="cms-collapse-icon"></a>');
   
   
   $('a.cms-collapse-icon', elem).click(collapse);
   
   //	alert('done');

   //	$('li:not(:has(ul))', elem).find('a.cms-collapse-icon').each(function(){
   //		this.parentNode.removeChild(this);
   //	});


};

var treeDrop_ = function() {
   var elem = 'ul.cms-item-list';
   $('li.last', elem).removeClass('last');
   $('ul', elem).each(function() {
      $(this).children('li').filter(':last').addClass('last')
   });
   var collapseIcon = $('<a class="cms-collapse-icon"></a>').click(function() {
      $(this).closest('li').toggleClass('cms-collapsed');
   });
   $('li:has(ul:has(li))', elem).children('div.ui-widget-content:not(:has(a.cms-collapse-icon))').append(collapseIcon);
   
   
};


var clickFavDeleteIcon = function() {
   var button = $(this);
   var toRemove = button.parent().parent();
   toRemove.remove();
}

var arrayToString = function(arr) {
   return "[" + arr.join(", ") + "]";
}

var saveFavorites = function() {
   var newFavs = [];
   $("#fav-dialog li.cms-item").each(function() {
      var resource_id = this.getAttribute("rel");
      addUnique(newFavs, resource_id);
   });
   favorites = newFavs;
   resetFavList();
   
}

var favEditOK = function() {
   $(this).dialog("close");
   saveFavorites();
}

var favEditCancel = function() {
   $(this).dialog("close");
}

var createItemFavDialogHtml = function(item) {
   var html = ['<li class="cms-item" rel="', item.resource_id, '">\
		<div class="left">\
			<span class="cms-delete-icon" ></span>\
		</div>\
		<div class="left ui-widget-content">\
			<div class="cms-head ui-state-hover">\
				<div class="cms-navtext"><a class="left ui-icon ui-icon-triangle-1-e"></a>', item.nav_text, '</div>\
				<span class="cms-title">', item.title, '</span>\
				<span class="cms-file-icon"></span>\
				<a class="cms-move cms-handle"></a>\
			</div>\
			<div class="cms-additional">\
				<div alt="File:', item.file, '"><span class="left">File:</span>', item.file, '</div>\
				<div alt="Date:', item.date, '"><span class="left">Date:</span>', item.date, '</div>\
				<div alt="User:', item.user, '"><span class="left">User:</span>', item.user, '</div>\
				<div alt="Type:', item.type, '"><span class="left">Type:</span>', item.type, '</div>\
			</div>\
		</div>\
		<br clear="all" />\
	</li>'];
   return html.join('');
}
var createItemFavListHtml = function(item) {
   var html = ['<li class="cms-item"  rel="', item.resource_id, '">\
			<div class=" ui-widget-content">\
				<div class="cms-head ui-state-hover">\
					<div class="cms-navtext"><a class="left ui-icon ui-icon-triangle-1-e"></a>', item.nav_text, '</div>\
					<span class="cms-title">', item.title, '</span>\
					<span class="cms-file-icon"></span>\
				</div>\
				<div class="cms-additional">\
					<div alt="File: ', item.file, '"><span class="left">File:</span>', item.file, '</div>\
					<div alt="Date: ', item.date, '"><span class="left">Date:</span>', item.date, '</div>\
					<div alt="User: ', item.user, '"><span class="left">User:</span>', item.user, '</div>\
					<div alt="Type: ', item.type, '"><span class="left">Type:</span>', item.type, '</div>\
				</div>\
			</div>\
			\
		</li>'];
   return html.join('');
}



var initFavDialog = function() {
   $("#fav-edit").click(showFavDialog);
   var buttons = {
      "Cancel": favEditCancel,
      "OK": favEditOK
   
   };
   $('#fav-dialog').dialog({
      width: 340,
      //height: 500,
      title: "Edit favorites",
      modal: true,
      autoOpen: false,
      draggable: true,
      resizable: false,
      position: ['center', 20],
      close: function() {
         $('#fav-edit').removeClass('ui-state-active');
      },
      buttons: buttons,
      zIndex: 10000
   });
   
   
}

var initFavDialogItems = function() {
   $("#fav-dialog ul").remove();
   $("#fav-dialog").append("<ul></ul>")
   var html = []
   for (var i = 0; i < favorites.length; i++) {
      html.push(createItemFavDialogHtml(cms_elements_list[favorites[i]]));
   }
   $("#fav-dialog ul").append(html.join(''));
   $("#fav-dialog .cms-delete-icon").click(clickFavDeleteIcon);
   $("#fav-dialog ul").sortable();
   //$("#fav-dialog a.ui-icon").click(function() {clickTriangle(this);});
   $('#fav-dialog div.cms-additional div').jHelperTip({
      trigger: 'hover',
      source: 'attribute',
      attrName: 'alt',
      topOff: -30,
      opacity: 0.8
   });
}


var showFavDialog = function() {
   var button = $(this);
   $("#fav-dialog li").show(); // Make "deleted" items show up again
   if (button.hasClass("ui-state-active")) {
      button.removeClass("ui-state-active");
   } else {
      $('button.ui-state-active').trigger('click');
      // enabling move-mode
      initFavDialogItems();
      $('#fav-dialog').dialog('open');
      button.addClass('ui-state-active');
   }
}

var clickTriangle = function(triangle) {
   var elem = $(triangle);
   if (elem.hasClass('ui-icon-triangle-1-e')) {
      elem.removeClass('ui-icon-triangle-1-e').addClass('ui-icon-triangle-1-s');
      elem.parents('.ui-widget-content').children('.cms-additional').show(5, function() {
         var list = $(this).parents('div.cms-item-list');
         $('div.ui-widget-shadow', list).css({
            height: list.outerHeight() + 2
         });
      });
   } else {
      elem.removeClass('ui-icon-triangle-1-s').addClass('ui-icon-triangle-1-e');
      elem.parents('.ui-widget-content').children('.cms-additional').hide(5, function() {
         var list = $(this).parents('div.cms-item-list');
         $('div.ui-widget-shadow', list).css({
            height: list.outerHeight() + 2
         });
      });
   }
   return false;
}

var resetFavList = function() {
   $("#favoritelist li.cms-item").remove();
   var $favlist = $("#favoritelist ul");
   for (var i = 0; i < favorites.length; i++) {
      $favlist.append(createItemFavListHtml(cms_elements_list[favorites[i]]))
   }
   //$("#favoritelist a.ui-icon").click(function() {clickTriangle(this)});


}


var addToList = function(resource_id, list, max_size) {
   var newList = [resource_id];
   for (var i = 0; i < list.length; i++) {
      if (resource_id != list[i]) 
         newList.push(list[i]);
      if (max_size && newList.length >= max_size) 
         break;
   }
   return newList;
}

var addUnique = function(list, item, maxlen) {
   for (var i = 0; i < list.length; i++) {
      if (list[i] == item) {
         list.splice(i, 1);
         break;
      }
   }
   list.splice(0, 0, item);
   if (maxlen) {
      var delLength = list.length - maxlen;
      if (delLength > 0) 
         list.splice(maxlen, delLength);
   }
}

var addRecentList = function() {
   $('body').append('\
		<div id="recentlist" class="cms-item-list">\
		<div class="connect ui-corner-top"></div>\
		<div class="ui-widget-shadow ui-corner-all"></div>\
		<div class="ui-widget-content ui-corner-bottom ui-corner-tl">\
			<ul id="recent_list_items">\
			</ul>\
		</div>	\
	</div>');
}
