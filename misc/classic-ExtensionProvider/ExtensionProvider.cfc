<cfcomponent>
	<cffunction name="getInfo" access="remote" returntype="struct" output="no">
    	<cfset var info=struct()>
        
        <cfset info.title="Lucee Association Switzerland">
        <cfset info.description="This is the offical Extension Provider from the Lucee Association Switzerland (www.lucee.org)">
        <cfset info.url="http://"&cgi.HTTP_HOST>
        <cfset info.mode="develop">
        
    	<cfreturn info>
    </cffunction>
    
    
    
	<cffunction name="listApplications" access="remote" returntype="query" output="no">
    	<cfset var apps=queryNew('type,id,name,label,description,version,category,image,download,paypal,author,codename,video,support,documentation,forum,mailinglist,network,created')>
        
        <cfdirectory action="list" directory="ext" name="local.dir" type="directory">
        
        
        <cfset var baseURL="http://#cgi.HTTP_HOST#/">
        <cfloop query="#dir#">
            <cfset var doc=evaluate(fileRead(dir.directory&"/"&dir.name&"/doc.json"))>
            
            <cfset QueryAddRow(apps)>
            <cfset QuerySetCell(apps,'id',doc.id)>
            <cfset QuerySetCell(apps,'name',doc.name)>
            <cfset QuerySetCell(apps,'type',doc.type)>
            <cfset QuerySetCell(apps,'label',doc.label)>
            <cfset QuerySetCell(apps,'description',doc.description)>
            <cfset QuerySetCell(apps,'author',doc.author)>
            <cfset QuerySetCell(apps,'codename',doc.codename)>
            <cfset QuerySetCell(apps,'created',parseDateTime(doc.builtDate))>
            <cfset QuerySetCell(apps,'version',doc.version)>
            <cfset QuerySetCell(apps,'category',doc.category)>
            <cfset QuerySetCell(apps,'image',baseURL&'ext/'&dir.name&'/logo.png')>
            <cfset QuerySetCell(apps,'download',baseURL&'ext/'&dir.name&'/extension.zip')>

        </cfloop>

		
        <cfreturn apps>
    </cffunction>






</cfcomponent>
