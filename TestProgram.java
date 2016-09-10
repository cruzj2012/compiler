class TestProgram{
    public static void main(String[] a){
	System.out.println(new Test().ComputeTest(7));
    }
}

class Test {
    int[] n1 ;
    int n2;
    public int ComputeTest(int num){
        int n;
        n = num;
        n1 = new int[num];
        n1[0] = 11 - 1;
        n1[1] = 12  + 4;
        n1[2] = 13 * 2;
        n1[3] = 14 + 3;
        n1[4] = 15 - 3;
        n1[5] = 16 + 1;
        n1[6] = 17 - 2;
        
        
        while(0 < n){
            n = n-1;
            n2 = n1[n];
            System.out.println(n2);
        }
        
        if (n < 1){
            System.out.println(n + 1);
        } else {
            System.out.println(n+ 2);
        }
	return n;
    }

}
