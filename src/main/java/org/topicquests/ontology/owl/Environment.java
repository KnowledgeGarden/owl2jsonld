/**
 * 
 */
package org.topicquests.ontology.owl;

import org.topicquests.ontology.owl.jena.JenaMemoryModel;
import org.topicquests.util.LoggingPlatform;

/**
 * @author jackpark
 *
 */
public class Environment {
	private LoggingPlatform log=null;
	private JenaMemoryModel jenaModel;

	/**
	 * if any <code>args</code> then process all files in /data
	 * @param args
	 */
	public Environment(String [] args) {
		log = LoggingPlatform.getInstance("logger.properties"); 
		jenaModel = new JenaMemoryModel(this);
		if (args.length > 0)
			doProcess();
	}

	void doProcess() {
		BulkProcessor bp = new BulkProcessor(this);
		bp.go();
	}
	public JenaMemoryModel getJenaModel() {
		return jenaModel;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Environment(args);
	}
	
	public void logDebug(String msg) {
		log.logDebug(msg);
	}

	public void logError(String msg, Exception e) {
		log.logError(msg, e);
	}


}
