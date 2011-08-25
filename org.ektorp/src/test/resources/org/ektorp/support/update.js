function(doc, req)
{
  if(doc.name == req.query.name) {
    return [doc, "updated"];
  }

  return [null,"not updated"];
}