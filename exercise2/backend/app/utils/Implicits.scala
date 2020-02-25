package utils

import io.circe._
import io.circe.parser._
import io.circe.syntax._
import play.api.libs.json.{JsError, JsSuccess, JsValue, Reads}
import play.api.http.{ContentTypeOf, ContentTypes, Writeable}
import play.api.libs.json.Json
import play.api.mvc.Codec

object ReadsImplicits {
  implicit def reads[A](implicit decoder: Decoder[A]): Reads[A] =
    (json: JsValue) =>
      decode[A](json.toString) match {
        case Right(value) => JsSuccess(value)
        case Left(error)  => JsError(error.getMessage)
      }
}

trait WriteableImplicits {
  implicit def jsonWritable[A](implicit writes: Encoder[A], codec: Codec): Writeable[A] = {
    implicit val contentType: ContentTypeOf[A] = ContentTypeOf[A](Some(ContentTypes.JSON))
    val transform = Writeable.writeableOf_JsValue.transform compose (Json
      .parse(_: String)) compose ((_: A).asJson.noSpaces)
    Writeable(transform)
  }
}

object WriteableImplicits extends WriteableImplicits
