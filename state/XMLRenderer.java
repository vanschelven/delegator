package state;

public abstract class XMLRenderer  {
	
	private String result = "";
	
//	private InnerRenderer myRender = new OutsideTableRender();

	public void startTable() {
//		myRender = new InTableRender();
		result = result + "<table>";
	}

	public void startHeading(String text) {
		result += renderHeading(text);
			
	}

	public abstract String renderHeading(String text);
	public abstract String bla();
	//		return myRender.renderHeading(text);
	

	public void endTable() {
//		myRender = new OutsideTableRender();
		result = result + "</table>";
	}


	public String render() {
		return result;
	}

	public void startThing(String string) {
		result = result + string;
	}
}
