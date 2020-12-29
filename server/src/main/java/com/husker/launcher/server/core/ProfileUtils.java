package com.husker.launcher.server.core;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;

public class ProfileUtils {

    public static void fileLineFilter(String path, Predicate<String> lineFilter){
        try {
            ArrayList<String> newLines = new ArrayList<>();
            List<String> lines = Files.readAllLines(Paths.get(path));
            for (String line : lines)
                if(lineFilter.test(line))
                    newLines.add(line);

            if(lines.size() != newLines.size())
                Files.write(Paths.get(path), newLines);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static boolean fileLinePredicate(String path, Predicate<String> lineFilter){
        try {
            List<String> lines = Files.readAllLines(Paths.get(path));
            for (String line : lines)
                if(lineFilter.test(line))
                    return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    public static void appendFile(String path, String line){
        try {
            BufferedWriter output = new BufferedWriter(new FileWriter(path, true));
            output.write(line + "\n");
            output.flush();
            output.close();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static String generatePassword(){
        return new KeyGenerator()
                .setLength(7)
                .addAlphabet(KeyGenerator.ALPHABET_LOWER_CASE)
                .addAlphabet(KeyGenerator.ALPHABET_UPPER_CASE)
                .get();
    }

    public static String generatePasswordChangeHash(){
        return new KeyGenerator()
                .setLength(24)
                .addAlphabet(KeyGenerator.ALPHABET_LOWER_CASE)
                .addAlphabet(KeyGenerator.ALPHABET_UPPER_CASE)
                .addAlphabet(KeyGenerator.DIGITS)
                .setCondition(generated -> {
                    for(Profile profile : Profile.getProfiles())
                        if(profile.Password.containsChangeHash(generated))
                            return false;
                    return true;
                }).get();
    }

    public static String generateAccessToken(){
        return new KeyGenerator()
                .setLength(24)
                .addAlphabet(KeyGenerator.ALPHABET_LOWER_CASE)
                .addAlphabet(KeyGenerator.ALPHABET_UPPER_CASE)
                .addAlphabet(KeyGenerator.DIGITS)
                .setCondition(generated -> {
                    for(Profile profile : Profile.getProfiles())
                        if(profile.Token.containsKey(generated))
                            return false;
                    return true;
                }).get();
    }

    public static String generateMailCode(){
        return new KeyGenerator()
                .setLength(6)
                .addAlphabet(KeyGenerator.DIGITS)
                .get();
    }

    public static boolean canChangeName(Profile profile, String newName){
        for(int id : Profile.getIds()) {
            if(profile.getId() == id)
                continue;
            try {
                if (new Profile(id).Data.getValue(Profile.LOGIN).equals(newName))
                    return false;
            }catch (Exception ignored){}
        }
        return true;
    }

    public static boolean isNicknameExist(String nickname){
        return Profile.getByName(nickname) != null;
    }

    public static boolean isEmailExist(String email){
        for(int id : Profile.getIds()){
            Profile profile = new Profile(id);
            if(profile.data.get(Profile.EMAIL).contains(email))
                return true;
        }
        return false;
    }

    public static boolean arePasswordsEquals(String hashed, String password){
        return hashed.equals(DigestUtils.md2Hex(password));
    }

    public static boolean isExist(int id){
        return new File(Profile.profilesFolder + "/" + id).exists();
    }

    public static class KeyGenerator{

        public static final String ALPHABET_UPPER_CASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        public static final String ALPHABET_LOWER_CASE = ALPHABET_UPPER_CASE.toLowerCase();
        public static final String DIGITS = "0123456789";

        private int length = 10;
        private final StringBuilder alphabet = new StringBuilder();
        private Predicate<String> condition = s -> true;

        public KeyGenerator addAlphabet(String... chars){
            for(String text : chars)
                alphabet.append(text);
            return this;
        }

        public KeyGenerator setCondition(Predicate<String> condition){
            this.condition = condition;
            return this;
        }

        public KeyGenerator setLength(int length){
            this.length = length;
            return this;
        }

        public String get(){
            StringBuilder text;

            do{
                text = new StringBuilder();
                for (int i = 0; i < length; i++)
                    text.append(alphabet.charAt(new Random().nextInt(alphabet.length() - 1)));
            }while(!condition.test(text.toString()));
            return text.toString();
        }
    }

}
