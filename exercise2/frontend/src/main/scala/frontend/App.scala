package frontend

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.html
import HttpClient._
import org.scalajs.dom
import sttp.client._
import io.circe.generic.auto._
import io.circe.parser.decode
import models._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.util.{Random, Success, Try}

object App {
  private val css = AppCSS
  dom.console.log(css.asInstanceOf[js.Object])

  val insertBus: EventBus[Int] = new EventBus()

  val objectId: Var[Int] = Var(3135557)
  val objectType: Var[String] = Var("track")
  val tagToPost: Var[String] = Var("rock")

  val returned: EventStream[String] = insertBus.events.flatMap(
    nbr => EventStream.fromFuture(
      boilerplate.response(asStringAlways)
        .post(path("hello").param("nbr", nbr.toString))
        .send().map(_.body)
    ))

  def randomBookmark(): Bookmark = Bookmark(Random.nextInt(), "track", "dance")

  def apply(): ReactiveHtmlElement[html.Div] = div(
    className := "App",
    h1("Exercise 2"),
    section(
      h2("Backend status"),
      p(child <-- EventStream.fromFuture(boilerplate.response(asStringAlways).get(path("hello")).send().map(_.body)))
    ),
    section(
      h2("Insert bookmark"),
      input(
        value <-- objectId.signal.map(_.toString),
        inContext(
          thisElem =>
            onInput.mapTo(Try(thisElem.ref.value.toInt)).collect { case Success(id) => id } --> objectId.writer
        )
      ),
      input(
        value <-- objectType.signal.map(_.toString),
        inContext(
          thisElem =>
            onInput.mapTo(Try(thisElem.ref.value)).collect { case Success(str) => str } --> objectType.writer
        )
      ),
      button("Insert", onClick.mapTo(objectId.now) --> insertBus.writer),
      br(),
      span("Returned: ", child <-- returned.map(identity[String]))
    ),
    section(
      h2("Insert Random"),
      p(button(onClick --> (_ => boilerplate.response(ignore)
          .post(path("track", "42")).body(randomBookmark())
          .send()
        ), "Insert random element")
      ), {
        val exportBus = new EventBus[Boolean]()

        p(
          button("Export all the tagged objects", onClick.mapTo(true) --> exportBus.writer),
          ul(
            children <-- exportBus.events.flatMap(
              _ =>
                EventStream.fromFuture(
                  boilerplate.get(path("export"))
                    .response(asStringAlways.map(decode[List[Bookmark]]))
                    .send().map(_.body.getOrElse(Nil)))
                .map(_.map(_.toString).map(li(_)))
            )
          )
        )
      }
    )
  )
}
