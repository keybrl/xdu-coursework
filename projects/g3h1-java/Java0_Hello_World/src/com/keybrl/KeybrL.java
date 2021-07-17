package com.keybrl;
/**A class about KeybrL(myself).
 * @author Luo Y.H. (16130120191)
 * @author keyboard-l@outlook.com
 */

import java.util.Random;
import java.util.Scanner;

public class KeybrL {
    private String name = "KeybrL";
    private String maxim = "hhh";
    private String thought = "";
    KeybrL(String name) {
        this.name = name;
    }
    KeybrL(String name, String maxim) {
        if (!name.equals("")) {
            this.name = name;
        }
        if (!maxim.equals("")) {
            this.maxim = maxim;
        }
    }

    private String think_core(String thought_pond, int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < length; i++) {
            int number = random.nextInt(thought_pond.length());
            sb.append(thought_pond.charAt(number));
        }
        return sb.toString();
    }
    public String think() {
        this.thought = think_core("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", 10 + (new Random()).nextInt(5));
        return this.thought;
    }
    public String think(String thought_pond) {
        this.thought = think_core(thought_pond, 10 + (new Random()).nextInt(5));
        return this.thought;
    }

    private void say(String something) {
        System.out.println(this.name + ": " + something);
    }
    private void say_continue(String something) {
        for (int i = 0; i < this.name.length() + 2; i++) {
            System.out.print(" ");
        }
        System.out.println(something);
    }
    public void talk_thought() {
        say(this.thought);
    }
    public void talk_something() {
        say(think());
    }
    public void say_maxim() {
        say(this.maxim);
    }
    public void say_hello() {
        say("Hello World!");
    }
    public void say_hello(String someone) {
        say("Hello " + someone + "!");
    }
    public void listen_then_talk() {
        Scanner scanner = new Scanner(System.in);
        say("Hi Stranger!");
        while(true) {
            System.out.println();
            System.out.print("You: ");
            String something = scanner.nextLine();
            System.out.println();
            if (something.equals("")) {
                continue;
            }
            else if (something.equals("What can you do?")) {
                say("You can ask me any question and say anything to me,");
                say("For example, \"Hello\", \"Hi\", \"Nice to meet you\", \"Goodbye\", \"Bye\", ");
                say_continue("\"What's your name?\", \"My name is xxx\", \"How are you?\", \"Introduce yourself\"");
                say_continue("\"What are you thinking about?\", \"Do you know xxx?\", ");
                say_continue("\"Tell me a joke\", \"Tell me a story\", etc.");
            }
            else if (something.equals("Hello")) {
                say("Hello!");
            }
            else if (something.equals("Hi")) {
                say("Hi~");
            }
            else if (something.equals("Nice to meet you")) {
                say("Nice to meet you too!");
            }
            else if (something.equals("Goodbye") || something.equals("Bye")) {
                say(this.name + " will miss you, bye!");
                break;
            }
            else if (something.equals("What's your name?")) {
                say("My name is " + this.name + ".");
                say("What about you?");
                System.out.print("You: ");
                String other_name = scanner.nextLine();
                say(other_name + "? This name is really nice.");
            }
            else if (something.length() > 11 && something.substring(0, 11).equals("My name is ")) {
                String other_name = something.substring(11);
                say(other_name + "? This name is really nice.");
            }
            else if (something.equals("How are you?")) {
                String[] answers = {"Fine~", "I am fine!", "Good!", "Fine, thank you!", "Nice!", "Not bad..."};
                say(answers[(new Random()).nextInt(6)]);
                if ((new Random()).nextInt(2) ==  0) {
                    say("What about you?");
                    System.out.print("You: ");
                    scanner.nextLine();
                    say("Oh, I am sorry to hear that.");
                }
            }
            else if (something.equals("Introduce yourself")) {
                say("My name is " + this.name + ",");
                say_continue("the one who often says \"" + this.maxim + "\".");
                say("...");
                say("I am powerful!");
                say_continue("If you are curious what I can do, you can ask me \"What can you do?\".");
            }
            else if (something.equals("What are you thinking about?")) {
                say("emmmm...");
                say("Maybe " + think() + "...");
            }
            else if (something.length() > 12 && something.substring(0, 12).equals("Do you know ")) {
                String thing = something.substring(12, something.length() - 1);
                say("Of course...");
                say("It is very nice, I think it must " + think(thing) + "...");
            }
            else if (something.equals("Tell me a joke")) {
                say("joke.");
            }
            else if (something.equals("Tell me a story")) {
                say("story.");
            }
            else {
                say("wow!!");
                say("I think, you mean " + think(something) + "...");
                say("emmmm...");
                say("What does it mean?");
                say("Maybe you could try asking me, \"What can you do?\"");
            }
        }

    }
    public static void main(String[] args) {
        KeybrL me = new KeybrL("KeybrL", "hhh");
        me.listen_then_talk();
    }
}
