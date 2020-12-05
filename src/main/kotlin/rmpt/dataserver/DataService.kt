package rmpt.dataserver

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.TextNode
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.lang.RuntimeException
import javax.annotation.PostConstruct

@Service
class DataService {

    @Value("\${endpoints}")
    private val endpointsParam: String? = null

    private val endpoints: MutableSet<String> = mutableSetOf()
    private val endpoint2idField: MutableMap<String, String> = mutableMapOf()
    private val data: MutableMap<String, MutableList<JsonNode>> = mutableMapOf()

    @PostConstruct
    private fun createEndpoints() {
        val endpointsList = mutableListOf("ROOT")
        if (endpointsParam != null) {
            endpointsList.addAll(endpointsParam.split(","))
        }
        endpointsList.forEach {
            var currEndpoint = it.trim()
            var currEndpointIdField = "id"

            if (currEndpoint.contains('[')) {
                val idStartIndex = currEndpoint.indexOf('[')
                val idEndIndex = currEndpoint.indexOf(']')

                if (idEndIndex == -1) throw RuntimeException("Malformed id definition. Id must be defined between []. Example: posts[id]")

                currEndpointIdField = currEndpoint.substring(idStartIndex + 1, idEndIndex)
                currEndpoint = currEndpoint.substring(0, idStartIndex)

                if ("" == currEndpointIdField) throw RuntimeException("Id field name cannot be empty. Endpoint: $currEndpoint")
            }
            endpoint2idField[currEndpoint] = currEndpointIdField

            data[currEndpoint] = mutableListOf()
            endpoints.add(currEndpoint)

            if ("ROOT" != it) {
                println("Endpoint '$currEndpoint' created")
            }
        }
    }

    fun exists(endpoint: String): Boolean = endpoints.contains(endpoint)

    fun add(endpoint: String, jsonElement: JsonNode) {
        data[endpoint]!!.add(jsonElement)
    }

    fun getAll(endpoint: String): List<JsonNode> = data[endpoint]!!

    fun getById(endpoint: String, idValue: String): JsonNode? {
        val endpointData = data[endpoint]!!
        val endpointKey = endpoint2idField[endpoint]
        return endpointData.firstOrNull {
            val value = it.get(endpointKey)
            value != null && value.isTextual && (value as TextNode).textValue() == idValue
        }
    }

    fun get(endpoint: String, queryParams: List<Pair<String, String>>): List<JsonNode> {
        return data[endpoint]!!.filter { endpointEntry ->
            queryParams.all { queryParam ->
                var match = false
                val queryKey = queryParam.first
                val queryValue = queryParam.second

                val value = endpointEntry.get(queryKey)
                if (value != null && value.isTextual && (value as TextNode).textValue() == queryValue) {
                    match = true
                }
                match
            }
        }
    }

    fun updateById(endpoint: String, id: String, jsonElement: JsonNode): JsonNode {
        val entry = getById(endpoint, id)
        data[endpoint]!!.remove(entry)
        data[endpoint]!!.add(jsonElement)
        return jsonElement
    }

    fun update(endpoint: String, queryParams: List<Pair<String, String>>, jsonElement: JsonNode): JsonNode {
        val entries = get(endpoint, queryParams)
        data[endpoint]!!.removeAll(entries)
        data[endpoint]!!.add(jsonElement)
        return jsonElement
    }

    fun deleteById(endpoint: String, idValue: String): Boolean {
        val entry = getById(endpoint, idValue)
        return data[endpoint]!!.remove(entry)
    }

    fun delete(endpoint: String, queryParams: List<Pair<String, String>>): Boolean {
        val entries = get(endpoint, queryParams)
        return data[endpoint]!!.removeAll(entries)
    }
}
