package webtuples

import zio._
import zio.config._
import zio.config.magnolia._
import zio.config.typesafe._
import zio.config.magnolia.DeriveConfigDescriptor.descriptor
import com.typesafe.config.ConfigFactory

import java.io.File

case class Config(version: String, adminPassword: String)

object Config {
  val descriptor: ConfigDescriptor[Config] =
    DeriveConfigDescriptor.descriptor[Config]

  val loadConfigDescriptor: ZIO[ZEnv, ReadError[String], ConfigDescriptor[Config]] = {

    val configDescriptor: ConfigDescriptor[Config] =
      DeriveConfigDescriptor.descriptor[Config]

    for {
      envConfigSource <- ConfigSource.fromSystemEnv(
        keyDelimiter = Some('_'),
        valueDelimiter = Some(',')
      )
      typesafeConfigSource <- IO.fromEither(TypesafeConfigSource.fromHoconFile(new File("application.conf")))
      sources: ConfigSource = envConfigSource <> typesafeConfigSource
    } yield {
      configDescriptor.updateSource(_ => sources)
    }
  }

  val live: ZLayer[ZEnv, ReadError[String], Has[Config]] =
    loadConfigDescriptor.map(read[Config](_)).absolve.toLayer

  val service: URIO[Has[Config], Config] = ZIO.service[Config]
}
