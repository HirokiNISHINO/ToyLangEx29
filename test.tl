def int fib(int n)
{
	if (n < 2){
		return 1;
	}
	
	return fib(n - 2) + fib(n - 1);
}

local int a;
a = 0;
while(a < 10){
	print(fib(a));
	a = a + 1;
}
11;
return;