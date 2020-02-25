package utils

import models._
import slick.lifted.Tag
import slick.jdbc.PostgresProfile.api._

// TODO delete this
// final class SharedModelTable(tag: Tag) extends Table[SharedModel](tag, "shared_model") {
//   def foo = column[String]("foo", O.PrimaryKey)
//   def bar = column[Int]("bar")
//
//   def * = (foo, bar) <> (SharedModel.tupled, SharedModel.unapply)
// }
//
// object SharedModelTable {
//   def query: TableQuery[SharedModelTable] = TableQuery[SharedModelTable]
// }

final class BookmarkTable(tag: Tag) extends Table[Bookmark](tag, "shared_model") {
  def id = column[Int]("id", O.PrimaryKey)
  def obj_id = column[Int]("obj_id")
  def obj_type = column[String]("obj_type")
  def tag_name = column[String]("tag_name")

  def * = (id, obj_id, obj_type, tag_name) <> (Bookmark.tupled, Bookmark.unapply)
}

object BookmarkTable {
  def query: TableQuery[BookmarkTable] = TableQuery[BookmarkTable]
}
