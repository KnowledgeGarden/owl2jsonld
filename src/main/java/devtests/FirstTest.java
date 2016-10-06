/**
 * 
 */
package devtests;

import java.util.*;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.topicquests.ontology.owl.Environment;
import org.topicquests.ontology.owl.jena.JenaMemoryModel;

/**
 * @author jackpark
 *
 */
public class FirstTest {
	private Environment environment;
	private JenaMemoryModel model;
	private String path = "data/pizza.owl.rdf"; //"data/nciOncology.owl"; //
	/**
	 * 
	 */
	public FirstTest() {
		environment = new Environment(new String[0]);
		model = environment.getJenaModel();
		OntModel ont = model.loadOwlFile(path);
		System.out.println("NS "+model.getPrefixMappings(ont));
		System.out.println("PROPS: "+model.listProperties(ont));
		List<OntClass> l = model.listClasses(ont);
		Iterator<OntClass>itr = l.iterator();
		OntClass oc;
		while (itr.hasNext()) {
			oc = itr.next();
			System.out.println("A "+model.listClassLabels(oc));
			System.out.println("B "+model.listClassComments(oc));
		}
		itr = l.iterator();
		while (itr.hasNext()) {
			oc = itr.next();
			System.out.println("C "+model.listSuperClasses(oc));
		}
		
		Individual ind;
		List<Individual>li = model.listIndividuals(ont);
		Iterator<Individual> iti = li.iterator();
		while (iti.hasNext()) {
			ind = iti.next();
			System.out.println("E "+model.listIndividualLabels(ind));
			System.out.println("F "+model.listIndividualComments(ind));
		}

		System.out.println("DID ");
	}

}
