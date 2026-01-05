package common;

public class Request {
    private String command;   // Ex: LIST, PULL, START
    private String argument;  // Ex: "ubuntu:latest" ou "container_id"

    public Request() {}

    public Request(String command, String argument) {
        this.command = command;
        this.argument = argument;
    }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public String getArgument() { return argument; }
    public void setArgument(String argument) { this.argument = argument; }
}
