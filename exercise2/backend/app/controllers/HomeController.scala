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
    if (resource.startsWith(config.get[String]("apiPrefix"))) {
      Action.async(r => errorHandler.onClientError(r, NOT_FOUND, "Not found"))
    } else {
      if (resource.contains(".")) assets.at(resource) else index
    }
  }

  def hello(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok("Backend is up!")
  }

  def helloNbr(nbr: Int): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(s"You gave me $nbr")
  }

  // TODO remove
  // def sharedModels: Action[AnyContent] = Action.async {
  //   db.run(SharedModelTable.query.result).map(Ok(_))
  // }

  private def insertBookmark(objId: Int, objType: String, tags: Seq[String]): Action[Bookmark] = Action(parse.json[Bookmark]).async {
    // INSERT INTO tags (name) VALUES
    //   ('dance'),
    //   ('electronic');

    // INSERT INTO bookmarks (obj_id, obj_type, tag_name)
    // VALUES
    //   (302127, 'album', 'dance'),
    //   (302127, 'album', 'electronic');

    implicit request: Request[Bookmark] =>
      db.run(BookmarkTable.query += request.body).map(Ok(_))
  }

  def setTrackTags(objId: Int): Action[Bookmark] = insertBookmark(objId, "track", Seq[String]())
  def setAlbumTags(objId: Int): Action[Bookmark] = insertBookmark(objId, "album", Seq[String]())
  def setArtistTags(objId: Int): Action[Bookmark] = insertBookmark(objId, "artist", Seq[String]())

  def getTracks(tags: Seq[String]): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    // > select distinct obj_id from bookmarks where obj_type = 'track' and tag_name in ('dance', 'electronic');
    //  obj_id
    // ---------
    //  3135556
    // (1 row)
    Ok(s"Hello $tags")
  }

  def getAlbums(tags: Seq[String]): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    // > select distinct obj_id from bookmarks where obj_type = 'album' and tag_name in ('dance', 'electronic');
    //  obj_id
    // ---------
    //   302127
    // (1 row)
    Ok(s"Hello $tags")
  }

  def getArtists(tags: Seq[String]): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    // > select distinct obj_id from bookmarks where obj_type = 'artist' and tag_name in ('dance', 'electronic');
    //  obj_id
    // ---------
    //       27
    // (1 row)

    Ok(s"Hello $tags")
  }

  def export(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    // > select obj_type, obj_id, array_agg(tag_name) from bookmarks group by (obj_id, obj_type);
    //  obj_type | obj_id  |           array_agg
    // ----------+---------+--------------------------------
    //  track    | 3135556 | {electronic}
    //  artist   |      27 | {house,electronic,dance,disco}
    //  album    |  302127 | {dance,electronic}
    // (3 rows)
    Ok("Hello from play!")
  }
}
