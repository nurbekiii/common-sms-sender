/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package solsmsengine;

/**
 * @author daliev
 */
public class Util {

    public static boolean hasUnicode(String inStr) {
        StringBuilder valid = new StringBuilder(" _€'^<>{}[]\\|~!@#$%&*()-+=\":;,.?/0123456789abcdefghijklmnopqrstuvwxyz\n");
        StringBuilder in = new StringBuilder(inStr);
        for (int i = 0; i < in.length(); i++) {
            if (valid.indexOf(String.valueOf(in.charAt(i)).toLowerCase()) == -1) {
                return true;
            }
        }
        return false;
    }

    public static String prepareResponseString(String in) {
        return replaceSpecialSymbols(rinse(translit(in))).trim();
    }

    private static String replaceSpecialSymbols(String srcTxt) {
        StringBuilder specialSymbols = new StringBuilder("_€^{}[]~\\|");
        String[] replaceSymbols = {"_--", "_XXe", "_XX_gl", "_XX(", "_XX)", "_XX<", "_XX>", "_XX=", "_XX/", "_XX_!!"};
        StringBuilder res = new StringBuilder(srcTxt);
        for (int i = 0; i < specialSymbols.length(); i++) {
            int ind = res.indexOf(specialSymbols.substring(i, i + 1));
            while (ind != -1) {
                res.replace(ind, ind + 1, replaceSymbols[i]);
                ind = res.indexOf(specialSymbols.substring(i, i + 1), ind + replaceSymbols[i].length());
            }
        }
        return res.toString();
    }

    private static String translit(String srcTxt) {
        StringBuilder cyrillicLetters = new StringBuilder("абвгдеёжзийклмнопрстуфхцчшьщъыэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЬЩЪЫЭЮЯ");
        String[] translit = {"a", "b", "v", "g", "d", "e", "e", "zh", "z", "i", "y", "k", "l", "m", "n", "o", "p", "r", "s", "t", "u", "f", "kh", "ts", "ch", "sh", "'", "shch", "'", "y", "e", "yu", "ya", "A", "B", "V", "G", "D", "E", "E", "ZH", "Z", "I", "Y", "K", "L", "M", "N", "O", "P", "R", "S", "T", "U", "F", "KH", "TS", "CH", "SH", "'", "SHCH", "'", "Y", "E", "YU", "YA"};
        StringBuilder res = new StringBuilder(srcTxt);
        for (int i = 0; i < cyrillicLetters.length(); i++) {
            int ind = res.indexOf(cyrillicLetters.substring(i, i + 1));
            while (ind != -1) {
                res.replace(ind, ind + 1, translit[i]);
                ind = res.indexOf(cyrillicLetters.substring(i, i + 1), ind + translit[i].length());
            }
        }
        return res.toString();
    }

    private static String rinse(String inStr) {
        StringBuilder valid = new StringBuilder(" _€'^<>{}[]\\|~!@#$%&*()-+=\":;,.?/0123456789abcdefghijklmnopqrstuvwxyz\n");
        StringBuilder in = new StringBuilder(inStr);
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < in.length(); i++) {
            if (valid.indexOf(String.valueOf(in.charAt(i)).toLowerCase()) > -1) {
                res.append(in.charAt(i));
            }
        }
        return res.toString();
    }

}
