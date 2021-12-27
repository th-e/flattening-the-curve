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
      span("virus free :)"),
      div(
        cls("header"),
        "Version: ",
        child.text <-- version.signal.map(_.toString),
        onMountSet(ctx => {
          runtime.unsafeRunAsync_ {
            client.version.tap { receivedVersionString =>
              {
                version.update(_ => receivedVersionString.toString)
                UIO.unit
              }
            }
          }
          color := "white"
        })
      )
    )
}
