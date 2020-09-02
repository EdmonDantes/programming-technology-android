package ru.loginov.chemistryapplication.api

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import org.apache.commons.io.IOUtils
import ru.loginov.chemistryapplication.ChemistryApplication.MAPPER
import ru.loginov.chemistryapplication.R
import ru.loginov.chemistryapplication.api.entity.*
import java.lang.reflect.Type
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class ChemistryApi(serverHost: String, serverPort: Int, private val timeout: Int = 1000) {

    private val serverUrl = URL("http://$serverHost:$serverPort")

    private val urlUnitCreate = URL(serverUrl, "unit/create")
    private val urlUnitGetAll = URL(serverUrl, "unit/all")
    private val urlUnitDelete = URL(serverUrl, "unit/remove")

    private val urlMetricsGetAll = URL(serverUrl,"metrics/all")

    private val urlGraphsGetAll = URL(serverUrl,"all")
    private val urlGraphsSave = URL(serverUrl,"save")
    private val urlGraphsGetValue = URL(serverUrl, "get")
    private val urlGraphDelete = URL(serverUrl, "delete")

    fun getAllUnits() : Response<List<ChemistryUnit>> = urlUnitGetAll.createHttpConnection(timeout, output = false).readBodyJson(object : TypeReference<List<ChemistryUnit>>(){})

    fun getAllMetrics() : Response<List<ChemistryMetric>> = urlMetricsGetAll.createHttpConnection(timeout, output = false).readBodyJson(object : TypeReference<List<ChemistryMetric>>(){})

    fun getAllGraph() : Response<List<ChemistryGraph>> = urlGraphsGetAll.createHttpConnection(timeout, output = false).readBodyJson(object: TypeReference<List<ChemistryGraph>>(){})

    fun createUnit(unit: ChemistrySaveUnitRequest) : Response<ChemistryUnit> = urlUnitCreate.createHttpConnection(timeout).writeBodyJson(unit).readBodyJson(ChemistryUnit::class.java)

    fun removeUnit(id: Long, force: Boolean = false): Response<Boolean> = urlUnitDelete.createHttpConnection(timeout).writeBodyJson(MAPPER.createObjectNode().also { it.put("id", id).put("force", force) }).readBodyJson(Boolean::class.java)

    fun removeGraph(id: Long) : Response<Boolean> = urlGraphDelete.createHttpConnection(timeout).writeBodyJson(MAPPER.createObjectNode().also { it.put("id", id) }).readBodyJson(Boolean::class.java)

    fun saveGraph(name: String, colors: List<Pair<Pair<ChemistryUnit, Double>, String>>) : Response<Long> {
        if (name.isEmpty()) {
            return Response(false, null, R.string.api_error_graph_name_can_not_be_empty)
        }

        if (colors.isEmpty()) {
            return Response(false, null, R.string.api_error_graph_colors_can_not_be_empty)
        }

        val body = MAPPER.createObjectNode().apply {
            put("name", name)
            putArray("colors").apply {
                for (color in colors) {
                    addObject().apply {
                        putObject("value").apply {
                            put("unit", color.first.first.id)
                            put("value", color.first.second)
                        }
                        putObject("color").put("base64", color.second)
                    }
                }
            }
        }

        return urlGraphsSave.createHttpConnection(timeout).writeBodyJson(body).readBodyJson(JsonNode::class.java).let {
            if (!it.status) {
                return@let Response(false, null, it.errorMessageId, it.serverErrorMessage)
            }

            val id = it.result?.get("id")?.asLong()
                    ?: return@let Response(false, null, R.string.api_error_can_not_read_from_connection, "Can not parse field \"id\" from response message")

            if (id > 0) {
                Response(true, id)
            } else {
                Response(false, null, R.string.api_error_can_not_read_from_connection, "Field \"id\" is wrong = $id")
            }
        }
    }

    fun getValueFromMetric(graphId: Long, metrics: List<ShortMetricDescription>, colors: String) : Response<Map<String, Pair<ChemistryUnit, Double?>>> {
        if (graphId < 1) {
            return Response(false, null, R.string.api_error_graph_id_can_not_be_less_one)
        }

        if (metrics.isEmpty()) {
            return Response(false, null, R.string.api_error_metrics_can_not_be_empty)
        }

        if (colors.isEmpty()) {
            return Response(false, null, R.string.api_error_colors_can_not_be_empty)
        }

        val body = MAPPER.createObjectNode().apply {
            put("graph", graphId)
            putArray("metrics").apply {
                for (metric in metrics) {
                    addPOJO(metric)
                }
            }
            putObject("colors").put("base64", colors)
        }

        return urlGraphsGetValue.createHttpConnection(timeout).writeBodyJson(body).readBodyJson(object : TypeReference<Map<String, Pair<ChemistryUnit, Double?>>>(){})
    }
}

fun <T> HttpURLConnection.readBodyJson(clazz: Class<T>) : Response<T> = readBodyJson(object : TypeReference<T>(){ override fun getType(): Type = clazz })

fun <T> HttpURLConnection.readBodyJson(type: TypeReference<T>) : Response<T> {
    try {
        return readBody().let {
            if (it.isEmpty()) {
                Response(false, null, R.string.api_error_can_not_read_from_connection, "Bytes are empty")
            } else {
                try {
                    val node = MAPPER.readTree(it)
                    val status = node["status"]?.asBoolean()
                    val result = node["result"]
                    val error = node["error"]?.asText()

                    if (status != null) {
                        if (status) {

                            if (result["requires"]?.asText() == "force") {
                                Response(true, null, null, result["reason"]?.asText(), null, true)
                            } else {
                                Response(true, result?.let { MAPPER.readerFor(type).readValue<T>(it) })
                            }
                        } else {
                            Response(false, null, R.string.api_error_can_not_read_from_connection, error)
                        }
                    } else {
                        Response(false, null, R.string.api_error_can_not_read_from_connection, "Can not parse field with name \"status\" from response message")
                    }
                } catch (e: Exception) {
                    Response(false, null, R.string.api_error_can_not_read_from_connection, "Can not parse response message", e)
                }
            }
        }
    } catch (e: ConnectException) {
        return Response(false, null, R.string.api_error_can_not_read_from_connection, "Can not connect to the server", e)
    } catch (e: SocketTimeoutException) {
        return Response(false, null, R.string.api_error_can_not_read_from_connection, "Connection timeout", e)
    }
}


fun HttpURLConnection.writeBodyJson(obj: Any) : HttpURLConnection {
    addHeader("Content-Type", "application/json")
    writeBody(MAPPER.writerFor(obj.javaClass).writeValueAsBytes(obj))
    return this
}

fun HttpURLConnection.readBody(): ByteArray = IOUtils.toByteArray(inputStream)

fun HttpURLConnection.writeBody(data: ByteArray) : Unit = IOUtils.write(data, outputStream)

fun HttpURLConnection.addHeader(name: String, value: String) : Unit = setRequestProperty(name, value)

fun URL.createHttpConnection(timeout: Int = 1000, input: Boolean = true, output: Boolean = true) : HttpURLConnection = (openConnection() as HttpURLConnection).apply {
        connectTimeout = timeout
        doInput = input
        doOutput = output
    }