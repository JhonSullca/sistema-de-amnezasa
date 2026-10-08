package app.servicios

import app.modelos.{AnalisisAmenaza, EventoSeguridad}

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import scala.io.Source
import scala.util.Using

class PrologService(projectRoot: Path, swiplPath: String):

  private val inputFile = projectRoot.resolve("data").resolve("entrada_actual.pl")
  private val rulesFile = projectRoot.resolve("prolog").resolve("detector.pl")

  def analizar(evento: EventoSeguridad): Either[String, AnalisisAmenaza] =
    try
      writeInputFacts(evento)

      val command = Seq(
        swiplPath,
        "-q",
        "-s", rulesFile.toAbsolutePath.toString,
        "-g", "ejecutar",
        "-t", "halt"
      )

      val process = new ProcessBuilder(command*).directory(projectRoot.toFile).start()
      val stdout = Using.resource(Source.fromInputStream(process.getInputStream, StandardCharsets.UTF_8.name()))(_.mkString)
      val stderr = Using.resource(Source.fromInputStream(process.getErrorStream, StandardCharsets.UTF_8.name()))(_.mkString)
      val exitCode = process.waitFor()

      if exitCode != 0 then
        Left(s"SWI-Prolog terminó con código $exitCode. $stderr")
      else
        parseOutput(stdout)
    catch
      case e: java.io.IOException =>
        Left("No se pudo ejecutar SWI-Prolog. Verifica que SWI-Prolog esté instalado y que 'swipl' esté en el PATH o configura swipl.path en config/app.properties.")
      case e: Exception =>
        Left(s"Error al ejecutar Prolog: ${e.getMessage}")

  private def writeInputFacts(evento: EventoSeguridad): Unit =
    val content =
      s"""% Archivo generado automáticamente por Scala
usuario('${escapeAtom(evento.usuario)}').
ip('${escapeAtom(evento.ip)}').
tipo_evento('${escapeAtom(evento.tipoEvento)}').
descripcion('${escapeAtom(evento.descripcion)}').
intentos_fallidos(${evento.intentosFallidos}).
conexion_sospechosa(${evento.conexionSospechosa}).
alerta_activa(${evento.alertaActiva}).
comportamiento_extrano(${evento.comportamientoExtrano}).
"""

    Files.writeString(
      inputFile,
      content,
      StandardCharsets.UTF_8,
      StandardOpenOption.CREATE,
      StandardOpenOption.TRUNCATE_EXISTING
    )

  private def escapeAtom(value: String): String =
    Option(value).getOrElse("").replace("'", "\\'").replace("\\", "\\\\")

  private def parseOutput(raw: String): Either[String, AnalisisAmenaza] =
    val values = raw.linesIterator
      .map(_.trim)
      .filter(_.contains("="))
      .map { line =>
        val index = line.indexOf('=')
        line.substring(0, index) -> line.substring(index + 1)
      }
      .toMap

    if !values.contains("nivel") then
      Left(s"Prolog no devolvió un resultado válido. Salida: $raw")
    else
      val razones = values.get("razones").toList
        .flatMap(_.split(";", -1).toList)
        .map(_.trim)
        .filter(_.nonEmpty)

      Right(
        AnalisisAmenaza(
          nivel = values.getOrElse("nivel", "BAJO"),
          amenaza = values.getOrElse("amenaza", "Sin amenaza identificada"),
          puntuacion = values.getOrElse("puntuacion", "0").toIntOption.getOrElse(0),
          recomendacion = values.getOrElse("recomendacion", "Mantener monitoreo."),
          razones = razones
        )
      )
