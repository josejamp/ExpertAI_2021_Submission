package expertai;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ini4j.Ini;
import org.ini4j.IniPreferences;

import ai.expert.nlapi.security.Authentication;
import ai.expert.nlapi.security.Authenticator;
import ai.expert.nlapi.security.BasicAuthenticator;
import ai.expert.nlapi.security.DefaultCredentialsProvider;
import ai.expert.nlapi.v2.API;
import ai.expert.nlapi.v2.cloud.Analyzer;
import ai.expert.nlapi.v2.cloud.AnalyzerConfig;
import ai.expert.nlapi.v2.message.AnalyzeResponse;

public class ExpertAI {
	
	public static Authentication createAuthentication() throws Exception {
        DefaultCredentialsProvider credentialsProvider = new DefaultCredentialsProvider();
        Authenticator authenticator = new BasicAuthenticator(credentialsProvider);
        return new Authentication(authenticator);
    }
	
	public static Analyzer createAnalyzer() throws Exception {
        return new Analyzer(AnalyzerConfig.builder()
                                          .withVersion(API.Versions.V2)
                                          .withContext("standard")
                                          .withLanguage(API.Languages.en)
                                          .withAuthentication(createAuthentication())
                                          .build());
    }
	
	protected static void setEnv(Map<String, String> newenv) throws Exception {
	  	  try {
	  	    Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
	  	    Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
	  	    theEnvironmentField.setAccessible(true);
	  	    Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
	  	    env.putAll(newenv);
	  	    Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
	  	    theCaseInsensitiveEnvironmentField.setAccessible(true);
	  	    Map<String, String> cienv = (Map<String, String>)     theCaseInsensitiveEnvironmentField.get(null);
	  	    cienv.putAll(newenv);
	  	  } catch (NoSuchFieldException e) {
	  	    Class[] classes = Collections.class.getDeclaredClasses();
	  	    Map<String, String> env = System.getenv();
	  	    for(Class cl : classes) {
	  	      if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
	  	        Field field = cl.getDeclaredField("m");
	  	        field.setAccessible(true);
	  	        Object obj = field.get(env);
	  	        Map<String, String> map = (Map<String, String>) obj;
	  	        map.clear();
	  	        map.putAll(newenv);
	  	      }
	  	    }
	  	  }
	  	}
	
	public List<String> callUnderstanding(String fnConf, String node, List<String> texts) {
		 
		try {

			Ini ini = new Ini(new File(fnConf));
			java.util.prefs.Preferences prefs = new IniPreferences(ini);
			//System.out.println(prefs.node(node).get("user", null));
			//System.out.println(prefs.node(node).get("password", null));
			System.setProperty("EAI_USERNAME", prefs.node(node).get("user", null));
		    System.setProperty("EAI_PASSWORD", prefs.node(node).get("password", null));
		    
		    Map<String,String> newenv = new HashMap<String, String>();
		    newenv.put("EAI_USERNAME", prefs.node(node).get("user", null));
		    newenv.put("EAI_PASSWORD", prefs.node(node).get("password", null));
		    setEnv(newenv);
		    
		    List<String> results = new ArrayList<String>();
            Analyzer analyzer = createAnalyzer();
            for(String text : texts) {
            	try {
		            AnalyzeResponse result = analyzer.analyze(text);
		            System.out.println( result.toJSON());
		            results.add(result.toJSON());
            	}
            	catch(Exception e) {
            		System.out.println(e);
            	}
            }
		    return results;
		}
		catch(Exception e) {
			return new ArrayList<String>();
		}
	  }
	

}
