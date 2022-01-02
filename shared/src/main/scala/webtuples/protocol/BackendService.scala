package webtuples.protocol

import zio._

trait BackendService {
  def version: UIO[String] 
}
  