class Abc {
    public void time_method(int x) {
        int a = 10 + x;

        int b = System.currentTimeMillis();
        int c = b + a;

        while (b < c) {
            Thread.sleep(1000);
            b = System.currentTimeMillis();
        }
    }
}