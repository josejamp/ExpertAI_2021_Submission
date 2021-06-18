package utils;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.*;

public class JsonReader {

	private JSONObject value;

	public JsonReader(String value) {
		super();
		try {
			System.out.println("Before reading classifying JSON");
			System.out.println(value);
			this.value = new JSONObject(value);
		} catch (JSONException e) {
			this.value = new JSONObject();
			e.printStackTrace();
		}
	}

	public String getValue() {
		return value.toString();
	}

	public void setValue(String value) {
		try {
			this.value = new JSONObject(value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public String getDocID() {
		try {
			return this.value.getString("doc_id");
		} catch (JSONException e) {
			e.printStackTrace();
			return new String();
		}
	}

	public HashMap<String, Double> getTypology() {
		try {
			System.out.println(this.value);
			HashMap<String, Double> res = new HashMap<String, Double>();
			JSONArray arr = this.value.getJSONArray("classes");

			for (int i = 0; i < arr.length(); i++) {
				String category = arr.getJSONObject(i).getString("class_name");
				Double probability = arr.getJSONObject(i).getDouble("confidence");

				res.put(category, probability);
			}

			return res;

		} catch (JSONException e) {
			e.printStackTrace();
			return new HashMap<String, Double>();
		}
	}
	
	
	public HashMap<String, Double> getClasses() {
		try {
			//System.out.println(this.value);
			HashMap<String, Double> res = new HashMap<String, Double>();
			JSONArray arr = this.value.getJSONArray("images").getJSONObject(0).getJSONArray("classifiers");
			JSONArray arr_classes = arr.getJSONObject(0).getJSONArray("classes");

			for (int i = 0; i < arr_classes.length(); i++) {
				String category = arr_classes.getJSONObject(i).getString("class");
				Double probability = arr_classes.getJSONObject(i).getDouble("score");

				res.put(category, probability);
			}

			return res;

		} catch (JSONException e) {
			e.printStackTrace();
			return new HashMap<String, Double>();
		}
	}

	public JSONArray getEntities() {

		JSONArray arr = null;
		try {
			arr = this.value.getJSONArray("entities");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return arr;

	}

	public HashMap<String, ArrayList<String>> getMetadata(JSONArray arr) {

		HashMap<String, ArrayList<String>> res = new HashMap<String, ArrayList<String>>();
		for (int i = 0; i < arr.length(); i++) {

			try {
				String type = arr.getJSONObject(i).getString("type");
				String att = arr.getJSONObject(i).getString("text");
				
				
				if(!res.containsKey(type)) {
					
					ArrayList<String> text = new ArrayList<String>();
					text.add(att);
					res.put(type, text);
					
				}else {
					res.get(type).add(att);	
				}
				

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return res;

	}


	public JSONObject getEmotionalTone() {

		JSONObject emotionalTone = null;
		try {

			JSONObject document_tone = (JSONObject) this.value.get("document_tone");

			JSONArray tone_categories = (JSONArray) document_tone.get("tone_categories");

			emotionalTone = tone_categories.getJSONObject(0);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return emotionalTone;

	}
	
	public HashMap<String, Double> getPolarity(JSONObject emotionalTone) {

		HashMap<String, Double> res = new HashMap<String, Double>();
		
		JSONArray arr;
		try {
			arr = (JSONArray) emotionalTone.get("tones");
			for (int i = 0; i < arr.length(); i++) {

				try {
					String tone_id = arr.getJSONObject(i).getString("tone_id");
					Double score = arr.getJSONObject(i).getDouble("score");
					res.put(tone_id, score);

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	

		return res;

	}

	public String getLanguageTone() {

		JSONObject languageTone = null;
		try {

			JSONObject document_tone = (JSONObject) this.value.get("document_tone");

			JSONArray tone_categories = (JSONArray) document_tone.get("tone_categories");

			languageTone = tone_categories.getJSONObject(1);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return languageTone.toString();

	}

	public String getSocialTone() {

		JSONObject socialTone = null;
		try {

			JSONObject document_tone = (JSONObject) this.value.get("document_tone");

			JSONArray tone_categories = (JSONArray) document_tone.get("tone_categories");

			socialTone = tone_categories.getJSONObject(2);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return socialTone.toString();

	}

	public String getAudioText() {

		String transcript = "";

		try {
			
			JSONArray results = (JSONArray) this.value.get("results");
			
			for (int i = 0; i < results.length(); i++) {
				
				JSONObject arr = results.getJSONObject(i);
				JSONArray arr2 = (JSONArray) arr.get("alternatives");
				transcript = transcript + arr2.getJSONObject(0).get("transcript");
			
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(transcript);

		return transcript;

	}

}
