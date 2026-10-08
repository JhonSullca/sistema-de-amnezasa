package app.modelos

case class Reporte(
    total: Int,
    criticas: Int,
    altas: Int,
    medias: Int,
    bajas: Int,
    amenazasDetectadas: Map[String, Int]
)
