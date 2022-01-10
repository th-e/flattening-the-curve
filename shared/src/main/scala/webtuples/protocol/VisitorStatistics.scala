package webtuples.protocol

case class VisitorStatistics(connectedUsers: Int) {
  def addOne: VisitorStatistics =
    copy(connectedUsers = connectedUsers + 1)

  def removeOne: VisitorStatistics =
    copy(connectedUsers = connectedUsers - 1)
}
