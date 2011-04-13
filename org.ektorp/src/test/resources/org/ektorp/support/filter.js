function(doc, req)
{
  if(doc.name == req.query.name) {
    return true;
  }

  return false;
}