function(head, req) {
  var row, userConf, configHeader, configFoot;
  configHeader = renderTopOfApacheConf(head, req.query.hostname);
  send(configHeader);
  while (row = getRow()) {
	    var userConf = renderUserConf(row);
	    send(userConf)
	  }
  configFoot = renderConfTail();
  return configFoot;
}