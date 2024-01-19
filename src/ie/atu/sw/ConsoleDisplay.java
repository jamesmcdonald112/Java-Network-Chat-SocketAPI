/**
 * @author James McDonald
 */

package ie.atu.sw;

/**
 * ConsoleDisplay provides methods for displaying different types of messages in the console.
 * It includes functionalities for displaying messages, prompts, error messages, and confirmation messages with color coding.
 */
public class ConsoleDisplay {

    /**
     * Displays a standard message in yellow color.
     *
     * @param message The message to be displayed.
     */
    public static void displayMessage(String message) {
        System.out.println(ConsoleColour.YELLOW + message + ConsoleColour.RESET);
    }

    /**
     * Displays a prompt message in blue color.
     *
     * @param message The prompt message to be displayed.
     */
    public static void displayPrompt(String message) {
        System.out.print(ConsoleColour.BLUE + message + ConsoleColour.RESET);
    }

    /**
     * Displays a message in a specified color.
     *
     * @param message The message to be displayed.
     * @param color   The color in which the message will be displayed.
     */
    public static void displayColoredMessage(String message, ConsoleColour color) {
        System.out.println(color + message + ConsoleColour.RESET);
    }

    /**
     * Displays an error message in red color.
     *
     * @param message The error message to be displayed.
     */
    public static void displayErrorMessage(String message) {
        System.out.println(ConsoleColour.RED + message + ConsoleColour.RESET);
    }

    /**
     * Displays a confirmation message in green color.
     *
     * @param message The confirmation message to be displayed.
     */
    public static void displayConfirmationMessage(String message) {
        System.out.println(ConsoleColour.GREEN + message + ConsoleColour.RESET);
    }
}
