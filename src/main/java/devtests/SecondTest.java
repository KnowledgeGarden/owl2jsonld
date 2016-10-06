/**
 * 
 */
package devtests;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

import org.apache.jena.ontology.OntModel;
import org.topicquests.ontology.owl.Environment;
import org.topicquests.ontology.owl.jena.JenaMemoryModel;
import org.topicquests.ontology.owl.json.JSONOwlModel;

import net.minidev.json.JSONObject;

/**
 * @author jackpark
 *
 */
public class SecondTest {
	private Environment environment;
	private JenaMemoryModel model;
	private String path = "data/nciOncology.owl"; // "data/pizza.owl.rdf";//"data/genealogy.owl"; // //
	private JSONOwlModel json;
	/**
	 * 
	 */
	public SecondTest() {
		environment = new Environment(new String[0]);
		model = environment.getJenaModel();
		json = environment.getJSONModel();
		OntModel ont = model.loadOwlFile(path);
		JSONObject jo = json.processOWLOntology(ont);
		//System.out.println(jo.toJSONString());
		saveDocument(jo.toJSONString());
	}

	void saveDocument(String doc) {
		String filename = pathToFilename();
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
	
	String pathToFilename() {
		StringBuilder buf = new StringBuilder();
		int where = path.indexOf("/")+1;
		int where2 = path.indexOf(".");
		buf.append(path.substring(where, where2));
		buf.append(".json.gz");
		return buf.toString();
	}
}
