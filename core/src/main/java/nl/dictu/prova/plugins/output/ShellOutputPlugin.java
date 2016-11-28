package nl.dictu.prova.plugins.output;

public interface ShellOutputPlugin extends OutputPlugin 
{
	void doExecute(String fileName) throws Exception;
}
