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
     * @raw.hint           Raw SMTP message string
	 * @subject.hint       Subject of the email
	 * @bodyHtml.hint      HTML body of the email, if supplied (empty string if not)
	 * @bodyText.hint      Plain text body of the email, if supplied (empty string if not)
	 * @recipients.hint    Array of recipient addresses
	 * @replyTo.hint       Array of reply to addresses
	 * @headers.hint       Structure of SMTP headers sent with the message
	 * @recipientList.hint Structure of recipients as results from the accept() method. Struct keys are the recipient address, values are the result set in your accept() method.
	 * @messageId.hint     MessageID set by the sending server
	 * @size.hint          Size, in bytes, of the message
	 * @contentType.hint   Content type of the message
	 * @sentDate.hint      Date that the message was sent
	 * @uniqueId.hint      Local unique ID of the message
	 * @userName.hint      Username used to authenticate the SMTP request
     */
    public function deliver(
    	  required string raw
		, required string subject
		, required string bodyHtml
		, required string bodyText
		, required array  recipients
		, required array  replyTo
		, required struct headers
		, required struct recipientList
		, required string messageId
		, required string size
		, required string contentType
		, required string sentDate
		, required string uniqueId
		, required string userName
    ) {

        log text="#variables.id# -deliver- #arguments.data.toString()#" file=variables.logfile;
    }

}