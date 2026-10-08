package app

import app.servicios.{AnalisisService, PrologService}
import app.web.WebServer

import java.nio.file.{Files, Path, Paths}
import java.util.Properties

object Main:

  def main(args: Array[String]): Unit =
    val projectRoot = Paths.get(System.getProperty("user.dir")).toAbsolutePath.normalize()
    val config = cargarConfiguracion(projectRoot.resolve("config").resolve("app.properties"))

    val host = config.getProperty("server.host", "0.0.0.0")
    val port = config.getProperty("server.port", "8080").toIntOption.getOrElse(8080)
    val swipl = resolverSwipl(config.getProperty("swipl.path", "swipl"))

    println("==============================================")
    println(" SISTEMA EXPERTO DE DETECCION DE AMENAZAS")
    println(" Scala + Prolog")
    println("==============================================")
    println(s"Scala  : ${util.Properties.versionNumberString}")
    println(s"Proyecto: $projectRoot")
    println(s"SWI-Prolog: $swipl")

    val prologService = PrologService(projectRoot, swipl)
    val analisisService = AnalisisService(prologService)
    val server = WebServer(host, port, projectRoot, analisisService)
    server.start()

  private def cargarConfiguracion(path: Path): Properties =
    val properties = new Properties()
    if Files.exists(path) then
      val input = Files.newInputStream(path)
      try properties.load(input)
      finally input.close()
    properties

  private def resolverSwipl(configured: String): String =
    if configured != "swipl" then configured
    else
      val candidatos = List(
        "swipl",
        "C:\\Program Files\\swipl\\bin\\swipl.exe",
        "C:\\Program Files\\swipl\\bin\\swipl-win.exe"
      )
      candidatos.find(c => c == "swipl" || Files.exists(Paths.get(c))).getOrElse(configured)
