package ca.dal.cs.dalooc.android.webservices;

public interface OnSaveCourseCallDoneListener {

	public void returnServiceResponse(int serviceCode);
	
	public String getUrlWebService(int serviceCode);
	
}
