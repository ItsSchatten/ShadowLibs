package com.itsschatten.libs.commandutils;

import com.itsschatten.libs.interfaces.IPermissions;
import com.itsschatten.libs.Utils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.Validate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * The class that will run a command for a player.
 */
public abstract class UserCommand extends Command {

    @Setter
    private static String onlyPlayer = "&cI'm sorry only players can use this command.";

    private Player player;

    private String[] args;

    /**
     * The command label.
     */
    @Getter
    @Setter(value = AccessLevel.PRIVATE)
    private String commandLabel;

    /**
     * The prefix.
     */
    @Setter(value = AccessLevel.PROTECTED)
    private String prefix = Utils.getPrefix();

    /**
     * The constructor of the class.
     *
     * @param name The command name.
     */
    public UserCommand(String name) {
        super(name);
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            tell(onlyPlayer);
            return true;
        }

        final Player player = (Player) sender;
        this.player = player;

        setCommandLabel(commandLabel.toLowerCase());
        this.args = args;

        try {
            run(player, args);
        } catch (final ReturnedCommandException ex) {
            final String tellMessage = ex.tellMessage;

            tell(tellMessage);
        }

        return true;
    }

    /**
     * What runs the command.
     *
     * @param player The player.
     * @param args   The arguments for the command.
     */
    protected abstract void run(Player player, String[] args);

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
     * @return Returns the number (If not valid 0)
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
     * Kind of an alteration of the checkArgs.
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
     * @throws ReturnedCommandException When the command is returned this is thrown.
     */
    protected void returnTell(String message) {
        throw new ReturnedCommandException(message);
    }

    /**
     * Send a message to a player.
     *
     * @param message The message to send.
     */
    protected void tell(String message) {
        Utils.tell(player, message);
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
     * Check if the person executing the command has permission to do so.
     *
     * @param player The player to check if they have permission for.
     * @param perms  The permission, usually an enumeration in my case, to run the command.
     */
    protected void checkPerms(Player player, IPermissions perms) {
        if (!player.hasPermission(perms.getPermission()))
            returnTell(Utils.getNoPermsMessage().replace("{prefix}", prefix).replace("{permission}", perms.getPermission()));
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
