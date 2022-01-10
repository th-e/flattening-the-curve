package webtuples

import webtuples.protocol.{BackendService}
import zio._
import zio.app._
import zio.console._
import zio.magic._
import zhttp.http._
import zhttp.service._
import zhttp.socket._
import zhttp.core.ByteBuf
import zio.stream.ZStream
import boopickle.Default._
import io.netty.buffer.Unpooled
import zhttp.core.ByteBuf

import java.nio.ByteBuffer
import scala.util.{Failure, Success, Try}
import webtuples.protocol.UserId
import webtuples.protocol.SlideState
import webtuples.protocol.VisitorStatistics
import webtuples.protocol.UserCommand
import webtuples.protocol.BackendCommand._
import webtuples.protocol.BackendCommand

object Backend extends App {
  private def httpApp = {
    //val backendService = DeriveRoutes.gen[BackendService]
    val socketHandling =
      HttpApp.collect { case Method.GET -> Root / "ws" =>
        Response.socket(userSocket)
      }
    //backendService <> socketHandling  TODO: combining does not work
    socketHandling
  }

  val program =
    for {
      port <- system.envOrElse("PORT", "8088").map(_.toInt).orElseSucceed(8088)
      _    <- putStrLn(s"STARTING SERVER ON PORT $port")
      _ <- zhttp.service.Server.start(port, httpApp)
    } yield ()

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    val serviceLayer = (ZLayer.identity[Console] ++ Config.live) >>> BackendServiceLive.layer
    program
      .injectCustom(serviceLayer ++ webtuples.SlideApp.live)
      .exitCode
  }

  def userSocket: SocketApp[Console with Has[SlideApp], Nothing] = {
    val userId = UserId.random

    val handleCommand = pickleSocket { (command: UserCommand) =>
      command match {
        case UserCommand.Subscribe =>
          ZStream
            .mergeAllUnbounded()(
              ZStream.fromEffect(SlideApp.userJoined).drain,
              SlideApp.slideStateStream.map(SendSlideState),
              SlideApp.visitorStatisticsStream.map(SendVisitorStatistics),
              ZStream.succeed[BackendCommand](SendUserId(userId))
            )
            .map { s =>
              val bytes: ByteBuffer = Pickle.intoBytes(s)
              val byteBuf           = Unpooled.wrappedBuffer(bytes)
              WebSocketFrame.binary(ByteBuf(byteBuf))
            }

        case command =>
          ZStream.fromEffect(SlideApp.receiveUserCommand(userId, command)).drain
      }
    }
    handleCommand
  }

  private def pickleSocket[R, E, A: Pickler](f: A => ZStream[R, E, WebSocketFrame]): SocketApp[Console with R, E] =
    SocketApp.message(
      Socket.collect {
        case WebSocketFrame.Binary(bytes) =>
          Try(Unpickle[A].fromBytes(bytes.asJava.nioBuffer())) match {
            case Failure(error) =>
              ZStream.fromEffect(putStrErr(s"Decoding Error: $error").!).drain
            case Success(command) =>
              f(command)
          }
        case other =>
          ZStream.fromEffect(UIO(println(s"RECEIVED $other"))).drain
      }
    )
}

case class BackendServiceLive(config: Config) extends BackendService {
  override def version = UIO.succeed(config.version)
}

object BackendServiceLive {
  val layer = (BackendServiceLive.apply _).toLayer[BackendService]
}
