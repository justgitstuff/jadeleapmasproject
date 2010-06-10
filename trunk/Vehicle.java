

import java.util.Iterator;

import jade.content.ContentManager;
import jade.content.abs.AbsAggregate;
import jade.content.abs.AbsConcept;
import jade.content.abs.AbsPredicate;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;
import jade.util.leap.Set;
import jade.util.leap.SortedSetImpl;

public class Vehicle extends Agent
{
	double viteza;
	int id_traseu;
	double id_deviatie;
	int timp_calculat;
	int nr_segmente;

	private traficGui myGui;
	private static final String vehicle_ID = "__vehicle__";
	private static final String vehicle_MANAGER_NAME = "manager";
	private Set participants = new SortedSetImpl();
	private Codec codec = new SLCodec();
	private Ontology onto = traficOntology.getInstance();
	private ACLMessage spokenMsg; 
	private Logger logger = Logger.getMyLogger(this.getClass().getName());
	
	
	protected void setup() {
		// Register language and ontology
		ContentManager cm = getContentManager();
		cm.registerLanguage(codec);
		cm.registerOntology(onto);
		cm.setValidationMode(false);
		
		// Add initial behaviours
		addBehaviour(new ParticipantsManager(this));
		addBehaviour(new traficListener(this));
		
		// Initialize the message used to convey spoken sentences
		spokenMsg = new ACLMessage(ACLMessage.INFORM);
		spokenMsg.setConversationId(vehicle_ID);
		
		// Activate the GUI
		//#MIDP_EXCLUDE_BEGIN
		myGui = new AWTtraficGui(this);
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		myGui = new MIDPChatGui(this);
		#MIDP_INCLUDE_END*/		
	}	
	
	protected void takeDown() {
		if (myGui != null) {
			myGui.dispose();
		}
	}
	
	
	class ParticipantsManager extends CyclicBehaviour {
		private MessageTemplate template;
		
		ParticipantsManager(Agent a) {
			super(a);
		}
		
		public void onStart() {
			// Subscribe as a chat participant to the ChatManager agent
			ACLMessage subscription = new ACLMessage(ACLMessage.SUBSCRIBE);
			subscription.setLanguage(codec.getName());
			subscription.setOntology(onto.getName());
			String convId = "C-"+myAgent.getLocalName();
			subscription.setConversationId(convId);
			subscription.addReceiver(new AID(vehicle_MANAGER_NAME, AID.ISLOCALNAME));
			myAgent.send(subscription);
			// Initialize the template used to receive notifications 
			// from the ChatManagerAgent
			template = MessageTemplate.MatchConversationId(convId);
		}
		
		public void action() {
			// Receives information about people joining and leaving 
			// the chat from the ChatManager agent
			ACLMessage msg = myAgent.receive(template);
			if (msg != null) {
				if (msg.getPerformative() == ACLMessage.INFORM) {
					try {
						AbsPredicate p = (AbsPredicate) myAgent.getContentManager().extractAbsContent(msg);
						if (p.getTypeName().equals(traficOntology.JOINED)) {
							// Get new participants, add them to the list of participants
							// and notify the gui
							AbsAggregate agg = (AbsAggregate) p.getAbsTerm(traficOntology.JOINED_WHO);
							if (agg != null) {
								Iterator it = agg.iterator();
								while (it.hasNext()) {
									AbsConcept c = (AbsConcept) it.next();
									participants.add(BasicOntology.getInstance().toObject(c));
								}
							}
							myGui.notifyParticipantsChanged(getParticipantNames());
						}
						if (p.getTypeName().equals(traficOntology.LEFT)) {
							// Get old participants, remove them from the list of participants
							// and notify the gui
							AbsAggregate agg = (AbsAggregate) p.getAbsTerm(traficOntology.JOINED_WHO);
							if (agg != null) {
								Iterator it = agg.iterator();
								while (it.hasNext()) {
									AbsConcept c = (AbsConcept) it.next();
									participants.remove(BasicOntology.getInstance().toObject(c));
								}
							}
							//myGui.notifyParticipantsChanged(getParticipantNames());
						}
					}
					catch (Exception e) {
						Logger.println(e.toString());
						e.printStackTrace();
					}
				}
				else {
					handleUnexpected(msg);
				}
			}
			else {
				block();
			}
		}
	}  // END of inner class ParticipantsManager
	
	/**
	   Inner class traficListener.
	   This behaviour registers as a chat participant and keeps the
	   list of participants up to date by managing the information 
	   received from the Manager agent.
	 */
	class traficListener extends CyclicBehaviour {
		private MessageTemplate template = MessageTemplate.MatchConversationId(vehicle_ID);
		
		traficListener(Agent a) {
			super(a);
		}
		
		public void action() {
			ACLMessage msg = myAgent.receive(template);
			if (msg != null) {
				if (msg.getPerformative() == ACLMessage.INFORM) {
					System.out.print(msg.getSender().getLocalName() + msg.getContent());
					//myGui.notifySpoken(msg.getSender().getLocalName(), msg.getContent());
				}
				else {
					handleUnexpected(msg);
				}
			}
			else {
				block();
			}
		}
	}  // END of inner class ChatListener
	
	/**
	   Inner class traficNotifier.
	   INFORMs other participants about a spoken sentence
	 */
	private class traficNotifier extends OneShotBehaviour {
		private String sentence;
		
		private traficNotifier(Agent a, String s) {
			super(a);
			sentence = s;
		}
		
		public void action() {
			spokenMsg.clearAllReceiver();
			Iterator it = participants.iterator();
			while (it.hasNext()) {
				spokenMsg.addReceiver((AID) it.next());
			}
			spokenMsg.setContent(sentence);
	    myGui.notifySpoken(myAgent.getLocalName(), sentence);
			send(spokenMsg);
		}
	}  // END of inner class 	
	
	//////////////////////////
	// Private utility methods
	//////////////////////////
	private String[] getParticipantNames() {
		String[] pp = new String[participants.size()];
		Iterator it = participants.iterator();
		int i = 0;
		while (it.hasNext()) {
			AID id = (AID) it.next();
			pp[i++] = id.getLocalName();
		}
		return pp;
	}	
	
	private void handleUnexpected(ACLMessage msg) {
		if(logger.isLoggable(Logger.WARNING)){
		logger.log(Logger.WARNING,"Unexpected message received from "+msg.getSender().getName());
		logger.log(Logger.WARNING,"Content is: "+msg.getContent());}
	}
}
