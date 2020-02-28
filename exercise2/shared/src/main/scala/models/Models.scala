package models

case class ContentType(name: String)
case class ContentTag(name: String)
case class Bookmark(objId: Int, objType: String, tagName: String)
