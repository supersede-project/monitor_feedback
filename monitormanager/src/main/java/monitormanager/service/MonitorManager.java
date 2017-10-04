package monitormanager.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.supersede.integration.api.monitoring.manager.types.MonitorSpecificConfiguration;
import eu.supersede.integration.api.monitoring.manager.types.AppStoreMonitorConfiguration;
import eu.supersede.integration.api.monitoring.manager.types.GooglePlayMonitorConfiguration;
import eu.supersede.integration.api.monitoring.manager.types.HttpMonitorConfiguration;
import eu.supersede.integration.api.monitoring.manager.types.TwitterMonitorConfiguration;
import eu.supersede.integration.api.monitoring.monitors.proxies.AppStoreMonitorProxy;
import eu.supersede.integration.api.monitoring.monitors.proxies.GooglePlayMonitorProxy;
import eu.supersede.integration.api.monitoring.monitors.proxies.HttpMonitorProxy;
import eu.supersede.integration.api.monitoring.monitors.proxies.TwitterMonitorProxy;

import monitormanager.model.ConfigurationParser;

@RequestMapping(value = "/")
@RestController
public class MonitorManager implements IMonitorManager {
		
	private ConfigurationParser parser = new ConfigurationParser();

	@Override
	@RequestMapping(value = "/{monitorName}/configuration", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	public JsonObject addConfiguration(@PathVariable String monitorName, @RequestBody JsonObject jsonObj) throws Exception {
		switch (monitorName) {
			case "Twitter":
				TwitterMonitorProxy<?,?> proxyT = new TwitterMonitorProxy<>();
				TwitterMonitorConfiguration confT = parser.getTwitterConfiguration(jsonObj);
				TwitterMonitorConfiguration resultT = proxyT.createMonitorConfiguration(confT);
				return getResponse(resultT);
			case "GooglePlay":
				GooglePlayMonitorProxy<?,?> proxyG = new GooglePlayMonitorProxy<>();
				GooglePlayMonitorConfiguration confG = parser.getGooglePlayConfiguration(jsonObj);
				GooglePlayMonitorConfiguration resultG = proxyG.createMonitorConfiguration(confG);
				return getResponse(resultG);
			case "AppStore":
				AppStoreMonitorProxy<?,?> proxyA = new AppStoreMonitorProxy<>();
				AppStoreMonitorConfiguration confA = parser.getAppStoreConfiguration(jsonObj);
				AppStoreMonitorConfiguration resultA = proxyA.createMonitorConfiguration(confA);
				return getResponse(resultA);
			case "Http":
				HttpMonitorProxy<?,?> proxyH = new HttpMonitorProxy<>();
				HttpMonitorConfiguration confH = parser.getHttpConfiguration(jsonObj);
				HttpMonitorConfiguration resultH = proxyH.createMonitorConfiguration(confH);
				return getResponse(resultH);
			default: throw new Exception("There is no monitor with this name");
		}
	}
	
	@Override
	@RequestMapping(value = "/{monitorName}/configuration/{confId}", method = RequestMethod.PUT)
	@ResponseStatus(value = HttpStatus.OK)
	public JsonObject updateConfiguration(@PathVariable String monitorName,
			@PathVariable int confId, @RequestBody JsonObject jsonObj) throws Exception {
		//JsonObject jsonObj = getJson(input);
		if (monitorName.equals("Twitter")) {
			TwitterMonitorProxy<?,?> proxy = new TwitterMonitorProxy<>();
			TwitterMonitorConfiguration conf = parser.getTwitterConfiguration(jsonObj);
			conf.setId(confId);
			TwitterMonitorConfiguration result = proxy.updateMonitorConfiguration(conf);
			return getResponse(result);
		} else if (monitorName.equals("GooglePlay")) {
			GooglePlayMonitorProxy<?,?> proxy = new GooglePlayMonitorProxy<>();
			GooglePlayMonitorConfiguration conf = parser.getGooglePlayConfiguration(jsonObj);
			conf.setId(confId);
			GooglePlayMonitorConfiguration result = proxy.updateMonitorConfiguration(conf);
			return getResponse(result);
		} else if (monitorName.equals("AppStore")) {
			AppStoreMonitorProxy<?,?> proxy = new AppStoreMonitorProxy<>();
			AppStoreMonitorConfiguration conf = parser.getAppStoreConfiguration(jsonObj);
			conf.setId(confId);
			AppStoreMonitorConfiguration result = proxy.updateMonitorConfiguration(conf);
			return getResponse(result);
		} else if (monitorName.contentEquals("Http")) {
			HttpMonitorProxy<?,?> proxy = new HttpMonitorProxy<>();
			HttpMonitorConfiguration conf = parser.getHttpConfiguration(jsonObj);
			HttpMonitorConfiguration result = proxy.updateMonitorConfiguration(conf);
			return getResponse(result);
		} else throw new Exception("There is no monitor with this name");
	}

	@Override
	@RequestMapping(value = "/{monitorName}/configuration/{confId}", method = RequestMethod.DELETE)
	@ResponseStatus(value = HttpStatus.OK)
	public void deleteConfiguration(@PathVariable String monitorName,
			@PathVariable int confId) throws Exception {
		if (monitorName.equals("Twitter")) {
			TwitterMonitorProxy<?,?> proxy = new TwitterMonitorProxy<>();
			TwitterMonitorConfiguration conf = new TwitterMonitorConfiguration();
			conf.setId(confId);
			proxy.deleteMonitorConfiguration(conf);
		} else if (monitorName.equals("GooglePlay")) {
			GooglePlayMonitorProxy<?,?> proxy = new GooglePlayMonitorProxy<>();
			GooglePlayMonitorConfiguration conf = new GooglePlayMonitorConfiguration();
			conf.setId(confId);
			proxy.deleteMonitorConfiguration(conf);
		} else if (monitorName.equals("AppStore")) {
			AppStoreMonitorProxy<?,?> proxy = new AppStoreMonitorProxy<>();
			AppStoreMonitorConfiguration conf = new AppStoreMonitorConfiguration();
			conf.setId(confId);
			proxy.deleteMonitorConfiguration(conf);
		} else if (monitorName.equals("Http")) {
			HttpMonitorProxy<?,?> proxy = new HttpMonitorProxy<>();
			HttpMonitorConfiguration conf = new HttpMonitorConfiguration();
			conf.setId(confId);
			proxy.deleteMonitorConfiguration(conf);
		} else throw new Exception("There is no monitor with this name");
	}
	
	private JsonObject getResponse(MonitorSpecificConfiguration result) {
		JsonObject json = new JsonObject();
		json.addProperty("idConf", result.getId());
		json.addProperty("status", "success");
		return json;
	}
	
}