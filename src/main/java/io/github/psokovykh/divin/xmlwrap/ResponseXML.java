package io.github.psokovykh.divin.xmlwrap;

import io.github.psokovykh.divin.core.ResponseMessage;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Unsafe dummy structure like {@link ResponseMessage}, only for marshalling/unmarshalling.
 */
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "priority", "text" })
@SuppressWarnings("WeakerAccess")
public class ResponseXML {

	private int priority;
	private String text;

	public ResponseXML() {}

	public ResponseXML(ResponseMessage parent) {
		this();
		this.setPriority(parent.getPriority());
		this.setText(parent.getText());
	}

	public int getPriority() {
		return this.priority;
	}


	public String getText() {
		return this.text;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public void setText(String text){
		this.text = text;
	}

}
