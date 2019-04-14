import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Searcher {
    private static List<String> inputWords = new ArrayList<String>();
    private static List<Character> operatorsList = new ArrayList<Character>();
    private static DirectIndexer t = new DirectIndexer();
    private static List<String> finalList = new ArrayList<String>();
    
    private static void andOperation(String first_operand, String second_operand,
            Map<String, HashMap<String, Integer>> invertedIndex) {

        HashMap<String, Integer> hasMapFirstOperand = new HashMap<>();
        HashMap<String, Integer> hasMapSecondOperand = new HashMap<>();
        int ok = 0;
        
        if(invertedIndex.get(first_operand).size() < invertedIndex.get(second_operand).size()) {
            hasMapFirstOperand = invertedIndex.get(first_operand);
            hasMapSecondOperand = invertedIndex.get(second_operand);
        }
        else {
            hasMapFirstOperand = invertedIndex.get(second_operand);
            hasMapSecondOperand = invertedIndex.get(first_operand);
        }
        
        for(String fileName1 : hasMapFirstOperand.keySet()) {
            for(String fileName2 : hasMapSecondOperand.keySet()) {
                
                if(finalList.isEmpty()) {
                    finalList.add(fileName1);
                    ok = 1;
                }
                else {
                    
                    if(fileName1.contains(fileName2)) {
                        if(!finalList.contains(fileName1)) {
                            finalList.add(fileName1);
                        }
                        ok = 1;
                    }
                }
            }
        }
        if (ok == 0) {
            finalList.remove(finalList.size() - 1);
            if(finalList.isEmpty()) {
                System.out.println(finalList);
                System.exit(0);
            }
        }
    }
    
    private static void notOperation(String first_operand, String second_operand,
            Map<String, HashMap<String, Integer>> invertedIndex) {
        
        HashMap<String, Integer> hasMapFirstOperand = invertedIndex.get(first_operand);
        HashMap<String, Integer> hasMapSecondOperand = invertedIndex.get(second_operand);
        int ok = 1;
        
        for(String fileName1 : hasMapFirstOperand.keySet()) {
            if(finalList.isEmpty()) {
                finalList.add(fileName1);
            }
            for(String fileName2 : hasMapSecondOperand.keySet()) {
                    if(fileName1.contains(fileName2)) {
                        finalList.remove(fileName1);
                        ok = 0;
                    }
            }
            if(ok == 1 && !finalList.contains(fileName1))
                finalList.add(fileName1);
        }
    }
    
    private static void orOperation(String first_operand, String second_operand,
            Map<String, HashMap<String, Integer>> invertedIndex) {
        
        HashMap<String, Integer> hasMapFirstOperand = new HashMap<>();
        HashMap<String, Integer> hasMapSecondOperand = new HashMap<>();
        
        if(invertedIndex.get(first_operand).size() > invertedIndex.get(second_operand).size()) {
            hasMapFirstOperand = invertedIndex.get(first_operand);
            hasMapSecondOperand = invertedIndex.get(second_operand);
        }
        else {
            hasMapFirstOperand = invertedIndex.get(second_operand);
            hasMapSecondOperand = invertedIndex.get(first_operand);
        }
        
        for(String fileName1 : hasMapFirstOperand.keySet()) {
            for(String fileName2 : hasMapSecondOperand.keySet()) {
                    if(!fileName2.contains(fileName1) && !finalList.contains(fileName2)) {
                        finalList.add(fileName2);
                    }
            }
            if(!finalList.contains(fileName1)) {
                finalList.add(fileName1);
            }
        }
    }
    
    private static void findMatch(Map<String, HashMap<String, Integer>> invertedIndex) {
        String word = "";

        if(inputWords.isEmpty()) {
            System.err.println("Nici un termen introdus!");
            System.exit(1);
        }
        
        if(operatorsList.isEmpty()) {
            word = inputWords.get(0);
            System.out.println(invertedIndex.get(word));
        }
        else {
            for(int i = 0; i < operatorsList.size(); ++i) {
                Character operator = operatorsList.get(i);
                
                for(int j = 1; j < inputWords.size(); ++j) {
                    String first_operand = inputWords.get(j - 1);
                    String second_operand = inputWords.get(j);
                
                    if(operator == '+') {
                        andOperation(first_operand, second_operand, invertedIndex);
                    }
                    
                    else if(operator == '-') {
                        notOperation(first_operand, second_operand, invertedIndex);
                    }
                    
                    else if(operator == ' ') {
                        orOperation(first_operand, second_operand, invertedIndex);
                    }
                }
            }
            System.out.println(finalList);
        }
    }

    public static void getWords(String searchInput)
    {
        
        t.getStopWords();
        
        try {

            String line = searchInput.toLowerCase();
            StringBuilder word = new StringBuilder();
            
            for(int i = 0; i < line.length(); ++i) {
                if(Character.isLetterOrDigit(line.charAt(i))) {
                    word.append(line.charAt(i));
                }
                else if(line.charAt(i) == ' ' || line.charAt(i) == '+' || line.charAt(i) == '-') {
                    if(t.stopWords(word.toString()) != true && !inputWords.contains(word.toString())) {
                        operatorsList.add(line.charAt(i));
                        inputWords.add(word.toString());
                    }
                    word.setLength(0);
                }                    
            }
            if(t.stopWords(word.toString()) != true && !inputWords.contains(word.toString())) {
                if(word.toString().length() != 0)
                    inputWords.add(word.toString());
            }
            if(inputWords.size() == 1)
                operatorsList.clear();
        }catch(Exception e)
        {
            System.err.println("Nici un termen introdus!");
            System.exit(1);
        }
    }
    public void search(Map<String, HashMap<String, Integer>> invertedIndex, String searchInput)
    {
        getWords(searchInput);
        findMatch(invertedIndex);
    }
}