package ca.uhn.fhir.jpa.starter.cdshooks;

import ca.uhn.fhir.rest.api.server.cdshooks.CdsServiceRequestJson;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties({"extension"}) 
public class CdsHooksRequest extends CdsServiceRequestJson {
	private String lang;
	private Map<String,Object> fhirAuthorization;
	public CdsHooksRequest setLang(String lang) {
	    this.lang = lang;
	    return this;
	}
	public CdsHooksRequest setFhirAuthorization(Map<String, Object> authMap) {
	    this.fhirAuthorization = authMap; 
	    return this;
	}
}
