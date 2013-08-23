component	{
	this.name = hash( getCurrentTemplatePath() )& gettickcount();
    ///setting showdebugoutput="no";
    
	request.baseURL="http://#cgi.HTTP_HOST##GetDirectoryFromPath(cgi.SCRIPT_NAME)#";
	request.currentPath=GetDirectoryFromPath(getCurrentTemplatePath());
	
	// if(server.railo.version LT '4.1.0.000') throw message="not supported in this version";

} 