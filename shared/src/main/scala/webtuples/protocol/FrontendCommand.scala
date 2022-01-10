package webtuples.protocol

import java.util.UUID

sealed trait FrontendCommand

sealed trait UserCommand extends FrontendCommand

object UserCommand {
  case object Subscribe extends UserCommand
}

sealed trait AdminCommand extends FrontendCommand

object AdminCommand {
  case object NextSlide               extends AdminCommand
  case object PrevSlide               extends AdminCommand
}
