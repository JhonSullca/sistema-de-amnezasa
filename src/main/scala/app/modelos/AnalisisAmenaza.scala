package app.modelos

case class AnalisisAmenaza(
    nivel: String,
    amenaza: String,
    puntuacion: Int,
    recomendacion: String,
    razones: List[String]
)

case class RegistroAnalisis(
    evento: EventoSeguridad,
    analisis: AnalisisAmenaza,
    fecha: String
)
