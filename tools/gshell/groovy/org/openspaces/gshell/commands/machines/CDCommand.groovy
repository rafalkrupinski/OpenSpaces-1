package org.openspaces.gshell.commands.machines

import org.openspaces.gshell.Groovysh
import org.openspaces.gshell.command.support.CommandSupport
import org.codehaus.groovy.tools.shell.util.SimpleCompletor
import org.openspaces.admin.machine.Machines
import org.openspaces.admin.machine.Machine
import org.openspaces.gshell.ContextItem

/**
 * @author kimchy
 */
public class CDCommand extends CommandSupport {

    CDCommand(final Groovysh shell) {
        super(shell, 'cd', '\\c')
    }

    protected List createCompletors() {
        return [
            new CDCommandCompletor(shell.currentContext.value),
            null
        ]
    }
    
    public Object execute(List args) {
        assert args != null
        if (args.isEmpty()) {
            fail("Command 'cd' requires one") // TODO: i18n
        }
        def String command = args.first();
        if (command == "..") {
            shell.removeContext()
        } else {
            Machines machines = shell.currentContext.value
            Machine machine = machines.getHostsByName()[command]
            if (!machine) {
                io.out.println("Machine on host name @|bold,red ${command}| not found")
            } else {
                shell.cdToContext(new ContextItem(command, "machine", machine, { "(${machine.processingUnitInstances.length})"}))
            }
        }
    }
}

class CDCommandCompletor extends SimpleCompletor
{

    final Machines machines;

    CDCommandCompletor(Machines machines) {
        this.machines = machines;
    }

    public SortedSet getCandidates() {
        def result = []
        machines.each { Machine m -> result.add(m.operatingSystem.details.hostName) }
        setCandidateStrings result.toArray(new String[result.size()])
        super.getCandidates()
    }
}
