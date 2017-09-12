package com.francetelecom.faas.jenkinstuleapfaasbranchsource.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


import org.apache.commons.io.IOUtils;

public class OrangeForgeSettings {

	private String username;
	private String password;
	private String apiBaseUrl;
	private String FaaSProjectId;

	private static String fromHomeDir(){
		File homeDir = new File(System.getProperty("user.home"));
		File propFile = new File(homeDir, "orangeForge.properties");
		return propFile.getPath();
	}

	private static Properties fromPath(String propPath) throws IOException {
		Properties result = new Properties();
		FileInputStream thePropIn = null;
		try {
			thePropIn = new FileInputStream(propPath);
			result.load(thePropIn);
		} finally {
			IOUtils.closeQuietly(thePropIn);
		}
		return result;
	}

	public OrangeForgeSettings() throws IOException {
		this(fromHomeDir());
	}

	public OrangeForgeSettings(String propPath) throws IOException {
		this(fromPath(propPath));
	}

	public OrangeForgeSettings(Properties props){
		this.username = props.getProperty("orangeforge.username");
		this.password = props.getProperty("orangeforge.password");
		this.apiBaseUrl = props.getProperty("orangeforge.api-base-url");
		this.FaaSProjectId = props.getProperty("orangeforge.project-id");
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getApiBaseUrl() {
		return apiBaseUrl;
	}

	public void setApiBaseUrl(String apiBaseUrl) {
		this.apiBaseUrl = apiBaseUrl;
	}

	public String getFaaSProjectId() {
		return FaaSProjectId;
	}

	public void setFaaSProjectId(String faaSProjectId) {
		FaaSProjectId = faaSProjectId;
	}

}
