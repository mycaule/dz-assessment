package models

// TODO delete
// final case class SharedModel(foo: String, bar: Int)
final case class Tag(name: String)
final case class Bookmark(id: Int, obj_id: Int, obj_type: String, tag_name: String)
