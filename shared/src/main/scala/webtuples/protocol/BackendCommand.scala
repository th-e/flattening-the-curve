package webtuples.protocol

sealed trait BackendCommand

object BackendCommand {
  case class SendSlideState(slideState: SlideState)                       extends BackendCommand
  case class SendUserId(id: UserId)                                       extends BackendCommand
  case class SendVisitorStatistics(visitorStatistics: VisitorStatistics)  extends BackendCommand
}