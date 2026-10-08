package app.web

import app.modelos.*

object JsonUtil:

  def texto(value: String): String =
    Option(value).getOrElse("")
      .replace("\\", "\\\\")
      .replace("\"", "\\\"")
      .replace("\n", "\\n")
      .replace("\r", "\\r")

  def bool(value: Boolean): String =
    if value then "true" else "false"

  def analisis(analisis: AnalisisAmenaza): String =
    val razones =
      analisis.razones
        .map(r => "\"" + texto(r) + "\"")
        .mkString("[", ",", "]")

    s"""{
      "nivel":"${texto(analisis.nivel)}",
      "amenaza":"${texto(analisis.amenaza)}",
      "puntuacion":${analisis.puntuacion},
      "recomendacion":"${texto(analisis.recomendacion)}",
      "razones":$razones
    }""".replace("\n", "").replace(" ", "")

  def registro(registro: RegistroAnalisis): String =
    s"""{
      "id":${registro.evento.id},
      "usuario":"${texto(registro.evento.usuario)}",
      "ip":"${texto(registro.evento.ip)}",
      "tipoEvento":"${texto(registro.evento.tipoEvento)}",
      "descripcion":"${texto(registro.evento.descripcion)}",
      "intentosFallidos":${registro.evento.intentosFallidos},
      "conexionSospechosa":${bool(registro.evento.conexionSospechosa)},
      "alertaActiva":${bool(registro.evento.alertaActiva)},
      "comportamientoExtrano":${bool(registro.evento.comportamientoExtrano)},
      "fecha":"${texto(registro.fecha)}",
      "analisis":${analisis(registro.analisis)}
    }""".replace("\n", "").replace(" ", "")

  def historial(registros: List[RegistroAnalisis]): String =
    registros
      .map(registro)
      .mkString("[", ",", "]")

  def reporte(reporte: Reporte): String =
    val amenazas =
      reporte.amenazasDetectadas
        .map { case (nombre, cantidad) =>
          "\"" + texto(nombre) + "\":" + cantidad
        }
        .mkString("{", ",", "}")

    s"""{
      "total":${reporte.total},
      "criticas":${reporte.criticas},
      "altas":${reporte.altas},
      "medias":${reporte.medias},
      "bajas":${reporte.bajas},
      "amenazasDetectadas":$amenazas
    }""".replace("\n", "").replace(" ", "")