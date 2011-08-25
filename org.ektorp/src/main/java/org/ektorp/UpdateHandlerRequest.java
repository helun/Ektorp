/**
 * 
 */
package org.ektorp;

import java.util.Map;
import java.util.TreeMap;

import org.codehaus.jackson.map.ObjectMapper;
import org.ektorp.http.URI;
import org.ektorp.impl.StdObjectMapperFactory;
import org.ektorp.util.Assert;
import org.ektorp.util.Exceptions;

/**
 * @author lubo
 * 
 */
public class UpdateHandlerRequest {
    private final static ObjectMapper DEFAULT_MAPPER = new StdObjectMapperFactory().createObjectMapper();
    private final Map<String, String> queryParams = new TreeMap<String, String>();

    private final ObjectMapper mapper;

    private String dbPath;
    private String designDocId;
    private String functionName;
    private String docId;
    private String content;

    public UpdateHandlerRequest() {
        mapper = DEFAULT_MAPPER;
    }

    public UpdateHandlerRequest(ObjectMapper om) {
        Assert.notNull(om, "ObjectMapper may not be null");
        mapper = om;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public String getDbPath() {
        return dbPath;
    }

    public String getDesignDocId() {
        return designDocId;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getContent() {
        return content == null ? "" : content;
    }

    public UpdateHandlerRequest dbPath(String dbPath) {
        this.dbPath = dbPath;
        return this;
    }

    public UpdateHandlerRequest designDocId(String designDocId) {
        this.designDocId = designDocId;
        return this;
    }

    public UpdateHandlerRequest functionName(String functionName) {
        this.functionName = functionName;
        return this;
    }

    public UpdateHandlerRequest content(String content) {
        this.content = content;
        return this;
    }

    public UpdateHandlerRequest content(Object o) {
        try {
            content = mapper.writeValueAsString(o);
        } catch (Exception e) {
            throw Exceptions.propagate(e);
        }
        return this;
    }

    public UpdateHandlerRequest param(String name, String value) {
        queryParams.put(name, value);
        return this;
    }

    public UpdateHandlerRequest params(Map<String, String> params) {
        if (params != null) {
            queryParams.putAll(params);
        }
        return this;
    }

    private URI buildUpdateHandlerRequestPath() {
        Assert.hasText(dbPath, "dbPath");
        Assert.hasText(functionName, "functionName");
        Assert.hasText(designDocId, "designDocId");
        Assert.hasText(docId, "docId");

        URI uri = URI.of(dbPath);
        uri.append(designDocId)
                .append("_update")
                .append(functionName)
                .append(docId);

        return uri;
    }

    private void appendQueryParams(URI query) {
        for (Map.Entry<String, String> param : queryParams.entrySet()) {
            query.param(param.getKey(), param.getValue());
        }
    }

    public String buildRequestUri() {
        URI uri = buildUpdateHandlerRequestPath();
        if (queryParams != null && !queryParams.isEmpty()) {
            appendQueryParams(uri);
        }

        return uri.toString();
    }

    /**
     * @return
     */
    public String getDocId() {
        return docId;
    }

    public UpdateHandlerRequest docId(String docId) {
        this.docId = docId;
        return this;
    }

}
