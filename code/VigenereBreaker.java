import java.util.*;
import edu.duke.*;
import java.io.File;

public class VigenereBreaker {
    public String sliceString(String message, int whichSlice, int totalSlices) {
        StringBuilder input = new StringBuilder(message);
        StringBuilder sb = new StringBuilder();
        for (int i = whichSlice; i < message.length(); i += totalSlices) {
            sb.append(input.charAt(i));
        }
        return sb.toString();
    }

    public int[] tryKeyLength(String encrypted, int klength, char mostCommon) {
        int[] key = new int[klength];
        CaesarCracker cc = new CaesarCracker(mostCommon);
        for (int i = 0; i < klength; i++) {
            String slicedString = sliceString(encrypted,i,klength);
            key[i] = cc.getKey(slicedString);
        }
        return key;
    }
    
    public HashSet<String> readDictionary (FileResource fr) {
        HashSet<String> map = new HashSet<String>();
        for (String word : fr.words()) {
            map.add(word.toLowerCase());
        }
        return map;
    }
    
    public int countWords (String message, HashSet<String> dictionary) {
        int count = 0;
        String[] array = message.split("\\W+");
        for (String word : array) {
            if (dictionary.contains(word.toLowerCase())) {
                count += 1;
            }
        }
        return count;
    }
    
    public String breakForLanguage (String encrypted, HashSet<String> dictionary, char mostCommonChar) {
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (int i = 1; i < 101; i++) {
            int[] key = tryKeyLength(encrypted,i,mostCommonChar);
            VigenereCipher vc = new VigenereCipher(key);
            String message = vc.decrypt(encrypted);
            int count = countWords(message,dictionary);
            map.put(i,count);
        }
        int klength = 1;
        for (Integer i : map.keySet()) {
            if (map.get(i) > map.get(klength)) {
                klength = i;
            }
        }
        int[] key = tryKeyLength(encrypted,klength,mostCommonChar);
        VigenereCipher vc = new VigenereCipher(key);
        String message = vc.decrypt(encrypted);
        return message;
    }
    
    public char mostCommonCharIn (HashSet<String> dictionary) {
        HashMap<Character,Integer> map = new HashMap<Character,Integer>();
        for (String word : dictionary) {
            StringBuilder sb = new StringBuilder(word);
            for (int i = 0; i < word.length(); i++) {
                char currChar = sb.charAt(i);
                if (!map.containsKey(currChar)) {
                    map.put(currChar,1);
                }
                else {
                    map.put(currChar,map.get(currChar) + 1);
                }
            }
        }
        char maxChar = ' ';
        for (char currChar : map.keySet()) {
            if (maxChar == ' ') {
                maxChar = currChar;
            }
            else {
                if (map.get(currChar) > map.get(maxChar)) {
                    maxChar = currChar;
                }
            }
        }
        return maxChar;
    }
    
    public String breakForAllLangs (String encrypted, HashMap<String, HashSet<String>> languages) {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        for (String lang : languages.keySet()) {
            char mostCommonChar = mostCommonCharIn(languages.get(lang));
            String message = breakForLanguage(encrypted, languages.get(lang), mostCommonChar);
            map.put(lang, countWords(message, languages.get(lang)));
        }
        String maxLang = null;
        for (String lang : map.keySet()) {
            if (maxLang == null) {
                maxLang = lang;
            }
            else {
                if (map.get(lang) > map.get(maxLang)) {
                    maxLang = lang;
                }
            }
        }
        System.out.println("Lang of text: " + maxLang);
        char mostCommonChar = mostCommonCharIn(languages.get(maxLang));
        String message = breakForLanguage(encrypted, languages.get(maxLang), mostCommonChar);
        return message;
    }
    
    public void breakVigenere () {
        FileResource fr = new FileResource();
        String encrypted = fr.asString();
        HashMap<String, HashSet<String>> languages = new HashMap<String, HashSet<String>>();
        DirectoryResource dr = new DirectoryResource();
        for (File f : dr.selectedFiles()) {
            FileResource file = new FileResource(f);
            System.out.println("Loading in lang: " + f.getName());
            HashSet<String> dictionary = readDictionary(file);
            languages.put(f.getName(),dictionary);
        }
        String message = breakForAllLangs(encrypted,languages);
        System.out.println(message.substring(0,150));
    }
    
}
