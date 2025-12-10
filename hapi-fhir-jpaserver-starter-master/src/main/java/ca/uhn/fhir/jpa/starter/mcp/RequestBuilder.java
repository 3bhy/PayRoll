package ca.uhn.fhir.jpa.starter.mcp;

import ca.uhn.fhir.context.FhirContext;
import com.google.gson.Gson;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.mock.web.MockHttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class RequestBuilder {

    private final FhirContext fhirContext;
    private final String resourceType;
    private final Interaction interaction;
    private final Map<String, Object> config;

    public RequestBuilder(FhirContext fhirContext, Map<String, Object> contextMap, Interaction interaction) {
        this.config = contextMap;

        if (interaction == Interaction.TRANSACTION) {
            this.resourceType = "";
        } else {
            Object rtObj = contextMap.get("resourceType");
            if (rtObj instanceof String) {
                String rt = (String) rtObj;
                if (!rt.isBlank()) {
                    this.resourceType = rt;
                } else {
                    throw new IllegalArgumentException("Missing or invalid 'resourceType' in contextMap");
                }
            } else {
                throw new IllegalArgumentException("Missing or invalid 'resourceType' in contextMap");
            }
        }

        this.interaction = interaction;
        this.fhirContext = fhirContext;
    }

    public MockHttpServletRequest buildRequest() {
        String basePath = "/" + resourceType;
        String method;
        MockHttpServletRequest req;

        switch (interaction) {
            case SEARCH: {
                method = "GET";
                req = new MockHttpServletRequest(method, basePath);
                Map<?, ?> sp = null;
                Object queryObj = config.get("query");
                if (queryObj instanceof Map<?, ?>) {
                    sp = (Map<?, ?>) queryObj;
                } else {
                    Object searchParamsObj = config.get("searchParams");
                    if (searchParamsObj instanceof Map<?, ?>) {
                        sp = (Map<?, ?>) searchParamsObj;
                    }
                }
                if (sp != null) {
                    sp.forEach((k, v) -> req.addParameter(k.toString(), v.toString()));
                }
                break;
            }
            case READ: {
                method = "GET";
                String id = requireString();
                req = new MockHttpServletRequest(method, basePath + "/" + id);
                break;
            }
            case CREATE:
            case TRANSACTION: {
                method = "POST";
                req = new MockHttpServletRequest(method, basePath);
                applyResourceBody(req);
                break;
            }
            case UPDATE: {
                method = "PUT";
                String id = requireString();
                req = new MockHttpServletRequest(method, basePath + "/" + id);
                applyResourceBody(req);
                break;
            }
            case DELETE: {
                method = "DELETE";
                String id = requireString();
                req = new MockHttpServletRequest(method, basePath + "/" + id);
                break;
            }
            case PATCH: {
                method = "PATCH";
                String id = requireString();
                req = new MockHttpServletRequest(method, basePath + "/" + id);
                applyPatchBody(req);
                break;
            }
            default:
                throw new IllegalArgumentException("Unsupported interaction: " + interaction);
        }

        req.setContentType("application/fhir+json");
        req.addHeader("Accept", "application/fhir+json");
        return req;
    }

    private void applyResourceBody(MockHttpServletRequest req) {
        Object resourceObj = config.get("resource");
        String json;
        if (resourceObj instanceof Map<?, ?>) {
            json = new Gson().toJson(resourceObj, Map.class);
        } else if (resourceObj instanceof String) {
            json = resourceObj.toString();
        } else {
            throw new IllegalArgumentException("Unsupported resource body type: " + resourceObj.getClass());
        }
        req.setContent(json.getBytes(StandardCharsets.UTF_8));
    }

    private void applyPatchBody(MockHttpServletRequest req) {
        Object patchBody = config.get("resource");
        if (patchBody == null) {
            throw new IllegalArgumentException("Missing 'resource' for patch interaction");
        }
        String content;
        if (patchBody instanceof String) {
            content = (String) patchBody;
        } else if (patchBody instanceof IBaseResource) {
            content = fhirContext.newJsonParser().encodeResourceToString((IBaseResource) patchBody);
        } else {
            throw new IllegalArgumentException("Unsupported patch body type: " + patchBody.getClass());
        }
        req.setContent(content.getBytes(StandardCharsets.UTF_8));
    }

    private String requireString() {
        Object val = config.get("id");
        if (val instanceof String) {
            String s = (String) val;
            if (!s.isBlank()) {
                return s;
            }
        }
        throw new IllegalArgumentException("Missing or invalid 'id'");
    }
}
