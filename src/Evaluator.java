import java.io.InputStream;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Simulate a PDA to evaluate a series of postfix expressions provided by a lexer.
 * The constructor argument is the lexer of type Lexer. A single line is evaluated and its
 * value is printed. Expression values can also be assigned to variables for later use.
 * If no variable is explicitly assigned, then the default variable "it" is assigned
 * the value of the most recently evaluated expression.
 *
 * @author Sam Snarr
 */

public class Evaluator {

    /**
     * Run the desk calculator.
     */
    public static void main(String[] args) {
        Evaluator evaluator = new Evaluator(new Lexer(System.in));
        evaluator.run();
    }

    private Lexer lexer; // providing a stream of tokens
    private LinkedList<Double> stack; // operands
    private HashMap<String, Double> symbols; // symbol table for variables
    private String target; // variable assigned the latest expression value

    public static final int ADD_OP      = 3;
    public static final int SUBTRACT_OP = 4;
    public static final int MULTIPLY_OP = 5;
    public static final int DIVIDE_OP   = 6;
    public static final int MINUS_OP    = 7;
    public static final int ASSIGN_OP   = 8;
    public static final int EOL         = 9;
    public static final int NUMBER      = 11;
    public static final int VARIABLE    = 12;
    public static final int BAD_TOKEN   = 100; // why don't I ever use this?
    public static int state;


    public Evaluator(Lexer lexer) {
        this.lexer = lexer;
        stack = new LinkedList<>();
        symbols = new HashMap<>();
        target = "it";
    }

    /**
     * Evaluate a single line of input, which should be a complete expression
     * optionally assigned to a variable; if no variable is assigned to, then
     * the result is assigned to "it". In any case, return the value of the
     * expression, or "no value" if there was some sort of error.
     */

    public Double evaluate() {
        int token = lexer.nextToken();
        String tokenText = lexer.text.trim();

        String firstVar = "";
        boolean seenNum = false;
        boolean assign = false;

        stack.clear();

        while(token != EOL){

            if(tokenText.equalsIgnoreCase("exit")){
                System.out.println("Bye");
                System.exit(0);
            }

            if(token==NUMBER){
                stack.push(Double.parseDouble(tokenText));
                seenNum = true;
            }
            else if(token==VARIABLE){
                if(symbols.containsKey(tokenText)){
                    stack.push(symbols.get(tokenText));
                }
                else{
                    symbols.put(tokenText, 0.0);
                    stack.push(0.0);
                }
                if(firstVar.equals("")){
                    firstVar = tokenText;
                }

            }
            else if(token==ASSIGN_OP){
                assign=true;
                if(stack.size()==0){
                    token = 100;
                    break;
                }
                stack.pop(); //removes variable value that has been pushed to stack

                if(seenNum){ // numbers never come before an equal sign
                    token=100;
                    break;
                }
            }
            else if(stack.size()<1){
                token=100;
                break;
            }
            else if(token==MINUS_OP){
                stack.push(-stack.pop());
            }
            else if(stack.size()<2){
                token = 100;
                break;
            }
            else if(token==ADD_OP){
                stack.push(stack.pop()+stack.pop());
            }
            else if(token==SUBTRACT_OP){
                stack.push(-stack.pop()+stack.pop());
            }
            else if(token==MULTIPLY_OP){
                stack.push(stack.pop()*stack.pop());
            }
            else if(token==DIVIDE_OP){
                stack.push(1/(stack.pop() / stack.pop()));
            }

            token = lexer.nextToken();
            tokenText = lexer.text.trim();
        }
        if(stack.size()!=1 || token == 100 ){
            return null;
        }
        if(!assign){
            firstVar = "";
        }
        if(firstVar.equals("") ){
            symbols.put(target, stack.pop());
            return symbols.get(target);
        }
        else{
            symbols.put(firstVar, stack.pop());
            return symbols.get(firstVar);
        }
    } // evaluate

    /**
     * Run evaluate on each line of input and print the result forever.
     */
    public void run() {
        int count = 1;
        while (true) {
            Double value = evaluate();
            if (value == null) {
                error("**Invalid**");
            }
            else{
                System.out.println(value);
            }
            count++;
        }
    }

    /**
     * Print an error message, display the offending line with the current
     * location marked, and flush the lexer in preparation for the next line.
     *
     * @param msg what to print as an error indication
     */
    private void error(String msg) {
        System.out.println(msg);
        String line = lexer.getCurrentLine();
        int index = lexer.getCurrentChar();
        System.out.print(line);
        for (int i = 1; i < index; i++) System.out.print(' ');
        System.out.println("^");
        lexer.flush();
    }

    ////////////////////////////////
    ///////// Lexer Class //////////

    /**
     * Read terminal input and convert it to a token type, and also record the text
     * of each token. Whitespace is skipped. The input comes from stdin, and each line
     * is prompted for.
     */
    public static class Lexer {

        // language token codes
        public static final int ADD_OP      = 3;
        public static final int SUBTRACT_OP = 4;
        public static final int MULTIPLY_OP = 5;
        public static final int DIVIDE_OP   = 6;
        public static final int MINUS_OP    = 7;
        public static final int ASSIGN_OP   = 8;
        public static final int EOL         = 9;
        public static final int NUMBER      = 11;
        public static final int VARIABLE    = 12;
        public static final int BAD_TOKEN   = 100;
        public static int state = 0;

        private Scanner input;     // for reading lines from stdin
        private String line;       // next input line
        public static int index;         // current character in this line
        private String text;       // text of the current token

        public Lexer(InputStream in) {
            input = new Scanner(in);
            line = "";
            index = 0;
            text = "";
        }

        /**
         * Fetch the next character from the terminal. If the current line is
         * exhausted, then prompt the user and wait for input. If end-of-file occurs,
         * then exit the program.
         */
        private char nextChar() {
            if (index == line.length()) {
                System.out.print(">> ");
                if (input.hasNextLine()) {
                    line = input.nextLine() + "\n";
                    index = 0;
                }
                else {
                    System.out.println("\nBye");
                    System.exit(0);
                }
            }
            char ch = line.charAt(index);
            index++;
            return ch;
        }

        /**
         * Put the last character back on the input line.
         */
        private void unread() {
            text = text.substring(0, text.length()-1);
            index -= 1;
        }

        /**
         * Return the next token from the terminal.
         */
        public int nextToken(){
            text = "";
            state=0;
            //initial check for state comparing with char c
            char c;

            while(state>=0){

                c = nextChar();
                text += c;

                if(state==0){
                    //text += c;
                    if(Character.isDigit(c)){
                        state=1;
                    }
                    else if(Character.isLetter(c) || c=='_'){
                        state=2;
                    }
                    else if(c==' ' || c=='\t'){
                        state=0;
                    }
                    else if(c=='+'){
                        state=-ADD_OP;
                    }
                    else if(c=='-'){
                        state=-SUBTRACT_OP;
                    }
                    else if(c=='*'){
                        state=-MULTIPLY_OP;
                    }
                    else if(c=='/'){
                        state=-DIVIDE_OP;
                    }
                    else if(c=='~'){
                        state=-MINUS_OP;
                    }
                    else if(c=='='){
                        state=-ASSIGN_OP;
                    }
                    else if(c=='\n'){
                        state=-EOL;
                    }
                    else {
                        state=-BAD_TOKEN;
                    }
                }
                //state 1
                else if(state==1){
                    if(Character.isDigit(c)){
                        state =1;
                    }
                    else if(c=='.'){
                        state=10;
                    }
                    else{
                        unread();
                        state=-NUMBER;
                    }
                }
                //state 2
                else if(state==2){

                    if(Character.isLetter(c) || Character.isDigit(c) || c=='_'){
                        state=2;
                    }
                    else{
                        state=-VARIABLE;
                        unread();
                    }
                }
                // state 10
                else if(state==10){

                    if(Character.isDigit(c)){
                        state=10;
                    }
                    else{
                        unread();
                        state=-NUMBER;
                    }
                }
            }

            return -state;
        } // nextToken

        /**
         * Return the current line for error messages.
         */
        public String getCurrentLine() {
            return line;
        }

        /**
         * Return the current character index for error messages.
         */
        public int getCurrentChar() {
            return index;
        }

        /**
         * /** Return the text of the current token.
         */
        public String getText() {
            return text;
        }

        /**
         * Clear the current line after an error
         */
        public void flush() {
            index = line.length();
        }

    } // Lexer
} // Evaluator
