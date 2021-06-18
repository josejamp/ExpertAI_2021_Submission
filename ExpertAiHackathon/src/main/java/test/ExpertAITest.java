package test;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ini4j.Ini;
import org.ini4j.IniPreferences;

import datamodels.ScrappedMessageInfo;
import expertai.ExpertAI;
import expertai.ExpertAIEngine;

public class ExpertAITest {
	
	public static void main(String args[]){
		
		Ini ini;
		try {
			ini = new Ini(new File(args[0]));
			
			java.util.prefs.Preferences prefs = new IniPreferences(ini);
			
			String NODE = prefs.node("NODES").get("credentials", null);
			List<String> credentials_to_use = Arrays.asList(NODE.split(","));
			
			String PATH_IN = prefs.node("FILES").get("PATH_IN", null);
			String PATH_OUT = prefs.node("FILES").get("PATH_OUT", null);
			
			List<File> files = Arrays.asList( new File(PATH_IN).listFiles());
			
			int partitionSize = files.size()/credentials_to_use.size();
			if(partitionSize < 1) {
				partitionSize = 1;
			}
			List<List<File>> partitions = new LinkedList<List<File>>();
			for (int i = 0; i < files.size(); i += partitionSize) {
				System.out.println(Integer.toString(i));
			    partitions.add(files.subList(i,
			            Math.min(i + partitionSize, files.size())));
			}
			
			int i = 0;
			for(List<File> list_files : partitions) {
				String node_to_use = credentials_to_use.get(i%(credentials_to_use.size()));
				
				List<ScrappedMessageInfo> scrappedMessages = new ArrayList<ScrappedMessageInfo>();
				List<String> textsToProcess = new ArrayList<String>();
				for(File f : list_files) {
					ExpertAIEngine engine = new ExpertAIEngine();
					engine.readOrigin(f.getAbsolutePath());
					scrappedMessages.add(engine.getJsonOrigin());
					textsToProcess.add(engine.getJsonOrigin().getText());
				}
				
				ExpertAI expertai = new ExpertAI();
				List<String> analyzedTexts = expertai.callUnderstanding(args[0], node_to_use, textsToProcess);
				
				int k = 0;
				for(String analyzedText : analyzedTexts) {
					ExpertAIEngine engine = new ExpertAIEngine();
					engine.setJsonOrigin(scrappedMessages.get(k));
					engine.saveAnalysis(analyzedText, PATH_OUT + "/" + scrappedMessages.get(k).getUuid());
					k++;
				}
				i++;
				System.out.println(i + "/" + list_files.size());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
