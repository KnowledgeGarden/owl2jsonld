/**
 * 
 */
package org.topicquests.ontology.owl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import org.apache.jena.ontology.OntModel;
import org.topicquests.common.api.IResult;
import org.topicquests.ontology.owl.jena.JenaMemoryModel;
import org.topicquests.ontology.owl.json.JSONOwlModel;

/**
 * @author Admin
 *
 */
public class BulkProcessor {
	private Environment environment;
	private JenaMemoryModel model;
	private JSONOwlModel json;
	private final String source = "data";
	private final String target = "output/";

	/**
	 * 
	 */
	public BulkProcessor(Environment env) {
		environment = env;
		model = environment.getJenaModel();
		json = environment.getJSONModel();
	}

	public void go() {
		File dir = new File(source);
		File [] files = dir.listFiles();
		int len = files.length;
		File f;
		IResult r;
		OntModel ont;
		JSONObject jo;
		for (int i=0;i<len;i++) {
			f = files[i];
			System.out.println("PROCESSING "+f.getAbsolutePath());
			ont = model.loadOwlFile(f.getAbsolutePath());
			jo = json.processOWLOntology(ont);
			if (jo != null)
				saveDocument(f, jo.toJSONString());
		}
	}
	
	private String pathToFilename(String path) {
		StringBuilder buf = new StringBuilder(target);
		int where = path.lastIndexOf("/");
		if (where == -1)
			where = path.lastIndexOf("\\");
		
		int where2 = path.indexOf(".");
		String x = path.substring(where, where2);
		buf.append(x);
		buf.append(".json.gz");
		return buf.toString();
	}

	
	private void saveDocument(File fx, String doc) {
		String filename = pathToFilename(fx.getAbsolutePath());
		File f = new File(filename);
		System.out.println("Saving "+f.getPath());
		try {
			FileOutputStream fos = new FileOutputStream(f);
			PrintWriter out; 
			GZIPOutputStream gos = new GZIPOutputStream(fos);
			BufferedWriter bfw = new BufferedWriter(new OutputStreamWriter(gos, StandardCharsets.UTF_8));
			out = new PrintWriter(bfw);

			out.print(doc);
			out.flush();
			out.close();
			out = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
