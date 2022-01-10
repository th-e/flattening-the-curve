package webtuples

import com.raquo.laminar.api.L._
import webtuples.protocol._
import zio._
import zio.app.DeriveClient
import animus._
import _root_.boopickle.Default._
import _root_.webtuples.State._
import io.laminext.websocket._
import io.laminext.websocket.boopickle.WebSocketReceiveBuilderBooPickleOps
import scala.concurrent.duration.DurationInt

//view state
object State {
  val slideStateVar: Var[SlideState]               = Var(SlideState.empty)
  val userIdVar: Var[Option[UserId]]               = Var(Option.empty[UserId])
  val visitorStatisticsVar: Var[VisitorStatistics] = Var(VisitorStatistics(1))

}

object Frontend {
  val runtime = Runtime.default
  val client  = DeriveClient.gen[BackendService]

  val ws: WebSocket[BackendCommand, UserCommand] =
    WebSocket
      .url(Config.webSocketsUrl)
      .pickle[BackendCommand, UserCommand]
      .build(reconnectRetries = Int.MaxValue, reconnectDelay = 3.seconds)

  val version = Var("")

  lazy val $connectionStatus = ws.isConnected
    .combineWithFn(ws.isConnecting) {
      case (true, _) => "CONNECTED"
      case (_, true) => "CONNECTING"
      case _         => { println(s"RECEIVED"); "OFFLINE" }
    }

  def renderConnectionStatus: Div = div(
    fontSize("14px"),
    opacity(0.7),
    div(
      lineHeight("1.5"),
      display.flex,
      children <-- $connectionStatus.splitOneTransition(identity) { (_, string, _, transition) =>
        div(string, transition.width, transition.opacity)
      },
      overflowY.hidden,
      height <-- EventStream
        .merge(
          $connectionStatus.changes.debounce(5000).mapTo(false),
          $connectionStatus.changes.mapTo(true)
        )
        .toSignal(false)
        .map { if (_) 20.0 else 0.0 }
        .spring
        .px
    )
  )

  def renderVisitorStatistics: Div = div(
    opacity <-- Animation.from(0).wait(1000).to(1).run,
    lineHeight("1.5"),
    display.flex,
    height("40px"),
    fontSize("14px"),
    div(s"VISITOR ${nbsp}", opacity(0.7)),
    AnimatedCount(visitorStatisticsVar.signal.map(_.connectedUsers))
  )

  def renderFirstSlide: Div = div(
    h3("Flattening the curve"),
    span(".... unrelated to any viruses :)")
  )

  def renderVersionNumber: Div = div(
    cls("header"),
    "Version: ",
    child.text <-- EventStream.fromFuture(Runtime.default.unsafeRunToFuture(client.version))
  )

  def view: Div =
    div(
      ws.connect,
      ws.connected --> { _ =>
        ws.sendOne(UserCommand.Subscribe)
      },
      ws.received --> { command =>
        println(s"RECEIVED COMMAND: $command")
        command match {
          case BackendCommand.SendSlideState(slideState)               => slideStateVar.set(slideState)
          case BackendCommand.SendUserId(id)                           => userIdVar.set(Some(id))
          case BackendCommand.SendVisitorStatistics(visitorStatistics) => visitorStatisticsVar.set(visitorStatistics)
        }
      },
      renderConnectionStatus,
      renderVisitorStatistics,
      renderVersionNumber,
      renderFirstSlide
    )
}
