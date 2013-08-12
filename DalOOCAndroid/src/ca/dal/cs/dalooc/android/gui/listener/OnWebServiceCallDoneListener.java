package ca.dal.cs.dalooc.android.gui.listener;

public interface OnWebServiceCallDoneListener {

	public void returnServiceResponse(int serviceCode, boolean resultOk);
	
	public String getUrlWebService(int serviceCode);
	
}
