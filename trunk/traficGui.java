
/**
   @author Ancuta Iordache , Lorena Bacanu, Andrei Avram
 */
public interface traficGui {
	void notifyParticipantsChanged(String[] names);
	void notifySpoken(String speaker, String sentence);
	void dispose();
}
