package org.onetwo.common.commandline;

public class ExitCommand extends AbstractCommand{
	
	public ExitCommand(){
		super("exit", "exit the system!");
	}

	@Override
	public void doExecute(CmdContext context) {
		System.exit(0);
	}

}
