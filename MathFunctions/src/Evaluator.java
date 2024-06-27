import java.util.HashMap;
import java.util.Map;

public class Evaluator {
	@FunctionalInterface
	interface Expression {
	    double eval();
	}
	
	public static double customRound(double number, int decimalPlaces) {
		//if (decimalPlaces < 0) throw new IllegalArgumentException();
		
		double factor = Math.pow(10, decimalPlaces);
		double result = ((int) Math.round(number * factor)) / factor;
		return result;
	}
	
	public static Expression parse(final String str, Map<String, Double> variables) {
	    return new Object() {
	        int pos = -1, ch;
	        
	        void nextChar() {
	            ch = (++pos < str.length()) ? str.charAt(pos) : -1;
	        }
	        
	        boolean eat(int charToEat) {
	            while (ch == ' ') nextChar();
	            if (ch == charToEat) {
	                nextChar();
	                return true;
	            }
	            return false;
	        }
	        
	        Expression beginParse() {
	            nextChar();
	            Expression x = parseExpression();
	            if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
	            // CHANGES: Custom rounding function
	            return x;
	        }
	        
	        // Grammar:
	        // expression = term | expression `+` term | expression `-` term
	        // term = factor | term `*` factor | term `/` factor
	        // factor = `+` factor | `-` factor | `(` expression `)` | number
	        //        | functionName `(` expression `)` | functionName factor
	        //        | factor `^` factor
	        Expression parseExpression() {
	            Expression x = parseTerm();
	            for (;;) {
	                if (eat('+')) { // addition
	                    Expression a = x, b = parseTerm();
	                    x = (() -> a.eval() + b.eval());
	                } else if (eat('-')) { // subtraction
	                    Expression a = x, b = parseTerm();
	                    x = (() -> a.eval() - b.eval());
	                } else {
	                    return x;
	                }
	            }
	        }
	        
	        Expression parseTerm() {
	            Expression x = parseFactor();
	            for (;;) {
	                if (eat('*')) { // multiplication
	                    Expression a = x, b = parseFactor();
	                    System.out.println("Multiplication (a, b) = (" + a.eval() + ", " + b.eval() + ")");
	                    x = (() -> a.eval() * b.eval());
	                    System.out.println("After Multiplication:" + x.eval());
	                } else if (eat('/')) { // division
	                    Expression a = x, b = parseFactor();
	                    x = (() -> a.eval() / b.eval());
	                } else if (eat('%')) { // division
	                    Expression a = x, b = parseFactor();
	                    x = (() -> a.eval() % b.eval());
	                } else {
	                    return x;
	                }
	            }
	        }
	        
	        Expression parseFactor() {
	        	if (eat('+')) return parseFactor(); // unary plus
	        	if (eat('-')) { // unary minus
	        	    Expression x = parseFactor();
	        	    return () -> -x.eval();
	        	}
	            
	            Expression x;
	            int startPos = this.pos;
	            if (eat('(')) { // parentheses
	                x = parseExpression();
	                if (!eat(')')) throw new RuntimeException("Missing ')'");
	            } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
	                while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
	                double value = Double.parseDouble(str.substring(startPos, this.pos));
	                x = () -> value;
	            } else if (ch >= 'a' && ch <= 'z') { // functions
	                while (ch >= 'a' && ch <= 'z' || ch == '1' || ch == '0') nextChar(); // Account for log10() function
	                String token = str.substring(startPos, this.pos);
	                if (eat('(')) {
	                    x = parseExpression();
	                    x = getFunctionExpression(token, x);
	                    if (!eat(')')) throw new RuntimeException("Missing ')' after argument to " + token);
	                } else {
	                    x = () -> variables.getOrDefault(token, 0.0);
	                }
	                
	            } else {
	                throw new RuntimeException("Unexpected: " + (char)ch);
	            }
	            
	            if (eat('^')) {
	            	Expression a = x, b = parseFactor();
	            	x = () -> Math.pow(a.eval(), b.eval()); // exponentiation
	            }
	            
	            return x;
	        }
	        
	        private Expression getFunctionExpression(String func, Expression x) {
	            switch (func) {
	                case "sqrt":
	                	System.out.println("Before SQRT:" + x.eval());
	                	System.out.println("After SQRT:" + Math.sqrt(x.eval()));
	                    return () -> Math.sqrt(x.eval());
	                case "sin":
	                    return () -> Math.sin(Math.toRadians(x.eval()));
	                case "cos":
	                    return () -> Math.cos(Math.toRadians(x.eval()));
	                case "tan":
	                    return () -> Math.tan(Math.toRadians(x.eval()));
	                case "round":
	                	if (eat(',')) {
	                		Expression a = x, b = parseFactor();
	                		System.out.println("Round (a, b) = (" + a.eval() + ", " + b.eval() + ")");
	                		System.out.println("After round:" + customRound(a.eval(), (int) b.eval()));
	                		return () -> customRound(a.eval(), (int) b.eval());
	                	} else {
	                        throw new RuntimeException("Missing second argument for round function");
	                    }
	                case "sqr":
	                	return () -> Math.pow(x.eval(), 2.0);
	                case "exp":
	                	return () -> Math.pow(Math.E, x.eval());
	                case "log":
	                	return () -> Math.log(x.eval());
	                case "log10":
	                	return () -> Math.log10(x.eval());
	                case "abs":
	                	return () -> Math.abs(x.eval());
	                case "neg":
	                	return () -> -x.eval();
	                default:
	                    throw new RuntimeException("Unknown function: " + func);
	            }
	        }
	        
	    }.beginParse();
	}
	
	public static void main(String[] args) {
	    Map<String,Double> variables = new HashMap<>();
	    Expression exp = parse("round(20 / 5 + 5 , 0)", variables);
	    variables.put("x", 11.2);
	    variables.put("y", 121.0);
	    System.out.println(exp.eval());
	    /*
	    for (double x = 0; x <= 4; x++) {
	        variables.put("x", x);
	        System.out.println(x + " => " + exp.eval());
	    }
	    */
	}

}
