package webtuples

import webtuples.protocol.{BackendService}
import zio._
import zio.app._
import zio.console._
import zio.magic._

object Backend extends App {
  private val httpApp =
    DeriveRoutes.gen[BackendService]

  val program =
    for {
      port <- system.envOrElse("PORT", "8088").map(_.toInt).orElseSucceed(8088)
      _    <- zhttp.service.Server.start(port, httpApp)
    } yield ()

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    val serviceLayer = (ZLayer.identity[Console] ++ Config.live) >>> ExampleServiceLive.layer
    program
      .injectCustom(serviceLayer)
      .exitCode
  }
}

case class ExampleServiceLive(config: Config) extends BackendService {
  override def version = UIO.succeed(config.version)
}

object ExampleServiceLive {
  val layer = (ExampleServiceLive.apply _).toLayer[BackendService]
}
