package rmpt.dataserver

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
class DataController(private val dataService: DataService) {

    private val mapper = ObjectMapper()

    private fun getEndpoint(requestUri: String): String {
        return if(requestUri == "/") "ROOT" else requestUri.substring(1, requestUri.length)
    }

    private fun requestParamMapToPairList(request: HttpServletRequest): List<Pair<String, String>> =
        request.parameterMap.map { it.key!! to it.value[0]!! }

    @PostMapping("*")
    fun post(request: HttpServletRequest, @RequestBody content: String): JsonNode? {
        val endpoint = getEndpoint(request.requestURI)
        val jsonElement = mapper.readTree(content)
        dataService.add(endpoint, jsonElement)
        return jsonElement

    }

    @GetMapping("*")
    fun get(request: HttpServletRequest): List<JsonNode>? {
        val endpoint = getEndpoint(request.requestURI)
        return if(request.parameterMap.keys.size >= 1) {
            dataService.get(endpoint, requestParamMapToPairList(request))
        } else {
            dataService.get(endpoint)
        }
    }

    @PutMapping("*")
    fun put(request: HttpServletRequest, @RequestBody content: String): JsonNode {
        val endpoint = getEndpoint(request.requestURI)
        val jsonElement = mapper.readTree(content)
        return dataService.update(endpoint, requestParamMapToPairList(request), jsonElement)
    }

    @DeleteMapping("*")
    fun delete(request: HttpServletRequest): Boolean {
        val endpoint = getEndpoint(request.requestURI)
        return dataService.delete(endpoint, requestParamMapToPairList(request))
    }
}