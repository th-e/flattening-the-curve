package webtuples

import com.raquo.laminar.api.L._
import webtuples.protocol.{BackendService}
import zio._
import zio.app.DeriveClient
import animus._

object Frontend {
  val runtime = Runtime.default
  val client  = DeriveClient.gen[BackendService]

  val version = Var("")

  def view: Div =
    div(
      h3("Flattening the curve"),
      span(".... unrelated to any viruses :)"),
      div(
        cls("header"),
        "Version: ",
        child.text <-- EventStream.fromFuture(Runtime.default.unsafeRunToFuture(client.version))
      )
    )
}
