	<div id="categories">
		<div id="categoryfolders"><div class="head"><%= wp.key(Messages.GUI_GALLERY_CATEGORIES_HL_AVAILABLE_0) %></div><div id="categoryfolderlist"></div></div>
		<div id="categoryitems">
			<div id="categorybuttons">
				<button id="opencategorybutton" onclick="showCategoryFolders();" title="<%= wp.key(Messages.GUI_GALLERY_CATEGORIES_BUTTON_SHOW_0) %>">
					<div> <%= wp.key(Messages.GUI_GALLERY_CATEGORIES_BUTTON_SHOW_0) %> </div>
				</button>
				<button id="categorysearchbutton" onclick="openSearchDialog('category');" title="<%= wp.key(Messages.GUI_GALLERY_BUTTON_SEARCH_0) %>">
					<div> &nbsp; </div>
				</button>
				<button id="categoryresetsearchbutton" onclick="resetSearch('category');" title="<%= wp.key(Messages.GUI_GALLERY_BUTTON_SEARCH_RESET_0) %>">
					<div> &nbsp; </div>
				</button>
			</div>
			<div id="categoryitemlist"></div>
			<div id="categoryiteminfo">
				<table cellspacing="0" cellpadding="0" border="0">
					<tr>
						<td style="width: 59%;" colspan="2" class="iteminfoheadline">
							<%= wp.key(Messages.GUI_GALLERY_ITEMDETAIL_HEADLINE_0) %>
							<button id="categoryitemselectbutton" onclick="setActiveItem(categoryItems.markedItem, 'category', false);" type="button" title="<%= wp.key(Messages.GUI_GALLERY_ITEMDETAIL_SELECT_0) %>">
								<img src="<%= org.opencms.workplace.CmsWorkplace.getSkinUri() %>buttons/apply.png" />
							</button>
							<button id="categoryitempublishbutton" onclick="publishItem(categoryItems.markedItem, 'category', false);" type="button" title="<%= wp.key(Messages.GUI_GALLERY_ITEMDETAIL_PUBLISH_0) %>">
								<img src="<%= org.opencms.workplace.CmsWorkplace.getSkinUri() %>buttons/publish.png" />
							</button>
							<button id="categoryitemdeletebutton" onclick="deleteItem(categoryItems.markedItem, 'category');" type="button" title="<%= wp.key(Messages.GUI_GALLERY_ITEMDETAIL_DELETE_0) %>">							
								<img src="<%= org.opencms.workplace.CmsWorkplace.getSkinUri() %>buttons/deletecontent.png" />
							</button>
						</td>
						<td style="width: 40%;" colspan="2" class="iteminfostate">
							<span id="categoryitemstate"></span>
						</td>
					</tr>
					<tr>
						<td style="width: 14%;" class="categoryiteminfotitle">
							<%= wp.key(Messages.GUI_GALLERY_ITEMDETAIL_TITLE_0) %>
						</td>
						<td style="width: 85%;" class="categoryiteminfotitle" colspan="3"><div id="categoryitemtitle"></div></td>
					</tr>					
					<tr>
						<td style="width: 14%;">
							<%= wp.key(Messages.GUI_GALLERY_ITEMDETAIL_NAME_0) %>
						</td>
						<td id="categoryitemname" style="width: 45%;"></td>
						<% if (wp.isModeEditor()) { %>
						<td id="linktargetname" style="width: 14%;">
							<%= wp.key(Messages.GUI_GALLERY_ITEMDETAIL_TARGET_0) %>
						</td>
						<td style="width: 26%">
							<select name="linktarget" id="categorylinktarget" size="1" >
  								<option value="_self"><%= wp.key(Messages.GUI_INPUT_LINKTARGETSELF_0) %></option>
  								<option value="_blank"><%= wp.key(Messages.GUI_INPUT_LINKTARGETBLANK_0) %></option>
	  							<option value="_top"><%= wp.key(Messages.GUI_INPUT_LINKTARGETTOP_0) %></option>
							</select>
						</td>
						<% } else { %>
							<td>
								<%= wp.key(Messages.GUI_GALLERY_ITEMDETAIL_DATE_MODIFIED_0) %>
							</td>
							<td id="categoryitemdm">&nbsp;</td>
							<td>&nbsp;</td>
							<td>&nbsp;</td>
						<% } %>
					</tr>
				</table>
				<div id="dialogbuttons">
					<button id="categoryokbutton" type="button" onclick="setActiveItem(categoryItems.markedItem, 'category', false);"><%= wp.key(Messages.GUI_GALLERY_BUTTON_OK_0) %></button>
					<button type="button" id="categorycancelbutton" onclick="window.close();"><%= wp.key(Messages.GUI_GALLERY_BUTTON_CANCEL_0) %></button>
				</div>
			</div>
		</div>
	</div>