/**
 * 
 */
package org.topicquests.ontology.owl.json;
import java.util.*;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.rdf.model.RDFNode;
import org.topicquests.ontology.owl.Environment;
import org.topicquests.ontology.owl.api.IJSONOwlOntology;
import org.topicquests.ontology.owl.jena.JenaMemoryModel;

import net.minidev.json.JSONObject;

/**
 * @author jackpark
 *
 */
public class JSONOwlModel {
	private Environment environment;
	private JenaMemoryModel jenaModel;

	/**
	 * 
	 */
	public JSONOwlModel(Environment env) {
		environment = env;
		jenaModel = environment.getJenaModel();
	}
	
	public JSONObject processOWLOntology(OntModel ontology) {
		JSONObject result = new JSONObject();
		JSONObject jo, jo2;
		//set the context
		Map<String,String>m = jenaModel.getPrefixMappings(ontology);
		if (m != null && !m.isEmpty()) {
			jo = new JSONObject();
			String key;
			Iterator<String> itr = m.keySet().iterator();
			while (itr.hasNext()) {
				key = itr.next();
				if (!key.equals("")) 
					jo.put(key, m.get(key));
			}
			result.put(IJSONOwlOntology.AT_CONTEXT, jo);
		}
		Iterator<OntClass>itr, itr2;
		Iterator<String>sitr;
		List<JSONObject>jList;
		List<JSONObject>labelList;
		List<JSONObject>commentList;
		List<String>superUriList;
		List<OntClass>supers;
		String x;
		String [] y;
		// process properties
		List<OntProperty> props = jenaModel.listProperties(ontology);
		if (!props.isEmpty()) {
			jList = new ArrayList<JSONObject>();
			Iterator<OntProperty>otr = props.iterator();
			while (otr.hasNext())
				jList.add(evaluateProperty(otr.next()));
			result.put(IJSONOwlOntology.PROPERTIES, jList);
		}
		// process classes
		List<OntClass> l = jenaModel.listClasses(ontology);
		if (!l.isEmpty()) {
			jList = new ArrayList<JSONObject>();
			itr = l.iterator();
			OntClass oc, oc2;
			while (itr.hasNext()) {
				oc = itr.next();
				x = jenaModel.getClassTypeURI(oc);
				//ignore restrictions
				//they are picked up in subclass lists
				if (x.indexOf("#Restriction") == -1) {
					jo = evaluateClass(oc);
					if (!jo.isEmpty())
						jList.add(jo);
				}
			}
			result.put(IJSONOwlOntology.ONT_CLASSES, jList);
			jList = null;
		}
		//process individuals
		Individual ind;
		List<Individual>li = jenaModel.listIndividuals(ontology);
		if (!li.isEmpty()) {
			jList = new ArrayList<JSONObject>();
			Iterator<Individual> iti = li.iterator();
			while (iti.hasNext()) {
				ind = iti.next();
				jList.add(evaluateIndividual(ind));
			}
			result.put(IJSONOwlOntology.ONT_INDIVIDUALS, jList);
			jList = null;
		}

		return result;
	}

	JSONObject evaluateClass(OntClass oc) {
		JSONObject jo = new JSONObject();
		List<List<JSONObject>> restrictionProperties = new ArrayList<List<JSONObject>>();
		List<OntClass>supers;
		List<String>sList;
		List<JSONObject>jList;
		Iterator<OntClass>itr, itr2;
		OntClass oc2;
		String x = jenaModel.getClassTypeURI(oc);
		String y = oc.getURI();
		//TODO Restrictions -- need to study these
		if (x != null && x.indexOf("#Restriction") > -1) {
			//jo = evaluateRestriction(oc.asRestriction());
		} else if (x != null && y != null) {
			
			jo.put(IJSONOwlOntology.URI, y);
			if (oc.isAnon()) {
				//It's a hidden node
				this.evaluateHiddenClass(oc, jo);
			
			} else {
				supers = jenaModel.listSuperClasses(oc);
				if (!supers.isEmpty()) {
					List<JSONObject> temp;
					itr2 = supers.iterator();
					sList = new ArrayList<String>();
					while (itr2.hasNext()) {
						oc2 = itr2.next();
						//TODO this may turn up restrictions
						temp = this.evaluateSuperClass(oc2, sList, jo);
						//System.out.println("X "+restrictionProperties+"\n"+temp);
						if (temp != null && !temp.isEmpty()) 
							restrictionProperties.add(temp);
							
					}
					jo.put(IJSONOwlOntology.SUPER_CLASSES, sList);
				}
				sList = null;
				sList = jenaModel.listClassLabels(oc); //reuse resource
				if (!sList.isEmpty()) {
					jList = evaluateText(sList);
					jo.put(IJSONOwlOntology.LABELS, jList);
				}
				sList = null;
				sList = jenaModel.listClassComments(oc); //reuse resource
				if (!sList.isEmpty()) {
					jList = evaluateText(sList);
					jo.put(IJSONOwlOntology.COMMENTS, jList);
				}
				//disjointWith picked up in properties
				//sList = jenaModel.listClassDisjointWith(oc);
				//if (!sList.isEmpty())
				//	jo.put(IJSONOwlOntology.DISJOINT_WITH, sList);
			}
			jList = jenaModel.listClassProperties(oc);
			if (!jList.isEmpty()) {
				restrictionProperties.add(jList);
				jo.put(IJSONOwlOntology.PROPERTIES, restrictionProperties);
			}
			jList = null;
			restrictionProperties = null;
		}
		
		return jo;		
	}
	
	void mergeRestrictionProperties(JSONObject target, JSONObject src) {
		List<JSONObject> tl = (List<JSONObject>)target.get(IJSONOwlOntology.PROPERTIES);
		List<JSONObject> sl = (List<JSONObject>)src.get(IJSONOwlOntology.PROPERTIES);
		mergeLists(tl, sl);
		target.put(IJSONOwlOntology.PROPERTIES, tl);
	}
	
	void mergeLists(List<JSONObject> tl, List<JSONObject> sl) {
		List<JSONObject> result = new ArrayList<JSONObject>();
		result.addAll(tl);
		result.addAll(sl);
		tl = result;
/*		JSONObject s;
		Iterator<JSONObject>itr = sl.iterator();
		while (itr.hasNext()) {
			s = itr.next();
			if (!tl.contains(s))
				tl.add(s);
		}*/
	}
	
	JSONObject evaluateIndividual(Individual ind) {
		JSONObject result = new JSONObject();
		String x = ind.getURI();
		if (x != null && !x.equals("")) 
			result.put(IJSONOwlOntology.URI, x);
		x = ind.getLocalName();
		if (x != null && !x.equals("")) 
			result.put(IJSONOwlOntology.LOCAL_NAME, x);
		List<JSONObject> props = jenaModel.listIndividualProperties(ind);
		if (!props.isEmpty())
			result.put(IJSONOwlOntology.PROPERTIES, props);
		return result;
	}
	
	List<JSONObject> evaluateText(List<String> list) {
		List<JSONObject>result = new ArrayList<JSONObject>();
		Iterator<String> sitr = list.iterator();
		String x;
		while (sitr.hasNext()) {
			x = sitr.next();
			result.add(splitString(x));
		}
		return result;
	}
	
	JSONObject evaluateProperty(OntProperty prop) {
		JSONObject jo = new JSONObject();
		String x = prop.getURI();
		if (x != null && !x.equals("")) 
			jo.put(IJSONOwlOntology.URI, x);
		List<String> ls = jenaModel.listPropertyComments(prop);
		if (!ls.isEmpty()) {
			jo.put(IJSONOwlOntology.COMMENTS, evaluateText(ls));
		}
		return jo;
	}
	
	List<JSONObject> evaluateRestriction(Restriction r) {
		//List<JSONObject> result = new ArrayList<JSONObject>();
		/*String x = r.getURI();
		if (x != null && !x.equals("")) 
			result.put(IJSONOwlOntology.URI, x);
		x = r.getLocalName();
		if (x != null && !x.equals("")) 
			result.put(IJSONOwlOntology.LOCAL_NAME, x);
		x = jenaModel.getClassTypeURI(r);
		if (x != null)
			result.put(IJSONOwlOntology.TYPE, x);
		List<String> ls = jenaModel.listRestrictionComments(r);
		if (!ls.isEmpty()) {
			result.put(IJSONOwlOntology.COMMENTS, evaluateText(ls));
		}
		ls = jenaModel.listRestrictionLabels(r);
		if (!ls.isEmpty()) {
			result.put(IJSONOwlOntology.LABELS, evaluateText(ls));
		}*/
		//List<OntClass> sups = jenaModel.listRestrictionSuperClasses(r);
		//List<OntProperty> props = jenaModel.listRestrictionProperties(r);
		//System.out.println("RestSups"+props);
		List<JSONObject> jList = jenaModel.listRestrictionProperties(r);
		//System.out.println("REST "+jList);
		//if (!jList.isEmpty())
		//	result.add(jList);
				
		return jList;
	}
	void evaluateHiddenClass(OntClass oc, JSONObject jo) {

		String x = oc.getLocalName();
		if (x != null)
			jo.put(IJSONOwlOntology.LOCAL_NAME, x);

	}
	
	/**
	 * Can return <code>null</code>
	 * Can populate a collection of properties associated with any restrictions
	 * @param oc
	 * @param sups
	 * @param jo
	 * @return
	 */
	List<JSONObject> evaluateSuperClass(OntClass oc, List<String>sups, JSONObject jo) {
		JSONObject r = null;
		if (oc.getURI() != null)
			sups.add(oc.getURI());
		else {
			if (oc.isRestriction()) {

				return evaluateRestriction(oc.asRestriction());
			} else
				sups.add("UNKNOWN");
		}
		return null;
	}
	
	/**
	 * Deal with language code on text
	 * @param text
	 * @return
	 */
	JSONObject splitString(String text) {
		JSONObject jo = new JSONObject();
		String lang = "en"; // default
		String val = text; // default
		if (text.indexOf('@') > -1) {
			String [] x = text.split("@");
			lang = x[1];
			val = x[0];
		}
		jo.put(IJSONOwlOntology.STRING, val);
		jo.put(IJSONOwlOntology.LANGUAGE, lang);
		return jo;
	}
}
