package io.github.psokovykh.divin.xmlwrap;

import io.github.psokovykh.divin.core.RequestMessage;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Unsafe dummy structure like {@link RequestMessage}, only for marshalling/unmarshalling.
 */
@XmlRootElement(name = "request")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "priority", "targetPmName", "text" })
@SuppressWarnings("WeakerAccess")
public class RequestXML {

	private int priority;
	private String targetPmName;
	private String text;

	public RequestXML() {}

	public RequestXML(RequestMessage parent) {
		this();
		this.setPriority(parent.getPriority());
		this.setTargetPmName(parent.getTargetPmName());
		this.setText(parent.getText());
	}

	public int getPriority() {
		return this.priority;
	}

	public @NotNull String getTargetPmName() {
		return this.targetPmName;
	}

	public String getText() {
		return this.text;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public void setTargetPmName(String targetPmName){
		this.targetPmName = targetPmName;
	}

	public void setText(String text){
		this.text = text;
	}

}
