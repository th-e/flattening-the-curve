package webtuples

import org.scalajs.dom.window

object Config {
  val isLocalHost: Boolean = window.location.host.startsWith("localhost")

  val webSocketsUrl: String =
    if (isLocalHost) "ws://localhost:8088/ws"
    else "ws://webtuples.de:8088/ws"
}
