/**
 * 
 */
package org.topicquests.ontology.owl.jena;
import java.util.*;
import org.topicquests.common.ResultPojo;
import org.topicquests.common.api.IResult;
import org.topicquests.ontology.owl.Environment;

import net.minidev.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NsIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;

/**
 * @author jackpark
 * Based on Jena Pizza example
 */
public class JenaMemoryModel {
	private Environment environment;
	/**
	 * 
	 */
	public JenaMemoryModel(Environment env) {
		environment = env;
	}

	public OntModel loadOwlFile(String filePath) {
		OntModel model = getModel();
		FileManager.get().readModel( model, filePath );
		return model;
	}
	
	protected OntModel getModel() {
        return ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
    }
	
	/////////////////////////
	// Ontology handling
	/////////////////////////
	public List<OntClass> listClasses(OntModel model) {
		List<OntClass> result =  model.listClasses().toList();
		return result;
	}
	
	public List<Individual> listIndividuals(OntModel model) {
		List<Individual> result =  model.listIndividuals().toList();
		return result;
	}
	
	public List<OntProperty> listProperties(OntModel model) {
		List<OntProperty> result =	model.listAllOntProperties().toList();
		return result;
	}

	public void foo (OntModel model, String id) {
		//model.ge
	}
	/**
	 * @deprecated
	 * @param model
	 * @return
	 */
	public List<String> listNameSpaces(OntModel model) {
		List<String> result = model.listNameSpaces().toList();
		return result;
	}
	
	////////////////////////////////
	// from test on pizza.owl, this returns
	// NS {=http://www.co-ode.org/ontologies/pizza/pizza.owl#, 
	// pizza=http://www.co-ode.org/ontologies/pizza/pizza.owl#, 
	// owl=http://www.w3.org/2002/07/owl#, 
	// rdf=http://www.w3.org/1999/02/22-rdf-syntax-ns#, 
	// owl11=http://www.w3.org/2006/12/owl11#, 
	// xsd=http://www.w3.org/2001/XMLSchema#, 
	// rdfs=http://www.w3.org/2000/01/rdf-schema#, 
	// owl11xml=http://www.w3.org/2006/12/owl11-xml#}
	////////////////////////////////

	
	public Map<String,String> getPrefixMappings(OntModel model) {
		return model.getNsPrefixMap();
	}
	/////////////////////////
	// Property Handling
	/////////////////////////

	public List<String> listPropertyComments(OntProperty ontProp) {
		List<String> result = new ArrayList<String>();
		
		ExtendedIterator<RDFNode> itr = ontProp.listComments(null);
		while (itr.hasNext())
			result.add(itr.next().toString());		
		return result;
	}
	//ToDo inverseOf, domain, range, subPropertyOf types (not just type)...
	
	/////////////////////////
	// Restriction handling
	/////////////////////////
	
	public List<JSONObject> listRestrictionProperties(Restriction i) {
		List<JSONObject> result = new ArrayList<JSONObject>();
		
		StmtIterator itr =	i.listProperties();
		Statement s;
		Property p;
		RDFNode n;
		JSONObject jo;
		String uri;
		boolean isreverse = false;
		while (itr.hasNext()) {
			s = itr.next();
			jo = new JSONObject();
			p = s.getPredicate();
			uri = p.getURI();
			if (uri.indexOf("ns#type") == -1 &&
				uri.indexOf("owl#Class") == -1) {
				jo.put("predicate", p.getURI());
				if (uri.indexOf("onProperty") > -1)
					isreverse = true;
				n = s.getObject();
				jo.put("value", n.toString());
				result.add(jo);
			}
			jo = null;
		}
		if (isreverse && result.size() == 2) {
			jo = result.get(1);
			result.set(1, result.get(0));
			result.set(0, jo);
		}
			
		//System.out.println("RP "+result);
		
		return result;
	}
	

	public List<String> listRestrictionComments(Restriction ontProp) {
		List<String> result = new ArrayList<String>();
		ExtendedIterator<RDFNode> itr = ontProp.listComments(null);
		while (itr.hasNext())
			result.add(itr.next().toString());		
		return result;
	}
	
	public List<String> listRestrictionLabels(Restriction ontClass) {
		List<String> result = new ArrayList<String>();
		ExtendedIterator<RDFNode> itr = ontClass.listLabels(null);
		while (itr.hasNext())
			result.add(itr.next().toString());
		return result;
	}

	public List<OntClass> listRestrictionSuperClasses(Restriction r) {
		List<OntClass> result = new ArrayList<OntClass>();
		ExtendedIterator<OntClass> itr = r.listSuperClasses();
		while (itr.hasNext()) 
			result.add(itr.next());
		return result;
	}
	
	public List<OntProperty> listRestrictionDeclaredProperties(Restriction r) {
		List<OntProperty> result = new ArrayList<OntProperty>();
		
		ExtendedIterator<OntProperty> itr =	r.listDeclaredProperties();
		while (itr.hasNext()) 
			result.add(itr.next());
		
		return result;
	}


	/////////////////////////
	// Class handling
	// http://www.programcreek.com/java-api-examples/index.php?api=com.hp.hpl.jena.rdf.model.RDFNode
	// to deal with language issues in labels and comments
	// TODO we start with toString, but looking for instances
	// to test language
	// HERE is what toString gives:
	//[CoberturaDeAspargos@pt]
	//[PizzaDeCarne@pt]
	//[Any pizza that has at least one meat topping@en]
	//TODO this means plucking out the language code later
	/////////////////////////

	public String getLocalName(OntClass ontClass) {
		return ontClass.getLocalName();
	}
	public List<String> listClassLabels(OntClass ontClass) {
		List<String> result = new ArrayList<String>();
		ExtendedIterator<RDFNode> itr = ontClass.listLabels(null);
		while (itr.hasNext())
			result.add(itr.next().toString());
		return result;
	}
	
	public List<OntProperty> listClassDeclaredProperties(OntClass ontClass) {
		List<OntProperty> result = new ArrayList<OntProperty>();
		
		ExtendedIterator<OntProperty> itr =	ontClass.listDeclaredProperties();
		while (itr.hasNext()) 
			result.add(itr.next());
		
		return result;
	}
	public List<JSONObject> listClassProperties(OntClass ontClass) {
		List<JSONObject> result = new ArrayList<JSONObject>();
		
		StmtIterator itr =	ontClass.listProperties();
		Statement s;
		Property p;
		RDFNode n;
		JSONObject jo;
		String uri;
		while (itr.hasNext()) {
			s = itr.next();
			jo = new JSONObject();
			p = s.getPredicate();
			uri = p.getURI();
			if (uri != null && uri.indexOf("subClassOf") == -1
					&& uri.indexOf("label") == -1
					&& uri.indexOf("comment") == -1) {
				jo.put("predicate", p.getURI());
				n = s.getObject();
				uri = n.toString();
				if (uri.indexOf("owl#Class") == -1) {
					jo.put("value", n.toString());
					result.add(jo);
				}
			}
			jo = null;
		}
		
		return result;
	}

	
	public List<String> listClassDisjointWith(OntClass ontClass) {
		List<String> result = new ArrayList<String>();
		ExtendedIterator<OntClass> itr = ontClass.listDisjointWith();
		OntClass oc;
		while (itr.hasNext()) {
			oc = itr.next();
			result.add(getClassURI(oc));
		}
		return result;
	}
	public List<String> listClassComments(OntClass ontClass) {
		List<String> result = new ArrayList<String>();
		ExtendedIterator<RDFNode> itr = ontClass.listComments(null);
		while (itr.hasNext())
			result.add(itr.next().toString());		
		return result;
	}

	
	public List<OntClass> listSuperClasses(OntClass ontClass) {
		List<OntClass> result = new ArrayList<OntClass>();
		ExtendedIterator<OntClass> itr = ontClass.listSuperClasses();
		while (itr.hasNext()) 
			result.add(itr.next());
		return result;
	}
	
	public String getClassURI(OntClass ontClass) {
		return ontClass.getURI();
	}
	
	/**
	 * Only valid for 
	 * @param ontClass
	 * @return
	 */
	public String getClassId (OntClass ontClass) {
		try {
			AnonId r = ontClass.getId();
			return r.getLabelString();
		} catch (Exception e) {}
		return null;
	}
	/**
	 * Return this class RDFType URI
	 * @param ontClass
	 * @return can return <code>null</code>
	 */
	public String getClassTypeURI(OntClass ontClass) {
		if (ontClass.getRDFType() == null)
			return null;
		return ontClass.getRDFType().getURI();
	}
	/////////////////////////
	// Individual handling
	/////////////////////////

	public List<JSONObject> listIndividualProperties(Individual i) {
		List<JSONObject> result = new ArrayList<JSONObject>();
		
		StmtIterator itr =	i.listProperties();
		Statement s;
		Property p;
		RDFNode n;
		JSONObject jo;
		while (itr.hasNext()) {
			s = itr.next();
			jo = new JSONObject();
			p = s.getPredicate();
			jo.put("predicate", p.getURI());
			n = s.getObject();
			jo.put("value", n.toString());
			result.add(jo);
		}
		
		return result;
	}

	public List<String> listIndividualLabels(Individual ontClass) {
		List<String> result = new ArrayList<String>();
		ExtendedIterator<RDFNode> itr = ontClass.listLabels(null);
		while (itr.hasNext())
			result.add(itr.next().toString());		
		return result;
	}
	
	public List<String> listIndividualComments(Individual ontClass) {
		List<String> result = new ArrayList<String>();
		ExtendedIterator<RDFNode> itr = ontClass.listComments(null);
		while (itr.hasNext())
			result.add(itr.next().toString());
		return result;
	}
	
	public String getIndividualURI(Individual ontClass) {
		return ontClass.getURI();
	}
	public String getTypeURI(Individual ontClass) {
		return ontClass.getRDFType().getURI();
	}
}
