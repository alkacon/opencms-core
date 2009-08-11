(function(cms) {

   var item_001 = {
      /* resource id (or special id for new items), also used as HTML id if the item isn't a subcontainer. */
      id: 'item_001',
      
      navText: 'XML',
      title: 'XML based contents',
      file: 'xml_contents.xml',
      date: '4/4/2009 5:30 PM',
      user: 'Admin',
      type: 'news',
      
      /* can the current user move the item? */
      allowMove: true,
      
      /* can the current user edit the item? */
      allowEdit: true,
      
      /* is this item locked by another user? */
      locked: false,
      
      /* changed, unchanged or new */
      status: 'unchanged',
      
      /* list of contained item ids if this is a subcontainer, else null */
      subItems: null,
      
      /* map container type to HTML for this item/container type combination, but only for container types 
       * which are compatible with this item
       */
      contents: {
         'column': '<div class="box box_schema2" rel="item_001"><h4>XML based contents</h4><div class="boxbody"><p>In this section, you find additinal examples for xml based contents (news and events) demonstrating how to manage structured contents.</p><p>&nbsp;</p><p>These contents are displayed in various list boxes, where the selection and the ordering of the contents shown inside is done by a so-called "collector". At this page, for example, the collector provides the two most-recent news or events, only.</p></div></div>'
      
      }
   };
   
   var item_002 = {
      id: item_002,
      navText: 'Direct edit',
      title: 'Direct edit',
      file: 'direct_edit.xml',
      date: '4/4/2009 5:30 PM',
      user: 'Admin',
      type: 'news',
      allowMove: true,
      allowEdit: true,
      locked: false,
      status: 'changed',
      subItems: null,
      contents: {
         'column': '<div class="box box_schema2" rel="item_002"><h4>Direct edit</h4><div class="boxbody"><p>By utilizing the direct edit feature, you can create new or edit already existing XML contents.</p></div></div>'
      }
   };
   var item_003 = {
      id: 'item_003',
      navText: 'Login',
      title: 'Login box',
      file: 'login.xml',
      date: '4/4/2009 5:30 PM',
      user: 'Admin',
      type: 'news',
      allowMove: true,
      allowEdit: true,
      locked: false,
      status: 'unchanged',
      subItems: null,
      contents: {
         'column': '<div class="box box_schema4" rel="item_003"><h4>Login</h4><div class="boxbody"><p><b>Logged in as:</b></p><form method="get" action="/opencms/opencms/demo_en/extra/login.html" class="loginform"><div class="boxform">(Admin)</div><div class="boxform"><input name="action" value="logoff" type="hidden"/><input name="requestedResource" value="/demo_en/today/index.html" type="hidden"/><input class="button" value="Logoff" type="submit"/></div></form></div></div>',
         'bottom': '<div class="box box_schema4" rel="item_003"><h4>Login</h4><div class="boxbody"><p><b>Logged in as:</b></p><form method="get" action="/opencms/opencms/demo_en/extra/login.html" class="loginform"><div class="boxform">(Admin)</div><div class="boxform"><input name="action" value="logoff" type="hidden"/><input name="requestedResource" value="/demo_en/today/index.html" type="hidden"/><input class="button" value="Logoff" type="submit"/></div></form></div></div>'
      
      }
   };
   
   var item_004 = {
      id: 'item_004',
      navText: 'Login',
      title: 'Login description',
      file: 'login_description.xml',
      date: '4/4/2009 5:30 PM',
      user: 'Admin',
      type: 'news',
      allowMove: true,
      allowEdit: true,
      locked: false,
      status: 'unchanged',
      subItems: null,
      contents: {
         'bottom': '<div class="box box_schema4" rel="item_004"><h4>Login</h4><div class="boxbody"><p>Via the login page or the login box, you can enter your credentials in order to access the protected section (extranet).</p><p>&nbsp;</p><p>Furthermore, since this login switches to the "Offline Project" (a working copy of all contents of the site), the user can edit the contents using the "direct edit" buttons.</p><p>&nbsp;</p><p>If you are already logged in, you can logout here.</p></div></div>'
      }
   };
   
   var col1_content = {
      /* id for the container, also used as a HTML id */
      id: 'col1_content',
      
      /* type of the container */
      type: 'column',
      
      /* list of IDs of elements in the container */
      elements: ['item_001', 'item_002'],
      
      /* maximum number of elements or null if there is no maximum number */
      maxElements: null
   }
   
   var col2_content = {
      id: 'col2_content',
      type: 'column',
      elements: ['item_002', 'item_003'],
      maxElements: null
   }
   
   var col3_content = {
      id: 'col3_content',
      type: 'column',
      elements: ['item_003', 'subcont_001'],
      maxElements: null
   }
   
   var bottom_cont = {
      id: 'bottom_cont',
      type: 'bottom',
      elements: ['item_004', 'item_001'],
      maxElements: 42
   }
   
//   var containers = cms.data.containers = {
//      'col1_content': col1_content,
//      'col2_content': col2_content,
//      'col3_content': col3_content,
//      'bottom_cont': bottom_cont
//   }
   
   var elements = cms.data.elements = {
      'item_001': item_001,
      'item_002': item_002,
      'item_003': item_003,
      'item_004': item_004
   }
   
   
   
   
   
   
   
   
   
   var cms_elements_list_x = cms.data.cms_elements_list_x = {
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
	christmastree vendors.</p></div></div></div></div>',
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
	christmastree vendors.</p></div></div></div></div>',
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
   
   var UNCHANGED = 'u';
   var CHANGED = 'c';
   var NEW = 'n';
   
   var elements = cms.data.elements = {
      'item_001': {
         'id': 'item_001',
         'navText': 'XML',
         'title': 'XML based contents',
         'file': 'xml_contents.xml',
         'date': '4/4/2009 5:30 PM',
         'user': 'Admin',
         'type': 'news',
         
         'contents': {
            'column': '<div class="box box_schema2" rel="item_001"><h4>XML based contents</h4><div class="boxbody"><p>In this section, you find additinal examples for xml based contents (news and events) demonstrating how to manage structured contents.</p><p>&nbsp;</p><p>These contents are displayed in various list boxes, where the selection and the ordering of the contents shown inside is done by a so-called "collector". At this page, for example, the collector provides the two most-recent news or events, only.</p></div></div>'
         },
         subItems: null,
         allowMove: true,
         allowEdit: true,
         locked: false,
         status: UNCHANGED
         
         
      },
      
      'item_002': {
         'id': 'item_002',
         'navText': 'Direct edit',
         'title': 'Direct edit',
         'file': 'direct_edit.xml',
         'date': '4/4/2009 5:30 PM',
         'user': 'Admin',
         'type': 'news',
         'contents': {
            'column': '<div class="box box_schema2" rel="item_002"><h4>Direct edit</h4><div class="boxbody"><p>By utilizing the direct edit feature, you can create new or edit already existing XML contents.</p></div></div>'
         },
         subItems: null,
         allowMove: true,
         allowEdit: true,
         locked: false,
         status: UNCHANGED
      },
      'item_003': {
         'id': 'item_003',
         'navText': 'Login',
         'title': 'Login box',
         'file': 'login.xml',
         'date': '4/4/2009 5:30 PM',
         'user': 'Admin',
         'type': 'news',
         'contents': {
            'column': '<div class="box box_schema4" rel="item_003"><h4>Login</h4><div class="boxbody"><p><b>Logged in as:</b></p><form method="get" action="/opencms/opencms/demo_en/extra/login.html" class="loginform"><div class="boxform">(Admin)</div><div class="boxform"><input name="action" value="logoff" type="hidden"/><input name="requestedResource" value="/demo_en/today/index.html" type="hidden"/><input class="button" value="Logoff" type="submit"/></div></form></div></div>'
         },
         subItems: null,
         allowMove: true,
         allowEdit: true,
         locked: false,
         status: UNCHANGED
      },
      'item_004': {
         'id': 'item_004',
         'navText': 'Login',
         'title': 'Login description',
         'file': 'login_description.xml',
         'date': '4/4/2009 5:30 PM',
         'user': 'Admin',
         'type': 'news',
         'contents': {
            'column': '<div class="box box_schema4" rel="item_004"><h4>Login</h4><div class="boxbody"><p>Via the login page or the login box, you can enter your credentials in order to access the protected section (extranet).</p><p>&nbsp;</p><p>Furthermore, since this login switches to the "Offline Project" (a working copy of all contents of the site), the user can edit the contents using the "direct edit" buttons.</p><p>&nbsp;</p><p>If you are already logged in, you can logout here.</p></div></div>'
         
         },
         subItems: null,
         allowMove: true,
         allowEdit: true,
         locked: false,
         status: UNCHANGED
      },
      'item_005': {
         'id': 'item_005',
         'navText': 'Photo album',
         'title': 'Photo album',
         'file': 'photo_album.xml',
         'date': '4/4/2009 5:30 PM',
         'user': 'Admin',
         'type': 'news',
         'contents': {
            'column': '<div class="box box_schema4" rel="item_005"><h4>Photo album</h4><div class="boxbody boxbody_listentry"><div class="left"><img src="flower-demo-Dateien/Workshop.jpg" alt="Cooking with flowers"/></div><p>A photo album renders overview pages showing images of a distinct folder. By clicking on them, the album renders the selected image.</p></div></div>'
         },
         subItems: null,
         allowMove: true,
         allowEdit: true,
         locked: false,
         status: UNCHANGED
      },
      
      'item_006': {
         'id': 'item_006',
         'navText': 'News',
         'title': 'Recent news',
         'file': 'news_list.xml',
         'date': '4/4/2009 5:30 PM',
         'user': 'Admin',
         'type': 'news_list',
         'contents': {
            'column': '<div class="box box_schema3" rel="item_006"><h4>Recent news</h4><div class="boxbody"><div class="boxbody_listentry"><h5><a href="http://localhost:8080/opencms/opencms/demo_en/today/news/news_0005.html">Cooking with flowers</a></h5><p><small>Jan 5, 2009</small></p>\
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
         },
         subItems: null,
         allowMove: true,
         allowEdit: true,
         locked: false,
         status: UNCHANGED
      },
      'item_007': {
         id: 'item_007',
         navText: 'Flower of the year',
         title: 'Flower of the year 2009',
         file: 'flower_of_year.xml',
         date: '4/4/2009 5:30 PM',
         user: 'Admin',
         type: 'news',
         contents: {
            'bottom': '<div class="box box_schema3" rel="item_007"><h4>Flower of the year 2009</h4><div class="boxbody boxbody_listentry"><div class="left"><img src="flower-demo-Dateien/Wegwarte.jpg" alt="Wegwarte" /></div><div><p>The flower of the year 2009 is the common chicory. Common chicory (Cichorium intybus) is a bushy perennial herb with blue, lavender, or occasionally white flowers.</p></div></div></div>'
         },
         subItems: null,
         allowMove: true,
         allowEdit: true,
         locked: false,
         status: UNCHANGED
      },
      'item_008': {
         'id': 'item_008',
         'navText': 'Flowerservice',
         'title': 'New Flowerservice!',
         'file': 'flowerservice.xml',
         'date': '4/4/2009 5:30 PM',
         'user': 'Admin',
         'type': 'news',
         'contents': {
            'bottom': '<div class="box box_schema3" rel="item_008"><h4>New Flowerservice!</h4><div class="boxbody boxbody_listentry"><div class="left"><img src="flower-demo-Dateien/GartenII.jpg" alt="Wegwarte" /></div><div><p>Berlin - In the hope of a bloomy summer trade lots of German nurseries offer their customers an All- Inclusive Package. Beside an extensive client counselling...</p></div></div></div>'
         },
         subItems: null,
         allowMove: true,
         allowEdit: true,
         locked: false,
         status: UNCHANGED
      },
      'item_009': {
         'id': 'item_009',
         'navText': 'Ikebana',
         'title': 'Ikebana - flowers a gateway to life',
         'file': 'ikebana.xml',
         'date': '4/4/2009 5:30 PM',
         'user': 'Admin',
         'type': 'news',
         'contents': {
            'bottom': '<div class="box box_schema3" rel="item_009"><h4>Ikebana - flowers a gateway to life</h4><div class="boxbody"><p>For interested women and men who want to open their view on the harmony\
	and beauty of flowers.\
	The event takes place at Missionshaus Hofstetten.\
	Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam\
	nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat,\
	sed diam voluptua. ...</p></div></div>'
         },
         subItems: null,
         allowMove: true,
         allowEdit: true,
         locked: false,
         status: UNCHANGED
      },
      'item_010': {
         'id': 'item_010',
         'navText': 'Differences in Botany',
         'title': 'Differences in Botany',
         'file': '/demo_en/site/news/news_01.html',
         'date': '4/4/2009 5:30 PM',
         'user': 'Admin',
         'type': 'news',
         'contents': {
            'bottom': '<div class="box box_schema4"  rel="item_010"><h4>Differences in Botany</h4><div class="boxbody"><p>Plants are living organisms belonging to the kingdom Plantae. They include familiar organisms such as trees, herbs, bushes, grasses, vines, ferns, mosses, and green algae. About 350,000 species of plants, defined as seed plants, bryophytes, ferns and fern allies, are estimated to exist currently.</p></div></div>',
            'column': '<div class="box box_schema4"  rel="item_010"><h4>Differences in Botany</h4><div class="boxbody"><p>Plants are living organisms belonging to the kingdom Plantae. They include familiar organisms such as trees, herbs, bushes, grasses, vines, ferns, mosses, and green algae. About 350,000 species of plants, defined as seed plants, bryophytes, ferns and fern allies, are estimated to exist currently.</p></div></div>'
         },
         subItems: null,
         allowMove: true,
         allowEdit: true,
         locked: false,
         status: UNCHANGED
      },
      'item_011': {
         'id': 'item_011',
         'navText': 'Flower workshop',
         'title': 'Flower workshop in Bonn, Germany',
         'file': '/demo_en/site/events/event_01.html',
         'date': '4/4/2009 5:30 PM',
         'user': 'Admin',
         'type': 'event',
         'contents': {
            'column': '<div class="box box_schema2">\
								<h4>Flower workshop in Bonn, Germany</h4>\
								<div class="boxbody boxbody_listentry">\
									<div class="left"><img src="flower-demo-Dateien/Ikebana.jpg" alt="Cooking with flowers"/></div>\
									<p>September 25, 2008 Flower bonding is an art. Who wants to know how it works can try it under professional instruction in a flower workshop at Thursday, September 25, 2008 in Bonn. The workshop is free and takes place from 3 PM to 6 PM in the foyer of the Stadthaus, Berliner Platz 2 in Bonn, Germany. ...</p>\
								</div>\
							</div>',
            'bottom': '<div class="box box_schema2">\
								<h4>Flower workshop in Bonn, Germany</h4>\
								<div class="boxbody boxbody_listentry">\
									<div class="left"><img src="flower-demo-Dateien/Ikebana.jpg" alt="Cooking with flowers"/></div>\
									<p>September 25, 2008 Flower bonding is an art. Who wants to know how it works can try it under professional instruction in a flower workshop at Thursday, September 25, 2008 in Bonn. The workshop is free and takes place from 3 PM to 6 PM in the foyer of the Stadthaus, Berliner Platz 2 in Bonn, Germany. ...</p>\
								</div>\
							</div>'
         },
         subItems: null,
         allowMove: true,
         allowEdit: true,
         locked: false,
         status: UNCHANGED
      },
      
      'item_012': {
         'id': 'item_012',
         'navText': 'Rose',
         'title': 'Rose',
         'file': '/demo_en/site/news/news_02.html',
         'date': '4/4/2009 5:30 PM',
         'user': 'Admin',
         'type': 'news',
         'contents': {
            'column': '<div class="box box_schema3">\
								<h4>Rose</h4>\
								<div class="boxbody">\
									<p>A rose is a perennial flowering shrub or vine of the genus Rosa, within the family Rosaceae, that contains over 100 species.</p>\
								</div>\
							</div>',
            'bottom': '<div class="box box_schema3">\
								<h4>Rose</h4>\
								<div class="boxbody">\
									<p>A rose is a perennial flowering shrub or vine of the genus Rosa, within the family Rosaceae, that contains over 100 species.</p>\
								</div>\
							</div>'
         },
         subItems: null,
         allowMove: true,
         allowEdit: true,
         locked: false,
         status: UNCHANGED
      },
      
      'item_013': {
         'id': 'item_001',
         'navText': '',
         'title': '',
         'file': '',
         'date': '4/4/2009 5:30 PM',
         'user': 'Admin',
         'type': 'news',
         'contents': {
            'column': '',
            'bottom': ''
         },
         subItems: null,
         allowMove: true,
         allowEdit: true,
         locked: false,
         status: UNCHANGED
      },
      'subcont_001' : {
          id: 'subcont_001',
          navText: 'Subcontainer',
          title: 'Subcontainer',
          file: 'login.xml',
          date: '4/4/2009 5:30 PM',
          user: 'Admin',
          type: 'subcontainer',
          allowMove: true,
          allowEdit: true,
          locked: false,
          status: 'unchanged',
          subItems: ['item_003', 'item_002'],
          contents: {
             'column': '<div class="box box_schema4" rel="item_003"><h4>Login</h4><div class="boxbody"><p><b>Logged in as:</b></p><form method="get" action="/opencms/opencms/demo_en/extra/login.html" class="loginform"><div class="boxform">(Admin)</div><div class="boxform"><input name="action" value="logoff" type="hidden"/><input name="requestedResource" value="/demo_en/today/index.html" type="hidden"/><input class="button" value="Logoff" type="submit"/></div></form></div></div><div class="box box_schema2" rel="item_002"><h4>Direct edit</h4><div class="boxbody"><p>By utilizing the direct edit feature, you can create new or edit already existing XML contents.</p></div></div>'
          }
       },
       'subcont_002' : {
          id: 'subcont_002',
          navText: 'Subcontainer',
          title: 'Subcontainer',
          file: 'login.xml',
          date: '4/4/2009 5:30 PM',
          user: 'Admin',
          type: 'subcontainer',
          allowMove: true,
          allowEdit: true,
          locked: false,
          status: 'unchanged',
          subItems: ['item_008', 'item_009'],
          contents: {
             'bottom': '<div class="box box_schema4" rel="item_003"><h4>Login</h4><div class="boxbody"><p><b>Logged in as:</b></p><form method="get" action="/opencms/opencms/demo_en/extra/login.html" class="loginform"><div class="boxform">(Admin)</div><div class="boxform"><input name="action" value="logoff" type="hidden"/><input name="requestedResource" value="/demo_en/today/index.html" type="hidden"/><input class="button" value="Logoff" type="submit"/></div></form></div></div><div class="box box_schema2" rel="item_002"><h4>Direct edit</h4><div class="boxbody"><p>By utilizing the direct edit feature, you can create new or edit already existing XML contents.</p></div></div>'
          }
       }
       
   
   };
   
   var containers = cms.data.containers = {
       col1_content: {
           type: 'column',
           id: 'col1_content',
           elements: ['item_001', 'item_002'],
           maxElem: null
       },
       
       col2_content: {
           type: 'column',
           id: 'col2_content',
           elements: ['item_003', 'item_004'],
           maxElem: null
       },
       
       col3_content: {
           type: 'column',
           elements:['item_005', 'subcont_001'],
           id:'col3_content',
           maxElem: null
       },
       
       bottom_cont: {
           type: 'bottom',
           elements:['subcont_002', 'item_010'],
           id: 'bottom_cont',
           maxElem: null
           
       }
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
   };
   
   var fillContainers = cms.data.fillContainers = function() {
       for (var containerName in containers) {
           $('#'+containerName+' > *').remove();
           var elementIds = containers[containerName].elements;
           for (var i = 0; i<elementIds.length; i++) {
               var elem = elements[elementIds[i]];
               var html='';
               var isSubcontainer=false;
               if (elem.subItems) {
                   
                   isSubcontainer=true;
                   html = $('<div class="cms-subcontainer"></div>');
                   for (var j = 0; j < elem.subItems.length; j++) {
                       var subElem = elements[elem.subItems[j]];
                       $(subElem.contents[containers[containerName].type]).attr('rel', subElem.id).addClass('cms-element').appendTo(html);
                   }
               } else {
                   html = $(elem.contents[containers[containerName].type]);
               }
               html.attr('rel', elem.id).addClass('cms-element');
               $('#'+containerName).append(html);
               if (isSubcontainer){
                   
                   var floatDirection = html.children('*:first').css('float');
                   if (floatDirection && (/left|right/).test(floatDirection)){
                       var dimensions=cms.util.getInnerDimensions(html, 0);
                       var addMargin;
                       if (floatDirection=='left'){
                           addMargin=parseFloat(html.children('*:first').css('margin-left')) + parseFloat(html.children('*:last').css('margin-right'));
                       }else{
                           addMargin=parseFloat(html.children('*:first').css('margin-right')) + parseFloat(html.children('*:last').css('margin-left'));
                       }
                       html.attr('title', addMargin);
                       html.children('*:visible').each(function(){
                           $(this).width($(this).width());
                       });
                       html.width(dimensions.width + addMargin).addClass('cms-'+floatDirection);
                   }
               }
           }
       }
       
   }
   
   var DATA_URL = "data.txt";
   var AJAX_TIMEOUT = 5000;
   var RECENT_URL = "recent.txt";
   var FAVORITE_URL = "fav.txt";
   var ITEM_URL = "item.json";
   var FAV_SAVE_URL = "sdfafsd";
   var RECENT_SAVE_URL = "fgsdfsdfad";
   var CONTAINER_SAVE_URL = "fsdafafasfd";
   
   var JSON_PARSE_ERROR = "ERROR: Couldn't parse JSON data";
 
   var persistFavorites = cms.data.persistFavorites = function() {
       // dummy
   }
   
   var persistRecent = cms.data.persistRecent = function() {
      // dummy
   }
   
   var loadAllData = cms.data.loadAllData = function(afterLoad) {
       $.ajax({
           url: DATA_URL,
           timeout: AJAX_TIMEOUT,
           error:    function(xhr, status, error) { alert("ERROR: couldn't load data from server"); },
           success: function(data) {
               try {
                   var jsonData = JSON.parse(data);
               } catch (e) {
                   alert("ERROR: Couldn't parse JSON data");
                   return;
               }
               if (jsonData.favorites) {
                   favorites = cms.toolbar.favorites = jsonData.favorites;
               }
               if (jsonData.recent) {
                   recent = cms.toolbar.recent = jsonData.recent;
               }
               if (jsonData.containers) {
                   containers = cms.data.containers = jsonData.containers;
               }
               if (jsonData.elements) {
                   elements = cms.data.elements = jsonData.elements;
               }
               afterLoad();
           }
       })
   }
  
   var loadJSON = cms.data.loadJSON = function(url, data, afterLoad) {
      $.ajax({
         url: url,
         timeout: AJAX_TIMEOUT,
         data: data,
         error: function(xhr, status, error) {
            alert("ERROR: couldn't load data from server");
         },
         success: function(data) {
            try {
               var jsonData = JSON.parse(data);
            } catch (e) {
               alert(JSON_PARSE_ERROR);
               return;
            }
            afterLoad(jsonData);
         }
      });
   }
   
   var postJSON = cms.data.postJSON = function(url, data, afterPost) {
       $.ajax({
           type: 'POST',
           timeout: AJAX_TIMEOUT,
           data: data,
           error: function(xhr, status, error) {
               alert("ERROR: couldn't send data to server");
           },
           success: afterPost
       });
   }
 
   
   var reloadItem = cms.data.reloadItem = function(id) {
      loadJSON(ITEM_URL, {
         url: window.location.href,
         id: id
      }, function(data) {
         cms.data.elements[data.id] = data;
         fillContainers();
      });
   }
   
   var loadFavorites = cms.data.loadFavorites = function(afterFavoritesLoad) {
      loadJSON(FAVORITES_URL, {
         url: window.location.href,
         id: id
      }, function(data) {
         cms.toolbar.favorites = data;
         afterFavoritesLoad();
         
      });
   }

   var loadRecent = cms.data.loadRecent = function(afterRecentLoad) {
      loadJSON(RECENT_URL, {
         url: window.location.href,
         id: id
      }, function(data) {
         cms.toolbar.recent = data;
         afterRecentLoad();
      });
   }
        
   var persistContainers = cms.data.persistContainers = function(afterSave) {
       postJSON(CONTAINER_SAVE_URL, {
           'url':window.location.href, 
           'containers': JSON.stringify(cms.data.containers)
           }, function() {});
   }
   
   var persistFavorites = cms.data.persistFavorites = function () {
       //postJSON(FAV_SAVE_URL, {'favorites': JSON.stringify(cms.toolbar.favorites)}, function() {});
   }
   
   var persistRecent = cms.data.persistRecent = function() {
       //postJSON(RECENT_SAVE_URL, {'recent': JSON.stringify(cms.toolbar.recent)}, function() {});
   }
  
  
   
})(cms);
