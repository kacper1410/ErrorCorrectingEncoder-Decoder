package correcter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Write a mode: ");
        String mode = scanner.next();
        byte[] text = {0};
        switch (mode) {
            case "encode":
                try {
                    text = Files.readAllBytes(Path.of("send.txt"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                writeArrayListToFile("encoded.txt", encode(text));
                break;
            case "send":
                try {
                    text = Files.readAllBytes(Path.of("encoded.txt"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                send(text);
                break;
            case "decode":
                decode("received.txt");
                break;
            default:
                System.out.println("Unknown mode");
        }


    }

    static ArrayList<Byte> encode(byte[] text) {
        ArrayList<Byte> output = new ArrayList<>();
        byte byteToWrite = 0;
        for (byte textByte : text) {
            byteToWrite |= (textByte >>> 7 & 1) << 5;
            byteToWrite |= (textByte >>> 6 & 1) << 3;
            byteToWrite |= (textByte >>> 5 & 1) << 2;
            byteToWrite |= (textByte >>> 4 & 1) << 1;
            byteToWrite = setParityBits(byteToWrite);
            output.add(byteToWrite);
            byteToWrite = 0;
            byteToWrite |= (textByte >>> 3 & 1) << 5;
            byteToWrite |= (textByte >>> 2 & 1) << 3;
            byteToWrite |= (textByte >>> 1 & 1) << 2;
            byteToWrite |= (textByte & 1) << 1;
            byteToWrite = setParityBits(byteToWrite);
            output.add(byteToWrite);
            byteToWrite = 0;

        }
        return output;
    }

    static byte setParityBits(byte b) {
        b |= ((b >>> 5 & 1) ^ (b >>> 3 & 1) ^ (b >>> 1 & 1)) << 7;
        b |= ((b >>> 5 & 1) ^ (b >>> 2 & 1) ^ (b >>> 1 & 1)) << 6;
        b |= ((b >>> 3 & 1) ^ (b >>> 2 & 1) ^ (b >>> 1 & 1)) << 4;

        return b;
    }

    static void generateError(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            int position = (int) (Math.random() * 8);
            bytes[i] = (byte) (bytes[i] ^  1 << position);
        }
    }

    static void send(byte[] message) {
        generateError(message);
        try (OutputStream outputStream = new FileOutputStream(new File("received.txt"), false)) {
            outputStream.write(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void decode(String path) {
        byte[] coded = {0};
        ArrayList<Byte> decoded = new ArrayList<>();
        try {
            coded = Files.readAllBytes(Path.of(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        int halfOfByte = 1;
        byte decodedByte = 0;
        for (byte b : coded) {
            int positionOfError = 0;
            if (((b >>> 7 & 1) ^ (b >>> 5 & 1) ^ (b >>> 3 & 1) ^ (b >>> 1 & 1)) == 1) {
                positionOfError += 1;
            }
            if (((b >>> 6 & 1) ^ (b >>> 5 & 1) ^ (b >>> 2 & 1) ^ (b >>> 1 & 1)) == 1) {
                positionOfError += 2;
            }
            if (((b >>> 4 & 1) ^ (b >>> 3 & 1) ^ (b >>> 2 & 1) ^ (b >>> 1 & 1)) == 1) {
                positionOfError += 4;
            }
            b ^= 1 << (8 - positionOfError);
            decodedByte |= (b >>> 5 & 1) << (halfOfByte * 4 + 3);
            decodedByte |= (b >>> 3 & 1) << (halfOfByte * 4 + 2);
            decodedByte |= (b >>> 2 & 1) << (halfOfByte * 4 + 1);
            decodedByte |= (b >>> 1 & 1) << (halfOfByte * 4);
            if (halfOfByte == 0) {
                decoded.add(decodedByte);
                decodedByte = 0;
            }
            halfOfByte ^= 1;
        }
        writeArrayListToFile("decoded.txt", decoded);


    }

    static void writeArrayListToFile(String path, ArrayList<Byte> message) {
        try (OutputStream outputStream = new FileOutputStream(new File(path), false)) {
            byte[] mess = new byte[message.size()];
            for (int i = 0; i < mess.length; i++) {
                mess[i] = message.get(i);
            }
            outputStream.write(mess);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
