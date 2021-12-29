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
    val serviceLayer = (ZLayer.identity[Console] ++ Config.live) >>> BackendServiceLive.layer
    program
      .injectCustom(serviceLayer)
      .exitCode
  }
}

case class BackendServiceLive(config: Config) extends BackendService {
  override def version = UIO.succeed(config.version)
}

object BackendServiceLive {
  val layer = (BackendServiceLive.apply _).toLayer[BackendService]
}
