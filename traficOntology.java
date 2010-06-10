
import jade.content.onto.*;
import jade.content.schema.*;
import jade.content.abs.*;

/**
   Ontology containing concepts, predicates and actions used 
   within the chat application.
   @author Ancuta Iordache, Lorena Bacanu, Andrei Avram
 */
public class traficOntology extends Ontology implements traficVocabulary {
  
  // The singleton instance of this ontology
	private static Ontology theInstance = new traficOntology();
	
	public static Ontology getInstance() {
		return theInstance;
	}
	
  /**
   * Constructor
   */
  private traficOntology() {
  	super(ONTOLOGY_NAME, BasicOntology.getInstance(), null);

    try {
    	add(new PredicateSchema(JOINED));
    	add(new PredicateSchema(LEFT));
    	add(new PredicateSchema(MOVE));
    	
    	PredicateSchema ps = (PredicateSchema) getSchema(JOINED);
    	ps.add(MOVE_TO, (ConceptSchema) getSchema(BasicOntology.AID), 0, ObjectSchema.UNLIMITED);

    	ps = (PredicateSchema) getSchema(LEFT);
    	ps.add(LEFT_WHO, (ConceptSchema) getSchema(BasicOntology.AID), 0, ObjectSchema.UNLIMITED);

    	ps = (PredicateSchema) getSchema(MOVE);
    	ps.add(MOVE_TO, (PrimitiveSchema) getSchema(BasicOntology.STRING));
    } 
    catch (OntologyException oe) {
    	oe.printStackTrace();
    } 
	}

}
