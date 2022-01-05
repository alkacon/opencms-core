group searchsetting_configs;

disptitle(name) ::= <<

<searchsettings>
	<searchsetting element="$name$">
		<solrfield targetfield="disptitle" sourcefield="*_sort" />
	</searchsetting>
</searchsettings>


>>

disporder(name) ::= <<

<searchsettings>
	<searchsetting element="$name$" searchcontent="false">
		<solrfield targetfield="disporder" sourcefield="*_i" default="0" />
	</searchsetting>
</searchsettings>

>>

instancedate(name) ::= <<

<searchsettings>
	<searchsetting element="$name$" searchcontent="false">
		<solrfield targetfield="instancedate" sourcefield="*_dt" />
		<solrfield targetfield="instancedatecurrenttill" sourcefield="*_dt" />
    </searchsetting>
</searchsettings>

>>

geocoords(name) ::= <<

<searchsettings>
	<searchsetting element="$name$">
		<solrfield targetfield="geocoords" sourcefield="*_loc" default="0.000000,0.000000" locale="">
		    <mapping type="dynamic" class="org.opencms.search.fields.CmsSearchFieldMappingGeoCoords">$name$</mapping>
		</solrfield>
	</searchsetting>
</searchsettings>

>>

listtitle(name) ::= <<$disptitle(name)$>>
listorder(name) ::= <<$disporder(name)$>>
listdate(name) ::= <<$instancedate(name)$>>
listgeocoords(name) ::= <<$geocoords(name)$>>
