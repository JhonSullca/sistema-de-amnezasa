package app.modelos

case class EventoSeguridad(
    id: Long,
    usuario: String,
    ip: String,
    tipoEvento: String,
    descripcion: String,
    intentosFallidos: Int,
    conexionSospechosa: Boolean,
    alertaActiva: Boolean,
    comportamientoExtrano: Boolean
)
