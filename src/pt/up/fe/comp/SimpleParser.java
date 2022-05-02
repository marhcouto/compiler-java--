package pt.up.fe.comp;

import java.util.Collections;
import java.util.Map;
import java.util.Stack;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParser;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsSystem;

/**
 * Copyright 2022 SPeCS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

public class SimpleParser implements JmmParser {
    private void includeLocation(BaseNode root) {
        Stack<BaseNode> nodeStack = new Stack<>();
        nodeStack.add(root);
        while(!nodeStack.empty()) {
            BaseNode curNode = nodeStack.pop();
            curNode.put("line", String.valueOf(curNode.getBeginLine()));
            curNode.put("column", String.valueOf(curNode.getBeginColumn()));
            for (var child: curNode.children) {
                nodeStack.add((BaseNode) child);
            }
        }
    }

    @Override
    public JmmParserResult parse(String jmmCode, Map<String, String> config) {
        try {
            JmmGrammarParser parser = new JmmGrammarParser(SpecsIo.toInputStream(jmmCode));
            parser.Start();

            var root = ((JmmNode) parser.rootNode()).sanitize();

            if (!(root instanceof JmmNode)) {
                return JmmParserResult.newError(new Report(ReportType.WARNING, Stage.SYNTATIC, -1,
                        "JmmNode interface not yet implemented, returning null root node"));
            }
            includeLocation((BaseNode) parser.rootNode());
            return new JmmParserResult((JmmNode) root, Collections.emptyList(), config);

        } catch (ParseException e) {
            Token t = e.getToken();
            int line = t.getBeginLine();
            int column = t.getBeginColumn();
            String message = e.getMessage();
            Report report = Report.newError(Stage.SYNTATIC, line, column, message, e);
            return JmmParserResult.newError(report);
        }
    }
    public JmmParserResult parse(String jmmCode, String startingRule, Map<String, String> config) {

        try {

            JmmGrammarParser parser = new JmmGrammarParser(SpecsIo.toInputStream(jmmCode));

            SpecsSystem.invoke(parser, startingRule);

            var rootNode = parser.rootNode();

            if (rootNode == null) {
                throw new ParseException(parser, "Parsing problems, root is null");
            }

            if (!(rootNode instanceof JmmNode)) {
                return JmmParserResult.newError(new Report(ReportType.WARNING, Stage.SYNTATIC, -1,
                        "JmmNode interface not yet implemented, returning null root node"));
            }

            var root = ((JmmNode) rootNode).sanitize();
            System.out.println(root.toTree());


            return new JmmParserResult((JmmNode) root, Collections.emptyList(), config);

        } catch (ParseException e) {
            Token t = e.getToken();
            int line = t.getBeginLine();
            int column = t.getBeginColumn();
            String message = e.getMessage();
            Report report = Report.newError(Stage.SYNTATIC, line, column, message, e);
            return JmmParserResult.newError(report);
        }
    }
}
