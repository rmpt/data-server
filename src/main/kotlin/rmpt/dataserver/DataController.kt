package rmpt.dataserver

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
class DataController(private val dataService: DataService) {

    private val mapper = ObjectMapper()

    private fun getEndpoint(requestUri: String): Pair<String, String?> {
        if (requestUri == "/") return "ROOT" to null

        val trimmedRequestUri = requestUri.substring(1, requestUri.length)

        if (dataService.exists(trimmedRequestUri)) return trimmedRequestUri to null

        val lastSlashIndex = trimmedRequestUri.lastIndexOf("/")
        val id = trimmedRequestUri.substring(lastSlashIndex + 1, trimmedRequestUri.length)
        val endpoint = if (lastSlashIndex == -1) "ROOT" else trimmedRequestUri.substring(0, lastSlashIndex)
        return endpoint to id
    }

    private fun requestParamMapToPairList(request: HttpServletRequest): List<Pair<String, String>> =
        request.parameterMap.map { it.key!! to it.value[0]!! }

    @PostMapping("**")
    fun post(request: HttpServletRequest, @RequestBody content: String): JsonNode? {
        val endpoint = getEndpoint(request.requestURI)
        val jsonElement = mapper.readTree(content)
        dataService.add(endpoint.first, jsonElement)
        return jsonElement
    }

    @GetMapping("**")
    fun get(request: HttpServletRequest): Any? {
        val endpoint2id = getEndpoint(request.requestURI)
        val endpoint = endpoint2id.first
        return when {
            endpoint2id.second != null -> dataService.getById(endpoint, endpoint2id.second!!)
            request.parameterMap.keys.size >= 1 -> dataService.get(endpoint, requestParamMapToPairList(request))
            else -> dataService.getAll(endpoint)
        }
    }

    @PutMapping("**")
    fun put(request: HttpServletRequest, @RequestBody content: String): JsonNode {
        val endpoint2id = getEndpoint(request.requestURI)
        val endpoint = endpoint2id.first
        val jsonElement = mapper.readTree(content)
        return when {
            endpoint2id.second != null -> dataService.updateById(endpoint, endpoint2id.second!!, jsonElement)
            else -> dataService.update(endpoint, requestParamMapToPairList(request), jsonElement)
        }
    }

    @DeleteMapping("**")
    fun delete(request: HttpServletRequest): Boolean {
        val endpoint2id = getEndpoint(request.requestURI)
        val endpoint = endpoint2id.first
        return when {
            endpoint2id.second != null -> dataService.deleteById(endpoint, endpoint2id.second!!)
            else -> dataService.delete(endpoint, requestParamMapToPairList(request))
        }
    }
}
