function envoy_on_request(request_handle)

  local url = request_handle:headers():get(":path");
  local upstream_cluster = os.getenv("DEFAULT_CLUSTER")
  local urlComponents = {}

  for token in string.gmatch(url, "%/([%w_]+)") do
    table.insert(urlComponents, token)
  end

  local cache_url = "/"

  if #urlComponents >=4 then
    cache_url = cache_url .. "?queryId=" .. urlComponents[4]
  end

  local headers, body = request_handle:httpCall(
  "cache",
  {
    [":method"] = "GET",
    [":path"] = cache_url,
    [":authority"] = "localhost"
  }, 
  "na", 5000)
  
  if headers[":status"] == "200" then
    upstream_cluster = body
  else
    request_handle:logWarn("Invalid response code '" .. headers[":status"] .. "' from cache! Routing to default cluster")
  end

  request_handle:logDebug("Upstream cluster: " .. upstream_cluster)
  request_handle:headers():add("X-Cluster-Id", upstream_cluster)
end



function envoy_on_response(response_handle)

  local queryId = response_handle:headers():get("X-Query-Id")
  local cluster = response_handle:headers():get("X-Cluster-Id")
  local default_cluster = os.getenv("DEFAULT_CLUSTER")

  if cluster ~= nil and queryId ~= nil then
    -- save queryid to cluser mapping in cache
    local headers, body = response_handle:httpCall(
    "cache",
    {
      [":method"] = "POST",
      [":path"] = "/?queryId=" .. queryId .. "&cluster=" .. cluster,
      [":authority"] = "localhost"
    }, 
    "na", 5000)
    
    if headers[":status"] ~= "200" then
      response_handle:logWarn("Invalid response code '" .. headers[":status"] .. "' from cache while saving query to cluster map")

      if cluster ~= default_cluster then
        request_handle:respond(
          {[":status"] = "500"},
          "Cache failure with status '" .. "' and error: " .. body
      )
      end

      response_handle:logDebug("Routed query: " .. queryId .. " to cluster " .. cluster)
    end

  end

end
