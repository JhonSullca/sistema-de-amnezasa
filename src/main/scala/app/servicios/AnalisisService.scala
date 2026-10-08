package app.servicios

import app.modelos.*

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.collection.mutable.ListBuffer

class AnalisisService(prologService: PrologService):

  private val historial = ListBuffer.empty[RegistroAnalisis]
  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  def analizar(evento: EventoSeguridad): Either[String, RegistroAnalisis] =
    prologService.analizar(evento).map { analisis =>
      val registro = RegistroAnalisis(evento, analisis, LocalDateTime.now().format(formatter))
      historial.prepend(registro)
      registro
    }

  def obtenerHistorial: List[RegistroAnalisis] = historial.synchronized(historial.toList)

  def obtenerReporte: Reporte =
    val registros = obtenerHistorial
    val porNivel = registros.groupBy(_.analisis.nivel).view.mapValues(_.size).toMap.withDefaultValue(0)
    val amenazas = registros
      .groupBy(_.analisis.amenaza)
      .view.mapValues(_.size).toMap

    Reporte(
      total = registros.size,
      criticas = porNivel("CRITICO"),
      altas = porNivel("ALTO"),
      medias = porNivel("MEDIO"),
      bajas = porNivel("BAJO"),
      amenazasDetectadas = amenazas
    )

  def limpiarHistorial(): Unit = historial.synchronized(historial.clear())
