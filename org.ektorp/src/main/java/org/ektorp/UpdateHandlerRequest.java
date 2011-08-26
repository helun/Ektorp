/**
 * 
 */
package org.ektorp;

import java.util.Map;
import java.util.TreeMap;

import org.ektorp.http.URI;
import org.ektorp.util.Assert;

/**
 * @author lubo
 * 
 */
public class UpdateHandlerRequest {
    private final Map<String, String> queryParams = new TreeMap<String, String>();

    private String dbPath;
    private String designDocId;
    private String functionName;
    private String docId;
    private Object body;

    public String getDbPath() {
        return dbPath;
    }

    public String getDesignDocId() {
        return designDocId;
    }

    public String getFunctionName() {
        return functionName;
    }

    public Object getBody() {
        return body == null ? "" : body;
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

    public UpdateHandlerRequest body(Object body) {
        this.body = body;
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
