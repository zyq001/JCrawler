package com.dict.score;

//import toolbox.misc.LogFormatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Administrator
 */
public class LeveDisMuti {


    public static final String HS_FILE = "ci/highschool_total.txt";

    public static final String CET4_FILE = "ci/cet4.txt";

    public static final String CET6_FILE = "ci/cet6.txt";

    public static final String TOEFL_FILE = "ci/toefl.txt";

    public static final String GRE_FILE = "ci/gre.txt";

    public static final String IELTS_FILE = "ci/ielts.txt";

    private String vocPath = null;

    private static LeveDisMuti instance = new LeveDisMuti("");

    private HashSet<String> hsVoc = new HashSet<String>();

    private HashSet<String> cet4Voc = new HashSet<String>();

    private HashSet<String> cet6Voc = new HashSet<String>();

    private HashSet<String> toeflVoc = new HashSet<String>();

    private HashSet<String> greVoc = new HashSet<String>();

    private HashSet<String> ielts = new HashSet<String>();

    public static String p = "";
//    public static String p = "/global/exec/zhanghui/data";
//    public static String p = "/home/disk/codespace/homework/MyCrawl/src/main/resources";

    public static LeveDisMuti getInstance(String path) {
        return instance;
    }

    public List<Double> compLevel(Set<String> wordSet) {
        if (wordSet == null) {
            return null;
        }

//        String[] ss = text.split(" ");
        int total = wordSet.size(), hs = 0, cet4 = 0, cet6 = 0, tem4 = 0, tem8 = 0, iets = 0;
        for (String s : wordSet) {
            if (hsVoc.contains(s)) {
                hs++;
            } else if (cet4Voc.contains(s)) {
                cet4++;
            } else if (cet6Voc.contains(s)) {
                cet6++;
            } else if (ielts.contains(s)) {
                iets++;
            } else if (toeflVoc.contains(s)) {
                tem4++;
            } else if (greVoc.contains(s)) {
                tem8++;
            } else {
                continue;
            }
//            total++;
        }
        List<Double> rate = new ArrayList<Double>();
        if (total <= 0) {
            return null;
        } else if (total >= 2 && total < 10) {
            total = 10;
        }
        rate.add(1.0 * hs / total);
        rate.add(1.0 * cet4 / total);
        rate.add(1.0 * cet6 / total);
        rate.add(1.0 * iets / total);
        rate.add(1.0 * tem4 / total);
        rate.add(1.0 * tem8 / total);
        //    rate.add(1.0 * hs );
        //  rate.add(1.0 * cet4 );
        // rate.add(1.0 * cet6 );
        // rate.add(1.0 * iets );
        // rate.add(1.0 * tem4 );
        // rate.add(1.0 * tem8 );
        // rate.add(1.0 * total );

        return rate;
    }

    public int compUserLevel(List<Double> array) {
        if (array == null) {
            return 1;
        }
        double cur = 0;
        for (int i = array.size() - 1; i >= 0; i--) {
            cur += array.get(i);
            if (cur >= 0.5) {
                return i;
            }
        }
        return 1;
    }

    public int compFileLevel(List<Double> array) {
        if (array == null) {
            return 1;
        }
        double cur = 0;
        for (int i = array.size() - 1; i >= 0; i--) {
            cur += array.get(i);
            if (cur >= 0.005) {
                return i;
            }
        }
        return 1;
    }

    LeveDisMuti(String path) {

        if (!loadVoc(HS_FILE, hsVoc)
                || !loadVoc(CET4_FILE, cet4Voc)
                || !loadVoc(CET6_FILE, cet6Voc)
                || !loadVoc(IELTS_FILE, ielts)
                || !loadVoc(TOEFL_FILE, toeflVoc)
                || !loadVoc(GRE_FILE, greVoc)) {
//            LOG.warning("[LevelWordDiscriminator] some loadVoc error...");
        }
    }

    private boolean loadVoc(String path, HashSet<String> voc) {
        if (path == null || voc == null) {
//            LOG.warning("[LevelWordDiscriminator] loadVoc error...");
            return false;
        }

        try {
            InputStream is = LeveDisMuti.class.getClassLoader().getResourceAsStream(path);
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim().toLowerCase();
                if (line.length() <= 3) {
                    continue;
                }
                int pos = line.indexOf(" ");
                if (pos < 0) {
                    continue;
                }
                String word = line.substring(0, pos);
                if (isEng(word)) {
                    voc.add(word.trim());
                }
            }
            br.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
//            LOG.warning("[LevelWordDiscriminator] loadVoc error...");
            return false;
        }

        return true;
    }

    private boolean loadVoc1(String path, HashSet<String> voc) {
        if (path == null || voc == null) {
//            LOG.warning("[LevelWordDiscriminator] loadVoc error...");
            return false;
        }

        try {
            InputStream is = LeveDisMuti.class.getClassLoader().getResourceAsStream(path);
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim().toLowerCase();
                if (line.length() <= 3) {
                    continue;
                }
                int pos = line.indexOf(";");
                if (pos < 0) {
                    continue;
                }
                int pos1 = line.indexOf(": ");
                if (pos1 < 0) {
                    continue;
                }
                String word = line.substring(pos1 + 2, pos);
                String word1 = word.replaceAll("\\*", "");
                //System.out.println(word1);
                if (isEng(word1)) {
                    voc.add(word1);
                }
            }
            br.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
//            LOG.warning("[LevelWordDiscriminator] loadVoc error...");
            return false;
        }

        return true;
    }

    private boolean isEng(String w) {
        for (int i = 0; i < w.length(); i++) {
            char c = w.charAt(i);
            if ((c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')) {
                continue;
            }
            return false;
        }
        return true;
    }

    public String tag(String text, int tagnum) {
/*        try {
            String path = "vocPath/stop_words_eng.txt";
            InputStream is = LeveDis.class.getClassLoader().getResourceAsStream(path);
            BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String rout = "";
            String buf;
            Set<String> stopWords = new HashSet<String>();
            while ((buf = in.readLine()) != null) {
                stopWords.add(buf.toLowerCase().trim());
            }
            in.close();
            is.close();
            Summary te = new Summary();

            Map<String, Integer> wordFrequencies = te.segStr(text);
            Set<String> mostFrequentWords = te.getMostFrequentWords(tagnum, wordFrequencies).keySet();

            for (String s : mostFrequentWords) {
                if (!"".equals(s.trim()) && !stopWords.contains(s)) {
                    rout += s;
                    rout += ",";
                }
            }
            if (!rout.contains(",")) {
                return rout;
            }
            return rout.substring(0, rout.lastIndexOf(","));
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        return "";
    }


    public static void main(String[] args) throws IOException {
        LeveDisMuti instance1 = null;
        instance1 = new LeveDisMuti(p);
        String buf = "Breast-feeding pushed for infant health.China's top health authority urged on Monday that greater efforts are made to encourage breast-feeding.It improves maternal and infant well-being, but its popularity has declined over the past decade despite clear health benefits, the authority said.Globally, about 35 percent of deaths of children under age 5 are attributed to malnutrition, and improving breast-feeding can decrease the infant mortality rate by 13 percent, Wang Guoqiang, vice-minister at the National Health and Family Planning Commission, said on Monday.Wang was speaking at an event to mark the World Breast-Feeding Week, which continues through Friday.China is one of the countries with the highest employment rates for women, and guaranteeing women employees' rights to breast-feeding is very important to decrease the mortality rate of infants and promote their healthy growth, Wang said.Breast-feeding is the best way to provide infants with the nutrients they need.The World Health Organization recommends exclusive breast-feeding for babies until they are 6 months old, and continued breast-feeding with the addition of other nutritious foods until children are 2 years old or more. Breast milk is the most nutritious source for humans and provides complete and rich nutrition for newborn babies. It cannot be substituted by milk powders, said Zhang Shuyi, a researcher at the Capital Institute of Pediatrics.It also contains many substances that can increase babies' immune system against diseases, she said.Less than 28 percent of babies under 6 months old were exclusively breast-fed in China in 2013, compared with 67 percent in 1998, according to the commission last year.In contrast, the central government wants more than 50 percent of all infants under 6 months old to be exclusively breast-fed by 2020, under a guideline released by the government in 2011.According to Zhang, breast-feeding has declined for many reasons, including a lack of maternal health education.The widespread promotion of powdered milk also has encouraged the use of formula, he said.The central government has taken measures in recent years to encourage breast-feeding, such as issuing regulations to ensure maternity leave for female employees and asking companies to set up special rooms so mothers can breast-feed their babies.In Beijing, the breast-feeding rate for infants under 6 months old reached 92 percent last year, and 70 percent of all infants under 6 months old were exclusively breast-fed, said Gao Xiaojun, spokesman for the Beijing Commission for Health and Family Planning.";
//        List<Double> arry = instance1.compLevel(buf);
//        int leavel = instance1.compFileLevel(arry);
//        System.out.println("LEVEL=" + leavel);

        String tags = instance1.tag(buf, 10);
        System.out.println(tags);
    }
}
