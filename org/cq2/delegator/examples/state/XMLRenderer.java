package org.cq2.delegator.examples.state;

public abstract class XMLRenderer  {
	
	private String result = "";
	
//	private InnerRenderer myRender = new OutsideTableRender();
	public void addToResult(String aresult) {
		result += aresult;
	}
	
	

	public String render() {
		return result;
	}

}
