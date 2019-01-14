package io.github.psokovykh.divin.debug;

import io.github.psokovykh.divin.core.RequestMessage;
import io.github.psokovykh.divin.core.ResponseMessage;
import io.github.psokovykh.divin.xmlwrap.RequestsListXML;
import io.github.psokovykh.divin.xmlwrap.ResponsesListXML;

import javax.xml.bind.annotation.*;
import java.util.Collection;

/**
 * XML model to save debug information, shown in window
 */
@XmlRootElement(name="debug-info")
@XmlAccessorType(XmlAccessType.FIELD)
public class DebugInfoXML {

	@XmlElement(name="requests")
	private RequestsListXML reqList;
	@XmlElement(name="responses")
	private ResponsesListXML resList;

	public DebugInfoXML(){};

	public DebugInfoXML(Collection<RequestMessage> reqList, Collection<ResponseMessage> resList){
		this.setReqList(reqList);
		this.setResList(resList);
	}

	public RequestsListXML getReqList() {
		return reqList;
	}

	public void setReqList(RequestsListXML reqList) {
		this.reqList = reqList;
	}

	public void setReqList(Collection<RequestMessage> reqList) {
		this.reqList = new RequestsListXML(reqList);
	}

	public ResponsesListXML getResList() {
		return resList;
	}

	public void setResList(ResponsesListXML resList) {
		this.resList = resList;
	}

	public void setResList(Collection<ResponseMessage> resList) {
		this.resList = new ResponsesListXML(resList);
	}
}
