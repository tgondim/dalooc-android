package ca.dal.cs.dalooc.android.util;

public class General {

	//gets the canonical path file name and aplies the id name on it
	public static String getIdFileName(String originalFileName, String id) {
		return originalFileName.substring(0, originalFileName.lastIndexOf("/")) + "/" 
    			+ id + originalFileName.substring(originalFileName.lastIndexOf("."));
	}
}
