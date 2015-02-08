component {

    variables.id        = "SmtpGatewayListener-#getTickCount()#";
    variables.logfile   = "SmtpGateway";

    /**
     * Called by the SMTP Event Gateway when authenticating SMTP requests
     * 
     * To fail authentication, set result.authenticated to false. You may
     * also optionally set a failure message using result.message.
     *
     */
    public void function authenticate( string username, string password, struct result ) output=false {
        result.authenticated = ( username == "bob" && password = "bob" );
        if ( !result.authenticated ) {
            result.message = "Authentication failed for user [#arguments.user#]";
        }
    }


    /**
     * Called by the SMTP Event Gateway once for each recipient. Will only be called if a call to 'authenticate' is first successful.
     * 
     * To reject a message, set the key in result.Reject to true, and optionally set result.Message and result.Code.
     * All calls to this method must be rejected in order to reject a message. If one call is accepted then the 
     * message is processed.
     * 
     * The username parameter is the username used in SMTP authentication (see the authenticate method, above).
     */
    public function accept( String from, String to, String ipAddress, Struct result, string uniqueId, string username  ) {

        //* example: to reject messages where to contains the substring 'Unkown'
        if ( arguments.to CT "Unknown" ) {

            result.Reject  = true;                                              // required to Reject

            result.Message = "Rejected [#to#] because it contains 'Unknown'";   // optional
            result.Code    = 530;                                               // optional
        }   //*/

        log text="#variables.id# -accept( #arguments.toString()# )" file=variables.logfile;
    }


    /**
     * Called by the smtp event gateway after authentication has passed, all the recipients have
     * been processed and at least one combination of from/to/ipAddress was accepted by the
     * accept() method.
     * 
     * Data provides the following keys with more info: 
     * 
     *   ContentType
     *   MessageId
     *   Recipients
     *   ReplyTo
     *   SentDate
     *   Size
     *   Subject
     *   MimeMessage (a javax.mail.internet.MimeMessage object which can be used to retrieve info about the message)
     * 
     * The username parameter is the username used in SMTP authentication (see the authenticate method, above).
     */
    public function deliver( Struct data, string uniqueId, string username  ) {

        log text="#variables.id# -deliver- #arguments.data.toString()#" file=variables.logfile;
    }

}