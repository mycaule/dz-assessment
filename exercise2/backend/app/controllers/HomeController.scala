package controllers

import javax.inject._
import play.api.Configuration
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.http.HttpErrorHandler
import play.api.mvc._
import utils.WriteableImplicits._
import io.circe.generic.auto._
import models._
import utils._
import utils.ReadsImplicits._
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext

@Singleton
final class HomeController @Inject()(
  assets: Assets,
  errorHandler: HttpErrorHandler,
  config: Configuration,
  protected val dbConfigProvider: DatabaseConfigProvider,
  cc: ControllerComponents
)(implicit val ec: ExecutionContext) extends AbstractController(cc) with HasDatabaseConfigProvider[JdbcProfile] {

  def index: Action[AnyContent] = assets.at("index.html")

  def assetOrDefault(resource: String): Action[AnyContent] = {
    if (resource.startsWith(config.get[String]("apiPrefix")))
      Action.async(r => errorHandler.onClientError(r, NOT_FOUND, "Not found"))
    else if (resource.contains("."))
      assets.at(resource)
    else
      index
  }

  def hello(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok("Backend is up!")
  }

  def helloNbr(nbr: Int): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(s"You gave me $nbr")
  }

  private def insertBookmarks(objId: Int, objType: String): Action[Seq[String]] = Action(parse.json[Seq[String]]).async {
    implicit request: Request[Seq[String]] => {
      val tagNames = request.body
      val tags = tagNames.map(ContentTag(_))
      val bookmarks = tagNames.map(Bookmark(objId, objType, _))

      // Try to update the unique list of tags
      // eg. INSERT INTO tags (name) VALUES ('dance'), ('electronic');
      // db.run(TableQuery[ContentTagTable] ++= tags)

      // eg. INSERT INTO bookmarks (obj_id, obj_type, tag_name)
      //     VALUES (302127, 'album', 'dance'), (302127, 'album', 'electronic');
      db.run(TableQuery[BookmarkTable] ++= bookmarks).map(Ok(_))
    }
  }

  def setTrackTags(objId: Int): Action[Seq[String]] = insertBookmarks(objId, "track")
  def setAlbumTags(objId: Int): Action[Seq[String]] = insertBookmarks(objId, "album")
  def setArtistTags(objId: Int): Action[Seq[String]] = insertBookmarks(objId, "artist")

  private def getBookmarks(tags: Seq[String], objType: String): Action[AnyContent] = Action.async {
    // eg. SELECT DISTINCT obj_id FROM bookmarks WHERE obj_type = 'track' AND tag_name IN ('dance', 'electronic');
    val query = TableQuery[BookmarkTable]
      .filter(x => x.objType === objType)
      .filter(x => tags.map(x.tagName === _).reduceLeft(_ && _))
      .map(_.objId).distinct

    db.run(query.result).map(Ok(_))
  }

  def getTracks(tags: Seq[String]): Action[AnyContent] = getBookmarks(tags, "track")
  def getAlbums(tags: Seq[String]): Action[AnyContent] = getBookmarks(tags, "album")
  def getArtists(tags: Seq[String]): Action[AnyContent] = getBookmarks(tags, "artist")

  def export(): Action[AnyContent] = Action.async {
    // eg. SELECT obj_type, obj_id, array_agg(tag_name) FROM bookmarks GROUP BY (obj_id, obj_type);
    val query = TableQuery[BookmarkTable]
    db.run(query.result).map(l => {
      val y = l.map(x => (x.objId, x.objType, x.tagName))
        .groupBy(x => (x._1, x._2)).mapValues(_.map(_._3))
        .toSeq
      Ok(y)
    })
  }
}
