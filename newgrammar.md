

Start = AdditiveExpression <EOF>

AdditiveExpression = MultiplicativeExpression ( <PLUS> MultiplicativeExpression)*
AdditiveExpression = MultiplicativeExpression ( <MINUS> MultiplicativeExpression)*

MultiplicativeExpression = UnaryOperator ( <TIMES> UnaryOperator)*
MultiplicativeExpression = UnaryOperator ( <DIVIDE> UnaryOperator)*

UnaryOperator = <MINUS> Factor | 
                Factor

Factor = <DIGIT> |
         <OPEN_PAREN> AdditiveExpression <CLOSE_PAREN>

         