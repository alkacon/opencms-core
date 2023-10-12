group searchsetting_configs;

disptitle(name) ::= <<

<searchsettings>
    <searchsetting element="$name$">
        <solrfield targetfield="disptitle" sourcefield="*_sort">
            <mapping type="item" class="org.opencms.search.fields.CmsMacroResolvingSearchFieldMapping"/>
        </solrfield>
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

listtitle(name) ::= <<$disptitle(name)$>>
listorder(name) ::= <<$disporder(name)$>>
listdate(name) ::= <<$instancedate(name)$>>
