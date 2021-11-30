import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.LinkedList;

/**
 * 
 * Wizard is the invoker of the commands
 *
 */
public class Wizard {

    private static final Logger LOGGER = LoggerFactory.getLogger(Wizard.class);

    private Deque<Command> undoStack = new LinkedList<>();
    private Deque<Command> redoStack = new LinkedList<>();

    public Wizard() {
      // comment to ignore sonar issue: LEVEL critical
    }

    /**
     * Cast spell
     */
    public void castSpell(Command command, Target target) {
      LOGGER.info("{} casts {} at {}", this, command, target);
      command.execute(target);
      undoStack.offerLast(command);
    }

    /**
     * Undo last spell
     */
    public void undoLastSpell() {
      if (!undoStack.isEmpty()) {
        Command previousSpell = undoStack.pollLast();
        redoStack.offerLast(previousSpell);
        LOGGER.info("{} undoes {}", this, previousSpell);
        previousSpell.undo();
      }
    }

    /**
     * Redo last spell
     */
    public void redoLastSpell() {
      if (!redoStack.isEmpty()) {
        Command previousSpell = redoStack.pollLast();
        undoStack.offerLast(previousSpell);
        LOGGER.info("{} redoes {}", this, previousSpell);
        previousSpell.redo();
      }
    }

    @Override
    public String toString() {
      return "Wizard";
    }
}
