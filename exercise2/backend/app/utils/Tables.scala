package utils

import models._
import slick.lifted.Tag
import slick.jdbc.PostgresProfile.api._

final class BookmarkTable(tag: Tag) extends Table[Bookmark](tag, "bookmarks") {
  def objId = column[Int]("obj_id")
  def objType = column[String]("obj_type")
  def tagName = column[String]("tag_name")

  def * = (objId, objType, tagName) <> (Bookmark.tupled, Bookmark.unapply)
}

final class ContentTagTable(tag: Tag) extends Table[ContentTag](tag, "tags") {
  def name = column[String]("name", O.PrimaryKey)

  def * = (name) <> (ContentTag.apply, ContentTag.unapply)
}
