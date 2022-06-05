public class Test extends java.lang.Object
{
    public Test() {
        super();
        return;
    }
    public static void main(String[] args) {

    }

    public boolean foo()
    {
        int a = 5;
        int b = 6;
        boolean c = a < b;

        if(c)
        {
            a = 1;
        }
        else
        {
            a = 2;
            b = 4;
        }
        return c;
    }
}