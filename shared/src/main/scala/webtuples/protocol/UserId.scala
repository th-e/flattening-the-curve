package webtuples.protocol

import java.util.UUID

case class UserId(string: String) extends AnyVal

object UserId {
  def random: UserId = UserId(UUID.randomUUID().toString)
}
