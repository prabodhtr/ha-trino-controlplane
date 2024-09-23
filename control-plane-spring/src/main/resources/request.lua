function envoy_on_request(request_handle)

  local url = request_handle:headers():get(":path");
  local urlComponents = {}

  for token in string.gmatch(url, "%/([%w_]+)") do
    table.insert(urlComponents, token)
  end

  local cache_url = "/cache/"
  local request_id = request_handle:headers():get("x-request-id")

  if #urlComponents >=4 then
    cache_url = cache_url .. "?queryId=" .. urlComponents[4]
  end

  local headers, body = request_handle:httpCall(
  "cache",
  {
    [":method"] = "GET",
    [":path"] = cache_url,
    [":authority"] = "localhost",
    ["x-request-id"] = request_id
  },
  "na", 5000)
  
  if headers[":status"] ~= "200" then
    request_handle:respond(
      {
          [":status"] = "500",
          ["x-request-id"] = request_id
      },
      "Cache failure with status '" .. headers[":status"] .. "' and error: " .. body
   )
  end

  upstream_cluster = body
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
      [":path"] = "/cache/?queryId=" .. queryId .. "&cluster=" .. cluster,
      [":authority"] = "localhost",
      ["x-request-id"] = response_handle:headers():get("x-request-id")
    },
    "na", 5000)
    
    if headers[":status"] ~= "200" then
      response_handle:logError("Invalid response code '" .. headers[":status"] .. "' from cache while saving query to cluster map")

      response_handle:respond(
        {
            [":status"] = "500"
        },
        "Cache failure with status '" .. "' and error: " .. body
      )

      response_handle:logDebug("Routed query: " .. queryId .. " to cluster " .. cluster)
    end

  end

end
