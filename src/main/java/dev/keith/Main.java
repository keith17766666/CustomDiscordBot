package dev.keith;

import dev.keith.bots.DiscordBot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println(new File("src/main/java/dev/keith/token.txt").getAbsolutePath());
        try(Scanner scanner = new Scanner(new File("src/main/java/dev/keith/token.txt"))) {
            DiscordBot.startBot(scanner.nextLine());
        } catch (FileNotFoundException e) {
            System.out.println("No token is provided!");
        }
    }
}
