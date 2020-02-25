package frontend

import io.circe._
import io.circe.syntax._
import org.scalajs.dom.document
import sttp.client._
import sttp.model.{MediaType, Uri}
import org.scalajs.dom

import scala.concurrent.Future

object HttpClient {
  implicit val backend: SttpBackend[Future, Nothing, NothingT] = FetchBackend()

  final val csrfTokenName = "Csrf-Token"

  def maybeCsrfToken: Option[String] =
    dom.document.cookie
      .split(";")
      .map(_.trim)
      .find(_.startsWith(s"$csrfTokenName="))
      .map(_.drop(csrfTokenName.length + 1))

  def boilerplate: RequestT[Empty, Either[String, String], Nothing] =
    basicRequest.header("Csrf-Token", maybeCsrfToken.getOrElse("none"))

  def host: Uri = Uri.parse(document.location.origin.toString).right.get

  def path(s: String, ss: String*): Uri = host.path("api", s, ss: _*)

  implicit def bodySerializer[A](implicit aEncoder: Encoder[A]): A => BasicRequestBody =
    (a: A) =>
      StringBody(
        a.asJson.noSpaces,
        "utf-8",
        Some(MediaType.ApplicationJson)
      )
}
