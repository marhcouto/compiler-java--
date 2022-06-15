./comp2022-9a test/fixtures/public/TicTacToe.jmm
java -jar libs/jasmin.jar TicTacToe.j
cp libs-jmm/compiled/BoardBase.class ./BoardBase.class
java TicTacToe
rm TicTacToe.jmm
rm TicTacToe.j
rm BoardBase.class