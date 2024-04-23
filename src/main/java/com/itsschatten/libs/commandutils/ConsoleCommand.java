package com.itsschatten.libs.commandutils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import com.itsschatten.libs.Utils;
import org.apache.commons.lang.Validate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class ConsoleCommand extends Command {

    @Setter
    private static String onlyConsole = "&cOnly console can use this command.";
    private CommandSender sender;
    private String[] args;

    /**
     * The command label.
     */
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private String commandLabel;

    /**
     * The prefix.
     */
    @Setter(AccessLevel.PROTECTED)
    private String prefix = Utils.getPrefix();

    public ConsoleCommand(String name) {
        super(name);
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        this.sender = sender;
        this.args = args;
        setCommandLabel(commandLabel.toLowerCase());

        if (sender instanceof Player) {
            tell(onlyConsole);
            return true;
        }

        try {
            run(sender, args);
        } catch (final ReturnedCommandException ex) {
            final String tellMessage = ex.tellMessage;

            tell(tellMessage);
        }

        return true;
    }

    /**
     * What runs the command.
     *
     * @param sender The sender.
     * @param args   The arguments for the command.
     */
    protected abstract void run(CommandSender sender, String[] args);

    /**
     * Ensure that an argument is actually a number.
     *
     * @param argsIndex    The argument location.
     * @param from         The minimum value.
     * @param to           The maximum value.
     * @param errorMessage The message to send if it doesn't match.
     * @return Returns the number (If valid)
     */
    protected int getNumber(int argsIndex, int from, int to, String errorMessage) {
        int number = 0;

        try {
            number = Integer.parseInt(args[argsIndex]);

            Validate.isTrue(number >= from && number <= to);

        } catch (final IllegalArgumentException ex) {
            returnTell(errorMessage.replace("{min}", from + "").replace("{max}", to + ""));
        }

        return number;

    }

    /**
     * An alteration of the above method, that doesn't take a from or to parameter.
     *
     * @param argsIndex    The numbers location.
     * @param errorMessage The message to send if it doesn't equal a number.
     * @return Returns the number (If valid)
     */
    protected int getNumber(int argsIndex, String errorMessage) {
        int number = 0;

        try {
            number = Integer.parseInt(args[argsIndex]);

        } catch (final IllegalArgumentException ex) {
            returnTell(errorMessage);
        }

        return number;

    }

    /**
     * Check if an object is equal to null, if so sends a message to a palyer.
     *
     * @param toCheck     The object to check.
     * @param nullMessage The message to send if it does equal null.
     */
    protected void checkNotNull(Object toCheck, String nullMessage) {
        if (toCheck == null)
            returnTell(nullMessage);
    }

    /**
     * Check the argument length.
     *
     * @param minLength The minimum length the arguments must be.
     * @param message   The message to send to someone if it doesn't work.
     */
    protected void checkArgs(int minLength, String message) {
        if (args.length < minLength)
            returnTell(message);
    }

    /**
     * Kind of an alteration of checkArgs
     *
     * @param requiredAmount The amount of arguments.
     * @param message        The message to send if it doesn't equal the arguments.
     */
    protected void checkArgsStrict(int requiredAmount, String message) {
        if (args.length != requiredAmount)
            returnTell(message);
    }

    /**
     * Send a message to a user and also return.
     *
     * @param message The message to send.
     */
    protected void returnTell(String message) {
        throw new ReturnedCommandException(message);
    }

    /**
     * Send a message to the CommandSender.
     *
     * @param message The message to send.
     */
    protected void tell(String message) {
        Utils.tell(sender, message);
    }

    /**
     * Tell another player a message.
     *
     * @param target The player to tell.
     * @param msg    The message to send.
     */
    protected void tellTarget(Player target, String msg) {
        Utils.tell(target, msg);
    }
    /**
     * Returns a command.
     */
    @RequiredArgsConstructor
    private final class ReturnedCommandException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        private final String tellMessage;

    }


}
