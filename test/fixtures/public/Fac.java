class Fac {
    public int compFac(int num)
    {
        int num_aux;
        if(num < 1){
            num_aux = 1;
        }
        else{
            int aux1;
            aux1 = num - 1;
            int aux2;
            aux2 = this.compFac(aux1);
            num_aux = num * aux2;
        }
        return num_aux;
    }
    public static void main(String[] args) {

        Fac aux1;
        aux1 = new Fac();
        int aux2;
        aux2 = aux1.compFac(10);
        //io.println(aux2);

    }

}