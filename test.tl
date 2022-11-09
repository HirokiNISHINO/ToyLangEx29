def int fib(int n)
{
	print("n:", n);
	return 1;
}

local int a;
a = 0;
while(a < 10){
	fib(a);
	a = a + 1;
}