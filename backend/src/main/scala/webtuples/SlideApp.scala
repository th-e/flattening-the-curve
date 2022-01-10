package webtuples

import zio._
import zio.clock.Clock
import zio.console.Console
import zio.duration.durationInt
import webtuples.protocol.UserId
import webtuples.protocol.SlideState
import webtuples.protocol.VisitorStatistics
import webtuples.protocol.UserCommand
import zio.stream._

trait SlideApp {
  def slideStateStream: UStream[SlideState]
  def visitorStatisticsStream: UStream[VisitorStatistics]

  def receiveUserCommand(id: UserId, userCommand: UserCommand): UIO[Unit]

  def userJoined: UIO[Unit]
  def userLeft: UIO[Unit]
}

object SlideApp {
  val live: URLayer[Clock with Console, Has[SlideApp]] = SlideAppLive.layer

  def slideStateStream: ZStream[Has[SlideApp], Nothing, SlideState] =
    ZStream.accessStream[Has[SlideApp]](_.get.slideStateStream)

  def visitorStatisticsStream: ZStream[Has[SlideApp], Nothing, VisitorStatistics] =
    ZStream.accessStream[Has[SlideApp]](_.get.visitorStatisticsStream)

  def receiveUserCommand(id: UserId, userCommand: UserCommand): ZIO[Has[SlideApp], Nothing, Unit] =
    ZIO.accessM[Has[SlideApp]](_.get.receiveUserCommand(id, userCommand))

  def userJoined: ZIO[Has[SlideApp], Nothing, Unit] =
    ZIO.accessM[Has[SlideApp]](_.get.userJoined)

  def userLeft: ZIO[Has[SlideApp], Nothing, Unit] =
    ZIO.accessM[Has[SlideApp]](_.get.userLeft)
}

case class SlideAppLive(
    slideStateRef: RefM[SlideState],
    slideStateStream: UStream[SlideState],
    visitorStatisticsRef: RefM[VisitorStatistics],
    visitorStatisticsStream: UStream[VisitorStatistics]
) extends SlideApp {

  def receiveUserCommand(id: UserId, userCommand: UserCommand): UIO[Unit] =
    userCommand match {
      case UserCommand.Subscribe => UIO.unit
    }

  override def userLeft: UIO[Unit] =
    visitorStatisticsRef.update(stats => UIO(stats.removeOne))

  override def userJoined: UIO[Unit] =
    visitorStatisticsRef.update(stats => UIO(stats.addOne))
}

object SlideAppLive {
  val layer: ZLayer[Clock with Console, Nothing, Has[SlideApp]] = {
    for {
      slideVar             <- SubscriptionRef.make(SlideState.empty).toManaged_
      visitorStatisticsVar <- SubscriptionRef.make(VisitorStatistics(0)).toManaged_
    } yield SlideAppLive(
      slideStateRef = slideVar.ref,
      slideStateStream = slideVar.changes,
      visitorStatisticsRef = visitorStatisticsVar.ref,
      visitorStatisticsStream = visitorStatisticsVar.changes
    )
  }.toLayer
}
