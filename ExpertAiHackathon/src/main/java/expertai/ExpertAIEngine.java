package expertai;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import datamodels.ScrappedMessageInfo;
import utils.CustomScrappedMessageInfoDeserializer;


public class ExpertAIEngine {
	
	private ScrappedMessageInfo jsonOrigin;
	private String metadata;

	public ExpertAIEngine() {
	}

	public String getMetadata() {
		return metadata;
	}

	public void Metadata(String metadata) {
		this.metadata = metadata;
	}
	
	public ScrappedMessageInfo getJsonOrigin() {
		return jsonOrigin;
	}

	public void setJsonOrigin(ScrappedMessageInfo jsonOrigin) {
		this.jsonOrigin = jsonOrigin;
	}

	public void readOrigin(String path) {
	    try {
	    	File fileDir = new File(path);
	        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileDir), "UTF-8"));
	        ObjectMapper mapper = new ObjectMapper();
	        SimpleModule module =
	          new SimpleModule("CustomScrappedMessageInfoDeserializer", new Version(1, 0, 0, null, null, null));
	        module.addDeserializer(ScrappedMessageInfo.class, new CustomScrappedMessageInfoDeserializer());
	        mapper.registerModule(module);
	        ScrappedMessageInfo smi = mapper.readValue(br, ScrappedMessageInfo.class);
	        this.jsonOrigin = smi;
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	public void saveAnalysis(String analyzedText, String path) {
		if(!analyzedText.equals("NO PROCESABLE")){
			try {
					/*
				Gson gson = new GsonBuilder().disableHtmlEscaping().create();
				File fileDir = new File(path);
				FileOutputStream fileOutputStream = new FileOutputStream(fileDir);
				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
				this.jsonOrigin.setAnalyzedText(analyzedText);
				gson.toJson(this.jsonOrigin, outputStreamWriter);
				outputStreamWriter.close();
				fileOutputStream.close();
				*/
				Gson gson = new GsonBuilder().disableHtmlEscaping().create();
				JsonElement jelem = gson.fromJson(analyzedText, JsonElement.class);
				JsonObject jobj = jelem.getAsJsonObject();
				
				JsonObject combined = new JsonObject();
				String json_objeto = gson.toJson(this.jsonOrigin);
				JsonElement jelem_objeto = gson.fromJson(json_objeto, JsonElement.class);
				
				combined.add("scrapped_object", jelem_objeto);
				combined.add("expertai_info", jobj);
				
				JsonElement final_json = gson.fromJson(combined, JsonElement.class);
				
				File fileDir = new File(path);
				FileOutputStream fileOutputStream = new FileOutputStream(fileDir);
				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
				//this.jsonOrigin.setAnalyzedText(analyzedText);
				gson.toJson(final_json, outputStreamWriter);
				outputStreamWriter.close();
				fileOutputStream.close();
				
			} catch (JsonIOException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	

}
