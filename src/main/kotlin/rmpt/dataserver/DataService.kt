package rmpt.dataserver

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.TextNode
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import javax.annotation.PostConstruct

@Service
class DataService {

    @Value("\${endpoints}")
    private val endpointsParam: String? = null

    private val endpoints: MutableSet<String> = mutableSetOf()
    private val data: MutableMap<String, MutableList<JsonNode>> = mutableMapOf()

    @PostConstruct
    private fun createEndpoints() {
        val endpointsList = mutableListOf("ROOT")
        if(endpointsParam != null){
            endpointsList.addAll(endpointsParam.split(","))
        }
        endpointsList.forEach {
            val currEndpoint = it.trim()
            data[currEndpoint] = mutableListOf()
            endpoints.add(currEndpoint)

            if("ROOT" != it) {
                println("Endpoint '$currEndpoint' created")
            }
        }
    }

    fun exists(endpoint: String): Boolean = endpoints.contains(endpoint)

    fun add(endpoint: String, jsonElement: JsonNode) {
        data[endpoint]!!.add(jsonElement)
    }

    fun get(endpoint: String, queryParams: List<Pair<String, String>>): List<JsonNode> {
        val endpointData = data[endpoint]!!

        return endpointData.filter { endpointEntry ->
            queryParams.all { queryParam ->
                var match = false
                val queryKey = queryParam.first
                val queryValue = queryParam.second

                val value = endpointEntry.get(queryKey)
                if(value != null && value.isTextual && (value as TextNode).textValue() == queryValue){
                    match = true
                }
                match
            }
        }
    }

    fun get(endpoint: String): List<JsonNode> = data[endpoint]!!

    fun update(endpoint: String, queryParams: List<Pair<String, String>>, jsonElement: JsonNode): JsonNode {
        val entries = get(endpoint, queryParams)
        data[endpoint]!!.removeAll(entries)
        data[endpoint]!!.add(jsonElement)
        return jsonElement
    }

    fun delete(endpoint: String, queryParams: List<Pair<String, String>>): Boolean {
        val entries = get(endpoint, queryParams)
        return data[endpoint]!!.removeAll(entries)
    }
}