package app.web

import app.modelos.*
import app.servicios.AnalisisService

import com.sun.net.httpserver.{HttpExchange, HttpHandler, HttpServer}

import java.net.{InetSocketAddress, URLDecoder}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters.*

class WebServer(
                 host: String,
                 port: Int,
                 projectRoot: Path,
                 analisisService: AnalisisService
               ):

  private val server: HttpServer =
    HttpServer.create(new InetSocketAddress(host, port), 0)

  def start(): Unit =
    server.createContext("/", rootHandler)
    server.createContext("/api/detect", detectHandler)
    server.createContext("/api/history", historyHandler)
    server.createContext("/api/report", reportHandler)
    server.createContext("/api/reset", resetHandler)

    server.setExecutor(
      java.util.concurrent.Executors.newFixedThreadPool(8)
    )

    server.start()

    println(s"Servidor iniciado en http://localhost:$port")

  // =========================================================
  // SERVIR LA PÁGINA PRINCIPAL
  // =========================================================

  private val rootHandler: HttpHandler =
    (exchange: HttpExchange) =>
      if exchange.getRequestURI.getPath == "/" then
        serveFile(
          exchange,
          projectRoot.resolve("web/index.html"),
          "text/html; charset=UTF-8"
        )
      else
        serveStatic(exchange)

  // =========================================================
  // SERVIR ARCHIVOS CSS, JS, IMÁGENES, ETC.
  // =========================================================

  private def serveStatic(exchange: HttpExchange): Unit =
    val uriPath =
      exchange.getRequestURI.getPath.stripPrefix("/")

    val file =
      projectRoot.resolve("web").resolve(uriPath).normalize()

    val webRoot =
      projectRoot.resolve("web").normalize()

    if !file.startsWith(webRoot) ||
      !Files.exists(file) ||
      Files.isDirectory(file) then

      sendText(
        exchange,
        404,
        "Recurso no encontrado",
        "text/plain; charset=UTF-8"
      )

    else

      val contentType =
        file.toString.toLowerCase match
          case p if p.endsWith(".html") =>
            "text/html; charset=UTF-8"

          case p if p.endsWith(".css") =>
            "text/css; charset=UTF-8"

          case p if p.endsWith(".js") =>
            "application/javascript; charset=UTF-8"

          case p if p.endsWith(".json") =>
            "application/json; charset=UTF-8"

          case p if p.endsWith(".png") =>
            "image/png"

          case p if p.endsWith(".jpg") || p.endsWith(".jpeg") =>
            "image/jpeg"

          case p if p.endsWith(".svg") =>
            "image/svg+xml"

          case _ =>
            "application/octet-stream"

      serveFile(exchange, file, contentType)

  // =========================================================
  // API PARA ANALIZAR UN EVENTO
  // =========================================================

  private val detectHandler: HttpHandler =
    (exchange: HttpExchange) =>

      if exchange.getRequestMethod != "POST" then

        sendText(
          exchange,
          405,
          "Método no permitido",
          "text/plain; charset=UTF-8"
        )

      else

        val form =
          parseForm(readBody(exchange))

        val evento =
          EventoSeguridad(
            id = System.currentTimeMillis(),
            usuario = form.getOrElse("usuario", "anonimo"),
            ip = form.getOrElse("ip", "0.0.0.0"),
            tipoEvento = form.getOrElse("tipoEvento", "general"),
            descripcion = form.getOrElse("descripcion", ""),
            intentosFallidos =
              form
                .getOrElse("intentosFallidos", "0")
                .toIntOption
                .getOrElse(0),
            conexionSospechosa =
              form.get("conexionSospechosa").contains("true"),
            alertaActiva =
              form.get("alertaActiva").contains("true"),
            comportamientoExtrano =
              form.get("comportamientoExtrano").contains("true")
          )

        analisisService.analizar(evento) match

          case Right(resultado) =>
            sendJson(
              exchange,
              200,
              JsonUtil.registro(resultado)
            )

          case Left(mensajeError) =>
            val json =
              "{\"error\":\"" +
                JsonUtil.texto(mensajeError) +
                "\"}"

            sendJson(
              exchange,
              500,
              json
            )

  // =========================================================
  // API PARA OBTENER EL HISTORIAL
  // =========================================================

  private val historyHandler: HttpHandler =
    (exchange: HttpExchange) =>

      if exchange.getRequestMethod != "GET" then

        sendText(
          exchange,
          405,
          "Método no permitido",
          "text/plain; charset=UTF-8"
        )

      else

        sendJson(
          exchange,
          200,
          JsonUtil.historial(
            analisisService.obtenerHistorial
          )
        )

  // =========================================================
  // API PARA OBTENER EL REPORTE
  // =========================================================

  private val reportHandler: HttpHandler =
    (exchange: HttpExchange) =>

      if exchange.getRequestMethod != "GET" then

        sendText(
          exchange,
          405,
          "Método no permitido",
          "text/plain; charset=UTF-8"
        )

      else

        sendJson(
          exchange,
          200,
          JsonUtil.reporte(
            analisisService.obtenerReporte
          )
        )

  // =========================================================
  // API PARA REINICIAR EL HISTORIAL
  // =========================================================

  private val resetHandler: HttpHandler =
    (exchange: HttpExchange) =>

      if exchange.getRequestMethod != "POST" then

        sendText(
          exchange,
          405,
          "Método no permitido",
          "text/plain; charset=UTF-8"
        )

      else

        analisisService.limpiarHistorial()

        sendJson(
          exchange,
          200,
          "{\"ok\":true}"
        )

  // =========================================================
  // LEER EL CUERPO DE LA PETICIÓN
  // =========================================================

  private def readBody(exchange: HttpExchange): String =
    val bytes =
      exchange.getRequestBody.readAllBytes()

    new String(
      bytes,
      StandardCharsets.UTF_8
    )

  // =========================================================
  // CONVERTIR FORMULARIO A MAP
  // =========================================================

  private def parseForm(
                         body: String
                       ): Map[String, String] =

    body
      .split("&")
      .toList
      .filter(_.nonEmpty)
      .map { pair =>

        val parts =
          pair.split("=", 2)

        val key =
          URLDecoder.decode(
            parts.headOption.getOrElse(""),
            StandardCharsets.UTF_8
          )

        val value =
          URLDecoder.decode(
            parts.drop(1).headOption.getOrElse(""),
            StandardCharsets.UTF_8
          )

        key -> value
      }
      .toMap

  // =========================================================
  // SERVIR ARCHIVOS
  // =========================================================

  private def serveFile(
                         exchange: HttpExchange,
                         file: Path,
                         contentType: String
                       ): Unit =

    if !Files.exists(file) then

      sendText(
        exchange,
        404,
        "Archivo no encontrado",
        "text/plain; charset=UTF-8"
      )

    else

      val bytes =
        Files.readAllBytes(file)

      exchange.getResponseHeaders.set(
        "Content-Type",
        contentType
      )

      exchange.getResponseHeaders.set(
        "Cache-Control",
        "no-cache"
      )

      exchange.sendResponseHeaders(
        200,
        bytes.length
      )

      val output =
        exchange.getResponseBody

      output.write(bytes)
      output.close()

  // =========================================================
  // RESPUESTA JSON
  // =========================================================

  private def sendJson(
                        exchange: HttpExchange,
                        status: Int,
                        json: String
                      ): Unit =

    exchange.getResponseHeaders.set(
      "Content-Type",
      "application/json; charset=UTF-8"
    )

    exchange.getResponseHeaders.set(
      "Access-Control-Allow-Origin",
      "*"
    )

    val bytes =
      json.getBytes(StandardCharsets.UTF_8)

    exchange.sendResponseHeaders(
      status,
      bytes.length
    )

    val output =
      exchange.getResponseBody

    output.write(bytes)
    output.close()

  // =========================================================
  // RESPUESTA DE TEXTO
  // =========================================================

  private def sendText(
                        exchange: HttpExchange,
                        status: Int,
                        text: String,
                        contentType: String
                      ): Unit =

    exchange.getResponseHeaders.set(
      "Content-Type",
      contentType
    )

    val bytes =
      text.getBytes(StandardCharsets.UTF_8)

    exchange.sendResponseHeaders(
      status,
      bytes.length
    )

    val output =
      exchange.getResponseBody

    output.write(bytes)
    output.close()