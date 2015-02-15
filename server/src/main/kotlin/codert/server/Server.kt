package codert.server

import spark.Spark
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import codert.vcs.svn.SvnRev
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import java.time.Instant
import com.fasterxml.jackson.databind.SerializationFeature
import spark.ResponseTransformer
import spark.Response

val jsonModule = KotlinModule()
        .addSerializer(javaClass<SvnRev>(), object : JsonSerializer<SvnRev>() {
            override fun serialize(value: SvnRev, jgen: JsonGenerator?, provider: SerializerProvider?) {
                jgen?.writeNumber(value.rev.getNumber())
            }
        })
        .addSerializer(javaClass<Instant>(), object : JsonSerializer<Instant>() {
            override fun serialize(value: Instant, jgen: JsonGenerator?, provider: SerializerProvider?) {
                jgen?.writeString(value.toString())
            }
        })
val mapper = ObjectMapper().registerModule(jsonModule)

fun jsonify(obj: Any) : ByteArrayOutputStream {
    val buffer = ByteArrayOutputStream()
    mapper.writeValue(buffer, obj)
    return buffer
}

fun returnJson(res: Response, obj: Any) : Any {
    res.type("application/json")
    return jsonify(obj)
}

fun main(args: Array<String>) {

    Spark.get("/:repo/diff/:rev1/:rev2") {(req, res) ->
        val repo = codert.vcs.repo(req.params("repo"))
        val rev1 = repo.rev(req.params("rev1"))
        val rev2 = repo.rev(req.params("rev2"))
        val diff = repo.diff(rev1, rev2)

        returnJson(res, diff)
    }

    Spark.get("/:repo/log/recent") {(req, res) ->
        val repo = codert.vcs.repo(req.params("repo"))
        val latestRev = repo.latestRev()
        val oldRev = latestRev.offset(-50)
        val logs = repo.logs(oldRev, latestRev)

        returnJson(res, logs)
    }

    Spark.get("/:repo/log/:rev1/:rev2") {(req, res) ->
        val repo = codert.vcs.repo(req.params("repo"))
        val rev1 = repo.rev(req.params("rev1"))
        val rev2 = repo.rev(req.params("rev2"))
        val logs = repo.logs(rev1, rev2)

        returnJson(res, logs)
    }
}