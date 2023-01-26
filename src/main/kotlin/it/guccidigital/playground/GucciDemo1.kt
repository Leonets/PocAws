package it.guccidigital.playground

import it.guccidigital.formats.JacksonMessage
import it.guccidigital.formats.jacksonMessageLens
import it.guccidigital.models.FreemarkerViewModel
import org.http4k.core.Body
import org.http4k.core.ContentType.Companion.APPLICATION_JSON
import org.http4k.core.ContentType.Companion.TEXT_HTML
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.DebuggingFilters.PrintRequest
import org.http4k.lens.Header.CONTENT_TYPE
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import org.http4k.template.FreemarkerTemplates
import org.http4k.template.viewModel

// AWS config and credentials
val awsRegion = "us-east-1"
val awsService = "s3"
val awsAccessKey = "myGreatAwsAccessKey"
val awsSecretKey = "myGreatAwsSecretKey"
val app: HttpHandler = routes(
    //register some routes
    "/ping" bind GET to {
        Response(OK).body("pong")
    },

    "/formats/json/jackson" bind GET to {
        Response(OK).with(jacksonMessageLens of JacksonMessage("Barry", "Hello there!"))
    },
    "/templates/freemarker" bind GET to {
        val renderer = FreemarkerTemplates().CachingClasspath()
        val view = Body.viewModel(renderer, TEXT_HTML).toLens()
        val viewModel = FreemarkerViewModel("Hello there!")
        Response(OK).with(view of viewModel)
    },
    "/testing/approval/json" bind GET to {
        Response(OK)
                .with(CONTENT_TYPE of APPLICATION_JSON)
                .body("""{"message":"value"}""")
    },
    "/testing/approval/text" bind GET to {
        Response(OK).body("hello world")
    }
)

fun main() {
    val printingApp: HttpHandler = PrintRequest().then(app)

    val server = printingApp.asServer(SunHttp(9000)).start()

    println("Server started on " + server.port())
}
