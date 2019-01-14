package io.github.psokovykh.divin.xmlwrap;

import io.github.psokovykh.divin.core.ResponseMessage;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Unsafe dummy wrapper around List of {@link ResponseXML}, only for marshalling/unmarshalling.
 */
@XmlRootElement(name = "responses")
@XmlAccessorType(XmlAccessType.FIELD)
public class ResponsesListXML {

	public ResponsesListXML(){}

	public ResponsesListXML(Collection<ResponseMessage> responses){
		this.setResponses(responses);
	}

	@XmlElement(name="response") //otherwise "s" will be postpended
	private List<ResponseXML> responses;

	public List<ResponseXML> getResponses() {
		return responses;
	}

	public void setResponses(Collection<ResponseMessage> responses) {
		this.responses = responses.stream().sorted()
				.map(ResponseXML::new)
				.collect(Collectors.toCollection(ArrayList::new));
	}
}
