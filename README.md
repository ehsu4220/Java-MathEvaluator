# Description
Java implementation of the MathEvaluator object using Java's functional interfaces
 - Supports variable definition and named functions
 - Generates lambda pipeline that computes result at runtime instead of compile time

# General usage
 - Define Variables that exist in the expression as a hashmap

   `Map<String, Double> variables  = new HashMap<>();`
   `variables.put("x", 11.2);`
   `variables.put("y", 121.0);`

  - Define Expression instance and assign the result of running `parse()`

    `Expression exp = parse("round(x + y / 5 + 5 , 0)", variables);`

  - Retrieve calculated result by calling `Expression.eval()`

    `System.out.printlin(exp.eval());`

# Notes
  - `round()` expression takes 2 arguments and may have unexpected behavior due to binary representation of decimal values
    - Look into https://forum.nim-lang.org/t/4402
