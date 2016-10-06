/**
 * 
 */
package devtests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.riot.IO_Jena;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.topicquests.ontology.owl.Environment;
import org.topicquests.ontology.owl.jena.JenaMemoryModel;

/**
 * @author Admin
 *
 */
public class ThirdTest {
	private Environment environment;
	private JenaMemoryModel model;
	private String path =  "data/nciOncology.owl"; // "data/pizza.owl.rdf";//"data/genealogy.owl"; // //

	/**
	 * 
	 */
	public ThirdTest() {
		environment = new Environment(new String[0]);
		model = environment.getJenaModel();
		OntModel ont = model.loadOwlFile(path);
		saveDocument(ont);
	}
	
	void saveDocument(OntModel ont) {
		String filename = pathToFilename();
		File f = new File(filename);
		System.out.println("Saving "+f.getPath());
		try {
			FileOutputStream fos = new FileOutputStream(f);
			PrintWriter out; 
			GZIPOutputStream gos = new GZIPOutputStream(fos);
			RDFDataMgr.write(gos, ont, Lang.JSONLD);
			gos.flush();
			gos.close();
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
