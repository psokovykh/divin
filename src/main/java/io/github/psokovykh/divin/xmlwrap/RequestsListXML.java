package io.github.psokovykh.divin.xmlwrap;

import io.github.psokovykh.divin.core.RequestMessage;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Unsafe dummy wrapper around List of {@link RequestXML}, only for marshalling/unmarshalling.
 */
@XmlRootElement(name = "requests")
@XmlAccessorType(XmlAccessType.FIELD)
public class RequestsListXML {

	public RequestsListXML(){}

	public RequestsListXML(Collection<RequestMessage> requests){
		this.setRequests(requests);
	}

	@XmlElement(name="request") //otherwise "s" will be postpended
	private List<RequestXML> requests;

	public List<RequestXML> getRequests() {
		return requests;
	}

	public void setRequests(Collection<RequestMessage> requests) {
		this.requests = requests.stream().sorted()
				.map(RequestXML::new)
				.collect(Collectors.toCollection(ArrayList::new));
	}
}
